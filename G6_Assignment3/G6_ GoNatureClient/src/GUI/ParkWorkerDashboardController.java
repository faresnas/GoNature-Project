package GUI;

import java.io.IOException;

import Client.ClientUI;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;

/**
 * Controller for the Park Worker Dashboard.
 * This class manages the main dashboard displayed to park workers.
 * It allows workers to access the park entry and exit screens,
 * edit their profile, log out of the system, or exit the application.
 */
public class ParkWorkerDashboardController {

    /**
     * Displays a welcome message containing the logged-in worker's name.
     */
    @FXML
    private Label lblWelcome;

    /**
     * Displays the name of the park assigned to the logged-in worker.
     */
    @FXML
    private Label lblPark;

    /**
     * Initializes the dashboard after the FXML file is loaded.
     * The method loads the logged-in worker's information
     * and displays the worker's name and assigned park.
     */
    @FXML
    public void initialize() {
        if (ClientUI.loggedInUser != null) {
            lblWelcome.setText("Welcome, " + ClientUI.loggedInUser.getFirstName()
                + " " + ClientUI.loggedInUser.getLastName());
            lblPark.setText("Park: " + ClientUI.loggedInUser.getParkName());
        }
    }

    /**
     * Opens the park entry screen.
     *
     * @param event the button click event
     */
    @FXML
    void parkEntry(ActionEvent event) {
        openScreen("/GUI/ParkEntry.fxml");
    }

    /**
     * Opens the park exit screen.
     *
     * @param event the button click event
     */
    @FXML
    void parkExit(ActionEvent event) {
        openScreen("/GUI/ParkExit.fxml");
    }

    /**
     * Loads and displays the requested FXML screen.
     *
     * @param fxmlPath the path of the FXML file to load
     */
    private void openScreen(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Scene scene = new Scene(loader.load());
            ClientUI.primaryStage.setScene(scene);
        } catch (IOException e) {
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
     * Opens the profile editing screen.
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
     * Disconnects the client from the server
     * and closes the application.
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