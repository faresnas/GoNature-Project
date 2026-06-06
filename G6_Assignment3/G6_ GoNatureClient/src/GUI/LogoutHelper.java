package GUI;

import Client.ClientUI;
import Common.Chat;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;

public class LogoutHelper {

    public static void logout() {
        try {
            if (ClientUI.client != null) {
                ClientUI.client.sendToServer(new Chat("CLIENT_EXIT", null));
                try { Thread.sleep(200); } catch (Exception ignored) {}
                ClientUI.client.closeConnection();
            }
        } catch (Exception e) {
            System.out.println("LogoutHelper: logout error — " + e.getMessage());
        } finally {
            ClientUI.client = null;
            ClientUI.loggedInUser = null;
            Platform.runLater(() -> {
                try {
                    FXMLLoader loader = new FXMLLoader(LogoutHelper.class.getResource("/GUI/Connection.fxml"));
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