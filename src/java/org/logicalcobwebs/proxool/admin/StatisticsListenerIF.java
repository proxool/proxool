/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool.admin;

/**
 * Listen for new {@link StatisticsIF statistics} as they are produced.
 * The frequency with which they occur is defined in the
 * {@link org.logicalcobwebs.proxool.ConnectionPoolDefinitionIF#getStatistics configuration}.
 *
 * <pre>
 * String alias = "myPool";
 * StatisticsListenerIF myStatisticsListener = new MyStatisticsListener();
 * ProxoolFacade.{@link org.logicalcobwebs.proxool.ProxoolFacade#addStatisticsListener addStatisticsListener}(alias, myStatisticsListener);
 * </pre>

 * @version $Revision: 1.2 $, $Date: 2003/03/03 11:11:59 $
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
 Revision 1.2  2003/03/03 11:11:59  billhorsman
 fixed licence

 Revision 1.1  2003/02/19 23:36:51  billhorsman
 renamed monitor package to admin

 Revision 1.2  2003/02/08 00:35:29  billhorsman
 doc

 Revision 1.1  2003/01/31 15:16:12  billhorsman
 new listener

 */