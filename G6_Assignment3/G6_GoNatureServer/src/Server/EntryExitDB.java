package Server;

import java.util.ArrayList;
import Common.EntryExitResponse;

/**
 * The {@code EntryExitDB} class manages the persistence and logic for physical visitor traffic
 * at GoNature park control gates.
 * <p>
 * This subsystem processes reservation validations upon arrival, tracks real-time park occupancy counters,
 * handles ad-hoc walk-in permissions based on remaining capacity safety thresholds, and processes visitor exit protocols.
 * It also encapsulates the centralized GoNature discount and pricing engine algorithm to generate bills based on the pricing model.
 * </p>
 *
 * @author GoNature Development Team
 * @version 1.0
 */
public class EntryExitDB {

    /** Central persistence driver wrapper managing JDBC communications with the SQL database server. */
    private DBController dbController;

    /** Real-time observer monitor used to broadcast live occupancy modifications out to background host managers. */
    private VisitorCountCallback visitorCountCallback;

    /**
     * Interface defining structural hooks to capture and route real-time physical park occupancy updates 
     * when visitors enter or exit.
     */
    public interface VisitorCountCallback {
        /**
         * Fired immediately when a park's concurrent visitor headroom parameter undergoes modifications.
         *
         * @param parkId         Unique index referencing the modified national park.
         * @param currentCount   Active cumulative headcount currently inside the physical parameters.
         * @param availableSpots Total remaining vacancy quota permitted before locking entry validation blocks.
         */
        void onVisitorCountChanged(int parkId, int currentCount, int availableSpots);
    }

    /**
     * Constructs an {@code EntryExitDB} control manager component and secures a reference
     * to the database execution instance.
     */
    public EntryExitDB() {
        dbController = DBController.getInstance();
    }
    
    /**
     * Registers an external observer interface task used to funnel live occupancy change alerts out onto server modules.
     *
     * @param callback The tracking listener implementation matching specification rules.
     */
    public void setVisitorCountCallback(VisitorCountCallback callback) {
        this.visitorCountCallback = callback;
    }

    /**
     * Validates and processes a formal park arrival entry tracking command matching an active customer pre-booking.
     * <p>
     * Scans database indices looking for matching date bounds and statuses (PENDING/CONFIRMED) utilizing either 
     * a traveler citizen ID or an alpha-numeric confirmation tracking code. If valid, generates a payment invoice, 
     * logs a persistent visit record, and updates live park capacity metrics.
     * </p>
     *
     * @param identifier Distinct citizen identification card digits or systemic alpha-numeric tracking code.
     * @return Formulated {@link EntryExitResponse} payload summarizing entry authorization state, price bills, and capacity bounds.
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
     * Approves and processes an ad-hoc walk-in traveler entry transaction at a park entrance terminal.
     * <p>
     * Audits live occupancy parameters to ensure the new party size fits within current safety quotas 
     * (Max Capacity minus Pre-booked Quotas minus Current Visitors). If allowed, calculates the appropriate non-member 
     * walk-in payment pricing matrix, logs a detached visit entry record, and ticks active occupancy flags.
     * </p>
     *
     * @param parkId      Unique identifier mapping to the physical park gate location.
     * @param numVisitors Cumulative traveler headcount requested for validation entry.
     * @param visitorType Pricing structural archetype descriptor keyword (e.g., "INDIVIDUAL" or "GROUP").
     * @return Formulated {@link EntryExitResponse} tracking authorization passes, costs, and current occupancy indices.
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
     * Registers a physical exit event for an active visitor party tracking under an open reservation.
     * <p>
     * Locates the active visit record via confirmation code or traveler ID, sets the exit timestamp to NOW, 
     * decrements active visitor metrics for that park, updates the reservation status to EXITED, 
     * and triggers a visitor count callback notification.
     * </p>
     *
     * @param identifier Distinct client citizen ID number string or unique reservation alphanumeric confirmation code.
     * @return Formulated {@link EntryExitResponse} outlining tracking execution successes and recalculated park vacancy parameters.
     */
    public EntryExitResponse registerExit(String identifier) {
        try {
            identifier = identifier.trim();

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
     * Forcefully executes a manual exit offset, decrementing the concurrent occupancy tracking indices 
     * of a target national park by a given headcount volume.
     * Used primarily by park desk rangers managing walk-in tour groups that exit without standard badge scanning.
     *
     * @param parkId      Unique index key matching the targeted park.
     * @param numVisitors Headcount value representing the number of exiting visitors.
     * @return Formulated {@link EntryExitResponse} containing adjusted current capacity ratios.
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

    /**
     * Queries structural database tables to pull the current volume of active inside visitors tracked inside a park index.
     * If no monitoring row exists for the targeted park, an empty entry is initialized.
     *
     * @param parkId Index referencing the targeted national park.
     * @return Total integer headcount currently inside the park location.
     */
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

    /**
     * Builds a response detailing live capacity stats, current counts, and calculated open entry gaps.
     *
     * @param parkId Index tracking the requested national park location.
     * @return Formulated {@link EntryExitResponse} wrapping structural capacity parameters.
     */
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

    /**
     * Increments the live concurrent visitor tracking row of a given park location.
     * Automatically dispatches a callback update across registered backend thread listeners.
     *
     * @param parkId Unique database primary key entry locating the park row.
     * @param amount Headcount volume addition index.
     */
    private void increaseActiveVisitors(int parkId, int amount) {
        getCurrentVisitors(parkId);
        dbController.executeUpdate(
            "UPDATE active_visitors SET current_count = current_count + " + amount +
            " WHERE park_id = " + parkId);
        fireVisitorCountCallback(parkId);
    }

    /**
     * Decrements the live concurrent visitor tracking row of a given park location.
     * Employs safe data clamping thresholds to prevent counts from dropping below 0.
     * Automatically dispatches a callback update across registered backend thread listeners.
     *
     * @param parkId Unique database primary key entry locating the park row.
     * @param amount Headcount volume reduction index.
     */
    private void decreaseActiveVisitors(int parkId, int amount) {
        int current = getCurrentVisitors(parkId);
        int safe = Math.min(amount, current);
        dbController.executeUpdate(
            "UPDATE active_visitors SET current_count = GREATEST(0, current_count - " + safe + ")" +
            " WHERE park_id = " + parkId);
        fireVisitorCountCallback(parkId);
    }

    /**
     * Resolves layout thresholds for a park and fires the visitor count callback to update active clients in real-time.
     *
     * @param parkId Unique index tracking the modified park location.
     */
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
            // silent fallback
        }
    }

