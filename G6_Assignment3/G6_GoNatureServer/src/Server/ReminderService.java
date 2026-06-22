package Server;

public class ReminderService extends Thread {

    private ReservationDB reservationDB;

    public ReminderService(ReservationDB reservationDB) {
        this.reservationDB = reservationDB;
    }

    @Override
    public void run() {
        while (true) {
            try {
                reservationDB.sendVisitReminders();
                reservationDB.autoCancelUnconfirmedReservations();
                reservationDB.expireWaitingListOffers();

                Thread.sleep(60000);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}