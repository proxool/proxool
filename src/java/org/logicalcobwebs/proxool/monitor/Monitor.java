/*
 * This software is released under the Apache Software Licence. See
 * package.html for details. The latest version is available at
 * http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool.monitor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.logicalcobwebs.proxool.ConnectionPoolDefinitionIF;
import org.logicalcobwebs.proxool.ConnectionPoolStatisticsIF;
import org.logicalcobwebs.proxool.ProxoolException;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * Provides statistics about the performance of a pool.
 *
 * @version $Revision: 1.2 $, $Date: 2003/01/31 16:38:51 $
 * @author bill
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.7
 */
public class Monitor {

    private Log log;

    private Map statsRollers = new HashMap();

    /**
     * @param alias identifies the pool
     * @param definition see {@link org.logicalcobwebs.proxool.ConnectionPoolDefinitionIF#getStatistics definition}
     */
    public Monitor(String alias, String definition) throws ProxoolException {
        log = LogFactory.getLog("org.logicalcobwebs.proxool.stats." + alias);

        StringTokenizer st = new StringTokenizer(definition, ",");
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            statsRollers.put(token, new StatsRoller(alias, token));
        }

    }

    /**
     * Call this every time an active connection is returned to the pool
     * @param activeTime how long the connection was active
     */
    public void connectionReturned(long activeTime) {
        Iterator i = statsRollers.values().iterator();
        while (i.hasNext()) {
            StatsRoller statsRoller = (StatsRoller) i.next();
            statsRoller.connectionReturned(activeTime);
        }
    }

    /**
     * Call this every time a connection is refused
     */
    public void connectionRefused() {
        Iterator i = statsRollers.values().iterator();
        while (i.hasNext()) {
            StatsRoller statsRoller = (StatsRoller) i.next();
            statsRoller.connectionRefused();
        }
    }

    /**
     * Returns the most recent sample that has completed its period
     * @return sample (or null if no statistics are complete yet)
     */
    public StatisticsIF getStatistics(String token) {
        try {
            return ((StatsRoller)statsRollers.get(token)).getCompleteStatistics();
        } catch (NullPointerException e) {
            return null;
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
    public SnapshotIF getSnapshot(ConnectionPoolStatisticsIF cps, ConnectionPoolDefinitionIF cpd, Set connectionInfos) {
        Snapshot status = new Snapshot(new Date());

        status.setDateStarted(cps.getDateStarted());
        status.setActiveConnectionCount(cps.getActiveConnectionCount());
        status.setAvailableConnectionCount(cps.getAvailableConnectionCount());
        status.setOfflineConnectionCount(cps.getOfflineConnectionCount());
        status.setMaximumConnectionCount(cpd.getMaximumConnectionCount());
        status.setServedCount(cps.getConnectionsServedCount());
        status.setRefusedCount(cps.getConnectionsRefusedCount());
        status.setConnectionInfos(connectionInfos);

        return status;
    }

}


/*
 Revision history:
 $Log: Monitor.java,v $
 Revision 1.2  2003/01/31 16:38:51  billhorsman
 doc (and removing public modifier for classes where possible)

 Revision 1.1  2003/01/31 11:35:57  billhorsman
 improvements to servlet (including connection details)

 Revision 1.2  2003/01/31 00:28:57  billhorsman
 now handles multiple statistics

 Revision 1.1  2003/01/30 17:20:19  billhorsman
 fixes, improvements and doc

 */