package GUI;

import java.io.IOException;
import java.util.ArrayList;

import Common.EntryExitResponse;
import Client.ClientUI;
import Client.OrderClient;
import Common.Chat;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

/**
 * Controller for the park exit screen.
 * This class manages visitor exit operations from the park.
 * It allows a park worker to register exits by confirmation code,
 * traveler ID, or manually by entering the number of visitors leaving.
 */
public class ParkExitController {

    /**
     * Text field used to enter a visit ID, confirmation code, or traveler ID.
     */
    @FXML private TextField txtVisitId;

    /**
     * Text field used to enter the number of visitors leaving manually.
     */
    @FXML private TextField txtManualExitVisitors;

    /**
     * Text area used to display operation results and error messages.
     */
    @FXML private TextArea txtResult;

    /**
     * Label used to display the current number of visitors in the park.
     */
    @FXML private Label lblCurrentVisitors;

    /**
     * Label used to display the park name of the logged-in worker.
     */
    @FXML private Label lblParkName;

    /**
     * The ID of the park managed by the logged-in park worker.
     */
    private int workerParkId = 1;

    /**
     * Initializes the park exit screen.
     * The method connects this controller to the OrderClient,
     * loads the worker's park details, displays the park name,
     * and refreshes the current visitor count.
     */
    @FXML
    public void initialize() {
        OrderClient.parkExitController = this;
        OrderClient.parkEntryController = null;

        if (ClientUI.loggedInUser != null && ClientUI.loggedInUser.getParkId() > 0) {
            workerParkId = ClientUI.loggedInUser.getParkId();
        }

        if (lblParkName != null) {
            lblParkName.setText("Park: " + ClientUI.loggedInUser.getParkName());
        }

        refreshCurrentVisitors();
    }

    /**
     * Registers an exit using a confirmation code or traveler ID.
     * The method validates the entered identifier and sends an exit request
     * to the server.
     *
     * @param event the button click event
     */
    @FXML
    void registerExit(ActionEvent event) {
        try {
            String identifier = txtVisitId.getText().trim();
            if (identifier.isEmpty()) {
                txtResult.setText("Please enter confirmation code or traveler ID.");
                return;
            }
            OrderClient.lastCommand = "REGISTER_EXIT";
            ClientUI.client.sendToServer(new Chat("REGISTER_EXIT", identifier));
        } catch (Exception e) {
            txtResult.setText("Failed to send exit request: " + e.getMessage());
        }
    }

    /**
     * Registers a manual exit by entering the number of visitors leaving the park.
     * The method validates that the input is a positive number and sends the data
     * to the server.
     *
     * @param event the button click event
     */
    @FXML
    void registerManualExit(ActionEvent event) {
        try {
            String text = txtManualExitVisitors.getText().trim();
            if (text.isEmpty()) {
                txtResult.setText("Please enter number of visitors leaving.");
                return;
            }
            int numVisitors = Integer.parseInt(text);
            if (numVisitors <= 0) {
                txtResult.setText("Number of visitors must be greater than 0.");
                return;
            }
            ArrayList<Object> data = new ArrayList<>();
            data.add(workerParkId);
            data.add(numVisitors);
            OrderClient.lastCommand = "REGISTER_MANUAL_EXIT";
            ClientUI.client.sendToServer(new Chat("REGISTER_MANUAL_EXIT", data));
        } catch (NumberFormatException e) {
            txtResult.setText("Visitors leaving must be a number.");
        } catch (Exception e) {
            txtResult.setText("Failed to send manual exit request: " + e.getMessage());
        }
    }

    /**
     * Requests the current number of visitors in the worker's park from the server.
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
     * Handles the response returned from the server after an entry or exit operation.
     * The method updates the current visitor label and displays the result message
     * on the screen.
     *
     * @param response the response object received from the server
     */
    public void handleEntryExitResponse(EntryExitResponse response) {
        if (response.isSuccess()) {
            lblCurrentVisitors.setText("Current visitors: " + response.getCurrentVisitors());
        }

        StringBuilder sb = new StringBuilder();
        sb.append(response.isSuccess() ? "SUCCESS\n" : "FAILED\n");
        sb.append(response.getMessage()).append("\n\n");
        if (response.getVisitId() > 0)
            sb.append("Visit ID: ").append(response.getVisitId()).append("\n");
        if (response.isSuccess()) {
            sb.append("Current Visitors: ").append(response.getCurrentVisitors()).append("\n");
            sb.append("Available Spots: ").append(response.getAvailableSpots()).append("\n");
        }
        txtResult.setText(sb.toString());
    }

    /**
     * Returns the park worker to the dashboard screen.
     *
     * @param event the button click event
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