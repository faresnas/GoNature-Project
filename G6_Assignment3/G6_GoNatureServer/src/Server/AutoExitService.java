package Server;

/**
 * Background service responsible for automatically closing
 * park visits when visitors exceed the allowed stay duration
 * or when the parks are closed at the end of the day.
 * <p>
 * The service runs continuously in its own thread and
 * periodically checks the database for expired visits.
 */
public class AutoExitService extends Thread {

    /**
     * Handles reservation-related database operations.
     */
    private ReservationDB reservationDB;

    /**
     * Handles park entry and exit database operations.
     */
    private EntryExitDB entryExitDB;

    /**
     * Creates a new automatic exit service.
     *
     * @param reservationDB the reservation database manager
     * @param entryExitDB the entry and exit database manager
     */
    public AutoExitService(ReservationDB reservationDB,
                           EntryExitDB entryExitDB) {
        this.reservationDB = reservationDB;
        this.entryExitDB = entryExitDB;
    }

    /**
     * Starts the background service.
     * The service periodically performs automatic visitor exits
     * and closes all active visits at park closing time.
     */
    @Override
    public void run() {
        while (true) {
            try {
                autoExitExpiredVisitors();
                closeParkAt18();

                Thread.sleep(60000);

            } catch (InterruptedException e) {
                break;

            } catch (Exception e) {
                // Silent failure
            }
        }
    }

    /**
     * Automatically checks for visitors whose allowed stay time
     * has expired and registers their exit.
     * The current visitor count of each affected park is updated
     * accordingly.
     */
    private void autoExitExpiredVisitors() {

        try {

            String sql =
                "SELECT pv.id, pv.park_id, pv.num_visitors " +
                "FROM park_visits pv " +
                "JOIN parks p ON pv.park_id = p.id " +
                "WHERE pv.exit_time IS NULL " +
                "AND TIMESTAMPADD(HOUR, p.avg_stay_hours, pv.entry_time) < NOW()";

            java.util.ArrayList<java.util.ArrayList<String>> expired =
                    DBController.getInstance().executeQuery(sql);

            if (expired == null || expired.isEmpty()) {
                return;
            }

            for (java.util.ArrayList<String> row : expired) {

                int visitId =
                        Integer.parseInt(row.get(0));

                int parkId =
                        Integer.parseInt(row.get(1));

                int numVisitors =
                        Integer.parseInt(row.get(2));

                DBController.getInstance().executeUpdate(
                        "UPDATE park_visits SET exit_time = NOW() WHERE id = "
                                + visitId);

                DBController.getInstance().executeUpdate(
                        "UPDATE active_visitors SET current_count = GREATEST(0, current_count - "
                                + numVisitors
                                + ") WHERE park_id = "
                                + parkId);
            }

        } catch (Exception e) {
            // Silent failure
        }
    }

    /**
     * Closes all active park visits at 18:00.
     * Any visitor still inside the park is automatically
     * marked as exited and all active visitor counters
     * are reset.
     */
    private void closeParkAt18() {

        try {

            java.time.LocalTime now =
                    java.time.LocalTime.now();

            if (now.getHour() == 18 &&
                now.getMinute() < 2) {

                DBController.getInstance().executeUpdate(
                        "UPDATE park_visits SET exit_time = NOW() WHERE exit_time IS NULL");

                DBController.getInstance().executeUpdate(
                        "UPDATE active_visitors SET current_count = 0");
            }

        } catch (Exception e) {
            // Silent failure
        }
    }
}