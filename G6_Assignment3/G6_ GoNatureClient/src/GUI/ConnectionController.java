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

public class ConnectionController {

    @FXML private TextField txtIP;
    @FXML private TextField txtPort;
    @FXML private Label errorLabel;
    @FXML private Button btnConnect;

    @FXML private RadioButton rbEmployee;
    @FXML private RadioButton rbGuide;
    @FXML private RadioButton rbVisitor;

    @FXML private VBox roleRow;
    @FXML private VBox usernameRow;
    @FXML private VBox passwordRow;
    @FXML private VBox idRow;

    @FXML private ComboBox<String> cbRole;
    @FXML private TextField tfUsername;
    @FXML private PasswordField pfPassword;
    @FXML private TextField tfIdNumber;

    private LoginResponse pendingRole;

    public static ConnectionController instance;

    @FXML
    public void initialize() {
        instance = this; // ADD THIS
        cbRole.getItems().addAll(
            "PARK_WORKER",
            "PARK_MANAGER",
            "DEPARTMENT_MANAGER",
            "SERVICE_REP"
        );
        cbRole.getSelectionModel().selectFirst();
    }
    
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

    public void handleReminders(ArrayList<ArrayList<String>> reminders) {
        Client.OrderClient.lastCommand = "";
        openDashboardForRole(pendingRole);

        if (reminders == null || reminders.isEmpty()) return;

        for (ArrayList<String> row : reminders) {
            // Check if this is a waiting list notification
            if ("WL".equals(row.get(0))) {
                // Waiting list notification
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
                }} else {
                // Regular reminder
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

    private void openDashboardForRole(LoginResponse response) {
        // Clear connectionController so it stops intercepting responses
   
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
                // Distinguish visitor vs subscriber
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
    
    @FXML
    void exit(ActionEvent event) {
        if (ClientUI.client != null) ClientUI.client.disconnect();
        System.exit(0);
    }
}