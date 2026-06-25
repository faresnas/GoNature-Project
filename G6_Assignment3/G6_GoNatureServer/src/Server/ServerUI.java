package Server;

import javafx.application.Application;
import javafx.stage.Stage;
import GUI.ServerInterface;

/**
 * Main entry point of the GoNature server application.
 * This class launches the JavaFX server interface and
 * starts the server on the port selected by the user.
 */
public class ServerUI extends Application {

    /**
     * Reference to the active server instance.
     * Used by the GUI to receive server events and log messages.
     */
    public static EchoServer serverInstance = null;

    /**
     * Launches the JavaFX server application.
     *
     * @param args command-line arguments
     * @throws Exception if the application fails to start
     */
    public static void main(String args[]) throws Exception {
        launch(args);
    }

    /**
     * Opens the server startup window.
     *
     * @param primaryStage the primary JavaFX stage
     * @throws Exception if the interface cannot be loaded
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        ServerInterface portFrame = new ServerInterface();
        portFrame.start(primaryStage);
    }

    /**
     * Creates and starts the GoNature server.
     *
     * @param p the server port number entered by the user
     */
    public static void runServer(String p) {

        int port = 0;

        try {
            port = Integer.parseInt(p);

        } catch (Exception e) {
            System.out.println("GoNatureServer: invalid port number.");
            e.printStackTrace();
        }

        serverInstance = new EchoServer(port);

        try {
            serverInstance.listen();

        } catch (Exception ex) {
            System.out.println("GoNatureServer: failed to start listening.");
            ex.printStackTrace();
        }
    }
}