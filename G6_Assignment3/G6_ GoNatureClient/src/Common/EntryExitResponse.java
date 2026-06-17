package Common;

import java.io.Serializable;

/**
 * Response object for park entry, exit, visitor count and payment operations.
 */
public class EntryExitResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean success;
    private String message;
    private double amountToPay;
    private int currentVisitors;
    private int visitId;

    public EntryExitResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public EntryExitResponse(boolean success, String message, double amountToPay, int currentVisitors, int visitId) {
        this.success = success;
        this.message = message;
        this.amountToPay = amountToPay;
        this.currentVisitors = currentVisitors;
        this.visitId = visitId;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public double getAmountToPay() {
        return amountToPay;
    }

    public int getCurrentVisitors() {
        return currentVisitors;
    }

    public int getVisitId() {
        return visitId;
    }
}
