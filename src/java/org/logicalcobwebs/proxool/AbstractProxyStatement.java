/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import org.logicalcobwebs.logging.Log;
import org.logicalcobwebs.logging.LogFactory;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

/**
 * Contains most of the functionality that we require to manipilate the
 * statement. The subclass of this defines how we delegate to the
 * real statement.

 * @version $Revision: 1.8 $, $Date: 2003/03/05 18:42:32 $
 * @author bill
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.7
 */
abstract class AbstractProxyStatement {

    private static final Log LOG = LogFactory.getLog(ProxyStatement.class);

    private Statement statement;

    private ConnectionPool connectionPool;

    private ProxyConnectionIF proxyConnection;

    private Map parameters;

    private String sqlStatement;

    /**
     * @param statement the real statement that we will delegate to
     * @param connectionPool the connection pool that we are using
     * @param proxyConnection the connection that was used to create the statement
     * @param sqlStatement the SQL statement that was used to create this statement
     * (optional, can be null) so that we can use if for tracing.
     */
    public AbstractProxyStatement(Statement statement, ConnectionPool connectionPool, ProxyConnectionIF proxyConnection, String sqlStatement) {
        this.statement = statement;
        this.connectionPool = connectionPool;
        this.proxyConnection = proxyConnection;
        this.sqlStatement = sqlStatement;
    }

    /**
     * Check to see whether an exception is a fatal one. If it is, then throw the connection
     * away (and it won't be made available again)
     * @param e the exception to test
     */
    protected void testException(SQLException e) {
        Iterator i = connectionPool.getDefinition().getFatalSqlExceptions().iterator();
        while (i.hasNext()) {
            if (e.getMessage().indexOf((String) i.next()) > -1) {
                // This SQL exception indicates a fatal problem with this connection. We should probably
                // just junk it.
                try {
                    statement.close();
                    connectionPool.throwConnection(proxyConnection, "Fatal SQL Exception has been detected");

                    // We should check all the existing connections as soon as possible
                    HouseKeeperController.sweepNow(connectionPool.getDefinition().getAlias());
                    
                    LOG.warn("Connection has been thrown away because fatal exception was detected", e);
                } catch (SQLException e2) {
                    LOG.error("Problem trying to throw away suspect connection", e2);
                }
            }
        }
    }

    /**
     * Gets the real Statement that we got from the delegate driver
     * @return delegate statement
     */
    public Statement getDelegateStatement() {
        return statement;
    }

    /**
     * The connection pool we are using
     * @return connectionPool
     */
    protected ConnectionPool getConnectionPool() {
        return connectionPool;
    }

    /**
     * The real, delegate statement
     * @return statement
     */
    protected Statement getStatement() {
        return statement;
    }

    /**
     * Close the statement and tell the ProxyConnection that it did so.
     * @throws SQLException if it couldn't be closed
     * @see ProxyConnectionIF#registerClosedStatement
     */
    public void close() throws SQLException {
        statement.close();
        proxyConnection.registerClosedStatement(statement);
    }

    /**
     * Whether the delegate statements are the same
     * @see Object#equals
     */
    public boolean equals(Object obj) {
        return (statement.hashCode() == obj.hashCode());
    }

    /**
     * Add a parameter so that we can show its value when tracing
     * @param index within the procedure
     * @param value an object describing its value
     */
    protected void putParameter(int index, Object value) {

        // Lazily instantiate parameters if necessary
        if (parameters == null) {
            parameters = new TreeMap(new Comparator() {
                public int compare(Object o1, Object o2) {
                    int c = 0;

                    if (o1 instanceof Integer && o2 instanceof Integer) {
                        c = ((Integer) o1).compareTo(((Integer) o2));
                    }

                    return c;
                }
            });
        }

        Object key = new Integer(index);
        if (value == null) {
            parameters.put(key, "*");
        } else if (value instanceof String) {
            parameters.put(key, "'" + value + "'");
        } else if (value instanceof Number) {
            parameters.put(key, value);
        } else {
            String className = value.getClass().getName();
            StringTokenizer st = new StringTokenizer(className, ".");
            while (st.hasMoreTokens()) {
                className = st.nextToken();
            }
            parameters.put(key, className);
        }
    }

    /**
     * Trace the call that was just made
     * @param startTime so we can log how long it took
     * @param exception if anything went wrong during execution
     * @throws SQLException if the {@link ConnectionPool#onExecute onExecute} method threw one.
     */
    protected void trace(long startTime, Exception exception) throws SQLException {

        // Log if configured to
        if (connectionPool.getLog().isDebugEnabled() && connectionPool.getDefinition().isTrace()) {
            if (parameters != null) {
                connectionPool.getLog().debug(parameters + " -> " + sqlStatement + " (" + (System.currentTimeMillis() - startTime) + " milliseconds)");
            } else {
                connectionPool.getLog().debug(sqlStatement + " (" + (System.currentTimeMillis() - startTime) + " milliseconds)");
            }
        }

        // Send to any listener
        connectionPool.onExecute(parameters + " -> " + sqlStatement, (System.currentTimeMillis() - startTime), exception);

        // Clear parameters for next time
        if (parameters != null) {
            parameters.clear();
        }
        sqlStatement = null;

    }

    protected boolean isTrace() {
        boolean isTrace = getConnectionPool().isConnectionListenedTo() || (getConnectionPool().getDefinition().isTrace() && getConnectionPool().getLog().isDebugEnabled());
        return isTrace;
    }

    /**
     * Sets sqlStatement if it isn't already set
     * @param sqlStatement the statement we are sending the database
     */
    protected void setSqlStatementIfNull(String sqlStatement) {
        if (this.sqlStatement == null) {
            this.sqlStatement = sqlStatement;
        }
    }
}


/*
 Revision history:
 $Log: AbstractProxyStatement.java,v $
 Revision 1.8  2003/03/05 18:42:32  billhorsman
 big refactor of prototyping and house keeping to
 drastically reduce the number of threads when using
 many pools

 Revision 1.7  2003/03/03 11:11:56  billhorsman
 fixed licence

 Revision 1.6  2003/02/26 16:05:52  billhorsman
 widespread changes caused by refactoring the way we
 update and redefine pool definitions.

 Revision 1.5  2003/02/19 22:38:32  billhorsman
 fatal sql exception causes house keeper to run
 immediately

 Revision 1.4  2003/02/13 17:06:42  billhorsman
 allow for sqlStatement in execute() method

 Revision 1.3  2003/02/06 17:41:04  billhorsman
 now uses imported logging

 Revision 1.2  2003/01/28 11:47:08  billhorsman
 new isTrace() and made close() public

 Revision 1.1  2003/01/27 18:26:35  billhorsman
 refactoring of ProxyConnection and ProxyStatement to
 make it easier to write JDK 1.2 patch

 */