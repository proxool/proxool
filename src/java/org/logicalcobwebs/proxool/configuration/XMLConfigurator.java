/*
 * This software is released under the Apache Software Licence. See
 * package.html for details. The latest version is available at
 * http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool.configuration;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;
import org.logicalcobwebs.proxool.ProxoolConstants;
import org.logicalcobwebs.proxool.ProxoolFacade;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.SQLException;
import java.util.Properties;

/**
 * <p>A SAX ContentHandler that can configure Proxool from an XML source.</p>
 *
 * <p>This is just a ContentHandler, so you must associate it with a SAX parser for it to actually do anything.
 * If you have JAXP available {@link JAXPConfigurator} will do this for you.</p>
 *
 * <p>Properties that you pass on to the delegate driver have to be treated specially. They
 * must be contained within a &lt;driver-properties&gt; element.</p>
 *
 * <p>See [TODO] for documentation on the available configuration properties.</p>
 *
 * Example configuration:
 * <pre>
 * &lt;proxool&gt;
 *     &lt;pool-name&gt;apple&lt;/pool-name&gt;
 *     &lt;driver-url&gt;jdbc:hsqldb:.&lt;/driver-url&gt;
 *     &lt;driver-class&gt;org.hsqldb.jdbcDriver&lt;/driver-class&gt;
 *     &lt;driver-properties&gt;
 *         &lt;user&gt;abc&lt;/user&gt;
 *         &lt;password&gt;abc&lt;/password&gt;
 *     &lt;/driver-properties&gt;
 *     &lt;house-keeping-sleep-time&gt;40000&lt;/house-keeping-sleep-time&gt;
 *     &lt;house-keeping-test-sql&gt;select CURRENT_DATE&lt;/house-keeping-test-sql&gt;
 *     &lt;maximum-connection-count&gt;10&lt;/maximum-connection-count&gt;
 *     &lt;minimum-connection-count&gt;3&lt;/minimum-connection-count&gt;
 *     &lt;maximum-connection-lifetime&gt;18000000&lt;/maximum-connection-lifetime&gt; &lt;!-- 5 hours --&gt;
 *     &lt;maximum-new-connections&gt;5&lt;/maximum-new-connections&gt;
 *     &lt;recently-started-threshold&gt;40000&lt;/recently-started-threshold&gt;
 *     &lt;overload-without-refusal-lifetime&gt;50000&lt;/overload-without-refusal-lifetime&gt;
 *     &lt;maximum-active-time&gt;60000&lt;/maximum-active-time&gt;
 *     &lt;verbose&gt;true&lt;/verbose&gt;
 *     &lt;trace&gt;true&lt;/trace&gt;
 *     &lt;fatal-sql-exception&gt;ORA-1234&lt;/fatal-sql-exception&gt;
 *     &lt;prototype-count&gt;2&lt;/prototype-count&gt;
 * &lt;/proxool&gt;
 * </pre>
 *
 * When the parser reaches the end of the &lt;proxool&gt; element the pool
 * is automatically registered. You can contain the &lt;proxool&gt; element
 * in any other elements as you wish. And the &lt;proxool&gt; element can
 * occur as many times as you wish. This allows you to use an XML file that
 * configures your whole application as the source. This configurator will
 * ignore everything apart from the elements contained within the &lt;proxool&gt;
 * element.
 *
 *<p>This class is not thread safe.</p>
 *
 * @version $Revision: 1.2 $, $Date: 2002/12/15 19:43:11 $
 * @author billhorsman
 * @author $Author: chr32 $ (current maintainer)
 */
public class XMLConfigurator extends DefaultHandler {
    private static final Log LOG = LogFactory.getLog(XMLConfigurator.class);

    private StringBuffer content = new StringBuffer();

    private String poolName;

    private String driverClass;

    private String driverUrl;

    private Properties properties = new Properties();

    private static final String PROXOOL = "proxool";

    private static final String POOL_NAME = "pool-name";

    private static final String DRIVER_CLASS = "driver-class";

    private static final String DRIVER_URL = "driver-url";

