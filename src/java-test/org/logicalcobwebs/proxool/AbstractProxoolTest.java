/**
 * Clever Little Trader
 *
 * Jubilee Group and Logical Cobwebs, 2002
 */
package org.logicalcobwebs.proxool;

import junit.framework.TestCase;
import org.logicalcobwebs.logging.Log;
import org.logicalcobwebs.logging.LogFactory;

import java.util.Stack;

/**
 * Provides common code for all Proxool tests
 * @version $Revision: 1.1 $, $Date: 2003/03/03 17:08:54 $
 * @author bill
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.8
 */
public class AbstractProxoolTest extends TestCase {

    private static final Log LOG = LogFactory.getLog(AbstractProxoolTest.class);

    private String alias;

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
    }

    /**
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        GlobalTest.globalTeardown();
        Thread.currentThread().setName((String) threadNames.pop());
        LOG.debug("Thread '" + alias + "' -> '" + Thread.currentThread().getName() + "'");
    }

}


/*
 Revision history:
 $Log: AbstractProxoolTest.java,v $
 Revision 1.1  2003/03/03 17:08:54  billhorsman
 all tests now extend AbstractProxoolTest

 */