package Server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class ManagementDB {

    private DBController dbController;

    public ManagementDB(DBController dbController) {
        this.dbController = dbController;
    }

    /**
     * Registers a new subscriber. Returns the generated subscriber_number or -1 on failure.
     */
    public int registerSubscriber(String firstName, String lastName, String idNumber,
            String phone, String email, int familySize, String creditCard) throws SQLException {
        Connection conn = dbController.getConnection();

        // Check if ID already registered
        String checkSql = "SELECT id FROM subscribers WHERE id_number = ?";
        PreparedStatement checkPs = conn.prepareStatement(checkSql);
        checkPs.setString(1, idNumber);
        ResultSet checkRs = checkPs.executeQuery();
        if (checkRs.next()) {
            checkRs.close();
            checkPs.close();
            return -2; // already exists
        }
        checkRs.close();
        checkPs.close();

        // Generate unique subscriber number (8-digit)
        int subscriberNumber = 10000000 + new java.util.Random().nextInt(90000000);

        String sql = "INSERT INTO subscribers (first_name, last_name, id_number, phone, email, family_size, credit_card, subscriber_number) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, firstName);
        ps.setString(2, lastName);
        ps.setString(3, idNumber);
        ps.setString(4, phone);
        ps.setString(5, email);
        ps.setInt(6, familySize);
        ps.setString(7, creditCard);
        ps.setInt(8, subscriberNumber);

        int rows = ps.executeUpdate();
        ps.close();

        return rows > 0 ? subscriberNumber : -1;
    }

    /**
     * Registers a new group guide. Returns true on success, false if username already exists.
     */
    public boolean registerGuide(String name, String email, String phone,
            String username, String password) throws SQLException {
        Connection conn = dbController.getConnection();

        // Check if username already taken
        String checkSql = "SELECT id FROM guides WHERE username = ?";
        PreparedStatement checkPs = conn.prepareStatement(checkSql);
        checkPs.setString(1, username);
        ResultSet checkRs = checkPs.executeQuery();
        if (checkRs.next()) {
            checkRs.close();
            checkPs.close();
            return false;
        }
        checkRs.close();
        checkPs.close();

        String sql = "INSERT INTO guides (name, email, phone, username, password) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, name);
        ps.setString(2, email);
        ps.setString(3, phone);
        ps.setString(4, username);
        ps.setString(5, password);

        int rows = ps.executeUpdate();
        ps.close();

        return rows > 0;
    }

    /**
     * Saves a park parameter update request as PENDING.
     * requestType: MAX_CAPACITY, PREBOOKED_RESERVED, AVG_STAY_HOURS, PROMOTION
     */
    public boolean submitParkUpdateRequest(int parkId, String requestType,
            double newValue, int requestedBy) throws SQLException {
        Connection conn = dbController.getConnection();

        // Check no duplicate pending request for same park + type
        String checkSql = "SELECT id FROM pending_requests WHERE park_id = ? AND request_type = ? AND status = 'PENDING'";
        PreparedStatement checkPs = conn.prepareStatement(checkSql);
        checkPs.setInt(1, parkId);
        checkPs.setString(2, requestType);
        ResultSet checkRs = checkPs.executeQuery();
        if (checkRs.next()) {
            checkRs.close();
            checkPs.close();
            return false; // already a pending request for this
        }
        checkRs.close();
        checkPs.close();

        String sql = "INSERT INTO pending_requests (park_id, request_type, new_value, requested_by) VALUES (?, ?, ?, ?)";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, parkId);
        ps.setString(2, requestType);
        ps.setDouble(3, newValue);
        ps.setInt(4, requestedBy);

        int rows = ps.executeUpdate();
        ps.close();

        return rows > 0;
    }

    /**
     * Returns all PENDING requests for the Dept Manager to review.
     * Each row: [id, park_name, request_type, new_value, requested_by_name, created_at]
     */
    public ArrayList<ArrayList<String>> getPendingRequests() throws SQLException {
        Connection conn = dbController.getConnection();

        String sql = "SELECT pr.id, p.name, pr.request_type, pr.new_value, " +
                     "CONCAT(e.first_name, ' ', e.last_name), pr.created_at " +
                     "FROM pending_requests pr " +
                     "JOIN parks p ON pr.park_id = p.id " +
                     "JOIN employees e ON pr.requested_by = e.id " +
                     "WHERE pr.status = 'PENDING' " +
                     "ORDER BY pr.created_at ASC";

        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();

        ArrayList<ArrayList<String>> result = new ArrayList<>();
        while (rs.next()) {
            ArrayList<String> row = new ArrayList<>();
            row.add(String.valueOf(rs.getInt(1)));       // id
            row.add(rs.getString(2));                     // park name
            row.add(rs.getString(3));                     // request_type
            row.add(String.valueOf(rs.getDouble(4)));     // new_value
            row.add(rs.getString(5));                     // requested by name
            row.add(String.valueOf(rs.getTimestamp(6)));  // created_at
            result.add(row);
        }

        rs.close();
        ps.close();

        return result;
    }

    /**
     * Approves a pending request — applies the change to the parks table immediately.
     */
    public boolean approveRequest(int requestId) throws SQLException {
        Connection conn = dbController.getConnection();

        // Get request details
        String selectSql = "SELECT park_id, request_type, new_value FROM pending_requests WHERE id = ? AND status = 'PENDING'";
        PreparedStatement selectPs = conn.prepareStatement(selectSql);
        selectPs.setInt(1, requestId);
        ResultSet rs = selectPs.executeQuery();

        if (!rs.next()) {
            rs.close();
            selectPs.close();
            return false;
        }

        int parkId = rs.getInt("park_id");
        String requestType = rs.getString("request_type");
        double newValue = rs.getDouble("new_value");
        rs.close();
        selectPs.close();

        // Apply change to parks table
        String column;
        switch (requestType) {
            case "MAX_CAPACITY":       column = "max_capacity";       break;
            case "PREBOOKED_RESERVED": column = "prebooked_reserved"; break;
            case "AVG_STAY_HOURS":     column = "avg_stay_hours";     break;
            case "PROMOTION":          column = "promotion_discount"; break;
            default: return false;
        }

        String updateParkSql = "UPDATE parks SET " + column + " = ? WHERE id = ?";
        PreparedStatement updatePs = conn.prepareStatement(updateParkSql);
        updatePs.setDouble(1, newValue);
        updatePs.setInt(2, parkId);
        updatePs.executeUpdate();
        updatePs.close();

        // Mark request as APPROVED
        String approveSql = "UPDATE pending_requests SET status = 'APPROVED' WHERE id = ?";
        PreparedStatement approvePs = conn.prepareStatement(approveSql);
        approvePs.setInt(1, requestId);
        approvePs.executeUpdate();
        approvePs.close();

        return true;
    }

    /**
     * Rejects a pending request — discards it, no DB change applied.
     */
    public boolean rejectRequest(int requestId) throws SQLException {
        Connection conn = dbController.getConnection();

        String sql = "UPDATE pending_requests SET status = 'REJECTED' WHERE id = ? AND status = 'PENDING'";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, requestId);
        int rows = ps.executeUpdate();
        ps.close();

        return rows > 0;
    }
    
    /**
     * Returns all guides as rows for the TableView.
     * Each row: [id, name, email, phone, username]
     */
    public ArrayList<ArrayList<String>> getAllGuides() throws SQLException {
        Connection conn = dbController.getConnection();
        String sql = "SELECT id, name, email, phone, username FROM guides";
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();

        ArrayList<ArrayList<String>> result = new ArrayList<>();
        while (rs.next()) {
            ArrayList<String> row = new ArrayList<>();
            row.add(String.valueOf(rs.getInt("id")));
            row.add(rs.getString("name"));
            row.add(rs.getString("email"));
            row.add(rs.getString("phone"));
            row.add(rs.getString("username"));
            result.add(row);
        }
        rs.close();
        ps.close();
        return result;
    }

    /**
     * Returns all subscribers as rows for the TableView.
     * Each row: [id, first_name, last_name, id_number, phone, email, family_size, subscriber_number]
     */
    public ArrayList<ArrayList<String>> getAllSubscribers() throws SQLException {
        Connection conn = dbController.getConnection();
        String sql = "SELECT id, first_name, last_name, id_number, phone, email, family_size, subscriber_number FROM subscribers";
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();

        ArrayList<ArrayList<String>> result = new ArrayList<>();
        while (rs.next()) {
            ArrayList<String> row = new ArrayList<>();
            row.add(String.valueOf(rs.getInt("id")));
            row.add(rs.getString("first_name"));
            row.add(rs.getString("last_name"));
            row.add(rs.getString("id_number"));
            row.add(rs.getString("phone"));
            row.add(rs.getString("email"));
            row.add(String.valueOf(rs.getInt("family_size")));
            row.add(rs.getString("subscriber_number")); // getString handles both formats
            result.add(row);
        }
        rs.close();
        ps.close();
        return result;
    }
    /**
     * Deletes a guide by ID.
     */
    public boolean deleteGuide(int guideId) throws SQLException {
        Connection conn = dbController.getConnection();
        String sql = "DELETE FROM guides WHERE id = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, guideId);
        int rows = ps.executeUpdate();
        ps.close();
        return rows > 0;
    }

    /**
     * Deletes a subscriber by ID.
     */
    public boolean deleteSubscriber(int subscriberId) throws SQLException {
        Connection conn = dbController.getConnection();
        String sql = "DELETE FROM subscribers WHERE id = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, subscriberId);
        int rows = ps.executeUpdate();
        ps.close();
        return rows > 0;
    }
    
    public boolean editGuide(int guideId, String name, String email,
            String phone, String password) throws SQLException {
        Connection conn = dbController.getConnection();
        String sql;
        PreparedStatement ps;

        if (password != null && !password.isEmpty()) {
            sql = "UPDATE guides SET name=?, email=?, phone=?, password=? WHERE id=?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, phone);
            ps.setString(4, password);
            ps.setInt(5, guideId);
        } else {
            sql = "UPDATE guides SET name=?, email=?, phone=? WHERE id=?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, phone);
            ps.setInt(4, guideId);
        }

        int rows = ps.executeUpdate();
        ps.close();
        return rows > 0;
    }

    public boolean editSubscriber(int subscriberId, String firstName, String lastName,
            String phone, String email, int familySize) throws SQLException {
        Connection conn = dbController.getConnection();
        String sql = "UPDATE subscribers SET first_name=?, last_name=?, phone=?, email=?, family_size=? WHERE id=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, firstName);
        ps.setString(2, lastName);
        ps.setString(3, phone);
        ps.setString(4, email);
        ps.setInt(5, familySize);
        ps.setInt(6, subscriberId);
        int rows = ps.executeUpdate();
        ps.close();
        return rows > 0;
    }
    
    
}