/*
 * This software is released under the Apache Software Licence. See
 * package.html for details. The latest version is available at
 * http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

/**
 * Configures a {@linkplain ConnectionPoolDefinitionIF definition}.
 * and listens to any changes made to it. This gives you the
 * opportunity to persist any changes made to it.
 *
 * One of the tasks for a configurator is to
 * {@linkplain ProxoolFacade#registerConnectionPool(java.lang.String, java.util.Properties, org.logicalcobwebs.proxool.ConfiguratorIF) register}
 * the pool. When it does this it should pass in a reference to this configurator. This ensures two things:
 *
 * 1) Any changes to the definition will call this object's {@linkplain #defintionUpdated} method
 *
 * 2) Any on-the-fly updates to the definition (i.e. when passed in as a Properties object to
 * the driver) will result in a warning log message,
 *
 * @version $Revision: 1.1 $, $Date: 2002/12/04 13:19:43 $
 * @author billhorsman
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.6
 */
public interface ConfiguratorIF {

    /**
     * Gets called once after a pool has been updated.
     * @param connectionPoolDefintion the new definition
     */
    void defintionUpdated(ConnectionPoolDefinitionIF connectionPoolDefintion);

}

/*
 Revision history:
 $Log: ConfiguratorIF.java,v $
 Revision 1.1  2002/12/04 13:19:43  billhorsman
 draft ConfiguratorIF stuff for persistent configuration

*/