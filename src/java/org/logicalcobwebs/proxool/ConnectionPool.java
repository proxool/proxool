/*
 * This software is released under the Apache Software Licence. See
 * package.html for details. The latest version is available at
 * http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Vector;

/**
 * This is where most things happen. (In fact, probably too many things happen in this one
 * class).
 * @version $Revision: 1.27 $, $Date: 2002/12/17 16:52:51 $
 * @author billhorsman
 * @author $Author: billhorsman $ (current maintainer)
 */
class ConnectionPool implements ConnectionPoolStatisticsIF {

    private Log log;

    private ReloadMonitor reloadMonitor;

    private static final String[] STATUS_DESCRIPTIONS = {"NULL", "AVAILABLE", "ACTIVE", "OFFLINE"};

    private static final String MSG_MAX_CONNECTION_COUNT =
            "Couldn't get connection because we are at maximum connection count and there are none available";

    /** This is the pool itself */
    private Vector proxyConnections = new Vector();

    /** This allows us to have a unique ID for each connection */
    private long nextConnectionId = 0;

    /** This is the "round robin" that makes sure we use all the connections */
    private int nextAvailableConnection = 0;

    /**
     * This is usually the same as poolableConnections.size() but it sometimes higher.  It is
     * alwasy right, since a connection exists before it is added to the pool
     */
    private int connectionCount = 0;

    private long connectionsServedCount = 0;

    private long connectionsRefusedCount = 0;

    /**
     * This is only incremented when a connection is connected.  In other words, connectionCount is
     * incremented when you start to make the connectiona and connectedConnectionCount is incremented
     * after you get it.  The difference between the two gives you an indication of how  many connections
     * are being made right now.
     */
    private int connectedConnectionCount = 0;

    /** This keeps a count of how many connections there are in each state */
    private int[] connectionCountByState = new int[4];

    /** Should be useful during finalize to clean up nicely but not sure if this ever gets called */
    private boolean connectionPoolUp = false;

    private ConnectionPoolDefinition definition;

    private Thread houseKeepingThread;

    private Prototyper prototypingThread;

    private DecimalFormat countFormat = new DecimalFormat("00");

    private DecimalFormat idFormat = new DecimalFormat("0000");

    private DecimalFormat bigCountFormat = new DecimalFormat("###000000");

    private ConnectionListenerIF connectionListener;

    private StateListenerIF stateListener;

    private boolean lastCreateWasSuccessful = true;

    private long timeMillisOfLastRefusal = 0;

    private int upState;

    private int recentlyStartedActiveConnectionCount;

    private static boolean loggedLegend;

    /**
     * Initialised in {@link ConnectionPool#ConnectionPool constructor}.
     */
    private ConnectionResetter connectionResetter;

    protected ConnectionPool(ConnectionPoolDefinition definition) {
        reloadMonitor = new ReloadMonitor(definition.getName());
        log = LogFactory.getLog("org.logicalcobwebs.proxool." + definition.getName());
        connectionResetter = new ConnectionResetter(log, definition.getDriver());
        setDefinition(definition);
        connectionPoolUp = true;
    }

    /** Starts up house keeping and prototyper threads. */
    protected void start() {
        pokeHouseKeeper();
        pokePrototyper();
    }

    private void pokeHouseKeeper() {
        if (houseKeepingThread == null) {
            houseKeepingThread = new Thread(new HouseKeeper());
            houseKeepingThread.setDaemon(true);
            houseKeepingThread.setName("HouseKeeper");
            houseKeepingThread.start();
        }
    }

    private void pokePrototyper() {
        if (prototypingThread == null) {
            prototypingThread = new Prototyper();
            prototypingThread.setDaemon(true);
            prototypingThread.setName("Prototyper");
            prototypingThread.start();
        } else {
            prototypingThread.wake();
        }
    }

