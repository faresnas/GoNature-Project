package Server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Singleton connection pool for GoNature DB.
 * Manages a pool of reusable MySQL connections.
 */
public class DBController {

    // Singleton instance
    private static DBController instance = null;

    private static final String URL =
        "jdbc:mysql://localhost:3306/GoNature?allowLoadLocalInfile=true&serverTimezone=Asia/Jerusalem&useSSL=false";
    private static final String USER = "root";
    private static final String PASSWORD = "Hadi218057";
    private static final int POOL_SIZE = 5;

    private List<Connection> pool = new ArrayList<>();

    // private constructor — no one can call new DBController()
    private DBController() {
        initPool();
    }

    // Singleton — only one instance exists
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

    // borrow a connection from the pool
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
        // if all connections are closed, try to reinitialize
        System.out.println("DBController: no available connections — reinitializing pool");
        pool.clear();
        initPool();
        return pool.isEmpty() ? null : pool.get(0);
    }

    public boolean isConnected() {
        Connection conn = getConnection();
        try {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    // close all connections — call on server shutdown
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