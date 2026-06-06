package GUI;

import Client.ClientUI;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ServiceRepDashboardController {

    @FXML private Label lblWelcome;

    @FXML
    public void initialize() {
        if (ClientUI.loggedInUser != null) {
            lblWelcome.setText("Welcome, " + ClientUI.loggedInUser.getFirstName()
                + " " + ClientUI.loggedInUser.getLastName());
        }
    }

    @FXML void registerSubscriber(ActionEvent event) { /* TODO: F6 — register subscriber */ }
    @FXML void registerGuide(ActionEvent event)      { /* TODO: F6 — register guide */ }
    @FXML void logout(ActionEvent event)             { LogoutHelper.logout(); }
    @FXML void exit(ActionEvent event) {
        if (ClientUI.client != null) ClientUI.client.disconnect();
        System.exit(0);
    }
}