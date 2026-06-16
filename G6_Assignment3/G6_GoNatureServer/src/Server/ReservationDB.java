package Server;

import data.Reservation;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Random;

public class ReservationDB {

    private DBController dbController;
    private static final Random RANDOM = new Random();

    public ReservationDB(DBController dbController) {
        this.dbController = dbController;
    }

    /**
     * Generates a random 8-digit numeric confirmation code.
     */
    private String generateConfirmationCode() {
        int code = 10000000 + RANDOM.nextInt(90000000); // 8-digit number
        return String.valueOf(code);
    }

    /**
     * Checks whether the park has capacity for the given visit slot.
     * Excludes a specific reservation ID from the overlap check (used during edits).
     */
    private boolean isParkAvailable(Connection conn, int parkId, java.sql.Date visitDate,
            java.sql.Time startTime, int numVisitors, int excludeReservationId) throws SQLException {

        String parkSql = "SELECT max_capacity, prebooked_reserved, avg_stay_hours FROM parks WHERE id = ?";
        PreparedStatement parkPs = conn.prepareStatement(parkSql);
        parkPs.setInt(1, parkId);
        ResultSet parkRs = parkPs.executeQuery();

        if (!parkRs.next()) {
            parkRs.close();
            parkPs.close();
            return false;
        }

        int maxCapacity = parkRs.getInt("max_capacity");
        int prebookedReserved = parkRs.getInt("prebooked_reserved");
        double avgStayDouble = parkRs.getDouble("avg_stay_hours");
        int avgStay = (avgStayDouble <= 0) ? 4 : (int) avgStayDouble;
        int allowedQuota = maxCapacity - prebookedReserved;
        parkRs.close();
        parkPs.close();

        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(startTime);
        cal.add(java.util.Calendar.HOUR_OF_DAY, avgStay);
        java.sql.Time endTime = new java.sql.Time(cal.getTimeInMillis());

        String checkSql = "SELECT SUM(num_visitors) FROM reservations " +
                "WHERE park_id = ? AND visit_date = ? AND status = 'CONFIRMED' AND id != ? " +
                "AND ((entry_time <= ? AND entry_time < ?) OR (entry_time >= ? AND entry_time < ?))";

        PreparedStatement checkPs = conn.prepareStatement(checkSql);
        checkPs.setInt(1, parkId);
        checkPs.setDate(2, visitDate);
        checkPs.setInt(3, excludeReservationId);
        checkPs.setTime(4, startTime);
        checkPs.setTime(5, endTime);
        checkPs.setTime(6, startTime);
        checkPs.setTime(7, endTime);

        ResultSet checkRs = checkPs.executeQuery();
        int currentBookedVisitors = 0;
        if (checkRs.next()) {
            currentBookedVisitors = checkRs.getInt(1);
        }
        checkRs.close();
        checkPs.close();

        return (currentBookedVisitors + numVisitors) <= allowedQuota;
    }

    /**
     * Calculates total price based on reservation type, traveler type, and prepaid status.
     */
    private double calculatePrice(Connection conn, Reservation r) throws SQLException {
        String sql = "SELECT full_price FROM parks WHERE id = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, r.getParkId());
        ResultSet rs = ps.executeQuery();
        double fullPrice = 40.0;
        if (rs.next()) {
            fullPrice = rs.getDouble("full_price");
        }
        rs.close();
        ps.close();

        double totalPrice;
        int visitors = r.getNumVisitors();

        if ("GROUP".equals(r.getType())) {
            int payingVisitors = Math.max(0, visitors - 1); // guide doesn't pay for pre-booked
            double pricePerPerson = fullPrice * 0.75;
            if (r.isPrepaid()) {
                pricePerPerson = pricePerPerson * 0.88;
            }
            totalPrice = payingVisitors * pricePerPerson;
        } else {
            double pricePerPerson = fullPrice * 0.85;
            if ("SUBSCRIBER".equals(r.getTravelerType())) {
                pricePerPerson = pricePerPerson * 0.90;
            }
            totalPrice = visitors * pricePerPerson;
        }

        return totalPrice;
    }

