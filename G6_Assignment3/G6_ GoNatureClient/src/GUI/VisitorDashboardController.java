package GUI;

import Client.ClientUI;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;

/**
 * Controller for the Visitor Dashboard.
 * This class manages the main dashboard displayed to visitors
 * and subscribers. It allows users to create reservations,
 * view their existing reservations, edit their profile,
 * access the waiting list, log out, and exit the application.
 */
public class VisitorDashboardController {

    /**
     * Displays a welcome message for the logged-in user.
     */
    @FXML
    private Label lblWelcome;

    /**
     * Displays the dashboard title according to the user's type.
     */
    @FXML
    private Label lblDashboard;

    /**
     * Initializes the dashboard after the FXML file is loaded.
     * Displays the user's full name and sets the dashboard title
     * according to whether the user is a visitor or subscriber.
     */
    @FXML
    public void initialize() {
        if (ClientUI.loggedInUser != null) {
            lblWelcome.setText("Welcome, "
                    + ClientUI.loggedInUser.getFirstName()
                    + " "
                    + ClientUI.loggedInUser.getLastName());

            String userType =
                    ClientUI.loggedInUser.getUserType() != null
                    ? ClientUI.loggedInUser.getUserType().toString()
                    : "";

            if ("SUBSCRIBER".equals(userType)) {
                lblDashboard.setText("Subscriber Dashboard");
            } else {
                lblDashboard.setText("Visitor Dashboard");
            }
        }
    }

    /**
     * Opens the reservation creation screen.
     *
     * @param event the button click event
     */
    @FXML
    public void makeReservation(ActionEvent event) {

        System.out.println("MAKE RESERVATION CLICKED");

        try {
            FXMLLoader loader =
                    new FXMLLoader(getClass().getResource("/GUI/Reservation.fxml"));

            Scene scene = new Scene(loader.load());

            ClientUI.primaryStage.setTitle("GoNature — Make Reservation");
            ClientUI.primaryStage.setScene(scene);

        } catch (Exception e) {
            System.out.println("Failed to open reservation screen.");
            e.printStackTrace();
        }
    }

    /**
     * Opens the user's reservation history screen.
     *
     * @param event the button click event
     */
    @FXML
    public void myReservations(ActionEvent event) {

        System.out.println("MY RESERVATIONS CLICKED");

        try {
            FXMLLoader loader =
                    new FXMLLoader(getClass().getResource("/GUI/MyReservations.fxml"));

            Scene scene = new Scene(loader.load());

            ClientUI.primaryStage.setTitle("GoNature — My Reservations");
            ClientUI.primaryStage.setScene(scene);

        } catch (Exception e) {
            System.out.println("Failed to open my reservations screen.");
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
            FXMLLoader loader =
                    new FXMLLoader(getClass().getResource("/GUI/EditProfile.fxml"));

            Scene scene = new Scene(loader.load());
            ClientUI.primaryStage.setScene(scene);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Opens the waiting list management screen.
     *
     * @param event the button click event
     */
    @FXML
    void openWaitingList(ActionEvent event) {
        try {
            FXMLLoader loader =
                    new FXMLLoader(getClass().getResource("/GUI/WaitingList.fxml"));

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
     * and closes the application.
     *
     * @param event the button click event
     */
    @FXML
    void exit(ActionEvent event) {
        if (ClientUI.client != null) {
            ClientUI.client.disconnect();
        }

        System.exit(0);
    }
}