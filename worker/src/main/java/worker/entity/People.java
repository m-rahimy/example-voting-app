package entity;

import java.util.List;

public class People {

    private String id;
    private String fullName;
    private String userAgent;
    private String remoteAddress;
    private String remotePort;
    private String firstVisit;
    private String lastVisit;
    private List<Voting> voteList;

    public People(String id, String fullName, String userAgent, String remoteAddress,
                  String remotePort, String firstVisit, String lastVisit, List<Voting> voteList) {
        this.id = id;
        this.fullName = fullName;
        this.userAgent = userAgent;
        this.remoteAddress = remoteAddress;
        this.remotePort = remotePort;
        this.firstVisit = firstVisit;
        this.lastVisit = lastVisit;
        this.voteList = voteList;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public void setRemoteAddress(String remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public String getRemotePort() {
        return remotePort;
    }

    public void setRemotePort(String remotePort) {
        this.remotePort = remotePort;
    }

    public String getFirstVisit() {
        return firstVisit;
    }

    public void setFirstVisit(String firstVisit) {
        this.firstVisit = firstVisit;
    }

    public String getLastVisit() {
        return lastVisit;
    }

    public void setLastVisit(String lastVisit) {
        this.lastVisit = lastVisit;
    }

    public List<Voting> getVoteList() {
        return voteList;
    }

    public void setVoteList(List<Voting> voteList) {
        this.voteList = voteList;
    }

    @Override
    public String toString() {
        return "People{" +
                "id='" + id + '\'' +
                ", fullName='" + fullName + '\'' +
                ", userAgent='" + userAgent + '\'' +
                ", remoteAddress='" + remoteAddress + '\'' +
                ", remotePort='" + remotePort + '\'' +
                ", firstVisit='" + firstVisit + '\'' +
                ", lastVisit='" + lastVisit + '\'' +
                ", voteList=" + voteList +
                '}';
    }
}
