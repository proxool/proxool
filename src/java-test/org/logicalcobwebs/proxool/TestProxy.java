/*
* Copyright 2002, Findexa AS (http://www.findex.no)
*
* This software is the proprietary information of Findexa AS.
* Use is subject to license terms.
*/
package org.logicalcobwebs.proxool;

import junit.framework.TestCase;

import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationHandler;

import org.logicalcobwebs.proxool.Delegate;
import org.logicalcobwebs.proxool.DelegateIF;
import org.logicalcobwebs.proxool.DelegateProxy;

/**
 * TODO 24-Aug-2002;bill;high; Add doc
 *
 * @version $Revision: 1.1 $
 * @author Bill Horsman (bill@logicalcobwebs.co.uk)
 * @author $Author: billhorsman $ (current maintainer)
 * @since TODO 24-Aug-2002;bill;high;complete
 */
public class TestProxy extends TestCase {

    public TestProxy(String s) {
        super(s);
    }

    public void testProxy() {

        InvocationHandler ih = new DelegateProxy("Test", "Gest");
        DelegateIF delegate = (DelegateIF) Proxy.newProxyInstance(DelegateIF.class.getClassLoader(), new Class[] {DelegateIF.class}, ih);

        WrapperIF wrapper = (WrapperIF) Proxy.getInvocationHandler(delegate);

        System.out.println("foo = " + delegate.getFoo());
        System.out.println("goo = " + wrapper.getGoo());
        System.out.println("delegate = " + delegate.getClass().getName());

    }

    public void testDirectPerformance() {

        double start = (double)System.currentTimeMillis();
        for (int i = 0; i < max; i++) {
            DelegateIF delegate = new Delegate("direct");
            delegate.getFoo();
            delegate.getFoo();
        }
        double elapsed = (double)System.currentTimeMillis() - start;
        System.out.println("Direct = " + (elapsed / max) + " milliseconds.");

    }

    public void testProxyPerformance() {

        double start = (double)System.currentTimeMillis();
        for (int i = 0; i < max; i++) {
            InvocationHandler ih = new DelegateProxy("Test", "Gest");
            DelegateIF delegate = (DelegateIF) Proxy.newProxyInstance(DelegateIF.class.getClassLoader(), new Class[] {DelegateIF.class}, ih);
            delegate.getFoo();
            delegate.getFoo();
        }
        double elapsed = (double)System.currentTimeMillis() - start;
        System.out.println("Proxy = " + (elapsed / max) + " milliseconds.");

    }

    private double max = 10000;
}

/*
 Revision history:
 $Log: TestProxy.java,v $
 Revision 1.1  2002/09/13 08:14:24  billhorsman
 Initial revision

*/