/*
 * This software is released under the Apache Software Licence. See
 * package.html for details. The latest version is available at
 * http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool.admin.jmx;

import junit.framework.TestCase;
import org.logicalcobwebs.proxool.GlobalTest;
import org.logicalcobwebs.proxool.ProxoolConstants;
import org.logicalcobwebs.proxool.ProxoolDriver;
import org.logicalcobwebs.proxool.ProxoolException;
import org.logicalcobwebs.proxool.ProxoolFacade;
import org.logicalcobwebs.proxool.TestConstants;
import org.logicalcobwebs.proxool.TestHelper;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.NotificationFilterSupport;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Properties;


/**
 * Test {@link ConnectionPoolMBean}.
 *
 * @version $Revision: 1.3 $, $Date: 2003/03/01 15:27:25 $
 * @author Christian Nedregaard (christian_nedregaard@email.com)
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.8
 */
public class ConnectionPoolMBeanTest extends TestCase {
    private MBeanServer mBeanServer;
    private boolean notified;

    /**
     * @see TestCase#TestCase(String)
     */
    public ConnectionPoolMBeanTest(String s) {
        super(s);
    }

    /**
     * Test that an attribute can be fetched from the MBean.
     * @throws Exception if an error occours.
     */
    public void testGetAttribute() throws Exception {
        final String alias = "testGetAttribute";
        createBasicPool(alias);
        final ObjectName objectName = ProxoolJMXHelper.getObjectName(alias);
        final String fatalSql = (String) this.mBeanServer.getAttribute(objectName,
            ProxoolJMXHelper.getValidIdentifier(ProxoolConstants.FATAL_SQL_EXCEPTION));
        assertTrue("Expected fatalSQLException to be '" + alias + "', but it was '" + fatalSql
            + "'. ", fatalSql.equals(alias));
        ProxoolFacade.removeConnectionPool(alias);
    }

    /**
     * Test that a list attributes can be fetched from the MBean.
     * @throws Exception if an error occours.
     */
    public void testGetAttributes() throws Exception {
        final String alias = "testGetAttributes";
        createBasicPool(alias);
        final String fatalSQLAttribute = ProxoolJMXHelper.getValidIdentifier(ProxoolConstants.FATAL_SQL_EXCEPTION);
        final String aliasAttribute = ProxoolJMXHelper.getValidIdentifier(ProxoolConstants.ALIAS);
        final ObjectName objectName = ProxoolJMXHelper.getObjectName(alias);
        final AttributeList attributes = this.mBeanServer.getAttributes(objectName, new String[]{
            fatalSQLAttribute,
            aliasAttribute
        });
        String fatalSqlValue = null;
        String aliasValue = null;
        Iterator attributeIterator = attributes.iterator();
        while (attributeIterator.hasNext()) {
            Attribute attribute = (Attribute) attributeIterator.next();
            if (attribute.getName().equals(aliasAttribute)) {
                aliasValue = (String) attribute.getValue();
            } else if (attribute.getName().equals(fatalSQLAttribute)) {
                fatalSqlValue = (String) attribute.getValue();
            }
        }
        assertNotNull("The value for the alias attribute is missing.", aliasValue);
        assertNotNull("The value for the fatalSQLException attribute is missing.", fatalSqlValue);
        assertTrue("Expeted alias to have value '" + aliasValue + "' but the value was '" + aliasValue + ".",
            aliasValue.equals(alias));
        assertTrue("Expexted fatalSQLException to have value '" + alias + "' but the value was '" + fatalSqlValue + ".",
            fatalSqlValue.equals(alias));
        ProxoolFacade.removeConnectionPool(alias);
    }

    /**
     * Test that an attribute can be fetched from the MBean.
     * @throws Exception if an error occours.
     */
    public void testSetAttribute() throws Exception {
        String alias = "testSetAttribute";
        createBasicPool(alias);
        final ObjectName objectName = ProxoolJMXHelper.getObjectName(alias);
        final String fatalSQLAttributeName = ProxoolJMXHelper.getValidIdentifier(ProxoolConstants.FATAL_SQL_EXCEPTION);
        final String newValue =  "dingo";
        this.mBeanServer.setAttribute(objectName,
            new Attribute(fatalSQLAttributeName, newValue));
        final String fatalSQLAttribtueValue = (String) mBeanServer.getAttribute(objectName, fatalSQLAttributeName);
        // check that value vas registered by the bean.
        assertTrue("Expexted fatalSQLException JMX attribtue to have value '" + newValue + "' but the value was '"
            + fatalSQLAttribtueValue + "'.",
            fatalSQLAttribtueValue.equals(newValue));
        // check that the bean updated the pool.
        final String proxoolProopertyValue = (String) ProxoolFacade.getConnectionPoolDefinition(alias)
                .getFatalSqlExceptions().toArray()[0];
        assertTrue("Expexted fatal-sql-exception Proxool property to have value '"
            + newValue + "' but the value was '" + proxoolProopertyValue + "'.",
            proxoolProopertyValue.equals(newValue));
        ProxoolFacade.removeConnectionPool(alias);
    }

