/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Properties;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.log4j.xml.DOMConfigurator;
import org.logicalcobwebs.logging.Log;
import org.logicalcobwebs.logging.LogFactory;

/**
 * Provides a suite of all tests. And some utility methods for setting
 * up the logging.
 * 
 * The test configuration can be specified using the env property "testConfig"
 *
 * @version $Revision: 1.19 $, $Date: 2004/05/26 17:19:09 $
 * @author bill
 * @author $Author: brenuart $ (current maintainer)
 * @since Proxool 0.5
 */
public class GlobalTest {

	private static final String defaultConfig = "/org/logicalcobwebs/proxool/testconfig-hsqldb.properties";
	
    private static final Log LOG = LogFactory.getLog(GlobalTest.class);

    private static boolean initialised;

    public static synchronized void globalSetup() throws Exception 
	{
    	// return immediately if we are already initialised
        if (initialised) {
        	return;
        }
        
        // configure log4j
        String log4jPath = System.getProperty("log4jPath");
        if (log4jPath != null && log4jPath.length() > 0) {
            try {
                DOMConfigurator.configureAndWatch(log4jPath);
            } catch (Exception e) {
                LOG.debug("Can't configure logging using " + log4jPath);
            }
        } else {
            // Well, at least switch on debugging
            System.setProperty("org.apache.commons.logging.simplelog.defaultlog", "debug");
            org.apache.log4j.BasicConfigurator.resetConfiguration();
            org.apache.log4j.BasicConfigurator.configure();
        }

        // Load ProxoolDriver class into DriverManager
        Class.forName(ProxoolDriver.class.getName());

        // initialise test configuration
        initTestConstants(defaultConfig);

        // remember we are correctly initialized
        initialised = true;
    }

    
    /**
     * 
     * @throws Exception
     */
    private static void initTestConstants() throws Exception
	{
    	String resourceName = System.getProperty("testConfig");
    	if( resourceName==null || resourceName.length()==0 )
    	{
    		LOG.info("Test configuration set to default value");
    	}
    	else
    	{
    		initTestConstants(resourceName);
    	}
	}
    
    /**
     * 
     * @param resourceName
     */
    private static void initTestConstants(String resourceName) throws Exception
    {
    	// locate and read resource file
    	InputStream resourceStream = null;
    	Properties props = new Properties();
    	try {
    		LOG.info("Loading test configuration from "+resourceName);
    		resourceStream = resourceName.getClass().getResourceAsStream(resourceName);
    		props.load(resourceStream);
    	}
    	catch(Exception e)
		{
    		LOG.error("Problem while loading test configuration", e);
    		throw e;
		}
    	finally {
    		if( resourceStream != null ) {
    			resourceStream.close();
    		}
    	}
    	
    	// parse resource file and initialize TestConstants
    	Field[] fields = TestConstants.class.getDeclaredFields();
    	for(int i=0; i<fields.length; i++)
    	{
    		Field field = fields[i];
    		
    		// locate value in property file
    		String propertyName = field.getName();
    		String value = props.getProperty(propertyName);
    		
    		if( value==null )
    		{
    			LOG.info("Set "+propertyName+" to default value");
    		}
    		else
    		{
    			LOG.info("Set " + propertyName+ " to '" + value + "'");
    			field.set(null, value);
    		}
    	}
    }
    
    
    public static synchronized void globalTeardown(String alias) {
        ProxoolFacade.shutdown(alias + ":teardown", 10000);
    }

    /**
     * Run all tests
     *
     * @return a composite test of all Proxool tests.
     */
    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(org.logicalcobwebs.proxool.AllTests.suite());
        suite.addTest(org.logicalcobwebs.proxool.configuration.AllTests.suite());
        suite.addTest(org.logicalcobwebs.proxool.admin.AllTests.suite());
        suite.addTest(org.logicalcobwebs.proxool.admin.jmx.AllTests.suite());
        suite.addTest(org.logicalcobwebs.proxool.util.AllTests.suite());
        suite.addTest(org.logicalcobwebs.logging.AllTests.suite());

        // create a wrapper for global initialization code.
        TestSetup wrapper = new TestSetup(suite) {
            public void setUp() throws Exception {
                GlobalTest.globalSetup();
            }

            protected void tearDown() throws Exception {
                GlobalTest.globalTeardown("global");
            }
        };
        return wrapper;
    }

}


/*
 Revision history:
 $Log: GlobalTest.java,v $
 Revision 1.19  2004/05/26 17:19:09  brenuart
 Allow JUnit tests to be executed against another database.
 By default the test configuration will be taken from the 'testconfig-hsqldb.properties' file located in the org.logicalcobwebs.proxool package.
 This behavior can be overriden by setting the 'testConfig' environment property to another location.

 Revision 1.18  2003/10/26 16:23:20  billhorsman
 Fixed up test suites

 Revision 1.17  2003/09/30 18:58:29  billhorsman
 Increase shutdown grace time to 10 seconds to make tests more robust.

 Revision 1.16  2003/09/07 22:10:17  billhorsman
 Default behaviour of test classes, if Log4J is not configured, is now DEBUG output to console.

 Revision 1.15  2003/05/06 23:17:59  chr32
 Added JMX tests to GlobalTest.

 Revision 1.14  2003/03/11 14:51:47  billhorsman
 more concurrency fixes relating to snapshots

 Revision 1.13  2003/03/10 15:31:26  billhorsman
 fixes

 Revision 1.12  2003/03/04 10:10:52  billhorsman
 loads ProxoolDriver

 Revision 1.11  2003/03/03 17:38:47  billhorsman
 leave shutdown to AbstractProxoolTest

 Revision 1.10  2003/03/03 11:12:04  billhorsman
 fixed licence

 Revision 1.9  2003/03/01 15:27:24  billhorsman
 checkstyle

 Revision 1.8  2003/02/19 23:36:50  billhorsman
 renamed monitor package to admin

 Revision 1.7  2003/02/10 00:14:33  chr32
 Added tests for AbstractListenerContainer.

 Revision 1.6  2003/02/07 15:10:11  billhorsman
 add admin tests

 Revision 1.5  2003/02/06 17:41:03  billhorsman
 now uses imported logging

 Revision 1.4  2003/01/31 16:38:05  billhorsman
 doc

 Revision 1.3  2002/12/18 03:15:03  chr32
 Added commented-out code that will make logging level DEBUG.

 Revision 1.2  2002/12/16 17:15:12  billhorsman
 fixes

 Revision 1.1  2002/12/16 17:05:25  billhorsman
 new test structure

 */