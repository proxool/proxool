/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import org.logicalcobwebs.logging.Log;
import org.logicalcobwebs.logging.LogFactory;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Enumeration;

/**
 * This defines a connection pool: the URL to connect to the database, the
 * delegate driver to use, and how the pool behaves.
 * @version $Revision: 1.11 $, $Date: 2003/03/05 18:42:32 $
 * @author billhorsman
 * @author $Author: billhorsman $ (current maintainer)
 */
class ConnectionPoolDefinition implements ConnectionPoolDefinitionIF {

    // TODO  add synch to avoid definition being read during update

    private static final Log LOG = LogFactory.getLog(ConnectionPoolDefinition.class);

    /**
     * This log has a category based on the alias
     */
    private Log poolLog = LOG;;

    private String alias;

    private Properties delegateProperties = new Properties();

    private Properties completeInfo = new Properties();

    private Properties changedInfo = new Properties();

    /**
     * Whether any of the properties that effect an individual
     * connection have changed. If they have, we need to kill
     * all the existing connections.
     */
    private boolean connectionPropertiesChanged;

    private String url;

    private String completeUrl;

    private String driver;

    private int maximumConnectionLifetime;;

    private int prototypeCount;

    private int minimumConnectionCount;

    private int maximumConnectionCount;

    private int houseKeepingSleepTime;

    private int maximumNewConnections;

    private int recentlyStartedThreshold;

    private int overloadWithoutRefusalLifetime;

    private int maximumActiveTime;

    private boolean verbose;

    private boolean trace;

    private String statistics;

    private String statisticsLogLevel;

    private Set fatalSqlExceptions = new HashSet();

    /**
     * A String of all the fatalSqlExceptions delimited by
     * {@link ConnectionPoolDefinitionIF#FATAL_SQL_EXCEPTIONS_DELIMITER}
     */
    private String fatalSqlExceptionsAsString;

    private String houseKeepingTestSql;

    /**
     * Construct a new definition
     * @param url the url that defines this pool
     * @param info additional properties (for Proxool and the delegate
     * driver)
     * @throws ProxoolException if anything goes wrong
     */
    protected ConnectionPoolDefinition(String url, Properties info) throws ProxoolException {
        this.alias = ProxoolFacade.getAlias(url);
        poolLog = LogFactory.getLog("org.logicalcobwebs.proxool." + alias);
        poolLog.info("Proxool " + Version.getVersion());
        reset();
        doChange(url, info);
    }

    /**
     * Redefine the definition. All existing properties are reset to their
     * default values
     * @param url the url that defines this pool
     * @param info additional properties (for Proxool and the delegate
     * driver)
     * @throws ProxoolException if anything goes wrong
     */
    protected void update(String url, Properties info) throws ProxoolException {
        changedInfo.clear();
        connectionPropertiesChanged = false;
        poolLog.debug("Updating definition");
        doChange(url, info);
        if (connectionPropertiesChanged) {
            poolLog.info("Mercifully killing all current connections because of definition changes");
            ProxoolFacade.killAllConnections(alias, "of definition changes", true);
        }
    }

    /**
     * Update the definition. All existing properties are retained
     * and only overwritten if included in the info parameter
     * @param url the url that defines this pool
     * @param info additional properties (for Proxool and the delegate
     * driver)
     * @throws ProxoolException if anything goes wrong
     */
    protected void redefine(String url, Properties info) throws ProxoolException {
        reset();
        changedInfo.clear();
        connectionPropertiesChanged = false;
        poolLog.debug("Redefining definition");
        doChange(url, info);

        // Check for minimum information
        if (getUrl() == null || getDriver() == null) {
            throw new ProxoolException("The URL is not defined properly: " + getCompleteUrl());
        }

        if (connectionPropertiesChanged) {
            LOG.info("Mercifully killing all current connections because of definition changes");
            ProxoolFacade.killAllConnections(alias, true);
        }
    }

