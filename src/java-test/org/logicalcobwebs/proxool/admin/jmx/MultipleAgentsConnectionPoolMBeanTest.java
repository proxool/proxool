/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool.admin.jmx;

import org.logicalcobwebs.proxool.ProxoolConstants;
import org.logicalcobwebs.proxool.ProxoolDriver;
import org.logicalcobwebs.proxool.TestConstants;
import org.logicalcobwebs.proxool.TestHelper;
import org.logicalcobwebs.proxool.ProxoolFacade;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;


/**
 * Test {@link org.logicalcobwebs.proxool.admin.jmx.ConnectionPoolMBean} when JMX is configured for multiple agents.
 *
 * @version $Revision: 1.1 $, $Date: 2003/10/20 07:40:44 $
 * @author Christian Nedregaard (christian_nedregaard@email.com)
 * @author $Author: chr32 $ (current maintainer)
 * @since Proxool 0.8
 */
public class MultipleAgentsConnectionPoolMBeanTest extends AbstractJMXTest {
    private static final String AGENT1_DOMAIN = "testAgent1";
    private static final String AGENT2_DOMAIN = "testAgent2";
    private MBeanServer mBeanServer1;
    private MBeanServer mBeanServer2;
    private String mBeanServer1Id;
    private String mBeanServer2Id;

    /**
     * @see junit.framework.TestCase#TestCase(java.lang.String)
     */
    public MultipleAgentsConnectionPoolMBeanTest(String s) {
        super(s);
    }


    /**
     * Test that pools can be regisered for multiple agents.
     * @throws Exception if the test fails in an unexpected way.
     */
    public void testMultipleAgents() throws Exception {
        final String alias = "testMultipleAgents";
        createMutipleAgentBasicPool(alias);
        final ObjectName objectName = ProxoolJMXHelper.getObjectName(alias);
        final String fatalSQLAttributeName = ProxoolJMXHelper.getValidIdentifier(ProxoolConstants.FATAL_SQL_EXCEPTION);
        String fatalSQLAttribtueValue = (String) mBeanServer1.getAttribute(objectName, fatalSQLAttributeName);
        assertTrue("Agent " + AGENT1_DOMAIN + " could not find " + fatalSQLAttribtueValue
                + " attribute.", fatalSQLAttribtueValue != null && fatalSQLAttribtueValue.trim().length() > 0);
        fatalSQLAttribtueValue = (String) mBeanServer2.getAttribute(objectName, fatalSQLAttributeName);
        assertTrue("Agent " + AGENT2_DOMAIN + " could not find " + fatalSQLAttribtueValue
                + " attribute.", fatalSQLAttribtueValue != null && fatalSQLAttribtueValue.trim().length() > 0);
        ProxoolFacade.removeConnectionPool(alias);
    }

    private Properties createMutipleAgentBasicPool(String alias) throws SQLException {
        final String url = TestHelper.buildProxoolUrl(alias,
                TestConstants.HYPERSONIC_DRIVER,
                TestConstants.HYPERSONIC_TEST_URL);
        final Properties info = createBasicProperties(alias);
        info.setProperty(ProxoolConstants.JMX_AGENT_PROPERTY, mBeanServer1Id + ", " + mBeanServer2Id);
        DriverManager.getConnection(url, info).close();
        return info;
    }

    /**
     * Calls {@link org.logicalcobwebs.proxool.AbstractProxoolTest#setUp}
     * @see junit.framework.TestCase#setUp
     */
    protected void setUp() throws Exception {
        Class.forName(ProxoolDriver.class.getName());
        this.mBeanServer1 = MBeanServerFactory.createMBeanServer(AGENT1_DOMAIN);
        this.mBeanServer1Id = this.mBeanServer1.getAttribute(
            new ObjectName("JMImplementation:type=MBeanServerDelegate"), "MBeanServerId").toString();
        this.mBeanServer2 = MBeanServerFactory.createMBeanServer(AGENT2_DOMAIN);
        this.mBeanServer2Id = this.mBeanServer2.getAttribute(
            new ObjectName("JMImplementation:type=MBeanServerDelegate"), "MBeanServerId").toString();
        super.setUp();
    }

    /**
     * Calls {@link org.logicalcobwebs.proxool.AbstractProxoolTest#tearDown}
     * @see junit.framework.TestCase#setUp
     */
    protected void tearDown() throws Exception {
        MBeanServerFactory.releaseMBeanServer(this.mBeanServer1);
        MBeanServerFactory.releaseMBeanServer(this.mBeanServer2);
        this.mBeanServer1 = null;
        this.mBeanServer2 = null;
        super.tearDown();
    }
}

/*
 Revision history:
 $Log: MultipleAgentsConnectionPoolMBeanTest.java,v $
 Revision 1.1  2003/10/20 07:40:44  chr32
 Improved tests.

 */
