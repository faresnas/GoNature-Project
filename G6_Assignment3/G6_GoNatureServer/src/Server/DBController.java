package Server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Singleton class responsible for managing the application's
 * database connections.
 * <p>
 * The class maintains a pool of reusable MySQL connections
 * and provides methods for executing SQL queries and updates.
 */
public class DBController {

    /**
     * The single instance of the database controller.
     */
    private static DBController instance = null;

    /**
     * Database connection URL.
     */
    private static final String URL =
        "jdbc:mysql://localhost:3306/gonature?allowLoadLocalInfile=true&serverTimezone=Asia/Jerusalem&useSSL=false";

    /**
     * Database username.
     */
    private static final String USER = "root";

    /**
     * Database password.
     */
    private static final String PASSWORD = "";

    /**
     * Number of connections maintained in the connection pool.
     */
    private static final int POOL_SIZE = 5;

    /**
     * Pool containing reusable database connections.
     */
    private List<Connection> pool = new ArrayList<>();

    /**
     * Creates the database controller and initializes
     * the connection pool.
     */
    private DBController() {
        initPool();
    }

    /**
     * Returns the singleton instance of the database controller.
     *
     * @return the database controller instance
     */
    public static DBController getInstance() {
        if (instance == null) {
            instance = new DBController();
        }
        return instance;
    }

    /**
     * Initializes the database connection pool.
     */
    private void initPool() {
        try {
            for (int i = 0; i < POOL_SIZE; i++) {
                Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                pool.add(conn);
            }

            System.out.println(
                "DBController: connection pool initialized (" +
                POOL_SIZE +
                " connections)");

        } catch (SQLException e) {
            System.out.println(
                "DBController: failed to initialize pool — "
                + e.getMessage());
        }
    }

    /**
     * Returns an available database connection from the pool.
     * If no valid connection exists, the pool is recreated.
     *
     * @return an active database connection, or null if unavailable
     */
    public synchronized Connection getConnection() {

        for (Connection conn : pool) {

            try {

                if (!conn.isClosed()) {
                    return conn;
                }

            } catch (SQLException e) {
                System.out.println(
                    "DBController: bad connection in pool — "
                    + e.getMessage());
            }
        }

        System.out.println(
            "DBController: no available connections — reinitializing pool");

        pool.clear();
        initPool();

        return pool.isEmpty() ? null : pool.get(0);
    }

    /**
     * Executes an SQL SELECT statement.
     *
     * @param query the SQL SELECT query
     * @return a list containing the query results
     */
    public ArrayList<ArrayList<String>> executeQuery(String query) {

        ArrayList<ArrayList<String>> results = new ArrayList<>();

        Connection conn = getConnection();

        if (conn == null) {
            System.out.println(
                "DBController: executeQuery — no connection available");
            return results;
        }

        try {

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            ResultSetMetaData meta = rs.getMetaData();
            int columnCount = meta.getColumnCount();

            while (rs.next()) {

                ArrayList<String> row = new ArrayList<>();

                for (int i = 1; i <= columnCount; i++) {

                    String value = rs.getString(i);
                    row.add(value == null ? "" : value);
                }

                results.add(row);
            }

            rs.close();
            stmt.close();

        } catch (SQLException e) {

            System.out.println(
                "DBController: executeQuery failed — "
                + e.getMessage());
        }

        return results;
    }

    /**
     * Executes an SQL INSERT, UPDATE, or DELETE statement.
     *
     * @param query the SQL update statement
     * @return the number of affected rows, or -1 if the operation fails
     */
    public int executeUpdate(String query) {

        Connection conn = getConnection();

        if (conn == null) {
            System.out.println(
                "DBController: executeUpdate — no connection available");
            return -1;
        }

        try {

            Statement stmt = conn.createStatement();

            int affectedRows = stmt.executeUpdate(query);

            stmt.close();

            return affectedRows;

        } catch (SQLException e) {

            System.out.println(
                "DBController: executeUpdate failed — "
                + e.getMessage());

            return -1;
        }
    }

    /**
     * Checks whether the database is currently connected.
     *
     * @return true if a valid connection exists, otherwise false
     */
    public boolean isConnected() {

        Connection conn = getConnection();

        try {
            return conn != null && !conn.isClosed();

        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Closes all database connections and clears the connection pool.
     */
    public void closeAll() {

        for (Connection conn : pool) {

            try {

                if (!conn.isClosed()) {
                    conn.close();
                }

            } catch (SQLException e) {

                System.out.println(
                    "DBController: error closing connection — "
                    + e.getMessage());
            }
        }

        pool.clear();
        instance = null;

        System.out.println(
            "DBController: all connections closed");
    }
}