    /**
     * Get a connection from the pool.  If none are available or there was an Exception
     * then an exception is thrown and something written to the log
     */
    protected Connection getConnection() throws SQLException {

        String requester = Thread.currentThread().getName();

        /* If we're busy, we need to return as quickly as possible. */

        if (connectionCount >= getDefinition().getMaximumConnectionCount() && getAvailableConnectionCount() < 1) {
            connectionsRefusedCount++;
            log.info(displayStatistics() + " - " + MSG_MAX_CONNECTION_COUNT);
            timeMillisOfLastRefusal = System.currentTimeMillis();
            calculateUpState();
            throwSQLException(MSG_MAX_CONNECTION_COUNT);
        }

        pokePrototyper();
        ProxyConnection proxyConnection = null;
        try {
            if (connectionCount - connectedConnectionCount > getDefinition().getMaximumNewConnections()) {
                throwSQLException("Already making " + (connectionCount - connectedConnectionCount)
                        + " connections - trying to avoid overload");
            } else {
                // We need to look at all the connections, but we don't want to keep looping round forever
                for (int connectionsTried = 0; connectionsTried < proxyConnections.size(); connectionsTried++) {
                    // By doing this in a try/catch we avoid needing to synch on the size().  We need to do be
                    // able to cope with connections being removed whilst we are going round this loop
                    try {
                        proxyConnection = (ProxyConnection) proxyConnections.elementAt(nextAvailableConnection);
                    } catch (ArrayIndexOutOfBoundsException e) {
                        nextAvailableConnection = 0;
                        proxyConnection = (ProxyConnection) proxyConnections.elementAt(nextAvailableConnection);
                    }
                    // setActive() returns false if the ProxyConnection wasn't available.  You
                    // can't set it active twice (at least, not without making it available again
                    // in between)
                    if (proxyConnection != null && proxyConnection.fromAvailableToActive()) {
                        nextAvailableConnection++;
                        break;
                    } else {
                        proxyConnection = null;
                    }
                    nextAvailableConnection++;
                }
                // Did we get one?
                if (proxyConnection == null) {
                    try {
                        // No!  Let's see if we can create one
                        proxyConnection = createPoolableConnection(ProxyConnection.STATUS_ACTIVE, "on demand");
                        addPoolableConnection(proxyConnection);
                    } catch (Exception e) {
                        log.error("Couldn't get connection", e);
                        throwSQLException(e.toString());
                    }
                }

            }
        } catch (SQLException e) {
            if (log.isDebugEnabled()) {
                log.debug("Problem getting connection", e);
            }
            calculateUpState();
            throw e;
        } catch (Throwable t) {
            log.error("Problem getting connection", t);
            calculateUpState();
            throwSQLException(t.toString());
        } finally {
            if (proxyConnection != null) {
                connectionsServedCount++;
                proxyConnection.setRequester(requester);
                calculateUpState();
            } else {
                connectionsRefusedCount++;
                timeMillisOfLastRefusal = System.currentTimeMillis();
                calculateUpState();
            }
        }

        if (proxyConnection == null) {
            throwSQLException("Unknown reason for not getting connection. Sorry.");
        }

        if (log.isDebugEnabled() && getDefinition().getDebugLevel() > ConnectionPoolDefinitionIF.DEBUG_LEVEL_QUIET) {
            log.debug(displayStatistics() + " - Connection #" + proxyConnection.getId() + " served");
        }

        return ProxyFactory.getConnection(proxyConnection);
    }

    private void throwSQLException(String message) throws SQLException {
        throw new SQLException(message + " [stats: " + displayStatistics() + "]");
    }

    private ProxyConnection createPoolableConnection(int state, String creator) throws SQLException {
        // Synch here rather than whole method because if is the new ProxyConnection() after
        // the synch that will take the time
        // It would be simpler to synch the whole thing from when we create the connection to adding it to the
        // pool but this would cause a bottleneck because only one connection could be created at a time.  It
        // complicates things doing it this way but I think it's worth it.
        synchronized (this) {
            // Check that we are allowed to make another connection
            if (connectionCount >= getDefinition().getMaximumConnectionCount()) {
                throwSQLException("ConnectionCount is " + connectionCount + ". Maximum connection count of "
                        + getDefinition().getMaximumConnectionCount() + " cannot be exceeded.");
            }
            connectionCount++;

            // Sanity check. Note that we can't check in much more detail. There might be other
            // connections being made right now. They will each increment the connectionCount
            // value but increment getXxxConnectionCount() a little later.
            if (getActiveConnectionCount() + getAvailableConnectionCount() > getDefinition().getMaximumConnectionCount()) {
                log.warn(displayStatistics() + " - We have possibly made too many connections: connectionCount=" + connectionCount
                        + ", maximumConnectionCount=" + getDefinition().getMaximumConnectionCount()
                        + ". If this persists then there has been an error within Proxool.");
            }

        }

        ProxyConnection proxyConnection = null;
        Connection connection = null;

        try {
            proxyConnection = ProxyFactory.buildProxyConnection(getNextId(), this);
            connection = ProxyFactory.getConnection(proxyConnection);

            try {
                onBirth(connection);
            } catch (Exception e) {
                log.error("Problem during onBirth (ignored)", e);
            }
            switch (state) {
                case ProxyConnection.STATUS_ACTIVE:
                    proxyConnection.fromOfflineToActive();
                    break;

                case ProxyConnection.STATUS_AVAILABLE:
                    proxyConnection.fromOfflineToAvailable();
                    break;

                default:
/* Not quite sure what we should do here. oh well, leave it offline*/
                    log.error(displayStatistics() + " - Didn't expect to set new connection to state " + state);
            }

            if (proxyConnection.getStatus() != state) {
                throwSQLException("Unable to set connection #" + proxyConnection.getId() + " to "
                        + STATUS_DESCRIPTIONS[state]);

            } else {
                if (log.isDebugEnabled()) {
                    StringBuffer out = new StringBuffer(displayStatistics());
                    out.append(" - Connection #");
                    out.append(proxyConnection.getId());
                    out.append(" created ");
                    out.append(creator);
                    out.append(" = ");
                    out.append(getStatusDescription(proxyConnection.getStatus()));
                    if (getDefinition().getDebugLevel() == ConnectionPoolDefinitionIF.DEBUG_LEVEL_LOUD) {
                        out.append(" -> ");
                        out.append(getDefinition().getUrl());
                        out.append(" (");
                        out.append(Integer.toHexString(proxyConnection.getConnection().hashCode()));
                        out.append(")");
                    }
                    log.debug(out);
                }
            }
        } catch (SQLException e) {
            // log.error(displayStatistics() + " - Couldn't initialise connection #" + proxyConnection.getId() + ": " + e);
            throw e;
        } catch (RuntimeException e) {
            if (log.isDebugEnabled()) {
                log.debug(e);
            }
            throw e;
        } catch (Throwable t) {
            if (log.isDebugEnabled()) {
                log.debug(t);
            }
        } finally {

            lastCreateWasSuccessful = (proxyConnection != null);
            calculateUpState();

            if (!lastCreateWasSuccessful) {
                // If there has been an exception then we won't be using this one and
                // we need to decrement the counter
                connectionCount--;

            }

        }

        return proxyConnection;
    }

