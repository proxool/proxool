/*
 * This software is released under the Apache Software Licence. See
 * package.html for details. The latest version is available at
 * http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

/**
 * Provides information about a connection.
 * @version $Revision: 1.1 $, $Date: 2002/09/13 08:12:32 $
 * @author billhorsman
 * @author $Author: billhorsman $ (current maintainer)
 */
public interface ConnectionInfoIF {

    long getBirthTime();

    long getAge();

    long getId();

    int getMark();

    int getStatus();

    long getTimeLastStartActive();

    long getTimeLastStopActive();

    String getRequester();
}

/*
 Revision history:
 $Log: ConnectionInfoIF.java,v $
 Revision 1.1  2002/09/13 08:12:32  billhorsman
 Initial revision

 Revision 1.3  2002/06/28 11:19:47  billhorsman
 improved doc

*/
