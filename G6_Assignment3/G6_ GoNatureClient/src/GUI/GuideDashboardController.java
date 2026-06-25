package GUI;

import Client.ClientUI;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;

/**
 * Controller for the Guide dashboard.
 * <p>
 * This controller manages all actions available to a guide,
 * including creating reservations, viewing existing reservations,
 * managing the waiting list, editing the profile, logging out,
 * and exiting the application.
 * </p>
 *
 * @author Fares
 * @version 1.0
 */
public class GuideDashboardController {

    /**
     * Displays a welcome message containing the guide's first name.
     */
    @FXML
    private Label lblWelcome;

    /**
     * Initializes the Guide dashboard.
     * <p>
     * Displays the first name of the currently logged-in guide.
     * </p>
     */
    @FXML
    public void initialize() {
        if (ClientUI.loggedInUser != null) {
            lblWelcome.setText("Welcome, " + ClientUI.loggedInUser.getFirstName());
        }
    }

    /**
     * Opens the reservation screen.
     * <p>
     * Allows the guide to create a new reservation.
     * </p>
     *
     * @param event the action event triggered by the user.
     */
    @FXML
    public void makeReservation(ActionEvent event) {
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
     * Opens the My Reservations screen.
     * <p>
     * Displays all reservations that belong to the logged-in guide.
     * </p>
     *
     * @param event the action event triggered by the user.
     */
    @FXML
    public void myReservations(ActionEvent event) {
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
     * Opens the Waiting List screen.
     * <p>
     * Allows the guide to view and manage waiting list requests.
     * </p>
     *
     * @param event the action event triggered by the user.
     */
    @FXML
    void openWaitingList(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GUI/WaitingList.fxml"));
            Scene scene = new Scene(loader.load());
            ClientUI.primaryStage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Logs the current guide out of the system.
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