/*
 * This software is released under the Apache Software Licence. See
 * package.html for details. The latest version is available at
 * http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool.admin;

import org.logicalcobwebs.proxool.ProxoolFacade;
import org.logicalcobwebs.proxool.ResultMonitor;
import org.logicalcobwebs.proxool.ProxoolException;

/**
 * A ResultMonitor specifically for Snapshots
 *
 * @version $Revision: 1.3 $, $Date: 2003/03/01 16:38:40 $
 * @author bill
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.8
 */
public class StatisticsResultMonitor extends ResultMonitor {

    private StatisticsIF statistics;

    private StatisticsIF oldStatistics;

    private String alias;

    private String token;

    /**
     * @param alias so we can lookup the latest {@link StatisticsIF statistics}
     * @param token so we can lookup the latest {@link StatisticsIF statistics}
     */
    public StatisticsResultMonitor(String alias, String token) {
        this.alias = alias;
        this.token = token;
    }

    /**
     * waits for statistics
     * @return {@link #SUCCESS} or {@link #TIMEOUT}
     * @throws Exception if anything goes wrong
     */
    public boolean check() throws Exception {
        statistics= ProxoolFacade.getStatistics(alias, token);
        if (statistics == null) {
            return false;
        } else if (oldStatistics == null) {
            return true;
        } else {
            return (!statistics.getStartDate().equals(oldStatistics.getStartDate()));
        }
    }

    public int getResult() throws ProxoolException {
        oldStatistics = statistics;
        return super.getResult();
    }

    /**
     * Get the statistics used in the most recent {@link #check check}
     * @return snapshot
     */
    public StatisticsIF getStatistics() {
        return statistics;
    }
}


/*
 Revision history:
 $Log: StatisticsResultMonitor.java,v $
 Revision 1.3  2003/03/01 16:38:40  billhorsman
 fix

 Revision 1.2  2003/03/01 16:18:31  billhorsman
 fix

 Revision 1.1  2003/03/01 16:07:26  billhorsman
 helper

 Revision 1.3  2003/03/01 15:27:24  billhorsman
 checkstyle

 Revision 1.2  2003/03/01 15:22:50  billhorsman
 doc

 Revision 1.1  2003/03/01 15:14:14  billhorsman
 new ResultMonitor to help cope with test threads

 */