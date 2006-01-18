/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.lang.reflect.Modifier;

/**
 * This defines a connection pool: the URL to connect to the database, the
 * delegate driver to use, and how the pool behaves.
 * @version $Revision: 1.34 $, $Date: 2006/01/18 14:40:01 $
 * @author billhorsman
 * @author $Author: billhorsman $ (current maintainer)
 */
class ConnectionPoolDefinition implements ConnectionPoolDefinitionIF {

    // TODO Should we check for defintion reads whilst updating?

    private static final Log LOG = LogFactory.getLog(ConnectionPoolDefinition.class);

    /**
     * This log has a category based on the alias
     */
    private Log poolLog = LOG;;

    private String alias;

    // JNDI properties

    private String jndiName;

    private String initialContextFactory;

    private String providerUrl;

    private String securityAuthentication;

    private String securityPrincipal;

    private String securityCredentials;

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

    private int simultaneousBuildThrottle;

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

    private String fatalSqlExceptionWrapper = null;

    private String houseKeepingTestSql;

    private boolean testBeforeUse;

    private boolean testAfterUse;

    private boolean jmx;

    private String jmxAgentId;

    private Class injectableConnectionInterface;

    private Class injectableStatementInterface;

    private Class injectablePreparedStatementInterface;

    private Class injectableCallableStatementInterface;

    /**
     * So we can set the values one by one if we want
     */
    public ConnectionPoolDefinition() {
    }

    /**
     * Construct a new definition
     * @param url the url that defines this pool
     * @param info additional properties (for Proxool and the delegate
     * driver)
     * @param explicitRegister set to true if we are registering a new pool explicitly, or false
     * if it's just because we are serving a url that we haven't come across before
     * @throws ProxoolException if anything goes wrong
     */
    protected ConnectionPoolDefinition(String url, Properties info, boolean explicitRegister) throws ProxoolException {
        this.alias = ProxoolFacade.getAlias(url);
        poolLog = LogFactory.getLog("org.logicalcobwebs.proxool." + alias);
        reset();
        doChange(url, info, false, !explicitRegister);
    }

    /**
     * Update the definition. All existing properties are retained
     * and only overwritten if included in the info parameter
     * @param url the url that defines this pool
     * @param info additional properties (for Proxool and the delegate
     * driver)
     * @throws ProxoolException if anything goes wrong
     */
    protected void update(String url, Properties info) throws ProxoolException {
        changedInfo.clear();
        connectionPropertiesChanged = false;
        poolLog.debug("Updating definition");
        doChange(url, info, false, false);
        if (connectionPropertiesChanged) {
            poolLog.info("Mercifully killing all current connections because of definition changes");
            ProxoolFacade.killAllConnections(alias, "of definition changes", true);
        }
    }

