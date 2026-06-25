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

/**
 * The {@code OrderClient} class manages the client-side communication with the GoNature server.
 * It extends {@link AbstractClient} from the OCSF framework to establish network connections,
 * wrap client requests using the {@link Chat} carrier object, and dynamically update various 
 * JavaFX UI controllers on the JavaFX Application Thread upon receiving server responses.
 * * <p>This class keeps track of the {@code lastCommand} submitted to maintain contextual state 
 * for incoming generic list-based or primitive responses.</p>
 * * @author GoNature Development Team
 * @version 1.0
 */
public class OrderClient extends AbstractClient {

    /** Controller for managing user reservations view. */
    public static MyReservationsController myReservationsController;
    
    /** Controller for booking new reservations and checking park availability. */
    public static ReservationController reservationController;
    
    /** Controller for a department manager evaluating pending park updates. */
    public static PendingRequestsController pendingRequestsController;
    
    /** Controller for viewing parameter requests belonging to a specific park. */
    public static UpdateParkParamsController updateParkParamsController;
    
    /** Controller managing the removal/viewing of registered guides. */
    public static RemoveGuideController removeGuideController;
    
    /** Controller managing the removal/viewing of subscribers. */
    public static RemoveSubscriberController removeSubscriberController;
    
    /** Controller responsible for compiling and rendering park occupancy, usage, and cancellation reports. */
    public static GUI.ReportsController reportsController;
    
    /** Controller running the live summary dashboard for a individual park manager. */
    public static ParkManagerDashboardController parkManagerDashboardController;
    
    /** Controller for updating personal guide or subscriber profile information. */
    public static GUI.EditProfileController editProfileController;
    
    /** Controller managing the active waiting list entries for travelers. */
    public static GUI.WaitingListController waitingListController;

    /** Controller operating physical park entry terminal events. */
    public static ParkEntryController parkEntryController;
    
    /** Controller operating physical park exit terminal events. */
    public static ParkExitController parkExitController;
    
    /** Controller displaying live concurrent capacities inside the parks. */
    public static VisitorCountController visitorCountController;

    /** Tracks the latest command identifier string sent out to correctly decipher generic raw array server responses. */
    public static String lastCommand = "";
    
    /** Local list storing contextual data arrays or orders retrieved from the database. */
    public static ArrayList<Order> ordersList;

    /**
     * Constructs an {@code OrderClient} connection instance targeting the defined host address and port number.
     *
     * @param host The remote domain name or IP address of the GoNature server.
     * @param port The port number listening on the server side.
     */
    public OrderClient(String host, int port) {
        super(host, port);
    }

    /**
     * Transmits a request to create a new reservation into the system database.
     *
     * @param reservation The {@link Reservation} object encapsulating traveler details, time, and group volume.
     */
    public void createReservation(Reservation reservation) {
        try {
            sendToServer(new Chat("CREATE_RESERVATION", reservation));
        } catch (Exception e) {
            System.out.println("Failed to create reservation: " + e.getMessage());
        }
    }

    /**
     * Pulls the history list of bookings attached to a specific traveler identification tag.
     *
     * @param travelerId   The unique structural identifier integer of the user.
     * @param travelerType The categorical designation string (e.g., "Subscriber", "Guide", "Regular").
     */
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

    /**
     * Modifies the scheduling parameters of an existing future reservation.
     *
     * @param reservationId The target identifier for the row matching the reservation.
     * @param visitDate     The updated calendar date string (YYYY-MM-DD).
     * @param entryTime     The newly targeted arrival hour string (HH:MM).
     * @param numVisitors   The modified cumulative headcount joining the entry group.
     */
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

    /**
     * Removes an established booking from the system.
     *
     * @param reservationId The system identifier key matching the canceled booking.
     * @param travelerId    The structural identification integer for the customer canceling.
     * @param travelerType  The profile type descriptor string for permission/fee logic handling.
     */
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

