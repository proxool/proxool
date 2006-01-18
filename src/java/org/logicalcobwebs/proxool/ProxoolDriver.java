/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.logicalcobwebs.proxool.resources.ResourceNamesIF;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * This is the Proxool implementation of the java.sql.Driver interface.
 * @version $Revision: 1.28 $, $Date: 2006/01/18 14:40:01 $
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

    private static final ResourceBundle ATTRIBUTE_DESCRIPTIONS_RESOURCE = createAttributeDescriptionsResource ();

    private static ResourceBundle createAttributeDescriptionsResource () {
        try {
            return ResourceBundle.getBundle (ResourceNamesIF.ATTRIBUTE_DESCRIPTIONS);
        } catch (Exception e) {
            LOG.error ("Could not find resource " + ResourceNamesIF.ATTRIBUTE_DESCRIPTIONS, e);
        }
        return null;
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
        if (!url.startsWith("proxool")) {
            return null;
        }

        ConnectionPool cp = null;
        try {
            String alias = ProxoolFacade.getAlias(url);

            if (!ConnectionPoolManager.getInstance().isPoolExists(alias)) {
                ProxoolFacade.registerConnectionPool(url, info, false);
                cp = ConnectionPoolManager.getInstance().getConnectionPool(alias);
            } else if (info != null && info.size() > 0) {
                // Perhaps we should be redefining the definition?
                cp = ConnectionPoolManager.getInstance().getConnectionPool(alias);
                ConnectionPoolDefinition cpd = cp.getDefinition();
                if (!cpd.isEqual(url, info)) {
                    cpd.redefine(url, info);
                }
            } else {
                cp = ConnectionPoolManager.getInstance().getConnectionPool(alias);
            }
            return cp.getConnection();

        } catch (SQLException e) {
            // We don't log exceptions. Leave that up to the client.
            // LOG.error("Problem", e);
            // Check to see if it's fatal. We might need to wrap it up.
            try {
                String alias = ProxoolFacade.getAlias(url);
                cp = ConnectionPoolManager.getInstance().getConnectionPool(alias);
                if (FatalSqlExceptionHelper.testException(cp.getDefinition(), e)) {
                    FatalSqlExceptionHelper.throwFatalSQLException(cp.getDefinition().getFatalSqlExceptionWrapper(), e);
                }
                // This bit isn't reached if throwFatalSQLException() above throws another exception 
                throw e;
            } catch (ProxoolException e1) {
                LOG.error("Problem", e);
                throw new SQLException(e.toString());
            }
        } catch (ProxoolException e) {
            LOG.error("Problem", e);
            throw new SQLException(e.toString());
        }

    }

    /**
     * @see Driver#acceptsURL
     */
    public boolean acceptsURL(String url) throws SQLException {
        return (url.startsWith("proxool"));
    }

    /**
     * @see Driver#getPropertyInfo
     */
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info)
            throws SQLException {

        DriverPropertyInfo[] dpi = new DriverPropertyInfo[18];
        ConnectionPool cp = null;
        try {
            cp = ConnectionPoolManager.getInstance().getConnectionPool(url);
        } catch (ProxoolException e) {
            throw new SQLException(e.toString());
        }

        ConnectionPoolDefinitionIF cpd = cp.getDefinition();

        dpi[0] = buildDriverPropertyInfo(ProxoolConstants.DELEGATE_DRIVER_PROPERTY,
                String.valueOf(cpd.getDriver()));

        dpi[1] = buildDriverPropertyInfo(ProxoolConstants.DELEGATE_URL_PROPERTY,
                String.valueOf(cpd.getUrl()));

        dpi[2] = buildDriverPropertyInfo(ProxoolConstants.MINIMUM_CONNECTION_COUNT_PROPERTY,
                String.valueOf(cpd.getMinimumConnectionCount()));

        dpi[3] = buildDriverPropertyInfo(ProxoolConstants.MAXIMUM_CONNECTION_COUNT_PROPERTY,
                String.valueOf(cpd.getMaximumConnectionCount()));

        dpi[4] = buildDriverPropertyInfo(ProxoolConstants.MAXIMUM_CONNECTION_LIFETIME_PROPERTY,
                String.valueOf(cpd.getMaximumConnectionLifetime()));

        dpi[5] = buildDriverPropertyInfo(ProxoolConstants.MAXIMUM_NEW_CONNECTIONS_PROPERTY,
                String.valueOf(cpd.getMaximumNewConnections()));

        dpi[6] = buildDriverPropertyInfo(ProxoolConstants.PROTOTYPE_COUNT_PROPERTY,
                String.valueOf(cpd.getPrototypeCount()));

        dpi[7] = buildDriverPropertyInfo(ProxoolConstants.HOUSE_KEEPING_SLEEP_TIME_PROPERTY,
                String.valueOf(cpd.getHouseKeepingSleepTime()));

        dpi[8] = buildDriverPropertyInfo(ProxoolConstants.HOUSE_KEEPING_TEST_SQL_PROPERTY,
                cpd.getHouseKeepingTestSql());

        dpi[9] = buildDriverPropertyInfo(ProxoolConstants.RECENTLY_STARTED_THRESHOLD_PROPERTY,
                String.valueOf(cpd.getRecentlyStartedThreshold()));

        dpi[10] = buildDriverPropertyInfo(ProxoolConstants.OVERLOAD_WITHOUT_REFUSAL_LIFETIME_PROPERTY,
                String.valueOf(cpd.getOverloadWithoutRefusalLifetime()));

        dpi[11] = buildDriverPropertyInfo(ProxoolConstants.MAXIMUM_ACTIVE_TIME_PROPERTY,
                String.valueOf(cpd.getMaximumActiveTime()));

        dpi[12] = buildDriverPropertyInfo(ProxoolConstants.VERBOSE_PROPERTY,
                String.valueOf(cpd.isVerbose()));

        dpi[13] = buildDriverPropertyInfo(ProxoolConstants.TRACE_PROPERTY,
                String.valueOf(cpd.isTrace()));

        dpi[14] = buildDriverPropertyInfo(ProxoolConstants.FATAL_SQL_EXCEPTION_PROPERTY,
                String.valueOf(cpd.getFatalSqlExceptions()));

        dpi[15] = buildDriverPropertyInfo(ProxoolConstants.FATAL_SQL_EXCEPTION_PROPERTY,
                String.valueOf(cpd.getFatalSqlExceptions()));

        dpi[16] = buildDriverPropertyInfo(ProxoolConstants.STATISTICS_PROPERTY,
                String.valueOf(cpd.getStatistics()));

        dpi[17] = buildDriverPropertyInfo(ProxoolConstants.STATISTICS_LOG_LEVEL_PROPERTY,
                String.valueOf(cpd.getStatisticsLogLevel()));

        return dpi;
    }

    private DriverPropertyInfo buildDriverPropertyInfo(String propertyName, String value) {
        DriverPropertyInfo dpi = new DriverPropertyInfo(propertyName,
                ATTRIBUTE_DESCRIPTIONS_RESOURCE.getString (propertyName));
        if (value != null) {
            dpi.value = value;
        }
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

}

