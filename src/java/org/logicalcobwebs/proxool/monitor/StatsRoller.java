/*
 * This software is released under the Apache Software Licence. See
 * package.html for details. The latest version is available at
 * http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool.monitor;

import org.logicalcobwebs.logging.Log;
import org.logicalcobwebs.logging.LogFactory;
import org.logicalcobwebs.proxool.ProxoolException;
import org.logicalcobwebs.proxool.ProxoolFacade;
import org.logicalcobwebs.proxool.ProxoolConstants;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Responsbile for a single set of statistics. It rolls over to a new set
 * whenever it should. It provides access to the latest complete set
 * when it is available.
 *
 * @version $Revision: 1.9 $, $Date: 2003/02/06 17:41:06 $
 * @author bill
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.7
 */
class StatsRoller {

    private Log log;

    private Statistics completeStatistics;

    private Statistics currentStatistics;

    private Calendar nextRollDate;

    private int period;

    private int units;

    private Timer timer = new Timer(true);

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00");

    private static final DateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");

    private String logLevel;

    public StatsRoller(String alias, String token) throws ProxoolException {
        log = LogFactory.getLog("org.logicalcobwebs.proxool.stats." + alias);
        logLevel = ProxoolFacade.getConnectionPoolDefinition(alias).getStatisticsLogLevel();

        nextRollDate = Calendar.getInstance();
        if (token.endsWith("s")) {
            units = Calendar.SECOND;
            nextRollDate.clear(Calendar.SECOND);
            nextRollDate.clear(Calendar.MILLISECOND);
        } else if (token.endsWith("m")) {
            units = Calendar.MINUTE;
            nextRollDate.clear(Calendar.MINUTE);
            nextRollDate.clear(Calendar.SECOND);
            nextRollDate.clear(Calendar.MILLISECOND);
        } else if (token.endsWith("h")) {
            nextRollDate.clear(Calendar.HOUR_OF_DAY);
            nextRollDate.clear(Calendar.MINUTE);
            nextRollDate.clear(Calendar.SECOND);
            nextRollDate.clear(Calendar.MILLISECOND);
            units = Calendar.HOUR_OF_DAY;
        } else if (token.endsWith("d")) {
            units = Calendar.DATE;
            nextRollDate.clear(Calendar.HOUR_OF_DAY);
            nextRollDate.clear(Calendar.MINUTE);
            nextRollDate.clear(Calendar.SECOND);
            nextRollDate.clear(Calendar.MILLISECOND);
        } else {
            throw new ProxoolException("Unrecognised suffix in statistics: " + token);
        }

        period = Integer.parseInt(token.substring(0, token.length() - 1));

        // Now roll forward until you get one step into the future
        Calendar now = Calendar.getInstance();
        while (nextRollDate.before(now)) {
            nextRollDate.add(units, period);
        }

        log.debug("Collecting first statistics for '" + token + "' at " + nextRollDate.getTime());
        currentStatistics = new Statistics(now.getTime());

        // Automatically trigger roll if no activity
        TimerTask tt = new TimerTask() {
            public void run() {
                roll();
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    // Oh well
                }
            }
        };
        timer.schedule(tt, 5000, 5000);
    }

    /**
     * Cancels the timer that outputs the stats
     */
    protected void cancel() {
        timer.cancel();
    }

    private synchronized void roll() {
        if (!isCurrent()) {
            currentStatistics.setStopDate(nextRollDate.getTime());
            completeStatistics = currentStatistics;
            currentStatistics = new Statistics(nextRollDate.getTime());
            nextRollDate.add(units, period);
            logStats(completeStatistics);
        }
    }

    private void logStats(StatisticsIF statistics) {

        if (statistics != null && logLevel != null) {

            StringBuffer out = new StringBuffer();

            out.append(TIME_FORMAT.format(statistics.getStartDate()));
            out.append(" - ");
            out.append(TIME_FORMAT.format(statistics.getStopDate()));
            out.append(", s:");
            out.append(statistics.getServedCount());
            out.append(":");
            out.append(DECIMAL_FORMAT.format(statistics.getServedPerSecond()));

            out.append("/s, r:");
            out.append(statistics.getRefusedCount());
            out.append(":");
            out.append(DECIMAL_FORMAT.format(statistics.getRefusedPerSecond()));

            out.append("/s, a:");
            out.append(DECIMAL_FORMAT.format(statistics.getAverageActiveTime()));
            out.append("ms/");
            out.append(DECIMAL_FORMAT.format(statistics.getAverageActiveCount()));

            if (logLevel.equals(ProxoolConstants.STATISTICS_LOG_LEVEL_TRACE)) {
                log.trace(out.toString());
            } else if (logLevel.equals(ProxoolConstants.STATISTICS_LOG_LEVEL_DEBUG)) {
                log.debug(out.toString());
            } else if (logLevel.equals(ProxoolConstants.STATISTICS_LOG_LEVEL_INFO)) {
                log.info(out.toString());
            }

        }
    }

    private boolean isCurrent() {
        return (System.currentTimeMillis() < nextRollDate.getTime().getTime());
    }

    /**
     * @see org.logicalcobwebs.proxool.monitor.Monitor#connectionReturned
     */
    public void connectionReturned(long activeTime) {
        if (!isCurrent()) {
            roll();
        }
        currentStatistics.connectionReturned(activeTime);
    }

    /**
     * @see org.logicalcobwebs.proxool.monitor.Monitor#connectionRefused
     */
    public void connectionRefused() {
        if (!isCurrent()) {
            roll();
        }
        currentStatistics.connectionRefused();
    }

    public Statistics getCompleteStatistics() {
        return completeStatistics;
    }
}


/*
 Revision history:
 $Log: StatsRoller.java,v $
 Revision 1.9  2003/02/06 17:41:06  billhorsman
 now uses imported logging

 Revision 1.8  2003/02/06 15:41:18  billhorsman
 add statistics-log-level

 Revision 1.7  2003/02/04 17:17:03  billhorsman
 make Timer a daemon

 Revision 1.6  2003/02/04 15:59:49  billhorsman
 finalize now shuts down StatsRoller timer

 Revision 1.5  2003/02/02 23:32:48  billhorsman
 fixed bug caused by last variable name change. :(

 Revision 1.4  2003/01/31 16:53:23  billhorsman
 checkstyle

 Revision 1.3  2003/01/31 16:38:54  billhorsman
 doc (and removing public modifier for classes where possible)

 Revision 1.2  2003/01/31 14:33:19  billhorsman
 fix for DatabaseMetaData

 Revision 1.1  2003/01/31 11:35:57  billhorsman
 improvements to servlet (including connection details)

 Revision 1.1  2003/01/31 00:28:57  billhorsman
 now handles multiple statistics

 */