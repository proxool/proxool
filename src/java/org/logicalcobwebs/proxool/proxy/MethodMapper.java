/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool.proxy;

import org.logicalcobwebs.proxool.ProxoolException;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.HashMap;

/**
 * Handles the mapping between methods with identical signatures but that are not related
 * by inheritance. This allows you to invoke a method on a class using an interface that
 * it doesn't actually implement. It caches the result of its reflective lookup to save time.
 * If the concreteClass does in fact implement the injectable interface then it quickly
 * returns the method without the penalty of mapping using reflection.
 *
 * @author <a href="mailto:bill@logicalcobwebs.co.uk">Bill Horsman</a>
 * @author $Author: billhorsman $ (current maintainer)
 * @version $Revision: 1.2 $, $Date: 2004/07/13 21:06:18 $
 * @since Proxool 0.9
 */
public class MethodMapper {

    private Class concreteClass;

    private Map cachedConcreteMethods = new HashMap();
    ;
    /**
     * @param concreteClass the class that we want to invoke methods on. It should either implement all methods on
     *                      the injectable interface, or provide methods with an identical signature.
     */
    public MethodMapper(Class concreteClass) {
        this.concreteClass = concreteClass;
    }

    /**
     * Returns the method in the concrete class with an indentical signature to that passed
     * as a parameter
     *
     * @param injectableMethod provides signature that we are trying to match
     * @return the method in the concrete class that we can invoke as if it were in the interface
     * @throws org.logicalcobwebs.proxool.ProxoolException
     *          if the method is not found.
     */
    protected Method getConcreteMethod(Method injectableMethod) throws ProxoolException {
        // Do we have a cached reference?
        Method concreteMethod = (Method) cachedConcreteMethods.get(injectableMethod);
        if (concreteMethod == null) {
            // Look it up
            Method[] candidateMethods = concreteClass.getMethods();
            for (int i = 0; i < candidateMethods.length; i++) {
                Method candidateMethod = candidateMethods[i];
                // First pass: does the name, parameter count and return type match?
                if (candidateMethod.getName().equals(injectableMethod.getName()) &&
                        candidateMethod.getParameterTypes().length == injectableMethod.getParameterTypes().length &&
                        candidateMethod.getReturnType().equals(injectableMethod.getReturnType())) {
                    // Let's check each parameter type
                    boolean matches = true;
                    Class[] candidateTypes = candidateMethod.getParameterTypes();
                    Class[] injectableTypes = injectableMethod.getParameterTypes();
                    for (int j = 0; j < candidateTypes.length; j++) {
                        if (!candidateTypes[j].equals(injectableTypes[j])) {
                            matches = false;
                            break;
                        }

                    }
                    if (matches) {
                        concreteMethod = candidateMethod;
                        break;
                    }
                }
            }
            // Success?
            if (concreteMethod == null) {
                throw new ProxoolException("Couldn't match injectable method " + injectableMethod + " with any of those " +
                        "found in " + concreteClass.getName());
            }
            // Remember it
            cachedConcreteMethods.put(injectableMethod, concreteMethod);
        }
        return concreteMethod;
    }

    /**
     * Don't use the one we calculate using {@link #getConcreteMethod(java.lang.reflect.Method)}, use this one instead.
     * @param injectableMethod the method supplied by the proxy
     * @param overridenMethod the one we are going to use (probably the same as injectrableMethod actually)
     */ 
    public void overrideConcreteMethod(Method injectableMethod, Method overridenMethod) {
        cachedConcreteMethods.put(injectableMethod, overridenMethod);
    }

}

/*
 Revision history:
 $Log: MethodMapper.java,v $
 Revision 1.2  2004/07/13 21:06:18  billhorsman
 Fix problem using injectable interfaces on methods that are declared in non-public classes.

 Revision 1.1  2004/06/02 20:43:53  billhorsman
 New classes to support injectable interfaces

*/