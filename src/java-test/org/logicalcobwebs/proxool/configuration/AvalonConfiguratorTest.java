/*
 * This software is released under the Apache Software Licence. See
 * package.html for details. The latest version is available at
 * http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool.configuration;

import junit.framework.TestCase;
import org.apache.avalon.excalibur.component.DefaultRoleManager;
import org.apache.avalon.excalibur.component.ExcaliburComponentManager;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.avalon.framework.context.DefaultContext;
import org.apache.log.Hierarchy;
import org.apache.log.LogTarget;
import org.apache.log.Logger;
import org.apache.log.Priority;
import org.logicalcobwebs.proxool.GlobalTest;
import org.logicalcobwebs.proxool.ProxoolException;
import org.logicalcobwebs.proxool.ProxoolFacade;
import org.logicalcobwebs.proxool.TestHelper;

import java.io.File;

/**
 * Tests that the AvalonConfgiuration works.
 *
 * @version $Revision: 1.6 $, $Date: 2003/03/01 15:27:25 $
 * @author Christian Nedregaard (christian_nedregaard@email.com)
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.6
 */
public class AvalonConfiguratorTest extends TestCase {
    private ExcaliburComponentManager componentManager;

    /**
     * @see TestCase#TestCase
     */
    public AvalonConfiguratorTest(String name) {
        super(name);
    }

    /**
     * Test that the configuration succeds and that all expected properties
     * has been received by Proxool. The configuration file does not use namspaces.
     * @throws org.apache.avalon.framework.component.ComponentException if the configuration fails.
     * @throws java.sql.SQLException if ProxoolFacade operation fails.
     */
    public void testNoNamspaces() throws Exception {
        initializeAvalon("src/java-test/org/logicalcobwebs/proxool/configuration/role.conf",
            "src/java-test/org/logicalcobwebs/proxool/configuration/component.conf");
        this.componentManager.lookup(AvalonConfigurator.ROLE);
        try {
            TestHelper.equalsCompleteAlternativeProperties(ProxoolFacade.getConnectionPoolDefinition("avalon-test"));
        } catch (ProxoolException e) {
            throw e;
        }
        try {
            TestHelper.equalsCompleteAlternativeProperties(ProxoolFacade.getConnectionPoolDefinition("avalon-test-2"));
        } catch (ProxoolException e) {
            throw e;
        }
        this.componentManager.dispose();
    }

    /**
     * Test that the configuration succeds and that all expected properties
     * has been received by Proxool. The configuration file uses namspaces.
     * @throws Exception if the test is interrupted.
     */
    public void testWithNamespaces() throws Exception {
        initializeAvalon("src/java-test/org/logicalcobwebs/proxool/configuration/role.conf",
            "src/java-test/org/logicalcobwebs/proxool/configuration/component-ns.conf");
        this.componentManager.lookup(AvalonConfigurator.ROLE);
        try {
            TestHelper.equalsCompleteAlternativeProperties(ProxoolFacade.getConnectionPoolDefinition("avalon-test-ns"));
        } catch (ProxoolException e) {
            throw e;
        }
        try {
            TestHelper.equalsCompleteAlternativeProperties(ProxoolFacade.getConnectionPoolDefinition("avalon-test-ns-2"));
        } catch (ProxoolException e) {
            throw e;
        }
        ProxoolFacade.removeConnectionPool("avalon-test-ns");
        ProxoolFacade.removeConnectionPool("avalon-test-ns-2");
        this.componentManager.dispose();
    }

    /**
     * Test that a configurator that does not have close-on-dispose="false"
     * closes the pools it has configured when it is disposed.
     * @throws Exception if the test is interrupted.
     */
    public void testDisposeOnClose() throws Exception {
        initializeAvalon("src/java-test/org/logicalcobwebs/proxool/configuration/role.conf",
            "src/java-test/org/logicalcobwebs/proxool/configuration/component.conf");
        this.componentManager.lookup(AvalonConfigurator.ROLE);
        this.componentManager.dispose();
        try {
            ProxoolFacade.getConnectionPoolDefinition("avalon-test");
            fail("ProxoolFacade found pool 'avalon-test' but we expected the configurator to have removed it.");
        } catch (ProxoolException e) {
            // This is what we want.
        }
        try {
            ProxoolFacade.getConnectionPoolDefinition("avalon-test-2");
            fail("ProxoolFacade found pool 'avalon-test-2' but we expected the configurator to have removed it.");
        } catch (ProxoolException e) {
            // This is what we want.
        }
    }

