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

    public int registerSubscriber(String firstName, String lastName, String idNumber,
            String phone, String email, int familySize, String creditCard) throws SQLException {
        Connection conn = dbController.getConnection();

        // Check visitor exists
        String visitorCheck = "SELECT id FROM visitors WHERE id_number = ?";
        PreparedStatement visitorPs = conn.prepareStatement(visitorCheck);
        visitorPs.setString(1, idNumber);
        ResultSet visitorRs = visitorPs.executeQuery();
        if (!visitorRs.next()) {
            visitorRs.close(); visitorPs.close();
            return -3; // not a registered visitor
        }
        visitorRs.close(); visitorPs.close();

        // Check not already a subscriber
        String checkSql = "SELECT id FROM subscribers WHERE id_number = ?";
        PreparedStatement checkPs = conn.prepareStatement(checkSql);
        checkPs.setString(1, idNumber);
        ResultSet checkRs = checkPs.executeQuery();
        if (checkRs.next()) {
            checkRs.close(); checkPs.close();
            return -2; // already a subscriber
        }
        checkRs.close(); checkPs.close();

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

        if (rows > 0) {
            // Move visitor out of visitors table
            String deleteSql = "DELETE FROM visitors WHERE id_number = ?";
            PreparedStatement deletePs = conn.prepareStatement(deleteSql);
            deletePs.setString(1, idNumber);
            deletePs.executeUpdate();
            deletePs.close();
            return subscriberNumber;
        }
        return -1;
    }

    public int registerGuide(String name, String email, String phone,
            String idNumber, String username, String password) throws SQLException {
        Connection conn = dbController.getConnection();

        // -3 = not a registered visitor
        String visitorCheck = "SELECT id FROM visitors WHERE id_number = ?";
        PreparedStatement visitorPs = conn.prepareStatement(visitorCheck);
        visitorPs.setString(1, idNumber);
        ResultSet visitorRs = visitorPs.executeQuery();
        if (!visitorRs.next()) {
            visitorRs.close(); visitorPs.close();
            return -3;
        }
        visitorRs.close(); visitorPs.close();

        // -2 = username taken
        String checkSql = "SELECT id FROM guides WHERE username = ?";
        PreparedStatement checkPs = conn.prepareStatement(checkSql);
        checkPs.setString(1, username);
        ResultSet checkRs = checkPs.executeQuery();
        if (checkRs.next()) {
            checkRs.close(); checkPs.close();
            return -2;
        }
        checkRs.close(); checkPs.close();

        String sql = "INSERT INTO guides (name, email, phone, id_number, username, password) VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, name);
        ps.setString(2, email);
        ps.setString(3, phone);
        ps.setString(4, idNumber);
        ps.setString(5, username);
        ps.setString(6, password);
        int rows = ps.executeUpdate();
        ps.close();

        if (rows > 0) {
            String deleteSql = "DELETE FROM visitors WHERE id_number = ?";
            PreparedStatement deletePs = conn.prepareStatement(deleteSql);
            deletePs.setString(1, idNumber);
            deletePs.executeUpdate();
            deletePs.close();
            return 1; // success
        }
        return -1;
    }

    public boolean submitParkUpdateRequest(int parkId, String requestType,
            double newValue, int requestedBy) throws SQLException {
        Connection conn = dbController.getConnection();

        String checkSql = "SELECT id FROM pending_requests WHERE park_id = ? AND request_type = ? AND status = 'PENDING'";
        PreparedStatement checkPs = conn.prepareStatement(checkSql);
        checkPs.setInt(1, parkId);
        checkPs.setString(2, requestType);
        ResultSet checkRs = checkPs.executeQuery();
        if (checkRs.next()) {
            checkRs.close(); checkPs.close();
            return false;
        }
        checkRs.close(); checkPs.close();

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
            row.add(String.valueOf(rs.getInt(1)));
            row.add(rs.getString(2));
            row.add(rs.getString(3));
            row.add(String.valueOf(rs.getDouble(4)));
            row.add(rs.getString(5));
            row.add(String.valueOf(rs.getTimestamp(6)));
            result.add(row);
        }
        rs.close(); ps.close();
        return result;
    }

    public boolean approveRequest(int requestId) throws SQLException {
        Connection conn = dbController.getConnection();

        String selectSql = "SELECT park_id, request_type, new_value FROM pending_requests WHERE id = ? AND status = 'PENDING'";
        PreparedStatement selectPs = conn.prepareStatement(selectSql);
        selectPs.setInt(1, requestId);
        ResultSet rs = selectPs.executeQuery();

        if (!rs.next()) { rs.close(); selectPs.close(); return false; }

        int parkId = rs.getInt("park_id");
        String requestType = rs.getString("request_type");
        double newValue = rs.getDouble("new_value");
        rs.close(); selectPs.close();

        String column;
        switch (requestType) {
            case "MAX_CAPACITY":       column = "max_capacity";       break;
            case "PREBOOKED_RESERVED": column = "prebooked_reserved"; break;
            case "AVG_STAY_HOURS":     column = "avg_stay_hours";     break;
            case "PROMOTION":          column = "promotion_discount"; break;
            default: return false;
        }

        PreparedStatement updatePs = conn.prepareStatement(
            "UPDATE parks SET " + column + " = ? WHERE id = ?");
        updatePs.setDouble(1, newValue);
        updatePs.setInt(2, parkId);
        updatePs.executeUpdate();
        updatePs.close();

        PreparedStatement approvePs = conn.prepareStatement(
            "UPDATE pending_requests SET status = 'APPROVED' WHERE id = ?");
        approvePs.setInt(1, requestId);
        approvePs.executeUpdate();
        approvePs.close();

        return true;
    }

    public boolean rejectRequest(int requestId) throws SQLException {
        Connection conn = dbController.getConnection();
        String sql = "UPDATE pending_requests SET status = 'REJECTED' WHERE id = ? AND status = 'PENDING'";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, requestId);
        int rows = ps.executeUpdate();
        ps.close();
        return rows > 0;
    }

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
        rs.close(); ps.close();
        return result;
    }

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
            row.add(rs.getString("subscriber_number"));
            result.add(row);
        }
        rs.close(); ps.close();
        return result;
    }

    public boolean deleteGuide(int guideId) throws SQLException {
        Connection conn = dbController.getConnection();
        String sql = "DELETE FROM guides WHERE id = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, guideId);
        int rows = ps.executeUpdate();
        ps.close();
        return rows > 0;
    }

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
    
    public boolean registerVisitor(String firstName, String lastName, String idNumber,
            String phone, String email) throws SQLException {
        // Check if ID already exists
        String checkSql = "SELECT id FROM visitors WHERE id_number = '" + idNumber + "'";
        ArrayList<ArrayList<String>> check = dbController.executeQuery(checkSql);
        if (check != null && !check.isEmpty()) {
            return false; // already exists
        }

        String sql = "INSERT INTO visitors (id_number, first_name, last_name, phone, email) " +
                     "VALUES ('" + idNumber + "', '" + firstName + "', '" + lastName + "', '" +
                     phone + "', " + (email != null ? "'" + email + "'" : "NULL") + ")";
        return dbController.executeUpdate(sql) > 0;
    }

    // Returns all requests for a specific park — all statuses
    public ArrayList<ArrayList<String>> getAllRequestsByPark(int parkId) throws SQLException {
        Connection conn = dbController.getConnection();

        String sql = "SELECT pr.id, pr.park_id, pr.request_type, pr.new_value, pr.status, pr.created_at " +
                     "FROM pending_requests pr " +
                     "WHERE pr.park_id = ? " +
                     "ORDER BY pr.created_at DESC";

        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, parkId);
        ResultSet rs = ps.executeQuery();

        ArrayList<ArrayList<String>> result = new ArrayList<>();
        while (rs.next()) {
            ArrayList<String> row = new ArrayList<>();
            row.add(String.valueOf(rs.getInt(1)));
            row.add(String.valueOf(rs.getInt(2)));
            row.add(rs.getString(3));
            row.add(String.valueOf(rs.getDouble(4)));
            row.add(rs.getString(5));
            row.add(String.valueOf(rs.getTimestamp(6)));
            result.add(row);
        }
        rs.close(); ps.close();
        return result;
    }
    
    public boolean updateVisitor(int id, String firstName, String lastName, String phone, String email, String idNumber) throws SQLException {
        String idPart = (idNumber != null && !idNumber.isEmpty()) ? ", id_number='" + idNumber + "'" : "";
        String sql = "UPDATE visitors SET first_name='" + firstName + "', last_name='" + lastName +
                     "', phone='" + phone + "', email='" + email + "'" + idPart + " WHERE id=" + id;
        return dbController.executeUpdate(sql) > 0;
    }

    public boolean updateSubscriber(int id, String firstName, String lastName, String phone, String email, String idNumber) throws SQLException {
        String idPart = (idNumber != null && !idNumber.isEmpty()) ? ", id_number='" + idNumber + "'" : "";
        String sql = "UPDATE subscribers SET first_name='" + firstName + "', last_name='" + lastName +
                     "', phone='" + phone + "', email='" + email + "'" + idPart + " WHERE id=" + id;
        return dbController.executeUpdate(sql) > 0;
    }

    public boolean updateGuide(int id, String name, String username, String phone, String email, String password) throws SQLException {
        String sql = (password != null && !password.isEmpty())
            ? "UPDATE guides SET name='" + name + "', email='" + email + "', phone='" + phone +
              "', username='" + username + "', password='" + password + "' WHERE id=" + id
            : "UPDATE guides SET name='" + name + "', email='" + email + "', phone='" + phone +
              "', username='" + username + "' WHERE id=" + id;
        return dbController.executeUpdate(sql) > 0;
    }

    public boolean updateEmployee(int id, String firstName, String lastName, String email, String username, String password) throws SQLException {
        String sql = (password != null && !password.isEmpty())
            ? "UPDATE employees SET first_name='" + firstName + "', last_name='" + lastName +
              "', email='" + email + "', username='" + username +
              "', password='" + password + "' WHERE id=" + id
            : "UPDATE employees SET first_name='" + firstName + "', last_name='" + lastName +
              "', email='" + email + "', username='" + username + "' WHERE id=" + id;
        return dbController.executeUpdate(sql) > 0;
    }
    
}