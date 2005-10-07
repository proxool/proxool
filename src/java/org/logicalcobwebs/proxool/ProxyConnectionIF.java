/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Contains most of the functionality that we require to manipilate the
 * connection. The subclass of this defines how we delegate to the
 * real connection.

 * @version $Revision: 1.7 $, $Date: 2005/10/07 08:18:24 $
 * @author bill
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.7
 */
public interface ProxyConnectionIF extends ConnectionInfoIF {

    /**
     * Changes the status and lets the ConnectionPool know so that it
     * can keep count of how many connections are at each status.
     * This method obtains a write lock.
     * @param oldStatus the expected existing status. if the existing
     * status is not this value then no change is made and false is returned.
     * @param newStatus the status to change to
     * @return true if status changed successfully, or false if no change made
     * (because of unexpected existing status).
     */
    boolean setStatus(int oldStatus, int newStatus);

    /**
     * Forces the new status regardless of the old state
     * @param newStatus the status to change to
     * @return true if status changed successfully, or false if no change made (should
     * always return true)
     * @see #setStatus(int, int)
     */
    boolean setStatus(int newStatus);

    /**
     * Mark this connection for expiry (destruction) as soon as it stops
     * being active.
     * @param reason why we are marking this connection
     * @see #isMarkedForExpiry
     */
    void markForExpiry(String reason);

    /**
     * Whether this connection is due for expiry
     * @return true if it is due for expiry
     * @see #markForExpiry
     */
    boolean isMarkedForExpiry();

    /**
     * Why this connection is marked (for instance, if a thread has
     * marked it for expiry then it's nice to know why)
     * @return reasonForMark
     */
    String getReasonForMark();

    /**
     * The real, delegate connection that we are using
     * @return connection
     */
    Connection getConnection();

    /**
     * @return true if the status is null
     */
    boolean isNull();

    /**
     * @return true if the status is available
     */
    boolean isAvailable();

    /**
     * @return true if the status is active
     */
    boolean isActive();

    /**
     * @return true if the status is offline
     */
    boolean isOffline();

    /**
     * Really close the connection, as opposed to just putting it back
     * in the pool.
     */
    void reallyClose() throws SQLException;

    /**
     *  @see ConnectionInfoIF#getRequester
     */
    void setRequester(String requester);

    /**
     * Doesn't really close the connection, just puts it back in the pool. And tries to
     * reset all the methods that need resetting.
     * @see Connection#close
     */
    void close() throws SQLException;

    /**
     * Notify that a statement has been closed and won't need closing
     * when the connection is returned to the poo.
     * @param statement the statement that has just been closed
     */
    void registerClosedStatement(Statement statement);

    /**
     * Find out if the delegated connection is close. Just calling isClosed() on the
     * proxied connection will only indicate whether it is in the pool or not.
     * @return true if the connection is really closed, or if the connection is null
     * @throws java.sql.SQLException if anything went wrong
     */
    boolean isReallyClosed() throws SQLException;

    ConnectionPoolDefinitionIF getDefinition();

    /**
     * Get the most recent of all the {@link #getSqlCalls()}
     * @return the SQL (could be a batch of SQLs)
     */
    String getLastSqlCall();

}


/*
 Revision history:
 $Log: ProxyConnectionIF.java,v $
 Revision 1.7  2005/10/07 08:18:24  billhorsman
 New sqlCalls gives list of SQL calls rather than just he most recent (for when a connection makes more than one call before being returned to the pool)

 Revision 1.6  2005/09/26 10:01:31  billhorsman
 Added lastSqlCall when trace is on.

 Revision 1.5  2005/05/04 16:24:13  billhorsman
 include a reference to the definition so we can spot it changing.

 Revision 1.4  2003/03/10 15:26:49  billhorsman
 refactoringn of concurrency stuff (and some import
 optimisation)

 Revision 1.3  2003/03/03 11:11:58  billhorsman
 fixed licence

 Revision 1.2  2003/02/26 16:05:53  billhorsman
 widespread changes caused by refactoring the way we
 update and redefine pool definitions.

 Revision 1.1  2003/01/27 18:26:39  billhorsman
 refactoring of ProxyConnection and ProxyStatement to
 make it easier to write JDK 1.2 patch

 */