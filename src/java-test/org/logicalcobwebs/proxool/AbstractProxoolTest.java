/**
 * Clever Little Trader
 *
 * Jubilee Group and Logical Cobwebs, 2002
 */
package org.logicalcobwebs.proxool;

import junit.framework.TestCase;
import org.logicalcobwebs.logging.Log;
import org.logicalcobwebs.logging.LogFactory;
import org.logicalcobwebs.concurrent.ReaderPreferenceReadWriteLock;

import java.util.Stack;

/**
 * Provides common code for all Proxool tests
 * @version $Revision: 1.4 $, $Date: 2003/09/30 19:09:46 $
 * @author bill
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.8
 */
public abstract class AbstractProxoolTest extends TestCase {

    private static final Log LOG = LogFactory.getLog(AbstractProxoolTest.class);

    private String alias;

    private static ReaderPreferenceReadWriteLock testLock = new ReaderPreferenceReadWriteLock();

    private Stack threadNames = new Stack();

    public AbstractProxoolTest(String alias) {
        super(alias);
        this.alias = alias;
    }

    /**
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        GlobalTest.globalSetup();
        testLock.writeLock().acquire();
        threadNames.push(Thread.currentThread().getName());
        LOG.debug("Thread '" + Thread.currentThread().getName() + "' -> '" + alias + "'");
        Thread.currentThread().setName(alias);
    }

    /**
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        GlobalTest.globalTeardown(alias);
        testLock.writeLock().release();
        Thread.currentThread().setName((String) threadNames.pop());
        LOG.debug("Thread '" + alias + "' -> '" + Thread.currentThread().getName() + "'");
    }

}


/*
 Revision history:
 $Log: AbstractProxoolTest.java,v $
 Revision 1.4  2003/09/30 19:09:46  billhorsman
 Now uses a readwrite lock to make sure that each test runs sequentially. This should be true all the time, but sometimes
 tests fail and it is always because of some timing issue that is very hard to track down. This is an attempt to
 fix that.

 Revision 1.3  2003/03/04 10:11:09  billhorsman
 actually made abstract

 Revision 1.2  2003/03/03 17:38:47  billhorsman
 leave shutdown to AbstractProxoolTest

 Revision 1.1  2003/03/03 17:08:54  billhorsman
 all tests now extend AbstractProxoolTest

 */