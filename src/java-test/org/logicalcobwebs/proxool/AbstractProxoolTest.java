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
 * @version $Revision: 1.5 $, $Date: 2004/03/26 16:00:23 $
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
        threadNames.push(Thread.currentThread().getName());
        LOG.debug("Thread '" + Thread.currentThread().getName() + "' -> '" + alias + "'");
        Thread.currentThread().setName(alias);
        testLock.writeLock().acquire();
    }

    /**
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        try {
            GlobalTest.globalTeardown(alias);
            Thread.currentThread().setName((String) threadNames.pop());
            LOG.debug("Thread '" + alias + "' -> '" + Thread.currentThread().getName() + "'");
        } finally {
            testLock.writeLock().release();
        }
    }

}


/*
 Revision history:
 $Log: AbstractProxoolTest.java,v $
 Revision 1.5  2004/03/26 16:00:23  billhorsman
 Make sure we release lock on tearDown. I don't think this was a problem, but it was unrobust.

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