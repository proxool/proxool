/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import org.logicalcobwebs.logging.Log;
import org.logicalcobwebs.logging.LogFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * A DataSource that is configurable via bean properties. Typically used in a J2EE environment.
 * @version $Revision: 1.1 $, $Date: 2004/03/15 23:54:25 $
 * @author bill
 * @author $Author: chr32 $ (current maintainer)
 * @since Proxool 0.9
 */
public class ProxoolDataSource extends AbstractProxoolDataSource {
    private static final Log LOG = LogFactory.getLog(ProxoolDataSource.class);

    private String alias;
    private String driver;
    private String fatalSqlExceptionWrapperClass;
    private int houseKeepingSleepTime;
    private String houseKeepingTestSql;
    private int maximumActiveTime;
    private int maximumConnectionCount;
    private int maximumConnectionLifetime;;
    private int minimumConnectionCount;
    private int overloadWithoutRefusalLifetime;
    private String password;
    private int prototypeCount;
    private int recentlyStartedThreshold;
    private int simultaneousBuildThrottle;
    private String statistics;
    private String statisticsLogLevel;
    private boolean trace;
    private String url;
    private String user;
    private boolean verbose;

    /**
     * A String of all the fatalSqlExceptions delimited by
     * {@link ConnectionPoolDefinitionIF#FATAL_SQL_EXCEPTIONS_DELIMITER}
     */
    private String fatalSqlExceptionsAsString;

    public ProxoolDataSource() {
        reset();
    }

    /**
     * @see javax.sql.DataSource#getConnection()
     */
    public Connection getConnection() throws SQLException {

        ConnectionPool cp = null;
        try {
            if (!ConnectionPoolManager.getInstance().isPoolExists(alias)) {
                registerPool();
            }
            cp = ConnectionPoolManager.getInstance().getConnectionPool(alias);
            return cp.getConnection();
        } catch (ProxoolException e) {
            LOG.error("Problem getting connection", e);
            throw new SQLException(e.toString());
        }
    }

    /**
     * Register a pool using the properties of this data source. (Check that it
     * exists first)
     * @throws ProxoolException if the pool couldn't be registered
     */
    private synchronized void registerPool() throws ProxoolException {
        if (!ConnectionPoolManager.getInstance().isPoolExists(alias)) {
            ConnectionPoolDefinition cpd = new ConnectionPoolDefinition();
            cpd.setAlias(getAlias());
            cpd.setDriver(getDriver());
            cpd.setFatalSqlExceptionsAsString(getFatalSqlExceptionsAsString());
            cpd.setFatalSqlExceptionWrapper(getFatalSqlExceptionWrapperClass());
            cpd.setHouseKeepingSleepTime(getHouseKeepingSleepTime());
            cpd.setHouseKeepingTestSql(getHouseKeepingTestSql());
            cpd.setMaximumActiveTime(getMaximumActiveTime());
            cpd.setMaximumConnectionCount(getMaximumConnectionCount());
            cpd.setMaximumConnectionLifetime(getMaximumConnectionLifetime());
            cpd.setMinimumConnectionCount(getMinimumConnectionCount());
            cpd.setOverloadWithoutRefusalLifetime(getOverloadWithoutRefusalLifetime());
            cpd.setPassword(getPassword());
            cpd.setPrototypeCount(getPrototypeCount());
            cpd.setRecentlyStartedThreshold(getRecentlyStartedThreshold());
            cpd.setSimultaneousBuildThrottle(getSimultaneousBuildThrottle());
            cpd.setStatistics(getStatistics());
            cpd.setStatisticsLogLevel(getStatisticsLogLevel());
            cpd.setTrace(isTrace());
            cpd.setUrl(getUrl());
            cpd.setUser(getUser());
            cpd.setVerbose(isVerbose());
            ProxoolFacade.registerConnectionPool(cpd);

        }
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
     * @see ConnectionPoolDefinitionIF#getSimultaneousBuildThrottle
     */
    public int getSimultaneousBuildThrottle() {
        return simultaneousBuildThrottle;
    }

    /**
     * @see ConnectionPoolDefinitionIF#getSimultaneousBuildThrottle
     */
    public void setSimultaneousBuildThrottle(int simultaneousBuildThrottle) {
        this.simultaneousBuildThrottle = simultaneousBuildThrottle;
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

    /**
     * @see ConnectionPoolDefinitionIF#getFatalSqlExceptions
     */
    public String getFatalSqlExceptionsAsString() {
        return fatalSqlExceptionsAsString;
    }

    /**
     * @see ConnectionPoolDefinitionIF#getFatalSqlExceptions
     */
    public void setFatalSqlExceptionsAsString(String fatalSqlExceptionsAsString) {
        this.fatalSqlExceptionsAsString = fatalSqlExceptionsAsString;
    }

    /**
     * @see ConnectionPoolDefinitionIF#getFatalSqlExceptionWrapper()
     */
    public String getFatalSqlExceptionWrapperClass() {
        return fatalSqlExceptionWrapperClass;
    }

    /**
     * @see ConnectionPoolDefinitionIF#getFatalSqlExceptionWrapper()
     */
    public void setFatalSqlExceptionWrapperClass(String fatalSqlExceptionWrapperClass) {
        this.fatalSqlExceptionWrapperClass = fatalSqlExceptionWrapperClass;
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
     * @see ConnectionPoolDefinitionIF#getUser
     */
    public String getUser() {
        return user;
    }

    /**
     * @see ConnectionPoolDefinitionIF#getUser
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * @see ConnectionPoolDefinitionIF#getPassword
     */
    public String getPassword() {
        return password;
    }

    /**
     * @see ConnectionPoolDefinitionIF#getPassword
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Reset all properties to their default values
     */
    private void reset() {
        url = null;
        driver = null;
        maximumConnectionLifetime = ConnectionPoolDefinitionIF.DEFAULT_MAXIMUM_CONNECTION_LIFETIME;
        prototypeCount = ConnectionPoolDefinitionIF.DEFAULT_PROTOTYPE_COUNT;
        minimumConnectionCount = ConnectionPoolDefinitionIF.DEFAULT_MINIMUM_CONNECTION_COUNT;
        maximumConnectionCount = ConnectionPoolDefinitionIF.DEFAULT_MAXIMUM_CONNECTION_COUNT;
        houseKeepingSleepTime = ConnectionPoolDefinitionIF.DEFAULT_HOUSE_KEEPING_SLEEP_TIME;
        houseKeepingTestSql = null;
        simultaneousBuildThrottle = ConnectionPoolDefinitionIF.DEFAULT_SIMULTANEOUS_BUILD_THROTTLE;
        recentlyStartedThreshold = ConnectionPoolDefinitionIF.DEFAULT_RECENTLY_STARTED_THRESHOLD;
        overloadWithoutRefusalLifetime = ConnectionPoolDefinitionIF.DEFAULT_OVERLOAD_WITHOUT_REFUSAL_THRESHOLD;
        maximumActiveTime = ConnectionPoolDefinitionIF.DEFAULT_MAXIMUM_ACTIVE_TIME;
        verbose = false;
        trace = false;
        statistics = null;
        statisticsLogLevel = null;
    }
}

/*
 Revision history:
 $Log: ProxoolDataSource.java,v $
 Revision 1.1  2004/03/15 23:54:25  chr32
 Initail Proxool J2EE-managed DataSource. Not quite complete yet.

 */