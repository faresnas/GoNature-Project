package GUI;

import Client.ClientUI;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ParkWorkerDashboardController {

    @FXML private Label lblWelcome;

    @FXML
    public void initialize() {
        if (ClientUI.loggedInUser != null) {
            lblWelcome.setText("Welcome, " + ClientUI.loggedInUser.getFirstName()
                + " " + ClientUI.loggedInUser.getLastName());
        }
    }

    @FXML void parkEntry(ActionEvent event)        { /* TODO: F4 — park entry */ }
    @FXML void parkExit(ActionEvent event)         { /* TODO: F4 — park exit */ }
    @FXML void viewVisitorCount(ActionEvent event) { /* TODO: F4 — visitor count */ }
    @FXML void logout(ActionEvent event)           { LogoutHelper.logout(); }
    @FXML void exit(ActionEvent event) {
        if (ClientUI.client != null) ClientUI.client.disconnect();
        System.exit(0);
    }
}