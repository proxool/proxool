/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import org.logicalcobwebs.logging.Log;
import org.logicalcobwebs.logging.LogFactory;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.logicalcobwebs.concurrent.WriterPreferenceReadWriteLock;

/**
 * Contains most of the functionality that we require to manipilate the
 * connection. The subclass of this defines how we delegate to the
 * real connection.
 *
 * @version $Revision: 1.15 $, $Date: 2003/03/10 15:26:42 $
 * @author bill
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.7
 */
abstract class AbstractProxyConnection implements ProxyConnectionIF {

    static final int STATUS_FORCE = -1;

    WriterPreferenceReadWriteLock statusReadWriteLock = new WriterPreferenceReadWriteLock();

    private static final Log LOG = LogFactory.getLog(AbstractProxyConnection.class);

    private Connection connection;

    private String delegateUrl;

    private int mark;

    private String reasonForMark;

    private int status;

    private long id;

    private Date birthDate;

    private long timeLastStartActive;

    private long timeLastStopActive;

    private ConnectionPool connectionPool;

    private String requester;

    private Set openStatements = new HashSet();

    private DecimalFormat idFormat = new DecimalFormat("0000");

    /**
     * Whether we have invoked a method that requires us to reset
     */
    private boolean needToReset = false;

    protected AbstractProxyConnection(Connection connection, long id, String delegateUrl, ConnectionPool connectionPool) throws SQLException {
        this.connection = connection;
        this.delegateUrl = delegateUrl;
        setId(id);
        this.connectionPool = connectionPool;
        setBirthTime(System.currentTimeMillis());

        // initialise the connection as offline for now
        setStatus(STATUS_OFFLINE);

        // We only need to call this for the first connection we make. But it returns really
        // quickly and we don't call it that often so we shouldn't worry.
        connectionPool.initialiseConnectionResetter(connection);

        if (connection == null) {
            throw new SQLException("Unable to create new connection");
        }
    }

    /**
     * Whether the underlying connections are equal
     * @param obj the object (probably another connection) that we
     * are being compared to
     * @return whether they are the same
     */
    public boolean equals(Object obj) {
        if (obj != null) {
            if (obj instanceof ProxyConnection) {
                return connection.hashCode() == ((ProxyConnection) obj).getConnection().hashCode();
            } else if (obj instanceof Connection) {
                return connection.hashCode() == obj.hashCode();
            } else {
                return super.equals(obj);
            }
        } else {
            return false;
        }
    }

    /**
     * Whether this connection is available. (When you close the connection
     * it doesn't really close, it just becomes available for others to use).
     * @return true if the connection is not active
     */
    public boolean isClosed() {
        return (getStatus() != STATUS_ACTIVE);
    }

    public DatabaseMetaData getMetaData() throws SQLException {
        return ProxyFactory.getDatabaseMetaData(connection, this);
    }

    /**
     * The subclass should call this to indicate that a change has been made to
     * the connection that might mean it needs to be reset (like setting autoCommit
     * to false or something). We don't reset unless this has been called to avoid
     * the overhead of unnecessary resetting.
     *
     * @param needToReset true if the connection might need resetting.
     */
    protected void setNeedToReset(boolean needToReset) {
        this.needToReset = needToReset;
    }

    /**
     * The ConnectionPool that was used to create this connection
     * @return connectionPool
     */
    protected ConnectionPool getConnectionPool() {
        return connectionPool;
    }

    /**
     * By calling this we can keep track of any statements that are
     * left open when this connection is returned to the pool.
     * @param statement the statement that we have just opened/created.
     * @see #registerClosedStatement
     */
    protected void addOpenStatement(Statement statement) {
        openStatements.add(statement);
    }

    /**
     * @see ProxyConnectionIF#registerClosedStatement
     */
    public void registerClosedStatement(Statement statement) {
        if (openStatements.contains(statement)) {
            openStatements.remove(statement);
        } else {
            connectionPool.getLog().warn(connectionPool.displayStatistics() + " - #" + getId() + " registered a statement as closed which wasn't known to be open.");
        }
    }

