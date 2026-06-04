package Client;

public class ClientConnector {

    private String serverIP;
    private int serverPort;

    public ClientConnector(String serverIP, int serverPort) {
        this.serverIP = serverIP;
        this.serverPort = serverPort;
    }

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

    public boolean isConnected() {
        return ClientUI.client != null && ClientUI.client.isConnected();
    }

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

    public String getServerIP() {
        return serverIP;
    }

    public int getServerPort() {
        return serverPort;
    }
}