/**
 * Clever Little Trader
 *
 * Jubilee Group and Logical Cobwebs, 2002
 */
package org.logicalcobwebs.proxool.admin;

import org.logicalcobwebs.proxool.ProxoolFacade;
import org.logicalcobwebs.proxool.ResultMonitor;

/**
 * TODO
 * @version $Revision: 1.1 $, $Date: 2003/03/01 15:14:14 $
 * @author bill
 * @author $Author: billhorsman $ (current maintainer)
 * @since TODO
 */
abstract public class SnapshotResultMonitor extends ResultMonitor {

    private SnapshotIF snapshot;

    private String alias;

    public SnapshotResultMonitor(String alias) {
        this.alias = alias;
    }

    public boolean check() throws Exception {
        snapshot = ProxoolFacade.getSnapshot(alias);
        return check(snapshot);
    }

    abstract public boolean check(SnapshotIF snapshot) throws Exception;

    public SnapshotIF getSnapshot() {
        return snapshot;
    }

}


/*
 Revision history:
 $Log: SnapshotResultMonitor.java,v $
 Revision 1.1  2003/03/01 15:14:14  billhorsman
 new ResultMonitor to help cope with test threads

 */