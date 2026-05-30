package common.entities;

import java.io.Serializable;

/**
 * Represents a registered subscriber ("club member") of the GoNature system.
 *
 * <p>Subscribers receive an additional 10% discount on top of any
 * other applicable pricing. They are registered by a Service Representative
 * at the central department office.</p>
 *
 * <p>Pricing rule (from system spec, section "Pricing Model"):</p>
 * <ul>
 *   <li>Subscriber discount: extra 10% on the final computed price.</li>
 * </ul>
 *
 * @author Group 6
 * @version 1.0
 */
public class Subscriber extends Visitor implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Unique system-assigned subscription number. */
    private int subscriberNumber;

    /** Total number of family members covered by this subscription. */
    private int familySize;

    /**
     * Credit card number for prepayment (optional — may be {@code null}
     * if the subscriber chose to pay in cash).
     */
    private String creditCardNumber;

    /**
     * Constructs a new Subscriber.
     *
     * @param idNumber         national ID number
     * @param firstName        first name
     * @param lastName         last name
     * @param email            contact email
     * @param phone            mobile phone
     * @param subscriberNumber system-assigned subscription ID
     * @param familySize       number of family members in the subscription
     * @param creditCardNumber credit card number, or {@code null} for cash payment
     */
    public Subscriber(String idNumber, String firstName, String lastName,
                      String email, String phone,
                      int subscriberNumber, int familySize,
                      String creditCardNumber) {
        super(idNumber, firstName, lastName, email, phone);
        this.subscriberNumber = subscriberNumber;
        this.familySize       = familySize;
        this.creditCardNumber = creditCardNumber;
    }

    // ── Getters & Setters ────────────────────────────────────────────────────

    /** @return the subscriber's unique subscription number */
    public int getSubscriberNumber() { return subscriberNumber; }

    /** @param subscriberNumber subscription number to set */
    public void setSubscriberNumber(int subscriberNumber) {
        this.subscriberNumber = subscriberNumber;
    }

    /** @return number of family members in this subscription */
    public int getFamilySize() { return familySize; }

    /** @param familySize family size to set */
    public void setFamilySize(int familySize) { this.familySize = familySize; }

    /**
     * @return credit card number, or {@code null} if cash payment was chosen
     */
    public String getCreditCardNumber() { return creditCardNumber; }

    /** @param creditCardNumber credit card number to set (may be null) */
    public void setCreditCardNumber(String creditCardNumber) {
        this.creditCardNumber = creditCardNumber;
    }

    /**
     * Returns a human-readable representation of this subscriber.
     *
     * @return string including name, ID, and subscription number
     */
    @Override
    public String toString() {
        return super.toString() + " [Subscriber #" + subscriberNumber + "]";
    }
}