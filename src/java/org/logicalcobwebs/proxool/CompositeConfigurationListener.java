/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import org.logicalcobwebs.proxool.util.AbstractListenerContainer;
import org.logicalcobwebs.logging.Log;
import org.logicalcobwebs.logging.LogFactory;

import java.util.Properties;
import java.util.Iterator;

/**
 * A {@link ConfigurationListenerIF} that keeps a list of <code>ConfigurationListenerIF</code>s
 * and notifies them in a thread safe manner.
 * It also implements {@link org.logicalcobwebs.proxool.util.ListenerContainerIF ListenerContainerIF}
 * which provides methods for
 * {@link org.logicalcobwebs.proxool.util.ListenerContainerIF#addListener(Object) adding} and
 * {@link org.logicalcobwebs.proxool.util.ListenerContainerIF#removeListener(Object) removing} listeners.
 * @version $Revision: 1.4 $, $Date: 2003/03/03 11:11:56 $
 * @author Christian Nedregaard (christian_nedregaard@email.com)
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.7
 */
public class CompositeConfigurationListener extends AbstractListenerContainer implements ConfigurationListenerIF {
    static final Log LOG = LogFactory.getLog(CompositeConfigurationListener.class);

    /**
     * @see ConfigurationListenerIF#definitionUpdated(ConnectionPoolDefinitionIF, Properties, Properties)
     */
    public void definitionUpdated(ConnectionPoolDefinitionIF connectionPoolDefinition,
                                 Properties completeInfo, Properties changedInfo) {
        Iterator listenerIterator = null;
        try {
            listenerIterator = getListenerIterator();
            if (listenerIterator != null) {
                ConfigurationListenerIF configurationListener = null;
                while (listenerIterator.hasNext()) {
                    configurationListener = (ConfigurationListenerIF) listenerIterator.next();
                    configurationListener.definitionUpdated(connectionPoolDefinition, (Properties) completeInfo.clone(), (Properties) changedInfo.clone());
                }
            }
        } catch (InterruptedException e) {
            LOG.error("Tried to aquire read lock for " + ConfigurationListenerIF.class.getName()
                    + " iterator but was interrupted.");
        } finally {
            releaseReadLock();
        }
    }
}

/*
 Revision history:
 $Log: CompositeConfigurationListener.java,v $
 Revision 1.4  2003/03/03 11:11:56  billhorsman
 fixed licence

 Revision 1.3  2003/02/26 16:05:52  billhorsman
 widespread changes caused by refactoring the way we
 update and redefine pool definitions.

 Revision 1.2  2003/02/07 17:20:17  billhorsman
 checkstyle

 Revision 1.1  2003/02/07 01:47:17  chr32
 Initial revition.

*/