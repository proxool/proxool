/*
 * This software is released under the Apache Software Licence. See
 * package.html for details. The latest version is available at
 * http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.Map;

/**
 * Delegates to a normal Coonection for everything but the close()
 * method (when it puts itself back into the pool instead).
 * @version $Revision: 1.1 $, $Date: 2002/09/13 08:14:03 $
 * @author billhorsman
 * @author $Author: billhorsman $ (current maintainer)
 */
public class ProxyConnection implements Connection, ConnectionInfoIF {

    /** This is the start and end state of every connection */
    protected static final int STATUS_NULL = 0;

    /** The connection is available for use */
    protected static final int STATUS_AVAILABLE = 1;

    /** The connection is in use */
    protected static final int STATUS_ACTIVE = 2;

    /** The connection is in use by the house keeping thread */
    protected static final int STATUS_OFFLINE = 3;

    /** Default - treat as normal */
    protected static final int MARK_FOR_USE = 0;

    /** The next time this connection is made available expire it. */
    protected static final int MARK_FOR_EXPIRY = 1;

    private Connection connection;

    private int mark;

    private int status;

    private long id;

    private long birthTime;

    private long timeLastStartActive;

    private long timeLastStopActive;

    private ConnectionPool connectionPool;

    private String requester;

    private DecimalFormat idFormat = new DecimalFormat("0000");

    public ProxyConnection(long id, ConnectionPoolDefinitionIF connectionPoolDefinition) throws SQLException,
        ClassNotFoundException {

        connectionPool = ConnectionPoolManager.getInstance().getConnectionPool(connectionPoolDefinition.getName());

        if (connectionPoolDefinition.getDebugLevel() > ConnectionPoolDefinitionIF.DEBUG_LEVEL_QUIET) {
            connectionPool.debug("Initialising connection #" + id + " using " + connectionPoolDefinition.getUrl());

        }

        try {
            connection = DriverManager.getConnection(connectionPoolDefinition.getUrl(), connectionPoolDefinition.getProperties());
        }
        catch (SQLException e) {
            throw e;
        }

        // Initialize some variables
        setId(id);
        setBirthTime(System.currentTimeMillis());
        setStatus(STATUS_OFFLINE);

        if (connection == null) {
            throw new SQLException("Unable to create new connection");
        }
    }

    /**
     * Delegates to {@link java.sql.Connection#createStatement}
     */
    public Statement createStatement() throws SQLException {
        return new ProxyStatement(connection.createStatement(), connectionPool);
    }

    /**
     * Delegates to {@link java.sql.Connection#prepareStatement}
     */
    public PreparedStatement prepareStatement(String sql)
        throws SQLException {
        return new ProxyPreparedStatement(connection.prepareStatement(sql), connectionPool);
    }

    /**
     * Delegates to {@link java.sql.Connection#prepareCall}
     */
    public CallableStatement prepareCall(String sql) throws SQLException {
        return new ProxyCallableStatement(connection.prepareCall(sql), connectionPool);
    }

    /**
     * Delegates to {@link java.sql.Connection#nativeSQL}
     */
    public String nativeSQL(String sql) throws SQLException {
        return connection.nativeSQL(sql);
    }

