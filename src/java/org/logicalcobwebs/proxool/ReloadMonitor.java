/*
 * This software is released under the Apache Software Licence. See
 * package.html for details. The latest version is available at
 * http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Date;

/**
 * <link rel="stylesheet" href="{@docRoot}/cg.css" type="text/css">
 *
 * TODO
 *
 * @version $Revision: 1.2 $, $Date: 2002/10/25 15:59:32 $
 * @author Bill Horsman (bill@logicalcobwebs.co.uk)
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.5
 */
class ReloadMonitor {

    private static final Log LOG = LogFactory.getLog(ReloadMonitor.class);

    private String poolName;

    /**
     * Used to lookup the time this class was loaded in the System object. Value is
     * the fully qualified classname + ".load-time"
     * @see #myClassLoadTime
     */
    private static final String LOAD_TIME_PROPERTY = ReloadMonitor.class.getName() + ".load-time";

    /**
     * This is a mechanism to avoid multiple instances of this _class_ being loaded
     * at the same time. In some environments, like Weblogic for instance, an
     * application can be reloaded at run time.
     *
     * By remembering the value when the class is instantiated and comparing it to
     * the current value from System.getProperty({@link #LOAD_TIME_PROPERTY})
     * we can determine whether the class has been reloaded.
     *
     * Each {@link ConnectionPool.HouseKeeper#run house keeper} needs to
     * check {@link #isProxoolReloaded} to see whether it should continue
     * running.
     *
     * @see #LOAD_TIME_PROPERTY
     * @see #isProxoolReloaded
     */
    private long myClassLoadTime;

    private static final int FINALIZE_DELAY = 500;

    /**
     * This method gets called every time the class is loaded. By setting the time
     * this happened in a System property (rather than a static variable) then we
     * can spot this happening.
     * @see #myClassLoadTime
     */
    static {
        System.setProperty(LOAD_TIME_PROPERTY, String.valueOf(System.currentTimeMillis()));
    }

    protected ReloadMonitor(String poolName) {
        this.poolName = poolName;
        this.myClassLoadTime = getLoadTimeFromSystemProperty();
    }

    /**
     * Checks whether this class has been reloaded since this instance was instantiated.
     *
     * @return true if the class has been reloaded, false if all is okay
     */
    protected boolean isProxoolReloaded() {
        if (myClassLoadTime != getLoadTimeFromSystemProperty()) {
            ProxoolFacade.removeAllConnectionPools("ReloadMonitor." + poolName, FINALIZE_DELAY);
            return true;
        } else {
            return false;
        }
    }

    protected Date getLoadDate() {
        return new Date(myClassLoadTime);
    }

    private long getLoadTimeFromSystemProperty() {
        try {
            return Long.parseLong(System.getProperty(LOAD_TIME_PROPERTY));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

}

/*
 Revision history:
 $Log: ReloadMonitor.java,v $
 Revision 1.2  2002/10/25 15:59:32  billhorsman
 made non-public where possible

 Revision 1.1  2002/10/25 10:12:52  billhorsman
 Improvements and fixes to the way connection pools close down. Including new ReloadMonitor to detect when a class is reloaded. Much better logging too.

*/
