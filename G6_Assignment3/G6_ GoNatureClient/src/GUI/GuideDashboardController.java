package GUI;

import Client.ClientUI;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class GuideDashboardController {

    @FXML private Label lblWelcome;

    @FXML
    public void initialize() {
        if (ClientUI.loggedInUser != null) {
            lblWelcome.setText("Welcome, " + ClientUI.loggedInUser.getFirstName());
        }
    }

    @FXML void makeReservation(ActionEvent event) { /* TODO: F2 — make group reservation */ }
    @FXML void myReservations(ActionEvent event)  { /* TODO: F2 — view my reservations */ }
    @FXML void logout(ActionEvent event)          { LogoutHelper.logout(); }
    @FXML void exit(ActionEvent event) {
        if (ClientUI.client != null) ClientUI.client.disconnect();
        System.exit(0);
    }
}