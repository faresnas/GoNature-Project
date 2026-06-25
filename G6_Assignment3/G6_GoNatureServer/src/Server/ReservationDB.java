package Server;

import data.Reservation;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Random;

/**
 * מחלקת רכיב הגישה לנתונים (DAO) האחראית על ניהול הזמנות ורשימות המתנה בבסיס הנתונים.
 * המחלקה מטפלת ביצירת הזמנות, עדכונים, ביטולים, חישובי מחירים, ניהול תורים (Waiting List)
 * ומנגנוני תזכורות אוטומטיים עבור מערכת ניהול הפארקים.
 * * @author Fares Ali Naser
 * @version 1.0
 */
public class ReservationDB {

    /** בקר בסיס הנתונים המשמש לקבלת חיבורים פעילים */
    private DBController dbController;
    
    /** מחולל מספרים אקראיים ליצירת קודי אישור */
    private static final Random RANDOM = new Random();

    /**
     * בנאי המאתחל את רכיב הגישה לנתוני ההזמנות עם בקר בסיס הנתונים שסופק.
     *
     * @param dbController בקר בסיס הנתונים לניהול החיבורים
     */
    public ReservationDB(DBController dbController) {
        this.dbController = dbController;
    }
    
    /**
     * ממשק (Interface) המשמש כפונקציית חזרה (Callback) לצורך דחיפת התראות
     * בזמן אמת למשתמשים מחוברים (למשל, הודעות על רשימת המתנה).
     */
    public interface NotificationCallback {
        /**
         * שולח התראה למשתמש ספציפי.
         *
         * @param username שם המשתמש הייחודי של היעד
         * @param message אובייקט הודעת התוכן של ההתראה
         */
        void notifyUser(String username, Object message);
    }

    /** מאזין ה-Callback הרשום לשליחת התראות בזמן אמת */
    private NotificationCallback notificationCallback;

    /**
     * מגדיר את ה-Callback לניהול התראות המשתמש במערכת.
     *
     * @param callback מימוש ממשק ההתראות
     */
    public void setNotificationCallback(NotificationCallback callback) {
        this.notificationCallback = callback;
    }

    /**
     * מייצר קוד אישור אקראי בעל 8 ספרות עבור הזמנה חדשה.
     *
     * @return מחרוזת המייצגת קוד אישור בן 8 ספרות
     */
    private String generateConfirmationCode() {
        int code = 10000000 + RANDOM.nextInt(90000000);
        return String.valueOf(code);
    }

