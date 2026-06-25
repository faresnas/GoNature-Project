package Common;

import java.io.Serializable;

/**
 * Represents the response returned by the server
 * after processing a park entry or exit request.
 * The response contains the operation status,
 * a descriptive message, payment information,
 * visitor statistics, and available parking capacity.
 */
public class EntryExitResponse implements Serializable {

    /**
     * Serialization version identifier.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Indicates whether the requested operation succeeded.
     */
    private boolean success;

    /**
     * A descriptive message explaining the operation result.
     */
    private String message;

    /**
     * The amount the visitor is required to pay.
     */
    private double amountToPay;

    /**
     * The current number of visitors inside the park.
     */
    private int currentVisitors;

    /**
     * The unique visit identifier.
     */
    private int visitId;

    /**
     * The number of available spots remaining in the park.
     */
    private int availableSpots;

    /**
     * Creates a response containing only the operation status
     * and a descriptive message.
     *
     * @param success true if the operation succeeded
     * @param message the operation result message
     */
    public EntryExitResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    /**
     * Creates a response containing payment and visitor information.
     *
     * @param success the operation result
     * @param message the operation message
     * @param amountToPay the payment amount
     * @param currentVisitors the current number of visitors
     * @param visitId the visit identifier
     */
    public EntryExitResponse(boolean success, String message,
                             double amountToPay,
                             int currentVisitors,
                             int visitId) {
        this.success = success;
        this.message = message;
        this.amountToPay = amountToPay;
        this.currentVisitors = currentVisitors;
        this.visitId = visitId;
    }

    /**
     * Creates a response containing payment,
     * visitor information, and available spots.
     *
     * @param success the operation result
     * @param message the operation message
     * @param amountToPay the payment amount
     * @param currentVisitors the current number of visitors
     * @param visitId the visit identifier
     * @param availableSpots the number of available spots
     */
    public EntryExitResponse(boolean success, String message,
                             double amountToPay,
                             int currentVisitors,
                             int visitId,
                             int availableSpots) {
        this.success = success;
        this.message = message;
        this.amountToPay = amountToPay;
        this.currentVisitors = currentVisitors;
        this.visitId = visitId;
        this.availableSpots = availableSpots;
    }

    /**
     * Returns whether the operation succeeded.
     *
     * @return true if successful, otherwise false
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Returns the operation message.
     *
     * @return the response message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Returns the payment amount.
     *
     * @return the amount to pay
     */
    public double getAmountToPay() {
        return amountToPay;
    }

    /**
     * Returns the current number of visitors.
     *
     * @return the current visitor count
     */
    public int getCurrentVisitors() {
        return currentVisitors;
    }

    /**
     * Returns the visit identifier.
     *
     * @return the visit ID
     */
    public int getVisitId() {
        return visitId;
    }

    /**
     * Returns the number of available spots.
     *
     * @return the available spots
     */
    public int getAvailableSpots() {
        return availableSpots;
    }
}