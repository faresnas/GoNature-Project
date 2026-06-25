package Server;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import Common.Chat;
import Common.LoginResponse;
import ocsf.server.AbstractServer;
import ocsf.server.ConnectionToClient;
import data.Reservation;

/**
 * The {@code EchoServer} class serves as the central backend controller for the GoNature system.
 * It extends {@link AbstractServer} from the OCSF framework to listen on a designated network port,
 * intercept incoming transport packets from clients, and delegate structural transactions to specialized
 * database subsystems (e.g., subsystem modules for entry/exit terminal operations, order processing, 
 * report compilation, and management approval workflows).
 * <p>
 * It maintains state synchronization by caching active user sessions within a concurrent map and houses
 * daemon infrastructure background tasks to automatically manage real-time events such as automated visitor 
 * exit protocols and booking notifications.
 * </p>
 *
 * @author GoNature Development Team
 * @version 1.0
 */
public class EchoServer extends AbstractServer {
    
    /** Database management unit operating hardware gate terminal entry and exit evaluations. */
    private EntryExitDB entryExitDB = new EntryExitDB();
    
    /** Base primary persistence driver managing systemic low-level JDBC driver tracking channels. */
    private DBController dbController;
    
    /** Specialized subsystem handling reservation persistence logic, scheduling, and capacity audits. */
    private ReservationDB reservationDB;
    
    /** Management database handling guide/subscriber registrations and parameter change requests. */
    private ManagementDB managementDB;
    
    /** Reporting module executing analytic summaries, compiling stay-durations, and calculating cancellations. */
    private ReportsDB reportsDB;
    
    /** Database driver tracking raw tabular order changes and manual parameters. */
    private OrderDB orderDB;
    
    /** Internal watchdog system configured to automatically shut down idle server resources if no nodes attach. */
    private java.util.Timer connectionTimer;
    
    /** Thread-safe dictionary tracking concurrent verified client identities mapped to active network socket instances. */
    private Map<String, ConnectionToClient> activeSessions = new ConcurrentHashMap<>();

    /** System logger adapter instance channeling system execution narratives out to physical server GUI panels. */
    private ServerLogCallback logCallback;

    /**
     * Interface providing callback capabilities to route operational logs from background thread tasks 
     * out onto visible interface log controls.
     */
    public interface ServerLogCallback {
        /**
         * Invoked to write descriptive tracking text out to targeted visualization interfaces.
         *
         * @param message Text tracing contextual execution statuses or structural tracking exceptions.
         */
        void log(String message);
    }

    /** Real-time callback monitor supervising network context attachments and registration tracking hooks. */
    private ClientListCallback clientListCallback;

    /**
     * Interface defining structural hooks to intercept physical connection modifications inside the 
     * OCSF management framework.
     */
    public interface ClientListCallback {
        /**
         * Fired immediately when a fresh client network connection establishes successfully on the port.
         *
         * @param clientId Generated temporal identifier token string tracking the client.
         * @param ip       Target source IP network address string.
         * @param host     Target source machine domain name lookup context.
         */
        void onClientConnected(String clientId, String ip, String host);
        
        /**
         * Fired immediately when a logged socket or connection context detaches from the server port.
         *
         * @param clientId Target programmatic tracking sequence identification tag.
         */
        void onClientDisconnected(String clientId);
    }

    /**
     * Constructs a {@code EchoServer} listening instance targeting the defined port allocation sequence.
     *
     * @param port Target communication port index mapping where socket servers monitor.
     */
    public EchoServer(int port) {
        super(port);
    }

    /**
     * Configures the programmatic log callback hook.
     *
     * @param callback Operational instance matching interface blueprints.
     */
    public void setLogCallback(ServerLogCallback callback) {
        this.logCallback = callback;
    }

    /**
     * Configures the programmatic client monitoring socket hook.
     *
     * @param callback Operational instance matching interface blueprints.
     */
    public void setClientListCallback(ClientListCallback callback) {
        this.clientListCallback = callback;
    }

