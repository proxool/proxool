/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.logicalcobwebs.proxool.admin.Admin;
import org.logicalcobwebs.proxool.admin.SnapshotIF;
import org.logicalcobwebs.proxool.admin.StatisticsIF;
import org.logicalcobwebs.proxool.admin.StatisticsListenerIF;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

/**
 * <p>This provides some nice-to-have features that can't be provided by the
 * {@link java.sql.Driver} {@link ProxoolDriver implementation} of java.sql.Driver. Like starting up
 * a pool before you need a connection. And getting statistical information.</p>
 *
 * <p>You need to use this class wisely. It is obviously specfic to proxool so it will
 * stop you switching to another driver. Consider isolating the code that calls this
 * class so that you can easily remove it if you have to.</p>
 *
 * @version $Revision: 1.85 $, $Date: 2006/11/02 10:00:34 $
 * @author billhorsman
 * @author $Author: billhorsman $ (current maintainer)
 */
public class ProxoolFacade {

    private static final Log LOG = LogFactory.getLog(ProxoolFacade.class);

    private static Map configurators = new HashMap();

    private static CompositeProxoolListener compositeProxoolListener = new CompositeProxoolListener();

    private static boolean versionLogged = false;

    /**
     * This is the thread that has been registered with {@link Runtime} as a
     * shutdownHook. It is removed during shutdown.
     */
    private static Thread shutdownHook;

    /**
     * If you setthis to false then it us up to you to call shutdown explicitly
     * @see #shutdown(String, int)
     */
    private static boolean shutdownHookEnabled = true;

    /**
     * Build a ConnectionPool based on this definition and then start it.
     * @param url defines the delegate driver and delegate url.
     * @param info the properties used to configure Proxool (and any for the delegate driver too) - optional
     * @return the alias for this pool (or the full url if no alias is specified)
     * @throws ProxoolException if anything goes wrong
     */
    public static synchronized String registerConnectionPool(String url, Properties info) throws ProxoolException {
        return registerConnectionPool(url, info, true);
    }

    /**
     * Build a ConnectionPool based on this definition and then start it.
     * @param url defines the delegate driver and delegate url.
     * @param info the properties used to configure Proxool (and any for the delegate driver too) - optional
     * @param explicitRegister set to true if we are registering a new pool explicitly, or false
     * if it's just because we are serving a url that we haven't come across before
     * @return the alias for this pool (or the full url if no alias is specified)
     * @throws ProxoolException if anything goes wrong
     */
    protected static synchronized String registerConnectionPool(String url, Properties info, boolean explicitRegister) throws ProxoolException {
        String alias = getAlias(url);

        if (!versionLogged) {
            versionLogged = true;
            LOG.info("Proxool " + Version.getVersion());
        }

        try {
            Class.forName(ProxoolDriver.class.getName());
        } catch (ClassNotFoundException e) {
            LOG.error("Couldn't load " + ProxoolDriver.class.getName());
        }

        if (!ConnectionPoolManager.getInstance().isPoolExists(alias)) {
            ConnectionPoolDefinition cpd = new ConnectionPoolDefinition(url, info, explicitRegister);
            registerConnectionPool(cpd);
        } else {
            throw new ProxoolException("Attempt to register duplicate pool called '" + alias + "'");
        }

        return alias;
    }

    protected synchronized static void registerConnectionPool(ConnectionPoolDefinition connectionPoolDefinition) throws ProxoolException {
        // check isPoolExists once more now we are inside synchronized block.
        if (!ConnectionPoolManager.getInstance().isPoolExists(connectionPoolDefinition.getAlias())) {
            Properties jndiProperties = extractJndiProperties(connectionPoolDefinition);
            ConnectionPool connectionPool = ConnectionPoolManager.getInstance().createConnectionPool(connectionPoolDefinition);
            connectionPool.start();
            compositeProxoolListener.onRegistration(connectionPoolDefinition, connectionPoolDefinition.getCompleteInfo());
            if (isConfiguredForJMX(connectionPoolDefinition.getCompleteInfo())) {
                registerForJmx(connectionPoolDefinition.getAlias(), connectionPoolDefinition.getCompleteInfo());
            }
            if (jndiProperties != null) {
                registerDataSource(connectionPoolDefinition.getAlias(), jndiProperties);
            }
        } else {
            LOG.debug("Ignoring duplicate attempt to register " + connectionPoolDefinition.getAlias() + " pool");
        }
    }

    /**
     * With no configurator or properties (using default values)
     * @see #registerConnectionPool(java.lang.String, java.util.Properties)
     */
    public static void registerConnectionPool(String url) throws ProxoolException {
        registerConnectionPool(url, null);
    }

    /**
     * Extracts the pool alias from the url:
     *
     *    proxool.alias:driver:url -> alias
     *    proxool.alias -> alias
     *
     * @return the alias defined within the url
     * @throws ProxoolException if we couldn't find the alias
     */
    protected static String getAlias(String url) throws ProxoolException {
        String alias = null;
        final String prefix = ProxoolConstants.PROXOOL + ProxoolConstants.ALIAS_DELIMITER;

        // Check that the prefix is there
        if (url.startsWith(prefix)) {

            // Check for the alias
            int endOfPrefix = url.indexOf(ProxoolConstants.URL_DELIMITER);

            if (endOfPrefix > -1) {
                alias = url.substring(prefix.length(), endOfPrefix);
            } else {
                alias = url.substring(prefix.length());
            }
        }

        // Check we found it.
        if (alias == null || alias.length() == 0) {
            throw new ProxoolException("The URL '" + url + "' is not in the correct form. It should be: 'proxool.alias:driver:url'");
        }

        return alias;
    }

