/*
 * This software is released under the Apache Software Licence. See
 * package.html for details. The latest version is available at
 * http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Enumeration;

/**
 * <p>This provides some nice-to-have features that can't be provided by the
 * {@link java.sql.Driver} {@link ProxoolDriver implementation} of java.sql.Driver. Like starting up
 * a pool before you need a connection. And getting statistical information.</p>
 *
 * <p>You need to use this class wisely. It is obviously specfic to proxool so it will
 * stop you switching to another driver. Consider isolating the code that calls this
 * class so that you can easily remove it if you have to.</p>
 *
 * @version $Revision: 1.17 $, $Date: 2002/12/12 10:49:43 $
 * @author billhorsman
 * @author $Author: billhorsman $ (current maintainer)
 */
public class ProxoolFacade {

    private static final Log LOG = LogFactory.getLog(ProxoolFacade.class);

    private static Map infos = new HashMap();

    private static Map configurators = new HashMap();

    /**
     * Build a ConnectionPool based on this definition and then start it.
     * @param url defines the delegate driver and delegate url.
     * @param info the properties used to configure Proxool (and any for the delegate driver too) - optional
     * @param configurator used to configure this pool, it will be notified if any changes occur to the definition - optional
     * @return the alias for this pool (or the full url if no alias is specified)
     * @throws SQLException if anything goes wrong
     */
    public static String registerConnectionPool(String url, Properties info, ConfiguratorIF configurator) throws SQLException {
        ConnectionPool connectionPool = ConnectionPoolManager.getInstance().getConnectionPool(url);
        String name = null;

        if (connectionPool == null) {
            ConnectionPoolDefinition cpd = new ConnectionPoolDefinition();
            cpd.setName(getAlias(url));
            cpd.setCompleteUrl(url);

            try {
                int endOfPrefix = url.indexOf(':');
                int endOfDriver = url.indexOf(':', endOfPrefix + 1);
                cpd.setDriver(url.substring(endOfPrefix + 1, endOfDriver));
                cpd.setUrl(url.substring(endOfDriver + 1));
            } catch (IndexOutOfBoundsException e) {
                throw new SQLException("Invalid URL format.");
            }

            definePool(null, url, cpd, info);
            connectionPool = ConnectionPoolManager.getInstance().createConnectionPool(cpd);
            connectionPool.start();
            name = cpd.getName();

            // Associate this configurator with this pool
            if (configurator != null) {
                configurators.put(name, configurator);
            }

        } else {
            throw new SQLException("Attempt to register duplicate pool");
        }

        return name;
    }

    /**
     * With no configurator
     * @see #registerConnectionPool(java.lang.String, java.util.Properties, org.logicalcobwebs.proxool.ConfiguratorIF)
     */
    public static String registerConnectionPool(String url, Properties info) throws SQLException {
        return registerConnectionPool(url, info,  null);
    }

    /**
     * With no configurator or properties (using default values)
     * @see #registerConnectionPool(java.lang.String, java.util.Properties, org.logicalcobwebs.proxool.ConfiguratorIF)
     */
    public static void registerConnectionPool(String url) throws SQLException {
        registerConnectionPool(url, null, null);
    }

    /**
     * Extracts the pool alias from the url:
     *
     *    proxool.alias:driver:url -> alias
     *    proxool:alias -> alias
     *    proxool:driver:url -> proxool:driver:url
     *
     */
    protected static String getAlias(String url) throws SQLException {
        String name = url;
        final String prefix = ProxoolConstants.PROXOOL + ProxoolConstants.ALIAS_DELIMITER;
        try {
            int endOfPrefix = url.indexOf(':');

            if (endOfPrefix > prefix.length()) {
                name = url.substring(prefix.length(), endOfPrefix);
            } else if (endOfPrefix == -1) {
                if (url.startsWith(prefix)) {
                    name = url.substring(prefix.length());
                }
            }

        } catch (IndexOutOfBoundsException e) {
            throw new SQLException("Invalid URL format.");
        }

        return name;

    }

