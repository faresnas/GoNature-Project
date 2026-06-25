package data;

import java.io.Serializable;

/**
 * Represents information about a connected client.
 * <p>
 * This class stores the client's host name, IP address,
 * and current connection status. It is used to transfer
 * client information between the server and other system
 * components.
 * </p>
 *
 * @author Fares
 * @version 1.0
 */
@SuppressWarnings("serial")
public class ClientInfo implements Serializable {

    /**
     * The host name of the client.
     */
    private String host;

    /**
     * The IP address of the client.
     */
    private String ip;

    /**
     * The current connection status of the client.
     */
    private String status;

    /**
     * Creates a new ClientInfo object.
     *
     * @param host the client's host name.
     * @param ip the client's IP address.
     * @param status the client's current connection status.
     */
    public ClientInfo(String host, String ip, String status) {
        this.host = host;
        this.ip = ip;
        this.status = status;
    }

    /**
     * Returns the client's host name.
     *
     * @return the host name.
     */
    public String getHost() {
        return host;
    }

    /**
     * Sets the client's host name.
     *
     * @param host the new host name.
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * Returns the client's IP address.
     *
     * @return the IP address.
     */
    public String getIp() {
        return ip;
    }

    /**
     * Sets the client's IP address.
     *
     * @param ip the new IP address.
     */
    public void setIp(String ip) {
        this.ip = ip;
    }

    /**
     * Returns the client's current connection status.
     *
     * @return the connection status.
     */
    public String getStatus() {
        return status;
    }

    /**
     * Updates the client's connection status.
     *
     * @param status the new connection status.
     */
    public void setStatus(String status) {
        this.status = status;
    }
}