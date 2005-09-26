/*
 * This software is released under a licence similar to the Apache Software Licence.
 * See org.logicalcobwebs.proxool.package.html for details.
 * The latest version is available at http://proxool.sourceforge.net
 */
package org.logicalcobwebs.proxool;

import java.util.Date;

/**
 * Implementation of ConnectionInfoIF. Unlike ConnectionPool it is
 * frozen and will not change. Used with a {@link org.logicalcobwebs.proxool.admin.SnapshotIF snapshot}
 *
 * @version $Revision: 1.7 $, $Date: 2005/09/26 10:01:31 $
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

    private String lastSqlCall;

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

    /**
     * Compares using {@link #getId()}
     * @param o must be another {@link ConnectionInfoIF} implementation
     * @return the comparison
     * @see Comparable#compareTo(Object)
     */
    public int compareTo(Object o) {
        return new Long(((ConnectionInfoIF) o).getId()).compareTo(new Long(getId()));
    }

    public String getLastSqlCall() {
        return lastSqlCall;
    }

    public void setLastSqlCall(String lastSqlCall) {
        this.lastSqlCall = lastSqlCall;
    }
}


/*
 Revision history:
 $Log: ConnectionInfo.java,v $
 Revision 1.7  2005/09/26 10:01:31  billhorsman
 Added lastSqlCall when trace is on.

 Revision 1.6  2003/10/30 00:05:50  billhorsman
 now implements Comparable (using ID)

 Revision 1.5  2003/03/03 11:11:56  billhorsman
 fixed licence

 Revision 1.4  2003/02/19 23:46:09  billhorsman
 renamed monitor package to admin

 Revision 1.3  2003/02/12 12:28:27  billhorsman
 added url, proxyHashcode and delegateHashcode to
 ConnectionInfoIF

 Revision 1.2  2003/01/31 16:53:15  billhorsman
 checkstyle

 Revision 1.1  2003/01/31 11:47:14  billhorsman
 new snapshot of connection info

 */