    /** Add a proxyConnection to the pool */
    private void addPoolableConnection(ProxyConnection proxyConnection) {
        proxyConnections.addElement(proxyConnection);
    }

    /**
     * It's nice to identify each Connection individually rather than just use their index
     * within the pool (which changes all the time)
     */
    private synchronized long getNextId() {
        return nextConnectionId++;
    }

    protected static String getStatusDescription(int status) {
        try {
            return STATUS_DESCRIPTIONS[status];
        } catch (ArrayIndexOutOfBoundsException e) {
            return "Unknown status: " + status;
        }
    }

    /**
     * When you have finished with a Connection you should put it back here.  That will make it available to others.
     * Unless it's due for expiry, in which case it will... expire
     */
    protected void putConnection(ProxyConnection proxyConnection) {
        try {
            // It's possible that this connection is due for expiry
            if (proxyConnection.isMarkedForExpiry()) {
                if (proxyConnection.fromActiveToNull()) {
                    expireProxyConnection(proxyConnection, REQUEST_EXPIRY);
                }
            } else {
                // Let's make it available for someone else
                proxyConnection.fromActiveToAvailable();
            }

            if (log.isDebugEnabled() && getDefinition().getDebugLevel() > ConnectionPoolDefinitionIF.DEBUG_LEVEL_QUIET) {
                log.debug(displayStatistics() + " - Connection #" + proxyConnection.getId() + " returned");
            }

        } finally {
            // This is probably due to getPoolableConnection returning a null.
            calculateUpState();
        }
    }

    /** This means that there's something wrong the connection and it's probably best if no one uses it again. */
    protected void throwConnection(ProxyConnection proxyConnection) {
        try {
            expireConnectionAsSoonAsPossible(proxyConnection, true);
        } finally {
            calculateUpState();
        }
    }

    /** Get a ProxyConnection by index */
    private ProxyConnection getProxyConnection(int i) {
        return (ProxyConnection) proxyConnections.elementAt(i);
    }

    /*
    private ProxyConnection getProxyConnection(int i) {
        return (ProxyConnection) Proxy.getInvocationHandler(getProxyConnection(i));
    }
    */

    protected void removeProxyConnection(ProxyConnection proxyConnection, String reason, boolean forceExpiry) {
        // Just check that it is null
        if (forceExpiry || proxyConnection.isNull()) {

            synchronized (this) {
                proxyConnection.setStatus(ProxyConnection.STATUS_NULL);
            }

            /* Run some code everytime we destroy a connection */

            try {
                onDeath(proxyConnection.getConnection());
            } catch (SQLException e) {
                log.error("Problem during onDeath (ignored)", e);
            }

            // The reallyClose() method also decrements the connectionCount.
            try {
                proxyConnection.reallyClose();
            } catch (SQLException e) {
                log.error(e);
            }

            proxyConnections.removeElement(proxyConnection);

            if (log.isDebugEnabled()) {
                log.debug(displayStatistics() + " - #" + idFormat.format(proxyConnection.getId())
                        + " removed because " + reason + ".");
            }
            pokePrototyper();
        } else {
            log.error(displayStatistics() + " - #" + idFormat.format(proxyConnection.getId())
                    + " was not removed because isNull() was false.");
        }
    }

    private void expireProxyConnection(ProxyConnection proxyConnection, boolean forceExpiry) {
        removeProxyConnection(proxyConnection, "age is " + proxyConnection.getAge() + "ms", forceExpiry);
        calculateUpState();
    }

