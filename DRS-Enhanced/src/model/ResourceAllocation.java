package model;

import java.time.LocalDateTime;

/**
 * Model class for resource allocation to disaster events.
 * Feature 2: Resource Management
 */
public class ResourceAllocation {

    public enum AllocationStatus {
        ACTIVE, RETURNED, LOST
    }

    private int allocationId;
    private int disasterId;
    private int resourceId;
    private int quantityAllocated;
    private LocalDateTime allocatedAt;
    private int allocatedBy;
    private LocalDateTime returnedAt;
    private AllocationStatus status;
    // Joined display fields
    private String resourceName;
    private String disasterTitle;

    public ResourceAllocation() {
    }

    public ResourceAllocation(int allocationId, int disasterId, int resourceId,
            int quantityAllocated, LocalDateTime allocatedAt,
            int allocatedBy, LocalDateTime returnedAt,
            AllocationStatus status) {
        this.allocationId = allocationId;
        this.disasterId = disasterId;
        this.resourceId = resourceId;
        this.quantityAllocated = quantityAllocated;
        this.allocatedAt = allocatedAt;
        this.allocatedBy = allocatedBy;
        this.returnedAt = returnedAt;
        this.status = status;
    }

    // Getters
    public int getAllocationId() { return allocationId; }
    public int getDisasterId() { return disasterId; }
    public int getResourceId() { return resourceId; }
    public int getQuantityAllocated() { return quantityAllocated; }
    public LocalDateTime getAllocatedAt() { return allocatedAt; }
    public int getAllocatedBy() { return allocatedBy; }
    public LocalDateTime getReturnedAt() { return returnedAt; }
    public AllocationStatus getStatus() { return status; }
    public String getResourceName() { return resourceName; }
    public String getDisasterTitle() { return disasterTitle; }

    // Setters
    public void setAllocationId(int allocationId) { this.allocationId = allocationId; }
    public void setDisasterId(int disasterId) { this.disasterId = disasterId; }
    public void setResourceId(int resourceId) { this.resourceId = resourceId; }
    public void setQuantityAllocated(int quantityAllocated) { this.quantityAllocated = quantityAllocated; }
    public void setAllocatedAt(LocalDateTime allocatedAt) { this.allocatedAt = allocatedAt; }
    public void setAllocatedBy(int allocatedBy) { this.allocatedBy = allocatedBy; }
    public void setReturnedAt(LocalDateTime returnedAt) { this.returnedAt = returnedAt; }
    public void setStatus(AllocationStatus status) { this.status = status; }
    public void setResourceName(String resourceName) { this.resourceName = resourceName; }
    public void setDisasterTitle(String disasterTitle) { this.disasterTitle = disasterTitle; }

    @Override
    public String toString() {
        return "ResourceAllocation{"
                + "allocationId=" + allocationId
                + ", disasterId=" + disasterId
                + ", resourceId=" + resourceId
                + ", quantityAllocated=" + quantityAllocated
                + ", status=" + status
                + '}';
    }
}