    /**
     * Local routing helper funneling operational logs to the standard console and registered callback hooks.
     *
     * @param msg Text tracing state changes or structural exceptions.
     */
    private void logMessage(String msg) {
        System.out.println(msg);
        if (logCallback != null) {
            logCallback.log(msg);
        }
    }
    
    /**
     * Interface establishing standard structures allowing sub-module engines to selectively invoke real-time
     * data pushes targeting distinct connected users.
     */
    public interface NotificationCallback {
        /**
         * Forwards structured message payloads down onto explicit user channels.
         *
         * @param username Target distinctive user credentials key tracking session bindings.
         * @param message  Transport parameters or data arrays targeted for delivery.
         */
        void notifyUser(String username, Object message);
    }

    /**
     * Handles cleaning up internal references and mapping states when a connection breaks or requests teardown.
     * Evicts cached records from session dictionaries and signals state listeners on the management thread.
     *
     * @param client The structural connection context item dropping communications.
     */
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

    /**
     * Iterates across active network connection pipes to forcefully push refreshed master lists down onto all 
     * connected clients except the user who initiated the original save transaction.
     *
     * @param requester Distinct active socket pipeline reference belonging to the user making changes.
     */
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
    
    /**
     * Broadcasts an explicit refresh notification command to all active clients, forcing visible manager dashboards
     * to refresh pending request tables in real-time.
     */
    private void broadcastRefreshRequests() {
        try {
            Thread[] clients = getClientConnections();
            for (Thread t : clients) {
                ConnectionToClient c = (ConnectionToClient) t;
                try {
                    c.sendToClient("REFRESH_REQUESTS");
                } catch (Exception ex) {
                    logMessage("[ERROR] Failed to push refresh to client: " + ex.getMessage());
                }
            }
        } catch (Exception e) {
            logMessage("[ERROR] broadcastRefreshRequests failed: " + e.getMessage());
        }
    }

