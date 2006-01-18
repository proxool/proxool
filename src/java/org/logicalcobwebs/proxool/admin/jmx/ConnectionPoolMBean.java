/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool.admin.jmx;

import org.logicalcobwebs.proxool.resources.ResourceNamesIF;
import org.logicalcobwebs.proxool.ProxoolConstants;
import org.logicalcobwebs.proxool.ConnectionPoolDefinitionIF;
import org.logicalcobwebs.proxool.ProxoolFacade;
import org.logicalcobwebs.proxool.ProxoolException;
import org.logicalcobwebs.proxool.ProxoolListenerIF;
import org.logicalcobwebs.proxool.ConfigurationListenerIF;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.DynamicMBean;
import javax.management.AttributeNotFoundException;
import javax.management.MBeanException;
import javax.management.ReflectionException;
import javax.management.RuntimeOperationsException;
import javax.management.Attribute;
import javax.management.InvalidAttributeValueException;
import javax.management.AttributeList;
import javax.management.MBeanParameterInfo;
import javax.management.NotificationBroadcaster;
import javax.management.NotificationBroadcasterSupport;
import javax.management.NotificationListener;
import javax.management.NotificationFilter;
import javax.management.ListenerNotFoundException;
import javax.management.Notification;
import javax.management.MBeanRegistration;
import javax.management.ObjectName;
import javax.management.MBeanServer;

import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.Properties;
import java.util.StringTokenizer;
import java.text.MessageFormat;

/**
 * JMX DynamicMBean adapter for a Proxool connection pool.<br>
 * See the configuration documentation to learn
 * how to activate a pool for JMX. No programming is necessary to do this.
 * <p>
 * <b>Attributes</b>
 * <ul>
 * <li>alias</li>
 * <li>driverClass</li>
 * <li>driverUrl</li>
 * <li>driverProperties</li>
 * <li>fatalSqlException</li>
 * <li>houseKeepingSleeptime</li>
 * <li>houseKeepingTestSql</li>
 * <li>maximumActiveTime</li>
 * <li>maximumConnectionCount</li>
 * <li>maximumConnectionLifetime</li>
 * <li>minimumConnectionCount</li>
 * <li>maximumNewConnections</li>
 * <li>overloadWithoutRefusalLifetime</li>
 * <li>recentlyStartedThreshold</li>
 * <li>prototypeCount</li>
 * <li>trace</li>
 * <li>verbose</li>
 * </ul>
 * </p>
 * <p>
 * <b>Operations</b>
 * <ul>
 * <li>shutdown</li>
 * </ul>
 * </p>
 * <p>
 * <b>Notifications</b>
 * <ul>
 * <li>{@link #NOTIFICATION_TYPE_DEFINITION_UPDATED}</li>
 * </ul>
 * </p>
 * @version $Revision: 1.15 $, $Date: 2006/01/18 14:39:55 $
 * @author Christian Nedregaard (christian_nedregaard@email.com)
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.8
 */
