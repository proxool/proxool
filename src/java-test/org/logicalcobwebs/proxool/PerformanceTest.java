/*
* Copyright 2002, Findexa AS (http://www.findexa.no)
*
* This software is the proprietary information of Findexa AS.
* Use is subject to license terms.
*/
package org.logicalcobwebs.proxool;

import junit.framework.TestCase;

import java.util.Properties;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <link rel="stylesheet" href="{@docRoot}/cg.css" type="text/css">
 *
 * TODO
 *
 * @version $Revision: 1.1 $, $Date: 2002/10/30 21:17:51 $
 * @author Bill Horsman (bill@logicalcobwebs.co.uk)
 * @author $Author: billhorsman $ (current maintainer)
 * @since GSI 5.0
 */
public class PerformanceTest extends TestCase {

    private static final Log LOG = LogFactory.getLog(PerformanceTest.class);

    private static final int LOAD = 5;

    private static final int LOOPS = 10;

    public PerformanceTest(String s) {
        super(s);
    }

    protected void setUp() throws Exception {
        AllTests.globalSetup();
    }

    protected void tearDown() throws Exception {
        AllTests.globalTeardown();
    }

    public void testProxoolHypersonic() throws SQLException, ClassNotFoundException {
        String url = TestHelper.buildProxoolUrl("ph",
                TestHelper.HYPERSONIC_DRIVER,
                TestHelper.HYPERSONIC_URL);
        loadHypersonic(TestHelper.PROXOOL_DRIVER, url, "ph", false);
    }

    public void testSimpoolHypersonic() throws SQLException, ClassNotFoundException {
        String url = TestHelper.buildSimpoolUrl("sh",
                TestHelper.HYPERSONIC_DRIVER,
                TestHelper.HYPERSONIC_URL);
        loadHypersonic(SimpoolDriver.class.getName(), url, "sh", true);
    }

    private void loadHypersonic(String driver, String url, String table, boolean ignoreClose) throws SQLException, ClassNotFoundException {

        Properties info = TestHelper.buildProperties();
        info.setProperty(ProxoolConstants.MAXIMUM_CONNECTION_COUNT_PROPERTY, String.valueOf(LOAD));
        info.setProperty(ProxoolConstants.PROTOTYPE_COUNT_PROPERTY, String.valueOf(LOAD));

        TestAgent ta = new TestAgent(driver, url, info, LOOPS);
        ta.setup(ignoreClose);
        try {
            ta.execute("CREATE TABLE " + table + " (A INT)", ignoreClose);
        } catch (SQLException e) {
            LOG.debug("Ignorable:", e);
        }
        long start = System.currentTimeMillis();
        ta.test("INSERT INTO " + table + " VALUES(1)", ignoreClose);
        long elapsed = System.currentTimeMillis() - start;
        double split = ((double)elapsed / (double)(LOAD * LOOPS));
        LOG.info(driver + ":hypersonic ran " + (LOAD * LOOPS) + " tests at an average of " + split + " milliseconds per test");

    }

}

/*
 Revision history:
 $Log: PerformanceTest.java,v $
 Revision 1.1  2002/10/30 21:17:51  billhorsman
 new performance tests

*/
