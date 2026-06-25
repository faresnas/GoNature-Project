package GUI;

import Client.ClientUI;
import Common.Chat;
import Common.LoginResponse;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.control.ButtonBar;

import java.util.ArrayList;
import java.util.Optional;

/**
 * Controller class for the {@code Connection.fxml} view.
 * <p>
 * This class handles the initial network connection setup to the GoNature server and 
 * manages user authentication (Login) based on three main profiles: Visitors, 
 * Guides, and Employees. It dynamically adjusts input form fields based on the selected 
 * profile, handles contextual alerts (such as booking reminders and waiting list clearances), 
 * and routes successfully authenticated users to their corresponding dashboards.
 * </p>
 * * @author GoNature Development Team
 * @version 1.0
 */
public class ConnectionController {

    /** Text field for entering the remote server IP address. */
    @FXML private TextField txtIP;
    
    /** Text field for entering the remote server listening port. */
    @FXML private TextField txtPort;
    
    /** Label to display validation errors and connection status messages to the user. */
    @FXML private Label errorLabel;
    
    /** Trigger button to establish connection and attempt authentication. */
    @FXML private Button btnConnect;

    /** Toggle radio button indicating the user is an organizational employee. */
    @FXML private RadioButton rbEmployee;
    
    /** Toggle radio button indicating the user is an authorized group guide. */
    @FXML private RadioButton rbGuide;
    
    /** Toggle radio button indicating the user is a standard visitor or subscriber. */
    @FXML private RadioButton rbVisitor;

    /** Container wrapper holding the functional employee role combo-box field. */
    @FXML private VBox roleRow;
    
    /** Container wrapper holding the username credential text field. */
    @FXML private VBox usernameRow;
    
    /** Container wrapper holding the secure password entry field. */
    @FXML private VBox passwordRow;
    
    /** Container wrapper holding the traveler citizenship identification input field. */
    @FXML private VBox idRow;

    /** ComboBox permitting selections among the different organizational employee variants. */
    @FXML private ComboBox<String> cbRole;
    
    /** Text field capturing employee/guide authentication usernames. */
    @FXML private TextField tfUsername;
    
    /** Secure text field hiding input keystrokes for employee/guide passwords. */
    @FXML private PasswordField pfPassword;
    
    /** Text field capturing traveler national identification card sequences. */
    @FXML private TextField tfIdNumber;

    /** Temporarily buffers authentication payloads for travelers during nested reminder status check queries. */
    private LoginResponse pendingRole;

    /** Globally accessible reference instance tracking the active instance of this connection panel. */
    public static ConnectionController instance;

    /**
     * Initializes the controller automatically upon loading the FXML tree stage.
     * <p>
     * Caches the singleton {@code instance} identifier and Populates the underlying 
     * employee role ComboBox selection layout with default authorized classifications.
     * </p>
     */
    @FXML
    public void initialize() {
        instance = this;
        cbRole.getItems().addAll(
            "PARK_WORKER",
            "PARK_MANAGER",
            "DEPARTMENT_MANAGER",
            "SERVICE_REP"
        );
        cbRole.getSelectionModel().selectFirst();
    }
    
    /**
     * Fired whenever a RadioButton context shift event is triggered on the UI panel.
     * <p>
     * Dynamically manipulates layout rendering visibility and structural spacing bounds 
     * of credential inputs (Username, Password, ID, Roles) relative to the chosen archetype.
     * </p>
     */
    @FXML
    private void onToggleChanged() {
        boolean isVisitor  = rbVisitor.isSelected();
        boolean isEmployee = rbEmployee.isSelected();

        usernameRow.setVisible(!isVisitor);
        usernameRow.setManaged(!isVisitor);
        passwordRow.setVisible(!isVisitor);
        passwordRow.setManaged(!isVisitor);
        roleRow.setVisible(isEmployee);
        roleRow.setManaged(isEmployee);
        idRow.setVisible(isVisitor);
        idRow.setManaged(isVisitor);

        errorLabel.setText("");
    }

