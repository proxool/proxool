/*
 * This software is released under the Apache Software Licence. See
 * package.html for details. The latest version is available at
 * http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

/**
 * All constants here please.
 *
 * @version $Revision: 1.4 $, $Date: 2002/11/09 15:50:49 $
 * @author billhorsman
 * @author $Author: billhorsman $ (current maintainer)
 */
interface ProxoolConstants {

    public final String PROXOOL = "proxool";

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

    /** @see ProxoolDriver#getPropertyInfo */
    public final String HOUSE_KEEPING_SLEEP_TIME_PROPERTY = PROPERTY_PREFIX + "house-keeping-sleep-time";

    /** @see ProxoolDriver#getPropertyInfo */
    public final String HOUSE_KEEPING_TEST_SQL_PROPERTY = PROPERTY_PREFIX + "house-keeping-test-sql";

    /** @see ProxoolDriver#getPropertyInfo */
    public final String MAXIMUM_CONNECTION_COUNT_PROPERTY = PROPERTY_PREFIX + "maximum-connection-count";

    /** @see ProxoolDriver#getPropertyInfo */
    public final String MAXIMUM_CONNECTION_LIFETIME_PROPERTY = PROPERTY_PREFIX + "maximum-connection-lifetime";

    /** @see ProxoolDriver#getPropertyInfo */
    public final String MAXIMUM_NEW_CONNECTIONS_PROPERTY = PROPERTY_PREFIX + "maximum-new-connections";

    /** @see ProxoolDriver#getPropertyInfo */
    public final String MINIMUM_CONNECTION_COUNT_PROPERTY = PROPERTY_PREFIX + "minimum-connection-count";

    /** @see ProxoolDriver#getPropertyInfo */
    public final String PROTOTYPE_COUNT_PROPERTY = PROPERTY_PREFIX + "prototype-count";

    /** @see ProxoolDriver#getPropertyInfo */
    public final String RECENTLY_STARTED_THRESHOLD_PROPERTY = PROPERTY_PREFIX + "recently-started-threshold";

    /** @see ProxoolDriver#getPropertyInfo */
    public final String OVERLOAD_WITHOUT_REFUSAL_LIFETIME_PROPERTY = PROPERTY_PREFIX + "overload-without-refusal-lifetime";

    /** @see ProxoolDriver#getPropertyInfo */
    public final String MAXIMUM_ACTIVE_TIME_PROPERTY = PROPERTY_PREFIX + "maximum-active-time";

    /**
     * Deprecated - use {@linkplain #VERBOSE_PROPERTY verbose} instead.
     * @see ProxoolDriver#getPropertyInfo
     */
    public final String DEBUG_LEVEL_PROPERTY = PROPERTY_PREFIX + "debug-level";

    /** @see ProxoolDriver#getPropertyInfo */
    public final String VERBOSE_PROPERTY = PROPERTY_PREFIX + "verbose";

    /** @see ProxoolDriver#getPropertyInfo */
    public final String TRACE_PROPERTY = PROPERTY_PREFIX + "trace";

    /** @see ProxoolDriver#getPropertyInfo */
    public final String FATAL_SQL_EXCEPTION_PROPERTY = PROPERTY_PREFIX + "fatal-sql-exception";

    /** @see ProxoolDriver#getPropertyInfo */
    public final String LOG_PROPERTY = PROPERTY_PREFIX + "log";

    /** @see ProxoolDriver#getPropertyInfo */
    public final String LOG_DEBUG_PROPERTY = PROPERTY_PREFIX + "log.debug";

    /** @see ProxoolDriver#getPropertyInfo */
    public final String LOG_INFO_PROPERTY = PROPERTY_PREFIX + "log.info";

    /** @see ProxoolDriver#getPropertyInfo */
    public final String LOG_AUTO_FLUSH_PROPERTY = PROPERTY_PREFIX + "log.autoFlush";

}

/*
 Revision history:
 $Log: ProxoolConstants.java,v $
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
