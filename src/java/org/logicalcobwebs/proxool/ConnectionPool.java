/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import org.logicalcobwebs.logging.Log;
import org.logicalcobwebs.logging.LogFactory;
import org.logicalcobwebs.proxool.admin.Admin;
import org.logicalcobwebs.proxool.util.FastArrayList;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

/**
 * This is where most things happen. (In fact, probably too many things happen in this one
 * class).
 * @version $Revision: 1.58 $, $Date: 2003/03/05 18:42:32 $
 * @author billhorsman
 * @author $Author: billhorsman $ (current maintainer)
 */
class ConnectionPool implements ConnectionPoolStatisticsIF {

    /**
     * Here we deviate from the standard of using the classname for the log
     * name. Here we want to use the alias for the pool so that we can log
     * each pool to different places. So we have to instantiate the log later.
     */
    private Log log;

    private static final String[] STATUS_DESCRIPTIONS = {"NULL", "AVAILABLE", "ACTIVE", "OFFLINE"};

    private static final String MSG_MAX_CONNECTION_COUNT =
            "Couldn't get connection because we are at maximum connection count and there are none available";

    /** This is the pool itself */
    private List proxyConnections;

    /** This is the "round robin" that makes sure we use all the connections */
    private int nextAvailableConnection = 0;

    /**
     * This is usually the same as poolableConnections.size() but it sometimes higher.  It is
     * alwasy right, since a connection exists before it is added to the pool
     */
    private int connectionCount = 0;

    private long connectionsServedCount = 0;

    private long connectionsRefusedCount = 0;

    /** This keeps a count of how many connections there are in each state */
    private int[] connectionCountByState = new int[4];

    private ConnectionPoolDefinition definition;

    private CompositeConnectionListener compositeConnectionListener = new CompositeConnectionListener();

    private CompositeStateListener compositeStateListener = new CompositeStateListener();

    private long timeOfLastRefusal = 0;

    private int upState;

    private static boolean loggedLegend;

    private Admin admin;

    private boolean locked = false;

    private Date dateStarted = new Date();

    private boolean connectionPoolUp = true;

    /**
     * Initialised in {@link ConnectionPool#ConnectionPool constructor}.
     */
    private ConnectionResetter connectionResetter;

    protected ConnectionPool(ConnectionPoolDefinition definition) {

        // Use the FastArrayList for performance and thread safe
        // behaviour. We set its behaviour to "fast"  (meaning reads are
        // unsynchronized, whilst writes are not).
        FastArrayList fal = new FastArrayList();
        fal.setFast(true);
        proxyConnections = fal;

        log = LogFactory.getLog("org.logicalcobwebs.proxool." + definition.getAlias());
        connectionResetter = new ConnectionResetter(log, definition.getDriver());
        setDefinition(definition);

        if (definition.getStatistics() != null) {
            try {
                admin = new Admin(definition);
            } catch (ProxoolException e) {
                log.error("Failed to initialise statistics", e);
            }
        }

        ShutdownHook.init();
    }

    /** Starts up house keeping and prototyper threads. */
    protected void start() throws ProxoolException {
        PrototyperController.register(this);
        HouseKeeperController.register(this);
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
            if (admin != null) {
                admin.connectionRefused();
            }
            log.info(displayStatistics() + " - " + MSG_MAX_CONNECTION_COUNT);
            timeOfLastRefusal = System.currentTimeMillis();
            setUpState(StateListenerIF.STATE_OVERLOADED);
            throw new SQLException(MSG_MAX_CONNECTION_COUNT);
        }

        try {
            PrototyperController.checkSimultaneousBuildThrottle(getDefinition().getAlias());
        } catch (ProxoolException e) {
            log.error("Unexpected problem", e);
            throw new SQLException(e.getMessage());
        }

        PrototyperController.triggerSweep(getDefinition().getAlias());
        ProxyConnectionIF proxyConnection = null;

