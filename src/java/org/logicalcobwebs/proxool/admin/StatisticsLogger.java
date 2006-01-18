/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool.admin;

import org.apache.commons.logging.Log;
import org.logicalcobwebs.proxool.ProxoolConstants;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

/**
 * Listens to statistics and logs them
 * @version $Revision: 1.3 $, $Date: 2006/01/18 14:39:57 $
 * @author bill
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.7
 */
public class StatisticsLogger implements StatisticsListenerIF {

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00");

    private static final DateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");

    private Log log;

    private String logLevel;

    public StatisticsLogger(Log log, String logLevel) {
        this.log = log;
        this.logLevel = logLevel;
    }

    public void statistics(String alias, StatisticsIF statistics) {

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
}


/*
 Revision history:
 $Log: StatisticsLogger.java,v $
 Revision 1.3  2006/01/18 14:39:57  billhorsman
 Unbundled Jakarta's Commons Logging.

 Revision 1.2  2003/03/03 11:11:59  billhorsman
 fixed licence

 Revision 1.1  2003/02/19 23:36:51  billhorsman
 renamed monitor package to admin

 Revision 1.1  2003/02/07 14:16:46  billhorsman
 support for StatisticsListenerIF

 */