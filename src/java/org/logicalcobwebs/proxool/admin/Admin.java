/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool.admin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.logicalcobwebs.proxool.ConnectionPoolDefinitionIF;
import org.logicalcobwebs.proxool.ConnectionPoolStatisticsIF;
import org.logicalcobwebs.proxool.ProxoolException;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * Provides statistics about the performance of a pool.
 *
 * @version $Revision: 1.9 $, $Date: 2006/01/18 14:39:57 $
 * @author bill
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.7
 */
public class Admin {

    private static final Log LOG = LogFactory.getLog(Admin.class);

    private Log log;

    private Map statsRollers = new HashMap();

    private CompositeStatisticsListener  compositeStatisticsListener  = new CompositeStatisticsListener();

    /**
     * @param definition gives access to pool definition
     * @param definition see {@link org.logicalcobwebs.proxool.ConnectionPoolDefinitionIF#getStatistics definition}
     */
    public Admin(ConnectionPoolDefinitionIF definition) throws ProxoolException {
        log = LogFactory.getLog("org.logicalcobwebs.proxool.stats." + definition.getAlias());

        StringTokenizer st = new StringTokenizer(definition.getStatistics(), ",");
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            statsRollers.put(token, new StatsRoller(definition.getAlias(), compositeStatisticsListener, token));
        }

        if (definition.getStatisticsLogLevel() != null) {
            compositeStatisticsListener.addListener(new StatisticsLogger(log, definition.getStatisticsLogLevel()));
        }

    }

    public void addStatisticsListener(StatisticsListenerIF statisticsListener) {
        this.compositeStatisticsListener.addListener(statisticsListener);
    }


    /**
     * Call this every time an active connection is returned to the pool
     * @param activeTime how long the connection was active
     */
    public void connectionReturned(long activeTime) {
        try {
            Iterator i = statsRollers.values().iterator();
            while (i.hasNext()) {
                StatsRoller statsRoller = (StatsRoller) i.next();
                statsRoller.connectionReturned(activeTime);
            }
        } catch (Throwable e) {
            LOG.error("Stats connectionReturned call failed. Ignoring.", e);
        }
    }

    /**
     * Call this every time a connection is refused
     */
    public void connectionRefused() {
        try {
            Iterator i = statsRollers.values().iterator();
            while (i.hasNext()) {
                StatsRoller statsRoller = (StatsRoller) i.next();
                statsRoller.connectionRefused();
            }
        } catch (Exception e) {
            LOG.error("Stats connectionRefused call failed. Ignoring.", e);
        }
    }

    /**
     * Returns the most recent sample that has completed its period
     * @return sample (or null if no statistics are complete yet)
     */
    public StatisticsIF getStatistics(String token) {
        try {
            return ((StatsRoller) statsRollers.get(token)).getCompleteStatistics();
        } catch (NullPointerException e) {
            return null;
        }
    }

    /**
     * Cancels the timer that outputs the stats
     */
    public void cancelAll() {
        Iterator i = statsRollers.values().iterator();
        while (i.hasNext()) {
            StatsRoller statsRoller = (StatsRoller) i.next();
            statsRoller.cancel();
        }
    }

    public StatisticsIF[] getStatistics() {
        List statistics = new Vector();
        Iterator i = statsRollers.values().iterator();
        while (i.hasNext()) {
            StatsRoller statsRoller = (StatsRoller) i.next();
            StatisticsIF s = statsRoller.getCompleteStatistics();
            if (s != null) {
                statistics.add(s);
            }
        }
        return (StatisticsIF[]) statistics.toArray(new StatisticsIF[statistics.size()]);
    }

    /**
     * Get a new snapshot
     * @param cps used to help populate the snapshot
     * @param cpd used to help populate the snapshot
     * @return snapshot
     */
    public static SnapshotIF getSnapshot(ConnectionPoolStatisticsIF cps, ConnectionPoolDefinitionIF cpd, Collection connectionInfos) {
        Snapshot s = new Snapshot(new Date());

        s.setDateStarted(cps.getDateStarted());
        s.setActiveConnectionCount(cps.getActiveConnectionCount());
        s.setAvailableConnectionCount(cps.getAvailableConnectionCount());
        s.setOfflineConnectionCount(cps.getOfflineConnectionCount());
        s.setMaximumConnectionCount(cpd.getMaximumConnectionCount());
        s.setServedCount(cps.getConnectionsServedCount());
        s.setRefusedCount(cps.getConnectionsRefusedCount());
        s.setConnectionInfos(connectionInfos);
        s.setConnectionCount(cps.getConnectionCount());

        /*
        if (s.getActiveConnectionCount() != getCount(s.getConnectionInfos(), ConnectionInfoIF.STATUS_ACTIVE)) {
            LOG.error("activeCount disparity: " + s.getActiveConnectionCount() + " != " + getCount(s.getConnectionInfos(), ConnectionInfoIF.STATUS_ACTIVE));
        }
        if (s.getAvailableConnectionCount() != getCount(s.getConnectionInfos(), ConnectionInfoIF.STATUS_AVAILABLE)) {
            LOG.error("activeCount disparity: " + s.getAvailableConnectionCount() + " != " + getCount(s.getConnectionInfos(), ConnectionInfoIF.STATUS_AVAILABLE));
        }
        if (s.getOfflineConnectionCount() != getCount(s.getConnectionInfos(), ConnectionInfoIF.STATUS_OFFLINE)) {
            LOG.error("offlineCount disparity: " + s.getOfflineConnectionCount() + " != " + getCount(s.getConnectionInfos(), ConnectionInfoIF.STATUS_OFFLINE));
        }
        */

        return s;
    }

}


