package ir.internship.logmonitor.database;

import ir.internship.logmonitor.model.LogAlert;
import ir.internship.logmonitor.model.LogComponent;
import ir.internship.logmonitor.model.LogLevel;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Database {
    private final Connection connection;

    public Database() throws SQLException {
        connection = DriverManager.getConnection(
                "jdbc:postgresql://localhost:5432/logmonitor",
                        "hamid",
                "1111"
        );
    }

    public void insert(LogAlert alert) throws SQLException {
        String sql = """
                INSERT INTO alerts
                (rule_name, component, level, timestamp, description)
                VALUES
                (?, ?, ?, ?, ?)
                """;
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, alert.ruleName);
        ps.setString(2, alert.component);
        ps.setString(3, alert.level);
        ps.setTimestamp(4, Timestamp.valueOf(alert.timestamp));
        ps.setString(5, alert.description);

        ps.executeUpdate();
        ps.close();
    }

    public static String buildWhere(
            String ruleName,
            LogComponent component,
            LogLevel level,
            Boolean levelMustBeNull,
            LocalDateTime from,
            LocalDateTime to
    ) {
        String sql = " WHERE true";

        if (ruleName != null) {
            sql += " AND rule_name = '" + ruleName + "'";
        }

        if (component != null) {
            sql += " AND component = '" + component + "'";
        }

        if (level != null) {
            sql += " AND level = '" + level.name() + "'";
        }
        else if (Boolean.TRUE.equals(levelMustBeNull)) {
            sql += " AND level IS NULL";
        }

        if (from != null) {
            sql += " AND timestamp >= '" + Timestamp.valueOf(from) + "'";
        }

        if (to != null) {
            sql += " AND timestamp <= '" + Timestamp.valueOf(to) + "'";
        }
        sql += "\n";
        return sql;
    }

    public List<LogAlert> getAlerts() throws SQLException {
        return getAlerts(null);
    }

    public List<LogAlert> getAlerts(String where) throws SQLException {
        List<LogAlert> alerts = new ArrayList<>();

        String sql = "SELECT * FROM alerts\n";
        if (where != null && !where.isBlank())
            sql += where;

        sql += "ORDER BY timestamp DESC";

        Statement s = connection.createStatement();
        ResultSet rs = s.executeQuery(sql);

        while (rs.next()) {
            alerts.add(new LogAlert(
                    rs.getInt("id"),
                    rs.getString("rule_name"),
                    rs.getString("component"),
                    rs.getString("level"),
                    rs.getTimestamp("timestamp").toLocalDateTime(),
                    rs.getString("description")
            ));
        }

        rs.close();
        s.close();

        return alerts;
    }

    public void deleteAlert(int id) throws SQLException {
        Statement s = connection.createStatement();
        s.executeUpdate("DELETE FROM alerts WHERE id = " + id);
        s.close();
    }

    public void deleteAlerts() throws SQLException {
        deleteAlerts(null);
    }

    public void deleteAlerts(String where) throws SQLException {
        String sql = "DELETE FROM alerts";

        if (where != null && !where.isBlank()) {
            sql += where;
        }

        Statement s = connection.createStatement();
        s.executeUpdate(sql);
        s.close();
    }


    public void close() throws SQLException {
        connection.close();
    }
}
