/*
* Copyright 2002, Findexa AS (http://www.findexa.no)
*
* This software is the proprietary information of Findexa AS.
* Use is subject to license terms.
*/
package org.logicalcobwebs.proxool.configuration;

import junit.framework.TestCase;

import java.util.Properties;

import org.logicalcobwebs.logging.Log;
import org.logicalcobwebs.logging.LogFactory;
import org.logicalcobwebs.proxool.TestHelper;
import org.logicalcobwebs.proxool.ProxoolAdapter;
import org.logicalcobwebs.proxool.ProxoolConstants;
import org.logicalcobwebs.proxool.GlobalTest;

/**
 * Tests that the programatic configuration of Proxool works.
 *
 * @version $Revision: 1.8 $, $Date: 2003/02/06 17:41:03 $
 * @author Bill Horsman (bill@logicalcobwebs.co.uk)
 * @author $Author: billhorsman $ (current maintainer)
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
        GlobalTest.globalSetup();
        try {
            TestHelper.createTable(TEST_TABLE);
        } catch (Exception e) {
            LOG.debug("Problem creating table", e);
        }
    }

    protected void tearDown() throws Exception {
        TestHelper.dropTable(TEST_TABLE);
        GlobalTest.globalTeardown();
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

}

/*
 Revision history:
 $Log: ConfiguratorTest.java,v $
 Revision 1.8  2003/02/06 17:41:03  billhorsman
 now uses imported logging

 Revision 1.7  2003/01/22 17:35:03  billhorsman
 checkstyle

 Revision 1.6  2003/01/18 15:13:14  billhorsman
 Signature changes (new ProxoolException
 thrown) on the ProxoolFacade API.

 Revision 1.5  2003/01/17 00:38:12  billhorsman
 wide ranging changes to clarify use of alias and url -
 this has led to some signature changes (new exceptions
 thrown) on the ProxoolFacade API.

 Revision 1.4  2002/12/26 11:35:02  billhorsman
 Removed test regarding property configurator.

 Revision 1.3  2002/12/16 17:06:53  billhorsman
 new test structure

 Revision 1.2  2002/12/15 19:41:28  chr32
 Style fixes.

 Revision 1.1  2002/12/15 19:10:49  chr32
 Init rev.

 Revision 1.4  2002/12/04 13:20:11  billhorsman
 ConfigurationListenerIF test

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