/*
 Revision history:
 $Log: ProxoolDriver.java,v $
 Revision 1.28  2006/01/18 14:40:01  billhorsman
 Unbundled Jakarta's Commons Logging.

 Revision 1.27  2004/06/02 20:41:13  billhorsman
 Don't log SQLExceptions. Leave that up to the client.

 Revision 1.26  2003/10/16 18:53:21  billhorsman
 When registering a new pool on the fly, indicate that it is implicit (for exception message handling)

 Revision 1.25  2003/09/30 18:39:08  billhorsman
 New test-before-use, test-after-use and fatal-sql-exception-wrapper-class properties.

 Revision 1.24  2003/09/05 16:59:42  billhorsman
 Added wrap-fatal-sql-exceptions property

 Revision 1.23  2003/08/15 10:13:24  billhorsman
 remove finalize() method

 Revision 1.22  2003/04/19 12:58:40  billhorsman
 fixed bug where ConfigurationListener's
 definitionUpdated was getting called too
 frequently

 Revision 1.21  2003/03/10 23:43:12  billhorsman
 reapplied checkstyle that i'd inadvertently let
 IntelliJ change...

 Revision 1.20  2003/03/10 15:26:48  billhorsman
 refactoringn of concurrency stuff (and some import
 optimisation)

 Revision 1.19  2003/03/03 11:11:58  billhorsman
 fixed licence

 Revision 1.18  2003/02/26 23:59:37  billhorsman
 accept now accepts just proxool not proxool:

 Revision 1.17  2003/02/26 16:05:52  billhorsman
 widespread changes caused by refactoring the way we
 update and redefine pool definitions.

 Revision 1.16  2003/02/07 10:12:59  billhorsman
 changed updatePoolByDriver sig.

 Revision 1.15  2003/02/06 17:41:04  billhorsman
 now uses imported logging

 Revision 1.14  2003/02/06 15:41:16  billhorsman
 add statistics-log-level

 Revision 1.13  2003/01/31 00:28:38  billhorsman
 updated doc for statistics

 Revision 1.12  2003/01/30 17:22:01  billhorsman
 new statistics property

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
