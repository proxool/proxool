/*
 * This software is released under the Apache Software Licence. See
 * package.html for details. The latest version is available at
 * http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool.stats;

import java.util.Date;

/**
 * Provides a snapshot of a pool
 *
 * @version $Revision: 1.1 $, $Date: 2003/01/30 17:20:15 $
 * @author bill
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.7
 */
public interface SnapshotIF {

    /**
     * When the pool was started
     * @return dateStarted
     */
    Date getDateStarted();

    /**
     * How many connections have been served since the pool started
     * @return servedCount
     */
    long getServedCount();

    /**
     * How many connections have been refused since the pool started
     * @return refusedCount
     */
    long getRefusedCount();

    /**
     * The number of active (busy) connections
     * @return activeConnectionCount
     */
    int getActiveConnectionCount();

    /**
     * The number of available (free) connections
     * @return availableConnectionCount
     */
    int getAvailableConnectionCount();

    /**
     * The number of offline connections. A connection is offline
     * if it is being tested by the house keeper.
     * @return offlineConnectionCount
     */
    int getOfflineConnectionCount();

    /**
     * Get the maximum possible connections (as defined in the
     * {@link org.logicalcobwebs.proxool.ConnectionPoolDefinitionIF definition}.
     * @return maximumConnectionCount
     */
    int getMaximumConnectionCount();

    /**
     * The date that this snapshot applies
     * @return snapshotDate
     */
    Date getSnapshotDate();
}


/*
 Revision history:
 $Log: SnapshotIF.java,v $
 Revision 1.1  2003/01/30 17:20:15  billhorsman
 fixes, improvements and doc

 */