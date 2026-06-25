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
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.application.Platform;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;

/**
 * Controller class for the {@code MyReservations.fxml} view.
 * <p>
 * This class manages the traveler's personal reservation dashboard inside the GoNature system.
 * It provides a comprehensive matrix showing all historical and future bookings, dynamically colors 
 * order statuses inside the table cells, and allows users to update parameters (date, time, group volume) 
 * or delete existing reservations. Front-end constraints ensure users don't reschedule bookings to 
 * past dates or choose visitor counts exceeding business bounds.
 * </p>
 *
 * @author GoNature Development Team
 * @version 1.0
 */
public class MyReservationsController {

    /** TableView layout container indexing the raw text matrices describing active reservations. */
    @FXML private TableView<ArrayList<String>> reservationsTable;
    
    /** Column mapping the unique structural internal identification key of the reservation. */
    @FXML private TableColumn<ArrayList<String>, String> idCol;
    
    /** Column mapping the legal operating name of the destination nature park. */
    @FXML private TableColumn<ArrayList<String>, String> parkCol;
    
    /** Column mapping the scheduled arrival calendar date string. */
    @FXML private TableColumn<ArrayList<String>, String> dateCol;
    
    /** Column mapping the exact timed target entry arrival hour string. */
    @FXML private TableColumn<ArrayList<String>, String> timeCol;
    
    /** Column mapping the cumulative headcount joining the visiting party. */
    @FXML private TableColumn<ArrayList<String>, String> visitorsCol;
    
    /** Column mapping pricing classification models or order generation categories. */
    @FXML private TableColumn<ArrayList<String>, String> typeCol;
    
    /** Column mapping the status state descriptor (e.g., PENDING, CONFIRMED, CANCELLED). */
    @FXML private TableColumn<ArrayList<String>, String> statusCol;
    
    /** Column mapping the verification or tracking code generated for entry clearance. */
    @FXML private TableColumn<ArrayList<String>, String> codeCol;

    /** DatePicker input tool regulating adjustments targeting appointment calendar dates. */
    @FXML private DatePicker datePicker;
    
    /** ComboBox element providing a fixed range of operating arrival operational timeframes. */
    @FXML private ComboBox<String> timeBox;
    
    /** Spinner component tracking group visitor volume headcounts. */
    @FXML private Spinner<Integer> visitorsSpinner;

    /** Local memory data wrapper observing and synchronizing structural modifications made over the table row elements. */
    private ObservableList<ArrayList<String>> tableData = FXCollections.observableArrayList();