        try {

            // We need to look at all the connections, but we don't want to keep looping round forever
            for (int connectionsTried = 0; connectionsTried < proxyConnections.size(); connectionsTried++) {
                // By doing this in a try/catch we avoid needing to synch on the size().  We need to do be
                // able to cope with connections being removed whilst we are going round this loop
                try {
                    proxyConnection = (ProxyConnectionIF) proxyConnections.get(nextAvailableConnection);
                } catch (ArrayIndexOutOfBoundsException e) {
                    // This is thrown by a Vector (which we no longer use), but is
                    // kept here for a while.
                    nextAvailableConnection = 0;
                    proxyConnection = (ProxyConnectionIF) proxyConnections.get(nextAvailableConnection);
                } catch (IndexOutOfBoundsException e) {
                    // This is thrown by a true List
                    nextAvailableConnection = 0;
                    proxyConnection = (ProxyConnectionIF) proxyConnections.get(nextAvailableConnection);
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
                    proxyConnection = PrototyperController.buildConnection(
                            getDefinition().getAlias(), ProxyConnection.STATUS_ACTIVE, "on demand");
                } catch (ProxoolException e) {
                    log.debug("Couldn't get connection", e);
                    throw new SQLException(e.toString());
                } catch (Exception e) {
                    log.error("Couldn't get connection", e);
                    throw new SQLException(e.toString());
                }
            }

        } catch (SQLException e) {
            if (log.isDebugEnabled()) {
                log.debug("Problem getting connection", e);
            }
            throw e;
        } catch (Throwable t) {
            log.error("Problem getting connection", t);
            throw new SQLException(t.toString());
        } finally {
            if (proxyConnection != null) {
                connectionsServedCount++;
                proxyConnection.setRequester(requester);
            } else {
                connectionsRefusedCount++;
                if (admin != null) {
                    admin.connectionRefused();
                }
                timeOfLastRefusal = System.currentTimeMillis();
                setUpState(StateListenerIF.STATE_OVERLOADED);
            }
        }

        if (proxyConnection == null) {
            throw new SQLException("Unknown reason for not getting connection. Sorry.");
        }

        if (log.isDebugEnabled() && getDefinition().isVerbose()) {
            log.debug(displayStatistics() + " - Connection #" + proxyConnection.getId() + " served");
        }