    /**
     *  Translates from properties to definition
     *
     * @param cpd The defintion to populate (can have existing settings)
     * @param info the properties object to read from
     * @return the name of the pool
     * @throws SQLException if there were any validation errors.
     */
    protected static String definePool(ConnectionPool cp, String url, ConnectionPoolDefinition cpd, Properties info) throws SQLException {

        Properties rememberedInfo = null;
        String rememberedKey = null;
        if (cp != null) {
            rememberedKey = cp.getDefinition().getCompleteUrl();
        } else {
            rememberedKey = url;
        }
        rememberedInfo = (Properties) infos.get(rememberedKey);

        if (info != null && (rememberedInfo == null || !info.equals(rememberedInfo))) {

            Log earlyLog = LogFactory.getLog("org.logicalcobwebs.proxool." + getAlias(url));

            if (earlyLog.isDebugEnabled()) {
                if (rememberedInfo == null) {
                    earlyLog.debug("Setting properties on " + url);
                } else {
                    earlyLog.debug("Updating properties on " + url);
                }
            }

            Iterator i = info.keySet().iterator();
            while (i.hasNext()) {
                String key = (String) i.next();
                String value = info.getProperty(key);
                boolean isProxoolProperty = true;

                if (key.equals(ProxoolConstants.USER_PROPERTY)) {
                    isProxoolProperty = false;
                    cpd.setUser(value);
                } else if (key.equals(ProxoolConstants.PASSWORD_PROPERTY)) {
                    isProxoolProperty = false;
                    cpd.setPassword(value);
                } else if (key.equals(ProxoolConstants.HOUSE_KEEPING_SLEEP_TIME_PROPERTY)) {
                    try {
                        int valueAsInt = Integer.parseInt(value);
                        cpd.setHouseKeepingSleepTime(valueAsInt);
                    } catch (NumberFormatException e) {
                        throw new SQLException("'" + key + "' property must be an integer. Found '" + value + "' instead.");
                    }
                } else if (key.equals(ProxoolConstants.HOUSE_KEEPING_TEST_SQL_PROPERTY)) {
                    cpd.setHouseKeepingTestSql(value);
                } else if (key.equals(ProxoolConstants.MAXIMUM_CONNECTION_COUNT_PROPERTY)) {
                    try {
                        int valueAsInt = Integer.parseInt(value);
                        cpd.setMaximumConnectionCount(valueAsInt);
                    } catch (NumberFormatException e) {
                        throw new SQLException("'" + key + "' property must be an integer. Found '" + value + "' instead.");
                    }
                } else if (key.equals(ProxoolConstants.MAXIMUM_CONNECTION_LIFETIME_PROPERTY)) {
                    try {
                        int valueAsInt = Integer.parseInt(value);
                        cpd.setMaximumConnectionLifetime(valueAsInt);
                    } catch (NumberFormatException e) {
                        throw new SQLException("'" + key + "' property must be an integer. Found '" + value + "' instead.");
                    }
                } else if (key.equals(ProxoolConstants.MAXIMUM_NEW_CONNECTIONS_PROPERTY)) {
                    try {
                        int valueAsInt = Integer.parseInt(value);
                        cpd.setMaximumNewConnections(valueAsInt);
                    } catch (NumberFormatException e) {
                        throw new SQLException("'" + key + "' property must be an integer. Found '" + value + "' instead.");
                    }
                } else if (key.equals(ProxoolConstants.MINIMUM_CONNECTION_COUNT_PROPERTY)) {
                    try {
                        int valueAsInt = Integer.parseInt(value);
                        cpd.setMinimumConnectionCount(valueAsInt);
                    } catch (NumberFormatException e) {
                        throw new SQLException("'" + key + "' property must be an integer. Found '" + value + "' instead.");
                    }
                } else if (key.equals(ProxoolConstants.PROTOTYPE_COUNT_PROPERTY)) {
                    try {
                        int valueAsInt = Integer.parseInt(value);
                        cpd.setPrototypeCount(valueAsInt);
                    } catch (NumberFormatException e) {
                        throw new SQLException("'" + key + "' property must be an integer. Found '" + value + "' instead.");
                    }
                } else if (key.equals(ProxoolConstants.RECENTLY_STARTED_THRESHOLD_PROPERTY)) {
                    try {
                        int valueAsInt = Integer.parseInt(value);
                        cpd.setRecentlyStartedThreshold(valueAsInt);
                    } catch (NumberFormatException e) {
                        throw new SQLException("'" + key + "' property must be an integer. Found '" + value + "' instead.");
                    }
                } else if (key.equals(ProxoolConstants.OVERLOAD_WITHOUT_REFUSAL_LIFETIME_PROPERTY)) {
                    try {
                        int valueAsInt = Integer.parseInt(value);
                        cpd.setOverloadWithoutRefusalLifetime(valueAsInt);
                    } catch (NumberFormatException e) {
                        throw new SQLException("'" + key + "' property must be an integer. Found '" + value + "' instead.");
                    }
                } else if (key.equals(ProxoolConstants.MAXIMUM_ACTIVE_TIME_PROPERTY)) {
                    try {
                        int valueAsInt = Integer.parseInt(value);
                        cpd.setMaximumActiveTime(valueAsInt);
                    } catch (NumberFormatException e) {
                        throw new SQLException("'" + key + "' property must be an integer. Found '" + value + "' instead.");
                    }
                } else if (key.equals(ProxoolConstants.DEBUG_LEVEL_PROPERTY)) {
                    if (value != null && value.equals("1")) {
                        earlyLog.warn("Use of " + ProxoolConstants.DEBUG_LEVEL_PROPERTY + "=1 is deprecated. Use " + ProxoolConstants.VERBOSE_PROPERTY + "=true instead.");
                        cpd.setVerbose(true);
                    } else {
                        earlyLog.warn("Use of " + ProxoolConstants.DEBUG_LEVEL_PROPERTY + "=0 is deprecated. Use " + ProxoolConstants.VERBOSE_PROPERTY + "=false instead.");
                        cpd.setVerbose(false);
                    }
                } else if (key.equals(ProxoolConstants.VERBOSE_PROPERTY)) {
                    cpd.setVerbose(Boolean.valueOf(value).booleanValue());
                } else if (key.equals(ProxoolConstants.TRACE_PROPERTY)) {
                    cpd.setTrace(Boolean.valueOf(value).booleanValue());
                } else if (key.equals(ProxoolConstants.FATAL_SQL_EXCEPTION_PROPERTY)) {
                    cpd.setFatalSqlException(value);
                } else {
                    cpd.setProperty(key, value);
                    isProxoolProperty = false;
                }

                if (earlyLog.isDebugEnabled()) {
                    if (isProxoolProperty) {
                        earlyLog.debug("Recognised proxool property: " + key + "=" + value);
                    } else {
                        if (key.toLowerCase().indexOf("password") > -1) {
                            earlyLog.debug("Delgating property to Driver: " + key + "=" + "*******");
                        } else {
                            earlyLog.debug("Delgating property to Driver: " + key + "=" + value);
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

            infos.put(rememberedKey, clone);
        }

        return cpd.getName();
    }

    /**
     * Remove a connection pool. Kills all the connections. Resets everything.
     * @param connectionPool the pool to remove
     * @param delay the time to wait for connections to become inactive before killing it (milliseconds)
     */
    private static void removeConnectionPool(String finalizer, ConnectionPool connectionPool, int delay) {
        if (connectionPool != null) {
            try {
                connectionPool.finalize(delay, finalizer);
            } catch (Throwable t) {
                LOG.error("Problem trying to remove " + connectionPool.getDefinition().getName() + " connection pool", t);
            }
            ConnectionPoolManager.getInstance().removeConnectionPool(connectionPool.getDefinition().getName());
        }
        connectionPool = null;
    }

    /**
     * Remove a connection pool. Kills all the connections. Resets everything.
     * @param name the pool to remove
     * @param delay the time to wait for connections to become inactive before killing it (milliseconds)
     */
    public static void removeConnectionPool(String name, int delay) {
        removeConnectionPool(Thread.currentThread().getName(), ConnectionPoolManager.getInstance().getConnectionPool(name), delay);
    }

    /**
     * Removes all connection pools. Kills all the connections. Resets everything.
     * @param delay the time to wait for connections to become inactive before killing it (milliseconds)
     */
    public static void removeAllConnectionPools(int delay) {
        removeAllConnectionPools(Thread.currentThread().getName(), delay);
    }

    /**
     * Removes all connection pools. Kills all the connections. Resets everything.
     * @param delay the time to wait for connections to become inactive before killing it (milliseconds)
     */
    protected static void removeAllConnectionPools(String finalizer, int delay) {

        Iterator connectionPools = ConnectionPoolManager.getInstance().getConnectionPoolMap().iterator();
        while (connectionPools.hasNext()) {
            ConnectionPool cp = (ConnectionPool) connectionPools.next();
            removeConnectionPool(finalizer, cp, delay);
        }

    }

    /**
     * Like {@link #removeConnectionPool(java.lang.String, int)} but uses no delay. (Kills
     * everything as quickly as possible).
     * @param name the pool to remove
     */
    public static void removeConnectionPool(String name) {
        removeConnectionPool(name, 0);
    }

    /**
     * Get real-time statistical information about how a pool is performing.
     */
    public static ConnectionPoolStatisticsIF getConnectionPoolStatistics(String alias) throws SQLException {
        try {
            return ConnectionPoolManager.getInstance().getConnectionPool(alias);
        } catch (NullPointerException e) {
            throw new SQLException("Couldn't find pool called '" + alias + "'. I only know about " + ConnectionPoolManager.getInstance().getConnectionPoolMap());
        }
    }

    /**
     * Get real-time statistical information about how a pool is performing.
     */
    public static String getConnectionPoolStatisticsDump(String alias) throws SQLException {
        try {
            return ConnectionPoolManager.getInstance().getConnectionPool(alias).displayStatistics();
        } catch (NullPointerException e) {
            throw new SQLException("Couldn't find pool called '" + alias + "'. I only know about " + ConnectionPoolManager.getInstance().getConnectionPoolMap());
        }
    }

    /**
     * Get the definition of a pool.
     * @param alias identifies the pool
     */
    public static ConnectionPoolDefinitionIF getConnectionPoolDefinition(String alias) throws SQLException {
        try {
            return ConnectionPoolManager.getInstance().getConnectionPool(alias).getDefinition();
        } catch (NullPointerException e) {
            throw new SQLException("Couldn't find pool called '" + alias + "'. I only know about " + ConnectionPoolManager.getInstance().getConnectionPoolMap());
        }
    }

    /**
     * Get details on each connection within the pool. This can tell you which ones are active, how long they have
     * been active, etc.
     * @param alias identifies the pool
     * @return a collection of {@link ConnectionInfoIF ConnectionInfoIFs}
     */
    public static Collection getConnectionInfos(String alias) {
        return ConnectionPoolManager.getInstance().getConnectionPool(alias).getConnectionInfos();
    }

    /**
     * Kill all connections in a pool. The pool continues to work however, and new connections will be
     * made as required.
     * @param connectionPoolName the pool containing the connection
     * @param merciful if true will only kill connections that aren't active
     */
    public static void killAllConnections(String connectionPoolName, boolean merciful) {
        ConnectionPoolManager.getInstance().getConnectionPool(connectionPoolName).expireAllConnections(merciful);
    }

    /**
     * Like {@link #killAllConnections} but defaults to merciful.
     */
    public static void killAllConnections(String connectionPoolName) {
        killAllConnections(connectionPoolName, MERCIFUL);
    }

    /**
     * Kill a single connection
     * @param connectionPoolName the pool containing the connection
     * @param id the id of the specific connection
     * @param merciful if true will only kill connections that aren't active
     * @return true if the connection was killed, or false if it couldn't be found or killed.
     */
    public static boolean killConnecton(String connectionPoolName, long id, boolean merciful) {
        return ConnectionPoolManager.getInstance().getConnectionPool(connectionPoolName).expireConnection(id, merciful);
    }

    /**
     * Monitors the change of state of the pool (quiet, busy, overloaded, or down)
     * @param connectionPoolName identifies the pool
     */
    public static void setStateListener(String connectionPoolName, StateListenerIF stateListener) {
        ConnectionPool cp = ConnectionPoolManager.getInstance().getConnectionPool(connectionPoolName);
        if (cp != null) {
            cp.setStateListener(stateListener);
        }
    }

    /**
     * Monitors each time a connection is made or destroyed
     * @param connectionPoolName identifies the pool
     */
    public static void setConnectionListener(String connectionPoolName, ConnectionListenerIF connectionListener) {
        ConnectionPool cp = ConnectionPoolManager.getInstance().getConnectionPool(connectionPoolName);
        if (cp != null) {
            cp.setConnectionListener(connectionListener);
        } else {
            StringBuffer knownPools = new StringBuffer();
            String[] poolNames = ConnectionPoolManager.getInstance().getConnectionPoolNames();
            for (int i = 0; i < poolNames.length; i++) {
                knownPools.append(poolNames[i]);
                knownPools.append(i > poolNames.length - 1 ? ", " : "");
            }
            LOG.warn("Couldn't add ConnectionListenerIF to " + connectionPoolName + " pool because it doesn't exist. "
                + "I do know about " + poolNames.length + " though: " + knownPools);
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
     * @param url the url that defines the pool (or the abbreviated ""proxool.name")
     * @param info the new properties
     */
    public static void updateConnectionPool(String url, Properties info) throws SQLException {
        String poolName = getAlias(url);
        ConnectionPool cp = ConnectionPoolManager.getInstance().getConnectionPool(poolName);
        ConnectionPoolDefinition cpd = cp.getDefinition();
        definePool(cp, url, cpd, info);

        ConfiguratorIF configurator = (ConfiguratorIF) configurators.get(cpd.getName());
        if (configurator != null) {
            // TODO send properties
            configurator.defintionUpdated(cpd, null, null);
        }

    }

    protected void finalize() throws Throwable {
        super.finalize();
        LOG.debug("Finalising");
    }

    protected static void updatePoolByDriver(ConnectionPool cp, String url, ConnectionPoolDefinition cpd, Properties info) throws SQLException {
        definePool(cp, url, cpd, info);

        ConfiguratorIF configurator = (ConfiguratorIF) configurators.get(cpd.getName());
        if (configurator != null) {
            cp.getLog().warn("The pool, which is associated with a configurator, has been updated on the fly. This is not recommended.");
            // TODO send properties
            configurator.defintionUpdated(cpd, null, null);
        }

    }
}

/*
 Revision history:
 $Log: ProxoolFacade.java,v $
 Revision 1.17  2002/12/12 10:49:43  billhorsman
 now includes properties in definitionChanged event

 Revision 1.16  2002/12/04 13:19:43  billhorsman
 draft ConfiguratorIF stuff for persistent configuration

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
