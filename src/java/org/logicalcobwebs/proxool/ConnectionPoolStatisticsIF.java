/*
 * This software is released under the Apache Software Licence. See
 * package.html for details. The latest version is available at
 * http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

/**
 * This provides real time information about the pool.
 * @version $Revision: 1.1 $, $Date: 2002/09/13 08:13:04 $
 * @author billhorsman
 * @author $Author: billhorsman $ (current maintainer)
 */
public interface ConnectionPoolStatisticsIF {

    /* The number of connections provided. */
    long getConnectionsServedCount();

    /* The number of connections refused. Either because there was a problem
    connecting to the database, or perhaps because the maximumConnectionCount
    was reached. */
    long getConnectionsRefusedCount();

    /* The number of connections currently in use. */
    int getActiveConnectionCount();

    /* The number of connections that are available for use (doesn't include
    active connections).*/
    int getAvailableConnectionCount();

    /* The number of connections that are neither active or available. Probably
    because the house keeping thread is checking them. */
    int getOfflineConnectionCount();

}

/*
 Revision history:
 $Log: ConnectionPoolStatisticsIF.java,v $
 Revision 1.1  2002/09/13 08:13:04  billhorsman
 Initial revision

 Revision 1.6  2002/07/10 16:14:47  billhorsman
 widespread layout changes and move constants into ProxoolConstants

 Revision 1.5  2002/07/02 11:19:08  billhorsman
 layout code and imports

 Revision 1.4  2002/06/28 11:19:47  billhorsman
 improved doc

*/
