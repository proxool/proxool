/*
* Copyright 2002, Findexa AS (http://www.findex.no)
*
* This software is the proprietary information of Findexa AS.
* Use is subject to license terms.
*/
package org.logicalcobwebs.proxool;

import junit.framework.TestCase;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Test what the performance of Proxool is compared to a "theoretical"
 * perfect pool. We can recreate the perfect pool fairly easily as long
 * as we keep a single threaded model (and because most systems work
 * in a multi-threaded environment is why we need a pool).
 *
 * @version $Revision: 1.1 $
 * @author Bill Horsman (bill@logicalcobwebs.co.uk)
 * @author $Author: billhorsman $ (current maintainer)
 * @since TODO 24-Aug-2002;bill;high;complete
 */
public class Performance extends TestCase {

    private static final Log LOG = LogFactory.getLog(Performance.class);

    private static final int LOOPS = 1000;

    private static final int THREADS = 10;

    private static final String ALIAS_PERFORMANCE = "performance";

    public Performance(String s) {
        super(s);
        TestHelper.configureLog4J();
    }

    protected void setUp() throws Exception {
        TestHelper.setup();
    }

    protected void tearDown() throws Exception {
        TestHelper.tearDown();
    }

    public void testPerfectPool() throws SQLException, ClassNotFoundException {

        LOG.info("Testing perfect pool");

        Connection c = TestHelper.getDirectConnection();
        long start = System.currentTimeMillis();

        for (int i = 0; i < LOOPS * THREADS; i++) {
            TestHelper.testConnection(c);
        }
        c.close();

        long elapsed = System.currentTimeMillis() - start;
        LOG.info("Perfect pool tested " + (LOOPS * THREADS) + " connections in " + elapsed + " milliseconds.");

    }

    public void testProxool() throws SQLException, ClassNotFoundException {

        LOG.info("Testing proxool");

        TestHelper.registerPool(ALIAS_PERFORMANCE);

        long start = System.currentTimeMillis();

        Connection[] c = new Connection[THREADS];
        String url = TestHelper.getSimpleUrl(ALIAS_PERFORMANCE);
        for (int i = 0; i < LOOPS; i++) {
            for (int j = 0; j < THREADS; j++) {
                c[j] = TestHelper.getProxoolConnection(url);
                TestHelper.testConnection(c[j]);
            }
            for (int j = 0; j < THREADS; j++) {
                c[j].close();
            }
        }

        long elapsed = System.currentTimeMillis() - start;
        LOG.info("Proxool tested " +  (LOOPS * THREADS) + " connections in " + elapsed + " milliseconds.");

    }



}