    /**
     * בודקת האם לפארק מסוים יש מספיק קיבולת פנויה עבור חלון זמן מבוקש של ביקור.
     * המתודה משקללת הן הזמנות במצב PENDING והן במצב CONFIRMED, ומאפשרת החרגה של מזהה הזמנה 
     * מסוים (משמש בעת עריכת/עדכון הזמנה קיימת כדי לא לספור את עצמה פעמיים).
     *
     * @param conn חיבור פעיל לבסיס הנתונים
     * @param parkId מזהה הפארק הייחודי
     * @param visitDate תאריך הביקור המבוקש
     * @param startTime שעת הכניסה המבוקשת
     * @param numVisitors מספר המבקרים בקבוצה
     * @param excludeReservationId מזהה הזמנה להחרגה מהחישוב (0 אם אין החרגה)
     * @return true אם יש מספיק מקום פנוי בפארק, false אחרת
     * @throws SQLException אם תתרחש שגיאה בביצוע שאילתות ה-SQL
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
     * מחשבת את המחיר הכולל עבור הזמנה בהתבסס על סוג ההזמנה (משפחתית/קבוצתית),
     * סוג המטייל (מנוי/מדריך), והאם בוצע תשלום מראש (Prepaid).
     *
     * @param conn חיבור פעיל לבסיס הנתונים
     * @param r אובייקט ההזמנה המכיל את נתוני המבקר והפארק
     * @return המחיר הסופי המחושב לאחר הנחות
     * @throws SQLException אם תתרחש שגיאה בביצוע שאילתות ה-SQL
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
     * יוצרת הזמנה חדשה במערכת עם סטטוס ראשוני 'PENDING'.
     * המתודה מסונכרנת (synchronized) ונועלת את הטרנזקציה כדי למנוע רישום יתר (Overbooking).
     *
     * @param r אובייקט ההזמנה החדש שיש לשמור במערכת
     * @return מחרוזת תשובה במבנה "SUCCESS:CODE:PRICE" במקרה של הצלחה, "FULL:..." אם אין מקום, או "ERROR:..."
     * @throws SQLException אם תתרחש שגיאת מסד נתונים במהלך הפעולה
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
     * שליפת כל ההזמנות המשוייכות למטייל ספציפי. 
     * הנתונים מוחזרים כמבנה של רשימת שורות (רשימות של מחרוזות) המתאים ישירות להצגה ב-TableView של ה-UI.
     *
     * @param travelerId מזהה המטייל
     * @param travelerType סוג המטייל (למשל: VISITOR, SUBSCRIBER, GUIDE)
     * @return רשימה דו-ממדית המכילה את שורות נתוני ההזמנות של המטייל
     * @throws SQLException אם תתרחש שגיאה במהלך שליפת הנתונים מה-DB
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
     * מעדכנת את פרטי התאריך, שעת הכניסה ומספר המבקרים עבור הזמנה קיימת.
     * מבצעת בדיקת זמינות מקום מחודשת לפני שמירת השינויים ומפיקה קוד אישור חדש.
     *
     * @param reservationId מזהה ההזמנה לעדכון
     * @param visitDate תאריך הביקור החדש (בפורמט מחרוזת YYYY-MM-DD)
     * @param entryTime שעת הכניסה החדשה (בפורמט מחרוזת HH:MM:SS)
     * @param numVisitors מספר המבקרים המעודכן
     * @return true אם ההזמנה עודכנה בהצלחה, false אם אין מקום פנוי או שההזמנה כבר מבוטלת
     * @throws SQLException אם תתרחש שגיאה בעדכון מסד הנתונים
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
     * מבטלת הזמנה קיימת במערכת על ידי שינוי הסטטוס שלה ל-'CANCELLED'.
     * כבדיקת בטיחות, הביטול יתבצע רק אם ההזמנה אכן שייכת למטייל המבקש. 
     * לאחר הביטול, המערכת מנסה אוטומטית להציע את המקום שהתפנה לאדם הבא בתור ברשימת ההמתנה.
     *
     * @param reservationId מזהה ההזמנה לביטול
     * @param travelerId מזהה המטייל המבקש את הביטול
     * @param travelerType סוג המטייל המבקש את הביטול
     * @return true אם הביטול בוצע בהצלחה, false אחרת
     * @throws SQLException אם תתרחש שגיאה בעדכון בסיס הנתונים
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

    /**
     * שולפת את רשימת כל הפארקים הקיימים במערכת (מזהה ושם).
     * מתודה זו שימושית בעיקר לצורך אכלוס תיבות בחירה (ComboBox) ב-UI.
     *
     * @return רשימה דו-ממדית של כל הפארקים הכוללת [מזהה, שם] לכל פארק
     * @throws SQLException אם תתרחש שגיאה בשליפת הפארקים
     */
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

    /**
     * שולפת רשימת מזהי הזמנות הנמצאות בסטטוס PENDING, שטרם נשלחה אליהן תזכורת,
     * ומועד הביקור שלהן חל מחר (הפרש של יום אחד מהתאריך הנוכחי).
     *
     * @return רשימת מזהי ההזמנות שזקוקות לשליחת תזכורת
     * @throws SQLException אם תתרחש שגיאה בשאילתת ה-SQL
     */
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

    /**
     * מסמנת הזמנה ספציפית ככזו שנשלחה אליה תזכורת, ומעדכנת את חותמת הזמן של השליחה.
     *
     * @param reservationId מזהה ההזמנה שקיבלה תזכורת
     * @throws SQLException אם העדכון בבסיס הנתונים נכשל
     */
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

    /**
     * מנגנון ביטול אוטומטי: מבטל הזמנות במצב PENDING שנשלחה אליהן תזכורת
     * אך המשתמש לא אישר אותן בתוך חלון הזמן המוקצב של שעתיים.
     *
     * @throws SQLException אם חל כשל בעדכון הסטטוסים בבסיס הנתונים
     */
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

    /**
     * מנגנון ניקוי אוטומטי: מבטל את כל ההזמנות שנשארו במצב PENDING
     * ותאריך הביקור המתוכנן שלהן כבר עבר (נמוך מהתאריך הנוכחי).
     *
     * @throws SQLException אם העדכון האוטומטי נכשל בבסיס הנתונים
     */
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

    /**
     * סימולציית עיבוד רשימת המתנה. שולפת את כל הרשומות לפי סדר המיקום שלהן בתור
     * ומדפיסה הודעת סימולציה על שליחת מייל ללוג המערכת.
     *
     * @throws SQLException אם השליפה מתוך טבלת רשימת ההמתנה נכשלה
     */
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

