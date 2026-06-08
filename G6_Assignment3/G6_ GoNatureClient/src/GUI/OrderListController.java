package GUI;

import Client.ClientUI;
import data.Order;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.util.ArrayList;

public class OrderListController {

    @FXML
    private TableView<ArrayList<String>> ordersTable;

    @FXML
    private TableColumn<ArrayList<String>, String> idCol;

    @FXML
    private TableColumn<ArrayList<String>, String> dateCol;

    @FXML
    private TableColumn<ArrayList<String>, String> visitorsCol;

    @FXML
    private TextField orderNumField;

    @FXML
    private TextField visitDateField;

    @FXML
    private TextField visitorCountField;

    @FXML
    private TextArea displayArea;

    private ObservableList<ArrayList<String>> tableData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // קישור הבקר הנוכחי ל-UI הגלובלי כדי שהלקוח יוכל לגשת אליו
        ClientUI.orderListController = this;

        if (idCol != null && dateCol != null && visitorsCol != null) {
            idCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(0)));
            dateCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(1)));
            visitorsCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(2)));
        }

        // האזנה לבחירת שורה בטבלה למילוי שדות העריכה במידה וקיימים
        if (ordersTable != null) {
            ordersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
                if (newSel != null && orderNumField != null) {
                    orderNumField.setText(newSel.get(0));
                    visitDateField.setText(newSel.get(1));
                    visitorCountField.setText(newSel.get(2));
                }
            });
        }
    }

    @FXML
    public void fetchOrders(ActionEvent event) {
        if (displayArea != null) {
            displayArea.setText("Loading orders...");
        }
        // תיקון שורה 28 - קריאה לשם המתודה המעודכן ב-OrderClient
        ClientUI.client.requestAllOrders();
    }

    @FXML
    public void submitUpdate(ActionEvent event) {
        try {
            if (orderNumField == null || orderNumField.getText().isEmpty()) {
                if (displayArea != null) displayArea.setText("Please select or enter an order number.");
                return;
            }

            int orderNum = Integer.parseInt(orderNumField.getText());
            String visitDate = visitDateField.getText();
            int visitorCount = Integer.parseInt(visitorCountField.getText());

            Order o = new Order();
            o.setOrderNumber(orderNum);
            o.setOrderDate(java.sql.Date.valueOf(visitDate));
            o.setNumberOfVisitors(visitorCount);

            if (displayArea != null) {
                displayArea.setText("Updating order " + orderNum + "...");
            }
            ClientUI.client.sendOrderUpdate(o);

        } catch (Exception e) {
            if (displayArea != null) {
                displayArea.setText("Error updating order: " + e.getMessage());
            }
            e.printStackTrace();
        }
    }

    // מתודה המופעלת על ידי הלקוח לקבלת רשימת ההזמנות הגנריות מהשרת
    public void showOrders(ArrayList<ArrayList<String>> orders) {
        if (ordersTable != null) {
            tableData.clear();
            tableData.addAll(orders);
            ordersTable.setItems(tableData);
        }
        if (displayArea != null) {
            StringBuilder sb = new StringBuilder("Orders Loaded Successfully:\n");
            for (ArrayList<String> row : orders) {
                sb.append("Order #").append(row.get(0))
                  .append(" | Date: ").append(row.get(1))
                  .append(" | Visitors: ").append(row.get(2)).append("\n");
            }
            displayArea.setText(sb.toString());
        }
    }

    public void showSuccess(String message) {
        if (displayArea != null) {
            displayArea.setText(message);
        }
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Action Status");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
        
        // טעינה מחדש של הנתונים כדי להציג את השינוי בטבלה
        ClientUI.client.requestAllOrders();
    }
}