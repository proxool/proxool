/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import org.logicalcobwebs.logging.Log;
import org.logicalcobwebs.logging.LogFactory;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Test whether we can register and remove a pool in various ways
 *
 * @version $Revision: 1.6 $, $Date: 2003/03/04 10:58:44 $
 * @author bill
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.8
 */
public class RegistrationTest extends AbstractProxoolTest {

    private static final Log LOG = LogFactory.getLog(RegistrationTest.class);

    public RegistrationTest(String alias) {
        super(alias);
    }

    /**
     * Test whether we can implicitly register a pool by
     */
    public void testRegister() throws Exception {

        String testName = "register";
        String alias = testName;


        String url = TestHelper.buildProxoolUrl(alias,
                TestConstants.HYPERSONIC_DRIVER,
                TestConstants.HYPERSONIC_TEST_URL);
        Properties info = new Properties();
        info.setProperty(ProxoolConstants.USER_PROPERTY, TestConstants.HYPERSONIC_USER);
        info.setProperty(ProxoolConstants.PASSWORD_PROPERTY, TestConstants.HYPERSONIC_PASSWORD);

        ProxoolFacade.registerConnectionPool(url, info);

        assertNotNull("snapshot exists", ProxoolFacade.getSnapshot(alias));

        // TODO check that properties are configured properly

    }

    /**
     * Can we register, remove and then re-register the same pool?
     */
    public void testRemove() throws Exception {

        String testName = "remove";
        String alias = testName;


        // Register
        String url = TestHelper.buildProxoolUrl(alias,
                TestConstants.HYPERSONIC_DRIVER,
                TestConstants.HYPERSONIC_TEST_URL);
        Properties info = new Properties();
        info.setProperty(ProxoolConstants.USER_PROPERTY, TestConstants.HYPERSONIC_USER);
        info.setProperty(ProxoolConstants.PASSWORD_PROPERTY, TestConstants.HYPERSONIC_PASSWORD);
        ProxoolFacade.registerConnectionPool(url, info);

        try {
            DriverManager.getConnection(url).close();
        } catch (SQLException e) {
            fail("Couldn't get connection");
        }

        // Remove using alias
        ProxoolFacade.removeConnectionPool(alias);
        try {
            ProxoolFacade.getConnectionPoolDefinition(alias);
            fail("Didn't expect to get definition of pool that was just removed");
        } catch (ProxoolException e) {
            LOG.debug("Ignore expected exception", e);
        }

        // Register again
        ProxoolFacade.registerConnectionPool(url, info);
        try {
            DriverManager.getConnection(url).close();
        } catch (SQLException e) {
            fail("Couldn't get connection");
        }
        // Should only be one served (the earlier one is forgotten)
        assertEquals("servedCount", 1L, ProxoolFacade.getSnapshot(alias).getServedCount());

        // Remove using alias
        ProxoolFacade.removeConnectionPool(alias);
        try {
            ProxoolFacade.getConnectionPoolDefinition(alias);
            fail("Didn't expect to get definition of pool that was just removed");
        } catch (ProxoolException e) {
            LOG.debug("Ignore expected exception", e);
        }

        // Register again
        ProxoolFacade.registerConnectionPool(url, info);
        try {
            DriverManager.getConnection(url).close();
        } catch (SQLException e) {
            fail("Couldn't get connection");
        }
        // Should only be one served (the earlier one is forgotten)
        assertEquals("servedCount", 1L, ProxoolFacade.getSnapshot(alias).getServedCount());


    }

    /**
     * Can we have multiple pools?
     */
    public void testMultiple() throws Exception, ClassNotFoundException {

        String testName = "multiple";
        String alias1 = testName + "1";
        String alias2 = testName + "2";

        // Register
        String url1 = TestHelper.buildProxoolUrl(alias1,
                TestConstants.HYPERSONIC_DRIVER,
                TestConstants.HYPERSONIC_TEST_URL);
        String url2 = TestHelper.buildProxoolUrl(alias2,
                TestConstants.HYPERSONIC_DRIVER,
                TestConstants.HYPERSONIC_TEST_URL);
        Properties info = new Properties();
        info.setProperty(ProxoolConstants.USER_PROPERTY, TestConstants.HYPERSONIC_USER);
        info.setProperty(ProxoolConstants.PASSWORD_PROPERTY, TestConstants.HYPERSONIC_PASSWORD);
        ProxoolFacade.registerConnectionPool(url1, info);
        ProxoolFacade.registerConnectionPool(url2, info);

        // Open 2 connections on #1
        DriverManager.getConnection(url1).close();
        DriverManager.getConnection(url1).close();

        // Open 1 connection on #2
        DriverManager.getConnection(url2).close();

        assertEquals("connectionsServedCount #1", 2L, ProxoolFacade.getSnapshot(alias1).getServedCount());
        assertEquals("connectionsServedCount #2", 1L, ProxoolFacade.getSnapshot(alias2).getServedCount());

    }

}

/*
 Revision history:
 $Log: RegistrationTest.java,v $
 Revision 1.6  2003/03/04 10:58:44  billhorsman
 checkstyle

 Revision 1.5  2003/03/04 10:24:40  billhorsman
 removed try blocks around each test

 Revision 1.4  2003/03/03 17:09:06  billhorsman
 all tests now extend AbstractProxoolTest

 Revision 1.3  2003/03/03 11:12:05  billhorsman
 fixed licence

 Revision 1.2  2003/03/01 15:27:24  billhorsman
 checkstyle

 Revision 1.1  2003/02/27 18:01:48  billhorsman
 completely rethought the test structure. it's now
 more obvious. no new tests yet though.

 */