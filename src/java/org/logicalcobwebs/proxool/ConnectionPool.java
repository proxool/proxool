/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.logicalcobwebs.concurrent.ReaderPreferenceReadWriteLock;
import org.logicalcobwebs.concurrent.WriterPreferenceReadWriteLock;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.logicalcobwebs.proxool.admin.Admin;
import org.logicalcobwebs.proxool.util.FastArrayList;

/**
 * This is where most things happen. (In fact, probably too many things happen in this one
 * class).
 * @version $Revision: 1.84 $, $Date: 2006/03/23 11:44:57 $
 * @author billhorsman
 * @author $Author: billhorsman $ (current maintainer)
 */
class ConnectionPool implements ConnectionPoolStatisticsIF {

    /**
     * Use this for messages that aren't useful for the pool specific log
     */
    private static final Log LOG = LogFactory.getLog(ConnectionPool.class);

    /**
     * Here we deviate from the standard of using the classname for the log
     * name. Here we want to use the alias for the pool so that we can log
     * each pool to different places. So we have to instantiate the log later.
     */
    private Log log;

    private ReaderPreferenceReadWriteLock connectionStatusReadWriteLock = new ReaderPreferenceReadWriteLock();

    /**
     * If you want to shutdown the pool you should get a write lock on this. And if you use the pool then
     * get a read lock. This stops us trying to shutdown the pool whilst it is in use. Only, we don't want
     * to delay shutdown just because some greedy user has got a connection active. Shutdown should be
     * relatively immediate. So we don't ask for a read lock for the whole time that a connection is active.
     */
    private WriterPreferenceReadWriteLock primaryReadWriteLock = new WriterPreferenceReadWriteLock();

    private static final String[] STATUS_DESCRIPTIONS = {"NULL", "AVAILABLE", "ACTIVE", "OFFLINE"};

    private static final String MSG_MAX_CONNECTION_COUNT =
            "Couldn't get connection because we are at maximum connection count and there are none available";

    /** This is the pool itself */
    private List proxyConnections;

    /** This is the "round robin" that makes sure we use all the connections */
    private int nextAvailableConnection = 0;

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

    private boolean connectionPoolUp = false;

    /**
     * This gets set during {@link #shutdown}. We use it to notify shutdown
     * that all connections are now non-active.
     */
    private Thread shutdownThread;

    private Prototyper prototyper;

    /**
     * Initialised in {@link ConnectionPool#ConnectionPool constructor}.
     */
    private ConnectionResetter connectionResetter;

    private ConnectionValidatorIF connectionValidator;
    
    
    protected ConnectionPool(ConnectionPoolDefinition definition) throws ProxoolException {

        // Use the FastArrayList for performance and thread safe
        // behaviour. We set its behaviour to "fast"  (meaning reads are
        // unsynchronized, whilst writes are not).
        FastArrayList fal = new FastArrayList();
        fal.setFast(true);
        proxyConnections = fal;

        log = LogFactory.getLog("org.logicalcobwebs.proxool." + definition.getAlias());
        connectionResetter = new ConnectionResetter(log, definition.getDriver());
        setDefinition(definition);

        connectionValidator = new DefaultConnectionValidator();
        
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
        connectionPoolUp = true;
        prototyper = new Prototyper(this);
        HouseKeeperController.register(this);
    }

