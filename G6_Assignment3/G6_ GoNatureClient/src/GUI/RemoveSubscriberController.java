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
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.util.ArrayList;

public class RemoveSubscriberController {

    @FXML private TableView<ArrayList<String>> subscribersTable;
    @FXML private TableColumn<ArrayList<String>, String> idCol;
    @FXML private TableColumn<ArrayList<String>, String> firstNameCol;
    @FXML private TableColumn<ArrayList<String>, String> lastNameCol;
    @FXML private TableColumn<ArrayList<String>, String> idNumberCol;
    @FXML private TableColumn<ArrayList<String>, String> phoneCol;
    @FXML private TableColumn<ArrayList<String>, String> emailCol;
    @FXML private TableColumn<ArrayList<String>, String> familySizeCol;
    @FXML private TableColumn<ArrayList<String>, String> subscriberNumCol;

    @FXML private TextField editFirstNameField;
    @FXML private TextField editLastNameField;
    @FXML private TextField editPhoneField;
    @FXML private TextField editEmailField;
    @FXML private Spinner<Integer> editFamilySizeSpinner;

    private ObservableList<ArrayList<String>> tableData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        OrderClient.removeSubscriberController = this;

        idCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(0)));
        firstNameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(1)));
        lastNameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(2)));
        idNumberCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(3)));
        phoneCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(4)));
        emailCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(5)));
        familySizeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(6)));
        subscriberNumCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(7)));

        editFamilySizeSpinner.setValueFactory(
            new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 1)
        );

        // Auto-fill edit fields on row select
        subscribersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                editFirstNameField.setText(newVal.get(1));
                editLastNameField.setText(newVal.get(2));
                editPhoneField.setText(newVal.get(4));
                editEmailField.setText(newVal.get(5));
                try {
                    editFamilySizeSpinner.getValueFactory()
                        .setValue(Integer.parseInt(newVal.get(6)));
                } catch (Exception e) {
                    editFamilySizeSpinner.getValueFactory().setValue(1);
                }
            }
        });

        javafx.application.Platform.runLater(() -> ClientUI.client.getAllSubscribers());
    }

    public void setSubscribersTable(ArrayList<ArrayList<String>> rows) {
        tableData.clear();
        tableData.addAll(rows);
        subscribersTable.setItems(tableData);
    }

    @FXML
    void handleEdit(ActionEvent event) {
        ArrayList<String> selected = subscribersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a subscriber to edit.");
            return;
        }

        String firstName = editFirstNameField.getText().trim();
        String lastName  = editLastNameField.getText().trim();
        String phone     = editPhoneField.getText().trim();
        String email     = editEmailField.getText().trim();
        int familySize   = editFamilySizeSpinner.getValue();

        if (firstName.isEmpty() || lastName.isEmpty() || phone.isEmpty() || email.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Fields", "Please fill in all fields.");
            return;
        }

        if (!email.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$")) {
            showAlert(Alert.AlertType.ERROR, "Invalid Email", "Please enter a valid email address.");
            return;
        }

        int subscriberId = Integer.parseInt(selected.get(0));
        ClientUI.client.editSubscriber(subscriberId, firstName, lastName, phone, email, familySize);
    }

    @FXML
    void handleRemove(ActionEvent event) {
        ArrayList<String> selected = subscribersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a subscriber to remove.");
            return;
        }
        int subscriberId = Integer.parseInt(selected.get(0));
        ClientUI.client.deleteSubscriber(subscriberId);
    }

    public void handleDeleteResponse(boolean result) {
        if (result) {
            showAlert(Alert.AlertType.INFORMATION, "Removed", "Subscriber removed successfully.");
        } else {
            showAlert(Alert.AlertType.ERROR, "Failed", "Failed to remove subscriber. Please try again.");
        }
        javafx.application.Platform.runLater(() -> ClientUI.client.getAllSubscribers());
    }

    public void handleEditResponse(boolean result) {
        if (result) {
            showAlert(Alert.AlertType.INFORMATION, "Updated", "Subscriber updated successfully.");
        } else {
            showAlert(Alert.AlertType.ERROR, "Failed", "Failed to update subscriber. Please try again.");
        }
        javafx.application.Platform.runLater(() -> ClientUI.client.getAllSubscribers());
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