/**
 * Clever Little Trader
 *
 * Jubilee Group and Logical Cobwebs, 2002
 */
package org.logicalcobwebs.logging.impl;

import java.security.PrivilegedAction;

/**
 * TODO
 * @version $Revision: 1.1 $, $Date: 2003/02/10 15:15:00 $
 * @author bill
 * @author $Author: billhorsman $ (current maintainer)
 * @since TODO
 */
public class MyPrivilegedAction implements PrivilegedAction {

    private ClassLoader threadCL;

    private String name;

    public MyPrivilegedAction(ClassLoader threadCL, String name) {
        this.threadCL = threadCL;
        this.name = name;
    }

    public Object run() {
        if (threadCL != null) {
            try {
                return threadCL.loadClass(name);
            } catch (ClassNotFoundException ex) {
                // ignore
            }
        }
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            return e;
        }
    }

}


/*
 Revision history:
 $Log: MyPrivilegedAction.java,v $
 Revision 1.1  2003/02/10 15:15:00  billhorsman
 extracted anonymous PrivilegedAction class to fix
 bizarre compilation problem with jdk1.2

 */