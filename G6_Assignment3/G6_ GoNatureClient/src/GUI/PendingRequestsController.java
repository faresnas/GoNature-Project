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

import java.util.ArrayList;

/**
 * Controller for the Pending Requests screen.
 * This class allows the department manager to view,
 * approve, or reject pending park parameter requests.
 * The requests are displayed in a table and updated
 * dynamically according to the server responses.
 */
public class PendingRequestsController {

    /**
     * Table displaying all pending requests.
     */
    @FXML
    private TableView<ArrayList<String>> requestsTable;

    /**
     * Column displaying the request ID.
     */
    @FXML
    private TableColumn<ArrayList<String>, String> idCol;

    /**
     * Column displaying the park name.
     */
    @FXML
    private TableColumn<ArrayList<String>, String> parkCol;

    /**
     * Column displaying the request type.
     */
    @FXML
    private TableColumn<ArrayList<String>, String> typeCol;

    /**
     * Column displaying the requested value.
     */
    @FXML
    private TableColumn<ArrayList<String>, String> valueCol;

    /**
     * Column displaying the employee who submitted the request.
     */
    @FXML
    private TableColumn<ArrayList<String>, String> requestedByCol;

    /**
     * Column displaying the request creation date.
     */
    @FXML
    private TableColumn<ArrayList<String>, String> createdAtCol;

    /**
     * Observable list containing all pending requests
     * displayed in the table.
     */
    private ObservableList<ArrayList<String>> tableData =
            FXCollections.observableArrayList();

    /**
     * Initializes the controller after loading the FXML file.
     * Configures the table columns, registers this controller,
     * and requests the latest pending requests from the server.
     */
    @FXML
    public void initialize() {
        OrderClient.pendingRequestsController = this;

        idCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(0)));
        parkCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(1)));
        typeCol.setCellValueFactory(data -> new SimpleStringProperty(
            friendlyTypeName(data.getValue().get(2))));
        valueCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(3)));
        requestedByCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(4)));
        createdAtCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(5)));

        refreshTable();
    }

    /**
     * Converts internal request type codes into
     * user-friendly names for display.
     *
     * @param type the internal request type
     * @return a readable request type description
     */
    private String friendlyTypeName(String type) {
        switch (type) {
            case "MAX_CAPACITY":       return "Max Capacity";
            case "PREBOOKED_RESERVED": return "Prebooked Reserved";
            case "AVG_STAY_HOURS":     return "Avg Stay Hours";
            case "PROMOTION":          return "Promotion Discount (%)";
            default:                   return type;
        }
    }

    /**
     * Requests the latest pending requests from the server.
     */
    public void refreshTable() {
        ClientUI.client.getPendingRequests();
    }

    /**
     * Public method used by other controllers to refresh
     * the pending requests table.
     */
    public void refreshTablePublic() {
        refreshTable();
    }

    /**
     * Updates the table with the latest pending requests
     * received from the server.
     *
     * @param rows the list of pending requests
     */
    public void setPendingRequestsTable(ArrayList<ArrayList<String>> rows) {
        tableData.clear();
        tableData.addAll(rows);
        requestsTable.setItems(tableData);
    }

    /**
     * Approves the selected request.
     * Sends the approval request to the server.
     *
     * @param event the button click event
     */
    @FXML
    void handleApprove(ActionEvent event) {
        ArrayList<String> selected = requestsTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert(Alert.AlertType.WARNING,
                    "No Selection",
                    "Please select a request to approve.");
            return;
        }

        int requestId = Integer.parseInt(selected.get(0));
        ClientUI.client.approveRequest(requestId);
    }

    /**
     * Rejects the selected request.
     * Sends the rejection request to the server.
     *
     * @param event the button click event
     */
    @FXML
    void handleReject(ActionEvent event) {
        ArrayList<String> selected = requestsTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert(Alert.AlertType.WARNING,
                    "No Selection",
                    "Please select a request to reject.");
            return;
        }

        int requestId = Integer.parseInt(selected.get(0));
        ClientUI.client.rejectRequest(requestId);
    }

    /**
     * Handles the server response after approving a request.
     *
     * @param result true if the request was approved successfully,
     *               otherwise false
     */
    public void handleApproveResponse(boolean result) {
        if (result) {
            showAlert(Alert.AlertType.INFORMATION,
                    "Approved",
                    "Request approved and changes applied to the park immediately.");
        } else {
            showAlert(Alert.AlertType.ERROR,
                    "Failed",
                    "Failed to approve request. It may have already been processed.");
        }

        refreshTable();
    }

    /**
     * Handles the server response after rejecting a request.
     *
     * @param result true if the request was rejected successfully,
     *               otherwise false
     */
    public void handleRejectResponse(boolean result) {
        if (result) {
            showAlert(Alert.AlertType.INFORMATION,
                    "Rejected",
                    "Request has been rejected and discarded.");
        } else {
            showAlert(Alert.AlertType.ERROR,
                    "Failed",
                    "Failed to reject request. It may have already been processed.");
        }

        refreshTable();
    }

    /**
     * Returns the user to the Department Manager Dashboard.
     *
     * @param event the button click event
     */
    @FXML
    void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/GUI/DeptManagerDashboard.fxml"));

            Scene scene = new Scene(loader.load());
            ClientUI.primaryStage.setScene(scene);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Displays an alert dialog with the specified information.
     *
     * @param type the alert type
     * @param title the alert title
     * @param content the alert message
     */
    private void showAlert(Alert.AlertType type,
                           String title,
                           String content) {

        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}