    /**
     * Get a connection from the pool.  If none are available or there was an Exception
     * then an exception is thrown and something written to the log
     */
    protected Connection getConnection() throws SQLException {

        String requester = Thread.currentThread().getName();

        /*
         *If we're busy, we need to return as quickly as possible. Because this is unsynchronized
         * we run the risk of refusing a connection when we might actually be able to. But that will
         * only happen when we're right at or near maximum connections anyway.
         */

        try {
            prototyper.quickRefuse();
        } catch (SQLException e) {
            connectionsRefusedCount++;
            if (admin != null) {
                admin.connectionRefused();
            }
            log.info(displayStatistics() + " - " + MSG_MAX_CONNECTION_COUNT);
            timeOfLastRefusal = System.currentTimeMillis();
            setUpState(StateListenerIF.STATE_OVERLOADED);
            throw e;
        }

        prototyper.checkSimultaneousBuildThrottle();       
        
        ProxyConnection proxyConnection = null;

        try {

            // We need to look at all the connections, but we don't want to keep looping round forever
            for (int connectionsTried = 0; connectionsTried < proxyConnections.size(); connectionsTried++) {
                // By doing this in a try/catch we avoid needing to synch on the size().  We need to do be
                // able to cope with connections being removed whilst we are going round this loop
                try {
                    proxyConnection = (ProxyConnection) proxyConnections.get(nextAvailableConnection);
                } catch (ArrayIndexOutOfBoundsException e) {
                    // This is thrown by a Vector (which we no longer use), but is
                    // kept here for a while.
                    nextAvailableConnection = 0;
                    proxyConnection = (ProxyConnection) proxyConnections.get(nextAvailableConnection);
                } catch (IndexOutOfBoundsException e) {
                    // This is thrown by a true List
                    nextAvailableConnection = 0;
                    proxyConnection = (ProxyConnection) proxyConnections.get(nextAvailableConnection);
                }
                // setActive() returns false if the ProxyConnection wasn't available.  You
                // can't set it active twice (at least, not without making it available again
                // in between)
                if (proxyConnection != null && proxyConnection.setStatus(ProxyConnectionIF.STATUS_AVAILABLE, ProxyConnectionIF.STATUS_ACTIVE)) {

                    // Okay. So we have it. But is it working ok?
                    if (getDefinition().isTestBeforeUse()) {
                        if (!testConnection(proxyConnection)) {
                            // Oops. No it's not. Let's choose another.
                            proxyConnection = null;
                        }
                    }
                    if (proxyConnection != null) {
                        nextAvailableConnection++;
                        break;
                    }
                } else {
                    proxyConnection = null;
                }
                nextAvailableConnection++;
            }
            // Did we get one?
            if (proxyConnection == null) {
                try {
                    // No!  Let's see if we can create one
                    proxyConnection = prototyper.buildConnection(ProxyConnection.STATUS_ACTIVE, "on demand");
                    
                    // Okay. So we have it. But is it working ok?
                    if (getDefinition().isTestBeforeUse()) {
                        if (!testConnection(proxyConnection)) {
                            // Oops. No it's not. There's not much more we can do for now
                            throw new SQLException("Created a new connection but it failed its test");
                        }
                    }
                } catch (SQLException e) {
                    throw e;
                } catch (ProxoolException e) {
                    log.debug("Couldn't get connection", e);
                    throw new SQLException(e.toString());
                } catch (Throwable e) {
                    log.error("Couldn't get connection", e);
                    throw new SQLException(e.toString());
                }
            }

        } catch (SQLException e) {
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

        // This gives the proxy connection a chance to reset itself before it is served.
        proxyConnection.open();

        return ProxyFactory.getWrappedConnection(proxyConnection);
    }

    /**
     * Test the connection (if required)
     * If the connection fails the test, it is removed from the pool.
     * If no ConnectionValidatorIF is defined, then the test always succeed.
     * 
     * @param proxyConnection the connection to test
     * @return TRUE if the connection pass the test, FALSE if it fails
     */
    private boolean testConnection(ProxyConnectionIF proxyConnection) {
        // is validation enabled ?
        if( connectionValidator == null ) {
            return true;
        }
        
        // validate the connection
        boolean success = connectionValidator.validate(getDefinition(), proxyConnection.getConnection());
        
        if( success ) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(displayStatistics() + " - Connection #" + proxyConnection.getId() + " tested: OK");
            }
        }
        else {
            proxyConnection.setStatus(ProxyConnectionIF.STATUS_NULL);
            removeProxyConnection(proxyConnection, "it didn't pass the validation", ConnectionPool.REQUEST_EXPIRY, true);
        }
        
