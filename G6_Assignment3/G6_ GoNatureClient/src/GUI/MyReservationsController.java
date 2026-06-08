package GUI;

import Client.ClientUI;
import Client.OrderClient;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.util.ArrayList;

public class MyReservationsController {

    @FXML
    private TableView<ArrayList<String>> reservationsTable;

    @FXML
    private TableColumn<ArrayList<String>, String> idCol;

    @FXML
    private TableColumn<ArrayList<String>, String> parkCol;

    @FXML
    private TableColumn<ArrayList<String>, String> dateCol;

    @FXML
    private TableColumn<ArrayList<String>, String> timeCol;

    @FXML
    private TableColumn<ArrayList<String>, String> visitorsCol;

    @FXML
    private TableColumn<ArrayList<String>, String> typeCol;

    @FXML
    private TableColumn<ArrayList<String>, String> statusCol;

    @FXML
    private TableColumn<ArrayList<String>, String> codeCol;

    @FXML
    private TextField dateField;

    @FXML
    private TextField timeField;

    @FXML
    private TextField visitorsField;

    private ObservableList<ArrayList<String>> tableData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // קישור זמני של בקר זה לתוך מחלקת הליבה של הלקוח כדי שיוכל להעביר נתונים חזרה לטבלה
        OrderClient.myReservationsController = this;

        // הגדרת הקישורים של העמודות בטבלה למערך הנתונים הדינמי שחוזר מהשרת
        idCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(0)));
        parkCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(1)));
        dateCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(2)));
        timeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(3)));
        visitorsCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(4)));
        typeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(5)));
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(6)));
        codeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(7)));

        // האזנה לבחירת שורה בטבלה - מילוי אוטומטי של שדות העריכה למטה
        reservationsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                dateField.setText(newSelection.get(2));
                timeField.setText(newSelection.get(3));
                visitorsField.setText(newSelection.get(4));
            }
        });

        // שליחת בקשה אוטומטית לשרת לטעינת ההזמנות של המשתמש הנוכחי
        refreshTable();
    }

    private void refreshTable() {
        if (ClientUI.loggedInUser != null) {
            int id = ClientUI.loggedInUser.getUserId();
            String role = ClientUI.loggedInUser.getRole();
            String travelerType = (role != null && role.equals("GUIDE")) ? "GUIDE" : "VISITOR";
            
            ClientUI.client.requestMyReservations(id, travelerType);
        } else {
            // לצרכי טסטים מבודדים ללא התחברות קודמת
            ClientUI.client.requestMyReservations(111111111, "VISITOR");
        }
    }

    // מתודה זו נקראת בצורה אוטומטית מתוך ה-OrderClient ברגע שהשרת מחזיר את רשימת השורות
    public void setReservationsTable(ArrayList<ArrayList<String>> rows) {
        tableData.clear();
        tableData.addAll(rows);
        reservationsTable.setItems(tableData);
    }

    @FXML
    void handleUpdateReservation(ActionEvent event) {
        ArrayList<String> selectedRow = reservationsTable.getSelectionModel().getSelectedItem();
        
        if (selectedRow == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Selection Missing");
            alert.setHeaderText(null);
            alert.setContentText("Please select a reservation from the table to update.");
            alert.showAndWait();
            return;
        }

        if (dateField.getText().isEmpty() || timeField.getText().isEmpty() || visitorsField.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Missing Information");
            alert.setHeaderText(null);
            alert.setContentText("Please fill in all the edit fields before updating.");
            alert.showAndWait();
            return;
        }

        try {
            int reservationId = Integer.parseInt(selectedRow.get(0));
            String newDate = dateField.getText();
            String newTime = timeField.getText();
            int newVisitors = Integer.parseInt(visitorsField.getText());

            // הגבלת כמות משתתפים למדריך (הזמנה קבוצתית) גם בזמן עריכה
            if ("GROUP".equals(selectedRow.get(5)) && newVisitors > 15) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Group Limit Exceeded");
                alert.setHeaderText(null);
                alert.setContentText("Group reservations cannot exceed 15 visitors.");
                alert.showAndWait();
                return;
            }

            // שליחת בקשת העדכון לשרת (השרת יבדוק זמינות ויחזיר תשובה שתקפיץ פופ-אפ)
            ClientUI.client.updateReservation(reservationId, newDate, newTime, newVisitors);
            
            // רענון אוטומטי של הטבלה מה-DB להצגת הנתונים החדשים
            refreshTable();

        } catch (IllegalArgumentException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Format Error");
            alert.setHeaderText(null);
            alert.setContentText("Please ensure the date is YYYY-MM-DD and time is HH:MM:SS.");
            alert.showAndWait();
        }
    }

    @FXML
    void handleGoBack(ActionEvent event) {
        try {
            String screen;

            if (ClientUI.loggedInUser != null && "GUIDE".equals(ClientUI.loggedInUser.getRole())) {
                screen = "/GUI/GuideDashboard.fxml";
            } else {
                screen = "/GUI/VisitorDashboard.fxml";
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(screen));
            Scene scene = new Scene(loader.load());
            ClientUI.primaryStage.setScene(scene);

        } catch (Exception e) {
            System.out.println("Failed to redirect back to dashboard");
            e.printStackTrace();
        }
    }
}