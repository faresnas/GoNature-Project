package GUI;

import Client.ClientUI;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class DeptManagerDashboardController {

    @FXML private Label lblWelcome;

    @FXML
    public void initialize() {
        if (ClientUI.loggedInUser != null) {
            lblWelcome.setText("Welcome, " + ClientUI.loggedInUser.getFirstName()
                + " " + ClientUI.loggedInUser.getLastName());
        }
    }
    @FXML
    void viewVisitorCount(ActionEvent event) {
        try {
            java.net.URL fxmlUrl = getClass().getResource("/GUI/VisitorCount.fxml");

            if (fxmlUrl == null) {
                System.out.println("ERROR: VisitorCount.fxml was not found in /GUI/VisitorCount.fxml");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Scene scene = new Scene(loader.load());
            ClientUI.primaryStage.setScene(scene);

        } catch (IOException e) {
            System.out.println("Failed to open VisitorCount: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void viewAllVisitorCount(ActionEvent event) { /* TODO: F4 */ }

    @FXML
    void visitsReport(ActionEvent event)        { /* TODO: F5 */ }

    @FXML
    void cancellationsReport(ActionEvent event) { /* TODO: F5 */ }

    @FXML
    void approveRejectParams(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/GUI/PendingRequests.fxml"));
            Scene scene = new Scene(loader.load());
            ClientUI.primaryStage.setScene(scene);
        } catch (Exception e) {
            System.out.println("Failed to open PendingRequests: " + e.getMessage());
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