    /**
     * Call this to shutdown gracefully.
     *  @param delay how long to wait for connections to become free before forcing them to close anyway
     */
    protected void finalize(int delay, String finalizerName) throws Throwable {

        /* This will stop us giving out any more connections and may
        cause some of the threads to die. */

        if (connectionPoolUp == true) {

            connectionPoolUp = false;
            long startFinalize = System.currentTimeMillis();

            if (delay > 0) {
                log.info("Closing down instance started at "
                        + reloadMonitor.getLoadDate() + " - waiting for " + delay
                        + " milliseconds for everything to stop.  [ "
                        + finalizerName + "]");
            } else {
                log.info("Closing down connection pool immediately [" + finalizerName + "]");
            }

            /* Interrupt the threads (in case they're sleeping) */

            boolean connectionClosedManually = false;
            try {
                try {
                    wakeThread(houseKeepingThread);
                } catch (Exception e) {
                    log.error("Can't wake houseKeepingThread", e);
                }

                try {
                    wakeThread(prototypingThread);
                } catch (Exception e) {
                    log.error("Can't wake prototypingThread", e);
                }

                /* Patience, patience. */

                try {
                    long sleepTime = Math.max(0, delay + startFinalize - System.currentTimeMillis());
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e1) {
                    log.debug("Interrupted whilst sleeping. Snooze.");
                    try {
                        long sleepTime = Math.max(0, delay + startFinalize - System.currentTimeMillis());
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e2) {
                        log.debug("Interrupted whilst sleeping. Snooze.");
                        try {
                            long sleepTime = Math.max(0, delay + startFinalize - System.currentTimeMillis());
                            Thread.sleep(sleepTime);
                        } catch (InterruptedException e3) {
                            log.warn("Interrupted whilst sleeping. Waking up.", e3);
                        }
                    }
                }

                // Silently close all connections
                for (int i = proxyConnections.size() - 1; i >= 0; i--) {
                    long id = getProxyConnection(i).getId();
                    try {
                        connectionClosedManually = true;
                        getProxyConnection(i).close();
                        if (log.isDebugEnabled()) {
                            log.debug("Connection #" + id + " closed manually");
                        }
                    } catch (Throwable t) {
                        if (log.isDebugEnabled()) {
                            log.debug("Problem closing connection #" + id, t);
                        }

                    }
                }
            } catch (Throwable t) {
                log.error("Unknown problem finalizing pool", t);
            } finally {
                if (log.isDebugEnabled()) {
                    log.debug("Connection pool has been closed down by " + finalizerName
                            + " in " + (System.currentTimeMillis() - startFinalize) + " milliseconds.");
                    if (!connectionClosedManually) {
                        log.debug("No connections required manual removal.");
                    }
                }
                super.finalize();
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Ignoring duplicate attempt to finalize connection pool by " + finalizerName);
            }
        }
    }

    // In theory, this gets called by the JVM.  Never actually does though.
    protected void finalize() throws Throwable {
        /* No delay! */
        finalize(0, "JVM");
    }

    private void wakeThread(Thread thread) {
        thread.interrupt();
    }

    public int getAvailableConnectionCount() {
        return connectionCountByState[ProxyConnection.STATUS_AVAILABLE];
    }

    public int getActiveConnectionCount() {
        return connectionCountByState[ProxyConnection.STATUS_ACTIVE];
    }

    public int getOfflineConnectionCount() {
        return connectionCountByState[ProxyConnection.STATUS_OFFLINE];
    }

    protected String displayStatistics() {

        if (!loggedLegend) {
            log.info("Proxool statistics legend: \"s - r  (a/t/o)\" > s=served, r=refused (only shown if non-zero), a=active, t=total, o=offline (being tested)");
            loggedLegend = true;
        }

        StringBuffer statistics = new StringBuffer();
        statistics.append(bigCountFormat.format(getConnectionsServedCount()));

        if (getConnectionsRefusedCount() > 0) {
            statistics.append(" -");
            statistics.append(bigCountFormat.format(getConnectionsRefusedCount()));
        }

        statistics.append(" (");
        statistics.append(countFormat.format(getActiveConnectionCount()));
        statistics.append("/");
        statistics.append(countFormat.format(getAvailableConnectionCount() + getActiveConnectionCount()));
        statistics.append("/");
        statistics.append(countFormat.format(getOfflineConnectionCount()));
        statistics.append(")");

        // Don't need this triple check any more.
        /*
        if (getDefinition().getDebugLevel() == ConnectionPoolDefinitionIF.DEBUG_LEVEL_LOUD) {
            statistics.append(", cc=");
            statistics.append(connectionCount);
            statistics.append(", ccc=");
            statistics.append(connectedConnectionCount);
        }
        */

        return statistics.toString();
    }

    class HouseKeeper implements Runnable {

