/*
 * This software is released under the Apache Software Licence. See
 * package.html for details. The latest version is available at
 * http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import junit.framework.TestCase;

import java.sql.DriverManager;
import java.util.Properties;

/**
 * Test that registering a {@link org.logicalcobwebs.proxool.ConfigurationListenerIF} with the {@link org.logicalcobwebs.proxool.ProxoolFacade}
 * works.
 *
 * @version $Revision: 1.5 $, $Date: 2003/02/26 16:05:49 $
 * @author Christian Nedregaard (christian_nedregaard@email.com)
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.7
 */
public class ConfigurationListenerTest extends TestCase {

    private Properties properties1;
    private Properties properties2;
    private boolean moreThanTwoReports;

    /**
     * @see junit.framework.TestCase#TestCase
     */
    public ConfigurationListenerTest(String s) {
        super(s);
    }

    /**
     * Test that multiple configuration listeners can be added through ProxoolFacade,
     * and that they get the expected events.
     * @throws java.lang.Exception if the test fails.
     */
    public void testAddConfigurationListener() throws Exception {
        clear();
        Properties info = new Properties();
        info.setProperty("proxool.maximum-connection-count", "2");
        info.setProperty("proxool.maximum-new-connections", "1");
        info.setProperty("proxool.minimum-connection-count", "0");
        info.setProperty("user", "sa");
        info.setProperty("password", "");
        String alias = "configurationListenerTest";
        String driverClass = "org.hsqldb.jdbcDriver";
        String driverUrl = "jdbc:hsqldb:test";
        String url = "proxool." + alias + ":" + driverClass + ":" + driverUrl;
        DriverManager.getConnection(url, info).close();
        ProxoolFacade.addConfigurationListener(alias, new TestConfigurationListener());
        ProxoolFacade.addConfigurationListener(alias, new TestConfigurationListener());
        info.setProperty("proxool.maximum-connection-count", "3");
        info.setProperty("proxool.prototype-count", "1");
        ProxoolFacade.updateConnectionPool(url, info);
        ProxoolFacade.killAllConnections(alias);
        ProxoolFacade.removeConnectionPool(alias);
        assertTrue("Expected properties1 to be set but it was null. ", this.properties1 != null);
        assertTrue("Expected properties2 to be set but it was null. ", this.properties2 != null);
        assertTrue("More than the expected two events was fired. ", !this.moreThanTwoReports);
        assertTrue("Properties 1 was not equal to the changed properties.", this.properties1.equals(info));
        assertTrue("Properties 2 was not equal to the changed properties.", this.properties2.equals(info));
    }

    /**
     * Test that multiple configuration listeners can be added through ProxoolFacade,
     * and then removed, and that they do not receive events after they have been removed.
     * @throws java.lang.Exception if the test fails.
     */
    public void testRemoveConfigurationListener() throws Exception {
        clear();
        Properties info = new Properties();
        info.setProperty("proxool.maximum-connection-count", "2");
        info.setProperty("maximum-new-connections", "1");
        info.setProperty("minimum-connection-count", "0");
        info.setProperty("user", "sa");
        info.setProperty("password", "");
        String alias = "removeConfigurationListenerTest";
        String driverClass = "org.hsqldb.jdbcDriver";
        String driverUrl = "jdbc:hsqldb:test";
        String url = "proxool." + alias + ":" + driverClass + ":" + driverUrl;
        DriverManager.getConnection(url, info).close();
        TestConfigurationListener testConfigurationListener1 = new TestConfigurationListener();
        TestConfigurationListener testConfigurationListener2 = new TestConfigurationListener();
        ProxoolFacade.addConfigurationListener(alias, testConfigurationListener1);
        ProxoolFacade.addConfigurationListener(alias, testConfigurationListener2);
        assertTrue("Listener 1 could not be removed.",
            ProxoolFacade.removeConfigurationListener(alias, testConfigurationListener1));
        assertTrue("Listener 2 could not be removed.",
            ProxoolFacade.removeConfigurationListener(alias, testConfigurationListener2));
        info.setProperty("proxool.maximum-connection-count", "3");
        info.setProperty("prototype-count", "1");
        ProxoolFacade.updateConnectionPool(url, info);
        ProxoolFacade.killAllConnections(alias);
        ProxoolFacade.removeConnectionPool(alias);
        assertTrue("Expected properties1 to be null but it was set. ", this.properties1 == null);
        assertTrue("Expected properties2 to be null but it was set. ", this.properties2 == null);
    }

    private void clear() {
        this.properties1 = null;
        this.properties2 = null;
        this.moreThanTwoReports = false;
    }

    /**
     * Calls {@link org.logicalcobwebs.proxool.GlobalTest#globalSetup}
     * @see junit.framework.TestCase#setUp
     */
    protected void setUp() throws Exception {
        GlobalTest.globalSetup();
        Class.forName("org.logicalcobwebs.proxool.ProxoolDriver");
    }

    /**
     * Calls {@link org.logicalcobwebs.proxool.GlobalTest#globalTeardown}
     * @see junit.framework.TestCase#setUp
     */
    protected void tearDown() throws Exception {
        GlobalTest.globalTeardown();
    }

    class TestConfigurationListener implements ConfigurationListenerIF {
        public void definitionUpdated(ConnectionPoolDefinitionIF connectionPoolDefinition,
            Properties completeInfo, Properties changedInfo) {
            if (properties1 == null) {
                properties1 = completeInfo;
            } else if (properties2 == null) {
                properties2 = completeInfo;
            } else {
                moreThanTwoReports = true;
            }
        }
    }
}

/*
 Revision history:
 $Log: ConfigurationListenerTest.java,v $
 Revision 1.5  2003/02/26 16:05:49  billhorsman
 widespread changes caused by refactoring the way we
 update and redefine pool definitions.

 Revision 1.4  2003/02/24 18:04:07  chr32
 Fixde some eroneous property names.

 Revision 1.3  2003/02/19 17:00:51  chr32
 Fixed eroneous method names.

 Revision 1.2  2003/02/19 15:14:22  billhorsman
 fixed copyright (copy and paste error,
 not copyright change)

 Revision 1.1  2003/02/19 13:47:31  chr32
 Added configuration listener test.

 Revision 1.2  2003/02/18 16:58:12  chr32
 Checkstyle.

 Revision 1.1  2003/02/18 16:51:20  chr32
 Added tests for ConnectionListeners.

*/