    /**
     * Redefine the definition. All existing properties are reset to their
     * default values
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
        doChange(url, info, false, false);

        // Check for minimum information
        if (getUrl() == null || getDriver() == null) {
            throw new ProxoolException("The URL is not defined properly: " + getCompleteUrl());
        }

        if (connectionPropertiesChanged) {
            LOG.info("Mercifully killing all current connections because of definition changes");
            ProxoolFacade.killAllConnections(alias, "definition has changed", true);
        }
    }

    private boolean doChange(String url, Properties info, boolean pretend, boolean implicitRegister) throws ProxoolException {

        boolean changed = false;

        try {
            int endOfPrefix = url.indexOf(':');
            int endOfDriver = url.indexOf(':', endOfPrefix + 1);

            if (endOfPrefix > -1 && endOfDriver > -1) {
                final String driver = url.substring(endOfPrefix + 1, endOfDriver);
                if (isChanged(getDriver(), driver)) {
                    changed = true;
                    if (!pretend) {
                        logChange(true, ProxoolConstants.DELEGATE_DRIVER_PROPERTY, driver);
                        setDriver(driver);
                    }
                }

                final String delegateUrl = url.substring(endOfDriver + 1);
                if (isChanged(getUrl(), delegateUrl)) {
                    changed = true;
                    if (!pretend) {
                        logChange(true, ProxoolConstants.DELEGATE_URL_PROPERTY, delegateUrl);
                        setUrl(delegateUrl);
                    }
                }
            } else {
                // Using alias. Nothing to do
            }
        } catch (IndexOutOfBoundsException e) {
            LOG.error("Invalid URL: '" + url + "'", e);
            throw new ProxoolException("Invalid URL: '" + url + "'");
        }

        if (!pretend) {
            setCompleteUrl(url);
        }

        if (info != null) {
            Iterator i = info.keySet().iterator();
            while (i.hasNext()) {
                String key = (String) i.next();
                String value = info.getProperty(key);
                changed = changed | setAnyProperty(key, value, pretend);
                if (!pretend) {
                    completeInfo.setProperty(key, value);
                }
            }
        }

        if (!pretend) {
            ProxoolFacade.definitionUpdated(getAlias(), this, completeInfo, changedInfo);
        }

        if ((getDriver() == null || getUrl() == null) && implicitRegister) {
            throw new ProxoolException("Attempt to refer to a unregistered pool by its alias '" + getAlias() + "'");
        }

        return changed;
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

    private boolean setAnyProperty(String key, String value, boolean pretend) throws ProxoolException {
        boolean proxoolProperty = true;
        boolean changed = false;

        // These groups of properties have been split off to make this method smaller
        changed = changed || setHouseKeeperProperty(key, value, pretend);
        changed = changed || setLoggingProperty(key, value, pretend);
        changed = changed || setInjectableProperty(key, value, pretend);
        changed = changed || setJndiProperty(key, value, pretend);

        if (key.equals(ProxoolConstants.USER_PROPERTY)) {
            proxoolProperty = false;
            if (isChanged(getUser(), value)) {
                changed = true;
                if (!pretend) {
                    setUser(value);
                }
            }
        } else if (key.equals(ProxoolConstants.PASSWORD_PROPERTY)) {
            proxoolProperty = false;
            if (isChanged(getPassword(), value)) {
                changed = true;
                if (!pretend) {
                    setPassword(value);
                }
            }
        } else if (key.equals(ProxoolConstants.DELEGATE_DRIVER_PROPERTY)) {
            if (isChanged(getDriver(), value)) {
                changed = true;
                if (!pretend) {
                    setDriver(value);
                }
            }
        } else if (key.equals(ProxoolConstants.DELEGATE_URL_PROPERTY)) {
            if (isChanged(getUrl(), value)) {
                changed = true;
                if (!pretend) {
                    setUrl(value);
                }
            }
        } else if (key.equals(ProxoolConstants.MAXIMUM_CONNECTION_COUNT_PROPERTY)) {
            if (getMaximumConnectionCount() != getInt(key, value)) {
                changed = true;
                if (!pretend) {
                    setMaximumConnectionCount(getInt(key, value));
                }
            }
        } else if (key.equals(ProxoolConstants.MAXIMUM_CONNECTION_LIFETIME_PROPERTY)) {
            if (getMaximumConnectionLifetime() != getInt(key, value)) {
                changed = true;
                if (!pretend) {
                    setMaximumConnectionLifetime(getInt(key, value));
                }
            }
        } else if (key.equals(ProxoolConstants.MAXIMUM_NEW_CONNECTIONS_PROPERTY)) {
            poolLog.warn("Use of " + ProxoolConstants.MAXIMUM_NEW_CONNECTIONS_PROPERTY + " is deprecated. Use more descriptive " + ProxoolConstants.SIMULTANEOUS_BUILD_THROTTLE_PROPERTY + " instead.");
            if (getSimultaneousBuildThrottle() != getInt(key, value)) {
                changed = true;
                if (!pretend) {
                    setSimultaneousBuildThrottle(getInt(key, value));
                }
            }
        } else if (key.equals(ProxoolConstants.SIMULTANEOUS_BUILD_THROTTLE_PROPERTY)) {
            if (getSimultaneousBuildThrottle() != getInt(key, value)) {
                changed = true;
                setSimultaneousBuildThrottle(getInt(key, value));
            }
        } else if (key.equals(ProxoolConstants.MINIMUM_CONNECTION_COUNT_PROPERTY)) {
            if (getMinimumConnectionCount() != getInt(key, value)) {
                changed = true;
                if (!pretend) {
                    setMinimumConnectionCount(getInt(key, value));
                }
            }
        } else if (key.equals(ProxoolConstants.PROTOTYPE_COUNT_PROPERTY)) {
            if (getPrototypeCount() != getInt(key, value)) {
                changed = true;
                if (!pretend) {
                    setPrototypeCount(getInt(key, value));
                }
            }
        } else if (key.equals(ProxoolConstants.RECENTLY_STARTED_THRESHOLD_PROPERTY)) {
            if (getRecentlyStartedThreshold() != getInt(key, value)) {
                changed = true;
                if (!pretend) {
                    setRecentlyStartedThreshold(getInt(key, value));
                }
            }
        } else if (key.equals(ProxoolConstants.OVERLOAD_WITHOUT_REFUSAL_LIFETIME_PROPERTY)) {
            if (getOverloadWithoutRefusalLifetime() != getInt(key, value)) {
                changed = true;
                if (!pretend) {
                    setOverloadWithoutRefusalLifetime(getInt(key, value));
                }
            }
        } else if (key.equals(ProxoolConstants.MAXIMUM_ACTIVE_TIME_PROPERTY)) {
            if (getMaximumActiveTime() != getInt(key, value)) {
                changed = true;
                if (!pretend) {
                    setMaximumActiveTime(getInt(key, value));
                }
            }
        } else if (key.equals(ProxoolConstants.FATAL_SQL_EXCEPTION_PROPERTY)) {
            if (isChanged(fatalSqlExceptionsAsString, value)) {
                changed = true;
                if (!pretend) {
                    setFatalSqlExceptionsAsString(value.length() > 0 ? value : null);
                }
            }
        } else if (key.equals(ProxoolConstants.FATAL_SQL_EXCEPTION_WRAPPER_CLASS_PROPERTY)) {
            if (isChanged(fatalSqlExceptionWrapper, value)) {
                changed = true;
                if (!pretend) {
                    setFatalSqlExceptionWrapper(value.length() > 0 ? value : null);
                }
            }
        } else if (key.equals(ProxoolConstants.STATISTICS_PROPERTY)) {
            if (isChanged(getStatistics(), value)) {
                changed = true;
                if (!pretend) {
                    setStatistics(value.length() > 0 ? value : null);
                }
            }
        } else if (key.equals(ProxoolConstants.STATISTICS_LOG_LEVEL_PROPERTY)) {
            if (isChanged(getStatisticsLogLevel(), value)) {
                changed = true;
                if (!pretend) {
                    setStatisticsLogLevel(value.length() > 0 ? value : null);
                }
            }
        }

        if (!key.startsWith(ProxoolConstants.PROPERTY_PREFIX)) {
            if (isChanged(getDelegateProperty(key), value)) {
                changed = true;
                if (!pretend) {
                    setDelegateProperty(key, value);
                }
            }
            proxoolProperty = false;
        }

        if (changed && !pretend) {
            logChange(proxoolProperty, key, value);
            changedInfo.setProperty(key, value);
        }
        return changed;
    }

    /**
     * Subset of {@link #setAnyProperty} to avoid overly long method
     * @see #setAnyProperty
     */
    private boolean setLoggingProperty(String key, String value, boolean pretend) {
        boolean changed = false;
        if (key.equals(ProxoolConstants.DEBUG_LEVEL_PROPERTY)) {
            if (value != null && value.equals("1")) {
                poolLog.warn("Use of " + ProxoolConstants.DEBUG_LEVEL_PROPERTY + "=1 is deprecated. Use " + ProxoolConstants.VERBOSE_PROPERTY + "=true instead.");
                if (!isVerbose()) {
                    changed = true;
                    if (!pretend) {
                        setVerbose(true);
                    }
                }
            } else {
                poolLog.warn("Use of " + ProxoolConstants.DEBUG_LEVEL_PROPERTY + "=0 is deprecated. Use " + ProxoolConstants.VERBOSE_PROPERTY + "=false instead.");
                if (isVerbose()) {
                    changed = true;
                    if (!pretend) {
                        setVerbose(false);
                    }
                }
            }
        } else if (key.equals(ProxoolConstants.VERBOSE_PROPERTY)) {
            final boolean valueAsBoolean = Boolean.valueOf(value).booleanValue();
            if (isVerbose() != valueAsBoolean) {
                changed = true;
                if (!pretend) {
                    setVerbose(valueAsBoolean);
                }
            }
        } else if (key.equals(ProxoolConstants.TRACE_PROPERTY)) {
            final boolean valueAsBoolean = Boolean.valueOf(value).booleanValue();
            if (isTrace() != valueAsBoolean) {
                changed = true;
                if (!pretend) {
                    setTrace(valueAsBoolean);
                }
            }
        }
        return changed;
    }

