/*
 * This software is released under the Apache Software Licence. See
 * package.html for details. The latest version is available at
 * http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool.stats;

import java.util.Date;

/**
 * Implementation of SnapshotIF
 *
 * @version $Revision: 1.1 $, $Date: 2003/01/30 17:20:09 $
 * @author bill
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.7
 */
public class Snapshot implements SnapshotIF {

    private Date dateStarted;

    private long servedCount;

    private long refusedCount;

    private int activeConnectionCount;

    private int availableConnectionCount;

    private int offlineConnectionCount;

    private int maximumConnectionCount;

    private Date snapshotDate;

    /**
     * @param snapshotDate see {@link SnapshotIF#getSnapshotDate}
     */
    public Snapshot(Date snapshotDate) {
        this.snapshotDate = snapshotDate;
    }

    /**
     * @see SnapshotIF#getDateStarted
     */
    public Date getDateStarted() {
        return dateStarted;
    }

    /**
     * @see SnapshotIF#getDateStarted
     */
    public void setDateStarted(Date dateStarted) {
        this.dateStarted = dateStarted;
    }

    /**
     * @see SnapshotIF#getServedCount
     */
    public long getServedCount() {
        return servedCount;
    }

    /**
     * @see SnapshotIF#getServedCount
     */
    public void setServedCount(long servedCount) {
        this.servedCount = servedCount;
    }

    /**
     * @see SnapshotIF#getRefusedCount
     */
    public long getRefusedCount() {
        return refusedCount;
    }

    /**
     * @see SnapshotIF#getRefusedCount
     */
    public void setRefusedCount(long refusedCount) {
        this.refusedCount = refusedCount;
    }

    /**
     * @see SnapshotIF#getActiveConnectionCount
     */
    public int getActiveConnectionCount() {
        return activeConnectionCount;
    }

    /**
     * @see SnapshotIF#getActiveConnectionCount
     */
    public void setActiveConnectionCount(int activeConnectionCount) {
        this.activeConnectionCount = activeConnectionCount;
    }

    /**
     * @see SnapshotIF#getAvailableConnectionCount
     */
    public int getAvailableConnectionCount() {
        return availableConnectionCount;
    }

    /**
     * @see SnapshotIF#getAvailableConnectionCount
     */
    public void setAvailableConnectionCount(int availableConnectionCount) {
        this.availableConnectionCount = availableConnectionCount;
    }

    /**
     * @see SnapshotIF#getOfflineConnectionCount
     */
    public int getOfflineConnectionCount() {
        return offlineConnectionCount;
    }

    /**
     * @see SnapshotIF#getOfflineConnectionCount
     */
    public void setOfflineConnectionCount(int offlineConnectionCount) {
        this.offlineConnectionCount = offlineConnectionCount;
    }

    /**
     * @see SnapshotIF#getMaximumConnectionCount
     */
    public int getMaximumConnectionCount() {
        return maximumConnectionCount;
    }

    /**
     * @see SnapshotIF#getMaximumConnectionCount
     */
    public void setMaximumConnectionCount(int maximumConnectionCount) {
        this.maximumConnectionCount = maximumConnectionCount;
    }

    /**
     * @see SnapshotIF#getSnapshotDate
     */
    public Date getSnapshotDate() {
        return snapshotDate;
    }

}


/*
 Revision history:
 $Log: Snapshot.java,v $
 Revision 1.1  2003/01/30 17:20:09  billhorsman
 fixes, improvements and doc

 */