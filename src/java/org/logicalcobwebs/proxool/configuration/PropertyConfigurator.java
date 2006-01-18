/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool.configuration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.logicalcobwebs.proxool.ProxoolConstants;
import org.logicalcobwebs.proxool.ProxoolException;
import org.logicalcobwebs.proxool.ProxoolFacade;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * Uses a standard Java properties file to configure Proxool. For example:
 *
 * <pre>
 * jdbc-0.proxool.alias=property-test
 * jdbc-0.proxool.driver-url=jdbc:hsqldb:.
 * jdbc-0.proxool.driver-class=org.hsqldb.jdbcDriver
 * jdbc-0.user=foo
 * jdbc-0.password=bar
 * jdbc-0.proxool.house-keeping-sleep-time=40000
 * jdbc-0.proxool.house-keeping-test-sql=select CURRENT_DATE
 * jdbc-0.proxool.maximum-connection-count=10
 * jdbc-0.proxool.minimum-connection-count=3
 * jdbc-0.proxool.maximum-connection-lifetime=18000000
 * jdbc-0.proxool.simultaneous-build-throttle=5
 * jdbc-0.proxool.recently-started-threshold=40000
 * jdbc-0.proxool.overload-without-refusal-lifetime=50000
 * jdbc-0.proxool.maximum-active-time=60000
 * jdbc-0.proxool.verbose=true
 * jdbc-0.proxool.trace=true
 * jdbc-0.proxool.fatal-sql-exception=Fatal error
 * jdbc-0.proxool.prototype-count=2
 *
 * jdbc-1.proxool.alias=property-test-2
 * jdbc-1.proxool.driver-url=jdbc:hsqldb:.
 * jdbc-1.proxool.driver-class=org.hsqldb.jdbcDriver
 * jdbc-1.user=scott
 * jdbc-1.password=tiger
 * jdbc-1.proxool.house-keeping-sleep-time=40000
 * jdbc-1.proxool.house-keeping-test-sql=select CURRENT_DATE
 * jdbc-1.proxool.maximum-connection-count=10
 * jdbc-1.proxool.minimum-connection-count=3
 * jdbc-1.proxool.maximum-connection-lifetime=18000000
 * jdbc-1.proxool.simultaneous-build-throttle=5
 * jdbc-1.proxool.recently-started-threshold=40000
 * jdbc-1.proxool.overload-without-refusal-lifetime=50000
 * jdbc-1.proxool.maximum-active-time=60000
 * jdbc-1.proxool.verbose=true
 * jdbc-1.proxool.trace=true
 * jdbc-1.proxool.fatal-sql-exception=Fatal error
 * jdbc-1.proxool.prototype-count=2
 * </pre>
 *
 * <p>The first word (up to the first dot) must start with "jdbc", but it can
 * be anything you like. Use unique names to identify each pool. Any property
 * not starting with "jdbc" will be ignored.</p>
 * <p>
 * The properties prefixed with "proxool."  will be used by Proxool while
 * the properties that are not prefixed will be passed on to the
 * delegate JDBC driver.
 * </p>
 *
 * @version $Revision: 1.11 $, $Date: 2006/01/18 14:39:58 $
 * @author Bill Horsman (bill@logicalcobwebs.co.uk)
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.5
 */
public class PropertyConfigurator {
    private static final Log LOG = LogFactory.getLog(PropertyConfigurator.class);

    protected static final String PREFIX = "jdbc";

    private static final String DOT = ".";

    private static final String EXAMPLE_FORMAT = PREFIX + "*" + DOT + "*";

