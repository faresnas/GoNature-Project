package GUI;

import Client.ClientUI;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class ConnectionController {

    @FXML
    private TextField txtIP;
    @FXML
    private TextField txtPort;
    @FXML
    private Label errorLabel;

    @FXML
    void connectToServer(ActionEvent event) {
        errorLabel.setText("");
        try {
            String serverIP = txtIP.getText().trim();
            String serverPort = txtPort.getText().trim();
            if (serverIP.isEmpty() || serverPort.isEmpty()) {
                errorLabel.setText("IP address and port are required.");
                return;
            }
            int port = Integer.parseInt(serverPort);
            ClientUI.startClient(serverIP, port);
        } catch (NumberFormatException e) {
            errorLabel.setText("Port must be a valid number.");
        } catch (Exception e) {
            errorLabel.setText("Could not connect to server.");
            e.printStackTrace();
        }
    }

    @FXML
    void exit(ActionEvent event) {
        System.exit(0);
    }
}