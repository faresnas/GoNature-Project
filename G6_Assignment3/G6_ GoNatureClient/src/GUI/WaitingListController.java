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

/**
 * Controller for the Waiting List screen.
 * This class allows visitors, subscribers, and guides
 * to view their waiting list entries and leave the waiting list
 * if the reservation has not yet been confirmed.
 */
public class WaitingListController {

    /** Table displaying all waiting list entries. */
    @FXML
    private TableView<ArrayList<String>> waitingTable;

    /** Column displaying the waiting list entry ID. */
    @FXML
    private TableColumn<ArrayList<String>, String> idCol;

    /** Column displaying the park name. */
    @FXML
    private TableColumn<ArrayList<String>, String> parkCol;

    /** Column displaying the reservation date. */
    @FXML
    private TableColumn<ArrayList<String>, String> dateCol;

    /** Column displaying the reservation time. */
    @FXML
    private TableColumn<ArrayList<String>, String> timeCol;

    /** Column displaying the number of visitors. */
    @FXML
    private TableColumn<ArrayList<String>, String> visitorsCol;

    /** Column displaying the visitor's position in the waiting list. */
    @FXML
    private TableColumn<ArrayList<String>, String> positionCol;

    /** Column displaying the waiting list status. */
    @FXML
    private TableColumn<ArrayList<String>, String> statusCol;

    /** Observable list containing the waiting list entries displayed in the table. */
    private ObservableList<ArrayList<String>> tableData =
            FXCollections.observableArrayList();

    /**
     * Initializes the screen after the FXML file is loaded.
     * Configures the table columns, registers this controller,
     * and requests the user's waiting list from the server.
     */
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

        javafx.application.Platform.runLater(this::loadWaitingList);
    }

    /**
     * Requests the current user's waiting list entries from the server.
     */
    private void loadWaitingList() {

        if (ClientUI.loggedInUser == null) {
            return;
        }

        int travelerId = ClientUI.loggedInUser.getUserId();
        String travelerType = getTravelerType();

        OrderClient.lastCommand = "GET_WAITING_LIST";

        ArrayList<Object> data = new ArrayList<>();
        data.add(travelerId);
        data.add(travelerType);

        ClientUI.client.sendToServer(new Chat("GET_WAITING_LIST", data));
    }

    /**
     * Updates the waiting list table with data received from the server.
     *
     * @param rows the waiting list entries received from the server
     */
    public void setWaitingListTable(ArrayList<ArrayList<String>> rows) {
        tableData.clear();
        tableData.addAll(rows);
        waitingTable.setItems(tableData);
    }

    /**
     * Removes the selected waiting list entry.
     * Confirmed reservations cannot be removed.
     *
     * @param event the button click event
     */
    @FXML
    void handleLeave(ActionEvent event) {

        ArrayList<String> selected =
                waitingTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert(Alert.AlertType.WARNING,
                    "No Selection",
                    "Please select a waiting list entry to leave.");
            return;
        }

        if ("CONFIRMED".equals(selected.get(6))) {
            showAlert(Alert.AlertType.ERROR,
                    "Cannot Leave",
                    "This entry has already been confirmed as a reservation.");
            return;
        }

        int waitingId = Integer.parseInt(selected.get(0));

        OrderClient.lastCommand = "LEAVE_WAITING_LIST";
        ClientUI.client.sendToServer(
                new Chat("LEAVE_WAITING_LIST", waitingId));
    }

    /**
     * Handles the server response after leaving the waiting list.
     *
     * @param result true if the user was removed successfully,
     *               otherwise false
     */
    public void handleLeaveResponse(boolean result) {

        if (result) {
            showAlert(Alert.AlertType.INFORMATION,
                    "Left Waiting List",
                    "You have been removed from the waiting list.");

            loadWaitingList();

        } else {
            showAlert(Alert.AlertType.ERROR,
                    "Failed",
                    "Failed to leave waiting list. Please try again.");
        }
    }

    /**
     * Returns the user to the appropriate dashboard
     * according to the logged-in user's role.
     *
     * @param event the button click event
     */
    @FXML
    void handleBack(ActionEvent event) {

        try {

            String screen =
                    "GUIDE".equals(
                            ClientUI.loggedInUser != null
                                    ? ClientUI.loggedInUser.getRole()
                                    : "")
                            ? "/GUI/GuideDashboard.fxml"
                            : "/GUI/VisitorDashboard.fxml";

            FXMLLoader loader =
                    new FXMLLoader(getClass().getResource(screen));

            Scene scene = new Scene(loader.load());
            ClientUI.primaryStage.setScene(scene);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Determines the traveler type of the logged-in user.
     *
     * @return the traveler type (VISITOR, SUBSCRIBER, or GUIDE)
     */
    private String getTravelerType() {

        if (ClientUI.loggedInUser == null) {
            return "VISITOR";
        }

        if ("GUIDE".equals(ClientUI.loggedInUser.getRole())) {
            return "GUIDE";
        }

        if (ClientUI.loggedInUser.getUserType()
                == Common.LoginResponse.UserType.SUBSCRIBER) {
            return "SUBSCRIBER";
        }

        return "VISITOR";
    }

    /**
     * Displays an alert dialog with the specified message.
     *
     * @param type the type of alert
     * @param title the alert window title
     * @param content the message displayed to the user
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