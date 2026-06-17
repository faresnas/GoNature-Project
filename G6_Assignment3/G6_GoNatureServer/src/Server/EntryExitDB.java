package Server;

import java.time.LocalDate;
import java.util.ArrayList;

import Common.EntryExitResponse;

/**
 * Handles Feature 4: park entry, walk-in entry, exit registration,
 * active visitor count and payment calculation.
 */
public class EntryExitDB {

    private DBController dbController;

    public EntryExitDB() {
        dbController = DBController.getInstance();
    }

    /**
     * Entry with reservation using traveler ID or confirmation code.
     *
     * @param identifier traveler ID or confirmation code
     * @return entry response including payment bill
     */
    public EntryExitResponse approveReservationEntry(String identifier) {
        try {
            identifier = identifier.trim();

            String sql =
                    "SELECT r.id, r.traveler_id, r.park_id, r.num_visitors, r.type, r.is_prepaid, p.full_price " +
                    "FROM reservations r " +
                    "JOIN parks p ON r.park_id = p.id " +
                    "WHERE (r.confirmation_code = '" + identifier + "' OR CAST(r.traveler_id AS CHAR) = '" + identifier + "') " +
                    "AND r.visit_date = CURDATE() " +
                    "AND UPPER(r.status) = 'CONFIRMED' " +
                    "LIMIT 1";

            ArrayList<ArrayList<String>> result = dbController.executeQuery(sql);

            if (result == null || result.isEmpty()) {
                return new EntryExitResponse(false,
                        "No valid confirmed reservation found for today.");
            }

            ArrayList<String> row = result.get(0);

            int reservationId = Integer.parseInt(row.get(0));
            int travelerId = Integer.parseInt(row.get(1));
            int parkId = Integer.parseInt(row.get(2));
            int numVisitors = Integer.parseInt(row.get(3));
            String visitorType = row.get(4);
            boolean isPrepaid = row.get(5).equals("1") || row.get(5).equalsIgnoreCase("true");
            double fullPrice = Double.parseDouble(row.get(6));

            if (hasOpenVisitForReservation(reservationId)) {
                return new EntryExitResponse(false,
                        "This reservation already entered the park and has not exited yet.");
            }

            boolean isSubscriber = visitorType.equalsIgnoreCase("SUBSCRIBER");

            double amount = calculatePayment(
                    fullPrice,
                    numVisitors,
                    visitorType,
                    true,
                    isPrepaid,
                    isSubscriber
            );

            String insertVisit =
                    "INSERT INTO park_visits " +
                    "(reservation_id, park_id, entry_time, exit_time, num_visitors, visitor_type) VALUES (" +
                    reservationId + ", " +
                    parkId + ", " +
                    "NOW(), " +
                    "NULL, " +
                    numVisitors + ", '" +
                    visitorType + "')";

            int insertResult = dbController.executeUpdate(insertVisit);

            if (insertResult <= 0) {
                return new EntryExitResponse(false, "Failed to record park entry.");
            }

            increaseActiveVisitors(parkId, numVisitors);

            int currentVisitors = getCurrentVisitors(parkId);
            int visitId = getLastVisitId();

            return new EntryExitResponse(true,
                    "Entry approved. Payment bill generated.",
                    amount,
                    currentVisitors,
                    visitId);

        } catch (Exception e) {
            return new EntryExitResponse(false, "Entry failed: " + e.getMessage());
        }
    }

    /**
     * Walk-in entry without reservation.
     *
     * @param parkId park ID
     * @param numVisitors number of visitors
     * @param visitorType visitor type
     * @return entry response including bill
     */
    public EntryExitResponse approveWalkInEntry(int parkId, int numVisitors, String visitorType) {
        try {
            ArrayList<ArrayList<String>> parkResult =
                    dbController.executeQuery("SELECT max_capacity, reserved_quota, full_price FROM parks WHERE id = " + parkId);

            if (parkResult == null || parkResult.isEmpty()) {
                return new EntryExitResponse(false, "Park not found.");
            }

            ArrayList<String> parkRow = parkResult.get(0);

            int maxCapacity = Integer.parseInt(parkRow.get(0));
            int reservedQuota = Integer.parseInt(parkRow.get(1));
            double fullPrice = Double.parseDouble(parkRow.get(2));

            int currentVisitors = getCurrentVisitors(parkId);
            int availableForWalkIn = maxCapacity - reservedQuota - currentVisitors;

            if (numVisitors > availableForWalkIn) {
                return new EntryExitResponse(false,
                        "Park is at full capacity. Walk-in entry is not available.");
            }

            boolean isSubscriber = visitorType.equalsIgnoreCase("SUBSCRIBER");
            double amount = calculatePayment(fullPrice, numVisitors, visitorType, false, false, isSubscriber);

            String insertVisit = "INSERT INTO park_visits " +
                    "(reservation_id, park_id, entry_time, exit_time, num_visitors, visitor_type) VALUES (" +
                    "NULL, " + parkId + ", NOW(), NULL, " + numVisitors + ", '" + visitorType + "')";

            int insertResult = dbController.executeUpdate(insertVisit);

            if (insertResult <= 0) {
                return new EntryExitResponse(false, "Failed to record walk-in entry.");
            }

            increaseActiveVisitors(parkId, numVisitors);
            int updatedCurrentVisitors = getCurrentVisitors(parkId);
            int visitId = getLastVisitId();

            return new EntryExitResponse(true,
                    "Walk-in entry approved. Payment bill generated.",
                    amount,
                    updatedCurrentVisitors,
                    visitId);

        } catch (Exception e) {
            return new EntryExitResponse(false, "Walk-in entry failed: " + e.getMessage());
        }
    }

