package GUI;

import Client.ClientUI;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;

/**
 * Controller for the Service Representative Dashboard.
 * This class manages the main dashboard displayed to service representatives.
 * It provides access to subscriber and guide management,
 * profile editing, logout, and application exit.
 */
public class ServiceRepDashboardController {

    /**
     * Displays a welcome message for the logged-in service representative.
     */
    @FXML
    private Label lblWelcome;

    /**
     * Initializes the dashboard after the FXML file is loaded.
     * Displays the logged-in user's full name.
     */
    @FXML
    public void initialize() {
        if (ClientUI.loggedInUser != null) {
            lblWelcome.setText("Welcome, " + ClientUI.loggedInUser.getFirstName()
                + " " + ClientUI.loggedInUser.getLastName());
        }
    }

    /**
     * Opens the subscriber registration screen.
     *
     * @param event the button click event
     */
    @FXML
    void registerSubscriber(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/GUI/RegisterSubscriber.fxml"));
            Scene scene = new Scene(loader.load());
            ClientUI.primaryStage.setScene(scene);
        } catch (Exception e) {
            System.out.println("Failed to open RegisterSubscriber: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Opens the guide registration screen.
     *
     * @param event the button click event
     */
    @FXML
    void registerGuide(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/GUI/RegisterGuide.fxml"));
            Scene scene = new Scene(loader.load());
            ClientUI.primaryStage.setScene(scene);
        } catch (Exception e) {
            System.out.println("Failed to open RegisterGuide: " + e.getMessage());
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

    /**
     * Opens the guide management screen,
     * allowing guides to be edited or removed.
     *
     * @param event the button click event
     */
    @FXML
    void removeGuide(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/GUI/RemoveGuide.fxml"));
            Scene scene = new Scene(loader.load());
            ClientUI.primaryStage.setScene(scene);
        } catch (Exception e) {
            System.out.println("Failed to open RemoveGuide: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Opens the profile editing screen.
     *
     * @param event the button click event
     */
    @FXML
    void editProfile(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/GUI/EditProfile.fxml"));
            Scene scene = new Scene(loader.load());
            ClientUI.primaryStage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Opens the subscriber management screen,
     * allowing subscribers to be edited or removed.
     *
     * @param event the button click event
     */
    @FXML
    void removeSubscriber(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/GUI/RemoveSubscriber.fxml"));
            Scene scene = new Scene(loader.load());
            ClientUI.primaryStage.setScene(scene);
        } catch (Exception e) {
            System.out.println("Failed to open RemoveSubscriber: " + e.getMessage());
            e.printStackTrace();
        }
    }
}