    /**
     * Test that a configurator that does have close-on-dispose="false"
     * does not close the pools it has configured when it is disposed.
     * @throws Exception if the test is interrupted.
     */
    public void testNotDisposeOnClose() throws Exception {
        initializeAvalon("src/java-test/org/logicalcobwebs/proxool/configuration/role.conf",
            "src/java-test/org/logicalcobwebs/proxool/configuration/component-ns.conf");
        this.componentManager.lookup(AvalonConfigurator.ROLE);
        this.componentManager.dispose();
        try {
            ProxoolFacade.getConnectionPoolDefinition("avalon-test-ns");
        } catch (ProxoolException e) {
            fail("ProxoolFacade did not find pool 'avalon-test-ns' but we didn't expect the configurator to have removed it.");
        }
        try {
            ProxoolFacade.getConnectionPoolDefinition("avalon-test-ns-2");
        } catch (ProxoolException e) {
            fail("ProxoolFacade did not find pool 'avalon-ns-test-2' but we didn't expect the configurator to have removed it.");
        }
        ProxoolFacade.removeConnectionPool("avalon-test-ns");
        ProxoolFacade.removeConnectionPool("avalon-test-ns-2");
    }

    private void initializeAvalon(String roleFile, String componentFile) throws Exception {
        // create a Avalon logger
        final LogTarget logTarget = new LogKitTargetAdapter();
        final Logger rootLogger = Hierarchy.getDefaultHierarchy().getLoggerFor("root");
        rootLogger.unsetLogTargets();
        rootLogger.setLogTargets(new LogTarget[] {logTarget});
        rootLogger.setPriority(Priority.WARN);
        final DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder(true);
        // create component configuration
        final Configuration componentConfiguration = builder.buildFromFile(new File(componentFile));
        // create role configuration
        final Configuration roleConfiguration = builder.buildFromFile(new File(roleFile));
        // create and configure role and component managers.
        this.componentManager = new ExcaliburComponentManager();
        this.componentManager.setLogger(rootLogger.getChildLogger(ExcaliburComponentManager.class.getName()));
        DefaultRoleManager roleManager = new DefaultRoleManager();
        roleManager.setLogger(rootLogger.getChildLogger(DefaultRoleManager.class.getName()));
        roleManager.configure(roleConfiguration);
        componentManager.contextualize(new DefaultContext());
        componentManager.setRoleManager(roleManager);
        componentManager.configure(componentConfiguration);
        componentManager.initialize();
    }

    /**
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        GlobalTest.globalSetup();
    }

    /**
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        GlobalTest.globalTeardown();
    }
}

/*
 Revision history:
 $Log: AvalonConfiguratorTest.java,v $
 Revision 1.6  2003/03/01 15:27:25  billhorsman
 checkstyle

 Revision 1.5  2003/02/27 18:01:49  billhorsman
 completely rethought the test structure. it's now
 more obvious. no new tests yet though.

 Revision 1.4  2003/02/19 16:52:00  chr32
 Added tests for close-on-dispose functionality.

 Revision 1.3  2003/02/19 15:14:26  billhorsman
 fixed copyright (copy and paste error,
 not copyright change)

 Revision 1.2  2002/12/23 02:48:07  chr32
 Checkstyle.

 Revision 1.1  2002/12/23 02:40:49  chr32
 Init rev.

 Revision 1.5  2002/12/18 03:13:00  chr32
 Added tests for xml validation.

 Revision 1.4  2002/12/16 17:06:41  billhorsman
 new test structure

 Revision 1.3  2002/12/16 02:35:40  chr32
 Updated to new driver-properties xml format.

 Revision 1.2  2002/12/15 19:41:26  chr32
 Style fixes.

 Revision 1.1  2002/12/15 19:10:49  chr32
 Init rev.

*/
