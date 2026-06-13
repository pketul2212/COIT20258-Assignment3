package model;

import java.time.LocalDateTime;

/**
 * Model class for disaster-team assignment records.
 * Feature 1: Team Coordination
 */
public class TeamAssignment {

    public enum AssignmentStatus {
        ACTIVE, COMPLETED, CANCELLED
    }

    private int assignmentId;
    private int disasterId;
    private int teamId;
    private LocalDateTime assignedAt;
    private int assignedBy;
    private AssignmentStatus status;
    private String notes;
    // Joined display fields
    private String teamName;
    private String disasterTitle;

    public TeamAssignment() {
    }

    public TeamAssignment(int assignmentId, int disasterId, int teamId,
            LocalDateTime assignedAt, int assignedBy,
            AssignmentStatus status, String notes) {
        this.assignmentId = assignmentId;
        this.disasterId = disasterId;
        this.teamId = teamId;
        this.assignedAt = assignedAt;
        this.assignedBy = assignedBy;
        this.status = status;
        this.notes = notes;
    }

    // Getters
    public int getAssignmentId() { return assignmentId; }
    public int getDisasterId() { return disasterId; }
    public int getTeamId() { return teamId; }
    public LocalDateTime getAssignedAt() { return assignedAt; }
    public int getAssignedBy() { return assignedBy; }
    public AssignmentStatus getStatus() { return status; }
    public String getNotes() { return notes; }
    public String getTeamName() { return teamName; }
    public String getDisasterTitle() { return disasterTitle; }

    // Setters
    public void setAssignmentId(int assignmentId) { this.assignmentId = assignmentId; }
    public void setDisasterId(int disasterId) { this.disasterId = disasterId; }
    public void setTeamId(int teamId) { this.teamId = teamId; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }
    public void setAssignedBy(int assignedBy) { this.assignedBy = assignedBy; }
    public void setStatus(AssignmentStatus status) { this.status = status; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setTeamName(String teamName) { this.teamName = teamName; }
    public void setDisasterTitle(String disasterTitle) { this.disasterTitle = disasterTitle; }

    @Override
    public String toString() {
        return "TeamAssignment{"
                + "assignmentId=" + assignmentId
                + ", disasterId=" + disasterId
                + ", teamId=" + teamId
                + ", status=" + status
                + ", assignedAt=" + assignedAt
                + '}';
    }
}
