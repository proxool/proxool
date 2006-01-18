/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Test that we can update the definition of a pool
 *
 * @version $Revision: 1.9 $, $Date: 2006/01/18 14:40:06 $
 * @author bill
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.8
 */
public class UpdateDefinitionTest extends AbstractProxoolTest {

    private static final Log LOG = LogFactory.getLog(UpdateDefinitionTest.class);

    public UpdateDefinitionTest(String alias) {
        super(alias);
    }

    /**
     * Can we change the delegate URL of a pool
     */
    public void testChangeUrl() throws Exception {

        String testName = "changeUrl";
        String alias = testName;

        String url1 = TestHelper.buildProxoolUrl(alias,
                TestConstants.HYPERSONIC_DRIVER,
                TestConstants.HYPERSONIC_TEST_URL);

        String url2 = TestHelper.buildProxoolUrl(alias,
                TestConstants.HYPERSONIC_DRIVER,
                TestConstants.HYPERSONIC_TEST_URL2);

        Properties info = new Properties();
        info.setProperty(ProxoolConstants.USER_PROPERTY, TestConstants.HYPERSONIC_USER);
        info.setProperty(ProxoolConstants.PASSWORD_PROPERTY, TestConstants.HYPERSONIC_PASSWORD);
        info.setProperty(ProxoolConstants.MINIMUM_CONNECTION_COUNT_PROPERTY, "2");

        // register pool
        ProxoolFacade.registerConnectionPool(url1, info);

        // Get one connection
        DriverManager.getConnection(url1).close();
        assertEquals("connectionsServedCount", 1L, ProxoolFacade.getSnapshot(alias, false).getServedCount());

        ProxoolFacade.updateConnectionPool(url2, null);

        // Get another connection
        DriverManager.getConnection(url2).close();
        assertEquals("connectionsServedCount", 2L, ProxoolFacade.getSnapshot(alias, false).getServedCount());

        ProxoolFacade.updateConnectionPool(url1, null);
        DriverManager.getConnection(url1).close();
        assertEquals("connectionsServedCount", 3L, ProxoolFacade.getSnapshot(alias, false).getServedCount());

    }

    /**
     * Can we update a pool definition by passing a new Properties object?
     */
    public void testUpdate() throws Exception, ClassNotFoundException {

        String testName = "update";
        String alias = testName;

        String url = TestHelper.buildProxoolUrl(alias,
                TestConstants.HYPERSONIC_DRIVER,
                TestConstants.HYPERSONIC_TEST_URL);
        Properties info = new Properties();
        info.setProperty(ProxoolConstants.USER_PROPERTY, TestConstants.HYPERSONIC_USER);
        info.setProperty(ProxoolConstants.PASSWORD_PROPERTY, TestConstants.HYPERSONIC_PASSWORD);
        info.setProperty(ProxoolConstants.MAXIMUM_CONNECTION_COUNT_PROPERTY, "1");
        ProxoolFacade.registerConnectionPool(url, info);

        // Open a connection. Just for the hell of it
        DriverManager.getConnection(url).close();

        assertEquals("maximumConnectionCount", 1, ProxoolFacade.getConnectionPoolDefinition(alias).getMaximumConnectionCount());

        // Update using facade
        info = new Properties();
        info.setProperty(ProxoolConstants.MAXIMUM_CONNECTION_COUNT_PROPERTY, "2");
        ProxoolFacade.updateConnectionPool(url, info);
        assertEquals("maximumConnectionCount", 2, ProxoolFacade.getConnectionPoolDefinition(alias).getMaximumConnectionCount());

        // Now do it on the fly (this is a redefine really)
        info = new Properties();
        info.setProperty(ProxoolConstants.USER_PROPERTY, TestConstants.HYPERSONIC_USER);
        info.setProperty(ProxoolConstants.PASSWORD_PROPERTY, TestConstants.HYPERSONIC_PASSWORD);
        info.setProperty(ProxoolConstants.MAXIMUM_CONNECTION_COUNT_PROPERTY, "3");
        DriverManager.getConnection(url, info).close();
        assertEquals("maximumConnectionCount", 3, ProxoolFacade.getConnectionPoolDefinition(alias).getMaximumConnectionCount());

    }

