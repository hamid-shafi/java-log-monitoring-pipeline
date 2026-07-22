package ir.internship.logmonitor.database;

import ir.internship.logmonitor.model.LogAlert;

import java.sql.*;

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
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, alert.ruleName);
            ps.setString(2, alert.component);
            ps.setString(3, alert.level);
            ps.setTimestamp(4, Timestamp.valueOf(alert.timestamp));
            ps.setString(5, alert.description);

            ps.executeUpdate();
        }
    }

    public void close() throws SQLException {
        connection.close();
    }
}
