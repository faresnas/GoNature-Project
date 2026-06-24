package GUI;

import Client.ClientUI;
import Client.OrderClient;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.util.ArrayList;

public class EditProfileController {

    @FXML private Label lblTitle;
    @FXML private Label lblRole;

    @FXML private VBox rowFirstName;
    @FXML private VBox rowLastName;
    @FXML private VBox rowFullName;
    @FXML private VBox rowIdNumber;
    @FXML private VBox rowFamilySize;
    @FXML private VBox rowPassword;
    @FXML private VBox rowEmployeeNumber;

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField fullNameField;
    @FXML private TextField idNumberField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private Spinner<Integer> familySizeSpinner;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField employeeNumberField;
    @FXML private VBox rowUsername;
    @FXML private TextField usernameField;
    @FXML private VBox rowEmployeeUsername;
    @FXML private TextField employeeUsernameField;
    @FXML private TextField idField;
    private String role;
    private String backScreen;
    @FXML private VBox rowPhone;

    @FXML
    public void initialize() {
        OrderClient.editProfileController = this;

        familySizeSpinner.setValueFactory(
            new SpinnerValueFactory.IntegerSpinnerValueFactory(Integer.MIN_VALUE, Integer.MAX_VALUE, 1)
        );
        familySizeSpinner.setEditable(true);

        if (ClientUI.loggedInUser == null) return;

        String userRole = ClientUI.loggedInUser.getRole();
        String userType = ClientUI.loggedInUser.getUserType() != null
                          ? ClientUI.loggedInUser.getUserType().toString() : "";

        // Determine role string and back screen
        if ("GUIDE".equals(userRole)) {
            role = "GUIDE";
            backScreen = "/GUI/GuideDashboard.fxml";
        } else if ("PARK_WORKER".equals(userRole) || "PARK_MANAGER".equals(userRole) ||
                   "DEPARTMENT_MANAGER".equals(userRole) || "SERVICE_REP".equals(userRole)) {
            role = "EMPLOYEE";
            switch (userRole) {
                case "PARK_WORKER":        backScreen = "/GUI/ParkWorkerDashboard.fxml"; break;
                case "PARK_MANAGER":       backScreen = "/GUI/ParkManagerDashboard.fxml"; break;
                case "DEPARTMENT_MANAGER": backScreen = "/GUI/DeptManagerDashboard.fxml"; break;
                case "SERVICE_REP":        backScreen = "/GUI/ServiceRepDashboard.fxml"; break;
            }
        } else if ("SUBSCRIBER".equals(userType)) {
            role = "SUBSCRIBER";
            backScreen = "/GUI/VisitorDashboard.fxml";
        } else {
            role = "VISITOR";
            backScreen = "/GUI/VisitorDashboard.fxml";
        }

        // Show/hide fields based on role
        rowFirstName.setVisible(false);     rowFirstName.setManaged(false);
        rowLastName.setVisible(false);      rowLastName.setManaged(false);
        rowFullName.setVisible(false);      rowFullName.setManaged(false);
        rowFamilySize.setVisible(false);    rowFamilySize.setManaged(false);
        rowPassword.setVisible(false);      rowPassword.setManaged(false);
        rowEmployeeNumber.setVisible(false);rowEmployeeNumber.setManaged(false);
        rowUsername.setVisible(false); rowUsername.setManaged(false);
        rowEmployeeUsername.setVisible(false); rowEmployeeUsername.setManaged(false);
        // employees have no phone column

        rowPhone.setVisible(false); rowPhone.setManaged(false);
        
        

        switch (role) {
            case "VISITOR":
                rowFirstName.setVisible(true);     rowFirstName.setManaged(true);
                rowLastName.setVisible(true);      rowLastName.setManaged(true);
                lblRole.setText("Visitor Profile");
                firstNameField.setText(ClientUI.loggedInUser.getFirstName());
                lastNameField.setText(ClientUI.loggedInUser.getLastName());
                rowIdNumber.setVisible(true); rowIdNumber.setManaged(true);
                rowPhone.setVisible(true); rowPhone.setManaged(true);
                break;
            case "SUBSCRIBER":
                rowFirstName.setVisible(true);     rowFirstName.setManaged(true);
                rowLastName.setVisible(true);      rowLastName.setManaged(true);
                lblRole.setText("Subscriber Profile");
                firstNameField.setText(ClientUI.loggedInUser.getFirstName());
                lastNameField.setText(ClientUI.loggedInUser.getLastName());
                rowIdNumber.setVisible(true); rowIdNumber.setManaged(true);
                rowPhone.setVisible(true); rowPhone.setManaged(true);
                break;
            case "GUIDE":
                rowFullName.setVisible(true);  rowFullName.setManaged(true);
                rowUsername.setVisible(true);  rowUsername.setManaged(true);
                rowPassword.setVisible(true);  rowPassword.setManaged(true);
                rowPhone.setVisible(true); rowPhone.setManaged(true);
                lblRole.setText("Guide Profile");
                fullNameField.setText(ClientUI.loggedInUser.getFirstName());
                // Load username from DB — for now pre-fill from login
                usernameField.setText("");  // will be loaded
                break;
            case "EMPLOYEE":
                rowFirstName.setVisible(true);      rowFirstName.setManaged(true);
                rowLastName.setVisible(true);       rowLastName.setManaged(true);
                rowPassword.setVisible(true);       rowPassword.setManaged(true);
                rowEmployeeNumber.setVisible(true); rowEmployeeNumber.setManaged(true);
                rowEmployeeUsername.setVisible(true); rowEmployeeUsername.setManaged(true);
                lblRole.setText(ClientUI.loggedInUser.getRole() + " Profile");
                firstNameField.setText(ClientUI.loggedInUser.getFirstName());
                lastNameField.setText(ClientUI.loggedInUser.getLastName());
                if (employeeNumberField != null) {
                    employeeNumberField.setText("Emp #" + ClientUI.loggedInUser.getUserId());
                    employeeNumberField.setEditable(false);
                }
                break;
        }

        emailField.setText(ClientUI.loggedInUser.getEmail() != null
            ? ClientUI.loggedInUser.getEmail() : "");
    }

