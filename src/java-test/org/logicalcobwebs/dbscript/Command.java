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
 * @version $Revision: 1.1 $, $Date: 2002/11/02 11:29:53 $
 * @author Bill Horsman (bill@logicalcobwebs.co.uk)
 * @author $Author: billhorsman $ (current maintainer)
 * @since GSI 5.0
 */
class Command {

    private String name;

    private String sql;

    private int load = 1;

    private int loops = 1;

    private boolean ignoreException;

    protected String getSql() {
        return sql;
    }

    protected void setSql(String sql) {
        this.sql = sql;
    }

    protected int getLoad() {
        return load;
    }

    protected void setLoad(int load) {
        this.load = load;
    }

    protected int getLoops() {
        return loops;
    }

    protected void setLoops(int loops) {
        this.loops = loops;
    }

    public boolean isIgnoreException() {
        return ignoreException;
    }

    public void setIgnoreException(boolean ignoreException) {
        this.ignoreException = ignoreException;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

/*
 Revision history:
 $Log: Command.java,v $
 Revision 1.1  2002/11/02 11:29:53  billhorsman
 new script runner for testing

*/
