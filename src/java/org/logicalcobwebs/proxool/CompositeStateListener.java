/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import org.logicalcobwebs.logging.Log;
import org.logicalcobwebs.logging.LogFactory;
import org.logicalcobwebs.proxool.util.AbstractListenerContainer;

import java.util.Iterator;

/**
 * A {@link StateListenerIF} that keeps a list of <code>StateListenerIF</code>s
 * and notifies them in a thread safe manner.
 * It also implements {@link org.logicalcobwebs.proxool.util.ListenerContainerIF ListenerContainerIF}
 * which provides methods for
 * {@link org.logicalcobwebs.proxool.util.ListenerContainerIF#addListener(Object) adding} and
 * {@link org.logicalcobwebs.proxool.util.ListenerContainerIF#removeListener(Object) removing} listeners.
 * @version $Revision: 1.4 $, $Date: 2003/03/10 15:26:44 $
 * @author Christian Nedregaard (christian_nedregaard@email.com)
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.7
 */
public class CompositeStateListener extends AbstractListenerContainer implements StateListenerIF {
    static final Log LOG = LogFactory.getLog(CompositeStateListener.class);

    /**
     * @see StateListenerIF#upStateChanged(int)
     */
    public void upStateChanged(int upState) {
        Iterator listenerIterator = null;
        try {
            listenerIterator = getListenerIterator();
            if (listenerIterator != null) {
                StateListenerIF stateListener = null;
                while (listenerIterator.hasNext()) {
                    stateListener = (StateListenerIF) listenerIterator.next();
                    stateListener.upStateChanged(upState);
                }
            }
        } catch (InterruptedException e) {
            LOG.error("Tried to aquire read lock for " + StateListenerIF.class.getName()
                    + " iterator but was interrupted.");
        } finally {
            releaseReadLock();
        }
    }
}

/*
 Revision history:
 $Log: CompositeStateListener.java,v $
 Revision 1.4  2003/03/10 15:26:44  billhorsman
 refactoringn of concurrency stuff (and some import
 optimisation)

 Revision 1.3  2003/03/03 11:11:56  billhorsman
 fixed licence

 Revision 1.2  2003/02/07 17:20:15  billhorsman
 checkstyle

 Revision 1.1  2003/02/07 01:47:17  chr32
 Initial revition.

*/