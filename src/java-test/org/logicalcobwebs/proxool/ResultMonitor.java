/*
 * This software is released under the Apache Software Licence. See
 * package.html for details. The latest version is available at
 * http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import org.logicalcobwebs.logging.Log;
import org.logicalcobwebs.logging.LogFactory;

/**
 * Waits for a set of results to become true with timeout
 * functionality
 *
 * @version $Revision: 1.6 $, $Date: 2003/03/02 00:53:38 $
 * @author bill
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.8
 */
public abstract class ResultMonitor {

    private static final Log LOG = LogFactory.getLog(ResultMonitor.class);

    /**
     * This monitor is still waiting for the result to come true
     */
    public static final int WAITING = 0;

    /**
     * The result has happened
     */
    public static final int SUCCESS = 1;

    /**
     * There was a timeout waiting for the result to happen
     * @see #setTimeout
     */
    public static final int TIMEOUT = 3;

    /**
     * Seems awfully long, but it seems to need it. Sometimes. 
     */
    private long timeout = 60000;

    private int result = WAITING;

    private int delay = 500;

    /**
     * Override this with your specific check
     * @return true if the result has happened, else false
     * @throws Exception if anything goes wrong
     */
    public abstract boolean check() throws Exception;

    /**
     * Wait for the result to happen, or for a timeout
     * @return {@link #SUCCESS} or {@link #TIMEOUT}
     * @throws ProxoolException if the {@link #check} threw an exception
     * @see #setTimeout
     */
    public int getResult() throws ProxoolException {

        try {
            long startTime = System.currentTimeMillis();
            if (check()) {
                result = SUCCESS;
            }
            while (true) {
                if (System.currentTimeMillis() - startTime > timeout) {
                    result = TIMEOUT;
                    LOG.debug("Timeout");
                    break;
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    LOG.error("Awoken", e);
                }
                if (check()) {
                    result = SUCCESS;
                    LOG.debug("Success");
                    break;
                }
            }
            return result;
        } catch (Exception e) {
            throw new ProxoolException("Problem monitoring result", e);
        }
    }

    /**
     * Set the timeout
     * @param timeout milliseconds
     */
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }
}


/*
 Revision history:
 $Log: ResultMonitor.java,v $
 Revision 1.6  2003/03/02 00:53:38  billhorsman
 increased timeout to 60 sec!

 Revision 1.5  2003/03/01 18:17:51  billhorsman
 arrffgh. fix,

 Revision 1.4  2003/03/01 16:54:20  billhorsman
 fix

 Revision 1.3  2003/03/01 15:27:24  billhorsman
 checkstyle

 Revision 1.2  2003/03/01 15:22:50  billhorsman
 doc

 Revision 1.1  2003/03/01 15:14:15  billhorsman
 new ResultMonitor to help cope with test threads

 */