    private void doChange(String url, Properties info) throws ProxoolException {

        try {
            int endOfPrefix = url.indexOf(':');
            int endOfDriver = url.indexOf(':', endOfPrefix + 1);

            if (endOfPrefix > -1 && endOfDriver > -1) {
                final String driver = url.substring(endOfPrefix + 1, endOfDriver);
                if (isChanged(getDriver(), driver)) {
                    logChange(true, ProxoolConstants.DELEGATE_DRIVER_PROPERTY, driver);
                    setDriver(driver);
                }

                final String delegateUrl = url.substring(endOfDriver + 1);
                if (isChanged(getUrl(), delegateUrl)) {
                    logChange(true, ProxoolConstants.DELEGATE_URL_PROPERTY, delegateUrl);
                    setUrl(delegateUrl);
                }
            } else {
                // Using alias. Nothing to do
            }
        } catch (IndexOutOfBoundsException e) {
            LOG.error("Invalid URL: '" + url + "'", e);
            throw new ProxoolException("Invalid URL: '" + url + "'");
        }

        setCompleteUrl(url);

        if (info != null) {
            Iterator i = info.keySet().iterator();
            while (i.hasNext()) {
                String key = (String) i.next();
                String value = info.getProperty(key);
                setAnyProperty(key, value);
                completeInfo.setProperty(key, value);
            }
        }

        ProxoolFacade.definitionUpdated(getAlias(), this, completeInfo, changedInfo);

    }

    private void logChange(boolean proxoolProperty, String key, String value) {
        if (poolLog.isDebugEnabled()) {
            String displayValue = value;
            if (key.toLowerCase().indexOf("password") > -1) {
                displayValue = "********";
            }
            poolLog.debug((proxoolProperty ? "Recognised proxool property: " : "Delegating property to driver: ") + key + "=" + displayValue);
        }
    }

