/*
* Copyright 2002, Findexa AS (http://www.findexa.no)
*
* This software is the proprietary information of Findexa AS.
* Use is subject to license terms.
*/
package org.logicalcobwebs.dbscript;

import org.logicalcobwebs.logging.Log;
import org.logicalcobwebs.logging.LogFactory;

/**
 * An SQL command that isrun by a {@link Script}. If {@link #getLoad load}
 * or {@link #getLoops loops} are configured then it might run more than
 * once.
 *
 * @version $Revision: 1.6 $, $Date: 2003/02/06 17:41:01 $
 * @author Bill Horsman (bill@logicalcobwebs.co.uk)
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.5
 */
class Command implements CommandIF {

    private static final Log LOG = LogFactory.getLog(Command.class);

    private String name;

    private String sql;

    private int load = 1;

    private int loops = 1;

    private String exception;

    /**
     * If this command encounters an exception it will stop executing (and if
     * it is in a loop it will break out of the loop)
     */
    protected static final String EXCEPTION_STOP = "stop";

    /**
     * If this command encounters an exception it will log it at DEBUG
     * level and continue
     */
    protected static final String EXCEPTION_LOG = "log";

    /**
     * If this command encounters an exception it will silently ignore the
     * exception and continue. But it still calls the
     */
    protected static final String EXCEPTION_IGNORE = "ignore";

    /**
     * @see CommandIF#getSql
     */
    public String getSql() {
        return sql;
    }

    /**
     * @see #getSql
     */
    protected void setSql(String sql) {
        this.sql = sql;
    }

    /**
     * @see CommandIF#getLoad
     */
    public int getLoad() {
        return load;
    }

    /**
     * @see #getLoad
     */
    protected void setLoad(int load) {
        this.load = load;
    }

    /**
     * @see CommandIF#getLoops
     */
    public int getLoops() {
        return loops;
    }

    /**
     * @see #getLoops
     */
    protected void setLoops(int loops) {
        this.loops = loops;
    }

    /**
     * @see CommandIF#isIgnoreException
     */
    public boolean isIgnoreException() {
        return exception != null && exception.equals(EXCEPTION_IGNORE);
    }

    /**
     * @see CommandIF#isIgnoreException
     */
    public boolean isLogException() {
        return exception != null && exception.equals(EXCEPTION_LOG);
    }

    /**
     * @see #isIgnoreException
     */
    public void setException(String exception) {
        if (exception == null) {
            this.exception = EXCEPTION_STOP;
            LOG.debug("Setting exception to default " + EXCEPTION_STOP);
        } else if (exception.equals(EXCEPTION_IGNORE)
                || exception.equals(EXCEPTION_LOG)
                || exception.equals(EXCEPTION_STOP)) {
            this.exception = exception;
            LOG.debug("Setting exception to " + exception);
        } else {
            throw new RuntimeException("Unknown exception value: " + exception);
        }
    }

    /**
     * @see CommandIF#getName
     */
    public String getName() {
        return name;
    }

    /**
     * @see #getName
     */
    public void setName(String name) {
        this.name = name;
    }

}

/*
 Revision history:
 $Log: Command.java,v $
 Revision 1.6  2003/02/06 17:41:01  billhorsman
 now uses imported logging

 Revision 1.5  2002/11/09 15:58:12  billhorsman
 fix doc

 Revision 1.4  2002/11/09 14:45:07  billhorsman
 now threaded and better exception handling

 Revision 1.3  2002/11/06 21:07:03  billhorsman
 Now supports the CommandIF interface

 Revision 1.2  2002/11/02 14:22:16  billhorsman
 Documentation

 Revision 1.1  2002/11/02 11:29:53  billhorsman
 new script runner for testing

*/