    /**
     * Registers visitor exit.
     *
     * @param visitId park visit ID
     * @return response with updated visitor count
     */
    public EntryExitResponse registerExit(int visitId) {
        try {
            ArrayList<ArrayList<String>> visitResult =
                    dbController.executeQuery("SELECT park_id, num_visitors FROM park_visits " +
                            "WHERE id = " + visitId + " AND exit_time IS NULL");

            if (visitResult == null || visitResult.isEmpty()) {
                return new EntryExitResponse(false, "No active visit found for this visit ID.");
            }

            ArrayList<String> visitRow = visitResult.get(0);
            int parkId = Integer.parseInt(visitRow.get(0));
            int numVisitors = Integer.parseInt(visitRow.get(1));

            int updateResult = dbController.executeUpdate(
                    "UPDATE park_visits SET exit_time = NOW() WHERE id = " + visitId + " AND exit_time IS NULL");

            if (updateResult <= 0) {
                return new EntryExitResponse(false, "Failed to register exit.");
            }

            decreaseActiveVisitors(parkId, numVisitors);
            int currentVisitors = getCurrentVisitors(parkId);

            return new EntryExitResponse(true,
                    "Exit registered successfully.",
                    0,
                    currentVisitors,
                    visitId);

        } catch (Exception e) {
            return new EntryExitResponse(false, "Exit failed: " + e.getMessage());
        }
    }

    /**
     * Gets current active visitors for a park.
     *
     * @param parkId park ID
     * @return current visitor count
     */
    public int getCurrentVisitors(int parkId) {
        ArrayList<ArrayList<String>> result =
                dbController.executeQuery("SELECT current_count FROM active_visitors WHERE park_id = " + parkId);

        if (result == null || result.isEmpty()) {
            dbController.executeUpdate("INSERT INTO active_visitors (park_id, current_count) VALUES (" + parkId + ", 0)");
            return 0;
        }

        return Integer.parseInt(result.get(0).get(0));
    }

    /**
     * Returns current visitors as response object.
     *
     * @param parkId park ID
     * @return response with count
     */
    public EntryExitResponse getCurrentVisitorsResponse(int parkId) {
        int count = getCurrentVisitors(parkId);
        return new EntryExitResponse(true, "Current visitor count loaded.", 0, count, 0);
    }

    private void increaseActiveVisitors(int parkId, int amount) {
        int current = getCurrentVisitors(parkId);
        dbController.executeUpdate("UPDATE active_visitors SET current_count = " +
                (current + amount) + " WHERE park_id = " + parkId);
    }

    private void decreaseActiveVisitors(int parkId, int amount) {
        int current = getCurrentVisitors(parkId);
        int updated = Math.max(current - amount, 0);
        dbController.executeUpdate("UPDATE active_visitors SET current_count = " +
                updated + " WHERE park_id = " + parkId);
    }

    private double getParkFullPrice(int parkId) {
        ArrayList<ArrayList<String>> result =
                dbController.executeQuery("SELECT full_price FROM parks WHERE id = " + parkId);

        if (result == null || result.isEmpty()) {
            return 0;
        }

        return Double.parseDouble(result.get(0).get(0));
    }

    private int getLastVisitId() {
        ArrayList<ArrayList<String>> result =
                dbController.executeQuery("SELECT MAX(id) FROM park_visits");

        if (result == null || result.isEmpty() || result.get(0).get(0).isEmpty()) {
            return 0;
        }

        return Integer.parseInt(result.get(0).get(0));
    }

    private boolean hasOpenVisitForReservation(int reservationId) {
        ArrayList<ArrayList<String>> result =
                dbController.executeQuery("SELECT id FROM park_visits WHERE reservation_id = " +
                        reservationId + " AND exit_time IS NULL");

        return result != null && !result.isEmpty();
    }

    /**
     * Calculates payment according to Feature 2 pricing rules.
     *
     * @param fullPrice full park ticket price
     * @param numVisitors number of visitors
     * @param type visitor/reservation type
     * @param hasReservation true if pre-booked
     * @param isPrepaid true if paid in advance
     * @param isSubscriber true if subscriber
     * @return total amount
     */
    private double calculatePayment(double fullPrice, int numVisitors, String type,
                                    boolean hasReservation, boolean isPrepaid, boolean isSubscriber) {

        double total = fullPrice * numVisitors;

        if (hasReservation && type.equalsIgnoreCase("INDIVIDUAL")) {
            total *= 0.85;
        } else if (hasReservation && type.equalsIgnoreCase("GROUP")) {
            total *= 0.75;
            if (isPrepaid) {
                total *= 0.88;
            }
        } else if (!hasReservation && type.equalsIgnoreCase("GROUP")) {
            total *= 0.90;
        }

        if (isSubscriber) {
            total *= 0.90;
        }

        return Math.round(total * 100.0) / 100.0;
    }
    public EntryExitResponse registerManualExit(int parkId, int numVisitors) {
        try {
            if (numVisitors <= 0) {
                return new EntryExitResponse(false, "Number of visitors must be greater than 0.");
            }

            int current = getCurrentVisitors(parkId);

            if (current <= 0) {
                return new EntryExitResponse(false, "There are no active visitors in this park.");
            }

            if (numVisitors > current) {
                return new EntryExitResponse(false, "Cannot exit more visitors than currently inside the park.");
            }

            decreaseActiveVisitors(parkId, numVisitors);

            int updatedCurrentVisitors = getCurrentVisitors(parkId);

            return new EntryExitResponse(true,
                    "Manual exit registered successfully.",
                    0,
                    updatedCurrentVisitors,
                    0);

        } catch (Exception e) {
            return new EntryExitResponse(false, "Manual exit failed: " + e.getMessage());
        }
    }
}