    /**
     * Subset of {@link #setAnyProperty} to avoid overly long method
     * @see #setAnyProperty
     */
    private boolean setInjectableProperty(String key, String value, boolean pretend) {
        boolean changed = false;
        if (key.equals(ProxoolConstants.INJECTABLE_CONNECTION_INTERFACE_NAME_PROPERTY)) {
            if (isChanged(getInjectableConnectionInterfaceName(), value)) {
                changed = true;
                if (!pretend) {
                    setInjectableConnectionInterfaceName(value.length() > 0 ? value : null);
                }
            }
        } else if (key.equals(ProxoolConstants.INJECTABLE_STATEMENT_INTERFACE_NAME_PROPERTY)) {
            if (isChanged(getInjectableStatementInterfaceName(), value)) {
                changed = true;
                if (!pretend) {
                    setInjectableStatementInterfaceName(value.length() > 0 ? value : null);
                }
            }
        } else if (key.equals(ProxoolConstants.INJECTABLE_PREPARED_STATEMENT_INTERFACE_NAME_PROPERTY)) {
            if (isChanged(getInjectablePreparedStatementInterfaceName(), value)) {
                changed = true;
                if (!pretend) {
                    setInjectablePreparedStatementInterfaceName(value.length() > 0 ? value : null);
                }
            }
        } else if (key.equals(ProxoolConstants.INJECTABLE_CALLABLE_STATEMENT_INTERFACE_NAME_PROPERTY)) {
            if (isChanged(getInjectableCallableStatememtInterfaceName(), value)) {
                changed = true;
                if (!pretend) {
                    setInjectableCallableStatementInterfaceName(value.length() > 0 ? value : null);
                }
            }
        }
        return changed;
    }

