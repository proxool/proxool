/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.logicalcobwebs.proxool.util.AbstractListenerContainer;

/**
 * A {@link ConfigurationListenerIF} that keeps a list of <code>ConfigurationListenerIF</code>s
 * and notifies them in a thread safe manner.
 * It also implements {@link org.logicalcobwebs.proxool.util.ListenerContainerIF ListenerContainerIF}
 * which provides methods for
 * {@link org.logicalcobwebs.proxool.util.ListenerContainerIF#addListener(Object) adding} and
 * {@link org.logicalcobwebs.proxool.util.ListenerContainerIF#removeListener(Object) removing} listeners.
 * 
 * @version $Revision: 1.7 $, $Date: 2006/01/18 14:40:01 $
 * @author Christian Nedregaard (christian_nedregaard@email.com)
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.7
 */
public class CompositeConfigurationListener extends AbstractListenerContainer implements ConfigurationListenerIF {
    static final Log LOG = LogFactory.getLog(CompositeConfigurationListener.class);

    /**
     * @see ConfigurationListenerIF#definitionUpdated(ConnectionPoolDefinitionIF, Properties, Properties)
     */
    public void definitionUpdated( ConnectionPoolDefinitionIF connectionPoolDefinition,
                                   Properties completeInfo, 
                                   Properties changedInfo) 
    {
        Object[] listeners = getListeners();
            
        for(int i=0; i<listeners.length; i++) {
            try {
                ConfigurationListenerIF configurationListener = (ConfigurationListenerIF) listeners[i];
                configurationListener.definitionUpdated(connectionPoolDefinition, (Properties) completeInfo.clone(), (Properties) changedInfo.clone());
            } 
            catch (RuntimeException re) {
                LOG.warn("RuntimeException received from listener "+listeners[i]+" when dispatching event", re);
            }
        }
    }
}

/*
 Revision history:
 $Log: CompositeConfigurationListener.java,v $
 Revision 1.7  2006/01/18 14:40:01  billhorsman
 Unbundled Jakarta's Commons Logging.

 Revision 1.6  2004/03/16 08:48:32  brenuart
 Changes in the AbstractListenerContainer:
 - provide more efficient concurrent handling;
 - better handling of RuntimeException thrown by external listeners.

 Revision 1.5  2003/03/10 15:26:43  billhorsman
 refactoringn of concurrency stuff (and some import
 optimisation)

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