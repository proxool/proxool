package org.logicalcobwebs.proxool;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.HibernateException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Tests the Proxool pool configured as a datasource in Spring and tested against Hibernate 3.1.x
 *
 * @author Mark Eagle
 * @author Phil Barnes
 * @since Mar 16, 2006 @ 8:19:48 AM
 */
public class Hibernate3SpringTest extends AbstractSpringIntegrationTestBase {

    private static final Log LOG = LogFactory.getLog(Hibernate3SpringTest.class);

    public void testSimpleConnection() throws ProxoolException {
        String alias = "memtest";

        SessionFactory sf = (SessionFactory) applicationContext.getBean("sessionFactory");
        Session session = null;
        try {
            session = sf.openSession();
        } catch (HibernateException e) {
            fail("Could not open a Hibernate connection from the pool " + e.getMessage());
            throw e;
        } finally {
            try {
                session.close();
            } catch (HibernateException e) {
                fail("Could not return a Hibernate connection to the pool " + e.getMessage());
                throw e;
            }
        }
    }
}
