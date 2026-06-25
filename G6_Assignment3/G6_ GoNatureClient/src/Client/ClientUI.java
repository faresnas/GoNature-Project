package Client;

import Common.LoginResponse;
import GUI.ConnectionController;
import GUI.DashboardController;
import GUI.OrderListController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.ArrayList;

/**
 * The main JavaFX application for the GoNature client.
 * <p>
 * This class is responsible for launching the client application,
 * initializing the primary stage, loading the connection screen,
 * and managing the application's main window.
 * It also stores references to the application's controllers and
 * the active client connection.
 * </p>
 *
 * @author Fares
 * @version 1.0
 */
public class ClientUI extends Application {

    /**
     * Reference to the dashboard controller.
     */
    public static DashboardController dashboardController;

    /**
     * Reference to the connection controller.
     */
    public static ConnectionController connectionController;

    /**
     * Reference to the order list controller.
     */
    public static OrderListController orderListController;

    /**
     * The active client used for communication with the server.
     */
    public static OrderClient client;

    /**
     * The primary JavaFX stage of the application.
     */
    public static Stage primaryStage;

    /**
     * Stores information about the currently logged-in user.
     */
    public static LoginResponse loggedInUser;

    /**
     * Stores login information that is waiting to be processed.
     */
    public static ArrayList<String> pendingLoginData;

    /**
     * Initializes the JavaFX application.
     * <p>
     * This method creates the main application window, loads the
     * connection screen, and registers an event handler to disconnect
     * the client when the application is closed.
     * </p>
     *
     * @param stage the primary stage used by the JavaFX application.
     */
    @Override
    public void start(Stage stage) {
        primaryStage = stage;

        primaryStage.setOnCloseRequest(e -> {
            if (client != null) {
                client.disconnect();
            }
        });

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GUI/Connection.fxml"));
            Scene scene = new Scene(loader.load());
            connectionController = loader.getController();
            primaryStage.setTitle("GoNature — Connect to Server");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            System.out.println("ClientUI: failed to load connection screen.");
            e.printStackTrace();
        }
    }

    /**
     * Launches the GoNature client application.
     *
     * @param args command-line arguments passed to the application.
     */
    public static void main(String[] args) {
        launch(args);
    }
}