    private void setAnyProperty(String key, String value) throws ProxoolException {

        boolean proxoolProperty = true;
        boolean changed = false;
        if (key.equals(ProxoolConstants.USER_PROPERTY)) {
            proxoolProperty = false;
            if (isChanged(getUser(), value)) {
                changed = true;
                setUser(value);
            }
        } else if (key.equals(ProxoolConstants.PASSWORD_PROPERTY)) {
            proxoolProperty = false;
            if (isChanged(getPassword(), value)) {
                changed = true;
                setPassword(value);
            }
        } else if (key.equals(ProxoolConstants.DELEGATE_DRIVER_PROPERTY)) {
            if (isChanged(getDriver(), value)) {
                changed = true;
                setDriver(value);
            }
        } else if (key.equals(ProxoolConstants.DELEGATE_URL_PROPERTY)) {
            if (isChanged(getUrl(), value)) {
                changed = true;
                setUrl(value);
            }
        } else if (key.equals(ProxoolConstants.HOUSE_KEEPING_SLEEP_TIME_PROPERTY)) {
            if (getHouseKeepingSleepTime() != getInt(key, value)) {
                changed = true;
                setHouseKeepingSleepTime(getInt(key, value));
            }
        } else if (key.equals(ProxoolConstants.HOUSE_KEEPING_TEST_SQL_PROPERTY)) {
            if (isChanged(getHouseKeepingTestSql(), value)) {
                changed = true;
                setHouseKeepingTestSql(value);
            }
        } else if (key.equals(ProxoolConstants.MAXIMUM_CONNECTION_COUNT_PROPERTY)) {
            if (getMaximumConnectionCount() != getInt(key, value)) {
                changed = true;
                setMaximumConnectionCount(getInt(key, value));
            }
        } else if (key.equals(ProxoolConstants.MAXIMUM_CONNECTION_LIFETIME_PROPERTY)) {
            if (getMaximumConnectionLifetime() != getInt(key, value)) {
                changed = true;
                setMaximumConnectionLifetime(getInt(key, value));
            }
        } else if (key.equals(ProxoolConstants.MAXIMUM_NEW_CONNECTIONS_PROPERTY)) {
            if (getMaximumNewConnections() != getInt(key, value)) {
                changed = true;
                setMaximumNewConnections(getInt(key, value));
            }
        } else if (key.equals(ProxoolConstants.MINIMUM_CONNECTION_COUNT_PROPERTY)) {
            if (getMinimumConnectionCount() != getInt(key, value)) {
                changed = true;
                setMinimumConnectionCount(getInt(key, value));
            }
        } else if (key.equals(ProxoolConstants.PROTOTYPE_COUNT_PROPERTY)) {
            if (getPrototypeCount() != getInt(key, value)) {
                changed = true;
                setPrototypeCount(getInt(key, value));
            }
        } else if (key.equals(ProxoolConstants.RECENTLY_STARTED_THRESHOLD_PROPERTY)) {
            if (getRecentlyStartedThreshold() != getInt(key, value)) {
                changed = true;
                setRecentlyStartedThreshold(getInt(key, value));
            }
        } else if (key.equals(ProxoolConstants.OVERLOAD_WITHOUT_REFUSAL_LIFETIME_PROPERTY)) {
            if (getOverloadWithoutRefusalLifetime() != getInt(key, value)) {
                changed = true;
                setOverloadWithoutRefusalLifetime(getInt(key, value));
            }
        } else if (key.equals(ProxoolConstants.MAXIMUM_ACTIVE_TIME_PROPERTY)) {
            if (getMaximumActiveTime() != getInt(key, value)) {
                changed = true;
                setMaximumActiveTime(getInt(key, value));
            }
        } else if (key.equals(ProxoolConstants.DEBUG_LEVEL_PROPERTY)) {
            if (value != null && value.equals("1")) {
                poolLog.warn("Use of " + ProxoolConstants.DEBUG_LEVEL_PROPERTY + "=1 is deprecated. Use " + ProxoolConstants.VERBOSE_PROPERTY + "=true instead.");
                if (!isVerbose()) {
                    changed = true;
                    setVerbose(true);
                }
            } else {
                poolLog.warn("Use of " + ProxoolConstants.DEBUG_LEVEL_PROPERTY + "=0 is deprecated. Use " + ProxoolConstants.VERBOSE_PROPERTY + "=false instead.");
                if (isVerbose()) {
                    changed = true;
                    setVerbose(false);
                }
            }
        } else if (key.equals(ProxoolConstants.VERBOSE_PROPERTY)) {
            final boolean valueAsBoolean = Boolean.valueOf(value).booleanValue();
            if (isVerbose() != valueAsBoolean) {
                changed = true;
                setVerbose(valueAsBoolean);
            }
        } else if (key.equals(ProxoolConstants.TRACE_PROPERTY)) {
            final boolean valueAsBoolean = Boolean.valueOf(value).booleanValue();
            if (isTrace() != valueAsBoolean) {
                changed = true;
                setTrace(valueAsBoolean);
            }
        } else if (key.equals(ProxoolConstants.FATAL_SQL_EXCEPTION_PROPERTY)) {
            if (isChanged(fatalSqlExceptionsAsString, value)) {
                changed = true;
                setFatalSqlExceptionsAsString(value);
            }
        } else if (key.equals(ProxoolConstants.STATISTICS_PROPERTY)) {
            if (isChanged(getStatistics(), value)) {
                changed = true;
                setStatistics(value);
            }
        } else if (key.equals(ProxoolConstants.STATISTICS_LOG_LEVEL_PROPERTY)) {
            if (isChanged(getStatisticsLogLevel(), value)) {
                changed = true;
                setStatisticsLogLevel(value);
            }
        } else {
            if (isChanged(getDelegateProperty(key), value)) {
                changed = true;
                setDelegateProperty(key, value);
            }
            proxoolProperty = false;
        }

        if (changed) {
            logChange(proxoolProperty, key, value);
            changedInfo.setProperty(key, value);
        }

    }

    private int getInt(String key, String value) throws ProxoolException {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new ProxoolException("'" + key + "' property must be an integer. Found '" + value + "' instead.");
        }
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
     * Reset all properties to their default values
     */
    private void reset() {
        completeUrl = null;
        delegateProperties.clear();
        completeInfo.clear();
        changedInfo.clear();

        url = null;
        driver = null;
        maximumConnectionLifetime = DEFAULT_MAXIMUM_CONNECTION_LIFETIME;
        prototypeCount = DEFAULT_PROTOTYPE_COUNT;
        minimumConnectionCount = DEFAULT_MINIMUM_CONNECTION_COUNT;
        maximumConnectionCount = DEFAULT_MAXIMUM_CONNECTION_COUNT;
        houseKeepingSleepTime = DEFAULT_HOUSE_KEEPING_SLEEP_TIME;
        houseKeepingTestSql = null;
        maximumNewConnections = DEFAULT_MAXIMUM_NEW_CONNECTIONS;
        recentlyStartedThreshold = DEFAULT_RECENTLY_STARTED_THRESHOLD;
        overloadWithoutRefusalLifetime = DEFAULT_OVERLOAD_WITHOUT_REFUSAL_THRESHOLD;
        maximumActiveTime = DEFAULT_MAXIMUM_ACTIVE_TIME;
        verbose = false;
        trace = false;
        statistics = null;
        statisticsLogLevel = null;
        fatalSqlExceptions.clear();
    }

