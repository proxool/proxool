/*
 * This software is released under the Apache Software Licence. See
 * package.html for details. The latest version is available at
 * http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import org.logicalcobwebs.proxool.ConnectionPool;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;

/**
 * Delegates to Statement for all calls. But also, for all execute methods, it
 * checks the SQLException and compares it to the fatalSqlException list in the
 * ConnectionPoolDefinition. If it detects a fatal exception it will destroy the
 * Connection so that it isn't used again.
 * @version $Revision: 1.4 $, $Date: 2003/01/28 15:20:30 $
 * @author billhorsman
 * @author $Author: billhorsman $ (current maintainer)
 */
public class ProxyStatement extends AbstractProxyStatement  implements Statement {

    private static final Log LOG = LogFactory.getLog(ProxyStatement.class);

    public ProxyStatement(Statement statement, ConnectionPool connectionPool, ProxyConnectionIF proxyConnection, String sqlStatement) {
        super(statement, connectionPool, proxyConnection, sqlStatement);
    }

    public ResultSet executeQuery(String sql) throws SQLException {
        long startTime = System.currentTimeMillis();
        Exception exception = null;
        try {
            return getStatement().executeQuery(sql);
        } catch (SQLException e) {
            testException(e);
            exception = e;
            throw e;
        } finally {
            trace(startTime, exception);
        }
    }

    public int executeUpdate(String sql) throws SQLException {
        long startTime = System.currentTimeMillis();
        Exception exception = null;
        try {
            return getStatement().executeUpdate(sql);
        } catch (SQLException e) {
            testException(e);
            exception = e;
            throw e;
        } finally {
            trace(startTime, exception);
        }
    }

    public int[] executeBatch() throws SQLException {
        long startTime = System.currentTimeMillis();
        Exception exception = null;
        try {
            return getStatement().executeBatch();
        } catch (SQLException e) {
            testException(e);
            exception = e;
            throw e;
        } finally {
            trace(startTime, exception);
        }
    }

    public int getMaxFieldSize() throws SQLException {
        return getStatement().getMaxFieldSize();
    }

    public void setMaxFieldSize(int max) throws SQLException {
        getStatement().setMaxFieldSize(max);
    }

    public int getMaxRows() throws SQLException {
        return getStatement().getMaxRows();
    }

    public void setMaxRows(int max) throws SQLException {
        getStatement().setMaxFieldSize(max);
    }

    public void setEscapeProcessing(boolean enable) throws SQLException {
        getStatement().setEscapeProcessing(enable);
    }

    public int getQueryTimeout() throws SQLException {
        return getStatement().getQueryTimeout();
    }

    public void setQueryTimeout(int seconds) throws SQLException {
        getStatement().setQueryTimeout(seconds);
    }

    public void cancel() throws SQLException {
        getStatement().cancel();
    }

    public SQLWarning getWarnings() throws SQLException {
        return getStatement().getWarnings();
    }

    public void clearWarnings() throws SQLException {
        getStatement().clearWarnings();
    }

    public void setCursorName(String name) throws SQLException {
        getStatement().setCursorName(name);
    }

    public boolean execute(String sql) throws SQLException {
        try {
            return getStatement().execute(sql);
        } catch (SQLException e) {
            testException(e);
            throw e;
        }
    }

    public ResultSet getResultSet() throws SQLException {
        ResultSet rs = getStatement().getResultSet();
        return rs;
    }

    public int getUpdateCount() throws SQLException {
        return getStatement().getUpdateCount();
    }

    public boolean getMoreResults() throws SQLException {
        return getStatement().getMoreResults();
    }

    public void setFetchDirection(int direction) throws SQLException {
        getStatement().setFetchDirection(direction);
    }

    public int getFetchDirection() throws SQLException {
        return getStatement().getFetchDirection();
    }

    public void setFetchSize(int rows) throws SQLException {
        getStatement().setFetchSize(rows);
    }

    public int getFetchSize() throws SQLException {
        return getStatement().getFetchSize();
    }

    public int getResultSetConcurrency() throws SQLException {
        return getStatement().getResultSetConcurrency();
    }

    public int getResultSetType() throws SQLException {
        return getStatement().getResultSetType();
    }

    public void addBatch(String sql) throws SQLException {
        getStatement().addBatch(sql);
    }

    public void clearBatch() throws SQLException {
        getStatement().clearBatch();
    }

    public Connection getConnection() throws SQLException {
        return getStatement().getConnection();
    }

}

/*
 Revision history:
 $Log: ProxyStatement.java,v $
 Revision 1.4  2003/01/28 15:20:30  billhorsman
 added tracing

 Revision 1.3  2003/01/28 11:55:04  billhorsman
 new JDK 1.2 patches (functioning but not complete)

 Revision 1.2  2002/09/18 13:47:14  billhorsman
 fixes for new logging

 Revision 1.1.1.1  2002/09/13 08:14:09  billhorsman
 new

 Revision 1.5  2002/07/02 11:19:08  billhorsman
 layout code and imports

 Revision 1.4  2002/06/28 11:19:47  billhorsman
 improved doc

*/
