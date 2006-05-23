/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.naming.spi.ObjectFactory;
import javax.naming.Name;
import javax.naming.Context;
import javax.naming.Reference;
import javax.naming.RefAddr;
import javax.naming.StringRefAddr;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Properties;
import java.util.StringTokenizer;
import java.io.PrintWriter;

/**
 * The Proxool DataSource implementation. Supports three modes of configuration:
 * <ul>
 *   <li>pre-configured</li>
 *   <li>bean-configured</li>
 *   <li>factory-configured</li>
 * </ul>
 *
 * TODO - expand
 * @version $Revision: 1.7 $, $Date: 2006/05/23 21:17:55 $
 * @author bill
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.9
 */
public class ProxoolDataSource implements DataSource, ObjectFactory {
    private static final Log LOG = LogFactory.getLog(ProxoolDataSource.class);

    private int loginTimeout;
    private PrintWriter logWriter;

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
    private String driverUrl;
    private String user;
    private boolean verbose;
    private boolean jmx;
    private String jmxAgentId;
    private boolean testBeforeUse;
    private boolean testAfterUse;
    private Properties delegateProperties = new Properties();

    /**
     * A String of all the fatalSqlExceptions delimited by
     * {@link ConnectionPoolDefinitionIF#FATAL_SQL_EXCEPTIONS_DELIMITER}
     */
    private String fatalSqlExceptionsAsString;

    public ProxoolDataSource() {
        reset();
    }

