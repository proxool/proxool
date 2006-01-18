/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool.configuration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.logicalcobwebs.proxool.ProxoolException;
import org.logicalcobwebs.proxool.ProxoolFacade;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import java.io.File;
import java.util.Enumeration;
import java.util.Properties;

/**
 * <p>Allows you to configure Proxool using a servlet. There are three
 * different ways:
 *
 * The init parameters
 * can either directly configure Proxool (in a similar fashion to the
 * PropertyConfigurator) or they can point to separate XML or
 * property files. For example:</p>
 *
 * <p><b>1. XML file</b> delegates to {@link JAXPConfigurator} passing
 * in the filename. If the filename is not absolute then it is prepended
 * with the application directory.</p>
 *
  *<pre>
 *    &lt;servlet&gt;
 *        &lt;servlet-name&gt;ServletConfigurator&lt;/servlet-name&gt;
 *        &lt;servlet-class&gt;org.logicalcobwebs.proxool.configuration.ServletConfigurator&lt;/servlet-class&gt;
 *        &lt;init-param&gt;
 *            &lt;param-name&gt;xmlFile&lt;/param-name&gt;
 *            &lt;param-value&gt;WEB-INF/proxool.xml&lt;/param-value&gt;
 *        &lt;/init-param&gt;
 *    &lt;/servlet&gt;
 * </pre>
 *
 * <b>2. Property file</b> delegates to {@link PropertyConfigurator}
 * passing in the filename. If the filename is not absolute then it is prepended
 * with the application directory.
 *
 *<pre>
 *    &lt;servlet&gt;
 *        &lt;servlet-name&gt;ServletConfigurator&lt;/servlet-name&gt;
 *        &lt;servlet-class&gt;org.logicalcobwebs.proxool.configuration.ServletConfigurator&lt;/servlet-class&gt;
 *        &lt;init-param&gt;
 *            &lt;param-name&gt;propertyFile&lt;/param-name&gt;
 *            &lt;param-value&gt;WEB-INF/proxool.properties&lt;/param-value&gt;
 *        &lt;/init-param&gt;
 *    &lt;/servlet&gt;
 * </pre>
 *
 * <b>3. Init parameters</b> delegates to {@link PropertyConfigurator}
 * by passing in a new Properties object based on the servlet's init
 * parameters.
 *
 *<pre>
 *    &lt;servlet&gt;
 *        &lt;servlet-name&gt;ServletConfigurator&lt;/servlet-name&gt;
 *        &lt;servlet-class&gt;org.logicalcobwebs.proxool.configuration.ServletConfigurator&lt;/servlet-class&gt;
 *        &lt;init-param&gt;
 *            &lt;param-name&gt;jdbc-0.proxool.alias&lt;/param-name&gt;
 *            &lt;param-value&gt;test&lt;/param-value&gt;
 *        &lt;/init-param&gt;
 *        &lt;init-param&gt;
 *            &lt;param-name&gt;jdbc-0.proxool.driver-url&lt;/param-name&gt;
 *            &lt;param-value&gt;jdbc:hsqldb:.&lt;/param-value&gt;
 *        &lt;/init-param&gt;
 *        &lt;init-param&gt;
 *            &lt;param-name&gt;jdbc-0.proxool.driver-class&lt;/param-name&gt;
 *            &lt;param-value&gt;org.hsqldb.jdbcDriver&lt;/param-value&gt;
 *        &lt;/init-param&gt;
 *    &lt;/servlet&gt;
 * </pre>
 *
 * <p>It will also automatically shutdown Proxool. See
 * {@link #destroy}.</p>
 *
 * @version $Revision: 1.7 $, $Date: 2006/01/18 14:39:58 $
 * @author bill
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.7
 */
public class ServletConfigurator extends HttpServlet  {

    private static final Log LOG = LogFactory.getLog(ServletConfigurator.class);

    private static final String XML_FILE_PROPERTY = "xmlFile";

    private static final String PROPERTY_FILE_PROPERTY = "propertyFile";

    private static final String AUTO_SHUTDOWN_PROPERTY = "autoShutdown";

    private boolean autoShutdown = true;

    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);

        String appDir = servletConfig.getServletContext().getRealPath("/");

        Properties properties = new Properties();

        Enumeration names = servletConfig.getInitParameterNames();
        while (names.hasMoreElements()) {
            String name = (String) names.nextElement();
            String value = servletConfig.getInitParameter(name);

            if (name.equals(XML_FILE_PROPERTY)) {
                try {
                    File file = new File(value);
                    if (file.isAbsolute()) {
                        JAXPConfigurator.configure(value, false);
                    } else {
                        JAXPConfigurator.configure(appDir + File.separator + value, false);
                    }
                } catch (ProxoolException e) {
                    LOG.error("Problem configuring " + value, e);
                }
            } else if (name.equals(PROPERTY_FILE_PROPERTY)) {
                try {
                    File file = new File(value);
                    if (file.isAbsolute()) {
                        PropertyConfigurator.configure(value);
                    } else {
                        PropertyConfigurator.configure(appDir + File.separator + value);
                    }
                } catch (ProxoolException e) {
                    LOG.error("Problem configuring " + value, e);
                }
            } else if (name.equals(AUTO_SHUTDOWN_PROPERTY)) {
                autoShutdown = Boolean.valueOf(value).booleanValue();
            } else if (name.startsWith(PropertyConfigurator.PREFIX)) {
                properties.setProperty(name, value);
            }
        }

        if (properties.size() > 0) {
            try {
                PropertyConfigurator.configure(properties);
            } catch (ProxoolException e) {
                LOG.error("Problem configuring using init properties", e);
            }
        }
    }

    /**
     * Shuts down Proxool by removing all connection pools. If you want
     * to disable this behaviour then use:
     * <pre>
     * &lt;init-param&gt;
     *   &lt;param-name&gt;autoShutdown&lt;/param-name&gt;
     *   &lt;param-value&gt;false&lt;/param-value&gt;
     * &lt;/init-param&gt;
     * </pre>
     */
    public void destroy() {
        if (autoShutdown) {
            ProxoolFacade.shutdown(0);
        }
    }
}


/*
 Revision history:
 $Log: ServletConfigurator.java,v $
 Revision 1.7  2006/01/18 14:39:58  billhorsman
 Unbundled Jakarta's Commons Logging.

 Revision 1.6  2003/03/10 15:26:54  billhorsman
 refactoringn of concurrency stuff (and some import
 optimisation)

 Revision 1.5  2003/03/03 11:12:00  billhorsman
 fixed licence

 Revision 1.4  2003/02/07 17:26:25  billhorsman
 use shutdown() instead of removeAllConnectionPools()

 Revision 1.3  2003/02/06 17:41:05  billhorsman
 now uses imported logging

 Revision 1.2  2003/02/06 15:45:26  billhorsman
 trivial doc changes

 Revision 1.1  2003/02/05 15:03:49  billhorsman
 new configuration servlet.

 */