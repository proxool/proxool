/*
 * This software is released under the Apache Software Licence. See
 * package.html for details. The latest version is available at
 * http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import java.util.Date;

/**
 * Implementation of ConnectionInfoIF. Unlike ConnectionPool it is
 * frozen and will not change. Used with a {@link org.logicalcobwebs.proxool.monitor.SnapshotIF snapshot}
 *
 * @version $Revision: 1.3 $, $Date: 2003/02/12 12:28:27 $
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

    private String delegateDriver;

    private String delegateUrl;

    private String proxyHashcode;

    private String delegateHashcode;


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

    public String getDelegateDriver() {
        return delegateDriver;
    }

    public void setDelegateDriver(String delegateDriver) {
        this.delegateDriver = delegateDriver;
    }

    public String getDelegateUrl() {
        return delegateUrl;
    }

    public void setDelegateUrl(String delegateUrl) {
        this.delegateUrl = delegateUrl;
    }

    public String getProxyHashcode() {
        return proxyHashcode;
    }

    public void setProxyHashcode(String proxyHashcode) {
        this.proxyHashcode = proxyHashcode;
    }

    public String getDelegateHashcode() {
        return delegateHashcode;
    }

    public void setDelegateHashcode(String delegateHashcode) {
        this.delegateHashcode = delegateHashcode;
    }
}


/*
 Revision history:
 $Log: ConnectionInfo.java,v $
 Revision 1.3  2003/02/12 12:28:27  billhorsman
 added url, proxyHashcode and delegateHashcode to
 ConnectionInfoIF

 Revision 1.2  2003/01/31 16:53:15  billhorsman
 checkstyle

 Revision 1.1  2003/01/31 11:47:14  billhorsman
 new snapshot of connection info

 */