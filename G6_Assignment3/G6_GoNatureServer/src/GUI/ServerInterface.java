package GUI;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import Server.ServerUI;

/**
 * Controller for the server startup screen.
 * This class allows the user to enter the server port,
 * start the GoNature server, open the server log window,
 * and exit the application.
 */
public class ServerInterface {

    /**
     * Text field used to enter the server port number.
     */
    @FXML
    private TextField portxt;

    /**
     * Button used to start the server.
     */
    @FXML
    private Button btnDone;

    /**
     * Button used to exit the application.
     */
    @FXML
    private Button btnExit;

    /**
     * Label used to display validation and startup errors.
     */
    @FXML
    private Label errorLabel;

    /**
     * Reads the port number entered by the user.
     *
     * @return the entered port number as a string
     */
    private String readPortInput() {
        return portxt.getText();
    }

    /**
     * Starts the server using the entered port number.
     * If the server starts successfully, the server log window
     * is opened. Otherwise, an error message is displayed.
     *
     * @param event the button click event
     */
    @FXML
    public void done(ActionEvent event) {

        errorLabel.setText("");

        String portInput = readPortInput();

        if (portInput == null || portInput.trim().isEmpty()) {
            errorLabel.setText("Port number is required.");
            return;
        }

        try {

            ServerUI.runServer(portInput.trim());

            ((Node) event.getSource()).getScene().getWindow().hide();

            FXMLLoader loader =
                    new FXMLLoader(getClass().getResource("/GUI/ServerMain.fxml"));

            Parent root = loader.load();

            Stage logStage = new Stage();
            logStage.setTitle("GoNature Server — Live Log");
            logStage.setScene(new Scene(root, 620, 540));
            logStage.setOnCloseRequest(e -> System.exit(0));
            logStage.show();

        } catch (Exception e) {
            errorLabel.setText("Failed to start GoNature server.");
            e.printStackTrace();
        }
    }

    /**
     * Closes the application.
     *
     * @param event the button click event
     */
    @FXML
    public void exit(ActionEvent event) {
        System.out.println("GoNature Server — exit requested");
        System.exit(0);
    }

    /**
     * Opens the server startup window.
     *
     * @param primaryStage the primary application stage
     * @throws Exception if the FXML file cannot be loaded
     */
    public void start(Stage primaryStage) throws Exception {

        Parent root =
                FXMLLoader.load(getClass().getResource("/GUI/ServerInterface.fxml"));

        Scene scene = new Scene(root);

        primaryStage.setTitle("GoNature Server — Port Setup");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}