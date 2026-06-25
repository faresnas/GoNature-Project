package GUI;

import Client.ClientUI;
import Common.Chat;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;

/**
 * Utility class responsible for handling user logout.
 * <p>
 * This class safely disconnects the client from the server,
 * clears the current session information, and returns the user
 * to the connection screen.
 * </p>
 *
 * @author Fares
 * @version 1.0
 */
public class LogoutHelper {

    /**
     * Logs the current user out of the GoNature system.
     * <p>
     * The method performs the following operations:
     * <ul>
     *     <li>Sends a logout request to the server.</li>
     *     <li>Closes the client connection.</li>
     *     <li>Clears the current client session.</li>
     *     <li>Loads the connection screen.</li>
     * </ul>
     */
    public static void logout() {
        try {
            if (ClientUI.client != null) {
                ClientUI.client.sendToServer(new Chat("CLIENT_EXIT", null));

                try {
                    Thread.sleep(200);
                } catch (Exception ignored) {
                }

                ClientUI.client.closeConnection();
            }
        } catch (Exception e) {
            System.out.println("LogoutHelper: logout error — " + e.getMessage());
        } finally {
            ClientUI.client = null;
            ClientUI.loggedInUser = null;

            Platform.runLater(() -> {
                try {
                    FXMLLoader loader = new FXMLLoader(
                            LogoutHelper.class.getResource("/GUI/Connection.fxml"));

                    Scene scene = new Scene(loader.load());

                    ClientUI.connectionController = loader.getController();
                    ClientUI.primaryStage.setTitle("GoNature — Connect to Server");
                    ClientUI.primaryStage.setScene(scene);

                } catch (Exception e) {
                    System.out.println("LogoutHelper: failed to return to connection screen.");
                    e.printStackTrace();
                }
            });
        }
    }
}