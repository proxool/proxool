/*
 * This software is released under the Apache Software Licence. See
 * package.html for details. The latest version is available at
 * http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;

/**
 * Delegates to a normal Coonection for everything but the close()
 * method (when it puts itself back into the pool instead).
 * @version $Revision: 1.1 $, $Date: 2002/09/13 08:13:30 $
 * @author billhorsman
 * @author $Author: billhorsman $ (current maintainer)
 */
public class ProxyConnection implements InvocationHandler {

    private Connection connection;

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

    private int mark;

    private int status;

    private long id;

    private long birthTime;

    private long timeLastStartActive;

    private long timeLastStopActive;

    private ConnectionPool connectionPool;

    private String requester;

    private DecimalFormat idFormat = new DecimalFormat("0000");

    private static final String CLOSE_METHOD = "close";

    public static Object newInstance(long id, ConnectionPoolDefinitionIF connectionPoolDefinition) throws SQLException {

        ConnectionPool connectionPool = ConnectionPoolManager.getInstance().getConnectionPool(connectionPoolDefinition.getName());
        Connection connection = null;

        if (connectionPoolDefinition.getDebugLevel() > ConnectionPoolDefinitionIF.DEBUG_LEVEL_QUIET) {
            connectionPool.getLog().debug("Initialising connection #" + id + " using " + connectionPoolDefinition.getUrl());
        }

        connection = DriverManager.getConnection(connectionPoolDefinition.getUrl(), connectionPoolDefinition.getProperties());

        return java.lang.reflect.Proxy.newProxyInstance(
                connection.getClass().getClassLoader(),
                connection.getClass().getInterfaces(),
                new ProxyConnection(connection, id, connectionPool));
    }

    private ProxyConnection(Connection connection, long id, ConnectionPool connectionPool) throws SQLException {
        this.connection = connection;
        setId(id);
        this.connectionPool = connectionPool;
        setBirthTime(System.currentTimeMillis());
        setStatus(STATUS_OFFLINE);

        if (connection == null) {
            throw new SQLException("Unable to create new connection");
        }
    }

    public Object invoke(Object proxy, Method m, Object[] args)
            throws Throwable {
        Object result = null;
        try {
            if (m.getName().equals(CLOSE_METHOD)) {
                close();
            } else {
                result = m.invoke(connection, args);
            }

            // If we have just made some sort of Statement then we should rather return
            // a proxy instead.
            if (result instanceof Statement) {
                Proxy.newProxyInstance(Statement.class.getClassLoader(), Statement.class.getInterfaces(), new ProxyStatement((Statement) result, connectionPool));
            }

        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        } catch (Exception e) {
            throw new RuntimeException("unexpected invocation exception: "
                    + e.getMessage());
        }
        return result;
    }

    protected void reallyClose() throws SQLException {
        try {
            connectionPool.registerRemovedConnection(getStatus());
            // Clean up the actual connection
            connection.close();
        } catch (Throwable t) {
            connectionPool.getLog().error("#" + idFormat.format(getId()) + " encountered errors during destruction: " + t);
        }

    }

    protected boolean isReallyClosed() throws SQLException {
        if (connection == null) {
            return true;
        } else {
            return connection.isClosed();
        }
    }

    /**
     * Doesn't really close the connection, just puts it back in the pool
     */
    public void close() throws SQLException {
        try {
            connectionPool.putConnection(this);
        } catch (Throwable t) {
            connectionPool.getLog().error("#" + idFormat.format(getId()) + " encountered errors during destruction: " + t);
        }

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

    public Connection getConnection() {
        return connection;
    }
}

/*
 Revision history:
 $Log: ProxyConnection.java,v $
 Revision 1.1  2002/09/13 08:13:30  billhorsman
 Initial revision

 Revision 1.10  2002/08/24 19:57:15  billhorsman
 checkstyle changes

 Revision 1.9  2002/08/24 19:42:26  billhorsman
 new proxy stuff to work with JDK 1.4

 Revision 1.6  2002/07/02 11:19:08  billhorsman
 layout code and imports

 Revision 1.5  2002/06/28 11:19:47  billhorsman
 improved doc

*/
