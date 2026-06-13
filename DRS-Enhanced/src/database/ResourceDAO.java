package database;

import model.Resource;
import model.ResourceAllocation;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ResourceDAO {

    private final Connection connection;

    public ResourceDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    public boolean createResource(Resource r) {
        String sql = "INSERT INTO resources(resource_name, resource_type, quantity, unit, location, status, department) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement s = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            s.setString(1, r.getResourceName());
            s.setString(2, r.getResourceType().name());
            s.setInt(3, r.getQuantity());
            s.setString(4, r.getUnit());
            s.setString(5, r.getLocation());
            s.setString(6, r.getStatus().name());

            Resource.Department department =
                    r.getDepartment() != null ? r.getDepartment() : Resource.Department.OTHER;

            s.setString(7, department.name());

            int rows = s.executeUpdate();

            if (rows > 0) {
                ResultSet keys = s.getGeneratedKeys();
                if (keys.next()) {
                    r.setResourceId(keys.getInt(1));
                }
                return true;
            }

        } catch (SQLException e) {
            System.err.println("[ResourceDAO] Create: " + e.getMessage());
        }

        return false;
    }

    public List<Resource> getAllResources() {
        List<Resource> list = new ArrayList<>();

        String sql = "SELECT * FROM resources ORDER BY resource_id";

        try (Statement s = connection.createStatement();
             ResultSet rs = s.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapResource(rs));
            }

        } catch (SQLException e) {
            System.err.println("[ResourceDAO] GetAll: " + e.getMessage());
        }

        return list;
    }

    public Resource getResourceById(int resourceId) {
        String sql = "SELECT * FROM resources WHERE resource_id = ?";

        try (PreparedStatement s = connection.prepareStatement(sql)) {
            s.setInt(1, resourceId);

            ResultSet rs = s.executeQuery();

            if (rs.next()) {
                return mapResource(rs);
            }

        } catch (SQLException e) {
            System.err.println("[ResourceDAO] GetById: " + e.getMessage());
        }

        return null;
    }

    public boolean updateResource(Resource r) {
        String sql = "UPDATE resources SET resource_name=?, resource_type=?, quantity=?, unit=?, "
                + "location=?, status=?, department=? WHERE resource_id=?";

        try (PreparedStatement s = connection.prepareStatement(sql)) {
            s.setString(1, r.getResourceName());
            s.setString(2, r.getResourceType().name());
            s.setInt(3, r.getQuantity());
            s.setString(4, r.getUnit());
            s.setString(5, r.getLocation());
            s.setString(6, r.getStatus().name());

            Resource.Department department =
                    r.getDepartment() != null ? r.getDepartment() : Resource.Department.OTHER;

            s.setString(7, department.name());
            s.setInt(8, r.getResourceId());

            return s.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[ResourceDAO] Update: " + e.getMessage());
        }

        return false;
    }

    public boolean allocateResource(ResourceAllocation allocation) {
        String checkSql = "SELECT quantity FROM resources WHERE resource_id = ?";
        String insertSql = "INSERT INTO resource_allocations(disaster_id, resource_id, quantity_allocated, allocated_by, status) "
                + "VALUES (?, ?, ?, ?, 'ACTIVE')";
        String updateResourceSql = "UPDATE resources SET quantity = quantity - ?, "
                + "status = CASE "
                + "WHEN quantity - ? <= 0 THEN 'DEPLETED' "
                + "ELSE 'AVAILABLE' "
                + "END "
                + "WHERE resource_id = ?";

        try {
            connection.setAutoCommit(false);

            int availableQuantity;

            try (PreparedStatement check = connection.prepareStatement(checkSql)) {
                check.setInt(1, allocation.getResourceId());

                try (ResultSet rs = check.executeQuery()) {
                    if (!rs.next()) {
                        connection.rollback();
                        return false;
                    }

                    availableQuantity = rs.getInt("quantity");
                }
            }

            if (allocation.getQuantityAllocated() <= 0 || allocation.getQuantityAllocated() > availableQuantity) {
                connection.rollback();
                return false;
            }

            try (PreparedStatement insert = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                insert.setInt(1, allocation.getDisasterId());
                insert.setInt(2, allocation.getResourceId());
                insert.setInt(3, allocation.getQuantityAllocated());
                insert.setInt(4, allocation.getAllocatedBy());

                if (insert.executeUpdate() <= 0) {
                    connection.rollback();
                    return false;
                }

                ResultSet keys = insert.getGeneratedKeys();
                if (keys.next()) {
                    allocation.setAllocationId(keys.getInt(1));
                }
            }

            try (PreparedStatement update = connection.prepareStatement(updateResourceSql)) {
                update.setInt(1, allocation.getQuantityAllocated());
                update.setInt(2, allocation.getQuantityAllocated());
                update.setInt(3, allocation.getResourceId());

                if (update.executeUpdate() <= 0) {
                    connection.rollback();
                    return false;
                }
            }

            connection.commit();
            return true;

        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackError) {
                System.err.println("[ResourceDAO] Allocate Rollback: " + rollbackError.getMessage());
            }

            System.err.println("[ResourceDAO] Allocate: " + e.getMessage());
            return false;

        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("[ResourceDAO] AutoCommit: " + e.getMessage());
            }
        }
    }

    public List<ResourceAllocation> getAllocationsByDisaster(int disasterId) {
        List<ResourceAllocation> list = new ArrayList<>();

        String sql = "SELECT ra.*, r.resource_name, d.title AS disaster_title "
                + "FROM resource_allocations ra "
                + "JOIN resources r ON ra.resource_id = r.resource_id "
                + "JOIN disasters d ON ra.disaster_id = d.disaster_id "
                + "WHERE ra.disaster_id = ? "
                + "ORDER BY ra.allocated_at DESC";

        try (PreparedStatement s = connection.prepareStatement(sql)) {
            s.setInt(1, disasterId);

            ResultSet rs = s.executeQuery();

            while (rs.next()) {
                ResourceAllocation allocation = mapAllocation(rs);
                allocation.setResourceName(rs.getString("resource_name"));
                allocation.setDisasterTitle(rs.getString("disaster_title"));
                list.add(allocation);
            }

        } catch (SQLException e) {
            System.err.println("[ResourceDAO] GetAllocationsByDisaster: " + e.getMessage());
        }

        return list;
    }

    public boolean returnAllocation(int allocationId) {
        String findSql = "SELECT resource_id, quantity_allocated, status "
                + "FROM resource_allocations WHERE allocation_id = ?";
        String returnSql = "UPDATE resource_allocations SET status = 'RETURNED', returned_at = NOW() "
                + "WHERE allocation_id = ? AND status = 'ACTIVE'";
        String updateResourceSql = "UPDATE resources SET quantity = quantity + ?, status = 'AVAILABLE' "
                + "WHERE resource_id = ?";

        try {
            connection.setAutoCommit(false);

            int resourceId;
            int quantityAllocated;
            String status;

            try (PreparedStatement find = connection.prepareStatement(findSql)) {
                find.setInt(1, allocationId);

                try (ResultSet rs = find.executeQuery()) {
                    if (!rs.next()) {
                        connection.rollback();
                        return false;
                    }

                    resourceId = rs.getInt("resource_id");
                    quantityAllocated = rs.getInt("quantity_allocated");
                    status = rs.getString("status");
                }
            }

            if (!"ACTIVE".equalsIgnoreCase(status)) {
                connection.rollback();
                return false;
            }

            try (PreparedStatement ret = connection.prepareStatement(returnSql)) {
                ret.setInt(1, allocationId);

                if (ret.executeUpdate() <= 0) {
                    connection.rollback();
                    return false;
                }
            }

            try (PreparedStatement update = connection.prepareStatement(updateResourceSql)) {
                update.setInt(1, quantityAllocated);
                update.setInt(2, resourceId);

                if (update.executeUpdate() <= 0) {
                    connection.rollback();
                    return false;
                }
            }

            connection.commit();
            return true;

        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackError) {
                System.err.println("[ResourceDAO] Return Rollback: " + rollbackError.getMessage());
            }

            System.err.println("[ResourceDAO] Return: " + e.getMessage());
            return false;

        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("[ResourceDAO] AutoCommit: " + e.getMessage());
            }
        }
    }

    private Resource mapResource(ResultSet rs) throws SQLException {
        Resource r = new Resource();

        r.setResourceId(rs.getInt("resource_id"));
        r.setResourceName(rs.getString("resource_name"));
        r.setResourceType(Resource.ResourceType.valueOf(rs.getString("resource_type")));
        r.setQuantity(rs.getInt("quantity"));
        r.setUnit(rs.getString("unit"));
        r.setLocation(rs.getString("location"));
        r.setStatus(Resource.ResourceStatus.valueOf(rs.getString("status")));

        String dept = rs.getString("department");

        try {
            if (dept != null && !dept.trim().isEmpty()) {
                r.setDepartment(Resource.Department.valueOf(dept));
            } else {
                r.setDepartment(Resource.Department.OTHER);
            }
        } catch (Exception e) {
            r.setDepartment(Resource.Department.OTHER);
        }

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            r.setCreatedAt(createdAt.toLocalDateTime());
        }

        return r;
    }

    private ResourceAllocation mapAllocation(ResultSet rs) throws SQLException {
        ResourceAllocation a = new ResourceAllocation();

        a.setAllocationId(rs.getInt("allocation_id"));
        a.setDisasterId(rs.getInt("disaster_id"));
        a.setResourceId(rs.getInt("resource_id"));
        a.setQuantityAllocated(rs.getInt("quantity_allocated"));
        a.setAllocatedBy(rs.getInt("allocated_by"));
        a.setStatus(ResourceAllocation.AllocationStatus.valueOf(rs.getString("status")));

        Timestamp allocatedAt = rs.getTimestamp("allocated_at");
        if (allocatedAt != null) {
            a.setAllocatedAt(allocatedAt.toLocalDateTime());
        }

        Timestamp returnedAt = rs.getTimestamp("returned_at");
        if (returnedAt != null) {
            a.setReturnedAt(returnedAt.toLocalDateTime());
        }

        return a;
    }

    public List<Resource> getAvailableResources() {
        List<Resource> list = new ArrayList<>();

        String sql = "SELECT * FROM resources WHERE status='AVAILABLE' AND quantity>0 ORDER BY resource_id";

        try (Statement s = connection.createStatement();
             ResultSet rs = s.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapResource(rs));
            }

        } catch (SQLException e) {
            System.err.println("[ResourceDAO] GetAvail: " + e.getMessage());
        }

        return list;
    }
}