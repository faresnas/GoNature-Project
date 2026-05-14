package Server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBController {
    private Connection conn;

    public DBController() {
        connectToDB();
    }

    private void connectToDB() {
        try {
            conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/GoNature?allowLoadLocalInfile=true&serverTimezone=Asia/Jerusalem&useSSL=false",
                "root", "Hadi218057");
            System.out.println("Connected to GoNature DB successfully");
        } catch (SQLException e) {
            System.out.println("DB connection failed: " + e.getMessage());
        }
    }

    public Connection getConnection() {
        return conn;
    }

    public boolean isConnected() {
        try {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}