    /**
     * Remove a connection pool. Kills all the connections. Resets everything.
     * @param finalizer the name of the thread requesting shutdown (for logging)
     * @param connectionPool the pool to remove
     * @param delay the time to wait for connections to become inactive before killing it (milliseconds)
     */
    private static void removeConnectionPool(String finalizer, ConnectionPool connectionPool, int delay) {
        final String alias = connectionPool.getDefinition().getAlias();
        if (connectionPool != null) {
            try {
                compositeProxoolListener.onShutdown(alias);
                connectionPool.shutdown(delay, finalizer);
            } catch (Throwable t) {
                LOG.error("Problem trying to shutdown '" + alias + "' connection pool", t);
            }
        }
        connectionPool = null;
    }

    /**
     * Remove a connection pool. Kills all the connections. Resets everything.
     * @param alias the pool to remove
     * @param delay the time to wait for connections to become inactive before killing it (milliseconds)
     * @throws ProxoolException if we couldn't find the pool
     */
    public static void removeConnectionPool(String alias, int delay) throws ProxoolException {
        removeConnectionPool(Thread.currentThread().getName(), ConnectionPoolManager.getInstance().getConnectionPool(alias), delay);
    }

    /**
     * Removes all connection pools. Kills all the connections. Resets everything.
     * @param delay the time to wait for connections to become inactive before killing it (milliseconds)
     * @deprecated use the better named {@link #shutdown(int) shutdown()} instead.
     */
    public static void removeAllConnectionPools(int delay) {
        shutdown(Thread.currentThread().getName(), delay);
    }

    /**
     * Removes all connection pools. Kills all the connections. Resets everything.
     * Like {@link #shutdown(java.lang.String, int)} but passes the current thread name
     * and a delay of zero.
     */
    public static void shutdown() {
        shutdown(Thread.currentThread().getName(), 0);
    }

    /**
     * Removes all connection pools. Kills all the connections. Resets everything.
     * Like {@link #shutdown(java.lang.String, int)} but passes the current thread name.
     * @param delay the time to wait for connections to become inactive before killing it (milliseconds)
     */
    public static void shutdown(int delay) {
        shutdown(Thread.currentThread().getName(), delay);
    }

