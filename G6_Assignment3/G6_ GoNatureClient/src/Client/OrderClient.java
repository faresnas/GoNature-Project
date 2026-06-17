package Client;

import Common.Chat;
import Common.LoginResponse;
import Common.EntryExitResponse;

import data.Order;
import data.Reservation;

import javafx.application.Platform;
import ocsf.client.AbstractClient;

import java.util.ArrayList;

import GUI.MyReservationsController;
import GUI.ReservationController;
import GUI.PendingRequestsController;
import GUI.UpdateParkParamsController;
import GUI.RemoveGuideController;
import GUI.RemoveSubscriberController;
import GUI.ParkEntryController;
import GUI.ParkExitController;
import GUI.VisitorCountController;

public class OrderClient extends AbstractClient {

    public static MyReservationsController myReservationsController;
    public static ReservationController reservationController;
    public static PendingRequestsController pendingRequestsController;
    public static UpdateParkParamsController updateParkParamsController;
    public static RemoveGuideController removeGuideController;
    public static RemoveSubscriberController removeSubscriberController;

    // Feature 4 controllers
    public static ParkEntryController parkEntryController;
    public static ParkExitController parkExitController;
    public static VisitorCountController visitorCountController;

    public static String lastCommand = "";
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

    public void deleteReservation(int reservationId, int travelerId, String travelerType) {
        try {
            ArrayList<Object> data = new ArrayList<>();
            data.add(reservationId);
            data.add(travelerId);
            data.add(travelerType);
            sendToServer(new Chat("DELETE_RESERVATION", data));
        } catch (Exception e) {
            System.out.println("Failed to delete reservation: " + e.getMessage());
        }
    }

    public void requestParks() {
        try {
            sendToServer(new Chat("GET_PARKS", null));
        } catch (Exception e) {
            System.out.println("Failed to request parks: " + e.getMessage());
        }
    }

    public void registerSubscriber(String firstName, String lastName, String idNumber,
            String phone, String email, int familySize, String creditCard) {
        try {
            lastCommand = "REGISTER_SUBSCRIBER";
            ArrayList<Object> data = new ArrayList<>();
            data.add(firstName);
            data.add(lastName);
            data.add(idNumber);
            data.add(phone);
            data.add(email);
            data.add(familySize);
            data.add(creditCard);
            sendToServer(new Chat("REGISTER_SUBSCRIBER", data));
        } catch (Exception e) {
            System.out.println("Failed to register subscriber: " + e.getMessage());
        }
    }

    public void registerGuide(String name, String email, String phone,
            String username, String password) {
        try {
            lastCommand = "REGISTER_GUIDE";
            ArrayList<Object> data = new ArrayList<>();
            data.add(name);
            data.add(email);
            data.add(phone);
            data.add(username);
            data.add(password);
            sendToServer(new Chat("REGISTER_GUIDE", data));
        } catch (Exception e) {
            System.out.println("Failed to register guide: " + e.getMessage());
        }
    }

    public void requestParkUpdate(int parkId, String requestType, double newValue, int requestedBy) {
        try {
            lastCommand = "REQUEST_PARK_UPDATE";
            ArrayList<Object> data = new ArrayList<>();
            data.add(parkId);
            data.add(requestType);
            data.add(newValue);
            data.add(requestedBy);
            sendToServer(new Chat("REQUEST_PARK_UPDATE", data));
        } catch (Exception e) {
            System.out.println("Failed to request park update: " + e.getMessage());
        }
    }

    public void getPendingRequests() {
        try {
            lastCommand = "GET_PENDING_REQUESTS";
            sendToServer(new Chat("GET_PENDING_REQUESTS", null));
        } catch (Exception e) {
            System.out.println("Failed to get pending requests: " + e.getMessage());
        }
    }

    public void approveRequest(int requestId) {
        try {
            lastCommand = "APPROVE_REQUEST";
            sendToServer(new Chat("APPROVE_REQUEST", requestId));
        } catch (Exception e) {
            System.out.println("Failed to approve request: " + e.getMessage());
        }
    }

    public void rejectRequest(int requestId) {
        try {
            lastCommand = "REJECT_REQUEST";
            sendToServer(new Chat("REJECT_REQUEST", requestId));
        } catch (Exception e) {
            System.out.println("Failed to reject request: " + e.getMessage());
        }
    }

    public void getAllGuides() {
        try {
            lastCommand = "GET_ALL_GUIDES";
            sendToServer(new Chat("GET_ALL_GUIDES", null));
        } catch (Exception e) {
            System.out.println("Failed to get guides: " + e.getMessage());
        }
    }

