/*
 * This software is released under the Apache Software Licence. See
 * package.html for details. The latest version is available at
 * http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import org.logicalcobwebs.proxool.ConnectionInfoIF;

import java.util.Date;

/**
 * Implementation of ConnectionInfoIF. Unlike ConnectionPool it is
 * frozen and will not change. Used with a {@link org.logicalcobwebs.proxool.monitor.SnapshotIF snapshot}
 *
 * @version $Revision: 1.1 $, $Date: 2003/01/31 11:47:14 $
 * @author bill
 * @author $Author: billhorsman $ (current maintainer)
 * @since Proxool 0.7
 */
class ConnectionInfo implements ConnectionInfoIF {

    private Date birthDate;

    private long age;

    private long id;

    private int mark;

    private int status;

    private long timeLastStartActive;

    private long timeLastStopActive;

    private String requester;

    public Date getBirthDate() {
        return birthDate;
    }

    public long getBirthTime() {
        return birthDate.getTime();
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public long getAge() {
        return age;
    }

    public void setAge(long age) {
        this.age = age;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getMark() {
        return mark;
    }

    public void setMark(int mark) {
        this.mark = mark;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getTimeLastStartActive() {
        return timeLastStartActive;
    }

    public void setTimeLastStartActive(long timeLastStartActive) {
        this.timeLastStartActive = timeLastStartActive;
    }

    public long getTimeLastStopActive() {
        return timeLastStopActive;
    }

    public void setTimeLastStopActive(long timeLastStopActive) {
        this.timeLastStopActive = timeLastStopActive;
    }

    public String getRequester() {
        return requester;
    }

    public void setRequester(String requester) {
        this.requester = requester;
    }
}


/*
 Revision history:
 $Log: ConnectionInfo.java,v $
 Revision 1.1  2003/01/31 11:47:14  billhorsman
 new snapshot of connection info

 */