package common.entities;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Represents a park visit booking in the GoNature system.
 *
 * <p>A booking covers both pre-arranged and walk-in visits. The {@link VisitType}
 * enum drives the pricing model defined in the system specification:</p>
 *
 * <table border="1">
 *   <caption>Pricing Model</caption>
 *   <tr><th>Type</th><th>Discount</th></tr>
 *   <tr><td>PRE_BOOKED_INDIVIDUAL</td><td>15% off full price</td></tr>
 *   <tr><td>WALK_IN_INDIVIDUAL</td><td>Full price</td></tr>
 *   <tr><td>PRE_BOOKED_GROUP</td><td>25% off + optional 12% prepay; guide free</td></tr>
 *   <tr><td>WALK_IN_GROUP</td><td>10% off; guide pays</td></tr>
 * </table>
 *
 * <p>Subscribers receive an additional 10% on top of the above.</p>
 *
 * @author Group 6
 * @version 1.0
 */
public class Booking implements Serializable {

    private static final long serialVersionUID = 1L;

    // ── Status constants ─────────────────────────────────────────────────────

    /**
     * All valid states a booking can be in during its lifecycle.
     */
    public enum BookingStatus {
        /** Booking confirmed and awaiting the visit date. */
        CONFIRMED,
        /** Visitor has entered the park. */
        ACTIVE,
        /** Visit completed normally. */
        COMPLETED,
        /** Booking was cancelled by the visitor. */
        CANCELLED,
        /** Booking was auto-cancelled (visitor did not confirm reminder). */
        AUTO_CANCELLED,
        /** Booking is on the waiting list (park was full). */
        WAITLISTED
    }

    /**
     * The type of visit — drives pricing and group rules.
     */
    public enum VisitType {
        PRE_BOOKED_INDIVIDUAL,
        WALK_IN_INDIVIDUAL,
        PRE_BOOKED_GROUP,
        WALK_IN_GROUP
    }

    // ── Fields ───────────────────────────────────────────────────────────────

    /** System-assigned booking ID. */
    private int bookingId;

    /** ID of the park where the visit is booked. */
    private int parkId;

    /** ID number of the visitor who made the booking. */
    private String visitorId;

    /** Total number of visitors included in this booking. */
    private int numberOfVisitors;

    /** Planned visit date and time. */
    private LocalDateTime visitDateTime;

    /** Date and time when the booking was created. */
    private LocalDateTime bookingDateTime;

    /** Current booking lifecycle status. */
    private BookingStatus status;

    /** Visit type — determines pricing category. */
    private VisitType visitType;

    /** Email address to receive confirmation and reminders. */
    private String contactEmail;

    /** Phone number for SMS simulation. */
    private String contactPhone;

    /**
     * Whether this is a group booking led by a registered guide.
     * Groups are limited to 15 participants.
     */
    private boolean isGroupBooking;

    /** Whether the group has prepaid (triggers extra 12% group discount). */
    private boolean isPrepaid;

    /** Numeric confirmation code sent to the visitor (simulates QR). */
    private String confirmationCode;

    /** Whether the visitor confirmed the day-before reminder. */
    private boolean reminderConfirmed;

    /** Computed bill amount (set at park entry). */
    private double billAmount;

    // ── Constructor ──────────────────────────────────────────────────────────

    /**
     * Creates a new booking.
     *
     * @param parkId           target park ID
     * @param visitorId        visitor national ID
     * @param numberOfVisitors number of visitors in the party
     * @param visitDateTime    planned visit date and time
     * @param visitType        pricing category
     * @param contactEmail     email for notifications
     * @param contactPhone     phone for SMS simulation
     * @param isGroupBooking   {@code true} if this is a guided group booking
     */
    public Booking(int parkId, String visitorId, int numberOfVisitors,
                   LocalDateTime visitDateTime, VisitType visitType,
                   String contactEmail, String contactPhone, boolean isGroupBooking) {
        this.parkId           = parkId;
        this.visitorId        = visitorId;
        this.numberOfVisitors = numberOfVisitors;
        this.visitDateTime    = visitDateTime;
        this.bookingDateTime  = LocalDateTime.now();
        this.visitType        = visitType;
        this.contactEmail     = contactEmail;
        this.contactPhone     = contactPhone;
        this.isGroupBooking   = isGroupBooking;
        this.status           = BookingStatus.CONFIRMED;
        this.reminderConfirmed = false;
        this.isPrepaid        = false;
        this.billAmount       = 0.0;
    }

    // ── Getters & Setters ────────────────────────────────────────────────────

    /** @return system booking ID */
    public int getBookingId() { return bookingId; }

    /** @param bookingId ID to set (set by DB after insert) */
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }

    /** @return park ID */
    public int getParkId() { return parkId; }

    /** @return visitor ID */
    public String getVisitorId() { return visitorId; }

    /** @return number of visitors */
    public int getNumberOfVisitors() { return numberOfVisitors; }

    /** @param n number of visitors to set */
    public void setNumberOfVisitors(int n) { this.numberOfVisitors = n; }

    /** @return planned visit date and time */
    public LocalDateTime getVisitDateTime() { return visitDateTime; }

    /** @param dt visit date/time to set */
    public void setVisitDateTime(LocalDateTime dt) { this.visitDateTime = dt; }

    /** @return when the booking was created */
    public LocalDateTime getBookingDateTime() { return bookingDateTime; }

    /** @return current booking status */
    public BookingStatus getStatus() { return status; }

    /** @param status status to set */
    public void setStatus(BookingStatus status) { this.status = status; }

    /** @return visit type (pricing category) */
    public VisitType getVisitType() { return visitType; }

    /** @return contact email */
    public String getContactEmail() { return contactEmail; }

    /** @return contact phone */
    public String getContactPhone() { return contactPhone; }

    /** @return {@code true} if this is a group booking */
    public boolean isGroupBooking() { return isGroupBooking; }

    /** @return {@code true} if the group has prepaid */
    public boolean isPrepaid() { return isPrepaid; }

    /** @param prepaid prepaid flag to set */
    public void setPrepaid(boolean prepaid) { this.isPrepaid = prepaid; }

    /** @return numeric confirmation code (simulates QR code) */
    public String getConfirmationCode() { return confirmationCode; }

    /** @param code confirmation code to set */
    public void setConfirmationCode(String code) { this.confirmationCode = code; }

    /** @return {@code true} if visitor confirmed the reminder */
    public boolean isReminderConfirmed() { return reminderConfirmed; }

    /** @param confirmed reminder confirmation flag to set */
    public void setReminderConfirmed(boolean confirmed) { this.reminderConfirmed = confirmed; }

    /** @return computed bill amount */
    public double getBillAmount() { return billAmount; }

    /** @param amount bill amount to set */
    public void setBillAmount(double amount) { this.billAmount = amount; }

    /**
     * Returns a short description of this booking for display.
     *
     * @return "Booking #ID — Park parkId, visitorId, N visitors, datetime"
     */
    @Override
    public String toString() {
        return "Booking #" + bookingId + " — Park " + parkId
               + ", visitor " + visitorId
               + ", " + numberOfVisitors + " visitors"
               + ", " + visitDateTime;
    }
}