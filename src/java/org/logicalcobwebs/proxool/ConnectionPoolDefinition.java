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
 * @version $Revision: 1.2 $, $Date: 2002/10/17 19:46:02 $
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

    private String name;

    private Properties properties = new Properties();

    private String url;

    private String completeUrl;

    private String driver;

    private int recentlyStartedThreshold = DEFAULT_RECENTLY_STARTED_THRESHOLD;

    private int overloadWithoutRefusalLifetime = DEFAULT_OVERLOAD_WITHOUT_REFUSAL_THRESHOLD;

    private int maximumActiveTime = DEFAULT_MAXIMUM_ACTIVE_TIME;

    private int debugLevel;

    private Set fatalSqlExceptions = new HashSet();

    /** Holds value of property houseKeepingTestSql. */
    private String houseKeepingTestSql;

    public String getUser() {
        return getProperty(USER_PROPERTY);
    }

    public void setUser(String user) {
        setProperty(USER_PROPERTY, user);
    }

    public String getPassword() {
        return getProperty(PASSWORD_PROPERTY);
    }

    public void setPassword(String password) {
        setProperty(PASSWORD_PROPERTY, password);
    }

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

    public String toString() {
        return getCompleteUrl();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMaximumConnectionLifetime() {
        return maximumConnectionLifetime;
    }

    public void setMaximumConnectionLifetime(int maximumConnectionLifetime) {
        this.maximumConnectionLifetime = maximumConnectionLifetime;
    }

    public int getPrototypeCount() {
        return prototypeCount;
    }

    public void setPrototypeCount(int prototypeCount) {
        this.prototypeCount = prototypeCount;
    }

    public int getMinimumConnectionCount() {
        return minimumConnectionCount;
    }

    public void setMinimumConnectionCount(int minimumConnectionCount) {
        this.minimumConnectionCount = minimumConnectionCount;
    }

    public int getMaximumConnectionCount() {
        return maximumConnectionCount;
    }

    public void setMaximumConnectionCount(int maximumConnectionCount) {
        this.maximumConnectionCount = maximumConnectionCount;
    }

    public int getHouseKeepingSleepTime() {
        return houseKeepingSleepTime;
    }

    public void setHouseKeepingSleepTime(int houseKeepingSleepTime) {
        this.houseKeepingSleepTime = houseKeepingSleepTime;
    }

    public int getMaximumNewConnections() {
        return maximumNewConnections;
    }

    public void setMaximumNewConnections(int maximumNewConnections) {
        this.maximumNewConnections = maximumNewConnections;
    }

    public Properties getProperties() {
        return properties;
    }

    public String getProperty(String name) {
        return getProperties().getProperty(name);
    }

    public void setProperty(String name, String value) {
        getProperties().setProperty(name, value);

    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public int getRecentlyStartedThreshold() {
        return recentlyStartedThreshold;
    }

    public void setRecentlyStartedThreshold(int recentlyStartedThreshold) {
        this.recentlyStartedThreshold = recentlyStartedThreshold;
    }

    public int getOverloadWithoutRefusalLifetime() {
        return overloadWithoutRefusalLifetime;
    }

    public void setOverloadWithoutRefusalLifetime(int overloadWithoutRefusalLifetime) {
        this.overloadWithoutRefusalLifetime = overloadWithoutRefusalLifetime;
    }

    public int getMaximumActiveTime() {
        return maximumActiveTime;
    }

    public void setMaximumActiveTime(int maximumActiveTime) {
        this.maximumActiveTime = maximumActiveTime;
    }

    public int getDebugLevel() {
        return debugLevel;
    }

    public void setDebugLevel(int debugLevel) {
        this.debugLevel = debugLevel;
    }

    public String getCompleteUrl() {
        return completeUrl;
    }

    public void setCompleteUrl(String completeUrl) {
        this.completeUrl = completeUrl;
    }

    public void setFatalSqlException(String messageFragment) {
        fatalSqlExceptions.add(messageFragment);
    }

    public Set getFatalSqlExceptions() {
        return fatalSqlExceptions;
    }

    /** Getter for property houseKeepingTestSql.
     * @return Value of property houseKeepingTestSql.
     */
    public String getHouseKeepingTestSql() {
        return houseKeepingTestSql;
    }

    /** Setter for property houseKeepingTestSql.
     * @param houseKeepingTestSql New value of property houseKeepingTestSql.
     */
    public void setHouseKeepingTestSql(String houseKeepingTestSql) {
        this.houseKeepingTestSql = houseKeepingTestSql;
    }

}

/*
 Revision history:
 $Log: ConnectionPoolDefinition.java,v $
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