        /** Housekeeping thread to look after all the connections */
        public void run() {
            // We just use this to see how the Connection is
            Statement testStatement = null;
            while (connectionPoolUp) {
                try {

                    try {
                        Thread.sleep(getDefinition().getHouseKeepingSleepTime());
                    } catch (InterruptedException e) {
                        return;
                    }

                    if (reloadMonitor.isProxoolReloaded()) {
                        return;
                    }

                    // Right, now we know we're the right thread then we can carry on house keeping
                    Connection connection = null;
                    ProxyConnection proxyConnection = null;

                    int recentlyStartedActiveConnectionCountTemp = 0;

                    // Loop through backwards so that we can cope with connections being removed
                    // as we go along.  If an element is removed (and it is was one haven't reached
                    // yet) the only risk is that we test the same connection twice (which doesn't
                    // matter at all).  If an element is added then it is added to the end and we
                    // will miss it on this house keeping run (but we'll get it next time).
                    for (int i = proxyConnections.size() - 1; i >= 0; i--) {
                        proxyConnection = getProxyConnection(i);
                        connection = proxyConnection.getConnection();
                        // First lets check whether the connection still works. We should only validate
                        // connections that are not is use!  SetOffline only succeeds if the connection
                        // is available.
                        if (proxyConnection.fromAvailableToOffline()) {
                            try {
                                testStatement = connection.createStatement();

                                // Some DBs return an object even if DB is shut down
                                if (proxyConnection.isReallyClosed()) {
                                    proxyConnection.fromOfflineToNull();
                                    removeProxyConnection(proxyConnection, "it appears to be closed", FORCE_EXPIRY);
                                }

                                String sql = getDefinition().getHouseKeepingTestSql();
                                if (sql != null && sql.length() > 0) {
                                    // A Test Statement has been provided. Execute it!
                                    boolean testResult = false;
                                    try {
                                        testResult = testStatement.execute(sql);
                                    } finally {
                                        if (log.isDebugEnabled() && getDefinition().getDebugLevel() == ConnectionPoolDefinitionIF.DEBUG_LEVEL_LOUD) {
                                            log.debug(displayStatistics() + " - Testing connection " + proxyConnection.getId() + (testResult ? ": OK" : ": FAIL"));
                                        }
                                    }
                                }

                                proxyConnection.fromOfflineToAvailable();
                            } catch (SQLException e) {
                                // There is a problem with this connection.  Let's remove it!
                                proxyConnection.fromOfflineToNull();
                                removeProxyConnection(proxyConnection, "it has problems: " + e, REQUEST_EXPIRY);
                            } finally {
                                try {
                                    testStatement.close();
                                } catch (Throwable t) {
                                    // Never mind.
                                }
                            }
                        } // END if (poolableConnection.setOffline())
                        // Now to check whether the connection is due for expiry
                        if (proxyConnection.getAge() > getDefinition().getMaximumConnectionLifetime()) {
                            // Check whether we can make it offline
                            if (proxyConnection.fromAvailableToOffline()) {
                                if (proxyConnection.fromOfflineToNull()) {
                                    // It is.  Expire it now .
                                    expireProxyConnection(proxyConnection, REQUEST_EXPIRY);
                                }
                            } else {
                                // Oh no, it's in use.  Never mind, we'll mark it for expiry
                                // next time it is available.  This will happen in the
                                // putConnection() method.
                                proxyConnection.markForExpiry();
                                if (log.isDebugEnabled()) {
                                    log.debug(displayStatistics() + " - #" + idFormat.format(proxyConnection.getId())
                                            + " marked for expiry.");
                                }
                            } // END if (poolableConnection.setOffline())
                        } // END if (poolableConnection.getAge() > maximumConnectionLifetime)

                        // Now let's see if this connection has been active for a
                        // suspiciously long time.
                        if (proxyConnection.isActive()) {

                            long activeTime = System.currentTimeMillis() - proxyConnection.getTimeLastStartActive();

                            if (activeTime < getDefinition().getRecentlyStartedThreshold()) {

                                // This connection hasn't been active for all that long
                                // after all. And as long as we have at least one
                                // connection that is "actively active" then we don't
                                // consider the pool to be down.
                                recentlyStartedActiveConnectionCountTemp++;
                            }

                            if (activeTime > getDefinition().getMaximumActiveTime()) {

                                // This connection has been active for way too long. We're
                                // going to kill it :)
                                removeProxyConnection(proxyConnection,
                                        "it has been active for too long", FORCE_EXPIRY);

                                log.warn("#" + idFormat.format(proxyConnection.getId()) + " was active for " + activeTime
                                        + " milliseconds and has been removed automaticaly. The Thread responsible was named '"
                                        + proxyConnection.getRequester() + "'.");

                            }
                        }

                    }

                    setRecentlyStartedActiveConnectionCount(recentlyStartedActiveConnectionCountTemp);

                    pokePrototyper();

                    calculateUpState();
                } catch (RuntimeException e) {
                    // We don't want the housekeeping thread to fall over!
                    log.error("Housekeeping log.error( :", e);
                } finally {
                    if (getDefinition().getDebugLevel() > getDefinition().DEBUG_LEVEL_QUIET) {
                        if (log.isDebugEnabled()) {
                            log.debug(displayStatistics() + " - House keeping sweep done");
                        }
                    }
                }
            } // END while (connectionPoolUp)
        }
    }

