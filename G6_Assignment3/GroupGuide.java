package common.entities;

import java.io.Serializable;

/**
 * Represents a registered group guide in the GoNature system.
 *
 * <p>Group guides are pre-registered by a Service Representative.
 * Only registered guides may make group bookings (up to 15 participants).
 * The guide himself does not pay entry for pre-booked group visits.</p>
 *
 * @author Group 6
 * @version 1.0
 */
public class GroupGuide extends Visitor implements Serializable {

    private static final long serialVersionUID = 1L;

    /** System-assigned guide ID. */
    private int guideId;

    /** Whether this guide is approved to make group bookings. */
    private boolean approved;

    /**
     * Constructs a GroupGuide.
     *
     * @param idNumber  national ID
     * @param firstName first name
     * @param lastName  last name
     * @param email     contact email
     * @param phone     mobile phone
     * @param guideId   system-assigned guide ID
     */
    public GroupGuide(String idNumber, String firstName, String lastName,
                      String email, String phone, int guideId) {
        super(idNumber, firstName, lastName, email, phone);
        this.guideId  = guideId;
        this.approved = true;
    }

    /** @return the guide's system ID */
    public int getGuideId() { return guideId; }

    /** @return {@code true} if the guide is approved for group bookings */
    public boolean isApproved() { return approved; }

    /** @param approved approval status to set */
    public void setApproved(boolean approved) { this.approved = approved; }

    @Override
    public String toString() {
        return super.toString() + " [Guide #" + guideId + "]";
    }
}