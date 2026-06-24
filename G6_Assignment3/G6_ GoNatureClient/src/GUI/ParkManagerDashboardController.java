package GUI;

import Client.ClientUI;
import Client.OrderClient;
import Common.Chat;
import Common.EntryExitResponse;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;

public class ParkManagerDashboardController {

    @FXML private Label lblWelcome;
    @FXML private Label lblParkName;
    @FXML private Label lblCurrentVisitors;
    @FXML private Label lblAvailableSpots;

    private int parkId = -1;

    @FXML
    public void initialize() {
        if (ClientUI.loggedInUser != null) {
            lblWelcome.setText("Welcome, " + ClientUI.loggedInUser.getFirstName()
                + " " + ClientUI.loggedInUser.getLastName());
            lblParkName.setText("Park: " + ClientUI.loggedInUser.getParkName());
            parkId = ClientUI.loggedInUser.getParkId();
        }
        // Register as visitor count receiver and load count
        OrderClient.visitorCountController = null;
        OrderClient.parkEntryController = null;
        OrderClient.parkExitController = null;
        OrderClient.parkManagerDashboardController = this;
        refreshVisitorCount();
    }

    private void refreshVisitorCount() {
        if (parkId > 0) {
            try {
                ClientUI.client.sendToServer(new Chat("GET_CURRENT_VISITORS", parkId));
            } catch (Exception e) {
                System.out.println("Failed to refresh visitor count: " + e.getMessage());
            }
        }
    }

    // Called by OrderClient when EntryExitResponse arrives and no entry/exit controller is active
    public void handleVisitorCountResponse(EntryExitResponse response) {
        Platform.runLater(() -> {
            lblCurrentVisitors.setText("Current visitors: " + response.getCurrentVisitors());
            lblAvailableSpots.setText("Available spots: " + response.getAvailableSpots());
        });
    }

    @FXML
    void openReports(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GUI/Reports.fxml"));
            javafx.scene.Parent root = loader.load();
            ReportsController controller = loader.getController();
            controller.initData("PARK_MANAGER", ClientUI.loggedInUser.getParkId());
            ClientUI.primaryStage.setScene(new Scene(root));
        } catch (Exception e) {
            System.out.println("Failed to open Reports: " + e.getMessage());
            e.printStackTrace();
        }
    }

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
    void editProfile(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GUI/EditProfile.fxml"));
            Scene scene = new Scene(loader.load());
            ClientUI.primaryStage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void logout(ActionEvent event) { LogoutHelper.logout(); }

    @FXML
    void exit(ActionEvent event) {
        if (ClientUI.client != null) ClientUI.client.disconnect();
        System.exit(0);
    }
}