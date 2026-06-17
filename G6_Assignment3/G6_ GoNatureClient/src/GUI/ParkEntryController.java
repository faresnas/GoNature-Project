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
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class ParkEntryController {

    @FXML
    private TextField txtReservationIdentifier;

    @FXML
    private TextField txtWalkInParkId;

    @FXML
    private TextField txtWalkInVisitors;

    @FXML
    private ComboBox<String> cmbWalkInType;

    @FXML
    private TextArea txtResult;

    @FXML
    private Label lblCurrentVisitors;

    private int workerParkId = 1;

    @FXML
    public void initialize() {
        OrderClient.parkEntryController = this;

        if (cmbWalkInType != null) {
            cmbWalkInType.setItems(FXCollections.observableArrayList(
                    "INDIVIDUAL",
                    "GROUP",
                    "SUBSCRIBER"
            ));
            cmbWalkInType.getSelectionModel().select("INDIVIDUAL");
        }

        if (ClientUI.loggedInUser != null && ClientUI.loggedInUser.getParkId() > 0) {
            workerParkId = ClientUI.loggedInUser.getParkId();
        }

        if (txtWalkInParkId != null) {
            txtWalkInParkId.setText(String.valueOf(workerParkId));
        }

        refreshCurrentVisitors();
    }

    @FXML
    void approveReservationEntry(ActionEvent event) {
        String identifier = txtReservationIdentifier.getText().trim();

        if (identifier.isEmpty()) {
            txtResult.setText("Please enter traveler ID or confirmation code.");
            return;
        }

        try {
            ClientUI.client.sendToServer(new Chat("ENTRY_WITH_RESERVATION", identifier));
        } catch (Exception e) {
            txtResult.setText("Failed to send reservation entry request: " + e.getMessage());
        }
    }

    @FXML
    void approveWalkInEntry(ActionEvent event) {
        try {
            int parkId = Integer.parseInt(txtWalkInParkId.getText().trim());
            int numVisitors = Integer.parseInt(txtWalkInVisitors.getText().trim());
            String visitorType = cmbWalkInType.getValue();

            if (numVisitors <= 0) {
                txtResult.setText("Number of visitors must be greater than 0.");
                return;
            }

            ArrayList<Object> data = new ArrayList<>();
            data.add(parkId);
            data.add(numVisitors);
            data.add(visitorType);

            ClientUI.client.sendToServer(new Chat("WALK_IN_ENTRY", data));

        } catch (NumberFormatException e) {
            txtResult.setText("Park ID and number of visitors must be numbers.");
        } catch (Exception e) {
            txtResult.setText("Failed to send walk-in request: " + e.getMessage());
        }
    }

    private void refreshCurrentVisitors() {
        try {
            int parkId = workerParkId;

            if (txtWalkInParkId != null && !txtWalkInParkId.getText().trim().isEmpty()) {
                parkId = Integer.parseInt(txtWalkInParkId.getText().trim());
            }

            ClientUI.client.sendToServer(new Chat("GET_CURRENT_VISITORS", parkId));

        } catch (Exception e) {
            if (txtResult != null) {
                txtResult.setText("Failed to refresh visitor count: " + e.getMessage());
            }
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

        if (response.getAmountToPay() > 0) {
            sb.append("Payment Bill: ").append(response.getAmountToPay()).append(" NIS\n");
        }

        sb.append("Current Visitors: ").append(response.getCurrentVisitors()).append("\n");

        txtResult.setText(sb.toString());
    }

    @FXML
    void backToDashboard(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/GUI/ParkWorkerDashboard.fxml"));

            Scene scene = new Scene(loader.load());
            ClientUI.primaryStage.setScene(scene);

        } catch (IOException e) {
            txtResult.setText("Failed to return to dashboard: " + e.getMessage());
        }
    }
}