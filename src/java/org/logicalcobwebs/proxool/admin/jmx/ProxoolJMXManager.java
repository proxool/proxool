/*
 * This software is released under the Apache Software Licence. See
 * package.html for details. The latest version is available at
 * http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool.admin.jmx;

import org.logicalcobwebs.proxool.ProxoolListenerIF;
import org.logicalcobwebs.proxool.ConnectionPoolDefinitionIF;
import org.logicalcobwebs.proxool.ProxoolFacade;
import org.logicalcobwebs.logging.Log;
import org.logicalcobwebs.logging.LogFactory;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;
import java.util.Properties;
import java.util.ArrayList;

/**
 *
 * @version $Revision: 1.1 $, $Date: 2003/02/24 01:14:17 $
 * @author Christian Nedregaard (christian_nedregaard@email.com)
 * @author $Author: chr32 $ (current maintainer)
 * @since Proxool 0.8
 */
public class ProxoolJMXManager implements ProxoolListenerIF {
    private static final Log LOG = LogFactory.getLog (ProxoolJMXManager.class);
    private static ProxoolJMXManager ourInstance;
    private boolean initialized;
    private String agentId;

    public static synchronized ProxoolJMXManager getInstance () {
        if (ourInstance == null) {
            ourInstance = new ProxoolJMXManager ();
        }
        return ourInstance;
    }

    private ProxoolJMXManager () {
    }

    public synchronized void initialize(String agentId) {
        if (!this.initialized) {
            this.agentId = agentId;
            ProxoolFacade.addProxoolListener(this);
            this.initialized = true;
        }
    }

    public void onRegistration (ConnectionPoolDefinitionIF connectionPoolDefinition, Properties completeInfo) {
        ArrayList servers = MBeanServerFactory.findMBeanServer(agentId);
        if (servers == null || servers.size() < 1) {
            LOG.error("Could not register pool " + connectionPoolDefinition.getAlias() + " for JMX instrumentation"
                + " because lookup of MBeanServer using agent id " + this.agentId + " failed.");
        } else {
            final MBeanServer mBeanServer = (MBeanServer) servers.get(0);
            try {
                mBeanServer.registerMBean(
                    new ConnectionPoolMBean(connectionPoolDefinition, completeInfo),
                    getObjectName(connectionPoolDefinition.getAlias()));
                LOG.info("Registered JMX MBean for pool " + connectionPoolDefinition.getAlias());
            } catch (Exception e) {
                LOG.error("Registration of JMX MBean for pool " + connectionPoolDefinition.getAlias() + " failed.", e);
            }
        }
    }

    private ObjectName getObjectName (String alias) throws MalformedObjectNameException {
        return new ObjectName("Proxool:type=Pool, name=" + alias);
    }

    public void onShutdown (String alias) {
        ArrayList servers = MBeanServerFactory.findMBeanServer(agentId);
        if (servers == null || servers.size() < 1) {
            LOG.error("Could not deregister pool " + alias
                + " because lookup of MBeanServer using agent id " + this.agentId + " failed.");
        } else {
            final MBeanServer mBeanServer = (MBeanServer) servers.get(0);
            try {
                mBeanServer.unregisterMBean(getObjectName(alias));
                LOG.info("JMX MBean for pool " + alias + " was unregistered.");
            } catch (Exception e) {
                LOG.error("Unregistration of JMX MBean for pool " + alias + " failed.", e);
            }
        }
    }
}

/*
 Revision history:
 $Log: ProxoolJMXManager.java,v $
 Revision 1.1  2003/02/24 01:14:17  chr32
 Init rev (unfinished).

*/