/*
 * This software is released under the Apache Software Licence. See
 * package.html for details. The latest version is available at
 * http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import org.logicalcobwebs.logging.Log;
import org.logicalcobwebs.logging.LogFactory;
import org.logicalcobwebs.proxool.monitor.StatisticsIF;
import org.logicalcobwebs.proxool.monitor.SnapshotIF;
import org.logicalcobwebs.proxool.monitor.Monitor;
import org.logicalcobwebs.proxool.monitor.StatisticsListenerIF;

import java.sql.Statement;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.Comparator;
import java.util.Date;

/**
 * <p>This provides some nice-to-have features that can't be provided by the
 * {@link java.sql.Driver} {@link ProxoolDriver implementation} of java.sql.Driver. Like starting up
 * a pool before you need a connection. And getting statistical information.</p>
 *
 * <p>You need to use this class wisely. It is obviously specfic to proxool so it will
 * stop you switching to another driver. Consider isolating the code that calls this
 * class so that you can easily remove it if you have to.</p>
 *
 * @version $Revision: 1.55 $, $Date: 2003/02/18 16:50:00 $
 * @author billhorsman
 * @author $Author: chr32 $ (current maintainer)
 */
public class ProxoolFacade {

    private static final Log LOG = LogFactory.getLog(ProxoolFacade.class);

    private static Map infos = new HashMap();

    private static Map completeInfos = new HashMap();

    private static Map configurators = new HashMap();

