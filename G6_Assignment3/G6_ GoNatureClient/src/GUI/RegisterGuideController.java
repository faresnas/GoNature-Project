package GUI;

import Client.ClientUI;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

public class RegisterGuideController {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField usernameField;
    @FXML private TextField passwordField;
    @FXML private TextField idNumberField;

    @FXML
    void handleRegister(ActionEvent event) {
        String name     = nameField.getText().trim();
        String email    = emailField.getText().trim();
        String phone    = phoneField.getText().trim();
        String idNumber = idNumberField.getText().trim();
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() ||
                idNumber.isEmpty() || username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Fields", "Please fill in all fields.");
            return;
        }
        if (!idNumber.matches("\\d+")) {
            showAlert(Alert.AlertType.ERROR, "Invalid ID", "ID number must contain digits only.");
            return;
        }
        if (!email.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$")) {
            showAlert(Alert.AlertType.ERROR, "Invalid Email", "Please enter a valid email address.");
            return;
        }
        if (!phone.matches("\\d+")) {
            showAlert(Alert.AlertType.ERROR, "Invalid Phone", "Phone number must contain digits only.");
            return;
        }
        if (password.length() < 4) {
            showAlert(Alert.AlertType.ERROR, "Weak Password", "Password must be at least 4 characters.");
            return;
        }

        ClientUI.client.registerGuide(name, email, phone, idNumber, username, password);
    }


    @FXML
    void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/GUI/ServiceRepDashboard.fxml"));
            Scene scene = new Scene(loader.load());
            ClientUI.primaryStage.setScene(scene);
        } catch (Exception e) {
            System.out.println("Failed to go back: " + e.getMessage());
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