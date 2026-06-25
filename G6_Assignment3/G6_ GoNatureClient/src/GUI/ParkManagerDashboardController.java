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

/**
 * Controller for the Park Manager Dashboard.
 * This class manages the main dashboard displayed to a park manager.
 * It presents park information, visitor statistics, and provides
 * access to reports, park parameter updates, profile editing,
 * logout, and application exit.
 */
public class ParkManagerDashboardController {

    /**
     * Displays a welcome message for the logged-in park manager.
     */
    @FXML
    private Label lblWelcome;

    /**
     * Displays the name of the park managed by the current user.
     */
    @FXML
    private Label lblParkName;

    /**
     * Displays the current number of visitors inside the park.
     */
    @FXML
    private Label lblCurrentVisitors;

    /**
     * Displays the number of available spots remaining in the park.
     */
    @FXML
    private Label lblAvailableSpots;

    /**
     * Stores the ID of the park managed by the logged-in user.
     */
    private int parkId = -1;

    /**
     * Initializes the dashboard after the FXML file is loaded.
     * Loads the manager's information, registers this controller
     * to receive visitor count updates, and requests the latest
     * park statistics from the server.
     */
    @FXML
    public void initialize() {
        if (ClientUI.loggedInUser != null) {
            lblWelcome.setText("Welcome, " + ClientUI.loggedInUser.getFirstName()
                + " " + ClientUI.loggedInUser.getLastName());
            lblParkName.setText("Park: " + ClientUI.loggedInUser.getParkName());
            parkId = ClientUI.loggedInUser.getParkId();
        }

        OrderClient.visitorCountController = null;
        OrderClient.parkEntryController = null;
        OrderClient.parkExitController = null;
        OrderClient.parkManagerDashboardController = this;

        refreshVisitorCount();
    }

    /**
     * Requests the current visitor count and available spots
     * from the server for the managed park.
     */
    private void refreshVisitorCount() {
        if (parkId > 0) {
            try {
                ClientUI.client.sendToServer(new Chat("GET_CURRENT_VISITORS", parkId));
            } catch (Exception e) {
                System.out.println("Failed to refresh visitor count: " + e.getMessage());
            }
        }
    }

    /**
     * Receives the updated visitor information from the server
     * and refreshes the dashboard labels.
     *
     * @param response the visitor count response received from the server
     */
    public void handleVisitorCountResponse(EntryExitResponse response) {
        Platform.runLater(() -> {
            lblCurrentVisitors.setText("Current visitors: " + response.getCurrentVisitors());
            lblAvailableSpots.setText("Available spots: " + response.getAvailableSpots());
        });
    }

    /**
     * Opens the reports screen and transfers the manager's park
     * information to the reports controller.
     *
     * @param event the button click event
     */
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

    /**
     * Opens the screen used for updating park parameters
     * such as capacity and operational settings.
     *
     * @param event the button click event
     */
    @FXML
    void updateParkParams(ActionEvent event) {
        try {
            FXMLLoader loader =
                new FXMLLoader(getClass().getResource("/GUI/UpdateParkParams.fxml"));

            Scene scene = new Scene(loader.load());
            ClientUI.primaryStage.setScene(scene);

        } catch (Exception e) {
            System.out.println("Failed to open UpdateParkParams: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Opens the profile editing screen for the logged-in manager.
     *
     * @param event the button click event
     */
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

    /**
     * Logs the current user out of the system
     * and returns to the login screen.
     *
     * @param event the button click event
     */
    @FXML
    void logout(ActionEvent event) {
        LogoutHelper.logout();
    }

    /**
     * Disconnects the client from the server
     * and terminates the application.
     *
     * @param event the button click event
     */
    @FXML
    void exit(ActionEvent event) {
        if (ClientUI.client != null)
            ClientUI.client.disconnect();

        System.exit(0);
    }
}