package GUI;

import Client.ClientUI;
import Common.Chat;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;

public class DashboardController {

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

    @FXML
    void exit(ActionEvent event) {
        if (ClientUI.client != null) {
            ClientUI.client.disconnect();
        }
        System.exit(0);
    }
}