    /**
     * Processes profile validations by searching data schemas (Visitors, Subscribers, Employees, Guides).
     * Enforces single-session constraints by blocking authentication attempts if the credentials match 
     * an active session key in {@code activeSessions}.
     *
     * @param loginData Array collection packing the identifier types, tags, and credential strings.
     * @param client    The corresponding active incoming socket instance requesting validation clearance.
     * @return Formulated {@link LoginResponse} instance carrying success tokens or validation error text descriptions.
     */
    private LoginResponse handleLogin(ArrayList<String> loginData, ConnectionToClient client) {
        String loginType = loginData.get(0);

        if (loginType.equals("VISITOR")) {
            String idNumber = loginData.get(1);

            if (activeSessions.containsKey(idNumber)) {
                return new LoginResponse(false, "This user is already logged in.");
            }

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
                activeSessions.put(idNumber, client);
                client.setInfo("username", idNumber);
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
                activeSessions.put(idNumber, client);
                client.setInfo("username", idNumber);
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
                    int pid = Integer.parseInt(parkIdStr);
                    res.setParkId(pid);
                    ArrayList<ArrayList<String>> parkResult = dbController.executeQuery(
                        "SELECT name FROM parks WHERE id = " + pid);
                    if (parkResult != null && !parkResult.isEmpty()) {
                        res.setParkName(parkResult.get(0).get(0));
                    }
                }
                logMessage("[LOGIN] " + username + " logged in as " + actualRole);
                activeSessions.put(username, client);
                client.setInfo("username", username);
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
                activeSessions.put(username, client);
                client.setInfo("username", username);
                return res;
            }
            return new LoginResponse(false, "Invalid username or password.");
        }

        return new LoginResponse(false, "Unknown login type.");
    }

    /**
     * Primary message dispatcher evaluating incoming transport requests from connected client nodes.
     * Extracted messages are cast into standard transport instances and processed via conditional branches
     * that map request strings to the appropriate target sub-database processing engines.
     *
     * @param msg    Polymorphic packet object received from the socket line.
     * @param client Unique reference pipe tracing back to the specific requesting source node.
     */
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
                client.sendToClient(response);

            } else if (command.equals("ENTRY_WITH_RESERVATION")) {
                logMessage("[REQUEST] " + clientIP + " → ENTRY_WITH_RESERVATION");
                String identifier = (String) request.getData();
                Common.EntryExitResponse response = entryExitDB.approveReservationEntry(identifier);
                client.sendToClient(response);

            } else if (command.equals("WALK_IN_ENTRY")) {
                logMessage("[REQUEST] " + clientIP + " → WALK_IN_ENTRY");
                ArrayList<Object> data = (ArrayList<Object>) request.getData();
                int parkId = (int) data.get(0);
                int numVisitors = (int) data.get(1);
                String visitorType = (String) data.get(2);
                Common.EntryExitResponse response =
                        entryExitDB.approveWalkInEntry(parkId, numVisitors, visitorType);
                client.sendToClient(response);

            } else if (command.equals("REGISTER_EXIT")) {
                logMessage("[REQUEST] " + clientIP + " → REGISTER_EXIT");
                String identifier = (String) request.getData();
                Common.EntryExitResponse response = entryExitDB.registerExit(identifier);
                client.sendToClient(response);

            } else if (command.equals("REGISTER_MANUAL_EXIT")) {
                logMessage("[REQUEST] " + clientIP + " → REGISTER_MANUAL_EXIT");
                ArrayList<Object> data = (ArrayList<Object>) request.getData();
                int parkId = (int) data.get(0);
                int numVisitors = (int) data.get(1);
                Common.EntryExitResponse response =
                        entryExitDB.registerManualExit(parkId, numVisitors);
                client.sendToClient(response);

            } else if (command.equals("GET_CURRENT_VISITORS")) {
                logMessage("[REQUEST] " + clientIP + " → GET_CURRENT_VISITORS");
                int parkId = (int) request.getData();
                Common.EntryExitResponse response = entryExitDB.getCurrentVisitorsResponse(parkId);
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
                try { client.close(); } catch (Exception e) {
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
                if (updateResult) broadcastUpdatedOrders(client);

            } else if (command.equals("CREATE_RESERVATION")) {
                logMessage("[REQUEST] " + clientIP + " → CREATE_RESERVATION");
                data.Reservation reservation = (data.Reservation) request.getData();
                String resultString = reservationDB.createReservation(reservation);
                client.sendToClient(resultString);

            } else if (command.equals("GET_MY_RESERVATIONS")) {
                logMessage("[REQUEST] " + clientIP + " → GET_MY_RESERVATIONS");
                ArrayList<Object> data = (ArrayList<Object>) request.getData();
                int travelerId = (int) data.get(0);
                String travelerType = (String) data.get(1);
                ArrayList<ArrayList<String>> reservations =
                        reservationDB.getReservationsByTraveler(travelerId, travelerType);
                client.sendToClient(reservations);

            } else if (command.equals("UPDATE_RESERVATION")) {
                logMessage("[REQUEST] " + clientIP + " → UPDATE_RESERVATION");
                ArrayList<Object> data = (ArrayList<Object>) request.getData();
                int reservationId = (int) data.get(0);
                String visitDate = (String) data.get(1);
                String entryTime = (String) data.get(2);
                int numVisitors = (int) data.get(3);
                boolean result =
                        reservationDB.updateReservation(reservationId, visitDate, entryTime, numVisitors);
                client.sendToClient(result);

            } else if (command.equals("DELETE_RESERVATION")) {
                logMessage("[REQUEST] " + clientIP + " → DELETE_RESERVATION");
                ArrayList<Object> data = (ArrayList<Object>) request.getData();
                int reservationId = (int) data.get(0);
                int travelerId = (int) data.get(1);
                String travelerType = (String) data.get(2);
                boolean result =
                        reservationDB.deleteReservation(reservationId, travelerId, travelerType);
                client.sendToClient(result);

            } else if (command.equals("GET_PARKS")) {
                logMessage("[REQUEST] " + clientIP + " → GET_PARKS");
                ArrayList<ArrayList<String>> parks = reservationDB.getParks();
                client.sendToClient(parks);

            } else if (command.equals("JOIN_WAITING_LIST")) {
                Reservation reservation = (Reservation) request.getData();
                boolean result = reservationDB.addToWaitingList(reservation);
                client.sendToClient(result);
              
            } else if (command.equals("CONFIRM_REMINDER")) {
                ArrayList<Object> data = (ArrayList<Object>) request.getData();
                int reservationId = (int) data.get(0);
                int travelerId = (int) data.get(1);
                String travelerType = (String) data.get(2);
                boolean result = reservationDB.confirmReminder(reservationId, travelerId, travelerType);
                client.sendToClient(result);

            } else if (command.equals("REGISTER_SUBSCRIBER")) {
                logMessage("[REQUEST] " + clientIP + " → REGISTER_SUBSCRIBER");
                ArrayList<Object> data = (ArrayList<Object>) request.getData();
                String firstName  = (String) data.get(0);
                String lastName   = (String) data.get(1);
                String idNumber   = (String) data.get(2);
                String phone      = (String) data.get(3);
                String email      = (String) data.get(4);
                int familySize    = (int)    data.get(5);
                String creditCard = (String) data.get(6);
                int subscriberNumber = managementDB.registerSubscriber(
                        firstName, lastName, idNumber, phone, email, familySize, creditCard);
                client.sendToClient(subscriberNumber);

            } else if (command.equals("REGISTER_GUIDE")) {
                logMessage("[REQUEST] " + clientIP + " → REGISTER_GUIDE");
                ArrayList<Object> data = (ArrayList<Object>) request.getData();
                String name     = (String) data.get(0);
                String email    = (String) data.get(1);
                String phone    = (String) data.get(2);
                String idNumber = (String) data.get(3);
                String username = (String) data.get(4);
                String password = (String) data.get(5);
                int result = managementDB.registerGuide(name, email, phone, idNumber, username, password);
                client.sendToClient(result);

            } else if (command.equals("REQUEST_PARK_UPDATE")) {
                logMessage("[REQUEST] " + clientIP + " → REQUEST_PARK_UPDATE");
                ArrayList<Object> data = (ArrayList<Object>) request.getData();
                int parkId      = (int)    data.get(0);
                String reqType  = (String) data.get(1);
                double newValue = (double) data.get(2);
                int requestedBy = (int)    data.get(3);
                boolean result = managementDB.submitParkUpdateRequest(parkId, reqType, newValue, requestedBy);
                client.sendToClient(result);
                if (result) broadcastRefreshRequests();

            } else if (command.equals("GET_PENDING_REQUESTS")) {
                logMessage("[REQUEST] " + clientIP + " → GET_PENDING_REQUESTS");
                ArrayList<ArrayList<String>> requests = managementDB.getPendingRequests();
                client.sendToClient(requests);

            } else if (command.equals("GET_PARK_REQUESTS")) {
                logMessage("[REQUEST] " + clientIP + " → GET_PARK_REQUESTS");
                int parkId = (int) request.getData();
                ArrayList<ArrayList<String>> requests = managementDB.getAllRequestsByPark(parkId);
                client.sendToClient(requests);

            } else if (command.equals("APPROVE_REQUEST")) {
                logMessage("[REQUEST] " + clientIP + " → APPROVE_REQUEST");
                int requestId = (int) request.getData();
                boolean result = managementDB.approveRequest(requestId);
                client.sendToClient(result);
                if (result) broadcastRefreshRequests();
                
            } else if (command.equals("REJECT_REQUEST")) {
                logMessage("[REQUEST] " + clientIP + " → REJECT_REQUEST");
                int requestId = (int) request.getData();
                boolean result = managementDB.rejectRequest(requestId);
                client.sendToClient(result);
                if (result) broadcastRefreshRequests();
                
            } else if (command.equals("GET_ALL_GUIDES")) {
                logMessage("[REQUEST] " + clientIP + " → GET_ALL_GUIDES");
                ArrayList<ArrayList<String>> guides = managementDB.getAllGuides();
                client.sendToClient(guides);

            } else if (command.equals("GET_ALL_SUBSCRIBERS")) {
                logMessage("[REQUEST] " + clientIP + " → GET_ALL_SUBSCRIBERS");
                ArrayList<ArrayList<String>> subscribers = managementDB.getAllSubscribers();
                client.sendToClient(subscribers);

            } else if (command.equals("DELETE_GUIDE")) {
                logMessage("[REQUEST] " + clientIP + " → DELETE_GUIDE");
                int guideId = (int) request.getData();
                boolean result = managementDB.deleteGuide(guideId);
                client.sendToClient(result);

            } else if (command.equals("DELETE_SUBSCRIBER")) {
                logMessage("[REQUEST] " + clientIP + " → DELETE_SUBSCRIBER");
                int subscriberId = (int) request.getData();
                boolean result = managementDB.deleteSubscriber(subscriberId);
                client.sendToClient(result);

            } else if (command.equals("EDIT_GUIDE")) {
                ArrayList<Object> data = (ArrayList<Object>) request.getData();
                int guideId     = (int)    data.get(0);
                String name     = (String) data.get(1);
                String email    = (String) data.get(2);
                String phone    = (String) data.get(3);
                String password = (String) data.get(4);
                boolean result  = managementDB.editGuide(guideId, name, email, phone, password);
                client.sendToClient(result);

            } else if (command.equals("EDIT_SUBSCRIBER")) {
                logMessage("[REQUEST] " + clientIP + " → EDIT_SUBSCRIBER");
                ArrayList<Object> data = (ArrayList<Object>) request.getData();
                int subscriberId = (int)    data.get(0);
                String firstName = (String) data.get(1);
                String lastName  = (String) data.get(2);
                String phone     = (String) data.get(3);
                String email     = (String) data.get(4);
                int familySize   = (int)    data.get(5);
                boolean result   = managementDB.editSubscriber(
                        subscriberId, firstName, lastName, phone, email, familySize);
                client.sendToClient(result);

            } else if (command.equals("GET_VISITS_REPORT")) {
                logMessage("[REQUEST] " + clientIP + " → GET_VISITS_REPORT");
                ArrayList<Object> data = (ArrayList<Object>) request.getData();
                int parkId = (int) data.get(0);
                int month  = (int) data.get(1);
                int year   = (int) data.get(2);
                ArrayList<ArrayList<String>> report = reportsDB.getVisitsReportByPark(parkId, month, year);
                client.sendToClient(report);

            } else if (command.equals("GET_VISITS_REPORT_ALL")) {
                logMessage("[REQUEST] " + clientIP + " → GET_VISITS_REPORT_ALL");
                ArrayList<Object> data = (ArrayList<Object>) request.getData();
                int month = (int) data.get(0);
                int year  = (int) data.get(1);
                ArrayList<ArrayList<String>> report = reportsDB.getVisitsReportAllParks(month, year);
                client.sendToClient(report);

            } else if (command.equals("GET_USAGE_REPORT")) {
                logMessage("[REQUEST] " + clientIP + " → GET_USAGE_REPORT");
                ArrayList<Object> data = (ArrayList<Object>) request.getData();
                int parkId = (int) data.get(0);
                int month  = (int) data.get(1);
                int year   = (int) data.get(2);
                ArrayList<ArrayList<String>> report = reportsDB.getUsageReport(parkId, month, year);
                client.sendToClient(report);

            } else if (command.equals("GET_CANCELLATIONS_REPORT")) {
                logMessage("[REQUEST] " + clientIP + " → GET_CANCELLATIONS_REPORT");
                ArrayList<Object> data = (ArrayList<Object>) request.getData();
                int month = (int) data.get(0);
                int year  = (int) data.get(1);
                ArrayList<ArrayList<String>> report = reportsDB.getCancellationsReport(month, year);
                client.sendToClient(report);
                
            } else if (command.equals("UPDATE_PROFILE")) {
                logMessage("[REQUEST] " + clientIP + " → UPDATE_PROFILE");
                ArrayList<Object> data = (ArrayList<Object>) request.getData();
                String role = (String) data.get(0);
                int userId  = (int)    data.get(1);
                boolean result = false;
                switch (role) {
                    case "VISITOR": {
                        String firstName = (String) data.get(2);
                        String lastName  = (String) data.get(3);
                        String phone     = (String) data.get(4);
                        String email     = (String) data.get(5);
                        String idNumber  = (String) data.get(6);
                        result = managementDB.updateVisitor(userId, firstName, lastName, phone, email, idNumber);
                        break;
                    }
                    case "SUBSCRIBER": {
                        String firstName = (String) data.get(2);
                        String lastName  = (String) data.get(3);
                        String phone     = (String) data.get(4);
                        String email     = (String) data.get(5);
                        String idNumber  = (String) data.get(6);
                        result = managementDB.updateSubscriber(userId, firstName, lastName, phone, email, idNumber);
                        break;
                    }
                    case "GUIDE": {
                        String name     = (String) data.get(2);
                        String username = (String) data.get(3);
                        String phone    = (String) data.get(4);
                        String email    = (String) data.get(5);
                        String password = (String) data.get(6);
                        result = managementDB.updateGuide(userId, name, username, phone, email, password);
                        break;
                    }
                    case "EMPLOYEE": {
                        String firstName = (String) data.get(2);
                        String lastName  = (String) data.get(3);
                        String email     = (String) data.get(4);
                        String username  = (String) data.get(5);
                        String password  = (String) data.get(6);
                        result = managementDB.updateEmployee(userId, firstName, lastName, email, username, password);
                        break;
                    }
                }
                client.sendToClient(result);
                
            } else if (command.equals("CHECK_REMINDERS")) {
                logMessage("[REQUEST] " + clientIP + " → CHECK_REMINDERS");
                ArrayList<Object> data = (ArrayList<Object>) request.getData();
                int travelerId      = (int)    data.get(0);
                String travelerType = (String) data.get(1);
                reservationDB.sendVisitRemindersForUser(travelerId, travelerType);
                reservationDB.autoCancelUnconfirmedReservations();
                ArrayList<ArrayList<String>> reminders =
                    reservationDB.getPendingReminders(travelerId, travelerType);
                
                ArrayList<ArrayList<String>> waitingNotifications =
                    reservationDB.getPendingWaitingListNotifications(travelerId, travelerType);
                for (ArrayList<String> row : waitingNotifications) {
                    row.add(0, "WL");
                    reminders.add(row);
                }
                client.sendToClient(reminders);
                
            } else if (command.equals("REGISTER_VISITOR")) {
                logMessage("[REQUEST] " + clientIP + " → REGISTER_VISITOR");
                ArrayList<Object> data = (ArrayList<Object>) request.getData();
                String firstName = (String) data.get(0);
                String lastName  = (String) data.get(1);
                String idNumber  = (String) data.get(2);
                String phone     = (String) data.get(3);
                String email     = (String) data.get(4);
                boolean result   = managementDB.registerVisitor(firstName, lastName, idNumber, phone, email);
                client.sendToClient(result);

            } else if (command.equals("CONFIRM_WAITING_LIST")) {
                logMessage("[REQUEST] " + clientIP + " → CONFIRM_WAITING_LIST");
                ArrayList<Object> data = (ArrayList<Object>) request.getData();
                int waitingListId = (int) data.get(0);
                int travelerId    = (int) data.get(1);
                String travelerType = (String) data.get(2);
                boolean result = reservationDB.confirmFromWaitingList(waitingListId, travelerId, travelerType);
                client.sendToClient(result);

            } else if (command.equals("DECLINE_WAITING_LIST")) {
                logMessage("[REQUEST] " + clientIP + " → DECLINE_WAITING_LIST");
                int waitingListId = (int) request.getData();
                boolean result = reservationDB.declineWaitingList(waitingListId);
                client.sendToClient(result);
                
            } else if (command.equals("GET_WAITING_LIST")) {
                logMessage("[REQUEST] " + clientIP + " → GET_WAITING_LIST");
                ArrayList<Object> data = (ArrayList<Object>) request.getData();
                int travelerId      = (int)    data.get(0);
                String travelerType = (String) data.get(1);
                ArrayList<ArrayList<String>> waitingList =
                    reservationDB.getWaitingListByTraveler(travelerId, travelerType);
                client.sendToClient(waitingList);

            } else if (command.equals("LEAVE_WAITING_LIST")) {
                logMessage("[REQUEST] " + clientIP + " → LEAVE_WAITING_LIST");
                int waitingListId = (int) request.getData();
                boolean result = reservationDB.leaveWaitingList(waitingListId);
                client.sendToClient(result);
                
            } else if (command.equals("GET_VISITOR_COUNT_REPORT")) {
                logMessage("[REQUEST] " + clientIP + " → GET_VISITOR_COUNT_REPORT");
                ArrayList<Object> data = (ArrayList<Object>) request.getData();
                int parkId = (int) data.get(0);
                int month  = (int) data.get(1);
                int year   = (int) data.get(2);
                ArrayList<ArrayList<String>> report = reportsDB.getVisitorCountByPark(parkId, month, year);
                client.sendToClient(report);

            } else if (command.equals("GET_VISITOR_COUNT_REPORT_ALL")) {
                logMessage("[REQUEST] " + clientIP + " → GET_VISITOR_COUNT_REPORT_ALL");
                ArrayList<Object> data = (ArrayList<Object>) request.getData();
                int month = (int) data.get(0);
                int year  = (int) data.get(1);
                ArrayList<ArrayList<String>> report = reportsDB.getVisitorCountAllParks(month, year);
                client.sendToClient(report);
                
            } else if (command.equals("GET_CANCELLATIONS_REPORT_BY_PARK")) {
                logMessage("[REQUEST] " + clientIP + " → GET_CANCELLATIONS_REPORT_BY_PARK");
                ArrayList<Object> data = (ArrayList<Object>) request.getData();
                int parkId = (int) data.get(0);
                int month  = (int) data.get(1);
                int year   = (int) data.get(2);
                ArrayList<ArrayList<String>> report = reportsDB.getCancellationsReportByPark(parkId, month, year);
                client.sendToClient(report);
                
            } else if (command.equals("CHECK_AVAILABILITY")) {
                logMessage("[REQUEST] " + clientIP + " → CHECK_AVAILABILITY");
                ArrayList<Object> data = (ArrayList<Object>) request.getData();
                int parkId   = (int)    data.get(0);
                String date  = (String) data.get(1);
                String time  = (String) data.get(2);
                ArrayList<Integer> result = reservationDB.getAvailability(parkId, date, time);
                client.sendToClient(result);
                
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
     * Hook method called automatically by the OCSF framework post successful initialization binds on the port.
     * <p>
     * Boots secondary database managers, wires atomic notification callbacks, initializes time-sensitive 
     * background processing daemons (like reminder checks and auto-exit enforcement), and boots a one-minute 
     * watchdog timer to automatically secure and close the environment if no external connections link up.
     * </p>
     */
    @Override
    protected void serverStarted() {
        dbController = DBController.getInstance();
        orderDB = new OrderDB(dbController);
        reservationDB = new ReservationDB(dbController);
        entryExitDB = new EntryExitDB();

        // Pass notification callback so ReservationDB can push to active clients
        reservationDB.setNotificationCallback((username, message) -> {
            broadcastToUser(username, message);
        });

        // Pass visitor count callback so EntryExitDB can push live updates to all clients
        entryExitDB.setVisitorCountCallback((parkId, currentCount, availableSpots) -> {
            broadcastVisitorCount(parkId, currentCount, availableSpots);
        });

        ReminderService reminderService = new ReminderService(reservationDB);
        reminderService.setDaemon(true);
        reminderService.start();
        AutoExitService autoExitService = new AutoExitService(reservationDB, entryExitDB);
        autoExitService.setDaemon(true);
        autoExitService.start();
        managementDB = new ManagementDB(dbController);
        reportsDB = new ReportsDB(dbController);
        logMessage("[SERVER] GoNature Server is listening on port " + getPort());

        connectionTimer = new java.util.Timer();
        connectionTimer.schedule(new java.util.TimerTask() {
            @Override public void run() {
                if (getNumberOfClients() == 0) {
                    logMessage("[SERVER] No clients connected after 1 minute — shutting down.");
                    try { close(); } catch (Exception e) { e.printStackTrace(); }
                }
            }
        }, 60000);
    }

    /**
     * Hook method called automatically by OCSF whenever a fresh client successfully completes socket negotiation.
     * Cancels the automated one-minute idle shutdown timer if active.
     *
     * @param client The structural socket channel connection established for the client.
     */
    @Override
    protected void clientConnected(ConnectionToClient client) {
        if (connectionTimer != null) {
            connectionTimer.cancel();
            connectionTimer = null;
        }
        String ip = client.getInetAddress().getHostAddress();
        String host = client.getInetAddress().getHostName();
        String clientId = ip + ":" + System.currentTimeMillis();
        client.setInfo("clientId", clientId);
        logMessage("[CONNECTED] Client joined — IP: " + ip + " | Host: " + host);
        if (clientListCallback != null) {
            clientListCallback.onClientConnected(clientId, ip, host);
        }
    }

    /**
     * Internal framework pusher targeting individual consumers to deliver real-time system notices
     * (e.g., automated waiting list spot confirmations).
     *
     * @param username Target distinctive customer database authorization identification sequence.
     * @param message  Data object structure payload transmitted down onto the user channel.
     */
    private void broadcastToUser(String username, Object message) {
        ConnectionToClient target = activeSessions.get(username);
        if (target != null) {
            try {
                target.sendToClient(message);
                logMessage("[PUSH] Sent waiting list notification to: " + username);
            } catch (Exception e) {
                logMessage("[ERROR] Failed to push to user " + username + ": " + e.getMessage());
            }
        }
    }
    
    /**
     * Pushes real-time concurrent capacity updates to all active client platforms.
     * This updates the active terminal count views on monitor screens automatically.
     *
     * @param parkId         Unique index referencing the targeted park entity.
     * @param currentCount   Active total number of visitors currently inside the physical parameters.
     * @param availableSpots Total vacant spots left before hitting maximum allowed park safety limits.
     */
    private void broadcastVisitorCount(int parkId, int currentCount, int availableSpots) {
        try {
            ArrayList<Object> payload = new ArrayList<>();
            payload.add("VISITOR_COUNT_UPDATE");
            payload.add(parkId);
            payload.add(currentCount);
            payload.add(availableSpots);
            Thread[] clients = getClientConnections();
            for (Thread t : clients) {
                ConnectionToClient c = (ConnectionToClient) t;
                try {
                    c.sendToClient(payload);
                } catch (Exception ex) {
                    // silent fallback
                }
            }
        } catch (Exception e) {
            // silent fallback
        }
    }

    /**
     * Hook method triggered automatically by the OCSF architecture when a client drops off gracefully.
     *
     * @param client The corresponding connection session item detaching.
     */
    @Override
    protected void clientDisconnected(ConnectionToClient client) {
        handleDisconnect(client);
    }

    /**
     * Hook method triggered automatically by OCSF when an active connection experiences unhandled socket errors.
     *
     * @param client    The structural network segment error source.
     * @param exception The root throw item detailing the physical network exception cause.
     */
    @Override
    protected void clientException(ConnectionToClient client, Throwable exception) {
        handleDisconnect(client);
        try { client.close(); } catch (Exception e) {
            logMessage("[ERROR] Failed to close dropped client: " + e.getMessage());
        }
    }

    /**
     * Hook method called automatically when the host server terminates its listening loop.
     * Flushes active resources and safely closes structural relational database connection links.
     */
    @Override
    protected void serverStopped() {
        if (dbController != null) dbController.closeAll();
        logMessage("[SERVER] GoNature Server stopped listening.");
    }
}