    /**
     * מוסיפה הזמנה לתור רשימת ההמתנה עבור פלח זמן ספציפי בפארק.
     * המתודה מוודא תחילה שגודל הקבוצה אינו עולה באופן תיאורטי על הקיבולת המרבית המותרת של הפארק.
     *
     * @param r אובייקט ההזמנה שמבקש להיכנס לרשימת ההמתנה
     * @return true אם ההוספה לרשימת ההמתנה הצליחה, false אם גודל הקבוצה חורג ממכסת הפארק הכוללת
     * @throws SQLException אם חלה שגיאה במהלך ההוספה לטבלת רשימת ההמתנה
     */
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
    
    /**
     * מחשבת את מספר המקומות התפוסים והפנויים עבור פארק, תאריך ושעה מוגדרים.
     * הלוגיקה לוקחת בחשבון את משך השהות הממוצע בפארק ובודקת חפיפות זמנים (Overlap)
     * מול הזמנות במצבים: 'PENDING', 'CONFIRMED', ו-'INSIDE'.
     *
     * @param parkId מזהה הפארק
     * @param visitDate תאריך הביקור המבוקש
     * @param entryTime שעת הכניסה המבוקשת
     * @return מערך דינמי המכיל שני איברים: [0] כמות המקומות המוזמנים, [1] כמות המקומות הפנויים שנותרו
     * @throws SQLException אם השאילתה מול בסיס הנתונים נכשלת
     */
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

    /**
     * מאשרת תזכורת ביקור על ידי המשתמש. מעדכנת את סטטוס ההזמנה מ-'PENDING' ל-'CONFIRMED'
     * ומסמנת כי בוצע אישור תזכורת (`reminder_confirmed = TRUE`).
     *
     * @param reservationId מזהה ההזמנה לאישור
     * @param travelerId מזהה המטייל המאשר
     * @param travelerType סוג המטייל המאשר
     * @return true אם הסטטוס עודכן בהצלחה, false אחרת
     * @throws SQLException אם חלה שגיאה בביצוע ה-Update
     */
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

    /**
     * סורקת את ההזמנות ושולחת תזכורות ביקור (בסימולציה) עבור כל ההזמנות במצב PENDING
     * שמועד ביקורן חל בטווח של 23 עד 25 שעות מעכשיו (חלון זמן של 24 שעות).
     * לאחר מכן מעדכנת את שורות אלו בבסיס הנתונים כנשלחו.
     *
     * @throws SQLException אם חל כשל במהלך סריקת או עדכון ההזמנות
     */
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

    /**
     * מבטלת אוטומטית הזמנות מסוג PENDING שתזכורת נשלחה אליהן אך חלפו למעלה משעתיים
     * ללא אישור מצד המשתמש. עבור כל הזמנה שמבוטלת, מופעל מנגנון עדכון רשימת ההמתנה.
     *
     * @throws SQLException אם חלה שגיאה בבסיס הנתונים במהלך הביטולים או עדכון רשימת ההמתנה
     */
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

    /**
     * מאתרת ומעדכנת את האדם הבא בתור ברשימת ההמתנה (הסטטוס משתנה ל-'NOTIFIED')
     * שגודל הקבוצה שלו מתאים למספר המקומות שהתפנו כרגע בחלון הזמן המוגדר.
     * במידה והמשתמש מחובר כעת למערכת, נדחפת אליו התראה בזמן אמת (Push Notification) באמצעות ה-Callback.
     *
     * @param parkId מזהה הפארק בו התפנה מקום
     * @param visitDate תאריך חלון הזמן הרלוונטי
     * @param entryTime שעת חלון הזמן הרלוונטי
     * @throws SQLException אם השליפה או עדכוני הנתונים נכשלים
     */
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
    
    /**
     * פונקציית עזר פנימית המאתרת את שם המשתמש (Username / ID Number) של מטייל
     * בהתאם לסוג הישות שלו בבסיס הנתונים (מבקר רגיל, מנוי או מדריך).
     *
     * @param travelerId מזהה המטייל במסד הנתונים
     * @param travelerType סוג המטייל (VISITOR, SUBSCRIBER, GUIDE)
     * @return שם המשתמש/תעודת הזהות של המטייל, או null אם אינו נמצא או שחל כשל
     */
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

    /**
     * מעדכנת לסטטוס 'EXPIRED' את כל הצעות רשימת ההמתנה שזמן הפקיעה שלהן עבר,
     * וכן הצעות שתאריך ושעת הביקור שלהן כבר חלפו לחלוטין.
     *
     * @throws SQLException אם חל כשל בביצוע העדכון במסד הנתונים
     */
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
    
