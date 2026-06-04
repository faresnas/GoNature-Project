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

    // maps clientId (client.toString()) -> display string shown in the list
    private Map<String, String> connectedClients = new HashMap<>();
    private ObservableList<String> clientDisplayList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        clientListView.setItems(clientDisplayList);
        appendLog("[SYSTEM] Server GUI started.");

        if (ServerUI.serverInstance != null) {
            int port = ServerUI.serverInstance.getPort();

            // attach log callback
            ServerUI.serverInstance.setLogCallback(
                message -> Platform.runLater(() -> appendLog(message))
            );

            // attach client list callback
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
            statusLabel.setStyle(
                "-fx-text-fill: #4caf50; -fx-font-size: 14px; -fx-font-weight: bold;");
            appendLog("[SERVER] Connected to GoNature Server on port " + port);
        } else {
            statusLabel.setText("● ERROR — no server instance found");
            statusLabel.setStyle(
                "-fx-text-fill: #e05555; -fx-font-size: 14px; -fx-font-weight: bold;");
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
        }
        clientCountLabel.setText(connectedClients.size() + " connected");
    }

    @FXML
    void clearLog() {
        logArea.clear();
    }

    private void appendLog(String message) {
        String timestamp = LocalTime.now().format(TIME_FORMAT);
        logArea.appendText("[" + timestamp + "] " + message + "\n");
    }
}