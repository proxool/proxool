/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import org.logicalcobwebs.logging.Log;
import org.logicalcobwebs.logging.LogFactory;

import javax.sql.DataSource;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.StringRefAddr;
import javax.naming.BinaryRefAddr;
import javax.naming.InitialContext;
import java.sql.Connection;
import java.sql.SQLException;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.Serializable;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.Hashtable;
import java.util.Properties;

/**
 * Basic implementation of DataSource
 * @version $Revision: 1.2 $, $Date: 2003/07/23 06:54:48 $
 * @author bill
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.8
 */
public class BasicDataSource implements DataSource {

    private static final Log LOG = LogFactory.getLog(BasicDataSource.class);

    private Properties jndiEnvironment;

    // JNDI properties

    private Context context;

    private String contextFactory;

    private String providerUrl;

    private String securityAuthentication;

    private String securityPrincipal;

    private String securityCredentials;

    // Main proxool properties

    private String alias;

    private String url;

    private String user;

    private String password;

    private String driver;

    private int maximumConnectionLifetime;;

    private int prototypeCount;

    private int minimumConnectionCount;

    private int maximumConnectionCount;

    private int houseKeepingSleepTime;

    private int simultaneousBuildThrottle;

    private int recentlyStartedThreshold;

    private int overloadWithoutRefusalLifetime;

    private int maximumActiveTime;

    private boolean verbose;

    private boolean trace;

    private String statistics;

    private String statisticsLogLevel;

    /**
     * A String of all the fatalSqlExceptions delimited by
     * {@link org.logicalcobwebs.proxool.ConnectionPoolDefinitionIF#FATAL_SQL_EXCEPTIONS_DELIMITER}
     */
    private String fatalSqlExceptionsAsString;

    private String houseKeepingTestSql;

