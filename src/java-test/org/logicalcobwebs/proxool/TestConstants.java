/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

/**
 * Some useful constants for testing.
 * 
 * Note: these values will be overriden at startup by the GlobalTest init procedure.
 *
 * @version $Revision: 1.7 $, $Date: 2005/10/07 08:10:33 $
 * @author Bill Horsman (bill@logicalcobwebs.co.uk)
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.5
 */
public class TestConstants {

	/**
	 * Proxool Driver class
	 */
    public static String PROXOOL_DRIVER = "org.logicalcobwebs.proxool.ProxoolDriver";

    /**
     * JDBC driver class
     */
    public static String HYPERSONIC_DRIVER = "org.hsqldb.jdbcDriver";

    /**
     * URL connection base (without database)
     */
    public static String HYPERSONIC_URL_PREFIX = "jdbc:hsqldb:db/";

    /**
     * URL to a first test database. User should have rw access
     */
    public static String HYPERSONIC_TEST_URL  = HYPERSONIC_URL_PREFIX + "test";
    
    /**
     * URL to a second test database
     */
    public static String HYPERSONIC_TEST_URL2 = HYPERSONIC_URL_PREFIX + "2";
    
    /**
     * Connection credentials
     */
    public static String HYPERSONIC_USER = "sa";

    /**
     * Connection credentials
     */
    public static String HYPERSONIC_PASSWORD = "";

    /**
     * SQL statement that should always succeed
     */
    public static String HYPERSONIC_TEST_SQL = "SELECT COUNT(1) FROM SYSTEM_CATALOGS";

    /**
     * SQL statement that should always succeed
     */
    public static String HYPERSONIC_TEST_SQL_2 = "SELECT COUNT(2) FROM SYSTEM_CATALOGS";

    /**
     * SQL statement that should always fail
     */
    public static String FATAL_SQL_STATEMENT = "drop table Z";
    
    /**
     * SQLException message fragment used to detect fatal exceptions
     */
    public static String FATAL_SQL_EXCEPTION = "Table not found";
}

/*
 Revision history:
 $Log: TestConstants.java,v $
 Revision 1.7  2005/10/07 08:10:33  billhorsman
 Second test SQL

 Revision 1.6  2004/05/26 17:19:09  brenuart
 Allow JUnit tests to be executed against another database.
 By default the test configuration will be taken from the 'testconfig-hsqldb.properties' file located in the org.logicalcobwebs.proxool package.
 This behavior can be overriden by setting the 'testConfig' environment property to another location.

 Revision 1.5  2003/09/30 18:39:39  billhorsman
 New test sql syntax constant

 Revision 1.4  2003/03/03 11:12:05  billhorsman
 fixed licence

 Revision 1.3  2003/02/27 18:01:48  billhorsman
 completely rethought the test structure. it's now
 more obvious. no new tests yet though.

 Revision 1.2  2003/02/19 15:14:26  billhorsman
 fixed copyright (copy and paste error,
 not copyright change)

 Revision 1.1  2002/11/13 20:23:58  billhorsman
 improved tests

*/
