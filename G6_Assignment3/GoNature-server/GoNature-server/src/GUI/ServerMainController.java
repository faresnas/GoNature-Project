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

public class ServerMainController {

    @FXML private TextArea logArea;
    @FXML private Label statusLabel;
    @FXML private Label clientCountLabel;
    @FXML private ListView<String> clientListView;

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private Map<String, String> connectedClients = new HashMap<>();
    private ObservableList<String> clientDisplayList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        clientListView.setItems(clientDisplayList);
        appendLog("[SYSTEM] Server GUI started.");

        if (ServerUI.serverInstance != null) {
            int port = ServerUI.serverInstance.getPort();

            ServerUI.serverInstance.setLogCallback(
                message -> Platform.runLater(() -> appendLog(message))
            );

            ServerUI.serverInstance.setClientListCallback(
                new EchoServer.ClientListCallback() {
                    @Override
                    public void onClientConnected(String clientId, String ip, String host) {
                        Platform.runLater(() -> addClient(clientId, ip, host));
                    }
                    @Override
                    public void onClientDisconnected(String clientId) {
                        Platform.runLater(() -> removeClient(clientId));
                    }
                }
            );

            statusLabel.setText("● LISTENING on port " + port);
            statusLabel.setStyle("-fx-text-fill: #4caf50; -fx-font-size: 14px; -fx-font-weight: bold;");
            appendLog("[SERVER] Connected to GoNature Server on port " + port);

        } else {
            statusLabel.setText("● ERROR — no server instance found");
            statusLabel.setStyle("-fx-text-fill: #e05555; -fx-font-size: 14px; -fx-font-weight: bold;");
            appendLog("[ERROR] Could not find server instance.");
        }
    }

    private void addClient(String clientId, String ip, String host) {
        String display = ip + "  (" + host + ")";
        connectedClients.put(clientId, display);
        clientDisplayList.add(display);
        clientCountLabel.setText(connectedClients.size() + " connected");
    }

    private void removeClient(String clientId) {
        String display = connectedClients.remove(clientId);
        if (display != null) {
            clientDisplayList.remove(display);
        } else {
            // fallback — if map is now empty, clear the display list too
            if (connectedClients.isEmpty()) {
                clientDisplayList.clear();
            }
        }
        clientCountLabel.setText(connectedClients.size() + " connected");
    }

    @FXML
    void clearLog() {
        logArea.clear();
    }

    @FXML
    void stopServer() {
        try {
            if (ServerUI.serverInstance != null) {
                ServerUI.serverInstance.close();
            }
        } catch (Exception e) {
            appendLog("[ERROR] Failed to stop server: " + e.getMessage());
        } finally {
            appendLog("[SERVER] Server stopped by user.");
            statusLabel.setText("● STOPPED");
            statusLabel.setStyle("-fx-text-fill: #e05555; -fx-font-size: 14px; -fx-font-weight: bold;");
            clientDisplayList.clear();
            connectedClients.clear();
            clientCountLabel.setText("0 connected");
            Platform.exit();
            System.exit(0);
        }
    }

    private void appendLog(String message) {
        String timestamp = LocalTime.now().format(TIME_FORMAT);
        logArea.appendText("[" + timestamp + "] " + message + "\n");
    }
}