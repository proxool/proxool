/*
 * This software is released under the Apache Software Licence. See
 * package.html for details. The latest version is available at
 * http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool.admin.jmx;

import org.logicalcobwebs.proxool.resources.ResourceNamesIF;
import org.logicalcobwebs.proxool.ProxoolConstants;
import org.logicalcobwebs.proxool.ConnectionPoolDefinitionIF;
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
import java.text.MessageFormat;

/**
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
 * @version $Revision: 1.1 $, $Date: 2003/02/24 01:14:17 $
 * @author Christian Nedregaard (christian_nedregaard@email.com)
 * @author $Author: chr32 $ (current maintainer)
 * @since Proxool 0.7
 */
public class ConnectionPoolMBean implements DynamicMBean {
    private static final Log LOG = LogFactory.getLog (ConnectionPoolMBean.class);
    private static final String CLASS_NAME = ConnectionPoolMBean.class.getName ();
    private static final ResourceBundle ATTRIBUTE_DESCRIPTIONS_RESOURCE = createAttributeDescriptionsResource ();
    private static final ResourceBundle JMX_RESOURCE = createJMXResource ();

    private MBeanInfo mBeanInfo;
    private ConnectionPoolDefinitionIF connectionPoolDefinition;
    private Properties poolProperties;


    public ConnectionPoolMBean (ConnectionPoolDefinitionIF connectionPoolDefinition, Properties poolProperties) {
        this.connectionPoolDefinition = connectionPoolDefinition;
        this.poolProperties = poolProperties;
        buildDynamicMBeanInfo (connectionPoolDefinition.getAlias ());
    }

    /**
     * @see DynamicMBean#getAttribute(String)
     */
    public Object getAttribute (String attributeName) throws AttributeNotFoundException, MBeanException, ReflectionException {
        if (attributeName == null) {
            throw new RuntimeOperationsException (new IllegalArgumentException ("Attribute name cannot be null"),
                "Cannot invoke a getter of " + CLASS_NAME + " with null attribute name");
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Getting attribute " + attributeName + ".");
        }
        return ((Attribute) getAttributes (new String[]{attributeName}).get (0)).getValue();
    }