    /**
     * Close the connection for real
     * @throws java.sql.SQLException if anything goes wrong
     */
    public void reallyClose() throws SQLException {
        try {
            connectionPool.registerRemovedConnection(getStatus());
            // Clean up the actual connection
            connection.close();
        } catch (Throwable t) {
            connectionPool.getLog().error("#" + idFormat.format(getId()) + " encountered errors during destruction: " + t);
        }

    }

    /**
     * @see ProxyConnectionIF#isReallyClosed
     */
    public boolean isReallyClosed() throws SQLException {
        if (connection == null) {
            return true;
        } else {
            return connection.isClosed();
        }
    }

    /**
     * @see ProxyConnectionIF#close
     */
    public void close() throws SQLException {
        try {

            // Close any open statements, as specified in JDBC
            Iterator i = openStatements.iterator();
            while (i.hasNext()) {
                Statement statement = (Statement) i.next();
                statement.close();
                if (connectionPool.getLog().isDebugEnabled()) {
                    connectionPool.getLog().debug("Closing statement " + Integer.toHexString(statement.hashCode()) + " automatically");
                }
            }
            openStatements.clear();

            if (needToReset) {
                // This call should be as quick as possible. Should we consider only
                // calling it if values have changed? The trouble with that is that it
                // means keeping track when they change and that might be even
                // slower
                if (!connectionPool.resetConnection(connection, "#" + getId())) {
                    connectionPool.removeProxyConnection(this, "it couldn't be reset", true, true);
                }
                needToReset = false;
            }
            connectionPool.putConnection(this);
        } catch (Throwable t) {
            connectionPool.getLog().error("#" + idFormat.format(getId()) + " encountered errors during closure: ", t);
        }

    }

    public int getMark() {
        return mark;
    }

    public int getStatus() {
        return status;
    }

    /**
     * @see ProxyConnectionIF#setStatus(int)
     */
    public boolean setStatus(int newStatus) {
        return setStatus(STATUS_FORCE, newStatus);
    }