    public ProxoolDataSource (String alias) {
        this.alias = alias;
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
            cpd.setUrl(getDriverUrl());
            cpd.setUser(getUser());
            cpd.setVerbose(isVerbose());
            cpd.setJmx(isJmx());
            cpd.setJmxAgentId(getJmxAgentId());
            cpd.setTestAfterUse(isTestAfterUse());
            cpd.setTestBeforeUse(isTestBeforeUse());
            cpd.setDelegateProperties(delegateProperties);
            ProxoolFacade.registerConnectionPool(cpd);
        }
    }


    public Object getObjectInstance(Object refObject, Name name, Context context, Hashtable hashtable) throws Exception {
        // we only handle references
        if (!(refObject instanceof Reference)) {
            return null;
        }
        Reference reference = (Reference) refObject;
/* Removed because JNDI implementations can not be trusted to implement reference.getFactoryClassName() correctly.
        // check if this is relevant for us
        if (!ProxoolDataSource.class.getName().equals(reference.getFactoryClassName())) {
            return null;
        }
*/
        // check if we've allready parsed the properties.
        if (!ConnectionPoolManager.getInstance().isPoolExists(reference.get(ProxoolConstants.ALIAS_PROPERTY).toString())) {
            populatePropertiesFromReference(reference);
        }
        return this;
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
    public String getDriverUrl() {
        return driverUrl;
    }

    /**
     * @see ConnectionPoolDefinitionIF#getUrl
     */
    public void setDriverUrl(String url) {
        this.driverUrl = url;
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
     * @see ConnectionPoolDefinitionIF#isJmx()
     */
    public boolean isJmx() {
        return jmx;
    }

    /**
     * @see ConnectionPoolDefinitionIF#isJmx()
     */
    public void setJmx(boolean jmx) {
        this.jmx = jmx;
    }

    /**
     * @see ConnectionPoolDefinitionIF#getJmxAgentId()
     */
    public String getJmxAgentId() {
        return jmxAgentId;
    }

    /**
     * @see ConnectionPoolDefinitionIF#getJmxAgentId()
     */
    public void setJmxAgentId(String jmxAgentId) {
        this.jmxAgentId = jmxAgentId;
    }

    /**
     * @see ConnectionPoolDefinitionIF#isTestBeforeUse
     */
    public boolean isTestBeforeUse() {
        return testBeforeUse;
    }

    /**
     * @see ConnectionPoolDefinitionIF#isTestBeforeUse
     */
    public void setTestBeforeUse(boolean testBeforeUse) {
        this.testBeforeUse = testBeforeUse;
    }

    /**
     * @see ConnectionPoolDefinitionIF#isTestAfterUse
     */
    public boolean isTestAfterUse() {
        return testAfterUse;
    }

    /**
     * @see ConnectionPoolDefinitionIF#isTestAfterUse
     */
    public void setTestAfterUse(boolean testAfterUse) {
        this.testAfterUse = testAfterUse;
    }

    /**
     * Set any property that should be handed to the delegate driver.
     * E.g. <code>foo=1,bar=true</code>
     * @param properties a comma delimited list of name=value pairs
     * @see ConnectionPoolDefinitionIF#getDelegateProperties()
     */
    public void setDelegateProperties(String properties) {
        StringTokenizer stOuter = new StringTokenizer(properties, ",");
        while (stOuter.hasMoreTokens()) {
            StringTokenizer stInner = new StringTokenizer(stOuter.nextToken(), "=");
            if (stInner.countTokens() != 2) {
                throw new IllegalArgumentException("Unexpected delegateProperties value: '" + properties + "'. Expected 'name=value'");
            }
            delegateProperties.put(stInner.nextToken().trim(), stInner.nextToken().trim());
        }
    }

    private void populatePropertiesFromReference(Reference reference) {
        RefAddr property = reference.get(ProxoolConstants.ALIAS_PROPERTY);
        if (property != null) {
            setAlias(property.getContent().toString());
        }
        property = reference.get(ProxoolConstants.DRIVER_CLASS_PROPERTY);
        if (property != null) {
            setDriver(property.getContent().toString());
        }
        property = reference.get(ProxoolConstants.FATAL_SQL_EXCEPTION_WRAPPER_CLASS_PROPERTY);
        if (property != null) {
            setFatalSqlExceptionWrapperClass(property.getContent().toString());
        }
        property = reference.get(ProxoolConstants.HOUSE_KEEPING_SLEEP_TIME_PROPERTY);
        if (property != null) {
            setHouseKeepingSleepTime(Integer.valueOf(property.getContent().toString()).intValue());
        }
        property = reference.get(ProxoolConstants.HOUSE_KEEPING_TEST_SQL_PROPERTY);
        if (property != null) {
            setHouseKeepingTestSql(property.getContent().toString());
        }
        property = reference.get(ProxoolConstants.MAXIMUM_CONNECTION_COUNT_PROPERTY);
        if (property != null) {
            setMaximumConnectionCount(Integer.valueOf(property.getContent().toString()).intValue());
        }
        property = reference.get(ProxoolConstants.MAXIMUM_CONNECTION_LIFETIME_PROPERTY);
        if (property != null) {
            setMaximumConnectionLifetime(Integer.valueOf(property.getContent().toString()).intValue());
        }
        property = reference.get(ProxoolConstants.MAXIMUM_ACTIVE_TIME_PROPERTY);
        if (property != null) {
            setMaximumActiveTime(Integer.valueOf(property.getContent().toString()).intValue());
        }
        property = reference.get(ProxoolConstants.MINIMUM_CONNECTION_COUNT_PROPERTY);
        if (property != null) {
            setMinimumConnectionCount(Integer.valueOf(property.getContent().toString()).intValue());
        }
        property = reference.get(ProxoolConstants.OVERLOAD_WITHOUT_REFUSAL_LIFETIME_PROPERTY);
        if (property != null) {
            setOverloadWithoutRefusalLifetime(Integer.valueOf(property.getContent().toString()).intValue());
        }
        property = reference.get(ProxoolConstants.PASSWORD_PROPERTY);
        if (property != null) {
            setPassword(property.getContent().toString());
        }
        property = reference.get(ProxoolConstants.PROTOTYPE_COUNT_PROPERTY);
        if (property != null) {
            setPrototypeCount(Integer.valueOf(property.getContent().toString()).intValue());
        }
        property = reference.get(ProxoolConstants.RECENTLY_STARTED_THRESHOLD_PROPERTY);
        if (property != null) {
            setRecentlyStartedThreshold(Integer.valueOf(property.getContent().toString()).intValue());
        }
        property = reference.get(ProxoolConstants.SIMULTANEOUS_BUILD_THROTTLE_PROPERTY);
        if (property != null) {
            setSimultaneousBuildThrottle(Integer.valueOf(property.getContent().toString()).intValue());
        }
        property = reference.get(ProxoolConstants.STATISTICS_PROPERTY);
        if (property != null) {
            setStatistics(property.getContent().toString());
        }
        property = reference.get(ProxoolConstants.STATISTICS_LOG_LEVEL_PROPERTY);
        if (property != null) {
            setStatisticsLogLevel(property.getContent().toString());
        }
        property = reference.get(ProxoolConstants.TRACE_PROPERTY);
        if (property != null) {
            setTrace("true".equalsIgnoreCase(property.getContent().toString()));
        }
        property = reference.get(ProxoolConstants.DRIVER_URL_PROPERTY);
        if (property != null) {
            setDriverUrl(property.getContent().toString());
        }
        property = reference.get(ProxoolConstants.USER_PROPERTY);
        if (property != null) {
            setUser(property.getContent().toString());
        }
        property = reference.get(ProxoolConstants.VERBOSE_PROPERTY);
        if (property != null) {
            setVerbose("true".equalsIgnoreCase(property.getContent().toString()));
        }
        property = reference.get(ProxoolConstants.JMX_PROPERTY);
        if (property != null) {
            setJmx("true".equalsIgnoreCase(property.getContent().toString()));
        }
        property = reference.get(ProxoolConstants.JMX_AGENT_PROPERTY);
        if (property != null) {
            setJmxAgentId(property.getContent().toString());
        }
        property = reference.get(ProxoolConstants.TEST_BEFORE_USE_PROPERTY);
        if (property != null) {
            setTestBeforeUse("true".equalsIgnoreCase(property.getContent().toString()));
        }
        property = reference.get(ProxoolConstants.TEST_AFTER_USE_PROPERTY);
        if (property != null) {
            setTestAfterUse("true".equalsIgnoreCase(property.getContent().toString()));
        }
        // Pick up any properties that we don't recognise
        Enumeration e = reference.getAll();
        while (e.hasMoreElements()) {
            StringRefAddr stringRefAddr = (StringRefAddr) e.nextElement();
            String name = stringRefAddr.getType();
            String content = stringRefAddr.getContent().toString();
            if (name.indexOf(ProxoolConstants.PROPERTY_PREFIX) != 0) {
                delegateProperties.put(name, content);
            }
        }
    }

    /**
     * Reset all properties to their default values
     */
    private void reset() {
        driverUrl = null;
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
        delegateProperties.clear();
    }

    public PrintWriter getLogWriter() throws SQLException {
        return this.logWriter;
    }

    public int getLoginTimeout() throws SQLException {
        return this.loginTimeout;
    }

    public void setLogWriter(PrintWriter logWriter) throws SQLException {
        this.logWriter = logWriter;
    }

    public void setLoginTimeout(int loginTimeout) throws SQLException {
        this.loginTimeout = loginTimeout;
    }

    public Connection getConnection(String s, String s1) throws SQLException {
        throw new UnsupportedOperationException("You should configure the username and password "
                + "within the proxool configuration and just call getConnection() instead.");
    }
}

/*
 Revision history:
 $Log: ProxoolDataSource.java,v $
 Revision 1.7  2006/05/23 21:17:55  billhorsman
 Add in maximum-active-time. Credit to Paolo Di Tommaso.

 Revision 1.6  2006/03/23 11:51:23  billhorsman
 Allow for delegate properties

 Revision 1.5  2006/01/18 14:40:01  billhorsman
 Unbundled Jakarta's Commons Logging.

 Revision 1.4  2004/08/19 12:28:28  chr32
 Removed factory type test.

 Revision 1.3  2004/03/18 17:16:58  chr32
 Added a timy bit of doc.

 Revision 1.2  2004/03/18 17:07:25  chr32
 Now supports all three modes: pre-configured, bean-configured and factory-configured.

 Revision 1.1  2004/03/15 23:54:25  chr32
 Initail Proxool J2EE-managed DataSource. Not quite complete yet.

 */