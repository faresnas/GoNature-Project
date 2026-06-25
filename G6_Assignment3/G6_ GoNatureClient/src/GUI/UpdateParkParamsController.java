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
import javafx.scene.control.*;

import java.util.ArrayList;

/**
 * Controller for the Update Park Parameters screen.
 * This class allows a park manager to submit requests
 * for updating park parameters such as capacity,
 * reserved spots, average stay hours, and promotion discount.
 */
public class UpdateParkParamsController {

    /** Combo box used to select the park parameter to update. */
    @FXML private ComboBox<String> paramBox;

    /** Text field used to enter the new value for the selected parameter. */
    @FXML private TextField newValueField;

    /** Label displaying an explanation of the selected parameter. */
    @FXML private Label currentValuesLabel;

    /** Table displaying previous update requests for the park. */
    @FXML private TableView<ArrayList<String>> requestsTable;

    /** Column displaying the requested parameter. */
    @FXML private TableColumn<ArrayList<String>, String> colParam;

    /** Column displaying the requested value. */
    @FXML private TableColumn<ArrayList<String>, String> colValue;

    /** Column displaying the request status. */
    @FXML private TableColumn<ArrayList<String>, String> colStatus;

    /** Column displaying the request creation date. */
    @FXML private TableColumn<ArrayList<String>, String> colDate;

    /** Observable list containing the update requests displayed in the table. */
    private ObservableList<ArrayList<String>> requestsData =
            FXCollections.observableArrayList();

    /** Stores the ID of the park managed by the logged-in park manager. */
    private int parkId;

    /**
     * Initializes the screen after the FXML file is loaded.
     * The method loads the park ID, prepares the parameter combo box,
     * configures the request table, and loads existing park update requests.
     */
    @FXML
    public void initialize() {
        OrderClient.updateParkParamsController = this;
        parkId = ClientUI.loggedInUser.getParkId();

        paramBox.getItems().addAll(
            "Max Capacity",
            "Prebooked Reserved",
            "Avg Stay Hours",
            "Promotion Discount (%)"
        );

        paramBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item);
                setStyle("-fx-text-fill: white;");
            }
        });

        paramBox.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item);
                setStyle("-fx-text-fill: black;");
            }
        });

        paramBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null)
                return;

            switch (newVal) {
                case "Max Capacity":
                    currentValuesLabel.setText("Maximum number of visitors allowed at any time.");
                    break;
                case "Prebooked Reserved":
                    currentValuesLabel.setText("Spots reserved for pre-booked visitors.");
                    break;
                case "Avg Stay Hours":
                    currentValuesLabel.setText("Average hours a visitor stays. Default: 4 hours.");
                    break;
                case "Promotion Discount (%)":
                    currentValuesLabel.setText("Extra discount % on top of regular pricing (0-50).");
                    break;
            }
        });

        colParam.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(2)));
        colValue.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(3)));
        colStatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(4)));
        colDate.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(5)));

        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);

                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                    return;
                }

                setText(status);

                switch (status) {
                    case "PENDING":
                        setStyle("-fx-text-fill: #f0c040; -fx-font-weight: bold;");
                        break;
                    case "APPROVED":
                        setStyle("-fx-text-fill: #4caf50; -fx-font-weight: bold;");
                        break;
                    case "REJECTED":
                        setStyle("-fx-text-fill: #f44336; -fx-font-weight: bold;");
                        break;
                    default:
                        setStyle("");
                        break;
                }
            }
        });

        requestsTable.setItems(requestsData);
        loadRequests();
    }

    /**
     * Requests all update requests related to the current park
     * from the server.
     */
    public void loadRequests() {
        ClientUI.client.getParkRequests(parkId);
    }

    /**
     * Updates the requests table with data received from the server.
     *
     * @param rows the park update request records received from the server
     */
    public void setRequestsTable(ArrayList<ArrayList<String>> rows) {
        requestsData.clear();
        requestsData.addAll(rows);
        requestsTable.refresh();
    }

    /**
     * Submits a new park parameter update request.
     * The method validates the selected parameter and value,
     * converts the selected parameter into the internal request type,
     * and sends the request to the server.
     *
     * @param event the button click event
     */
    @FXML
    void handleSubmit(ActionEvent event) {
        if (paramBox.getValue() == null || newValueField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING,
                    "Missing Fields",
                    "Please select a parameter and enter a new value.");
            return;
        }

        double newValue;
        try {
            newValue = Double.parseDouble(newValueField.getText().trim());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR,
                    "Invalid Value",
                    "Please enter a valid number.");
            return;
        }

        if (newValue < 0) {
            showAlert(Alert.AlertType.ERROR,
                    "Invalid Value",
                    "Value cannot be negative.");
            return;
        }

        if ("Promotion Discount (%)".equals(paramBox.getValue()) && newValue > 50) {
            showAlert(Alert.AlertType.ERROR,
                    "Invalid Discount",
                    "Promotion discount cannot exceed 50%.");
            return;
        }

        String requestType;

        switch (paramBox.getValue()) {
            case "Max Capacity":
                requestType = "MAX_CAPACITY";
                break;
            case "Prebooked Reserved":
                requestType = "PREBOOKED_RESERVED";
                break;
            case "Avg Stay Hours":
                requestType = "AVG_STAY_HOURS";
                break;
            case "Promotion Discount (%)":
                requestType = "PROMOTION";
                break;
            default:
                return;
        }

        int requestedBy = ClientUI.loggedInUser.getUserId();
        ClientUI.client.requestParkUpdate(parkId, requestType, newValue, requestedBy);
    }

    /**
     * Returns the user to the Park Manager Dashboard.
     *
     * @param event the button click event
     */
    @FXML
    void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/GUI/ParkManagerDashboard.fxml"));

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