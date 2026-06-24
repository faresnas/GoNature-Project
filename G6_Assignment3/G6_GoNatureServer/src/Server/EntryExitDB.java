package Server;

import java.util.ArrayList;
import Common.EntryExitResponse;

public class EntryExitDB {

    private DBController dbController;

    public EntryExitDB() {
        dbController = DBController.getInstance();
    }
    
    public interface VisitorCountCallback {
        void onVisitorCountChanged(int parkId, int currentCount, int availableSpots);
    }

    private VisitorCountCallback visitorCountCallback;

    public void setVisitorCountCallback(VisitorCountCallback callback) {
        this.visitorCountCallback = callback;
    }

    /**
     * Approves entry for a visitor with a confirmed or pending reservation.
     * Accepts traveler ID or confirmation code.
     * Generates and returns a payment bill based on the pricing model.
     */
    public EntryExitResponse approveReservationEntry(String identifier) {
        try {
            identifier = identifier.trim();

            String sql =
                "SELECT r.id, r.traveler_id, r.park_id, r.num_visitors, r.type, r.traveler_type, r.is_prepaid, " +
                "p.full_price, p.promotion_discount, p.max_capacity, p.prebooked_reserved " +
                "FROM reservations r " +
                "JOIN parks p ON r.park_id = p.id " +
                "WHERE (r.confirmation_code = '" + identifier + "' OR CAST(r.traveler_id AS CHAR) = '" + identifier + "') " +
                "AND r.visit_date = CURDATE() " +
                "AND UPPER(r.status) IN ('CONFIRMED', 'PENDING') " +
                "LIMIT 1";

            ArrayList<ArrayList<String>> result = dbController.executeQuery(sql);

            if (result == null || result.isEmpty()) {
                return new EntryExitResponse(false,
                    "No valid confirmed reservation found for today.");
            }

            ArrayList<String> row = result.get(0);
            int reservationId      = Integer.parseInt(row.get(0));
            int parkId             = Integer.parseInt(row.get(2));
            int numVisitors        = Integer.parseInt(row.get(3));
            String reservationType = row.get(4);
            String travelerType    = row.get(5);
            boolean isPrepaid      = row.get(6).equals("1") || row.get(6).equalsIgnoreCase("true");
            double fullPrice       = Double.parseDouble(row.get(7));
            double promotionDisc   = Double.parseDouble(row.get(8));
            int maxCapacity        = Integer.parseInt(row.get(9));
            int prebookedReserved  = Integer.parseInt(row.get(10));

            if (hasOpenVisitForReservation(reservationId)) {
                return new EntryExitResponse(false,
                    "This reservation already has an active visit that has not exited yet.");
            }

            boolean isSubscriber = travelerType.equalsIgnoreCase("SUBSCRIBER");

            double amount = calculatePayment(
                fullPrice, numVisitors, reservationType,
                true, isPrepaid, isSubscriber, promotionDisc
            );

            String insertVisit =
                "INSERT INTO park_visits " +
                "(reservation_id, park_id, entry_time, exit_time, num_visitors, visitor_type) VALUES (" +
                reservationId + ", " + parkId + ", NOW(), NULL, " + numVisitors + ", '" + reservationType + "')";

            int insertResult = dbController.executeUpdate(insertVisit);
            if (insertResult <= 0) {
                return new EntryExitResponse(false, "Failed to record park entry.");
            }

            increaseActiveVisitors(parkId, numVisitors);

            // Mark reservation as INSIDE
            dbController.executeUpdate(
                "UPDATE reservations SET status='INSIDE' WHERE id=" + reservationId);

            int currentVisitors = getCurrentVisitors(parkId);
            int available = Math.max(0, maxCapacity - prebookedReserved - currentVisitors);
            int visitId = getLastVisitId();

            return new EntryExitResponse(true,
                "Entry approved. Payment bill generated.",
                amount, currentVisitors, visitId, available);

        } catch (Exception e) {
            return new EntryExitResponse(false, "Entry failed: " + e.getMessage());
        }
    }

