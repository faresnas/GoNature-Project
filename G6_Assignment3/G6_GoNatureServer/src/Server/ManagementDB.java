package Server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Class {@code ManagementDB} handling administrative, organizational database profiles 
 * and configuration transactions inside the GoNature server.
 * <p>
 * This manager provides structural operations encompassing the upgrade of registered 
 * standard visitors into licensed Subscribers or certified Group Guides, managing administrative 
 * park setting configuration upgrade requests initiated by Park Managers, and updating personal 
 * descriptive information details belonging to employees or customers.
 * </p>
 *
 * @author GoNature Development Team
 * @version 1.0
 */
public class ManagementDB {

    /** Low-level database communication controller managing basic JDBC drivers and persistent connections. */
    private DBController dbController;

    /**
     * Constructs a {@code ManagementDB} structural manager mapping the foundational 
     * shared connection layout provider.
     *
     * @param dbController The primary shared relational database transaction execution wrapper.
     */
    public ManagementDB(DBController dbController) {
        this.dbController = dbController;
    }

    /**
     * Registers a new subscriber by transferring an existing standard visitor record row 
     * into the subscribers registry schema.
     * <p>
     * Validates that the citizen matches a registered visitor first, asserts that a duplicate subscription 
     * block does not exist, constructs a unique randomized subscriber identification number sequence, 
     * inserts the tracking information, and evicts the deprecated record row from the base visitors lookup index.
     * </p>
     *
     * @param firstName  First name of the traveler.
     * @param lastName   Last name of the traveler.
     * @param idNumber   Unique identification card number.
     * @param phone      Contact phone digits series.
     * @param email      Electronic mail contact address string.
     * @param familySize Allotted group volume headroom size assigned onto the subscription unit.
     * @param creditCard Card information details associated with financial file billing.
     * @return Generated numerical subscriber account reference sequence integer on success, 
     * {@code -3} if not a registered visitor, {@code -2} if already subscribed, or {@code -1} on sql exceptions.
     * @throws SQLException If database execution parameters or connections malfunction.
     */
    public int registerSubscriber(String firstName, String lastName, String idNumber,
            String phone, String email, int familySize, String creditCard) throws SQLException {
        Connection conn = dbController.getConnection();

        String visitorCheck = "SELECT id FROM visitors WHERE id_number = ?";
        PreparedStatement visitorPs = conn.prepareStatement(visitorCheck);
        visitorPs.setString(1, idNumber);
        ResultSet visitorRs = visitorPs.executeQuery();
        if (!visitorRs.next()) {
            visitorRs.close(); visitorPs.close();
            return -3;
        }
        visitorRs.close(); visitorPs.close();

        String checkSql = "SELECT id FROM subscribers WHERE id_number = ?";
        PreparedStatement checkPs = conn.prepareStatement(checkSql);
        checkPs.setString(1, idNumber);
        ResultSet checkRs = checkPs.executeQuery();
        if (checkRs.next()) {
            checkRs.close(); checkPs.close();
            return -2;
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
            String deleteSql = "DELETE FROM visitors WHERE id_number = ?";
            PreparedStatement deletePs = conn.prepareStatement(deleteSql);
            deletePs.setString(1, idNumber);
            deletePs.executeUpdate();
            deletePs.close();
            return subscriberNumber;
        }
        return -1;
    }

