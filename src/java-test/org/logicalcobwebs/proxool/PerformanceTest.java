/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import org.logicalcobwebs.logging.Log;
import org.logicalcobwebs.logging.LogFactory;
import org.logicalcobwebs.proxool.admin.StatisticsIF;
import org.logicalcobwebs.proxool.admin.StatisticsListenerIF;
import org.logicalcobwebs.proxool.admin.SnapshotIF;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.text.DecimalFormat;

/**
 * Tests how fast Proxool is compared to the "perfect" pool, {@link SimpoolAdapter}.
 *
 * @version $Revision: 1.13 $, $Date: 2003/03/10 15:31:26 $
 * @author Bill Horsman (bill@logicalcobwebs.co.uk)
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.5
 */
public class PerformanceTest extends AbstractProxoolTest  implements StatisticsListenerIF {

    private static final Log LOG = LogFactory.getLog(PerformanceTest.class);

    private static DecimalFormat millisecondsFormat = new DecimalFormat("0.00");

    private Thread waitingThead;

    private StatisticsIF statistics;
    private static final int period = 10;
    private static final int count = 1;

    public PerformanceTest(String s) {
        super(s);
    }

    /**
     * Test how many connections we can serve if we go as fast as we can!
     * @throws ProxoolException if anything goes wrong
     */
    public void testPerformance() throws ProxoolException {

        waitingThead = Thread.currentThread();

        String alias = "testPeformance";
        int threadCount = 5;
        String url = TestHelper.buildProxoolUrl(alias,
                TestConstants.HYPERSONIC_DRIVER,
                TestConstants.HYPERSONIC_TEST_URL);
        Properties info = new Properties();
        info.setProperty(ProxoolConstants.USER_PROPERTY, TestConstants.HYPERSONIC_USER);
        info.setProperty(ProxoolConstants.PASSWORD_PROPERTY, TestConstants.HYPERSONIC_PASSWORD);
        info.setProperty(ProxoolConstants.MINIMUM_CONNECTION_COUNT_PROPERTY, String.valueOf(threadCount));
        info.setProperty(ProxoolConstants.STATISTICS_PROPERTY, String.valueOf(period) + "s");
        info.setProperty(ProxoolConstants.STATISTICS_LOG_LEVEL_PROPERTY, ProxoolConstants.STATISTICS_LOG_LEVEL_INFO);
        ProxoolFacade.registerConnectionPool(url, info);
        ProxoolFacade.addStatisticsListener(alias, this);

        doWait();

        AnnoyingConnector[] annoyingConnectors = new AnnoyingConnector[threadCount];
        for (int i = 0; i < annoyingConnectors.length; i++) {
            annoyingConnectors[i] = new AnnoyingConnector(alias);
            Thread t = new Thread(annoyingConnectors[i]);
            t.start();
        }

        for (int i = 0; i < count; i++) {
            doWait();
        }

        for (int i = 0; i < annoyingConnectors.length; i++) {
            annoyingConnectors[i].cancel();
        }

        LOG.info("Served " + statistics.getServedCount()
            + " at " + millisecondsFormat.format((double) (1000 * period * count) / (double) statistics.getServedCount()) + " ms per connection");

    }

    private void doWait() {
        synchronized (Thread.currentThread()) {
            try {
                Thread.currentThread().wait(60000);
            } catch (InterruptedException e) {
                fail("Statistics didn't arrive as expected");
            }
        }
    }

    public void statistics(String alias, StatisticsIF statistics) {
        this.statistics = statistics;
        synchronized (waitingThead) {
            waitingThead.notify();
        }
    }

