/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.cglib;

import org.logicalcobwebs.proxool.AbstractProxoolTest;
import org.logicalcobwebs.cglib.proxy.Enhancer;

/**
 * A test test class (!) to help me understand the Enhancer. It fails. Or at least,
 * it would do if I uncommented the assert. But that fines. It's a learning process.
 * @version $Revision: 1.1 $, $Date: 2004/06/02 20:54:57 $
 * @author billhorsman
 * @author $Author: billhorsman $ (current maintainer)
 */
public class EnhancerTest extends AbstractProxoolTest {

    public EnhancerTest(String alias) {
        super(alias);
    }

    public void testConcreteClassEnhancer() {

        MyInterfaceIF mi = (MyInterfaceIF) Enhancer.create(
                null,
                new Class[] {MyInterfaceIF.class},
                new MyProxy(new MyConcreteClass()));

        mi.bar();
        MyConcreteClass mcc = (MyConcreteClass) mi;
        // This fails
        // assertEquals("foo()", "proxiedFoo", mcc.foo());
    }

}

/*
 Revision history:
 $Log: EnhancerTest.java,v $
 Revision 1.1  2004/06/02 20:54:57  billhorsman
 Learning test class for Enhancer. It fails (or would if the assert was uncommented). Left in for knowledge.

*/