    /**
     * Removes all connection pools. Kills all the connections. Resets everything.
     * @param finalizer used to identify who is causing the pools to be removed (helps logging)
     * @param delay the time to wait for connections to become inactive before killing it (milliseconds)
     */
    protected static void shutdown(String finalizer, int delay) {

        ConnectionPool[] cps = ConnectionPoolManager.getInstance().getConnectionPools();
        for (int i = 0; i < cps.length; i++) {
            removeConnectionPool(finalizer, cps[i], delay);
        }

        // If a shutdown hook was registered then remove it
        try {
            if (shutdownHook != null) {
                ShutdownHook.remove(shutdownHook);
            }
        } catch (Throwable t) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Unanticipated error during removal of ShutdownHook. Ignoring it.", t);
            }
        }

        // Stop threads
        PrototyperController.shutdown();
        HouseKeeperController.shutdown();

    }

    /**
     * If you call this then you'll have to call shutdown explicitly
     * @see #shutdown(String, int)
     */
    public static void disableShutdownHook() {
        ProxoolFacade.shutdownHookEnabled = false;
    }

    /**
     * Call this if you change your mind about {@link #disableShutdownHook() disabling} it.
     * The default behaviour is to have it enabled so unless you have disabled it then
     * there's nothing to do.
     */
    public static void enableShutdownHook() {
        ProxoolFacade.shutdownHookEnabled = true;
    }

    /**
     * Whether the {@link ShutdownHook} should do anything.
     * @see #disableShutdownHook()
     * @see #enableShutdownHook()
     * @return true if the shutdown hook should clean up
     */
    public static boolean isShutdownHookEnabled() {
        return shutdownHookEnabled;
    }

    /**
     * Like {@link #removeConnectionPool(java.lang.String, int)} but uses no delay. (Kills
     * everything as quickly as possible).
     * @param alias to identify the pool
     * @throws ProxoolException if we couldn't find the pool
     */
    public static void removeConnectionPool(String alias) throws ProxoolException {
        removeConnectionPool(alias, 0);
    }

    /**
     * Get real-time statistical information about how a pool is performing.
     * @param alias to identify the pool
     * @return the statistics
     * @throws ProxoolException if we couldn't find the pool
     * @deprecated use {@link #getSnapshot}
     */
    public static ConnectionPoolStatisticsIF getConnectionPoolStatistics(String alias) throws ProxoolException {
        return ConnectionPoolManager.getInstance().getConnectionPool(alias);
    }

    /**
     * Get real-time statistical information about how a pool is performing.
     * and extract the information piece by piece.
     * @param alias to identify the pool
     * @return a horrible string describing the statistics
     * @throws ProxoolException if we couldn't find the pool
     * @deprecated use {@link #getSnapshot}
     */
    public static String getConnectionPoolStatisticsDump(String alias) throws ProxoolException {
        return ConnectionPoolManager.getInstance().getConnectionPool(alias).displayStatistics();
    }

    /**
     * Get the definition of a pool.
     * @param alias identifies the pool
     * @throws ProxoolException if we couldn't find the pool
     */
    public static ConnectionPoolDefinitionIF getConnectionPoolDefinition(String alias) throws ProxoolException {
        return ConnectionPoolManager.getInstance().getConnectionPool(alias).getDefinition();
    }

    /**
     * Get details on each connection within the pool. This can tell you which ones are active, how long they have
     * been active, etc.
     * @param alias identifies the pool
     * @return a collection of {@link ConnectionInfoIF ConnectionInfoIFs}
     * @throws ProxoolException if we couldn't find the pool
     * @deprecated use {@link #getSnapshot(java.lang.String, boolean) snapshot} instead.
     */
    public static Collection getConnectionInfos(String alias) throws ProxoolException {
        return ConnectionPoolManager.getInstance().getConnectionPool(alias).getConnectionInfos();
    }

    /**
     * Kill all connections in a pool. The pool continues to work however, and new connections will be
     * made as required.
     * @param alias the pool containing the connection
     * @param merciful if true will only kill connections that aren't active
     * @throws ProxoolException if we couldn't find the pool
     * @deprecated use {@link #killAllConnections(java.lang.String, java.lang.String, boolean) alternative}
     * to provide better auditing in log
     */
    public static void killAllConnections(String alias, boolean merciful) throws ProxoolException {
        killAllConnections(alias, "of thread " + Thread.currentThread().getName(), merciful);
    }

    /**
     * Kill all connections in a pool. The pool continues to work however, and new connections will be
     * made as required.
     * @param alias the pool containing the connection
     * @param reason provides audit in log of why connections were killed
     * @param merciful if true will only kill connections that aren't active
     * @throws ProxoolException if we couldn't find the pool
     */
    public static void killAllConnections(String alias, String reason, boolean merciful) throws ProxoolException {
        ConnectionPoolManager.getInstance().getConnectionPool(alias).expireAllConnections(reason, merciful);
    }

    /**
     * Like {@link #killAllConnections} but defaults to merciful.
     * @param alias to identify the pool
     * @throws ProxoolException if we couldn't find the pool
     * @deprecated use {@link #killAllConnections(java.lang.String, java.lang.String) alternative}
     * to provide better auditing in log
     */
    public static void killAllConnections(String alias) throws ProxoolException {
        killAllConnections(alias, "of thread " + Thread.currentThread().getName(), MERCIFUL);
    }

    /**
     * Like {@link #killAllConnections} but defaults to merciful.
     * @param alias to identify the pool
     * @param reason provides audit in log of why connections were killed
     * @throws ProxoolException if we couldn't find the pool
     */
    public static void killAllConnections(String alias, String reason) throws ProxoolException {
        killAllConnections(alias, reason, MERCIFUL);
    }

    /**
     * Kill a single connection
     * @param alias the pool containing the connection
     * @param id the id of the specific connection
     * @param merciful if true will only kill connections that aren't active
     * @return true if the connection was killed, or false if it couldn't be found.
     * @throws ProxoolException if we couldn't find the pool
     */
    public static boolean killConnecton(String alias, long id, boolean merciful) throws ProxoolException {
        // Let's be explicit about what we're doing here
        boolean forceExpiry = !merciful;
        return ConnectionPoolManager.getInstance().getConnectionPool(alias).expireConnection(id, forceExpiry);
    }

    /**
     * Kill a single connection
     * @param connection the connection to kill
     * @param merciful if true will only kill connections that aren't active
     * @return true if the connection was killed, or false if it couldn't be found.
     * @throws ProxoolException if we didn't recognise the connection
     */
    public static boolean killConnecton(Connection connection, boolean merciful) throws ProxoolException {
        WrappedConnection wrappedConnection = ProxyFactory.getWrappedConnection(connection);
        if (wrappedConnection != null) {
            long id = wrappedConnection.getId();
            String alias = wrappedConnection.getAlias();
            return killConnecton(alias, id, merciful);
        } else {
            throw new ProxoolException("Attempt to kill unrecognised exception " + connection);
        }
    }

    /**
     * Add a listener that gets called everytime a global Proxool event ocours.
     * @param proxoolListener the listener to add.
     */
    public static void addProxoolListener(ProxoolListenerIF proxoolListener) {
        compositeProxoolListener.addListener(proxoolListener);
    }

    /**
     * Remove a registered <code>ProxoolListenerIF</code>.
     * @param proxoolListener the listener to remove.
     * @return whether the listener was found or removed or not.
     */
    public static boolean removeProxoolListener(ProxoolListenerIF proxoolListener) {
        return compositeProxoolListener.removeListener(proxoolListener);
    }

    /**
     * @deprecated  use {@link #addStateListener(String, StateListenerIF)} instead.
     */
    public static void setStateListener(String alias, StateListenerIF stateListener) throws ProxoolException {
        addStateListener(alias, stateListener);
    }

    /**
     * Add a listener that monitors the change of state of the pool (quiet, busy, overloaded, or down)
     * @param alias identifies the pool
     * @param stateListener the new listener
     * @throws ProxoolException if we couldn't find the pool
     */
    public static void addStateListener(String alias, StateListenerIF stateListener) throws ProxoolException {
        ConnectionPool cp = ConnectionPoolManager.getInstance().getConnectionPool(alias);
        cp.addStateListener(stateListener);
    }

    /**
     * Remove a listener that monitors the change of state of the pool (quiet, busy, overloaded, or down)
     * @param alias identifies the pool
     * @param stateListener the listener to be removed.
     * @throws ProxoolException if we couldn't find the pool
     * @return wether the listnener was found and removed or not.
     */
    public boolean removeStateListener(String alias, StateListenerIF stateListener) throws ProxoolException {
        ConnectionPool cp = ConnectionPoolManager.getInstance().getConnectionPool(alias);
        return cp.removeStateListener(stateListener);
    }

    /**
     * @deprecated use {@link #addConnectionListener(String, ConnectionListenerIF)} instead.
     */
    public static void setConnectionListener(String alias, ConnectionListenerIF connectionListener) throws ProxoolException {
        addConnectionListener(alias, connectionListener);
    }

    /**
     * Add a listener that monitors each time a connection is made or destroyed.
     * @param alias identifies the pool
     * @param connectionListener the new listener
     * @throws ProxoolException if we couldn't find the pool
     */
    public static void addConnectionListener(String alias, ConnectionListenerIF connectionListener) throws ProxoolException {
        ConnectionPool cp = ConnectionPoolManager.getInstance().getConnectionPool(alias);
        cp.addConnectionListener(connectionListener);
    }

    /**
     * Remove a listener that monitors each time a connection is made or destroyed.
     * @param alias identifies the pool
     * @param connectionListener the listener to be removed
     * @throws ProxoolException if we couldn't find the pool
     * @return wether the listnener was found and removed or not.
     */
    public static boolean removeConnectionListener(String alias, ConnectionListenerIF connectionListener) throws ProxoolException {
        ConnectionPool cp = ConnectionPoolManager.getInstance().getConnectionPool(alias);
        return cp.removeConnectionListener(connectionListener);
    }

    /**
     * @deprecated use {@link #addConfigurationListener(String, ConfigurationListenerIF)} instead.
     */
    public static void setConfigurationListener(String alias, ConfigurationListenerIF configurationListener) throws ProxoolException {
        addConfigurationListener(alias, configurationListener);
    }

    /**
     * Adds a listener that gets called everytime the configuration changes.
     * @param alias identifies the pool
     * @param configurationListener the new listener
     * @throws ProxoolException if we couldn't find the pool
     */
    public static void addConfigurationListener(String alias, ConfigurationListenerIF configurationListener) throws ProxoolException {
        if (ConnectionPoolManager.getInstance().isPoolExists(alias)) {
            CompositeConfigurationListener compositeConfigurationListener = (CompositeConfigurationListener)
                    configurators.get(alias);
            if (compositeConfigurationListener == null) {
                compositeConfigurationListener = new CompositeConfigurationListener();
                configurators.put(alias, compositeConfigurationListener);
            }
            compositeConfigurationListener.addListener(configurationListener);
        } else {
            throw new ProxoolException(ConnectionPoolManager.getInstance().getKnownPools(alias));
        }
    }

    /**
     * Broadcast a configuration change
     * @param alias identifies the pool
     * @param connectionPoolDefinition the definition
     * @param completeInfo all properties
     * @param changedInfo only changed properties (since the last
     * time this method was called)
     */
    protected static void definitionUpdated(String alias, ConnectionPoolDefinitionIF connectionPoolDefinition,
                                            Properties completeInfo, Properties changedInfo) {
        CompositeConfigurationListener ccl = (CompositeConfigurationListener) configurators.get(alias);
        if (ccl != null) {
            ccl.definitionUpdated(connectionPoolDefinition, completeInfo, changedInfo);
        }
    }

    /**
     * Remove a listener that gets called everytime the configuration changes.
     * @param alias identifies the pool.
     * @param configurationListener the listener to be removed.
     * @throws ProxoolException if we couldn't find the pool
     * @return wether the listnener was found and removed or not.
     *
     */
    public static boolean removeConfigurationListener(String alias, ConfigurationListenerIF configurationListener) throws ProxoolException {
        boolean removed = false;
        if (ConnectionPoolManager.getInstance().isPoolExists(alias)) {
            CompositeConfigurationListener compositeConfigurationListener = (CompositeConfigurationListener)
                    configurators.get(alias);
            if (compositeConfigurationListener != null) {
                removed = compositeConfigurationListener.removeListener(configurationListener);
            }
        } else {
            throw new ProxoolException(ConnectionPoolManager.getInstance().getKnownPools(alias));
        }
        return removed;
    }

    /**
     * @see #killAllConnections(java.lang.String)
     */
    private static final boolean MERCIFUL = true;

    /**
     * Redefine the behaviour of the pool. All existing properties (for Proxool
     * and the delegate driver are reset to their default) and reapplied
     * based on the parameters sent here.
     *
     * @param url the url that defines the pool (or the abbreviated ""proxool.alias")
     * @param info the new properties
     * @see #updateConnectionPool
     */
    public static void redefineConnectionPool(String url, Properties info) throws ProxoolException {
        String alias = getAlias(url);
        ConnectionPool cp = ConnectionPoolManager.getInstance().getConnectionPool(alias);
        try {
            // Clone the old one
            ConnectionPoolDefinition cpd = (ConnectionPoolDefinition) cp.getDefinition().clone();
            cpd.redefine(url, info);
            cp.setDefinition(cpd);
        } catch (CloneNotSupportedException e) {
            throw new ProxoolException("Funny, why couldn't we clone a definition?", e);
        }
    }


    /**
     * Update the behaviour of the pool. Only properties that are defined here are overwritten. That is, properties
     * that were defined before but are not mentioned here are retained.
     *
     * @param url the url that defines the pool (or the abbreviated ""proxool.alias")
     * @param info the new properties
     * @see #redefineConnectionPool
     */
    public static void updateConnectionPool(String url, Properties info) throws ProxoolException {
        String alias = getAlias(url);
        ConnectionPool cp = ConnectionPoolManager.getInstance().getConnectionPool(alias);
        try {
            // Clone the old one
            ConnectionPoolDefinition cpd = (ConnectionPoolDefinition) cp.getDefinition().clone();
            cpd.update(url, info);
            cp.setDefinition(cpd);
        } catch (CloneNotSupportedException e) {
            throw new ProxoolException("Funny, why couldn't we clone a definition?", e);
        }
    }

    protected void finalize() throws Throwable {
        super.finalize();
        LOG.debug("Finalising");
    }

    /**
     * Returns the driver provided statement that Proxool wraps up before it gives it to you.
     * @return delegate statement
     * @deprecated Just cast the statement that you are given into the driver specific one.
     */
    public static Statement getDelegateStatement(Statement statement) throws ProxoolException {
        try {
            return ProxyFactory.getDelegateStatement(statement);
        } catch (IllegalArgumentException e) {
            throw new ProxoolException("Statement argument is not one provided by Proxool (it's a " + statement.getClass() + ")");
        }
    }

    /**
     * Returns the driver provided connection that Proxool wraps up before it gives it to you.
     * @return delegate connection
     * @deprecated Just cast the connection that you are given into the driver specific one.
     */
    public static Connection getDelegateConnection(Connection connection) throws ProxoolException {
        try {
            return ProxyFactory.getDelegateConnection(connection);
        } catch (IllegalArgumentException e) {
            throw new ProxoolException("Connection argument is not one provided by Proxool (it's a " + connection.getClass() + ")");
        }
    }

    /**
     * Get the connection ID for a connection
     * @param connection the connection that was served
     * @return the ID
     * @throws ProxoolException if the connection wasn't recognised.
     */
    public static long getId(Connection connection) throws ProxoolException {
        try {
            return ProxyFactory.getWrappedConnection(connection).getId();
        } catch (NullPointerException e) {
            throw new ProxoolException("Connection argument is not one provided by Proxool (it's a " + connection.getClass() + ")");
        } catch (IllegalArgumentException e) {
            throw new ProxoolException("Connection argument is not one provided by Proxool (it's a " + connection.getClass() + ")");
        }
    }

    /**
     * Get the alias for the connection pool that served a connection
     * @param connection the connection that was served
     * @return the alias
     * @throws ProxoolException if the connection wasn't recognised.
     */
    public static String getAlias(Connection connection) throws ProxoolException {
        try {
            return ProxyFactory.getWrappedConnection(connection).getAlias();
        } catch (NullPointerException e) {
            throw new ProxoolException("Connection argument is not one provided by Proxool (it's a " + connection.getClass() + ")");
        } catch (IllegalArgumentException e) {
            throw new ProxoolException("Connection argument is not one provided by Proxool (it's a " + connection.getClass() + ")");
        }
    }

    /**
     * Get a list of all the registered pools
     * @return an array of aliases
     * @since Proxool 0.7
     */
    public static String[] getAliases() {
        return ConnectionPoolManager.getInstance().getConnectionPoolNames();
    }

    /**
     * Get a particular set of performance statistics for this pool
     * @param alias identifies the pool
     * @param token identifies which set, as defined in the configuration (see {@link ConnectionPoolDefinitionIF#getStatistics definition})
     * @return a sample containing the statistics
     * @throws ProxoolException if we couldn't find the pool
     */
    public static StatisticsIF getStatistics(String alias, String token) throws ProxoolException {
        return ConnectionPoolManager.getInstance().getConnectionPool(alias).getAdmin().getStatistics(token);
    }

    /**
     * Get all the lastest performance statistics for this pool
     * @param alias identifies the pool
     * @return a sample containing the statistics, or a zero length array if there none
     * @throws ProxoolException if we couldn't find the pool
     */
    public static StatisticsIF[] getStatistics(String alias) throws ProxoolException {
        final Admin monitor = ConnectionPoolManager.getInstance().getConnectionPool(alias).getAdmin();
        if (monitor != null) {
            return monitor.getStatistics();
        } else {
            return new StatisticsIF[0];
        }
    }

    /**
     * Add a listener that receives statistics as they are produced
     * @param statisticsListener the new listener
     * @throws ProxoolException if the pool couldn't be found
     */
    public static void addStatisticsListener(String alias, StatisticsListenerIF statisticsListener) throws ProxoolException {
        // TODO investigate what happens if we add a statistics monitor after we register a listener
        final Admin monitor = ConnectionPoolManager.getInstance().getConnectionPool(alias).getAdmin();
        if (monitor != null) {
            monitor.addStatisticsListener(statisticsListener);
        } else {
            throw new ProxoolException("Statistics are switched off, your can't add a listener");
        }
    }

    /**
     * Gives a snapshot of what the pool is doing
     * @param alias identifies the pool
     * @param detail if true then include detail of each connection. Note it you ask for
     * detail then the pool must necessarily be locked for the duration it takes to gather
     * the information (which isn't very long). You probably shouldn't do it that often (like
     * not every second or something). Being locked means that connections cannot be
     * served or returned (it doesn't mean that they can't be active).
     * @return the current status of the pool
     * @throws ProxoolException if we couldn't find the pool
     */
    public static SnapshotIF getSnapshot(String alias, boolean detail) throws ProxoolException {
        SnapshotIF snapshot = null;
        ConnectionPool cp = ConnectionPoolManager.getInstance().getConnectionPool(alias);

        if (detail) {
            try {
                // Only try for 10 seconds!
                long start = System.currentTimeMillis();
                if (cp.attemptConnectionStatusReadLock(10000)) {
                    snapshot = Admin.getSnapshot(cp, cp.getDefinition(), cp.getConnectionInfos());
                } else {
                    LOG.warn("Give up waiting for detailed snapshot after " + (System.currentTimeMillis() - start) + " milliseconds. Serving standard snapshot instead.");
                }
            } finally {
                cp.releaseConnectionStatusReadLock();
            }
        }
        if (snapshot == null) {
            snapshot = Admin.getSnapshot(cp, cp.getDefinition(), null);
        }

        return snapshot;
    }

    /**
     * Calls {@link #getSnapshot(java.lang.String, boolean) getSnapshot}
     * using false for the detail parameter.
     * @see #getSnapshot(java.lang.String, boolean)
     */
    public static SnapshotIF getSnapshot(String alias) throws ProxoolException {
        return getSnapshot(alias, false);
    }

    // all jmx operations are done through reflection
    // to avoid making the facade dependant on the JMX classes
    private static boolean registerForJmx(String alias, Properties properties) {
        boolean success = false;
        try {
            Class jmxHelperClass = Class.forName("org.logicalcobwebs.proxool.admin.jmx.ProxoolJMXHelper");
            Method registerMethod = jmxHelperClass.getDeclaredMethod("registerPool", new Class[]{String.class, Properties.class});
            registerMethod.invoke(null, new Object[]{alias, properties});
            success = true;
        } catch (Exception e) {
            LOG.error("JMX registration of " + alias + " pool failed.", e);
        }
        return success;
    }

    // all JNDI operations are done through reflection
    // to avoid making the facade dependant on the JNDI classes
    private static boolean registerDataSource(String alias, Properties jndiProperties) {
        boolean success = false;
        try {
            Class jndiHelperClass = Class.forName("org.logicalcobwebs.proxool.admin.jndi.ProxoolJNDIHelper");
            Method registerMethod = jndiHelperClass.getDeclaredMethod("registerDatasource", new Class[]{String.class,
                Properties.class});
            registerMethod.invoke(null, new Object[]{alias, jndiProperties});
            success = true;
        } catch (Exception e) {
            LOG.error("JNDI DataSource binding of " + alias + " pool failed.", e);
        }
        return success;
    }

    /**
     * Get the JNDI properties for the given pool definition if it is configured for JNDI registration.
     * Will remove generic JNDI properties from the delegate properties so that they will not be passed to the
     * delegate driver.
     * @param connectionPoolDefinition the pool definition to get the eventual JNDI configuration from.
     * @return the JNDI properties, or <code>null</code> if the given definition was not configured for JNDI.
     */
    private static Properties extractJndiProperties(ConnectionPoolDefinition connectionPoolDefinition) {
        if (connectionPoolDefinition.getJndiName() == null) {
            return null;
        }
        Properties jndiProperties = new Properties();
        jndiProperties.setProperty(ProxoolConstants.JNDI_NAME, connectionPoolDefinition.getJndiName());
        if (connectionPoolDefinition.getDelegateProperties() != null) {
            Properties delegateProperties = connectionPoolDefinition.getDelegateProperties();
            // we must retrieve all the relevant property names before removing them from
            // the given properties to avoid ConcurrentModificationException
            String propertyName = null;
            List propertyNamesList = new ArrayList(10);
            Iterator keySetIterator = delegateProperties.keySet().iterator();
            while (keySetIterator.hasNext()) {
                propertyName = (String) keySetIterator.next();
                if (propertyName.startsWith(ProxoolConstants.JNDI_PROPERTY_PREFIX)) {
                    propertyNamesList.add(propertyName);
                }
            }
            for (int i = 0; i < propertyNamesList.size(); i++) {
                propertyName = (String) propertyNamesList.get(i);
                if (propertyName.startsWith(ProxoolConstants.JNDI_PROPERTY_PREFIX)) {
                    jndiProperties.setProperty(propertyName.substring(ProxoolConstants.JNDI_PROPERTY_PREFIX.length()),
                        (String) delegateProperties.getProperty(propertyName));
                    delegateProperties.remove(propertyName);
                }
            }
        }
        return jndiProperties;
    }

    /**
     * Get wether the given pool properties contains configuration for JMX instrumentation of the pool.
     * @param poolProperties the properties to check for JMX configuration.
     * @return wether the given pool properties contains configuration for JMX instrumentation or not.
     */
    private static boolean isConfiguredForJMX(Properties poolProperties) {
        final String jmxProperty = poolProperties.getProperty(ProxoolConstants.JMX_PROPERTY);
        if (jmxProperty != null && jmxProperty.equalsIgnoreCase("true")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * By remembering the most recent {@link ShutdownHook} ProxoolFacade
     * will know to disable it when it is {@link #shutdown}. It will gracefully
     * cope with the fact that it may be shutting down by the request of the
     * sutdownHook. If you don't do this and do several "hot deploys" then you
     * end up with a series of shutdown hooks. We only every want one.
     * @param t the thread that will be run as a shutdown hook
     * @see ShutdownHook
     */
    protected static void setShutdownHook(Thread t) {
        shutdownHook = t;
    }
}

/*
 Revision history:
 $Log: ProxoolFacade.java,v $
 Revision 1.85  2006/11/02 10:00:34  billhorsman
 Added ProxoolFacade.disableShutdownHook.

 Revision 1.84  2006/01/18 14:40:01  billhorsman
 Unbundled Jakarta's Commons Logging.

 Revision 1.83  2005/09/26 09:54:14  billhorsman
 Avoid suspected deadlock when getting a detailed snapshot. Only attempt to get the concurrent lock for 10 seconds before giving up.

 Revision 1.82  2005/05/04 16:32:31  billhorsman
 Clone the definition when redefining or updating the pool.

 Revision 1.81  2004/09/29 15:43:25  billhorsman
 Recheck isPoolExists inside synchronized registerConnectionPool method. Credit Juergen Hoeller.

 Revision 1.80  2004/06/02 20:47:05  billhorsman
 Override shutdown with a zero-parameter version for Spring integration.

 Revision 1.79  2004/03/26 15:58:56  billhorsman
 Fixes to ensure that house keeper and prototyper threads finish after shutdown.

 Revision 1.78  2004/03/23 21:25:54  billhorsman
 Added getAlias() call.

 Revision 1.77  2004/03/23 21:19:45  billhorsman
 Added disposable wrapper to proxied connection. And made proxied objects implement delegate interfaces too.

 Revision 1.76  2004/03/15 02:45:19  chr32
 Added handling of Proxool managed JNDI DataSources.

 Revision 1.75  2004/02/12 12:54:49  billhorsman
 Fix merciful/forceExpiry confusion

 Revision 1.74  2003/10/30 00:16:13  billhorsman
 Throw a friendlier exception if you try and add a statistics listener to a pool with no statistics

 Revision 1.73  2003/10/16 18:52:35  billhorsman
 Fixed a bug: the redefine() method was actually calling the update() method. Also, added checks to make the
 "Attempt to use a pool with incomplete definition" exception a bit more descriptive. It's often because you
 are referring to an unregistered pool simply by using an alias.

 Revision 1.72  2003/09/07 22:09:21  billhorsman
 Remove any registered ShutdownHooks during shutdown.

 Revision 1.71  2003/08/30 14:54:04  billhorsman
 Checkstyle

 Revision 1.70  2003/08/28 10:55:49  billhorsman
 comment out JNDI stuff for now

 Revision 1.69  2003/08/27 18:03:20  billhorsman
 added new getDelegateConnection() method

 Revision 1.68  2003/07/23 12:38:50  billhorsman
 some fixes, but more to come

 Revision 1.67  2003/07/23 06:54:48  billhorsman
 draft JNDI changes (shouldn't effect normal operation)

 Revision 1.66  2003/04/10 21:49:34  billhorsman
 refactored registration slightly to allow DataSource access

 Revision 1.65  2003/03/11 14:51:53  billhorsman
 more concurrency fixes relating to snapshots

 Revision 1.64  2003/03/10 15:26:49  billhorsman
 refactoringn of concurrency stuff (and some import
 optimisation)

 Revision 1.63  2003/03/03 11:11:58  billhorsman
 fixed licence

 Revision 1.62  2003/02/28 10:42:59  billhorsman
 ConnectionPoolManager now passes ProxoolFacade an
 array of ConnectionPools rather than a Collection
 to avoid a ConcurrentModificationException during
 shutdown.

 Revision 1.61  2003/02/27 17:19:18  billhorsman
 new overloaded getSnapshot method

 Revision 1.60  2003/02/26 16:05:53  billhorsman
 widespread changes caused by refactoring the way we
 update and redefine pool definitions.

 Revision 1.59  2003/02/24 18:03:24  chr32
 Added JMX operations.

 Revision 1.58  2003/02/24 01:15:33  chr32
 Added support for ProxoolListenerIF.

 Revision 1.57  2003/02/19 23:46:10  billhorsman
 renamed monitor package to admin

 Revision 1.56  2003/02/19 13:48:28  chr32
 Added 'removeConfigurationListener' method.

 Revision 1.55  2003/02/18 16:50:00  chr32
 Added possibility to remove connection and state listeners.

 Revision 1.54  2003/02/14 14:19:42  billhorsman
 automatically load ProxoolDriver

 Revision 1.53  2003/02/14 13:26:23  billhorsman
 better exception for incorrect url

 Revision 1.52  2003/02/12 12:28:27  billhorsman
 added url, proxyHashcode and delegateHashcode to
 ConnectionInfoIF

 Revision 1.51  2003/02/07 17:26:05  billhorsman
 deprecated removeAllConnectionPools in favour of
 shutdown (and dropped unreliable finalize() method)

 Revision 1.50  2003/02/07 15:12:41  billhorsman
 fix statisticsLogLevel property recognition again

 Revision 1.49  2003/02/07 14:45:40  billhorsman
 fix statisticsLogLevel property recognition

 Revision 1.48  2003/02/07 14:16:46  billhorsman
 support for StatisticsListenerIF

 Revision 1.47  2003/02/07 10:27:47  billhorsman
 change in shutdown procedure to allow re-registration

 Revision 1.46  2003/02/07 01:48:15  chr32
 Started using new composite listeners.

 Revision 1.45  2003/02/06 17:41:04  billhorsman
 now uses imported logging

 Revision 1.44  2003/02/06 15:46:43  billhorsman
 checkstyle

 Revision 1.43  2003/02/06 15:41:16  billhorsman
 add statistics-log-level

 Revision 1.42  2003/02/05 17:05:02  billhorsman
 registerConnectionPool is now synchronized

 Revision 1.41  2003/02/05 00:20:01  billhorsman
 copes with pools with no statistics

 Revision 1.40  2003/02/04 17:18:29  billhorsman
 move ShutdownHook init code

 Revision 1.39  2003/02/04 15:04:17  billhorsman
 New ShutdownHook

 Revision 1.38  2003/01/31 16:53:17  billhorsman
 checkstyle

 Revision 1.37  2003/01/31 11:50:39  billhorsman
 changes for snapshot improvements

 Revision 1.36  2003/01/31 00:18:27  billhorsman
 statistics is now a string to allow multiple,
 comma-delimited values (plus allow access to all
 statistics)

 Revision 1.35  2003/01/30 17:50:28  billhorsman
 spelling

 Revision 1.34  2003/01/30 17:48:50  billhorsman
 configuration listener now linked to alias not url

 Revision 1.33  2003/01/30 17:22:23  billhorsman
 add statistics support

 Revision 1.32  2003/01/27 18:26:36  billhorsman
 refactoring of ProxyConnection and ProxyStatement to
 make it easier to write JDK 1.2 patch

 Revision 1.31  2003/01/23 11:08:26  billhorsman
 new setConfiguratorListener method (and remove from optional
 parameter when registering pool)

 Revision 1.30  2003/01/19 15:21:07  billhorsman
 doc

 Revision 1.29  2003/01/18 15:13:12  billhorsman
 Signature changes (new ProxoolException
 thrown) on the ProxoolFacade API.

 Revision 1.28  2003/01/17 00:38:12  billhorsman
 wide ranging changes to clarify use of alias and url -
 this has led to some signature changes (new exceptions
 thrown) on the ProxoolFacade API.

 Revision 1.27  2003/01/15 14:51:40  billhorsman
 checkstyle

 Revision 1.26  2003/01/15 12:20:06  billhorsman
 deprecated getConnectionPoolStatisticsDump

 Revision 1.25  2003/01/14 23:50:58  billhorsman
 logs version

 Revision 1.24  2002/12/16 17:15:03  billhorsman
 fix for url

 Revision 1.23  2002/12/16 16:47:22  billhorsman
 fix for updating properties with zero length strings

 Revision 1.22  2002/12/16 16:42:30  billhorsman
 allow URL updates to pool

 Revision 1.21  2002/12/16 11:46:00  billhorsman
 send properties to definitionUpdated

 Revision 1.20  2002/12/16 11:16:51  billhorsman
 checkstyle

 Revision 1.19  2002/12/16 11:15:19  billhorsman
 fixed getDelegateStatement

 Revision 1.18  2002/12/16 10:57:48  billhorsman
 add getDelegateStatement to allow access to the
 delegate JDBC driver's Statement

 Revision 1.17  2002/12/12 10:49:43  billhorsman
 now includes properties in definitionChanged event

 Revision 1.16  2002/12/04 13:19:43  billhorsman
 draft ConfigurationListenerIF stuff for persistent configuration

 Revision 1.15  2002/11/13 19:12:24  billhorsman
 fix where update properties weren't being recognised
 when the properties object was the same as the original

 Revision 1.14  2002/11/13 11:28:38  billhorsman
 trace property wasn't being configured

 Revision 1.13  2002/11/09 15:56:31  billhorsman
 log if serConnectionListener couldn't find pool

 Revision 1.12  2002/11/03 10:46:57  billhorsman
 hide passwords in log

 Revision 1.11  2002/11/02 13:57:33  billhorsman
 checkstyle

 Revision 1.10  2002/10/29 08:54:45  billhorsman
 fix to getAlias so that it correctly extracts alias from "proxool.alias" form

 Revision 1.9  2002/10/28 19:43:30  billhorsman
 configuring of pool now gets logged to that pool's logger (rather than general log)

 Revision 1.8  2002/10/27 13:29:38  billhorsman
 deprecated debug-level in favour of verbose

 Revision 1.7  2002/10/27 13:01:23  billhorsman
 layout

 Revision 1.6  2002/10/25 15:59:32  billhorsman
 made non-public where possible

 Revision 1.5  2002/10/25 10:12:52  billhorsman
 Improvements and fixes to the way connection pools close down. Including new ReloadMonitor to detect when a class is reloaded. Much better logging too.

 Revision 1.4  2002/10/24 17:40:31  billhorsman
 Fixed recognition of existing pool (which was resulting in an extra configuration step - but which didn't cause any problems)

 Revision 1.3  2002/10/23 21:04:36  billhorsman
 checkstyle fixes (reduced max line width and lenient naming convention

 Revision 1.2  2002/10/17 15:27:31  billhorsman
 better reporting of property settings

 Revision 1.1.1.1  2002/09/13 08:13:19  billhorsman
 new

 Revision 1.11  2002/08/24 20:07:28  billhorsman
 removed debug to stdout

 Revision 1.10  2002/08/24 19:44:13  billhorsman
 fixes for logging

 Revision 1.9  2002/07/10 16:14:47  billhorsman
 widespread layout changes and move constants into ProxoolConstants

 Revision 1.8  2002/07/05 13:24:32  billhorsman
 doc

 Revision 1.7  2002/07/04 09:04:20  billhorsman
 Now throws an SQLException if you ask for a ConnectionPoolDefinition, ConnectionPoolStatistics that doesn't exist. This makes it easier to show the list of possible ones you can choose.

 Revision 1.6  2002/07/02 11:19:08  billhorsman
 layout code and imports

 Revision 1.5  2002/07/02 11:14:26  billhorsman
 added test (andbug fixes) for FileLogger

 Revision 1.4  2002/07/02 09:13:08  billhorsman
 removed redundant import

 Revision 1.3  2002/07/02 08:52:42  billhorsman
 Added lots more methods and moved configuration stuff here

 Revision 1.2  2002/06/28 11:19:47  billhorsman
 improved doc

*/
