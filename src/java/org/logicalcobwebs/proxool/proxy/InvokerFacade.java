/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool.proxy;

import org.logicalcobwebs.proxool.ProxoolException;

import java.util.Map;
import java.util.HashMap;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Invokes a method using a cached method.
 * @version $Revision: 1.3 $, $Date: 2004/07/13 21:13:14 $
 * @author billhorsman
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.9
 */
public class InvokerFacade {
    
    private static Map methodMappers = new HashMap();
    
    /**
     * Returns the method in the concrete class with an indentical signature to that passed
     * @param concreteClass the class that we want to invoke methods on. It should either implement all methods on
     * the injectable interface, or provide methods with an identical signature.
     * @param injectableMethod provides signature that we are trying to match
     * @return the method in the concrete class that we can invoke as if it were in the interface
     * @throws org.logicalcobwebs.proxool.ProxoolException if the method is not found.
     */
    public static Method getConcreteMethod(Class concreteClass, Method injectableMethod) throws ProxoolException {
        // Unless the concrete class is public we can't do anything
        if (Modifier.isPublic(concreteClass.getModifiers())) {
            Object key = concreteClass.getName() + ":" + injectableMethod.getName();
            MethodMapper methodMapper = (MethodMapper) methodMappers.get(key);
            if (methodMapper == null) {
                methodMapper = new MethodMapper(concreteClass);
                methodMappers.put(key, methodMapper);
            }
            return methodMapper.getConcreteMethod(injectableMethod);
        } else {
            return injectableMethod;
        }
    }

    /**
     * Override the method provided by the {@link #getConcreteMethod(java.lang.Class, java.lang.reflect.Method)}. Use this
     * if you decide that the concrete method provided wasn't any good. For instance, if you get an IllegalAccessException
     * whilst invoking the concrete method then you should perhaps try using the proxy supplied method instead.
     * @param concreteClass the class we are invoking upon
     * @param injectableMethod the method supplied by the proxy
     * @param overridenMethod the one we are going to use (probably the same as injectrableMethod actually)
     */
    public static void overrideConcreteMethod(Class concreteClass, Method injectableMethod, Method overridenMethod) {
        Object key = concreteClass.getName() + ":" + injectableMethod.getName();
        MethodMapper methodMapper = (MethodMapper) methodMappers.get(key);
        if (methodMapper == null) {
            methodMapper = new MethodMapper(concreteClass);
            methodMappers.put(key, methodMapper);
        }
        methodMapper.overrideConcreteMethod(injectableMethod, overridenMethod);
    }

}
/*
 Revision history:
 $Log: InvokerFacade.java,v $
 Revision 1.3  2004/07/13 21:13:14  billhorsman
 Optimise using injectable interfaces on methods that are declared in non-public classes by not bothering to use concrete methods at all (it's not possible).

 Revision 1.2  2004/07/13 21:06:16  billhorsman
 Fix problem using injectable interfaces on methods that are declared in non-public classes.

 Revision 1.1  2004/06/02 20:43:53  billhorsman
 New classes to support injectable interfaces

*/