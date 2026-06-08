package GUI;

import Client.ClientUI;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class VisitorDashboardController {

    @FXML private Label lblWelcome;

    @FXML
    public void initialize() {
        if (ClientUI.loggedInUser != null) {
            lblWelcome.setText("Welcome, " + ClientUI.loggedInUser.getFirstName()
                + " " + ClientUI.loggedInUser.getLastName());
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
    
    @FXML void logout(ActionEvent event)          { LogoutHelper.logout(); }
    @FXML void exit(ActionEvent event) {
        if (ClientUI.client != null) ClientUI.client.disconnect();
        System.exit(0);
    }
}