    /**
     * Get all the properties used to define this pool
     * @return
     */
    protected Properties getCompleteInfo() {
        return completeInfo;
    }

    /**
     * @see ConnectionPoolDefinitionIF#getUser
     */
    public String getUser() {
        return getDelegateProperty(USER_PROPERTY);
    }

    /**
     * @see ConnectionPoolDefinitionIF#getUser
     */
    public void setUser(String user) {
        setDelegateProperty(USER_PROPERTY, user);
    }

    /**
     * @see ConnectionPoolDefinitionIF#getPassword
     */
    public String getPassword() {
        return getDelegateProperty(PASSWORD_PROPERTY);
    }

    /**
     * @see ConnectionPoolDefinitionIF#getPassword
     */
    public void setPassword(String password) {
        setDelegateProperty(PASSWORD_PROPERTY, password);
    }

    /**
     * @see ConnectionPoolDefinitionIF#getJdbcDriverVersion
     */
    public String getJdbcDriverVersion() {

        try {
            Driver driver = DriverManager.getDriver(getUrl());
            return driver.getMajorVersion() + "." + driver.getMinorVersion();
        } catch (SQLException e) {
            return "Trying to locate driver version for '" + getUrl() + "' caused: " + e.toString();
        } catch (NullPointerException e) {
            return "Couldn't locate driver for '" + getUrl() + "'!";
        }

    }

    /**
     * @see Object#toString
     */
    public String toString() {
        return getCompleteUrl();
    }

    /**
     * @see ConnectionPoolDefinitionIF#getName
     * @deprecated use {@link #getAlias}
     */
    public String getName() {
        return alias;
    }

    /**
     * @see ConnectionPoolDefinitionIF#getAlias
     */
    public String getAlias() {
        return alias;
    }

    /**
     * @see ConnectionPoolDefinitionIF#getAlias
     */
    public void setAlias(String alias) {
        this.alias = alias;
    }

    /**
     * @see ConnectionPoolDefinitionIF#getMaximumConnectionLifetime
     */
    public int getMaximumConnectionLifetime() {
        return maximumConnectionLifetime;
    }

    /**
     * @see ConnectionPoolDefinitionIF#getMaximumConnectionLifetime
     */
    public void setMaximumConnectionLifetime(int maximumConnectionLifetime) {
        this.maximumConnectionLifetime = maximumConnectionLifetime;
    }

    /**
     * @see ConnectionPoolDefinitionIF#getPrototypeCount
     */
    public int getPrototypeCount() {
        return prototypeCount;
    }

    /**
     * @see ConnectionPoolDefinitionIF#getPrototypeCount
     */
    public void setPrototypeCount(int prototypeCount) {
        this.prototypeCount = prototypeCount;
    }

    /**
     * @see ConnectionPoolDefinitionIF#getMinimumConnectionCount
     */
    public int getMinimumConnectionCount() {
        return minimumConnectionCount;
    }

    /**
     * @see ConnectionPoolDefinitionIF#getMinimumConnectionCount
     */
    public void setMinimumConnectionCount(int minimumConnectionCount) {
        this.minimumConnectionCount = minimumConnectionCount;
    }

    /**
     * @see ConnectionPoolDefinitionIF#getMaximumConnectionCount
     */
    public int getMaximumConnectionCount() {
        return maximumConnectionCount;
    }

    /**
     * @see ConnectionPoolDefinitionIF#getMaximumConnectionCount
     */
    public void setMaximumConnectionCount(int maximumConnectionCount) {
        this.maximumConnectionCount = maximumConnectionCount;
    }

    /**
     * @see ConnectionPoolDefinitionIF#getHouseKeepingSleepTime
     */
    public int getHouseKeepingSleepTime() {
        return houseKeepingSleepTime;
    }

