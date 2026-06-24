package GUI;

import Client.ClientUI;
import Client.OrderClient;
import Common.Chat;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

import java.util.ArrayList;

public class RegisterVisitorController {

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField idNumberField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;

    private String serverIP;
    private int serverPort;

    public void initConnection(String ip, int port) {
        this.serverIP = ip;
        this.serverPort = port;
    }

    @FXML
    void handleRegister(ActionEvent event) {
        String firstName = firstNameField.getText().trim();
        String lastName  = lastNameField.getText().trim();
        String idNumber  = idNumberField.getText().trim();
        String phone     = phoneField.getText().trim();
        String email     = emailField.getText().trim();

        if (firstName.isEmpty() || lastName.isEmpty() || idNumber.isEmpty() || phone.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Fields",
                "Please fill in all required fields.");
            return;
        }

        if (!firstName.matches("[a-zA-Z\\u0590-\\u05FF ]+")) {
            showAlert(Alert.AlertType.ERROR, "Invalid Name",
                "First name must contain letters only.");
            return;
        }

        if (!lastName.matches("[a-zA-Z\\u0590-\\u05FF ]+")) {
            showAlert(Alert.AlertType.ERROR, "Invalid Name",
                "Last name must contain letters only.");
            return;
        }

        if (!idNumber.matches("\\d+")) {
            showAlert(Alert.AlertType.ERROR, "Invalid ID",
                "ID number must contain digits only.");
            return;
        }

        if (!phone.matches("\\d{7,15}")) {
            showAlert(Alert.AlertType.ERROR, "Invalid Phone",
                "Phone must contain 7 to 15 digits only.");
            return;
        }

        if (!email.isEmpty() && !email.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,4}$")) {
            showAlert(Alert.AlertType.ERROR, "Invalid Email",
                "Please enter a valid email address.");
            return;
        }

        // Connect fresh to server
        try {
            if (ClientUI.client != null) {
                try { ClientUI.client.closeConnection(); } catch (Exception ignored) {}
            }
            ClientUI.client = new Client.OrderClient(serverIP, serverPort);
            ClientUI.client.openConnection();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Connection Error",
                "Could not connect to server.");
            return;
        }

        OrderClient.lastCommand = "REGISTER_VISITOR";
        ArrayList<Object> data = new ArrayList<>();
        data.add(firstName);
        data.add(lastName);
        data.add(idNumber);
        data.add(phone);
        data.add(email.isEmpty() ? null : email);
        ClientUI.client.sendToServer(new Chat("REGISTER_VISITOR", data));
    }

    @FXML
    void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/GUI/Connection.fxml"));
            Scene scene = new Scene(loader.load());
            ClientUI.connectionController = loader.getController();
            ClientUI.primaryStage.setTitle("GoNature — Connect to Server");
            ClientUI.primaryStage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}