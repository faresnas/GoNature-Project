package common.entities;

import java.io.Serializable;

/**
 * Represents a nature park managed by the GoNature system.
 *
 * <p>Each park has its own capacity quota and a gap value that reserves
 * spots for walk-in visitors. The park manager controls these values,
 * subject to approval from the Department Manager.</p>
 *
 * <p>Parameters (all per-park):</p>
 * <ul>
 *   <li>{@code maxCapacity}  — absolute maximum simultaneous visitors.</li>
 *   <li>{@code orderCapacity} — how many bookings the system accepts
 *       ({@code maxCapacity - gap}).</li>
 *   <li>{@code defaultVisitTime} — estimated stay in hours (default: 4).</li>
 * </ul>
 *
 * @author Group 6
 * @version 1.0
 */
public class Park implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Unique park ID (matches DB primary key). */
    private int parkId;

    /** Display name of the park. */
    private String parkName;

    /** Geographic location description. */
    private String location;

    /**
     * Maximum number of visitors allowed in the park at any given time.
     * Controlled by the Park Manager; requires Department Manager approval to change.
     */
    private int maxCapacity;

    /**
     * Number of bookings the system will accept at any time slot.
     * Equals {@code maxCapacity - gap}.  The gap reserves spots for walk-ins.
     */
    private int orderCapacity;

    /**
     * Estimated visit duration in hours used when calculating occupancy.
     * Default: 4 hours.  Controlled by Park Manager with dept. approval.
     */
    private int defaultVisitTimeHours;

    /** Current number of visitors physically inside the park. */
    private int currentVisitors;

    /** Full-price entry fee (set by the Tourism Ministry). */
    private double fullPrice;

    /**
     * Constructs a Park with all parameters.
     *
     * @param parkId               unique park ID
     * @param parkName             display name
     * @param location             geographic location
     * @param maxCapacity          absolute maximum simultaneous visitors
     * @param orderCapacity        bookable capacity (maxCapacity minus gap)
     * @param defaultVisitTimeHours estimated stay time in hours
     * @param fullPrice            full entry price per person
     */
    public Park(int parkId, String parkName, String location,
                int maxCapacity, int orderCapacity,
                int defaultVisitTimeHours, double fullPrice) {
        this.parkId                = parkId;
        this.parkName              = parkName;
        this.location              = location;
        this.maxCapacity           = maxCapacity;
        this.orderCapacity         = orderCapacity;
        this.defaultVisitTimeHours = defaultVisitTimeHours;
        this.currentVisitors       = 0;
        this.fullPrice             = fullPrice;
    }

    // ── Getters & Setters ────────────────────────────────────────────────────

    /** @return unique park ID */
    public int getParkId()    { return parkId; }

    /** @return park display name */
    public String getParkName() { return parkName; }

    /** @param parkName name to set */
    public void setParkName(String parkName) { this.parkName = parkName; }

    /** @return geographic location */
    public String getLocation() { return location; }

    /** @return maximum simultaneous visitor capacity */
    public int getMaxCapacity()  { return maxCapacity; }

    /** @param maxCapacity capacity to set (requires dept. approval in UI) */
    public void setMaxCapacity(int maxCapacity) { this.maxCapacity = maxCapacity; }

    /** @return bookable order capacity */
    public int getOrderCapacity() { return orderCapacity; }

    /** @param orderCapacity order capacity to set */
    public void setOrderCapacity(int orderCapacity) { this.orderCapacity = orderCapacity; }

    /** @return default visit duration in hours */
    public int getDefaultVisitTimeHours() { return defaultVisitTimeHours; }

    /** @param hours estimated visit duration hours to set */
    public void setDefaultVisitTimeHours(int hours) { this.defaultVisitTimeHours = hours; }

    /** @return current number of visitors inside the park */
    public int getCurrentVisitors() { return currentVisitors; }

    /** @param currentVisitors number of visitors currently inside */
    public void setCurrentVisitors(int currentVisitors) { this.currentVisitors = currentVisitors; }

    /** @return full entry price per visitor */
    public double getFullPrice() { return fullPrice; }

    /** @param fullPrice full price to set */
    public void setFullPrice(double fullPrice) { this.fullPrice = fullPrice; }

    /**
     * Checks whether the park has room for additional pre-booked visitors.
     *
     * @param requestedVisitors number of visitors in the booking request
     * @return {@code true} if the park's order capacity is not exceeded
     */
    public boolean hasAvailableOrderCapacity(int requestedVisitors) {
        return (currentVisitors + requestedVisitors) <= orderCapacity;
    }

    /**
     * Checks whether the park can admit walk-in visitors right now.
     *
     * @param requestedVisitors number of walk-in visitors
     * @return {@code true} if the park's absolute capacity is not exceeded
     */
    public boolean hasAvailableWalkInCapacity(int requestedVisitors) {
        return (currentVisitors + requestedVisitors) <= maxCapacity;
    }

    /** @return "ParkName (location)" */
    @Override
    public String toString() {
        return parkName + " (" + location + ")";
    }
}