    /**
     * @see ConnectionPoolDefinitionIF#getHouseKeepingSleepTime
     */
    public void setHouseKeepingSleepTime(int houseKeepingSleepTime) {
        this.houseKeepingSleepTime = houseKeepingSleepTime;
    }

    /**
     * @see ConnectionPoolDefinitionIF#getMaximumNewConnections
     */
    public int getMaximumNewConnections() {
        return maximumNewConnections;
    }

    /**
     * @see ConnectionPoolDefinitionIF#getMaximumNewConnections
     */
    public void setMaximumNewConnections(int maximumNewConnections) {
        this.maximumNewConnections = maximumNewConnections;
    }

    /**
     * @see ConnectionPoolDefinitionIF#getProperties
     * @deprecated use less ambiguous {@link #getDelegateProperties} instead
     */
    public Properties getProperties() {
        return delegateProperties;
    }

    /**
     * @see ConnectionPoolDefinitionIF#getDelegateProperties
     */
    public Properties getDelegateProperties() {
        return delegateProperties;
    }

    /**
     * Get a property
     * @param name the name of the property
     * @return the value of the property
     */
    public String getDelegateProperty(String name) {
        return getDelegateProperties().getProperty(name);
    }

    /**
     * Set a property
     * @param name the name of the property
     * @param value the value of the property
     * @see ConnectionPoolDefinitionIF#getProperties
     */
    public void setDelegateProperty(String name, String value) {
        connectionPropertiesChanged = true;
        getDelegateProperties().setProperty(name, value);
    }

    /**
     * @see ConnectionPoolDefinitionIF#getUrl
     */
    public String getUrl() {
        return url;
    }

    /**
     * @see ConnectionPoolDefinitionIF#getUrl
     */
    public void setUrl(String url) {
        this.url = url;
        connectionPropertiesChanged = true;
    }

    /**
     * @see ConnectionPoolDefinitionIF#getDriver
     */
    public String getDriver() {
        return driver;
    }

    /**
     * @see ConnectionPoolDefinitionIF#getDriver
     */
    public void setDriver(String driver) {
        this.driver = driver;
        connectionPropertiesChanged = true;
    }

    /**
     * @see ConnectionPoolDefinitionIF#getRecentlyStartedThreshold
     */
    public int getRecentlyStartedThreshold() {
        return recentlyStartedThreshold;
    }

    /**
     * @see ConnectionPoolDefinitionIF#getRecentlyStartedThreshold
     */
    public void setRecentlyStartedThreshold(int recentlyStartedThreshold) {
        this.recentlyStartedThreshold = recentlyStartedThreshold;
    }

    /**
     * @see ConnectionPoolDefinitionIF#getOverloadWithoutRefusalLifetime
     */
    public int getOverloadWithoutRefusalLifetime() {
        return overloadWithoutRefusalLifetime;
    }

    /**
     * @see ConnectionPoolDefinitionIF#getOverloadWithoutRefusalLifetime
     */
    public void setOverloadWithoutRefusalLifetime(int overloadWithoutRefusalLifetime) {
        this.overloadWithoutRefusalLifetime = overloadWithoutRefusalLifetime;
    }

    /**
     * @see ConnectionPoolDefinitionIF#getMaximumActiveTime
     */
    public int getMaximumActiveTime() {
        return maximumActiveTime;
    }

    /**
     * @see ConnectionPoolDefinitionIF#getMaximumActiveTime
     */
    public void setMaximumActiveTime(int maximumActiveTime) {
        this.maximumActiveTime = maximumActiveTime;
    }

    /**
     * @see ConnectionPoolDefinitionIF#getDebugLevel
     * @deprecated use {@link #isVerbose} instead
     */
    public int getDebugLevel() {
        return (verbose ? 1 : 0);
    }

    /**
     * @see ConnectionPoolDefinitionIF#isVerbose
     */
    public boolean isVerbose() {
        return verbose;
    }

    /**
     * @see ConnectionPoolDefinitionIF#isVerbose
     */
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    /**
     * @see ConnectionPoolDefinitionIF#isTrace
     */
    public boolean isTrace() {
        return trace;
    }

    /**
     * @see ConnectionPoolDefinitionIF#isTrace
     */
    public void setTrace(boolean trace) {
        this.trace = trace;
    }

