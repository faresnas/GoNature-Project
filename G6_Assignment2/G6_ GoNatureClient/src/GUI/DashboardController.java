package GUI;

import Client.ClientUI;
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
    void exit(ActionEvent event) {
        System.exit(0);
    }
}