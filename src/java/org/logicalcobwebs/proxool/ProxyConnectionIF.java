/*
 * This software is released under the Apache Software Licence. See
 * package.html for details. The latest version is available at
 * http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Contains most of the functionality that we require to manipilate the
 * connection. The subclass of this defines how we delegate to the
 * real connection.

 * @version $Revision: 1.1 $, $Date: 2003/01/27 18:26:39 $
 * @author bill
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.7
 */
public interface ProxyConnectionIF extends ConnectionInfoIF {

    /**
     * Thread-safe way of changing the status. It will return false if the
     * old status wasn't what was expected (and no change will be made).
     * Hence, fromXToY only changes to Y if it is X to start with.
     * @return true if it succeeded
     */
    boolean fromActiveToAvailable();

    /**
     * Thread-safe way of changing the status. It will return false if the
     * old status wasn't what was expected (and no change will be made).
     * Hence, fromXToY only changes to Y if it is X to start with.
     * @return true if it succeeded
     */
    boolean fromAnythingToNull();

    /**
     * Thread-safe way of changing the status. It will return false if the
     * old status wasn't what was expected (and no change will be made).
     * Hence, fromXToY only changes to Y if it is X to start with.
     * @return true if it succeeded
     */
    boolean fromActiveToNull();

    /**
     * Thread-safe way of changing the status. It will return false if the
     * old status wasn't what was expected (and no change will be made).
     * Hence, fromXToY only changes to Y if it is X to start with.
     * @return true if it succeeded
     */
    boolean fromAvailableToActive();

    /**
     * Thread-safe way of changing the status. It will return false if the
     * old status wasn't what was expected (and no change will be made).
     * Hence, fromXToY only changes to Y if it is X to start with.
     * @return true if it succeeded
     */
    boolean fromOfflineToAvailable();

    /**
     * Thread-safe way of changing the status. It will return false if the
     * old status wasn't what was expected (and no change will be made).
     * Hence, fromXToY only changes to Y if it is X to start with.
     * @return true if it succeeded
     */
    boolean fromOfflineToNull();

    /**
     * Thread-safe way of changing the status. It will return false if the
     * old status wasn't what was expected (and no change will be made).
     * Hence, fromXToY only changes to Y if it is X to start with.
     * @return true if it succeeded
     */
    boolean fromOfflineToActive();

    /**
     * Thread-safe way of changing the status. It will return false if the
     * old status wasn't what was expected (and no change will be made).
     * Hence, fromXToY only changes to Y if it is X to start with.
     * @return true if it succeeded
     */
    boolean fromAvailableToOffline();

    /**
     * Mark this connection for expiry (destruction) as soon as it stops
     * being active.
     * @see #isMarkedForExpiry
     */
    void markForExpiry();

    /**
     * Whether this connection is due for expiry
     * @return true if it is due for expiry
     * @see #markForExpiry
     */
    boolean isMarkedForExpiry();

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

}


/*
 Revision history:
 $Log: ProxyConnectionIF.java,v $
 Revision 1.1  2003/01/27 18:26:39  billhorsman
 refactoring of ProxyConnection and ProxyStatement to
 make it easier to write JDK 1.2 patch

 */