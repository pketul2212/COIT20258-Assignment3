package database;

import model.ResponseTeam;
import model.TeamAssignment;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ResponseTeamDAO {

    private final Connection connection;

    public ResponseTeamDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    public boolean createTeam(ResponseTeam t) {
        String sql = "INSERT INTO response_teams(team_name,department,team_leader,contact_number,status,capacity,current_members) VALUES(?,?,?,?,?,?,?)";

        try (PreparedStatement s = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            s.setString(1, t.getTeamName());
            s.setString(2, t.getDepartment().name());
            s.setString(3, t.getTeamLeader());
            s.setString(4, t.getContactNumber());
            s.setString(5, t.getStatus().name());
            s.setInt(6, t.getCapacity());
            s.setInt(7, t.getCurrentMembers());

            if (s.executeUpdate() > 0) {
                ResultSet k = s.getGeneratedKeys();
                if (k.next()) t.setTeamId(k.getInt(1));
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[TeamDAO] Create: " + e.getMessage());
        }

        return false;
    }

    public List<ResponseTeam> getAllTeams() {
        List<ResponseTeam> list = new ArrayList<>();

        try (Statement s = connection.createStatement();
             ResultSet rs = s.executeQuery("SELECT * FROM response_teams ORDER BY team_id")) {

            while (rs.next()) list.add(mapTeam(rs));

        } catch (SQLException e) {
            System.err.println("[TeamDAO] GetAll: " + e.getMessage());
        }

        return list;
    }

    public List<ResponseTeam> getAvailableTeams() {
        List<ResponseTeam> list = new ArrayList<>();

        String sql = "SELECT * FROM response_teams WHERE status='AVAILABLE' ORDER BY team_id";

        try (Statement s = connection.createStatement();
             ResultSet rs = s.executeQuery(sql)) {

            while (rs.next()) list.add(mapTeam(rs));

        } catch (SQLException e) {
            System.err.println("[TeamDAO] GetAvailableTeams: " + e.getMessage());
        }

        return list;
    }

    public boolean updateTeamStatus(int teamId, ResponseTeam.TeamStatus status) {
        try (PreparedStatement s = connection.prepareStatement("UPDATE response_teams SET status=? WHERE team_id=?")) {
            s.setString(1, status.name());
            s.setInt(2, teamId);
            return s.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[TeamDAO] UpdateStatus: " + e.getMessage());
        }

        return false;
    }

    public boolean updateTeam(ResponseTeam t) {
        String sql = "UPDATE response_teams SET team_name=?,department=?,team_leader=?,contact_number=?,status=?,capacity=?,current_members=? WHERE team_id=?";

        try (PreparedStatement s = connection.prepareStatement(sql)) {
            s.setString(1, t.getTeamName());
            s.setString(2, t.getDepartment().name());
            s.setString(3, t.getTeamLeader());
            s.setString(4, t.getContactNumber());
            s.setString(5, t.getStatus().name());
            s.setInt(6, t.getCapacity());
            s.setInt(7, t.getCurrentMembers());
            s.setInt(8, t.getTeamId());

            return s.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[TeamDAO] Update: " + e.getMessage());
        }

        return false;
    }

    public boolean assignTeamToDisaster(TeamAssignment a) {
        String sql = "INSERT INTO disaster_team_assignments(disaster_id,team_id,assigned_by,notes,status) VALUES(?,?,?,?,?)";

        try (PreparedStatement s = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            s.setInt(1, a.getDisasterId());
            s.setInt(2, a.getTeamId());
            s.setInt(3, a.getAssignedBy());
            s.setString(4, a.getNotes());
            s.setString(5, "ACTIVE");

            if (s.executeUpdate() > 0) {
                ResultSet k = s.getGeneratedKeys();
                if (k.next()) a.setAssignmentId(k.getInt(1));

                updateTeamStatus(a.getTeamId(), ResponseTeam.TeamStatus.DEPLOYED);
                updateDisasterStatusAndCoordinator(a.getDisasterId(), "RESPONDING", a.getAssignedBy());

                return true;
            }
        } catch (SQLException e) {
            System.err.println("[TeamDAO] Assign: " + e.getMessage());
        }

        return false;
    }

    public List<TeamAssignment> getAssignmentsByDisaster(int disasterId) {
        List<TeamAssignment> list = new ArrayList<>();

        String sql = "SELECT dta.*, rt.team_name, d.title AS disaster_title "
                + "FROM disaster_team_assignments dta "
                + "JOIN response_teams rt ON dta.team_id = rt.team_id "
                + "JOIN disasters d ON dta.disaster_id = d.disaster_id "
                + "WHERE dta.disaster_id = ? "
                + "ORDER BY dta.assigned_at DESC";

        try (PreparedStatement s = connection.prepareStatement(sql)) {
            s.setInt(1, disasterId);
            ResultSet rs = s.executeQuery();

            while (rs.next()) {
                TeamAssignment ta = mapAssignment(rs);
                ta.setTeamName(rs.getString("team_name"));
                ta.setDisasterTitle(rs.getString("disaster_title"));
                list.add(ta);
            }

        } catch (SQLException e) {
            System.err.println("[TeamDAO] GetByDisaster: " + e.getMessage());
        }

        return list;
    }

    public boolean completeAssignment(int assignmentId) {
        String sql = "UPDATE disaster_team_assignments SET status='COMPLETED' WHERE assignment_id=?";

        try (PreparedStatement s = connection.prepareStatement(sql)) {
            s.setInt(1, assignmentId);

            if (s.executeUpdate() > 0) {
                int teamId = 0;
                int disasterId = 0;

                try (PreparedStatement fs = connection.prepareStatement(
                        "SELECT team_id, disaster_id FROM disaster_team_assignments WHERE assignment_id=?")) {
                    fs.setInt(1, assignmentId);

                    try (ResultSet rs = fs.executeQuery()) {
                        if (rs.next()) {
                            teamId = rs.getInt("team_id");
                            disasterId = rs.getInt("disaster_id");
                        }
                    }
                }

                if (teamId > 0) {
                    updateTeamStatus(teamId, ResponseTeam.TeamStatus.AVAILABLE);
                }

                if (disasterId > 0 && !hasActiveAssignments(disasterId)) {
                    updateDisasterStatus(disasterId, "RESOLVED");
                }

                return true;
            }
        } catch (SQLException e) {
            System.err.println("[TeamDAO] Complete: " + e.getMessage());
        }

        return false;
    }

    private boolean hasActiveAssignments(int disasterId) {
        String sql = "SELECT COUNT(*) FROM disaster_team_assignments "
                + "WHERE disaster_id=? AND status <> 'COMPLETED'";

        try (PreparedStatement s = connection.prepareStatement(sql)) {
            s.setInt(1, disasterId);

            try (ResultSet rs = s.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("[TeamDAO] ActiveAssignments: " + e.getMessage());
        }

        return true;
    }

    private void updateDisasterStatus(int disasterId, String status) {
        String sql = "UPDATE disasters SET status=?, "
                + "resolved_at=CASE WHEN ?='RESOLVED' THEN NOW() ELSE resolved_at END "
                + "WHERE disaster_id=?";

        try (PreparedStatement s = connection.prepareStatement(sql)) {
            s.setString(1, status);
            s.setString(2, status);
            s.setInt(3, disasterId);
            s.executeUpdate();

        } catch (SQLException e) {
            System.err.println("[TeamDAO] UpdateDisasterStatus: " + e.getMessage());
        }
    }

    private void updateDisasterStatusAndCoordinator(int disasterId, String status, int coordinatorId) {
        String sql = "UPDATE disasters SET status=?, assigned_coordinator=?, "
                + "assessed_at=CASE WHEN ?='RESPONDING' THEN NOW() ELSE assessed_at END "
                + "WHERE disaster_id=?";

        try (PreparedStatement s = connection.prepareStatement(sql)) {
            s.setString(1, status);

            if (coordinatorId > 0) {
                s.setInt(2, coordinatorId);
            } else {
                s.setNull(2, java.sql.Types.INTEGER);
            }

            s.setString(3, status);
            s.setInt(4, disasterId);
            s.executeUpdate();

        } catch (SQLException e) {
            System.err.println("[TeamDAO] UpdateDisasterStatusAndCoordinator: " + e.getMessage());
        }
    }

    private ResponseTeam mapTeam(ResultSet rs) throws SQLException {
        ResponseTeam t = new ResponseTeam();

        t.setTeamId(rs.getInt("team_id"));
        t.setTeamName(rs.getString("team_name"));
        t.setDepartment(ResponseTeam.Department.valueOf(rs.getString("department")));
        t.setTeamLeader(rs.getString("team_leader"));
        t.setContactNumber(rs.getString("contact_number"));
        t.setStatus(ResponseTeam.TeamStatus.valueOf(rs.getString("status")));
        t.setCapacity(rs.getInt("capacity"));
        t.setCurrentMembers(rs.getInt("current_members"));

        Timestamp ca = rs.getTimestamp("created_at");
        if (ca != null) t.setCreatedAt(ca.toLocalDateTime());

        return t;
    }

    private TeamAssignment mapAssignment(ResultSet rs) throws SQLException {
        TeamAssignment ta = new TeamAssignment();

        ta.setAssignmentId(rs.getInt("assignment_id"));
        ta.setDisasterId(rs.getInt("disaster_id"));
        ta.setTeamId(rs.getInt("team_id"));

        Timestamp at = rs.getTimestamp("assigned_at");
        if (at != null) ta.setAssignedAt(at.toLocalDateTime());

        ta.setAssignedBy(rs.getInt("assigned_by"));
        ta.setStatus(TeamAssignment.AssignmentStatus.valueOf(rs.getString("status")));
        ta.setNotes(rs.getString("notes"));

        return ta;
    }
}