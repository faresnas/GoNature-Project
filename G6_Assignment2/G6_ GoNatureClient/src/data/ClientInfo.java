package data;

import java.io.Serializable;

@SuppressWarnings("serial")
public class ClientInfo implements Serializable {

    private String host;
    private String ip;
    private String status;

    public ClientInfo(String host, String ip, String status) {
        this.host = host;
        this.ip = ip;
        this.status = status;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}