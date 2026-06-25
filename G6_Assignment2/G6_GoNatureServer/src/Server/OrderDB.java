package Server;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class OrderDB {

    private DBController dbController;

    public OrderDB(DBController dbController) {
        this.dbController = dbController;
    }

    public ArrayList<ArrayList<String>> selectQuery(String query) throws SQLException {
        PreparedStatement stmt = dbController.getConnection().prepareStatement(query);
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

    public boolean updateQuery(int orderNumber, String orderDate, int numberOfVisitors) throws SQLException {
        String sql = "UPDATE `orders` SET order_date = ?, number_of_visitors = ? WHERE order_number = ?";
        PreparedStatement ps = dbController.getConnection().prepareStatement(sql);
        ps.setDate(1, Date.valueOf(orderDate));
        ps.setInt(2, numberOfVisitors);
        ps.setInt(3, orderNumber);
        int rowsAffected = ps.executeUpdate();
        return rowsAffected > 0;
    }
}