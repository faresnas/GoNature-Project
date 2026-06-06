package Client;

import Common.Chat;
import Common.LoginResponse;
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
    protected void handleMessageFromServer(Object chat) {
        if (chat instanceof LoginResponse) {
            LoginResponse response = (LoginResponse) chat;
            Platform.runLater(() -> {
                if (ClientUI.connectionController != null)
                    ClientUI.connectionController.handleLoginResponse(response);
            });

        } else if (chat instanceof ArrayList<?>) {
            ArrayList<ArrayList<String>> receivedOrders = (ArrayList<ArrayList<String>>) chat;
            Platform.runLater(() -> {
                if (ClientUI.orderListController != null)
                    ClientUI.orderListController.showOrders(receivedOrders);
            });

        } else if (chat instanceof Boolean) {
            boolean result = (Boolean) chat;
            String feedback = result ? "Order updated successfully" : "Update failed — please try again";
            Platform.runLater(() -> {
                if (ClientUI.orderListController != null)
                    ClientUI.orderListController.showSuccess(feedback);
            });

        } else {
            System.out.println("OrderClient: unexpected message — " + chat);
        }
    }

    public void requestOrders() {
        try {
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
            sendToServer(new Chat("UPDATE_ORDER", updateData));
        } catch (Exception e) {
            System.out.println("OrderClient: failed to send update — " + e.getMessage());
        }
    }

    public void sendToServer(Chat chat) {
        try {
            super.sendToServer(chat);
        } catch (Exception e) {
            System.out.println("OrderClient: failed to send — " + e.getMessage());
        }
    }

    public void disconnect() {
        try {
            sendToServer(new Chat("CLIENT_EXIT", null));
            closeConnection();
        } catch (Exception e) {
            System.out.println("OrderClient: failed to disconnect cleanly — " + e.getMessage());
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