/*
* Copyright 2002, Findexa AS (http://www.findex.no)
*
* This software is the proprietary information of Findexa AS.
* Use is subject to license terms.
*/
package org.logicalcobwebs.proxool;

import org.logicalcobwebs.proxool.Delegate;
import org.logicalcobwebs.proxool.DelegateIF;

import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * TODO 24-Aug-2002;bill;high; Add doc
 *
 * @version $Revision: 1.1 $
 * @author Bill Horsman (bill@logicalcobwebs.co.uk)
 * @author $Author: billhorsman $ (current maintainer)
 * @since TODO 24-Aug-2002;bill;high;complete
 */
public class DelegateProxy implements InvocationHandler, WrapperIF {

    private DelegateIF delegate;

    private String goo;

    public DelegateProxy(String foo, String goo) {
        delegate = new Delegate(foo);
        this.goo = goo;
    }

    public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {

        //if (method.getName().equals("getGoo")) {
        //    return goo;
        //} else {
            return method.invoke(delegate, args);
        //'}
    }

    public DelegateIF getDelegate() {
        return delegate;
    }

    public void setDelegate(DelegateIF delegate) {
        this.delegate = delegate;
    }

    public String getGoo() {
        return goo;
    }

    public void setGoo(String goo) {
        this.goo = goo;
    }
}

/*
 Revision history:
 $Log: DelegateProxy.java,v $
 Revision 1.1  2002/09/13 08:14:19  billhorsman
 Initial revision

*/