    /**
     * Creates a new reservation. Returns "SUCCESS:CODE:PRICE" or "FULL:..." or "ERROR:...".
     */
    public String createReservation(Reservation r) throws SQLException {
        Connection conn = dbController.getConnection();

        if (!isParkAvailable(conn, r.getParkId(), r.getVisitDate(), r.getEntryTime(), r.getNumVisitors(), 0)) {
            return "FULL: Park capacity reached for this time slot.";
        }

        String confirmationCode = generateConfirmationCode();
        double calculatedPrice = calculatePrice(conn, r);

        String insertSql = "INSERT INTO reservations " +
                "(traveler_id, traveler_type, park_id, visit_date, entry_time, num_visitors, email, type, status, confirmation_code, is_prepaid) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement ps = conn.prepareStatement(insertSql);
        ps.setInt(1, r.getTravelerId());
        ps.setString(2, r.getTravelerType());
        ps.setInt(3, r.getParkId());
        ps.setDate(4, r.getVisitDate());
        ps.setTime(5, r.getEntryTime());
        ps.setInt(6, r.getNumVisitors());
        ps.setString(7, r.getEmail());
        ps.setString(8, r.getType());
        ps.setString(9, "CONFIRMED");
        ps.setString(10, confirmationCode);
        ps.setBoolean(11, r.isPrepaid());

        int rows = ps.executeUpdate();
        ps.close();

        if (rows > 0) {
            return "SUCCESS:" + confirmationCode + ":" + String.format("%.2f", calculatedPrice);
        } else {
            return "ERROR: Database insert failed.";
        }
    }

    /**
     * Returns all reservations for a given traveler as rows of strings for the TableView.
     */
    public ArrayList<ArrayList<String>> getReservationsByTraveler(int travelerId, String travelerType) throws SQLException {
        Connection conn = dbController.getConnection();

        String sql = "SELECT r.id, p.name, r.visit_date, r.entry_time, r.num_visitors, r.type, r.status, r.confirmation_code " +
                "FROM reservations r JOIN parks p ON r.park_id = p.id " +
                "WHERE r.traveler_id = ? AND r.traveler_type = ?";

        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, travelerId);
        ps.setString(2, travelerType);

        ResultSet rs = ps.executeQuery();
        ArrayList<ArrayList<String>> result = new ArrayList<>();

        while (rs.next()) {
            ArrayList<String> row = new ArrayList<>();
            row.add(String.valueOf(rs.getInt("id")));
            row.add(rs.getString("name"));
            row.add(String.valueOf(rs.getDate("visit_date")));
            row.add(String.valueOf(rs.getTime("entry_time")));
            row.add(String.valueOf(rs.getInt("num_visitors")));
            row.add(rs.getString("type"));
            row.add(rs.getString("status"));
            row.add(rs.getString("confirmation_code"));
            result.add(row);
        }

        rs.close();
        ps.close();

        return result;
    }

    /**
     * Updates date, time, and visitor count for an existing reservation.
     * Re-checks park availability before applying the change.
     * Returns false if park is full or reservation not found.
     */
    public boolean updateReservation(int reservationId, String visitDate, String entryTime, int numVisitors) throws SQLException {
        Connection conn = dbController.getConnection();

        String selectSql = "SELECT park_id, status FROM reservations WHERE id = ?";
        PreparedStatement selectPs = conn.prepareStatement(selectSql);
        selectPs.setInt(1, reservationId);
        ResultSet selectRs = selectPs.executeQuery();

        if (!selectRs.next()) {
            selectRs.close();
            selectPs.close();
            return false;
        }

        int parkId = selectRs.getInt("park_id");
        String currentStatus = selectRs.getString("status");
        selectRs.close();
        selectPs.close();

        // Block editing cancelled reservations
        if ("CANCELLED".equals(currentStatus)) {
            return false;
        }

        java.sql.Date vDate = java.sql.Date.valueOf(visitDate);
        java.sql.Time eTime = java.sql.Time.valueOf(entryTime);

        if (!isParkAvailable(conn, parkId, vDate, eTime, numVisitors, reservationId)) {
            return false;
        }

        String sql = "UPDATE reservations SET visit_date = ?, entry_time = ?, num_visitors = ? WHERE id = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setDate(1, vDate);
        ps.setTime(2, eTime);
        ps.setInt(3, numVisitors);
        ps.setInt(4, reservationId);

        int rows = ps.executeUpdate();
        ps.close();

        return rows > 0;
    }

    /**
     * Deletes a reservation by ID. Only deletes if it belongs to the given traveler
     * as a safety check so users can't delete each other's reservations.
     * Returns true if a row was deleted, false otherwise.
     */
    public boolean deleteReservation(int reservationId, int travelerId, String travelerType) throws SQLException {
        Connection conn = dbController.getConnection();

        String sql = "DELETE FROM reservations WHERE id = ? AND traveler_id = ? AND traveler_type = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, reservationId);
        ps.setInt(2, travelerId);
        ps.setString(3, travelerType);

        int rows = ps.executeUpdate();
        ps.close();

        return rows > 0;
    }
    
    
    public ArrayList<ArrayList<String>> getParks() throws SQLException {
        Connection conn = dbController.getConnection();

        String sql = "SELECT id, name FROM parks";
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();

        ArrayList<ArrayList<String>> result = new ArrayList<>();
        while (rs.next()) {
            ArrayList<String> row = new ArrayList<>();
            row.add(String.valueOf(rs.getInt("id")));
            row.add(rs.getString("name"));
            result.add(row);
        }

        rs.close();
        ps.close();

        return result;
    }
    
    
}


