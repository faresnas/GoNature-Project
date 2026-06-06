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

import java.util.ArrayList;

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

    @FXML
    public void initialize() {
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
        boolean isVisitor = rbVisitor.isSelected();
        boolean isEmployee = rbEmployee.isSelected();

        // username + password shown for employee and guide
        usernameRow.setVisible(!isVisitor);
        usernameRow.setManaged(!isVisitor);
        passwordRow.setVisible(!isVisitor);
        passwordRow.setManaged(!isVisitor);

        // role dropdown only for employee
        roleRow.setVisible(isEmployee);
        roleRow.setManaged(isEmployee);

        // id field only for visitor
        idRow.setVisible(isVisitor);
        idRow.setManaged(isVisitor);

        errorLabel.setText("");
    }

    @FXML
    void connectToServer(ActionEvent event) {
        errorLabel.setText("");

        String serverIP = txtIP.getText().trim();
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
            // Employee
            String username = tfUsername.getText().trim();
            String password = pfPassword.getText().trim();
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

        // disconnect stale connection
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
            openDashboardForRole(response);
        });
    }

    private void openDashboardForRole(LoginResponse response) {
        try {
            String fxml;
            String title;
            String role = response.getRole() == null ? "VISITOR" : response.getRole();
            switch (role) {
                case "PARK_WORKER":        fxml = "/GUI/ParkWorkerDashboard.fxml";       title = "Park Worker";        break;
                case "PARK_MANAGER":       fxml = "/GUI/ParkManagerDashboard.fxml";      title = "Park Manager";       break;
                case "DEPARTMENT_MANAGER": fxml = "/GUI/DeptManagerDashboard.fxml";      title = "Dept Manager";       break;
                case "SERVICE_REP":        fxml = "/GUI/ServiceRepDashboard.fxml";       title = "Service Rep";        break;
                case "GUIDE":              fxml = "/GUI/GuideDashboard.fxml";            title = "Guide";              break;
                default:                   fxml = "/GUI/VisitorDashboard.fxml";          title = "Visitor";            break;
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
    void exit(ActionEvent event) {
        if (ClientUI.client != null) {
            ClientUI.client.disconnect();
        }
        System.exit(0);
    }
}