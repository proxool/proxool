/*
 * This software is released under the Apache Software Licence. See
 * package.html for details. The latest version is available at
 * http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Delegates to Statement for all calls. But also, for all execute methods, it
 * checks the SQLException and compares it to the fatalSqlException list in the
 * ConnectionPoolDefinition. If it detects a fatal exception it will destroy the
 * Connection so that it isn't used again.
 * @version $Revision: 1.1 $, $Date: 2002/09/13 08:14:09 $
 * @author billhorsman
 * @author $Author: billhorsman $ (current maintainer)
 */
public class ProxyStatement implements Statement {

    private Statement statement;

    private ConnectionPool connectionPool;

    protected Set resultSets = new HashSet();

    public ProxyStatement(Statement statement, ConnectionPool connectionPool) {
        this.statement = statement;
        this.connectionPool = connectionPool;
    }

    private void testException(SQLException e) {
        Iterator i = connectionPool.getDefinition().getFatalSqlExceptions().iterator();
        while (i.hasNext()) {
            if (e.getMessage().indexOf((String) i.next()) > -1) {
                // This SQL exception indicates a fatal problem with this connection. We should probably
                // just junk it.
                try {
                    close();
                    connectionPool.throwConnection(getConnection());
                }
                catch (SQLException e2) {
                }
            }
        }
    }

    public ResultSet executeQuery(String sql) throws SQLException {
        try {
            ResultSet rs = statement.executeQuery(sql);
            resultSets.add(rs);
            return rs;
        }
        catch (SQLException e) {
            testException(e);
            throw e;
        }
    }

    public int executeUpdate(String sql) throws SQLException {
        try {
            return statement.executeUpdate(sql);
        }
        catch (SQLException e) {
            testException(e);
            throw e;
        }
    }

    public void close() throws SQLException {
        statement.close();
    }

    public int getMaxFieldSize() throws SQLException {
        return statement.getMaxFieldSize();
    }

    public void setMaxFieldSize(int max) throws SQLException {
        statement.setMaxFieldSize(max);
    }

    public int getMaxRows() throws SQLException {
        return statement.getMaxRows();
    }

    public void setMaxRows(int max) throws SQLException {
        statement.setMaxFieldSize(max);
    }

    public void setEscapeProcessing(boolean enable) throws SQLException {
        statement.setEscapeProcessing(enable);
    }

    public int getQueryTimeout() throws SQLException {
        return statement.getQueryTimeout();
    }

    public void setQueryTimeout(int seconds) throws SQLException {
        statement.setQueryTimeout(seconds);
    }

    public void cancel() throws SQLException {
        statement.cancel();
    }

    public SQLWarning getWarnings() throws SQLException {
        return statement.getWarnings();
    }

    public void clearWarnings() throws SQLException {
        statement.clearWarnings();
    }

    public void setCursorName(String name) throws SQLException {
        statement.setCursorName(name);
    }

    public boolean execute(String sql) throws SQLException {
        try {
            return statement.execute(sql);
        }
        catch (SQLException e) {
            testException(e);
            throw e;
        }
    }

    public ResultSet getResultSet() throws SQLException {
        ResultSet rs = statement.getResultSet();
        resultSets.add(rs);
        return rs;
    }

    public int getUpdateCount() throws SQLException {
        return statement.getUpdateCount();
    }

    public boolean getMoreResults() throws SQLException {
        return statement.getMoreResults();
    }

    public void setFetchDirection(int direction) throws SQLException {
        statement.setFetchDirection(direction);
    }

    public int getFetchDirection() throws SQLException {
        return statement.getFetchDirection();
    }

    public void setFetchSize(int rows) throws SQLException {
        statement.setFetchSize(rows);
    }

    public int getFetchSize() throws SQLException {
        return statement.getFetchSize();
    }

    public int getResultSetConcurrency() throws SQLException {
        return statement.getResultSetConcurrency();
    }

    public int getResultSetType() throws SQLException {
        return statement.getResultSetType();
    }

    public void addBatch(String sql) throws SQLException {
        statement.addBatch(sql);
    }

    public void clearBatch() throws SQLException {
        statement.clearBatch();
    }

    public int[] executeBatch() throws SQLException {
        try {
            return statement.executeBatch();
        }
        catch (SQLException e) {
            testException(e);
            throw e;
        }
    }

    public Connection getConnection() throws SQLException {
        return statement.getConnection();
    }
}

/*
 Revision history:
 $Log: ProxyStatement.java,v $
 Revision 1.1  2002/09/13 08:14:09  billhorsman
 Initial revision

 Revision 1.5  2002/07/02 11:19:08  billhorsman
 layout code and imports

 Revision 1.4  2002/06/28 11:19:47  billhorsman
 improved doc

*/
