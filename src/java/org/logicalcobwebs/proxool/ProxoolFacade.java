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

/**
 * <p>This provides some nice-to-have features that can't be provided by the
 * {@link java.sql.Driver} {@link ProxoolDriver implementation} of java.sql.Driver. Like starting up
 * a pool before you need a connection. And getting statistical information.</p>
 *
 * <p>You need to use this class wisely. It is obviously specfic to proxool so it will
 * stop you switching to another driver. Consider isolating the code that calls this
 * class so that you can easily remove it if you have to.</p>
 * 
 * @version $Revision: 1.6 $, $Date: 2002/10/25 15:59:32 $
 * @author billhorsman
 * @author $Author: billhorsman $ (current maintainer)
 */
public class ProxoolFacade {

    private static final Log LOG = LogFactory.getLog(ProxoolFacade.class);

    private static Map infos = new HashMap();

    /** Build a ConnectionPool based on this definition and then start it. */
    public static String registerConnectionPool(String url, Properties info) throws SQLException {
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

        } else {
            throw new SQLException("Attempt to register duplicate pool");
        }

        return name;
    }

    /**
     * Extracts the pool alias from the url:
     *
     *    proxool.alias:driver:url -> alias
     *    proxool:driver:url -> proxool:driver:url
     *
     */
    private static String getAlias(String url) throws SQLException {
        String name = url;

        try {
            int endOfPrefix = url.indexOf(':');

            if (endOfPrefix > "proxool.".length()) {
                name = url.substring("proxool.".length(), endOfPrefix);
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
    private static String definePool(ConnectionPool cp, String url, ConnectionPoolDefinition cpd, Properties info) throws SQLException {

        Properties rememberedInfo = null;
        String rememberedKey = null;
        if (cp != null) {
            rememberedKey = cp.getDefinition().getCompleteUrl();
        } else {
            rememberedKey = url;
        }
        rememberedInfo = (Properties) infos.get(rememberedKey);

        if (info != null && (rememberedInfo == null || !info.equals(rememberedInfo))) {

            if (LOG.isDebugEnabled()) {
                if (rememberedInfo == null) {
                    LOG.debug("Setting properties on " + url);
                } else {
                    LOG.debug("Updating properties on " + url);
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
                    try {
                        int valueAsInt = Integer.parseInt(value);
                        cpd.setDebugLevel(valueAsInt);
                    } catch (NumberFormatException e) {
                        throw new SQLException("'" + key + "' property must be an integer. Found '" + value + "' instead.");
                    }
                } else if (key.equals(ProxoolConstants.FATAL_SQL_EXCEPTION_PROPERTY)) {
                    cpd.setFatalSqlException(value);
                } else {
                    cpd.setProperty(key, value);
                    isProxoolProperty = false;
                }

                if (LOG.isDebugEnabled()) {
                    if (isProxoolProperty) {
                        LOG.debug("Recognised proxool property: " + key + "=" + value);
                    } else {
                        LOG.debug("Delgating property to Driver: " + key + "=" + value);
                    }
                }

            }

            infos.put(rememberedKey, info);
        }

        return cpd.getName();
    }

    /** Build a ConnectionPool based on this definition and then start it. */
    public static void registerConnectionPool(String url) throws SQLException {
        registerConnectionPool(url, null);
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
    }

    protected void finalize() throws Throwable {
        super.finalize();
        LOG.debug("Finalising");
    }
}

/*
 Revision history:
 $Log: ProxoolFacade.java,v $
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
