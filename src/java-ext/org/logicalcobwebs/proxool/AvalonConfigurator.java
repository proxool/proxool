/*
 * This software is released under the Apache Software Licence. See
 * package.html for details. The latest version is available at
 * http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.thread.ThreadSafe;

import java.util.Properties;

/**
 * Presents proxool as an Avalon component
 *
 * <b>If you don't have avalon-framework.jar in your classpath you should simply delete
 * this file. This class does not appear in the binary distribution.</b>

 * <h1>Avalon configuration properties</h1>
 *
 * <table border="1">
 *
 * <tr>
 *   <th>Name</th>
 *   <th>Mandatory</th>
 *   <th>Legal values</th>
 *   <th>Description</th>
 * </tr>
 *
 * <tr>
 *   <td>name</td>
 *   <td>yes</td>
 *   <td>A unique String</td>
 *   <td>The name associated with this connection pool.</td>
 * </tr>
 *
 * <tr>
 *   <td>driver-class</td>
 *   <td>yes</td>
 *   <td>Fully qualified class name of a JDBC driver.</td>
 *   <td>&nbsp;</td>
 * </tr>
 *
 * <tr>
 *   <td>url</td>
 *   <td>yes</td>
 *   <td>A legal JDBC URL.</td>
 *   <td>&nbsp;</td>
 * </tr>
 *
 * <tr>
 *   <td>user</td>
 *   <td>yes</td>
 *   <td>A valid user</td>
 *   <td>The user who logs into the database</td>
 * </tr>
 *
 * <tr>
 *   <td>password</td>
 *   <td>yes</td>
 *   <td>A valid password</td>
 *   <td>The password to use to login to the database</td>
 * </tr>
 *
 * <tr>
 *   <td>house-keeping-sleep-time</td>
 *   <td>no</td>
 *   <td>Integer >= 0 (milliseconds), sensible range 10000 (10 seconds) - 86400000 (1 day)</td>
 *   <td>The length of time the house keeping thread sleeps for. Default is 30000 (30 seconds).</td>
 * </tr>
 *
 * <tr>
 *   <td>house-keeping-test-sql</td>
 *   <td>no</td>
 *   <td>And valid SQL statement</td>
 *   <td>During house keeping, any idle connection will be asked to execute this statement. If any exception is thrown then the Connection is destroyed. It should be a really simple, fast statement.</td>
 * </tr>
 *
 * <tr>
 *   <td>maximum-connection-count</td>
 *   <td>no</td>
 *   <td>Integer >= 1, sensible value is more than minimum-connection-count</td>
 *   <td>The maximum number of connections allowed. Default is 15.</td>
 * </tr>
 *
 * <tr>
 *   <td>maximum-connection-lifetime</td>
 *   <td>no</td>
 *   <td>Integer >= 1 (milliseconds), sensible range 60000 (1 minute) - 86400000 (1 day)</td>
 *   <td>The maximum amount of time that a connection exists for before it is killed. Default is 14400000 (4 hours).</td>
 * </tr>
 *
 * <tr>
 *   <td>maximum-new-connections</td>
 *   <td>no</td>
 *   <td>Integer >= 0</td>
 *   <td>In order to prevent overloading, this is the maximum number of connections that you can have that are in the progress of being made. If it is zero then there is no limit. That is, ones we have started to make but haven't finished yet. Default is zero.</td>
 * </tr>
 *
 * <tr>
 *   <td>minimum-connection-count</td>
 *   <td>no</td>
 *   <td>Integer >= 0, sensible value is less than maximum-connection-count</td>
 *   <td>The minimum number of connections we will attempt to keep open, regardless of whether anyone needs them or not. Default is 5.</td>
 * </tr>
 *
 * <tr>
 *   <td>prototype-count</td>
 *   <td>no</td>
 *   <td>Integer >= 0</td>
 *   <td>This is the number of spare connections we will strive to have. So, if we have a prototypeCount of 5 but only 3 spare connections the prototyper will make an additional 2. This is important because it can take around a seconds to establish a connection, and if we are being very strict about killing connections when they get too old it happens a fair bit. Default is 5.</td>
 * </tr>
 *
 * <tr>
 *   <td>recently-started-threshold</td>
 *   <td>no</td>
 *   <td>Integer >= 1 (milliseconds), sensible value is more than the longest predicted connection time.</td>
 *   <td>We use this to decide whether a connection is locked up or not. It's like a timeout but reacts a little differently. As long as we have at least one connection that was started more recently than this threshold, or we have some available connections, then the pool is considered to be up. Default is 60000 (1 minute).</td>
 * </tr>
 *
 * <tr>
 *   <td>overload-without-refusal-lifetime</td>
 *   <td>no</td>
 *   <td>Integer >= 0 (milliseconds)</td>
 *   <td>This is the time in milliseconds after the last time that we refused a connection that we still consider ourselves to be overloaded. We have to do this because, even when overloaded, it's not impossible for the available connection count to be high and it's possible to be serving a lot of connections. Recognising an overload is easy (we refuse a connection) - it's recognising when we stop being overloaded that is hard. Hence this fudge :) Default is 60000 (1 minute).</td>
 * </tr>
 *
 * <tr>
 *   <td>maximum-active-time</td>
 *   <td>no</td>
 *   <td>Integer >= 1 (milliseconds), sensible value is more than the longest predicted connection time.</td>
 *   <td>An connection that has been active for longer than this time is liable to be destroyed (when the next house keeping thread runs). Default is 300000 (5 minutes).</td>
 * </tr>
 *
 * <tr>
 *   <td>debug-level</td>
 *   <td>no</td>
 *   <td>0 (quiet) or 1 (loud)</td>
 *   <td>How much information to send to debug. Default is 0 (quiet).</td>
 * </tr>
 *
 * <tr>
 *   <td>fatal-sql-exception</td>
 *   <td>no</td>
 *   <td>Any String</td>
 *   <td>If an exception is thrown and its message contains this String then the Connection is destroyed. Use if for exceptions that might indicate a problem with the actual Connection. You can have multiple entries.</td>
 * </tr>
 *
 * <tr>
 *   <td>[any]</td>
 *   <td>no</td>
 *   <td>A JDBC property value that is usable for the driver.</td>
 *   <td>Any property that isn't explicitly defined will be added to a Properties object and given to the DriverManager.</td>
 * </tr>
 *
 * </table>
 *
 * <h1>Configuration example</h1>
 *
 * <h2>Role configuration</h2>
 *
 * <pre>
 * &lt;!-- JDBC manager role --&gt;
 * &lt;role
 * &nbsp;name="no.findexa.common.jdbc.JDBCManagerIF"
 * &nbsp;shorthand="jdbc-manager"
 * &nbsp;default-class="no.findexa.common.jdbc.ConnectionPoolJDBCManager"
 * /&gt;
 * </pre>
 *
 * <h2>Component configuration</h2>
 *
 * <pre>
 * &lt;!-- JDBC manager configuration --&gt;
 * &lt;jdbc-manager&gt;
 * &nbsp;&nbsp;&lt;driver-class&gt;oracle.jdbc.driver.OracleDriver&lt;/driver-class&gt;
 * &nbsp;&nbsp;&lt;url&gt;jdbc:oracle:thin:@tmhpw231:666:hpgs1&lt;/url&gt;
 * &nbsp;&nbsp;&lt;user&gt;my_user&lt;/user&gt;
 * &nbsp;&nbsp;&lt;password&gt;my_pw&lt;/password&gt;
 * &nbsp;&nbsp;&lt;maximum-connection-count&gt;20&lt;/maximum-connection-count&gt;
 * &nbsp;&nbsp;&lt;fatal-sql-exception&gt;out of memory&lt;/fatal-sql-exception&gt;
 * &nbsp;&nbsp;&lt;fatal-sql-exception&gt;socket failure&lt;/fatal-sql-exception&gt;
 * &lt;/jdbc-manager&gt;
 * </pre>
 *
 * @version $Revision: 1.2 $, $Date: 2002/09/18 13:48:56 $
 * @author billhorsman
 * @author $Author: billhorsman $ (current maintainer)
 */