    /**
     * Evaluates text field configurations, instantiates a local client network socket, 
     * and forwards an authentication request payload toward the remote server host stream.
     *
     * @param event The action event capturing the interaction trigger on the connection button.
     */
    @FXML
    void connectToServer(ActionEvent event) {
        errorLabel.setText("");

        String serverIP   = txtIP.getText().trim();
        String serverPort = txtPort.getText().trim();

        if (serverIP.isEmpty() || serverPort.isEmpty()) {
            errorLabel.setText("IP address and port are required.");
            return;
        }

        int port;
        try {
            port = Integer.parseInt(serverPort);
        } catch (NumberFormatException e) {
            errorLabel.setText("Port must be a valid number.");
            return;
        }

        ArrayList<String> loginData = new ArrayList<>();

        if (rbVisitor.isSelected()) {
            String idNumber = tfIdNumber.getText().trim();
            if (idNumber.isEmpty()) {
                errorLabel.setText("Please enter your ID number.");
                return;
            }
            loginData.add("VISITOR");
            loginData.add(idNumber);

        } else if (rbGuide.isSelected()) {
            String username = tfUsername.getText().trim();
            String password = pfPassword.getText().trim();
            if (username.isEmpty() || password.isEmpty()) {
                errorLabel.setText("Please enter username and password.");
                return;
            }
            loginData.add("GUIDE");
            loginData.add(username);
            loginData.add(password);

        } else {
            String username     = tfUsername.getText().trim();
            String password     = pfPassword.getText().trim();
            String selectedRole = cbRole.getSelectionModel().getSelectedItem();
            if (username.isEmpty() || password.isEmpty()) {
                errorLabel.setText("Please enter username and password.");
                return;
            }
            if (selectedRole == null) {
                errorLabel.setText("Please select a role.");
                return;
            }
            loginData.add("EMPLOYEE");
            loginData.add(username);
            loginData.add(password);
            loginData.add(selectedRole);
        }

        if (ClientUI.client != null) {
            try { ClientUI.client.closeConnection(); } catch (Exception ignored) {}
            ClientUI.client = null;
        }

        try {
            ClientUI.client = new Client.OrderClient(serverIP, port);
            ClientUI.client.openConnection();
        } catch (Exception e) {
            errorLabel.setText("Could not connect to server — check IP and port.");
            return;
        }

        btnConnect.setDisable(true);
        errorLabel.setText("Connecting...");
        ClientUI.client.sendToServer(new Chat("LOGIN_REQUEST", loginData));
    }

    /**
     * Handles incoming login evaluation confirmations received back from the host system.
     * <p>
     * If successful, checks whether the client requires reminder audits or directly routes 
     * the session payload forward into contextual organizational profile dashboard frames.
     * </p>
     *
     * @param response The structured {@link LoginResponse} instance carrying state flags and message attributes.
     */
    public void handleLoginResponse(LoginResponse response) {
        Platform.runLater(() -> {
            btnConnect.setDisable(false);
            if (!response.isSuccess()) {
                errorLabel.setText(response.getMessage());
                try {
                    if (ClientUI.client != null) {
                        ClientUI.client.sendToServer(new Chat("CLIENT_EXIT", null));
                        ClientUI.client.closeConnection();
                    }
                } catch (Exception ignored) {}
                ClientUI.client = null;
                return;
            }
            ClientUI.loggedInUser = response;

            String userType = response.getUserType() != null
                              ? response.getUserType().toString() : "";
            String role     = response.getRole() != null ? response.getRole() : "";

            boolean needsReminderCheck = "VISITOR".equals(userType)
                || "SUBSCRIBER".equals(userType)
                || "GUIDE".equals(role);

            if (needsReminderCheck) {
                String travelerType = "GUIDE".equals(role) ? "GUIDE"
                    : "SUBSCRIBER".equals(userType) ? "SUBSCRIBER" : "VISITOR";
                pendingRole = response;
                ClientUI.client.checkReminders(response.getUserId(), travelerType);
            } else {
                openDashboardForRole(response);
            }
        });
    }

