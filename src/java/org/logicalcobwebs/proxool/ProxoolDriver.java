/*
 * This software is released under the Apache Software Licence. See
 * package.html for details. The latest version is available at
 * http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Properties;

/**
 * This is the Proxool implementation of the java.sql.Driver interface.
 * @version $Revision: 1.11 $, $Date: 2003/01/18 15:13:11 $
 * @author billhorsman
 * @author $Author: billhorsman $ (current maintainer)
 */
public class ProxoolDriver implements Driver {

    private static final Log LOG = LogFactory.getLog(ProxoolDriver.class);

    static {
        try {
            DriverManager.registerDriver(new ProxoolDriver());
        } catch (SQLException e) {
            System.out.println(e.toString());
        }
    }

    /**
     * The url should be of the form:
     * <pre>
     *   proxool:delegate-class:delegate-url
     * </pre>
     * or,
     * <pre>
     *   proxool.name:delegate-class:delegate-url
     * </pre>
     * where <pre>delegate-class</pre> is the actual Driver that will be used and
     * <pre>delegate-url</pre> is the url that will be based to that Driver
     *
     * By defining <pre>name</pre> you are able to define multiple connection pools
     * even if the delegate url is the same. The entire url (including the proxool.name) is
     * used to uniquely identify this pool.
     *
     */
    public Connection connect(String url, Properties info)
            throws SQLException {
        Connection connection = null;

        if (!url.startsWith("proxool")) {
            return null;
        }

        ConnectionPool cp = null;
        try {
            String alias = ProxoolFacade.getAlias(url);

            if (!ConnectionPoolManager.getInstance().isPoolExists(alias)) {
                ProxoolFacade.registerConnectionPool(url, info);
                cp = ConnectionPoolManager.getInstance().getConnectionPool(alias);
            } else if (info != null) {
                // Perhaps we should be updating the definition?
                cp = ConnectionPoolManager.getInstance().getConnectionPool(alias);
                ConnectionPoolDefinition cpd = cp.getDefinition();
                ProxoolFacade.updatePoolByDriver(cp, url, cpd, info);
            }
        } catch (ProxoolException e) {
            LOG.error("Problem", e);
            throw new SQLException(e.toString());
        }

        connection = cp.getConnection();
        return connection;
    }

    /**
     * @see Driver#acceptsURL
     */
    public boolean acceptsURL(String url) throws SQLException {
        return (url.startsWith("proxool:"));
    }

