/*
 * This software is released under the Apache Software Licence. See
 * package.html for details. The latest version is available at
 * http://proxool.sourceforge.net
 */
package org.logicalcobwebs.logging.impl;

import java.security.PrivilegedAction;

/**
 * This used to be an anonymous class in LogFactoryImpl but it
 * was causing bizarre compilation failures in JDK1.2.
 *
 * @version $Revision: 1.2 $, $Date: 2003/02/10 15:49:23 $
 * @author bill
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.7
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
 Revision 1.2  2003/02/10 15:49:23  billhorsman
 extracted anonymous PrivilegedAction class to fix
 bizarre compilation problem with jdk1.2

 */