    /**
     * Intercepts and parses reminder packages belonging to the authenticated traveler session.
     * <p>
     * Iterates incoming collections to sort and trigger JavaFX prompt alerts representing 
     * either explicit day-prior booking validation warnings, or real-time waiting list slot clearances.
     * </p>
     *
     * @param reminders A nested two-dimensional matrix containing categorized property structures of waiting notifications.
     */
    public void handleReminders(ArrayList<ArrayList<String>> reminders) {
        Client.OrderClient.lastCommand = "";
        openDashboardForRole(pendingRole);

        if (reminders == null || reminders.isEmpty()) return;

        for (ArrayList<String> row : reminders) {
            if ("WL".equals(row.get(0))) {
                String waitingId  = row.get(1);
                String parkName   = row.get(2);
                String visitDate  = row.get(3);
                String entryTime  = row.get(4);
                String visitors   = row.get(5);
                String expiresAt  = row.get(6);

                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("🎉 Spot Available!");
                alert.setHeaderText("A spot opened up on the waiting list!");
                alert.setContentText(
                    "Park: " + parkName + "\n" +
                    "Date: " + visitDate + "\n" +
                    "Time: " + entryTime + "\n" +
                    "Visitors: " + visitors + "\n\n" +
                    "Offer expires at: " + expiresAt + "\n\n" +
                    "Do you want to confirm this reservation?"
                );

                ButtonType confirmBtn = new ButtonType("✔ Confirm", ButtonBar.ButtonData.OK_DONE);
                ButtonType declineBtn = new ButtonType("✖ Decline", ButtonBar.ButtonData.CANCEL_CLOSE);
                alert.getButtonTypes().setAll(confirmBtn, declineBtn);

                Optional<ButtonType> result = alert.showAndWait();
                int wlId = Integer.parseInt(waitingId);

                String userType = (pendingRole != null && pendingRole.getUserType() != null)
                        ? pendingRole.getUserType().toString()
                        : (ClientUI.loggedInUser != null && ClientUI.loggedInUser.getUserType() != null)
                        ? ClientUI.loggedInUser.getUserType().toString() : "";
                String roleStr  = (pendingRole != null && pendingRole.getRole() != null)
                        ? pendingRole.getRole()
                        : (ClientUI.loggedInUser != null && ClientUI.loggedInUser.getRole() != null)
                        ? ClientUI.loggedInUser.getRole() : "";
                int userId = (pendingRole != null) ? pendingRole.getUserId()
                   : (ClientUI.loggedInUser != null) ? ClientUI.loggedInUser.getUserId() : -1;
                String travelerType = "GUIDE".equals(roleStr) ? "GUIDE"
                        : "SUBSCRIBER".equals(userType) ? "SUBSCRIBER" : "VISITOR";

                if (result.isPresent() && result.get() == confirmBtn) {
                    ClientUI.client.confirmWaitingList(wlId, userId, travelerType);
                } else {
                    ClientUI.client.declineWaitingList(wlId);
                }
            } else {
                String reservationId = row.get(0);
                String parkName      = row.get(1);
                String visitDate     = row.get(2);
                String entryTime     = row.get(3);
                String visitors      = row.get(4);
                String code          = row.get(5);

                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("⏰ Visit Reminder");
                alert.setHeaderText("You have a reservation tomorrow!");
                alert.setContentText(
                    "Park: " + parkName + "\n" +
                    "Date: " + visitDate + "\n" +
                    "Time: " + entryTime + "\n" +
                    "Visitors: " + visitors + "\n" +
                    "Confirmation Code: " + code + "\n\n" +
                    "Do you confirm this visit?\n" +
                    "You have 2 hours to confirm or it will be cancelled automatically."
                );

                ButtonType confirmBtn = new ButtonType("✔ Confirm", ButtonBar.ButtonData.OK_DONE);
                ButtonType cancelBtn  = new ButtonType("✖ Cancel Visit", ButtonBar.ButtonData.CANCEL_CLOSE);
                alert.getButtonTypes().setAll(confirmBtn, cancelBtn);

                Optional<ButtonType> result = alert.showAndWait();
                int resId = Integer.parseInt(reservationId);

                String userType = pendingRole.getUserType() != null
                                  ? pendingRole.getUserType().toString() : "";
                String roleStr  = pendingRole.getRole() != null ? pendingRole.getRole() : "";
                String travelerType = "GUIDE".equals(roleStr) ? "GUIDE"
                    : "SUBSCRIBER".equals(userType) ? "SUBSCRIBER" : "VISITOR";

                if (result.isPresent() && result.get() == confirmBtn) {
                    ClientUI.client.confirmReminder(resId, pendingRole.getUserId(), travelerType);
                } else {
                    ClientUI.client.deleteReservation(resId, pendingRole.getUserId(), travelerType);
                }
            }
        }
    }