    @FXML
    void handleSave(ActionEvent event) {
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();

        // Email validation
        if (!email.isEmpty() && !email.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,4}$")) {
            showAlert(Alert.AlertType.ERROR, "Invalid Email", "Please enter a valid email address.");
            return;
        }

        // Phone validation — digits only, not empty
        if (!"EMPLOYEE".equals(role)) {
            if (phone.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Missing Phone", "Phone number is required.");
                return;
            }
            if (!phone.matches("\\d{7,15}")) {
                showAlert(Alert.AlertType.ERROR, "Invalid Phone",
                    "Phone must contain 7 to 15 digits only.");
                return;
            }
        }

        int userId = ClientUI.loggedInUser.getUserId();
        ArrayList<Object> data = new ArrayList<>();
        data.add(role);
        data.add(userId);

        switch (role) {
            case "VISITOR": {
                String firstName = firstNameField.getText().trim();
                String lastName  = lastNameField.getText().trim();
                if (firstName.isEmpty() || lastName.isEmpty()) {
                    showAlert(Alert.AlertType.WARNING, "Missing Fields", "First and last name are required.");
                    return;
                }
                if (!firstName.matches("[a-zA-Z\\u0590-\\u05FF ]+")) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Name", "First name must contain letters only.");
                    return;
                }
                if (!lastName.matches("[a-zA-Z\\u0590-\\u05FF ]+")) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Name", "Last name must contain letters only.");
                    return;
                }
                String idNumber = idField.getText().trim();
                if (!idNumber.isEmpty() && !idNumber.matches("\\d+")) {
                    showAlert(Alert.AlertType.ERROR, "Invalid ID", "ID number must contain digits only.");
                    return;
                }
                data.add(firstName);
                data.add(lastName);
                data.add(phone);
                data.add(email);
                data.add(idNumber.isEmpty() ? null : idNumber);
                break;
                
            }
            case "SUBSCRIBER": {
                String firstName = firstNameField.getText().trim();
                String lastName  = lastNameField.getText().trim();
                if (firstName.isEmpty() || lastName.isEmpty()) {
                    showAlert(Alert.AlertType.WARNING, "Missing Fields", "First and last name are required.");
                    return;
                }
                if (!firstName.matches("[a-zA-Z\\u0590-\\u05FF ]+")) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Name", "First name must contain letters only.");
                    return;
                }
                if (!lastName.matches("[a-zA-Z\\u0590-\\u05FF ]+")) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Name", "Last name must contain letters only.");
                    return;
                }
                data.add(firstName);
                data.add(lastName);
                data.add(phone);
                data.add(email);
                break;
            }
            case "GUIDE": {
                String fullName = fullNameField.getText().trim();
                String username = usernameField.getText().trim();
                String password = passwordField.getText().trim();
                String confirm  = confirmPasswordField.getText().trim();

                if (fullName.isEmpty()) {
                    showAlert(Alert.AlertType.WARNING, "Missing Fields", "Name is required.");
                    return;
                }
                if (!fullName.matches("[a-zA-Z\\u0590-\\u05FF ]+")) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Name", "Name must contain letters only.");
                    return;
                }
                if (username.isEmpty()) {
                    showAlert(Alert.AlertType.WARNING, "Missing Fields", "Username is required.");
                    return;
                }
                if (!username.matches("[a-zA-Z0-9_]+")) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Username",
                        "Username can only contain letters, numbers and underscores.");
                    return;
                }
                if (!password.isEmpty() && !password.equals(confirm)) {
                    showAlert(Alert.AlertType.ERROR, "Password Mismatch", "Passwords do not match.");
                    return;
                }
                if (!password.isEmpty() && password.length() < 4) {
                    showAlert(Alert.AlertType.ERROR, "Weak Password", "Password must be at least 4 characters.");
                    return;
                }
                // Order must match EchoServer: 2=name, 3=username, 4=phone, 5=email, 6=password
                data.add(fullName);
                data.add(username);
                data.add(phone);
                data.add(email);
                data.add(password.isEmpty() ? null : password);
                break;
            }
            case "EMPLOYEE": {
                String firstName = firstNameField.getText().trim();
                String lastName  = lastNameField.getText().trim();
                if (firstName.isEmpty() || lastName.isEmpty()) {
                    showAlert(Alert.AlertType.WARNING, "Missing Fields", "First and last name are required.");
                    return;
                }
                if (!firstName.matches("[a-zA-Z\\u0590-\\u05FF ]+")) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Name", "First name must contain letters only.");
                    return;
                }
                if (!lastName.matches("[a-zA-Z\\u0590-\\u05FF ]+")) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Name", "Last name must contain letters only.");
                    return;
                }
                String password = passwordField.getText().trim();
                String confirm  = confirmPasswordField.getText().trim();
                if (!password.isEmpty() && !password.equals(confirm)) {
                    showAlert(Alert.AlertType.ERROR, "Password Mismatch", "Passwords do not match.");
                    return;
                }
                if (!password.isEmpty() && password.length() < 4) {
                    showAlert(Alert.AlertType.ERROR, "Weak Password", "Password must be at least 4 characters.");
                    return;
                }
                String empUsername = employeeUsernameField.getText().trim();
                if (empUsername.isEmpty()) {
                    showAlert(Alert.AlertType.WARNING, "Missing Fields", "Username is required.");
                    return;
                }
                if (!empUsername.matches("[a-zA-Z0-9_]+")) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Username",
                        "Username can only contain letters, numbers and underscores.");
                    return;
                }
                data.add(firstName);
                data.add(lastName);
                data.add(email);
                data.add(empUsername);
                data.add(password.isEmpty() ? null : password);
                break;
            }
        }

        ClientUI.client.updateProfile(data);
    }

    public void onUpdateSuccess() {
        if ("VISITOR".equals(role) || "SUBSCRIBER".equals(role) || "EMPLOYEE".equals(role)) {
            ClientUI.loggedInUser.setFirstName(firstNameField.getText().trim());
            ClientUI.loggedInUser.setLastName(lastNameField.getText().trim());
        } else if ("GUIDE".equals(role)) {
            ClientUI.loggedInUser.setFirstName(fullNameField.getText().trim());
        }
        ClientUI.loggedInUser.setEmail(emailField.getText().trim());

        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
            javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("Profile Updated");
        alert.setHeaderText("✅ Success!");
        alert.setContentText("Your profile has been updated successfully.");
        alert.showAndWait();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(backScreen));
            Scene scene = new Scene(loader.load());
            ClientUI.primaryStage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(backScreen));
            Scene scene = new Scene(loader.load());
            ClientUI.primaryStage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}