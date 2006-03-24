package org.logicalcobwebs.proxool;

import org.springframework.test.AbstractTransactionalSpringContextTests;

/**
 * TODO: Document!
 *
 * @author Mark Eagle
 * @author Phil Barnes
 * @since Mar 16, 2006 @ 7:55:30 AM
 */
public abstract class AbstractSpringIntegrationTestBase extends AbstractTransactionalSpringContextTests {
    protected String[] getConfigLocations() {
        return new String[]{
                "classpath:org/logicalcobwebs/proxool/applicationContext.xml"
        };
    }

    protected void onSetUpBeforeTransaction() throws Exception {
        GlobalTest.globalSetup();
    }

    
}