    /**
     * Build a ConnectionPool based on this definition and then start it.
     * @param url defines the delegate driver and delegate url.
     * @param info the properties used to configure Proxool (and any for the delegate driver too) - optional
     * @return the alias for this pool (or the full url if no alias is specified)
     * @throws ProxoolException if anything goes wrong
     */
    public static synchronized String registerConnectionPool(String url, Properties info) throws ProxoolException {
        String alias = getAlias(url);

        try {
            Class.forName(ProxoolDriver.class.getName());
        } catch (ClassNotFoundException e) {
            LOG.error("Couldn't load " + ProxoolDriver.class.getName());
        }

        if (!ConnectionPoolManager.getInstance().isPoolExists(alias)) {
            ConnectionPoolDefinition cpd = new ConnectionPoolDefinition();
            cpd.setAlias(alias);
            definePool(url, cpd, info);

            // Check for minimum information
            if (cpd.getUrl() == null || cpd.getDriver() == null) {
                throw new ProxoolException("The URL is not defined properly.");
            }

            ConnectionPool connectionPool = ConnectionPoolManager.getInstance().createConnectionPool(cpd);
            connectionPool.start();
        } else {
            throw new ProxoolException("Attempt to register duplicate pool called '" + alias + "'");
        }

        return alias;
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
     *  Translates from properties to definition
     *
     * @param url the connection we are defining
     * @param cpd The defintion to populate (can have existing settings)
     * @param info the properties object to read from
     * @return the alias
     * @throws ProxoolException if there were any validation errors.
     */
    protected static String definePool(String url, ConnectionPoolDefinition cpd,
                                       Properties info) throws ProxoolException {

        Properties rememberedInfo = null;
        Properties changedProperties = null;
        final String alias = getAlias(url);
        rememberedInfo = (Properties) infos.get(alias);

        Properties completeInfo = (Properties) completeInfos.get(alias);
        if (completeInfo == null) {
            completeInfo = new Properties();
            completeInfos.put(alias, completeInfo);
        }

        cpd.setCompleteUrl(url);

        Log earlyLog = LogFactory.getLog("org.logicalcobwebs.proxool." + alias);

        if (cpd.getDriver() == null) {
            earlyLog.info("Proxool " + Version.getVersion());
        }

        try {
            int endOfPrefix = url.indexOf(':');
            int endOfDriver = url.indexOf(':', endOfPrefix + 1);

            if (endOfPrefix > -1 && endOfDriver > -1) {
                final String driver = url.substring(endOfPrefix + 1, endOfDriver);
                if (cpd.getDriver() == null) {
                    cpd.setDriver(driver);
                    earlyLog.debug("Setting driver for " + alias + " pool to " + driver);
                } else if (cpd.getDriver() != null && !cpd.getDriver().equals(driver)) {
                    cpd.setDriver(driver);
                    earlyLog.debug("Updating driver for " + alias + " pool to " + driver);
                }
                final String delegateUrl = url.substring(endOfDriver + 1);
                if (cpd.getUrl() == null) {
                    cpd.setUrl(delegateUrl);
                    earlyLog.debug("Setting url for " + alias + " pool to " + delegateUrl);
                } else if (cpd.getUrl() != null && !cpd.getUrl().equals(delegateUrl)) {
                    cpd.setUrl(delegateUrl);
                    earlyLog.debug("Updating url for " + alias + " pool to " + delegateUrl);
                }
            } else {
                // Using alias. Nothing to do
            }
        } catch (IndexOutOfBoundsException e) {
            LOG.error("Invalid URL " + url, e);
            throw new ProxoolException("Invalid URL format.");
        }

        if (info != null && (rememberedInfo == null || !info.equals(rememberedInfo))) {

            if (earlyLog.isDebugEnabled()) {
                if (rememberedInfo == null) {
                    earlyLog.debug("Setting properties on " + url);
                } else {
                    earlyLog.debug("Updating properties on " + url);
                }
            }

            changedProperties = new Properties();

            Iterator i = info.keySet().iterator();
            while (i.hasNext()) {
                String key = (String) i.next();
                String value = info.getProperty(key);

                completeInfo.setProperty(key, value);

                boolean isProxoolProperty = setProperty(key, cpd, value, changedProperties, earlyLog);

                if (earlyLog.isDebugEnabled()) {
                    if (isProxoolProperty) {
                        earlyLog.debug("Recognised proxool property: " + key + "=" + value);
                    } else {
                        if (key.toLowerCase().indexOf("password") > -1) {
                            earlyLog.debug("Delgating property to Driver: " + key + "=" + "*******");
                        } else {
                            earlyLog.debug("Delegating property to Driver: " + key + "=" + value);
                        }
                    }
                }

            }

            // Clone the property. Otherwise we won't detect changes if the
            // same properties object is passed back in (but with different
            // content)
            Properties clone = new Properties();
            Enumeration e = info.propertyNames();
            while (e.hasMoreElements()) {
                String key = (String) e.nextElement();
                String value = info.getProperty(key);
                clone.setProperty(key, value);
            }

            infos.put(alias, clone);
        }

        ConfigurationListenerIF configurationListener = (ConfigurationListenerIF) configurators.get(alias);
        if (configurationListener != null) {
            configurationListener.defintionUpdated(cpd, completeInfo, changedProperties);
        }

        return cpd.getAlias();
    }

    private static boolean setProperty(String key, ConnectionPoolDefinition cpd, String value, Properties changedProperties, Log earlyLog) throws ProxoolException {
        boolean isProxoolProperty = true;
        if (key.equals(ProxoolConstants.USER_PROPERTY)) {
            isProxoolProperty = false;
            if (isChanged(cpd.getUser(), value)) {
                changedProperties.setProperty(key, value);
                cpd.setUser(value);
            }
        } else if (key.equals(ProxoolConstants.PASSWORD_PROPERTY)) {
            isProxoolProperty = false;
            if (isChanged(cpd.getPassword(), value)) {
                changedProperties.setProperty(key, value);
                cpd.setPassword(value);
            }
        } else if (key.equals(ProxoolConstants.HOUSE_KEEPING_SLEEP_TIME_PROPERTY)) {
            try {
                int valueAsInt = Integer.parseInt(value);
                if (cpd.getHouseKeepingSleepTime() != valueAsInt) {
                    changedProperties.setProperty(key, value);
                    cpd.setHouseKeepingSleepTime(valueAsInt);
                }
            } catch (NumberFormatException e) {
                throw new ProxoolException("'" + key + "' property must be an integer. Found '" + value + "' instead.");
            }
        } else if (key.equals(ProxoolConstants.HOUSE_KEEPING_TEST_SQL_PROPERTY)) {
            if (isChanged(cpd.getHouseKeepingTestSql(), value)) {
                changedProperties.setProperty(key, value);
                cpd.setHouseKeepingTestSql(value);
            }
        } else if (key.equals(ProxoolConstants.MAXIMUM_CONNECTION_COUNT_PROPERTY)) {
            try {
                int valueAsInt = Integer.parseInt(value);
                if (cpd.getMaximumConnectionCount() != valueAsInt) {
                    changedProperties.setProperty(key, value);
                    cpd.setMaximumConnectionCount(valueAsInt);
                }
            } catch (NumberFormatException e) {
                throw new ProxoolException("'" + key + "' property must be an integer. Found '" + value + "' instead.");
            }
        } else if (key.equals(ProxoolConstants.MAXIMUM_CONNECTION_LIFETIME_PROPERTY)) {
            try {
                int valueAsInt = Integer.parseInt(value);
                if (cpd.getMaximumConnectionLifetime() != valueAsInt) {
                    changedProperties.setProperty(key, value);
                    cpd.setMaximumConnectionLifetime(valueAsInt);
                }
            } catch (NumberFormatException e) {
                throw new ProxoolException("'" + key + "' property must be an integer. Found '" + value + "' instead.");
            }
        } else if (key.equals(ProxoolConstants.MAXIMUM_NEW_CONNECTIONS_PROPERTY)) {
            try {
                int valueAsInt = Integer.parseInt(value);
                if (cpd.getMaximumNewConnections() != valueAsInt) {
                    changedProperties.setProperty(key, value);
                    cpd.setMaximumNewConnections(valueAsInt);
                }
            } catch (NumberFormatException e) {
                throw new ProxoolException("'" + key + "' property must be an integer. Found '" + value + "' instead.");
            }
        } else if (key.equals(ProxoolConstants.MINIMUM_CONNECTION_COUNT_PROPERTY)) {
            try {
                int valueAsInt = Integer.parseInt(value);
                if (cpd.getMinimumConnectionCount() != valueAsInt) {
                    changedProperties.setProperty(key, value);
                    cpd.setMinimumConnectionCount(valueAsInt);
                }
            } catch (NumberFormatException e) {
                throw new ProxoolException("'" + key + "' property must be an integer. Found '" + value + "' instead.");
            }
        } else if (key.equals(ProxoolConstants.PROTOTYPE_COUNT_PROPERTY)) {
            try {
                int valueAsInt = Integer.parseInt(value);
                if (cpd.getPrototypeCount() != valueAsInt) {
                    changedProperties.setProperty(key, value);
                    cpd.setPrototypeCount(valueAsInt);
                }
            } catch (NumberFormatException e) {
                throw new ProxoolException("'" + key + "' property must be an integer. Found '" + value + "' instead.");
            }
        } else if (key.equals(ProxoolConstants.RECENTLY_STARTED_THRESHOLD_PROPERTY)) {
            try {
                int valueAsInt = Integer.parseInt(value);
                if (cpd.getRecentlyStartedThreshold() != valueAsInt) {
                    changedProperties.setProperty(key, value);
                    cpd.setRecentlyStartedThreshold(valueAsInt);
                }
            } catch (NumberFormatException e) {
                throw new ProxoolException("'" + key + "' property must be an integer. Found '" + value + "' instead.");
            }
        } else if (key.equals(ProxoolConstants.OVERLOAD_WITHOUT_REFUSAL_LIFETIME_PROPERTY)) {
            try {
                int valueAsInt = Integer.parseInt(value);
                if (cpd.getOverloadWithoutRefusalLifetime() != valueAsInt) {
                    changedProperties.setProperty(key, value);
                    cpd.setOverloadWithoutRefusalLifetime(valueAsInt);
                }
            } catch (NumberFormatException e) {
                throw new ProxoolException("'" + key + "' property must be an integer. Found '" + value + "' instead.");
            }
        } else if (key.equals(ProxoolConstants.MAXIMUM_ACTIVE_TIME_PROPERTY)) {
            try {
                int valueAsInt = Integer.parseInt(value);
                if (cpd.getMaximumActiveTime() != valueAsInt) {
                    changedProperties.setProperty(key, value);
                    cpd.setMaximumActiveTime(valueAsInt);
                }
            } catch (NumberFormatException e) {
                throw new ProxoolException("'" + key + "' property must be an integer. Found '" + value + "' instead.");
            }
        } else if (key.equals(ProxoolConstants.DEBUG_LEVEL_PROPERTY)) {
            if (value != null && value.equals("1")) {
                earlyLog.warn("Use of " + ProxoolConstants.DEBUG_LEVEL_PROPERTY + "=1 is deprecated. Use " + ProxoolConstants.VERBOSE_PROPERTY + "=true instead.");
                if (!cpd.isVerbose()) {
                    changedProperties.setProperty(key, value);
                    cpd.setVerbose(true);
                }
            } else {
                earlyLog.warn("Use of " + ProxoolConstants.DEBUG_LEVEL_PROPERTY + "=0 is deprecated. Use " + ProxoolConstants.VERBOSE_PROPERTY + "=false instead.");
                if (cpd.isVerbose()) {
                    changedProperties.setProperty(key, value);
                    cpd.setVerbose(false);
                }
            }
        } else if (key.equals(ProxoolConstants.VERBOSE_PROPERTY)) {
            final boolean valueAsBoolean = Boolean.valueOf(value).booleanValue();
            if (cpd.isVerbose() != valueAsBoolean) {
                changedProperties.setProperty(key, value);
                cpd.setVerbose(valueAsBoolean);
            }
        } else if (key.equals(ProxoolConstants.TRACE_PROPERTY)) {
            cpd.setTrace(Boolean.valueOf(value).booleanValue());
        } else if (key.equals(ProxoolConstants.FATAL_SQL_EXCEPTION_PROPERTY)) {
            cpd.setFatalSqlException(value);
        } else if (key.equals(ProxoolConstants.STATISTICS_PROPERTY)) {
            if (isChanged(cpd.getStatistics(), value)) {
                changedProperties.setProperty(key, value);
                cpd.setStatistics(value);
            }
        } else if (key.equals(ProxoolConstants.STATISTICS_LOG_LEVEL_PROPERTY)) {
            if (isChanged(cpd.getStatisticsLogLevel(), value)) {
                changedProperties.setProperty(key, value);
                cpd.setStatisticsLogLevel(value);
            }
        } else {
            cpd.setProperty(key, value);
            isProxoolProperty = false;
        }
        return isProxoolProperty;
    }

    private static boolean isChanged(String oldValue, String newValue) {
        boolean changed = false;
        if (oldValue == null) {
            if (newValue != null) {
                changed = true;
            }
        } else if (newValue == null) {
            changed = true;
        } else if (!oldValue.equals(newValue)) {
            changed = true;
        }
        return changed;
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
                connectionPool.shutdown(delay, finalizer);
            } catch (Throwable t) {
                LOG.error("Problem trying to shutdown '" + alias + "' connection pool", t);
            }
        }
        connectionPool = null;
    }

    /**
     * When a connection pool is removed then we need to forget the cached information
     * we hold for that alias: namely, the properties that get reused for each connection.
     * @param alias to identify the pool we are removing
     */
    protected static void forgetAlias(String alias) {
        infos.remove(alias);
        completeInfos.remove(alias);
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

        Iterator connectionPools = ConnectionPoolManager.getInstance().getConnectionPoolMap().iterator();
        while (connectionPools.hasNext()) {
            ConnectionPool cp = (ConnectionPool) connectionPools.next();
            removeConnectionPool(finalizer, cp, delay);
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
     */
    public static void killAllConnections(String alias, boolean merciful) throws ProxoolException {
        ConnectionPoolManager.getInstance().getConnectionPool(alias).expireAllConnections(merciful);
    }

    /**
     * Like {@link #killAllConnections} but defaults to merciful.
     * @param alias to identify the pool
     * @throws ProxoolException if we couldn't find the pool
     */
    public static void killAllConnections(String alias) throws ProxoolException {
        killAllConnections(alias, MERCIFUL);
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
     * @see #killAllConnections(java.lang.String)
     */
    private static final boolean MERCIFUL = true;

    /**
     * Update the behaviour of the pool. Only properties that are defined here are overwritten. That is, properties
     * that were defined before but are not mentioned here are retained.
     *
     * @param url the url that defines the pool (or the abbreviated ""proxool.alias")
     * @param info the new properties
     */
    public static void updateConnectionPool(String url, Properties info) throws ProxoolException {
        String alias = getAlias(url);
        ConnectionPool cp = ConnectionPoolManager.getInstance().getConnectionPool(alias);
        ConnectionPoolDefinition cpd = cp.getDefinition();
        definePool(url, cpd, info);

    }

    protected void finalize() throws Throwable {
        super.finalize();
        LOG.debug("Finalising");
    }

    protected static void updatePoolByDriver(String url, ConnectionPoolDefinition cpd, Properties info) throws ProxoolException {
        definePool(url, cpd, info);
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
            throw new ProxoolException("Statement argument is not one provided by Proxool (it's " + statement.getClass() + ")");
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
        return ConnectionPoolManager.getInstance().getConnectionPool(alias).getMonitor().getStatistics(token);
    }

    /**
     * Get all the lastest performance statistics for this pool
     * @param alias identifies the pool
     * @return a sample containing the statistics, or a zero length array if there none
     * @throws ProxoolException if we couldn't find the pool
     */
    public static StatisticsIF[] getStatistics(String alias) throws ProxoolException {
        final Monitor monitor = ConnectionPoolManager.getInstance().getConnectionPool(alias).getMonitor();
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
        final Monitor monitor = ConnectionPoolManager.getInstance().getConnectionPool(alias).getMonitor();
        monitor.addStatisticsListener(statisticsListener);
    }

    /**
     * Gives a snapshot of what the pool is doing
     * @param alias identifies the pool
     * @return the current status of the pool
     * @throws ProxoolException if we couldn't find the pool
     */
    public static SnapshotIF getSnapshot(String alias, boolean detail) throws ProxoolException {
        SnapshotIF snapshot = null;
        ConnectionPool cp = ConnectionPoolManager.getInstance().getConnectionPool(alias);

        Set connectionInfos = null;
        if (detail) {
            connectionInfos = new TreeSet(new Comparator() {
                        public int compare(Object o1, Object o2) {
                            try {
                                Date birth1 = ((ConnectionInfoIF) o1).getBirthDate();
                                Date birth2 = ((ConnectionInfoIF) o2).getBirthDate();
                                return birth1.compareTo(birth2);
                            } catch (ClassCastException e) {
                                LOG.error("Unexpected contents of connectionInfos Set: " + o1.getClass() + " and " + o2.getClass(), e);
                                return String.valueOf(o1.hashCode()).compareTo(String.valueOf(o2.hashCode()));
                            }
                        }
                    });
            cp.lock();

            Iterator i = getConnectionInfos(alias).iterator();
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
                connectionInfos.add(ci);
            }
        }

        snapshot = Monitor.getSnapshot(cp, cp.getDefinition(), connectionInfos);

        if (detail) {
            cp.unlock();
        }

        return snapshot;
    }
}

/*
 Revision history:
 $Log: ProxoolFacade.java,v $
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
