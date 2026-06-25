package GUI;

import java.io.IOException;
import java.util.ArrayList;

import Client.ClientUI;
import Client.OrderClient;
import Common.Chat;
import Common.EntryExitResponse;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

/**
 * Controller for the park entry screen.
 * <p>
 * This controller allows a park worker to approve visitor entry
 * either by reservation identifier or as a walk-in entry. It also
 * displays the current number of visitors, available spots, park name,
 * and billing information when payment is required.
 * </p>
 *
 * @author Fares
 * @version 1.0
 */
public class ParkEntryController {

    /**
     * Text field used to enter a traveler ID or confirmation code.
     */
    @FXML
    private TextField txtReservationIdentifier;

    /**
     * Text field used to enter the number of walk-in visitors.
     */
    @FXML
    private TextField txtWalkInVisitors;

    /**
     * Combo box used to select the walk-in visitor type.
     */
    @FXML
    private ComboBox<String> cmbWalkInType;

    /**
     * Text area used to display operation results and messages.
     */
    @FXML
    private TextArea txtResult;

    /**
     * Label that displays the current number of visitors in the park.
     */
    @FXML
    private Label lblCurrentVisitors;

    /**
     * Label that displays the number of available spots in the park.
     */
    @FXML
    private Label lblAvailableSpots;

    /**
     * Label that displays the park name.
     */
    @FXML
    private Label lblParkName;

    /**
     * The ID of the park assigned to the logged-in park worker.
     */
    private int workerParkId = 1;

    /**
     * Initializes the park entry screen.
     * <p>
     * This method links the current controller to {@link OrderClient},
     * resets the exit controller reference, initializes the walk-in type
     * combo box, loads the worker's assigned park, displays the park name,
     * and requests the current visitor count.
     * </p>
     */
    @FXML
    public void initialize() {
        OrderClient.parkEntryController = this;
        OrderClient.parkExitController = null;

        if (cmbWalkInType != null) {
            cmbWalkInType.setItems(FXCollections.observableArrayList(
                "INDIVIDUAL", "GROUP", "SUBSCRIBER"
            ));
            cmbWalkInType.getSelectionModel().select("INDIVIDUAL");
        }

        if (ClientUI.loggedInUser != null && ClientUI.loggedInUser.getParkId() > 0) {
            workerParkId = ClientUI.loggedInUser.getParkId();
        }

        if (lblParkName != null) {
            lblParkName.setText("Park: " + ClientUI.loggedInUser.getParkName());
        }

        refreshCurrentVisitors();
    }

    /**
     * Sends a request to approve entry for a visitor with an existing reservation.
     * <p>
     * The method reads the traveler ID or confirmation code from the input field
     * and sends it to the server for validation.
     * </p>
     *
     * @param event the action event triggered by the user.
     */
    @FXML
    void approveReservationEntry(ActionEvent event) {
        String identifier = txtReservationIdentifier.getText().trim();
        if (identifier.isEmpty()) {
            txtResult.setText("Please enter traveler ID or confirmation code.");
            return;
        }
        try {
            OrderClient.lastCommand = "ENTRY_WITH_RESERVATION";
            ClientUI.client.sendToServer(new Chat("ENTRY_WITH_RESERVATION", identifier));
        } catch (Exception e) {
            txtResult.setText("Failed to send reservation entry request: " + e.getMessage());
        }
    }

    /**
     * Sends a request to approve entry for walk-in visitors.
     * <p>
     * The method validates the number of visitors, reads the selected visitor type,
     * and sends the walk-in entry data to the server.
     * </p>
     *
     * @param event the action event triggered by the user.
     */
    @FXML
    void approveWalkInEntry(ActionEvent event) {
        try {
            String visitorsText = txtWalkInVisitors.getText().trim();
            if (visitorsText.isEmpty()) {
                txtResult.setText("Please enter number of visitors.");
                return;
            }
            int numVisitors = Integer.parseInt(visitorsText);
            if (numVisitors <= 0) {
                txtResult.setText("Number of visitors must be greater than 0.");
                return;
            }
            String visitorType = cmbWalkInType.getValue();
            ArrayList<Object> data = new ArrayList<>();
            data.add(workerParkId);
            data.add(numVisitors);
            data.add(visitorType);
            OrderClient.lastCommand = "WALK_IN_ENTRY";
            ClientUI.client.sendToServer(new Chat("WALK_IN_ENTRY", data));
        } catch (NumberFormatException e) {
            txtResult.setText("Number of visitors must be a number.");
        } catch (Exception e) {
            txtResult.setText("Failed to send walk-in request: " + e.getMessage());
        }
    }