    /**
     * Upgrades an active visitor profile record into an authorized corporate Group Guide credential registry.
     * <p>
     * Verifies preceding visitor existence status checkpoints, ensures target access usernames are unoccupied, 
     * and shifts active structural dependencies while erasing the basic tracking node from base logs.
     * </p>
     *
     * @param name     Full legal name of the guide.
     * @param email    Electronic communication inbox string.
     * @param phone    Mobile connection digit series.
     * @param idNumber Unique citizen identity card number.
     * @param username Unique login access credential index string.
     * @param password Security credential access password.
     * @return {@code 1} on successful execution processing, {@code -3} if identity is unregistered as a base visitor, 
     * {@code -2} if username is already taken, or {@code -1} on internal processing errors.
     * @throws SQLException If database execution parameters or connections malfunction.
     */
    public int registerGuide(String name, String email, String phone,
            String idNumber, String username, String password) throws SQLException {
        Connection conn = dbController.getConnection();

        String visitorCheck = "SELECT id FROM visitors WHERE id_number = ?";
        PreparedStatement visitorPs = conn.prepareStatement(visitorCheck);
        visitorPs.setString(1, idNumber);
        ResultSet visitorRs = visitorPs.executeQuery();
        if (!visitorRs.next()) {
            visitorRs.close(); visitorPs.close();
            return -3;
        }
        visitorRs.close(); visitorPs.close();

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
            return 1;
        }
        return -1;
    }

    /**
     * Generates a formal management configuration parameter alteration ticket item waiting for review 
     * by a regional Department Manager supervisor.
     * Blocks overlapping duplication entries targeting the identical modification property if one is already pending.
     *
     * @param parkId      Programmatic internal reference tracking the target nature park row.
     * @param requestType Setting attribute targeted for parameter upgrades (e.g. MAX_CAPACITY, PROMOTION).
     * @param newValue    Floating numerical scale modification value targeted for activation.
     * @param requestedBy Employee identity context tracking the park manager submitting the request.
     * @return {@code true} if submission succeeds and persists; {@code false} if an identical request is already pending or fails.
     * @throws SQLException If database execution parameters or connections malfunction.
     */
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

    /**
     * Compiles a collection tracking all open pending configuration parameter modification tickets across all parks.
     * Resolves relational database references, fetching readable park labels and employee names.
     *
     * @return Two-dimensional list collection matrix containing string rows detailing open pending request properties.
     * @throws SQLException If database execution parameters or connections malfunction.
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

    /**
     * Commits a successful approval configuration action over a pending request ticket.
     * Updates the running active environment columns belonging to the matching target park row 
     * and sets ticket statuses to {@code APPROVED}.
     *
     * @param requestId Database structural index key matching the specific request row.
     * @return {@code true} if approval processes complete successfully; {@code false} if the ticket is missing or invalid.
     * @throws SQLException If database execution parameters or connections malfunction.
     */
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

    /**
     * Rejects an open configuration parameter update request ticket, changing its tracking state flag to {@code REJECTED}.
     *
     * @param requestId Database primary index key matching the configuration task ticket row.
     * @return {@code true} if status modifiers complete successfully; {@code false} otherwise.
     * @throws SQLException If database execution parameters or connections malfunction.
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
     * Fetches details for all registered group guide members inside the database profile system.
     *
     * @return Nested data matrix wrapping identification parameters, names, contacts, and access tags.
     * @throws SQLException If database execution parameters or connections malfunction.
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
            row.add(String.valueOf(rs.getString("phone")));
            row.add(rs.getString("username"));
            result.add(row);
        }
        rs.close(); ps.close();
        return result;
    }

    /**
     * Fetches details for all registered family or individual subscriber profiles inside the system.
     *
     * @return Nested matrix matching standard text information lines detailing subscriber parameters.
     * @throws SQLException If database execution parameters or connections malfunction.
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
            row.add(rs.getString("subscriber_number"));
            result.add(row);
        }
        rs.close(); ps.close();
        return result;
    }

    /**
     * Erases an existing guide registration record row permanently from the system files.
     *
     * @param guideId DB unique identifier reference key tracking the specific guide.
     * @return {@code true} if deletion parameters execute row counts greater than 0; {@code false} otherwise.
     * @throws SQLException If database execution parameters or connections malfunction.
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
     * Erases an existing subscriber registration record row permanently from the system data sheets.
     *
     * @param subscriberId DB unique identifier reference key tracking the specific subscriber.
     * @return {@code true} if deletion parameters execute row counts greater than 0; {@code false} otherwise.
     * @throws SQLException If database execution parameters or connections malfunction.
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

    /**
     * Modifies the operational profile elements describing an existing guide account.
     * Conditionally updates security clearance passphrases only if a non-empty value string parameter is supplied.
     *
     * @param guideId  Unique reference tracking the targeted group guide.
     * @param name     Modified name property.
     * @param email    Updated mailbox contact address.
     * @param phone    Altered telephone digit series.
     * @param password Fresh security password string (pass {@code null} or empty to keep current setting).
     * @return {@code true} if update operations return a positive modification count; {@code false} otherwise.
     * @throws SQLException If database execution parameters or connections malfunction.
     */
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

    /**
     * Modifies the parameters defining an active subscriber profile row record.
     *
     * @param subscriberId Unique index primary token key matching the subscriber.
     * @param firstName    Modified first name entry.
     * @param lastName     Modified last name entry.
     * @param phone        Altered telephone connection digits.
     * @param email        Updated communication mailbox.
     * @param familySize   Revised family headcount index value.
     * @return {@code true} if modifications commit successfully; {@code false} otherwise.
     * @throws SQLException If database execution parameters or connections malfunction.
     */
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
    
    /**
     * Registers a new individual visitor profile on the system.
     * Blocks execution tracking if a profile matching the national identity card digits is already captured.
     *
     * @param firstName First name entry.
     * @param lastName  Last name entry.
     * @param idNumber  Unique citizenship card identification number string.
     * @param phone     Mobile contact digits series.
     * @param email     Electronic communication mailbox address (can be {@code null}).
     * @return {@code true} if insertion succeeds; {@code false} if the citizen is already logged on system records.
     * @throws SQLException If database execution parameters or connections malfunction.
     */
    public boolean registerVisitor(String firstName, String lastName, String idNumber,
            String phone, String email) throws SQLException {
        String checkSql = "SELECT id FROM visitors WHERE id_number = '" + idNumber + "'";
        ArrayList<ArrayList<String>> check = dbController.executeQuery(checkSql);
        if (check != null && !check.isEmpty()) {
            return false;
        }

        String sql = "INSERT INTO visitors (id_number, first_name, last_name, phone, email) " +
                     "VALUES ('" + idNumber + "', '" + firstName + "', '" + lastName + "', '" +
                     phone + "', " + (email != null ? "'" + email + "'" : "NULL") + ")";
        return dbController.executeUpdate(sql) > 0;
    }

    /**
     * Compiles an analytical audit list detailing parameter update requests filtered to a specific park.
     * Includes all operational statuses (PENDING, APPROVED, REJECTED) ordered by recency.
     *
     * @param parkId Index reference pointing to the targeted park location.
     * @return Nested data matrix wrapping the parameter logs.
     * @throws SQLException If database execution parameters or connections malfunction.
     */
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
    
    /**
     * Performs a direct runtime field overwrite updating a profile record inside the visitors table.
     *
     * @param id        Unique primary auto-indexing entry key row token.
     * @param firstName Modified personal name text.
     * @param lastName  Modified family name text.
     * @param phone     Altered phone connection information.
     * @param email     Updated electronic communication mail string.
     * @param idNumber  Altered identification card string (if empty or {@code null}, keeps current database settings).
     * @return {@code true} if update operations map across row targets; {@code false} otherwise.
     * @throws SQLException If database execution parameters or connections malfunction.
     */
    public boolean updateVisitor(int id, String firstName, String lastName, String phone, String email, String idNumber) throws SQLException {
        String idPart = (idNumber != null && !idNumber.isEmpty()) ? ", id_number='" + idNumber + "'" : "";
        String sql = "UPDATE visitors SET first_name='" + firstName + "', last_name='" + lastName +
                     "', phone='" + phone + "', email='" + email + "'" + idPart + " WHERE id=" + id;
        return dbController.executeUpdate(sql) > 0;
    }

    /**
     * Performs a direct runtime field overwrite updating a profile record inside the subscribers table.
     *
     * @param id        Unique primary auto-indexing entry key row token matching the subscriber profile row.
     * @param firstName Modified personal name text.
     * @param lastName  Modified family name text.
     * @param phone     Altered phone connection information.
     * @param email     Updated electronic communication mail string.
     * @param idNumber  Altered identification card string (if empty or {@code null}, keeps current database settings).
     * @return {@code true} if update operations map across row targets; {@code false} otherwise.
     * @throws SQLException If database execution parameters or connections malfunction.
     */
    public boolean updateSubscriber(int id, String firstName, String lastName, String phone, String email, String idNumber) throws SQLException {
        String idPart = (idNumber != null && !idNumber.isEmpty()) ? ", id_number='" + idNumber + "'" : "";
        String sql = "UPDATE subscribers SET first_name='" + firstName + "', last_name='" + lastName +
                     "', phone='" + phone + "', email='" + email + "'" + idPart + " WHERE id=" + id;
        return dbController.executeUpdate(sql) > 0;
    }

    /**
     * Performs a direct runtime field overwrite updating profile details for an active certified Guide.
     *
     * @param id       Unique primary index locating the specific guide profile row.
     * @param name     Modified combined name text configuration.
     * @param username Updated customized application account credential login string.
     * @param phone    Altered telephone contact digits.
     * @param email    Updated structural digital mail string.
     * @param password New login validation password string (keeps current setting if empty or {@code null}).
     * @return {@code true} if updates reflect inside data sheets successfully; {@code false} otherwise.
     * @throws SQLException If database execution parameters or connections malfunction.
     */
    public boolean updateGuide(int id, String name, String username, String phone, String email, String password) throws SQLException {
        String sql = (password != null && !password.isEmpty())
            ? "UPDATE guides SET name='" + name + "', email='" + email + "', phone='" + phone +
              "', username='" + username + "', password='" + password + "' WHERE id=" + id
            : "UPDATE guides SET name='" + name + "', email='" + email + "', phone='" + phone +
              "', username='" + username + "' WHERE id=" + id;
        return dbController.executeUpdate(sql) > 0;
    }

    /**
     * Performs a direct runtime field overwrite updating profile parameters for an active organization Employee.
     *
     * @param id        Unique primary index tracking the employee record row.
     * @param firstName Modified personal name.
     * @param lastName  Modified family name.
     * @param email     Updated communication electronic mail configuration string.
     * @param username  Altered unique login profile username credential token.
     * @param password  New security access clearance phrase (keeps current setting if empty or {@code null}).
     * @return {@code true} if updates register over data bounds successfully; {@code false} otherwise.
     * @throws SQLException If database execution parameters or connections malfunction.
     */
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