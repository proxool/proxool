/*
 * This software is released under the Apache Software Licence. See
 * package.html for details. The latest version is available at
 * http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;

/**
 * Delegates to a normal Coonection for everything but the close()
 * method (when it puts itself back into the pool instead).
 * @version $Revision: 1.13 $, $Date: 2002/11/06 20:26:49 $
 * @author billhorsman
 * @author $Author: billhorsman $ (current maintainer)
 */
class ProxyConnection implements InvocationHandler, ConnectionInfoIF {

    private static final Log LOG = LogFactory.getLog(ProxyConnection.class);

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

    private static final String CLOSE_METHOD = "close";

    private static final String IS_CLOSED_METHOD = "isClosed";

    private static final String EQUALS_METHOD = "equals";

    protected ProxyConnection(Connection connection, long id, ConnectionPool connectionPool) throws SQLException {
        this.connection = connection;
        setId(id);
        this.connectionPool = connectionPool;
        setBirthTime(System.currentTimeMillis());
        setStatus(STATUS_OFFLINE);

        // We only need to call this for the first connection we make. But it returns really
        // quickly and we don't call it that often so we shouldn't worry.
        connectionPool.initialiseConnectionResetter(connection);

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
            } else if (m.getName().equals(EQUALS_METHOD) && args.length == 1) {
                result = new Boolean(connection.hashCode() == args[0].hashCode());
            } else if (m.getName().equals(IS_CLOSED_METHOD) && args.length == 0) {
                result = new Boolean(getStatus() != STATUS_ACTIVE);
            } else {
                result = m.invoke(connection, args);
            }

            // If we have just made some sort of Statement then we should rather return
            // a proxy instead.
            if (result instanceof Statement) {
                // Work out whether we were passed the sql statement during the
                // call to get the statement object. Sometimes you do, sometimes
                // you don't:
                // connection.prepareCall(sql);
                // connection.createProxyStatement();
                String sqlStatement = null;
                if (args != null && args.length > 0 && args[0] instanceof String) {
                    sqlStatement = (String) args[0];
                }
                result = ProxyFactory.createProxyStatement((Statement) result, connectionPool, sqlStatement);
            }

        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        } catch (Exception e) {
            LOG.error("Unexpected invocation exception", e);
            throw new RuntimeException("Unexpected invocation exception: "
                    + e.getMessage());
        }

        return result;
    }

    /**
     * Close the connection for real
     * @throws SQLException if anything goes wrong
     */
    protected void reallyClose() throws SQLException {
        try {
            connectionPool.registerRemovedConnection(getStatus());
            // Clean up the actual connection
            connection.close();
        } catch (Throwable t) {
            connectionPool.getLog().error("#" + idFormat.format(getId()) + " encountered errors during destruction: " + t);
        }

    }

    /**
     * Find out if the delegated connection is close. Just calling isClosed() on the
     * proxied connection will only indicate whether it is in the pool or not.
     * @return true if the connection is really closed, or if the connection is null
     * @throws SQLException if anything went wrong
     */
    protected boolean isReallyClosed() throws SQLException {
        if (connection == null) {
            return true;
        } else {
            return connection.isClosed();
        }
    }

    /**
     * Doesn't really close the connection, just puts it back in the pool. And tries to
     * reset all the methods that need resetting.
     */
    public void close() throws SQLException {
        try {
            // This call should be as quick as possible. Should we consider only
            // calling it if values have changed? The trouble with that is that it
            // means keeping track when they change and that might be even
            // slower
            connectionPool.resetConnection(connection);
            connectionPool.putConnection(this);
        } catch (Throwable t) {
            connectionPool.getLog().error("#" + idFormat.format(getId()) + " encountered errors during closure: ", t);
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

    /**
     * @see ConnectionInfoIF#getBirthTime
     */
    public long getBirthTime() {
        return birthTime;
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
        this.birthTime = birthTime;
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

    protected Connection getConnection() {
        return connection;
    }

    public String toString() {
        return getId() + " is " + ConnectionPool.getStatusDescription(getStatus());
    }

}

/*
 Revision history:
 $Log: ProxyConnection.java,v $
 Revision 1.13  2002/11/06 20:26:49  billhorsman
 improved doc, added connection resetting, and made
 isClosed() work correctly

 Revision 1.12  2002/11/02 13:57:33  billhorsman
 checkstyle

 Revision 1.11  2002/10/30 21:25:09  billhorsman
 move createStatement into ProxyFactory

 Revision 1.10  2002/10/30 21:19:17  billhorsman
 make use of ProxyFactory

 Revision 1.9  2002/10/28 19:51:34  billhorsman
 Fixed NullPointerException when calling connection.createProxyStatement()

 Revision 1.8  2002/10/28 19:28:25  billhorsman
 checkstyle

 Revision 1.7  2002/10/28 08:20:23  billhorsman
 draft sql dump stuff

 Revision 1.6  2002/10/25 15:59:32  billhorsman
 made non-public where possible

 Revision 1.5  2002/10/24 18:15:09  billhorsman
 removed unnecessary debug

 Revision 1.4  2002/10/17 15:29:18  billhorsman
 fixes so that equals() works

 Revision 1.3  2002/09/19 10:33:57  billhorsman
 added ProxyConnection#toString

 Revision 1.2  2002/09/18 13:48:56  billhorsman
 checkstyle and doc

 Revision 1.1.1.1  2002/09/13 08:13:30  billhorsman
 new

 Revision 1.10  2002/08/24 19:57:15  billhorsman
 checkstyle changes

 Revision 1.9  2002/08/24 19:42:26  billhorsman
 new proxy stuff to work with JDK 1.4

 Revision 1.6  2002/07/02 11:19:08  billhorsman
 layout code and imports

 Revision 1.5  2002/06/28 11:19:47  billhorsman
 improved doc

*/
