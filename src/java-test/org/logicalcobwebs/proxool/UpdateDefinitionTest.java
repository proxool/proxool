/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import junit.framework.TestCase;
import org.logicalcobwebs.logging.Log;
import org.logicalcobwebs.logging.LogFactory;

import java.sql.DriverManager;
import java.util.Properties;

/**
 * Test that we can update the definition of a pool
 *
 * @version $Revision: 1.3 $, $Date: 2003/03/03 11:12:05 $
 * @author bill
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.8
 */
public class UpdateDefinitionTest extends TestCase {

    private static final Log LOG = LogFactory.getLog(UpdateDefinitionTest.class);

    public UpdateDefinitionTest(String alias) {
        super(alias);
    }

    /**
     * Calls {@link GlobalTest#globalSetup}
     * @see TestCase#setUp
     */
    protected void setUp() throws Exception {
        GlobalTest.globalSetup();
    }

    /**
     * Calls {@link GlobalTest#globalTeardown}
     * @see TestCase#setUp
     */
    protected void tearDown() throws Exception {
        GlobalTest.globalTeardown();
    }

    /**
     * Can we change the delegate URL of a pool
     */
    public void testChangeUrl() throws Exception {

        String testName = "changeUrl";

        try {
            String alias = testName;

            String url1 = TestHelper.buildProxoolUrl(alias,
                    TestConstants.HYPERSONIC_DRIVER,
                    TestConstants.HYPERSONIC_URL_PREFIX + "1");

            String url2 = TestHelper.buildProxoolUrl(alias,
                    TestConstants.HYPERSONIC_DRIVER,
                    TestConstants.HYPERSONIC_URL_PREFIX + "2");

            Properties info = new Properties();
            info.setProperty(ProxoolConstants.USER_PROPERTY, TestConstants.HYPERSONIC_USER);
            info.setProperty(ProxoolConstants.PASSWORD_PROPERTY, TestConstants.HYPERSONIC_PASSWORD);
            info.setProperty("proxool.minimum-connection-count", "2");

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

        } catch (Exception e) {
            LOG.error("Whilst performing " + testName, e);
            throw e;
        }
    }

    /**
     * Can we update a pool definition by passing a new Properties object?
     */
    public void testUpdate() throws Exception, ClassNotFoundException {

        String testName = "update";
        String alias = testName;
        try {
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

        } catch (Exception e) {
            LOG.error("Whilst performing " + testName, e);
            throw e;
        } finally {
            ProxoolFacade.removeConnectionPool(alias);
        }

    }

    /**
     * Can we update a pool definition by calling updateConnectionPool?
     */
    public void testUpdateUsingAPI() throws Exception, ClassNotFoundException {

        String testName = "updateUsingAPI";

        try {
            String alias = testName;

            String url = ProxoolConstants.PROXOOL
                    + ProxoolConstants.ALIAS_DELIMITER
                    + alias
                    + ProxoolConstants.URL_DELIMITER
                    + TestHelper.HYPERSONIC_DRIVER
                    + ProxoolConstants.URL_DELIMITER
                    + "jdbc:hsqldb:db/update";


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

        } catch (Exception e) {
            LOG.error("Whilst performing " + testName, e);
            throw e;
        }

    }

}


/*
 Revision history:
 $Log: UpdateDefinitionTest.java,v $
 Revision 1.3  2003/03/03 11:12:05  billhorsman
 fixed licence

 Revision 1.2  2003/03/01 15:27:24  billhorsman
 checkstyle

 Revision 1.1  2003/02/27 18:01:48  billhorsman
 completely rethought the test structure. it's now
 more obvious. no new tests yet though.

 */