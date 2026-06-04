package Server;

import java.util.ArrayList;
import Common.Chat;
import ocsf.server.AbstractServer;
import ocsf.server.ConnectionToClient;

/**
 * This class overrides some of the methods in the abstract superclass in order
 * to give more functionality to the server.
 *
 * @author Dr Timothy C. Lethbridge
 * @author Dr Robert Laganiére
 * @author François Bélanger
 * @author Paul Holden
 * @version July 2000
 */
public class EchoServer extends AbstractServer {

    // Class variables *************************************************

    // Constructors ****************************************************

    /**
     * Constructs an instance of the echo server.
     *
     * @param port The port number to connect on.
     */
    private DBController dbController;
    private OrderDB orderDB;
    private java.util.Timer connectionTimer;

    // callback to send log messages to the GUI
    private ServerLogCallback logCallback;

    public interface ServerLogCallback {
        void log(String message);
    }

    // callback to update the connected clients list in the GUI
    private ClientListCallback clientListCallback;

    public interface ClientListCallback {
        void onClientConnected(String clientId, String ip, String host);
        void onClientDisconnected(String clientId);
    }

    public EchoServer(int port) {
        super(port);
    }

    public void setLogCallback(ServerLogCallback callback) {
        this.logCallback = callback;
    }

    public void setClientListCallback(ClientListCallback callback) {
        this.clientListCallback = callback;
    }

    private void logMessage(String msg) {
        System.out.println(msg);
        if (logCallback != null) {
            logCallback.log(msg);
        }
    }

    private void handleDisconnect(ConnectionToClient client) {
        if (client.getInfo("Disconnected") == null) {
            client.setInfo("Disconnected", true);
            String ip = client.getInetAddress().getHostAddress();
            String host = client.getInetAddress().getHostName();
            String clientId = client.toString();
            logMessage("[DISCONNECTED] Client left — IP: " + ip + " | Host: " + host);
            if (clientListCallback != null) {
                clientListCallback.onClientDisconnected(clientId);
            }
        }
    }

    // Observer pattern — broadcast updated orders to all connected clients except the one who requested
    private void broadcastUpdatedOrders(ConnectionToClient requester) {
        try {
            String query = "SELECT * FROM `orders`";
            ArrayList<ArrayList<String>> updatedOrders = orderDB.selectQuery(query);
            logMessage("[BROADCAST] Order updated — pushing fresh data to all clients");
            Thread[] clients = getClientConnections();
            for (Thread t : clients) {
                ConnectionToClient c = (ConnectionToClient) t;
                if (c != requester) {
                    try {
                        c.sendToClient(updatedOrders);
                    } catch (Exception ex) {
                        logMessage("[ERROR] Failed to push update to client: " + ex.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            logMessage("[ERROR] Broadcast failed: " + e.getMessage());
        }
    }

    // Instance methods ************************************************

    /**
     * This method handles any messages received from the client.
     *
     * @param msg    The message received from the client.
     * @param client The connection from which the message originated.
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void handleMessageFromClient(Object msg, ConnectionToClient client) {
        try {
            Chat request = (Chat) msg;
            String command = request.getCommand();
            String clientIP = client.getInetAddress().getHostAddress();

            if (command.equals("GET_ORDERS")) {
                logMessage("[REQUEST] " + clientIP + " → GET_ORDERS");
                String query = "SELECT * FROM `orders`";
                ArrayList<ArrayList<String>> orderList = orderDB.selectQuery(query);
                client.sendToClient(orderList);

            } else if (command.equals("UPDATE_ORDER")) {
                logMessage("[REQUEST] " + clientIP + " → UPDATE_ORDER");
                ArrayList<Object> updateData = (ArrayList<Object>) request.getData();
                int id = (int) updateData.get(0);
                String newDate = (String) updateData.get(1);
                int visitors = (int) updateData.get(2);
                boolean updateResult = orderDB.updateQuery(id, newDate, visitors);

                // send success/fail back to the client that made the request
                client.sendToClient(updateResult);

                // Observer pattern — if update succeeded, push fresh data to all other clients
                if (updateResult) {
                    broadcastUpdatedOrders(client);
                }

            } else if (command.equals("CLIENT_EXIT")) {
                handleDisconnect(client);

            } else {
                logMessage("[WARNING] " + clientIP + " → unknown command: " + command);
                client.sendToClient(false);
            }

        } catch (Exception e) {
            logMessage("[ERROR] " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * This method overrides the one in the superclass. Called when the server
     * starts listening for connections.
     */
    @Override
    protected void serverStarted() {
        dbController = DBController.getInstance();
        orderDB = new OrderDB(dbController);
        logMessage("[SERVER] GoNature Server is listening on port " + getPort());

        // start 1 minute timer — if no client connects, stop server
        connectionTimer = new java.util.Timer();
        connectionTimer.schedule(new java.util.TimerTask() {
            @Override
            public void run() {
                if (getNumberOfClients() == 0) {
                    logMessage("[SERVER] No clients connected after 1 minute — shutting down.");
                    try {
                        close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }, 60000);
    }

    @Override
    protected void clientConnected(ConnectionToClient client) {
        if (connectionTimer != null) {
            connectionTimer.cancel();
            connectionTimer = null;
        }
        String ip = client.getInetAddress().getHostAddress();
        String host = client.getInetAddress().getHostName();
        String clientId = client.toString();
        logMessage("[CONNECTED] Client joined — IP: " + ip + " | Host: " + host);
        if (clientListCallback != null) {
            clientListCallback.onClientConnected(clientId, ip, host);
        }
    }

    @Override
    protected void clientDisconnected(ConnectionToClient client) {
        handleDisconnect(client);
    }

    @Override
    protected void clientException(ConnectionToClient client, Throwable exception) {
        handleDisconnect(client);
        try {
            client.close();
        } catch (Exception e) {
            logMessage("[ERROR] Failed to close dropped client: " + e.getMessage());
        }
    }

    /**
     * This method overrides the one in the superclass. Called when the server stops
     * listening for connections.
     */
    @Override
    protected void serverStopped() {
        if (dbController != null) {
            dbController.closeAll();
        }
        logMessage("[SERVER] GoNature Server stopped listening.");
    }
}