    /**
     * @see ConnectionPoolDefinitionIF#getCompleteUrl
     */
    public String getCompleteUrl() {
        return completeUrl;
    }

    /**
     * @see ConnectionPoolDefinitionIF#getCompleteUrl
     */
    public void setCompleteUrl(String completeUrl) {
        this.completeUrl = completeUrl;
    }

    /**
     * @see ConnectionPoolDefinitionIF#getFatalSqlExceptions
     */
    public void setFatalSqlExceptionsAsString(String fatalSqlExceptionsAsString) {
        this.fatalSqlExceptionsAsString = fatalSqlExceptionsAsString;
        fatalSqlExceptions.clear();
        StringTokenizer st = new StringTokenizer(fatalSqlExceptionsAsString, FATAL_SQL_EXCEPTIONS_DELIMITER);
        while (st.hasMoreTokens()) {
            fatalSqlExceptions.add(st.nextToken());
        }
    }

    /**
     * @see ConnectionPoolDefinitionIF#getFatalSqlExceptions
     */
    public Set getFatalSqlExceptions() {
        return fatalSqlExceptions;
    }

    /**
     * @see ConnectionPoolDefinitionIF#getHouseKeepingTestSql
     */
    public String getHouseKeepingTestSql() {
        return houseKeepingTestSql;
    }

    /**
     * @see ConnectionPoolDefinitionIF#getHouseKeepingTestSql
     */
    public void setHouseKeepingTestSql(String houseKeepingTestSql) {
        this.houseKeepingTestSql = houseKeepingTestSql;
    }

    /**
     * @see ConnectionPoolDefinitionIF#getStatistics
     */
    public String getStatistics() {
        return statistics;
    }

    /**
     * @see ConnectionPoolDefinitionIF#getStatistics
     */
    public void setStatistics(String statistics) {
        this.statistics = statistics;
    }

    /**
     * @see ConnectionPoolDefinitionIF#getStatisticsLogLevel
     */
    public String getStatisticsLogLevel() {
        return statisticsLogLevel;
    }

    /**
     * @see ConnectionPoolDefinitionIF#getStatisticsLogLevel
     */
    public void setStatisticsLogLevel(String statisticsLogLevel) {
        this.statisticsLogLevel = statisticsLogLevel;
    }

}

/*
 Revision history:
 $Log: ConnectionPoolDefinition.java,v $
 Revision 1.11  2003/03/05 18:42:32  billhorsman
 big refactor of prototyping and house keeping to
 drastically reduce the number of threads when using
 many pools

 Revision 1.10  2003/03/03 11:11:57  billhorsman
 fixed licence

 Revision 1.9  2003/02/26 16:05:52  billhorsman
 widespread changes caused by refactoring the way we
 update and redefine pool definitions.

 Revision 1.8  2003/02/06 15:41:17  billhorsman
 add statistics-log-level

 Revision 1.7  2003/01/31 00:17:05  billhorsman
 statistics is now a string to allow multiple,
 comma-delimited values

 Revision 1.6  2003/01/30 17:20:38  billhorsman
 new statistics property

 Revision 1.5  2003/01/17 00:38:12  billhorsman
 wide ranging changes to clarify use of alias and url -
 this has led to some signature changes (new exceptions
 thrown) on the ProxoolFacade API.

 Revision 1.4  2002/11/09 15:50:15  billhorsman
 new trace property and better doc

 Revision 1.3  2002/10/27 13:29:38  billhorsman
 deprecated debug-level in favour of verbose

 Revision 1.2  2002/10/17 19:46:02  billhorsman
 removed redundant reference to logFilename (we now use Jakarta's Commons Logging component

 Revision 1.1.1.1  2002/09/13 08:13:00  billhorsman
 new

 Revision 1.8  2002/07/10 16:14:47  billhorsman
 widespread layout changes and move constants into ProxoolConstants

 Revision 1.7  2002/07/04 09:05:36  billhorsman
 Fixes

 Revision 1.6  2002/07/02 11:19:08  billhorsman
 layout code and imports

 Revision 1.5  2002/07/02 08:41:59  billhorsman
 No longer public - we should be confuring pools with Properties now. Also added completeUrl property so that we can access pool by either alias or full url.

 Revision 1.4  2002/06/28 11:19:47  billhorsman
 improved doc

*/
