/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import org.logicalcobwebs.logging.Log;
import org.logicalcobwebs.logging.LogFactory;
import org.logicalcobwebs.proxool.admin.Admin;
import org.logicalcobwebs.proxool.admin.SnapshotIF;
import org.logicalcobwebs.proxool.admin.StatisticsIF;
import org.logicalcobwebs.proxool.admin.StatisticsListenerIF;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.lang.reflect.Method;
import java.sql.Statement;
import java.sql.Connection;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Hashtable;

/**
 * <p>This provides some nice-to-have features that can't be provided by the
 * {@link java.sql.Driver} {@link ProxoolDriver implementation} of java.sql.Driver. Like starting up
 * a pool before you need a connection. And getting statistical information.</p>
 *
 * <p>You need to use this class wisely. It is obviously specfic to proxool so it will
 * stop you switching to another driver. Consider isolating the code that calls this
 * class so that you can easily remove it if you have to.</p>
 *
 * @version $Revision: 1.70 $, $Date: 2003/08/28 10:55:49 $
 * @author billhorsman
 * @author $Author: billhorsman $ (current maintainer)
 */
public class ProxoolFacade {

    private static final Log LOG = LogFactory.getLog(ProxoolFacade.class);

    private static Map configurators = new HashMap();

    private static CompositeProxoolListener compositeProxoolListener = new CompositeProxoolListener();

    private static boolean versionLogged = false;

    /**
     * Build a ConnectionPool based on this definition and then start it.
     * @param url defines the delegate driver and delegate url.
     * @param info the properties used to configure Proxool (and any for the delegate driver too) - optional
     * @return the alias for this pool (or the full url if no alias is specified)
     * @throws ProxoolException if anything goes wrong
     */
    public static synchronized String registerConnectionPool(String url, Properties info) throws ProxoolException {
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
            ConnectionPoolDefinition cpd = new ConnectionPoolDefinition(url, info);
            registerConnectionPool(cpd);
        } else {
            throw new ProxoolException("Attempt to register duplicate pool called '" + alias + "'");
        }

        return alias;
    }

    protected static void registerConnectionPool(ConnectionPoolDefinition connectionPoolDefinition) throws ProxoolException {
        ConnectionPool connectionPool = ConnectionPoolManager.getInstance().createConnectionPool(connectionPoolDefinition);
        connectionPool.start();
        compositeProxoolListener.onRegistration(connectionPoolDefinition, connectionPoolDefinition.getCompleteInfo());
        if (isConfiguredForJMX(connectionPoolDefinition.getCompleteInfo())) {
            registerForJmx(connectionPoolDefinition.getAlias(), connectionPoolDefinition.getCompleteInfo());
        }
/*
// Start JNDI
        if (connectionPoolDefinition.getJndiName() != null) {
            try {
                Hashtable env = new Hashtable();
                if (connectionPoolDefinition.getInitialContextFactory() != null) {
                    env.put(Context.INITIAL_CONTEXT_FACTORY, connectionPoolDefinition.getInitialContextFactory());
                }
                if (connectionPoolDefinition.getProviderUrl() != null) {
                    env.put(Context.PROVIDER_URL, connectionPoolDefinition.getProviderUrl());
                }
                if (connectionPoolDefinition.getSecurityAuthentication() != null) {
                    env.put(Context.SECURITY_AUTHENTICATION, connectionPoolDefinition.getSecurityAuthentication());
                }
                if (connectionPoolDefinition.getSecurityPrincipal() != null) {
                    env.put(Context.SECURITY_PRINCIPAL, connectionPoolDefinition.getSecurityPrincipal());
                }
                if (connectionPoolDefinition.getSecurityCredentials() != null) {
                    env.put(Context.SECURITY_CREDENTIALS, connectionPoolDefinition.getSecurityCredentials());
                }
                Context context = new InitialContext(env);
                ProxoolDataSource proxoolDataSource = new ProxoolDataSource(connectionPoolDefinition.getAlias());
                context.bind(connectionPoolDefinition.getJndiName(), proxoolDataSource);
            } catch (NamingException e) {
                LOG.error("Couldn't bind " + connectionPoolDefinition.getJndiName() + " to JNDI context", e);
                throw new ProxoolException("Problem registering with JNDI", e);
            }
        }
// End JNDI
*/
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
        return ConnectionPoolManager.getInstance().getConnectionPool(alias).expireConnection(id, merciful);
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
     * Redfine the behaviour of the pool. All existing properties (for Proxool
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
        ConnectionPoolDefinition cpd = cp.getDefinition();
        cpd.update(url, info);
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
        ConnectionPoolDefinition cpd = cp.getDefinition();
        cpd.update(url, info);
    }

    protected void finalize() throws Throwable {
        super.finalize();
        LOG.debug("Finalising");
    }

    /**
     * When you ask a connection for a {@link Statement  Statement} (or a
     * {@link java.sql.PreparedStatement PreparedStatement} or a
     * {@link java.sql.CallableStatement CallableStatement} then you
     * don't actually  get the Statement passed to you from the delegate
     * Driver. It isn't recommended, but if you need to use any driver
     * specific methods then this is your only way.
     * @return delegate statement
     */
    public static Statement getDelegateStatement(Statement statement) throws ProxoolException {
        try {
            return ProxyFactory.getDelegateStatement(statement);
        } catch (IllegalArgumentException e) {
            throw new ProxoolException("Statement argument is not one provided by Proxool (it's a " + statement.getClass() + ")");
        }
    }

    /**
     * When you ask for a  connection then you don't actually get the Connection
     * created by the delegate Driver. It isn't recommended, but if you need to
     * use any driver specific methods then this is your only way.
     * @return delegate connection
     */
    public static Connection getDelegateConnection(Connection connection) throws ProxoolException {
        try {
            return ProxyFactory.getDelegateConnection(connection);
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
        final Admin monitor = ConnectionPoolManager.getInstance().getConnectionPool(alias).getAdmin();
        monitor.addStatisticsListener(statisticsListener);
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
                cp.acquireConnectionStatusReadLock();
                LOG.debug("Starting snapshot");
                snapshot = Admin.getSnapshot(cp, cp.getDefinition(), cp.getConnectionInfos());
                LOG.debug("Finishing snapshot");
            } finally {
                cp.releaseConnectionStatusReadLock();
            }
        } else {
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
}

/*
 Revision history:
 $Log: ProxoolFacade.java,v $
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
