/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool.admin;

import org.logicalcobwebs.logging.Log;
import org.logicalcobwebs.logging.LogFactory;
import org.logicalcobwebs.proxool.AbstractProxoolTest;
import org.logicalcobwebs.proxool.ConnectionPoolDefinitionIF;
import org.logicalcobwebs.proxool.ProxoolConstants;
import org.logicalcobwebs.proxool.ProxoolFacade;
import org.logicalcobwebs.proxool.ResultMonitor;
import org.logicalcobwebs.proxool.TestConstants;
import org.logicalcobwebs.proxool.TestHelper;

import java.sql.DriverManager;
import java.text.DecimalFormat;
import java.util.Properties;

/**
 * Test {@link StatisticsIF}
 *
 * @version $Revision: 1.20 $, $Date: 2003/03/06 12:45:06 $
 * @author Bill Horsman (bill@logicalcobwebs.co.uk)
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.7
 */
public class StatisticsTest extends AbstractProxoolTest {

    private static final Log LOG = LogFactory.getLog(StatisticsTest.class);

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00");

    /**
     * @see junit.framework.TestCase#TestCase
     */
    public StatisticsTest(String s) {
        super(s);
    }

    /**
     * Test whether the statistics we get back are roughly right.
     */
    public void testStatistics() throws Exception {

        String testName = "statistics";
        String alias = testName;

        String url = TestHelper.buildProxoolUrl(alias,
                TestConstants.HYPERSONIC_DRIVER,
                TestConstants.HYPERSONIC_TEST_URL);
        Properties info = new Properties();
        info.setProperty(ProxoolConstants.USER_PROPERTY, TestConstants.HYPERSONIC_USER);
        info.setProperty(ProxoolConstants.PASSWORD_PROPERTY, TestConstants.HYPERSONIC_PASSWORD);
        info.setProperty(ProxoolConstants.STATISTICS_PROPERTY, "10s,15s");
        info.setProperty(ProxoolConstants.MINIMUM_CONNECTION_COUNT_PROPERTY, "1");
        info.setProperty(ProxoolConstants.VERBOSE_PROPERTY, "true");

        // We don't test whether anything is logged, but this line should make something appear
        info.setProperty(ProxoolConstants.STATISTICS_LOG_LEVEL_PROPERTY, ProxoolConstants.STATISTICS_LOG_LEVEL_DEBUG);

        // Register pool
        ProxoolFacade.registerConnectionPool(url, info);

        // Skip past the first set because they will probably be for only part
        // of the 10s period.
        StatisticsResultMonitor srm = new StatisticsResultMonitor(alias, "10s");
        assertEquals("Timeout", ResultMonitor.SUCCESS, srm.getResult());
        srm.getStatistics();


        DriverManager.getConnection(url).close();

        assertEquals("Timeout", ResultMonitor.SUCCESS, srm.getResult());
        StatisticsIF statistics = srm.getStatistics();

        assertEquals("servedCount", 1L, statistics.getServedCount());
        assertEquals("servedPerSecond", 0.09, 0.11, statistics.getServedPerSecond());
        assertEquals("refusedCount", 0L, statistics.getRefusedCount());

    }

    public void testOverhead() throws Exception {
        String testName = "overhead";
        String alias = testName;
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

    }

}

/*
 Revision history:
 $Log: StatisticsTest.java,v $
 Revision 1.20  2003/03/06 12:45:06  billhorsman
 switch on verbose logging

 Revision 1.19  2003/03/06 10:37:41  billhorsman
 better timeout assertion

 Revision 1.18  2003/03/04 10:58:44  billhorsman
 checkstyle

 Revision 1.17  2003/03/04 10:24:41  billhorsman
 removed try blocks around each test

 Revision 1.16  2003/03/03 17:09:09  billhorsman
 all tests now extend AbstractProxoolTest

 Revision 1.15  2003/03/03 11:12:06  billhorsman
 fixed licence

 Revision 1.14  2003/03/03 09:10:41  billhorsman
 removed debug

 Revision 1.13  2003/03/02 01:16:37  billhorsman
 removed flakey average active time test

 Revision 1.12  2003/03/01 18:25:53  billhorsman
 *** empty log message ***

 Revision 1.11  2003/03/01 16:46:08  billhorsman
 debug

 Revision 1.10  2003/03/01 16:14:32  billhorsman
 debug

 Revision 1.9  2003/03/01 16:04:45  billhorsman
 fix

 Revision 1.8  2003/03/01 15:27:25  billhorsman
 checkstyle

 Revision 1.7  2003/02/28 12:36:33  billhorsman
 more robust waiting for statistics

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