    /**
     * Requests the current number of visitors in the worker's park.
     */
    private void refreshCurrentVisitors() {
        try {
            OrderClient.lastCommand = "GET_CURRENT_VISITORS";
            ClientUI.client.sendToServer(new Chat("GET_CURRENT_VISITORS", workerParkId));
        } catch (Exception e) {
            if (txtResult != null)
                txtResult.setText("Failed to refresh visitor count: " + e.getMessage());
        }
    }

    /**
     * Handles the server response after an entry attempt.
     * <p>
     * If the operation succeeds, the visitor count labels are updated.
     * If a payment amount exists, a billing popup is displayed to the
     * park worker. The result area is updated in all cases.
     * </p>
     *
     * @param response the entry or exit response received from the server.
     */
    public void handleEntryExitResponse(EntryExitResponse response) {
        if (response.isSuccess()) {
            lblCurrentVisitors.setText("Current visitors: " + response.getCurrentVisitors());
            if (lblAvailableSpots != null) {
                lblAvailableSpots.setText("Available spots: " + response.getAvailableSpots());
            }

            // Show billing popup if a bill was generated (entry events only)
            if (response.getAmountToPay() > 0) {
                showBillPopup(response);
            }
        }

        // Always update the result text area
        StringBuilder sb = new StringBuilder();
        sb.append(response.isSuccess() ? "✅ SUCCESS\n" : "❌ FAILED\n");
        sb.append(response.getMessage()).append("\n\n");
        if (response.getVisitId() > 0)
            sb.append("Visit ID: ").append(response.getVisitId()).append("\n");
        if (response.getAmountToPay() > 0)
            sb.append("Payment Bill: ").append(String.format("%.2f", response.getAmountToPay())).append(" NIS\n");
        if (response.isSuccess()) {
            sb.append("Current Visitors: ").append(response.getCurrentVisitors()).append("\n");
            sb.append("Available Spots: ").append(response.getAvailableSpots()).append("\n");
        }
        txtResult.setText(sb.toString());
    }

    /**
     * Displays a payment bill dialog to the park worker.
     * <p>
     * The worker presents this bill to the visitors before allowing entry.
     * The actual payment process is handled outside the GoNature system.
     * </p>
     *
     * @param response the response containing the visit ID and amount to pay.
     */
    private void showBillPopup(EntryExitResponse response) {
        Alert billAlert = new Alert(Alert.AlertType.INFORMATION);
        billAlert.setTitle("Simulation — Payment Bill");
        billAlert.setHeaderText("💰 Payment Bill — Present to Visitors");

        String billContent =
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
            "          GoNature Park Entry\n" +
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
            "Visit ID:        " + response.getVisitId() + "\n\n" +
            "TOTAL AMOUNT DUE:\n\n" +
            "  ➤  " + String.format("%.2f", response.getAmountToPay()) + " NIS\n\n" +
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
            "Payment is collected outside the\n" +
            "GoNature system.\n" +
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━";

        billAlert.setContentText(billContent);

        // Style the dialog
        DialogPane dialogPane = billAlert.getDialogPane();
        dialogPane.setStyle(
            "-fx-background-color: #1a3a1a;" +
            "-fx-font-family: 'Courier New';" +
            "-fx-font-size: 13;"
        );
        dialogPane.lookup(".content.label").setStyle(
            "-fx-text-fill: #f0f7f0;" +
            "-fx-font-family: 'Courier New';" +
            "-fx-font-size: 13;"
        );
        if (dialogPane.lookup(".header-panel") != null) {
            dialogPane.lookup(".header-panel").setStyle(
                "-fx-background-color: #0f2210;"
            );
        }
        if (dialogPane.lookup(".header-panel .label") != null) {
            dialogPane.lookup(".header-panel .label").setStyle(
                "-fx-text-fill: #7ec87e; -fx-font-weight: bold; -fx-font-size: 14;"
            );
        }

        billAlert.getButtonTypes().setAll(
            new ButtonType("✅ Bill Presented — Allow Entry")
        );

        billAlert.showAndWait();
    }

    /**
     * Returns the user to the Park Worker dashboard.
     *
     * @param event the action event triggered by the user.
     */
    @FXML
    void backToDashboard(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GUI/ParkWorkerDashboard.fxml"));
            Scene scene = new Scene(loader.load());
            ClientUI.primaryStage.setScene(scene);
        } catch (IOException e) {
            txtResult.setText("Failed to return to dashboard: " + e.getMessage());
        }
    }
}