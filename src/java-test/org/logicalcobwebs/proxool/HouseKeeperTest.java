/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import org.logicalcobwebs.logging.Log;
import org.logicalcobwebs.logging.LogFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

/**
 * Test the house keeper in ConnectionPool
 *
 * @version $Revision: 1.7 $, $Date: 2003/09/11 23:58:05 $
 * @author bill
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.8
 */
public class HouseKeeperTest extends AbstractProxoolTest {

    private static final Log LOG = LogFactory.getLog(HouseKeeperTest.class);

    public HouseKeeperTest(String alias) {
        super(alias);
    }

    /**
     * Test that connections that remain active for longer than the configured
     * time are closed (and destroyed) automatically.
     */
    public void testMaximumActiveTime() throws Exception {

        String testName = "maximumActiveTime";
        String alias = testName;

        String url = TestHelper.buildProxoolUrl(alias,
                TestConstants.HYPERSONIC_DRIVER,
                TestConstants.HYPERSONIC_TEST_URL);
        Properties info = new Properties();
        info.setProperty(ProxoolConstants.USER_PROPERTY, TestConstants.HYPERSONIC_USER);
        info.setProperty(ProxoolConstants.PASSWORD_PROPERTY, TestConstants.HYPERSONIC_PASSWORD);
        info.setProperty(ProxoolConstants.MAXIMUM_ACTIVE_TIME_PROPERTY, "1000");
        info.setProperty(ProxoolConstants.HOUSE_KEEPING_SLEEP_TIME_PROPERTY, "1000");
        ProxoolFacade.registerConnectionPool(url, info);

        assertEquals("Shouldn't be any active connections yet", 0, ProxoolFacade.getSnapshot(alias, false).getServedCount());

        final Connection connection = DriverManager.getConnection(url);
        ;
        long start = System.currentTimeMillis();

        assertEquals("We just opened 1 connection", 1, ProxoolFacade.getSnapshot(alias, false).getServedCount());

        new ResultMonitor() {
            public boolean check() throws Exception {
                return connection.isClosed();
            }
        }.getResult();
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            LOG.debug("Awoken.");
        }

        long elapsed = System.currentTimeMillis() - start;
        assertTrue("Connection has not been closed after " + elapsed + " milliseconds as expected", connection.isClosed());

        assertEquals("Expected the connection to be inactive", 0, ProxoolFacade.getSnapshot(alias, false).getActiveConnectionCount());

    }

    /**
     * Test that house keeper destroys connections that fail configured
     * the test sql
     */
    public void testHouseKeeperTestSql() throws Exception {

        String testName = "houseKeeperTestSql";
        String alias = testName;

        String url = TestHelper.buildProxoolUrl(alias,
                TestConstants.HYPERSONIC_DRIVER,
                TestConstants.HYPERSONIC_TEST_URL);
        Properties info = new Properties();
        info.setProperty(ProxoolConstants.USER_PROPERTY, TestConstants.HYPERSONIC_USER);
        info.setProperty(ProxoolConstants.PASSWORD_PROPERTY, TestConstants.HYPERSONIC_PASSWORD);
        info.setProperty(ProxoolConstants.HOUSE_KEEPING_TEST_SQL_PROPERTY, "SELECT NOW");
        info.setProperty(ProxoolConstants.HOUSE_KEEPING_SLEEP_TIME_PROPERTY, "1000");
        ProxoolFacade.registerConnectionPool(url, info);

        DriverManager.getConnection(url).close();

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            LOG.debug("Awoken.");
        }

        DriverManager.getConnection(url).close();
    }

}


/*
 Revision history:
 $Log: HouseKeeperTest.java,v $
 Revision 1.7  2003/09/11 23:58:05  billhorsman
 New test for house-keeper-test-sql

 Revision 1.6  2003/03/04 10:24:40  billhorsman
 removed try blocks around each test

 Revision 1.5  2003/03/03 17:08:57  billhorsman
 all tests now extend AbstractProxoolTest

 Revision 1.4  2003/03/03 11:12:04  billhorsman
 fixed licence

 Revision 1.3  2003/03/02 00:53:49  billhorsman
 more robust wait

 Revision 1.2  2003/03/01 15:27:24  billhorsman
 checkstyle

 Revision 1.1  2003/02/27 18:01:48  billhorsman
 completely rethought the test structure. it's now
 more obvious. no new tests yet though.

 */