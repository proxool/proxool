/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import java.util.Properties;

/**
 * Listens to any changes made to a {@link ConnectionPoolDefinitionIF definition}.
 * This gives you the opportunity to persist a definition.
 *
 * <pre>
 * String alias = "myPool";
 * ConfigurationListenerIF myConfigurationListener = new MyConfigurationListener();
 * ProxoolFacade.{@link ProxoolFacade#addConfigurationListener addConfigurationListener}(alias, myConfigurationListener);
 * </pre>
 *
 * @version $Revision: 1.5 $, $Date: 2003/03/03 11:11:56 $
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
    void definitionUpdated(ConnectionPoolDefinitionIF connectionPoolDefinition, Properties completeInfo, Properties changedInfo);

}

/*
 Revision history:
 $Log: ConfigurationListenerIF.java,v $
 Revision 1.5  2003/03/03 11:11:56  billhorsman
 fixed licence

 Revision 1.4  2003/02/26 16:05:52  billhorsman
 widespread changes caused by refactoring the way we
 update and redefine pool definitions.

 Revision 1.3  2003/02/08 00:35:30  billhorsman
 doc

 Revision 1.2  2003/01/23 11:41:56  billhorsman
 doc

 Revision 1.1  2003/01/18 15:12:23  billhorsman
 renamed ConfiguratorIF to ConfigurationListenerIF to better reflect role

 Revision 1.3  2002/12/15 19:21:42  chr32
 Changed @linkplain to @link (to preserve JavaDoc for 1.2/1.3 users).

 Revision 1.2  2002/12/12 10:49:43  billhorsman
 now includes properties in definitionChanged event

 Revision 1.1  2002/12/04 13:19:43  billhorsman
 draft ConfigurationListenerIF stuff for persistent configuration

*/