public class ConnectionPoolMBean implements DynamicMBean, MBeanRegistration, NotificationBroadcaster,
        ProxoolListenerIF, ConfigurationListenerIF {
    /**
     * Notification type emitted when the pool definition is updated.
     */
    public static final String NOTIFICATION_TYPE_DEFINITION_UPDATED = "proxool.definitionUpdated";


    private static final Log LOG = LogFactory.getLog(ConnectionPoolMBean.class);
    private static final String CLASS_NAME = ConnectionPoolMBean.class.getName();

    private static final String RECOURCE_NAME_MBEAN_POOL_DESCRIPTION = "mbean.pool.description";
    private static final String RECOURCE_NAME_MBEAN_NOTIFICATION_DESCRIPTION = "mbean.notification.description";
    private static final String RECOURCE_NAME_MBEAN_NOTIFICATION_DEF_UPDATED = "mbean.notification.defUpdated";

    private static final String OPERATION_NAME_SHUTDOWN = "shutdown";

    private static final ResourceBundle ATTRIBUTE_DESCRIPTIONS_RESOURCE = createAttributeDescriptionsResource();
    private static final ResourceBundle JMX_RESOURCE = createJMXResource();

    private static final MBeanNotificationInfo[] NOTIFICATION_INFOS = getNotificationInfos();

    private MBeanInfo mBeanInfo;
    private ConnectionPoolDefinitionIF poolDefinition;
    private Properties poolProperties;
    private long definitionUpdatedSequence;
    private NotificationBroadcasterSupport notificationHelper = new NotificationBroadcasterSupport();
    private boolean active;

    public ConnectionPoolMBean(String alias, Properties poolProperties)
            throws ProxoolException {
        this.poolDefinition = ProxoolFacade
                .getConnectionPoolDefinition(alias);
        this.poolProperties = poolProperties;
        this.mBeanInfo = getDynamicMBeanInfo(this.poolDefinition.getAlias());
        ProxoolFacade.addProxoolListener(this);
        ProxoolFacade.addConfigurationListener(alias, this);
    }

    /**
     * @see javax.management.DynamicMBean#getAttribute(java.lang.String)
     */
    public Object getAttribute(String attributeName) throws AttributeNotFoundException, MBeanException, ReflectionException {
        if (attributeName == null) {
            final String message = "Cannot invoke a getter of " + CLASS_NAME + " with null attribute name";
            LOG.error(message);
            throw new RuntimeOperationsException(new IllegalArgumentException("Attribute name cannot be null"),
                    message);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Getting attribute " + attributeName + ".");
        }
        return ((Attribute) getAttributes(new String[]{attributeName}).get(0)).getValue();
    }

    /**
     * @see javax.management.DynamicMBean#setAttribute(javax.management.Attribute)
     */
    public void setAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException,
            MBeanException, ReflectionException {
        if (attribute == null) {
            final String message = "Cannot invoke a setter of " + CLASS_NAME + " with null attribute";
            LOG.error(message);
            throw new RuntimeOperationsException(new IllegalArgumentException("Attribute cannot be null"),
                    message);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Setting attribute " + attribute.getName() + ".");
        }
        final AttributeList attributeList = new AttributeList();
        attributeList.add(attribute);
        setAttributes(attributeList);
    }

    /**
     * @see javax.management.DynamicMBean#getAttributes(java.lang.String[])
     */
    public AttributeList getAttributes(String[] attributeNames) {
        if (attributeNames == null) {
            final String message = "Cannot invoke a null getter of " + CLASS_NAME;
            LOG.error(message);
            throw new RuntimeOperationsException(new IllegalArgumentException("attributeNames[] cannot be null"),
                    message);
        }
        AttributeList resultList = new AttributeList();

        // if attributeNames is empty, return an empty result list
        if (attributeNames.length == 0) {
            return resultList;
        }

        // build the result attribute list
        for (int i = 0; i < attributeNames.length; i++) {
            try {
                if (equalsProperty(attributeNames[i], ProxoolConstants.ALIAS)) {
                    resultList.add(new Attribute(attributeNames[i],
                            this.poolDefinition.getAlias()));
                } else if (equalsProperty(attributeNames[i], ProxoolConstants.DRIVER_PROPERTIES)) {
                    resultList.add(new Attribute(attributeNames[i],
                            getDelegatePropertiesAsString(this.poolProperties)));
                } else if (equalsProperty(attributeNames[i], ProxoolConstants.DRIVER_URL)) {
                    resultList.add(new Attribute(attributeNames[i],
                            this.poolDefinition.getUrl()));
                } else if (equalsProperty(attributeNames[i], ProxoolConstants.FATAL_SQL_EXCEPTION)) {
                    resultList.add(new Attribute(attributeNames[i],
                            getValueOrEmpty(this.poolProperties.getProperty(ProxoolConstants.FATAL_SQL_EXCEPTION_PROPERTY))));
                } else if (equalsProperty(attributeNames[i], ProxoolConstants.HOUSE_KEEPING_SLEEP_TIME)) {
                    resultList.add(new Attribute(attributeNames[i],
                            new Integer(this.poolDefinition.getHouseKeepingSleepTime())));
                } else if (equalsProperty(attributeNames[i], ProxoolConstants.HOUSE_KEEPING_TEST_SQL)) {
                    resultList.add(new Attribute(attributeNames[i],
                            getValueOrEmpty(poolDefinition.getHouseKeepingTestSql())));
                } else if (equalsProperty(attributeNames[i], ProxoolConstants.TEST_BEFORE_USE)) {
                    resultList.add(new Attribute(attributeNames[i],
                            new Boolean(this.poolDefinition.isTestBeforeUse())));
                } else if (equalsProperty(attributeNames[i], ProxoolConstants.TEST_AFTER_USE)) {
                    resultList.add(new Attribute(attributeNames[i],
                            new Boolean(this.poolDefinition.isTestAfterUse())));
                } else if (equalsProperty(attributeNames[i], ProxoolConstants.MAXIMUM_ACTIVE_TIME)) {
                    resultList.add(new Attribute(attributeNames[i],
                            new Integer(this.poolDefinition.getMaximumActiveTime())));
                } else if (equalsProperty(attributeNames[i], ProxoolConstants.MAXIMUM_CONNECTION_COUNT)) {
                    resultList.add(new Attribute(attributeNames[i],
                            new Integer(this.poolDefinition.getMaximumConnectionCount())));
                } else if (equalsProperty(attributeNames[i], ProxoolConstants.MAXIMUM_CONNECTION_LIFETIME)) {
                    resultList.add(new Attribute(attributeNames[i],
                            new Integer(this.poolDefinition.getMaximumConnectionLifetime())));
                } else if (equalsProperty(attributeNames[i], ProxoolConstants.MAXIMUM_NEW_CONNECTIONS)) {
                    resultList.add(new Attribute(attributeNames[i],
                            new Integer(this.poolDefinition.getMaximumNewConnections())));
                } else if (equalsProperty(attributeNames[i], ProxoolConstants.SIMULTANEOUS_BUILD_THROTTLE)) {
                    resultList.add(new Attribute(attributeNames[i],
                            new Integer(this.poolDefinition.getSimultaneousBuildThrottle())));
                } else if (equalsProperty(attributeNames[i], ProxoolConstants.MINIMUM_CONNECTION_COUNT)) {
                    resultList.add(new Attribute(attributeNames[i],
                            new Integer(this.poolDefinition.getMinimumConnectionCount())));
                } else if (equalsProperty(attributeNames[i], ProxoolConstants.OVERLOAD_WITHOUT_REFUSAL_LIFETIME)) {
                    resultList.add(new Attribute(attributeNames[i],
                            new Integer(this.poolDefinition.getOverloadWithoutRefusalLifetime())));
                } else if (equalsProperty(attributeNames[i], ProxoolConstants.PROTOTYPE_COUNT)) {
                    resultList.add(new Attribute(attributeNames[i],
                            new Integer(this.poolDefinition.getPrototypeCount())));
                } else if (equalsProperty(attributeNames[i], ProxoolConstants.RECENTLY_STARTED_THRESHOLD)) {
                    resultList.add(new Attribute(attributeNames[i],
                            new Integer(this.poolDefinition.getRecentlyStartedThreshold())));
                } else if (equalsProperty(attributeNames[i], ProxoolConstants.STATISTICS)) {
                    resultList.add(new Attribute(attributeNames[i],
                            getValueOrEmpty(this.poolDefinition.getStatistics())));
                } else if (equalsProperty(attributeNames[i], ProxoolConstants.STATISTICS_LOG_LEVEL)) {
                    resultList.add(new Attribute(attributeNames[i],
                            getValueOrEmpty(this.poolDefinition.getStatisticsLogLevel())));
                } else if (equalsProperty(attributeNames[i], ProxoolConstants.TRACE)) {
                    resultList.add(new Attribute(attributeNames[i],
                            new Boolean(this.poolDefinition.isTrace())));
                } else if (equalsProperty(attributeNames[i], ProxoolConstants.VERBOSE)) {
                    resultList.add(new Attribute(attributeNames[i],
                            new Boolean(this.poolDefinition.isVerbose())));
                } else if (equalsProperty(attributeNames[i], ProxoolConstants.FATAL_SQL_EXCEPTION_WRAPPER_CLASS)) {
                    resultList.add(new Attribute(attributeNames[i],
                            getValueOrEmpty(this.poolDefinition.getFatalSqlExceptionWrapper())));
                } else {
                    final String message = "Unknown attribute: " + attributeNames[i];
                    LOG.error(message);
                    throw new AttributeNotFoundException(message);
                }
            } catch (AttributeNotFoundException e) {
                throw new RuntimeOperationsException(new IllegalArgumentException(e.getMessage()));
            }
        }
        return resultList;
    }

    /**
     * @see javax.management.DynamicMBean#setAttributes(javax.management.AttributeList)
     */
    public AttributeList setAttributes(AttributeList attributes) {

        if (attributes == null) {
            final String message = "AttributeList attributes cannot be null";
            LOG.error(message);
            throw new RuntimeOperationsException(new IllegalArgumentException(message),
                    "Cannot invoke a setter of " + CLASS_NAME);
        }
        AttributeList resultList = new AttributeList();

        if (attributes.isEmpty()) {
            return resultList;
        }

        String name = null;
        Object value = null;
        final Properties newProperties = new Properties();
        Attribute attribute = null;
        for (Iterator i = attributes.iterator(); i.hasNext();) {
            attribute = (Attribute) i.next();
            try {
                name = attribute.getName();
                value = attribute.getValue();

                if (equalsProperty(name, ProxoolConstants.DRIVER_PROPERTIES)) {
                    if (!isEqualProperties(value.toString(), getDelegatePropertiesAsString(this.poolProperties))) {
                        checkAssignable(name, String.class, value);
                        setDelegateProperties(newProperties, value.toString());
                        resultList.add(new Attribute(name, value));
                    }
                } else if (equalsProperty(name, ProxoolConstants.DRIVER_URL)) {
                    checkAssignable(name, String.class, value);
                    if (notEmpty(value)) {
                        newProperties.setProperty(ProxoolConstants.DRIVER_URL_PROPERTY, value.toString());
                    } else {
                        newProperties.setProperty(ProxoolConstants.DRIVER_URL_PROPERTY, "");
                    }
                    resultList.add(new Attribute(name, value));
                } else if (equalsProperty(name, ProxoolConstants.FATAL_SQL_EXCEPTION)) {
                    if (!isEqualProperties(value.toString(),
                            this.poolProperties.getProperty(ProxoolConstants.FATAL_SQL_EXCEPTION_PROPERTY))) {
                        checkAssignable(name, String.class, value);
                        if (notEmpty(value)) {
                            newProperties.setProperty(ProxoolConstants.FATAL_SQL_EXCEPTION_PROPERTY, value.toString());
                        } else {
                            newProperties.setProperty(ProxoolConstants.FATAL_SQL_EXCEPTION_PROPERTY, "");
                        }
                        resultList.add(new Attribute(name, value));
                    }
                } else if (equalsProperty(name, ProxoolConstants.HOUSE_KEEPING_SLEEP_TIME)) {
                    setIntegerAttribute(name, ProxoolConstants.HOUSE_KEEPING_SLEEP_TIME_PROPERTY, value,
                            ConnectionPoolDefinitionIF.DEFAULT_HOUSE_KEEPING_SLEEP_TIME, newProperties, resultList);
                } else if (equalsProperty(name, ProxoolConstants.HOUSE_KEEPING_TEST_SQL)) {
                    checkAssignable(name, String.class, value);
                    if (notEmpty(value)) {
                        newProperties.setProperty(ProxoolConstants.HOUSE_KEEPING_TEST_SQL_PROPERTY, value.toString());
                    } else {
                        newProperties.setProperty(ProxoolConstants.HOUSE_KEEPING_TEST_SQL_PROPERTY, "");
                    }
                    resultList.add(new Attribute(name, value));
                } else if (equalsProperty(name, ProxoolConstants.TEST_BEFORE_USE)) {
                    checkAssignable(name, Boolean.class, value);
                    newProperties.setProperty(ProxoolConstants.TEST_BEFORE_USE_PROPERTY, value.toString());
                    resultList.add(new Attribute(name, value));
                } else if (equalsProperty(name, ProxoolConstants.TEST_AFTER_USE)) {
                    checkAssignable(name, Boolean.class, value);
                    newProperties.setProperty(ProxoolConstants.TEST_AFTER_USE_PROPERTY, value.toString());
                    resultList.add(new Attribute(name, value));
                } else if (equalsProperty(name, ProxoolConstants.MAXIMUM_ACTIVE_TIME)) {
                    setIntegerAttribute(name, ProxoolConstants.MAXIMUM_ACTIVE_TIME_PROPERTY, value,
                            ConnectionPoolDefinitionIF.DEFAULT_MAXIMUM_ACTIVE_TIME, newProperties, resultList);
                } else if (equalsProperty(name, ProxoolConstants.MAXIMUM_CONNECTION_COUNT)) {
                    setIntegerAttribute(name, ProxoolConstants.MAXIMUM_CONNECTION_COUNT_PROPERTY, value,
                            ConnectionPoolDefinitionIF.DEFAULT_MAXIMUM_CONNECTION_COUNT, newProperties, resultList);
                } else if (equalsProperty(name, ProxoolConstants.MAXIMUM_CONNECTION_LIFETIME)) {
                    setIntegerAttribute(name, ProxoolConstants.MAXIMUM_CONNECTION_LIFETIME_PROPERTY, value,
                            ConnectionPoolDefinitionIF.DEFAULT_MAXIMUM_CONNECTION_LIFETIME, newProperties, resultList);
                } else if (equalsProperty(name, ProxoolConstants.MAXIMUM_NEW_CONNECTIONS)) {
                    setIntegerAttribute(name, ProxoolConstants.MAXIMUM_NEW_CONNECTIONS_PROPERTY, value,
                            ConnectionPoolDefinitionIF.DEFAULT_MAXIMUM_NEW_CONNECTIONS, newProperties, resultList);
                } else if (equalsProperty(name, ProxoolConstants.SIMULTANEOUS_BUILD_THROTTLE)) {
                    setIntegerAttribute(name, ProxoolConstants.SIMULTANEOUS_BUILD_THROTTLE_PROPERTY, value,
                            ConnectionPoolDefinitionIF.DEFAULT_SIMULTANEOUS_BUILD_THROTTLE, newProperties, resultList);
                } else if (equalsProperty(name, ProxoolConstants.MINIMUM_CONNECTION_COUNT)) {
                    checkAssignable(name, Integer.class, value);
                    newProperties.setProperty(ProxoolConstants.MINIMUM_CONNECTION_COUNT_PROPERTY, value.toString());
                    resultList.add(new Attribute(name, value));
                } else if (equalsProperty(name, ProxoolConstants.OVERLOAD_WITHOUT_REFUSAL_LIFETIME)) {
                    setIntegerAttribute(name, ProxoolConstants.OVERLOAD_WITHOUT_REFUSAL_LIFETIME_PROPERTY, value,
                            ConnectionPoolDefinitionIF.DEFAULT_OVERLOAD_WITHOUT_REFUSAL_THRESHOLD, newProperties, resultList);
                } else if (equalsProperty(name, ProxoolConstants.PROTOTYPE_COUNT)) {
                    checkAssignable(name, Integer.class, value);
                    newProperties.setProperty(ProxoolConstants.PROTOTYPE_COUNT_PROPERTY, value.toString());
                    resultList.add(new Attribute(name, value));
                } else if (equalsProperty(name, ProxoolConstants.RECENTLY_STARTED_THRESHOLD)) {
                    setIntegerAttribute(name, ProxoolConstants.RECENTLY_STARTED_THRESHOLD_PROPERTY, value,
                            ConnectionPoolDefinitionIF.DEFAULT_RECENTLY_STARTED_THRESHOLD, newProperties, resultList);
                } else if (equalsProperty(name, ProxoolConstants.STATISTICS)) {
                    checkAssignable(name, String.class, value);
                    if (notEmpty(value)) {
                        newProperties.setProperty(ProxoolConstants.STATISTICS_PROPERTY, value.toString());
                    } else {
                        newProperties.setProperty(ProxoolConstants.STATISTICS_PROPERTY, "");
                    }
                    resultList.add(new Attribute(name, value));
                } else if (equalsProperty(name, ProxoolConstants.STATISTICS_LOG_LEVEL)) {
                    checkAssignable(name, String.class, value);
                    if (notEmpty(value)) {
                        newProperties.setProperty(ProxoolConstants.STATISTICS_LOG_LEVEL_PROPERTY, value.toString());
                    } else {
                        newProperties.setProperty(ProxoolConstants.STATISTICS_LOG_LEVEL_PROPERTY, "");
                    }
                    resultList.add(new Attribute(name, value));
                } else if (equalsProperty(name, ProxoolConstants.TRACE)) {
                    checkAssignable(name, Boolean.class, value);
                    newProperties.setProperty(ProxoolConstants.TRACE_PROPERTY, value.toString());
                    resultList.add(new Attribute(name, value));
                } else if (equalsProperty(name, ProxoolConstants.VERBOSE)) {
                    checkAssignable(name, Boolean.class, value);
                    newProperties.setProperty(ProxoolConstants.VERBOSE_PROPERTY, value.toString());
                    resultList.add(new Attribute(name, value));
                } else if (equalsProperty(name, ProxoolConstants.FATAL_SQL_EXCEPTION_WRAPPER_CLASS)) {
                    checkAssignable(name, Boolean.class, value);
                    newProperties.setProperty(ProxoolConstants.FATAL_SQL_EXCEPTION_WRAPPER_CLASS_PROPERTY, value.toString());
                    resultList.add(new Attribute(name, value));
                } else {
                    final String message = "Unknown attribute: " + name;
                    LOG.error(message);
                    throw new AttributeNotFoundException(message);
                }
            } catch (InvalidAttributeValueException e) {
                final String message = "Attribute value was illegal: " + e.getMessage();
                LOG.error(message);
                throw new RuntimeOperationsException(new RuntimeException(message));
            } catch (AttributeNotFoundException e) {
                throw new RuntimeOperationsException(new IllegalArgumentException(e.getMessage()));
            }
        }
        try {
            ProxoolFacade.updateConnectionPool(ProxoolConstants.PROPERTY_PREFIX + this.poolDefinition.getAlias(), newProperties);
        } catch (ProxoolException e) {
            LOG.error("Update of Proxool pool failed: ", e);
            throw new RuntimeOperationsException(new RuntimeException(e.getMessage()));
        }
        return resultList;
    }

    /**
     * @see javax.management.DynamicMBean#invoke(java.lang.String, java.lang.Object[], java.lang.String[])
     */
    public Object invoke(String operationName, Object params[], String signature[]) throws MBeanException, ReflectionException {
        if (operationName == null) {
            throw new RuntimeOperationsException(new IllegalArgumentException("Operation name cannot be null"), "Cannot invoke a null operation in " + CLASS_NAME);
        } else if (operationName.equals(OPERATION_NAME_SHUTDOWN)) {
            try {
                ProxoolFacade.removeConnectionPool(this.poolDefinition.getAlias());
            } catch (ProxoolException e) {
                LOG.error("Shutdown of pool " + this.poolDefinition.getAlias() + " failed.", e);
            }
            return null;
        } else {
            throw new ReflectionException(new NoSuchMethodException(operationName),
                    "Cannot find the operation " + operationName + ".");
        }
    }

    /**
     * @see javax.management.DynamicMBean#getMBeanInfo()
     */
    public MBeanInfo getMBeanInfo() {
        return mBeanInfo;
    }

    private MBeanInfo getDynamicMBeanInfo(String alias) {
        final MBeanAttributeInfo[] attributeInfos = new MBeanAttributeInfo[]{
            createProxoolAttribute(ProxoolConstants.ALIAS, String.class, false),
            createProxoolAttribute(ProxoolConstants.DRIVER_PROPERTIES, String.class),
            createProxoolAttribute(ProxoolConstants.DRIVER_URL, String.class),
            createProxoolAttribute(ProxoolConstants.FATAL_SQL_EXCEPTION, String.class),
            createProxoolAttribute(ProxoolConstants.HOUSE_KEEPING_SLEEP_TIME, Integer.class),
            createProxoolAttribute(ProxoolConstants.HOUSE_KEEPING_TEST_SQL, String.class),
            createProxoolAttribute(ProxoolConstants.TEST_BEFORE_USE, Boolean.class),
            createProxoolAttribute(ProxoolConstants.TEST_AFTER_USE, Boolean.class),
            createProxoolAttribute(ProxoolConstants.MAXIMUM_ACTIVE_TIME, Integer.class),
            createProxoolAttribute(ProxoolConstants.MAXIMUM_CONNECTION_COUNT, Integer.class),
            createProxoolAttribute(ProxoolConstants.MAXIMUM_CONNECTION_LIFETIME, Integer.class),
            createProxoolAttribute(ProxoolConstants.SIMULTANEOUS_BUILD_THROTTLE, Integer.class),
            createProxoolAttribute(ProxoolConstants.MINIMUM_CONNECTION_COUNT, Integer.class),
            createProxoolAttribute(ProxoolConstants.OVERLOAD_WITHOUT_REFUSAL_LIFETIME, Integer.class),
            createProxoolAttribute(ProxoolConstants.PROTOTYPE_COUNT, Integer.class),
            createProxoolAttribute(ProxoolConstants.RECENTLY_STARTED_THRESHOLD, Integer.class),
            createProxoolAttribute(ProxoolConstants.STATISTICS, String.class),
            createProxoolAttribute(ProxoolConstants.STATISTICS_LOG_LEVEL, String.class),
            createProxoolAttribute(ProxoolConstants.TRACE, Boolean.class),
            createProxoolAttribute(ProxoolConstants.VERBOSE, Boolean.class),
            createProxoolAttribute(ProxoolConstants.FATAL_SQL_EXCEPTION_WRAPPER_CLASS, String.class),
        };

        final MBeanConstructorInfo[] constructorInfos = new MBeanConstructorInfo[]{
            new MBeanConstructorInfo("ConnectionPoolMBean(): Construct a ConnectionPoolMBean object.", ConnectionPoolMBean.class.getConstructors()[0])
        };

        final MBeanOperationInfo[] operationInfos = new MBeanOperationInfo[]{
            new MBeanOperationInfo(OPERATION_NAME_SHUTDOWN, "Stop and dispose this connection pool.",
                    new MBeanParameterInfo[]{}, "void", MBeanOperationInfo.ACTION)
        };

        return new MBeanInfo(CLASS_NAME, MessageFormat.format(getJMXText(RECOURCE_NAME_MBEAN_POOL_DESCRIPTION),
                new Object[]{alias}), attributeInfos, constructorInfos, operationInfos, new MBeanNotificationInfo[0]);
    }

    private static String getAttributeDescription(String attributeName) {
        String description = "";
        if (ATTRIBUTE_DESCRIPTIONS_RESOURCE != null) {
            try {
                description = ATTRIBUTE_DESCRIPTIONS_RESOURCE.getString(attributeName);
            } catch (Exception e) {
                LOG.warn("Could not get description for attribute '" + attributeName + "' from resource " + ResourceNamesIF.ATTRIBUTE_DESCRIPTIONS + ".");
            }
        }
        return description;
    }

    private static String getJMXText(String key) {
        String value = "";
        if (JMX_RESOURCE != null) {
            try {
                value = JMX_RESOURCE.getString(key);
            } catch (Exception e) {
                LOG.warn("Could not get value for attribute '" + key + "' from resource " + ResourceNamesIF.JMX + ".");
            }
        }
        return value;
    }

    private static ResourceBundle createAttributeDescriptionsResource() {
        try {
            return ResourceBundle.getBundle(ResourceNamesIF.ATTRIBUTE_DESCRIPTIONS);
        } catch (Exception e) {
            LOG.error("Could not find resource " + ResourceNamesIF.ATTRIBUTE_DESCRIPTIONS, e);
        }
        return null;
    }

    private static ResourceBundle createJMXResource() {
        try {
            return ResourceBundle.getBundle(ResourceNamesIF.JMX);
        } catch (Exception e) {
            LOG.error("Could not find resource " + ResourceNamesIF.JMX, e);
        }
        return null;
    }

    private static MBeanAttributeInfo createProxoolAttribute(String attributeName, Class type) {
        return createProxoolAttribute(attributeName, type, true);
    }

    private static MBeanAttributeInfo createProxoolAttribute(String attributeName, Class type, boolean writable) {
        return new MBeanAttributeInfo(ProxoolJMXHelper.getValidIdentifier(attributeName), type.getName(),
                getAttributeDescription(attributeName), true, writable, false);
    }

    private void checkAssignable(String name, Class clazz, Object value) throws InvalidAttributeValueException {
        if (value == null) {
            if (!String.class.equals(clazz)) {
                throw(new InvalidAttributeValueException("Cannot set attribute " + name + " to null "
                        + " an instance of " + clazz.getName() + " expected."));
            }
        } else {
            Class valueClass = value.getClass();
            if (!clazz.isAssignableFrom(valueClass)) {
                throw(new InvalidAttributeValueException("Cannot set attribute " + name + " to a " + valueClass.getName()
                        + " instance, " + clazz.getName() + " expected."));
            }
        }
    }

    private boolean equalsProperty(String beanAttribute, String proxoolProperty) {
        return beanAttribute.equals(ProxoolJMXHelper.getValidIdentifier(proxoolProperty));
    }

    private void setDelegateProperties(Properties properties, String propertyString)
            throws InvalidAttributeValueException {
        if (propertyString == null || propertyString.trim().length() == 0) {
            return;
        }
        StringTokenizer tokenizer = new StringTokenizer(propertyString, ",");
        String keyValuePair = null;
        int equalsIndex = -1;
        while (tokenizer.hasMoreElements()) {
            keyValuePair = tokenizer.nextToken().trim();
            equalsIndex = keyValuePair.indexOf("=");
            if (equalsIndex != -1) {
                properties.put(keyValuePair.substring(0, equalsIndex).trim(),
                        keyValuePair.substring(equalsIndex + 1).trim());
            } else {
                throw new InvalidAttributeValueException("Could not find key/value delimiter '=' in property definition: '"
                        + keyValuePair + "'.");
            }
        }
    }

    private String getDelegatePropertiesAsString(Properties properties) {
        final StringBuffer result = new StringBuffer();
        Iterator keyIterator = properties.keySet().iterator();
        String key = null;
        boolean first = true;
        while (keyIterator.hasNext()) {
            key = (String) keyIterator.next();
            if (!key.startsWith(ProxoolConstants.PROPERTY_PREFIX)) {
                if (!first) {
                    result.append(", ");
                } else {
                    first = false;
                }
                result.append(key).append("=").append(properties.getProperty(key));
            }
        }
        return result.toString();
    }

    private boolean notEmpty(Object object) {
        return object != null && object.toString().trim().length() > 0;
    }

    private boolean notEmptyOrZero(Integer integer) {
        return integer != null && integer.intValue() > 0;
    }

    private String getValueOrEmpty(String property) {
        return property == null ? "" : property;
    }

    private void setIntegerAttribute(String attributeName, String propertyName, Object value, int defaultValue, Properties properties,
                                     AttributeList resultList) throws InvalidAttributeValueException {
        checkAssignable(attributeName, Integer.class, value);
        if (notEmptyOrZero((Integer) value)) {
            properties.setProperty(propertyName, value.toString());
            resultList.add(new Attribute(attributeName, value));
        } else {
            resultList.add(new Attribute(attributeName,
                    new Integer(defaultValue)));
        }
    }

    private boolean isEqualProperties(String property1, String property2) {
        if (property1 == null) {
            return property2 == null;
        } else if (property2 == null) {
            return property1 == null;
        } else {
            return property1.equals(property2);
        }
    }

    private static MBeanNotificationInfo[] getNotificationInfos() {
        return new MBeanNotificationInfo[]{
            new MBeanNotificationInfo(
                    new String[]{NOTIFICATION_TYPE_DEFINITION_UPDATED}, Notification.class.getName(), getJMXText(RECOURCE_NAME_MBEAN_NOTIFICATION_DESCRIPTION))
        };
    }

    // Listener methods

    /**
     * Not used.
     * @see org.logicalcobwebs.proxool.ProxoolListenerIF#onRegistration(org.logicalcobwebs.proxool.ConnectionPoolDefinitionIF, java.util.Properties)
     */
    public void onRegistration(ConnectionPoolDefinitionIF connectionPoolDefinition, Properties completeInfo) {
        // Not used.
    }

    /**
     * If the given alias equals this pools alias: Unregister this JMX bean.
     * @see org.logicalcobwebs.proxool.ProxoolListenerIF#onShutdown(java.lang.String)
     */
    public void onShutdown(String alias) {
        if (alias.equals(this.poolDefinition.getAlias())) {
            if (this.active) {
                this.active = false;
                ProxoolJMXHelper.unregisterPool(this.poolDefinition.getAlias(), this.poolProperties);
                LOG.info(this.poolDefinition.getAlias() + " MBean unregistered.");
            }
        }
    }

    /**
     * Update the attributes of this MBean and emit a {@link #NOTIFICATION_TYPE_DEFINITION_UPDATED} event.
     * @see org.logicalcobwebs.proxool.ConfigurationListenerIF#definitionUpdated(org.logicalcobwebs.proxool.ConnectionPoolDefinitionIF, java.util.Properties, java.util.Properties)
     */
    public void definitionUpdated(ConnectionPoolDefinitionIF connectionPoolDefinition, Properties completeInfo,
                                  Properties changedInfo) {
        this.poolDefinition = connectionPoolDefinition;
        this.poolProperties = completeInfo;
        this.notificationHelper.sendNotification(new Notification(NOTIFICATION_TYPE_DEFINITION_UPDATED, this,
                definitionUpdatedSequence++, System.currentTimeMillis(),
                getJMXText(RECOURCE_NAME_MBEAN_NOTIFICATION_DEF_UPDATED)));
    }

    /**
     * @see javax.management.NotificationBroadcaster#addNotificationListener(javax.management.NotificationListener, javax.management.NotificationFilter, java.lang.Object)
     */
    public void addNotificationListener(NotificationListener notificationListener, NotificationFilter notificationFilter,
                                        Object handBack) throws IllegalArgumentException {
        this.notificationHelper.addNotificationListener(notificationListener, notificationFilter, handBack);
    }

    /**
     * @see javax.management.NotificationBroadcaster#removeNotificationListener(javax.management.NotificationListener)
     */
    public void removeNotificationListener(NotificationListener notificationListener) throws ListenerNotFoundException {
        this.notificationHelper.removeNotificationListener(notificationListener);
    }

    /**
     * @see javax.management.NotificationBroadcaster#getNotificationInfo()
     */
    public MBeanNotificationInfo[] getNotificationInfo() {
        return NOTIFICATION_INFOS;
    }

    /**
     * @see javax.management.MBeanRegistration#preRegister(javax.management.MBeanServer, javax.management.ObjectName)
     */
    public ObjectName preRegister(MBeanServer mBeanServer, ObjectName objectName) throws Exception {
        if (objectName == null) {
            throw new ProxoolException("objectName was null, but we can not construct an MBean instance without knowing"
                    + " the pool alias.");
        }
        return objectName;
    }

    /**
     * @see javax.management.MBeanRegistration#postRegister(java.lang.Boolean)
     */
    public void postRegister(Boolean success) {
        if (success.booleanValue() == true) {
            this.active = true;
        }
    }

    /**
     * @see javax.management.MBeanRegistration#preDeregister()
     */
    public void preDeregister() throws Exception {
        this.active = false;
    }

    /**
     * @see javax.management.MBeanRegistration#postDeregister()
     */
    public void postDeregister() {
    }
}