    /**
     * Utility look-up fetching the absolute highest structural entry key inside the visit logs.
     * Used to assign tracking indexes to active responses.
     *
     * @return Highest index key tracked; returns 0 if table lacks rows.
     */
    private int getLastVisitId() {
        ArrayList<ArrayList<String>> result = dbController.executeQuery(
            "SELECT MAX(id) FROM park_visits");
        if (result == null || result.isEmpty() || result.get(0).get(0) == null) return 0;
        return Integer.parseInt(result.get(0).get(0));
    }

    /**
     * Evaluates database rows to assert whether a single reservation key has an active visit row 
     * that has not recorded an exit timestamp yet.
     *
     * @param reservationId Target index identifying the specific booking.
     * @return {@code true} if an un-exited entry matches the parameter id; {@code false} otherwise.
     */
    private boolean hasOpenVisitForReservation(int reservationId) {
        ArrayList<ArrayList<String>> result = dbController.executeQuery(
            "SELECT id FROM park_visits WHERE reservation_id = " + reservationId + " AND exit_time IS NULL");
        return result != null && !result.isEmpty();
    }

    /**
     * Compiles and resolves financial payment invoices based on the official GoNature multi-tiered pricing model.
     * Calculates pricing rules based on booking types, pre-payments, and membership statuses:
     * <ul>
     * <li>Personal/Family pre-booked: 15% discount on full base price.</li>
     * <li>Personal/Family walk-in: Full standard base price.</li>
     * <li>Group pre-booked: 25% base discount + 12% extra discount if pre-paid, and Group Guide passes for free.</li>
     * <li>Group walk-in: 10% base discount, and Group Guide pays standard rate.</li>
     * <li>Subscriber status: Additional 10% cumulative compound discount applied on top of any of the above paths.</li>
     * <li>Active promotional conditions are evaluated last as a percentage deduction off the final cumulative group sum.</li>
     * </ul>
     *
     * @param fullPrice         Base individual price standard tracking index configured for the park.
     * @param numVisitors       Cumulative total headcount count of people inside the party.
     * @param type              Organizational category designation keyword ("INDIVIDUAL" or "GROUP").
     * @param hasReservation    Flag indicating if the visit was pre-booked.
     * @param isPrepaid         Flag tracking if payment transaction tokens were settled prior to arrival.
     * @param isSubscriber      Flag tracking if the customer holds an active Subscriber tier account.
     * @param promotionDiscount Active seasonal percentage reduction token layer loaded from park configurations.
     * @return Rounded double value tracking the total aggregated cost of the generated payment bill.
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