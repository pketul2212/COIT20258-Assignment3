package database;

import model.Disaster;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DisasterDAO {

    private final Connection connection;

    public DisasterDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    public boolean createDisaster(Disaster d) {
        String sql = "INSERT INTO disasters (disaster_type,title,description,location,latitude,longitude,"
                + "severity,status,reported_by,estimated_affected) VALUES(?,?,?,?,?,?,?,?,?,?)";

        try (PreparedStatement s = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            s.setString(1, d.getDisasterType().name());
            s.setString(2, d.getTitle());
            s.setString(3, d.getDescription());
            s.setString(4, d.getLocation());
            s.setDouble(5, d.getLatitude());
            s.setDouble(6, d.getLongitude());
            s.setString(7, d.getSeverity().name());
            s.setString(8, d.getStatus().name());
            s.setInt(9, d.getReportedBy());
            s.setInt(10, d.getEstimatedAffected());

            if (s.executeUpdate() > 0) {
                ResultSet k = s.getGeneratedKeys();
                if (k.next()) d.setDisasterId(k.getInt(1));
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[DisasterDAO] Create: " + e.getMessage());
        }

        return false;
    }

    public List<Disaster> getAllDisasters() {
        List<Disaster> list = new ArrayList<>();

        try (Statement s = connection.createStatement();
             ResultSet rs = s.executeQuery("SELECT * FROM disasters ORDER BY reported_at DESC")) {

            while (rs.next()) list.add(map(rs));

        } catch (SQLException e) {
            System.err.println("[DisasterDAO] GetAll: " + e.getMessage());
        }

        return list;
    }

    public Disaster getDisasterById(int id) {
        try (PreparedStatement s = connection.prepareStatement("SELECT * FROM disasters WHERE disaster_id=?")) {
            s.setInt(1, id);
            ResultSet rs = s.executeQuery();

            if (rs.next()) return map(rs);

        } catch (SQLException e) {
            System.err.println("[DisasterDAO] GetById: " + e.getMessage());
        }

        return null;
    }

    public boolean updateDisasterStatus(int disasterId, Disaster.Status newStatus) {
        String sql = "UPDATE disasters SET status=?, "
                + "assessed_at=CASE WHEN ?='ASSESSED' OR ?='RESPONDING' THEN NOW() ELSE assessed_at END, "
                + "resolved_at=CASE WHEN ?='RESOLVED' THEN NOW() ELSE resolved_at END "
                + "WHERE disaster_id=?";

        try (PreparedStatement s = connection.prepareStatement(sql)) {
            s.setString(1, newStatus.name());
            s.setString(2, newStatus.name());
            s.setString(3, newStatus.name());
            s.setString(4, newStatus.name());
            s.setInt(5, disasterId);

            return s.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[DisasterDAO] UpdateStatus: " + e.getMessage());
        }

        return false;
    }

    public boolean updateDisaster(Disaster d) {
        String sql = "UPDATE disasters SET disaster_type=?, title=?, description=?, location=?, "
                + "severity=?, status=?, estimated_affected=?, assigned_coordinator=? "
                + "WHERE disaster_id=?";

        try (PreparedStatement s = connection.prepareStatement(sql)) {
            s.setString(1, d.getDisasterType().name());
            s.setString(2, d.getTitle());
            s.setString(3, d.getDescription());
            s.setString(4, d.getLocation());
            s.setString(5, d.getSeverity().name());
            s.setString(6, d.getStatus().name());
            s.setInt(7, d.getEstimatedAffected());

            if (d.getAssignedCoordinator() > 0) {
                s.setInt(8, d.getAssignedCoordinator());
            } else {
                s.setNull(8, java.sql.Types.INTEGER);
            }

            s.setInt(9, d.getDisasterId());

            return s.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[DisasterDAO] Update: " + e.getMessage());
        }

        return false;
    }

    public List<Disaster> getPrioritizedDisasters() {
        List<Disaster> list = getAllDisasters();
        list.sort((a, b) -> b.getPriorityScore() - a.getPriorityScore());
        return list;
    }

    private Disaster map(ResultSet rs) throws SQLException {
        Disaster d = new Disaster();

        d.setDisasterId(rs.getInt("disaster_id"));
        d.setDisasterType(Disaster.DisasterType.valueOf(rs.getString("disaster_type")));
        d.setTitle(rs.getString("title"));
        d.setDescription(rs.getString("description"));
        d.setLocation(rs.getString("location"));
        d.setLatitude(rs.getDouble("latitude"));
        d.setLongitude(rs.getDouble("longitude"));
        d.setSeverity(Disaster.Severity.valueOf(rs.getString("severity")));
        d.setStatus(Disaster.Status.valueOf(rs.getString("status")));
        d.setReportedBy(rs.getInt("reported_by"));

        int assignedCoordinator = rs.getInt("assigned_coordinator");
        if (rs.wasNull()) {
            d.setAssignedCoordinator(0);
        } else {
            d.setAssignedCoordinator(assignedCoordinator);
        }

        Timestamp reportedAt = rs.getTimestamp("reported_at");
        if (reportedAt != null) d.setReportedAt(reportedAt.toLocalDateTime());

        Timestamp assessedAt = rs.getTimestamp("assessed_at");
        if (assessedAt != null) d.setAssessedAt(assessedAt.toLocalDateTime());

        Timestamp resolvedAt = rs.getTimestamp("resolved_at");
        if (resolvedAt != null) d.setResolvedAt(resolvedAt.toLocalDateTime());

        d.setEstimatedAffected(rs.getInt("estimated_affected"));
        d.setEncrypted(rs.getBoolean("is_encrypted"));

        return d;
    }
}