    /**
     * Subset of {@link #setAnyProperty} to avoid overly long method.
     * @see #setAnyProperty
     */
    private boolean setHouseKeeperProperty(String key, String value, boolean pretend) throws ProxoolException {
        boolean changed = false;
        if (key.equals(ProxoolConstants.HOUSE_KEEPING_SLEEP_TIME_PROPERTY)) {
            if (getHouseKeepingSleepTime() != getInt(key, value)) {
                changed = true;
                if (!pretend) {
                    setHouseKeepingSleepTime(getInt(key, value));
                }
            }
        } else if (key.equals(ProxoolConstants.HOUSE_KEEPING_TEST_SQL_PROPERTY)) {
            if (isChanged(getHouseKeepingTestSql(), value)) {
                changed = true;
                if (!pretend) {
                    setHouseKeepingTestSql(value.length() > 0 ? value : null);
                }
            }
        } else if (key.equals(ProxoolConstants.TEST_BEFORE_USE_PROPERTY)) {
            final boolean valueAsBoolean = Boolean.valueOf(value).booleanValue();
            if (isTestBeforeUse() != valueAsBoolean) {
                changed = true;
                if (!pretend) {
                    setTestBeforeUse(valueAsBoolean);
                }
            }
        } else if (key.equals(ProxoolConstants.TEST_AFTER_USE_PROPERTY)) {
            final boolean valueAsBoolean = Boolean.valueOf(value).booleanValue();
            if (isTestAfterUse() != valueAsBoolean) {
                changed = true;
                if (!pretend) {
                    setTestAfterUse(valueAsBoolean);
                }
            }
        }
        return changed;
    }