    public BasicDataSource() {
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
        } catch (NamingException e) {
            LOG.error("JNDI Problem whilst getting connection", e);
            throw new SQLException(e.toString());
        }
    }

    /**
     * Register a pool using the properties of this data source. (Check that it
     * exists first)
     * @throws org.logicalcobwebs.proxool.ProxoolException if the pool couldn't be registered
     */
    private synchronized void registerPool() throws ProxoolException, NamingException {
        if (!ConnectionPoolManager.getInstance().isPoolExists(alias)) {
            ConnectionPoolDefinition cpd = new ConnectionPoolDefinition();
            cpd.setAlias(getAlias());
            cpd.setDriver(getDriver());
            cpd.setFatalSqlExceptionsAsString(getFatalSqlExceptionsAsString());
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

        Hashtable env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY, getContextFactory());
        env.put(Context.PROVIDER_URL, getProviderUrl());
        env.put(Context.SECURITY_AUTHENTICATION, getSecurityAuthentication());
        env.put(Context.SECURITY_PRINCIPAL, getSecurityPrincipal());
        env.put(Context.SECURITY_CREDENTIALS, getSecurityCredentials());
        context = new InitialContext(env);
        context.bind("java:/comp/env/jdbc/proxool." + getAlias(), this);

    }

    public String getContextFactory() {
        return contextFactory;
    }

    public void setContextFactory(String contextFactory) {
        this.contextFactory = contextFactory;
    }

    public String getProviderUrl() {
        return providerUrl;
    }

    public void setProviderUrl(String providerUrl) {
        this.providerUrl = providerUrl;
    }

    public String getSecurityAuthentication() {
        return securityAuthentication;
    }

    public void setSecurityAuthentication(String securityAuthentication) {
        this.securityAuthentication = securityAuthentication;
    }

    public String getSecurityPrincipal() {
        return securityPrincipal;
    }

    public void setSecurityPrincipal(String securityPrincipal) {
        this.securityPrincipal = securityPrincipal;
    }

    public String getSecurityCredentials() {
        return securityCredentials;
    }

    public void setSecurityCredentials(String securityCredentials) {
        this.securityCredentials = securityCredentials;
    }

    /**
     * Use {@link #getConnection()} instead
     * @see javax.sql.DataSource#getConnection(java.lang.String, java.lang.String)
     */
    public Connection getConnection(String username, String password)
            throws SQLException {
        throw new UnsupportedOperationException("You should configure the username and password "
            + "within the proxool configuration and just call getConnection() instead.");
    }

    /**
     * Unsupported operation
     * @see javax.sql.DataSource#getLoginTimeout
     */
    public int getLoginTimeout() {
        throw new UnsupportedOperationException("login timeout is not supported");
    }

    /**
     * Unsupported operation
     * @see javax.sql.DataSource#setLoginTimeout
     */
    public void setLoginTimeout(int loginTimeout) {
        throw new UnsupportedOperationException("login timeout is not supported");
    }

    /**
     * Unsupported operation
     * @see javax.sql.DataSource#getLogWriter
     */
    public PrintWriter getLogWriter() {
        throw new UnsupportedOperationException("Proxool uses Jakarta's Commons' Logging API");
    }

    /**
     * Unsupported operation
     * @see javax.sql.DataSource#setLogWriter
     */
    public void setLogWriter(PrintWriter logWriter) {
        throw new UnsupportedOperationException("Proxool uses Jakarta's Commons' Logging API");
    }

    /**
     * @see org.logicalcobwebs.proxool.ConnectionPoolDefinitionIF#getAlias
     */
    public String getAlias() {
        return alias;
    }

    /**
     * @see org.logicalcobwebs.proxool.ConnectionPoolDefinitionIF#getAlias
     */
    public void setAlias(String alias) {
        this.alias = alias;
    }

    /**
     * @see org.logicalcobwebs.proxool.ConnectionPoolDefinitionIF#getUrl
     */
    public String getUrl() {
        return url;
    }

    /**
     * @see org.logicalcobwebs.proxool.ConnectionPoolDefinitionIF#getUrl
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @see org.logicalcobwebs.proxool.ConnectionPoolDefinitionIF#getDriver
     */
    public String getDriver() {
        return driver;
    }

    /**
     * @see org.logicalcobwebs.proxool.ConnectionPoolDefinitionIF#getDriver
     */
    public void setDriver(String driver) {
        this.driver = driver;
    }

    /**
     * @see org.logicalcobwebs.proxool.ConnectionPoolDefinitionIF#getMaximumConnectionLifetime
     */
    public int getMaximumConnectionLifetime() {
        return maximumConnectionLifetime;
    }

    /**
     * @see org.logicalcobwebs.proxool.ConnectionPoolDefinitionIF#getMaximumConnectionLifetime
     */
    public void setMaximumConnectionLifetime(int maximumConnectionLifetime) {
        this.maximumConnectionLifetime = maximumConnectionLifetime;
    }

    /**
     * @see org.logicalcobwebs.proxool.ConnectionPoolDefinitionIF#getPrototypeCount
     */
    public int getPrototypeCount() {
        return prototypeCount;
    }

    /**
     * @see org.logicalcobwebs.proxool.ConnectionPoolDefinitionIF#getPrototypeCount
     */
    public void setPrototypeCount(int prototypeCount) {
        this.prototypeCount = prototypeCount;
    }

    /**
     * @see org.logicalcobwebs.proxool.ConnectionPoolDefinitionIF#getMinimumConnectionCount
     */
    public int getMinimumConnectionCount() {
        return minimumConnectionCount;
    }

    /**
     * @see org.logicalcobwebs.proxool.ConnectionPoolDefinitionIF#getMinimumConnectionCount
     */
    public void setMinimumConnectionCount(int minimumConnectionCount) {
        this.minimumConnectionCount = minimumConnectionCount;
    }

    /**
     * @see org.logicalcobwebs.proxool.ConnectionPoolDefinitionIF#getMaximumConnectionCount
     */
    public int getMaximumConnectionCount() {
        return maximumConnectionCount;
    }

    /**
     * @see org.logicalcobwebs.proxool.ConnectionPoolDefinitionIF#getMaximumConnectionCount
     */
    public void setMaximumConnectionCount(int maximumConnectionCount) {
        this.maximumConnectionCount = maximumConnectionCount;
    }

    /**
     * @see org.logicalcobwebs.proxool.ConnectionPoolDefinitionIF#getHouseKeepingSleepTime
     */
    public int getHouseKeepingSleepTime() {
        return houseKeepingSleepTime;
    }

    /**
     * @see org.logicalcobwebs.proxool.ConnectionPoolDefinitionIF#getHouseKeepingSleepTime
     */
    public void setHouseKeepingSleepTime(int houseKeepingSleepTime) {
        this.houseKeepingSleepTime = houseKeepingSleepTime;
    }

    /**
     * @see org.logicalcobwebs.proxool.ConnectionPoolDefinitionIF#getSimultaneousBuildThrottle
     */
    public int getSimultaneousBuildThrottle() {
        return simultaneousBuildThrottle;
    }

    /**
     * @see org.logicalcobwebs.proxool.ConnectionPoolDefinitionIF#getSimultaneousBuildThrottle
     */
    public void setSimultaneousBuildThrottle(int simultaneousBuildThrottle) {
        this.simultaneousBuildThrottle = simultaneousBuildThrottle;
    }

    /**
     * @see org.logicalcobwebs.proxool.ConnectionPoolDefinitionIF#getRecentlyStartedThreshold
     */
    public int getRecentlyStartedThreshold() {
        return recentlyStartedThreshold;
    }

    /**
     * @see org.logicalcobwebs.proxool.ConnectionPoolDefinitionIF#getRecentlyStartedThreshold
     */
    public void setRecentlyStartedThreshold(int recentlyStartedThreshold) {
        this.recentlyStartedThreshold = recentlyStartedThreshold;
    }

    /**
     * @see org.logicalcobwebs.proxool.ConnectionPoolDefinitionIF#getOverloadWithoutRefusalLifetime
     */
    public int getOverloadWithoutRefusalLifetime() {
        return overloadWithoutRefusalLifetime;
    }

    /**
     * @see org.logicalcobwebs.proxool.ConnectionPoolDefinitionIF#getOverloadWithoutRefusalLifetime
     */
    public void setOverloadWithoutRefusalLifetime(int overloadWithoutRefusalLifetime) {
        this.overloadWithoutRefusalLifetime = overloadWithoutRefusalLifetime;
    }

    /**
     * @see org.logicalcobwebs.proxool.ConnectionPoolDefinitionIF#getMaximumActiveTime
     */
    public int getMaximumActiveTime() {
        return maximumActiveTime;
    }

    /**
     * @see org.logicalcobwebs.proxool.ConnectionPoolDefinitionIF#getMaximumActiveTime
     */
    public void setMaximumActiveTime(int maximumActiveTime) {
        this.maximumActiveTime = maximumActiveTime;
    }

    /**
     * @see org.logicalcobwebs.proxool.ConnectionPoolDefinitionIF#isVerbose
     */
    public boolean isVerbose() {
        return verbose;
    }

    /**
     * @see org.logicalcobwebs.proxool.ConnectionPoolDefinitionIF#isVerbose
     */
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    /**
     * @see org.logicalcobwebs.proxool.ConnectionPoolDefinitionIF#isTrace
     */
    public boolean isTrace() {
        return trace;
    }

    /**
     * @see org.logicalcobwebs.proxool.ConnectionPoolDefinitionIF#isTrace
     */
    public void setTrace(boolean trace) {
        this.trace = trace;
    }

    /**
     * @see org.logicalcobwebs.proxool.ConnectionPoolDefinitionIF#getStatistics
     */
    public String getStatistics() {
        return statistics;
    }

    /**
     * @see org.logicalcobwebs.proxool.ConnectionPoolDefinitionIF#getStatistics
     */
    public void setStatistics(String statistics) {
        this.statistics = statistics;
    }

    /**
     * @see org.logicalcobwebs.proxool.ConnectionPoolDefinitionIF#getStatisticsLogLevel
     */
    public String getStatisticsLogLevel() {
        return statisticsLogLevel;
    }

    /**
     * @see org.logicalcobwebs.proxool.ConnectionPoolDefinitionIF#getStatisticsLogLevel
     */
    public void setStatisticsLogLevel(String statisticsLogLevel) {
        this.statisticsLogLevel = statisticsLogLevel;
    }

    /**
     * @see org.logicalcobwebs.proxool.ConnectionPoolDefinitionIF#getFatalSqlExceptions
     */
    public String getFatalSqlExceptionsAsString() {
        return fatalSqlExceptionsAsString;
    }

    /**
     * @see org.logicalcobwebs.proxool.ConnectionPoolDefinitionIF#getFatalSqlExceptions
     */
    public void setFatalSqlExceptionsAsString(String fatalSqlExceptionsAsString) {
        this.fatalSqlExceptionsAsString = fatalSqlExceptionsAsString;
    }

    /**
     * @see org.logicalcobwebs.proxool.ConnectionPoolDefinitionIF#getHouseKeepingTestSql
     */
    public String getHouseKeepingTestSql() {
        return houseKeepingTestSql;
    }

    /**
     * @see org.logicalcobwebs.proxool.ConnectionPoolDefinitionIF#getHouseKeepingTestSql
     */
    public void setHouseKeepingTestSql(String houseKeepingTestSql) {
        this.houseKeepingTestSql = houseKeepingTestSql;
    }

    /**
     * @see org.logicalcobwebs.proxool.ConnectionPoolDefinitionIF#getUser
     */
    public String getUser() {
        return user;
    }

    /**
     * @see org.logicalcobwebs.proxool.ConnectionPoolDefinitionIF#getUser
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * @see org.logicalcobwebs.proxool.ConnectionPoolDefinitionIF#getPassword
     */
    public String getPassword() {
        return password;
    }

    /**
     * @see org.logicalcobwebs.proxool.ConnectionPoolDefinitionIF#getPassword
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
        simultaneousBuildThrottle = ConnectionPoolDefinitionIF.DEFAULT_SIMULTANEOUS_BUILD_THROTTLE;;
        recentlyStartedThreshold = ConnectionPoolDefinitionIF.DEFAULT_RECENTLY_STARTED_THRESHOLD;
        overloadWithoutRefusalLifetime = ConnectionPoolDefinitionIF.DEFAULT_OVERLOAD_WITHOUT_REFUSAL_THRESHOLD;
        maximumActiveTime = ConnectionPoolDefinitionIF.DEFAULT_MAXIMUM_ACTIVE_TIME;
        verbose = false;
        trace = false;
        statistics = null;
        statisticsLogLevel = null;
    }

    /**
     * <CODE>Referenceable</CODE> implementation prepares object for
     * binding in jndi.
     */
    public Reference getReference()
        throws NamingException
    {
        // this class implements its own factory
        String factory = getClass().getName();
        Reference ref = new Reference(getClass().getName(), factory, null);

        ref.add(new StringRefAddr("url", getUrl()));

        byte[] ser = null;
        // BinaryRefAddr does not allow null byte[].
        if ( jndiEnvironment != null )
        {
            try
            {
                ser = serialize(jndiEnvironment);
                ref.add(new BinaryRefAddr("jndiEnvironment", ser));
            }
            catch (IOException ioe)
            {
                throw new NamingException("An IOException prevented " +
                   "serializing the jndiEnvironment properties.");
            }
        }

        // TODO
        return null;
    }

    /**
     * implements ObjectFactory to create an instance of this class
     */
    public Object getObjectInstance(Object refObj, Name name,
                                    Context context, Hashtable env)
        throws Exception
    {
        // TODO
        return null;
    }

    /**
     * Get the value of jndiEnvironment which is used when instantiating
     * a jndi InitialContext.  This InitialContext is used to locate the
     * backend ConnectionPoolDataSource.
     *
     * @return value of jndiEnvironment.
     */
    public String getJndiEnvironment(String key)
    {
        String value = null;
        if (jndiEnvironment != null)
        {
            value = jndiEnvironment.getProperty(key);
        }
        return value;
    }

    /**
     * Set the value of jndiEnvironment which is used when instantiating
     * a jndi InitialContext.  This InitialContext is used to locate the
     * backend ConnectionPoolDataSource.
     *
     * @param key property key
     * @param value  to assign to jndiEnvironment.
     */
    public void setJndiEnvironment(String key, String value)
    {
        if (jndiEnvironment == null)
        {
            jndiEnvironment = new Properties();
        }
        jndiEnvironment.setProperty(key, value);
    }

    /**
     * Converts a object to a byte array for storage/serialization.
     *
     * @param obj The Serializable to convert.
     * @return A byte[] with the converted Serializable.
     * @exception java.io.IOException if conversion to a byte[] fails.
     */
    private static byte[] serialize(Serializable obj)
        throws IOException
    {
        byte[] byteArray = null;
        ByteArrayOutputStream baos = null;
        ObjectOutputStream out = null;
        try
        {
            // These objects are closed in the finally.
            baos = new ByteArrayOutputStream();
            out = new ObjectOutputStream(baos);

            out.writeObject(obj);
            byteArray = baos.toByteArray();
        }
        finally
        {
            if (out != null)
            {
                out.close();
            }
        }
        return byteArray;
    }

}

/*
 Revision history:
 $Log:
 */