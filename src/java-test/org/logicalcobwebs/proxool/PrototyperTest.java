/*
 * This software is released under the Apache Software Licence. See
 * package.html for details. The latest version is available at
 * http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import junit.framework.TestCase;
import org.logicalcobwebs.logging.Log;
import org.logicalcobwebs.logging.LogFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

/**
 * Test the prototyper in ConnectionPool
 *
 * @version $Revision: 1.1 $, $Date: 2003/02/27 18:01:48 $
 * @author bill
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.8
 */
public class PrototyperTest extends TestCase {

    private static final Log LOG = LogFactory.getLog(PrototyperTest.class);

    public PrototyperTest(String alias) {
        super(alias);
    }

    protected void setUp() throws Exception {
        GlobalTest.globalSetup();
    }

    protected void tearDown() throws Exception {
        GlobalTest.globalTeardown();
    }

    /**
     * Test that spare connections are made as we run out of them
     */
    public void testPrototypeCount() throws Exception {

        String testName = "prototypeCount";
        String alias = testName;
        try {
            Properties info = new Properties();
            info.setProperty(ProxoolConstants.USER_PROPERTY, TestConstants.HYPERSONIC_USER);
            info.setProperty(ProxoolConstants.PASSWORD_PROPERTY, TestConstants.HYPERSONIC_PASSWORD);
            info.setProperty(ProxoolConstants.VERBOSE_PROPERTY, Boolean.TRUE.toString());
            info.setProperty(ProxoolConstants.MINIMUM_CONNECTION_COUNT_PROPERTY, "0");
            info.setProperty(ProxoolConstants.MAXIMUM_CONNECTION_COUNT_PROPERTY, "5");
            info.setProperty(ProxoolConstants.PROTOTYPE_COUNT_PROPERTY, "2");
            info.setProperty(ProxoolConstants.HOUSE_KEEPING_SLEEP_TIME_PROPERTY, "1000");
            String url = ProxoolConstants.PROXOOL
                + ProxoolConstants.ALIAS_DELIMITER
                + alias
                + ProxoolConstants.URL_DELIMITER
                + TestConstants.HYPERSONIC_DRIVER
                + ProxoolConstants.URL_DELIMITER
                + TestConstants.HYPERSONIC_TEST_URL;
            ProxoolFacade.registerConnectionPool(url, info);

            Connection[] connections = new Connection[6];

            Thread.sleep(2000);
            assertEquals("activeConnectionCount", 0, ProxoolFacade.getSnapshot(alias, false).getActiveConnectionCount());
            assertEquals("availableConnectionCount", 2, ProxoolFacade.getSnapshot(alias, false).getAvailableConnectionCount());

            connections[0] = DriverManager.getConnection(url);

            Thread.sleep(2000);
            assertEquals("activeConnectionCount", 1, ProxoolFacade.getSnapshot(alias, false).getActiveConnectionCount());
            assertEquals("availableConnectionCount", 2, ProxoolFacade.getSnapshot(alias, false).getAvailableConnectionCount());

            connections[1] = DriverManager.getConnection(url);
            connections[2] = DriverManager.getConnection(url);
            connections[3] = DriverManager.getConnection(url);

            Thread.sleep(2000);
            assertEquals("activeConnectionCount", 4, ProxoolFacade.getSnapshot(alias, false).getActiveConnectionCount());
            assertEquals("availableConnectionCount", 1, ProxoolFacade.getSnapshot(alias, false).getAvailableConnectionCount());

        } catch (Exception e) {
            LOG.error("Whilst performing " + testName, e);
            throw e;
        } finally {
            ProxoolFacade.removeConnectionPool(alias);
        }

    }


    /**
     * Test that the minimum number of connections is maintained
     */
    public void testMinimumConnectionCount() throws Exception {

        String testName = "miniumumConnectionCount";
        String alias = testName;
        try {
            Properties info = new Properties();
            info.setProperty(ProxoolConstants.USER_PROPERTY, TestConstants.HYPERSONIC_USER);
            info.setProperty(ProxoolConstants.PASSWORD_PROPERTY, TestConstants.HYPERSONIC_PASSWORD);
            info.setProperty(ProxoolConstants.VERBOSE_PROPERTY, Boolean.TRUE.toString());
            info.setProperty(ProxoolConstants.MINIMUM_CONNECTION_COUNT_PROPERTY, "2");
            info.setProperty(ProxoolConstants.MAXIMUM_CONNECTION_COUNT_PROPERTY, "5");
            info.setProperty(ProxoolConstants.PROTOTYPE_COUNT_PROPERTY, "0");
            info.setProperty(ProxoolConstants.HOUSE_KEEPING_SLEEP_TIME_PROPERTY, "1000");
            String url = TestHelper.buildProxoolUrl(alias, TestConstants.HYPERSONIC_DRIVER, TestConstants.HYPERSONIC_TEST_URL);
            ProxoolFacade.registerConnectionPool(url, info);

            Thread.sleep(2000);
            assertEquals("availableConnectionCount", 2, ProxoolFacade.getSnapshot(alias, false).getAvailableConnectionCount());

        } catch (Exception e) {
            LOG.error("Whilst performing " + testName, e);
            throw e;
        } finally {
            ProxoolFacade.removeConnectionPool(alias);
        }

    }

}


/*
 Revision history:
 $Log: PrototyperTest.java,v $
 Revision 1.1  2003/02/27 18:01:48  billhorsman
 completely rethought the test structure. it's now
 more obvious. no new tests yet though.

 */