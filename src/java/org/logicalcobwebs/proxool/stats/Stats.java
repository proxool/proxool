/*
 * This software is released under the Apache Software Licence. See
 * package.html for details. The latest version is available at
 * http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool.stats;

import org.logicalcobwebs.proxool.FastArrayList;
import org.logicalcobwebs.proxool.ConnectionPoolStatisticsIF;
import org.logicalcobwebs.proxool.ConnectionPoolDefinitionIF;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Calendar;
import java.util.List;
import java.util.Date;
import java.util.TimerTask;
import java.util.Timer;
import java.text.DecimalFormat;

/**
 * Provides statistics about the performance of a pool.
 *
 * @version $Revision: 1.1 $, $Date: 2003/01/30 17:20:19 $
 * @author bill
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.7
 */
public class Stats {

    private Log log;

    private List statistics = new FastArrayList();

    private static final int historySize = 1;

    private Statistics currentStatistics;

    private String alias;

    private Calendar nextRollDate;

    private DecimalFormat decimalFormat = new DecimalFormat("0.00");

    private int period;

    /**
     * @param alias identifies the pool
     * @param period the period in seconds between statistic samples
     */
    public Stats(String alias, int period) {
        this.alias = alias;
        this.period = period;
        log = LogFactory.getLog("org.logicalcobwebs.proxool.stats." + alias);
        nextRollDate = Calendar.getInstance();
        nextRollDate.clear(Calendar.SECOND);
        nextRollDate.clear(Calendar.MILLISECOND);
        nextRollDate.add(Calendar.SECOND, period);

        currentStatistics = new Statistics(new Date());

        // Automatically trigger roll if no activity
        TimerTask tt = new TimerTask() {
            public void run() {
                rollSample();
            }
        };
        Timer t = new Timer(true);
        t.schedule(tt, 5000, 5000);
    }

    /**
     * Identify the pool
     * @return alias
     */
    public String getAlias() {
        return alias;
    }

    private synchronized void rollSample() {
        if (!isCurrent()) {
            currentStatistics.setStopDate(nextRollDate.getTime());
            statistics.add(currentStatistics);
            currentStatistics = new Statistics(nextRollDate.getTime());
            nextRollDate.add(Calendar.SECOND, period);
            if (statistics.size() > historySize) {
                statistics.remove(0);
            }
            logStats(getStatistics());
        }
    }

    private void logStats(StatisticsIF sample) {

        if (sample != null) {
            StringBuffer out = new StringBuffer();

            out.append("s:");
            out.append(sample.getServedCount());
            out.append(":");
            out.append(decimalFormat.format(sample.getServedPerSecond()));

            out.append("/s, r:");
            out.append(sample.getRefusedCount());
            out.append(":");
            out.append(decimalFormat.format(sample.getRefusedPerSecond()));

            out.append("/s, a:");
            out.append(decimalFormat.format(sample.getAverageActiveTime()));
            out.append("ms/");
            out.append(decimalFormat.format(sample.getAverageActiveCount()));

            log.info(out.toString());
        }
    }

    private boolean isCurrent() {
        return (System.currentTimeMillis() < nextRollDate.getTime().getTime());
    }

    /**
     * Call this every time an active connection is returned to the pool
     * @param activeTime how long the connection was active
     */
    public void connectionReturned(long activeTime) {
        if (!isCurrent()) {
            rollSample();
        }
        currentStatistics.connectionReturned(activeTime);
    }

    /**
     * Call this every time a connection is refused
     */
    public void connectionRefused() {
        if (!isCurrent()) {
            rollSample();
        }
        currentStatistics.connectionRefused();
    }

    /**
     * Returns the most recent sample that has completed its perion
     * @return sample (or null if no statistics are complete yet)
     */
    public StatisticsIF getStatistics() {
        if (!isCurrent()) {
            rollSample();
        }
        try {
            return (StatisticsIF) statistics.get(statistics.size() - 1);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    /**
     * Get a new snapshot
     * @param cps used to help populate the snapshot
     * @param cpd used to help populate the snapshot
     * @return snapshot
     */
    public SnapshotIF getSnapshot(ConnectionPoolStatisticsIF cps, ConnectionPoolDefinitionIF cpd) {
        Snapshot status = new Snapshot(new Date());

        status.setDateStarted(cps.getDateStarted());
        status.setActiveConnectionCount(cps.getActiveConnectionCount());
        status.setAvailableConnectionCount(cps.getAvailableConnectionCount());
        status.setOfflineConnectionCount(cps.getOfflineConnectionCount());
        status.setMaximumConnectionCount(cpd.getMaximumConnectionCount());
        status.setServedCount(cps.getConnectionsServedCount());
        status.setRefusedCount(cps.getConnectionsRefusedCount());

        return status;
    }

}


/*
 Revision history:
 $Log: Stats.java,v $
 Revision 1.1  2003/01/30 17:20:19  billhorsman
 fixes, improvements and doc

 */