package GUI;

import Client.ClientUI;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
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

    @FXML
    void viewVisitorCount(ActionEvent event) { /* TODO: F4 */ }

    @FXML
    void visitsReport(ActionEvent event)     { /* TODO: F5 */ }

    @FXML
    void usageReport(ActionEvent event)      { /* TODO: F5 */ }

    @FXML
    void updateParkParams(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/GUI/UpdateParkParams.fxml"));
            Scene scene = new Scene(loader.load());
            ClientUI.primaryStage.setScene(scene);
        } catch (Exception e) {
            System.out.println("Failed to open UpdateParkParams: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void logout(ActionEvent event) {
        LogoutHelper.logout();
    }

    @FXML
    void exit(ActionEvent event) {
        if (ClientUI.client != null) ClientUI.client.disconnect();
        System.exit(0);
    }
}