    public void getAllSubscribers() {
        try {
            lastCommand = "GET_ALL_SUBSCRIBERS";
            sendToServer(new Chat("GET_ALL_SUBSCRIBERS", null));
        } catch (Exception e) {
            System.out.println("Failed to get subscribers: " + e.getMessage());
        }
    }

    public void deleteGuide(int guideId) {
        try {
            lastCommand = "DELETE_GUIDE";
            sendToServer(new Chat("DELETE_GUIDE", guideId));
        } catch (Exception e) {
            System.out.println("Failed to delete guide: " + e.getMessage());
        }
    }

    public void deleteSubscriber(int subscriberId) {
        try {
            lastCommand = "DELETE_SUBSCRIBER";
            sendToServer(new Chat("DELETE_SUBSCRIBER", subscriberId));
        } catch (Exception e) {
            System.out.println("Failed to delete subscriber: " + e.getMessage());
        }
    }

    public void editGuide(int guideId, String name, String email,
            String phone, String password) {
        try {
            lastCommand = "EDIT_GUIDE";
            ArrayList<Object> data = new ArrayList<>();
            data.add(guideId);
            data.add(name);
            data.add(email);
            data.add(phone);
            data.add(password);
            sendToServer(new Chat("EDIT_GUIDE", data));
        } catch (Exception e) {
            System.out.println("Failed to edit guide: " + e.getMessage());
        }
    }

    public void editSubscriber(int subscriberId, String firstName, String lastName,
            String phone, String email, int familySize) {
        try {
            lastCommand = "EDIT_SUBSCRIBER";
            ArrayList<Object> data = new ArrayList<>();
            data.add(subscriberId);
            data.add(firstName);
            data.add(lastName);
            data.add(phone);
            data.add(email);
            data.add(familySize);
            sendToServer(new Chat("EDIT_SUBSCRIBER", data));
        } catch (Exception e) {
            System.out.println("Failed to edit subscriber: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void handleMessageFromServer(Object chat) {
        if (chat instanceof LoginResponse) {
            LoginResponse response = (LoginResponse) chat;
            Platform.runLater(() -> {
                if (ClientUI.connectionController != null) {
                    ClientUI.connectionController.handleLoginResponse(response);
                }
            });

        } else if (chat instanceof EntryExitResponse) {
            EntryExitResponse response = (EntryExitResponse) chat;
            Platform.runLater(() -> {
                if (parkEntryController != null) {
                    parkEntryController.handleEntryExitResponse(response);
                }

                if (parkExitController != null) {
                    parkExitController.handleEntryExitResponse(response);
                }

                if (visitorCountController != null) {
                    visitorCountController.handleEntryExitResponse(response);
                }
            });

        } else if (chat instanceof ArrayList<?>) {
            ArrayList<?> list = (ArrayList<?>) chat;
            Platform.runLater(() -> {
                if (!list.isEmpty() && list.get(0) instanceof ArrayList) {
                    ArrayList<ArrayList<String>> data = (ArrayList<ArrayList<String>>) list;

                    switch (lastCommand) {
                        case "GET_PENDING_REQUESTS":
                            if (pendingRequestsController != null) {
                                pendingRequestsController.setPendingRequestsTable(data);
                            }
                            break;

                        case "GET_ALL_GUIDES":
                            if (removeGuideController != null) {
                                removeGuideController.setGuidesTable(data);
                            }
                            break;

                        case "GET_ALL_SUBSCRIBERS":
                            if (removeSubscriberController != null) {
                                removeSubscriberController.setSubscribersTable(data);
                            }
                            break;

                        case "GET_MY_RESERVATIONS":
                            if (myReservationsController != null) {
                                myReservationsController.setReservationsTable(data);
                            }
                            break;

                        default:
                            if (reservationController != null && data.get(0).size() == 2) {
                                reservationController.populateParks(data);
                            } else if (myReservationsController != null) {
                                myReservationsController.setReservationsTable(data);
                            } else if (ClientUI.orderListController != null) {
                                ClientUI.orderListController.showOrders(data);
                            }
                            break;
                    }
                }
            });

        } else if (chat instanceof Integer) {
            int result = (Integer) chat;
            Platform.runLater(() -> {
                if (result > 0) {
                    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                            javafx.scene.control.Alert.AlertType.INFORMATION);
                    alert.setTitle("Subscriber Registered");
                    alert.setHeaderText("✅ Registration Successful!");
                    alert.setContentText("Subscriber registered successfully.\nSubscriber Number: " + result);
                    alert.showAndWait();

                } else if (result == -2) {
                    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                            javafx.scene.control.Alert.AlertType.WARNING);
                    alert.setTitle("Already Registered");
                    alert.setHeaderText(null);
                    alert.setContentText("A subscriber with this ID number already exists.");
                    alert.showAndWait();

                } else {
                    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                            javafx.scene.control.Alert.AlertType.ERROR);
                    alert.setTitle("Registration Failed");
                    alert.setHeaderText(null);
                    alert.setContentText("Failed to register subscriber. Please try again.");
                    alert.showAndWait();
                }
            });

        } else if (chat instanceof Boolean) {
            boolean result = (Boolean) chat;
            Platform.runLater(() -> {
                switch (lastCommand) {
                    case "REGISTER_GUIDE":
                        if (result) {
                            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                                    javafx.scene.control.Alert.AlertType.INFORMATION);
                            alert.setTitle("Guide Registered");
                            alert.setHeaderText("✅ Registration Successful!");
                            alert.setContentText("Group guide registered successfully.");
                            alert.showAndWait();
                        } else {
                            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                                    javafx.scene.control.Alert.AlertType.ERROR);
                            alert.setTitle("Registration Failed");
                            alert.setHeaderText(null);
                            alert.setContentText("Username already taken. Please choose a different username.");
                            alert.showAndWait();
                        }
                        break;

                    case "REQUEST_PARK_UPDATE":
                        if (result) {
                            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                                    javafx.scene.control.Alert.AlertType.INFORMATION);
                            alert.setTitle("Request Submitted");
                            alert.setHeaderText("✅ Request Sent!");
                            alert.setContentText("Your park update request has been submitted and is awaiting Department Manager approval.");
                            alert.showAndWait();
                        } else {
                            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                                    javafx.scene.control.Alert.AlertType.WARNING);
                            alert.setTitle("Request Failed");
                            alert.setHeaderText(null);
                            alert.setContentText("A pending request for this parameter already exists. Wait for it to be approved or rejected first.");
                            alert.showAndWait();
                        }
                        break;

