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
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Properties;

/**
 * <p>A SAX 2 ContentHandler that can configure Proxool from an XML source.</p>
 *
 * <p>This is just a <a
 * href="http://www.saxproject.org/apidoc/org/xml/sax/ContentHandler.html" target="_new"
 * >ContentHandler</a>, so you must associate it with a SAX parser for it to actually do anything.
 * If you have JAXP available {@link JAXPConfigurator} will do this for you.</p>
 *
 * <p>Properties that you pass on to the delegate driver have to be treated specially. They
 * must be contained within a &lt;driver-properties&gt; element.</p>
 *
 * <p>See the <a href="The latest version is available at http://proxool.sourceforge.net/properties.html" target="_new">Proxool properties</a> for documentation
 * on the available configuration properties.</p>
 *
 * Example configuration:
 * <pre>
 * &lt;proxool&gt;
 *     &lt;alias&gt;apple&lt;/alias&gt;
 *     &lt;driver-url&gt;jdbc:hsqldb:.&lt;/driver-url&gt;
 *     &lt;driver-class&gt;org.hsqldb.jdbcDriver&lt;/driver-class&gt;
 *     &lt;driver-properties&gt;
 *         &lt;property name="user" value="abc" /&gt;
 *         &lt;property name="password" value="def" /&gt;
 *     &lt;/driver-properties&gt;
 *     &lt;house-keeping-sleep-time&gt;40000&lt;/house-keeping-sleep-time&gt;
 *     &lt;house-keeping-test-sql&gt;select CURRENT_DATE&lt;/house-keeping-test-sql&gt;
 *     &lt;maximum-connection-count&gt;10&lt;/maximum-connection-count&gt;
 *     &lt;minimum-connection-count&gt;3&lt;/minimum-connection-count&gt;
 *     &lt;maximum-connection-lifetime&gt;18000000&lt;/maximum-connection-lifetime&gt; &lt;!-- 5 hours --&gt;
 *     &lt;simultaneous-build-throttle&gt;5&lt;/simultaneous-build-throttle&gt;
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
 *<p><a name="#validation">
 * <b>Validation</b><br>
 * A couple of additional steps are required if you want your SAX parser to validate your Proxool xml confguration:
 * <ul>
 * <li>
 * Put your proxool configuration elements inside a root <code>proxool-config</code> element.
 * The document must adhere to the <a href="proxool.dtd">Proxool dtd</a>.
 * </li>
 * <li>
 * Add a <code>DOCTYPE</code> entry to your xml with a system id containing the <i>absolute url</i> to the Proxool
 * dtd. The Proxool jar contains a copy of the Proxool dtd in the confguration package. You can reference that with
 * a jar url like this:<br>
 * <code><nobr>&lt;!DOCTYPE proxool-config SYSTEM "jar:file:///C:/Proxool/lib/proxool.jar!/org/logicalcobwebs/proxool/configuration/proxool.dtd"&gt;</nobr></code></li>
 * <li>
 * Configure your parser to be validating. In the {@link JAXPConfigurator} this is done by passing <code>true</code> as
 * the second arghument to any of the <code>configure</code> methods.
 * </li>
 * </ul>
 * </p>
 *<p>This class is not thread safe.</p>
 *
 * @version $Revision: 1.18 $, $Date: 2006/01/18 14:39:58 $
 * @author billhorsman
 * @author $Author: billhorsman $ (current maintainer)
 */
public class XMLConfigurator extends DefaultHandler {
    private static final Log LOG = LogFactory.getLog(XMLConfigurator.class);

    private StringBuffer content = new StringBuffer();

    private String poolName;

    private String driverClass;

    private String driverUrl;

    private Properties properties = new Properties();

    private static final String PROXOOL = "proxool";

    private static final String DRIVER_PROPERTIES = "driver-properties";

    private static final String PROPERTY = "property";

    private static final String NAME = "name";

    private static final String VALUE = "value";

