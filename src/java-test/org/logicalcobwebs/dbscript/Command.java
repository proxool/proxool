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
 * @version $Revision: 1.3 $, $Date: 2002/11/06 21:07:03 $
 * @author Bill Horsman (bill@logicalcobwebs.co.uk)
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.5
 */
class Command implements CommandIF {

    private String name;

    private String sql;

    private int load = 1;

    private int loops = 1;

    private boolean ignoreException;

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
        return ignoreException;
    }

    /**
     * @see #isIgnoreException
     */
    public void setIgnoreException(boolean ignoreException) {
        this.ignoreException = ignoreException;
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
 Revision 1.3  2002/11/06 21:07:03  billhorsman
 Now supports the CommandIF interface

 Revision 1.2  2002/11/02 14:22:16  billhorsman
 Documentation

 Revision 1.1  2002/11/02 11:29:53  billhorsman
 new script runner for testing

*/
