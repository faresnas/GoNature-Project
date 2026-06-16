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
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.time.LocalDate;
import java.util.ArrayList;

public class MyReservationsController {

    @FXML private TableView<ArrayList<String>> reservationsTable;
    @FXML private TableColumn<ArrayList<String>, String> idCol;
    @FXML private TableColumn<ArrayList<String>, String> parkCol;
    @FXML private TableColumn<ArrayList<String>, String> dateCol;
    @FXML private TableColumn<ArrayList<String>, String> timeCol;
    @FXML private TableColumn<ArrayList<String>, String> visitorsCol;
    @FXML private TableColumn<ArrayList<String>, String> typeCol;
    @FXML private TableColumn<ArrayList<String>, String> statusCol;
    @FXML private TableColumn<ArrayList<String>, String> codeCol;

    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> timeBox;
    @FXML private Spinner<Integer> visitorsSpinner;

    private ObservableList<ArrayList<String>> tableData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        OrderClient.myReservationsController = this;

        // Table column bindings
        idCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(0)));
        parkCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(1)));
        dateCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(2)));
        timeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(3)));
        visitorsCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(4)));
        typeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(5)));
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(6)));
        codeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(7)));

        // Time slots
        for (int h = 8; h <= 18; h++) {
            timeBox.getItems().add(String.format("%02d:00:00", h));
            if (h < 18) timeBox.getItems().add(String.format("%02d:30:00", h));
        }
        timeBox.getSelectionModel().selectFirst();

        // Fix dark theme text visibility
        timeBox.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item);
                setStyle("-fx-text-fill: white; -fx-background-color: #1e4a1e;");
            }
        });

        // Visitors spinner
        visitorsSpinner.setValueFactory(
            new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 50, 1)
        );

        // Auto-fill edit fields when row selected
        reservationsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                try {
                    datePicker.setValue(LocalDate.parse(newVal.get(2)));
                } catch (Exception e) {
                    datePicker.setValue(null);
                }
                // Match selected time to dropdown
                String rowTime = newVal.get(3);
                if (timeBox.getItems().contains(rowTime)) {
                    timeBox.setValue(rowTime);
                } else {
                    timeBox.getSelectionModel().selectFirst();
                }
                try {
                    visitorsSpinner.getValueFactory().setValue(Integer.parseInt(newVal.get(4)));
                } catch (Exception e) {
                    visitorsSpinner.getValueFactory().setValue(1);
                }
            }
        });

        refreshTable();
    }

    private void refreshTable() {
        if (ClientUI.loggedInUser != null) {
            int id = ClientUI.loggedInUser.getUserId();
            String role = ClientUI.loggedInUser.getRole();
            String travelerType = (role != null && role.equals("GUIDE")) ? "GUIDE" : "VISITOR";
            ClientUI.client.requestMyReservations(id, travelerType);
        } else {
            ClientUI.client.requestMyReservations(111111111, "VISITOR");
        }
    }

    public void setReservationsTable(ArrayList<ArrayList<String>> rows) {
        tableData.clear();
        tableData.addAll(rows);
        reservationsTable.setItems(tableData);
    }

    @FXML
    void handleUpdateReservation(ActionEvent event) {
        ArrayList<String> selectedRow = reservationsTable.getSelectionModel().getSelectedItem();

        if (selectedRow == null) {
            showAlert(Alert.AlertType.WARNING, "Selection Missing", "Please select a reservation from the table to update.");
            return;
        }

        if ("CANCELLED".equals(selectedRow.get(6))) {
            showAlert(Alert.AlertType.ERROR, "Cannot Edit", "This reservation has been cancelled and cannot be edited.");
            return;
        }

        if (datePicker.getValue() == null || timeBox.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Missing Information", "Please fill in all the edit fields before updating.");
            return;
        }

        // Past date check
        if (datePicker.getValue().isBefore(LocalDate.now())) {
            showAlert(Alert.AlertType.ERROR, "Invalid Date", "Visit date cannot be in the past. Please choose a future date.");
            return;
        }

        int newVisitors = visitorsSpinner.getValue();

        // Group limit check
        if ("GROUP".equals(selectedRow.get(5)) && newVisitors > 15) {
            showAlert(Alert.AlertType.ERROR, "Group Limit Exceeded", "Group reservations cannot exceed 15 visitors.");
            return;
        }

        int reservationId = Integer.parseInt(selectedRow.get(0));
        String newDate = datePicker.getValue().toString();
        String newTime = timeBox.getValue();

        ClientUI.client.updateReservation(reservationId, newDate, newTime, newVisitors);
        refreshTable();
    }

    @FXML
    void handleDeleteReservation(ActionEvent event) {
        ArrayList<String> selectedRow = reservationsTable.getSelectionModel().getSelectedItem();

        if (selectedRow == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a reservation from the table to delete.");
            return;
        }

        int reservationId = Integer.parseInt(selectedRow.get(0));
        int travelerId = ClientUI.loggedInUser.getUserId();
        String travelerType = ("GUIDE".equals(ClientUI.loggedInUser.getRole())) ? "GUIDE" : "VISITOR";

        ClientUI.client.deleteReservation(reservationId, travelerId, travelerType);
        refreshTable();
    }

    @FXML
    void handleGoBack(ActionEvent event) {
        try {
            String screen = "GUIDE".equals(
                ClientUI.loggedInUser != null ? ClientUI.loggedInUser.getRole() : ""
            ) ? "/GUI/GuideDashboard.fxml" : "/GUI/VisitorDashboard.fxml";

            FXMLLoader loader = new FXMLLoader(getClass().getResource(screen));
            Scene scene = new Scene(loader.load());
            ClientUI.primaryStage.setScene(scene);
        } catch (Exception e) {
            System.out.println("Failed to redirect back to dashboard");
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