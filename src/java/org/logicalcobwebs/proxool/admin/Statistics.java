/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool.admin;

import java.util.Date;

/**
 * Implementation of StatisticsIF
 *
 * @version $Revision: 1.2 $, $Date: 2003/03/03 11:11:59 $
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
     * @param startDate see {@link org.logicalcobwebs.proxool.admin.StatisticsIF#getStartDate}
     */
    protected Statistics(Date startDate) {
        this.startDate = startDate;
    }

    /**
     * @see org.logicalcobwebs.proxool.admin.Admin#connectionReturned
     */
    protected void connectionReturned(long activeTime) {
        totalActiveTime += activeTime;
        servedCount++;
    }

    /**
     * @see org.logicalcobwebs.proxool.admin.Admin#connectionRefused
     */
    protected void connectionRefused() {
        refusedCount++;
    }

    /**
     * @see org.logicalcobwebs.proxool.admin.StatisticsIF#getStopDate
     */
    protected void setStopDate(Date stopDate) {
        this.stopDate = stopDate;
    }

    /**
     * @see org.logicalcobwebs.proxool.admin.StatisticsIF#getStartDate
     */
    public Date getStartDate() {
        return startDate;
    }

    /**
     * @see org.logicalcobwebs.proxool.admin.StatisticsIF#getStopDate
     */
    public Date getStopDate() {
        return stopDate;
    }

    /**
     * @see org.logicalcobwebs.proxool.admin.StatisticsIF#getPeriod
     */
    public long getPeriod() {
        if (stopDate != null) {
            return stopDate.getTime() - startDate.getTime();
        } else {
            return System.currentTimeMillis() - startDate.getTime();
        }
    }

    /**
     * @see org.logicalcobwebs.proxool.admin.StatisticsIF#getAverageActiveTime
     */
    public double getAverageActiveTime() {
        if (servedCount > 0) {
            return ((double) totalActiveTime / (double) servedCount);
        } else {
            return 0.0;
        }
    }

    /**
     * @see org.logicalcobwebs.proxool.admin.StatisticsIF#getAverageActiveCount
     */
    public double getAverageActiveCount() {
        return (double) totalActiveTime / (double) getPeriod();
    }

    /**
     * @see org.logicalcobwebs.proxool.admin.StatisticsIF#getServedPerSecond
     */
    public double getServedPerSecond() {
        return (double) servedCount / ((double) getPeriod() / 1000.0);
    }

    /**
     * @see org.logicalcobwebs.proxool.admin.StatisticsIF#getRefusedPerSecond
     */
    public double getRefusedPerSecond() {
        return (double) refusedCount / ((double) getPeriod() / 1000.0);
    }

    /**
     * @see org.logicalcobwebs.proxool.admin.StatisticsIF#getServedCount
     */
    public long getServedCount() {
        return servedCount;
    }

    /**
     * @see org.logicalcobwebs.proxool.admin.StatisticsIF#getRefusedCount
     */
    public long getRefusedCount() {
        return refusedCount;
    }

}


/*
 Revision history:
 $Log: Statistics.java,v $
 Revision 1.2  2003/03/03 11:11:59  billhorsman
 fixed licence

 Revision 1.1  2003/02/19 23:36:51  billhorsman
 renamed monitor package to admin

 Revision 1.3  2003/01/31 16:53:22  billhorsman
 checkstyle

 Revision 1.2  2003/01/31 16:38:53  billhorsman
 doc (and removing public modifier for classes where possible)

 Revision 1.1  2003/01/31 11:35:57  billhorsman
 improvements to servlet (including connection details)

 Revision 1.1  2003/01/30 17:20:13  billhorsman
 fixes, improvements and doc

 */