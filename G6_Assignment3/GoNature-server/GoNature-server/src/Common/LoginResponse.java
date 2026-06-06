package Common;

import java.io.Serializable;

public class LoginResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum UserType { EMPLOYEE, SUBSCRIBER, VISITOR, GUIDE }

    private boolean success;
    private String message;
    private String role;       // PARK_WORKER, PARK_MANAGER, etc. null for visitor
    private UserType userType;
    private int userId;
    private String firstName;
    private String lastName;
    private String email;
    private Integer parkId;    // only for PARK_WORKER and PARK_MANAGER

    public LoginResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    // getters and setters
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public UserType getUserType() { return userType; }
    public void setUserType(UserType userType) { this.userType = userType; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Integer getParkId() { return parkId; }
    public void setParkId(Integer parkId) { this.parkId = parkId; }
}