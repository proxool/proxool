/*
 * This software is released under the Apache Software Licence. See
 * package.html for details. The latest version is available at
 * http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

/**
 * All constants here please.
 *
 * @version $Revision: 1.9 $, $Date: 2003/01/30 17:22:03 $
 * @author billhorsman
 * @author $Author: billhorsman $ (current maintainer)
 */
public interface ProxoolConstants {

    public final String PROXOOL = "proxool";

    /**
     * The namespace uri associated with namepace aware Proxool xml configurations.<br>
     * Value: http://proxool.sourceforge.net/xml-namespace
     */
    public final String PROXOOL_XML_NAMESPACE_URI = "http://proxool.sourceforge.net/xml-namespace";

    public final String ALIAS_DELIMITER = ".";

    public final String PROPERTY_PREFIX = PROXOOL + ".";

    public final String URL_DELIMITER = ":";

    /** Standard JDBC property */
    public final String USER_PROPERTY = "user";

    /** Standard JDBC property */
    public final String PASSWORD_PROPERTY = "password";

    /** Used to build up URL */
    public final String ALIAS_PROPERTY = PROPERTY_PREFIX + "alias";

    /** Define URL directly */
    public final String URL_PROPERTY = PROPERTY_PREFIX + "url";

    /** Used to build up URL */
    public final String DELEGATE_CLASS_PROPERTY = PROPERTY_PREFIX + "class";

    /** Used to build up URL */
    public final String DELEGATE_URL_PROPERTY = PROPERTY_PREFIX + "url";

    /** @see #HOUSE_KEEPING_SLEEP_TIME_PROPERTY */
    public final String HOUSE_KEEPING_SLEEP_TIME = "house-keeping-sleep-time";

    /** @see ProxoolDriver#getPropertyInfo */
     public final String HOUSE_KEEPING_SLEEP_TIME_PROPERTY = PROPERTY_PREFIX + HOUSE_KEEPING_SLEEP_TIME;

    /** @see #HOUSE_KEEPING_TEST_SQL_PROPERTY */
    public final String HOUSE_KEEPING_TEST_SQL = "house-keeping-test-sql";

    /** @see ProxoolDriver#getPropertyInfo */
    public final String HOUSE_KEEPING_TEST_SQL_PROPERTY = PROPERTY_PREFIX + HOUSE_KEEPING_TEST_SQL;

    /** @see #MAXIMUM_CONNECTION_COUNT_PROPERTY */
    public final String MAXIMUM_CONNECTION_COUNT = "maximum-connection-count";

    /** @see ProxoolDriver#getPropertyInfo */
    public final String MAXIMUM_CONNECTION_COUNT_PROPERTY = PROPERTY_PREFIX + MAXIMUM_CONNECTION_COUNT;

    /** @see #MAXIMUM_CONNECTION_LIFETIME_PROPERTY */
    public final String MAXIMUM_CONNECTION_LIFETIME = "maximum-connection-lifetime";

    /** @see ProxoolDriver#getPropertyInfo */
    public final String MAXIMUM_CONNECTION_LIFETIME_PROPERTY = PROPERTY_PREFIX + MAXIMUM_CONNECTION_LIFETIME;

    /** @see #MAXIMUM_NEW_CONNECTIONS_PROPERTY */
    public final String MAXIMUM_NEW_CONNECTIONS = "maximum-new-connections";

    /** @see ProxoolDriver#getPropertyInfo */
    public final String MAXIMUM_NEW_CONNECTIONS_PROPERTY = PROPERTY_PREFIX + MAXIMUM_NEW_CONNECTIONS;

    /** @see #MINIMUM_CONNECTION_COUNT_PROPERTY */
    public final String MINIMUM_CONNECTION_COUNT = "minimum-connection-count";

    /** @see ProxoolDriver#getPropertyInfo */
    public final String MINIMUM_CONNECTION_COUNT_PROPERTY = PROPERTY_PREFIX + MINIMUM_CONNECTION_COUNT;

    /** @see #PROTOTYPE_COUNT_PROPERTY */
    public final String PROTOTYPE_COUNT = "prototype-count";

    /** @see ProxoolDriver#getPropertyInfo */
    public final String PROTOTYPE_COUNT_PROPERTY = PROPERTY_PREFIX + PROTOTYPE_COUNT;

    /** @see #RECENTLY_STARTED_THRESHOLD_PROPERTY */
    public final String RECENTLY_STARTED_THRESHOLD = "recently-started-threshold";

    /** @see ProxoolDriver#getPropertyInfo */
    public final String RECENTLY_STARTED_THRESHOLD_PROPERTY = PROPERTY_PREFIX + RECENTLY_STARTED_THRESHOLD;

    /** @see #OVERLOAD_WITHOUT_REFUSAL_LIFETIME_PROPERTY */
    public final String OVERLOAD_WITHOUT_REFUSAL_LIFETIME = "overload-without-refusal-lifetime";

