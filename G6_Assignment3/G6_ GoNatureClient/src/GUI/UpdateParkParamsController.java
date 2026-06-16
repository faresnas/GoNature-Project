package GUI;

import Client.ClientUI;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class UpdateParkParamsController {

    @FXML private ComboBox<String> paramBox;
    @FXML private TextField newValueField;
    @FXML private Label currentValuesLabel;

    @FXML
    public void initialize() {
        paramBox.getItems().addAll(
            "Max Capacity",
            "Prebooked Reserved",
            "Avg Stay Hours",
            "Promotion Discount (%)"
        );

        paramBox.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item);
                setStyle("-fx-text-fill: white;");
            }
        });

        paramBox.setCellFactory(lv -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item);
                setStyle("-fx-text-fill: black;");
            }
        });

        // Show hint when parameter selected
        paramBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) return;
            switch (newVal) {
                case "Max Capacity":
                    currentValuesLabel.setText("Maximum number of visitors allowed in the park at any time.");
                    break;
                case "Prebooked Reserved":
                    currentValuesLabel.setText("Number of spots reserved for pre-booked visitors (walk-ins fill the rest).");
                    break;
                case "Avg Stay Hours":
                    currentValuesLabel.setText("Average hours a visitor stays in the park. Default is 4 hours.");
                    break;
                case "Promotion Discount (%)":
                    currentValuesLabel.setText("Extra discount % applied on top of regular pricing. Enter a value between 0 and 50.");
                    break;
            }
        });
    }

    @FXML
    void handleSubmit(ActionEvent event) {
        if (paramBox.getValue() == null || newValueField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Fields",
                "Please select a parameter and enter a new value.");
            return;
        }

        double newValue;
        try {
            newValue = Double.parseDouble(newValueField.getText().trim());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Value",
                "Please enter a valid number.");
            return;
        }

        if (newValue < 0) {
            showAlert(Alert.AlertType.ERROR, "Invalid Value",
                "Value cannot be negative.");
            return;
        }

        // Promotion discount cap
        if ("Promotion Discount (%)".equals(paramBox.getValue()) && newValue > 50) {
            showAlert(Alert.AlertType.ERROR, "Invalid Discount",
                "Promotion discount cannot exceed 50%.");
            return;
        }

        // Map display name to DB enum
        String requestType;
        switch (paramBox.getValue()) {
            case "Max Capacity":           requestType = "MAX_CAPACITY";       break;
            case "Prebooked Reserved":     requestType = "PREBOOKED_RESERVED"; break;
            case "Avg Stay Hours":         requestType = "AVG_STAY_HOURS";     break;
            case "Promotion Discount (%)": requestType = "PROMOTION";          break;
            default: return;
        }

        int parkId      = ClientUI.loggedInUser.getParkId();
        int requestedBy = ClientUI.loggedInUser.getUserId();

        ClientUI.client.requestParkUpdate(parkId, requestType, newValue, requestedBy);
    }

    @FXML
    void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/GUI/ParkManagerDashboard.fxml"));
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