/*
 * This software is released under the Apache Software Licence. See
 * package.html for details. The latest version is available at
 * http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.Map;

/**
 * Delegates to a normal Coonection for everything but the close()
 * method (when it puts itself back into the pool instead).
 * @version $Revision: 1.6 $, $Date: 2003/02/12 12:46:30 $
 * @author billhorsman
 * @author $Author: billhorsman $ (current maintainer)
 */
public class ProxyConnection extends AbstractProxyConnection implements Connection {

    public ProxyConnection(Connection connection, long id, String delegateUrl, ConnectionPool connectionPool) throws SQLException {
        super(connection, id, delegateUrl, connectionPool);
    }

    /**
     * Delegates to {@link Connection#createStatement}
     */
    public Statement createStatement() throws SQLException {
        final Statement statement = getConnection().createStatement();
        addOpenStatement(statement);
        return ProxyFactory.createProxyStatement(statement, getConnectionPool(), this, null);
    }

    /**
     * Delegates to {@link Connection#prepareStatement}
     */
    public PreparedStatement prepareStatement(String sql)
            throws SQLException {
        final PreparedStatement statement = getConnection().prepareStatement(sql);
        addOpenStatement(statement);
        return (PreparedStatement) ProxyFactory.createProxyStatement(statement, getConnectionPool(), this, sql);
    }

    /**
     * Delegates to {@link Connection#prepareCall}
     */
    public CallableStatement prepareCall(String sql) throws SQLException {
        final CallableStatement statement = getConnection().prepareCall(sql);
        addOpenStatement(statement);
        return (CallableStatement) ProxyFactory.createProxyStatement(statement, getConnectionPool(), this, sql);
    }

    /**
     * Delegates to {@link Connection#nativeSQL}
     */
    public String nativeSQL(String sql) throws SQLException {
        return getConnection().nativeSQL(sql);
    }

    /**
     * Delegates to {@link Connection#setAutoCommit}
     */
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        setNeedToReset(true);
        getConnection().setAutoCommit(autoCommit);
    }

    /**
     * Delegates to {@link Connection#getAutoCommit}
     */
    public boolean getAutoCommit() throws SQLException {
        return getConnection().getAutoCommit();
    }

    /**
     * Delegates to {@link Connection#commit}
     */
    public void commit() throws SQLException {
        getConnection().commit();
    }

    /**
     * Delegates to {@link Connection#rollback}
     */
    public void rollback() throws SQLException {
        getConnection().rollback();
    }

    /**
     * Delegates to {@link Connection#setReadOnly}
     */
    public void setReadOnly(boolean readOnly) throws SQLException {
        setNeedToReset(true);
        getConnection().setReadOnly(readOnly);
    }

    /**
     * Delegates to {@link Connection#isReadOnly}
     */
    public boolean isReadOnly() throws SQLException {
        return getConnection().isReadOnly();
    }

    /**
     * Delegates to {@link Connection#setCatalog}
     */
    public void setCatalog(String catalog) throws SQLException {
        setNeedToReset(true);
        getConnection().setCatalog(catalog);
    }

    /**
     * Delegates to {@link Connection#getCatalog}
     */
    public String getCatalog() throws SQLException {
        return getConnection().getCatalog();
    }

    /**
     * Delegates to {@link Connection#setTransactionIsolation}
     */
    public void setTransactionIsolation(int level) throws SQLException {
        setNeedToReset(true);
        getConnection().setTransactionIsolation(level);
    }

    /**
     * Delegates to {@link Connection#getTransactionIsolation}
     */
    public int getTransactionIsolation() throws SQLException {
        return getConnection().getTransactionIsolation();
    }

    /**
     * Delegates to {@link Connection#getWarnings}
     */
    public SQLWarning getWarnings() throws SQLException {
        return getConnection().getWarnings();
    }

    /**
     * Delegates to {@link Connection#clearWarnings}
     */
    public void clearWarnings() throws SQLException {
        getConnection().clearWarnings();
    }

    /**
     * Delegates to {@link Connection#createStatement(int, int)}
     */
    public Statement createStatement(int resultSetType, int resultSetConcurrency)
            throws SQLException {
        final Statement statement = getConnection().createStatement(resultSetType, resultSetConcurrency);
        addOpenStatement(statement);
        return ProxyFactory.createProxyStatement(statement, getConnectionPool(), this, null);
    }

    /**
     * Delegates to {@link Connection#prepareStatement(String, int, int)}
     */
    public PreparedStatement prepareStatement(String sql, int resultSetType,
                                              int resultSetConcurrency)
            throws SQLException {
        final PreparedStatement statement = getConnection().prepareStatement(sql, resultSetType, resultSetConcurrency);
        addOpenStatement(statement);
        return (PreparedStatement) ProxyFactory.createProxyStatement(statement, getConnectionPool(), this, sql);
    }

    /**
     * Delegates to {@link Connection#prepareCall(String, int, int)}
     */
    public CallableStatement prepareCall(String sql, int resultSetType,
                                         int resultSetConcurrency) throws SQLException {
        final CallableStatement statement = getConnection().prepareCall(sql, resultSetType, resultSetConcurrency);
        addOpenStatement(statement);
        return (CallableStatement) ProxyFactory.createProxyStatement(statement, getConnectionPool(), this, sql);
    }

    /**
     * Delegates to {@link Connection#getTypeMap}
     */
    public Map getTypeMap() throws SQLException {
        return getConnection().getTypeMap();
    }

    /**
     * Delegates to {@link Connection#setTypeMap}
     */
    public void setTypeMap(Map map) throws SQLException {
        setNeedToReset(true);
        getConnection().setTypeMap(map);
    }

}

/*
 Revision history:
 $Log: ProxyConnection.java,v $
 Revision 1.6  2003/02/12 12:46:30  billhorsman
 brought up to date with recent changes in main source

 Revision 1.5  2003/01/31 16:53:25  billhorsman
 checkstyle

 Revision 1.4  2003/01/31 14:33:19  billhorsman
 fix for DatabaseMetaData

 Revision 1.3  2003/01/28 11:55:03  billhorsman
 new JDK 1.2 patches (functioning but not complete)

 Revision 1.2  2002/09/18 13:47:14  billhorsman
 fixes for new logging

 Revision 1.1.1.1  2002/09/13 08:14:03  billhorsman
 new

 Revision 1.6  2002/07/02 11:19:08  billhorsman
 layout code and imports

 Revision 1.5  2002/06/28 11:19:47  billhorsman
 improved doc

*/
