/*
 * This software is released under the Apache Software Licence. See
 * package.html for details. The latest version is available at
 * http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import java.util.Properties;

/**
 * Configures a {@link ConnectionPoolDefinitionIF definition}.
 * and listens to any changes made to it. This gives you the
 * opportunity to persist any changes made to it.
 *
 * One of the tasks for a configurator is to
 * {@link ProxoolFacade#registerConnectionPool(java.lang.String, java.util.Properties, org.logicalcobwebs.proxool.ConfigurationListenerIF) register}
 * the pool. When it does this it should pass in a reference to this configurator. This ensures two things:
 *
 * 1) Any changes to the definition will call this object's {@link #defintionUpdated} method
 *
 * 2) Any on-the-fly updates to the definition (i.e. when passed in as a Properties object to
 * the driver) will result in a warning log message,
 *
 * @version $Revision: 1.1 $, $Date: 2003/01/18 15:12:23 $
 * @author billhorsman
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.6
 */
public interface ConfigurationListenerIF {

    /**
     * Gets called once after a pool has been updated.
     * @param connectionPoolDefinition the new definition
     * @param completeInfo the properties that could be used to create this definition
     * @param changedInfo only the properties that have changed since the pool was
     * registered, or this method was las called.
     */
    void defintionUpdated(ConnectionPoolDefinitionIF connectionPoolDefinition, Properties completeInfo, Properties changedInfo);

}

/*
 Revision history:
 $Log: ConfigurationListenerIF.java,v $
 Revision 1.1  2003/01/18 15:12:23  billhorsman
 renamed ConfiguratorIF to ConfigurationListenerIF to better reflect role

 Revision 1.3  2002/12/15 19:21:42  chr32
 Changed @linkplain to @link (to preserve JavaDoc for 1.2/1.3 users).

 Revision 1.2  2002/12/12 10:49:43  billhorsman
 now includes properties in definitionChanged event

 Revision 1.1  2002/12/04 13:19:43  billhorsman
 draft ConfigurationListenerIF stuff for persistent configuration

*/