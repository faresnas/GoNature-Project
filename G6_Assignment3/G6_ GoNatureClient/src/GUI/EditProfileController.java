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

/**
 * Controller class for the {@code EditProfile.fxml} view.
 * <p>
 * This class handles the logic for viewing and updating personal profile details
 * within the GoNature application. It supports dynamic UI re-rendering based on the 
 * logged-in user's specific role (Visitor, Subscriber, Guide, or various Employee types).
 * It enforces rigid front-end validations (regex filters for email, phone, and name formatting)
 * before dispatching updates to the server.
 * </p>
 * * @author GoNature Development Team
 * @version 1.0
 */
public class EditProfileController {

    /** Label displaying the primary title text of the profile management view. */
    @FXML private Label lblTitle;
    
    /** Label dynamically populated with the current user's role descriptive string. */
    @FXML private Label lblRole;

    /** Layout wrapper container for the visitor/subscriber/employee first name field. */
    @FXML private VBox rowFirstName;
    
    /** Layout wrapper container for the visitor/subscriber/employee last name field. */
    @FXML private VBox rowLastName;
    
    /** Layout wrapper container for the guide full name field. */
    @FXML private VBox rowFullName;
    
    /** Layout wrapper container for the national identification card field. */
    @FXML private VBox rowIdNumber;
    
    /** Layout wrapper container for the dynamic subscriber family size numeric field. */
    @FXML private VBox rowFamilySize;
    
    /** Layout wrapper container for the profile access password fields. */
    @FXML private VBox rowPassword;
    
    /** Layout wrapper container for the unique employee index registration sequence field. */
    @FXML private VBox rowEmployeeNumber;

    /** Input text field capturing the first name component. */
    @FXML private TextField firstNameField;
    
    /** Input text field capturing the last name component. */
    @FXML private TextField lastNameField;
    
    /** Input text field capturing the combined full name component for guide registries. */
    @FXML private TextField fullNameField;
    
    /** Input text field displaying or capturing citizen identification details. */
    @FXML private TextField idNumberField;
    
    /** Input text field capturing standard mobile or residential contact digits. */
    @FXML private TextField phoneField;
    
    /** Input text field capturing contact electronic mail configurations. */
    @FXML private TextField emailField;
    
    /** Spinner component regulating subscriber maximum cumulative family unit parameters. */
    @FXML private Spinner<Integer> familySizeSpinner;
    
    /** Secure password text field for entering a new security clearance keyphrase. */
    @FXML private PasswordField passwordField;
    
    /** Secure password text field to verify and confirm the new password sequence. */
    @FXML private PasswordField confirmPasswordField;
    
    /** Input text field isolating specific organization employee badge structures. */
    @FXML private TextField employeeNumberField;
    
    /** Layout wrapper container for the guide profile username input row. */
    @FXML private VBox rowUsername;
    
    /** Input text field capturing the distinct login credential key assigned to a guide. */
    @FXML private TextField usernameField;
    
    /** Layout wrapper container for the employee profile username input row. */
    @FXML private VBox rowEmployeeUsername;
    
    /** Input text field capturing the distinct login credential key assigned to an staff member. */
    @FXML private TextField employeeUsernameField;
    
    /** Input text field representing alternative fallback matching targets for raw identifier values. */
    @FXML private TextField idField;
    
    /** Layout wrapper container for the user telephone context rows. */
    @FXML private VBox rowPhone;

    /** Internal state tracker mapping the unified system profile classification (VISITOR, SUBSCRIBER, GUIDE, EMPLOYEE). */
    private String role;
    
    /** Caches the specific target FXML view pathway resource leading back to the originating dashboard. */
    private String backScreen;

    /**
     * Initializes the profile view environment automatically upon building the FXML UI node tree.
     * <p>
     * Caches a reference of this instance onto the centralized {@link OrderClient} reference register,
     * configures family size spinner initialization limits, resolves role classifications, and 
     * manipulates the visual visibility matrix rows to show relevant fields.
     * </p>
     */
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

        rowFirstName.setVisible(false);     rowFirstName.setManaged(false);
        rowLastName.setVisible(false);      rowLastName.setManaged(false);
        rowFullName.setVisible(false);      rowFullName.setManaged(false);
        rowFamilySize.setVisible(false);    rowFamilySize.setManaged(false);
        rowPassword.setVisible(false);      rowPassword.setManaged(false);
        rowEmployeeNumber.setVisible(false);rowEmployeeNumber.setManaged(false);
        rowUsername.setVisible(false);       rowUsername.setManaged(false);
        rowEmployeeUsername.setVisible(false); rowEmployeeUsername.setManaged(false);
        rowPhone.setVisible(false);          rowPhone.setManaged(false);

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
                rowPhone.setVisible(true);     rowPhone.setManaged(true);
                lblRole.setText("Guide Profile");
                fullNameField.setText(ClientUI.loggedInUser.getFirstName());
                usernameField.setText("");
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

    /**
     * Extracts values from the input fields, checks parameter integrity via 
     * formatting validations, and transmits a profile modification array up to the server database.
     *
     * @param event Action activation signal mapping to the profile save button click trigger.
     */
    @FXML
    void handleSave(ActionEvent event) {
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();

        if (!email.isEmpty() && !email.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,4}$")) {
            showAlert(Alert.AlertType.ERROR, "Invalid Email", "Please enter a valid email address.");
            return;
        }

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

    /**
     * Triggered asynchronously on the UI Thread when the server confirms a profile update.
     * <p>
     * Syncs the locally cached session state properties in {@link ClientUI#loggedInUser},
     * launches an informational validation confirmation dialog popup, and dynamically reloads 
     * the user's specific dashboard scene using {@code backScreen}.
     * </p>
     */
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

    /**
     * Aborts the ongoing modification workflow and transfers the active layout scene back 
     * to the user's role-specific dashboard menu view.
     *
     * @param event Action trigger event tied to the back button interaction.
     */
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

    /**
     * Internal formatting helper utility to quickly assemble and display JavaFX Alert popups.
     *
     * @param type    The programmatic classification severity of the notification alert.
     * @param title   The text line displayed inside the title banner region.
     * @param content The contextual body narrative text explaining the cause or corrective rule required.
     */
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}