    /**
     * @see DynamicMBean#setAttribute(Attribute)
     */
    public void setAttribute (Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
        if (attribute == null) {
            throw new RuntimeOperationsException (new IllegalArgumentException ("Attribute cannot be null"), "Cannot invoke a setter of " + CLASS_NAME + " with null attribute");
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
            throw new RuntimeOperationsException (new IllegalArgumentException ("attributeNames[] cannot be null"), "Cannot invoke a getter of " + CLASS_NAME);
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
                    resultList.add (new Attribute (attributeNames[i], this.connectionPoolDefinition.getAlias ()));
                } else if (equalsProperty(attributeNames[i], ProxoolConstants.DRIVER_PROPERTIES)) {
                    resultList.add (new Attribute (attributeNames[i], "TODO"));
                } else if (equalsProperty(attributeNames[i], ProxoolConstants.DRIVER_URL)) {
                    resultList.add (new Attribute (attributeNames[i], this.connectionPoolDefinition.getUrl ()));
                } else if (equalsProperty(attributeNames[i], ProxoolConstants.FATAL_SQL_EXCEPTION)) {
                    resultList.add (new Attribute (attributeNames[i], this.connectionPoolDefinition.getFatalSqlExceptions ()));
                } else if (equalsProperty(attributeNames[i], ProxoolConstants.HOUSE_KEEPING_SLEEP_TIME)) {
                    resultList.add (new Attribute (attributeNames[i], new Integer (this.connectionPoolDefinition.getHouseKeepingSleepTime ())));
                } else if (equalsProperty(attributeNames[i], ProxoolConstants.HOUSE_KEEPING_TEST_SQL)) {
                    resultList.add (new Attribute (attributeNames[i], this.connectionPoolDefinition.getHouseKeepingTestSql ()));
                } else if (equalsProperty(attributeNames[i], ProxoolConstants.MAXIMUM_ACTIVE_TIME)) {
                    resultList.add (new Attribute (attributeNames[i], new Integer (this.connectionPoolDefinition.getMaximumActiveTime ())));
                } else if (equalsProperty(attributeNames[i], ProxoolConstants.MAXIMUM_CONNECTION_COUNT)) {
                    resultList.add (new Attribute (attributeNames[i], new Integer (this.connectionPoolDefinition.getMaximumConnectionCount ())));
                } else if (equalsProperty(attributeNames[i], ProxoolConstants.MAXIMUM_CONNECTION_LIFETIME)) {
                    resultList.add (new Attribute (attributeNames[i], new Integer (this.connectionPoolDefinition.getMaximumConnectionLifetime ())));
                } else if (equalsProperty(attributeNames[i], ProxoolConstants.MAXIMUM_NEW_CONNECTIONS)) {
                    resultList.add (new Attribute (attributeNames[i], new Integer (this.connectionPoolDefinition.getMaximumNewConnections ())));
                } else if (equalsProperty(attributeNames[i], ProxoolConstants.MINIMUM_CONNECTION_COUNT)) {
                    resultList.add (new Attribute (attributeNames[i], new Integer (this.connectionPoolDefinition.getMinimumConnectionCount ())));
                } else if (equalsProperty(attributeNames[i], ProxoolConstants.OVERLOAD_WITHOUT_REFUSAL_LIFETIME)) {
                    resultList.add (new Attribute (attributeNames[i], new Integer (this.connectionPoolDefinition.getOverloadWithoutRefusalLifetime ())));
                } else if (equalsProperty(attributeNames[i], ProxoolConstants.PROTOTYPE_COUNT)) {
                    resultList.add (new Attribute (attributeNames[i], new Integer (this.connectionPoolDefinition.getPrototypeCount ())));
                } else if (equalsProperty(attributeNames[i], ProxoolConstants.RECENTLY_STARTED_THRESHOLD)) {
                    resultList.add (new Attribute (attributeNames[i], new Integer (this.connectionPoolDefinition.getRecentlyStartedThreshold ())));
                } else if (equalsProperty(attributeNames[i], ProxoolConstants.TRACE)) {
                    resultList.add (new Attribute (attributeNames[i], new Boolean (this.connectionPoolDefinition.isTrace ())));
                } else if (equalsProperty(attributeNames[i], ProxoolConstants.VERBOSE)) {
                    resultList.add (new Attribute (attributeNames[i], new Boolean (this.connectionPoolDefinition.isVerbose ())));
                } else {
                    throw new AttributeNotFoundException ("Unknown attribute: " + attributeNames[i]);
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
            throw new RuntimeOperationsException (new IllegalArgumentException ("AttributeList attributes cannot be null"), "Cannot invoke a setter of " + CLASS_NAME);
        }
        AttributeList resultList = new AttributeList ();

        if (attributes.isEmpty ()) {
            return resultList;
        }

        String name = null;
        Object value = null;
        for (Iterator i = attributes.iterator (); i.hasNext ();) {
            Attribute attr = (Attribute) i.next ();
            try {
                name = attr.getName ();
                value = attr.getValue ();

                if (equalsProperty(name, ProxoolConstants.ALIAS)) {
                    checkAssignable (name, String.class, value.getClass ());
                    resultList.add (new Attribute (name, this.connectionPoolDefinition.getAlias ()));
                } else if (equalsProperty(name, ProxoolConstants.DRIVER_PROPERTIES)) {
                    checkAssignable (name, String.class, value.getClass ());
                    resultList.add (new Attribute (name, "TODO"));
                } else if (equalsProperty(name, ProxoolConstants.DRIVER_URL)) {
                    checkAssignable (name, String.class, value.getClass ());
                    resultList.add (new Attribute (name, this.connectionPoolDefinition.getUrl ()));
                } else if (equalsProperty(name, ProxoolConstants.FATAL_SQL_EXCEPTION)) {
                    checkAssignable (name, String.class, value.getClass ());
                    resultList.add (new Attribute (name, this.connectionPoolDefinition.getFatalSqlExceptions ()));
                } else if (equalsProperty(name, ProxoolConstants.HOUSE_KEEPING_SLEEP_TIME)) {
                    checkAssignable (name, Integer.class, value.getClass ());
                    resultList.add (new Attribute (name, new Integer (this.connectionPoolDefinition.getHouseKeepingSleepTime ())));
                } else if (equalsProperty(name, ProxoolConstants.HOUSE_KEEPING_TEST_SQL)) {
                    checkAssignable (name, String.class, value.getClass ());
                    resultList.add (new Attribute (name, this.connectionPoolDefinition.getHouseKeepingTestSql ()));
                } else if (equalsProperty(name, ProxoolConstants.MAXIMUM_ACTIVE_TIME)) {
                    checkAssignable (name, Integer.class, value.getClass ());
                    resultList.add (new Attribute (name, new Integer (this.connectionPoolDefinition.getMaximumActiveTime ())));
                } else if (equalsProperty(name, ProxoolConstants.MAXIMUM_CONNECTION_COUNT)) {
                    checkAssignable (name, Integer.class, value.getClass ());
                    resultList.add (new Attribute (name, new Integer (this.connectionPoolDefinition.getMaximumConnectionCount ())));
                } else if (equalsProperty(name, ProxoolConstants.MAXIMUM_CONNECTION_LIFETIME)) {
                    checkAssignable (name, Integer.class, value.getClass ());
                    resultList.add (new Attribute (name, new Integer (this.connectionPoolDefinition.getMaximumConnectionLifetime ())));
                } else if (equalsProperty(name, ProxoolConstants.MAXIMUM_NEW_CONNECTIONS)) {
                    checkAssignable (name, Integer.class, value.getClass ());
                    resultList.add (new Attribute (name, new Integer (this.connectionPoolDefinition.getMaximumNewConnections ())));
                } else if (equalsProperty(name, ProxoolConstants.MINIMUM_CONNECTION_COUNT)) {
                    checkAssignable (name, Integer.class, value.getClass ());
                    resultList.add (new Attribute (name, new Integer (this.connectionPoolDefinition.getMinimumConnectionCount ())));
                } else if (equalsProperty(name, ProxoolConstants.OVERLOAD_WITHOUT_REFUSAL_LIFETIME)) {
                    checkAssignable (name, Integer.class, value.getClass ());
                    resultList.add (new Attribute (name, new Integer (this.connectionPoolDefinition.getOverloadWithoutRefusalLifetime ())));
                } else if (equalsProperty(name, ProxoolConstants.PROTOTYPE_COUNT)) {
                    checkAssignable (name, Integer.class, value.getClass ());
                    resultList.add (new Attribute (name, new Integer (this.connectionPoolDefinition.getPrototypeCount ())));
                } else if (equalsProperty(name, ProxoolConstants.RECENTLY_STARTED_THRESHOLD)) {
                    checkAssignable (name, Integer.class, value.getClass ());
                    resultList.add (new Attribute (name, new Integer (this.connectionPoolDefinition.getRecentlyStartedThreshold ())));
                } else if (equalsProperty(name, ProxoolConstants.TRACE)) {
                    checkAssignable (name, Boolean.class, value.getClass ());
                    resultList.add (new Attribute (name, new Boolean (this.connectionPoolDefinition.isTrace ())));
                } else if (equalsProperty(name, ProxoolConstants.VERBOSE)) {
                    checkAssignable (name, Boolean.class, value.getClass ());
                    resultList.add (new Attribute (name, new Boolean (this.connectionPoolDefinition.isVerbose ())));
                } else {
                    throw new AttributeNotFoundException ("Unknown attribute: " + name);
                }
            } catch (InvalidAttributeValueException e) {
                throw new RuntimeOperationsException (new RuntimeException ("Attribute value was illegal: " + e.getMessage ()));
            } catch (AttributeNotFoundException e) {
                throw new RuntimeOperationsException (new IllegalArgumentException (e.getMessage ()));
            }
        }
        return resultList;
    }

