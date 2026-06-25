package data;

import java.io.Serializable;
import java.sql.Date;

/**
 * Represents an order in the GoNature system.
 * <p>
 * This class stores all information related to a reservation order,
 * including the order number, visit date, number of visitors,
 * confirmation code, subscriber ID, and the date the order was created.
 * </p>
 *
 * @author Fares
 * @version 1.0
 */
@SuppressWarnings("serial")
public class Order implements Serializable {

    /**
     * The unique order number.
     */
    private int orderNumber;

    /**
     * The scheduled visit date.
     */
    private Date orderDate;

    /**
     * The number of visitors included in the order.
     */
    private int numberOfVisitors;

    /**
     * The confirmation code assigned to the order.
     */
    private int confirmationCode;

    /**
     * The subscriber who created the order.
     */
    private int subscriberId;

    /**
     * The date on which the order was placed.
     */
    private Date dateOfPlacingOrder;

    /**
     * Creates a new Order object.
     *
     * @param orderNumber the unique order number.
     * @param orderDate the scheduled visit date.
     * @param numberOfVisitors the number of visitors.
     * @param confirmationCode the confirmation code.
     * @param subscriberId the subscriber identifier.
     * @param dateOfPlacingOrder the date the order was placed.
     */
    public Order(int orderNumber, Date orderDate, int numberOfVisitors,
                 int confirmationCode, int subscriberId,
                 Date dateOfPlacingOrder) {

        this.orderNumber = orderNumber;
        this.orderDate = orderDate;
        this.numberOfVisitors = numberOfVisitors;
        this.confirmationCode = confirmationCode;
        this.subscriberId = subscriberId;
        this.dateOfPlacingOrder = dateOfPlacingOrder;
    }

    /**
     * Creates an empty Order object.
     */
    public Order() {
    }

    /**
     * Returns the order number.
     *
     * @return the order number.
     */
    public int getOrderNumber() {
        return orderNumber;
    }

    /**
     * Sets the order number.
     *
     * @param orderNumber the order number.
     */
    public void setOrderNumber(int orderNumber) {
        this.orderNumber = orderNumber;
    }

    /**
     * Returns the scheduled visit date.
     *
     * @return the order date.
     */
    public Date getOrderDate() {
        return orderDate;
    }

    /**
     * Sets the scheduled visit date.
     *
     * @param orderDate the visit date.
     */
    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    /**
     * Returns the number of visitors.
     *
     * @return the number of visitors.
     */
    public int getNumberOfVisitors() {
        return numberOfVisitors;
    }

    /**
     * Sets the number of visitors.
     *
     * @param numberOfVisitors the number of visitors.
     */
    public void setNumberOfVisitors(int numberOfVisitors) {
        this.numberOfVisitors = numberOfVisitors;
    }

    /**
     * Returns the confirmation code.
     *
     * @return the confirmation code.
     */
    public int getConfirmationCode() {
        return confirmationCode;
    }

    /**
     * Sets the confirmation code.
     *
     * @param confirmationCode the confirmation code.
     */
    public void setConfirmationCode(int confirmationCode) {
        this.confirmationCode = confirmationCode;
    }

    /**
     * Returns the subscriber ID.
     *
     * @return the subscriber ID.
     */
    public int getSubscriberId() {
        return subscriberId;
    }

    /**
     * Sets the subscriber ID.
     *
     * @param subscriberId the subscriber ID.
     */
    public void setSubscriberId(int subscriberId) {
        this.subscriberId = subscriberId;
    }

    /**
     * Returns the date on which the order was placed.
     *
     * @return the order creation date.
     */
    public Date getDateOfPlacingOrder() {
        return dateOfPlacingOrder;
    }

    /**
     * Sets the date on which the order was placed.
     *
     * @param dateOfPlacingOrder the order creation date.
     */
    public void setDateOfPlacingOrder(Date dateOfPlacingOrder) {
        this.dateOfPlacingOrder = dateOfPlacingOrder;
    }

    /**
     * Returns a formatted string representation of the order.
     *
     * @return a string containing the order details.
     */
    @Override
    public String toString() {

        return "Order Number: " + orderNumber
                + " | Visitors: " + numberOfVisitors
                + " | Order Date: " + orderDate;
    }
}