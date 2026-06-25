package GUI;

import Server.EchoServer;
import Server.ServerUI;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller for the Server Monitor screen.
 * This class displays the server status, connected clients,
 * and real-time server log messages.
 * It also allows clearing the log and stopping the server.
 */
public class ServerMainController {

    /** Text area displaying the server log. */
    @FXML
    private TextArea logArea;

    /** Label displaying the current server status. */
    @FXML
    private Label statusLabel;

    /** Label displaying the number of connected clients. */
    @FXML
    private Label clientCountLabel;

    /** List view displaying the connected clients. */
    @FXML
    private ListView<String> clientListView;

    /** Formatter used to display timestamps in the server log. */
    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm:ss");

    /** Maps client identifiers to their display information. */
    private Map<String, String> connectedClients = new HashMap<>();

    /** List displayed in the connected clients view. */
    private ObservableList<String> clientDisplayList =
            FXCollections.observableArrayList();

    /**
     * Initializes the server monitor screen.
     * Configures the client list, registers callbacks,
     * and displays the current server status.
     */
    @FXML
    public void initialize() {

        clientListView.setItems(clientDisplayList);

        appendLog("[SYSTEM] Server GUI started.");

        if (ServerUI.serverInstance != null) {

            int port = ServerUI.serverInstance.getPort();

            ServerUI.serverInstance.setLogCallback(
                    message -> Platform.runLater(() -> appendLog(message)));

            ServerUI.serverInstance.setClientListCallback(
                    new EchoServer.ClientListCallback() {

                        @Override
                        public void onClientConnected(String clientId,
                                                      String ip,
                                                      String host) {
                            Platform.runLater(() ->
                                    addClient(clientId, ip, host));
                        }

                        @Override
                        public void onClientDisconnected(String clientId) {
                            Platform.runLater(() ->
                                    removeClient(clientId));
                        }
                    });

            statusLabel.setText("● LISTENING on port " + port);
            statusLabel.setStyle(
                    "-fx-text-fill: #4caf50; "
                    + "-fx-font-size: 14px; "
                    + "-fx-font-weight: bold;");

            appendLog("[SERVER] Connected to GoNature Server on port " + port);

        } else {

            statusLabel.setText("● ERROR — no server instance found");
            statusLabel.setStyle(
                    "-fx-text-fill: #e05555; "
                    + "-fx-font-size: 14px; "
                    + "-fx-font-weight: bold;");

            appendLog("[ERROR] Could not find server instance.");
        }
    }

    /**
     * Adds a newly connected client to the client list.
     *
     * @param clientId the client identifier
     * @param ip the client's IP address
     * @param host the client's host name
     */
    private void addClient(String clientId,
                           String ip,
                           String host) {

        String display = ip + " (" + host + ")";

        connectedClients.put(clientId, display);
        clientDisplayList.add(display);

        clientCountLabel.setText(
                connectedClients.size() + " connected");
    }

    /**
     * Removes a disconnected client from the client list.
     *
     * @param clientId the client identifier
     */
    private void removeClient(String clientId) {

        String display = connectedClients.remove(clientId);

        if (display != null) {
            clientDisplayList.remove(display);
        } else if (connectedClients.isEmpty()) {
            clientDisplayList.clear();
        }

        clientCountLabel.setText(
                connectedClients.size() + " connected");
    }

    /**
     * Clears all messages from the server log.
     */
    @FXML
    void clearLog() {
        logArea.clear();
    }

    /**
     * Stops the server, clears all client information,
     * and closes the application.
     */
    @FXML
    void stopServer() {

        try {

            if (ServerUI.serverInstance != null) {
                ServerUI.serverInstance.close();
            }

        } catch (Exception e) {

            appendLog("[ERROR] Failed to stop server: "
                    + e.getMessage());

        } finally {

            appendLog("[SERVER] Server stopped by user.");

            statusLabel.setText("● STOPPED");
            statusLabel.setStyle(
                    "-fx-text-fill: #e05555; "
                    + "-fx-font-size: 14px; "
                    + "-fx-font-weight: bold;");

            clientDisplayList.clear();
            connectedClients.clear();

            clientCountLabel.setText("0 connected");

            Platform.exit();
            System.exit(0);
        }
    }

    /**
     * Appends a timestamped message to the server log.
     *
     * @param message the message to append
     */
    private void appendLog(String message) {

        String timestamp =
                LocalTime.now().format(TIME_FORMAT);

        logArea.appendText(
                "[" + timestamp + "] "
                + message + "\n");
    }
}