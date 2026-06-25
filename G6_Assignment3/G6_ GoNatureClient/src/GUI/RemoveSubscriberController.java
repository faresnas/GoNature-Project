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

/**
 * Controller for the Remove Subscriber screen.
 * This class allows a service representative to view all subscribers,
 * edit subscriber details, and remove subscribers from the system.
 */
public class RemoveSubscriberController {

    /** Table displaying all registered subscribers. */
    @FXML private TableView<ArrayList<String>> subscribersTable;

    /** Column displaying the subscriber ID. */
    @FXML private TableColumn<ArrayList<String>, String> idCol;

    /** Column displaying the subscriber first name. */
    @FXML private TableColumn<ArrayList<String>, String> firstNameCol;

    /** Column displaying the subscriber last name. */
    @FXML private TableColumn<ArrayList<String>, String> lastNameCol;

    /** Column displaying the subscriber ID number. */
    @FXML private TableColumn<ArrayList<String>, String> idNumberCol;

    /** Column displaying the subscriber phone number. */
    @FXML private TableColumn<ArrayList<String>, String> phoneCol;

    /** Column displaying the subscriber email address. */
    @FXML private TableColumn<ArrayList<String>, String> emailCol;

    /** Column displaying the subscriber family size. */
    @FXML private TableColumn<ArrayList<String>, String> familySizeCol;

    /** Column displaying the subscriber membership number. */
    @FXML private TableColumn<ArrayList<String>, String> subscriberNumCol;

    /** Text field used to edit the subscriber first name. */
    @FXML private TextField editFirstNameField;

    /** Text field used to edit the subscriber last name. */
    @FXML private TextField editLastNameField;

    /** Text field used to edit the subscriber phone number. */
    @FXML private TextField editPhoneField;

    /** Text field used to edit the subscriber email address. */
    @FXML private TextField editEmailField;

    /** Spinner used to edit the subscriber family size. */
    @FXML private Spinner<Integer> editFamilySizeSpinner;

    /**
     * Observable list containing the subscriber records displayed in the table.
     */
    private ObservableList<ArrayList<String>> tableData =
            FXCollections.observableArrayList();

    /**
     * Initializes the screen after the FXML file is loaded.
     * Configures the table columns, prepares the editable family size spinner,
     * fills the edit fields when a subscriber is selected,
     * and requests the subscribers list from the server.
     */
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
            new SpinnerValueFactory.IntegerSpinnerValueFactory(
                Integer.MIN_VALUE,
                Integer.MAX_VALUE,
                1)
        );
        editFamilySizeSpinner.setEditable(true);

        subscribersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                editFirstNameField.setText(newVal.get(1));
                editLastNameField.setText(newVal.get(2));
                editPhoneField.setText(newVal.get(4));
                editEmailField.setText(newVal.get(5));
                editFamilySizeSpinner.getEditor().setText(newVal.get(6));
            }
        });

        javafx.application.Platform.runLater(() -> ClientUI.client.getAllSubscribers());
    }

    /**
     * Updates the subscribers table with data received from the server.
     *
     * @param rows the subscriber records received from the server
     */
    public void setSubscribersTable(ArrayList<ArrayList<String>> rows) {
        tableData.clear();
        tableData.addAll(rows);
        subscribersTable.setItems(tableData);
    }

    /**
     * Edits the selected subscriber using the values entered in the edit fields.
     *
     * @param event the button click event
     */
    @FXML
    void handleEdit(ActionEvent event) {
        ArrayList<String> selected = subscribersTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a subscriber to edit.");
            return;
        }

        String firstName = editFirstNameField.getText().trim();
        String lastName = editLastNameField.getText().trim();
        String phone = editPhoneField.getText().trim();
        String email = editEmailField.getText().trim();

        String rawFamily = editFamilySizeSpinner.getEditor().getText().trim();

        int familySize;
        try {
            familySize = Integer.parseInt(rawFamily);
        } catch (NumberFormatException e) {
            familySize = 0;
        }

        if (familySize < 1) {
            showAlert(Alert.AlertType.ERROR,
                    "Invalid Family Size",
                    "Family size must be at least 1.");
            return;
        }

        if (familySize > 15) {
            showAlert(Alert.AlertType.ERROR,
                    "Invalid Family Size",
                    "Family size cannot exceed 15.");
            return;
        }

        if (firstName.isEmpty() || lastName.isEmpty() || phone.isEmpty() || email.isEmpty()) {
            showAlert(Alert.AlertType.WARNING,
                    "Missing Fields",
                    "Please fill in all fields.");
            return;
        }

        if (!email.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,4}$")) {
            showAlert(Alert.AlertType.ERROR,
                    "Invalid Email",
                    "Please enter a valid email address.");
            return;
        }

        int subscriberId = Integer.parseInt(selected.get(0));
        ClientUI.client.editSubscriber(
                subscriberId,
                firstName,
                lastName,
                phone,
                email,
                familySize);
    }

    /**
     * Removes the selected subscriber from the system.
     *
     * @param event the button click event
     */
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

    /**
     * Handles the server response after deleting a subscriber.
     *
     * @param result true if the subscriber was removed successfully,
     *               otherwise false
     */
    public void handleDeleteResponse(boolean result) {
        if (result) {
            showAlert(Alert.AlertType.INFORMATION, "Removed", "Subscriber removed successfully.");
        } else {
            showAlert(Alert.AlertType.ERROR, "Failed", "Failed to remove subscriber. Please try again.");
        }

        javafx.application.Platform.runLater(() -> ClientUI.client.getAllSubscribers());
    }

    /**
     * Handles the server response after editing subscriber information.
     *
     * @param result true if the subscriber was updated successfully,
     *               otherwise false
     */
    public void handleEditResponse(boolean result) {
        if (result) {
            showAlert(Alert.AlertType.INFORMATION, "Updated", "Subscriber updated successfully.");
        } else {
            showAlert(Alert.AlertType.ERROR, "Failed", "Failed to update subscriber. Please try again.");
        }

        javafx.application.Platform.runLater(() -> ClientUI.client.getAllSubscribers());
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