    /**
     * Fetches metadata describing all parks operating within the current jurisdiction ecosystem.
     */
    public void requestParks() {
        try {
            lastCommand = "GET_PARKS";
            sendToServer(new Chat("GET_PARKS", null));
        } catch (Exception e) {
            System.out.println("Failed to request parks: " + e.getMessage());
        }
    }

    /**
     * Directs client fields to form and persist a brand-new Subscriber profile on the server database.
     *
     * @param firstName  First name of the subscriber.
     * @param lastName   Last name of the subscriber.
     * @param idNumber   Unique identification card number.
     * @param phone      Contact phone digits.
     * @param email      Electronic mail address.
     * @param familySize Total number of family members registered under the subscription account.
     * @param creditCard Payment card digits associated with the file billing registry.
     */
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

    /**
     * Directs client fields to register a verified Group Guide account on the server database.
     *
     * @param name     Full legal name of the guide.
     * @param email    Electronic mail contact destination.
     * @param phone    Mobile telephone digits.
     * @param idNumber Government issued unique citizen identifier string.
     * @param username Desired unique login username credential.
     * @param password Desired login security clearance phrase string.
     */
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

    /**
     * Creates an official alteration request for a park operation property (e.g., maximum capacity, gap offset).
     * Needs validation by an authoritative department supervisor before changing live environment variables.
     *
     * @param parkId      Numeric index referencing the target national park.
     * @param requestType String keyword indicating which setting is targeted for adjustment.
     * @param newValue    The new capacity index, scale ratio, or time calculation value.
     * @param requestedBy Employee identity token of the park manager initiating the request.
     */
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

    /**
     * Requests the full collection of global pending park configuration modifications awaiting department evaluation.
     */
    public void getPendingRequests() {
        try {
            lastCommand = "GET_PENDING_REQUESTS";
            sendToServer(new Chat("GET_PENDING_REQUESTS", null));
        } catch (Exception e) {
            System.out.println("Failed to get pending requests: " + e.getMessage());
        }
    }

    /**
     * Requests pending park configuration update requests filtered down to a single designated park.
     *
     * @param parkId Numeric index referencing the target national park.
     */
    public void getParkRequests(int parkId) {
        try {
            lastCommand = "GET_PARK_REQUESTS";
            sendToServer(new Chat("GET_PARK_REQUESTS", parkId));
        } catch (Exception e) {
            System.out.println("Failed to get park requests: " + e.getMessage());
        }
    }

    /**
     * Submits an approval action for an ongoing park parameter adjustment request.
     *
     * @param requestId Database structural index matching the configuration task ticket.
     */
    public void approveRequest(int requestId) {
        try {
            lastCommand = "APPROVE_REQUEST";
            sendToServer(new Chat("APPROVE_REQUEST", requestId));
        } catch (Exception e) {
            System.out.println("Failed to approve request: " + e.getMessage());
        }
    }

    /**
     * Submits a rejection action for an ongoing park parameter adjustment request.
     *
     * @param requestId Database structural index matching the configuration task ticket.
     */
    public void rejectRequest(int requestId) {
        try {
            lastCommand = "REJECT_REQUEST";
            sendToServer(new Chat("REJECT_REQUEST", requestId));
        } catch (Exception e) {
            System.out.println("Failed to reject request: " + e.getMessage());
        }
    }

    /**
     * Requests a collection containing all registered Group Guides inside the system.
     */
    public void getAllGuides() {
        try {
            lastCommand = "GET_ALL_GUIDES";
            sendToServer(new Chat("GET_ALL_GUIDES", null));
        } catch (Exception e) {
            System.out.println("Failed to get guides: " + e.getMessage());
        }
    }

    /**
     * Requests a collection containing all registered Subscriber client records inside the system.
     */
    public void getAllSubscribers() {
        try {
            lastCommand = "GET_ALL_SUBSCRIBERS";
            sendToServer(new Chat("GET_ALL_SUBSCRIBERS", null));
        } catch (Exception e) {
            System.out.println("Failed to get subscribers: " + e.getMessage());
        }
    }

