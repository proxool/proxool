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

/**
 * Invokes a method using a cached method.
 * @version $Revision: 1.1 $, $Date: 2004/06/02 20:43:53 $
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
        Object key = concreteClass.getName() + ":" + injectableMethod.getName();
        MethodMapper methodMapper = (MethodMapper) methodMappers.get(key);
        if (methodMapper == null) {
            methodMapper = new MethodMapper(concreteClass);
            methodMappers.put(key, methodMapper);
        }
        return methodMapper.getConcreteMethod(injectableMethod);
    }

}
/*
 Revision history:
 $Log: InvokerFacade.java,v $
 Revision 1.1  2004/06/02 20:43:53  billhorsman
 New classes to support injectable interfaces

*/