    /**
     * Subset of {@link #setAnyProperty} to avoid overly long method
     * @see #setAnyProperty
     */
    private boolean setJndiProperty(String key, String value, boolean pretend) {
        boolean changed = false;
        if (key.equals(ProxoolConstants.JNDI_NAME_PROPERTY)) {
            if (isChanged(getJndiName(), value)) {
                changed = true;
                if (!pretend) {
                    setJndiName(value.length() > 0 ? value : null);
                }
            }
        } else {

        }
        return changed;
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
     * Deep clone of definition
     * @return the new definition
     * @throws CloneNotSupportedException
     */
    protected Object clone() throws CloneNotSupportedException {
        ConnectionPoolDefinition clone = new ConnectionPoolDefinition();

        clone.setCompleteUrl(completeUrl);
        clone.setDelegateProperties((Properties) delegateProperties.clone());
        clone.setCompleteInfo((Properties) completeInfo.clone());
        clone.clearChangedInfo();

        clone.setAlias(alias);
        clone.setUrl(url);
        clone.setDriver(driver);
        clone.setMaximumConnectionLifetime(maximumConnectionLifetime);
        clone.setPrototypeCount(prototypeCount);
        clone.setMinimumConnectionCount(minimumConnectionCount);
        clone.setMaximumConnectionCount(maximumConnectionCount);
        clone.setHouseKeepingSleepTime(houseKeepingSleepTime);
        clone.setHouseKeepingTestSql(houseKeepingTestSql);
        clone.setTestAfterUse(testAfterUse);
        clone.setTestBeforeUse(testBeforeUse);
        clone.setSimultaneousBuildThrottle(simultaneousBuildThrottle);
        clone.setRecentlyStartedThreshold(recentlyStartedThreshold);
        clone.setOverloadWithoutRefusalLifetime(overloadWithoutRefusalLifetime);
        clone.setMaximumActiveTime(maximumActiveTime);
        clone.setVerbose(verbose);
        clone.setTrace(trace);
        clone.setStatistics(statistics);
        clone.setStatisticsLogLevel(statisticsLogLevel);
        clone.setFatalSqlExceptionsAsString(fatalSqlExceptionsAsString);
        try {
            clone.setFatalSqlExceptionWrapper(fatalSqlExceptionWrapper);
        } catch (ProxoolException e) {
            throw new IllegalArgumentException("Problem cloning fatalSqlExceptionWrapper: " + fatalSqlExceptionWrapper);
        }
        return clone;
    }

    private void clearChangedInfo() {
        changedInfo.clear();
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
        testAfterUse = false;
        testBeforeUse = false;
        simultaneousBuildThrottle = DEFAULT_SIMULTANEOUS_BUILD_THROTTLE;
        recentlyStartedThreshold = DEFAULT_RECENTLY_STARTED_THRESHOLD;
        overloadWithoutRefusalLifetime = DEFAULT_OVERLOAD_WITHOUT_REFUSAL_THRESHOLD;
        maximumActiveTime = DEFAULT_MAXIMUM_ACTIVE_TIME;
        verbose = false;
        trace = false;
        statistics = null;
        statisticsLogLevel = null;
        fatalSqlExceptions.clear();
        fatalSqlExceptionWrapper = null;
    }

    /**
     * Get all the properties used to define this pool
     * @return
     */
    protected Properties getCompleteInfo() {
        return completeInfo;
    }

    /**
     * Overwrite the complete info
     * @param completeInfo the new properties
     * @see #getCompleteInfo()
     */
    public void setCompleteInfo(Properties completeInfo) {
        this.completeInfo = completeInfo;
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
     * @deprecated use more descriptive {@link  #getSimultaneousBuildThrottle} instead
     */
    public int getMaximumNewConnections() {
        return simultaneousBuildThrottle;
    }

    /**
     * @see ConnectionPoolDefinitionIF#getMaximumNewConnections
     * @deprecated use more descriptive {@link  #setSimultaneousBuildThrottle} instead
     */
    public void setMaximumNewConnections(int maximumNewConnections) {
        this.simultaneousBuildThrottle = maximumNewConnections;
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
     * Overwrite the delegate properties
     * @param delegateProperties the new properties
     * @see ConnectionPoolDefinitionIF#getProperties
     */
    public void setDelegateProperties(Properties delegateProperties) {
        this.delegateProperties = delegateProperties;
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
        if (fatalSqlExceptionsAsString != null) {
            StringTokenizer st = new StringTokenizer(fatalSqlExceptionsAsString, FATAL_SQL_EXCEPTIONS_DELIMITER);
            while (st.hasMoreTokens()) {
                fatalSqlExceptions.add(st.nextToken().trim());
            }
        }
    }

    /**
     * @see ConnectionPoolDefinitionIF#getFatalSqlExceptions
     */
    public Set getFatalSqlExceptions() {
        return fatalSqlExceptions;
    }

    /**
     * @see ConnectionPoolDefinitionIF#getFatalSqlExceptionWrapper
     */
    public String getFatalSqlExceptionWrapper() {
        return fatalSqlExceptionWrapper;
    }

    /**
     * @see ConnectionPoolDefinitionIF#getFatalSqlExceptionWrapper
     */
    public void setFatalSqlExceptionWrapper(String fatalSqlExceptionWrapper) throws ProxoolException {

        //  Test it out. That's the best way.
        try {
            FatalSqlExceptionHelper.throwFatalSQLException(fatalSqlExceptionWrapper, new SQLException("Test"));
        } catch (SQLException e) {
            // That's OK, we were expecting one of these
        } catch (RuntimeException e) {
            // That's OK, we were expecting one of these
        }

        this.fatalSqlExceptionWrapper = fatalSqlExceptionWrapper;
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

// Start JNDI
    public String getJndiName() {
        return jndiName;
    }

    public void setJndiName(String jndiName) {
        this.jndiName = jndiName;
    }

    public String getInitialContextFactory() {
        return initialContextFactory;
    }

    public void setInitialContextFactory(String initialContextFactory) {
        this.initialContextFactory = initialContextFactory;
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
// End JNDI

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
     * @see ConnectionPoolDefinitionIF#getInjectableConnectionInterface()
     */
    public Class getInjectableConnectionInterface() {
        return injectableConnectionInterface;
    }

    /**
     * @see ConnectionPoolDefinitionIF#getInjectableConnectionInterface()
     */
    public String getInjectableConnectionInterfaceName() {
        if (getInjectableConnectionInterface() != null) {
            return getInjectableConnectionInterface().getName();
        } else {
            return null;
        }
    }

    /**
     * @param injectableConnectionInterfaceName the fully qualified class name
     * @see ConnectionPoolDefinitionIF#getInjectableConnectionInterface()
     */
    public void setInjectableConnectionInterfaceName(String injectableConnectionInterfaceName) {
        this.injectableConnectionInterface = getInterface(injectableConnectionInterfaceName);
    }

    /**
     * @see ConnectionPoolDefinitionIF#getInjectableStatementInterface()
     */
    public Class getInjectableStatementInterface() {
        return injectableStatementInterface;
    }

    /**
     * @see ConnectionPoolDefinitionIF#getInjectableStatementInterface()
     */
    public String getInjectableStatementInterfaceName() {
        if (getInjectableStatementInterface() != null) {
            return getInjectableStatementInterface().getName();
        } else {
            return null;
        }
    }

    /**
     * @param injectableStatementInterfaceName the fully qualified class name
     * @see ConnectionPoolDefinitionIF#getInjectableStatementInterface()
     */
    public void setInjectableStatementInterfaceName(String injectableStatementInterfaceName) {
        this.injectableStatementInterface = getInterface(injectableStatementInterfaceName);
    }

    /**
     * @see ConnectionPoolDefinitionIF#getInjectablePreparedStatementInterface()
     */
    public Class getInjectablePreparedStatementInterface() {
        return injectablePreparedStatementInterface;
    }

    /**
     * @see ConnectionPoolDefinitionIF#getInjectablePreparedStatementInterface()
     */
    public String getInjectablePreparedStatementInterfaceName() {
        if (getInjectablePreparedStatementInterface() != null) {
            return getInjectablePreparedStatementInterface().getName();
        } else {
            return null;
        }
    }

    /**
     * @param injectablePreparedStatementInterfaceName the fully qualified class name
     * @see ConnectionPoolDefinitionIF#getInjectablePreparedStatementInterface()
     */
    public void setInjectablePreparedStatementInterfaceName(String injectablePreparedStatementInterfaceName) {
        this.injectablePreparedStatementInterface = getInterface(injectablePreparedStatementInterfaceName);
    }

    /**
     * @see ConnectionPoolDefinitionIF#getInjectableCallableStatementInterface()
     */
    public String getInjectableCallableStatememtInterfaceName() {
        if (getInjectableCallableStatementInterface() != null) {
            return getInjectableCallableStatementInterface().getName();
        } else {
            return null;
        }
    }

    /**
     * @see ConnectionPoolDefinitionIF#getInjectableCallableStatementInterface()
     */
    public Class getInjectableCallableStatementInterface() {
        return injectableCallableStatementInterface;
    }

    /**
     * @param injectableCallableStatementInterfaceName the fully qualified class name
     * @see ConnectionPoolDefinitionIF#getInjectableCallableStatementInterface()
     */
    public void setInjectableCallableStatementInterfaceName(String injectableCallableStatementInterfaceName) {
        this.injectableCallableStatementInterface = getInterface(injectableCallableStatementInterfaceName);
    }

    private Class getInterface(String className) {
        try {
            Class clazz = null;
            if (className != null && className.length() > 0) {
                clazz = Class.forName(className);
                if (!clazz.isInterface()) {
                    throw new IllegalArgumentException(className + " is a class. It must be an interface.");
                }
                if (!Modifier.isPublic(clazz.getModifiers())) {
                    throw new IllegalArgumentException(className + " is a protected interface. It must be public.");
                }
            }
            return clazz;
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(className + " couldn't be found");
        }
    }

    /**
     * Returns true if {@link #redefine redefining} the pool using
     * these parameters would not change the definition. You can
     * use this to decide whether or not to trigger a change
     * {@link ConfigurationListenerIF#definitionUpdated event}.
     *
     * @param url the url (containing alias and possible delegate url and driver)
     * @param info the properties
     * @return true if the definition is identical to that that represented by these parameters
     */
    public boolean isEqual(String url, Properties info) {
        try {
            return !doChange(url, info, true, false);
        } catch (ProxoolException e) {
            LOG.error("Problem checking equality", e);
            return false;
        }
/*
        boolean equal = true;

        if (info == null && completeInfo != null) {
            equal = false;
        } else if (info != null && completeInfo == null) {
            equal = false;
        } else if (!info.equals(completeInfo)) {
            equal = false;
        } else if (!url.equals(completeUrl)) {
            equal = false;
        }

        return equal;
*/
    }

}

/*
 Revision history:
 $Log: ConnectionPoolDefinition.java,v $
 Revision 1.34  2006/01/18 14:40:01  billhorsman
 Unbundled Jakarta's Commons Logging.

 Revision 1.33  2005/05/04 16:24:59  billhorsman
 Now supports cloning.

 Revision 1.32  2004/06/02 20:19:14  billhorsman
 Added injectable interface properties

 Revision 1.31  2004/03/18 17:08:14  chr32
 Added jmx* properties.

 Revision 1.30  2004/03/15 02:42:44  chr32
 Removed explicit JNDI properties. Going for a generic approach instead.

 Revision 1.29  2003/10/30 00:13:59  billhorsman
 Fixed bug where all proxool properties were getting passed onto the delegate driver, and all delegate properties weren't

 Revision 1.28  2003/10/24 15:22:21  billhorsman
 Fixed bug where connection pool was being recognised as changed even when it wasn't. (This bug introduced after 0.7.2).

 Revision 1.27  2003/10/20 11:40:53  billhorsman
 Smarter handling of null and empty strings. No NPE during unit tests now.

 Revision 1.26  2003/10/19 13:31:57  billhorsman
 Setting a property to a zero length String actually sets it to a null

 Revision 1.25  2003/10/16 18:54:49  billhorsman
 Fixed javadoc for update() and redefine() methods which were transposed. Also improved exception handling for
 incomplete pool definitions.

 Revision 1.24  2003/09/30 18:39:08  billhorsman
 New test-before-use, test-after-use and fatal-sql-exception-wrapper-class properties.

 Revision 1.23  2003/09/29 17:48:08  billhorsman
 New fatal-sql-exception-wrapper-class allows you to define what exception is used as a wrapper. This means that you
 can make it a RuntimeException if you need to.

 Revision 1.22  2003/09/05 16:59:42  billhorsman
 Added wrap-fatal-sql-exceptions property

 Revision 1.21  2003/08/30 14:54:04  billhorsman
 Checkstyle

 Revision 1.20  2003/08/30 11:37:31  billhorsman
 Trim fatal-sql-exception messages so that whitespace around the comma delimiters does not
 get used to match against exception message.

 Revision 1.19  2003/07/23 06:54:48  billhorsman
 draft JNDI changes (shouldn't effect normal operation)

 Revision 1.18  2003/04/27 15:42:21  billhorsman
 fix to condition that meant configuration change was getting sent too often (and sometimes not at all)

 Revision 1.17  2003/04/19 12:58:41  billhorsman
 fixed bug where ConfigurationListener's
 definitionUpdated was getting called too
 frequently

 Revision 1.16  2003/04/10 21:50:16  billhorsman
 empty constructor for use by DataSource

 Revision 1.15  2003/03/11 14:51:49  billhorsman
 more concurrency fixes relating to snapshots

 Revision 1.14  2003/03/10 23:43:09  billhorsman
 reapplied checkstyle that i'd inadvertently let
 IntelliJ change...

 Revision 1.13  2003/03/10 15:26:45  billhorsman
 refactoringn of concurrency stuff (and some import
 optimisation)

 Revision 1.12  2003/03/05 23:28:56  billhorsman
 deprecated maximum-new-connections property in favour of
 more descriptive simultaneous-build-throttle

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