    public void testSetAttributes() throws Exception {
        String alias = "testSetAttributes";
        createBasicPool(alias);
        final ObjectName objectName = ProxoolJMXHelper.getObjectName(alias);
        final String fatalSQLAttributeName = ProxoolJMXHelper.getValidIdentifier(ProxoolConstants.FATAL_SQL_EXCEPTION);
        final String testSQLAttributeName = ProxoolJMXHelper.getValidIdentifier(ProxoolConstants.HOUSE_KEEPING_TEST_SQL);
        final String newValue =  "dingo";
        // test when updated through JMX.
        final AttributeList attributeList = new AttributeList();
        attributeList.add(new Attribute(fatalSQLAttributeName, newValue));
        attributeList.add(new Attribute(testSQLAttributeName, newValue));
        this.mBeanServer.setAttributes(objectName, attributeList);
        final String fatalSQLAttribtueValue = (String) mBeanServer.getAttribute(objectName, fatalSQLAttributeName);
        final String testSQLAttribtueValue = (String) mBeanServer.getAttribute(objectName, testSQLAttributeName);
        // check that values vas registered by the bean.
        assertTrue("Expexted fatalSQLException JMX attribtue to have value '" + newValue + "' but the value was '"
            + fatalSQLAttribtueValue + "'.",
            fatalSQLAttribtueValue.equals(newValue));
        assertTrue("Expexted housekeeperTestSQL JMX attribtue to have value '" + newValue + "' but the value was '"
            + testSQLAttribtueValue + "'.",
            testSQLAttribtueValue.equals(newValue));
        // check that the bean updated the pool.
        final String fatalSQLProxoolPropertyValue = (String) ProxoolFacade.getConnectionPoolDefinition(alias)
                .getFatalSqlExceptions().toArray()[0];
        final String testSQLProxoolPropertyValue = ProxoolFacade.getConnectionPoolDefinition(alias)
                .getHouseKeepingTestSql();
        assertTrue("Expexted fatal-sql-exception Proxool property to have value '"
            + newValue + "' but the value was '" + fatalSQLProxoolPropertyValue + ".",
            fatalSQLProxoolPropertyValue.equals(newValue));
        assertTrue("Expexted housekeeper-test-sql Proxool property to have value '"
            + newValue + "' but the value was '" + testSQLProxoolPropertyValue + ".",
            testSQLProxoolPropertyValue.equals(newValue));
        ProxoolFacade.removeConnectionPool(alias);
    }

    public void testInvokeShutown() throws Exception {
        final String alias = "testInvokeShutown";
        createBasicPool(alias);
        final ObjectName objectName = ProxoolJMXHelper.getObjectName(alias);
        this.mBeanServer.invoke(objectName, "shutdown", new Object[0], new String[0]);
        try {
            ProxoolFacade.removeConnectionPool(alias);
            fail("Removal of pool alias should have failed, because it should have allready be removed.");
        } catch (ProxoolException e) {
            // we want this
        }
    }

    public void testNotification() throws Exception {
        String alias = "testNotification";
        Properties info = createBasicPool(alias);
        final NotificationListener notificationListener = new TestNotificationListener();
        final ObjectName objectName = ProxoolJMXHelper.getObjectName(alias);
        this.mBeanServer.addNotificationListener(objectName, notificationListener, getFilter(), notificationListener);
        // test when updated through JMX.
        this.mBeanServer.setAttribute(objectName,
            new Attribute(ProxoolJMXHelper.getValidIdentifier(ProxoolConstants.FATAL_SQL_EXCEPTION), alias));
        assertTrue("We did not get notified when updating through JMX.", this.notified);
        this.notified = false;
        // test when updated through ProxoolFacade
        info = (Properties) info.clone();
        info.put(ProxoolConstants.MAXIMUM_CONNECTION_COUNT, "1");
        ProxoolFacade.updateConnectionPool(ProxoolConstants.PROPERTY_PREFIX + alias, info);
        assertTrue("We did not get notified when updating through ProxoolFacade.", this.notified);
        ProxoolFacade.removeConnectionPool(alias);
    }

    private class TestNotificationListener implements NotificationListener {
        public void handleNotification(Notification notification, Object handBack) {
            if (handBack.equals(this)) {
                notified = true;
            } else {
                fail("Got notification with unknown handback.");
            }
        }
    }

    private NotificationFilter getFilter() {
        final NotificationFilterSupport notificationFilter = new NotificationFilterSupport();
        notificationFilter.enableType(ConnectionPoolMBean.NOTIFICATION_TYPE_DEFINITION_UPDATED);
        return notificationFilter;
    }

    private Properties createBasicPool(String alias) throws SQLException {
        String url = TestHelper.buildProxoolUrl(alias,
                TestConstants.HYPERSONIC_DRIVER,
                TestConstants.HYPERSONIC_TEST_URL);
        Properties info = new Properties();
        info.setProperty(ProxoolConstants.USER_PROPERTY, TestConstants.HYPERSONIC_USER);
        info.setProperty(ProxoolConstants.PASSWORD_PROPERTY, TestConstants.HYPERSONIC_PASSWORD);
        info.setProperty(ProxoolConstants.JMX_PROPERTY, Boolean.TRUE.toString());
        info.setProperty(ProxoolConstants.FATAL_SQL_EXCEPTION_PROPERTY, alias);
        DriverManager.getConnection(url, info).close();
        return info;
    }

    /**
     * Calls {@link GlobalTest#globalSetup}
     * @see TestCase#setUp
     */
    protected void setUp() throws Exception {
        this.notified = false;
        Class.forName(ProxoolDriver.class.getName());
        this.mBeanServer = MBeanServerFactory.createMBeanServer();
        GlobalTest.globalSetup();
    }

    /**
     * Calls {@link GlobalTest#globalTeardown}
     * @see TestCase#setUp
     */
    protected void tearDown() throws Exception {
        MBeanServerFactory.releaseMBeanServer(this.mBeanServer);
        this.mBeanServer = null;
        GlobalTest.globalTeardown();
    }
}

/*
 Revision history:
 $Log: ConnectionPoolMBeanTest.java,v $
 Revision 1.3  2003/03/01 15:27:25  billhorsman
 checkstyle

 Revision 1.2  2003/02/28 12:53:59  billhorsman
 move database to db directory and use constants where possible

 Revision 1.1  2003/02/26 19:03:43  chr32
 Init rev.

 */
