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