    /**
     * Configure proxool with the given properties file.
     * @param filename the filename of the properties file.
     * @throws ProxoolException if the configuration fails.
     */
    public static void configure(String filename) throws ProxoolException {
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(filename));
        } catch (IOException e) {
            throw new ProxoolException("Couldn't load property file " + filename);
        }
        configure(properties);
    }

    /**
     * Configure proxool with the given properties.
     * @param properties the properties instance to use.
     * @throws ProxoolException if the configuration fails.
     */
    public static void configure(Properties properties) throws ProxoolException {
        final Map propertiesMap = new HashMap();
        final Iterator allPropertyKeysIterator = properties.keySet().iterator();
        Properties proxoolProperties = null;

        while (allPropertyKeysIterator.hasNext()) {
            String key = (String) allPropertyKeysIterator.next();
            String value = properties.getProperty(key);

            if (key.startsWith(PREFIX)) {
                int a = key.indexOf(DOT);
                if (a == -1) {
                    throw new ProxoolException("Property " + key + " must be of the format " + EXAMPLE_FORMAT);
                }
                final String tag = key.substring(0, a);
                final String name = key.substring(a + 1);
                proxoolProperties = (Properties) propertiesMap.get(tag);
                if (proxoolProperties == null) {
                    proxoolProperties = new Properties();
                    propertiesMap.put(tag, proxoolProperties);
                }
                proxoolProperties.put(name, value);
            }
        }

        final Iterator tags = propertiesMap.keySet().iterator();
        while (tags.hasNext()) {
            proxoolProperties = (Properties) propertiesMap.get(tags.next());
            // make sure that required propeties are defined
            // and build the url
            // Check that we have defined the minimum information
            final String driverClass = proxoolProperties.getProperty(ProxoolConstants.DRIVER_CLASS_PROPERTY);
            final String driverUrl = proxoolProperties.getProperty(ProxoolConstants.DRIVER_URL_PROPERTY);
            if (driverClass == null || driverUrl == null) {
                throw new ProxoolException("You must define the " + ProxoolConstants.DRIVER_CLASS_PROPERTY + " and the "
                    + ProxoolConstants.DRIVER_URL_PROPERTY + ".");
            }
            final String alias = proxoolProperties.getProperty(ProxoolConstants.ALIAS_PROPERTY);

            // Build the URL; optionally defining a name
            StringBuffer url = new StringBuffer();
            url.append("proxool");
            if (alias != null) {
                url.append(ProxoolConstants.ALIAS_DELIMITER);
                url.append(alias);
                proxoolProperties.remove(ProxoolConstants.ALIAS_PROPERTY);
            }
            url.append(ProxoolConstants.URL_DELIMITER);
            url.append(driverClass);
            proxoolProperties.remove(ProxoolConstants.DRIVER_CLASS_PROPERTY);
            url.append(ProxoolConstants.URL_DELIMITER);
            url.append(driverUrl);
            proxoolProperties.remove(ProxoolConstants.DRIVER_URL_PROPERTY);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Created url: " + url);
            }

            ProxoolFacade.registerConnectionPool(url.toString(), proxoolProperties);
        }
    }

}

/*
 Revision history:
 $Log: PropertyConfigurator.java,v $
 Revision 1.11  2006/01/18 14:39:58  billhorsman
 Unbundled Jakarta's Commons Logging.

 Revision 1.10  2003/03/05 23:28:56  billhorsman
 deprecated maximum-new-connections property in favour of
 more descriptive simultaneous-build-throttle

 Revision 1.9  2003/03/03 11:12:00  billhorsman
 fixed licence

 Revision 1.8  2003/02/06 17:41:05  billhorsman
 now uses imported logging

 Revision 1.7  2003/02/05 14:46:31  billhorsman
 fixed copyright and made PREFIX protected for
 use by ServletConfigurator

 Revision 1.6  2003/01/27 18:26:43  billhorsman
 refactoring of ProxyConnection and ProxyStatement to
 make it easier to write JDK 1.2 patch

 Revision 1.5  2003/01/23 10:41:05  billhorsman
 changed use of pool-name to alias for consistency

 Revision 1.4  2003/01/22 17:35:01  billhorsman
 checkstyle

 Revision 1.3  2003/01/18 15:13:12  billhorsman
 Signature changes (new ProxoolException
 thrown) on the ProxoolFacade API.

 Revision 1.2  2002/12/26 11:32:59  billhorsman
 Rewrote to support new format.

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
