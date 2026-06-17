package GUI;

import java.io.IOException;

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

public class VisitorCountController {

    @FXML
    private TextField txtParkId;

    @FXML
    private TextArea txtResult;

    @FXML
    private Label lblCurrentVisitors;

    private int selectedParkId = 1;

    @FXML
    public void initialize() {
        OrderClient.parkEntryController = null;
        OrderClient.parkExitController = null;
        OrderClient.visitorCountController = this;

        if (ClientUI.loggedInUser != null
                && ClientUI.loggedInUser.getParkId() != null
                && ClientUI.loggedInUser.getParkId() > 0) {

            selectedParkId = ClientUI.loggedInUser.getParkId();
        }

        txtParkId.setText(String.valueOf(selectedParkId));
        refreshCurrentVisitors(null);
    }

    @FXML
    void refreshCurrentVisitors(ActionEvent event) {
        try {
            int parkId = Integer.parseInt(txtParkId.getText().trim());
            ClientUI.client.sendToServer(new Chat("GET_CURRENT_VISITORS", parkId));

        } catch (NumberFormatException e) {
            txtResult.setText("Park ID must be a number.");

        } catch (Exception e) {
            txtResult.setText("Failed to refresh visitor count: " + e.getMessage());
        }
    }

    public void handleEntryExitResponse(EntryExitResponse response) {
        lblCurrentVisitors.setText("Current visitors: " + response.getCurrentVisitors());

        txtResult.setText(
                (response.isSuccess() ? "SUCCESS\n" : "FAILED\n")
                + response.getMessage()
                + "\n\nCurrent Visitors: "
                + response.getCurrentVisitors()
        );
    }

    @FXML
    void backToDashboard(ActionEvent event) {
        try {
            String dashboardPath = "/GUI/ParkWorkerDashboard.fxml";

            if (ClientUI.loggedInUser != null && ClientUI.loggedInUser.getRole() != null) {
                String role = ClientUI.loggedInUser.getRole();

                if (role.equals("DEPARTMENT_MANAGER")) {
                    dashboardPath = "/GUI/DeptManagerDashboard.fxml";
                } else if (role.equals("PARK_MANAGER")) {
                    dashboardPath = "/GUI/ParkManagerDashboard.fxml";
                } else if (role.equals("PARK_WORKER")) {
                    dashboardPath = "/GUI/ParkWorkerDashboard.fxml";
                }
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(dashboardPath));
            Scene scene = new Scene(loader.load());
            ClientUI.primaryStage.setScene(scene);

        } catch (IOException e) {
            txtResult.setText("Failed to return to dashboard: " + e.getMessage());
        }
    }
}