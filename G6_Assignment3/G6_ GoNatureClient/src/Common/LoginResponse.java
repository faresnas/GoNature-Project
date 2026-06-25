package Common;

import java.io.Serializable;

/**
 * Represents the response returned after a user login attempt.
 * The object contains the login status and the authenticated
 * user's information.
 *
 * @author Fares
 * @version 1.0
 */
public class LoginResponse implements Serializable {

    /**
     * Serialization identifier.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Defines the supported user types.
     */
    public enum UserType {
        EMPLOYEE,
        SUBSCRIBER,
        VISITOR,
        GUIDE
    }

    /** Indicates whether login succeeded. */
    private boolean success;

    /** Login response message. */
    private String message;

    /** User role. */
    private String role;

    /** User type. */
    private UserType userType;

    /** User identifier. */
    private int userId;

    /** User first name. */
    private String firstName;

    /** User last name. */
    private String lastName;

    /** User email address. */
    private String email;

    /** Park identifier. */
    private Integer parkId;

    /** Park name. */
    private String parkName;

    /** Username. */
    private String username;

    /**
     * Creates a login response.
     *
     * @param success login result.
     * @param message response message.
     */
    public LoginResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    /** @return login status. */
    public boolean isSuccess() { return success; }

    /** @return response message. */
    public String getMessage() { return message; }

    /** @return user role. */
    public String getRole() { return role; }

    /** Sets the user role. */
    public void setRole(String role) { this.role = role; }

    /** @return user type. */
    public UserType getUserType() { return userType; }

    /** Sets the user type. */
    public void setUserType(UserType userType) { this.userType = userType; }

    /** @return user ID. */
    public int getUserId() { return userId; }

    /** Sets the user ID. */
    public void setUserId(int userId) { this.userId = userId; }

    /** @return first name. */
    public String getFirstName() { return firstName; }

    /** Sets the first name. */
    public void setFirstName(String firstName) { this.firstName = firstName; }

    /** @return last name. */
    public String getLastName() { return lastName; }

    /** Sets the last name. */
    public void setLastName(String lastName) { this.lastName = lastName; }

    /** @return email address. */
    public String getEmail() { return email; }

    /** Sets the email address. */
    public void setEmail(String email) { this.email = email; }

    /** @return park ID or -1 if none exists. */
    public Integer getParkId() { return parkId != null ? parkId : -1; }

    /** Sets the park ID. */
    public void setParkId(Integer parkId) { this.parkId = parkId; }

    /** @return park name or an empty string. */
    public String getParkName() { return parkName != null ? parkName : ""; }

    /** Sets the park name. */
    public void setParkName(String parkName) { this.parkName = parkName; }

    /** @return username or an empty string. */
    public String getUsername() { return username != null ? username : ""; }

    /** Sets the username. */
    public void setUsername(String username) { this.username = username; }
}