package Server;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import Common.Chat;
import Common.LoginResponse;
import ocsf.server.AbstractServer;
import ocsf.server.ConnectionToClient;

public class EchoServer extends AbstractServer {

    private DBController dbController;
    private OrderDB orderDB;
    private java.util.Timer connectionTimer;
    private Map<String, ConnectionToClient> activeSessions = new ConcurrentHashMap<>();

    private ServerLogCallback logCallback;

    public interface ServerLogCallback {
        void log(String message);
    }

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
            String username = (String) client.getInfo("username");
            if (username != null) {
                activeSessions.remove(username);
                logMessage("[SESSION] Removed session for: " + username);
            }
            String ip = client.getInetAddress().getHostAddress();
            String host = client.getInetAddress().getHostName();
            String clientId = (String) client.getInfo("clientId");
            if (clientId == null) clientId = client.toString();
            logMessage("[DISCONNECTED] Client left — IP: " + ip + " | Host: " + host);
            if (clientListCallback != null) {
                clientListCallback.onClientDisconnected(clientId);
            }
        }
    }

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

    private LoginResponse handleLogin(ArrayList<String> loginData, ConnectionToClient client) {
        String loginType = loginData.get(0);

        if (loginType.equals("VISITOR")) {
            String idNumber = loginData.get(1);

            String sql = "SELECT * FROM visitors WHERE id_number = '" + idNumber + "'";
            ArrayList<ArrayList<String>> result = dbController.executeQuery(sql);
            if (result != null && !result.isEmpty()) {
                ArrayList<String> row = result.get(0);
                LoginResponse res = new LoginResponse(true, "Welcome!");
                res.setUserType(LoginResponse.UserType.VISITOR);
                res.setUserId(Integer.parseInt(row.get(0)));
                res.setFirstName(row.get(2));
                res.setLastName(row.get(3));
                res.setEmail(row.get(5));
                return res;
            }

            sql = "SELECT * FROM subscribers WHERE id_number = '" + idNumber + "'";
            result = dbController.executeQuery(sql);
            if (result != null && !result.isEmpty()) {
                ArrayList<String> row = result.get(0);
                LoginResponse res = new LoginResponse(true, "Welcome!");
                res.setUserType(LoginResponse.UserType.SUBSCRIBER);
                res.setUserId(Integer.parseInt(row.get(0)));
                res.setFirstName(row.get(1));
                res.setLastName(row.get(2));
                res.setEmail(row.get(5));
                return res;
            }

            return new LoginResponse(false, "ID number not found.");
        }

        if (loginType.equals("EMPLOYEE")) {
            String username = loginData.get(1);
            String password = loginData.get(2);
            String selectedRole = loginData.get(3);

            if (activeSessions.containsKey(username)) {
                return new LoginResponse(false, "This user is already logged in.");
            }

            String sql = "SELECT * FROM employees WHERE username = '" + username + "' AND password = '" + password + "'";
            ArrayList<ArrayList<String>> result = dbController.executeQuery(sql);
            if (result != null && !result.isEmpty()) {
                ArrayList<String> row = result.get(0);
                String actualRole = row.get(5);
                if (!actualRole.equals(selectedRole)) {
                    return new LoginResponse(false, "Wrong role selected for this account.");
                }
                LoginResponse res = new LoginResponse(true, "Welcome!");
                res.setUserType(LoginResponse.UserType.EMPLOYEE);
                res.setUserId(Integer.parseInt(row.get(0)));
                res.setFirstName(row.get(1));
                res.setLastName(row.get(2));
                res.setEmail(row.get(4));
                res.setRole(actualRole);
                String parkIdStr = row.get(6);
                if (parkIdStr != null && !parkIdStr.equals("NULL") && !parkIdStr.isEmpty()) {
                    res.setParkId(Integer.parseInt(parkIdStr));
                }
                logMessage("[LOGIN] " + username + " logged in as " + actualRole);
                return res;
            }
            return new LoginResponse(false, "Invalid username or password.");
        }

        if (loginType.equals("GUIDE")) {
            String username = loginData.get(1);
            String password = loginData.get(2);

            if (activeSessions.containsKey(username)) {
                return new LoginResponse(false, "This user is already logged in.");
            }

            String sql = "SELECT * FROM guides WHERE username = '" + username + "' AND password = '" + password + "'";
            ArrayList<ArrayList<String>> result = dbController.executeQuery(sql);
            if (result != null && !result.isEmpty()) {
                ArrayList<String> row = result.get(0);
                LoginResponse res = new LoginResponse(true, "Welcome!");
                res.setUserType(LoginResponse.UserType.GUIDE);
                res.setRole("GUIDE");
                res.setUserId(Integer.parseInt(row.get(0)));
                res.setFirstName(row.get(1));
                res.setEmail(row.get(2));
                logMessage("[LOGIN] " + username + " logged in as GUIDE");
                return res;
            }
            return new LoginResponse(false, "Invalid username or password.");
        }

        return new LoginResponse(false, "Unknown login type.");
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void handleMessageFromClient(Object msg, ConnectionToClient client) {
        try {
            Chat request = (Chat) msg;
            String command = request.getCommand();
            String clientIP = client.getInetAddress().getHostAddress();

            if (command.equals("LOGIN_REQUEST")) {
                logMessage("[REQUEST] " + clientIP + " → LOGIN_REQUEST");
                ArrayList<String> loginData = (ArrayList<String>) request.getData();
                LoginResponse response = handleLogin(loginData, client);
                if (response.isSuccess() && response.getRole() != null) {
                    activeSessions.put(loginData.get(1), client);
                    client.setInfo("username", loginData.get(1));
                }
                client.sendToClient(response);

            } else if (command.equals("LOGOUT_REQUEST")) {
                logMessage("[REQUEST] " + clientIP + " → LOGOUT_REQUEST");
                String username = (String) client.getInfo("username");
                if (username != null) {
                    activeSessions.remove(username);
                    client.setInfo("username", null);
                    logMessage("[LOGOUT] " + username + " logged out");
                }
                handleDisconnect(client);
                try {
                    client.close();
                } catch (Exception e) {
                    logMessage("[ERROR] Failed to close client after logout: " + e.getMessage());
                }

            } else if (command.equals("GET_ORDERS")) {
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
                client.sendToClient(updateResult);
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

    @Override
    protected void serverStarted() {
        dbController = DBController.getInstance();
        orderDB = new OrderDB(dbController);
        logMessage("[SERVER] GoNature Server is listening on port " + getPort());

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
        // store stable clientId at connect time so it never changes
        String clientId = ip + ":" + System.currentTimeMillis();
        client.setInfo("clientId", clientId);
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

    @Override
    protected void serverStopped() {
        if (dbController != null) {
            dbController.closeAll();
        }
        logMessage("[SERVER] GoNature Server stopped listening.");
    }
}