    /**
     * Erases a targeted Group Guide profile record from the database registry.
     *
     * @param guideId DB unique reference tracking the target guide profile row.
     */
    public void deleteGuide(int guideId) {
        try {
            lastCommand = "DELETE_GUIDE";
            sendToServer(new Chat("DELETE_GUIDE", guideId));
        } catch (Exception e) {
            System.out.println("Failed to delete guide: " + e.getMessage());
        }
    }

    /**
     * Erases a targeted Subscriber profile record from the database registry.
     *
     * @param subscriberId DB unique reference tracking the target subscriber profile row.
     */
    public void deleteSubscriber(int subscriberId) {
        try {
            lastCommand = "DELETE_SUBSCRIBER";
            sendToServer(new Chat("DELETE_SUBSCRIBER", subscriberId));
        } catch (Exception e) {
            System.out.println("Failed to delete subscriber: " + e.getMessage());
        }
    }

    /**
     * Submits updated descriptive properties over writing an existing Group Guide profile row.
     *
     * @param guideId  Unique primary index key matching the specific record row.
     * @param name     Modified legal name string.
     * @param email    Updated contact electronic mailbox.
     * @param phone    Altered telephone digit series.
     * @param password New account access password.
     */
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

    /**
     * Submits updated descriptive properties over writing an existing Subscriber profile row.
     *
     * @param subscriberId Unique primary index key matching the specific subscriber profile.
     * @param firstName    Modified subscriber personal name.
     * @param lastName     Modified subscriber family name.
     * @param phone        Altered telephone digit series.
     * @param email        Updated contact electronic mailbox.
     * @param familySize   Revised family member count allotment value.
     */
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

    /**
     * Compiles analytical entry metrics and total traffic information regarding a single specified national park.
     *
     * @param parkId Target identifier key mapping to the requested location.
     * @param month  Numerical calendar month query scope (1-12).
     * @param year   Numerical target calendar year query scope.
     */
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

    /**
     * Compiles entry metrics and total traffic across all parks simultaneously for organizational comparison.
     *
     * @param month Numerical calendar month query scope (1-12).
     * @param year  Numerical target calendar year query scope.
     */
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

    /**
     * Asks the server to generate capacity-to-vacancy ratios throughout a monthly period for a specific park.
     *
     * @param parkId Structural reference id matching the national park.
     * @param month  Numerical calendar month target index.
     * @param year   Numerical calendar year target index.
     */
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

    /**
     * Gathers a breakdown detailing unfulfilled, deleted, or missed visitor appointments inside a month interval.
     *
     * @param month Numerical target calendar month index.
     * @param year  Numerical target calendar year index.
     */
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

    /**
     * Sends an request payload pulling all registered orders currently captured across the server framework.
     */
    public void requestAllOrders() {
        try {
            sendToServer(new Chat("GET_ORDERS", null));
        } catch (Exception e) {
            System.out.println("OrderClient: failed to request orders — " + e.getMessage());
        }
    }

    /**
     * Dispatches properties of an adjusted, active order instance to synchronize backend database fields.
     *
     * @param order The {@link Order} model component carrying altered headcount or scheduling configurations.
     */
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

    /**
     * Standardized internal wrapping helper directing structured communications directly up onto the system host stream.
     *
     * @param chat Encapsulated transport class matching server communication protocol standards.
     */
    public void sendToServer(Chat chat) {
        try {
            super.sendToServer(chat);
        } catch (Exception e) {
            System.out.println("OrderClient: failed to send — " + e.getMessage());
        }
    }

    /**
     * Signs out the customer session from active indexing on the host, and securely terminates client sockets.
     */
    public void disconnect() {
        try {
            sendToServer(new Chat("CLIENT_EXIT", null));
            closeConnection();
        } catch (Exception e) {
            System.out.println("OrderClient: failed to disconnect cleanly — " + e.getMessage());
        }
    }

