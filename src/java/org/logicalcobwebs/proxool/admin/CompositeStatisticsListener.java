/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool.admin;

import org.logicalcobwebs.proxool.util.AbstractListenerContainer;
import org.logicalcobwebs.logging.Log;
import org.logicalcobwebs.logging.LogFactory;

import java.util.Iterator;

/**
 * A {@link StatisticsListenerIF} that keeps a list of <code>StatisticsListenerIF</code>s
 * and notifies them in a thread safe manner.
 * It also implements {@link org.logicalcobwebs.proxool.util.ListenerContainerIF ListenerContainerIF}
 * which provides methods for
 * {@link org.logicalcobwebs.proxool.util.ListenerContainerIF#addListener(Object) adding} and
 * {@link org.logicalcobwebs.proxool.util.ListenerContainerIF#removeListener(Object) removing} listeners.
 * @version $Revision: 1.2 $, $Date: 2003/03/03 11:11:59 $
 * @author Christian Nedregaard (christian_nedregaard@email.com)
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.7
 */
public class CompositeStatisticsListener extends AbstractListenerContainer implements StatisticsListenerIF {
    static final Log LOG = LogFactory.getLog(CompositeStatisticsListener.class);

    /**
     * @see StatisticsListenerIF#statistics(String, StatisticsIF)
     */
    public void statistics(String alias, StatisticsIF statistics) {
        Iterator listenerIterator = null;
        try {
            listenerIterator = getListenerIterator();
            if (listenerIterator != null) {
                StatisticsListenerIF statisticsListener = null;
                while (listenerIterator.hasNext()) {
                    statisticsListener = (StatisticsListenerIF) listenerIterator.next();
                    statisticsListener.statistics(alias, statistics);
                }
            }
        } catch (InterruptedException e) {
            LOG.error("Tried to aquire read lock for " + StatisticsListenerIF.class.getName()
                    + " iterator but was interrupted.");
        } finally {
            releaseReadLock();
        }
    }
}

/*
 Revision history:
 $Log: CompositeStatisticsListener.java,v $
 Revision 1.2  2003/03/03 11:11:59  billhorsman
 fixed licence

 Revision 1.1  2003/02/19 23:36:51  billhorsman
 renamed monitor package to admin

 Revision 1.2  2003/02/07 17:20:17  billhorsman
 checkstyle

 Revision 1.1  2003/02/07 01:49:04  chr32
 Initial revition.

*/