    /**
     * Approves walk-in entry if space is available.
     * Generates and returns a payment bill based on visitor type.
     */
    public EntryExitResponse approveWalkInEntry(int parkId, int numVisitors, String visitorType) {
        try {
            ArrayList<ArrayList<String>> parkResult = dbController.executeQuery(
                "SELECT max_capacity, prebooked_reserved, full_price, promotion_discount FROM parks WHERE id = " + parkId);

            if (parkResult == null || parkResult.isEmpty()) {
                return new EntryExitResponse(false, "Park not found.");
            }

            ArrayList<String> parkRow = parkResult.get(0);
            int maxCapacity       = Integer.parseInt(parkRow.get(0));
            int prebookedReserved = Integer.parseInt(parkRow.get(1));
            double fullPrice      = Double.parseDouble(parkRow.get(2));
            double promotionDisc  = Double.parseDouble(parkRow.get(3));

            int currentVisitors = getCurrentVisitors(parkId);
            int availableForWalkIn = maxCapacity - prebookedReserved - currentVisitors;

            if (numVisitors > availableForWalkIn) {
                return new EntryExitResponse(false,
                    "Park is at full capacity. Walk-in entry is not available.");
            }

            boolean isSubscriber = visitorType.equalsIgnoreCase("SUBSCRIBER");
            double amount = calculatePayment(fullPrice, numVisitors, visitorType, false, false, isSubscriber, promotionDisc);

            String insertVisit =
                "INSERT INTO park_visits " +
                "(reservation_id, park_id, entry_time, exit_time, num_visitors, visitor_type) VALUES (" +
                "NULL, " + parkId + ", NOW(), NULL, " + numVisitors + ", '" + visitorType + "')";

            int insertResult = dbController.executeUpdate(insertVisit);
            if (insertResult <= 0) {
                return new EntryExitResponse(false, "Failed to record walk-in entry.");
            }

            increaseActiveVisitors(parkId, numVisitors);
            int updatedCurrent = getCurrentVisitors(parkId);
            int available = Math.max(0, maxCapacity - prebookedReserved - updatedCurrent);
            int visitId = getLastVisitId();

            return new EntryExitResponse(true,
                "Walk-in entry approved. Payment bill generated.",
                amount, updatedCurrent, visitId, available);

        } catch (Exception e) {
            return new EntryExitResponse(false, "Walk-in entry failed: " + e.getMessage());
        }
    }

    /**
     * Registers exit for a specific visit ID.
     * Updates reservation status to EXITED.
     */
    public EntryExitResponse registerExit(String identifier) {
        try {
            identifier = identifier.trim();

            // Look up the open visit by confirmation code or traveler ID
            String sql =
                "SELECT pv.id, pv.park_id, pv.num_visitors FROM park_visits pv " +
                "JOIN reservations r ON pv.reservation_id = r.id " +
                "WHERE pv.exit_time IS NULL " +
                "AND pv.park_id = (SELECT park_id FROM reservations WHERE " +
                "    (confirmation_code = '" + identifier + "' OR CAST(traveler_id AS CHAR) = '" + identifier + "') " +
                "    AND visit_date = CURDATE() LIMIT 1) " +
                "AND (r.confirmation_code = '" + identifier + "' OR CAST(r.traveler_id AS CHAR) = '" + identifier + "') " +
                "LIMIT 1";

            ArrayList<ArrayList<String>> visitResult = dbController.executeQuery(sql);

            if (visitResult == null || visitResult.isEmpty()) {
                return new EntryExitResponse(false, "No active visit found for this identifier.");
            }

            ArrayList<String> visitRow = visitResult.get(0);
            int visitId     = Integer.parseInt(visitRow.get(0));
            int parkId      = Integer.parseInt(visitRow.get(1));
            int numVisitors = Integer.parseInt(visitRow.get(2));

            int updateResult = dbController.executeUpdate(
                "UPDATE park_visits SET exit_time = NOW() WHERE id = " + visitId + " AND exit_time IS NULL");

            if (updateResult <= 0) {
                return new EntryExitResponse(false, "Failed to register exit.");
            }

            decreaseActiveVisitors(parkId, numVisitors);

            // Mark reservation as EXITED
            dbController.executeUpdate(
                "UPDATE reservations r " +
                "JOIN park_visits pv ON pv.reservation_id = r.id " +
                "SET r.status='EXITED' WHERE pv.id=" + visitId);

            int currentVisitors = getCurrentVisitors(parkId);

            ArrayList<ArrayList<String>> parkResult = dbController.executeQuery(
                "SELECT max_capacity, prebooked_reserved FROM parks WHERE id = " + parkId);
            int available = 0;
            if (parkResult != null && !parkResult.isEmpty()) {
                int maxCap  = Integer.parseInt(parkResult.get(0).get(0));
                int prebook = Integer.parseInt(parkResult.get(0).get(1));
                available = Math.max(0, maxCap - prebook - currentVisitors);
            }

            return new EntryExitResponse(true,
                "Exit registered successfully.",
                0, currentVisitors, visitId, available);

        } catch (Exception e) {
            return new EntryExitResponse(false, "Exit failed: " + e.getMessage());
        }
    }

