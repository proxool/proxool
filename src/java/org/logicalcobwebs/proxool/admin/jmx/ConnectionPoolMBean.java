/*
 * This software is released under the Apache Software Licence. See
 * package.html for details. The latest version is available at
 * http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool.admin.jmx;

import org.logicalcobwebs.proxool.resources.ResourceNamesIF;
import org.logicalcobwebs.proxool.ProxoolConstants;
import org.logicalcobwebs.proxool.ConnectionPoolDefinitionIF;
import org.logicalcobwebs.proxool.ProxoolFacade;
import org.logicalcobwebs.proxool.ProxoolException;
import org.logicalcobwebs.proxool.ProxoolListenerIF;
import org.logicalcobwebs.proxool.ConfigurationListenerIF;
import org.logicalcobwebs.logging.Log;
import org.logicalcobwebs.logging.LogFactory;

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

import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.Properties;
import java.util.StringTokenizer;
import java.text.MessageFormat;

/**
 * JMX DynamicMBean adapter for a Proxool connection pool.<br>
 * Supports the following attributes:
 *<code><pre>   alias
 * driver-url
 * driver-class
 * driver-properties
 * house-keeping-sleep-time
 * house-keeping-test-sql
 * maximum-connection-count
 * maximum-connection-lifetime
 * maximum-new-connections
 * minimum-connection-count
 * recently-started-threshold
 * overload-without-refusal-lifetime
 * maximum-active-time
 * verbose
 * trace
 * fatal-sql-exception
 * prototype-count
 * </pre>
 * </code>
 * ...and the following operation:
 * <code><pre>   shutdown
 * </pre></code>
 * @version $Revision: 1.2 $, $Date: 2003/02/24 18:01:57 $
 * @author Christian Nedregaard (christian_nedregaard@email.com)
 * @author $Author: chr32 $ (current maintainer)
 * @since Proxool 0.7
 */
public class ConnectionPoolMBean implements DynamicMBean, ProxoolListenerIF, ConfigurationListenerIF {
    private static final Log LOG = LogFactory.getLog (ConnectionPoolMBean.class);
    private static final String CLASS_NAME = ConnectionPoolMBean.class.getName ();
    private static final ResourceBundle ATTRIBUTE_DESCRIPTIONS_RESOURCE = createAttributeDescriptionsResource ();
    private static final ResourceBundle JMX_RESOURCE = createJMXResource ();

    private MBeanInfo mBeanInfo;
    private ConnectionPoolDefinitionIF poolDefinition;
    private Properties poolProperties;


    public ConnectionPoolMBean (String alias, Properties poolProperties)
        throws ProxoolException {
        this.poolDefinition = ProxoolFacade
            .getConnectionPoolDefinition(alias);
        this.poolProperties = poolProperties;
        buildDynamicMBeanInfo (this.poolDefinition.getAlias ());
        ProxoolFacade.addProxoolListener(this);
        ProxoolFacade.addConfigurationListener(alias, this);
    }

    /**
     * @see DynamicMBean#getAttribute(String)
     */
    public Object getAttribute (String attributeName) throws AttributeNotFoundException, MBeanException, ReflectionException {
        if (attributeName == null) {
            final String message = "Cannot invoke a getter of " + CLASS_NAME + " with null attribute name";
            LOG.error(message);
            throw new RuntimeOperationsException (new IllegalArgumentException ("Attribute name cannot be null"),
                message);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Getting attribute " + attributeName + ".");
        }
        return ((Attribute) getAttributes (new String[]{attributeName}).get (0)).getValue();
    }

