/*
* Copyright 2002, Findexa AS (http://www.findexa.no)
*
* This software is the proprietary information of Findexa AS.
* Use is subject to license terms.
*/
package org.logicalcobwebs.dbscript;

import java.util.List;
import java.util.Properties;
import java.util.Vector;

/**
 * <link rel="stylesheet" href="{@docRoot}/cg.css" type="text/css">
 *
 * Defines a set of {@link #getCommands commands} to run. And which
 * {@link #getDriver driver} to use. And its {@link #getInfo configuration}.
 *
 * @version $Revision: 1.3 $, $Date: 2002/11/02 14:22:16 $
 * @author Bill Horsman (bill@logicalcobwebs.co.uk)
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.5
 */
class Script {

    private String name;

    private String driver;

    private String url;

    private Properties info = new Properties();

    private List commands = new Vector();

    /**
     * Add a command to the script.
     * @param command to add
     */
    protected void addCommand(Command command) {
        commands.add(command);
    }

    /**
     * Get all the commands, in the order in which they were added
     * @return list of commands
     */
    protected Command[] getCommands() {
        return (Command[]) commands.toArray(new Command[commands.size()]);
    }

    /**
     * So we can recognise this script in the logs
     * @return name
     */
    protected String getName() {
        return name;
    }

    /**
     * @see #getName
     */
    protected void setName(String name) {
        this.name = name;
    }

    /**
     * The URL to pass to the Driver
     */
    protected String getUrl() {
        return url;
    }

    /**
     * @see #getUrl
     */
    protected void setUrl(String url) {
        this.url = url;
    }

    /**
     * The driver to use
     * @return the driver
     */
    protected String getDriver() {
        return driver;
    }

    /**
     * @see #getDriver
     */
    protected void setDriver(String driver) {
        this.driver = driver;
    }

    /**
     * Configuration of the Driver
     * @return properties
     */
    protected Properties getInfo() {
        return info;
    }

    /**
     * Add a new property
     * @param name name of property
     * @param value value of property
     */
    protected void addProperty(String name, String value) {
        info.setProperty(name, value);
    }

}

/*
 Revision history:
 $Log: Script.java,v $
 Revision 1.3  2002/11/02 14:22:16  billhorsman
 Documentation

 Revision 1.2  2002/11/02 13:57:34  billhorsman
 checkstyle

 Revision 1.1  2002/11/02 11:29:53  billhorsman
 new script runner for testing

*/
