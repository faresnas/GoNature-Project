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

/**
 * Controller for the Visitor Count screen.
 * This class displays the current number of visitors and available
 * spots in a park. Department managers can select different parks,
 * while park managers and park workers view only their assigned park.
 */
public class VisitorCountController {

    /** Combo box used to select a park. */
    @FXML
    private ComboBox<String> parkSelector;

    /** Text area used to display operation results and status messages. */
    @FXML
    private TextArea txtResult;

    /** Label displaying the current number of visitors. */
    @FXML
    private Label lblCurrentVisitors;

    /** Label displaying the number of available spots. */
    @FXML
    private Label lblAvailableSpots;

    /** Indicates whether the logged-in user is a department manager. */
    private boolean isDeptManager = false;

    /** List containing all park IDs. */
    private ArrayList<Integer> parkIds = new ArrayList<>();

    /** List containing all park names. */
    private ArrayList<String> parkNames = new ArrayList<>();

    /**
     * Initializes the screen after the FXML file is loaded.
     * Registers this controller, determines the user's role,
     * and loads the appropriate park information.
     */
    @FXML
    public void initialize() {
        OrderClient.parkEntryController = null;
        OrderClient.parkExitController = null;
        OrderClient.parkManagerDashboardController = null;
        OrderClient.visitorCountController = this;

        String role = ClientUI.loggedInUser != null
                ? ClientUI.loggedInUser.getRole()
                : "";

        isDeptManager = "DEPARTMENT_MANAGER".equals(role);

        if (isDeptManager) {
            Platform.runLater(this::loadAllParks);
        } else {
            int parkId = ClientUI.loggedInUser != null
                    ? ClientUI.loggedInUser.getParkId()
                    : 1;

            String parkName = ClientUI.loggedInUser != null
                    ? ClientUI.loggedInUser.getParkName()
                    : "Park";

            parkIds.add(parkId);
            parkNames.add(parkName);
            parkSelector.getItems().add(parkName);
            parkSelector.getSelectionModel().selectFirst();

            Platform.runLater(this::refreshForSelectedPark);
        }
    }

    /**
     * Updates the visitor count displayed on the screen.
     *
     * @param parkId the updated park ID
     * @param currentCount the current number of visitors
     * @param availableSpots the number of available spots
     */
    public void updateVisitorCount(int parkId,
                                   int currentCount,
                                   int availableSpots) {

        Platform.runLater(() -> {

            int index = parkSelector.getSelectionModel().getSelectedIndex();

            if (index >= 0 &&
                index < parkIds.size() &&
                parkIds.get(index) == parkId) {

                lblCurrentVisitors.setText("Current visitors: " + currentCount);

                if (lblAvailableSpots != null) {
                    lblAvailableSpots.setText(
                            "Available spots: " + availableSpots);
                }

                txtResult.setText(
                        "SUCCESS\nLive update received.\n\n"
                        + "Current Visitors: " + currentCount
                        + "\nAvailable Spots: " + availableSpots);
            }
        });
    }

    /**
     * Requests the complete list of parks from the server.
     */
    private void loadAllParks() {
        try {
            OrderClient.lastCommand = "GET_PARKS";
            ClientUI.client.sendToServer(new Chat("GET_PARKS", null));
        } catch (Exception e) {
            txtResult.setText("Failed to load parks: " + e.getMessage());
        }
    }

    /**
     * Loads the parks received from the server into the park selector.
     *
     * @param parks the list of parks received from the server
     */
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

    /**
     * Refreshes the displayed visitor information
     * when a different park is selected.
     *
     * @param event the selection event
     */
    @FXML
    void onParkSelected(ActionEvent event) {
        refreshForSelectedPark();
    }

    /**
     * Requests the current visitor count for the selected park.
     */
    private void refreshForSelectedPark() {

        int index = parkSelector.getSelectionModel().getSelectedIndex();

        if (index < 0 || index >= parkIds.size()) {
            return;
        }

        int parkId = parkIds.get(index);

        try {
            ClientUI.client.sendToServer(
                    new Chat("GET_CURRENT_VISITORS", parkId));
        } catch (Exception e) {
            txtResult.setText("Failed to refresh: " + e.getMessage());
        }
    }

    /**
     * Handles the response received from the server
     * and updates the visitor information displayed.
     *
     * @param response the server response containing visitor statistics
     */
    public void handleEntryExitResponse(EntryExitResponse response) {

        Platform.runLater(() -> {

            lblCurrentVisitors.setText(
                    "Current visitors: " + response.getCurrentVisitors());

            if (lblAvailableSpots != null) {
                lblAvailableSpots.setText(
                        "Available spots: " + response.getAvailableSpots());
            }

            txtResult.setText(
                    (response.isSuccess() ? "SUCCESS\n" : "FAILED\n")
                    + response.getMessage()
                    + "\n\nCurrent Visitors: "
                    + response.getCurrentVisitors()
                    + "\nAvailable Spots: "
                    + response.getAvailableSpots());
        });
    }

    /**
     * Returns the user to the appropriate dashboard
     * according to the logged-in user's role.
     *
     * @param event the button click event
     */
    @FXML
    void backToDashboard(ActionEvent event) {

        try {

            String dashboardPath = "/GUI/VisitorDashboard.fxml";

            if (ClientUI.loggedInUser != null &&
                ClientUI.loggedInUser.getRole() != null) {

                switch (ClientUI.loggedInUser.getRole()) {

                    case "DEPARTMENT_MANAGER":
                        dashboardPath = "/GUI/DeptManagerDashboard.fxml";
                        break;

                    case "PARK_MANAGER":
                        dashboardPath = "/GUI/ParkManagerDashboard.fxml";
                        break;

                    case "PARK_WORKER":
                        dashboardPath = "/GUI/ParkWorkerDashboard.fxml";
                        break;
                }
            }

            FXMLLoader loader =
                    new FXMLLoader(getClass().getResource(dashboardPath));

            Scene scene = new Scene(loader.load());
            ClientUI.primaryStage.setScene(scene);

        } catch (IOException e) {
            txtResult.setText(
                    "Failed to return to dashboard: " + e.getMessage());
        }
    }
}