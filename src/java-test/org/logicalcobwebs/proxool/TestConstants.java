/*
 * This software is released under the Apache Software Licence. See
 * package.html for details. The latest version is available at
 * http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

/**
 * Some useful constants for testing
 *
 * @version $Revision: 1.3 $, $Date: 2003/02/27 18:01:48 $
 * @author Bill Horsman (bill@logicalcobwebs.co.uk)
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.5
 */
public interface TestConstants {

    static final String PROXOOL_DRIVER = "org.logicalcobwebs.proxool.ProxoolDriver";

    static final String HYPERSONIC_DRIVER = "org.hsqldb.jdbcDriver";

    static final String HYPERSONIC_URL_PREFIX = "jdbc:hsqldb:db/";

    static final String HYPERSONIC_TEST_URL = HYPERSONIC_URL_PREFIX + "test";

    static final String HYPERSONIC_USER = "sa";

    static final String HYPERSONIC_PASSWORD = "";

}

/*
 Revision history:
 $Log: TestConstants.java,v $
 Revision 1.3  2003/02/27 18:01:48  billhorsman
 completely rethought the test structure. it's now
 more obvious. no new tests yet though.

 Revision 1.2  2003/02/19 15:14:26  billhorsman
 fixed copyright (copy and paste error,
 not copyright change)

 Revision 1.1  2002/11/13 20:23:58  billhorsman
 improved tests

*/
