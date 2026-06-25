package data;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Time;

/**
 * Represents a reservation in the GoNature system.
 * <p>
 * This class stores all the information related to a park reservation,
 * including the traveler details, park information, visit date and time,
 * number of visitors, reservation status, confirmation code, and payment status.
 * </p>
 *
 * @author Fares
 * @version 1.0
 */
@SuppressWarnings("serial")
public class Reservation implements Serializable {

    /**
     * The unique reservation identifier.
     */
    private int id;

    /**
     * The identifier of the traveler who created the reservation.
     */
    private int travelerId;

    /**
     * The type of traveler (Visitor, Subscriber, Guide, etc.).
     */
    private String travelerType;

    /**
     * The identifier of the selected park.
     */
    private int parkId;

    /**
     * The scheduled visit date.
     */
    private Date visitDate;

    /**
     * The scheduled entry time.
     */
    private Time entryTime;

    /**
     * The number of visitors included in the reservation.
     */
    private int numVisitors;

    /**
     * The traveler's email address.
     */
    private String email;

    /**
     * The reservation type.
     */
    private String type;

    /**
     * The current reservation status.
     */
    private String status;

    /**
     * The reservation confirmation code.
     */
    private String confirmationCode;

    /**
     * Indicates whether the reservation has been prepaid.
     */
    private boolean prepaid;

    /**
     * Creates an empty Reservation object.
     */
    public Reservation() {}

    /**
     * Returns the reservation ID.
     *
     * @return the reservation identifier.
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the reservation ID.
     *
     * @param id the reservation identifier.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Returns the traveler ID.
     *
     * @return the traveler identifier.
     */
    public int getTravelerId() {
        return travelerId;
    }

    /**
     * Sets the traveler ID.
     *
     * @param travelerId the traveler identifier.
     */
    public void setTravelerId(int travelerId) {
        this.travelerId = travelerId;
    }

    /**
     * Returns the traveler type.
     *
     * @return the traveler type.
     */
    public String getTravelerType() {
        return travelerType;
    }

    /**
     * Sets the traveler type.
     *
     * @param travelerType the traveler type.
     */
    public void setTravelerType(String travelerType) {
        this.travelerType = travelerType;
    }

    /**
     * Returns the park ID.
     *
     * @return the park identifier.
     */
    public int getParkId() {
        return parkId;
    }

    /**
     * Sets the park ID.
     *
     * @param parkId the park identifier.
     */
    public void setParkId(int parkId) {
        this.parkId = parkId;
    }

    /**
     * Returns the scheduled visit date.
     *
     * @return the visit date.
     */
    public Date getVisitDate() {
        return visitDate;
    }

    /**
     * Sets the scheduled visit date.
     *
     * @param visitDate the visit date.
     */
    public void setVisitDate(Date visitDate) {
        this.visitDate = visitDate;
    }

    /**
     * Returns the scheduled entry time.
     *
     * @return the entry time.
     */
    public Time getEntryTime() {
        return entryTime;
    }

    /**
     * Sets the scheduled entry time.
     *
     * @param entryTime the entry time.
     */
    public void setEntryTime(Time entryTime) {
        this.entryTime = entryTime;
    }

    /**
     * Returns the number of visitors.
     *
     * @return the number of visitors.
     */
    public int getNumVisitors() {
        return numVisitors;
    }

    /**
     * Sets the number of visitors.
     *
     * @param numVisitors the number of visitors.
     */
    public void setNumVisitors(int numVisitors) {
        this.numVisitors = numVisitors;
    }

    /**
     * Returns the traveler's email address.
     *
     * @return the email address.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the traveler's email address.
     *
     * @param email the email address.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Returns the reservation type.
     *
     * @return the reservation type.
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the reservation type.
     *
     * @param type the reservation type.
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Returns the reservation status.
     *
     * @return the reservation status.
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the reservation status.
     *
     * @param status the reservation status.
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Returns the reservation confirmation code.
     *
     * @return the confirmation code.
     */
    public String getConfirmationCode() {
        return confirmationCode;
    }

    /**
     * Sets the reservation confirmation code.
     *
     * @param confirmationCode the confirmation code.
     */
    public void setConfirmationCode(String confirmationCode) {
        this.confirmationCode = confirmationCode;
    }

    /**
     * Indicates whether the reservation has already been prepaid.
     *
     * @return true if prepaid, otherwise false.
     */
    public boolean isPrepaid() {
        return prepaid;
    }

    /**
     * Sets the payment status of the reservation.
     *
     * @param prepaid true if the reservation has been prepaid.
     */
    public void setPrepaid(boolean prepaid) {
        this.prepaid = prepaid;
    }

    /**
     * Returns a formatted string representation of the reservation.
     *
     * @return a string containing the reservation details.
     */
    @Override
    public String toString() {
        return "Reservation #" + id +
               " | Park: " + parkId +
               " | Date: " + visitDate +
               " | Visitors: " + numVisitors;
    }
}