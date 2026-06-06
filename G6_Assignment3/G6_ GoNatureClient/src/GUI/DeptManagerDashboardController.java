package GUI;

import Client.ClientUI;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class DeptManagerDashboardController {

    @FXML private Label lblWelcome;

    @FXML
    public void initialize() {
        if (ClientUI.loggedInUser != null) {
            lblWelcome.setText("Welcome, " + ClientUI.loggedInUser.getFirstName()
                + " " + ClientUI.loggedInUser.getLastName());
        }
    }

    @FXML void viewAllVisitorCount(ActionEvent event) { /* TODO: F4 — all parks visitor count */ }
    @FXML void visitsReport(ActionEvent event)        { /* TODO: F5 — visits report all parks */ }
    @FXML void cancellationsReport(ActionEvent event) { /* TODO: F5 — cancellations report */ }
    @FXML void approveRejectParams(ActionEvent event) { /* TODO: F6 — approve/reject params */ }
    @FXML void logout(ActionEvent event)              { LogoutHelper.logout(); }
    @FXML void exit(ActionEvent event) {
        if (ClientUI.client != null) ClientUI.client.disconnect();
        System.exit(0);
    }
}