    /**
     * Manually registers exit for a given number of visitors from a park.
     */
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
                return new EntryExitResponse(false,
                    "Cannot exit more visitors than currently inside the park.");
            }

            decreaseActiveVisitors(parkId, numVisitors);
            int updatedCurrent = getCurrentVisitors(parkId);

            ArrayList<ArrayList<String>> parkResult = dbController.executeQuery(
                "SELECT max_capacity, prebooked_reserved FROM parks WHERE id = " + parkId);
            int available = 0;
            if (parkResult != null && !parkResult.isEmpty()) {
                int maxCap  = Integer.parseInt(parkResult.get(0).get(0));
                int prebook = Integer.parseInt(parkResult.get(0).get(1));
                available = Math.max(0, maxCap - prebook - updatedCurrent);
            }

            return new EntryExitResponse(true,
                "Manual exit registered successfully.",
                0, updatedCurrent, 0, available);

        } catch (Exception e) {
            return new EntryExitResponse(false, "Manual exit failed: " + e.getMessage());
        }
    }

    public int getCurrentVisitors(int parkId) {
        ArrayList<ArrayList<String>> result = dbController.executeQuery(
            "SELECT current_count FROM active_visitors WHERE park_id = " + parkId);

        if (result == null || result.isEmpty()) {
            dbController.executeUpdate(
                "INSERT INTO active_visitors (park_id, current_count) VALUES (" + parkId + ", 0)");
            return 0;
        }
        return Integer.parseInt(result.get(0).get(0));
    }

    public EntryExitResponse getCurrentVisitorsResponse(int parkId) {
        int current = getCurrentVisitors(parkId);

        ArrayList<ArrayList<String>> parkResult = dbController.executeQuery(
            "SELECT max_capacity, prebooked_reserved FROM parks WHERE id = " + parkId);
        int available = 0;
        if (parkResult != null && !parkResult.isEmpty()) {
            int maxCap  = Integer.parseInt(parkResult.get(0).get(0));
            int prebook = Integer.parseInt(parkResult.get(0).get(1));
            available = Math.max(0, maxCap - prebook - current);
        }

        return new EntryExitResponse(true, "Current visitor count loaded.", 0, current, 0, available);
    }

    private void increaseActiveVisitors(int parkId, int amount) {
        getCurrentVisitors(parkId);
        dbController.executeUpdate(
            "UPDATE active_visitors SET current_count = current_count + " + amount +
            " WHERE park_id = " + parkId);
        fireVisitorCountCallback(parkId);
    }

    private void decreaseActiveVisitors(int parkId, int amount) {
        int current = getCurrentVisitors(parkId);
        int safe = Math.min(amount, current);
        dbController.executeUpdate(
            "UPDATE active_visitors SET current_count = GREATEST(0, current_count - " + safe + ")" +
            " WHERE park_id = " + parkId);
        fireVisitorCountCallback(parkId);
    }

    private void fireVisitorCountCallback(int parkId) {
        if (visitorCountCallback == null) return;
        try {
            ArrayList<ArrayList<String>> parkResult = dbController.executeQuery(
                "SELECT max_capacity, prebooked_reserved FROM parks WHERE id = " + parkId);
            int current = getCurrentVisitors(parkId);
            int available = 0;
            if (parkResult != null && !parkResult.isEmpty()) {
                int maxCap  = Integer.parseInt(parkResult.get(0).get(0));
                int prebook = Integer.parseInt(parkResult.get(0).get(1));
                available = Math.max(0, maxCap - prebook - current);
            }
            visitorCountCallback.onVisitorCountChanged(parkId, current, available);
        } catch (Exception e) {
            // silent
        }
    }

    private int getLastVisitId() {
        ArrayList<ArrayList<String>> result = dbController.executeQuery(
            "SELECT MAX(id) FROM park_visits");
        if (result == null || result.isEmpty() || result.get(0).get(0) == null) return 0;
        return Integer.parseInt(result.get(0).get(0));
    }

    private boolean hasOpenVisitForReservation(int reservationId) {
        ArrayList<ArrayList<String>> result = dbController.executeQuery(
            "SELECT id FROM park_visits WHERE reservation_id = " + reservationId + " AND exit_time IS NULL");
        return result != null && !result.isEmpty();
    }

    /**
     * Calculates total payment based on the GoNature pricing model:
     *
     * 1. Personal/family pre-booked:   15% off full price
     * 2. Personal/family walk-in:      full price
     * 3. Group pre-booked:             25% off, +12% off if prepaid, guide free
     * 4. Group walk-in:                10% off, guide pays
     * 5. Subscriber:                   additional 10% off (cumulative) on top of any above
     *
     * Promotion discount applied last on the total.
     */
    private double calculatePayment(double fullPrice, int numVisitors, String type,
            boolean hasReservation, boolean isPrepaid,
            boolean isSubscriber, double promotionDiscount) {

        double pricePerPerson = fullPrice;
        int payingVisitors = numVisitors;

        if (hasReservation && type.equalsIgnoreCase("GROUP")) {
            pricePerPerson = fullPrice * 0.75;
            if (isPrepaid) {
                pricePerPerson = pricePerPerson * 0.88;
            }
            payingVisitors = Math.max(0, numVisitors - 1);

        } else if (!hasReservation && type.equalsIgnoreCase("GROUP")) {
            pricePerPerson = fullPrice * 0.90;

        } else if (hasReservation) {
            pricePerPerson = fullPrice * 0.85;
        }

        if (isSubscriber) {
            pricePerPerson = pricePerPerson * 0.90;
        }

        double total = pricePerPerson * payingVisitors;

        if (promotionDiscount > 0) {
            total = total * (1.0 - (promotionDiscount / 100.0));
        }

        return Math.round(total * 100.0) / 100.0;
    }
}