/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool.admin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.logicalcobwebs.proxool.ConnectionInfoIF;

import java.util.Collection;
import java.util.Date;

/**
 * Implementation of SnapshotIF
 *
 * @version $Revision: 1.5 $, $Date: 2006/01/18 14:39:57 $
 * @author bill
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.7
 */
class Snapshot implements SnapshotIF {

    private static final Log LOG = LogFactory.getLog(Snapshot.class);

    private Date dateStarted;

    private long servedCount;

    private long refusedCount;

    private int activeConnectionCount;

    private int availableConnectionCount;

    private int offlineConnectionCount;

    private int maximumConnectionCount;

    private Date snapshotDate;

    private Collection connectionInfos;

    private long connectionCount;

    /**
     * @param snapshotDate see {@link org.logicalcobwebs.proxool.admin.SnapshotIF#getSnapshotDate}
     */
    public Snapshot(Date snapshotDate) {
        this.snapshotDate = snapshotDate;
    }

    /**
     * @see org.logicalcobwebs.proxool.admin.SnapshotIF#getDateStarted
     */
    public Date getDateStarted() {
        return dateStarted;
    }

    /**
     * @see org.logicalcobwebs.proxool.admin.SnapshotIF#getDateStarted
     */
    public void setDateStarted(Date dateStarted) {
        this.dateStarted = dateStarted;
    }

    /**
     * @see org.logicalcobwebs.proxool.admin.SnapshotIF#getServedCount
     */
    public long getServedCount() {
        return servedCount;
    }

    /**
     * @see org.logicalcobwebs.proxool.admin.SnapshotIF#getServedCount
     */
    public void setServedCount(long servedCount) {
        this.servedCount = servedCount;
    }

    /**
     * @see org.logicalcobwebs.proxool.admin.SnapshotIF#getRefusedCount
     */
    public long getRefusedCount() {
        return refusedCount;
    }

    /**
     * @see org.logicalcobwebs.proxool.admin.SnapshotIF#getRefusedCount
     */
    public void setRefusedCount(long refusedCount) {
        this.refusedCount = refusedCount;
    }

    /**
     * @see org.logicalcobwebs.proxool.admin.SnapshotIF#getActiveConnectionCount
     */
    public int getActiveConnectionCount() {
        return activeConnectionCount;
    }

    /**
     * @see org.logicalcobwebs.proxool.admin.SnapshotIF#getActiveConnectionCount
     */
    public void setActiveConnectionCount(int activeConnectionCount) {
        this.activeConnectionCount = activeConnectionCount;
    }

    /**
     * @see org.logicalcobwebs.proxool.admin.SnapshotIF#getAvailableConnectionCount
     */
    public int getAvailableConnectionCount() {
        return availableConnectionCount;
    }

    /**
     * @see org.logicalcobwebs.proxool.admin.SnapshotIF#getAvailableConnectionCount
     */
    public void setAvailableConnectionCount(int availableConnectionCount) {
        this.availableConnectionCount = availableConnectionCount;
    }

    /**
     * @see org.logicalcobwebs.proxool.admin.SnapshotIF#getOfflineConnectionCount
     */
    public int getOfflineConnectionCount() {
        return offlineConnectionCount;
    }

    /**
     * @see org.logicalcobwebs.proxool.admin.SnapshotIF#getOfflineConnectionCount
     */
    public void setOfflineConnectionCount(int offlineConnectionCount) {
        this.offlineConnectionCount = offlineConnectionCount;
    }

    /**
     * @see org.logicalcobwebs.proxool.admin.SnapshotIF#getMaximumConnectionCount
     */
    public int getMaximumConnectionCount() {
        return maximumConnectionCount;
    }

    /**
     * @see org.logicalcobwebs.proxool.admin.SnapshotIF#getMaximumConnectionCount
     */
    public void setMaximumConnectionCount(int maximumConnectionCount) {
        this.maximumConnectionCount = maximumConnectionCount;
    }

    /**
     * @see org.logicalcobwebs.proxool.admin.SnapshotIF#getSnapshotDate
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
    public void setConnectionInfos(Collection connectionInfos) {
        this.connectionInfos = connectionInfos;
    }

    /**
     * @see SnapshotIF#getConnectionInfo
     */
    public ConnectionInfoIF getConnectionInfo(long id) {
        ConnectionInfoIF connectionInfo = null;
        ConnectionInfoIF[] connectionInfos = getConnectionInfos();
        for (int i = 0; i < connectionInfos.length; i++) {
            if (connectionInfos[i].getId() == id) {
                connectionInfo = connectionInfos[i];
            }
        }
        return connectionInfo;
    }

    /**
     * @see SnapshotIF#isDetail
     */
    public boolean isDetail() {
        return connectionInfos != null;
    }

    public long getConnectionCount() {
        return connectionCount;
    }

    public void setConnectionCount(long connectionCount) {
        this.connectionCount = connectionCount;
    }
}


/*
 Revision history:
 $Log: Snapshot.java,v $
 Revision 1.5  2006/01/18 14:39:57  billhorsman
 Unbundled Jakarta's Commons Logging.

 Revision 1.4  2005/10/02 12:32:01  billhorsman
 Make connectionCount available to statistics

 Revision 1.3  2003/03/10 15:26:51  billhorsman
 refactoringn of concurrency stuff (and some import
 optimisation)

 Revision 1.2  2003/03/03 11:11:59  billhorsman
 fixed licence

 Revision 1.1  2003/02/19 23:36:51  billhorsman
 renamed monitor package to admin

 Revision 1.4  2003/02/12 12:28:28  billhorsman
 added url, proxyHashcode and delegateHashcode to
 ConnectionInfoIF

 Revision 1.3  2003/02/06 17:41:06  billhorsman
 now uses imported logging

 Revision 1.2  2003/01/31 16:38:53  billhorsman
 doc (and removing public modifier for classes where possible)

 Revision 1.1  2003/01/31 11:35:57  billhorsman
 improvements to servlet (including connection details)

 Revision 1.1  2003/01/30 17:20:09  billhorsman
 fixes, improvements and doc

 */