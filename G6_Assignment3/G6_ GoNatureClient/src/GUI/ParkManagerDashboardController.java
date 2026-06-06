package GUI;

import Client.ClientUI;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ParkManagerDashboardController {

    @FXML private Label lblWelcome;

    @FXML
    public void initialize() {
        if (ClientUI.loggedInUser != null) {
            lblWelcome.setText("Welcome, " + ClientUI.loggedInUser.getFirstName()
                + " " + ClientUI.loggedInUser.getLastName());
        }
    }

    @FXML void viewVisitorCount(ActionEvent event) { /* TODO: F4 — visitor count */ }
    @FXML void visitsReport(ActionEvent event)     { /* TODO: F5 — visits report */ }
    @FXML void usageReport(ActionEvent event)      { /* TODO: F5 — usage report */ }
    @FXML void updateParkParams(ActionEvent event) { /* TODO: F6 — update park params */ }
    @FXML void logout(ActionEvent event)           { LogoutHelper.logout(); }
    @FXML void exit(ActionEvent event) {
        if (ClientUI.client != null) ClientUI.client.disconnect();
        System.exit(0);
    }
}