package GUI;

import Client.ClientUI;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;

public class VisitorDashboardController {

    @FXML private Label lblWelcome;
    @FXML private Label lblDashboard;

    @FXML
    public void initialize() {
        if (ClientUI.loggedInUser != null) {
            lblWelcome.setText("Welcome, " + ClientUI.loggedInUser.getFirstName()
                + " " + ClientUI.loggedInUser.getLastName());
            String userType = ClientUI.loggedInUser.getUserType() != null
                              ? ClientUI.loggedInUser.getUserType().toString() : "";
            if ("SUBSCRIBER".equals(userType)) {
                lblDashboard.setText("Subscriber Dashboard");
            } else {
                lblDashboard.setText("Visitor Dashboard");
            }
        }
    }

    @FXML
    public void makeReservation(ActionEvent event) {
        	System.out.println("MY RESERVATIONS CLICKED");
    	   try {
            javafx.fxml.FXMLLoader loader =
                new javafx.fxml.FXMLLoader(getClass().getResource("/GUI/Reservation.fxml"));

            javafx.scene.Scene scene = new javafx.scene.Scene(loader.load());

            ClientUI.primaryStage.setTitle("GoNature — Make Reservation");
            ClientUI.primaryStage.setScene(scene);

        } catch (Exception e) {
            System.out.println("Failed to open reservation screen.");
            e.printStackTrace();
        }
    }
    
    @FXML
    public void myReservations(ActionEvent event) {
      	System.out.println("MY RESERVATIONS CLICKED");
    	  try {
            javafx.fxml.FXMLLoader loader =
                new javafx.fxml.FXMLLoader(getClass().getResource("/GUI/MyReservations.fxml"));

            javafx.scene.Scene scene = new javafx.scene.Scene(loader.load());

            ClientUI.primaryStage.setTitle("GoNature — My Reservations");
            ClientUI.primaryStage.setScene(scene);

        } catch (Exception e) {
            System.out.println("Failed to open my reservations screen.");
            e.printStackTrace();
        }
    }
    @FXML
    void editProfile(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GUI/EditProfile.fxml"));
            Scene scene = new Scene(loader.load());
            ClientUI.primaryStage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    void openWaitingList(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GUI/WaitingList.fxml"));
            Scene scene = new Scene(loader.load());
            ClientUI.primaryStage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @FXML void logout(ActionEvent event)          { LogoutHelper.logout(); }
    @FXML void exit(ActionEvent event) {
        if (ClientUI.client != null) ClientUI.client.disconnect();
        System.exit(0);
    }
}