/*
* Copyright 2002, Findexa AS (http://www.findexa.no)
*
* This software is the proprietary information of Findexa AS.
* Use is subject to license terms.
*/
package org.logicalcobwebs.dbscript;

/**
 * <link rel="stylesheet" href="{@docRoot}/cg.css" type="text/css">
 *
 * An SQL command that isrun by a {@link Script}. If {@link #getLoad load}
 * or {@link #getLoops loops} are configured then it might run more than
 * once.
 *
 * @version $Revision: 1.2 $, $Date: 2002/11/02 14:22:16 $
 * @author Bill Horsman (bill@logicalcobwebs.co.uk)
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.5
 */
class Command {

    private String name;

    private String sql;

    private int load = 1;

    private int loops = 1;

    private boolean ignoreException;

    /**
     * The SQL statement to run
     * @return sql
     */
    protected String getSql() {
        return sql;
    }

    /**
     * @see #getSql
     */
    protected void setSql(String sql) {
        this.sql = sql;
    }

    /**
     * How many "threads" to simulate. See {@link Script} to see how
     * it actually implements thread-like behaviour.
     * @return load
     */
    protected int getLoad() {
        return load;
    }

    /**
     * @see #getLoad
     */
    protected void setLoad(int load) {
        this.load = load;
    }

    /**
     * The number of loops to perform. Each loop will run the {@link #getSql sql}
     * {@link #getLoad load} times.
     * @return loops
     */
    protected int getLoops() {
        return loops;
    }

    /**
     * @see #getLoops
     */
    protected void setLoops(int loops) {
        this.loops = loops;
    }

    /**
     * If true then errors that occur during this command are logged as debug
     * messages but do not stop the {@link Script script} running.
     * @return true if exceptions should be ignored
     */
    public boolean isIgnoreException() {
        return ignoreException;
    }

    /**
     * @see #isIgnoreException
     */
    public void setIgnoreException(boolean ignoreException) {
        this.ignoreException = ignoreException;
    }

    /**
     * A convenient name to call this command to help logging.
     * @return name
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
 Revision 1.2  2002/11/02 14:22:16  billhorsman
 Documentation

 Revision 1.1  2002/11/02 11:29:53  billhorsman
 new script runner for testing

*/
