package org.logicalcobwebs.proxool.admin.jmx;

import java.util.Properties;
import java.sql.SQLException;
import java.sql.DriverManager;

import org.logicalcobwebs.proxool.AbstractProxoolTest;
import org.logicalcobwebs.proxool.TestHelper;
import org.logicalcobwebs.proxool.TestConstants;
import org.logicalcobwebs.proxool.ProxoolConstants;

/**
 * Parent class for the JMX tests.
 *
 * @version $Revision: 1.1 $, $Date: 2003/10/20 07:40:44 $
 * @author Christian Nedregaard (christian_nedregaard@email.com)
 * @author $Author: chr32 $ (current maintainer)
 * @since Proxool 0.8
 */
public class AbstractJMXTest extends AbstractProxoolTest {
    /**
     * @see junit.framework.TestCase#TestCase(java.lang.String)
     */
    public AbstractJMXTest(String alias) {
        super(alias);
    }

    /**
     * Create a very basic Proxool pool.
     * @param alias the alias of the pool
     * @return the properties used to create the pool.
     * @throws SQLException if the pool creation fails.
     */
    protected Properties createBasicPool(String alias) throws SQLException {
        final String url = TestHelper.buildProxoolUrl(alias,
            TestConstants.HYPERSONIC_DRIVER,
            TestConstants.HYPERSONIC_TEST_URL);
        final Properties info = createBasicProperties(alias);
        DriverManager.getConnection(url, info).close();
        return info;
    }

    /**
     * Create some very basic Proxool configuration.
     * @param alias the alias of the pool to be configured.
     * @return the created properties.
     */ 
    protected Properties createBasicProperties(String alias) {
        final Properties info = new Properties();
        info.setProperty(ProxoolConstants.USER_PROPERTY, TestConstants.HYPERSONIC_USER);
        info.setProperty(ProxoolConstants.PASSWORD_PROPERTY, TestConstants.HYPERSONIC_PASSWORD);
        info.setProperty(ProxoolConstants.JMX_PROPERTY, Boolean.TRUE.toString());
        info.setProperty(ProxoolConstants.FATAL_SQL_EXCEPTION_PROPERTY, alias);
        return info;
    }
}