    /**
     * @see ProxyConnectionIF#setStatus(int, int)
     */
    public boolean setStatus(int oldStatus, int newStatus) {
        boolean success = false;
        try {
            statusReadWriteLock.writeLock().acquire();
            if (this.status == oldStatus || oldStatus == STATUS_FORCE) {
                connectionPool.changeStatus(this.status, newStatus);
                this.status = newStatus;
                success = true;

                if (newStatus == oldStatus) {
                    LOG.warn("Unexpected attempt to change status from " + oldStatus + " to " + newStatus
                            + ". Why would you want to do that?");
                } else if (newStatus == STATUS_ACTIVE) {
                    setTimeLastStartActive(System.currentTimeMillis());
                } else if (oldStatus == STATUS_ACTIVE) {
                    setTimeLastStopActive(System.currentTimeMillis());
                }
            }
        } catch (InterruptedException e) {
            LOG.error("Unable to acquire write lock for status");
        } finally {
            statusReadWriteLock.writeLock().release();
        }
        return success;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    /**
     * @see ConnectionInfoIF#getBirthTime
     */
    public long getBirthTime() {
        return birthDate.getTime();
    }

    /**
     * @see ConnectionInfoIF#getBirthDate
     */
    public Date getBirthDate() {
        return birthDate;
    }

    /**
     * @see ConnectionInfoIF#getAge
     */
    public long getAge() {
        return System.currentTimeMillis() - getBirthTime();
    }

    /**
     * @see ConnectionInfoIF#getBirthTime
     */
    public void setBirthTime(long birthTime) {
        birthDate = new Date(birthTime);
    }

    /**
     * @see ConnectionInfoIF#getTimeLastStartActive
     */
    public long getTimeLastStartActive() {
        return timeLastStartActive;
    }

    /**
     * @see ConnectionInfoIF#getTimeLastStartActive
     */
    public void setTimeLastStartActive(long timeLastStartActive) {
        this.timeLastStartActive = timeLastStartActive;
        setTimeLastStopActive(0);
    }

    /**
     * @see ConnectionInfoIF#getTimeLastStopActive
     */
    public long getTimeLastStopActive() {
        return timeLastStopActive;
    }

    /**
     * @see ConnectionInfoIF#getTimeLastStopActive
     */
    public void setTimeLastStopActive(long timeLastStopActive) {
        this.timeLastStopActive = timeLastStopActive;
    }

    /**
     * @see ConnectionInfoIF#getRequester
     */
    public String getRequester() {
        return requester;
    }

    /**
     * @see ConnectionInfoIF#getRequester
     */
    public void setRequester(String requester) {
        this.requester = requester;
    }

    /**
     * @see ProxyConnectionIF#isNull
     */
    public boolean isNull() {
        return getStatus() == STATUS_NULL;
    }

    /**
     * @see ProxyConnectionIF#isAvailable
     */
    public boolean isAvailable() {
        return getStatus() == STATUS_AVAILABLE;
    }

    /**
     * @see ProxyConnectionIF#isActive
     */
    public boolean isActive() {
        return getStatus() == STATUS_ACTIVE;
    }

    /**
     * @see ProxyConnectionIF#isOffline
     */
    public boolean isOffline() {
        return getStatus() == STATUS_OFFLINE;
    }

    /**
     * @see ProxyConnectionIF#markForExpiry
     */
    public void markForExpiry(String reason) {
        mark = MARK_FOR_EXPIRY;
        reasonForMark = reason;
    }

    /**
     * @see ProxyConnectionIF#isMarkedForExpiry
     */
    public boolean isMarkedForExpiry() {
        return getMark() == MARK_FOR_EXPIRY;
    }

    /**
     * @see ProxyConnectionIF#getReasonForMark
     */
    public String getReasonForMark() {
        return reasonForMark;
    }

    /**
     * @see ProxyConnectionIF#getConnection
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * @see Object#toString
     */
    public String toString() {
        return getId() + " is " + ConnectionPool.getStatusDescription(getStatus());
    }

    /**
     * @see ConnectionInfoIF#getDelegateUrl
     */
    public String getDelegateUrl() {
        return delegateUrl;
    }

    /**
     * @see ConnectionInfoIF#getProxyHashcode
     */
    public String getProxyHashcode() {
        return Integer.toHexString(hashCode());
    }

    /**
     * @see ConnectionInfoIF#getDelegateHashcode
     */
    public String getDelegateHashcode() {
        if (connection != null) {
            return Integer.toHexString(connection.hashCode());
        } else {
            return null;
        }
    }
}


/*
 Revision history:
 $Log: AbstractProxyConnection.java,v $
 Revision 1.15  2003/03/10 15:26:42  billhorsman
 refactoringn of concurrency stuff (and some import
 optimisation)

 Revision 1.14  2003/03/05 18:42:32  billhorsman
 big refactor of prototyping and house keeping to
 drastically reduce the number of threads when using
 many pools

 Revision 1.13  2003/03/03 11:11:56  billhorsman
 fixed licence

 Revision 1.12  2003/02/26 16:05:52  billhorsman
 widespread changes caused by refactoring the way we
 update and redefine pool definitions.

 Revision 1.11  2003/02/13 17:01:27  billhorsman
 use hex for statement hashcode

 Revision 1.10  2003/02/12 12:30:10  billhorsman
 checkstyle

 Revision 1.9  2003/02/12 12:28:27  billhorsman
 added url, proxyHashcode and delegateHashcode to
 ConnectionInfoIF

 Revision 1.8  2003/02/11 00:30:45  billhorsman
 fixed equals for jdk1.2

 Revision 1.7  2003/02/07 10:58:22  billhorsman
 fixed equals method so that connections are actually
 removed (they were just being marked as status null)

 Revision 1.6  2003/02/06 17:41:04  billhorsman
 now uses imported logging

 Revision 1.5  2003/01/31 16:53:15  billhorsman
 checkstyle

 Revision 1.4  2003/01/31 14:33:14  billhorsman
 fix for DatabaseMetaData

 Revision 1.3  2003/01/31 11:38:57  billhorsman
 birthDate now stored as Date not long

 Revision 1.2  2003/01/28 11:50:35  billhorsman
 more verbose debug

 Revision 1.1  2003/01/27 18:26:33  billhorsman
 refactoring of ProxyConnection and ProxyStatement to
 make it easier to write JDK 1.2 patch

 */