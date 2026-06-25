package GUI;

import Client.ClientUI;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;

/**
 * Controller for the Subscriber Registration screen.
 * This class allows a service representative to register
 * a new subscriber by entering personal details, family size,
 * and an optional credit card number.
 * All user input is validated before being sent to the server.
 */
public class RegisterSubscriberController {

    /**
     * Text field used to enter the subscriber's first name.
     */
    @FXML
    private TextField firstNameField;

    /**
     * Text field used to enter the subscriber's last name.
     */
    @FXML
    private TextField lastNameField;

    /**
     * Text field used to enter the subscriber's ID number.
     */
    @FXML
    private TextField idNumberField;

    /**
     * Text field used to enter the subscriber's phone number.
     */
    @FXML
    private TextField phoneField;

    /**
     * Text field used to enter the subscriber's email address.
     */
    @FXML
    private TextField emailField;

    /**
     * Spinner used to select the subscriber's family size.
     */
    @FXML
    private Spinner<Integer> familySizeSpinner;

    /**
     * Text field used to enter the subscriber's credit card number.
     * This field is optional.
     */
    @FXML
    private TextField creditCardField;

    /**
     * Initializes the registration screen.
     * Configures the family size spinner and allows
     * users to enter values manually.
     */
    @FXML
    public void initialize() {
        familySizeSpinner.setValueFactory(
            new SpinnerValueFactory.IntegerSpinnerValueFactory(
                Integer.MIN_VALUE,
                Integer.MAX_VALUE,
                1)
        );

        familySizeSpinner.setEditable(true);
    }

    /**
     * Registers a new subscriber.
     * The method validates all entered information and sends
     * the registration request to the server if all values are valid.
     *
     * @param event the button click event
     */
    @FXML
    void handleRegister(ActionEvent event) {

        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String idNumber = idNumberField.getText().trim();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();
        String creditCard = creditCardField.getText().trim();

        String rawFamily = familySizeSpinner.getEditor().getText().trim();

        int familySize;
        try {
            familySize = Integer.parseInt(rawFamily);
        } catch (NumberFormatException e) {
            familySize = 0;
        }

        if (familySize < 1) {
            showAlert(Alert.AlertType.ERROR,
                    "Invalid Family Size",
                    "Family size must be at least 1.");
            return;
        }

        if (familySize > 15) {
            showAlert(Alert.AlertType.ERROR,
                    "Invalid Family Size",
                    "Family size cannot exceed 20.");
            return;
        }

        if (firstName.isEmpty() || lastName.isEmpty() || idNumber.isEmpty()
                || phone.isEmpty() || email.isEmpty()) {

            showAlert(Alert.AlertType.WARNING,
                    "Missing Fields",
                    "Please fill in all required fields.");
            return;
        }

        if (!email.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,4}$")) {
            showAlert(Alert.AlertType.ERROR,
                    "Invalid Email",
                    "Please enter a valid email address.");
            return;
        }

        if (!idNumber.matches("\\d+")) {
            showAlert(Alert.AlertType.ERROR,
                    "Invalid ID",
                    "ID number must contain digits only.");
            return;
        }

        if (!phone.matches("\\d+")) {
            showAlert(Alert.AlertType.ERROR,
                    "Invalid Phone",
                    "Phone number must contain digits only.");
            return;
        }

        ClientUI.client.registerSubscriber(
                firstName,
                lastName,
                idNumber,
                phone,
                email,
                familySize,
                creditCard.isEmpty() ? null : creditCard);
    }

    /**
     * Returns the user to the Service Representative Dashboard.
     *
     * @param event the button click event
     */
    @FXML
    void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/GUI/ServiceRepDashboard.fxml"));

            Scene scene = new Scene(loader.load());
            ClientUI.primaryStage.setScene(scene);

        } catch (Exception e) {
            System.out.println("Failed to go back: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Displays an alert dialog with the specified message.
     *
     * @param type the type of alert
     * @param title the alert window title
     * @param content the message displayed to the user
     */
    private void showAlert(Alert.AlertType type,
                           String title,
                           String content) {

        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}