/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool.admin;

import java.util.Date;

/**
 * Provides statistical performance information for a period ot
 * time.
 *
 * @version $Revision: 1.2 $, $Date: 2003/03/03 11:11:59 $
 * @author bill
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.7
 */
public interface StatisticsIF {

    /**
     * The length of time this sample represents,
     * @return period (milliseconds)
     */
    long getPeriod();

    /**
     * The average time that each connection spent active.
     * @return averageActiveTime (milliseconds)
     */
    double getAverageActiveTime();

    /**
     * The average number of active connections,
     * @return averageActiveCount
     */
    double getAverageActiveCount();

    /**
     * The number of connections served during this sample.
     * @return servedCount
     */
    long getServedCount();

    /**
     * The number of connections refused during this sample.
     * @return refusedCount
     */
    long getRefusedCount();

    /**
     * When this sample started.
     * @return startDate
     * @see #getStopDate
     * @see #getPeriod
     */
    Date getStartDate();

    /**
     * When this sample stopped
     * @return stopDate
     * @see #getStartDate
     * @see #getPeriod
     */
    Date getStopDate();

    /**
     * The rate at which we have served connections
     * @return servedPerSecond
     */
    double getServedPerSecond();

    /**
     * The rate at which we have refused connections
     * @return refusedPerSecond
     */
    double getRefusedPerSecond();

}


/*
 Revision history:
 $Log: StatisticsIF.java,v $
 Revision 1.2  2003/03/03 11:11:59  billhorsman
 fixed licence

 Revision 1.1  2003/02/19 23:36:51  billhorsman
 renamed monitor package to admin

 Revision 1.3  2003/01/31 16:53:23  billhorsman
 checkstyle

 Revision 1.2  2003/01/31 16:38:54  billhorsman
 doc (and removing public modifier for classes where possible)

 Revision 1.1  2003/01/31 11:35:57  billhorsman
 improvements to servlet (including connection details)

 Revision 1.1  2003/01/30 17:20:12  billhorsman
 fixes, improvements and doc

 */