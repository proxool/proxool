/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.cglib;


import org.logicalcobwebs.cglib.proxy.MethodInterceptor;
import org.logicalcobwebs.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

/**
 * See {@link EnhancerTest}
 * @version $Revision: 1.1 $, $Date: 2004/06/02 20:54:57 $
 * @author billhorsman
 * @author $Author: billhorsman $ (current maintainer)
 */
public class MyProxy implements MethodInterceptor {

    private MyConcreteClass myConcreteClass;

    public MyProxy(MyConcreteClass myConcreteClass) {
        this.myConcreteClass = myConcreteClass;
    }

    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        if (method.getName().equals("foo")) {
            return "proxiedFoo";
        } else {
            return method.invoke(myConcreteClass, args);
        }
    }

}
/*
 Revision history:
 $Log: MyProxy.java,v $
 Revision 1.1  2004/06/02 20:54:57  billhorsman
 Learning test class for Enhancer. It fails (or would if the assert was uncommented). Left in for knowledge.

*/