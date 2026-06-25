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

/**
 * Controller for the Remove Guide screen.
 * This class allows a service representative to view all registered guides,
 * edit guide details, and remove guides from the system.
 */
public class RemoveGuideController {

    /** Table displaying all registered guides. */
    @FXML private TableView<ArrayList<String>> guidesTable;

    /** Column displaying the guide ID. */
    @FXML private TableColumn<ArrayList<String>, String> idCol;

    /** Column displaying the guide name. */
    @FXML private TableColumn<ArrayList<String>, String> nameCol;

    /** Column displaying the guide email address. */
    @FXML private TableColumn<ArrayList<String>, String> emailCol;

    /** Column displaying the guide phone number. */
    @FXML private TableColumn<ArrayList<String>, String> phoneCol;

    /** Column displaying the guide username. */
    @FXML private TableColumn<ArrayList<String>, String> usernameCol;

    /** Text field used to edit the guide name. */
    @FXML private TextField editNameField;

    /** Text field used to edit the guide email address. */
    @FXML private TextField editEmailField;

    /** Text field used to edit the guide phone number. */
    @FXML private TextField editPhoneField;

    /** Text field used to enter a new guide password. */
    @FXML private TextField editPasswordField;

    /**
     * Observable list containing the guide records displayed in the table.
     */
    private ObservableList<ArrayList<String>> tableData =
            FXCollections.observableArrayList();

    /**
     * Initializes the screen after the FXML file is loaded.
     * Configures the table columns, registers this controller,
     * fills the edit fields when a guide is selected,
     * and requests the guide list from the server.
     */
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

    /**
     * Updates the guides table with data received from the server.
     *
     * @param rows the guide records received from the server
     */
    public void setGuidesTable(ArrayList<ArrayList<String>> rows) {
        tableData.clear();
        tableData.addAll(rows);
        guidesTable.setItems(tableData);
    }

    /**
     * Edits the selected guide using the values entered in the edit fields.
     *
     * @param event the button click event
     */
    @FXML
    void handleEdit(ActionEvent event) {
        ArrayList<String> selected = guidesTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a guide to edit.");
            return;
        }

        String name = editNameField.getText().trim();
        String email = editEmailField.getText().trim();
        String phone = editPhoneField.getText().trim();
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
        ClientUI.client.editGuide(
                guideId,
                name,
                email,
                phone,
                password.isEmpty() ? null : password);
    }

    /**
     * Removes the selected guide from the system.
     *
     * @param event the button click event
     */
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

    /**
     * Handles the server response after deleting a guide.
     *
     * @param result true if the guide was removed successfully,
     *               otherwise false
     */
    public void handleDeleteResponse(boolean result) {
        if (result) {
            showAlert(Alert.AlertType.INFORMATION, "Removed", "Guide removed successfully.");
        } else {
            showAlert(Alert.AlertType.ERROR, "Failed", "Failed to remove guide. Please try again.");
        }

        javafx.application.Platform.runLater(() -> ClientUI.client.getAllGuides());
    }

    /**
     * Handles the server response after editing guide information.
     *
     * @param result true if the guide was updated successfully,
     *               otherwise false
     */
    public void handleEditResponse(boolean result) {
        if (result) {
            showAlert(Alert.AlertType.INFORMATION, "Updated", "Guide updated successfully.");
        } else {
            showAlert(Alert.AlertType.ERROR, "Failed", "Failed to update guide. Please try again.");
        }

        javafx.application.Platform.runLater(() -> ClientUI.client.getAllGuides());
    }

    /**
     * Returns the user to the Service Representative Dashboard.
     *
     * @param event the button click event
     */
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

    /**
     * Displays an alert dialog with the specified message.
     *
     * @param type the type of alert
     * @param title the alert window title
     * @param content the message displayed to the user
     */
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}