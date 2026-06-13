package model;

import java.time.LocalDateTime;

/**
 * Model class representing a system user.
 * Supports role-based access control.
 */
public class User {

    public enum Role {
        ADMIN, COORDINATOR, RESPONDER, PUBLIC
    }

    private int userId;
    private String username;
    private String passwordHash;
    private Role role;
    private String fullName;
    private String email;
    private String phone;
    private String department;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
    private boolean isActive;

    public User() {
    }

    public User(int userId, String username, String passwordHash, Role role,
            String fullName, String email, String phone, String department,
            LocalDateTime createdAt, LocalDateTime lastLogin, boolean isActive) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.department = department;
        this.createdAt = createdAt;
        this.lastLogin = lastLogin;
        this.isActive = isActive;
    }

    // Getters
    public int getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public Role getRole() { return role; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getDepartment() { return department; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getLastLogin() { return lastLogin; }
    public boolean isActive() { return isActive; }

    // Setters
    public void setUserId(int userId) { this.userId = userId; }
    public void setUsername(String username) { this.username = username; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public void setRole(Role role) { this.role = role; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setDepartment(String department) { this.department = department; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }
    public void setActive(boolean active) { isActive = active; }

    public boolean hasPermission(String action) {
        switch (action) {
            case "MANAGE_USERS":   return role == Role.ADMIN;
            case "ASSIGN_TEAMS":   return role == Role.ADMIN || role == Role.COORDINATOR;
            case "MANAGE_RESOURCES": return role == Role.ADMIN || role == Role.COORDINATOR;
            case "ASSESS_DISASTER":  return role == Role.ADMIN || role == Role.COORDINATOR;
            case "REPORT_DISASTER":  return true;
            case "VIEW_DISASTERS":   return true;
            default: return false;
        }
    }

    @Override
    public String toString() {
        return "User{"
                + "userId=" + userId
                + ", username='" + username + '\''
                + ", role=" + role
                + ", fullName='" + fullName + '\''
                + ", email='" + email + '\''
                + ", department='" + department + '\''
                + ", isActive=" + isActive
                + '}';
    }
}
