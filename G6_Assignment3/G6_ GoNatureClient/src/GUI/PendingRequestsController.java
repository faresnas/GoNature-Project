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

public class PendingRequestsController {

    @FXML private TableView<ArrayList<String>> requestsTable;
    @FXML private TableColumn<ArrayList<String>, String> idCol;
    @FXML private TableColumn<ArrayList<String>, String> parkCol;
    @FXML private TableColumn<ArrayList<String>, String> typeCol;
    @FXML private TableColumn<ArrayList<String>, String> valueCol;
    @FXML private TableColumn<ArrayList<String>, String> requestedByCol;
    @FXML private TableColumn<ArrayList<String>, String> createdAtCol;

    private ObservableList<ArrayList<String>> tableData = FXCollections.observableArrayList();

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

    private String friendlyTypeName(String type) {
        switch (type) {
            case "MAX_CAPACITY":       return "Max Capacity";
            case "PREBOOKED_RESERVED": return "Prebooked Reserved";
            case "AVG_STAY_HOURS":     return "Avg Stay Hours";
            case "PROMOTION":          return "Promotion Discount (%)";
            default:                   return type;
        }
    }

    private void refreshTable() {
        ClientUI.client.getPendingRequests();
    }

    public void setPendingRequestsTable(ArrayList<ArrayList<String>> rows) {
        tableData.clear();
        tableData.addAll(rows);
        requestsTable.setItems(tableData);
    }

    @FXML
    void handleApprove(ActionEvent event) {
        ArrayList<String> selected = requestsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection",
                "Please select a request to approve.");
            return;
        }
        int requestId = Integer.parseInt(selected.get(0));
        ClientUI.client.approveRequest(requestId);
    }

    @FXML
    void handleReject(ActionEvent event) {
        ArrayList<String> selected = requestsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection",
                "Please select a request to reject.");
            return;
        }
        int requestId = Integer.parseInt(selected.get(0));
        ClientUI.client.rejectRequest(requestId);
    }

    public void handleApproveResponse(boolean result) {
        if (result) {
            showAlert(Alert.AlertType.INFORMATION, "Approved",
                "Request approved and changes applied to the park immediately.");
        } else {
            showAlert(Alert.AlertType.ERROR, "Failed",
                "Failed to approve request. It may have already been processed.");
        }
        refreshTable();
    }

    public void handleRejectResponse(boolean result) {
        if (result) {
            showAlert(Alert.AlertType.INFORMATION, "Rejected",
                "Request has been rejected and discarded.");
        } else {
            showAlert(Alert.AlertType.ERROR, "Failed",
                "Failed to reject request. It may have already been processed.");
        }
        refreshTable();
    }

    @FXML
    void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/GUI/DeptManagerDashboard.fxml"));
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