    /**
     * שולפת רשימת תזכורות הממתינות לאישור המשתמש הנוכחי (סטטוס PENDING, תזכורת נשלחה,
     * וטרם עברו שעתיים מחלון הזמן לאישור).
     * מיועד להצגת התראות אקטיביות בממשק הגרפי (UI).
     *
     * @param travelerId מזהה המטייל המחובר
     * @param travelerType סוג המטייל המחובר
     * @return רשימה דו-ממדית של שורות התזכורות הממתינות לאישור
     * @throws SQLException אם השילוף נכשל
     */
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
            row.add(String.valueOf(rs.getString(6)));
            result.add(row);
        }
        rs.close();
        ps.close();
        return result;
    }

    /**
     * מעדכנת את הסטטוס ל-'reminder_sent = TRUE' באופן יזום עבור מטייל ספציפי,
     * במידה וההזמנה שלו נמצאת בטווח של 24 שעות ממועד הביקור.
     *
     * @param travelerId מזהה המטייל
     * @param travelerType סוג המטייל
     * @throws SQLException אם חלה שגיאה בביצוע העדכון בבסיס הנתונים
     */
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
    
    /**
     * שולפת את כל הודעות רשימת ההמתנה הפעילות שבסטטוס 'NOTIFIED' עבור משתמש מסוים,
     * אשר זמן פקיעת ההצעה שלהן טרם עבר. משמש להצגת חלונות בחירה/אישור ב-UI של הלקוח.
     *
     * @param travelerId מזהה המטייל
     * @param travelerType סוג המטייל
     * @return רשימה דו-ממדית של התראות תור הממתינות לתגובת המשתמש
     * @throws SQLException אם שליפת הנתונים נכשלה
     */
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
    
    /**
     * מאשרת מעבר מרשימת ההמתנה להזמנה קבועה ומאושרת. 
     * המתודה בודקת שוב זמינות מקום בפועל; אם יש מקום, נוצרת הזמנה רגילה חדשה בסטטוס 'CONFIRMED'
     * ורשומת רשימת ההמתנה מסומנת כ-'CONFIRMED'. אם אין מקום פנוי, ההצעה פוקעת ומבוטלת.
     *
     * @param waitingListId מזהה רשומת רשימת ההמתנה
     * @param travelerId מזהה המטייל שמממש את ההצעה
     * @param travelerType סוג המטייל שמממש את ההצעה
     * @return true אם המעבר והפיכתה להזמנה אושרו והצליחו, false אם המקום נתפס או חלה שגיאה
     * @throws SQLException אם חלה שגיאת מסד נתונים במהלך הפעולות המשולבות
     */
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

    /**
     * דוחה ומבטלת הצעת מקום שהתקבלה מרשימת ההמתנה. 
     * הרשומה מסומנת כ-'EXPIRED', והמערכת קוראת מיידית לפונקציה שמציעה את המקום הפנוי לאדם הבא בתור.
     *
     * @param waitingListId מזהה רשומת רשימת ההמתנה שהמשתמש סירב לה
     * @return true תמיד, לאחר סימון הרשומה וקריאה להבאת הבא בתור
     * @throws SQLException אם עדכון הסטטוס נכשל
     */
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

    /**
     * שולפת את כל רישומי התור (רשימת ההמתנה) של מטייל מסוים שנמצאים במצב רלוונטי לתצוגה 
     * ('WAITING', 'NOTIFIED', 'CANCELLED'). הנתונים מוחזרים במבנה המתאים ישירות ל-TableView.
     *
     * @param travelerId מזהה המטייל המבקש
     * @param travelerType סוג המטייל המבקש
     * @return רשימה דו-ממדית המייצגת את שורות רשימת ההמתנה של המשתמש
     * @throws SQLException אם שליפת השורות נכשלה
     */
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

    /**
     * מאפשרת למשתמש לעזוב באופן יזום את רשימת ההמתנה אליה נרשם.
     * הסטטוס משתנה ל-'CANCELLED' (על מנת לשמור היסטוריה ולא למחוק לחלוטין),
     * והמערכת מפעילה בדיקה מחודשת כדי להציע את המקום שהתפנה לאדם הבא שממתין בתור.
     *
     * @param waitingListId מזהה הרשומה ברשימת ההמתנה שיש לבטל
     * @return true אם הביטול והעזיבה עודכנו בהצלחה, false אחרת
     * @throws SQLException אם חלה שגיאה בביצוע העדכונים ב-DB
     */
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