/*
 Revision history:
 $Log: Admin.java,v $
 Revision 1.9  2006/01/18 14:39:57  billhorsman
 Unbundled Jakarta's Commons Logging.

 Revision 1.8  2005/10/02 12:32:01  billhorsman
 Make connectionCount available to statistics

 Revision 1.7  2003/10/27 20:26:19  billhorsman
 connectionReturned() and connectionRefused() calls will now log any errors and continue rather than
 possibly throwing RuntimeExceptions back to the caller. In principle, stats should not cause problems
 to the core code. (No evidence of this happening - but it's more robust now.)

 Revision 1.6  2003/08/30 14:54:04  billhorsman
 Checkstyle

 Revision 1.5  2003/03/11 14:51:55  billhorsman
 more concurrency fixes relating to snapshots

 Revision 1.4  2003/03/10 23:43:14  billhorsman
 reapplied checkstyle that i'd inadvertently let
 IntelliJ change...

 Revision 1.3  2003/03/10 15:26:50  billhorsman
 refactoringn of concurrency stuff (and some import
 optimisation)

 Revision 1.2  2003/03/03 11:11:58  billhorsman
 fixed licence

 Revision 1.1  2003/02/19 23:36:51  billhorsman
 renamed monitor package to admin

 Revision 1.8  2003/02/07 15:08:51  billhorsman
 removed redundant accessor

 Revision 1.7  2003/02/07 14:16:45  billhorsman
 support for StatisticsListenerIF

 Revision 1.6  2003/02/06 17:41:05  billhorsman
 now uses imported logging

 Revision 1.5  2003/02/05 00:20:27  billhorsman
 getSnapshot is now static (because it can be)

 Revision 1.4  2003/02/04 15:59:49  billhorsman
 finalize now shuts down StatsRoller timer

 Revision 1.3  2003/01/31 16:53:21  billhorsman
 checkstyle

 Revision 1.2  2003/01/31 16:38:51  billhorsman
 doc (and removing public modifier for classes where possible)

 Revision 1.1  2003/01/31 11:35:57  billhorsman
 improvements to servlet (including connection details)

 Revision 1.2  2003/01/31 00:28:57  billhorsman
 now handles multiple statistics

 Revision 1.1  2003/01/30 17:20:19  billhorsman
 fixes, improvements and doc

 */