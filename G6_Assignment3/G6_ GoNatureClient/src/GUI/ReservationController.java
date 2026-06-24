package GUI;

import Client.ClientUI;
import Client.OrderClient;
import data.Reservation;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.application.Platform;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;

public class ReservationController {

    @FXML private ComboBox<String> parkBox;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> timeBox;
    @FXML private Spinner<Integer> visitorsSpinner;
    @FXML private TextField emailField;
    @FXML private javafx.scene.control.CheckBox prepaidCheck;
    @FXML private javafx.scene.layout.VBox prepaidBox;
    private Reservation lastReservationAttempt;
    @FXML private javafx.scene.control.Label availabilityLabel;

    private HashMap<String, Integer> parkIdMap = new HashMap<>();

    @FXML
    public void initialize() {
        OrderClient.reservationController = this;

        for (int h = 8; h <= 18; h++) {
            timeBox.getItems().add(String.format("%02d:00:00", h));
            if (h < 18) timeBox.getItems().add(String.format("%02d:30:00", h));
        }
        timeBox.getSelectionModel().selectFirst();

        // No min/max — we validate manually
        visitorsSpinner.setValueFactory(
            new SpinnerValueFactory.IntegerSpinnerValueFactory(Integer.MIN_VALUE, Integer.MAX_VALUE, 1)
        );
        visitorsSpinner.setEditable(true);

        parkBox.getItems().clear();
        OrderClient.lastCommand = "GET_PARKS";
        Platform.runLater(() -> ClientUI.client.requestParks());

        if (ClientUI.loggedInUser != null && "GUIDE".equals(ClientUI.loggedInUser.getRole())) {
            prepaidBox.setVisible(true);
            prepaidBox.setManaged(true);
        }

        timeBox.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item);
                setStyle("-fx-text-fill: white;");
            }
        });

        parkBox.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item);
                setStyle("-fx-text-fill: white;");
            }
        });
        parkBox.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> checkAvailability());
        datePicker.valueProperty().addListener((obs, old, newVal) -> checkAvailability());
        timeBox.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> checkAvailability());
    }
    
    public void refreshAvailability() {
        checkAvailability();
    }
    private void checkAvailability() {
        if (parkBox.getValue() == null || datePicker.getValue() == null || timeBox.getValue() == null) {
            availabilityLabel.setText("");
            return;
        }
        Integer parkId = parkIdMap.get(parkBox.getValue());
        if (parkId == null) return;

        String date = datePicker.getValue().toString();
        String time = timeBox.getValue();

        OrderClient.lastCommand = "CHECK_AVAILABILITY";
        ArrayList<Object> data = new ArrayList<>();
        data.add(parkId);
        data.add(date);
        data.add(time);
        try {
            ClientUI.client.sendToServer(new Common.Chat("CHECK_AVAILABILITY", data));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setAvailability(int booked, int available) {
        Platform.runLater(() -> {
            if (available <= 0) {
                availabilityLabel.setText("⚠️ Park is FULL for this slot — " + booked + " booked");
                availabilityLabel.setStyle("-fx-text-fill: #ff6b6b; -fx-font-size: 12; -fx-padding: 6 10; -fx-background-color: #0f2210; -fx-background-radius: 6;");
            } else {
                availabilityLabel.setText("✅ Available spots: " + available + "  |  Booked: " + booked);
                availabilityLabel.setStyle("-fx-text-fill: #7ec87e; -fx-font-size: 12; -fx-padding: 6 10; -fx-background-color: #0f2210; -fx-background-radius: 6;");
            }
        });
    }

    public void populateParks(ArrayList<ArrayList<String>> parks) {
        parkIdMap.clear();
        parkBox.getItems().clear();
        for (ArrayList<String> row : parks) {
            String parkId = row.get(0);
            String parkName = row.get(1);
            parkIdMap.put(parkName, Integer.parseInt(parkId));
            parkBox.getItems().add(parkName);
        }
    }

    @FXML
    void submitReservation(ActionEvent event) {
        try {
            // READ RAW TEXT FIRST before factory touches it
            String rawVisitors = visitorsSpinner.getEditor().getText().trim();
            int numVisitors;
            try {
                numVisitors = Integer.parseInt(rawVisitors);
            } catch (NumberFormatException e) {
                numVisitors = 0;
            }

            if (numVisitors < 1) {
                showAlert(Alert.AlertType.ERROR, "Invalid Number of Visitors",
                    "Number of visitors must be at least 1.");
                return;
            }

            if (numVisitors > 15) {
                showAlert(Alert.AlertType.ERROR, "Invalid Number of Visitors",
                    "Maximum number of visitors is 15.");
                return;
            }

            // Empty field check
            if (parkBox.getValue() == null || datePicker.getValue() == null ||
                    timeBox.getValue() == null || emailField.getText().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Missing Fields",
                    "Please fill in all the fields in the reservation form.");
                return;
            }

            LocalDate selectedDate = datePicker.getValue();
            LocalTime selectedTime = LocalTime.parse(timeBox.getValue().substring(0, 5));
            LocalDate today = LocalDate.now();
            LocalTime now = LocalTime.now();

            if (selectedDate.isBefore(today)) {
                showAlert(Alert.AlertType.ERROR, "Invalid Date",
                    "Visit date cannot be in the past.");
                return;
            }

            // Must be at least 24 hours from now
            java.time.LocalDateTime selectedDateTime = java.time.LocalDateTime.of(selectedDate, selectedTime);
            java.time.LocalDateTime minDateTime = java.time.LocalDateTime.now().plusHours(24);

            if (selectedDateTime.isBefore(minDateTime)) {
                showAlert(Alert.AlertType.ERROR, "Invalid Date/Time",
                    "Reservations must be made at least 24 hours in advance.\n"    );
                return;
            }

            if (!emailField.getText().matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,4}$")) {
                showAlert(Alert.AlertType.ERROR, "Invalid Email",
                    "Please enter a valid email address (e.g. name@example.com).");
                return;
            }

            Reservation r = new Reservation();

            if (ClientUI.loggedInUser != null) {
                r.setTravelerId(ClientUI.loggedInUser.getUserId());

                String userType = ClientUI.loggedInUser.getUserType() != null
                                  ? ClientUI.loggedInUser.getUserType().toString()
                                  : "";

                if ("GUIDE".equals(ClientUI.loggedInUser.getRole())) {
                    r.setTravelerType("GUIDE");
                    r.setType("GROUP");
                    r.setPrepaid(prepaidCheck != null && prepaidCheck.isSelected());
                } else if ("SUBSCRIBER".equals(userType)) {
                    r.setTravelerType("SUBSCRIBER");
                    r.setType("INDIVIDUAL");
                    r.setPrepaid(false);
                } else {
                    r.setTravelerType("VISITOR");
                    r.setType("INDIVIDUAL");
                    r.setPrepaid(false);
                }
            } else {
                r.setTravelerId(111111111);
                r.setTravelerType("VISITOR");
                r.setType("INDIVIDUAL");
                r.setPrepaid(false);
            }

            r.setParkId(parkIdMap.get(parkBox.getValue()));
            r.setVisitDate(Date.valueOf(selectedDate));
            r.setEntryTime(Time.valueOf(timeBox.getValue()));
            r.setNumVisitors(numVisitors);
            r.setEmail(emailField.getText());
            r.setStatus("PENDING");

            lastReservationAttempt = r;
            ClientUI.client.createReservation(r);

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error",
                "Something went wrong. Please check your inputs and try again.");
            e.printStackTrace();
        }
    }

    @FXML
    void goBack(ActionEvent event) {
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

    public void joinWaitingList() {
        if (lastReservationAttempt != null) {
            ClientUI.client.joinWaitingList(lastReservationAttempt);
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