    /**
     * Triggered automatically by OCSF when the communication session closes.
     */
    @Override
    protected void connectionClosed() {
        System.out.println("OrderClient: connection closed");
    }

    /**
     * Triggered automatically by OCSF when an unexpected network connectivity error occurs.
     *
     * @param exception The root exception item tracing the socket failure source.
     */
    @Override
    protected void connectionException(Exception exception) {
        System.out.println("OrderClient: connection error — " + exception.getMessage());
    }

    /**
     * Places a customer reservation block request inside the standby waiting hierarchy list when a destination park is full.
     *
     * @param reservation Contextual details of the desired booking slot.
     */
    public void joinWaitingList(Reservation reservation) {
        try {
            lastCommand = "JOIN_WAITING_LIST";
            sendToServer(new Chat("JOIN_WAITING_LIST", reservation));
        } catch (Exception e) {
            System.out.println("Failed waiting list");
        }
    }

    /**
     * Formulates explicit validation responding to a pre-arrival checking alert reminder system notification.
     *
     * @param reservationId Key index linking back to the designated booking.
     * @param travelerId    The identity index matching the corresponding booking recipient traveler.
     * @param travelerType  Authorization access context profiling key.
     */
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

    /**
     * Audits whether there are outstanding pre-visit confirmation reminders waiting for a specific client.
     *
     * @param travelerId    The structural identification integer for the customer.
     * @param travelerType  The profile type descriptor string for the customer.
     */
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

    /**
     * Commits descriptive modifications regarding the user profile back to database structures.
     *
     * @param data Packed array containing parameters targeted for modification.
     */
    public void updateProfile(ArrayList<Object> data) {
        try {
            lastCommand = "UPDATE_PROFILE";
            sendToServer(new Chat("UPDATE_PROFILE", data));
        } catch (Exception e) {
            System.out.println("Failed to update profile: " + e.getMessage());
        }
    }

    /**
     * Confirms and claims a recently freed appointment slot that opened up for a traveler on the waiting list.
     *
     * @param waitingListId Unique database primary key entry locating the waiting list row.
     * @param travelerId    Target user database identifier string.
     * @param travelerType  User clearance metadata categorization string.
     */
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

    /**
     * Declines a waiting list spot that opened up, allowing the system to pass the opening to the next traveler.
     *
     * @param waitingListId Structural index mapping to the waiting queue track row.
     */
    public void declineWaitingList(int waitingListId) {
        try {
            lastCommand = "DECLINE_WAITING_LIST";
            sendToServer(new Chat("DECLINE_WAITING_LIST", waitingListId));
        } catch (Exception e) {
            System.out.println("Failed to decline waiting list: " + e.getMessage());
        }
    }

    /**
     * Core handler implementation sorting asynchronous down-stream response signals incoming from the server socket.
     * It parses response type instances, wraps presentation inside asynchronous FX platform tasks via 
     * {@link Platform#runLater(Runnable)}, handles fallback defaults, and throws UI warning alert popups.
     *
     * @param chat Polymorphic packet object transmitting server updates.
     */
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

                    if (!data.isEmpty() && !data.get(0).isEmpty() && "WL_PUSH".equals(data.get(0).get(0))) {
                        ArrayList<String> row = data.get(0);
                        row.remove(0); 
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
    
    /**
     * Compiles data detailing total visitor volumes for a specific park during a requested month timeframe.
     *
     * @param parkId Numeric identifier matching the targeted park.
     * @param month  Numerical calendar month query scope (1-12).
     * @param year   Numerical target calendar year query scope.
     */
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

    /**
     * Compiles cross-park data tracking global total visitor volume metrics across all company parks simultaneously.
     *
     * @param month Numerical calendar month query scope (1-12).
     * @param year  Numerical target calendar year query scope.
     */
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
    
    /**
     * Gathers structural cancellations metadata metrics isolated to a single park property.
     *
     * @param parkId Unique structural identifier index for the targeted park.
     * @param month  Numerical target month filter index.
     * @param year   Numerical target year filter index.
     */
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