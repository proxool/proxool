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
 * TODO
 *
 * @version $Revision: 1.1 $, $Date: 2002/11/06 21:07:42 $
 * @author Bill Horsman (bill@logicalcobwebs.co.uk)
 * @author $Author: billhorsman $ (current maintainer)
 * @since GSI 5.0
 */
public interface CommandIF {

    /**
     * The SQL statement to run
     * @return sql
     */
    String getSql();

    /**
     * How many "threads" to simulate. See {@link org.logicalcobwebs.dbscript.Script} to see how
     * it actually implements thread-like behaviour.
     * @return load
     */
    int getLoad();

    /**
     * The number of loops to perform. Each loop will run the {@link #getSql sql}
     * {@link #getLoad load} times.
     * @return loops
     */
    int getLoops();

    /**
     * If true then errors that occur during this command are logged as debug
     * messages but do not stop the {@link org.logicalcobwebs.dbscript.Script script} running.
     * @return true if exceptions should be ignored
     */
    boolean isIgnoreException();

    /**
     * A convenient name to call this command to help logging.
     * @return name
     */
    String getName();

}

/*
 Revision history:
 $Log: CommandIF.java,v $
 Revision 1.1  2002/11/06 21:07:42  billhorsman
 New interfaces to allow filtering of commands

*/
