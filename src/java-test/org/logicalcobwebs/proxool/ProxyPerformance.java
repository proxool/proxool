/*
 * This software is released under the Apache Software Licence. See
 * package.html for details. The latest version is available at
 * http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import junit.framework.TestCase;
import org.logicalcobwebs.proxool.ConnectionPoolDefinitionIF;
import org.logicalcobwebs.proxool.ConnectionPoolStatisticsIF;
import org.logicalcobwebs.proxool.ProxoolConstants;
import org.logicalcobwebs.proxool.ProxoolFacade;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Check to see if there is any overhead in using Proxy class
 *
 * @version $Revision: 1.1 $, $Date: 2002/09/13 08:14:24 $
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
 Revision 1.1  2002/09/13 08:14:24  billhorsman
 Initial revision


*/