                    case "APPROVE_REQUEST":
                        if (pendingRequestsController != null) {
                            pendingRequestsController.handleApproveResponse(result);
                        }
                        break;

                    case "REJECT_REQUEST":
                        if (pendingRequestsController != null) {
                            pendingRequestsController.handleRejectResponse(result);
                        }
                        break;

                    case "DELETE_GUIDE":
                        if (removeGuideController != null) {
                            removeGuideController.handleDeleteResponse(result);
                        }
                        break;

                    case "DELETE_SUBSCRIBER":
                        if (removeSubscriberController != null) {
                            removeSubscriberController.handleDeleteResponse(result);
                        }
                        break;

                    case "EDIT_GUIDE":
                        if (removeGuideController != null) {
                            removeGuideController.handleEditResponse(result);
                        }
                        break;

                    case "EDIT_SUBSCRIBER":
                        if (removeSubscriberController != null) {
                            removeSubscriberController.handleEditResponse(result);
                        }
                        break;

                    default:
                        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                                result ? javafx.scene.control.Alert.AlertType.INFORMATION
                                       : javafx.scene.control.Alert.AlertType.WARNING);
                        alert.setTitle(result ? "Update Successful" : "Update Failed");
                        alert.setHeaderText(null);
                        alert.setContentText(result ? "Your reservation has been updated successfully!"
                                                    : "Failed to update. Please try again.");
                        alert.showAndWait();
                        break;
                }
            });

        } else if (chat instanceof String) {
            String serverResponse = (String) chat;
            Platform.runLater(() -> {
                if (serverResponse.startsWith("SUCCESS:")) {
                    String[] parts = serverResponse.split(":");
                    String code = parts[1];
                    String price = parts[2];

                    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                            javafx.scene.control.Alert.AlertType.INFORMATION);
                    alert.setTitle("Simulation");
                    alert.setHeaderText("✨ Reservation Confirmed!");
                    alert.setContentText("[SIMULATION] Email & SMS Notification Sent!\n" +
                            "Confirmation Code: " + code + "\n" +
                            "Total Estimated Price: " + price + " NIS\n\n" +
                            "Status: CONFIRMED");
                    alert.showAndWait();

                } else if (serverResponse.startsWith("FULL:")) {
                    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                            javafx.scene.control.Alert.AlertType.WARNING);
                    alert.setTitle("Park Full");
                    alert.setHeaderText("Cannot Complete Booking");
                    alert.setContentText("The selected park is full for this time slot.\n" +
                            "Please try choosing another date, time, or reduce visitor count.");
                    alert.showAndWait();

                } else {
                    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                            javafx.scene.control.Alert.AlertType.ERROR);
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