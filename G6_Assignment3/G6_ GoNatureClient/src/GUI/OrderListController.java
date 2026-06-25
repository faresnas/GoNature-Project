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

/**
 * Controller for the order list screen.
 * <p>
 * This controller is responsible for displaying orders in a table,
 * requesting order data from the server, allowing the user to update
 * an existing order, and showing success or error messages to the user.
 * </p>
 *
 * @author Fares
 * @version 1.0
 */
public class OrderListController {

    /**
     * Table view used to display the orders received from the server.
     */
    @FXML
    private TableView<ArrayList<String>> ordersTable;

    /**
     * Column that displays the order number.
     */
    @FXML
    private TableColumn<ArrayList<String>, String> idCol;

    /**
     * Column that displays the visit date of the order.
     */
    @FXML
    private TableColumn<ArrayList<String>, String> dateCol;

    /**
     * Column that displays the number of visitors in the order.
     */
    @FXML
    private TableColumn<ArrayList<String>, String> visitorsCol;

    /**
     * Text field used to display or enter the order number.
     */
    @FXML
    private TextField orderNumField;

    /**
     * Text field used to display or enter the visit date.
     */
    @FXML
    private TextField visitDateField;

    /**
     * Text field used to display or enter the number of visitors.
     */
    @FXML
    private TextField visitorCountField;

    /**
     * Text area used to display status messages and order information.
     */
    @FXML
    private TextArea displayArea;

    /**
     * Observable list that stores the data shown in the orders table.
     */
    private ObservableList<ArrayList<String>> tableData = FXCollections.observableArrayList();

    /**
     * Initializes the order list screen.
     * <p>
     * This method links the controller to {@link ClientUI}, configures
     * the table columns, and adds a listener to fill the edit fields
     * when the user selects a row in the table.
     * </p>
     */
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

    /**
     * Requests all orders from the server.
     * <p>
     * The method displays a loading message and sends a request through
     * the client communication layer.
     * </p>
     *
     * @param event the action event triggered by the user.
     */
    @FXML
    public void fetchOrders(ActionEvent event) {
        if (displayArea != null) {
            displayArea.setText("Loading orders...");
        }
        // תיקון שורה 28 - קריאה לשם המתודה המעודכן ב-OrderClient
        ClientUI.client.requestAllOrders();
    }

    /**
     * Sends an update request for the selected order.
     * <p>
     * The method reads the order number, visit date, and visitor count
     * from the input fields, creates an {@link Order} object, and sends
     * it to the server for updating.
     * </p>
     *
     * @param event the action event triggered by the user.
     */
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

    /**
     * Displays the list of orders received from the server.
     * <p>
     * The method updates the table view and writes a textual summary
     * of all loaded orders in the display area.
     * </p>
     *
     * @param orders list of orders represented as rows of string values.
     */
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

    /**
     * Displays a success message to the user and refreshes the orders table.
     *
     * @param message the message to display.
     */
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