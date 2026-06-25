package Common;

import java.io.Serializable;

/**
 * Represents the response returned after a park entry or exit operation.
 * It stores information such as the operation result, message,
 * payment amount, visitor count, visit ID and available park capacity.
 *
 * @author Fares
 * @version 1.0
 */
public class EntryExitResponse implements Serializable {

    /**
     * Serialization identifier.
     */
    private static final long serialVersionUID = 1L;

    /** Indicates whether the operation succeeded. */
    private boolean success;

    /** Response message. */
    private String message;

    /** Amount to pay for the visit. */
    private double amountToPay;

    /** Current number of visitors in the park. */
    private int currentVisitors;

    /** Visit identifier. */
    private int visitId;

    /** Number of available spots in the park. */
    private int availableSpots;

    /**
     * Creates a basic response.
     *
     * @param success operation result.
     * @param message response message.
     */
    public EntryExitResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    /**
     * Creates a response with payment and visitor information.
     *
     * @param success operation result.
     * @param message response message.
     * @param amountToPay payment amount.
     * @param currentVisitors current visitors inside the park.
     * @param visitId visit identifier.
     */
    public EntryExitResponse(boolean success, String message,
            double amountToPay, int currentVisitors, int visitId) {
        this.success = success;
        this.message = message;
        this.amountToPay = amountToPay;
        this.currentVisitors = currentVisitors;
        this.visitId = visitId;
    }

    /**
     * Creates a complete response.
     *
     * @param success operation result.
     * @param message response message.
     * @param amountToPay payment amount.
     * @param currentVisitors current visitors inside the park.
     * @param visitId visit identifier.
     * @param availableSpots available spots in the park.
     */
    public EntryExitResponse(boolean success, String message,
            double amountToPay, int currentVisitors,
            int visitId, int availableSpots) {
        this.success = success;
        this.message = message;
        this.amountToPay = amountToPay;
        this.currentVisitors = currentVisitors;
        this.visitId = visitId;
        this.availableSpots = availableSpots;
    }

    /** @return true if the operation succeeded. */
    public boolean isSuccess() { return success; }

    /** @return the response message. */
    public String getMessage() { return message; }

    /** @return the payment amount. */
    public double getAmountToPay() { return amountToPay; }

    /** @return the current visitor count. */
    public int getCurrentVisitors() { return currentVisitors; }

    /** @return the visit ID. */
    public int getVisitId() { return visitId; }

    /** @return available spots in the park. */
    public int getAvailableSpots() { return availableSpots; }
}