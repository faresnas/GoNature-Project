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
import java.io.IOException;

/**
 * Controller for the Department Manager dashboard.
 * <p>
 * This controller manages all actions available to a Department Manager,
 * including viewing visitor statistics, opening reports, approving park
 * parameter requests, editing the user's profile, logging out, and exiting
 * the application.
 * </p>
 *
 * @author Fares
 * @version 1.0
 */
public class DeptManagerDashboardController {

    /**
     * Displays a welcome message containing the logged-in user's name.
     */
    @FXML
    private Label lblWelcome;

    /**
     * Initializes the Department Manager dashboard.
     * <p>
     * Displays the logged-in user's full name and clears any previously
     * assigned entry, exit, visitor count, or park manager controllers.
     * </p>
     */
    @FXML
    public void initialize() {
        if (ClientUI.loggedInUser != null) {
            lblWelcome.setText("Welcome, " + ClientUI.loggedInUser.getFirstName()
                + " " + ClientUI.loggedInUser.getLastName());
        }

        // Clear any leftover entry/exit controllers
        OrderClient.parkEntryController = null;
        OrderClient.parkExitController = null;
        OrderClient.parkManagerDashboardController = null;
        OrderClient.visitorCountController = null;
    }

    /**
     * Opens the Visitor Count screen.
     *
     * @param event the action event triggered by the user.
     */
    @FXML
    void viewVisitorCount(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GUI/VisitorCount.fxml"));
            Scene scene = new Scene(loader.load());
            ClientUI.primaryStage.setScene(scene);
        } catch (IOException e) {
            System.out.println("Failed to open VisitorCount: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Opens the Reports screen.
     * <p>
     * Initializes the Reports controller with Department Manager
     * permissions before displaying the screen.
     * </p>
     *
     * @param event the action event triggered by the user.
     */
    @FXML
    void openReports(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GUI/Reports.fxml"));
            javafx.scene.Parent root = loader.load();
            ReportsController controller = loader.getController();
            controller.initData("DEPARTMENT_MANAGER", -1);
            ClientUI.primaryStage.setScene(new Scene(root));
        } catch (Exception e) {
            System.out.println("Failed to open Reports: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Opens the Pending Requests screen where the Department Manager
     * can approve or reject park parameter requests.
     *
     * @param event the action event triggered by the user.
     */
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

    /**
     * Opens the Edit Profile screen.
     *
     * @param event the action event triggered by the user.
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
     * Logs the current user out of the system and returns
     * to the connection screen.
     *
     * @param event the action event triggered by the user.
     */
    @FXML
    void logout(ActionEvent event) {
        LogoutHelper.logout();
    }

    /**
     * Disconnects the client from the server and closes the application.
     *
     * @param event the action event triggered by the user.
     */
    @FXML
    void exit(ActionEvent event) {
        if (ClientUI.client != null)
            ClientUI.client.disconnect();

        System.exit(0);
    }
}