    /**
     * @see Driver#getPropertyInfo
     */
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info)
            throws SQLException {

        DriverPropertyInfo[] dpi = new DriverPropertyInfo[13];
        ConnectionPool cp = null;
        try {
            cp = ConnectionPoolManager.getInstance().getConnectionPool(url);
        } catch (ProxoolException e) {
            throw new SQLException(e.toString());
        }

        ConnectionPoolDefinitionIF cpd = cp.getDefinition();

        dpi[0] = new DriverPropertyInfo(ProxoolConstants.HOUSE_KEEPING_SLEEP_TIME_PROPERTY,
                "How long the house keeping thread sleeps for (milliseconds). Defaults to " + ConnectionPoolDefinitionIF.DEFAULT_HOUSE_KEEPING_SLEEP_TIME + ".");
        dpi[0].value = String.valueOf(cpd.getHouseKeepingSleepTime());

        dpi[1] = new DriverPropertyInfo(ProxoolConstants.HOUSE_KEEPING_TEST_SQL_PROPERTY,
                "If the house keeping thread finds and idle connections it will test them with this SQL statement. "
                + "It should be _very_ quick to execute. Something like checking the current date or something. If not defined then this test is omitted.");
        dpi[1].value = cpd.getHouseKeepingTestSql();

        dpi[2] = new DriverPropertyInfo(ProxoolConstants.MAXIMUM_CONNECTION_COUNT_PROPERTY,
                "The maximum amount of connections to the database. Defaults to " + ConnectionPoolDefinitionIF.DEFAULT_MAXIMUM_CONNECTION_COUNT + ".");
        dpi[2].value = String.valueOf(cpd.getMaximumConnectionCount());

        dpi[3] = new DriverPropertyInfo(ProxoolConstants.MAXIMUM_CONNECTION_LIFETIME_PROPERTY,
                "Any idle connections older than this will be removed by the housekeeper (milliseconds). Defaults to " + ConnectionPoolDefinitionIF.DEFAULT_MAXIMUM_CONNECTION_LIFETIME + ".");
        dpi[3].value = String.valueOf(cpd.getMaximumConnectionLifetime());

        dpi[4] = new DriverPropertyInfo(ProxoolConstants.MAXIMUM_NEW_CONNECTIONS_PROPERTY,
                "This is the maximum number of connections we can be building at any one time. That is, the number of new connections that have been requested but aren't "
                + "yet available for use. Defaults to " + ConnectionPoolDefinitionIF.DEFAULT_MAXIMUM_NEW_CONNECTIONS + ".");
        dpi[4].value = String.valueOf(cpd.getMaximumNewConnections());

        dpi[5] = new DriverPropertyInfo(ProxoolConstants.MINIMUM_CONNECTION_COUNT_PROPERTY,
                "If the connection cound it less than this then the housekeeper will build some more.");
        dpi[5].value = String.valueOf(cpd.getMinimumConnectionCount());

        dpi[6] = new DriverPropertyInfo(ProxoolConstants.PROTOTYPE_COUNT_PROPERTY,
                "If there are fewer than this number of connections available then we will build some more (assuming the maximum-connection-count is not exceeded).");
        dpi[6].value = String.valueOf(cpd.getPrototypeCount());

        dpi[7] = new DriverPropertyInfo(ProxoolConstants.RECENTLY_STARTED_THRESHOLD_PROPERTY,
                "This helps us determine whether the pool status. As long as at least one connection was started within this threshold (milliseconds) or there "
                + " are some spare connections available then we assume the pool is up. Defaults to 60 seconds.");
        dpi[7].value = String.valueOf(cpd.getRecentlyStartedThreshold());

        dpi[8] = new DriverPropertyInfo(ProxoolConstants.OVERLOAD_WITHOUT_REFUSAL_LIFETIME_PROPERTY,
                "This helps us determine the pool status. If we have refused a connection within this threshold (milliseconds) then we are overloaded. Defaults to 60 seconds.");
        dpi[8].value = String.valueOf(cpd.getOverloadWithoutRefusalLifetime());

        dpi[9] = new DriverPropertyInfo(ProxoolConstants.MAXIMUM_ACTIVE_TIME_PROPERTY,
                "If a connection is active for longer than this (milliseconds) then we assume it has stalled or something. And we kill it.");
        dpi[9].value = String.valueOf(cpd.getMaximumActiveTime());

        dpi[10] = new DriverPropertyInfo(ProxoolConstants.VERBOSE_PROPERTY,
                "Either false (quiet) or true (loud). Default is false.");

        dpi[11] = new DriverPropertyInfo(ProxoolConstants.TRACE_PROPERTY,
                "If true then every execution will be logged. Default is false.");

        dpi[12] = new DriverPropertyInfo(ProxoolConstants.FATAL_SQL_EXCEPTION_PROPERTY,
                "All SQLExceptions are caught and tested for containing this text fragment. If it matches than this connection is considered useless "
                + "and it is discarded. Regardless of what happens the exception is always thrown again. This property behaves like a collection; "
                + "you can set it more than once and each value is checked.");

        return dpi;
    }

    /**
     * @see Driver#getMajorVersion
     */
    public int getMajorVersion() {
        return 1;
    }

    /**
     * @see Driver#getMinorVersion
     */
    public int getMinorVersion() {
        return 0;
    }

    /**
     * @see Driver#jdbcCompliant
     */
    public boolean jdbcCompliant() {
        return true;
    }

    /**
     * @see Object#finalize
     */
    protected void finalize() throws Throwable {
        super.finalize();

        // I guess it's safe to assume that if this driver is being
        // finalized then the whole thing is going down?
        ProxoolFacade.removeAllConnectionPools(0);
    }
}

/*
 Revision history:
 $Log: ProxoolDriver.java,v $
 Revision 1.11  2003/01/18 15:13:11  billhorsman
 Signature changes (new ProxoolException
 thrown) on the ProxoolFacade API.

 Revision 1.10  2003/01/17 00:38:12  billhorsman
 wide ranging changes to clarify use of alias and url -
 this has led to some signature changes (new exceptions
 thrown) on the ProxoolFacade API.

 Revision 1.9  2002/12/11 01:48:41  billhorsman
 added default values for property info documentation

 Revision 1.8  2002/12/04 13:19:43  billhorsman
 draft ConfigurationListenerIF stuff for persistent configuration

 Revision 1.7  2002/12/03 00:41:56  billhorsman
 fixed getPropertyInfo() for TRACE property and better explanation of FATAL_SQL_EXCEPTION

 Revision 1.6  2002/11/09 15:55:42  billhorsman
 added propertyInfo for verbose

 Revision 1.5  2002/10/27 13:29:38  billhorsman
 deprecated debug-level in favour of verbose

 Revision 1.4  2002/10/23 21:04:36  billhorsman
 checkstyle fixes (reduced max line width and lenient naming convention

 Revision 1.3  2002/10/17 15:25:37  billhorsman
 use the url when updating, not the name

 Revision 1.2  2002/09/18 13:48:56  billhorsman
 checkstyle and doc

 Revision 1.1.1.1  2002/09/13 08:13:09  billhorsman
 new

 Revision 1.13  2002/07/10 16:14:47  billhorsman
 widespread layout changes and move constants into ProxoolConstants

 Revision 1.12  2002/07/04 09:10:48  billhorsman
 update definition changes

 Revision 1.11  2002/07/03 10:16:20  billhorsman
 autoFlush and configuration for logging

 Revision 1.10  2002/07/02 11:19:08  billhorsman
 layout code and imports

 Revision 1.9  2002/07/02 11:14:26  billhorsman
 added test (andbug fixes) for FileLogger

 Revision 1.8  2002/07/02 09:05:25  billhorsman
 All properties now start with "proxool." to avoid possible confusion with delegate driver's properties

 Revision 1.7  2002/07/02 08:50:33  billhorsman
 Responsibility for configuring pool is now simplifed and moved to here

 Revision 1.6  2002/06/28 11:19:47  billhorsman
 improved doc

*/
