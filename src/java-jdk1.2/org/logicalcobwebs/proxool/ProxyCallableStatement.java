/*
 * This software is released under the Apache Software Licence. See
 * package.html for details. The latest version is available at
 * http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Map;

/**
 * Delegates to CallableStatement for all calls. But also, for all execute methods, it
 * checks the SQLException and compares it to the fatalSqlException list in the
 * ConnectionPoolDefinition. If it detects a fatal exception it will destroy the
 * Connection so that it isn't used again.
 * @version $Revision: 1.2 $, $Date: 2002/09/18 13:47:14 $
 * @author billhorsman
 * @author $Author: billhorsman $ (current maintainer)
 */
public class ProxyCallableStatement implements CallableStatement {

    private CallableStatement callableStatement;

    private ConnectionPool connectionPool;

    public ProxyCallableStatement(CallableStatement callableStatement, ConnectionPool connectionPool) {
        this.callableStatement = callableStatement;
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
                } catch (SQLException e2) {
                    connectionPool.getLog().debug("Couldn't close statement after detecting fatal exception", e2);
                }
            }
        }
    }

    public void registerOutParameter(int parameterIndex, int sqlType)
            throws SQLException {
        callableStatement.registerOutParameter(parameterIndex, sqlType);
    }

    public ResultSet executeQuery() throws SQLException {
        try {
            return callableStatement.executeQuery();
        } catch (SQLException e) {
            testException(e);
            throw e;
        }
    }

    public ResultSet executeQuery(String sql) throws SQLException {
        try {
            return callableStatement.executeQuery(sql);
        } catch (SQLException e) {
            testException(e);
            throw e;
        }
    }

    public void registerOutParameter(int parameterIndex, int sqlType, int scale)
            throws SQLException {
        callableStatement.registerOutParameter(parameterIndex, sqlType, scale);
    }

    public int executeUpdate() throws SQLException {
        try {
            return callableStatement.executeUpdate();
        } catch (SQLException e) {
            testException(e);
            throw e;
        }
    }

    public int executeUpdate(String sql) throws SQLException {
        try {
            return callableStatement.executeUpdate(sql);
        } catch (SQLException e) {
            testException(e);
            throw e;
        }
    }

    public boolean wasNull() throws SQLException {
        return callableStatement.wasNull();
    }

    public void setNull(int parameterIndex, int sqlType) throws SQLException {
        callableStatement.setNull(parameterIndex, sqlType);
    }

    public void close() throws SQLException {
        callableStatement.close();
    }

    public String getString(int parameterIndex) throws SQLException {
        return callableStatement.getString(parameterIndex);
    }

    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
        callableStatement.setBoolean(parameterIndex, x);
    }

    public int getMaxFieldSize() throws SQLException {
        return callableStatement.getMaxFieldSize();
    }

    public boolean getBoolean(int parameterIndex) throws SQLException {
        return callableStatement.getBoolean(parameterIndex);
    }

    public void setByte(int parameterIndex, byte x) throws SQLException {
        callableStatement.setByte(parameterIndex, x);
    }

    public void setMaxFieldSize(int max) throws SQLException {
        callableStatement.setMaxFieldSize(max);
    }

    public byte getByte(int parameterIndex) throws SQLException {
        return callableStatement.getByte(parameterIndex);
    }

    public void setShort(int parameterIndex, short x) throws SQLException {
        callableStatement.setShort(parameterIndex, x);
    }

    public int getMaxRows() throws SQLException {
        return callableStatement.getMaxRows();
    }

    public short getShort(int parameterIndex) throws SQLException {
        return callableStatement.getShort(parameterIndex);
    }

    public void setInt(int parameterIndex, int x) throws SQLException {
        callableStatement.setInt(parameterIndex, x);
    }

    public void setMaxRows(int max) throws SQLException {
        callableStatement.setMaxRows(max);
    }

    public int getInt(int parameterIndex) throws SQLException {
        return callableStatement.getInt(parameterIndex);
    }

    public void setLong(int parameterIndex, long x) throws SQLException {
        callableStatement.setLong(parameterIndex, x);
    }

    public void setEscapeProcessing(boolean enable) throws SQLException {
        callableStatement.setEscapeProcessing(enable);
    }

    public long getLong(int parameterIndex) throws SQLException {
        return callableStatement.getLong(parameterIndex);
    }

    public void setFloat(int parameterIndex, float x) throws SQLException {
        callableStatement.setFloat(parameterIndex, x);
    }

    public int getQueryTimeout() throws SQLException {
        return callableStatement.getQueryTimeout();
    }

    public float getFloat(int parameterIndex) throws SQLException {
        return callableStatement.getFloat(parameterIndex);
    }

    public void setDouble(int parameterIndex, double x) throws SQLException {
        callableStatement.setDouble(parameterIndex, x);
    }

    public void setQueryTimeout(int seconds) throws SQLException {
        callableStatement.setQueryTimeout(seconds);
    }

    public double getDouble(int parameterIndex) throws SQLException {
        return callableStatement.getDouble(parameterIndex);
    }

    public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
        callableStatement.setBigDecimal(parameterIndex, x);
    }

    public void cancel() throws SQLException {
        callableStatement.cancel();
    }

    /**
     * @deprecated
     */
    public BigDecimal getBigDecimal(int parameterIndex, int scale)
            throws SQLException {
        return callableStatement.getBigDecimal(parameterIndex, scale);
    }

    public void setString(int parameterIndex, String x) throws SQLException {
        callableStatement.setString(parameterIndex, x);
    }

    public SQLWarning getWarnings() throws SQLException {
        return callableStatement.getWarnings();
    }

    public byte[] getBytes(int parameterIndex) throws SQLException {
        return callableStatement.getBytes(parameterIndex);
    }

    public void setBytes(int parameterIndex, byte x[]) throws SQLException {
        callableStatement.setBytes(parameterIndex, x);
    }

    public void clearWarnings() throws SQLException {
        callableStatement.clearWarnings();
    }

    public Date getDate(int parameterIndex) throws SQLException {
        return callableStatement.getDate(parameterIndex);
    }

    public void setDate(int parameterIndex, Date x)
            throws SQLException {
        callableStatement.setDate(parameterIndex, x);
    }

    public void setCursorName(String name) throws SQLException {
        callableStatement.setCursorName(name);
    }

    public Time getTime(int parameterIndex) throws SQLException {
        return callableStatement.getTime(parameterIndex);
    }

    public void setTime(int parameterIndex, Time x)
            throws SQLException {
        callableStatement.setTime(parameterIndex, x);
    }

    public boolean execute(String sql) throws SQLException {
        try {
            return callableStatement.execute(sql);
        } catch (SQLException e) {
            testException(e);
            throw e;
        }
    }

    public Timestamp getTimestamp(int parameterIndex)
            throws SQLException {
        return callableStatement.getTimestamp(parameterIndex);
    }

    public void setTimestamp(int parameterIndex, Timestamp x)
            throws SQLException {
        callableStatement.setTimestamp(parameterIndex, x);
    }

    public ResultSet getResultSet() throws SQLException {
        return callableStatement.getResultSet();
    }

    public Object getObject(int parameterIndex) throws SQLException {
        return callableStatement.getObject(parameterIndex);
    }

    public void setAsciiStream(int parameterIndex, InputStream x, int length)
            throws SQLException {
        callableStatement.setAsciiStream(parameterIndex, x, length);
    }

    public int getUpdateCount() throws SQLException {
        return callableStatement.getUpdateCount();
    }

    public BigDecimal getBigDecimal(int parameterIndex) throws SQLException {
        return callableStatement.getBigDecimal(parameterIndex);
    }

    /**
     * @deprecated
     */
    public void setUnicodeStream(int parameterIndex, InputStream x,
                                 int length) throws SQLException {
        callableStatement.setUnicodeStream(parameterIndex, x, length);
    }

    public boolean getMoreResults() throws SQLException {
        return callableStatement.getMoreResults();
    }

    public Object getObject(int i, Map map) throws SQLException {
        return callableStatement.getObject(i, map);
    }

    public void setBinaryStream(int parameterIndex, InputStream x,
                                int length) throws SQLException {
        callableStatement.setBinaryStream(parameterIndex, x, length);
    }

    public void setFetchDirection(int direction) throws SQLException {
        callableStatement.setFetchDirection(direction);
    }

    public Ref getRef(int i) throws SQLException {
        return callableStatement.getRef(i);
    }

    public void clearParameters() throws SQLException {
        callableStatement.clearParameters();
    }

    public int getFetchDirection() throws SQLException {
        return callableStatement.getFetchDirection();
    }

    public Blob getBlob(int i) throws SQLException {
        return callableStatement.getBlob(i);
    }

    public void setObject(int parameterIndex, Object x, int targetSqlType, int scale)
            throws SQLException {
        callableStatement.setObject(parameterIndex, x, targetSqlType, scale);
    }

    public void setFetchSize(int rows) throws SQLException {
        callableStatement.setFetchSize(rows);
    }

    public Clob getClob(int i) throws SQLException {
        return callableStatement.getClob(i);
    }

    public void setObject(int parameterIndex, Object x, int targetSqlType)
            throws SQLException {
        callableStatement.setObject(parameterIndex, x, targetSqlType);
    }

    public int getFetchSize() throws SQLException {
        return callableStatement.getFetchSize();
    }

    public Array getArray(int i) throws SQLException {
        return callableStatement.getArray(i);
    }

    public void setObject(int parameterIndex, Object x) throws SQLException {
        callableStatement.setObject(parameterIndex, x);
    }

    public int getResultSetConcurrency() throws SQLException {
        return callableStatement.getResultSetConcurrency();
    }

    public Date getDate(int parameterIndex, Calendar cal)
            throws SQLException {
        return callableStatement.getDate(parameterIndex, cal);
    }

    public boolean execute() throws SQLException {
        try {
            return callableStatement.execute();
        } catch (SQLException e) {
            testException(e);
            throw e;
        }
    }

    public int getResultSetType() throws SQLException {
        return callableStatement.getResultSetType();
    }

    public Time getTime(int parameterIndex, Calendar cal)
            throws SQLException {
        return callableStatement.getTime(parameterIndex, cal);
    }

    public void addBatch() throws SQLException {
        callableStatement.addBatch();
    }

    public void addBatch(String sql) throws SQLException {
        callableStatement.addBatch(sql);
    }

    public Timestamp getTimestamp(int parameterIndex, Calendar cal)
            throws SQLException {
        return callableStatement.getTimestamp(parameterIndex, cal);
    }

    public void setCharacterStream(int parameterIndex,
                                   Reader reader,
                                   int length) throws SQLException {
        callableStatement.setCharacterStream(parameterIndex, reader, length);
    }

    public void clearBatch() throws SQLException {
        callableStatement.clearBatch();
    }

    public void registerOutParameter(int paramIndex, int sqlType, String typeName)
            throws SQLException {
        callableStatement.registerOutParameter(paramIndex, sqlType, typeName);
    }

    public void setRef(int i, Ref x) throws SQLException {
        callableStatement.setRef(i, x);
    }

    public int[] executeBatch() throws SQLException {
        try {
            return callableStatement.executeBatch();
        } catch (SQLException e) {
            testException(e);
            throw e;
        }
    }

    public void setBlob(int i, Blob x) throws SQLException {
        callableStatement.setBlob(i, x);
    }

    public Connection getConnection() throws SQLException {
        return callableStatement.getConnection();
    }

    public void setClob(int i, Clob x) throws SQLException {
        callableStatement.setClob(i, x);
    }

    public void setArray(int i, Array x) throws SQLException {
        callableStatement.setArray(i, x);
    }

    public ResultSetMetaData getMetaData() throws SQLException {
        return callableStatement.getMetaData();
    }

    public void setDate(int parameterIndex, Date x, Calendar cal)
            throws SQLException {
        callableStatement.setDate(parameterIndex, x, cal);
    }

    public void setTime(int parameterIndex, Time x, Calendar cal)
            throws SQLException {
        callableStatement.setTime(parameterIndex, x, cal);
    }

    public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal)
            throws SQLException {
        callableStatement.setTimestamp(parameterIndex, x, cal);
    }

    public void setNull(int paramIndex, int sqlType, String typeName)
            throws SQLException {
        callableStatement.setNull(paramIndex, sqlType, typeName);
    }

}

/*
 Revision history:
 $Log: ProxyCallableStatement.java,v $
 Revision 1.2  2002/09/18 13:47:14  billhorsman
 fixes for new logging

 Revision 1.1.1.1  2002/09/13 08:14:00  billhorsman
 new

 Revision 1.6  2002/07/02 11:19:08  billhorsman
 layout code and imports

 Revision 1.5  2002/07/02 08:27:47  billhorsman
 bug fix when settiong definition, displayStatistics now available to ProxoolFacade, prototyper no longer attempts to make connections when maximum is reached

 Revision 1.4  2002/06/28 11:19:47  billhorsman
 improved doc

*/
