/*
* Copyright 2002, Findexa AS (http://www.findex.no)
*
* This software is the proprietary information of Findexa AS.
* Use is subject to license terms.
*/
package org.logicalcobwebs.proxool;

import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Test what the performance of Proxool is compared to a "theoretical"
 * perfect pool. We can recreate the perfect pool fairly easily as long
 * as we keep a single threaded model (and because most systems work
 * in a multi-threaded environment is why we need a pool).
 *
 * @version $Revision: 1.5 $
 * @author Bill Horsman (bill@logicalcobwebs.co.uk)
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.5
 */
public class Performance extends TestCase {

    private static final Log LOG = LogFactory.getLog(Performance.class);

    private String DIRECT_TABLE = "direct";

    private String PROXOOL_TABLE = "proxool";

    private static final int LOOPS = 1000;

    private static final int THREADS = 5;

    private static final int MAX_PROXIES = 10000;

    private static final String ALIAS_PERFORMANCE = "performance";

    /**
     * @see TestCase#TestCase
     */
    public Performance(String s) {
        super(s);
    }

    protected void setUp() throws Exception {
        AllTests.globalSetup();
        TestHelper.createTable(DIRECT_TABLE);
        TestHelper.createTable(PROXOOL_TABLE);
    }

    protected void tearDown() throws Exception {
        TestHelper.dropTable(DIRECT_TABLE);
        TestHelper.dropTable(PROXOOL_TABLE);
        AllTests.globalTeardown();
    }

    /**
     * Test how Proxool matches up to the direct pool
     *
     * @throws SQLException if there is any problem
     * @throws ClassNotFoundException if we can't find a driver
     */
    public void testProxool() throws SQLException, ClassNotFoundException {

        double direct = getDirectPoolTime();
        double proxool = getProxoolTime();

        LOG.info("Proxool overhead is " + (proxool - direct) + " milliseconds.");
    }

    private double getDirectPoolTime() {

        long elapsed = 0;
        try {
            LOG.info("Testing direct pool");

            Connection[] c = new Connection[THREADS];
            for (int j = 0; j < THREADS; j++) {
                c[j] = TestHelper.getDirectConnection();
            }

            long start = System.currentTimeMillis();

            for (int i = 0; i < LOOPS; i++) {
                for (int j = 0; j < THREADS; j++) {
                    TestHelper.insertRow(c[j], DIRECT_TABLE);
                }
            }

            for (int j = 0; j < THREADS; j++) {
                c[j].close();
            }

            int count = TestHelper.getCount(TestHelper.getDirectConnection(), DIRECT_TABLE);
            assertEquals("Wrong number of rows added", (LOOPS * THREADS), count);

            elapsed = System.currentTimeMillis() - start;
            LOG.info("Perfect pool added " + (LOOPS * THREADS) + " rows in " + elapsed + " milliseconds.");
        } catch (Exception e) {
            LOG.error("Eh?", e);
        }

        return (elapsed / (double) (LOOPS * THREADS));
    }

    private double getProxoolTime() throws SQLException, ClassNotFoundException {

        LOG.info("Testing proxool");

        Properties info = TestHelper.buildProperties();
        info.setProperty("proxool.maximum-connection-count", "10");
        info.setProperty("proxool.minimum-connection-count", "10");
        info.setProperty("proxool.prototypes-count", "10");
        TestHelper.registerPool(ALIAS_PERFORMANCE, info);

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            LOG.error("Awoken", e);
        }

        long start = System.currentTimeMillis();

        Connection[] c = new Connection[THREADS];
        String url = TestHelper.getSimpleUrl(ALIAS_PERFORMANCE);
        for (int i = 0; i < LOOPS; i++) {
            for (int j = 0; j < THREADS; j++) {
                c[j] = TestHelper.getProxoolConnection(url);
                TestHelper.insertRow(c[j], PROXOOL_TABLE);
            }
            for (int k = 0; k < THREADS; k++) {
                c[k].close();
            }
        }

        int count = TestHelper.getCount(TestHelper.getProxoolConnection(url), PROXOOL_TABLE);
        assertEquals("Wrong number of rows added", (LOOPS * THREADS), count);

        long elapsed = System.currentTimeMillis() - start;
        LOG.info("Proxool added " + (LOOPS * THREADS) + " rows in " + elapsed + " milliseconds.");

        return (elapsed / (double) (LOOPS * THREADS));

    }

    /**
     * Test the overhead of using the {@link Proxy} class instead of normal
     * delegation.
     */
    public void testProxy() {

        double direct = getDirectTime();
        double proxy = getProxyTime();

        LOG.info("Proxy overhead is " + (proxy - direct) + " milliseconds.");
    }

    private double getDirectTime() {

        double start = (double) System.currentTimeMillis();
        for (int i = 0; i < MAX_PROXIES; i++) {
            DelegateIF delegate = new Delegate("direct");
            delegate.getFoo();
            delegate.getFoo();
        }
        double elapsed = (double) System.currentTimeMillis() - start;

        return (elapsed / (double) (MAX_PROXIES));

    }

    private double getProxyTime() {

        double start = (double) System.currentTimeMillis();
        for (int i = 0; i < MAX_PROXIES; i++) {
            InvocationHandler ih = new DelegateProxy("Test", "Gest");
            DelegateIF delegate = (DelegateIF) Proxy.newProxyInstance(DelegateIF.class.getClassLoader(), new Class[]{DelegateIF.class}, ih);
            delegate.getFoo();
            delegate.getFoo();
        }
        double elapsed = (double) System.currentTimeMillis() - start;

        return (elapsed / (double) (MAX_PROXIES));

    }

}
