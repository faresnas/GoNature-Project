package Server;

/**
 * Background service responsible for periodic reservation maintenance.
 * <p>
 * The service runs continuously in its own thread and performs
 * scheduled tasks such as sending visit reminders, automatically
 * cancelling unconfirmed reservations, and expiring waiting list offers.
 */
public class ReminderService extends Thread {

    /**
     * Handles reservation-related database operations.
     */
    private ReservationDB reservationDB;

    /**
     * Creates a new reminder service.
     *
     * @param reservationDB the reservation database manager
     */
    public ReminderService(ReservationDB reservationDB) {
        this.reservationDB = reservationDB;
    }

    /**
     * Starts the reminder service.
     * The service executes scheduled maintenance tasks every minute.
     */
    @Override
    public void run() {

        while (true) {

            try {

                Thread.sleep(60000);

                reservationDB.sendVisitReminders();
                reservationDB.autoCancelUnconfirmedReservations();
                reservationDB.expireWaitingListOffers();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}