    /**
     * If you start this up as a Thread it will try and make sure there are some
     * free connections available in case you need them.  Because we are now a bit more rigorous in
     * destroying unwanted connections we want to make sure that we don't create them on demand to
     * often.  Creating on demand makes the user wait (and we don't want that do we?)
     */
    class Prototyper extends Thread {

        public void run() {

            while (connectionPoolUp) {

                boolean keepAtIt = true;
                boolean somethingDone = false;
                while (connectionPoolUp && keepAtIt) {

                    String reason = null;
                    if (connectionCount >= getDefinition().getMaximumConnectionCount()) {
                        // We don't want to make any more that the maximum
                        break;
                    } else if (connectionCount < getDefinition().getMinimumConnectionCount()) {
                        reason = "to achieve minimum of " + getDefinition().getMinimumConnectionCount();
                    } else if (getAvailableConnectionCount() < getDefinition().getPrototypeCount()) {
                        reason = "to keep " + getDefinition().getPrototypeCount() + " available";
                    } else {
                        // Nothing to do
                        break;
                    }

                    try {
                        ProxyConnection poolableConnection = createPoolableConnection(ProxyConnection.STATUS_AVAILABLE, reason);
                        addPoolableConnection(poolableConnection);
                        somethingDone = true;
                    } catch (Exception e) {
                        log.error("Prototype", e);
                        // If there's been an exception, perhaps we should stop
                        // prototyping for a while.  Otherwise if the database
                        // has problems we end up trying the connection every 2ms
                        // or so and then the log grows pretty fast.
                        keepAtIt = false;
                        // Don't wory, we'll start again the next time the
                        // housekeeping thread runs.
                    }
                }

                if (log.isDebugEnabled()
                        && getDefinition().getDebugLevel() > getDefinition().DEBUG_LEVEL_QUIET
                        && !somethingDone) {
                    log.debug(displayStatistics() + " - Prototyper didn't need to do anything");
                }

                try {
                    synchronized (this) {
                        this.wait();
                    }
                } catch (InterruptedException e) {
                    //
                }
            }
        }

        protected synchronized void wake() {
            this.notify();
        }
    }

    protected void expireAllConnections(boolean merciful) {
        for (int i = proxyConnections.size() - 1; i >= 0; i--) {
            expireConnectionAsSoonAsPossible((ProxyConnection) proxyConnections.elementAt(i), merciful);
        }
    }

    protected void expireConnectionAsSoonAsPossible(ProxyConnection proxyConnection, boolean merciful) {
        if (proxyConnection.fromAvailableToOffline()) {
            if (proxyConnection.fromOfflineToNull()) {
                // It is.  Expire it now .
                expireProxyConnection(proxyConnection, REQUEST_EXPIRY);
            }
        } else {
            // Oh no, it's in use.

            if (merciful) {
                //Never mind, we'll mark it for expiry
                // next time it is available.  This will happen in the
                // putConnection() method.
                proxyConnection.markForExpiry();
                if (log.isDebugEnabled()) {
                    log.debug(displayStatistics() + " - #" + idFormat.format(proxyConnection.getId()) + " marked for expiry.");
                }
            } else {
                // So? Kill, kill, kill

                // We have to make sure it's null first.
                expireProxyConnection(proxyConnection, FORCE_EXPIRY);
            }

        } // END if (proxyConnection.setOffline())
    }

    protected void registerRemovedConnection(int status) {
        if (log.isDebugEnabled()) {
            log.debug("Decrementing connectionCount, connectedConnectionCount and connectionCountByState[" + status + "]");
        }

        connectionCount--;
        connectedConnectionCount--;
        connectionCountByState[status]--;
    }

    protected void changeStatus(int oldStatus, int newStatus) {
        connectionCountByState[oldStatus]--;
        connectionCountByState[newStatus]++;
    }

    protected void incrementConnectedConnectionCount() {
        connectedConnectionCount++;
    }

    public long getConnectionsServedCount() {
        return connectionsServedCount;
    }

    public long getConnectionsRefusedCount() {
        return connectionsRefusedCount;
    }

    protected ConnectionPoolDefinition getDefinition() {
        return definition;
    }

    /**
     * Changes both the way that any new connections will be made, and the behaviour of the pool. Consider
     * calling expireAllConnections() if you're in a hurry.
     */
    protected synchronized void setDefinition(ConnectionPoolDefinition definition) {
        this.definition = definition;

        try {
            Class.forName(definition.getDriver());
        } catch (ClassNotFoundException e) {
            log.error(e);
        }

    }

    public void setStateListener(StateListenerIF stateListener) {
        this.stateListener = stateListener;
    }

    public void setConnectionListener(ConnectionListenerIF connectionListener) {
        this.connectionListener = connectionListener;
    }