/*
 Revision history:
 $Log: ConnectionPoolMBean.java,v $
 Revision 1.15  2006/01/18 14:39:55  billhorsman
 Unbundled Jakarta's Commons Logging.

 Revision 1.14  2003/10/20 07:37:07  chr32
 Bettered handling of empty values. Now not setting attributes that has not changed.

 Revision 1.13  2003/09/30 18:38:27  billhorsman
 New properties

 Revision 1.12  2003/09/29 17:48:08  billhorsman
 New fatal-sql-exception-wrapper-class allows you to define what exception is used as a wrapper. This means that you
 can make it a RuntimeException if you need to.

 Revision 1.11  2003/09/14 21:29:31  chr32
 Added support for wrap-fatal-sql-exceptions,statistics and statistics-log-level properties.

 Revision 1.10  2003/09/10 22:21:04  chr32
 Removing > jdk 1.2 dependencies.

 Revision 1.9  2003/05/06 23:15:55  chr32
 Moving JMX classes back in from sandbox.

 Revision 1.1  2003/03/07 16:35:17  billhorsman
 moved jmx stuff into sandbox until it is tested

 Revision 1.7  2003/03/05 23:28:56  billhorsman
 deprecated maximum-new-connections property in favour of
 more descriptive simultaneous-build-throttle

 Revision 1.6  2003/03/03 11:11:59  billhorsman
 fixed licence

 Revision 1.5  2003/02/26 19:04:30  chr32
 Added active/inactive state check.

 Revision 1.4  2003/02/26 16:37:48  billhorsman
 fixed spelling in ConfigurationListenerIF

 Revision 1.3  2003/02/25 16:50:31  chr32
 Added JMX notification and doc.

 Revision 1.2  2003/02/24 18:01:57  chr32
 1st working version.

 Revision 1.1  2003/02/24 01:14:17  chr32
 Init rev (unfinished).

*/