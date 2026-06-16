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

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;

public class ReservationController {

    @FXML private ComboBox<String> parkBox;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> timeBox;
    @FXML private Spinner<Integer> visitorsSpinner;
    @FXML private TextField emailField;

    private HashMap<String, Integer> parkIdMap = new HashMap<>();

    @FXML
    public void initialize() {
        OrderClient.reservationController = this;

        // Load time slots 08:00 to 18:00
        for (int h = 8; h <= 18; h++) {
            timeBox.getItems().add(String.format("%02d:00:00", h));
            if (h < 18) timeBox.getItems().add(String.format("%02d:30:00", h));
        }
        timeBox.getSelectionModel().selectFirst();

        // Visitors spinner 1–50
        visitorsSpinner.setValueFactory(
            new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 50, 1)
        );

        // Request parks from server
        parkBox.getItems().clear();
        ClientUI.client.requestParks();

        // Force ComboBox text to show correctly in dark theme
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
            // Empty field check
            if (parkBox.getValue() == null || datePicker.getValue() == null ||
                    timeBox.getValue() == null || emailField.getText().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Missing Fields");
                alert.setHeaderText(null);
                alert.setContentText("Please fill in all the fields in the reservation form.");
                alert.showAndWait();
                return;
            }

            // Past date check
            LocalDate selectedDate = datePicker.getValue();
            if (selectedDate.isBefore(LocalDate.now())) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Invalid Date");
                alert.setHeaderText(null);
                alert.setContentText("Visit date cannot be in the past. Please choose a future date.");
                alert.showAndWait();
                return;
            }

            // Email format check
            if (!emailField.getText().matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$")) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Invalid Email");
                alert.setHeaderText(null);
                alert.setContentText("Please enter a valid email address.");
                alert.showAndWait();
                return;
            }

            int numVisitors = visitorsSpinner.getValue();

            Reservation r = new Reservation();

            if (ClientUI.loggedInUser != null) {
                r.setTravelerId(ClientUI.loggedInUser.getUserId());

                if ("GUIDE".equals(ClientUI.loggedInUser.getRole())) {
                    if (numVisitors > 15) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Group Limit Exceeded");
                        alert.setHeaderText("Invalid Number of Visitors");
                        alert.setContentText("An organized group reservation is limited to a maximum of 15 participants.");
                        alert.showAndWait();
                        return;
                    }
                    r.setTravelerType("GUIDE");
                    r.setType("GROUP");
                } else {
                    if (ClientUI.loggedInUser.getRole() != null && !ClientUI.loggedInUser.getRole().isEmpty()) {
                        r.setTravelerType(ClientUI.loggedInUser.getRole());
                    } else {
                        r.setTravelerType("VISITOR");
                    }
                    r.setType("INDIVIDUAL");
                }
            } else {
                r.setTravelerId(111111111);
                r.setTravelerType("VISITOR");
                r.setType("INDIVIDUAL");
            }

            r.setParkId(parkIdMap.get(parkBox.getValue()));
            r.setVisitDate(Date.valueOf(selectedDate));
            r.setEntryTime(Time.valueOf(timeBox.getValue()));
            r.setNumVisitors(numVisitors);
            r.setEmail(emailField.getText());
            r.setStatus("PENDING");
            r.setPrepaid(false);

            ClientUI.client.createReservation(r);

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Something went wrong. Please check your inputs and try again.");
            alert.showAndWait();
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
            System.out.println("Failed to redirect back to dashboard");
            e.printStackTrace();
        }
    }
}