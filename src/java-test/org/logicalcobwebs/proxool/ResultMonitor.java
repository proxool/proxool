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
 * @version $Revision: 1.1 $, $Date: 2003/03/01 15:14:15 $
 * @author bill
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.8
 */
abstract public class ResultMonitor {

    private static final Log LOG = LogFactory.getLog(ResultMonitor.class);

    public static final int WAITING = 0;

    public static final int SUCCESS = 1;

    public static final int TIMEOUT = 3;

    private long timeout = 30000;

    private int result = WAITING;

    abstract public boolean check() throws Exception;

    public int getResult() throws ProxoolException {

        try {
            long startTime = System.currentTimeMillis();
            if (check()) {
                result = SUCCESS;
            }
            while (result  != SUCCESS) {
                if (System.currentTimeMillis() - startTime > timeout) {
                    result = TIMEOUT;
                    break;
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    LOG.error("Awoken", e);
                }
                if (check()) {
                    result = SUCCESS;
                }
            }
            return result;
        } catch (Exception e) {
            throw new ProxoolException("Problem monitoring result", e);
        }
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

}


/*
 Revision history:
 $Log: ResultMonitor.java,v $
 Revision 1.1  2003/03/01 15:14:15  billhorsman
 new ResultMonitor to help cope with test threads

 */