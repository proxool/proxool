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
 * <link rel="stylesheet" href="{@docRoot}/cg.css" type="text/css">
 *
 * Tests that the various ways of configuring proxool work.
 *
 * @version $Revision: 1.3 $, $Date: 2002/10/27 13:05:02 $
 * @author Bill Horsman (bill@logicalcobwebs.co.uk)
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.5
 */
public class Configurators extends TestCase {

    /**
     * @see TestCase#TestCase
     */
    public Configurators(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        AllTests.globalSetup();
        TestHelper.setupDatabase();
    }

    protected void tearDown() throws Exception {
        TestHelper.tearDownDatabase();
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
 $Log: Configurators.java,v $
 Revision 1.3  2002/10/27 13:05:02  billhorsman
 checkstyle

 Revision 1.2  2002/10/27 12:03:33  billhorsman
 clear up of tests

 Revision 1.1  2002/10/25 10:41:07  billhorsman
 draft changes to test globalSetup

*/
