/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool.configuration;

import org.logicalcobwebs.proxool.AbstractProxoolTest;
import org.logicalcobwebs.proxool.ProxoolException;
import org.logicalcobwebs.proxool.ProxoolFacade;
import org.logicalcobwebs.proxool.TestHelper;

import java.sql.SQLException;

/**
 * Tests that the PropertyConfigurator works.
 *
 * @version $Revision: 1.8 $, $Date: 2003/03/04 10:58:45 $
 * @author Christian Nedregaard (christian_nedregaard@email.com)
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.6
 */
public class PropertyConfiguratorTest extends AbstractProxoolTest {

    /**
     * @see junit.framework.TestCase#TestCase
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
        TestHelper.equalsCompleteAlternativeProperties(ProxoolFacade.getConnectionPoolDefinition("property-test"));
        TestHelper.equalsCompleteAlternativeProperties(ProxoolFacade.getConnectionPoolDefinition("property-test-2"));
        ProxoolFacade.removeConnectionPool("property-test");
        ProxoolFacade.removeConnectionPool("property-test-2");

    }

}

/*
 Revision history:
 $Log: PropertyConfiguratorTest.java,v $
 Revision 1.8  2003/03/04 10:58:45  billhorsman
 checkstyle

 Revision 1.7  2003/03/04 10:24:41  billhorsman
 removed try blocks around each test

 Revision 1.6  2003/03/03 17:09:18  billhorsman
 all tests now extend AbstractProxoolTest

 Revision 1.5  2003/03/03 11:12:07  billhorsman
 fixed licence

 Revision 1.4  2003/03/01 15:27:25  billhorsman
 checkstyle

 Revision 1.3  2003/02/27 18:01:49  billhorsman
 completely rethought the test structure. it's now
 more obvious. no new tests yet though.

 Revision 1.2  2003/02/19 15:14:27  billhorsman
 fixed copyright (copy and paste error,
 not copyright change)

 Revision 1.1  2002/12/26 11:34:15  billhorsman
 Init rev.

*/
