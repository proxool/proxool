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
import java.util.Map;

/**
 * Delegates to CallableStatement for all calls. But also, for all execute methods, it
 * checks the SQLException and compares it to the fatalSqlException list in the
 * ConnectionPoolDefinition. If it detects a fatal exception it will destroy the
 * Connection so that it isn't used again.
 * @version $Revision: 1.4 $, $Date: 2003/01/28 15:20:29 $
 * @author billhorsman
 * @author $Author: billhorsman $ (current maintainer)
 */
public class ProxyCallableStatement extends ProxyPreparedStatement  implements CallableStatement {

    private CallableStatement callableStatement;

    public ProxyCallableStatement(CallableStatement callableStatement, ConnectionPool connectionPool, ProxyConnectionIF proxyConnection, String sqlStatement) {
        super(callableStatement, connectionPool, proxyConnection, sqlStatement);
        this.callableStatement = callableStatement;
    }

    public void registerOutParameter(int parameterIndex, int sqlType)
            throws SQLException {
        callableStatement.registerOutParameter(parameterIndex, sqlType);
    }

    public void registerOutParameter(int parameterIndex, int sqlType, int scale)
            throws SQLException {
        callableStatement.registerOutParameter(parameterIndex, sqlType, scale);
    }

    public boolean wasNull() throws SQLException {
        return callableStatement.wasNull();
    }

    public String getString(int parameterIndex) throws SQLException {
        return callableStatement.getString(parameterIndex);
    }

    public boolean getBoolean(int parameterIndex) throws SQLException {
        return callableStatement.getBoolean(parameterIndex);
    }

    public byte getByte(int parameterIndex) throws SQLException {
        return callableStatement.getByte(parameterIndex);
    }

    public short getShort(int parameterIndex) throws SQLException {
        return callableStatement.getShort(parameterIndex);
    }

    public int getInt(int parameterIndex) throws SQLException {
        return callableStatement.getInt(parameterIndex);
    }

    public long getLong(int parameterIndex) throws SQLException {
        return callableStatement.getLong(parameterIndex);
    }

    public float getFloat(int parameterIndex) throws SQLException {
        return callableStatement.getFloat(parameterIndex);
    }

     public double getDouble(int parameterIndex) throws SQLException {
        return callableStatement.getDouble(parameterIndex);
    }

    /**
     * @deprecated
     */
    public BigDecimal getBigDecimal(int parameterIndex, int scale)
            throws SQLException {
        return callableStatement.getBigDecimal(parameterIndex, scale);
    }

    public byte[] getBytes(int parameterIndex) throws SQLException {
        return callableStatement.getBytes(parameterIndex);
    }

    public Date getDate(int parameterIndex) throws SQLException {
        return callableStatement.getDate(parameterIndex);
    }

    public Time getTime(int parameterIndex) throws SQLException {
        return callableStatement.getTime(parameterIndex);
    }

    public Timestamp getTimestamp(int parameterIndex)
            throws SQLException {
        return callableStatement.getTimestamp(parameterIndex);
    }

    public Object getObject(int parameterIndex) throws SQLException {
        return callableStatement.getObject(parameterIndex);
    }

    public BigDecimal getBigDecimal(int parameterIndex) throws SQLException {
        return callableStatement.getBigDecimal(parameterIndex);
    }

    public Object getObject(int i, Map map) throws SQLException {
        return callableStatement.getObject(i, map);
    }

    public Ref getRef(int i) throws SQLException {
        return callableStatement.getRef(i);
    }

    public Blob getBlob(int i) throws SQLException {
        return callableStatement.getBlob(i);
    }

    public Clob getClob(int i) throws SQLException {
        return callableStatement.getClob(i);
    }

    public Array getArray(int i) throws SQLException {
        return callableStatement.getArray(i);
    }

    public Date getDate(int parameterIndex, Calendar cal)
            throws SQLException {
        return callableStatement.getDate(parameterIndex, cal);
    }

    public Time getTime(int parameterIndex, Calendar cal)
            throws SQLException {
        return callableStatement.getTime(parameterIndex, cal);
    }

    public Timestamp getTimestamp(int parameterIndex, Calendar cal)
            throws SQLException {
        return callableStatement.getTimestamp(parameterIndex, cal);
    }

    public void registerOutParameter(int paramIndex, int sqlType, String typeName)
            throws SQLException {
        callableStatement.registerOutParameter(paramIndex, sqlType, typeName);
    }

}

/*
 Revision history:
 $Log: ProxyCallableStatement.java,v $
 Revision 1.4  2003/01/28 15:20:29  billhorsman
 added tracing

 Revision 1.3  2003/01/28 11:55:04  billhorsman
 new JDK 1.2 patches (functioning but not complete)

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
