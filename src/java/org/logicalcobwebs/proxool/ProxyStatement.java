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
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;

/**
 * Delegates to Statement for all calls. But also, for all execute methods, it
 * checks the SQLException and compares it to the fatalSqlException list in the
 * ConnectionPoolDefinition. If it detects a fatal exception it will destroy the
 * Connection so that it isn't used again.
 * @version $Revision: 1.7 $, $Date: 2002/11/09 15:57:33 $
 * @author billhorsman
 * @author $Author: billhorsman $ (current maintainer)
 */
class ProxyStatement implements InvocationHandler {

    private static final Log LOG = LogFactory.getLog(ProxyStatement.class);

    private Statement statement;

    private ConnectionPool connectionPool;

    private Set resultSets = new HashSet();

    private static final String EXECUTE_FRAGMENT = "execute";

    private static final String NOT_IMPLEMENTED = "not implemented";

    private static final String EQUALS_METHOD = "equals";

    private Map parameters;

    private String sqlStatement;

    public ProxyStatement(Statement statement, ConnectionPool connectionPool, String sqlStatement) {
        this.statement = statement;
        this.connectionPool = connectionPool;
        this.sqlStatement = sqlStatement;
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
        final int argCount = args != null ? args.length : 0;

        // We need to remember an exceptions that get thrown so that we can optionally
        // pass them to the onExecute() call below
        Exception exception = null;
        try {
            if (method.getName().equals(EQUALS_METHOD) && argCount == 1) {
                result = new Boolean(statement.hashCode() == args[0].hashCode());
            } else {
                result = method.invoke(statement, args);
            }

            // We only dump sql calls if we are in verbose mode and debug is enabled
            if (LOG.isDebugEnabled() && connectionPool.getDefinition().isVerbose()) {
                try {

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

                    // What sort of method is it
                    if (method.getName().startsWith("set") && argCount == 2) {
                        // Okay, we're probably setting a parameter
                        if (method.getName().equals("setNull")) {
                            // Treat setNull as a special case
                            parameters.put(args[0], "*");
                        } else {
                            if (args[1] == null) {
                                parameters.put(args[0], "*");
                            } else if (args[1] instanceof String) {
                                parameters.put(args[0], "'" + args[1] + "'");
                            } else if (args[1] instanceof Number) {
                                parameters.put(args[0], args[1]);
                            } else {
                                String className = args[1].getClass().getName();
                                StringTokenizer st = new StringTokenizer(className, ".");
                                while (st.hasMoreTokens()) {
                                    className = st.nextToken();
                                }
                                parameters.put(args[0], className);
                            }
                        }
                    }
                } catch (Exception e) {
                    // We don't want an error during dump screwing up the transaction
                    LOG.error("Ignoring error during dump", e);
                }
            }
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

                if (connectionPool.isConnectionListenedTo() || connectionPool.getDefinition().isTrace()) {

                    if (sqlStatement == null && argCount > 0 && args[0] instanceof String) {
                        sqlStatement = (String) args[0];
                    }

                    if (sqlStatement != null) {
                        // TODO it would be nice to format this a bit more nicely.
                        // Maybe replace the ? in the sql with the real values. I think
                        // the goal should be that this dump should be executable
                        // sql. At least, it should be when we call the onExecute()
                        // method below. That is supposed to contain performance
                        // information and the sql that was executed.
                    }

                    // Log if configured to
                    if (connectionPool.getLog().isDebugEnabled() && connectionPool.getDefinition().isTrace()) {
                        connectionPool.getLog().debug(parameters + " -> " + sqlStatement + " (" + (System.currentTimeMillis() - startTime) + " milliseconds)");
                    }

                    // Send to any listener
                    connectionPool.onExecute(parameters + " -> " + sqlStatement, (System.currentTimeMillis() - startTime), exception);

                    // Clear parameters for next time
                    parameters.clear();
                    sqlStatement = null;

                }
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
 Revision 1.7  2002/11/09 15:57:33  billhorsman
 finished off execute logging and listening

 Revision 1.6  2002/10/29 23:20:55  billhorsman
 logs execute time when debug is enabled and verbose is true

 Revision 1.5  2002/10/28 19:28:25  billhorsman
 checkstyle

 Revision 1.4  2002/10/28 08:20:23  billhorsman
 draft sql dump stuff

 Revision 1.3  2002/10/25 15:59:32  billhorsman
 made non-public where possible

 Revision 1.2  2002/10/17 15:29:18  billhorsman
 fixes so that equals() works

 Revision 1.1.1.1  2002/09/13 08:13:30  billhorsman
 new

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
