package GUI;

import java.io.IOException;
import java.util.ArrayList;
import Client.ClientUI;
import Client.OrderClient;
import Common.Chat;
import Common.EntryExitResponse;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

public class VisitorCountController {

    @FXML private ComboBox<String> parkSelector;
    @FXML private TextArea txtResult;
    @FXML private Label lblCurrentVisitors;
    @FXML private Label lblAvailableSpots;

    private boolean isDeptManager = false;
    // parkId -> parkName mapping
    private ArrayList<Integer> parkIds = new ArrayList<>();
    private ArrayList<String> parkNames = new ArrayList<>();

    @FXML
    public void initialize() {
        OrderClient.parkEntryController = null;
        OrderClient.parkExitController = null;
        OrderClient.parkManagerDashboardController = null;
        OrderClient.visitorCountController = this;

        String role = ClientUI.loggedInUser != null ? ClientUI.loggedInUser.getRole() : "";
        isDeptManager = "DEPARTMENT_MANAGER".equals(role);

        if (isDeptManager) {
            Platform.runLater(() -> loadAllParks());
        } else {
            int parkId = ClientUI.loggedInUser != null ? ClientUI.loggedInUser.getParkId() : 1;
            String parkName = ClientUI.loggedInUser != null ? ClientUI.loggedInUser.getParkName() : "Park";
            parkIds.add(parkId);
            parkNames.add(parkName);
            parkSelector.getItems().add(parkName);
            parkSelector.getSelectionModel().selectFirst();
            Platform.runLater(() -> refreshForSelectedPark());
        }
    }
    public void updateVisitorCount(int parkId, int currentCount, int availableSpots) {
        Platform.runLater(() -> {
            int index = parkSelector.getSelectionModel().getSelectedIndex();
            if (index >= 0 && index < parkIds.size() && parkIds.get(index) == parkId) {
                lblCurrentVisitors.setText("Current visitors: " + currentCount);
                if (lblAvailableSpots != null) {
                    lblAvailableSpots.setText("Available spots: " + availableSpots);
                }
                txtResult.setText("SUCCESS\nLive update received.\n\nCurrent Visitors: " 
                    + currentCount + "\nAvailable Spots: " + availableSpots);
            }
        });
    }

    private void loadAllParks() {
        try {
            OrderClient.lastCommand = "GET_PARKS";
            ClientUI.client.sendToServer(new Chat("GET_PARKS", null));
        } catch (Exception e) {
            txtResult.setText("Failed to load parks: " + e.getMessage());
        }
    }

    // Called by OrderClient when parks list arrives (via GET_PARKS)
    public void setParks(ArrayList<ArrayList<String>> parks) {
        Platform.runLater(() -> {
            parkIds.clear();
            parkNames.clear();
            parkSelector.getItems().clear();
            for (ArrayList<String> row : parks) {
                parkIds.add(Integer.parseInt(row.get(0)));
                parkNames.add(row.get(1));
                parkSelector.getItems().add(row.get(1));
            }
            if (!parkSelector.getItems().isEmpty()) {
                parkSelector.getSelectionModel().selectFirst();
                refreshForSelectedPark();
            }
        });
    }

    @FXML
    void onParkSelected(ActionEvent event) {
        refreshForSelectedPark();
    }

    private void refreshForSelectedPark() {
        int index = parkSelector.getSelectionModel().getSelectedIndex();
        if (index < 0 || index >= parkIds.size()) return;
        int parkId = parkIds.get(index);
        try {
            ClientUI.client.sendToServer(new Chat("GET_CURRENT_VISITORS", parkId));
        } catch (Exception e) {
            txtResult.setText("Failed to refresh: " + e.getMessage());
        }
    }

    public void handleEntryExitResponse(EntryExitResponse response) {
        Platform.runLater(() -> {
            lblCurrentVisitors.setText("Current visitors: " + response.getCurrentVisitors());
            if (lblAvailableSpots != null) {
                lblAvailableSpots.setText("Available spots: " + response.getAvailableSpots());
            }
            txtResult.setText(
                (response.isSuccess() ? "SUCCESS\n" : "FAILED\n")
                + response.getMessage()
                + "\n\nCurrent Visitors: " + response.getCurrentVisitors()
                + "\nAvailable Spots: " + response.getAvailableSpots()
            );
        });
    }

    @FXML
    void backToDashboard(ActionEvent event) {
        try {
            String dashboardPath = "/GUI/VisitorDashboard.fxml";
            if (ClientUI.loggedInUser != null && ClientUI.loggedInUser.getRole() != null) {
                switch (ClientUI.loggedInUser.getRole()) {
                    case "DEPARTMENT_MANAGER": dashboardPath = "/GUI/DeptManagerDashboard.fxml"; break;
                    case "PARK_MANAGER":       dashboardPath = "/GUI/ParkManagerDashboard.fxml"; break;
                    case "PARK_WORKER":        dashboardPath = "/GUI/ParkWorkerDashboard.fxml";  break;
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