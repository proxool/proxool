/*
 * This software is released under the Apache Software Licence. See
 * package.html for details. The latest version is available at
 * http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool.admin.jmx;

import org.logicalcobwebs.proxool.ConnectionPoolDefinitionIF;
import org.logicalcobwebs.proxool.ProxoolFacade;
import org.logicalcobwebs.proxool.ProxoolConstants;
import org.logicalcobwebs.proxool.ProxoolException;
import org.logicalcobwebs.logging.Log;
import org.logicalcobwebs.logging.LogFactory;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;
import java.util.Properties;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.List;

/**
 * Utilities for Proxool JMX instrumentation.
 * @version $Revision: 1.1 $, $Date: 2003/02/24 18:01:29 $
 * @author Christian Nedregaard (christian_nedregaard@email.com)
 * @author $Author: chr32 $ (current maintainer)
 * @since Proxool 0.8
 */
public class ProxoolJMXHelper {
    private static final Log LOG = LogFactory.getLog (ProxoolJMXHelper.class);

    private ProxoolJMXHelper () {
    }

    /**
     * Create and register a {@link ConnectionPoolMBean} to the given agents.
     * Will log errors instead of throwing exceptions if one or more of the registrations fails.
     * @param poolPropeties the complete pool properties.
     * @throws ProxoolException if the pool can not be found.
     */
    public static void registerPool (String alias, Properties poolPropeties) throws ProxoolException {
        ConnectionPoolDefinitionIF connectionPoolDefinition =
            ProxoolFacade.getConnectionPoolDefinition(alias);
        String[] agentIds = getAgentIds(poolPropeties);
        ArrayList servers = null;
        for (int i = 0; i < agentIds.length; i++) {
            servers = MBeanServerFactory.findMBeanServer(agentIds[i]);
            if (servers == null || servers.size() < 1) {
                LOG.error("Could not register pool " + connectionPoolDefinition.getAlias() + " for JMX instrumentation"
                    + " because lookup of MBeanServer using agent id " + agentIds[i] + " failed.");
            } else {
                final Iterator serverIterator = servers.iterator();
                MBeanServer mBeanServer = null;
                ConnectionPoolMBean poolMBean = new ConnectionPoolMBean(alias, poolPropeties);
                while (serverIterator.hasNext()) {
                    mBeanServer = (MBeanServer) serverIterator.next();
                    try {
                        mBeanServer.registerMBean(poolMBean, getObjectName(connectionPoolDefinition.getAlias()));
                        LOG.info("Registered JMX MBean for pool " + connectionPoolDefinition.getAlias() + " in agent " + agentIds[i]);
                    } catch (Exception e) {
                        LOG.error("Registration of JMX MBean for pool " + connectionPoolDefinition.getAlias()
                            + "in agent " +  agentIds[i] + " failed.", e);
                    }
                }
            }
        }
    }

    /**
     * Unregister a {@link ConnectionPoolMBean} from the given agents.
     * Will log errors instead of throwing exceptions if one or more of the unregistrations fails.
     * @param poolPropeties the complete pool properties.
     */
    public static void unregisterPool(String alias, Properties poolPropeties) {
        String[] agentIds = getAgentIds(poolPropeties);
        ArrayList servers = null;
        for (int i = 0; i < agentIds.length; i++) {
            servers = MBeanServerFactory.findMBeanServer(agentIds[i]);
            if (servers == null || servers.size() < 1) {
                LOG.error("Could not unregister MBean for pool " + alias
                    + " because lookup of MBeanServer using agent id " + agentIds[i] + " failed.");
            } else {
                final Iterator serverIterator = servers.iterator();
                MBeanServer mBeanServer = null;
                while (serverIterator.hasNext()) {
                    mBeanServer = (MBeanServer) serverIterator.next();
                    try {
                        mBeanServer.unregisterMBean(getObjectName(alias));
                        LOG.info("Unregistered JMX MBean for pool " + alias + " in agent " + agentIds[i]);
                    } catch (Exception e) {
                        LOG.error("Unregistration of JMX MBean for pool " + alias + "in agent "
                            + agentIds[i] + " failed.", e);
                    }
                }
            }
        }
    }

    private static ObjectName getObjectName (String alias) throws MalformedObjectNameException {
        return new ObjectName("Proxool:type=Pool, name=" + alias);
    }

    private static String[] getAgentIds(Properties poolPropeties) {
        String idString = poolPropeties.getProperty(ProxoolConstants.JMX_AGENT_PROPERTY);
        if (idString == null || idString.trim().equals("")) {
            // null agent id means 'default agent'
            return new String[]{null};
        } else {
            StringTokenizer tokenizer = new StringTokenizer(idString, ",");
            List tokens = new ArrayList(3);
            while (tokenizer.hasMoreElements()) {
                tokens.add(tokenizer.nextToken().trim());
            }
            return (String[]) tokens.toArray(new String[tokens.size()]);
        }
    }
}

/*
 Revision history:
 $Log: ProxoolJMXHelper.java,v $
 Revision 1.1  2003/02/24 18:01:29  chr32
 Rewrite and renamed: ProxoolJMXHelper.

 Revision 1.1  2003/02/24 01:14:17  chr32
 Init rev (unfinished).

*/