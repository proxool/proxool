/*
 * This software is released under the Apache Software Licence. See
 * package.html for details. The latest version is available at
 * http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool.configuration;

import junit.framework.TestCase;

import java.sql.SQLException;

import org.logicalcobwebs.proxool.TestHelper;
import org.logicalcobwebs.proxool.ProxoolException;
import org.logicalcobwebs.proxool.ProxoolFacade;
import org.logicalcobwebs.proxool.GlobalTest;

/**
 * Tests that the PropertyConfigurator works.
 *
 * @version $Revision: 1.3 $, $Date: 2003/02/27 18:01:49 $
 * @author Christian Nedregaard (christian_nedregaard@email.com)
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.6
 */
public class PropertyConfiguratorTest extends TestCase {

    /**
     * @see TestCase#TestCase
     */
    public PropertyConfiguratorTest(String name) {
        super(name);
    }

    /**
     * Test that the configuration succeds and that all expected properties
     * has been received by Proxool.
     * @throws ProxoolException if the configuration fails.
     * @throws SQLException if ProxoolFacade operation fails.
     */
    public void testPropertyConfigurator() throws ProxoolException, SQLException {
        PropertyConfigurator.configure("src/java-test/org/logicalcobwebs/proxool/configuration/test.properties");
        try {
            TestHelper.equalsCompleteAlternativeProperties(ProxoolFacade.getConnectionPoolDefinition("property-test"));
        } catch (ProxoolException e) {
            throw e;
        }
        try {
            TestHelper.equalsCompleteAlternativeProperties(ProxoolFacade.getConnectionPoolDefinition("property-test-2"));
        } catch (ProxoolException e) {
            throw e;
        }
        ProxoolFacade.removeConnectionPool("property-test");
        ProxoolFacade.removeConnectionPool("property-test-2");

    }

    /**
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        GlobalTest.globalSetup();
    }

    /**
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        GlobalTest.globalTeardown();
    }
}

/*
 Revision history:
 $Log: PropertyConfiguratorTest.java,v $
 Revision 1.3  2003/02/27 18:01:49  billhorsman
 completely rethought the test structure. it's now
 more obvious. no new tests yet though.

 Revision 1.2  2003/02/19 15:14:27  billhorsman
 fixed copyright (copy and paste error,
 not copyright change)

 Revision 1.1  2002/12/26 11:34:15  billhorsman
 Init rev.

*/