    /** Call the onBirth() method on each StateListenerIF . */
    private void onBirth(Connection connection) throws SQLException {
        if (connectionListener != null) {
            connectionListener.onBirth(connection);
        }
    }

    /** Call the onDeath() method on each StateListenerIF . */
    protected void onDeath(Connection connection) throws SQLException {
        if (connectionListener != null) {
            connectionListener.onDeath(connection);
        }
    }

    /** Call the onExecute() method on each StateListenerIF . */
    protected void onExecute(String command, long elapsedTime, Exception exception) throws SQLException {
        if (connectionListener != null) {
            if (exception == null) {
                connectionListener.onExecute(command, elapsedTime);
            } else {
                connectionListener.onFail(command, exception);
            }

        }
    }

    /**
     * Is there a {@link ConnectionListenerIF listener} for connections
     * @return true if there is a listener registered.
     */
    protected boolean isConnectionListenedTo() {
        return (connectionListener != null);
    }

    public String toString() {
        return getDefinition().toString();
    }

    public int getUpState() {
        return upState;
    }

    private void calculateUpState() {

        try {

            int calculatedUpState = StateListenerIF.STATE_QUIET;

/* We're up if the last time we tried to make a connection it
             * was successful
             */

/* I've changed the way we do this. Just because we failed to create
             * a connection doesn't mean we're down. As long as we have some
             * available connections, or the active ones we have aren't locked
             * up then we should be able to struggle on. The last thing we want
             * to do is say we're down when we're not!
             */

            // if (this.lastCreateWasSuccessful) {
            if (getAvailableConnectionCount() > 0 || getRecentlyStartedActiveConnectionCount() > 0) {

/* Defintion of overloaded is that we refused a connection
                 * (because we were too busy) within the last minute.
                 */

                if (this.timeMillisOfLastRefusal > (System.currentTimeMillis()
                        - getDefinition().getOverloadWithoutRefusalLifetime())) {
                    calculatedUpState = StateListenerIF.STATE_OVERLOADED;
                } else if (getActiveConnectionCount() > 0) {
                    /* Are we doing anything at all?
                 */
                    calculatedUpState = StateListenerIF.STATE_BUSY;
                }

            } else {
                calculatedUpState = StateListenerIF.STATE_DOWN;
            }

            setUpState(calculatedUpState);

        } catch (Exception e) {
            log.error(e);
        }
    }

    public void setUpState(int upState) {

        if (this.upState != upState) {

            if (stateListener != null) {
                stateListener.upStateChanged(upState);
            }

            this.upState = upState;

        }
    }

    public Collection getConnectionInfos() {
        return proxyConnections;
    }

    public int getRecentlyStartedActiveConnectionCount() {
        return recentlyStartedActiveConnectionCount;
    }

    public void setRecentlyStartedActiveConnectionCount(int recentlyStartedActiveConnectionCount) {
        this.recentlyStartedActiveConnectionCount = recentlyStartedActiveConnectionCount;
    }

    /**
     * Manually expire a connection.
     * @param id the id of the connection to kill
     * @param forceExpiry use true to expire even if it is in use
     * @return true if the connection was found and expired, else false
     */
    public boolean expireConnection(long id, boolean forceExpiry) {
        boolean success = false;
        ProxyConnection proxyConnection = null;

        // We need to look at all the connections, but we don't want to keep looping round forever
        for (int connectionsTried = 0; connectionsTried < proxyConnections.size(); connectionsTried++) {
            // By doing this in a try/catch we avoid needing to synch on the size().  We need to do be
            // able to cope with connections being removed whilst we are going round this loop
            try {
                proxyConnection = (ProxyConnection) proxyConnections.elementAt(nextAvailableConnection);
            } catch (ArrayIndexOutOfBoundsException e) {
                nextAvailableConnection = 0;
                proxyConnection = (ProxyConnection) proxyConnections.elementAt(nextAvailableConnection);
            }

            if (proxyConnection.getId() == id) {
                // This is the one
                proxyConnection.fromAvailableToOffline();
                proxyConnection.fromOfflineToNull();
                removeProxyConnection(proxyConnection, "it was manually killed", forceExpiry);
                calculateUpState();
                success = true;
                break;
            }

            nextAvailableConnection++;
        }

        if (!success) {
            if (log.isDebugEnabled()) {
                log.debug(displayStatistics() + " - couldn't find " + idFormat.format(proxyConnection.getId())
                        + " and I've just been asked to expire it");
            }
        }

        return success;
    }

    public Log getLog() {
        return log;
    }

    /**
     * {@link ConnectionResetter#initialise Initialises} the ConnectionResetter.
     * @param connection sample Connection to use for default values
     */
    protected void initialiseConnectionResetter(Connection connection) {
        connectionResetter.initialise(connection);
    }

