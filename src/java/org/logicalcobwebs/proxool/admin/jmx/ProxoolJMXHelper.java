/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool.admin.jmx;

import org.logicalcobwebs.proxool.ConnectionPoolDefinitionIF;
import org.logicalcobwebs.proxool.ProxoolFacade;
import org.logicalcobwebs.proxool.ProxoolConstants;
import org.logicalcobwebs.proxool.ProxoolException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;
import java.util.Properties;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.List;

/**
 * Utilities for Proxool JMX instrumentation.
 * @version $Revision: 1.7 $, $Date: 2006/01/18 14:39:56 $
 * @author Christian Nedregaard (christian_nedregaard@email.com)
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.8
 */
public class ProxoolJMXHelper {
    private static final Log LOG = LogFactory.getLog(ProxoolJMXHelper.class);

    private ProxoolJMXHelper() {
    }

    /**
     * Create and register a {@link org.logicalcobwebs.proxool.admin.jmx.ConnectionPoolMBean} to the given agents.
     * Will log errors instead of throwing exceptions if one or more of the registrations fails.
     * @param poolPropeties the complete pool properties.
     * @throws org.logicalcobwebs.proxool.ProxoolException if the pool can not be found.
     */
    public static void registerPool(String alias, Properties poolPropeties) throws ProxoolException {
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
                MBeanServer mBeanServer = (MBeanServer) servers.get(0);
                ConnectionPoolMBean poolMBean = new ConnectionPoolMBean(alias, poolPropeties);

                try {
                    mBeanServer.registerMBean(poolMBean, getObjectName(connectionPoolDefinition.getAlias()));
                    LOG.info("Registered JMX MBean for pool " + connectionPoolDefinition.getAlias() + " in agent " + agentIds[i]);
                } catch (Exception e) {
                    LOG.error("Registration of JMX MBean for pool " + connectionPoolDefinition.getAlias()
                            + "in agent " + agentIds[i] + " failed.", e);
                }
            }
        }
    }

    /**
     * Unregister a {@link org.logicalcobwebs.proxool.admin.jmx.ConnectionPoolMBean} from the given agents.
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
                MBeanServer mBeanServer = (MBeanServer) servers.get(0);
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

    /**
     * Get the prefered JMX object name for a Proxool pool.
     * @param alias the alias of the pool.
     * @return the generated object name.
     * @throws javax.management.MalformedObjectNameException if the creation of the object name fails.
     */
    public static ObjectName getObjectName(String alias) throws MalformedObjectNameException {
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

    /**
     * Generate a valid JMX identifier attribute name from a Proxool property name.
     * This basically involves changing all occurences of <code>-&lt;char&gt;</code> to
     * <code>&lt;uppercase char&gt;</code>.<br>
     * <code>driver-properties</code> will for instance become
     * <code>driverProperties</code>.
     * @param propertyName the name to be converted.
     * @return the converted attribute name.
     */
    public static String getValidIdentifier(String propertyName) {
        if (propertyName.indexOf("-") == -1) {
            return propertyName;
        } else {
            StringBuffer buffer = new StringBuffer (propertyName);
            int index = -1;
            while ((index = buffer.toString().indexOf("-")) > -1) {
                buffer.deleteCharAt(index);
                buffer.setCharAt(index, Character.toUpperCase(buffer.charAt(index)));
            }
            return buffer.toString();
        }
    }
}

/*
 Revision history:
 $Log: ProxoolJMXHelper.java,v $
 Revision 1.7  2006/01/18 14:39:56  billhorsman
 Unbundled Jakarta's Commons Logging.

 Revision 1.6  2003/05/06 23:15:56  chr32
 Moving JMX classes back in from sandbox.

 Revision 1.1  2003/03/07 16:35:18  billhorsman
 moved jmx stuff into sandbox until it is tested

 Revision 1.4  2003/03/05 15:14:15  billhorsman
 fix for jdk 1.2

 Revision 1.3  2003/03/03 11:12:00  billhorsman
 fixed licence

 Revision 1.2  2003/02/26 19:05:03  chr32
 Added utility methods.
 Fixed mutiple servers bug.

 Revision 1.1  2003/02/24 18:01:29  chr32
 Rewrite and renamed: ProxoolJMXHelper.

 Revision 1.1  2003/02/24 01:14:17  chr32
 Init rev (unfinished).

*/