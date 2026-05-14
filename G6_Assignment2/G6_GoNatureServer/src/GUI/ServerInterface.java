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

// handles the port input screen for the GoNature server
public class ServerInterface {

    @FXML
    private TextField portxt;

    @FXML
    private Button btnDone;

    @FXML
    private Button btnExit;

    @FXML
    private Label errorLabel;

    

    private String readPortInput() {
        return portxt.getText();
    }

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
        } catch (Exception e) {
            errorLabel.setText("Failed to start GoNature server.");
            e.printStackTrace();
        }
    }

    @FXML
    public void exit(ActionEvent event) {
        System.out.println("GoNature Server — exit requested");
        System.exit(0);
    }

    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/GUI/ServerInterface.fxml"));
        Scene scene = new Scene(root);
        primaryStage.setTitle("GoNature Server — Port Setup");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}