    public void testSnapshotImpact() throws ProxoolException {

        waitingThead = Thread.currentThread();

        String alias = "testPeformance";
        int threadCount = 10;
        String url = TestHelper.buildProxoolUrl(alias,
                TestConstants.HYPERSONIC_DRIVER,
                TestConstants.HYPERSONIC_TEST_URL);
        Properties info = new Properties();
        info.setProperty(ProxoolConstants.USER_PROPERTY, TestConstants.HYPERSONIC_USER);
        info.setProperty(ProxoolConstants.PASSWORD_PROPERTY, TestConstants.HYPERSONIC_PASSWORD);
        info.setProperty(ProxoolConstants.MINIMUM_CONNECTION_COUNT_PROPERTY, String.valueOf(threadCount));
        info.setProperty(ProxoolConstants.STATISTICS_PROPERTY, String.valueOf(period) + "s");
        info.setProperty(ProxoolConstants.STATISTICS_LOG_LEVEL_PROPERTY, ProxoolConstants.STATISTICS_LOG_LEVEL_INFO);
        ProxoolFacade.registerConnectionPool(url, info);
        ProxoolFacade.addStatisticsListener(alias, this);
        DisagreeableSnapshotter disagreeableSnapshotter = new DisagreeableSnapshotter(alias);
        new Thread(disagreeableSnapshotter).start();

        AnnoyingConnector[] annoyingConnectors = new AnnoyingConnector[threadCount];
        for (int i = 0; i < annoyingConnectors.length; i++) {
            annoyingConnectors[i] = new AnnoyingConnector(alias);
            Thread t = new Thread(annoyingConnectors[i]);
            t.start();
        }

        doWait();

        for (int i = 0; i < count; i++) {
            doWait();
        }

        for (int i = 0; i < annoyingConnectors.length; i++) {
            annoyingConnectors[i].cancel();
        }
        disagreeableSnapshotter.cancel();

        LOG.info("Served " + statistics.getServedCount()
            + " at " + millisecondsFormat.format((double) (1000 * period * count) / (double) statistics.getServedCount()) + " ms per connection");

    }

    class DisagreeableSnapshotter implements Runnable {

        private String alias;

        private boolean cancelled;

        public DisagreeableSnapshotter(String alias) {
            this.alias = alias;
        }

        public void run() {

            while (!cancelled) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    LOG.error("Awoken", e);
                }
                try {
                    SnapshotIF s = ProxoolFacade.getSnapshot(alias, true);
                    if (LOG.isInfoEnabled()) {
                        LOG.info("Snapshot: served=" + s.getServedCount() + ", active=" + s.getActiveConnectionCount());
                    }
                } catch (ProxoolException e) {
                    LOG.error("Couldn't get snapshot", e);
                }
            }
        }

        public void cancel() {
            cancelled = true;
        }

    }

    class AnnoyingConnector implements Runnable {

        private String alias;

        private boolean cancelled;

        private int exceptionCount;

        public AnnoyingConnector(String alias) {
            this.alias = alias;
        }

        public void run() {
                while (!cancelled) {
                    try {
                        Connection connection = null;
                        try {
                            connection = DriverManager.getConnection(TestHelper.buildProxoolUrl(alias));
                            Statement s = connection.createStatement();
                            Thread.yield();
                        } finally {
                            if (connection != null) {
                                connection.close();
                            }
                        }
                    } catch (SQLException e) {
                        LOG.error(Thread.currentThread().getName(), e);
                        exceptionCount++;
                    }
                }
        }

        public void cancel() {
            cancelled = true;
        }

        public int getExceptionCount() {
            return exceptionCount;
        }

    }

}

/*
 Revision history:
 $Log: PerformanceTest.java,v $
 Revision 1.13  2003/03/10 15:31:26  billhorsman
 fixes

 Revision 1.12  2003/03/04 10:24:40  billhorsman
 removed try blocks around each test

 Revision 1.11  2003/03/03 17:08:57  billhorsman
 all tests now extend AbstractProxoolTest

 Revision 1.10  2003/03/03 11:12:04  billhorsman
 fixed licence

 Revision 1.9  2003/03/01 15:27:24  billhorsman
 checkstyle

 Revision 1.8  2003/02/19 15:14:23  billhorsman
 fixed copyright (copy and paste error,
 not copyright change)

 Revision 1.7  2003/02/06 17:41:03  billhorsman
 now uses imported logging

 Revision 1.6  2002/12/16 17:05:05  billhorsman
 new test structure

 Revision 1.5  2002/11/09 16:01:53  billhorsman
 fix doc

 Revision 1.4  2002/11/02 14:22:16  billhorsman
 Documentation

 Revision 1.3  2002/11/02 13:57:34  billhorsman
 checkstyle

 Revision 1.2  2002/11/02 11:37:48  billhorsman
 New tests

 Revision 1.1  2002/10/30 21:17:51  billhorsman
 new performance tests

*/
