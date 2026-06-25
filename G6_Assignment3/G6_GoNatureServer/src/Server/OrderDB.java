package Server;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Handles database operations related to customer orders.
 * This class provides methods for retrieving and updating
 * order information stored in the database.
 */
public class OrderDB {

    /**
     * Database controller used to obtain database connections.
     */
    private DBController dbController;

    /**
     * Creates a new OrderDB object.
     *
     * @param dbController the database controller used for database access
     */
    public OrderDB(DBController dbController) {
        this.dbController = dbController;
    }

    /**
     * Executes a SELECT query and returns the matching orders.
     *
     * @param query the SQL SELECT query
     * @return a list containing the retrieved order records
     * @throws SQLException if a database access error occurs
     */
    public ArrayList<ArrayList<String>> selectQuery(String query) throws SQLException {

        Connection conn = dbController.getConnection();

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

    /**
     * Updates an existing order in the database.
     *
     * @param orderNumber the order number to update
     * @param orderDate the new reservation date
     * @param numberOfVisitors the updated number of visitors
     * @return true if the order was updated successfully, otherwise false
     * @throws SQLException if a database access error occurs
     */
    public boolean updateQuery(int orderNumber,
                               String orderDate,
                               int numberOfVisitors) throws SQLException {

        Connection conn = dbController.getConnection();

        String sql =
                "UPDATE `orders` SET order_date = ?, number_of_visitors = ? WHERE order_number = ?";

        PreparedStatement ps = conn.prepareStatement(sql);

        ps.setDate(1, Date.valueOf(orderDate));
        ps.setInt(2, numberOfVisitors);
        ps.setInt(3, orderNumber);

        int rowsAffected = ps.executeUpdate();

        return rowsAffected > 0;
    }
}