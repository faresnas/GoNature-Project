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
    
    public interface NotificationCallback {
        void notifyUser(String username, Object message);
    }

    private NotificationCallback notificationCallback;

    public void setNotificationCallback(NotificationCallback callback) {
        this.notificationCallback = callback;
    }

    /**
     * Generates a random 8-digit numeric confirmation code.
     */
    private String generateConfirmationCode() {
        int code = 10000000 + RANDOM.nextInt(90000000);
        return String.valueOf(code);
    }

    /**
     * Checks whether the park has capacity for the given visit slot.
     * Counts both PENDING and CONFIRMED reservations against capacity.
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

        // Count PENDING + CONFIRMED — both hold capacity
        String checkSql = "SELECT SUM(num_visitors) FROM reservations " +
                "WHERE park_id = ? AND visit_date = ? AND status IN ('PENDING','CONFIRMED') AND id != ? " +
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
        String sql = "SELECT full_price, promotion_discount FROM parks WHERE id = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, r.getParkId());
        ResultSet rs = ps.executeQuery();

        double fullPrice = 40.0;
        double promotionDiscount = 0.0;

        if (rs.next()) {
            fullPrice = rs.getDouble("full_price");
            promotionDiscount = rs.getDouble("promotion_discount");
        }
        rs.close();
        ps.close();

        double totalPrice;
        int visitors = r.getNumVisitors();

        if ("GROUP".equals(r.getType())) {
            int payingVisitors = Math.max(0, visitors - 1); // guide doesn't pay
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

        // Apply promotion discount on top if set
        if (promotionDiscount > 0) {
            totalPrice = totalPrice * (1.0 - (promotionDiscount / 100.0));
        }

        return totalPrice;
    }

    /**
     * Creates a new reservation with status PENDING.
     * Returns "SUCCESS:CODE:PRICE" or "FULL:..." or "ERROR:...".
     */
    public synchronized String createReservation(Reservation r) throws SQLException {
        Connection conn = dbController.getConnection();
        
        try {
            // Lock the table for this transaction
            conn.setAutoCommit(false);
            
            // Re-check availability inside the lock
            if (!isParkAvailable(conn, r.getParkId(), r.getVisitDate(), r.getEntryTime(), r.getNumVisitors(), 0)) {
                conn.rollback();
                conn.setAutoCommit(true);
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
            ps.setString(9, "PENDING");
            ps.setString(10, confirmationCode);
            ps.setBoolean(11, r.isPrepaid());

            int rows = ps.executeUpdate();
            ps.close();

            if (rows > 0) {
                conn.commit();
                conn.setAutoCommit(true);
                return "SUCCESS:" + confirmationCode + ":" + String.format("%.2f", calculatedPrice);
            } else {
                conn.rollback();
                conn.setAutoCommit(true);
                return "ERROR: Database insert failed.";
            }
            
        } catch (Exception e) {
            try { conn.rollback(); conn.setAutoCommit(true); } catch (Exception ignored) {}
            return "ERROR: " + e.getMessage();
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
     * Re-checks park availability (PENDING+CONFIRMED) before applying the change.
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

        if ("CANCELLED".equals(currentStatus)) {
            return false;
        }

        java.sql.Date vDate = java.sql.Date.valueOf(visitDate);
        java.sql.Time eTime = java.sql.Time.valueOf(entryTime);

        if (!isParkAvailable(conn, parkId, vDate, eTime, numVisitors, reservationId)) {
            return false;
        }

        String newConfirmationCode = generateConfirmationCode();

        String sql = "UPDATE reservations SET visit_date = ?, entry_time = ?, num_visitors = ?, confirmation_code = ? WHERE id = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setDate(1, vDate);
        ps.setTime(2, eTime);
        ps.setInt(3, numVisitors);
        ps.setString(4, newConfirmationCode);
        ps.setInt(5, reservationId);

        int rows = ps.executeUpdate();
        ps.close();

        return rows > 0;
    }

    /**
     * Cancels a reservation by setting status to CANCELLED.
     * Safety check: only cancels if it belongs to the given traveler.
     */
    public boolean deleteReservation(int reservationId, int travelerId, String travelerType) throws SQLException {
        Connection conn = dbController.getConnection();

        // First get park/date/time so we can notify waiting list after cancellation
        String selectSql =
            "SELECT park_id, visit_date, entry_time FROM reservations " +
            "WHERE id = ? AND traveler_id = ? AND traveler_type = ?";
        PreparedStatement selectPs = conn.prepareStatement(selectSql);
        selectPs.setInt(1, reservationId);
        selectPs.setInt(2, travelerId);
        selectPs.setString(3, travelerType);
        ResultSet rs = selectPs.executeQuery();

        int parkId = 0;
        java.sql.Date visitDate = null;
        java.sql.Time entryTime = null;
        if (rs.next()) {
            parkId    = rs.getInt("park_id");
            visitDate = rs.getDate("visit_date");
            entryTime = rs.getTime("entry_time");
        }
        rs.close();
        selectPs.close();

        // Cancel the reservation
        String sql = "UPDATE reservations SET status='CANCELLED' " +
                     "WHERE id = ? AND traveler_id = ? AND traveler_type = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, reservationId);
        ps.setInt(2, travelerId);
        ps.setString(3, travelerType);
        int rows = ps.executeUpdate();
        ps.close();

        // Notify next person on waiting list
        if (rows > 0 && parkId > 0) {
            notifyNextWaitingTraveler(parkId, visitDate, entryTime);
        }

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

    public ArrayList<Integer> getReservationsNeedingReminder() throws SQLException {
        Connection conn = dbController.getConnection();

        String sql =
            "SELECT id FROM reservations " +
            "WHERE status='PENDING' " +
            "AND reminder_sent = FALSE " +
            "AND DATEDIFF(visit_date,CURDATE()) = 1";

        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();

        ArrayList<Integer> ids = new ArrayList<>();
        while (rs.next()) {
            ids.add(rs.getInt("id"));
        }

        rs.close();
        ps.close();

        return ids;
    }

    public void markReminderSent(int reservationId) throws SQLException {
        Connection conn = dbController.getConnection();

        String sql =
            "UPDATE reservations " +
            "SET reminder_sent = TRUE, " +
            "reminder_sent_at = NOW() " +
            "WHERE id=?";

        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, reservationId);
        ps.executeUpdate();
        ps.close();
    }

    public void autoCancelExpiredReservations() throws SQLException {
        Connection conn = dbController.getConnection();

        String sql =
            "UPDATE reservations " +
            "SET status='CANCELLED' " +
            "WHERE reminder_sent=TRUE " +
            "AND reminder_confirmed=FALSE " +
            "AND TIMESTAMPDIFF(HOUR,reminder_sent_at,NOW()) >= 2";

        PreparedStatement ps = conn.prepareStatement(sql);
        ps.executeUpdate();
        ps.close();
    }

    public void autoCancelUnconfirmed() throws SQLException {
        Connection conn = dbController.getConnection();

        String sql =
            "UPDATE reservations " +
            "SET status='CANCELLED' " +
            "WHERE status='PENDING' " +
            "AND visit_date < CURDATE()";

        PreparedStatement ps = conn.prepareStatement(sql);
        ps.executeUpdate();
        ps.close();
    }

    public void processWaitingList() throws SQLException {
        Connection conn = dbController.getConnection();

        String sql =
            "SELECT * FROM waiting_list " +
            "ORDER BY position";

        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            System.out.println("[SIMULATION] Waiting list notification sent to " + rs.getString("email"));
        }

        rs.close();
        ps.close();
    }

    public boolean addToWaitingList(Reservation r) throws SQLException {
        Connection conn = dbController.getConnection();

        // Block if group size can never fit in this park
        String parkSql = "SELECT max_capacity, prebooked_reserved FROM parks WHERE id = ?";
        PreparedStatement parkPs = conn.prepareStatement(parkSql);
        parkPs.setInt(1, r.getParkId());
        ResultSet parkRs = parkPs.executeQuery();

        if (parkRs.next()) {
            int maxCapacity       = parkRs.getInt("max_capacity");
            int prebookedReserved = parkRs.getInt("prebooked_reserved");
            int allowedQuota      = maxCapacity - prebookedReserved;

            if (r.getNumVisitors() > allowedQuota) {
                parkRs.close(); parkPs.close();
                return false; // group too large, will never fit
            }
        }
        parkRs.close(); parkPs.close();

        // Get next position
        String posSql =
            "SELECT COALESCE(MAX(position), 0) + 1 " +
            "FROM waiting_list " +
            "WHERE park_id = ? AND visit_date = ? AND entry_time = ?";

        PreparedStatement posPs = conn.prepareStatement(posSql);
        posPs.setInt(1, r.getParkId());
        posPs.setDate(2, r.getVisitDate());
        posPs.setTime(3, r.getEntryTime());

        ResultSet rs = posPs.executeQuery();
        int position = 1;
        if (rs.next()) {
            position = rs.getInt(1);
        }
        rs.close(); posPs.close();

        String sql =
            "INSERT INTO waiting_list " +
            "(traveler_id, traveler_type, park_id, visit_date, entry_time, " +
            "num_visitors, email, position) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, r.getTravelerId());
        ps.setString(2, r.getTravelerType());
        ps.setInt(3, r.getParkId());
        ps.setDate(4, r.getVisitDate());
        ps.setTime(5, r.getEntryTime());
        ps.setInt(6, r.getNumVisitors());
        ps.setString(7, r.getEmail());
        ps.setInt(8, position);

        int rows = ps.executeUpdate();
        ps.close();

        return rows > 0;
    }
    
    public ArrayList<Integer> getAvailability(int parkId, String visitDate, String entryTime) throws SQLException {
        Connection conn = dbController.getConnection();

        String parkSql = "SELECT max_capacity, prebooked_reserved, avg_stay_hours FROM parks WHERE id = ?";
        PreparedStatement parkPs = conn.prepareStatement(parkSql);
        parkPs.setInt(1, parkId);
        ResultSet parkRs = parkPs.executeQuery();

        int maxCapacity = 0, prebookedReserved = 0, avgStay = 4;
        if (parkRs.next()) {
            maxCapacity       = parkRs.getInt("max_capacity");
            prebookedReserved = parkRs.getInt("prebooked_reserved");
            double avg        = parkRs.getDouble("avg_stay_hours");
            avgStay           = (avg <= 0) ? 4 : (int) avg;
        }
        parkRs.close(); parkPs.close();

        int allowedQuota = maxCapacity - prebookedReserved;

        // Calculate time window — same logic as isParkAvailable
        java.sql.Time startTime = java.sql.Time.valueOf(entryTime);
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(startTime);
        cal.add(java.util.Calendar.HOUR_OF_DAY, avgStay);
        java.sql.Time endTime = new java.sql.Time(cal.getTimeInMillis());

        // Only count reservations that OVERLAP with the selected time slot
        String checkSql =
        	    "SELECT COALESCE(SUM(num_visitors), 0) FROM reservations " +
        	    "WHERE park_id = ? AND visit_date = ? AND status IN ('PENDING','CONFIRMED','INSIDE') " +
        	    "AND entry_time < ? " +  // existing reservation starts before selected slot ends
        	    "AND ADDTIME(entry_time, SEC_TO_TIME(? * 3600)) > ?"; // existing reservation ends after selected slot starts

        PreparedStatement checkPs = conn.prepareStatement(checkSql);
        checkPs.setInt(1, parkId);
        checkPs.setDate(2, java.sql.Date.valueOf(visitDate));
        checkPs.setTime(3, endTime);       // existing.entry_time < selected.endTime
        checkPs.setInt(4, avgStay);        // avgStay hours in seconds
        checkPs.setTime(5, startTime);     // existing.endTime > selected.startTime
        ResultSet checkRs = checkPs.executeQuery();
        int booked = 0;
        if (checkRs.next()) booked = checkRs.getInt(1);
        checkRs.close(); checkPs.close();

        int available = Math.max(0, allowedQuota - booked);

        ArrayList<Integer> result = new ArrayList<>();
        result.add(booked);
        result.add(available);
        return result;
    }

    public boolean confirmReminder(int reservationId, int travelerId, String travelerType) throws SQLException {
        Connection conn = dbController.getConnection();

        String sql =
            "UPDATE reservations SET reminder_confirmed = TRUE, status = 'CONFIRMED' " +
            "WHERE id = ? AND traveler_id = ? AND traveler_type = ? AND status='PENDING'";

        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, reservationId);
        ps.setInt(2, travelerId);
        ps.setString(3, travelerType);

        int rows = ps.executeUpdate();
        ps.close();
        return rows > 0;
    }

    public void sendVisitReminders() throws SQLException {
        Connection conn = dbController.getConnection();

        // Send reminder when visit is between 23 and 25 hours from now (24hr window)
        String sql =
            "SELECT r.id, r.email " +
            "FROM reservations r " +
            "WHERE r.status='PENDING' " +
            "AND r.reminder_sent = FALSE " +
            "AND TIMESTAMPDIFF(MINUTE, NOW(), TIMESTAMP(visit_date, entry_time)) BETWEEN 1320 AND 1440";

        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            System.out.println("[SIMULATION] Reminder sent | Reservation ID: "
                    + rs.getInt("id")
                    + " | Email: " + rs.getString("email"));
        }
        rs.close();
        ps.close();

        String updateSql =
            "UPDATE reservations SET reminder_sent=TRUE, reminder_sent_at=NOW() " +
            "WHERE status='PENDING' AND reminder_sent=FALSE " +
            "AND TIMESTAMPDIFF(MINUTE, NOW(), TIMESTAMP(visit_date, entry_time)) BETWEEN 1320 AND 1440";

        PreparedStatement updatePs = conn.prepareStatement(updateSql);
        updatePs.executeUpdate();
        updatePs.close();
    }

    public void autoCancelUnconfirmedReservations() throws SQLException {
        Connection conn = dbController.getConnection();

        String selectSql =
            "SELECT id, park_id, visit_date, entry_time " +
            "FROM reservations " +
            "WHERE status='PENDING' " +
            "AND reminder_sent=TRUE " +
            "AND reminder_confirmed=FALSE " +
            "AND TIMESTAMPDIFF(HOUR, reminder_sent_at, NOW()) >= 2";

        PreparedStatement ps = conn.prepareStatement(selectSql);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            int reservationId = rs.getInt("id");
            int parkId = rs.getInt("park_id");
            java.sql.Date visitDate = rs.getDate("visit_date");
            java.sql.Time entryTime = rs.getTime("entry_time");

            PreparedStatement cancelPs =
                conn.prepareStatement("UPDATE reservations SET status='CANCELLED' WHERE id=?");
            cancelPs.setInt(1, reservationId);
            cancelPs.executeUpdate();
            cancelPs.close();

            notifyNextWaitingTraveler(parkId, visitDate, entryTime);
        }

        rs.close();
        ps.close();
    }

    public void notifyNextWaitingTraveler(int parkId, java.sql.Date visitDate, java.sql.Time entryTime) throws SQLException {
        Connection conn = dbController.getConnection();

        // Calculate available space first
        ArrayList<ArrayList<String>> parkResult = dbController.executeQuery(
            "SELECT max_capacity, prebooked_reserved FROM parks WHERE id = " + parkId);
        if (parkResult == null || parkResult.isEmpty()) return;

        int maxCapacity       = Integer.parseInt(parkResult.get(0).get(0));
        int prebookedReserved = Integer.parseInt(parkResult.get(0).get(1));

        ArrayList<ArrayList<String>> currentResult = dbController.executeQuery(
            "SELECT COALESCE(SUM(num_visitors), 0) FROM reservations " +
            "WHERE park_id = " + parkId +
            " AND visit_date = '" + visitDate + "'" +
            " AND entry_time = '" + entryTime + "'" +
            " AND status IN ('PENDING','CONFIRMED')");

        int currentBooked = 0;
        if (currentResult != null && !currentResult.isEmpty()) {
            currentBooked = Integer.parseInt(currentResult.get(0).get(0));
        }

        int availableSpots = maxCapacity - prebookedReserved - currentBooked;
        if (availableSpots <= 0) return;

        // Find first person in waiting list who fits the available space
        String sql =
            "SELECT wl.id, wl.email, wl.traveler_id, wl.traveler_type " +
            "FROM waiting_list wl " +
            "WHERE wl.park_id=? AND wl.visit_date=? AND wl.entry_time=? AND wl.status='WAITING' " +
            "AND wl.num_visitors <= " + availableSpots +
            " ORDER BY wl.position ASC LIMIT 1";

        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, parkId);
        ps.setDate(2, visitDate);
        ps.setTime(3, entryTime);

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            int waitingId       = rs.getInt("id");
            String email        = rs.getString("email");
            int travelerIdWL    = rs.getInt("traveler_id");
            String travelerTypeWL = rs.getString("traveler_type");

            System.out.println("[SIMULATION] Waiting list offer sent to: " + email);

            PreparedStatement updatePs = conn.prepareStatement(
                "UPDATE waiting_list SET status='NOTIFIED', notified_at=NOW(), " +
                "offer_expires_at=DATE_ADD(NOW(), INTERVAL 1 HOUR) WHERE id=?");
            updatePs.setInt(1, waitingId);
            updatePs.executeUpdate();
            updatePs.close();

            // Push real-time notification if user is logged in
            if (notificationCallback != null) {
                String username = lookupUsername(travelerIdWL, travelerTypeWL);
                if (username != null) {
                    ArrayList<String> notifRow = new ArrayList<>();
                    notifRow.add(String.valueOf(waitingId));

                    ArrayList<ArrayList<String>> parkNameResult = dbController.executeQuery(
                        "SELECT name FROM parks WHERE id = " + parkId);
                    String parkName = (parkNameResult != null && !parkNameResult.isEmpty())
                        ? parkNameResult.get(0).get(0) : "Park";

                    notifRow.add(parkName);
                    notifRow.add(String.valueOf(visitDate));
                    notifRow.add(String.valueOf(entryTime));

                    ArrayList<ArrayList<String>> wlResult = dbController.executeQuery(
                        "SELECT num_visitors, offer_expires_at FROM waiting_list WHERE id = " + waitingId);
                    if (wlResult != null && !wlResult.isEmpty()) {
                        notifRow.add(wlResult.get(0).get(0));
                        notifRow.add(wlResult.get(0).get(1));
                    }

                    ArrayList<ArrayList<String>> payload = new ArrayList<>();
                    ArrayList<String> markerRow = new ArrayList<>(notifRow);
                    markerRow.add(0, "WL_PUSH");
                    payload.add(markerRow);

                    notificationCallback.notifyUser(username, payload);
                }
            }
        }

        rs.close();
        ps.close();
    }
    
    private String lookupUsername(int travelerId, String travelerType) {
        try {
            String sql;
            if ("VISITOR".equals(travelerType)) {
                sql = "SELECT id_number FROM visitors WHERE id = " + travelerId;
            } else if ("SUBSCRIBER".equals(travelerType)) {
                sql = "SELECT id_number FROM subscribers WHERE id = " + travelerId;
            } else if ("GUIDE".equals(travelerType)) {
                sql = "SELECT username FROM guides WHERE id = " + travelerId;
            } else {
                return null;
            }
            ArrayList<ArrayList<String>> result = dbController.executeQuery(sql);
            if (result != null && !result.isEmpty()) {
                return result.get(0).get(0);
            }
        } catch (Exception e) {
            System.out.println("[ERROR] lookupUsername failed: " + e.getMessage());
        }
        return null;
    }

    public void expireWaitingListOffers() throws SQLException {
        Connection conn = dbController.getConnection();

        String sql =
            "UPDATE waiting_list SET status='EXPIRED' " +
            "WHERE status='NOTIFIED' AND offer_expires_at < NOW()";

        PreparedStatement ps = conn.prepareStatement(sql);
        ps.executeUpdate();
        ps.close();

        String oldSql =
            "UPDATE waiting_list SET status='EXPIRED' " +
            "WHERE TIMESTAMP(visit_date, entry_time) < NOW()";

        PreparedStatement oldPs = conn.prepareStatement(oldSql);
        oldPs.executeUpdate();
        oldPs.close();
    }
    
    public ArrayList<ArrayList<String>> getPendingReminders(int travelerId, String travelerType) throws SQLException {
        Connection conn = dbController.getConnection();
        String sql =
            "SELECT r.id, p.name, r.visit_date, r.entry_time, r.num_visitors, r.confirmation_code " +
            "FROM reservations r " +
            "JOIN parks p ON r.park_id = p.id " +
            "WHERE r.traveler_id = ? AND r.traveler_type = ? " +
            "AND r.status = 'PENDING' " +
            "AND r.reminder_sent = TRUE " +
            "AND r.reminder_confirmed = FALSE " +
            "AND TIMESTAMPDIFF(HOUR, r.reminder_sent_at, NOW()) < 2";

        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, travelerId);
        ps.setString(2, travelerType);
        ResultSet rs = ps.executeQuery();

        ArrayList<ArrayList<String>> result = new ArrayList<>();
        while (rs.next()) {
            ArrayList<String> row = new ArrayList<>();
            row.add(String.valueOf(rs.getInt(1)));
            row.add(rs.getString(2));
            row.add(String.valueOf(rs.getDate(3)));
            row.add(String.valueOf(rs.getTime(4)));
            row.add(String.valueOf(rs.getInt(5)));
            row.add(rs.getString(6));
            result.add(row);
        }
        rs.close();
        ps.close();
        return result;
    }
    public void sendVisitRemindersForUser(int travelerId, String travelerType) throws SQLException {
        Connection conn = dbController.getConnection();

        String sql =
            "UPDATE reservations SET reminder_sent=TRUE, reminder_sent_at=NOW() " +
            "WHERE traveler_id=? AND traveler_type=? " +
            "AND status='PENDING' " +
            "AND reminder_sent=FALSE " +
            "AND TIMESTAMPDIFF(MINUTE, NOW(), TIMESTAMP(visit_date, entry_time)) BETWEEN 1320 AND 1440";

        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, travelerId);
        ps.setString(2, travelerType);
        ps.executeUpdate();
        ps.close();
    }
    
    public ArrayList<ArrayList<String>> getPendingWaitingListNotifications(int travelerId, String travelerType) throws SQLException {
        Connection conn = dbController.getConnection();
        String sql =
            "SELECT wl.id, p.name, wl.visit_date, wl.entry_time, wl.num_visitors, wl.offer_expires_at " +
            "FROM waiting_list wl " +
            "JOIN parks p ON wl.park_id = p.id " +
            "WHERE wl.traveler_id = ? AND wl.traveler_type = ? " +
            "AND wl.status = 'NOTIFIED' " +
            "AND wl.offer_expires_at > NOW()";

        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, travelerId);
        ps.setString(2, travelerType);
        ResultSet rs = ps.executeQuery();

        ArrayList<ArrayList<String>> result = new ArrayList<>();
        while (rs.next()) {
            ArrayList<String> row = new ArrayList<>();
            row.add(String.valueOf(rs.getInt(1)));      // waiting_list id
            row.add(rs.getString(2));                    // park name
            row.add(String.valueOf(rs.getDate(3)));      // visit date
            row.add(String.valueOf(rs.getTime(4)));      // entry time
            row.add(String.valueOf(rs.getInt(5)));       // num visitors
            row.add(String.valueOf(rs.getTimestamp(6))); // offer expires at
            result.add(row);
        }
        rs.close();
        ps.close();
        return result;
    }
    
    public boolean confirmFromWaitingList(int waitingListId, int travelerId, String travelerType) throws SQLException {
        Connection conn = dbController.getConnection();

        // Get waiting list entry details
        String selectSql =
            "SELECT park_id, visit_date, entry_time, num_visitors, email " +
            "FROM waiting_list WHERE id = ? AND status = 'NOTIFIED'";
        PreparedStatement ps = conn.prepareStatement(selectSql);
        ps.setInt(1, waitingListId);
        ResultSet rs = ps.executeQuery();

        if (!rs.next()) { rs.close(); ps.close(); return false; }

        int parkId          = rs.getInt("park_id");
        java.sql.Date visitDate = rs.getDate("visit_date");
        java.sql.Time entryTime = rs.getTime("entry_time");
        int numVisitors     = rs.getInt("num_visitors");
        String email        = rs.getString("email");
        rs.close(); ps.close();

        // Check if space is available for this person
        if (!isParkAvailable(conn, parkId, visitDate, entryTime, numVisitors, 0)) {
            // Not enough space — expire this offer and notify next in line
            PreparedStatement expirePs = conn.prepareStatement(
                "UPDATE waiting_list SET status='EXPIRED' WHERE id=?");
            expirePs.setInt(1, waitingListId);
            expirePs.executeUpdate();
            expirePs.close();
            notifyNextWaitingTraveler(parkId, visitDate, entryTime);
            return false;
        }

        // Space available — create the reservation
        String confirmationCode = String.valueOf(10000000 + new java.util.Random().nextInt(90000000));

        String insertSql =
            "INSERT INTO reservations " +
            "(traveler_id, traveler_type, park_id, visit_date, entry_time, num_visitors, " +
            "email, type, status, confirmation_code, is_prepaid) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, 'INDIVIDUAL', 'CONFIRMED', ?, 0)";

        PreparedStatement insertPs = conn.prepareStatement(insertSql);
        insertPs.setInt(1, travelerId);
        insertPs.setString(2, travelerType);
        insertPs.setInt(3, parkId);
        insertPs.setDate(4, visitDate);
        insertPs.setTime(5, entryTime);
        insertPs.setInt(6, numVisitors);
        insertPs.setString(7, email);
        insertPs.setString(8, confirmationCode);
        int rows = insertPs.executeUpdate();
        insertPs.close();

        if (rows > 0) {
            PreparedStatement updatePs = conn.prepareStatement(
                "UPDATE waiting_list SET status='CONFIRMED' WHERE id=?");
            updatePs.setInt(1, waitingListId);
            updatePs.executeUpdate();
            updatePs.close();
            return true;
        }
        return false;
    }

    public boolean declineWaitingList(int waitingListId) throws SQLException {
        Connection conn = dbController.getConnection();

        // Get details to notify next in line
        String selectSql =
            "SELECT park_id, visit_date, entry_time FROM waiting_list WHERE id = ?";
        PreparedStatement ps = conn.prepareStatement(selectSql);
        ps.setInt(1, waitingListId);
        ResultSet rs = ps.executeQuery();

        int parkId = 0;
        java.sql.Date visitDate = null;
        java.sql.Time entryTime = null;
        if (rs.next()) {
            parkId    = rs.getInt("park_id");
            visitDate = rs.getDate("visit_date");
            entryTime = rs.getTime("entry_time");
        }
        rs.close(); ps.close();

        // Mark as expired
        PreparedStatement updatePs = conn.prepareStatement(
            "UPDATE waiting_list SET status='EXPIRED' WHERE id=?");
        updatePs.setInt(1, waitingListId);
        updatePs.executeUpdate();
        updatePs.close();

        // Notify next in line
        if (parkId > 0) {
            notifyNextWaitingTraveler(parkId, visitDate, entryTime);
        }
        return true;
    }
    public ArrayList<ArrayList<String>> getWaitingListByTraveler(int travelerId, String travelerType) throws SQLException {
        Connection conn = dbController.getConnection();
        String sql =
            "SELECT wl.id, p.name, wl.visit_date, wl.entry_time, wl.num_visitors, wl.position, wl.status " +
            "FROM waiting_list wl " +
            "JOIN parks p ON wl.park_id = p.id " +
            "WHERE wl.traveler_id = ? AND wl.traveler_type = ? " +
            "AND wl.status IN ('WAITING', 'NOTIFIED', 'CANCELLED') " +
            "ORDER BY wl.visit_date, wl.entry_time";

        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, travelerId);
        ps.setString(2, travelerType);
        ResultSet rs = ps.executeQuery();

        ArrayList<ArrayList<String>> result = new ArrayList<>();
        while (rs.next()) {
            ArrayList<String> row = new ArrayList<>();
            row.add(String.valueOf(rs.getInt(1)));
            row.add(rs.getString(2));
            row.add(String.valueOf(rs.getDate(3)));
            row.add(String.valueOf(rs.getTime(4)));
            row.add(String.valueOf(rs.getInt(5)));
            row.add(String.valueOf(rs.getInt(6)));
            row.add(rs.getString(7));
            result.add(row);
        }
        rs.close();
        ps.close();
        return result;
    }

    public boolean leaveWaitingList(int waitingListId) throws SQLException {
        Connection conn = dbController.getConnection();

        String selectSql =
            "SELECT park_id, visit_date, entry_time FROM waiting_list WHERE id = ?";
        PreparedStatement ps = conn.prepareStatement(selectSql);
        ps.setInt(1, waitingListId);
        ResultSet rs = ps.executeQuery();

        int parkId = 0;
        java.sql.Date visitDate = null;
        java.sql.Time entryTime = null;
        if (rs.next()) {
            parkId    = rs.getInt("park_id");
            visitDate = rs.getDate("visit_date");
            entryTime = rs.getTime("entry_time");
        }
        rs.close(); ps.close();

        // Mark as CANCELLED instead of deleting
        PreparedStatement updatePs = conn.prepareStatement(
        	    "UPDATE waiting_list SET status='CANCELLED' WHERE id=?");
        updatePs.setInt(1, waitingListId);
        int rows = updatePs.executeUpdate();
        updatePs.close();

        if (rows > 0 && parkId > 0) {
            notifyNextWaitingTraveler(parkId, visitDate, entryTime);
        }
        return rows > 0;
    }
}