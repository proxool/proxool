/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.SessionFactory;
import net.sf.hibernate.cfg.Configuration;
import net.sf.hibernate.cfg.Environment;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.util.Properties;

/**
 * Tests that we are able to get a connection from
 * <a href="http://www.hibernate.org">Hibernate</a>.
 * (Code contributed by Mark Eagle)
 * @version $Revision: 1.2 $, $Date: 2006/01/18 14:40:06 $
 * @author Bill Horsman {bill@logicalcobwebs.co.uk)
 * @author $Author: billhorsman $ (current maintainer)
 */
public class HibernateTest extends AbstractProxoolTest {

    private static final Log LOG = LogFactory.getLog(HibernateTest.class);

    public HibernateTest(String alias) {
        super(alias);
    }

    /**
     * Can we get a connection straight from Hibernate? We register the pool first
     * and theb ask for Hibernate for it.
     * @throws ProxoolException if there was a Proxool problem
     * @throws HibernateException if there was a Hibernate problem
     */
    public void testSimpleHibernateConnection() throws HibernateException, ProxoolException {

        String testName = "simpleHibernateConnection";
        String alias = testName;

        String url = TestHelper.buildProxoolUrl(alias,
                TestConstants.HYPERSONIC_DRIVER,
                TestConstants.HYPERSONIC_TEST_URL);
        Properties info = new Properties();
        info.setProperty(ProxoolConstants.USER_PROPERTY, TestConstants.HYPERSONIC_USER);
        info.setProperty(ProxoolConstants.PASSWORD_PROPERTY, TestConstants.HYPERSONIC_PASSWORD);
        info.setProperty(ProxoolConstants.VERBOSE_PROPERTY, "true");
        ProxoolFacade.registerConnectionPool(url, info);

        Configuration configuration = null;
        SessionFactory sessionFactory = null;
        Session session = null;
        Properties hibernateProperties = new Properties();
        Connection connection = null;

        try {
            hibernateProperties.setProperty(Environment.DRIVER, ProxoolDriver.class.getName());
            hibernateProperties.setProperty(Environment.URL, url);

            configuration = new Configuration().addProperties(hibernateProperties);

            // create a session object to the database
            sessionFactory = configuration.buildSessionFactory();
            session = sessionFactory.openSession();
            assertNotNull("Expected a session", session);

            // Inspect the assigned connection to the session from
            // the pool.
            connection = session.connection();

            // assert that the connection is not null
            assertNotNull("Expected a connection", connection);

        } finally {
            try {
                connection.close();
            } catch (Exception e) {
                LOG.error("Problem closing Hibernate session", e);
            }
            // close the session which will also close it's assigned connection
            try {
                session.close();
            } catch (Exception e) {
                LOG.error("Problem closing Hibernate session", e);
            }
        }

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            LOG.debug("Woken up", e);
        }

