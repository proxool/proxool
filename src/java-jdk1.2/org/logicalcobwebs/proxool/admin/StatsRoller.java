/*
 * This software is released under the Apache Software Licence. See
 * package.html for details. The latest version is available at
 * http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool.admin;

import org.logicalcobwebs.proxool.ProxoolException;
import org.logicalcobwebs.logging.Log;
import org.logicalcobwebs.logging.LogFactory;
import org.logicalcobwebs.concurrent.WriterPreferenceReadWriteLock;

import java.util.Calendar;

/**
 * Responsbile for a single set of statistics. It rolls over to a new set
 * whenever it should. It provides access to the latest complete set
 * when it is available.
 *
 * @version $Revision: 1.3 $, $Date: 2003/03/11 00:49:01 $
 * @author bill
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.7
 */
public class StatsRoller {

    private static final Log LOG = LogFactory.getLog(StatsRoller.class);

    private WriterPreferenceReadWriteLock readWriteLock = new WriterPreferenceReadWriteLock();

    private Statistics completeStatistics;

    private Statistics currentStatistics;

    private Calendar nextRollDate;

    private int period;

    private int units;

    private CompositeStatisticsListener compositeStatisticsListener;

    private String alias;

    private boolean running = true;

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

        final Thread t = new Thread() {

            public void run() {
                while (running) {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        LOG.debug("Interruption", e);
                    }
                    roll();
                }
            }

        };
        t.setDaemon(true);
        t.start();

    }

    /**
     * Cancels the timer that outputs the stats
     */
    protected void cancel() {
        running = false;
    }

    private void roll() {
        try {
            readWriteLock.writeLock().acquire();
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
            readWriteLock.writeLock().release();
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
            readWriteLock.readLock().acquire();
            currentStatistics.connectionReturned(activeTime);
            LOG.debug("Logging connectionReturned to stats starting at " + currentStatistics.getStartDate());
        } catch (InterruptedException e) {
            LOG.error("Unable to log connectionReturned", e);
        } finally {
            readWriteLock.readLock().release();
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
            readWriteLock.readLock().acquire();
            currentStatistics.connectionRefused();
        } catch (InterruptedException e) {
            LOG.error("Unable to log connectionRefused", e);
        } finally {
            readWriteLock.readLock().release();
        }
    }

    /**
     *
     * @return
     */
    public Statistics getCompleteStatistics() {
        try {
            readWriteLock.readLock().acquire();
            return completeStatistics;
        } catch (InterruptedException e) {
            LOG.error("Couldn't read statistics", e);
            return null;
        } finally {
            readWriteLock.readLock().release();
        }
    }
    
}


/*
 Revision history:
 $Log: StatsRoller.java,v $
 Revision 1.3  2003/03/11 00:49:01  billhorsman
 switched to concurrent package

 Revision 1.2  2003/03/06 13:06:14  billhorsman
 replicated readWriteLock fix just made to src/java

 Revision 1.1  2003/02/20 00:33:14  billhorsman
 renamed monitor package -> admin

 Revision 1.7  2003/02/11 00:32:12  billhorsman
 added daemon to roll stats

 Revision 1.6  2003/02/08 14:27:52  chr32
 Style fixes.
 Also tried to fix the dublicate linebreaks in the logging classes.

 Revision 1.5  2003/02/08 00:56:03  billhorsman
 merge changes from main source

 Revision 1.4  2003/02/06 23:48:10  billhorsman
 Use imported logging

 Revision 1.3  2003/02/04 16:01:09  billhorsman
 implement empty cancel() method

 Revision 1.2  2003/01/31 16:53:29  billhorsman
 checkstyle

 Revision 1.1  2003/01/31 14:39:08  billhorsman
 fixes for JDK 1.2 to remove dependency on JDK 1.3 Timer and javax.imageio

 Revision 1.1  2003/01/31 11:35:57  billhorsman
 improvements to servlet (including connection details)

 Revision 1.1  2003/01/31 00:28:57  billhorsman
 now handles multiple statistics

 */
