/*
 * This software is released under the Apache Software Licence. See
 * package.html for details. The latest version is available at
 * http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool.monitor;

/**
 * Listen for new {@link StatisticsIF statistics} as they are produced.
 * The frequency with which they occur is defined in the
 * {@link org.logicalcobwebs.proxool.ConnectionPoolDefinitionIF#getStatistics configuration}.
 *
 * @version $Revision: 1.1 $, $Date: 2003/01/31 15:16:12 $
 * @author bill
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.7
 */
public interface StatisticsListenerIF {

    /**
     * A new set of statistics have just been produced
     * @param alias identifies the pool
     * @param statistics new statistics
     */
    void statistics(String alias, StatisticsIF statistics);
}


/*
 Revision history:
 $Log: StatisticsListenerIF.java,v $
 Revision 1.1  2003/01/31 15:16:12  billhorsman
 new listener

 */