    private boolean insideDelegateProperties;

    private boolean insideProxool;

    /**
     * @see org.xml.sax.ContentHandler#startElement
     */
    public void startElement(String uri, String lname, String qname, Attributes attributes) throws SAXException {
        content.setLength(0);

        if (!namespaceOk(uri)) {
            return;
        }

        final String elementName = getElementName(uri, lname, qname);

        if (elementName.equals(PROXOOL)) {
            if (insideProxool) {
                throw new SAXException("A <" + PROXOOL + "> element can't contain another <" + PROXOOL + "> element.");
            }
            insideProxool = true;
            properties.clear();
            driverClass = null;
            driverUrl = null;
        }

        if (insideProxool) {
            if (elementName.equals(DRIVER_PROPERTIES)) {
                insideDelegateProperties = true;
            } else if (insideDelegateProperties) {
                if (elementName.equals(PROPERTY)) {
                    setDriverProperty(attributes);
                }
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
        if (!namespaceOk(uri)) {
            return;
        }

        final String elementName = getElementName(uri, lname, qname);

        // Are we ending a proxool configuration section?
        if (elementName.equals(PROXOOL)) {

            // Check that we have defined the minimum information
            if (driverClass == null || driverUrl == null) {
                throw new SAXException("You must define the " + ProxoolConstants.DRIVER_CLASS + " and the " + ProxoolConstants.DRIVER_URL + ".");
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
            } catch (ProxoolException e) {
                throw new SAXException(e);
            }

            // This ensures we ignore remaining XML until we come across another
            // <proxool> element.
            insideProxool = false;
        }

        if (insideProxool && !elementName.equals(PROXOOL)) {
            if (elementName.equals(DRIVER_PROPERTIES)) {
                insideDelegateProperties = false;
            } else if (!insideDelegateProperties) {
                setProxoolProperty(elementName, content.toString().trim());
            }
        }
    }

    private void setProxoolProperty(String localName, String value) {
        if (localName.equals(ProxoolConstants.ALIAS)) {
            poolName = value;
        } else if (localName.equals(ProxoolConstants.DRIVER_CLASS)) {
            driverClass = value;
        } else if (localName.equals(ProxoolConstants.DRIVER_URL)) {
            driverUrl = value;
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Setting property '" + ProxoolConstants.PROPERTY_PREFIX + localName + "' to value '" + value + "'.");
            }
            properties.put(ProxoolConstants.PROPERTY_PREFIX + localName, value);
        }
    }

    private void setDriverProperty(Attributes attributes) throws SAXException {
        final String name = attributes.getValue(NAME);
        final String value = attributes.getValue(VALUE);
        if (name == null || name.length() < 1 || value == null) {
            throw new SAXException("Name or value attribute missing from property element."
                + "Name: '" + name + "' Value: '" + value + "'.");
        }
        if (LOG.isDebugEnabled()) {
            if (name.toLowerCase().indexOf("password") > -1) {
                LOG.debug("Adding driver property: " + name + "=" + "*******");
            } else {
                LOG.debug("Adding driver property: " + name + "=" + value);
            }
        }
        properties.put(name, value);
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
        // On error we rethrow the exception.
        // This should cause the parser stop and an exeption be thrown back to the client.
        throw e;
    }

    /**
     * @see org.xml.sax.ErrorHandler#fatalError(SAXParseException)
     */
    public void fatalError(SAXParseException e) throws SAXException {
        // On fatal error we rethrow the exception.
        // This should cause the parser stop and an exeption be thrown back to the client.
        throw e;
    }

    // If no namespace use qname, else use lname.
    private String getElementName(String uri, String lname, String qname) {
        if (uri == null || "".equals(uri)) {
            return qname;
        } else {
            return lname;
        }
    }

    private boolean namespaceOk(String uri) {
        return uri == null || uri.length() == 0 || uri.equals(ProxoolConstants.PROXOOL_XML_NAMESPACE_URI);
    }
}
