/*
 * This software is released under the Apache Software Licence. See
 * package.html for details. The latest version is available at
 * http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * This defines a connection pool: the URL to connect to the database, the
 * delegate driver to use, and how the pool behaves.
 * @version $Revision: 1.8 $, $Date: 2003/02/06 15:41:17 $
 * @author billhorsman
 * @author $Author: billhorsman $ (current maintainer)
 */
class ConnectionPoolDefinition implements ConnectionPoolDefinitionIF {

    private int maximumConnectionLifetime = DEFAULT_MAXIMUM_CONNECTION_LIFETIME;

    private int prototypeCount = DEFAULT_PROTOTYPE_COUNT;

    private int minimumConnectionCount = DEFAULT_MINIMUM_CONNECTION_COUNT;

    private int maximumConnectionCount = DEFAULT_MAXIMUM_CONNECTION_COUNT;

    private int houseKeepingSleepTime = DEFAULT_HOUSE_KEEPING_SLEEP_TIME;

    private int maximumNewConnections = DEFAULT_MAXIMUM_NEW_CONNECTIONS;

    private String alias;

    private Properties properties = new Properties();

    private String url;

    private String completeUrl;

    private String driver;

    private int recentlyStartedThreshold = DEFAULT_RECENTLY_STARTED_THRESHOLD;

    private int overloadWithoutRefusalLifetime = DEFAULT_OVERLOAD_WITHOUT_REFUSAL_THRESHOLD;

    private int maximumActiveTime = DEFAULT_MAXIMUM_ACTIVE_TIME;

    private boolean verbose;

    private boolean trace;

    private String statistics;

    private String statisticsLogLevel;

    private Set fatalSqlExceptions = new HashSet();

    /** Holds value of property houseKeepingTestSql. */
    private String houseKeepingTestSql;

    /**
     * @see ConnectionPoolDefinitionIF#getUser
     */
    public String getUser() {
        return getProperty(USER_PROPERTY);
    }

    /**
     * @see ConnectionPoolDefinitionIF#getUser
     */
    public void setUser(String user) {
        setProperty(USER_PROPERTY, user);
    }

    /**
     * @see ConnectionPoolDefinitionIF#getPassword
     */
    public String getPassword() {
        return getProperty(PASSWORD_PROPERTY);
    }

    /**
     * @see ConnectionPoolDefinitionIF#getPassword
     */
    public void setPassword(String password) {
        setProperty(PASSWORD_PROPERTY, password);
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
     */
    public Properties getProperties() {
        return properties;
    }

    /**
     * Get a property
     * @param name the name of the property
     * @return the value of the property
     * @see ConnectionPoolDefinitionIF#getProperties
     */
    public String getProperty(String name) {
        return getProperties().getProperty(name);
    }

    /**
     * Set a property
     * @param name the name of the property
     * @param value the value of the property
     * @see ConnectionPoolDefinitionIF#getProperties
     */
    public void setProperty(String name, String value) {
        getProperties().setProperty(name, value);

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
    public void setFatalSqlException(String messageFragment) {
        fatalSqlExceptions.add(messageFragment);
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
