/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Connection;
import java.util.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Contains most of the functionality that we require to manipilate the
 * statement. The subclass of this defines how we delegate to the
 * real statement.
 * @version $Revision: 1.22 $, $Date: 2006/03/03 09:58:26 $
 * @author bill
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.7
 */
abstract class AbstractProxyStatement {

    private static final Log LOG = LogFactory.getLog(ProxyStatement.class);

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd-MMM-yyyy.HH:mm:ss");

    private Statement statement;

    private ConnectionPool connectionPool;

    private ProxyConnectionIF proxyConnection;

    private Map parameters;

    private String sqlStatement;

    private StringBuffer sqlLog = new StringBuffer();

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
     * @param t the exception to test
     */
    protected boolean testException(Throwable t) {
        if (FatalSqlExceptionHelper.testException(connectionPool.getDefinition(), t)) {
            // This SQL exception indicates a fatal problem with this connection. We should probably
            // just junk it.
            try {
                statement.close();
                connectionPool.throwConnection(proxyConnection, "Fatal SQL Exception has been detected");

                // We should check all the existing connections as soon as possible
                HouseKeeperController.sweepNow(connectionPool.getDefinition().getAlias());

                LOG.warn("Connection has been thrown away because fatal exception was detected", t);
            } catch (SQLException e2) {
                LOG.error("Problem trying to throw away suspect connection", e2);
            }
            return true;
        } else {
            return false;
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

    protected Connection getConnection() {
        return ProxyFactory.getWrappedConnection((ProxyConnection) proxyConnection);
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
            parameters.put(key, "NULL");
        } else if (value instanceof String) {
            parameters.put(key, "'" + value + "'");
        } else if (value instanceof Number) {
            parameters.put(key, value);
        } else if (value instanceof Boolean) {
            parameters.put(key, ((Boolean) value).toString());
        } else if (value instanceof Date) {
            parameters.put(key, "'" + getDateAsString((Date) value) + "'");
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

        if (isTrace()) {
            // Log if configured to
            if (connectionPool.getLog().isDebugEnabled() && connectionPool.getDefinition().isTrace()) {
                connectionPool.getLog().debug(sqlLog.toString() + " (" + (System.currentTimeMillis() - startTime) + " milliseconds"
                        + (exception != null ? ", threw a " + exception.getClass().getName()  + ": " + exception.getMessage() + ")" : ")"));
            }
            // Send to any listener
            connectionPool.onExecute(sqlLog.toString(), (System.currentTimeMillis() - startTime), exception);
        }

        // Clear parameters for next time
        if (parameters != null) {
            parameters.clear();
        }
        sqlStatement = null;
        sqlLog.setLength(0);

    }

    protected void startExecute() {
        if (isTrace()) {
            ((ProxyConnection) proxyConnection).addSqlCall(sqlLog.toString());
        }
    }

    /**
     * Get the parameters that have been built up and use them to fill in any parameters
     * withing the sqlStatement and produce a log. If the log already exists (for instance,
     * if a batch is being peformed) then it is appended to the end.
     */
    protected void appendToSqlLog() {
        if (sqlStatement != null && sqlStatement.length() > 0 && isTrace()) {
            int parameterIndex = 0;
            StringTokenizer st = new StringTokenizer(sqlStatement, "?");
            while (st.hasMoreTokens()) {
                if (parameterIndex > 0) {
                    if (parameters != null) {
                        final Object value = parameters.get(new Integer(parameterIndex));
                        if (value != null) {
                            sqlLog.append(value);
                        } else {
                            sqlLog.append("?");
                        }
                    } else {
                            sqlLog.append("?");
                    }
                }
                parameterIndex++;
                sqlLog.append(st.nextToken());
            }
            if (sqlStatement.endsWith("?")) {
                if (parameterIndex > 0) {
                    if (parameters != null) {
                        final Object value = parameters.get(new Integer(parameterIndex));
                        if (value != null) {
                            sqlLog.append(value);
                        } else {
                            sqlLog.append("?");
                        }
                    } else {
                            sqlLog.append("?");
                    }
                }
            }
            if (sqlStatement != null && !sqlStatement.trim().endsWith(";")) {
                sqlLog.append("; ");
            }
        }
        if (parameters != null) {
            parameters.clear();
        }
    }

    protected boolean isTrace() {
        return getConnectionPool().isConnectionListenedTo() || (getConnectionPool().getDefinition().isTrace());
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

    protected static String getDateAsString(Date date) {
        return DATE_FORMAT.format(date);
    }

}


/*
 Revision history:
 $Log: AbstractProxyStatement.java,v $
 Revision 1.22  2006/03/03 09:58:26  billhorsman
 Fix for statement.getConnection(). See bug 1149834.

 Revision 1.21  2006/01/18 14:40:00  billhorsman
 Unbundled Jakarta's Commons Logging.

 Revision 1.20  2005/10/07 08:25:15  billhorsman
 Support new sqlCalls list and isTrace() is now true if the connection pool is being listened to or if trace is on. It no longer depends on the log level. This is because the sqlCalls are available in AdminServlet and not just the logs.

 Revision 1.19  2005/09/26 10:01:31  billhorsman
 Added lastSqlCall when trace is on.

 Revision 1.18  2004/06/02 20:04:54  billhorsman
 Fixed sql log: boolean and date now supported, and last parameter is included

 Revision 1.17  2003/11/04 13:54:02  billhorsman
 checkstyle

 Revision 1.16  2003/10/27 12:21:59  billhorsman
 Optimisation to avoid preparing sql log if tracing is off.

 Revision 1.15  2003/10/27 11:18:42  billhorsman
 Fix for sqlStatement being null.

 Revision 1.14  2003/10/19 09:50:08  billhorsman
 Debug exception displays class name.

 Revision 1.13  2003/10/18 20:44:48  billhorsman
 Better SQL logging (embed parameter values within SQL call) and works properly with batched statements now.

 Revision 1.12  2003/09/30 18:39:07  billhorsman
 New test-before-use, test-after-use and fatal-sql-exception-wrapper-class properties.

 Revision 1.11  2003/09/05 16:26:50  billhorsman
 testException() now returns true if a fatal exception was detected.

 Revision 1.10  2003/03/10 23:43:09  billhorsman
 reapplied checkstyle that i'd inadvertently let
 IntelliJ change...

 Revision 1.9  2003/03/10 15:26:42  billhorsman
 refactoringn of concurrency stuff (and some import
 optimisation)

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