    /**
     * Delegates to {@link java.sql.Connection#setAutoCommit}
     */
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        connection.setAutoCommit(autoCommit);
    }

    /**
     * Delegates to {@link java.sql.Connection#getAutoCommit}
     */
    public boolean getAutoCommit() throws SQLException {
        return connection.getAutoCommit();
    }

    /**
     * Delegates to {@link java.sql.Connection#commit}
     */
    public void commit() throws SQLException {
        connection.commit();
    }

    /**
     * Delegates to {@link java.sql.Connection#rollback}
     */
    public void rollback() throws SQLException {
        connection.rollback();
    }

    protected void reallyClose() throws SQLException {
        try {
            connectionPool.registerRemovedConnection(getStatus());
            // Clean up the actual connection
            connection.close();
        }
        catch (Throwable t) {
            connectionPool.error("#" + idFormat.format(getId()) + " encountered errors during destruction: " + t);
        }

    }

    /**
     * Doesn't really close the connection, just puts it back in the pool
     */
    public void close() throws SQLException {
        try {
            connectionPool.putConnection(this);
        }
        catch (Throwable t) {
            connectionPool.error("#" + idFormat.format(getId()) + " encountered errors during destruction: " + t);
        }

    }

    /**
     * Not whether the actual Connection is closed but whether it is "not active".
     */
    public boolean isClosed() throws SQLException {
        return !isActive();
    }

    public boolean isReallyClosed() throws SQLException {
        return connection.isClosed();
    }

    /**
     * Delegates to {@link java.sql.Connection#getMetaData}
     */
    public DatabaseMetaData getMetaData() throws SQLException {
        return connection.getMetaData();
    }

    /**
     * Delegates to {@link java.sql.Connection#setReadOnly}
     */
    public void setReadOnly(boolean readOnly) throws SQLException {
        connection.setReadOnly(readOnly);
    }

    /**
     * Delegates to {@link java.sql.Connection#isReadOnly}
     */
    public boolean isReadOnly() throws SQLException {
        return connection.isReadOnly();
    }

    /**
     * Delegates to {@link java.sql.Connection#setCatalog}
     */
    public void setCatalog(String catalog) throws SQLException {
        connection.setCatalog(catalog);
    }

    /**
     * Delegates to {@link java.sql.Connection#getCatalog}
     */
    public String getCatalog() throws SQLException {
        return connection.getCatalog();
    }

    /**
     * Delegates to {@link java.sql.Connection#setTransactionIsolation}
     */
    public void setTransactionIsolation(int level) throws SQLException {
        connection.setTransactionIsolation(level);
    }

    /**
     * Delegates to {@link java.sql.Connection#getTransactionIsolation}
     */
    public int getTransactionIsolation() throws SQLException {
        return connection.getTransactionIsolation();
    }

    /**
     * Delegates to {@link java.sql.Connection#getWarnings}
     */
    public SQLWarning getWarnings() throws SQLException {
        return connection.getWarnings();
    }

    /**
     * Delegates to {@link java.sql.Connection#clearWarnings}
     */
    public void clearWarnings() throws SQLException {
        connection.clearWarnings();
    }

    /**
     * Delegates to {@link java.sql.Connection#createStatement(int, int)}
     */
    public Statement createStatement(int resultSetType, int resultSetConcurrency)
        throws SQLException {
        return new ProxyStatement(connection.createStatement(resultSetType, resultSetConcurrency), connectionPool);
    }

    /**
     * Delegates to {@link java.sql.Connection#prepareStatement(java.lang.String, int, int)}
     */
    public PreparedStatement prepareStatement(String sql, int resultSetType,
        int resultSetConcurrency)
        throws SQLException {
        return new ProxyPreparedStatement(connection.prepareStatement(sql, resultSetType, resultSetConcurrency), connectionPool);
    }

    /**
     * Delegates to {@link java.sql.Connection#prepareCall(java.lang.String, int, int)}
     */
    public CallableStatement prepareCall(String sql, int resultSetType,
        int resultSetConcurrency) throws SQLException {
        return new ProxyCallableStatement(connection.prepareCall(sql, resultSetType, resultSetConcurrency), connectionPool);
    }

    /**
     * Delegates to {@link java.sql.Connection#getTypeMap}
     */
    public Map getTypeMap() throws SQLException {
        return connection.getTypeMap();
    }

    /**
     * Delegates to {@link java.sql.Connection#setTypeMap}
     */
    public void setTypeMap(Map map) throws SQLException {
        connection.setTypeMap(map);
    }

    public int getMark() {
        return mark;
    }

    public void setMark(int mark) {
        this.mark = mark;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        if (this.status != status) {
            connectionPool.changeStatus(this.status, status);
        }
        if (this.status == ProxyConnection.STATUS_NULL && status != ProxyConnection.STATUS_NULL) {
            connectionPool.incrementConnectedConnectionCount();
        }
        this.status = status;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getBirthTime() {
        return birthTime;
    }

    public long getAge() {
        return System.currentTimeMillis() - getBirthTime();
    }

    public void setBirthTime(long birthTime) {
        this.birthTime = birthTime;
    }

    public long getTimeLastStartActive() {
        return timeLastStartActive;
    }

    public void setTimeLastStartActive(long timeLastStartActive) {
        this.timeLastStartActive = timeLastStartActive;
        setTimeLastStopActive(0);
    }

    public long getTimeLastStopActive() {
        return timeLastStopActive;
    }

    public void setTimeLastStopActive(long timeLastStopActive) {
        this.timeLastStopActive = timeLastStopActive;
    }

    public String getRequester() {
        return requester;
    }

    public void setRequester(String requester) {
        this.requester = requester;
    }

    protected boolean fromActiveToAvailable() {
        boolean success = false;
        synchronized (this) {
            if (isActive()) {
                setStatus(STATUS_AVAILABLE);
                setTimeLastStopActive(System.currentTimeMillis());
                success = true;
            }
        }
        return success;
    }

    protected boolean fromActiveToNull() {
        boolean success = false;
        synchronized (this) {
            if (isActive()) {
                setStatus(STATUS_NULL);
                setTimeLastStopActive(System.currentTimeMillis());
                success = true;
            }
        }
        return success;
    }

    protected boolean fromAvailableToActive() {
        boolean success = false;
        synchronized (this) {
            if (isAvailable()) {
                setStatus(STATUS_ACTIVE);
                setTimeLastStartActive(System.currentTimeMillis());
                success = true;
            }
        }
        return success;
    }

    protected boolean fromOfflineToAvailable() {
        boolean success = false;
        synchronized (this) {
            if (isOffline()) {
                setStatus(STATUS_AVAILABLE);
                success = true;
            }
        }
        return success;
    }

    protected boolean fromOfflineToNull() {
        boolean success = false;
        synchronized (this) {
            if (isOffline()) {
                setStatus(STATUS_NULL);
                success = true;
            }
        }
        return success;
    }

    protected boolean fromOfflineToActive() {
        boolean success = false;
        synchronized (this) {
            if (isOffline()) {
                setStatus(STATUS_ACTIVE);
                setTimeLastStartActive(System.currentTimeMillis());
                success = true;
            }
        }
        return success;
    }

    protected boolean fromAvailableToOffline() {
        boolean success = false;
        synchronized (this) {
            if (isAvailable()) {
                setStatus(STATUS_OFFLINE);
                success = true;
            }
        }
        return success;
    }

    protected boolean isNull() {
        return getStatus() == STATUS_NULL;
    }

    protected boolean isAvailable() {
        return getStatus() == STATUS_AVAILABLE;
    }

    protected boolean isActive() {
        return getStatus() == STATUS_ACTIVE;
    }

    protected boolean isOffline() {
        return getStatus() == STATUS_OFFLINE;
    }

    protected void markForExpiry() {
        setMark(MARK_FOR_EXPIRY);
    }

    protected boolean isMarkedForExpiry() {
        return getMark() == MARK_FOR_EXPIRY;
    }

}

/*
 Revision history:
 $Log: ProxyConnection.java,v $
 Revision 1.1  2002/09/13 08:14:03  billhorsman
 Initial revision

 Revision 1.6  2002/07/02 11:19:08  billhorsman
 layout code and imports

 Revision 1.5  2002/06/28 11:19:47  billhorsman
 improved doc

*/