    /**
     * If we request a connection using exactly the same URL and properties check that it doesn't trigger an update
     * which forces the pool to be restarted (all existing connections destroyed).
     */
    public void testDefinitionNotChanging() throws SQLException, ProxoolException {

        String testName = "definitionNotChanging";
        String alias = testName;


        Connection c1 = null;
        try {
            String url = TestHelper.buildProxoolUrl(alias,
                    TestConstants.HYPERSONIC_DRIVER,
                    TestConstants.HYPERSONIC_TEST_URL);
            Properties info = new Properties();
            info.setProperty(ProxoolConstants.USER_PROPERTY, TestConstants.HYPERSONIC_USER);
            info.setProperty(ProxoolConstants.PASSWORD_PROPERTY, TestConstants.HYPERSONIC_PASSWORD);
            info.setProperty(ProxoolConstants.MAXIMUM_CONNECTION_COUNT_PROPERTY, "1");
            c1 = DriverManager.getConnection(url, info);
            assertEquals("id=1", 1L, ProxoolFacade.getId(c1));
        } finally {
            c1.close();
        }
        // The second attempt (using the same definition) should give back the same connection ID
        Connection c2 = null;
        try {
            String url = TestHelper.buildProxoolUrl(alias,
                    TestConstants.HYPERSONIC_DRIVER,
                    TestConstants.HYPERSONIC_TEST_URL);
            Properties info = new Properties();
            info.setProperty(ProxoolConstants.USER_PROPERTY, TestConstants.HYPERSONIC_USER);
            info.setProperty(ProxoolConstants.PASSWORD_PROPERTY, TestConstants.HYPERSONIC_PASSWORD);
            info.setProperty(ProxoolConstants.MAXIMUM_CONNECTION_COUNT_PROPERTY, "1");
            c2 = DriverManager.getConnection(url, info);
            assertEquals("id=1", 1L, ProxoolFacade.getId(c2));
        } finally {
            c2.close();
        }
        // Not the same object. It's wrapped.
        assertNotSame("c1!=c2", c1, c2);
    }

    /**
     * Can we update a pool definition by calling updateConnectionPool?
     */
    public void testUpdateUsingAPI() throws Exception, ClassNotFoundException {

        String testName = "updateUsingAPI";
        String alias = testName;

        String url = ProxoolConstants.PROXOOL
                + ProxoolConstants.ALIAS_DELIMITER
                + alias
                + ProxoolConstants.URL_DELIMITER
                + TestConstants.HYPERSONIC_DRIVER
                + ProxoolConstants.URL_DELIMITER
                + TestConstants.HYPERSONIC_TEST_URL2; //"jdbc:hsqldb:db/update";


        LOG.debug("Register pool");
        Properties info = new Properties();
        String checkAlias = ProxoolFacade.registerConnectionPool(url, info);
        assertEquals(alias, checkAlias);

        LOG.debug("setConfigurationListener");
        ProxoolFacade.addConfigurationListener(alias, new ConfigurationListenerIF() {
            public void definitionUpdated(ConnectionPoolDefinitionIF connectionPoolDefinition, Properties completeInfo, Properties changedInfo) {
            }
        });

        LOG.debug("setStateListener");
        ProxoolFacade.addStateListener(alias, new StateListenerIF() {
            public void upStateChanged(int upState) {
            }
        });

        LOG.debug("Update pool");
        ProxoolFacade.updateConnectionPool(url, info);

    }

}


/*
 Revision history:
 $Log: UpdateDefinitionTest.java,v $
 Revision 1.9  2006/01/18 14:40:06  billhorsman
 Unbundled Jakarta's Commons Logging.

 Revision 1.8  2005/09/25 21:48:09  billhorsman
 New test to check that asking for a connection using the same URL and properties doesn't redefine the pool.

 Revision 1.7  2004/05/26 17:19:10  brenuart
 Allow JUnit tests to be executed against another database.
 By default the test configuration will be taken from the 'testconfig-hsqldb.properties' file located in the org.logicalcobwebs.proxool package.
 This behavior can be overriden by setting the 'testConfig' environment property to another location.

 Revision 1.6  2003/03/04 12:50:44  billhorsman
 fix

 Revision 1.5  2003/03/04 10:24:40  billhorsman
 removed try blocks around each test

 Revision 1.4  2003/03/03 17:09:07  billhorsman
 all tests now extend AbstractProxoolTest

 Revision 1.3  2003/03/03 11:12:05  billhorsman
 fixed licence

 Revision 1.2  2003/03/01 15:27:24  billhorsman
 checkstyle

 Revision 1.1  2003/02/27 18:01:48  billhorsman
 completely rethought the test structure. it's now
 more obvious. no new tests yet though.

 */