    private static final String DRIVER_PROPERTIES = "driver-properties";

    private boolean insideDelegateProperties;

    private boolean insideProxool;

    /**
     * @see org.xml.sax.ContentHandler#startElement
     */
    public void startElement(String uri, String lname, String qname, Attributes attributes) throws SAXException {
        content.setLength(0);

        if (lname.equals(PROXOOL)) {
            if (insideProxool) {
                throw new SAXException("A <" + PROXOOL + "> element can't contain another <" + PROXOOL + "> element.");
            }
            insideProxool = true;
            properties.clear();
            driverClass = null;
            driverUrl = null;
        }

        if (insideProxool) {
            if (lname.equals(DRIVER_PROPERTIES)) {
                insideDelegateProperties = true;
            }
        }
    }

    /**
     * @see org.xml.sax.ContentHandler#characters
     */
    public void characters(char[] chars, int start, int length) throws SAXException {
        if (insideProxool) {
            content.append(chars, start, length);
        }
    }

    /**
     * @see org.xml.sax.ContentHandler#endElement
     */
    public void endElement(String uri, String lname, String qname) throws SAXException {
        if (uri != null && uri.length() > 0 && !uri.equals(ProxoolConstants.PROXOOL_XML_NAMESPACE_URI)) {
            // not our namespace
            return;
        }

        // Are we ending a proxool configuration section?
        if (lname.equals(PROXOOL)) {

            // Check that we have defined the minimum information
            if (driverClass == null || driverUrl == null) {
                throw new SAXException("You must define the " + DRIVER_CLASS + " and the " + DRIVER_URL + ".");
            }

            // Build the URL; optinally defining a name
            StringBuffer url = new StringBuffer();
            url.append("proxool");
            if (poolName != null) {
                url.append(ProxoolConstants.ALIAS_DELIMITER);
                url.append(poolName);
            }
            url.append(ProxoolConstants.URL_DELIMITER);
            url.append(driverClass);
            url.append(ProxoolConstants.URL_DELIMITER);
            url.append(driverUrl);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Created url: " + url);
            }

            // Register the pool
            try {
                ProxoolFacade.registerConnectionPool(url.toString(), properties);
            } catch (SQLException e) {
                throw new SAXException(e);
            }

            // This ensures we ignore remaining XML until we come across another
            // <proxool> element.
            insideProxool = false;
        }

        if (insideProxool && !lname.equals(PROXOOL)) {
            if (lname.equals(DRIVER_PROPERTIES)) {
                insideDelegateProperties = false;
            } else {
                setProperty(lname, content.toString().trim());
            }
        }
    }

    private void setProperty(String localName, String value) {
        if (localName.equals(POOL_NAME)) {
            poolName = value;
        } else if (localName.equals(DRIVER_CLASS)) {
            driverClass = value;
        } else if (localName.equals(DRIVER_URL)) {
            driverUrl = value;
        } else if (!localName.equals(DRIVER_PROPERTIES)) {
            final String name = insideDelegateProperties ? localName : (ProxoolConstants.PROPERTY_PREFIX + localName);
            if (LOG.isDebugEnabled()) {
                        LOG.debug("Setting property '" + name + "' to value '" + value + "'.");
                    }
            properties.put(name, value);
        }
    }

    /**
     * @see org.xml.sax.ErrorHandler#warning(SAXParseException)
     */
    public void warning(SAXParseException e) throws SAXException {
        // Just debug-log the warning. We'll probably survive.
        LOG.debug("The saxparser reported a warning.", e);
    }

    /**
     * @see org.xml.sax.ErrorHandler#error(SAXParseException)
     */
    public void error(SAXParseException e) throws SAXException {
        // Just log the error. We'll probably survive.
        LOG.warn("The saxparser reported an error.", e);
    }

    /**
     * @see org.xml.sax.ErrorHandler#fatalError(SAXParseException)
     */
    public void fatalError(SAXParseException e) throws SAXException {
        // On fatal error we rethrow the exception.
        // This should make the parser stop.
        LOG.warn("The saxparser reported a fatal error, interrupting.", e);
        throw e;
    }
}
