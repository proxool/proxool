/*
 * This software is released under the Apache Software Licence. See
 * package.html for details. The latest version is available at
 * http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

/**
 * Tells you the version. You can tell what sort of release it is
 * from the version. For instance:
 *
 * 1.0 (1 January
 *   A proper released binary file at version 1.0.
 *
 * 1.0.*
 *   Built from the source based on version 1.0 (but there is no
 *   way of knowing whether the source has been altered).
 *
 * 1.0.2003.1.2
 *   A snapshot release built on January 2nd. This comes after the
 *   version 1.0 and before 1.1.
 *
 * @version $Revision: 1.3 $, $Date: 2003/01/16 11:45:02 $
 * @author bill
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.6
 */
public class Version {

    /**
     * This is changed by the Ant script when you build from the
     * source code.
     */
    private static final String RELEASE = null;

    public static String getVersion() {
        if (RELEASE != null) {
            return RELEASE;
        } else {
            /**
             * This means that we haven't used the Ant script so this
             * is just our best guess at the version.
             */
            return "0.6.*";
        }
    }

}

/*
 Revision history:
 $Log: Version.java,v $
 Revision 1.3  2003/01/16 11:45:02  billhorsman
 changed format from x.y+ to x.y.*

 Revision 1.2  2003/01/16 11:22:00  billhorsman
 new version

 Revision 1.1  2003/01/14 23:50:59  billhorsman
 keeps track of version

 */