package GUI;

import Client.ClientUI;
import Client.OrderClient;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.util.ArrayList;

public class RemoveGuideController {

    @FXML private TableView<ArrayList<String>> guidesTable;
    @FXML private TableColumn<ArrayList<String>, String> idCol;
    @FXML private TableColumn<ArrayList<String>, String> nameCol;
    @FXML private TableColumn<ArrayList<String>, String> emailCol;
    @FXML private TableColumn<ArrayList<String>, String> phoneCol;
    @FXML private TableColumn<ArrayList<String>, String> usernameCol;

    @FXML private TextField editNameField;
    @FXML private TextField editEmailField;
    @FXML private TextField editPhoneField;
    @FXML private TextField editPasswordField;

    private ObservableList<ArrayList<String>> tableData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        OrderClient.removeGuideController = this;

        idCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(0)));
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(1)));
        emailCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(2)));
        phoneCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(3)));
        usernameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(4)));

        guidesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                editNameField.setText(newVal.get(1));
                editEmailField.setText(newVal.get(2));
                editPhoneField.setText(newVal.get(3));
                editPasswordField.clear();
            }
        });

        javafx.application.Platform.runLater(() -> ClientUI.client.getAllGuides());
    }

    public void setGuidesTable(ArrayList<ArrayList<String>> rows) {
        tableData.clear();
        tableData.addAll(rows);
        guidesTable.setItems(tableData);
    }

    @FXML
    void handleEdit(ActionEvent event) {
        ArrayList<String> selected = guidesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a guide to edit.");
            return;
        }

        String name     = editNameField.getText().trim();
        String email    = editEmailField.getText().trim();
        String phone    = editPhoneField.getText().trim();
        String password = editPasswordField.getText().trim();

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Fields", "Name, email and phone cannot be empty.");
            return;
        }

        if (!email.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,4}$")) {
            showAlert(Alert.AlertType.ERROR, "Invalid Email", "Please enter a valid email address.");
            return;
        }

        if (!phone.matches("\\d+")) {
            showAlert(Alert.AlertType.ERROR, "Invalid Phone", "Phone must contain digits only.");
            return;
        }

        int guideId = Integer.parseInt(selected.get(0));
        ClientUI.client.editGuide(guideId, name, email, phone,
            password.isEmpty() ? null : password);
    }

    @FXML
    void handleRemove(ActionEvent event) {
        ArrayList<String> selected = guidesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a guide to remove.");
            return;
        }
        int guideId = Integer.parseInt(selected.get(0));
        ClientUI.client.deleteGuide(guideId);
    }

    public void handleDeleteResponse(boolean result) {
        if (result) {
            showAlert(Alert.AlertType.INFORMATION, "Removed", "Guide removed successfully.");
        } else {
            showAlert(Alert.AlertType.ERROR, "Failed", "Failed to remove guide. Please try again.");
        }
        javafx.application.Platform.runLater(() -> ClientUI.client.getAllGuides());
    }

    public void handleEditResponse(boolean result) {
        if (result) {
            showAlert(Alert.AlertType.INFORMATION, "Updated", "Guide updated successfully.");
        } else {
            showAlert(Alert.AlertType.ERROR, "Failed", "Failed to update guide. Please try again.");
        }
        javafx.application.Platform.runLater(() -> ClientUI.client.getAllGuides());
    }

    @FXML
    void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/GUI/ServiceRepDashboard.fxml"));
            Scene scene = new Scene(loader.load());
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