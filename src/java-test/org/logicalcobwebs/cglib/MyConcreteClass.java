/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.cglib;

import org.logicalcobwebs.logging.Log;
import org.logicalcobwebs.logging.LogFactory;

/**
 * See {@link EnhancerTest}
 * @version $Revision: 1.2 $, $Date: 2004/06/17 21:40:06 $
 * @author billhorsman
 * @author $Author: billhorsman $ (current maintainer)
 */
public class MyConcreteClass implements MyInterfaceIF {

    private static final Log LOG = LogFactory.getLog(MyConcreteClass.class);

    MyConcreteClass() {
        LOG.debug("MyConcreteClass.init");
    }

    public String foo() {
        return "foo";
    }

    public void bar() {}

}

/*
 Revision history:
 $Log: MyConcreteClass.java,v $
 Revision 1.2  2004/06/17 21:40:06  billhorsman
 Log message should be debug not error

 Revision 1.1  2004/06/02 20:54:57  billhorsman
 Learning test class for Enhancer. It fails (or would if the assert was uncommented). Left in for knowledge.

*/