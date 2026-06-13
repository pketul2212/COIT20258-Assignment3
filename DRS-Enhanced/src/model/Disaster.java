package model;

import java.time.LocalDateTime;

/**
 * Model class representing a disaster event in the DRS system.
 */
public class Disaster {

    public enum DisasterType {
        HURRICANE, FIRE, EARTHQUAKE, FLOOD, TORNADO, TSUNAMI, OTHER
    }

    public enum Severity {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    public enum Status {
        REPORTED, ASSESSED, RESPONDING, RESOLVED, CLOSED
    }

    private int disasterId;
    private DisasterType disasterType;
    private String title;
    private String description;
    private String location;
    private double latitude;
    private double longitude;
    private Severity severity;
    private Status status;
    private int reportedBy;
    private int assignedCoordinator;
    private LocalDateTime reportedAt;
    private LocalDateTime assessedAt;
    private LocalDateTime resolvedAt;
    private int estimatedAffected;
    private boolean isEncrypted;

    public Disaster() {
    }

    public Disaster(int disasterId, DisasterType disasterType, String title, String description,
            String location, double latitude, double longitude, Severity severity,
            Status status, int reportedBy, int assignedCoordinator,
            LocalDateTime reportedAt, LocalDateTime assessedAt, LocalDateTime resolvedAt,
            int estimatedAffected, boolean isEncrypted) {
        this.disasterId = disasterId;
        this.disasterType = disasterType;
        this.title = title;
        this.description = description;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
        this.severity = severity;
        this.status = status;
        this.reportedBy = reportedBy;
        this.assignedCoordinator = assignedCoordinator;
        this.reportedAt = reportedAt;
        this.assessedAt = assessedAt;
        this.resolvedAt = resolvedAt;
        this.estimatedAffected = estimatedAffected;
        this.isEncrypted = isEncrypted;
    }

    // Getters
    public int getDisasterId() { return disasterId; }
    public DisasterType getDisasterType() { return disasterType; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getLocation() { return location; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public Severity getSeverity() { return severity; }
    public Status getStatus() { return status; }
    public int getReportedBy() { return reportedBy; }
    public int getAssignedCoordinator() { return assignedCoordinator; }
    public LocalDateTime getReportedAt() { return reportedAt; }
    public LocalDateTime getAssessedAt() { return assessedAt; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public int getEstimatedAffected() { return estimatedAffected; }
    public boolean isEncrypted() { return isEncrypted; }

    // Setters
    public void setDisasterId(int disasterId) { this.disasterId = disasterId; }
    public void setDisasterType(DisasterType disasterType) { this.disasterType = disasterType; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setLocation(String location) { this.location = location; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public void setSeverity(Severity severity) { this.severity = severity; }
    public void setStatus(Status status) { this.status = status; }
    public void setReportedBy(int reportedBy) { this.reportedBy = reportedBy; }
    public void setAssignedCoordinator(int assignedCoordinator) { this.assignedCoordinator = assignedCoordinator; }
    public void setReportedAt(LocalDateTime reportedAt) { this.reportedAt = reportedAt; }
    public void setAssessedAt(LocalDateTime assessedAt) { this.assessedAt = assessedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
    public void setEstimatedAffected(int estimatedAffected) { this.estimatedAffected = estimatedAffected; }
    public void setEncrypted(boolean encrypted) { isEncrypted = encrypted; }

    /**
     * Calculates a numeric priority score for ranking disasters.
     * Higher score = needs faster response.
     */
    public int getPriorityScore() {
        int score = 0;
        // Severity contribution
        switch (severity) {
            case CRITICAL: score += 40; break;
            case HIGH:     score += 30; break;
            case MEDIUM:   score += 20; break;
            case LOW:      score += 10; break;
        }
        // Type contribution
        switch (disasterType) {
            case EARTHQUAKE: score += 30; break;
            case TSUNAMI:    score += 30; break;
            case HURRICANE:  score += 25; break;
            case TORNADO:    score += 20; break;
            case FIRE:       score += 20; break;
            case FLOOD:      score += 15; break;
            default:         score += 10; break;
        }
        // People affected contribution
        if (estimatedAffected > 10000)      score += 30;
        else if (estimatedAffected > 1000)  score += 20;
        else if (estimatedAffected > 100)   score += 10;
        return score;
    }

    @Override
    public String toString() {
        return "Disaster{"
                + "disasterId=" + disasterId
                + ", disasterType=" + disasterType
                + ", title='" + title + '\''
                + ", location='" + location + '\''
                + ", severity=" + severity
                + ", status=" + status
                + ", estimatedAffected=" + estimatedAffected
                + ", reportedAt=" + reportedAt
                + '}';
    }
}
