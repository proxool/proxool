/*
 * This software is released under the Apache Software Licence. See
 * package.html for details. The latest version is available at
 * http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool.monitor;

import org.logicalcobwebs.proxool.ConnectionInfoIF;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Date;
import java.util.Set;
import java.util.TreeSet;
import java.util.Comparator;

/**
 * Implementation of SnapshotIF
 *
 * @version $Revision: 1.1 $, $Date: 2003/01/31 11:35:57 $
 * @author bill
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.7
 */
public class Snapshot implements SnapshotIF {

    private static final Log LOG = LogFactory.getLog(Snapshot.class);

    private Date dateStarted;

    private long servedCount;

    private long refusedCount;

    private int activeConnectionCount;

    private int availableConnectionCount;

    private int offlineConnectionCount;

    private int maximumConnectionCount;

    private Date snapshotDate;

    private Set connectionInfos;

    /**
     * @param snapshotDate see {@link org.logicalcobwebs.proxool.monitor.SnapshotIF#getSnapshotDate}
     */
    public Snapshot(Date snapshotDate) {
        this.snapshotDate = snapshotDate;
    }

    /**
     * @see org.logicalcobwebs.proxool.monitor.SnapshotIF#getDateStarted
     */
    public Date getDateStarted() {
        return dateStarted;
    }

    /**
     * @see org.logicalcobwebs.proxool.monitor.SnapshotIF#getDateStarted
     */
    public void setDateStarted(Date dateStarted) {
        this.dateStarted = dateStarted;
    }

    /**
     * @see org.logicalcobwebs.proxool.monitor.SnapshotIF#getServedCount
     */
    public long getServedCount() {
        return servedCount;
    }

    /**
     * @see org.logicalcobwebs.proxool.monitor.SnapshotIF#getServedCount
     */
    public void setServedCount(long servedCount) {
        this.servedCount = servedCount;
    }

    /**
     * @see org.logicalcobwebs.proxool.monitor.SnapshotIF#getRefusedCount
     */
    public long getRefusedCount() {
        return refusedCount;
    }

    /**
     * @see org.logicalcobwebs.proxool.monitor.SnapshotIF#getRefusedCount
     */
    public void setRefusedCount(long refusedCount) {
        this.refusedCount = refusedCount;
    }

    /**
     * @see org.logicalcobwebs.proxool.monitor.SnapshotIF#getActiveConnectionCount
     */
    public int getActiveConnectionCount() {
        return activeConnectionCount;
    }

    /**
     * @see org.logicalcobwebs.proxool.monitor.SnapshotIF#getActiveConnectionCount
     */
    public void setActiveConnectionCount(int activeConnectionCount) {
        this.activeConnectionCount = activeConnectionCount;
    }

    /**
     * @see org.logicalcobwebs.proxool.monitor.SnapshotIF#getAvailableConnectionCount
     */
    public int getAvailableConnectionCount() {
        return availableConnectionCount;
    }

    /**
     * @see org.logicalcobwebs.proxool.monitor.SnapshotIF#getAvailableConnectionCount
     */
    public void setAvailableConnectionCount(int availableConnectionCount) {
        this.availableConnectionCount = availableConnectionCount;
    }

    /**
     * @see org.logicalcobwebs.proxool.monitor.SnapshotIF#getOfflineConnectionCount
     */
    public int getOfflineConnectionCount() {
        return offlineConnectionCount;
    }

    /**
     * @see org.logicalcobwebs.proxool.monitor.SnapshotIF#getOfflineConnectionCount
     */
    public void setOfflineConnectionCount(int offlineConnectionCount) {
        this.offlineConnectionCount = offlineConnectionCount;
    }

    /**
     * @see org.logicalcobwebs.proxool.monitor.SnapshotIF#getMaximumConnectionCount
     */
    public int getMaximumConnectionCount() {
        return maximumConnectionCount;
    }

    /**
     * @see org.logicalcobwebs.proxool.monitor.SnapshotIF#getMaximumConnectionCount
     */
    public void setMaximumConnectionCount(int maximumConnectionCount) {
        this.maximumConnectionCount = maximumConnectionCount;
    }

    /**
     * @see org.logicalcobwebs.proxool.monitor.SnapshotIF#getSnapshotDate
     */
    public Date getSnapshotDate() {
        return snapshotDate;
    }

    /**
     * @see SnapshotIF#getConnectionInfos
     */
    public ConnectionInfoIF[] getConnectionInfos() {
        return (ConnectionInfoIF[]) connectionInfos.toArray(new ConnectionInfoIF[connectionInfos.size()]);
    }

    /**
     * @see SnapshotIF#getConnectionInfos
     */
    public void setConnectionInfos(Set connectionInfos) {
        this.connectionInfos = connectionInfos;
    }

    /**
     * @see SnapshotIF#isDetail
     */
    public boolean isDetail() {
        return connectionInfos != null;
    }

}


/*
 Revision history:
 $Log: Snapshot.java,v $
 Revision 1.1  2003/01/31 11:35:57  billhorsman
 improvements to servlet (including connection details)

 Revision 1.1  2003/01/30 17:20:09  billhorsman
 fixes, improvements and doc

 */