package GUI;

import Client.ClientUI;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;

public class RegisterSubscriberController {

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField idNumberField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private Spinner<Integer> familySizeSpinner;
    @FXML private TextField creditCardField;

    @FXML
    public void initialize() {
        // No min/max — we validate manually so negative values show error instead of clamping
        familySizeSpinner.setValueFactory(
            new SpinnerValueFactory.IntegerSpinnerValueFactory(Integer.MIN_VALUE, Integer.MAX_VALUE, 1)
        );
        familySizeSpinner.setEditable(true);
    }

    @FXML
    void handleRegister(ActionEvent event) {
        String firstName  = firstNameField.getText().trim();
        String lastName   = lastNameField.getText().trim();
        String idNumber   = idNumberField.getText().trim();
        String phone      = phoneField.getText().trim();
        String email      = emailField.getText().trim();
        String creditCard = creditCardField.getText().trim();

        // Read raw spinner text
        String rawFamily = familySizeSpinner.getEditor().getText().trim();
        int familySize;
        try {
            familySize = Integer.parseInt(rawFamily);
        } catch (NumberFormatException e) {
            familySize = 0;
        }

        if (familySize < 1) {
            showAlert(Alert.AlertType.ERROR, "Invalid Family Size",
                "Family size must be at least 1.");
            return;
        }

        if (familySize > 15) {
            showAlert(Alert.AlertType.ERROR, "Invalid Family Size",
                "Family size cannot exceed 20.");
            return;
        }

        if (firstName.isEmpty() || lastName.isEmpty() || idNumber.isEmpty() ||
                phone.isEmpty() || email.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Fields",
                "Please fill in all required fields.");
            return;
        }

        if (!email.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,4}$")) {
            showAlert(Alert.AlertType.ERROR, "Invalid Email",
                "Please enter a valid email address.");
            return;
        }

        if (!idNumber.matches("\\d+")) {
            showAlert(Alert.AlertType.ERROR, "Invalid ID",
                "ID number must contain digits only.");
            return;
        }

        if (!phone.matches("\\d+")) {
            showAlert(Alert.AlertType.ERROR, "Invalid Phone",
                "Phone number must contain digits only.");
            return;
        }

        ClientUI.client.registerSubscriber(
            firstName, lastName, idNumber, phone, email, familySize,
            creditCard.isEmpty() ? null : creditCard
        );
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