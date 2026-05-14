package Client;

import Common.Chat;
import data.Order;
import javafx.application.Platform;
import ocsf.client.AbstractClient;
import java.util.ArrayList;

public class OrderClient extends AbstractClient {

    public static ArrayList<Order> ordersList;

    public OrderClient(String host, int port) {
        super(host, port);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void handleMessageFromServer(Object msg) {
        if (msg instanceof ArrayList<?>) {
            ArrayList<ArrayList<String>> receivedOrders = (ArrayList<ArrayList<String>>) msg;
            Platform.runLater(() -> {
                if (ClientUI.orderListController != null)
                    ClientUI.orderListController.showOrders(receivedOrders);
            });
        } else if (msg instanceof Boolean) {
            boolean result = (Boolean) msg;
            String feedback = result ? "Order updated successfully" : "Update failed — please try again";
            Platform.runLater(() -> {
                if (ClientUI.orderListController != null)
                    ClientUI.orderListController.showSuccess(feedback);
            });
        } else {
            System.out.println("OrderClient: unexpected message — " + msg);
        }
    }

    public void requestOrders() {
        try {
            System.out.println("OrderClient: sending GET_ORDERS request");
            sendToServer(new Chat("GET_ORDERS", null));
        } catch (Exception e) {
            System.out.println("OrderClient: failed to request orders — " + e.getMessage());
        }
    }

    public void sendOrderUpdate(Order order) {
        try {
            ArrayList<Object> updateData = new ArrayList<>();
            updateData.add(order.getOrderNumber());
            updateData.add(order.getOrderDate().toString());
            updateData.add(order.getNumberOfVisitors());
            System.out.println("OrderClient: sending UPDATE_ORDER request");
            sendToServer(new Chat("UPDATE_ORDER", updateData));
        } catch (Exception e) {
            System.out.println("OrderClient: failed to send update — " + e.getMessage());
        }
    }

    @Override
    protected void connectionClosed() {
        System.out.println("OrderClient: connection closed");
    }

    @Override
    protected void connectionException(Exception exception) {
        System.out.println("OrderClient: connection error — " + exception.getMessage());
    }
}