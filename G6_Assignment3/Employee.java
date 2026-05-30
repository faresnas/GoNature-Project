package common.entities;

import java.io.Serializable;

/**
 * Represents an employee of the Parks and Recreation department.
 *
 * <p>All employees (park workers, park managers, department manager,
 * service representatives) log in with username and password.
 * Their role determines which system actions they are authorised to perform.</p>
 *
 * <p>Role hierarchy and permissions:</p>
 * <ul>
 *   <li>{@link Role#PARK_WORKER}  — park entry/exit control, billing.</li>
 *   <li>{@link Role#PARK_MANAGER} — capacity/parameter management,
 *       monthly reports; changes need dept. manager approval.</li>
 *   <li>{@link Role#DEPT_MANAGER} — approves parameter changes,
 *       views all department-wide reports.</li>
 *   <li>{@link Role#SERVICE_REP}  — registers subscribers and guides.</li>
 * </ul>
 *
 * @author Group 6
 * @version 1.0
 */
public class Employee implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Defines all employee roles recognised by the GoNature system.
     */
    public enum Role {
        /** Operates the park entry/exit kiosk. */
        PARK_WORKER,
        /** Manages park parameters and generates monthly reports. */
        PARK_MANAGER,
        /** Approves parameter changes and views department-wide reports. */
        DEPT_MANAGER,
        /** Registers subscribers and group guides at the central office. */
        SERVICE_REP
    }

    /** System employee number (primary key). */
    private int employeeId;

    /** Login username. */
    private String username;

    /** Hashed password (stored as SHA-256 hex in the DB). */
    private String passwordHash;

    /** First name. */
    private String firstName;

    /** Last name. */
    private String lastName;

    /** Contact email. */
    private String email;

    /** Employee's role — determines authorised actions. */
    private Role role;

    /**
     * ID of the park this employee belongs to.
     * For {@link Role#DEPT_MANAGER} and {@link Role#SERVICE_REP} this is 0 (central office).
     */
    private int parkId;

    /** Whether this employee is currently logged in. */
    private boolean loggedIn;

    /**
     * Constructs an Employee.
     *
     * @param employeeId   unique employee number
     * @param username     login username
     * @param passwordHash SHA-256 hex of the password
     * @param firstName    first name
     * @param lastName     last name
     * @param email        contact email
     * @param role         system role
     * @param parkId       affiliated park ID (0 for central office)
     */
    public Employee(int employeeId, String username, String passwordHash,
                    String firstName, String lastName, String email,
                    Role role, int parkId) {
        this.employeeId   = employeeId;
        this.username     = username;
        this.passwordHash = passwordHash;
        this.firstName    = firstName;
        this.lastName     = lastName;
        this.email        = email;
        this.role         = role;
        this.parkId       = parkId;
        this.loggedIn     = false;
    }

    // ── Getters & Setters ────────────────────────────────────────────────────

    /** @return the employee's ID number */
    public int getEmployeeId()  { return employeeId; }

    /** @return login username */
    public String getUsername() { return username; }

    /** @return SHA-256 hex password hash */
    public String getPasswordHash() { return passwordHash; }

    /** @return first name */
    public String getFirstName() { return firstName; }

    /** @return last name */
    public String getLastName()  { return lastName; }

    /** @return contact email */
    public String getEmail()     { return email; }

    /** @return employee role */
    public Role getRole()        { return role; }

    /** @return affiliated park ID (0 = central office) */
    public int getParkId()       { return parkId; }

    /** @return {@code true} if currently logged in */
    public boolean isLoggedIn()  { return loggedIn; }

    /** @param loggedIn login state to set */
    public void setLoggedIn(boolean loggedIn) { this.loggedIn = loggedIn; }

    /**
     * Returns a display string for this employee.
     *
     * @return "FirstName LastName (role)"
     */
    @Override
    public String toString() {
        return firstName + " " + lastName + " (" + role + ")";
    }
}