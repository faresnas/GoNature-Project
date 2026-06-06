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
 * Singleton connection pool for GoNature DB.
 * Manages a pool of reusable MySQL connections.
 */
public class DBController {

    private static DBController instance = null;

    private static final String URL =
        "jdbc:mysql://localhost:3306/GoNature?allowLoadLocalInfile=true&serverTimezone=Asia/Jerusalem&useSSL=false";
    private static final String USER = "root";
    private static final String PASSWORD = "Hadi218057";
    private static final int POOL_SIZE = 5;

    private List<Connection> pool = new ArrayList<>();

    private DBController() {
        initPool();
    }

    public static DBController getInstance() {
        if (instance == null) {
            instance = new DBController();
        }
        return instance;
    }

    private void initPool() {
        try {
            for (int i = 0; i < POOL_SIZE; i++) {
                Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                pool.add(conn);
            }
            System.out.println("DBController: connection pool initialized (" + POOL_SIZE + " connections)");
        } catch (SQLException e) {
            System.out.println("DBController: failed to initialize pool — " + e.getMessage());
        }
    }

    public synchronized Connection getConnection() {
        for (Connection conn : pool) {
            try {
                if (!conn.isClosed()) {
                    return conn;
                }
            } catch (SQLException e) {
                System.out.println("DBController: bad connection in pool — " + e.getMessage());
            }
        }
        System.out.println("DBController: no available connections — reinitializing pool");
        pool.clear();
        initPool();
        return pool.isEmpty() ? null : pool.get(0);
    }

    /**
     * Executes a SELECT query and returns results as a list of rows,
     * where each row is a list of column values as Strings.
     *
     * @param query the SQL SELECT query to execute
     * @return list of rows, each row is a list of column values
     */
    public ArrayList<ArrayList<String>> executeQuery(String query) {
        ArrayList<ArrayList<String>> results = new ArrayList<>();
        Connection conn = getConnection();
        if (conn == null) {
            System.out.println("DBController: executeQuery — no connection available");
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
                    String val = rs.getString(i);
                    row.add(val == null ? "" : val);
                }
                results.add(row);
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.out.println("DBController: executeQuery failed — " + e.getMessage());
        }
        return results;
    }

    public boolean isConnected() {
        Connection conn = getConnection();
        try {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    public void closeAll() {
        for (Connection conn : pool) {
            try {
                if (!conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException e) {
                System.out.println("DBController: error closing connection — " + e.getMessage());
            }
        }
        pool.clear();
        instance = null;
        System.out.println("DBController: all connections closed");
    }
}