    /**
     * Determines and launches the specific FXML dashboard scenery corresponding to the validated user role.
     *
     * @param response The successful {@link LoginResponse} token mapping the verified security rights context.
     */
    private void openDashboardForRole(LoginResponse response) {
        try {
            String fxml;
            String title;
            String role = response.getRole() == null ? "VISITOR" : response.getRole();
            switch (role) {
                case "PARK_WORKER":        fxml = "/GUI/ParkWorkerDashboard.fxml";  title = "Park Worker";  break;
                case "PARK_MANAGER":       fxml = "/GUI/ParkManagerDashboard.fxml"; title = "Park Manager"; break;
                case "DEPARTMENT_MANAGER": fxml = "/GUI/DeptManagerDashboard.fxml"; title = "Dept Manager"; break;
                case "SERVICE_REP":        fxml = "/GUI/ServiceRepDashboard.fxml";  title = "Service Rep";  break;
                case "GUIDE":              fxml = "/GUI/GuideDashboard.fxml";       title = "Guide";        break;
                default:
                    String userType = response.getUserType() != null ? response.getUserType().toString() : "";
                    if ("SUBSCRIBER".equals(userType)) {
                        fxml = "/GUI/VisitorDashboard.fxml";
                        title = "Subscriber";
                    } else {
                        fxml = "/GUI/VisitorDashboard.fxml";
                        title = "Visitor";
                    }
                    break;
            }
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Scene scene = new Scene(loader.load());
            ClientUI.primaryStage.setTitle("GoNature — " + title);
            ClientUI.primaryStage.setScene(scene);
        } catch (Exception e) {
            errorLabel.setText("Failed to open dashboard.");
            e.printStackTrace();
        }
    }
    
    /**
     * Re-routes the primary screen stage layout toward the brand-new structural visitor account sign-up layout scene.
     * Requires verifying input network configurations before proceeding.
     *
     * @param event Action trigger event tied to the register hyperlink element.
     */
    @FXML
    void openRegisterVisitor(ActionEvent event) {
        String serverIP   = txtIP.getText().trim();
        String serverPort = txtPort.getText().trim();

        if (serverIP.isEmpty() || serverPort.isEmpty()) {
            errorLabel.setText("Please enter IP and port before registering.");
            return;
        }

        int port;
        try {
            port = Integer.parseInt(serverPort);
        } catch (NumberFormatException e) {
            errorLabel.setText("Port must be a valid number.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/GUI/RegisterVisitor.fxml"));
            Scene scene = new Scene(loader.load());
            RegisterVisitorController controller = loader.getController();
            controller.initConnection(serverIP, port);
            ClientUI.primaryStage.setTitle("GoNature — Register Visitor");
            ClientUI.primaryStage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Safely closes existing host application connection vectors and completely terminates the runtime execution space.
     *
     * @param event Close button execution context trigger action.
     */
    @FXML
    void exit(ActionEvent event) {
        if (ClientUI.client != null) ClientUI.client.disconnect();
        System.exit(0);
    }
}