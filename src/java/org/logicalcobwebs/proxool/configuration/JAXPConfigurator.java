/*
 * This software is released under the Apache Software Licence. See
 * package.html for details. The latest version is available at
 * http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool.configuration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.logicalcobwebs.proxool.ProxoolException;
import org.xml.sax.*;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.Reader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Configurator that uses JAXP to get a parser for Proxool configuration xml. The parser is validating
 * and namespace aware.<br>
 * See {@link XMLConfigurator} for the Proxool xml configuration format.
 * @version $Revision: 1.1 $, $Date: 2002/12/15 18:49:55 $
 * @author Christian Nedregaard (christian_nedregaard@email.com)
 * @author $Author: chr32 $ (current maintainer)
 * @since Proxool 0.6
 */
public class JAXPConfigurator {
    private static final Log LOG = LogFactory.getLog(JAXPConfigurator.class);
    private static final boolean NAMESPACE_AWARE = true;

    /**
     * Configure Proxool with xml from the given file.
     * @param xmlFileName the file to read xml from.
     * @param validate <code>true</code> if the parsel shall be validating, and <code>false</code> otherwise.
     * @throws ProxoolException if the configuration fails.
     */
    public static void configure(String xmlFileName, boolean validate) throws ProxoolException{
        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Configuring from xml file: " + xmlFileName);
            }
            configure(new InputSource(new FileReader(xmlFileName)), validate);
        } catch (FileNotFoundException e) {
            throw new ProxoolException(e);
        }
    }

    /**
     * Configure Proxool with xml from the given InputSource.
     * @param inputSource the InputSource to read xml from.
     * @param validate <code>true</code> if the parsel shall be validating, and <code>false</code> otherwise.
     * @throws ProxoolException if the configuration fails.
     */
    public static void configure(InputSource inputSource, boolean validate) throws ProxoolException{
        try {
            SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
            if (LOG.isDebugEnabled()) { LOG.debug("SAXParserFactory class: " + saxParserFactory.getClass().getName());}
            saxParserFactory.setValidating(validate);
            final SAXParser saxParser = saxParserFactory.newSAXParser();
            if (LOG.isDebugEnabled()) { LOG.debug("sax parser class" + saxParser.getClass().getName());}
            final XMLReader xmlReader = saxParser.getXMLReader();
            if (LOG.isDebugEnabled()) { LOG.debug("XML reader class: " + xmlReader.getClass().getName());}
            final XMLConfigurator xmlConfigurator = new XMLConfigurator();
            xmlReader.setErrorHandler(xmlConfigurator);
            setSAXFeauture(xmlReader, "http://xml.org/sax/features/namespaces", NAMESPACE_AWARE);
            setSAXFeauture(xmlReader, "http://xml.org/sax/features/namespace-prefixes", !NAMESPACE_AWARE);
            saxParser.parse(inputSource, xmlConfigurator);
        } catch (ParserConfigurationException pce) {
            throw new ProxoolException("Parser configuration failed", pce);
        } catch (SAXException se) {
            throw new ProxoolException("Parsing failed.", se);
        } catch (IOException ioe) {
            throw new ProxoolException("Parsing failed.", ioe);
        }
    }

    /**
     * Configure Proxool with xml from the given reader.
     * @param reader the reader to read xml from.
     * @param validate <code>true</code> if the parsel shall be validating, and <code>false</code> otherwise.
     * @throws ProxoolException if the configuration fails.
     */
    public static void configure(Reader reader, boolean validate) throws ProxoolException{
        if (LOG.isDebugEnabled()) {
            LOG.debug("Configuring from reader: " + reader);
        }
        configure(new InputSource(reader), validate);
    }

    // only log warning on problems with recognition and support of features
    // we'll probably pull through anyway...
    private static void setSAXFeauture(XMLReader xmlReader, String feature, boolean state) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Setting sax feature: '" + feature + "'. State: " + state + ".");
        }
        try {
            xmlReader.setFeature(feature, state);
        } catch (SAXNotRecognizedException e) {
            LOG.warn("Feature: '" + feature + "' not recognised by xml reader " + xmlReader + ".", e);
        } catch (SAXNotSupportedException e) {
            LOG.warn("Feature: '" + feature + "' not supported by xml reader " + xmlReader + ".", e);
        }
    }
}
/*
 Revision history:
 $Log: JAXPConfigurator.java,v $
 Revision 1.1  2002/12/15 18:49:55  chr32
 Init rev.

*/