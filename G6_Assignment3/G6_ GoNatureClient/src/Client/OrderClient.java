package Client;

import Common.Chat;
import Common.LoginResponse;
import data.Order;
import javafx.application.Platform;
import ocsf.client.AbstractClient;
import java.util.ArrayList;
import data.Reservation;
import GUI.MyReservationsController;

public class OrderClient extends AbstractClient {
     
    public static MyReservationsController myReservationsController;
    public static ArrayList<Order> ordersList;

    public OrderClient(String host, int port) {
        super(host, port);
    }
    
    public void createReservation(Reservation reservation) {
        try {
            sendToServer(new Chat("CREATE_RESERVATION", reservation));
        } catch (Exception e) {
            System.out.println("Failed to create reservation: " + e.getMessage());
        }
    }
    
    public void requestMyReservations(int travelerId, String travelerType) {
        try {
            ArrayList<Object> data = new ArrayList<>();
            data.add(travelerId);
            data.add(travelerType);
            sendToServer(new Chat("GET_MY_RESERVATIONS", data));
        } catch (Exception e) {
            System.out.println("Failed to get reservations: " + e.getMessage());
        }
    }

    public void updateReservation(int reservationId, String visitDate, String entryTime, int numVisitors) {
        try {
            ArrayList<Object> data = new ArrayList<>();
            data.add(reservationId);
            data.add(visitDate);
            data.add(entryTime);
            data.add(numVisitors);
            sendToServer(new Chat("UPDATE_RESERVATION", data));
        } catch (Exception e) {
            System.out.println("Failed to update reservation: " + e.getMessage());
        }
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
            Platform.runLater(() -> {
                if (ClientUI.orderListController != null) {
                    ClientUI.orderListController.showOrders((ArrayList<ArrayList<String>>) chat);
                } else if (OrderClient.myReservationsController != null) {
                    // תיקון השורה שזרקה שגיאה - קריאה למתודה הקיימת בקונטרולר המעודכן
                    OrderClient.myReservationsController.setReservationsTable((ArrayList<ArrayList<String>>) chat);
                }
            });

        } else if (chat instanceof Boolean) {
            boolean result = (Boolean) chat;
            Platform.runLater(() -> {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    result ? javafx.scene.control.Alert.AlertType.INFORMATION : javafx.scene.control.Alert.AlertType.WARNING
                );
                alert.setTitle(result ? "Update Successful" : "Update Failed");
                alert.setHeaderText(null);
                alert.setContentText(result ? "Your reservation has been updated successfully!" : "Failed to update reservation. No available slots.");
                alert.showAndWait();
            });

        } else if (chat instanceof String) {
            String serverResponse = (String) chat;
            
            Platform.runLater(() -> {
                if (serverResponse.startsWith("SUCCESS:")) {
                    String[] parts = serverResponse.split(":");
                    String code = parts[1];
                    String price = parts[2];
                    
                    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                    alert.setTitle("Simulation");
                    alert.setHeaderText("✨ Reservation Confirmed!");
                    alert.setContentText("[SIMULATION] Email & SMS Notification Sent!\n" +
                                         "Confirmation Code: " + code + "\n" +
                                         "Total Estimated Price: " + price + " NIS\n\n" +
                                         "Status: CONFIRMED");
                    alert.showAndWait();
                    
                } else if (serverResponse.startsWith("FULL:")) {
                    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
                    alert.setTitle("Park Full");
                    alert.setHeaderText("Cannot Complete Booking");
                    alert.setContentText("The selected park is full for this time slot.\n" +
                                         "Please try choosing another date, time, or reduce visitor count.");
                    alert.showAndWait();
                    
                } else {
                    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Action Failed");
                    alert.setContentText("Server response: " + serverResponse);
                    alert.showAndWait();
                }
            });
        }
    }

    public void requestAllOrders() {
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