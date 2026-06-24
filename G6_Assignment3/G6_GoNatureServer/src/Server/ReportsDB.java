package Server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class ReportsDB {

    private DBController dbController;

    public ReportsDB(DBController dbController) {
        this.dbController = dbController;
    }

    public ArrayList<ArrayList<String>> getVisitsReportByPark(int parkId, int month, int year) throws SQLException {
        Connection conn = dbController.getConnection();
        String sql =
            "SELECT p.name, pv.entry_time, pv.exit_time, pv.num_visitors, " +
            "CASE WHEN r.traveler_type = 'SUBSCRIBER' THEN 'SUBSCRIBER' ELSE pv.visitor_type END AS visitor_type, " +
            "TIMESTAMPDIFF(MINUTE, pv.entry_time, pv.exit_time) AS stay_minutes, " +
            "DAY(pv.entry_time) AS visit_day " +
            "FROM park_visits pv " +
            "JOIN parks p ON pv.park_id = p.id " +
            "LEFT JOIN reservations r ON pv.reservation_id = r.id " +
            "WHERE pv.park_id = ? " +
            "AND MONTH(pv.entry_time) = ? AND YEAR(pv.entry_time) = ? " +
            "AND pv.visitor_type IS NOT NULL " +
            "ORDER BY pv.entry_time ASC";

        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, parkId);
        ps.setInt(2, month);
        ps.setInt(3, year);
        ResultSet rs = ps.executeQuery();

        ArrayList<ArrayList<String>> result = new ArrayList<>();
        while (rs.next()) {
            ArrayList<String> row = new ArrayList<>();
            row.add(rs.getString(1));
            row.add(String.valueOf(rs.getTimestamp(2)));
            row.add(rs.getTimestamp(3) != null ? String.valueOf(rs.getTimestamp(3)) : "Still Inside");
            row.add(String.valueOf(rs.getInt(4)));
            row.add(rs.getString(5));
            int stay = rs.getInt(6);
            row.add(rs.wasNull() ? "N/A" : stay + " min");
            row.add(String.valueOf(rs.getInt(7)));
            result.add(row);
        }
        rs.close(); ps.close();
        return result;
    }

    public ArrayList<ArrayList<String>> getVisitsReportAllParks(int month, int year) throws SQLException {
        Connection conn = dbController.getConnection();
        String sql =
            "SELECT p.name, pv.entry_time, pv.exit_time, pv.num_visitors, " +
            "CASE WHEN r.traveler_type = 'SUBSCRIBER' THEN 'SUBSCRIBER' ELSE pv.visitor_type END AS visitor_type, " +
            "TIMESTAMPDIFF(MINUTE, pv.entry_time, pv.exit_time) AS stay_minutes, " +
            "DAY(pv.entry_time) AS visit_day " +
            "FROM park_visits pv " +
            "JOIN parks p ON pv.park_id = p.id " +
            "LEFT JOIN reservations r ON pv.reservation_id = r.id " +
            "WHERE MONTH(pv.entry_time) = ? AND YEAR(pv.entry_time) = ? " +
            "AND pv.visitor_type IS NOT NULL " +
            "ORDER BY pv.entry_time ASC";

        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, month);
        ps.setInt(2, year);
        ResultSet rs = ps.executeQuery();

        ArrayList<ArrayList<String>> result = new ArrayList<>();
        while (rs.next()) {
            ArrayList<String> row = new ArrayList<>();
            row.add(rs.getString(1));
            row.add(String.valueOf(rs.getTimestamp(2)));
            row.add(rs.getTimestamp(3) != null ? String.valueOf(rs.getTimestamp(3)) : "Still Inside");
            row.add(String.valueOf(rs.getInt(4)));
            row.add(rs.getString(5));
            int stay = rs.getInt(6);
            row.add(rs.wasNull() ? "N/A" : stay + " min");
            row.add(String.valueOf(rs.getInt(7)));
            result.add(row);
        }
        rs.close(); ps.close();
        return result;
    }

    public ArrayList<ArrayList<String>> getUsageReport(int parkId, int month, int year) throws SQLException {
        Connection conn = dbController.getConnection();
        String sql =
            "SELECT DAY(pv.entry_time) AS visit_day, " +
            "SUM(pv.num_visitors) AS total_visitors, " +
            "p.max_capacity, p.prebooked_reserved, " +
            "(p.max_capacity - p.prebooked_reserved) AS available_quota " +
            "FROM park_visits pv " +
            "JOIN parks p ON pv.park_id = p.id " +
            "WHERE pv.park_id = ? " +
            "AND MONTH(pv.entry_time) = ? AND YEAR(pv.entry_time) = ? " +
            "GROUP BY DAY(pv.entry_time), p.max_capacity, p.prebooked_reserved " +
            "ORDER BY visit_day ASC";

        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, parkId);
        ps.setInt(2, month);
        ps.setInt(3, year);
        ResultSet rs = ps.executeQuery();

        ArrayList<ArrayList<String>> result = new ArrayList<>();
        while (rs.next()) {
            ArrayList<String> row = new ArrayList<>();
            row.add("Day " + rs.getInt(1));
            row.add(String.valueOf(rs.getInt(2)));
            row.add(String.valueOf(rs.getInt(3)));
            row.add(String.valueOf(rs.getInt(5)));
            result.add(row);
        }
        rs.close(); ps.close();
        return result;
    }

    public ArrayList<ArrayList<String>> getCancellationsReport(int month, int year) throws SQLException {
        Connection conn = dbController.getConnection();

        String sql =
            "SELECT p.name, DATE(r.visit_date), r.status, COUNT(*), SUM(r.num_visitors) " +
            "FROM reservations r " +
            "JOIN parks p ON r.park_id = p.id " +
            "WHERE r.status = 'CANCELLED' " +
            "AND MONTH(r.visit_date) = ? AND YEAR(r.visit_date) = ? " +
            "GROUP BY p.name, DATE(r.visit_date), r.status " +
            "ORDER BY DATE(r.visit_date) DESC, p.name";

        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, month);
        ps.setInt(2, year);
        ResultSet rs = ps.executeQuery();

        ArrayList<ArrayList<String>> result = new ArrayList<>();
        while (rs.next()) {
            ArrayList<String> row = new ArrayList<>();
            row.add(rs.getString(1));
            row.add(String.valueOf(rs.getDate(2)));
            row.add(rs.getString(3));
            row.add(String.valueOf(rs.getInt(4)));
            row.add(String.valueOf(rs.getInt(5)));
            result.add(row);
        }
        rs.close(); ps.close();

        // NO_SHOW — includes explicit NO_SHOW status AND past PENDING/CONFIRMED
        String noShowSql =
            "SELECT p.name, DATE(r.visit_date), 'NO_SHOW', COUNT(*), SUM(r.num_visitors) " +
            "FROM reservations r " +
            "JOIN parks p ON r.park_id = p.id " +
            "WHERE (r.status = 'NO_SHOW' OR (r.status IN ('PENDING','CONFIRMED') AND r.visit_date < CURDATE())) " +
            "AND MONTH(r.visit_date) = ? AND YEAR(r.visit_date) = ? " +
            "GROUP BY p.name, DATE(r.visit_date) " +
            "ORDER BY DATE(r.visit_date) DESC, p.name";

        PreparedStatement noShowPs = conn.prepareStatement(noShowSql);
        noShowPs.setInt(1, month);
        noShowPs.setInt(2, year);
        ResultSet noShowRs = noShowPs.executeQuery();

        while (noShowRs.next()) {
            ArrayList<String> row = new ArrayList<>();
            row.add(noShowRs.getString(1));
            row.add(String.valueOf(noShowRs.getDate(2)));
            row.add("NO_SHOW");
            row.add(String.valueOf(noShowRs.getInt(4)));
            row.add(String.valueOf(noShowRs.getInt(5)));
            result.add(row);
        }
        noShowRs.close(); noShowPs.close();
        return result;
    }

    public ArrayList<ArrayList<String>> getVisitorCountByPark(int parkId, int month, int year) throws SQLException {
        Connection conn = dbController.getConnection();
        String sql =
            "SELECT p.name, " +
            "CASE WHEN r.traveler_type = 'SUBSCRIBER' THEN 'SUBSCRIBER' ELSE pv.visitor_type END AS visitor_type, " +
            "SUM(pv.num_visitors) AS total " +
            "FROM park_visits pv " +
            "JOIN parks p ON pv.park_id = p.id " +
            "LEFT JOIN reservations r ON pv.reservation_id = r.id " +
            "WHERE pv.park_id = ? " +
            "AND MONTH(pv.entry_time) = ? AND YEAR(pv.entry_time) = ? " +
            "GROUP BY p.name, visitor_type " +
            "ORDER BY visitor_type";

        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, parkId);
        ps.setInt(2, month);
        ps.setInt(3, year);
        ResultSet rs = ps.executeQuery();

        ArrayList<ArrayList<String>> result = new ArrayList<>();
        while (rs.next()) {
            ArrayList<String> row = new ArrayList<>();
            row.add(rs.getString(1));
            row.add(rs.getString(2));
            row.add(String.valueOf(rs.getInt(3)));
            result.add(row);
        }
        rs.close(); ps.close();
        return result;
    }

    public ArrayList<ArrayList<String>> getVisitorCountAllParks(int month, int year) throws SQLException {
        Connection conn = dbController.getConnection();
        String sql =
            "SELECT p.name, " +
            "CASE WHEN r.traveler_type = 'SUBSCRIBER' THEN 'SUBSCRIBER' ELSE pv.visitor_type END AS visitor_type, " +
            "SUM(pv.num_visitors) AS total " +
            "FROM park_visits pv " +
            "JOIN parks p ON pv.park_id = p.id " +
            "LEFT JOIN reservations r ON pv.reservation_id = r.id " +
            "WHERE MONTH(pv.entry_time) = ? AND YEAR(pv.entry_time) = ? " +
            "GROUP BY p.name, visitor_type " +
            "ORDER BY p.name, visitor_type";

        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, month);
        ps.setInt(2, year);
        ResultSet rs = ps.executeQuery();

        ArrayList<ArrayList<String>> result = new ArrayList<>();
        while (rs.next()) {
            ArrayList<String> row = new ArrayList<>();
            row.add(rs.getString(1));
            row.add(rs.getString(2));
            row.add(String.valueOf(rs.getInt(3)));
            result.add(row);
        }
        rs.close(); ps.close();
        return result;
    }

    public ArrayList<ArrayList<String>> getCancellationsReportByPark(int parkId, int month, int year) throws SQLException {
        Connection conn = dbController.getConnection();

        String sql =
            "SELECT p.name, DATE(r.visit_date), r.status, COUNT(*), SUM(r.num_visitors) " +
            "FROM reservations r " +
            "JOIN parks p ON r.park_id = p.id " +
            "WHERE r.status = 'CANCELLED' AND r.park_id = ? " +
            "AND MONTH(r.visit_date) = ? AND YEAR(r.visit_date) = ? " +
            "GROUP BY p.name, DATE(r.visit_date), r.status " +
            "ORDER BY DATE(r.visit_date) DESC";

        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, parkId);
        ps.setInt(2, month);
        ps.setInt(3, year);
        ResultSet rs = ps.executeQuery();

        ArrayList<ArrayList<String>> result = new ArrayList<>();
        while (rs.next()) {
            ArrayList<String> row = new ArrayList<>();
            row.add(rs.getString(1));
            row.add(String.valueOf(rs.getDate(2)));
            row.add(rs.getString(3));
            row.add(String.valueOf(rs.getInt(4)));
            row.add(String.valueOf(rs.getInt(5)));
            result.add(row);
        }
        rs.close(); ps.close();

        String noShowSql =
            "SELECT p.name, DATE(r.visit_date), 'NO_SHOW', COUNT(*), SUM(r.num_visitors) " +
            "FROM reservations r " +
            "JOIN parks p ON r.park_id = p.id " +
            "WHERE (r.status = 'NO_SHOW' OR (r.status IN ('PENDING','CONFIRMED') AND r.visit_date < CURDATE())) " +
            "AND r.park_id = ? " +
            "AND MONTH(r.visit_date) = ? AND YEAR(r.visit_date) = ? " +
            "GROUP BY p.name, DATE(r.visit_date) " +
            "ORDER BY DATE(r.visit_date) DESC";

        PreparedStatement noShowPs = conn.prepareStatement(noShowSql);
        noShowPs.setInt(1, parkId);
        noShowPs.setInt(2, month);
        noShowPs.setInt(3, year);
        ResultSet noShowRs = noShowPs.executeQuery();

        while (noShowRs.next()) {
            ArrayList<String> row = new ArrayList<>();
            row.add(noShowRs.getString(1));
            row.add(String.valueOf(noShowRs.getDate(2)));
            row.add("NO_SHOW");
            row.add(String.valueOf(noShowRs.getInt(4)));
            row.add(String.valueOf(noShowRs.getInt(5)));
            result.add(row);
        }
        noShowRs.close(); noShowPs.close();
        return result;
    }
}