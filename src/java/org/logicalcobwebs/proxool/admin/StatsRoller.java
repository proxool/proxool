/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool.admin;

import org.logicalcobwebs.logging.Log;
import org.logicalcobwebs.logging.LogFactory;
import org.logicalcobwebs.proxool.ProxoolException;
import org.logicalcobwebs.proxool.util.ReadWriteLock;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Responsbile for a single set of statistics. It rolls over to a new set
 * whenever it should. It provides access to the latest complete set
 * when it is available.
 *
 * @version $Revision: 1.5 $, $Date: 2003/03/06 21:56:27 $
 * @author bill
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.7
 */
class StatsRoller {

    private static final Log LOG = LogFactory.getLog(StatsRoller.class);

    private ReadWriteLock readWriteLock = new ReadWriteLock();

    private Statistics completeStatistics;

    private Statistics currentStatistics;

    private Calendar nextRollDate;

    private int period;

    private int units;

    private Timer timer = new Timer(true);

    private CompositeStatisticsListener compositeStatisticsListener;

    private String alias;

    public StatsRoller(String alias, CompositeStatisticsListener compositeStatisticsListener, String token) throws ProxoolException {
        this.alias = alias;
        this.compositeStatisticsListener = compositeStatisticsListener;

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

        LOG.debug("Collecting first statistics for '" + token + "' at " + nextRollDate.getTime());
        currentStatistics = new Statistics(now.getTime());

        // Automatically trigger roll if no activity
        TimerTask tt = new TimerTask() {
            public void run() {
                roll();
            }
        };
        timer.schedule(tt, 1000, 5000);
    }

    /**
     * Cancels the timer that outputs the stats
     */
    protected void cancel() {
        timer.cancel();
    }

    private void roll() {
        try {
            readWriteLock.aquireWrite();
            if (!isCurrent()) {
                currentStatistics.setStopDate(nextRollDate.getTime());
                completeStatistics = currentStatistics;
                currentStatistics = new Statistics(nextRollDate.getTime());
                nextRollDate.add(units, period);
                compositeStatisticsListener.statistics(alias, completeStatistics);
            }
        } catch (InterruptedException e) {
            LOG.error("Unable to roll statistics log", e);
        } finally {
            readWriteLock.release();
        }
    }

    private boolean isCurrent() {
        return (System.currentTimeMillis() < nextRollDate.getTime().getTime());
    }

    /**
     * @see org.logicalcobwebs.proxool.admin.Admin#connectionReturned
     */
    public void connectionReturned(long activeTime) {
        if (!isCurrent()) {
            roll();
        }
        try {
            readWriteLock.aquireRead();
            currentStatistics.connectionReturned(activeTime);
        } catch (InterruptedException e) {
            LOG.error("Unable to log connectionReturned", e);
        } finally {
            readWriteLock.release();
        }
    }

    /**
     * @see org.logicalcobwebs.proxool.admin.Admin#connectionRefused
     */
    public void connectionRefused() {
        if (!isCurrent()) {
            roll();
        }
        try {
            readWriteLock.aquireRead();
            currentStatistics.connectionRefused();
        } catch (InterruptedException e) {
            LOG.error("Unable to log connectionRefused", e);
        } finally {
            readWriteLock.release();
        }
    }

    /**
     *
     * @return
     */
    public Statistics getCompleteStatistics() {
        try {
            readWriteLock.aquireRead();
            return completeStatistics;
        } catch (InterruptedException e) {
            LOG.error("Couldn't read statistics", e);
            return null;
        } finally {
            readWriteLock.release();
        }
    }
}


/*
 Revision history:
 $Log: StatsRoller.java,v $
 Revision 1.5  2003/03/06 21:56:27  billhorsman
 remove too much debug

 Revision 1.4  2003/03/06 12:44:02  billhorsman
 add readWriteLock

 Revision 1.3  2003/03/03 11:11:59  billhorsman
 fixed licence

 Revision 1.2  2003/02/28 12:42:45  billhorsman
 removed unnecessary sleep in timer

 Revision 1.1  2003/02/19 23:36:51  billhorsman
 renamed monitor package to admin

 Revision 1.11  2003/02/08 14:27:51  chr32
 Style fixes.
 Also tried to fix the dublicate linebreaks in the logging classes.

 Revision 1.10  2003/02/07 14:16:46  billhorsman
 support for StatisticsListenerIF

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