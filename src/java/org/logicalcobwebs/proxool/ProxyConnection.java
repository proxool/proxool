/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import org.logicalcobwebs.concurrent.WriterPreferenceReadWriteLock;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.logicalcobwebs.proxool.util.FastArrayList;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.text.DecimalFormat;

/**
 * Manages a connection. This is wrapped up inside a...
 *
 * @version $Revision: 1.37 $, $Date: 2006/01/18 14:40:02 $
 * @author bill
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.10
 */
public class ProxyConnection implements ProxyConnectionIF {

    static final int STATUS_FORCE = -1;

    private WriterPreferenceReadWriteLock statusReadWriteLock = new WriterPreferenceReadWriteLock();

    private static final Log LOG = LogFactory.getLog(ProxyConnection.class);

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

    private ConnectionPoolDefinitionIF definition;

    private String requester;

    private Set openStatements = new HashSet();

    private DecimalFormat idFormat = new DecimalFormat("0000");

    private List sqlCalls = new FastArrayList();

    /**
     * Whether we have invoked a method that requires us to reset
     */
    private boolean needToReset = false;

    /**
     *
     * @param connection the real connection that is used
     * @param id unique ID
     * @param delegateUrl
     * @param connectionPool the pool it is a member of
     * @param definition the definition that was used to build it (could possibly be different from
     * the one held in the connectionPool)
     * @param status {@link #STATUS_ACTIVE}, {@link #STATUS_AVAILABLE}, {@link #STATUS_FORCE}, {@link #STATUS_NULL}, or {@link #STATUS_OFFLINE}
     * @throws SQLException
     */
    protected ProxyConnection(Connection connection, long id, String delegateUrl, ConnectionPool connectionPool, ConnectionPoolDefinitionIF definition, int status) throws SQLException {
        this.connection = connection;
        this.delegateUrl = delegateUrl;
        setId(id);
        this.connectionPool = connectionPool;
        this.definition = definition;
        setBirthTime(System.currentTimeMillis());

        this.status = status;
        if (status == STATUS_ACTIVE) {
            setTimeLastStartActive(System.currentTimeMillis());
        }

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
     * The ConnectionPool that this connection belongs to
     * @return connectionPool
     */
    protected ConnectionPool getConnectionPool() {
        return connectionPool;
    }

    /**
     * Get the definition that was used to create this connection
     * @return definition
     */
    public ConnectionPoolDefinitionIF getDefinition() {
        return definition;
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
            connectionPool.getLog().error("#" + idFormat.format(getId()) + " encountered errors during destruction: ",  t);
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
            boolean removed = false;
            if (isMarkedForExpiry()) {
                if (connectionPool.getLog().isDebugEnabled()) {
                    connectionPool.getLog().debug("Closing connection quickly (without reset) because it's marked for expiry anyway");
                }
            } else {
                // Close any open statements, as specified in JDBC
                Statement[] statements = (Statement[]) openStatements.toArray(new Statement[openStatements.size()]);
                for (int j = 0; j < statements.length; j++) {
                    Statement statement = statements[j];
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
                        removed = true;
                    }
                    needToReset = false;
                }
            }
            // If we removed it above then putting it back will only cause a confusing log event later when
            // it is unable to be changed from ACTIVE to AVAILABLE.
            if (!removed) {
                connectionPool.putConnection(this);
            }
        } catch (Throwable t) {
            connectionPool.getLog().error("#" + idFormat.format(getId()) + " encountered errors during closure: ", t);
        }

    }

    /**
     * This gets called /just/ before a connection is served. You can use it to reset some of the attributes.
     * The lifecycle is: {@link #open()} then {@link #close()}
     */
    protected void open() {
        sqlCalls.clear();
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
            connectionPool.acquireConnectionStatusWriteLock();
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
            connectionPool.releaseConnectionStatusWriteLock();
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

    /**
     * Compares using {@link #getId()}
     * @param o must be another {@link ConnectionInfoIF} implementation
     * @return the comparison
     * @see Comparable#compareTo(Object)
     */
    public int compareTo(Object o) {
        return new Long(((ConnectionInfoIF) o).getId()).compareTo(new Long(getId()));
    }

    public String[] getSqlCalls() {
        return (String[]) sqlCalls.toArray(new String[0]);
    }

    public String getLastSqlCall() {
        if (sqlCalls != null && sqlCalls.size() > 0) {
            return (String) sqlCalls.get(sqlCalls.size() - 1);
        } else {
            return null;
        }
    }

    public void addSqlCall(String sqlCall) {
        this.sqlCalls.add(sqlCall);
    }
}
