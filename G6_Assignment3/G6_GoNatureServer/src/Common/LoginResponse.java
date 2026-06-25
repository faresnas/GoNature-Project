package Common;

import java.io.Serializable;

/**
 * Represents the server response returned after a login request.
 * The response contains the login status, user information,
 * role, user type, and park information when applicable.
 */
public class LoginResponse implements Serializable {

    /**
     * Serialization version identifier.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Defines the supported user types in the system.
     */
    public enum UserType {
        EMPLOYEE,
        SUBSCRIBER,
        VISITOR,
        GUIDE
    }

    /**
     * Indicates whether the login operation succeeded.
     */
    private boolean success;

    /**
     * Message describing the login result.
     */
    private String message;

    /**
     * The user's role in the system.
     */
    private String role;

    /**
     * The type of the logged-in user.
     */
    private UserType userType;

    /**
     * The unique user identifier.
     */
    private int userId;

    /**
     * The user's first name.
     */
    private String firstName;

    /**
     * The user's last name.
     */
    private String lastName;

    /**
     * The user's email address.
     */
    private String email;

    /**
     * The park assigned to the user.
     */
    private Integer parkId;

    /**
     * The name of the assigned park.
     */
    private String parkName;

    /**
     * Creates a new login response.
     *
     * @param success true if login succeeded, otherwise false
     * @param message the login result message
     */
    public LoginResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    /**
     * Returns whether the login was successful.
     *
     * @return true if login succeeded
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Returns the login result message.
     *
     * @return the response message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Returns the user's role.
     *
     * @return the user role
     */
    public String getRole() {
        return role;
    }

    /**
     * Sets the user's role.
     *
     * @param role the user role
     */
    public void setRole(String role) {
        this.role = role;
    }

    /**
     * Returns the user type.
     *
     * @return the user type
     */
    public UserType getUserType() {
        return userType;
    }

    /**
     * Sets the user type.
     *
     * @param userType the user type
     */
    public void setUserType(UserType userType) {
        this.userType = userType;
    }

    /**
     * Returns the user ID.
     *
     * @return the unique user identifier
     */
    public int getUserId() {
        return userId;
    }

    /**
     * Sets the user ID.
     *
     * @param userId the unique user identifier
     */
    public void setUserId(int userId) {
        this.userId = userId;
    }

    /**
     * Returns the user's first name.
     *
     * @return the first name
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Sets the user's first name.
     *
     * @param firstName the first name
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Returns the user's last name.
     *
     * @return the last name
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Sets the user's last name.
     *
     * @param lastName the last name
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Returns the user's email address.
     *
     * @return the email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the user's email address.
     *
     * @param email the email address
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Returns the assigned park ID.
     *
     * @return the park ID, or -1 if no park is assigned
     */
    public Integer getParkId() {
        return parkId != null ? parkId : -1;
    }

    /**
     * Sets the assigned park ID.
     *
     * @param parkId the park ID
     */
    public void setParkId(Integer parkId) {
        this.parkId = parkId;
    }

    /**
     * Returns the assigned park name.
     *
     * @return the park name, or an empty string if unavailable
     */
    public String getParkName() {
        return parkName != null ? parkName : "";
    }

    /**
     * Sets the assigned park name.
     *
     * @param parkName the park name
     */
    public void setParkName(String parkName) {
        this.parkName = parkName;
    }
}