/*
* Copyright 2002, Findexa AS (http://www.findexa.no)
*
* This software is the proprietary information of Findexa AS.
* Use is subject to license terms.
*/
package org.logicalcobwebs.proxool;

/**
 * Some useful constants for testing
 *
 * @version $Revision: 1.1 $, $Date: 2002/11/13 20:23:58 $
 * @author Bill Horsman (bill@logicalcobwebs.co.uk)
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.5
 */
public interface TestConstants {

    static final String PROXOOL_DRIVER = "org.logicalcobwebs.proxool.ProxoolDriver";

    static final String HYPERSONIC_DRIVER = "org.hsqldb.jdbcDriver";

    static final String HYPERSONIC_URL = "jdbc:hsqldb:test";

    static final String HYPERSONIC_USER = "sa";

    static final String HYPERSONIC_PASSWORD = "";

}

/*
 Revision history:
 $Log: TestConstants.java,v $
 Revision 1.1  2002/11/13 20:23:58  billhorsman
 improved tests

*/