    /**
     * @see DynamicMBean#setAttribute(Attribute)
     */
    public void setAttribute (Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException,
        MBeanException, ReflectionException {
        if (attribute == null) {
            final String message = "Cannot invoke a setter of " + CLASS_NAME + " with null attribute";
            LOG.error(message);
            throw new RuntimeOperationsException (new IllegalArgumentException ("Attribute cannot be null"),
                message);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Setting attribute " + attribute.getName() + ".");
        }
        final AttributeList attributeList = new AttributeList ();
        attributeList.add (attribute);
        setAttributes (attributeList);
    }

    /**
     * @see DynamicMBean#getAttributes(String[])
     */
    public AttributeList getAttributes (String[] attributeNames) {
        if (attributeNames == null) {
            final String message = "Cannot invoke a null getter of " + CLASS_NAME;
            LOG.error(message);
            throw new RuntimeOperationsException (new IllegalArgumentException ("attributeNames[] cannot be null"),
                message);
        }
        AttributeList resultList = new AttributeList ();

        // if attributeNames is empty, return an empty result list
        if (attributeNames.length == 0) {
            return resultList;
        }

        // build the result attribute list
        for (int i = 0; i < attributeNames.length; i++) {
            try {
                if (equalsProperty(attributeNames[i], ProxoolConstants.ALIAS)) {
                    resultList.add (new Attribute (attributeNames[i],
                        this.poolDefinition.getAlias()));
                } else if (equalsProperty(attributeNames[i], ProxoolConstants.DRIVER_PROPERTIES)) {
                    resultList.add (new Attribute (attributeNames[i],
                        getDelegatePropertiesAsString(this.poolProperties)));
                } else if (equalsProperty(attributeNames[i], ProxoolConstants.DRIVER_URL)) {
                    resultList.add (new Attribute (attributeNames[i],
                        this.poolDefinition.getUrl ()));
                } else if (equalsProperty(attributeNames[i], ProxoolConstants.FATAL_SQL_EXCEPTION)) {
                    resultList.add (new Attribute (attributeNames[i],
                        this.poolProperties.getProperty(ProxoolConstants.FATAL_SQL_EXCEPTION_PROPERTY)));
                } else if (equalsProperty(attributeNames[i], ProxoolConstants.HOUSE_KEEPING_SLEEP_TIME)) {
                    resultList.add (new Attribute (attributeNames[i],
                        new Integer (this.poolDefinition.getHouseKeepingSleepTime ())));
                } else if (equalsProperty(attributeNames[i], ProxoolConstants.HOUSE_KEEPING_TEST_SQL)) {
                    resultList.add (new Attribute (attributeNames[i],
                        this.poolDefinition.getHouseKeepingTestSql ()));
                } else if (equalsProperty(attributeNames[i], ProxoolConstants.MAXIMUM_ACTIVE_TIME)) {
                    resultList.add (new Attribute (attributeNames[i],
                        new Integer (this.poolDefinition.getMaximumActiveTime ())));
                } else if (equalsProperty(attributeNames[i], ProxoolConstants.MAXIMUM_CONNECTION_COUNT)) {
                    resultList.add (new Attribute (attributeNames[i],
                        new Integer (this.poolDefinition.getMaximumConnectionCount ())));
                } else if (equalsProperty(attributeNames[i], ProxoolConstants.MAXIMUM_CONNECTION_LIFETIME)) {
                    resultList.add (new Attribute (attributeNames[i],
                        new Integer (this.poolDefinition.getMaximumConnectionLifetime ())));
                } else if (equalsProperty(attributeNames[i], ProxoolConstants.MAXIMUM_NEW_CONNECTIONS)) {
                    resultList.add (new Attribute (attributeNames[i],
                        new Integer (this.poolDefinition.getMaximumNewConnections ())));
                } else if (equalsProperty(attributeNames[i], ProxoolConstants.MINIMUM_CONNECTION_COUNT)) {
                    resultList.add (new Attribute (attributeNames[i],
                        new Integer (this.poolDefinition.getMinimumConnectionCount ())));
                } else if (equalsProperty(attributeNames[i], ProxoolConstants.OVERLOAD_WITHOUT_REFUSAL_LIFETIME)) {
                    resultList.add (new Attribute (attributeNames[i],
                        new Integer (this.poolDefinition.getOverloadWithoutRefusalLifetime ())));
                } else if (equalsProperty(attributeNames[i], ProxoolConstants.PROTOTYPE_COUNT)) {
                    resultList.add (new Attribute (attributeNames[i],
                        new Integer (this.poolDefinition.getPrototypeCount ())));
                } else if (equalsProperty(attributeNames[i], ProxoolConstants.RECENTLY_STARTED_THRESHOLD)) {
                    resultList.add (new Attribute (attributeNames[i],
                        new Integer (this.poolDefinition.getRecentlyStartedThreshold ())));
                } else if (equalsProperty(attributeNames[i], ProxoolConstants.TRACE)) {
                    resultList.add (new Attribute (attributeNames[i],
                        new Boolean (this.poolDefinition.isTrace ())));
                } else if (equalsProperty(attributeNames[i], ProxoolConstants.VERBOSE)) {
                    resultList.add (new Attribute (attributeNames[i],
                        new Boolean (this.poolDefinition.isVerbose ())));
                } else {
                    final String message = "Unknown attribute: " + attributeNames[i];
                    LOG.error(message);
                    throw new AttributeNotFoundException ();
                }
            } catch (AttributeNotFoundException e) {
                throw new RuntimeOperationsException (new IllegalArgumentException (e.getMessage ()));
            }
        }
        return resultList;
    }

    /**
     * @see DynamicMBean#setAttributes(AttributeList)
     */
    public AttributeList setAttributes (AttributeList attributes) {

        if (attributes == null) {
            final String message = "AttributeList attributes cannot be null";
            LOG.error(message);
            throw new RuntimeOperationsException (new IllegalArgumentException (message),
                "Cannot invoke a setter of " + CLASS_NAME);
        }
        AttributeList resultList = new AttributeList ();

        if (attributes.isEmpty ()) {
            return resultList;
        }

        String name = null;
        Object value = null;
        final Properties newProperties = new Properties();
        for (Iterator i = attributes.iterator (); i.hasNext ();) {
            Attribute attribute = (Attribute) i.next ();
            try {
                name = attribute.getName ();
                value = attribute.getValue ();

                if (equalsProperty(name, ProxoolConstants.DRIVER_PROPERTIES)) {
                    checkAssignable (name, String.class, value.getClass ());
                    setDelegateProperties(newProperties, value.toString());
                    resultList.add (new Attribute (name, value));
                } else if (equalsProperty(name, ProxoolConstants.DRIVER_URL)) {
                    checkAssignable (name, String.class, value.getClass ());
                    if (notEmpty(value.toString())) {
                        newProperties.setProperty(ProxoolConstants.DRIVER_URL_PROPERTY, value.toString());
                    }
                    resultList.add (new Attribute (name, value));
                } else if (equalsProperty(name, ProxoolConstants.FATAL_SQL_EXCEPTION)) {
                    checkAssignable (name, String.class, value.getClass ());
                    if (notEmpty(value.toString())) {
                        newProperties.setProperty(ProxoolConstants.FATAL_SQL_EXCEPTION_PROPERTY, value.toString());
                    }
                    resultList.add (new Attribute (name, value));
                } else if (equalsProperty(name, ProxoolConstants.HOUSE_KEEPING_SLEEP_TIME)) {
                    setIntegerAttribute(name, ProxoolConstants.HOUSE_KEEPING_SLEEP_TIME_PROPERTY, value,
                            ConnectionPoolDefinitionIF.DEFAULT_HOUSE_KEEPING_SLEEP_TIME, newProperties, resultList);
                } else if (equalsProperty(name, ProxoolConstants.HOUSE_KEEPING_TEST_SQL)) {
                    checkAssignable (name, String.class, value.getClass ());
                    if (notEmpty(value.toString())) {
                        newProperties.setProperty(ProxoolConstants.HOUSE_KEEPING_TEST_SQL_PROPERTY, value.toString());
                    }
                    resultList.add (new Attribute (name, value));
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
                } else if (equalsProperty(name, ProxoolConstants.MINIMUM_CONNECTION_COUNT)) {
                    checkAssignable (name, Integer.class, value.getClass ());
                    newProperties.setProperty(ProxoolConstants.MINIMUM_CONNECTION_COUNT_PROPERTY, value.toString());
                    resultList.add (new Attribute (name, value));
                } else if (equalsProperty(name, ProxoolConstants.OVERLOAD_WITHOUT_REFUSAL_LIFETIME)) {
                    setIntegerAttribute(name, ProxoolConstants.OVERLOAD_WITHOUT_REFUSAL_LIFETIME_PROPERTY, value,
                            ConnectionPoolDefinitionIF.DEFAULT_OVERLOAD_WITHOUT_REFUSAL_THRESHOLD, newProperties, resultList);
                } else if (equalsProperty(name, ProxoolConstants.PROTOTYPE_COUNT)) {
                    checkAssignable (name, Integer.class, value.getClass ());
                    newProperties.setProperty(ProxoolConstants.PROTOTYPE_COUNT_PROPERTY, value.toString());
                    resultList.add (new Attribute (name, value));
                } else if (equalsProperty(name, ProxoolConstants.RECENTLY_STARTED_THRESHOLD)) {
                    setIntegerAttribute(name, ProxoolConstants.RECENTLY_STARTED_THRESHOLD_PROPERTY, value,
                            ConnectionPoolDefinitionIF.DEFAULT_RECENTLY_STARTED_THRESHOLD, newProperties, resultList);
                } else if (equalsProperty(name, ProxoolConstants.TRACE)) {
                    checkAssignable (name, Boolean.class, value.getClass ());
                    newProperties.setProperty(ProxoolConstants.TRACE_PROPERTY, value.toString());
                    resultList.add (new Attribute (name, value));
                } else if (equalsProperty(name, ProxoolConstants.VERBOSE)) {
                    checkAssignable (name, Boolean.class, value.getClass ());
                    newProperties.setProperty(ProxoolConstants.VERBOSE_PROPERTY, value.toString());
                    resultList.add (new Attribute (name, value));
                } else {
                    final String message = "Unknown attribute: " + name;
                    LOG.error(message);
                    throw new AttributeNotFoundException (message);
                }
            } catch (InvalidAttributeValueException e) {
                final String message = "Attribute value was illegal: " + e.getMessage ();
                LOG.error(message);
                throw new RuntimeOperationsException (new RuntimeException (message));
            } catch (AttributeNotFoundException e) {
                throw new RuntimeOperationsException (new IllegalArgumentException (e.getMessage ()));
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
     * @see DynamicMBean#invoke(String, Object[], String[])
     */
    public Object invoke (String operationName, Object params[], String signature[]) throws MBeanException, ReflectionException {
        if (operationName == null) {
            throw new RuntimeOperationsException (new IllegalArgumentException ("Operation name cannot be null"), "Cannot invoke a null operation in " + CLASS_NAME);
        } else if (operationName.equals("shutdown")) {
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
     * @see DynamicMBean#getMBeanInfo()
     */
    public MBeanInfo getMBeanInfo () {
        return mBeanInfo;
    }

    private void buildDynamicMBeanInfo (String alias) {
        final MBeanAttributeInfo[] attributeInfos = new MBeanAttributeInfo[]{
            createProxoolAttribute (ProxoolConstants.ALIAS, String.class, false),
            createProxoolAttribute (ProxoolConstants.DRIVER_PROPERTIES, String.class),
            createProxoolAttribute (ProxoolConstants.DRIVER_URL, String.class),
            createProxoolAttribute (ProxoolConstants.FATAL_SQL_EXCEPTION, String.class),
            createProxoolAttribute (ProxoolConstants.HOUSE_KEEPING_SLEEP_TIME, Integer.class),
            createProxoolAttribute (ProxoolConstants.HOUSE_KEEPING_TEST_SQL, String.class),
            createProxoolAttribute (ProxoolConstants.MAXIMUM_ACTIVE_TIME, Integer.class),
            createProxoolAttribute (ProxoolConstants.MAXIMUM_CONNECTION_COUNT, Integer.class),
            createProxoolAttribute (ProxoolConstants.MAXIMUM_CONNECTION_LIFETIME, Integer.class),
            createProxoolAttribute (ProxoolConstants.MAXIMUM_NEW_CONNECTIONS, Integer.class),
            createProxoolAttribute (ProxoolConstants.MINIMUM_CONNECTION_COUNT, Integer.class),
            createProxoolAttribute (ProxoolConstants.OVERLOAD_WITHOUT_REFUSAL_LIFETIME, Integer.class),
            createProxoolAttribute (ProxoolConstants.PROTOTYPE_COUNT, Integer.class),
            createProxoolAttribute (ProxoolConstants.RECENTLY_STARTED_THRESHOLD, Integer.class),
            createProxoolAttribute (ProxoolConstants.TRACE, Boolean.class),
            createProxoolAttribute (ProxoolConstants.VERBOSE, Boolean.class)
        };

        final MBeanConstructorInfo[] constructorInfos = new MBeanConstructorInfo[]{
            new MBeanConstructorInfo ("ConnectionPoolMBean(): Construct a ConnectionPoolMBean object.", ConnectionPoolMBean.class.getConstructors ()[0])
        };

        final MBeanOperationInfo[] operationInfos = new MBeanOperationInfo[]{
            new MBeanOperationInfo ("shutdown", "Stop and dispose this connection pool.",
                new MBeanParameterInfo[]{}, "void", MBeanOperationInfo.ACTION)
        };

        mBeanInfo = new MBeanInfo (CLASS_NAME, MessageFormat.format (getJMXText ("mbean.pool"),
            new Object[]{alias}), attributeInfos, constructorInfos, operationInfos, new MBeanNotificationInfo[0]);
    }

    private static String getAttributeDescription (String attributeName) {
        String description = "";
        if (ATTRIBUTE_DESCRIPTIONS_RESOURCE != null) {
            try {
                description = ATTRIBUTE_DESCRIPTIONS_RESOURCE.getString (attributeName);
            } catch (Exception e) {
                LOG.warn ("Could not get description for attribute '" + attributeName + "' from resource " + ResourceNamesIF.ATTRIBUTE_DESCRIPTIONS + ".");
            }
        }
        return description;
    }

    private static String getJMXText (String key) {
        String value = "";
        if (JMX_RESOURCE != null) {
            try {
                value = JMX_RESOURCE.getString (key);
            } catch (Exception e) {
                LOG.warn ("Could not get value for attribute '" + key + "' from resource " + ResourceNamesIF.JMX + ".");
            }
        }
        return value;
    }

    private static ResourceBundle createAttributeDescriptionsResource () {
        try {
            return ResourceBundle.getBundle (ResourceNamesIF.ATTRIBUTE_DESCRIPTIONS);
        } catch (Exception e) {
            LOG.error ("Could not find resource " + ResourceNamesIF.ATTRIBUTE_DESCRIPTIONS, e);
        }
        return null;
    }

    private static ResourceBundle createJMXResource () {
        try {
            return ResourceBundle.getBundle (ResourceNamesIF.JMX);
        } catch (Exception e) {
            LOG.error ("Could not find resource " + ResourceNamesIF.JMX, e);
        }
        return null;
    }

    private static MBeanAttributeInfo createProxoolAttribute (String attributeName, Class type) {
        return createProxoolAttribute(attributeName, type, true);
    }

    private static MBeanAttributeInfo createProxoolAttribute (String attributeName, Class type, boolean writable) {
        return new MBeanAttributeInfo (getValidIdentifier(attributeName), type.getName (),
            getAttributeDescription (attributeName), true, writable, false);
    }

    private void checkAssignable (String name, Class class1, Class class2) throws InvalidAttributeValueException {
        if (!class1.isAssignableFrom (class2)) {
            throw(new InvalidAttributeValueException ("Cannot set attribute " + name + " to a " + class2.getName ()
                + " object, " + class1.getName () + " expected."));
        }
    }

    private static String getValidIdentifier(String propertyName) {
        if (propertyName.indexOf("-") == -1) {
            return propertyName;
        } else {
            StringBuffer buffer = new StringBuffer (propertyName);
            int index = -1;
            while ((index = buffer.indexOf("-")) > -1) {
                buffer.deleteCharAt(index);
                buffer.setCharAt(index, Character.toUpperCase(buffer.charAt(index)));
            }
            return buffer.toString();
        }
    }

    private boolean equalsProperty(String beanAttribute, String proxoolProperty) {
        return beanAttribute.equals(getValidIdentifier(proxoolProperty));
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
                    +  keyValuePair + "'.");
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

    private boolean notEmpty(String string) {
        return string != null && string.trim().length() > 0;
    }

    private boolean notEmptyOrZero(Integer integer) {
        return integer != null && integer.intValue() > 0;
    }

    private void setIntegerAttribute(String attributeName, String propertyName, Object value, int defaultValue, Properties properties,
        AttributeList resultList) throws InvalidAttributeValueException {
        checkAssignable (attributeName, Integer.class, value.getClass ());
        if (notEmptyOrZero((Integer) value)) {
            properties.setProperty(propertyName, value.toString());
            resultList.add (new Attribute (attributeName, value));
        } else {
            resultList.add (new Attribute (attributeName,
                new Integer(defaultValue)));
        }
    }

    // Listener methods

    /**
     * Not used.
     * @see ProxoolListenerIF#onRegistration(ConnectionPoolDefinitionIF, Properties)
     */
    public void onRegistration(ConnectionPoolDefinitionIF connectionPoolDefinition, Properties completeInfo) {
        // Not used.
    }

    /**
     * If the given alias equals this pools alias: Unregister this JMX bean.
     * @see ProxoolListenerIF#onShutdown(String)
     */
    public void onShutdown(String alias) {
        if (alias.equals(this.poolDefinition.getAlias())) {
            ProxoolJMXHelper.unregisterPool(this.poolDefinition.getAlias(), this.poolProperties);
            LOG.info(this.poolDefinition.getAlias() + " MBean unregistered.");
        }
    }

    /**
     * Update the attributes of this MBean.
s     * @see ConfigurationListenerIF#defintionUpdated(ConnectionPoolDefinitionIF, Properties, Properties)
     */
    public void defintionUpdated(ConnectionPoolDefinitionIF connectionPoolDefinition, Properties completeInfo,
        Properties changedInfo) {
        this.poolDefinition = connectionPoolDefinition;
        this.poolProperties = completeInfo;
    }
}

/*
 Revision history:
 $Log: ConnectionPoolMBean.java,v $
 Revision 1.2  2003/02/24 18:01:57  chr32
 1st working version.

 Revision 1.1  2003/02/24 01:14:17  chr32
 Init rev (unfinished).

*/