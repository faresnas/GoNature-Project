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
import GUI.ParkManagerDashboardController;
import GUI.ConnectionController;

public class OrderClient extends AbstractClient {

    public static MyReservationsController myReservationsController;
    public static ReservationController reservationController;
    public static PendingRequestsController pendingRequestsController;
    public static UpdateParkParamsController updateParkParamsController;
    public static RemoveGuideController removeGuideController;
    public static RemoveSubscriberController removeSubscriberController;
    public static GUI.ReportsController reportsController;
    public static ParkManagerDashboardController parkManagerDashboardController;
    public static GUI.EditProfileController editProfileController;
    public static GUI.WaitingListController waitingListController;

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
            lastCommand = "GET_MY_RESERVATIONS";
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
            lastCommand = "GET_PARKS";
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
            String idNumber, String username, String password) {
        try {
            lastCommand = "REGISTER_GUIDE";
            ArrayList<Object> data = new ArrayList<>();
            data.add(name);
            data.add(email);
            data.add(phone);
            data.add(idNumber);
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

    public void getParkRequests(int parkId) {
        try {
            lastCommand = "GET_PARK_REQUESTS";
            sendToServer(new Chat("GET_PARK_REQUESTS", parkId));
        } catch (Exception e) {
            System.out.println("Failed to get park requests: " + e.getMessage());
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

    public void getVisitsReport(int parkId, int month, int year) {
        try {
            lastCommand = "GET_VISITS_REPORT";
            ArrayList<Object> data = new ArrayList<>();
            data.add(parkId);
            data.add(month);
            data.add(year);
            sendToServer(new Chat("GET_VISITS_REPORT", data));
        } catch (Exception e) {
            System.out.println("Failed to get visits report: " + e.getMessage());
        }
    }

    public void getVisitsReportAll(int month, int year) {
        try {
            lastCommand = "GET_VISITS_REPORT_ALL";
            ArrayList<Object> data = new ArrayList<>();
            data.add(month);
            data.add(year);
            sendToServer(new Chat("GET_VISITS_REPORT_ALL", data));
        } catch (Exception e) {
            System.out.println("Failed to get visits report all: " + e.getMessage());
        }
    }

    public void getUsageReport(int parkId, int month, int year) {
        try {
            lastCommand = "GET_USAGE_REPORT";
            ArrayList<Object> data = new ArrayList<>();
            data.add(parkId);
            data.add(month);
            data.add(year);
            sendToServer(new Chat("GET_USAGE_REPORT", data));
        } catch (Exception e) {
            System.out.println("Failed to get usage report: " + e.getMessage());
        }
    }

    public void getCancellationsReport(int month, int year) {
        try {
            lastCommand = "GET_CANCELLATIONS_REPORT";
            ArrayList<Object> data = new ArrayList<>();
            data.add(month);
            data.add(year);
            sendToServer(new Chat("GET_CANCELLATIONS_REPORT", data));
        } catch (Exception e) {
            System.out.println("Failed to get cancellations report: " + e.getMessage());
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

    public void joinWaitingList(Reservation reservation) {
        try {
            lastCommand = "JOIN_WAITING_LIST";
            sendToServer(new Chat("JOIN_WAITING_LIST", reservation));
        } catch (Exception e) {
            System.out.println("Failed waiting list");
        }
    }

    public void confirmReminder(int reservationId, int travelerId, String travelerType) {
        try {
            lastCommand = "CONFIRM_REMINDER";
            ArrayList<Object> data = new ArrayList<>();
            data.add(reservationId);
            data.add(travelerId);
            data.add(travelerType);
            sendToServer(new Chat("CONFIRM_REMINDER", data));
        } catch (Exception e) {
            System.out.println("Failed to confirm reminder: " + e.getMessage());
        }
    }

    public void checkReminders(int travelerId, String travelerType) {
        try {
            lastCommand = "CHECK_REMINDERS";
            ArrayList<Object> data = new ArrayList<>();
            data.add(travelerId);
            data.add(travelerType);
            sendToServer(new Chat("CHECK_REMINDERS", data));
        } catch (Exception e) {
            System.out.println("Failed to check reminders: " + e.getMessage());
        }
    }

    public void updateProfile(ArrayList<Object> data) {
        try {
            lastCommand = "UPDATE_PROFILE";
            sendToServer(new Chat("UPDATE_PROFILE", data));
        } catch (Exception e) {
            System.out.println("Failed to update profile: " + e.getMessage());
        }
    }

    public void confirmWaitingList(int waitingListId, int travelerId, String travelerType) {
        try {
            lastCommand = "CONFIRM_WAITING_LIST";
            ArrayList<Object> data = new ArrayList<>();
            data.add(waitingListId);
            data.add(travelerId);
            data.add(travelerType);
            sendToServer(new Chat("CONFIRM_WAITING_LIST", data));
        } catch (Exception e) {
            System.out.println("Failed to confirm waiting list: " + e.getMessage());
        }
    }

    public void declineWaitingList(int waitingListId) {
        try {
            lastCommand = "DECLINE_WAITING_LIST";
            sendToServer(new Chat("DECLINE_WAITING_LIST", waitingListId));
        } catch (Exception e) {
            System.out.println("Failed to decline waiting list: " + e.getMessage());
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
                } else if (parkExitController != null) {
                    parkExitController.handleEntryExitResponse(response);
                } else if (visitorCountController != null) {
                    visitorCountController.handleEntryExitResponse(response);
                } else if (parkManagerDashboardController != null) {
                    parkManagerDashboardController.handleVisitorCountResponse(response);
                }
            });

        } else if (chat instanceof ArrayList<?>) {
            ArrayList<?> list = (ArrayList<?>) chat;
            Platform.runLater(() -> {

                if (list.isEmpty()) {
                    if (lastCommand.equals("CHECK_REMINDERS")) {
                        if (ClientUI.connectionController != null) {
                            ClientUI.connectionController.handleReminders(new ArrayList<>());
                        }
                        return;
                    }
                    if (lastCommand.equals("GET_WAITING_LIST")) {
                        if (waitingListController != null) {
                            waitingListController.setWaitingListTable(new ArrayList<>());
                        }
                        return;
                    }
                    if (lastCommand.equals("GET_VISITS_REPORT") ||
                            lastCommand.equals("GET_VISITS_REPORT_ALL") ||
                            lastCommand.equals("GET_USAGE_REPORT") ||
                            lastCommand.equals("GET_CANCELLATIONS_REPORT") ||
                            lastCommand.equals("GET_VISITOR_COUNT_REPORT") ||
                            lastCommand.equals("GET_VISITOR_COUNT_REPORT_ALL")) {
                        if (reportsController != null) {
                            reportsController.setReportData(new ArrayList<>());
                        }
                    }
                    return;
                }
                if (!list.isEmpty() && "VISITOR_COUNT_UPDATE".equals(list.get(0))) {
                    int parkId       = (int) list.get(1);
                    int currentCount = (int) list.get(2);
                    int available    = (int) list.get(3);
                    if (visitorCountController != null) {
                        visitorCountController.updateVisitorCount(parkId, currentCount, available);
                    }
                    if (parkManagerDashboardController != null) {
                        // also update park manager dashboard if open
                        Common.EntryExitResponse r = new Common.EntryExitResponse(
                            true, "Live update", 0, currentCount, 0, available);
                        parkManagerDashboardController.handleVisitorCountResponse(r);
                    }
                    return;
                }
                if (!list.isEmpty() && list.get(0) instanceof Integer) {
                    ArrayList<Integer> intList = (ArrayList<Integer>) list;
                    if (lastCommand.equals("CHECK_AVAILABILITY") && reservationController != null) {
                        reservationController.setAvailability(intList.get(0), intList.get(1));
                    }
                    return;
                }
                if (list.get(0) instanceof ArrayList) {
                    ArrayList<ArrayList<String>> data = (ArrayList<ArrayList<String>>) list;

                    // Check for server-pushed waiting list notification
                 // Check for server-pushed waiting list notification
                    if (!data.isEmpty() && !data.get(0).isEmpty() && "WL_PUSH".equals(data.get(0).get(0))) {
                        ArrayList<String> row = data.get(0);
                        row.remove(0); // remove WL_PUSH marker
                        // Add WL marker so handleReminders treats it as waiting list notification
                        row.add(0, "WL");

                        ArrayList<ArrayList<String>> notifList = new ArrayList<>();
                        notifList.add(row);

                        if (ConnectionController.instance != null) {
                            ConnectionController.instance.handleReminders(notifList);
                        }
                        return;
                    }

                    switch (lastCommand) {
                        case "CHECK_REMINDERS":
                            if (ClientUI.connectionController != null) {
                                ClientUI.connectionController.handleReminders(data);
                            }
                            break;

                        case "GET_WAITING_LIST":
                            if (waitingListController != null) {
                                waitingListController.setWaitingListTable(data);
                            }
                            break;

                        case "GET_PENDING_REQUESTS":
                            if (pendingRequestsController != null) {
                                pendingRequestsController.setPendingRequestsTable(data);
                            }
                            break;

                        case "GET_PARK_REQUESTS":
                            if (updateParkParamsController != null) {
                                updateParkParamsController.setRequestsTable(data);
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

                        case "GET_VISITS_REPORT":
                        case "GET_VISITS_REPORT_ALL":
                        case "GET_USAGE_REPORT":
                        case "GET_CANCELLATIONS_REPORT":
                        case "GET_VISITOR_COUNT_REPORT":
                        case "GET_VISITOR_COUNT_REPORT_ALL":
                            if (reportsController != null) {
                                reportsController.setReportData(data);
                            }
                            break;

                        case "GET_PARKS":
                            if (reservationController != null) {
                                reservationController.populateParks(data);
                            } else if (reportsController != null) {
                                reportsController.setParks(data);
                            } else if (visitorCountController != null) {
                                visitorCountController.setParks(data);
                            }
                            break;

                        default:
                            if (myReservationsController != null) {
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
                if (lastCommand.equals("REGISTER_GUIDE")) {
                    if (result == 1) {
                        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                                javafx.scene.control.Alert.AlertType.INFORMATION);
                        alert.setTitle("Guide Registered");
                        alert.setHeaderText("✅ Registration Successful!");
                        alert.setContentText("Group guide registered successfully.");
                        alert.showAndWait();
                    } else if (result == -3) {
                        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                                javafx.scene.control.Alert.AlertType.ERROR);
                        alert.setTitle("Not a Visitor");
                        alert.setHeaderText(null);
                        alert.setContentText("This ID number is not registered as a visitor. The person must register as a visitor first.");
                        alert.showAndWait();
                    } else if (result == -2) {
                        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                                javafx.scene.control.Alert.AlertType.ERROR);
                        alert.setTitle("Username Taken");
                        alert.setHeaderText(null);
                        alert.setContentText("Username already taken. Please choose a different username.");
                        alert.showAndWait();
                    } else {
                        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                                javafx.scene.control.Alert.AlertType.ERROR);
                        alert.setTitle("Registration Failed");
                        alert.setHeaderText(null);
                        alert.setContentText("Failed to register guide. Please try again.");
                        alert.showAndWait();
                    }
                } else {
                    // REGISTER_SUBSCRIBER
                    if (result > 0) {
                        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                                javafx.scene.control.Alert.AlertType.INFORMATION);
                        alert.setTitle("Subscriber Registered");
                        alert.setHeaderText("✅ Registration Successful!");
                        alert.setContentText("Subscriber registered successfully.\nSubscriber Number: " + result);
                        alert.showAndWait();
                    } else if (result == -3) {
                        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                                javafx.scene.control.Alert.AlertType.ERROR);
                        alert.setTitle("Not a Visitor");
                        alert.setHeaderText(null);
                        alert.setContentText("This ID number is not registered as a visitor. The person must register as a visitor first.");
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
                }
            });

        } else if (chat instanceof Boolean) {
            boolean result = (Boolean) chat;
            Platform.runLater(() -> {
                switch (lastCommand) {
                    case "REQUEST_PARK_UPDATE":
                        if (result) {
                            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                                    javafx.scene.control.Alert.AlertType.INFORMATION);
                            alert.setTitle("Request Submitted");
                            alert.setHeaderText("✅ Request Sent!");
                            alert.setContentText("Your park update request has been submitted and is awaiting Department Manager approval.");
                            alert.showAndWait();
                            if (updateParkParamsController != null) {
                                updateParkParamsController.loadRequests();
                            }
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

                    case "UPDATE_PROFILE":
                        if (result) {
                            if (editProfileController != null) {
                                editProfileController.onUpdateSuccess();
                            }
                        } else {
                            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                                    javafx.scene.control.Alert.AlertType.ERROR);
                            alert.setTitle("Update Failed");
                            alert.setHeaderText(null);
                            alert.setContentText("Failed to update profile. Please try again.");
                            alert.showAndWait();
                        }
                        break;

                    case "REGISTER_VISITOR":
                        if (result) {
                            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                                javafx.scene.control.Alert.AlertType.INFORMATION);
                            alert.setTitle("Registration Successful");
                            alert.setHeaderText("✅ Welcome to GoNature!");
                            alert.setContentText("Your account has been created. You can now login with your ID number.");
                            alert.showAndWait();
                            try {
                                if (ClientUI.client != null) {
                                    ClientUI.client.sendToServer(new Chat("CLIENT_EXIT", null));
                                    ClientUI.client.closeConnection();
                                    ClientUI.client = null;
                                }
                            } catch (Exception e) { e.printStackTrace(); }
                            try {
                                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                                    GUI.RegisterVisitorController.class.getResource("/GUI/Connection.fxml"));
                                javafx.scene.Scene scene = new javafx.scene.Scene(loader.load());
                                ClientUI.connectionController = loader.getController();
                                ClientUI.primaryStage.setTitle("GoNature — Connect to Server");
                                ClientUI.primaryStage.setScene(scene);
                            } catch (Exception e) { e.printStackTrace(); }
                        } else {
                            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                                javafx.scene.control.Alert.AlertType.ERROR);
                            alert.setTitle("Registration Failed");
                            alert.setHeaderText(null);
                            alert.setContentText("An account with this ID number already exists.");
                            alert.showAndWait();
                            try {
                                if (ClientUI.client != null) {
                                    ClientUI.client.sendToServer(new Chat("CLIENT_EXIT", null));
                                    ClientUI.client.closeConnection();
                                    ClientUI.client = null;
                                }
                            } catch (Exception e) { e.printStackTrace(); }
                        }
                        break;

                    case "CONFIRM_WAITING_LIST":
                        if (result) {
                            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                                javafx.scene.control.Alert.AlertType.INFORMATION);
                            alert.setTitle("Reservation Confirmed");
                            alert.setHeaderText("✅ You're booked!");
                            alert.setContentText("Your reservation has been created successfully from the waiting list.");
                            alert.showAndWait();
                            try {
                                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                                    getClass().getResource("/GUI/MyReservations.fxml"));
                                javafx.scene.Scene scene = new javafx.scene.Scene(loader.load());
                                ClientUI.primaryStage.setScene(scene);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                                javafx.scene.control.Alert.AlertType.ERROR);
                            alert.setTitle("Failed");
                            alert.setHeaderText(null);
                            alert.setContentText("Failed to confirm reservation. Please try again.");
                            alert.showAndWait();
                        }
                        break;

                    case "DECLINE_WAITING_LIST":
                        break;

                    case "LEAVE_WAITING_LIST":
                        if (waitingListController != null) {
                            waitingListController.handleLeaveResponse(result);
                        }
                        break;
                    case "JOIN_WAITING_LIST":
                        if (result) {
                            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                                javafx.scene.control.Alert.AlertType.INFORMATION);
                            alert.setTitle("Waiting List");
                            alert.setHeaderText("✅ Added to Waiting List!");
                            alert.setContentText("You have been added to the waiting list. You will be notified if a spot opens up.");
                            alert.showAndWait();
                        } else {
                            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                                javafx.scene.control.Alert.AlertType.ERROR);
                            alert.setTitle("Cannot Join Waiting List");
                            alert.setHeaderText(null);
                            alert.setContentText("Your group size exceeds the maximum capacity of this park. You cannot join the waiting list.");
                            alert.showAndWait();
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
                if (serverResponse.equals("REFRESH_REQUESTS")) {
                    if (pendingRequestsController != null) {
                        pendingRequestsController.setPendingRequestsTable(new java.util.ArrayList<>());
                        pendingRequestsController.refreshTablePublic();
                    }
                    if (updateParkParamsController != null) {
                        updateParkParamsController.loadRequests();
                    }
                    return;
                }
                if (serverResponse.startsWith("SUCCESS:")) {
                    String[] parts = serverResponse.split(":");
                    String code  = parts[1];
                    String price = parts[2];
                    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                            javafx.scene.control.Alert.AlertType.INFORMATION);
                    alert.setTitle("Simulation");
                    alert.setHeaderText("✨ Reservation Confirmed!");
                    alert.setContentText("[SIMULATION] Email & SMS Notification Sent!\n" +
                            "Confirmation Code: " + code + "\n" +
                            "Total Estimated Price: " + price + " NIS\n\n" +
                            "Status: PENDING");
                    alert.showAndWait();
                 // Refresh availability label after booking
                    if (reservationController != null) {
                        reservationController.refreshAvailability();
                    }

                } else if (serverResponse.startsWith("FULL:")) {
                    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                            javafx.scene.control.Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Park Full");
                    alert.setHeaderText("⚠️ No Space Available");
                    alert.setContentText("The park is fully booked for this time slot.\n\nWhat would you like to do?");

                    javafx.scene.control.ButtonType waitingListBtn = new javafx.scene.control.ButtonType(
                        "📋 Join Waiting List", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
                    javafx.scene.control.ButtonType altDateBtn = new javafx.scene.control.ButtonType(
                        "📅 Choose Different Date", javafx.scene.control.ButtonBar.ButtonData.OTHER);
                    javafx.scene.control.ButtonType cancelBtn = new javafx.scene.control.ButtonType(
                        "Cancel", javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);
                    alert.getButtonTypes().setAll(waitingListBtn, altDateBtn, cancelBtn);

                    java.util.Optional<javafx.scene.control.ButtonType> result = alert.showAndWait();
                    if (result.isPresent()) {
                        if (result.get() == waitingListBtn) {
                            if (reservationController != null) {
                                reservationController.joinWaitingList();
                            }
                        } else if (result.get() == altDateBtn) {
                            javafx.scene.control.Alert hint = new javafx.scene.control.Alert(
                                javafx.scene.control.Alert.AlertType.INFORMATION);
                            hint.setTitle("Choose Different Date");
                            hint.setHeaderText(null);
                            hint.setContentText("Please select a different date or time and try again.");
                            hint.showAndWait();
                        }
                    }

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
    
    public void getVisitorCountReport(int parkId, int month, int year) {
        try {
            lastCommand = "GET_VISITOR_COUNT_REPORT";
            ArrayList<Object> data = new ArrayList<>();
            data.add(parkId);
            data.add(month);
            data.add(year);
            sendToServer(new Chat("GET_VISITOR_COUNT_REPORT", data));
        } catch (Exception e) {
            System.out.println("Failed to get visitor count report: " + e.getMessage());
        }
    }

    public void getVisitorCountReportAll(int month, int year) {
        try {
            lastCommand = "GET_VISITOR_COUNT_REPORT_ALL";
            ArrayList<Object> data = new ArrayList<>();
            data.add(month);
            data.add(year);
            sendToServer(new Chat("GET_VISITOR_COUNT_REPORT_ALL", data));
        } catch (Exception e) {
            System.out.println("Failed to get visitor count report all: " + e.getMessage());
        }
    }
    
    public void getCancellationsReportByPark(int parkId, int month, int year) {
        try {
            lastCommand = "GET_CANCELLATIONS_REPORT";
            ArrayList<Object> data = new ArrayList<>();
            data.add(parkId);
            data.add(month);
            data.add(year);
            sendToServer(new Chat("GET_CANCELLATIONS_REPORT_BY_PARK", data));
        } catch (Exception e) {
            System.out.println("Failed to get cancellations report by park: " + e.getMessage());
        }
    }
}