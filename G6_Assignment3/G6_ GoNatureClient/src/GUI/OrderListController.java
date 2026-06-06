package GUI;

import Client.ClientUI;
import data.Order;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import java.sql.Date;
import java.util.ArrayList;

public class OrderListController {

    @FXML
    private TextArea displayArea;
    @FXML
    private TextField orderIdField;
    @FXML
    private TextField visitDateField;
    @FXML
    private TextField visitorCountField;

    @FXML
    public void fetchOrders(ActionEvent event) {
        displayArea.setText("Loading orders...");
        ClientUI.client.requestOrders();
    }

    @FXML
    public void submitUpdate(ActionEvent event) {
        try {
            int orderNum = Integer.parseInt(orderIdField.getText());
            Date visitDate = Date.valueOf(visitDateField.getText());
            int visitorCount = Integer.parseInt(visitorCountField.getText());
            Order o = new Order();
            o.setOrderNumber(orderNum);
            o.setOrderDate(visitDate);
            o.setNumberOfVisitors(visitorCount);
            displayArea.setText("Updating order...");
            ClientUI.client.sendOrderUpdate(o);
        } catch (Exception e) {
            displayArea.setText("Something went wrong — check your input and try again");
        }
    }

    public void showOrders(ArrayList<ArrayList<String>> rows) {
        displayArea.clear();
        for (ArrayList<String> row : rows) {
            displayArea.appendText(formatRow(row));
        }
    }

    private String formatRow(ArrayList<String> row) {
        return "Order #" + row.get(0) +
               " | Date: " + row.get(1) +
               " | Visitors: " + row.get(2) +
               " | Confirmation: " + row.get(3) +
               " | Subscriber: " + row.get(4) +
               " | Placed: " + row.get(5) + "\n";
    }

    public void showSuccess(String message) {
        displayArea.setText(message);
    }

    @FXML
    void goBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GUI/Dashboard.fxml"));
            Scene scene = new Scene(loader.load());
            ClientUI.primaryStage.setTitle("GoNature — Dashboard");
            ClientUI.primaryStage.setScene(scene);
        } catch (Exception e) {
            System.out.println("OrderListController: failed to go back.");
            e.printStackTrace();
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