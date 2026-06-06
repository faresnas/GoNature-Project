package GUI;

import Client.ClientUI;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class VisitorDashboardController {

    @FXML private Label lblWelcome;

    @FXML
    public void initialize() {
        if (ClientUI.loggedInUser != null) {
            lblWelcome.setText("Welcome, " + ClientUI.loggedInUser.getFirstName()
                + " " + ClientUI.loggedInUser.getLastName());
        }
    }

    @FXML void makeReservation(ActionEvent event) { /* TODO: F2 — make reservation */ }
    @FXML void myReservations(ActionEvent event)  { /* TODO: F2 — view my reservations */ }
    @FXML void logout(ActionEvent event)          { LogoutHelper.logout(); }
    @FXML void exit(ActionEvent event) {
        if (ClientUI.client != null) ClientUI.client.disconnect();
        System.exit(0);
    }
}