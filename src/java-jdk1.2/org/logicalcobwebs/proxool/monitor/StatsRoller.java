/*
 * This software is released under the Apache Software Licence. See
 * package.html for details. The latest version is available at
 * http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool.monitor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.logicalcobwebs.proxool.ProxoolException;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * TODO
 * @version $Revision: 1.1 $, $Date: 2003/01/31 14:39:08 $
 * @author bill
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.7
 */
public class StatsRoller {

    private Log log;

    private Statistics completeStatistics;

    private Statistics currentStatistics;

    private Calendar nextRollDate;

    private int period;

    private int units;

    private DecimalFormat decimalFormat = new DecimalFormat("0.00");

    private static final DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

    public StatsRoller(String alias, String token) throws ProxoolException {
        log = LogFactory.getLog("org.logicalcobwebs.proxool.stats." + alias);

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

        if (statistics != null) {
            StringBuffer out = new StringBuffer();

            out.append(timeFormat.format(statistics.getStartDate()));
            out.append(" - ");
            out.append(timeFormat.format(statistics.getStopDate()));
            out.append(", s:");
            out.append(statistics.getServedCount());
            out.append(":");
            out.append(decimalFormat.format(statistics.getServedPerSecond()));

            out.append("/s, r:");
            out.append(statistics.getRefusedCount());
            out.append(":");
            out.append(decimalFormat.format(statistics.getRefusedPerSecond()));

            out.append("/s, a:");
            out.append(decimalFormat.format(statistics.getAverageActiveTime()));
            out.append("ms/");
            out.append(decimalFormat.format(statistics.getAverageActiveCount()));

            log.info(out.toString());
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
 Revision 1.1  2003/01/31 14:39:08  billhorsman
 fixes for JDK 1.2 to remove dependency on JDK 1.3 Timer and javax.imageio

 Revision 1.1  2003/01/31 11:35:57  billhorsman
 improvements to servlet (including connection details)

 Revision 1.1  2003/01/31 00:28:57  billhorsman
 now handles multiple statistics

 */