/*
* Copyright 2002, Findexa AS (http://www.findexa.no)
*
* This software is the proprietary information of Findexa AS.
* Use is subject to license terms.
*/
package org.logicalcobwebs.dbscript;

import java.util.Properties;
import java.util.List;
import java.util.Vector;

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
class Script {

    private String name;

    private String driver;

    private String url;

    private Properties info = new Properties();

    private List commands = new Vector();

    protected void addCommand(Command command) {
        commands.add(command);
    }

    protected Command[] getCommands() {
        return (Command[])commands.toArray(new Command[commands.size()]);
    }

    protected String getName() {
        return name;
    }

    protected void setName(String name) {
        this.name = name;
    }

    protected String getUrl() {
        return url;
    }

    protected void setUrl(String url) {
        this.url = url;
    }

    protected String getDriver() {
        return driver;
    }

    protected void setDriver(String driver) {
        this.driver = driver;
    }

    protected Properties getInfo() {
        return info;
    }

    protected void addProperty(String name, String value) {
        info.setProperty(name, value);
    }

}

/*
 Revision history:
 $Log: Script.java,v $
 Revision 1.1  2002/11/02 11:29:53  billhorsman
 new script runner for testing

*/