    /** @see ProxoolDriver#getPropertyInfo */
    public final String OVERLOAD_WITHOUT_REFUSAL_LIFETIME_PROPERTY = PROPERTY_PREFIX + OVERLOAD_WITHOUT_REFUSAL_LIFETIME;

    /** @see #MAXIMUM_ACTIVE_TIME_PROPERTY */
    public final String MAXIMUM_ACTIVE_TIME = "maximum-active-time";

    /** @see ProxoolDriver#getPropertyInfo */
    public final String MAXIMUM_ACTIVE_TIME_PROPERTY = PROPERTY_PREFIX + MAXIMUM_ACTIVE_TIME;

    /**
     * Deprecated - use {@link #VERBOSE_PROPERTY verbose} instead.
     * @see ProxoolDriver#getPropertyInfo
     */
    public final String DEBUG_LEVEL_PROPERTY = PROPERTY_PREFIX + "debug-level";

    /** @see #VERBOSE_PROPERTY */
    public final String VERBOSE = "verbose";

    /** @see ProxoolDriver#getPropertyInfo */
    public final String VERBOSE_PROPERTY = PROPERTY_PREFIX + VERBOSE;

    /** @see #TRACE_PROPERTY */
    public final String TRACE = "trace";

    /** @see ProxoolDriver#getPropertyInfo */
    public final String TRACE_PROPERTY = PROPERTY_PREFIX + TRACE;

    /** @see #FATAL_SQL_EXCEPTION_PROPERTY **/
    public final String FATAL_SQL_EXCEPTION = "fatal-sql-exception";

    /** @see ProxoolDriver#getPropertyInfo */
    public final String FATAL_SQL_EXCEPTION_PROPERTY = PROPERTY_PREFIX + FATAL_SQL_EXCEPTION;

    public static final String STATISTICS = "statistics";

    /** @see ProxoolDriver#getPropertyInfo */
    public final String STATISTICS_PROPERTY = PROPERTY_PREFIX + STATISTICS;
    /**
     *  Un-prefixed propety name for the Proxool alias configuration property. Value: alias
     */
    public final String ALIAS = "alias";

    /**
     *  Un-prefixed propety name for the Proxool driver class  configuration property. Value: driver-class
     */
    public final String DRIVER_CLASS = "driver-class";
    /**
     *  Prefixed propety name for the Proxool driver class  configuration property. Value: proxool.driver-class
     */
    public final String DRIVER_CLASS_PROPERTY = PROPERTY_PREFIX + DRIVER_CLASS;;
    /**
     *  Un-prefixed propety name for the Proxool driver url configuration property. Value: driver-url
     */
    public final String DRIVER_URL = "driver-url";
    /**
     *  Prefixed propety name for the Proxool driver url configuration property. Value: proxool.driver-url
     */
    public final String DRIVER_URL_PROPERTY = PROPERTY_PREFIX + DRIVER_URL;
}

/*
 Revision history:
 $Log: ProxoolConstants.java,v $
 Revision 1.9  2003/01/30 17:22:03  billhorsman
 new statistics property

 Revision 1.8  2003/01/23 10:41:05  billhorsman
 changed use of pool-name to alias for consistency

 Revision 1.7  2002/12/26 11:32:22  billhorsman
 Moved ALIAS, DRIVER_URL and DRIVER_CLASS constants
 from XMLConfgiurator to ProxoolConstants.

 Revision 1.6  2002/12/15 19:22:51  chr32
 Added constant for proxool xml namespace.

 Revision 1.5  2002/12/11 01:47:12  billhorsman
 extracted property names without proxool. prefix for use
 by XMLConfigurators.

 Revision 1.4  2002/11/09 15:50:49  billhorsman
 new trace constant

 Revision 1.3  2002/10/27 13:29:38  billhorsman
 deprecated debug-level in favour of verbose

 Revision 1.2  2002/10/25 15:59:32  billhorsman
 made non-public where possible

 Revision 1.1.1.1  2002/09/13 08:13:06  billhorsman
 new

 Revision 1.3  2002/08/24 19:57:15  billhorsman
 checkstyle changes

 Revision 1.2  2002/07/12 23:03:22  billhorsman
 added doc headers

 Revision 1.7  2002/07/10 16:14:47  billhorsman
 widespread layout changes and move constants into ProxoolConstants

 Revision 1.6  2002/07/02 11:19:08  billhorsman
 layout code and imports

 Revision 1.5  2002/07/02 08:27:47  billhorsman
 bug fix when settiong definition, displayStatistics now available to ProxoolFacade, prototyper no longer attempts to make connections when maximum is reached

 Revision 1.4  2002/06/28 11:19:47  billhorsman
 improved doc

*/
