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

/**
 * Test what the performance of Proxool is compared to a "theoretical"
 * perfect pool. We can recreate the perfect pool fairly easily as long
 * as we keep a single threaded model (and because most systems work
 * in a multi-threaded environment is why we need a pool).
 *
 * @version $Revision: 1.3 $
 * @author Bill Horsman (bill@logicalcobwebs.co.uk)
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.5
 */
public class Performance extends TestCase {

    private static final Log LOG = LogFactory.getLog(Performance.class);

    private static final int LOOPS = 1000;

    private static final int THREADS = 10;

    private static final int MAX_PROXIES = 10000;

    private static final String ALIAS_PERFORMANCE = "performance";

    /**
     * @see TestCase#TestCase
     */
    public Performance(String s) {
        super(s);
    }

    protected void setUp() throws Exception {
        AllTests.setup();
        TestHelper.setupDatabase();
    }

    protected void tearDown() throws Exception {
        TestHelper.tearDownDatabase();
        AllTests.teardown();
    }

    /**
     * Test how Proxool matches up to the perfect pool
     *
     * @throws SQLException if there is any problem
     * @throws ClassNotFoundException if we can't find a driver
     */
    public void testProxool() throws SQLException, ClassNotFoundException {

        double perfect = getPerfectPoolTime();
        double proxool = getProxoolTime();

        LOG.info("Proxool overhead is " + (proxool - perfect) + " milliseconds.");
    }

    private double getPerfectPoolTime() throws SQLException, ClassNotFoundException {

        LOG.info("Testing perfect pool");

        Connection c = TestHelper.getDirectConnection();
        long start = System.currentTimeMillis();

        for (int i = 0; i < LOOPS * THREADS; i++) {
            TestHelper.testConnection(c);
        }
        c.close();

        long elapsed = System.currentTimeMillis() - start;
        LOG.info("Perfect pool tested " + (LOOPS * THREADS) + " connections in " + elapsed + " milliseconds.");

        return (elapsed / (double) (LOOPS * THREADS));
    }

    private double getProxoolTime() throws SQLException, ClassNotFoundException {

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
        LOG.info("Proxool tested " + (LOOPS * THREADS) + " connections in " + elapsed + " milliseconds.");

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
