package GUI;

import Client.ClientUI;
import Common.Chat;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;

/**
 * Controller for the main dashboard screen.
 * <p>
 * This class handles navigation from the dashboard to other screens,
 * such as the order list screen. It also manages logout and exit actions.
 * </p>
 *
 * @author Fares
 * @version 1.0
 */
public class DashboardController {

    /**
     * Opens the order list screen.
     * <p>
     * This method loads the OrderList FXML file, stores its controller
     * in ClientUI, and changes the primary stage scene to the orders screen.
     * </p>
     *
     * @param event the action event triggered by the user.
     */
    @FXML
    public void openOrderView(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GUI/OrderList.fxml"));
            Scene scene = new Scene(loader.load());
            ClientUI.orderListController = loader.getController();
            ClientUI.primaryStage.setTitle("GoNature — Orders");
            ClientUI.primaryStage.setScene(scene);
        } catch (Exception e) {
            System.out.println("DashboardController: failed to load order list.");
            e.printStackTrace();
        }
    }

    /**
     * Logs the current user out of the system.
     * <p>
     * This method sends a logout request to the server, closes the client
     * connection, clears the logged-in user information, and returns the
     * application to the connection screen.
     * </p>
     *
     * @param event the action event triggered by the user.
     */
    @FXML
    void logout(ActionEvent event) {
        try {
            if (ClientUI.client != null) {
                ClientUI.client.sendToServer(new Chat("LOGOUT_REQUEST", null));
                ClientUI.client.closeConnection();
            }
        } catch (Exception e) {
            System.out.println("DashboardController: logout error — " + e.getMessage());
        } finally {
            ClientUI.client = null;
            ClientUI.loggedInUser = null;
            try {
                FXMLLoader loader = new FXMLLoader(DashboardController.class.getResource("/GUI/Connection.fxml"));
                Scene scene = new Scene(loader.load());
                ClientUI.connectionController = loader.getController();
                ClientUI.primaryStage.setTitle("GoNature — Connect to Server");
                ClientUI.primaryStage.setScene(scene);
            } catch (Exception e) {
                System.out.println("DashboardController: failed to return to connection screen.");
                e.printStackTrace();
            }
        }
    }

    /**
     * Exits the application.
     * <p>
     * If a client connection exists, it is disconnected before the
     * application is closed.
     * </p>
     *
     * @param event the action event triggered by the user.
     */
    @FXML
    void exit(ActionEvent event) {
        if (ClientUI.client != null) {
            ClientUI.client.disconnect();
        }
        System.exit(0);
    }
}