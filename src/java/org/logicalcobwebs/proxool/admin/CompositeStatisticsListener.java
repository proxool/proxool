/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool.admin;

import org.logicalcobwebs.logging.Log;
import org.logicalcobwebs.logging.LogFactory;
import org.logicalcobwebs.proxool.util.AbstractListenerContainer;

/**
 * A {@link StatisticsListenerIF} that keeps a list of <code>StatisticsListenerIF</code>s
 * and notifies them in a thread safe manner.
 * It also implements {@link org.logicalcobwebs.proxool.util.ListenerContainerIF ListenerContainerIF}
 * which provides methods for
 * {@link org.logicalcobwebs.proxool.util.ListenerContainerIF#addListener(Object) adding} and
 * {@link org.logicalcobwebs.proxool.util.ListenerContainerIF#removeListener(Object) removing} listeners.
 * 
 * @version $Revision: 1.4 $, $Date: 2004/03/16 08:48:32 $
 * @author Christian Nedregaard (christian_nedregaard@email.com)
 * @author $Author: brenuart $ (current maintainer)
 * @since Proxool 0.7
 */
public class CompositeStatisticsListener extends AbstractListenerContainer implements StatisticsListenerIF {
    static final Log LOG = LogFactory.getLog(CompositeStatisticsListener.class);

    /**
     * @see StatisticsListenerIF#statistics(String, StatisticsIF)
     */
    public void statistics(String alias, StatisticsIF statistics) 
    {
        Object[] listeners = getListeners();
        
        for(int i=0; i<listeners.length; i++) {
            try {
                StatisticsListenerIF statisticsListener = (StatisticsListenerIF) listeners[i];
                statisticsListener.statistics(alias, statistics);
            }
            catch (RuntimeException re) {
                LOG.warn("RuntimeException received from listener "+listeners[i]+" when dispatching statistics event", re);
            }
        }
    }
}

/*
 Revision history:
 $Log: CompositeStatisticsListener.java,v $
 Revision 1.4  2004/03/16 08:48:32  brenuart
 Changes in the AbstractListenerContainer:
 - provide more efficient concurrent handling;
 - better handling of RuntimeException thrown by external listeners.

 Revision 1.3  2003/03/10 15:26:51  billhorsman
 refactoringn of concurrency stuff (and some import
 optimisation)

 Revision 1.2  2003/03/03 11:11:59  billhorsman
 fixed licence

 Revision 1.1  2003/02/19 23:36:51  billhorsman
 renamed monitor package to admin

 Revision 1.2  2003/02/07 17:20:17  billhorsman
 checkstyle

 Revision 1.1  2003/02/07 01:49:04  chr32
 Initial revition.

*/