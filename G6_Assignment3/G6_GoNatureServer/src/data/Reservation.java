package data;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Time;

/**
 * Represents a reservation made by a visitor, subscriber, or guide.
 * This class stores all information related to a reservation,
 * including traveler details, park information, visit date and time,
 * reservation status, and payment information.
 */
@SuppressWarnings("serial")
public class Reservation implements Serializable {

    /** The unique reservation identifier. */
    private int id;

    /** The identifier of the traveler who created the reservation. */
    private int travelerId;

    /** The traveler type (Visitor, Subscriber, or Guide). */
    private String travelerType;

    /** The identifier of the reserved park. */
    private int parkId;

    /** The reservation visit date. */
    private Date visitDate;

    /** The planned park entry time. */
    private Time entryTime;

    /** The number of visitors included in the reservation. */
    private int numVisitors;

    /** The contact email associated with the reservation. */
    private String email;

    /** The reservation type. */
    private String type;

    /** The current reservation status. */
    private String status;

    /** The reservation confirmation code. */
    private String confirmationCode;

    /** Indicates whether the reservation has already been prepaid. */
    private boolean prepaid;

    /**
     * Creates an empty reservation object.
     */
    public Reservation() {}

    /**
     * Returns the reservation ID.
     *
     * @return the reservation ID
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the reservation ID.
     *
     * @param id the reservation ID
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Returns the traveler ID.
     *
     * @return the traveler ID
     */
    public int getTravelerId() {
        return travelerId;
    }

    /**
     * Sets the traveler ID.
     *
     * @param travelerId the traveler ID
     */
    public void setTravelerId(int travelerId) {
        this.travelerId = travelerId;
    }

    /**
     * Returns the traveler type.
     *
     * @return the traveler type
     */
    public String getTravelerType() {
        return travelerType;
    }

    /**
     * Sets the traveler type.
     *
     * @param travelerType the traveler type
     */
    public void setTravelerType(String travelerType) {
        this.travelerType = travelerType;
    }

    /**
     * Returns the park ID.
     *
     * @return the park ID
     */
    public int getParkId() {
        return parkId;
    }

    /**
     * Sets the park ID.
     *
     * @param parkId the park ID
     */
    public void setParkId(int parkId) {
        this.parkId = parkId;
    }

    /**
     * Returns the visit date.
     *
     * @return the visit date
     */
    public Date getVisitDate() {
        return visitDate;
    }

    /**
     * Sets the visit date.
     *
     * @param visitDate the visit date
     */
    public void setVisitDate(Date visitDate) {
        this.visitDate = visitDate;
    }

    /**
     * Returns the planned entry time.
     *
     * @return the entry time
     */
    public Time getEntryTime() {
        return entryTime;
    }

    /**
     * Sets the planned entry time.
     *
     * @param entryTime the entry time
     */
    public void setEntryTime(Time entryTime) {
        this.entryTime = entryTime;
    }

    /**
     * Returns the number of visitors.
     *
     * @return the number of visitors
     */
    public int getNumVisitors() {
        return numVisitors;
    }

    /**
     * Sets the number of visitors.
     *
     * @param numVisitors the number of visitors
     */
    public void setNumVisitors(int numVisitors) {
        this.numVisitors = numVisitors;
    }

    /**
     * Returns the reservation email.
     *
     * @return the email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the reservation email.
     *
     * @param email the email address
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Returns the reservation type.
     *
     * @return the reservation type
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the reservation type.
     *
     * @param type the reservation type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Returns the reservation status.
     *
     * @return the reservation status
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the reservation status.
     *
     * @param status the reservation status
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Returns the confirmation code.
     *
     * @return the confirmation code
     */
    public String getConfirmationCode() {
        return confirmationCode;
    }

    /**
     * Sets the confirmation code.
     *
     * @param confirmationCode the confirmation code
     */
    public void setConfirmationCode(String confirmationCode) {
        this.confirmationCode = confirmationCode;
    }

    /**
     * Returns whether the reservation has been prepaid.
     *
     * @return true if prepaid, otherwise false
     */
    public boolean isPrepaid() {
        return prepaid;
    }

    /**
     * Sets whether the reservation has been prepaid.
     *
     * @param prepaid true if prepaid, otherwise false
     */
    public void setPrepaid(boolean prepaid) {
        this.prepaid = prepaid;
    }

    /**
     * Returns a string representation of the reservation.
     *
     * @return a formatted reservation summary
     */
    @Override
    public String toString() {
        return "Reservation #" + id +
               " | Park: " + parkId +
               " | Date: " + visitDate +
               " | Visitors: " + numVisitors;
    }
}