/*
 * This software is released under the Apache Software Licence. See
 * package.html for details. The latest version is available at
 * http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool.admin;

import junit.framework.TestCase;
import org.logicalcobwebs.logging.Log;
import org.logicalcobwebs.logging.LogFactory;
import org.logicalcobwebs.proxool.GlobalTest;
import org.logicalcobwebs.proxool.ProxoolConstants;
import org.logicalcobwebs.proxool.ProxoolException;
import org.logicalcobwebs.proxool.ProxoolFacade;
import org.logicalcobwebs.proxool.TestHelper;
import org.logicalcobwebs.proxool.ConnectionPoolDefinitionIF;
import org.logicalcobwebs.proxool.TestConstants;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;
import java.text.DecimalFormat;

/**
 * Test {@link StatisticsIF}
 *
 * @version $Revision: 1.6 $, $Date: 2003/02/28 12:23:59 $
 * @author Bill Horsman (bill@logicalcobwebs.co.uk)
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.7
 */
public class StatisticsTest extends TestCase {

    private static final Log LOG = LogFactory.getLog(StatisticsTest.class);

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00");

    /**
     * @see TestCase#TestCase
     */
    public StatisticsTest(String s) {
        super(s);
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
     * Test whether the statistics we get back are roughly right.
     */
    public void testStatistics() throws Exception {

        String testName = "statistics";
        String alias = testName;
        try {
            String url = TestHelper.buildProxoolUrl(alias,
                    TestConstants.HYPERSONIC_DRIVER,
                    TestConstants.HYPERSONIC_TEST_URL);
            Properties info = new Properties();
            info.setProperty(ProxoolConstants.USER_PROPERTY, TestConstants.HYPERSONIC_USER);
            info.setProperty(ProxoolConstants.PASSWORD_PROPERTY, TestConstants.HYPERSONIC_PASSWORD);
            info.setProperty(ProxoolConstants.STATISTICS_PROPERTY, "10s,15s");
            info.setProperty(ProxoolConstants.MINIMUM_CONNECTION_COUNT_PROPERTY, "1");

            // We don't test whether anything is logged, but this line should make something appear
            info.setProperty(ProxoolConstants.STATISTICS_LOG_LEVEL_PROPERTY, ProxoolConstants.STATISTICS_LOG_LEVEL_DEBUG);

            // Register pool
            ProxoolFacade.registerConnectionPool(url, info);

            // Skip past the first set because they will probably be for only part
            // of the 10s period.
            StatisticsIF statistics = waitForNextStatistics(alias, "10s", null, 20000);


            Thread.sleep(1000);
            Connection c = DriverManager.getConnection(url);
            // Ensure that active time is non-zero (due to rounding)
            Thread.sleep(20);
            c.close();

            statistics = waitForNextStatistics(alias, "10s", statistics, 20000);

            assertEquals("servedCount", 1L, statistics.getServedCount());
            assertEquals("servedPerSecond", 0.09, 0.11, statistics.getServedPerSecond());
            assertEquals("refusedCount", 0L, statistics.getRefusedCount());
            assertTrue("averageActiveTime > 0", statistics.getAverageActiveTime() > 0);

        } catch (Exception e) {
            LOG.error("Whilst performing " + testName, e);
            throw e;
        } finally {
            ProxoolFacade.removeConnectionPool(alias);
        }

    }

    public void testOverhead() throws Exception {
        String testName = "overhead";
        String alias = testName;
        try {
            String url = TestHelper.buildProxoolUrl(alias,
                    TestConstants.HYPERSONIC_DRIVER,
                    TestConstants.HYPERSONIC_TEST_URL);
            Properties info = new Properties();
            info.setProperty(ProxoolConstants.USER_PROPERTY, TestConstants.HYPERSONIC_USER);
            info.setProperty(ProxoolConstants.PASSWORD_PROPERTY, TestConstants.HYPERSONIC_PASSWORD);
            info.setProperty(ProxoolConstants.STATISTICS_PROPERTY, "10s");
            info.setProperty(ProxoolConstants.MINIMUM_CONNECTION_COUNT_PROPERTY, "1");

            // We don't test whether anything is logged, but this line should make something appear
            info.setProperty(ProxoolConstants.STATISTICS_LOG_LEVEL_PROPERTY, ProxoolConstants.STATISTICS_LOG_LEVEL_DEBUG);

            // Register pool
            ProxoolFacade.registerConnectionPool(url, info);

            ConnectionPoolDefinitionIF cpd = ProxoolFacade.getConnectionPoolDefinition(alias);
            Admin admin = new Admin(cpd);

            final int loops = 100000;
            long start = System.currentTimeMillis();
            for (int i = 0; i < loops; i++) {
                admin.connectionReturned(10);
            }
            double avg = (double) (System.currentTimeMillis() - start) / (double) loops;
            LOG.info("Statistics take " + DECIMAL_FORMAT.format(avg * 1000) + " microseconds");

        } catch (Exception e) {
            LOG.error("Whilst performing " + testName, e);
            throw e;
        } finally {
            ProxoolFacade.removeConnectionPool(alias);
        }
    }

    private StatisticsIF waitForNextStatistics(String alias, String token, StatisticsIF oldStatistics, int timeout) throws ProxoolException {
        long startWaiting = System.currentTimeMillis();
        StatisticsIF statistics = null;
        while (statistics == null || statistics == oldStatistics) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                LOG.debug("Awoken", e);
            }
            if (System.currentTimeMillis() - startWaiting > timeout) {
                fail("Statistics didn't arrive within expected 20 seconds");
            }
            statistics = ProxoolFacade.getStatistics(alias, token);
        }
        LOG.debug("Got stats after " + DECIMAL_FORMAT.format((double) (System.currentTimeMillis() - startWaiting) / 1000) + " seconds");
        return statistics;
    }

}

/*
 Revision history:
 $Log: StatisticsTest.java,v $
 Revision 1.6  2003/02/28 12:23:59  billhorsman
 more robust waiting for statistics

 Revision 1.5  2003/02/27 18:01:49  billhorsman
 completely rethought the test structure. it's now
 more obvious. no new tests yet though.

 Revision 1.4  2003/02/26 23:45:18  billhorsman
 add some sleep

 Revision 1.3  2003/02/26 18:30:02  billhorsman
 test for stats overhead

 Revision 1.2  2003/02/26 16:05:51  billhorsman
 widespread changes caused by refactoring the way we
 update and redefine pool definitions.

 Revision 1.1  2003/02/20 00:33:15  billhorsman
 renamed monitor package -> admin

 Revision 1.3  2003/02/19 23:36:50  billhorsman
 renamed monitor package to admin

 Revision 1.2  2003/02/19 15:14:31  billhorsman
 fixed copyright (copy and paste error,
 not copyright change)

 Revision 1.1  2003/02/07 17:28:36  billhorsman
 *** empty log message ***

  */
