/*
* Copyright 2002, Findexa AS (http://www.findexa.no)
*
* This software is the proprietary information of Findexa AS.
* Use is subject to license terms.
*/
package org.logicalcobwebs.proxool.configuration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.logicalcobwebs.proxool.ProxoolFacade;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * Uses a standard Java properties file to cofigure Proxool. For example:
 *
 * <pre>
 * jdbc-0.url=proxool.apple:org.hsqldb.jdbcDriver:jdbc:hsqldb:.
 * jdbc-0.user=sa
 * jdbc-0.proxool.maximum-connection-count=10
 * jdbc-1.url=proxool.banana:org.hsqldb.jdbcDriver:jdbc:hsqldb:.
 * jdbc-1.user=sa
 * jdbc-1.proxool.maximum-connection-count=20
 * </pre>
 *
 * <p>The first word (up to the first dot) must start with "jdbc", but it can
 * be anything you like. Use unique names to identify each pool. Any property
 * not starting with "jdbc" will be ignored.</p>
 *
 *<p>Note that there is nothing Proxool specific about this configurator. You can
 * configure any JDBC connection with it.</p>
 *
 * @version $Revision: 1.1 $, $Date: 2002/12/15 18:48:33 $
 * @author Bill Horsman (bill@logicalcobwebs.co.uk)
 * @author $Author: chr32 $ (current maintainer)
 * @since Proxool 0.5
 */
public class PropertyConfigurator {
    private static final Log LOG = LogFactory.getLog(PropertyConfigurator.class);

    private static final String PREFIX = "jdbc";

    private static final String DOT = ".";

    private static final String EXAMPLE_FORMAT = PREFIX + "*" + DOT + "*";

    private static final String URL_PROPERTY = "url";

    public static void configure(String filename) throws SQLException {
        Properties p = new Properties();
        try {
            p.load(new FileInputStream(filename));
        } catch (IOException e) {
            LOG.warn("Problem whilst loading " + filename, e);
            throw new SQLException("Couldn't load property file " + filename);
        }
        configure(p);
    }

    public static void configure(Properties properties) throws SQLException {

        Map urlMap = new HashMap();
        Map propertiesMap = new HashMap();

        Iterator i = properties.keySet().iterator();
        while (i.hasNext()) {
            String key = (String) i.next();
            String value = properties.getProperty(key);

            if (key.startsWith(PREFIX)) {
                int a = key.indexOf(DOT);
                if (a == -1) {
                    throw new RuntimeException("Property " + key + " must be of the format " + EXAMPLE_FORMAT);
                }
                String tag = key.substring(0, a);
                String name = key.substring(a + 1);

                if (name.equals(URL_PROPERTY)) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Configuring " + value + " pool");
                    }

                    urlMap.put(tag, value);
                } else {
                    Properties p = (Properties) propertiesMap.get(tag);
                    if (p == null) {
                        p = new Properties();
                        propertiesMap.put(tag, p);
                    }
                    p.setProperty(name, value);
                }
            }
        }

        Iterator tags = urlMap.keySet().iterator();
        while (tags.hasNext()) {
            String tag = (String) tags.next();
            String url = (String) urlMap.get(tag);
            Properties p = (Properties) propertiesMap.get(tag);
            ProxoolFacade.registerConnectionPool(url, p);
        }
    }

}

/*
 Revision history:
 $Log: PropertyConfigurator.java,v $
 Revision 1.1  2002/12/15 18:48:33  chr32
 Movied in from 'ext' source tree.

 Revision 1.4  2002/11/09 15:57:57  billhorsman
 fix doc

 Revision 1.3  2002/11/02 14:22:16  billhorsman
 Documentation

 Revision 1.2  2002/10/27 13:05:01  billhorsman
 checkstyle

 Revision 1.1  2002/10/27 12:00:16  billhorsman
 moved classes from ext sub-package which is now obsolete - let's keep everything together in one place

 Revision 1.1  2002/10/25 10:40:27  billhorsman
 draft

*/
