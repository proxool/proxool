/*
* Copyright 2002, Findexa AS (http://www.findexa.no)
*
* This software is the proprietary information of Findexa AS.
* Use is subject to license terms.
*/
package org.logicalcobwebs.proxool;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.sql.Statement;
import java.lang.reflect.Proxy;

/**
 * <link rel="stylesheet" href="{@docRoot}/cg.css" type="text/css">
 *
 * A central place to build proxy objects ({@link ProxyConnection connections}
 * and {@link ProxyStatement statements}).
 *
 * @version $Revision: 1.3 $, $Date: 2002/11/02 14:22:15 $
 * @author Bill Horsman (bill@logicalcobwebs.co.uk)
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.5
 */
class ProxyFactory {

    protected static ProxyConnection buildProxyConnection(long id, ConnectionPool connectionPool) throws SQLException {
        Connection realConnection = null;
        realConnection = DriverManager.getConnection(
                connectionPool.getDefinition().getUrl(),
                connectionPool.getDefinition().getProperties());

        Object delegate = Proxy.newProxyInstance(
                realConnection.getClass().getClassLoader(),
                realConnection.getClass().getInterfaces(),
                new ProxyConnection(realConnection, id, connectionPool));

        return (ProxyConnection) Proxy.getInvocationHandler(delegate);
    }

    /**
     * Get a Connection from the ProxyConnection
     *
     * @param proxyConnection where to find the connection
     * @return
     */
    protected static Connection getConnection(ProxyConnection proxyConnection) {
        return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class[]{Connection.class},
                proxyConnection);
    }

    protected static Object createProxyStatement(Statement delegate, ConnectionPool connectionPool, String sqlStatement) {
        return Proxy.newProxyInstance(delegate.getClass().getClassLoader(), delegate.getClass().getInterfaces(), new ProxyStatement(delegate, connectionPool, sqlStatement));
    }


}

/*
 Revision history:
 $Log: ProxyFactory.java,v $
 Revision 1.3  2002/11/02 14:22:15  billhorsman
 Documentation

 Revision 1.2  2002/10/30 21:25:08  billhorsman
 move createStatement into ProxyFactory

 Revision 1.1  2002/10/30 21:19:16  billhorsman
 make use of ProxyFactory

*/
