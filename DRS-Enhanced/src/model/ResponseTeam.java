package model;

import java.time.LocalDateTime;

/**
 * Model class representing a disaster response team.
 * Feature 1: Team Coordination
 */
public class ResponseTeam {

    public enum Department {
        FIRE_EMERGENCY, HOSPITAL, ELECTRICITY, TRANSPORTATION,
        WASTE_MANAGEMENT, WATER_SUPPLY, SCHOOLS, LAW_ENFORCEMENT, OTHER
    }

    public enum TeamStatus {
        AVAILABLE, DEPLOYED, STANDBY, UNAVAILABLE
    }

    private int teamId;
    private String teamName;
    private Department department;
    private String teamLeader;
    private String contactNumber;
    private TeamStatus status;
    private int capacity;
    private int currentMembers;
    private LocalDateTime createdAt;

    public ResponseTeam() {
    }

    public ResponseTeam(int teamId, String teamName, Department department, String teamLeader,
            String contactNumber, TeamStatus status, int capacity,
            int currentMembers, LocalDateTime createdAt) {
        this.teamId = teamId;
        this.teamName = teamName;
        this.department = department;
        this.teamLeader = teamLeader;
        this.contactNumber = contactNumber;
        this.status = status;
        this.capacity = capacity;
        this.currentMembers = currentMembers;
        this.createdAt = createdAt;
    }

    // Getters
    public int getTeamId() { return teamId; }
    public String getTeamName() { return teamName; }
    public Department getDepartment() { return department; }
    public String getTeamLeader() { return teamLeader; }
    public String getContactNumber() { return contactNumber; }
    public TeamStatus getStatus() { return status; }
    public int getCapacity() { return capacity; }
    public int getCurrentMembers() { return currentMembers; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // Setters
    public void setTeamId(int teamId) { this.teamId = teamId; }
    public void setTeamName(String teamName) { this.teamName = teamName; }
    public void setDepartment(Department department) { this.department = department; }
    public void setTeamLeader(String teamLeader) { this.teamLeader = teamLeader; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }
    public void setStatus(TeamStatus status) { this.status = status; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public void setCurrentMembers(int currentMembers) { this.currentMembers = currentMembers; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isAvailable() {
        return status == TeamStatus.AVAILABLE;
    }

    @Override
    public String toString() {
        return "ResponseTeam{"
                + "teamId=" + teamId
                + ", teamName='" + teamName + '\''
                + ", department=" + department
                + ", teamLeader='" + teamLeader + '\''
                + ", status=" + status
                + ", currentMembers=" + currentMembers + "/" + capacity
                + '}';
    }
}
