/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Tells you the version. You can tell what sort of release it is
 * from the version. For instance:
 *
 * 1.0.0 (1 January)
 *   A proper released binary file at version 1.0.
 *
 * 1.0.0+
 *   Built from the source based on version 1.0 (but there is no
 *   way of knowing whether the source has been altered).
 *
 * 1.0.1 (2 January)
 *   A bug fix release built on January 2nd.
 *
 * @version $Revision: 1.23 $, $Date: 2008/08/23 10:08:08 $
 * @author bill
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.6
 */
public class Version {

    private static final Log LOG = LogFactory.getLog(Version.class);

    /**
     * This is changed by the Ant script when you build from the
     * source code.
     */
    private static final String VERSION = null;

    private static final String BUILD_DATE = null;

    private static final String CVS = "0.9.1+";

    public static String getVersion() {
        StringBuffer version = new StringBuffer();

        if (VERSION != null) {
            version.append(VERSION);
        } else {
            /**
             * This means that we haven't used the Ant script so this
             * is just our best guess at the version.
             */
            version.append(CVS);
        }

        if (BUILD_DATE != null) {
            version.append(" (");
            version.append(BUILD_DATE);
            version.append(")");
        }

        return version.toString();
    }

    /**
     * Convenient way of verifying version
     * @param args none required (any sent are ignored)
     */
    public static void main(String[] args) {
        LOG.info("Version " + getVersion());
    }

}

/*
 Revision history:
 $Log: Version.java,v $
 Revision 1.23  2008/08/23 10:08:08  billhorsman
 Version 0.9.1 - packaging of Cglib

 Revision 1.22  2008/08/19 19:17:11  billhorsman
 Version 0.9.0

 Revision 1.21  2007/01/10 09:24:35  billhorsman
 0.9.0RC3

 Revision 1.20  2006/01/18 14:40:02  billhorsman
 Unbundled Jakarta's Commons Logging.

 Revision 1.19  2005/09/26 21:47:46  billhorsman
 no message

 Revision 1.18  2003/12/13 12:21:54  billhorsman
 Release 0.8.3

 Revision 1.17  2003/11/05 00:19:48  billhorsman
 new revision

 Revision 1.16  2003/10/27 13:13:58  billhorsman
 new version

 Revision 1.15  2003/10/26 15:23:30  billhorsman
 0.8.0

 Revision 1.14  2003/10/01 19:31:26  billhorsman
 0.8.0RC2

 Revision 1.13  2003/09/11 11:17:52  billhorsman
 *** empty log message ***

 Revision 1.12  2003/08/30 11:43:32  billhorsman
 Update for next release.

 Revision 1.11  2003/07/23 06:54:48  billhorsman
 draft JNDI changes (shouldn't effect normal operation)

 Revision 1.10  2003/06/18 10:04:47  billhorsman
 versioning

 Revision 1.9  2003/03/12 15:59:53  billhorsman
 *** empty log message ***

 Revision 1.8  2003/03/03 11:11:58  billhorsman
 fixed licence

 Revision 1.7  2003/02/21 15:19:09  billhorsman
 update version

 Revision 1.6  2003/02/12 00:50:34  billhorsman
 change the CVS version to be x.y+ (a bit more informative)

 Revision 1.5  2003/02/06 17:41:05  billhorsman
 now uses imported logging

 Revision 1.4  2003/01/21 10:56:40  billhorsman
 new version approach

 Revision 1.3  2003/01/16 11:45:02  billhorsman
 changed format from x.y+ to x.y.*

 Revision 1.2  2003/01/16 11:22:00  billhorsman
 new version

 Revision 1.1  2003/01/14 23:50:59  billhorsman
 keeps track of version

 */