/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import java.util.Date;


/**
 * Provides information about an individual connection. You can get a collection
 * of these from {@link ProxoolFacade#getConnectionInfos ProxoolFacade}. You
 * get back information about all the connections in a particular pool.
 *
 * <pre>
 * String alias = "myPool";
 * Iterator i = ProxoolFacade.getConnectionInfos(alias).iterator();
 * while (i.hasNext()) {
 *  ConnectionInfoIF c = (ConnectionInfoIF)i.next();
 *   ...
 * }
 * </pre>
 *
 * @version $Revision: 1.12 $, $Date: 2005/10/07 08:18:23 $
 * @author billhorsman
 * @author $Author: billhorsman $ (current maintainer)
 */
public interface ConnectionInfoIF extends Comparable {

    /**
     * Default - treat as normal
     * @see #getMark
     */
    static final int MARK_FOR_USE = 0;

    /**
     * The next time this connection is made available we should expire it.
     * @see #getMark
     */
    static final int MARK_FOR_EXPIRY = 1;

    /**
     * This is the start and end state of every connection
     * @see #getStatus
     */
    static final int STATUS_NULL = 0;

    /**
     * The connection is available for use
     * @see #getStatus
     */
    static final int STATUS_AVAILABLE = 1;

    /**
     * The connection is in use
     * @see #getStatus
     */
    static final int STATUS_ACTIVE = 2;

    /**
     * The connection is in use by the house keeping thread
     * @see #getStatus
     */
    static final int STATUS_OFFLINE = 3;

    /**
     * The time that this connection was created.
     * The number of milliseconds
     * since midnight, January 1, 1970 UTC.
     */
    long getBirthTime();

    /**
     * Like {@link #getBirthTime} but in Date format
     * @return birthDate
     */
    Date getBirthDate();

    /**
     * The age in millseconds since this connection was built
     */
    long getAge();

    /**
     * A unique ID for this connection
     */
    long getId();

    /**
     * Sometimes we want do something to a connection but can't because it is still
     * active and we don't want to disrupt its use. So we mark it instead and when it
     * stops being active we can perform the necessary operation.
     *
     * The only thing we do at the moment is {@link #MARK_FOR_EXPIRY expire} the
     * connection (if it is too old for instance). And this will happen if the
     * housekeeper decides it should but the connection is still active.
     */
    int getMark();

    /**
     * The status of the connection. Can be either:
     * {@link #STATUS_NULL null},
     * {@link #STATUS_AVAILABLE available},
     * {@link #STATUS_ACTIVE active} or
     * {@link #STATUS_OFFLINE offline}.
     */
    int getStatus();

    /**
     * When this connection was last given out.  The number of milliseconds
     * since midnight, January 1, 1970 UTC.
     */
    long getTimeLastStartActive();

    /**
     * When this connection was last given back (or zero if it is still active).
     * The number of milliseconds
     * since midnight, January 1, 1970 UTC.
     */
    long getTimeLastStopActive();

    /**
     * The name of the thread that asked for this connection.
     */
    String getRequester();

    /**
     * The hashcode (in hex) of the ProxyConnection object. This
     * uniquely identifies this proxy connection.
     * @return proxyHashcode
     */
    String getProxyHashcode();

    /**
     * The hashcode (in hex) of the delegate connection object. This
     * uniquely identifies the underlying connection.
     * @return delegateHashcode
     */
    String getDelegateHashcode();

    /**
     * The URL that this connection is using (the definition
     * might have changed since this connection was built).
     * @return delegateUrl
     */
    String getDelegateUrl();

    /**
     * A log of the last SQL used on this connection. Only populated
     * if {@link org.logicalcobwebs.proxool.ConnectionPoolDefinitionIF#isTrace()}
     * is enabled.
     * @return the most recent SQL to be used
     */
    String[] getSqlCalls();
    
}

/*
 Revision history:
 $Log: ConnectionInfoIF.java,v $
 Revision 1.12  2005/10/07 08:18:23  billhorsman
 New sqlCalls gives list of SQL calls rather than just he most recent (for when a connection makes more than one call before being returned to the pool)

 Revision 1.11  2005/09/26 10:01:31  billhorsman
 Added lastSqlCall when trace is on.

 Revision 1.10  2003/10/30 00:05:50  billhorsman
 now implements Comparable (using ID)

 Revision 1.9  2003/03/03 11:11:57  billhorsman
 fixed licence

 Revision 1.8  2003/02/12 12:28:27  billhorsman
 added url, proxyHashcode and delegateHashcode to
 ConnectionInfoIF

 Revision 1.7  2003/01/31 11:38:57  billhorsman
 birthDate now stored as Date not long

 Revision 1.6  2003/01/27 18:26:35  billhorsman
 refactoring of ProxyConnection and ProxyStatement to
 make it easier to write JDK 1.2 patch

 Revision 1.5  2003/01/15 00:12:13  billhorsman
 doc

 Revision 1.4  2002/12/15 19:21:42  chr32
 Changed @linkplain to @link (to preserve JavaDoc for 1.2/1.3 users).

 Revision 1.3  2002/10/25 16:00:19  billhorsman
 added better class javadoc

 Revision 1.2  2002/09/18 13:48:56  billhorsman
 checkstyle and doc

 Revision 1.1.1.1  2002/09/13 08:12:32  billhorsman
 new

 Revision 1.3  2002/06/28 11:19:47  billhorsman
 improved doc

*/
