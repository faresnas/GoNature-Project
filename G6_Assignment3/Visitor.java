package common.entities;

import java.io.Serializable;

/**
 * Represents a visitor (traveler) who can book park visits.
 * Visitors identify themselves by ID number only — no password required.
 *
 * <p>A visitor may optionally be a {@link Subscriber} (club member),
 * in which case they receive additional pricing discounts.</p>
 *
 * @author Group 6
 * @version 1.0
 */
public class Visitor implements Serializable {

    private static final long serialVersionUID = 1L;

    /** National ID number used for identification at booking and park entry. */
    private String idNumber;

    /** First name of the visitor. */
    private String firstName;

    /** Last name of the visitor. */
    private String lastName;

    /** Email address for booking confirmations and reminders. */
    private String email;

    /** Mobile phone number for SMS simulations. */
    private String phone;

    /**
     * Constructs a Visitor with all required fields.
     *
     * @param idNumber  national ID number (used as login key)
     * @param firstName visitor's first name
     * @param lastName  visitor's last name
     * @param email     contact email
     * @param phone     mobile phone for SMS simulation
     */
    public Visitor(String idNumber, String firstName, String lastName,
                   String email, String phone) {
        this.idNumber  = idNumber;
        this.firstName = firstName;
        this.lastName  = lastName;
        this.email     = email;
        this.phone     = phone;
    }

    // ── Getters & Setters ────────────────────────────────────────────────────

    /** @return the visitor's national ID number */
    public String getIdNumber()  { return idNumber; }

    /** @param idNumber national ID to set */
    public void setIdNumber(String idNumber) { this.idNumber = idNumber; }

    /** @return the visitor's first name */
    public String getFirstName() { return firstName; }

    /** @param firstName first name to set */
    public void setFirstName(String firstName) { this.firstName = firstName; }

    /** @return the visitor's last name */
    public String getLastName()  { return lastName; }

    /** @param lastName last name to set */
    public void setLastName(String lastName) { this.lastName = lastName; }

    /** @return the visitor's email address */
    public String getEmail()     { return email; }

    /** @param email email to set */
    public void setEmail(String email) { this.email = email; }

    /** @return the visitor's phone number */
    public String getPhone()     { return phone; }

    /** @param phone phone to set */
    public void setPhone(String phone) { this.phone = phone; }

    /**
     * Returns a human-readable representation of this visitor.
     *
     * @return string in the format "FirstName LastName (ID)"
     */
    @Override
    public String toString() {
        return firstName + " " + lastName + " (" + idNumber + ")";
    }
}