/*
 * This software is released under the Apache Software Licence. See
 * package.html for details. The latest version is available at
 * http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import junit.framework.TestCase;

import java.sql.SQLException;

/**
 * Check to see if there is any overhead in using Proxy class
 *
 * @version $Revision: 1.2 $, $Date: 2002/09/18 13:48:56 $
 * @author billhorsman
 * @author $Author: billhorsman $ (current maintainer)
 */
public class ProxyPerformance extends TestCase {

    public ProxyPerformance(String name) {
        super(name);
    }

    /**
     * Test
     */
    public void testProxyFoo() throws SQLException {


    }


}

/*
 Revision history:
 $Log: ProxyPerformance.java,v $
 Revision 1.2  2002/09/18 13:48:56  billhorsman
 checkstyle and doc

 Revision 1.1.1.1  2002/09/13 08:14:24  billhorsman
 new


*/