        // We just need to test that we served at least one connection. I suspect that
        // Hibernate is doing its own house keeping and getting at least an additional
        // one.
        assertTrue("servedCount", ProxoolFacade.getSnapshot(alias).getServedCount() > 0);
        // They should definitely all be returned to the pool once we're finished though
        assertEquals("activeCount", 0, ProxoolFacade.getSnapshot(alias).getActiveConnectionCount());

    }

    /**
     * Can we get a connection from a Proxool pool that we have already registered? We
     * ask Hibernate to lookup the pool by its alias.
     * @throws ProxoolException if there was a Proxool problem
     * @throws HibernateException if there was a Hibernate problem
     */
    public void testDirectHibernateConnection() throws HibernateException, ProxoolException {

        String testName = "directHibernateConnection";
        String alias = testName;

        String url = TestHelper.buildProxoolUrl(alias,
                TestConstants.HYPERSONIC_DRIVER,
                TestConstants.HYPERSONIC_TEST_URL);
        Properties info = new Properties();
        info.setProperty(ProxoolConstants.USER_PROPERTY, TestConstants.HYPERSONIC_USER);
        info.setProperty(ProxoolConstants.PASSWORD_PROPERTY, TestConstants.HYPERSONIC_PASSWORD);
        ProxoolFacade.registerConnectionPool(url, info);

        Configuration configuration = null;
        SessionFactory sessionFactory = null;
        Session session = null;
        Properties hibernateProperties = new Properties();
        Connection connection = null;

        try {
            hibernateProperties.setProperty(Environment.PROXOOL_EXISTING_POOL, "true");
            hibernateProperties.setProperty(Environment.PROXOOL_POOL_ALIAS, alias);

            configuration = new Configuration().addProperties(hibernateProperties);

            // create a session object to the database
            sessionFactory = configuration.buildSessionFactory();
            session = sessionFactory.openSession();
            assertNotNull("Expected a session", session);

            // Inspect the assigned connection to the session from
            // the pool.
            connection = session.connection();

            // assert that the connection is not null
            assertNotNull("Expected a connection", connection);

        } finally {
            try {
                connection.close();
            } catch (Exception e) {
                LOG.error("Problem closing Hibernate session", e);
            }
            // close the session which will also close it's assigned connection
            try {
                session.close();
            } catch (Exception e) {
                LOG.error("Problem closing Hibernate session", e);
            }
        }

        // We just need to test that we served at least one connection. I suspect that
        // Hibernate is doing its own house keeping and getting at least an additional
        // one.
        assertTrue("servedCount", ProxoolFacade.getSnapshot(alias).getServedCount() > 0);
        // They should definitely all be returned to the pool once we're finished though
        assertEquals("activeCount", 0, ProxoolFacade.getSnapshot(alias).getActiveConnectionCount());

    }

    /**
     * Can we get a connection from a pool configured by Hibernate
     * @throws ProxoolException if there was a Proxool problem
     * @throws HibernateException if there was a Hibernate problem
     */
    public void testHibernateConfiguredConnection() throws HibernateException, ProxoolException {

        String testName = "hibernateConfiguredConnection";
        String alias = testName;

        Configuration configuration = null;
        SessionFactory sessionFactory = null;
        Session session = null;
        Properties hibernateProperties = new Properties();
        Connection connection = null;

        try {
            hibernateProperties.setProperty(Environment.PROXOOL_XML, "src/java-test/org/logicalcobwebs/proxool/hibernate.xml");
            hibernateProperties.setProperty(Environment.PROXOOL_POOL_ALIAS, alias);

            configuration = new Configuration().addProperties(hibernateProperties);

            // create a session object to the database
            sessionFactory = configuration.buildSessionFactory();
            session = sessionFactory.openSession();
            assertNotNull("Expected a session", session);

            // Inspect the assigned connection to the session from
            // the pool.
            connection = session.connection();
            assertNotNull("Expected a connection", connection);

        } finally {
            try {
                connection.close();
            } catch (Exception e) {
                LOG.error("Problem closing Hibernate session", e);
            }
            // close the session which will also close it's assigned connection
            try {
                session.close();
            } catch (Exception e) {
                LOG.error("Problem closing Hibernate session", e);
            }
        }

        // We just need to test that we served at least one connection. I suspect that
        // Hibernate is doing its own house keeping and getting at least an additional
        // one.
        assertTrue("servedCount", ProxoolFacade.getSnapshot(alias).getServedCount() > 0);
        // They should definitely all be returned to the pool once we're finished though
        assertEquals("activeCount", 0, ProxoolFacade.getSnapshot(alias).getActiveConnectionCount());

    }

}

/*
Revision history:
$Log: HibernateTest.java,v $
Revision 1.2  2006/01/18 14:40:06  billhorsman
Unbundled Jakarta's Commons Logging.

Revision 1.1  2003/09/28 09:38:30  billhorsman
New unit test for Hibernate.

*/

