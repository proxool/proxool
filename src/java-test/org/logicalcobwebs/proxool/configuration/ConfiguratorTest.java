/*
* Copyright 2002, Findexa AS (http://www.findexa.no)
*
* This software is the proprietary information of Findexa AS.
* Use is subject to license terms.
*/
package org.logicalcobwebs.proxool.configuration;

import junit.framework.TestCase;

import java.sql.SQLException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.logicalcobwebs.proxool.configuration.PropertyConfigurator;
import org.logicalcobwebs.proxool.configuration.JAXPConfigurator;
import org.logicalcobwebs.proxool.AllTests;
import org.logicalcobwebs.proxool.TestHelper;
import org.logicalcobwebs.proxool.ProxoolAdapter;
import org.logicalcobwebs.proxool.ProxoolConstants;
import org.logicalcobwebs.proxool.ProxoolException;
import org.logicalcobwebs.proxool.ProxoolFacade;

/**
 * Tests that the various ways of configuring proxool work.
 *
 * @version $Revision: 1.1 $, $Date: 2002/12/15 19:10:49 $
 * @author Bill Horsman (bill@logicalcobwebs.co.uk)
 * @author $Author: chr32 $ (current maintainer)
 * @since Proxool 0.5
 */
public class ConfiguratorTest extends TestCase {

    private static final Log LOG = LogFactory.getLog(ConfiguratorTest.class);

    private static final String TEST_TABLE = "test";

    /**
     * @see junit.framework.TestCase#TestCase
     */
    public ConfiguratorTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        AllTests.globalSetup();
        try {
            TestHelper.createTable(TEST_TABLE);
        } catch (Exception e) {
            LOG.debug("Problem creating table", e);
        }
    }

    protected void tearDown() throws Exception {
        TestHelper.dropTable(TEST_TABLE);
        AllTests.globalTeardown();
    }


    public void testConfigurator() {

        String testName = "template";
        ProxoolAdapter adapter = null;
        try {
            String alias = testName;
            Properties info = TestHelper.buildProperties();
            adapter = new ProxoolAdapter(alias);
            adapter.setup(TestHelper.HYPERSONIC_DRIVER, TestHelper.HYPERSONIC_URL, info);

            Properties newInfo = new Properties();
            newInfo.setProperty(ProxoolConstants.PROTOTYPE_COUNT_PROPERTY, "3");
            adapter.update(newInfo);

            assertNotNull("adapter.getConnectionPoolDefinition() is null", adapter.getConnectionPoolDefinition());
            assertEquals("prototypeCount", 3, adapter.getConnectionPoolDefinition().getPrototypeCount());

        } catch (Exception e) {
            LOG.error("Whilst performing " + testName, e);
            fail(e.getMessage());
        } finally {
            adapter.tearDown();
        }

    }

    /**
     * Load test file and check that Proxool is properly configured
     */
    public void testPropertyConfigurator() throws SQLException {

        PropertyConfigurator.configure("src/java-test/org/logicalcobwebs/proxool/configuration/test.properties");

    }
}

/*
 Revision history:
 $Log: ConfiguratorTest.java,v $
 Revision 1.1  2002/12/15 19:10:49  chr32
 Init rev.

 Revision 1.4  2002/12/04 13:20:11  billhorsman
 ConfiguratorIF test

 Revision 1.3  2002/11/09 16:00:45  billhorsman
 fix doc

 Revision 1.2  2002/11/02 13:57:34  billhorsman
 checkstyle

 Revision 1.1  2002/11/02 11:37:48  billhorsman
 New tests

 Revision 1.4  2002/10/29 23:17:38  billhorsman
 Cleaned up SQL stuff

 Revision 1.3  2002/10/27 13:05:02  billhorsman
 checkstyle

 Revision 1.2  2002/10/27 12:03:33  billhorsman
 clear up of tests

 Revision 1.1  2002/10/25 10:41:07  billhorsman
 draft changes to test globalSetup

*/