    /**
     * {@link ConnectionResetter#reset Resets} a Connection to its
     * original state.
     * @param connection the one to reset
     */
    protected boolean resetConnection(Connection connection, String id) {
        return connectionResetter.reset(connection, id);
    }

    private static final boolean FORCE_EXPIRY = true;

    private static final boolean REQUEST_EXPIRY = false;

}

/*
 Revision history:
 $Log: ConnectionPool.java,v $
 Revision 1.27  2002/12/17 16:52:51  billhorsman
 synchronize part of removeProxyConnection to avoid
 possible bug where connection by status count drifts.

 Revision 1.26  2002/12/12 12:28:34  billhorsman
 just in case: changed == 0 to < 1

 Revision 1.25  2002/12/12 10:48:47  billhorsman
 checkstyle

 Revision 1.24  2002/12/03 12:24:00  billhorsman
 fixed fatal sql exception

 Revision 1.23  2002/11/12 20:24:12  billhorsman
 checkstyle

 Revision 1.22  2002/11/12 20:18:23  billhorsman
 Made connection resetter a bit more friendly. Now, if it encounters any problems during
 reset then that connection is thrown away. This is going to cause you problems if you
 always close connections in an unstable state (e.g. with transactions open. But then
 again, it's better to know about that as soon as possible, right?

 Revision 1.21  2002/11/09 15:48:55  billhorsman
 new isConnectionListenedTo() to stop unnecessary processing
 if nobody is listening

 Revision 1.20  2002/11/08 18:03:50  billhorsman
 when connections are closed because they have been
 active for too long then a log message is written
 of level WARN, and it includes the name of the
 thread responsible (reminder, name your threads).

 Revision 1.19  2002/11/07 19:31:25  billhorsman
 added sanity check against suspected situation where you
 can make more connections than the maximumConnectionCount

 Revision 1.18  2002/11/07 19:17:55  billhorsman
 removed obsolete method

 Revision 1.17  2002/11/06 20:27:30  billhorsman
 supports the ConnectionResetter

 Revision 1.16  2002/11/05 21:24:18  billhorsman
 cosmetic: changed format of statistics dumped to log to make it less confusing for locales that use a space separator for thousands

 Revision 1.15  2002/11/02 13:57:33  billhorsman
 checkstyle

 Revision 1.14  2002/10/30 21:19:17  billhorsman
 make use of ProxyFactory

 Revision 1.13  2002/10/29 23:00:33  billhorsman
 fixed sign error in prototyper (that meant that protoyping never happened)

 Revision 1.12  2002/10/29 22:58:22  billhorsman
 added connection hashcode to debug

 Revision 1.11  2002/10/28 19:44:03  billhorsman
 small change to cleanup log

 Revision 1.10  2002/10/27 13:02:45  billhorsman
 change to prototyper logic to make it clearer (but no functional change)

 Revision 1.9  2002/10/27 12:07:45  billhorsman
 fix bug where prototyper kept making connections up to maximum. Log now gives reason why connection was prototyped. Fix bug where definition with no properties was not allowed (it is now).

 Revision 1.8  2002/10/25 10:12:52  billhorsman
 Improvements and fixes to the way connection pools close down. Including new ReloadMonitor to detect when a class is reloaded. Much better logging too.

 Revision 1.7  2002/10/24 17:25:20  billhorsman
 cleaned up logging and made it more informative

 Revision 1.6  2002/10/23 21:04:36  billhorsman
 checkstyle fixes (reduced max line width and lenient naming convention

 Revision 1.5  2002/10/16 11:46:23  billhorsman
 removed obsolete cleanupClob method and made onBirth call failsafe

 Revision 1.4  2002/09/19 10:33:57  billhorsman
 added ProxyConnection#toString

 Revision 1.3  2002/09/18 13:48:56  billhorsman
 checkstyle and doc

 Revision 1.2  2002/09/13 12:13:50  billhorsman
 added debug and fixed ClassCastException during housekeeping

 Revision 1.1.1.1  2002/09/13 08:12:55  billhorsman
 new

 Revision 1.13  2002/08/24 19:57:15  billhorsman
 checkstyle changes

 Revision 1.12  2002/08/24 19:44:13  billhorsman
 fixes for logging

 Revision 1.11  2002/07/10 16:14:47  billhorsman
 widespread layout changes and move constants into ProxoolConstants

 Revision 1.10  2002/07/04 09:05:36  billhorsmaNn
 Fixes

 Revision 1.9  2002/07/02 11:19:08  billhorsman
 layout code and imports

 Revision 1.8  2002/07/02 11:14:26  billhorsman
 added test (andbug fixes) for FileLogger

 Revision 1.7  2002/07/02 08:39:55  billhorsman
 getConnectionInfos now returns a Collection instead of an array. displayStatistics is
 now available to ProxoolFacade. Prototyper no longer tries to make connections
 when maximum is reached (stopping unnecessary log messages). bug fix.

 Revision 1.6  2002/06/28 11:19:47  billhorsman
 improved doc

*/