public class AvalonConfigurator implements Configurable, ThreadSafe, Disposable {

    private String connectionPoolName;

    protected void finalize() throws Throwable {
        super.finalize();

        ProxoolFacade.removeConnectionPool(connectionPoolName, 10000);

    }

    public void configure(Configuration configuration) throws ConfigurationException {
        try {
            Properties info = new Properties();
            String name = null;
            String delegateDriver = null;
            String delegateUrl = null;

            try {
                name = configuration.getAttribute(ProxoolConstants.ALIAS_PROPERTY);
            } catch (ConfigurationException e) {
                // It's probably not set. Never mind.
            }

            final Configuration[] conf = configuration.getChildren();
            String propertyName = null;
            for (int i = 0; i < conf.length; ++i) {
                propertyName = conf[i].getName();
                if (propertyName.equals(ProxoolConstants.ALIAS_PROPERTY)) {
                    name = conf[i].getValue();
                } else if (propertyName.equals(ProxoolConstants.DELEGATE_CLASS_PROPERTY)) {
                    delegateDriver = conf[i].getValue();
                } else if (propertyName.equals(ProxoolConstants.URL_PROPERTY)) {
                    delegateUrl = conf[i].getValue();

                    // Set the name to the url by default just in case we
                    // don't get a name (if the component is not selectable).
                    if (name == null) {
                        name = delegateUrl;
                    }
                } else {
                    info.setProperty(propertyName, conf[i].getValue());
                }

                /* TODO log somewhere
                if (log.isDebugEnabled()) {
                    if (propertyName.indexOf("password") == -1) {
                        if (log.isDebugEnabled()) log.debug("Setting " + (recognised ? "pool": "general JDBC") + " property: " + propertyName + "=" + conf[i].getValue());
                    }
                }
                */
            }

            connectionPoolName = name;

            if (delegateUrl == null) {
                throw new ConfigurationException("Mandatory property "
                        + ProxoolConstants.URL_PROPERTY + " is missing.");
            }
            if (delegateDriver == null) {
                throw new ConfigurationException("Mandatory property "
                        + ProxoolConstants.DELEGATE_CLASS_PROPERTY + " is missing.");
            }

            /* TODO log somewhere
            if (log.isDebugEnabled()) {
                log.debug("Connecting to " + cpd.getUrl());
                log.debug("maximumConnectionLifetime=" + cpd.getMaximumConnectionLifetime());
            }
            */
            // TODO should be passing a log writer here...
            ProxoolFacade.registerConnectionPool("proxool." + name + ProxoolConstants.URL_DELIMITER + delegateDriver + ProxoolConstants.URL_DELIMITER + delegateUrl, info);

        } catch (ConfigurationException ce) {
            /* TODO log somewhere
            log.warn("Configuration failed.", ce);
            */
            throw ce;
        } catch (Exception e) {
            /* TODO log somewhere
            log.warn("Configuration failed.", e);
            */
            throw new ConfigurationException("Configuration of the JDBC manager failed", e);
        }
    }