        // return 
        return success;
    }

    /**
     * Add a ProxyConnection to the pool
     * @param proxyConnection new connection
     * @return true if the connection was added or false if it wasn't (for instance, if the definition it
     * was built with is out of date).
     */
    protected boolean addProxyConnection(ProxyConnectionIF proxyConnection) {
        boolean added = false;
        try {
            acquireConnectionStatusWriteLock();
            if (proxyConnection.getDefinition() == getDefinition()) {
                proxyConnections.add(proxyConnection);
                connectionCountByState[proxyConnection.getStatus()]++;
                added = true;
            }
        } finally {
            releaseConnectionStatusWriteLock();
        }
        return added;
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
            long now = System.currentTimeMillis();
            long start = proxyConnection.getTimeLastStartActive();
            if (now - start < 0) {
                log.warn("Future start time detected. #" + proxyConnection.getId() + " start = " + new Date(start)
                        + " (" + (now - start) + " milliseconds)");
            } else if (now - start > 1000000) {
                log.warn("Suspiciously long active time. #" + proxyConnection.getId() + " start = " + new Date(start));
            }
            admin.connectionReturned(now - start);
        }

        // It's possible that this connection is due for expiry
        if (proxyConnection.isMarkedForExpiry()) {
            if (proxyConnection.setStatus(ProxyConnectionIF.STATUS_ACTIVE, ProxyConnectionIF.STATUS_NULL)) {
                expireProxyConnection(proxyConnection, proxyConnection.getReasonForMark(), REQUEST_EXPIRY);
            }
        } else {

            // Optionally, test it to see if it is ok
            if (getDefinition().isTestAfterUse()) {
                // It will get removed by this call if it is no good
                testConnection(proxyConnection);
            }

            // Let's make it available for someone else
            if (!proxyConnection.setStatus(ProxyConnectionIF.STATUS_ACTIVE, ProxyConnectionIF.STATUS_AVAILABLE)) {
                if (proxyConnection.getStatus() == ProxyConnectionIF.STATUS_AVAILABLE) {
                    // This is *probably* because the connection has been closed twice.
                    // Although we can't tell for sure. We'll have to refactor this to use
                    // throw away wrappers to avoid this problem.
                    log.warn("Unable to close connection " + proxyConnection.getId()
                            + " - I suspect that it has been closed already. Closing it more"
                            + " than once is unwise and should be avoided.");
                } else {
                    log.warn("Unable to set status of connection " + proxyConnection.getId()
                            + " from " + getStatusDescription(ProxyConnectionIF.STATUS_ACTIVE)
                            + " to " + getStatusDescription(ProxyConnectionIF.STATUS_AVAILABLE)
                            + " because it's state was " + getStatusDescription(proxyConnection.getStatus()));
                }
            }
        }

        if (log.isDebugEnabled() && getDefinition().isVerbose()) {
            log.debug(displayStatistics() + " - Connection #" + proxyConnection.getId() + " returned (now "
                    + getStatusDescription(proxyConnection.getStatus()) + ")");
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

            proxyConnection.setStatus(ProxyConnectionIF.STATUS_NULL);

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

            try {
                // If we're shutting down then getting a write lock will cause a deadlock
                if (isConnectionPoolUp()) {
                    acquireConnectionStatusWriteLock();
                }
                proxyConnections.remove(proxyConnection);
            } finally {
                if (isConnectionPoolUp()) {
                    releaseConnectionStatusWriteLock();
                }
            }

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

        final String alias = getDefinition().getAlias();
        try {
            /* This will stop us giving out any more connections and may
            cause some of the threads to die. */

            acquirePrimaryWriteLock();

            if (connectionPoolUp) {

                connectionPoolUp = false;
                long startFinalize = System.currentTimeMillis();
                shutdownThread = Thread.currentThread();

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

                    try {
                        HouseKeeperController.cancel(alias);
                    } catch (ProxoolException e) {
                        log.error("Shutdown couldn't cancel house keeper", e);
                    }

                    // Cancel the admin thread (for statistics)
                    if (admin != null) {
                        admin.cancelAll();
                    }

                    /* Patience, patience. */

                    if (connectionCountByState[ProxyConnectionIF.STATUS_ACTIVE] != 0) {
                        long endWait = startFinalize + delay;
                        LOG.info("Waiting until " + new Date(endWait) + " for all connections to become inactive (active count is "
                                + connectionCountByState[ProxyConnectionIF.STATUS_ACTIVE] + ").");
                        while (true) {
                            long timeout = endWait - System.currentTimeMillis();
                            if (timeout > 0) {
                                synchronized (Thread.currentThread()) {
                                    try {
                                        Thread.currentThread().wait(timeout);
                                    } catch (InterruptedException e) {
                                        log.debug("Interrupted whilst sleeping.");
                                    }
                                }
                            }
                            int activeCount = connectionCountByState[ProxyConnectionIF.STATUS_ACTIVE];
                            if (activeCount == 0) {
                                break;
                            }
                            if (System.currentTimeMillis() < endWait) {
                                LOG.info("Still waiting for active count to reach zero (currently " + activeCount + ").");
                            } else {
                                // There are still connections active. Oh well, we're not _that_ patient
                                LOG.warn("Shutdown waited for "
                                        + (System.currentTimeMillis() - startFinalize) + " milliseconds for all "
                                        + "the connections to become inactive but the active count is still "
                                        + activeCount + ". Shutting down anyway.");
                                break;
                            }
                            Thread.sleep(100);
                        }
                    }

                    prototyper.cancel();
                    
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
                        log.info("'" + alias + "' pool has been closed down by " + finalizerName
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
        } catch (Throwable t) {
            log.error(finalizerName + " couldn't shutdown pool", t);
        } finally {
            releasePrimaryWriteLock();
        }
    }

    /**
     * You should {@link #acquireConnectionStatusReadLock acquire}
     * a read lock if you want this to be accurate (but that might have
     * an impact on the performance of your pool).
     * @see ConnectionPoolStatisticsIF#getAvailableConnectionCount
     */
    public int getAvailableConnectionCount() {
        return connectionCountByState[ConnectionInfoIF.STATUS_AVAILABLE];
    }

    /**
     * You should {@link #acquireConnectionStatusReadLock acquire}
     * a read lock if you want this to be accurate (but that might have
     * an impact on the performance of your pool).
     * @see ConnectionPoolStatisticsIF#getActiveConnectionCount
     */
    public int getActiveConnectionCount() {
        return connectionCountByState[ConnectionInfoIF.STATUS_ACTIVE];
    }

    /**
     * You should {@link #acquireConnectionStatusReadLock acquire}
     * a read lock if you want this to be accurate (but that might have
     * an impact on the performance of your pool).
     * @see ConnectionPoolStatisticsIF#getOfflineConnectionCount
     */
    public int getOfflineConnectionCount() {
        return connectionCountByState[ConnectionInfoIF.STATUS_OFFLINE];
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
        if (proxyConnection.setStatus(ProxyConnectionIF.STATUS_AVAILABLE, ProxyConnectionIF.STATUS_OFFLINE)) {
            if (proxyConnection.setStatus(ProxyConnectionIF.STATUS_OFFLINE, ProxyConnectionIF.STATUS_NULL)) {
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
        prototyper.connectionRemoved();
        connectionCountByState[status]--;
    }

    /**
     * You should {@link #acquireConnectionStatusWriteLock acquire} a write lock
     * before calling this method
     * @param oldStatus so we know which count to decrement
     * @param newStatus so we know which count to increment
     */
    protected void changeStatus(int oldStatus, int newStatus) {
        // LOG.debug("About to change status");
        connectionCountByState[oldStatus]--;
        connectionCountByState[newStatus]++;
        // LOG.debug("Changing status from " + oldStatus + " to " + newStatus);
        // Check to see if shutdown is waiting for all connections to become
        // non-active
        if (shutdownThread != null && connectionCountByState[ProxyConnectionIF.STATUS_ACTIVE] == 0) {
            synchronized (shutdownThread) {
                shutdownThread.notify();
            }
        }

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
    protected synchronized void setDefinition(ConnectionPoolDefinition definition) throws ProxoolException {
        this.definition = definition;

        try {
            Class.forName(definition.getDriver());
        } catch (ClassNotFoundException e) {
            log.error("Couldn't load class " + definition.getDriver(), e);
            throw new ProxoolException("Couldn't load class " + definition.getDriver());
        } catch (NullPointerException e) {
            log.error("Definition did not contain driver", e);
            throw new ProxoolException("Definition did not contain driver");
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

    protected Collection getConnectionInfos() {
        Collection cis = null;
        cis = new TreeSet();
        Iterator i = proxyConnections.iterator();
        while (i.hasNext()) {
            ConnectionInfoIF connectionInfo = (ConnectionInfoIF) i.next();
            ConnectionInfo ci = new ConnectionInfo();
            ci.setAge(connectionInfo.getAge());
            ci.setBirthDate(connectionInfo.getBirthDate());
            ci.setId(connectionInfo.getId());
            ci.setMark(connectionInfo.getMark());
            ci.setRequester(connectionInfo.getRequester());
            ci.setStatus(connectionInfo.getStatus());
            ci.setTimeLastStartActive(connectionInfo.getTimeLastStartActive());
            ci.setTimeLastStopActive(connectionInfo.getTimeLastStopActive());
            ci.setDelegateUrl(connectionInfo.getDelegateUrl());
            ci.setProxyHashcode(connectionInfo.getProxyHashcode());
            ci.setDelegateHashcode(connectionInfo.getDelegateHashcode());
            String[] sqlCalls = connectionInfo.getSqlCalls();
            for (int j = 0; j < sqlCalls.length; j++) {
                ci.addSqlCall(sqlCalls[j]);
            }
            cis.add(ci);
        }
        return cis;
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
            } catch (IndexOutOfBoundsException e) {
                nextAvailableConnection = 0;
                proxyConnection = (ProxyConnection) proxyConnections.get(nextAvailableConnection);
            }

            if (proxyConnection.getId() == id) {
                // This is the one
                proxyConnection.setStatus(ProxyConnectionIF.STATUS_AVAILABLE, ProxyConnectionIF.STATUS_OFFLINE);
                proxyConnection.setStatus(ProxyConnectionIF.STATUS_OFFLINE, ProxyConnectionIF.STATUS_NULL);
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

    /**
     * Call this if you want to do something important to the pool. Like shut it down.
     * @throws InterruptedException if we couldn't
     */
    protected void acquirePrimaryReadLock() throws InterruptedException {
//        if (log.isDebugEnabled()) {
//            try {
//                throw new RuntimeException("TRACE ONLY");
//            } catch (RuntimeException e) {
//                log.debug("About to acquire primary read lock", e);
//            }
//            // log.debug("About to acquire primary read lock");
//        }
        primaryReadWriteLock.readLock().acquire();
//        if (log.isDebugEnabled()) {
//            try {
//                throw new RuntimeException("TRACE ONLY");
//            } catch (RuntimeException e) {
//                log.debug("Acquired primary read lock", e);
//            }
//            //log.debug("Acquired primary read lock");
//        }
    }

    /**
     * @see #acquirePrimaryReadLock
     */
    protected void releasePrimaryReadLock() {
//        try {
//            throw new RuntimeException("TRACE ONLY");
//        } catch (RuntimeException e) {
//            log.debug("Released primary read lock", e);
//        }
        //log.debug("Released primary read lock");
        primaryReadWriteLock.readLock().release();
    }

    /**
     * Call this everytime you build a connection. It ensures that we're not
     * trying to shutdown the pool whilst we are building a connection. So you
     * should check that the pool is still {@link #isConnectionPoolUp up}.
     * @throws InterruptedException if there was a problem.
     */
    protected void acquirePrimaryWriteLock() throws InterruptedException {
//        boolean success = false;
//        try {
//            if (log.isDebugEnabled()) {
//                try {
//                    throw new RuntimeException("TRACE ONLY");
//                } catch (RuntimeException e) {
//                    log.debug("About to acquire primary write lock", e);
//                }
//                //log.debug("About to acquire primary write lock");
//            }
            primaryReadWriteLock.writeLock().acquire();
//            success = true;
//            if (log.isDebugEnabled()) {
//                try {
//                    throw new RuntimeException("TRACE ONLY");
//                } catch (RuntimeException e) {
//                    log.debug("Acquired primary write lock", e);
//                }
//                //log.debug("Acquired primary write lock");
//            }
//        } finally {
//            if (log.isDebugEnabled() && !success) {
//                try {
//                    throw new RuntimeException("TRACE ONLY");
//                } catch (RuntimeException e) {
//                    log.debug("Failed to acquire primary write lock", e);
//                }
//                //log.debug("Failed to acquire primary write lock");
//            }
//        }
    }

    /**
     * @see #acquirePrimaryReadLock
     */
    protected void releasePrimaryWriteLock() {
        primaryReadWriteLock.writeLock().release();
//        try {
//            throw new RuntimeException("TRACE ONLY");
//        } catch (RuntimeException e) {
//            log.debug("Released primary write lock", e);
//        }
        //log.debug("Released primary write lock");
    }

    /**
     * Is the pool up?
     * @return false is the connection pool has been {@link #shutdown shutdown}
     * (or is in the process of being shutdown).
     */
    protected boolean isConnectionPoolUp() {
        return connectionPoolUp;
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

    protected void acquireConnectionStatusWriteLock() {
        try {
//            try {
//                throw new RuntimeException("TRACE ONLY");
//            } catch (RuntimeException e) {
//                LOG.debug("About to acquire connectionStatus write lock", e);
//            }
            connectionStatusReadWriteLock.writeLock().acquire();
//            try {
//                throw new RuntimeException("TRACE ONLY");
//            } catch (RuntimeException e) {
//                LOG.debug("Acquired connectionStatus write lock", e);
//            }
        } catch (InterruptedException e) {
            log.error("Couldn't acquire connectionStatus write lock", e);
        }
    }

    protected void releaseConnectionStatusWriteLock() {
        connectionStatusReadWriteLock.writeLock().release();
//        try {
//            throw new RuntimeException("TRACE ONLY");
//        } catch (RuntimeException e) {
//            LOG.debug("Released connectionStatus write lock", e);
//        }
    }

    protected void acquireConnectionStatusReadLock() {
        try {
            connectionStatusReadWriteLock.readLock().acquire();
        } catch (InterruptedException e) {
            log.error("Couldn't acquire connectionStatus read lock", e);
        }
    }

    protected boolean attemptConnectionStatusReadLock(long msecs) {
        try {
            return connectionStatusReadWriteLock.readLock().attempt(msecs);
        } catch (InterruptedException e) {
            log.error("Couldn't acquire connectionStatus read lock", e);
            return false;
        }
    }

    protected void releaseConnectionStatusReadLock() {
        connectionStatusReadWriteLock.readLock().release();
//        LOG.debug("Released connectionStatus read lock");
    }

    protected Prototyper getPrototyper() {
        return prototyper;
    }

    public long getConnectionCount() {
        return getPrototyper().getConnectionCount();
    }
}

/*
 Revision history:
 $Log: ConnectionPool.java,v $
 Revision 1.84  2006/03/23 11:44:57  billhorsman
 More information when quickly refusing

 Revision 1.83  2006/01/18 14:40:01  billhorsman
 Unbundled Jakarta's Commons Logging.

 Revision 1.82  2005/10/07 08:19:05  billhorsman
 New sqlCalls gives list of SQL calls rather than just he most recent (for when a connection makes more than one call before being returned to the pool)

 Revision 1.81  2005/10/02 12:32:02  billhorsman
 Make connectionCount available to statistics

 Revision 1.80  2005/09/26 09:54:14  billhorsman
 Avoid suspected deadlock when getting a detailed snapshot. Only attempt to get the concurrent lock for 10 seconds before giving up.

 Revision 1.79  2005/05/04 16:26:31  billhorsman
 Only add a new connection if the definition matches

 Revision 1.78  2004/03/25 22:02:15  brenuart
 First step towards pluggable ConnectionBuilderIF & ConnectionValidatorIF.
 Include some minor refactoring that lead to deprecation of some PrototyperController methods.

 Revision 1.76  2004/02/23 17:47:32  billhorsman
 Improved message that gets logged if the state change of a connection fails.

 Revision 1.75  2004/02/23 17:38:58  billhorsman
 Improved message that gets logged if you close a connection more than once.

 Revision 1.74  2004/02/12 13:02:17  billhorsman
 Catch correct exception when iterating through list.

 Revision 1.73  2003/12/09 18:54:55  billhorsman
 Make closure of statement during connection test more robust - credit to John Hume

 Revision 1.72  2003/11/04 13:52:01  billhorsman
 Fixed warning message

 Revision 1.71  2003/10/30 00:11:15  billhorsman
 Debug info and error logged if unsuccessful attempt to put connection back in pool. Plus connectioninfo comparator changed

 Revision 1.70  2003/09/30 18:39:08  billhorsman
 New test-before-use, test-after-use and fatal-sql-exception-wrapper-class properties.

 Revision 1.69  2003/09/30 07:50:04  billhorsman
 Smarter throwing of caught SQLExceptions without wrapping them up inside another (and losing the stack trace)

 Revision 1.68  2003/09/20 17:04:06  billhorsman
 Fix for incorrect OFFLINE count when house keeper removed a connection if the test SQL failed. This
 meant that the offline count went negative. The only consequence of that is that the logs look funny.

 Revision 1.67  2003/08/30 14:54:04  billhorsman
 Checkstyle

 Revision 1.66  2003/04/10 08:23:54  billhorsman
 removed very frequent debug

 Revision 1.65  2003/03/11 23:58:04  billhorsman
 fixed deadlock on connection expiry

 Revision 1.64  2003/03/11 14:51:49  billhorsman
 more concurrency fixes relating to snapshots

 Revision 1.63  2003/03/11 01:16:29  billhorsman
 removed misleasing debug

 Revision 1.62  2003/03/11 00:32:13  billhorsman
 fixed negative timeout

 Revision 1.61  2003/03/10 23:39:51  billhorsman
 shutdown is now notified when active count reaches
 zero

 Revision 1.60  2003/03/10 16:26:35  billhorsman
 removed debug traces

 Revision 1.59  2003/03/10 15:26:44  billhorsman
 refactoringn of concurrency stuff (and some import
 optimisation)

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
