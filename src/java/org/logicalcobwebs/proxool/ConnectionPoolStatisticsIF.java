/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import java.util.Date;

/**
 * This provides real time information about the pool. You can get this from
 * {@link ProxoolFacade#getConnectionPoolStatistics ProxoolFacade}.
 *
 * <pre>
 * String alias = "myPool";
 * ConnectionPoolStatisticsIF cps = ProxoolFacade.getConnectionPoolStatistics(alias);
 * </pre>
 *
 * @version $Revision: 1.6 $, $Date: 2005/10/02 12:32:02 $
 * @author billhorsman
 * @author $Author: billhorsman $ (current maintainer)
 */
public interface ConnectionPoolStatisticsIF {

    /**
     * The number of connections provided.
     * @return connectionsServedCount
     */
    long getConnectionsServedCount();

    /**
     * The number of connections refused. Either because there was a problem
    * connecting to the database, or perhaps because the maximumConnectionCount
    * was reached.
     * @return connectionsRefusedCount
     */
    long getConnectionsRefusedCount();

    /**
     * The number of connections currently in use.
     * @return activeConnectionCount
     */
    int getActiveConnectionCount();

    /**
     * The number of connections that are available for use (doesn't include
    * active connections).
     * @return availableConnectionCount
     */
    int getAvailableConnectionCount();

    /**
     * The number of connections that are neither active or available. Probably
     * because the house keeping thread is checking them.
     * @return offlineConnectionCount
     */
    int getOfflineConnectionCount();

    /**
     * When this pool was started
     * @return dateStarted
     */
    Date getDateStarted();

    long getConnectionCount();
}

/*
 Revision history:
 $Log: ConnectionPoolStatisticsIF.java,v $
 Revision 1.6  2005/10/02 12:32:02  billhorsman
 Make connectionCount available to statistics

 Revision 1.5  2003/03/03 11:11:57  billhorsman
 fixed licence

 Revision 1.4  2003/01/15 12:01:21  billhorsman
 added getDateStarted() plus better doc

 Revision 1.3  2002/12/15 19:21:42  chr32
 Changed @linkplain to @link (to preserve JavaDoc for 1.2/1.3 users).

 Revision 1.2  2002/10/25 16:00:26  billhorsman
 added better class javadoc

 Revision 1.1.1.1  2002/09/13 08:13:04  billhorsman
 new

 Revision 1.6  2002/07/10 16:14:47  billhorsman
 widespread layout changes and move constants into ProxoolConstants

 Revision 1.5  2002/07/02 11:19:08  billhorsman
 layout code and imports

 Revision 1.4  2002/06/28 11:19:47  billhorsman
 improved doc

*/
