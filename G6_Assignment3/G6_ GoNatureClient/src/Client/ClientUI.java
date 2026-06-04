package Client;

import GUI.DashboardController;
import GUI.ConnectionController;
import GUI.OrderListController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ClientUI extends Application {

    public static DashboardController dashboardController;
    public static ConnectionController connectionController;
    public static OrderListController orderListController;
    public static OrderClient client;
    public static Stage primaryStage;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;

        // send CLIENT_EXIT and close connection cleanly when window is closed
        primaryStage.setOnCloseRequest(e -> {
            if (client != null) {
                client.disconnect();
            }
        });

        try {
            FXMLLoader connectionLoader = new FXMLLoader(getClass().getResource("/GUI/Connection.fxml"));
            Scene connectionScene = new Scene(connectionLoader.load());
            connectionController = connectionLoader.getController();
            primaryStage.setTitle("GoNature — Connect to Server");
            primaryStage.setScene(connectionScene);
            primaryStage.show();
        } catch (Exception e) {
            System.out.println("ClientUI: failed to load connection screen.");
            e.printStackTrace();
        }
    }

    public static void startClient(String serverIP, int serverPort) {
        try {
            client = new OrderClient(serverIP, serverPort);
            client.openConnection();
            System.out.println("ClientUI: connected to " + serverIP + ":" + serverPort);
            FXMLLoader dashboardLoader = new FXMLLoader(ClientUI.class.getResource("/GUI/Dashboard.fxml"));
            Scene dashboardScene = new Scene(dashboardLoader.load());
            dashboardController = dashboardLoader.getController();
            primaryStage.setTitle("GoNature — Dashboard");
            primaryStage.setScene(dashboardScene);
        } catch (Exception e) {
            System.out.println("ClientUI: connection failed.");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}