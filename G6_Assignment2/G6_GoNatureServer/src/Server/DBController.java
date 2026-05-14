package Server;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

// Handles all database operations for the GoNature server
public class DBController {

    private Connection conn;

    public DBController() {
        connectToDB();
    }

    private void connectToDB() {
        try {
            conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/GoNature?allowLoadLocalInfile=true&serverTimezone=Asia/Jerusalem&useSSL=false",
                "root", "Hadi218057");
            System.out.println("Connected to GoNature DB successfully");
        } catch (SQLException e) {
            System.out.println("DB connection failed: " + e.getMessage());
        }
    }

    public boolean isConnected() {
        try {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    // Returns all orders from the orders table
    public ArrayList<ArrayList<String>> selectQuery(String query) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(query);
        ResultSet rs = stmt.executeQuery();
        ArrayList<ArrayList<String>> result = new ArrayList<>();
        while (rs.next()) {
            ArrayList<String> row = new ArrayList<>();
            row.add(String.valueOf(rs.getInt("order_number")));
            row.add(String.valueOf(rs.getDate("order_date")));
            row.add(String.valueOf(rs.getInt("number_of_visitors")));
            row.add(String.valueOf(rs.getInt("confirmation_code")));
            row.add(String.valueOf(rs.getInt("subscriber_id")));
            row.add(String.valueOf(rs.getDate("date_of_placing_order")));
            result.add(row);
        }
        return result;
    }

    // Updates order_date and number_of_visitors for a given order
    public boolean updateQuery(int orderNumber, String orderDate, int numberOfVisitors) throws SQLException {
        String sql = "UPDATE `orders` SET order_date = ?, number_of_visitors = ? WHERE order_number = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setDate(1, Date.valueOf(orderDate));
        ps.setInt(2, numberOfVisitors);
        ps.setInt(3, orderNumber);
        return ps.executeUpdate() > 0;
    }
}