    /**
     * Initializes the view elements automatically upon construction of the FXML scene graph.
     * <p>
     * Connects cell factor mapping properties, injects color rules to highlight discrete booking status paths, 
     * builds default operational hour choices (08:00 to 18:30), and attaches row selection listening monitors 
     * to populate modification controls when an existing row is clicked.
     * </p>
     */
    @FXML
    public void initialize() {
        OrderClient.myReservationsController = this;

        idCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(0)));
        parkCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(1)));
        dateCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(2)));
        timeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(3)));
        visitorsCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(4)));
        typeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(5)));
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(6)));
        codeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(7)));

        statusCol.setCellFactory(col -> new TableCell<ArrayList<String>, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    switch (status) {
                        case "PENDING":
                            setStyle("-fx-text-fill: #aaaaaa; -fx-font-weight: bold;");
                            break;
                        case "CONFIRMED":
                            setStyle("-fx-text-fill: #4caf50; -fx-font-weight: bold;");
                            break;
                        case "CANCELLED":
                            setStyle("-fx-text-fill: #f44336; -fx-font-weight: bold;");
                            break;
                        case "INSIDE":
                            setStyle("-fx-text-fill: #00bcd4; -fx-font-weight: bold;");
                            break;
                        case "EXITED":
                            setStyle("-fx-text-fill: #9e9e9e; -fx-font-weight: bold;");
                            break;
                        case "NO_SHOW":
                            setStyle("-fx-text-fill: #ff9800; -fx-font-weight: bold;");
                            break;
                        default:
                            setStyle("-fx-text-fill: white;");
                            break;
                    }
                }
            }
        });

        for (int h = 8; h <= 18; h++) {
            timeBox.getItems().add(String.format("%02d:00:00", h));
            if (h < 18) timeBox.getItems().add(String.format("%02d:30:00", h));
        }
        timeBox.getSelectionModel().selectFirst();

        timeBox.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item);
                setStyle("-fx-text-fill: white; -fx-background-color: #1e4a1e;");
            }
        });

        visitorsSpinner.setValueFactory(
            new SpinnerValueFactory.IntegerSpinnerValueFactory(Integer.MIN_VALUE, Integer.MAX_VALUE, 1)
        );
        visitorsSpinner.setEditable(true);

        reservationsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                try {
                    datePicker.setValue(LocalDate.parse(newVal.get(2)));
                } catch (Exception e) {
                    datePicker.setValue(null);
                }
                String rowTime = newVal.get(3);
                if (timeBox.getItems().contains(rowTime)) {
                    timeBox.setValue(rowTime);
                } else {
                    timeBox.getSelectionModel().selectFirst();
                }
                visitorsSpinner.getEditor().setText(newVal.get(4));
            }
        });

        refreshTable();
    }

    /**
     * Determines the pricing and profile classification string corresponding to the current session details.
     *
     * @return A category string identifying the user context ("GUIDE", "SUBSCRIBER", or "VISITOR").
     */
    private String getTravelerType() {
        if (ClientUI.loggedInUser == null) return "VISITOR";
        if ("GUIDE".equals(ClientUI.loggedInUser.getRole())) return "GUIDE";
        if (ClientUI.loggedInUser.getUserType() == Common.LoginResponse.UserType.SUBSCRIBER) return "SUBSCRIBER";
        return "VISITOR";
    }

    /**
     * Pulls the history list of bookings from the server for the currently active traveler profile.
     * Fallback queries are dispatched utilizing default mock parameters if a user context is absent.
     */
    private void refreshTable() {
        if (ClientUI.loggedInUser != null) {
            int id = ClientUI.loggedInUser.getUserId();
            Platform.runLater(() -> ClientUI.client.requestMyReservations(id, getTravelerType()));
        } else {
            ClientUI.client.requestMyReservations(111111111, "VISITOR");
        }
    }

    /**
     * Re-renders the graphical table item mapping with fresh data arrays supplied from server results.
     * Called asynchronously from network connection update handlers.
     *
     * @param rows Multi-dimensional sequence list containing fresh database reservation fields.
     */
    public void setReservationsTable(ArrayList<ArrayList<String>> rows) {
        tableData.clear();
        tableData.addAll(rows);
        reservationsTable.setItems(tableData);
    }

    /**
     * Evaluates modification fields, performs data bounds checks, and forwards an 
     * adjustment command to the server socket to update the selected reservation.
     *
     * @param event Action trigger originating from interacting with the update button.
     */
    @FXML
    void handleUpdateReservation(ActionEvent event) {
        ArrayList<String> selectedRow = reservationsTable.getSelectionModel().getSelectedItem();

        if (selectedRow == null) {
            showAlert(Alert.AlertType.WARNING, "Selection Missing",
                "Please select a reservation from the table to update.");
            return;
        }

        if ("CANCELLED".equals(selectedRow.get(6))) {
            showAlert(Alert.AlertType.ERROR, "Cannot Edit",
                "This reservation has been cancelled and cannot be edited.");
            return;
        }

        if (datePicker.getValue() == null || timeBox.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Missing Information",
                "Please fill in all the edit fields before updating.");
            return;
        }

        String rawVisitors = visitorsSpinner.getEditor().getText().trim();
        int newVisitors;
        try {
            newVisitors = Integer.parseInt(rawVisitors);
        } catch (NumberFormatException e) {
            newVisitors = 0;
        }

        if (newVisitors < 1) {
            showAlert(Alert.AlertType.ERROR, "Invalid Number of Visitors",
                "Number of visitors must be at least 1.");
            return;
        }

        if (newVisitors > 15) {
            showAlert(Alert.AlertType.ERROR, "Invalid Number of Visitors",
                "Maximum number of visitors is 15.");
            return;
        }

        LocalDate selectedDate = datePicker.getValue();
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        if (selectedDate.isBefore(today)) {
            showAlert(Alert.AlertType.ERROR, "Invalid Date",
                "Visit date cannot be in the past. Please choose today or a future date.");
            return;
        }

        LocalTime selectedTime = LocalTime.parse(timeBox.getValue().substring(0, 5));
        if (selectedDate.isEqual(today) && selectedTime.isBefore(now.plusHours(1))) {
            showAlert(Alert.AlertType.ERROR, "Invalid Time",
                "For same-day reservations, the visit time must be at least 1 hour from now.");
            return;
        }

        int reservationId = Integer.parseInt(selectedRow.get(0));
        String newDate = selectedDate.toString();
        String newTime = timeBox.getValue();

        ClientUI.client.updateReservation(reservationId, newDate, newTime, newVisitors);
        javafx.application.Platform.runLater(() -> refreshTable());
    }

    /**
     * Transmits a removal command payload to delete the selected reservation row from the server database registry.
     * It optimistically changes row elements status markers locally to prevent duplicate click submissions.
     *
     * @param event Action trigger event mapping to the reservation delete/cancel button.
     */
    @FXML
    void handleDeleteReservation(ActionEvent event) {
        ArrayList<String> selectedRow = reservationsTable.getSelectionModel().getSelectedItem();

        if (selectedRow == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection",
                "Please select a reservation from the table to delete.");
            return;
        }

        if ("CANCELLED".equals(selectedRow.get(6))) {
            showAlert(Alert.AlertType.ERROR, "Already Cancelled",
                "This reservation is already cancelled.");
            return;
        }

        int reservationId = Integer.parseInt(selectedRow.get(0));
        int travelerId = ClientUI.loggedInUser.getUserId();

        selectedRow.set(6, "CANCELLED");
        selectedRow.set(7, "—");
        reservationsTable.refresh();

        ClientUI.client.deleteReservation(reservationId, travelerId, getTravelerType());
    }

    /**
     * Dispatches an explicit affirmation message responding to a system visit alert verification sequence.
     *
     * @param event Action event mapped to the confirmation trigger layout control element.
     */
    @FXML
    void handleConfirmReminder(ActionEvent event) {
        ArrayList<String> selectedRow = reservationsTable.getSelectionModel().getSelectedItem();

        if (selectedRow == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a reservation.");
            return;
        }

        if (!"CONFIRMED".equals(selectedRow.get(6))) {
            showAlert(Alert.AlertType.ERROR, "Invalid Reservation",
                "Only reservations awaiting confirmation can be confirmed.");
            return;
        }

        int reservationId = Integer.parseInt(selectedRow.get(0));
        int travelerId = ClientUI.loggedInUser.getUserId();
        ClientUI.client.confirmReminder(reservationId, travelerId, getTravelerType());
        refreshTable();
    }

    /**
     * Shifts the primary active stage frame scenery back to the user role's primary dashboard menu view.
     *
     * @param event Action navigation interaction trigger signal.
     */
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

    /**
     * Internal framework utility helping construct and present standardized structural Alert popups to the customer.
     *
     * @param type    Programmatic category setting error or validation alert thresholds.
     * @param title   Main header label text applied inside the prompt layout banner region.
     * @param content Dynamic body narrative explaining validation failures or processing updates.
     */
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}