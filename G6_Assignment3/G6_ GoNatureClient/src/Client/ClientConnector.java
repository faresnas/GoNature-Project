package Client;

/**
 * Manages the connection between the client application and the GoNature server.
 * This class is responsible for establishing, monitoring, and closing the
 * communication with the server.
 *
 * @author Fares
 * @version 1.0
 */
public class ClientConnector {

    /**
     * The IP address of the server.
     */
    private String serverIP;

    /**
     * The port number used to connect to the server.
     */
    private int serverPort;

    /**
     * Creates a new ClientConnector with the specified server address and port.
     *
     * @param serverIP   the server IP address.
     * @param serverPort the server port number.
     */
    public ClientConnector(String serverIP, int serverPort) {
        this.serverIP = serverIP;
        this.serverPort = serverPort;
    }

    /**
     * Attempts to establish a connection with the server.
     *
     * @return {@code true} if the connection was established successfully,
     *         otherwise {@code false}.
     */
    public boolean connect() {
        try {
            ClientUI.client = new OrderClient(serverIP, serverPort);
            ClientUI.client.openConnection();
            System.out.println("ClientConnector: connection established to " + serverIP + ":" + serverPort);
            return true;
        } catch (Exception e) {
            System.out.println("ClientConnector: failed to connect — " + e.getMessage());
            return false;
        }
    }

    /**
     * Checks whether the client is currently connected to the server.
     *
     * @return {@code true} if the client is connected,
     *         otherwise {@code false}.
     */
    public boolean isConnected() {
        return ClientUI.client != null && ClientUI.client.isConnected();
    }

    /**
     * Closes the connection with the server if it is currently open.
     */
    public void disconnect() {
        try {
            if (isConnected()) {
                ClientUI.client.closeConnection();
                System.out.println("ClientConnector: disconnected from server");
            }
        } catch (Exception e) {
            System.out.println("ClientConnector: error during disconnect — " + e.getMessage());
        }
    }

    /**
     * Returns the configured server IP address.
     *
     * @return the server IP address.
     */
    public String getServerIP() {
        return serverIP;
    }

    /**
     * Returns the configured server port number.
     *
     * @return the server port number.
     */
    public int getServerPort() {
        return serverPort;
    }
}