package model;

import java.time.LocalDateTime;

/**
 * Model class representing a resource for disaster response.
 * Feature 2: Resource Management
 */
public class Resource {

    public enum ResourceType {
        VEHICLE, MEDICAL_SUPPLY, FOOD_WATER, EQUIPMENT, PERSONNEL, SHELTER, OTHER
    }

    public enum ResourceStatus {
        AVAILABLE, IN_USE, DEPLETED, MAINTENANCE
    }

    public enum Department {
        FIRE_EMERGENCY, HOSPITAL, ELECTRICITY, TRANSPORTATION,
        WASTE_MANAGEMENT, WATER_SUPPLY, SCHOOLS, LAW_ENFORCEMENT, OTHER
    }

    private int resourceId;
    private String resourceName;
    private ResourceType resourceType;
    private int quantity;
    private String unit;
    private String location;
    private ResourceStatus status;
    private Department department;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Resource() {
    }

    public Resource(int resourceId, String resourceName, ResourceType resourceType,
            int quantity, String unit, String location, ResourceStatus status,
            Department department, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.resourceId = resourceId;
        this.resourceName = resourceName;
        this.resourceType = resourceType;
        this.quantity = quantity;
        this.unit = unit;
        this.location = location;
        this.status = status;
        this.department = department;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters
    public int getResourceId() { return resourceId; }
    public String getResourceName() { return resourceName; }
    public ResourceType getResourceType() { return resourceType; }
    public int getQuantity() { return quantity; }
    public String getUnit() { return unit; }
    public String getLocation() { return location; }
    public ResourceStatus getStatus() { return status; }
    public Department getDepartment() { return department; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // Setters
    public void setResourceId(int resourceId) { this.resourceId = resourceId; }
    public void setResourceName(String resourceName) { this.resourceName = resourceName; }
    public void setResourceType(ResourceType resourceType) { this.resourceType = resourceType; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setUnit(String unit) { this.unit = unit; }
    public void setLocation(String location) { this.location = location; }
    public void setStatus(ResourceStatus status) { this.status = status; }
    public void setDepartment(Department department) { this.department = department; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "Resource{"
                + "resourceId=" + resourceId
                + ", resourceName='" + resourceName + '\''
                + ", resourceType=" + resourceType
                + ", quantity=" + quantity + " " + unit
                + ", status=" + status
                + ", location='" + location + '\''
                + '}';
    }
}