        return ProxyFactory.getConnection(proxyConnection);
    }

    /**
     * Add a ProxyConnection to the pool
     * @param proxyConnection new connection
     */
    protected void addProxyConnection(ProxyConnectionIF proxyConnection) {
        proxyConnections.add(proxyConnection);
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
    protected void putConnection(ProxyConnectionIF proxyConnection) {

        if (admin != null) {
            admin.connectionReturned(System.currentTimeMillis() - proxyConnection.getTimeLastStartActive());
        }

        // It's possible that this connection is due for expiry
        if (proxyConnection.isMarkedForExpiry()) {
            if (proxyConnection.fromActiveToNull()) {
                expireProxyConnection(proxyConnection, proxyConnection.getReasonForMark(), REQUEST_EXPIRY);
            }
        } else {
            // Let's make it available for someone else
            proxyConnection.fromActiveToAvailable();
        }

        if (log.isDebugEnabled() && getDefinition().isVerbose()) {
            log.debug(displayStatistics() + " - Connection #" + proxyConnection.getId() + " returned");
        }

    }

    /** This means that there's something wrong the connection and it's probably best if no one uses it again. */
    protected void throwConnection(ProxyConnectionIF proxyConnection, String reason) {
        expireConnectionAsSoonAsPossible(proxyConnection, reason, true);
    }

    /** Get a ProxyConnection by index */
    private ProxyConnectionIF getProxyConnection(int i) {
        return (ProxyConnectionIF) proxyConnections.get(i);
    }

    /**
     * Return an array of all the connections
     * @return array of connections
     */
    protected ProxyConnectionIF[] getProxyConnections() {
        return (ProxyConnectionIF[]) proxyConnections.toArray(new ProxyConnectionIF[proxyConnections.size()]);
    }

    /**
     * Remove a ProxyConnection by calling its {@link ConnectionListenerIF#onDeath onDeath} event,
     * closing it (for real) and then removing it from the list.
     * @param proxyConnection the connection to remove
     * @param reason for log audit
     * @param forceExpiry true means close now, whether it is active or not; false means if it is active then
     * merely mark it for expiry so that it is removed as soon as it finished being active
     * @param triggerSweep if true then this removal will trigger a prototype sweep
     */
    protected void removeProxyConnection(ProxyConnectionIF proxyConnection, String reason, boolean forceExpiry, boolean triggerSweep) {
        // Just check that it is null
        if (forceExpiry || proxyConnection.isNull()) {

            proxyConnection.fromAnythingToNull();

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

            proxyConnections.remove(proxyConnection);

            if (log.isDebugEnabled()) {
                log.debug(displayStatistics() + " - #" + FormatHelper.formatMediumNumber(proxyConnection.getId())
                        + " removed because " + reason + ".");
            }

            if (triggerSweep) {
                PrototyperController.triggerSweep(getDefinition().getAlias());
            }

        } else {
            log.error(displayStatistics() + " - #" + FormatHelper.formatMediumNumber(proxyConnection.getId())
                    + " was not removed because isNull() was false.");
        }
    }

    protected void expireProxyConnection(ProxyConnectionIF proxyConnection, String reason, boolean forceExpiry) {
        removeProxyConnection(proxyConnection, reason, forceExpiry, true);
    }

    /**
     * Call this to shutdown gracefully.
     *  @param delay how long to wait for connections to become free before forcing them to close anyway
     */
    protected void shutdown(int delay, String finalizerName) throws Throwable {

        /* This will stop us giving out any more connections and may
        cause some of the threads to die. */

        final String alias = getDefinition().getAlias();
        if (connectionPoolUp == true) {

            connectionPoolUp = false;
            long startFinalize = System.currentTimeMillis();

            if (delay > 0) {
                log.info("Shutting down '" + alias + "' pool started at "
                        + dateStarted + " - waiting for " + delay
                        + " milliseconds for everything to stop.  [ "
                        + finalizerName + "]");
            } else {
                log.info("Shutting down '" + alias + "' pool immediately [" + finalizerName + "]");
            }

            /* Interrupt the threads (in case they're sleeping) */

            boolean connectionClosedManually = false;
            try {

                // TODO need some sort of synchronization here. If the house keeper
                // or prototyper are running right now they might create a new
                // connection after we have closed them all below
                try {
                    HouseKeeperController.cancel(alias);
                } catch (ProxoolException e) {
                    log.error("Shutdown couldn't cancel house keeper", e);
                }

                try {
                    PrototyperController.cancel(alias);
                } catch (NullPointerException e) {
                    log.error("PrototypingThread already dead", e);
                } catch (Exception e) {
                    log.error("Can't wake prototypingThread", e);
                }

                // Cancel the admin thread (for statistics)
                if (admin != null) {
                    admin.cancelAll();
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
                        removeProxyConnection(getProxyConnection(i), "of shutdown", true, false);
                        if (log.isDebugEnabled()) {
                            log.debug("Connection #" + id + " closed");
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

                ConnectionPoolManager.getInstance().removeConnectionPool(alias);

                if (log.isDebugEnabled()) {
                    log.debug("'" + alias + "' pool has been closed down by " + finalizerName
                            + " in " + (System.currentTimeMillis() - startFinalize) + " milliseconds.");
                    if (!connectionClosedManually) {
                        log.debug("No connections required manual removal.");
                    }
                }
                super.finalize();
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Ignoring duplicate attempt to shutdown '" + alias + "' pool by " + finalizerName);
            }
        }
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
        statistics.append(FormatHelper.formatBigNumber(getConnectionsServedCount()));

        if (getConnectionsRefusedCount() > 0) {
            statistics.append(" -");
            statistics.append(FormatHelper.formatBigNumber(getConnectionsRefusedCount()));
        }

        statistics.append(" (");
        statistics.append(FormatHelper.formatSmallNumber(getActiveConnectionCount()));
        statistics.append("/");
        statistics.append(FormatHelper.formatSmallNumber(getAvailableConnectionCount() + getActiveConnectionCount()));
        statistics.append("/");
        statistics.append(FormatHelper.formatSmallNumber(getOfflineConnectionCount()));
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

    protected void expireAllConnections(String reason, boolean merciful) {

        // Do this in two stages because expiring a connection will trigger
        // the prototyper to make more. And that might mean we end up
        // killing a newly made connection;
        Set pcs = new HashSet();
        for (int i = proxyConnections.size() - 1; i >= 0; i--) {
            pcs.add(proxyConnections.get(i));
        }

        Iterator i = pcs.iterator();
        while (i.hasNext()) {
            ProxyConnectionIF pc = (ProxyConnectionIF) i.next();
            expireConnectionAsSoonAsPossible(pc, reason, merciful);
        }
    }

    protected void expireConnectionAsSoonAsPossible(ProxyConnectionIF proxyConnection, String reason, boolean merciful) {
        if (proxyConnection.fromAvailableToOffline()) {
            if (proxyConnection.fromOfflineToNull()) {
                // It is.  Expire it now .
                expireProxyConnection(proxyConnection, reason, REQUEST_EXPIRY);
            }
        } else {
            // Oh no, it's in use.

            if (merciful) {
                //Never mind, we'll mark it for expiry
                // next time it is available.  This will happen in the
                // putConnection() method.
                proxyConnection.markForExpiry(reason);
                if (log.isDebugEnabled()) {
                    log.debug(displayStatistics() + " - #" + FormatHelper.formatMediumNumber(proxyConnection.getId()) + " marked for expiry.");
                }
            } else {
                // So? Kill, kill, kill

                // We have to make sure it's null first.
                expireProxyConnection(proxyConnection, reason, FORCE_EXPIRY);
            }

        } // END if (proxyConnection.setOffline())
    }

    protected void registerRemovedConnection(int status) {
        connectionCount--;
        PrototyperController.connectionRemoved(getDefinition().getAlias());
        connectionCountByState[status]--;
    }

    protected void changeStatus(int oldStatus, int newStatus) {
        connectionCountByState[oldStatus]--;
        connectionCountByState[newStatus]++;
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

    /**
     * @deprecated use {@link #addStateListener(StateListenerIF)} instead.
     */
    public void setStateListener(StateListenerIF stateListener) {
        addStateListener(stateListener);
    }

    public void addStateListener(StateListenerIF stateListener) {
        this.compositeStateListener.addListener(stateListener);
    }

    public boolean removeStateListener(StateListenerIF stateListener) {
        return this.compositeStateListener.removeListener(stateListener);
    }

    /**
     * @deprecated use {@link #addConnectionListener(ConnectionListenerIF)} instead.
     */
    public void setConnectionListener(ConnectionListenerIF connectionListener) {
        addConnectionListener(connectionListener);
    }

    public void addConnectionListener(ConnectionListenerIF connectionListener) {
        this.compositeConnectionListener.addListener(connectionListener);
    }

    public boolean removeConnectionListener(ConnectionListenerIF connectionListener) {
        return this.compositeConnectionListener.removeListener(connectionListener);
    }

    /** Call the onBirth() method on each StateListenerIF . */
    protected void onBirth(Connection connection) throws SQLException {
        this.compositeConnectionListener.onBirth(connection);
    }

    /** Call the onDeath() method on each StateListenerIF . */
    protected void onDeath(Connection connection) throws SQLException {
        this.compositeConnectionListener.onDeath(connection);
    }

    /** Call the onExecute() method on each StateListenerIF . */
    protected void onExecute(String command, long elapsedTime, Exception exception) throws SQLException {
        if (exception == null) {
            this.compositeConnectionListener.onExecute(command, elapsedTime);
        } else {
            this.compositeConnectionListener.onFail(command, exception);
        }
    }

    /**
     * Is there a {@link ConnectionListenerIF listener} for connections
     * @return true if there is a listener registered.
     */
    protected boolean isConnectionListenedTo() {
        return !compositeConnectionListener.isEmpty();
    }

    public String toString() {
        return getDefinition().toString();
    }

    public int getUpState() {
        return upState;
    }


    public void setUpState(int upState) {
        if (this.upState != upState) {
            compositeStateListener.upStateChanged(upState);
            this.upState = upState;
        }
    }

    public Collection getConnectionInfos() {
        return proxyConnections;
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
                proxyConnection = (ProxyConnection) proxyConnections.get(nextAvailableConnection);
            } catch (ArrayIndexOutOfBoundsException e) {
                nextAvailableConnection = 0;
                proxyConnection = (ProxyConnection) proxyConnections.get(nextAvailableConnection);
            }

            if (proxyConnection.getId() == id) {
                // This is the one
                proxyConnection.fromAvailableToOffline();
                proxyConnection.fromOfflineToNull();
                removeProxyConnection(proxyConnection, "it was manually killed", forceExpiry, true);
                success = true;
                break;
            }

            nextAvailableConnection++;
        }

        if (!success) {
            if (log.isDebugEnabled()) {
                log.debug(displayStatistics() + " - couldn't find " + FormatHelper.formatMediumNumber(proxyConnection.getId())
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

    /**
     * @see ConnectionPoolStatisticsIF#getDateStarted
     */
    public Date getDateStarted() {
        return dateStarted;
    }

    /**
     * Get the admin for this pool
     * @return admin
     */
    protected Admin getAdmin() {
        return admin;
    }

    protected boolean isLocked() {
        return locked;
    }

    protected void lock() {
        locked = true;
    }

    protected void unlock() {
        locked = false;
    }

    protected static final boolean FORCE_EXPIRY = true;

    protected static final boolean REQUEST_EXPIRY = false;

    /**
     * The time (in milliseconds) that we last refused a connection
     * @return timeOfLastRefusal
     */
    protected long getTimeOfLastRefusal() {
        return timeOfLastRefusal;
    }
}

/*
 Revision history:
 $Log: ConnectionPool.java,v $
 Revision 1.58  2003/03/05 18:42:32  billhorsman
 big refactor of prototyping and house keeping to
 drastically reduce the number of threads when using
 many pools

 Revision 1.57  2003/03/03 16:06:44  billhorsman
 name house keeper and prototyper threads now includes alias

 Revision 1.56  2003/03/03 11:11:57  billhorsman
 fixed licence

 Revision 1.55  2003/02/28 18:08:55  billhorsman
 OVERLOAD state is now triggered immediately rather
 than waiting for house keeper

 Revision 1.54  2003/02/28 10:10:25  billhorsman
 on death now gets called for connections killed during shutdown

 Revision 1.53  2003/02/26 16:05:52  billhorsman
 widespread changes caused by refactoring the way we
 update and redefine pool definitions.

 Revision 1.52  2003/02/26 12:57:30  billhorsman
 added TO DO

 Revision 1.51  2003/02/19 23:46:10  billhorsman
 renamed monitor package to admin

 Revision 1.50  2003/02/19 23:07:46  billhorsman
 state changes are now only calculated every time the house
 keeper runs, but it's more accurate

 Revision 1.49  2003/02/19 22:38:33  billhorsman
 fatal sql exception causes house keeper to run
 immediately

 Revision 1.48  2003/02/18 16:49:59  chr32
 Added possibility to remove connection and state listeners.

 Revision 1.47  2003/02/12 12:27:16  billhorsman
 log proxy hashcode too

 Revision 1.46  2003/02/07 17:26:04  billhorsman
 deprecated removeAllConnectionPools in favour of
 shutdown (and dropped unreliable finalize() method)

 Revision 1.45  2003/02/07 14:19:01  billhorsman
 fixed deprecated use of debugLevel property

 Revision 1.44  2003/02/07 14:16:46  billhorsman
 support for StatisticsListenerIF

 Revision 1.43  2003/02/07 10:27:47  billhorsman
 change in shutdown procedure to allow re-registration

 Revision 1.42  2003/02/07 01:48:15  chr32
 Started using new composite listeners.

 Revision 1.41  2003/02/06 17:41:04  billhorsman
 now uses imported logging

 Revision 1.40  2003/02/04 17:18:30  billhorsman
 move ShutdownHook init code

 Revision 1.39  2003/02/04 15:59:50  billhorsman
 finalize now shuts down StatsRoller timer

 Revision 1.38  2003/02/02 23:35:48  billhorsman
 removed ReloadMonitor to remove use of System properties

 Revision 1.37  2003/01/31 16:53:16  billhorsman
 checkstyle

 Revision 1.36  2003/01/31 11:49:28  billhorsman
 use Admin instead of Stats

 Revision 1.35  2003/01/31 00:20:05  billhorsman
 statistics is now a string to allow multiple,
 comma-delimited values (plus better logging of errors
 during destruction)

 Revision 1.34  2003/01/30 17:22:21  billhorsman
 add statistics support

 Revision 1.33  2003/01/27 18:26:35  billhorsman
 refactoring of ProxyConnection and ProxyStatement to
 make it easier to write JDK 1.2 patch

 Revision 1.32  2003/01/15 14:51:40  billhorsman
 checkstyle

 Revision 1.31  2003/01/15 12:01:37  billhorsman
 added getDateStarted()

 Revision 1.30  2003/01/15 00:07:43  billhorsman
 now uses FastArrayList instead of Vector for thread safe
 improvements

 Revision 1.29  2002/12/18 12:16:22  billhorsman
 double checking of connection state counts

 Revision 1.28  2002/12/17 17:15:39  billhorsman
 Better synchronization of status stuff

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
