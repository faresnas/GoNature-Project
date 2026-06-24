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

public class ParkExitController {

    @FXML private TextField txtVisitId;
    @FXML private TextField txtManualExitVisitors;
    @FXML private TextArea txtResult;
    @FXML private Label lblCurrentVisitors;
    @FXML private Label lblParkName;

    private int workerParkId = 1;

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

    private void refreshCurrentVisitors() {
        try {
            OrderClient.lastCommand = "GET_CURRENT_VISITORS";
            ClientUI.client.sendToServer(new Chat("GET_CURRENT_VISITORS", workerParkId));
        } catch (Exception e) {
            if (txtResult != null)
                txtResult.setText("Failed to refresh visitor count: " + e.getMessage());
        }
    }

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