/*
* Copyright 2002, Findexa AS (http://www.findexa.no)
*
* This software is the proprietary information of Findexa AS.
* Use is subject to license terms.
*/
package org.logicalcobwebs.proxool;

import junit.framework.TestCase;

import java.sql.SQLException;

/**
 * Tests that the various ways of configuring proxool work.
 *
 * @version $Revision: 1.3 $, $Date: 2002/11/09 16:00:45 $
 * @author Bill Horsman (bill@logicalcobwebs.co.uk)
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.5
 */
public class ConfiguratorTest extends TestCase {

    private static final String TEST_TABLE = "test";

    /**
     * @see TestCase#TestCase
     */
    public ConfiguratorTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        AllTests.globalSetup();
        TestHelper.createTable(TEST_TABLE);
    }

    protected void tearDown() throws Exception {
        TestHelper.dropTable(TEST_TABLE);
        AllTests.globalTeardown();
    }

    /**
     * Load test file and check that Proxool is properly configured
     */
    public void testPropertyConfigurator() throws SQLException {

        PropertyConfigurator.configure("src/java-test/org/logicalcobwebs/proxool/test.properties");

    }

    /**
     * Load test file and check that Proxool is properly configured
     */
    public void testXMLConfigurator() {

        // TODO implement

    }

    /**
     * Load test file and check that Proxool is properly configured
     */
    public void testAvalonConfigurator() {

        // TODO implement

    }

}

/*
 Revision history:
 $Log: ConfiguratorTest.java,v $
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
