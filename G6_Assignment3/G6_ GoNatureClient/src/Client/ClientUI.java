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

public class ClientUI extends Application {

    public static DashboardController dashboardController;
    public static ConnectionController connectionController;
    public static OrderListController orderListController;
    public static OrderClient client;
    public static Stage primaryStage;
    public static LoginResponse loggedInUser;
    public static ArrayList<String> pendingLoginData;

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

    public static void main(String[] args) {
        launch(args);
    }
}