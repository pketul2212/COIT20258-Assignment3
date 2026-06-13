package database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for audit logging — supports non-repudiation security requirement.
 */
public class AuditDAO {

    private final Connection connection;

    public AuditDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    public void log(int userId, String action, String tableAffected, int recordId,
            String oldValues, String newValues) {
        String sql = "INSERT INTO audit_log(user_id,action,table_affected,record_id,old_values,new_values) VALUES(?,?,?,?,?,?)";
        try (PreparedStatement s = connection.prepareStatement(sql)) {
            s.setInt(1, userId); s.setString(2, action); s.setString(3, tableAffected);
            s.setInt(4, recordId); s.setString(5, oldValues); s.setString(6, newValues);
            s.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[AuditDAO] Log: " + e.getMessage());
        }
    }

    public List<String[]> getRecentLogs(int limit) {
        List<String[]> logs = new ArrayList<>();
        String sql = "SELECT al.*,u.username FROM audit_log al "
                + "LEFT JOIN users u ON al.user_id=u.user_id "
                + "ORDER BY timestamp DESC LIMIT ?";
        try (PreparedStatement s = connection.prepareStatement(sql)) {
            s.setInt(1, limit); 
            ResultSet rs = s.executeQuery();
            while (rs.next()) {
                logs.add(new String[]{
                    rs.getString("timestamp"),
                    rs.getString("username") != null ? rs.getString("username") : "system",
                    rs.getString("action"),
                    rs.getString("table_affected") != null ? rs.getString("table_affected") : "",
                    String.valueOf(rs.getInt("record_id"))
                });
            }
        } catch (SQLException e) {
            System.err.println("[AuditDAO] GetLogs: " + e.getMessage());
        }
        return logs;
    }
}
