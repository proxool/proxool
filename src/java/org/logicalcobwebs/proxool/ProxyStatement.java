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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Delegates to Statement for all calls. But also, for all execute methods, it
 * checks the SQLException and compares it to the fatalSqlException list in the
 * ConnectionPoolDefinition. If it detects a fatal exception it will destroy the
 * Connection so that it isn't used again.
 * @version $Revision: 1.1 $, $Date: 2002/09/13 08:13:30 $
 * @author billhorsman
 * @author $Author: billhorsman $ (current maintainer)
 */
public class ProxyStatement implements InvocationHandler {

    private static final Log LOG = LogFactory.getLog(ProxoolFacade.class);

    private Statement statement;

    private ConnectionPool connectionPool;

    private Set resultSets = new HashSet();

    private static final String EXECUTE_FRAGMENT = "execute";

    private static final String NOT_IMPLEMENTED = "not implemented";

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
                    statement.close();
                    connectionPool.throwConnection(getConnection());
                    LOG.warn("Connection has been thrown away because fatal exception was detected", e);
                } catch (SQLException e2) {
                    LOG.error("Problem trying to throw away suspect connection", e2);
                }
            }
        }
    }

    public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {
        Object result = null;
        long startTime = System.currentTimeMillis();

        // We need to remember an exceptions that get thrown so that we can optionally
        // pass them to the onExecute() call below
        Exception exception = null;
        try {
            result = method.invoke(statement, args);
        } catch (InvocationTargetException e) {
            exception = e;
            throw e.getTargetException();
        } catch (Exception e) {
            exception = e;
            if (e instanceof SQLException) {
                testException((SQLException) e);
                throw e;
            } else {
                throw new RuntimeException("unexpected invocation exception: "
                        + e.getMessage());
            }
        } finally {

            // If we executed something then we should tell the listener.
            if (method.getName().startsWith(EXECUTE_FRAGMENT)) {
                connectionPool.onExecute(NOT_IMPLEMENTED, (System.currentTimeMillis() - startTime), exception);
            }

        }

        if (result instanceof ResultSet) {
            resultSets.add(result);
        }

        return result;
    }

    private Connection getConnection() throws SQLException {
        return statement.getConnection();
    }
}

/*
 Revision history:
 $Log: ProxyStatement.java,v $
 Revision 1.1  2002/09/13 08:13:30  billhorsman
 Initial revision

 Revision 1.8  2002/08/24 19:57:15  billhorsman
 checkstyle changes

 Revision 1.7  2002/08/24 19:42:26  billhorsman
 new proxy stuff to work with JDK 1.4

 Revision 1.6  2002/07/10 16:14:47  billhorsman
 widespread layout changes and move constants into ProxoolConstants

 Revision 1.5  2002/07/02 11:19:08  billhorsman
 layout code and imports

 Revision 1.4  2002/06/28 11:19:47  billhorsman
 improved doc

*/
