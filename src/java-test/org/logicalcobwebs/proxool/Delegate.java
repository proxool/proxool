/*
* Copyright 2002, Findexa AS (http://www.findex.no)
*
* This software is the proprietary information of Findexa AS.
* Use is subject to license terms.
*/
package org.logicalcobwebs.proxool;

/**
 * TODO 24-Aug-2002;bill;high; Add doc
 *
 * @version $Revision: 1.1 $
 * @author Bill Horsman (bill@logicalcobwebs.co.uk)
 * @author $Author: billhorsman $ (current maintainer)
 * @since TODO 24-Aug-2002;bill;high;complete
 */
public class Delegate implements DelegateIF {

    private String foo;

    public Delegate(String foo) {
        this.foo = foo;
    }

    public String getFoo() {
        return foo;
    }

    public void setFoo(String foo) {
        this.foo = foo;
    }

}

/*
 Revision history:
 $Log: Delegate.java,v $
 Revision 1.1  2002/09/13 08:14:19  billhorsman
 Initial revision

*/