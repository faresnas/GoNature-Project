package GUI;

import Client.ClientUI;
import Client.OrderClient;
import Common.Chat;
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

public class WaitingListController {

    @FXML private TableView<ArrayList<String>> waitingTable;
    @FXML private TableColumn<ArrayList<String>, String> idCol;
    @FXML private TableColumn<ArrayList<String>, String> parkCol;
    @FXML private TableColumn<ArrayList<String>, String> dateCol;
    @FXML private TableColumn<ArrayList<String>, String> timeCol;
    @FXML private TableColumn<ArrayList<String>, String> visitorsCol;
    @FXML private TableColumn<ArrayList<String>, String> positionCol;
    @FXML private TableColumn<ArrayList<String>, String> statusCol;

    private ObservableList<ArrayList<String>> tableData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        OrderClient.waitingListController = this;

        idCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(0)));
        parkCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(1)));
        dateCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(2)));
        timeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(3)));
        visitorsCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(4)));
        positionCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(5)));
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(6)));

        javafx.application.Platform.runLater(() -> loadWaitingList());
    }

    private void loadWaitingList() {
        if (ClientUI.loggedInUser == null) return;
        int travelerId = ClientUI.loggedInUser.getUserId();
        String travelerType = getTravelerType();
        OrderClient.lastCommand = "GET_WAITING_LIST";
        ArrayList<Object> data = new ArrayList<>();
        data.add(travelerId);
        data.add(travelerType);
        ClientUI.client.sendToServer(new Chat("GET_WAITING_LIST", data));
    }

    public void setWaitingListTable(ArrayList<ArrayList<String>> rows) {
        tableData.clear();
        tableData.addAll(rows);
        waitingTable.setItems(tableData);
    }

    @FXML
    void handleLeave(ActionEvent event) {
        ArrayList<String> selected = waitingTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection",
                "Please select a waiting list entry to leave.");
            return;
        }

        if ("CONFIRMED".equals(selected.get(6))) {
            showAlert(Alert.AlertType.ERROR, "Cannot Leave",
                "This entry has already been confirmed as a reservation.");
            return;
        }

        int waitingId = Integer.parseInt(selected.get(0));
        OrderClient.lastCommand = "LEAVE_WAITING_LIST";
        ClientUI.client.sendToServer(new Chat("LEAVE_WAITING_LIST", waitingId));
    }

    public void handleLeaveResponse(boolean result) {
        if (result) {
            showAlert(Alert.AlertType.INFORMATION, "Left Waiting List",
                "You have been removed from the waiting list.");
            loadWaitingList();
        } else {
            showAlert(Alert.AlertType.ERROR, "Failed",
                "Failed to leave waiting list. Please try again.");
        }
    }

    @FXML
    void handleBack(ActionEvent event) {
        try {
            String screen = "GUIDE".equals(
                ClientUI.loggedInUser != null ? ClientUI.loggedInUser.getRole() : ""
            ) ? "/GUI/GuideDashboard.fxml" : "/GUI/VisitorDashboard.fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(screen));
            Scene scene = new Scene(loader.load());
            ClientUI.primaryStage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getTravelerType() {
        if (ClientUI.loggedInUser == null) return "VISITOR";
        if ("GUIDE".equals(ClientUI.loggedInUser.getRole())) return "GUIDE";
        if (ClientUI.loggedInUser.getUserType() == Common.LoginResponse.UserType.SUBSCRIBER) return "SUBSCRIBER";
        return "VISITOR";
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}