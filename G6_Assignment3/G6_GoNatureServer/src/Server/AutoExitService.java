package Server;

public class AutoExitService extends Thread {

    private ReservationDB reservationDB;
    private EntryExitDB entryExitDB;

    public AutoExitService(ReservationDB reservationDB, EntryExitDB entryExitDB) {
        this.reservationDB = reservationDB;
        this.entryExitDB = entryExitDB;
    }

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
                // silent
            }
        }
    }

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

            if (expired == null || expired.isEmpty()) return;

            for (java.util.ArrayList<String> row : expired) {
                int visitId     = Integer.parseInt(row.get(0));
                int parkId      = Integer.parseInt(row.get(1));
                int numVisitors = Integer.parseInt(row.get(2));

                DBController.getInstance().executeUpdate(
                    "UPDATE park_visits SET exit_time = NOW() WHERE id = " + visitId);

                DBController.getInstance().executeUpdate(
                    "UPDATE active_visitors SET current_count = GREATEST(0, current_count - " +
                    numVisitors + ") WHERE park_id = " + parkId);
            }
        } catch (Exception e) {
            // silent
        }
    }

    private void closeParkAt18() {
        try {
            java.time.LocalTime now = java.time.LocalTime.now();
            if (now.getHour() == 18 && now.getMinute() < 2) {
                DBController.getInstance().executeUpdate(
                    "UPDATE park_visits SET exit_time = NOW() WHERE exit_time IS NULL");
                DBController.getInstance().executeUpdate(
                    "UPDATE active_visitors SET current_count = 0");
            }
        } catch (Exception e) {
            // silent
        }
    }
}