    /**
     * @see DynamicMBean#invoke(String, Object[], String[])
     */
    public Object invoke (String operationName, Object params[], String signature[]) throws MBeanException, ReflectionException {
        if (operationName == null) {
            throw new RuntimeOperationsException (new IllegalArgumentException ("Operation name cannot be null"), "Cannot invoke a null operation in " + CLASS_NAME);
        }
        return null;
    }

    /**
     * @see DynamicMBean#getMBeanInfo()
     */
    public MBeanInfo getMBeanInfo () {
        return mBeanInfo;
    }

    private void buildDynamicMBeanInfo (String alias) {
        final MBeanAttributeInfo[] attributeInfos = new MBeanAttributeInfo[]{
            createProxoolAttribute (ProxoolConstants.ALIAS, String.class),
            createProxoolAttribute (ProxoolConstants.DRIVER_PROPERTIES, String.class),
            createProxoolAttribute (ProxoolConstants.DRIVER_URL, String.class),
            createProxoolAttribute (ProxoolConstants.FATAL_SQL_EXCEPTION, String.class),
            createProxoolAttribute (ProxoolConstants.HOUSE_KEEPING_SLEEP_TIME, Integer.class),
            createProxoolAttribute (ProxoolConstants.HOUSE_KEEPING_TEST_SQL, Integer.class),
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
            new MBeanOperationInfo ("shutdown", "shutdown(): Stop and dispose this connection pool.",
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
        return new MBeanAttributeInfo (getValidIdentifier(attributeName), type.getName (),
            getAttributeDescription (attributeName), true, true, false);
    }

    private void checkAssignable (String name, Class class1, Class class2) throws InvalidAttributeValueException {
        if (!class1.isAssignableFrom (class2)) {
            throw(new InvalidAttributeValueException ("Cannot set attribute " + name + " to a " + class2.getName () + " object, " + class1.getName () + " expected."));
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
}

/*
 Revision history:
 $Log: ConnectionPoolMBean.java,v $
 Revision 1.1  2003/02/24 01:14:17  chr32
 Init rev (unfinished).

*/