    /**
     * This ensures that the connection pool is finalized. It might happen anyway when the
     * JVM calls the finalize method, but this is just in case.
     */
    public void dispose() {

        ProxoolFacade.removeConnectionPool(connectionPoolName);

    }
}

/*
 Revision history:
 $Log: AvalonConfigurator.java,v $
 Revision 1.2  2002/09/18 13:48:56  billhorsman
 checkstyle and doc

 Revision 1.1.1.1  2002/09/13 08:14:16  billhorsman
 new

 Revision 1.5  2002/08/24 19:57:15  billhorsman
 checkstyle changes

 Revision 1.4  2002/07/10 16:14:47  billhorsman
 widespread layout changes and move constants into ProxoolConstants

 Revision 1.3  2002/07/10 10:52:51  billhorsman
 doc fix

 Revision 1.2  2002/07/02 23:08:55  billhorsman
 new config package to allow us to introduce other ways

 Revision 1.1  2002/07/02 23:05:31  billhorsman
 New config package so that we can introduce other ways of configuring pools.

 Revision 1.6  2002/07/02 11:19:08  billhorsman
 layout code and imports

 Revision 1.5  2002/07/02 11:14:26  billhorsman
 added test (andbug fixes) for FileLogger

 Revision 1.4  2002/06/28 11:19:47  billhorsman
 improved doc

 Revision 1.3  2002/06/05 09:01:31  billhorsman
 removed ConnectionFacadeIF interface in preparation for new, cleaner ProxoolFacade class. _And_ did a code layout update. Why, of why did I mix that up with one commit? It makes it unclear where the cosmetic changes and code changes were made. I won't do it again.

 Revision 1.2  2002/06/04 22:23:00  billhorsman
 added class header comments

 Revision 1.1.1.1  2002/06/04 14:24:01  billhorsman
 start


*/
