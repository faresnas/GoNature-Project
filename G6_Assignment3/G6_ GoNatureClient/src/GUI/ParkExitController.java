package GUI;

import java.io.IOException;
import java.util.ArrayList;
import Common.EntryExitResponse;
import Client.ClientUI;
import Client.OrderClient;
import Common.Chat;
import Common.EntryExitResponse;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class ParkExitController {

    @FXML private TextField txtVisitId;
    @FXML private TextField txtManualExitParkId;
    @FXML private TextField txtManualExitVisitors;
    @FXML private TextArea txtResult;
    @FXML private Label lblCurrentVisitors;

    private int workerParkId = 1;

    @FXML
    public void initialize() {
        OrderClient.parkExitController = this;

        if (ClientUI.loggedInUser != null && ClientUI.loggedInUser.getParkId() > 0) {
            workerParkId = ClientUI.loggedInUser.getParkId();
        }

        txtManualExitParkId.setText(String.valueOf(workerParkId));
        refreshCurrentVisitors();
    }

    @FXML
    void registerExit(ActionEvent event) {
        try {
            int visitId = Integer.parseInt(txtVisitId.getText().trim());
            ClientUI.client.sendToServer(new Chat("REGISTER_EXIT", visitId));
        } catch (NumberFormatException e) {
            txtResult.setText("Visit ID must be a number.");
        } catch (Exception e) {
            txtResult.setText("Failed to send exit request: " + e.getMessage());
        }
    }

    @FXML
    void registerManualExit(ActionEvent event) {
        try {
            int parkId = Integer.parseInt(txtManualExitParkId.getText().trim());
            int numVisitors = Integer.parseInt(txtManualExitVisitors.getText().trim());

            if (numVisitors <= 0) {
                txtResult.setText("Number of visitors must be greater than 0.");
                return;
            }

            ArrayList<Object> data = new ArrayList<>();
            data.add(parkId);
            data.add(numVisitors);

            ClientUI.client.sendToServer(new Chat("REGISTER_MANUAL_EXIT", data));

        } catch (NumberFormatException e) {
            txtResult.setText("Park ID and visitors leaving must be numbers.");
        } catch (Exception e) {
            txtResult.setText("Failed to send manual exit request: " + e.getMessage());
        }
    }

    private void refreshCurrentVisitors() {
        try {
            int parkId = Integer.parseInt(txtManualExitParkId.getText().trim());
            ClientUI.client.sendToServer(new Chat("GET_CURRENT_VISITORS", parkId));
        } catch (Exception e) {
            txtResult.setText("Failed to refresh visitor count: " + e.getMessage());
        }
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
    public void handleEntryExitResponse(EntryExitResponse response) {
        lblCurrentVisitors.setText("Current visitors: " + response.getCurrentVisitors());

        StringBuilder sb = new StringBuilder();
        sb.append(response.isSuccess() ? "SUCCESS\n" : "FAILED\n");
        sb.append(response.getMessage()).append("\n\n");

        if (response.getVisitId() > 0) {
            sb.append("Visit ID: ").append(response.getVisitId()).append("\n");
        }

        sb.append("Current Visitors: ").append(response.getCurrentVisitors()).append("\n");

        txtResult.setText(sb.toString());
    }
}