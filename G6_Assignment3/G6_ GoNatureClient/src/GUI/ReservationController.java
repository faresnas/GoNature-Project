package GUI;

import Client.ClientUI;
import data.Reservation;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

import java.sql.Date;
import java.sql.Time;

public class ReservationController {

    @FXML
    private ComboBox<String> parkBox;

    @FXML
    private TextField dateField;

    @FXML
    private TextField timeField;

    @FXML
    private TextField visitorsField;

    @FXML
    private TextField emailField;

    @FXML
    public void initialize() {
        parkBox.getItems().clear();
        parkBox.getItems().addAll("1", "2", "3");
    }

    @FXML
    void submitReservation(ActionEvent event) {
        try {
            if (parkBox.getValue() == null || dateField.getText().isEmpty() || 
                timeField.getText().isEmpty() || visitorsField.getText().isEmpty() || 
                emailField.getText().isEmpty()) {
                
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Missing Fields");
                alert.setHeaderText(null);
                alert.setContentText("Please fill in all the fields in the reservation form.");
                alert.showAndWait();
                return;
            }

            int numVisitors = Integer.parseInt(visitorsField.getText());

            Reservation r = new Reservation();

            if (ClientUI.loggedInUser != null) {
                r.setTravelerId(ClientUI.loggedInUser.getUserId());
                
                if (ClientUI.loggedInUser.getRole() != null && ClientUI.loggedInUser.getRole().equals("GUIDE")) {
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
                    // הגנה מפני ערך null עבור מטיילים/מנויים רגילים שאין להם תפקיד עובד
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

            r.setParkId(Integer.parseInt(parkBox.getValue()));
            r.setVisitDate(Date.valueOf(dateField.getText())); 
            r.setEntryTime(Time.valueOf(timeField.getText())); 
            r.setNumVisitors(numVisitors);
            r.setEmail(emailField.getText());
            r.setStatus("PENDING"); 
            r.setPrepaid(false);

            ClientUI.client.createReservation(r);

        } catch (IllegalArgumentException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Format Error");
            alert.setHeaderText("Invalid Data Formats Entered");
            alert.setContentText("Please ensure the date is in YYYY-MM-DD format and the time is in HH:MM:SS format (e.g., 14:30:00).");
            alert.showAndWait();
        } catch (Exception e) {
            System.out.println("Reservation submission failed");
            e.printStackTrace();
        }
    }

    @FXML
    void goBack(ActionEvent event) {
        try {
            String screen;

            if (ClientUI.loggedInUser != null && "GUIDE".equals(ClientUI.loggedInUser.getRole())) {
                screen = "/GUI/GuideDashboard.fxml";
            } else {
                screen = "/GUI/VisitorDashboard.fxml";
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(screen));
            Scene scene = new Scene(loader.load());
            ClientUI.primaryStage.setScene(scene);

        } catch (Exception e) {
            System.out.println("Failed to redirect back to dashboard");
            e.printStackTrace();
        }
    }
}