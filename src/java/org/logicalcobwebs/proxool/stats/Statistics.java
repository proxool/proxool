/**
 * Clever Little Trader
 *
 * Jubilee Group and Logical Cobwebs, 2002
 */
package org.logicalcobwebs.proxool.stats;

import java.util.Date;

/**
 * Implementation of StatisticsIF
 *
 * @version $Revision: 1.1 $, $Date: 2003/01/30 17:20:13 $
 * @author bill
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.7
 */
class Statistics implements StatisticsIF {

    private Date startDate;

    private Date stopDate;

    private long servedCount;

    private long refusedCount;

    private long totalActiveTime;

    /**
     * @param startDate see {@link StatisticsIF#getStartDate}
     */
    protected Statistics(Date startDate) {
        this.startDate = startDate;
    }

    /**
     * @see Stats#connectionReturned
     */
    protected void connectionReturned(long activeTime) {
        totalActiveTime += activeTime;
        servedCount++;
    }

    /**
     * @see Stats#connectionRefused
     */
    protected void connectionRefused() {
        refusedCount++;
    }

    /**
     * @see StatisticsIF#getStopDate
     */
    protected void setStopDate(Date stopDate) {
        this.stopDate = stopDate;
    }

    /**
     * @see StatisticsIF#getStartDate
     */
    public Date getStartDate() {
        return startDate;
    }

    /**
     * @see StatisticsIF#getStopDate
     */
    public Date getStopDate() {
        return stopDate;
    }

    /**
     * @see StatisticsIF#getPeriod
     */
    public long getPeriod() {
        if (stopDate != null) {
            return stopDate.getTime() - startDate.getTime();
        } else {
            return System.currentTimeMillis() - startDate.getTime();
        }
    }

    /**
     * @see StatisticsIF#getAverageActiveTime
     */
    public double getAverageActiveTime() {
        if (servedCount > 0) {
            return ((double) totalActiveTime/(double) servedCount);
        } else {
            return 0.0;
        }
    }

    /**
     * @see StatisticsIF#getAverageActiveCount
     */
    public double getAverageActiveCount() {
        return (double) totalActiveTime/(double) getPeriod();
    }

    /**
     * @see StatisticsIF#getServedPerSecond
     */
    public double getServedPerSecond() {
        return (double) servedCount/((double) getPeriod() / 1000.0);
    }

    /**
     * @see StatisticsIF#getRefusedPerSecond
     */
    public double getRefusedPerSecond() {
        return (double) refusedCount/((double) getPeriod() / 1000.0);
    }

    /**
     * @see StatisticsIF#getServedCount
     */
    public long getServedCount() {
        return servedCount;
    }

    /**
     * @see StatisticsIF#getRefusedCount
     */
    public long getRefusedCount() {
        return refusedCount;
    }

}


/*
 Revision history:
 $Log: Statistics.java,v $
 Revision 1.1  2003/01/30 17:20:13  billhorsman
 fixes, improvements and doc

 */