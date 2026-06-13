package server;

import database.*;
import model.*;
import util.Protocol;
import util.SecurityUtil;

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;

/**
 * Handles an individual client connection in its own thread.
 *
 * FIXES ADDED:
 * 1. Server-side permission checks.
 * 2. Safer invalid input handling for numbers/enums.
 * 3. Safer output text to prevent protocol delimiter breaking GUI parsing.
 * 4. Graceful error response instead of crashing server thread.
 */
public class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private BufferedReader reader;
    private PrintWriter writer;
    private User currentUser;

    private final DisasterDAO disasterDAO;
    private final UserDAO userDAO;
    private final ResponseTeamDAO teamDAO;
    private final ResourceDAO resourceDAO;
    private final AuditDAO auditDAO;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.disasterDAO = new DisasterDAO();
        this.userDAO = new UserDAO();
        this.teamDAO = new ResponseTeamDAO();
        this.resourceDAO = new ResourceDAO();
        this.auditDAO = new AuditDAO();
    }

    @Override
    public void run() {
        try {
            reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            writer = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()), true);

            System.out.println("[Server] Client connected: " + clientSocket.getInetAddress());

            String request;
            while ((request = reader.readLine()) != null) {
                writer.println(processRequest(request));
            }
        } catch (IOException e) {
            System.out.println("[Server] Client disconnected: " + clientSocket.getInetAddress());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException ignored) {
            }
        }
    }

    private String processRequest(String request) {
        try {
            if (request == null || request.trim().isEmpty()) {
                return fail("Empty request");
            }

            String[] parts = request.split("\\" + Protocol.DELIMITER, 2);
            String cmd = parts[0].trim();
            String data = parts.length > 1 ? parts[1] : "";

            System.out.println("[Server] CMD: " + cmd);

            if (!isAllowed(cmd)) {
                return fail("Access denied. You do not have permission to perform this action.");
            }

            switch (cmd) {
                case Protocol.LOGIN: return handleLogin(data);
                case Protocol.REGISTER: return handleRegister(data);

                case Protocol.GET_ALL_DISASTERS: return handleGetAllDisasters();
                case Protocol.GET_PRIORITIZED_DISASTERS: return handleGetPrioritizedDisasters();
                case Protocol.REPORT_DISASTER: return handleReportDisaster(data);
                case Protocol.UPDATE_DISASTER: return handleUpdateDisaster(data);
                case Protocol.UPDATE_DISASTER_STATUS: return handleUpdateDisasterStatus(data);

                case Protocol.GET_ALL_TEAMS: return handleGetAllTeams();
                case Protocol.GET_AVAILABLE_TEAMS: return handleGetAvailableTeams();
                case Protocol.CREATE_TEAM: return handleCreateTeam(data);
                case Protocol.UPDATE_TEAM: return handleUpdateTeam(data);
                case Protocol.ASSIGN_TEAM: return handleAssignTeam(data);
                case Protocol.GET_TEAM_ASSIGNMENTS: return handleGetTeamAssignments(data);
                case Protocol.COMPLETE_ASSIGNMENT: return handleCompleteAssignment(data);

                case Protocol.GET_ALL_RESOURCES: return handleGetAllResources();
                case Protocol.GET_AVAILABLE_RESOURCES: return handleGetAvailableResources();
                case Protocol.CREATE_RESOURCE: return handleCreateResource(data);
                case Protocol.UPDATE_RESOURCE: return handleUpdateResource(data);
                case Protocol.ALLOCATE_RESOURCE: return handleAllocateResource(data);
                case Protocol.GET_RESOURCE_ALLOCATIONS: return handleGetResourceAllocations(data);
                case Protocol.RETURN_ALLOCATION: return handleReturnAllocation(data);

                case Protocol.GET_ALL_USERS: return handleGetAllUsers();
                case Protocol.UPDATE_USER: return handleUpdateUser(data);
                case Protocol.GET_AUDIT_LOGS: return handleGetAuditLogs();

                default: return fail("Unknown command: " + cmd);
            }
        } catch (IllegalArgumentException e) {
            return fail("Invalid input: " + safeOutput(e.getMessage()));
        } catch (Exception e) {
            System.err.println("[Server] Request error: " + e.getMessage());
            return fail("Server error. Please check input values and try again.");
        }
    }

    private boolean isAllowed(String cmd) {
        if (Protocol.LOGIN.equals(cmd) || Protocol.REGISTER.equals(cmd)) {
            return true;
        }

        if (currentUser == null || !currentUser.isActive()) {
            return false;
        }

        User.Role role = currentUser.getRole();

        switch (cmd) {
            case Protocol.GET_ALL_DISASTERS:
            case Protocol.GET_PRIORITIZED_DISASTERS:
            case Protocol.GET_ALL_TEAMS:
            case Protocol.GET_AVAILABLE_TEAMS:
            case Protocol.GET_TEAM_ASSIGNMENTS:
            case Protocol.GET_ALL_RESOURCES:
            case Protocol.GET_AVAILABLE_RESOURCES:
            case Protocol.GET_RESOURCE_ALLOCATIONS:
                return true;

            case Protocol.REPORT_DISASTER:
                return true;

            case Protocol.UPDATE_DISASTER:
            case Protocol.UPDATE_DISASTER_STATUS:
            case Protocol.CREATE_TEAM:
            case Protocol.UPDATE_TEAM:
            case Protocol.ASSIGN_TEAM:
            case Protocol.COMPLETE_ASSIGNMENT:
            case Protocol.CREATE_RESOURCE:
            case Protocol.UPDATE_RESOURCE:
            case Protocol.ALLOCATE_RESOURCE:
            case Protocol.RETURN_ALLOCATION:
                return role == User.Role.ADMIN || role == User.Role.COORDINATOR;

            case Protocol.GET_ALL_USERS:
            case Protocol.UPDATE_USER:
            case Protocol.GET_AUDIT_LOGS:
                return role == User.Role.ADMIN;

            default:
                return false;
        }
    }

    /* ---- AUTH ---- */

    private String handleLogin(String data) {
        String[] p = data.split(Protocol.FIELD_DELIMITER, -1);
        if (p.length < 2) return fail("Username and password are required");

        String username = cleanInput(p[0]);
        String password = p[1];

        if (username.isEmpty() || password.isEmpty()) {
            return fail("Username and password are required");
        }

        User user = userDAO.authenticate(username, SecurityUtil.hashPassword(password));

        if (user != null && user.isActive()) {
            this.currentUser = user;
            auditDAO.log(user.getUserId(), "LOGIN", "users", user.getUserId(), null, null);
            return Protocol.SUCCESS + Protocol.DELIMITER + serUser(user);
        }

        return fail("Invalid credentials or inactive account");
    }

    private String handleRegister(String data) {
        String[] p = data.split(Protocol.FIELD_DELIMITER, -1);
        if (p.length < 5) return fail("Invalid registration data");

        String username = cleanInput(p[0]);
        String password = p[1];
        String fullName = cleanInput(p[2]);
        String email = cleanInput(p[3]);
        String phone = cleanInput(p[4]);

        if (username.isEmpty() || password.isEmpty() || fullName.isEmpty()) {
            return fail("Username, password and full name are required");
        }

        User u = new User();
        u.setUsername(username);
        u.setPasswordHash(SecurityUtil.hashPassword(password));
        u.setFullName(fullName);
        u.setEmail(email);
        u.setPhone(phone);
        u.setRole(User.Role.PUBLIC);
        u.setActive(true);

        if (userDAO.createUser(u)) {
            auditDAO.log(u.getUserId(), "REGISTER", "users", u.getUserId(), null, serUser(u));
            return ok("Registered successfully");
        }

        return fail("Username already exists");
    }

    /* ---- DISASTERS ---- */

    private String handleGetAllDisasters() {
        StringBuilder sb = new StringBuilder(Protocol.SUCCESS + Protocol.DELIMITER);
        for (Disaster d : disasterDAO.getAllDisasters()) {
            sb.append(serDisaster(d)).append(Protocol.LIST_DELIMITER);
        }
        return sb.toString();
    }

    private String handleGetPrioritizedDisasters() {
        StringBuilder sb = new StringBuilder(Protocol.SUCCESS + Protocol.DELIMITER);
        for (Disaster d : disasterDAO.getPrioritizedDisasters()) {
            sb.append(serDisaster(d)).append(Protocol.LIST_DELIMITER);
        }
        return sb.toString();
    }

    private String handleReportDisaster(String data) {
        String[] p = data.split(Protocol.FIELD_DELIMITER, -1);
        if (p.length < 7) return fail("Invalid disaster data");

        Disaster d = new Disaster();
        d.setDisasterType(parseEnum(Disaster.DisasterType.class, p[0], "disaster type"));
        d.setTitle(requiredText(p[1], "title"));
        d.setDescription(cleanInput(p[2]));
        d.setLocation(requiredText(p[3], "location"));
        d.setSeverity(parseEnum(Disaster.Severity.class, p[4], "severity"));
        d.setEstimatedAffected(parseNonNegativeInt(p[5], "estimated affected"));
        d.setReportedBy(parsePositiveInt(p[6], "reported by"));
        d.setStatus(Disaster.Status.REPORTED);
        d.setReportedAt(LocalDateTime.now());

        if (disasterDAO.createDisaster(d)) {
            int uid = currentUser != null ? currentUser.getUserId() : 0;
            auditDAO.log(uid, "REPORT_DISASTER", "disasters", d.getDisasterId(), null, serDisaster(d));
            return Protocol.SUCCESS + Protocol.DELIMITER + d.getDisasterId();
        }

        return fail("Failed to report disaster");
    }

    private String handleUpdateDisaster(String data) {
        String[] p = data.split(Protocol.FIELD_DELIMITER, -1);
        if (p.length < 8) return fail("Invalid disaster update data");

        Disaster d = new Disaster();
        d.setDisasterId(parsePositiveInt(p[0], "disaster id"));
        d.setDisasterType(parseEnum(Disaster.DisasterType.class, p[1], "disaster type"));
        d.setTitle(requiredText(p[2], "title"));
        d.setDescription(cleanInput(p[3]));
        d.setLocation(requiredText(p[4], "location"));
        d.setSeverity(parseEnum(Disaster.Severity.class, p[5], "severity"));
        d.setStatus(parseEnum(Disaster.Status.class, p[6], "status"));
        d.setEstimatedAffected(parseNonNegativeInt(p[7], "estimated affected"));
        d.setAssignedCoordinator(p.length > 8 && !p[8].trim().isEmpty() ? parseNonNegativeInt(p[8], "assigned coordinator") : 0);

        if (disasterDAO.updateDisaster(d)) {
            int uid = currentUser != null ? currentUser.getUserId() : 0;
            auditDAO.log(uid, "UPDATE_DISASTER", "disasters", d.getDisasterId(), null, serDisaster(d));
            return ok("Disaster updated");
        }

        return fail("Update failed");
    }

    private String handleUpdateDisasterStatus(String data) {
        String[] p = data.split(Protocol.FIELD_DELIMITER, -1);
        if (p.length < 2) return fail("Disaster id and status are required");

        int id = parsePositiveInt(p[0], "disaster id");
        Disaster.Status status = parseEnum(Disaster.Status.class, p[1], "status");

        if (disasterDAO.updateDisasterStatus(id, status)) {
            int uid = currentUser != null ? currentUser.getUserId() : 0;
            auditDAO.log(uid, "UPDATE_STATUS", "disasters", id, null, status.name());
            return ok("Status updated");
        }

        return fail("Update failed");
    }

    /* ---- TEAMS ---- */

    private String handleGetAllTeams() {
        StringBuilder sb = new StringBuilder(Protocol.SUCCESS + Protocol.DELIMITER);
        for (ResponseTeam t : teamDAO.getAllTeams()) {
            sb.append(serTeam(t)).append(Protocol.LIST_DELIMITER);
        }
        return sb.toString();
    }

    private String handleGetAvailableTeams() {
        StringBuilder sb = new StringBuilder(Protocol.SUCCESS + Protocol.DELIMITER);
        for (ResponseTeam t : teamDAO.getAvailableTeams()) {
            sb.append(serTeam(t)).append(Protocol.LIST_DELIMITER);
        }
        return sb.toString();
    }

    private String handleCreateTeam(String data) {
        String[] p = data.split(Protocol.FIELD_DELIMITER, -1);
        if (p.length < 5) return fail("Invalid team data");

        ResponseTeam t = new ResponseTeam();
        t.setTeamName(requiredText(p[0], "team name"));
        t.setDepartment(parseEnum(ResponseTeam.Department.class, p[1], "department"));
        t.setTeamLeader(cleanInput(p[2]));
        t.setContactNumber(cleanInput(p[3]));
        t.setCapacity(parseNonNegativeInt(p[4], "capacity"));
        t.setCurrentMembers(p.length > 5 && !p[5].trim().isEmpty() ? parseNonNegativeInt(p[5], "current members") : 0);
        t.setStatus(ResponseTeam.TeamStatus.AVAILABLE);

        if (teamDAO.createTeam(t)) {
            int uid = currentUser != null ? currentUser.getUserId() : 0;
            auditDAO.log(uid, "CREATE_TEAM", "response_teams", t.getTeamId(), null, serTeam(t));
            return Protocol.SUCCESS + Protocol.DELIMITER + t.getTeamId();
        }

        return fail("Failed to create team");
    }

    private String handleUpdateTeam(String data) {
        String[] p = data.split(Protocol.FIELD_DELIMITER, -1);
        if (p.length < 7) return fail("Invalid team update data");

        ResponseTeam t = new ResponseTeam();
        t.setTeamId(parsePositiveInt(p[0], "team id"));
        t.setTeamName(requiredText(p[1], "team name"));
        t.setDepartment(parseEnum(ResponseTeam.Department.class, p[2], "department"));
        t.setTeamLeader(cleanInput(p[3]));
        t.setContactNumber(cleanInput(p[4]));
        t.setStatus(parseEnum(ResponseTeam.TeamStatus.class, p[5], "team status"));
        t.setCapacity(parseNonNegativeInt(p[6], "capacity"));
        t.setCurrentMembers(p.length > 7 && !p[7].trim().isEmpty() ? parseNonNegativeInt(p[7], "current members") : 0);

        return teamDAO.updateTeam(t) ? ok("Team updated") : fail("Update failed");
    }

    private String handleAssignTeam(String data) {
        String[] p = data.split(Protocol.FIELD_DELIMITER, -1);
        if (p.length < 3) return fail("Invalid team assignment data");

        TeamAssignment a = new TeamAssignment();
        a.setDisasterId(parsePositiveInt(p[0], "disaster id"));
        a.setTeamId(parsePositiveInt(p[1], "team id"));
        a.setAssignedBy(parsePositiveInt(p[2], "assigned by"));
        a.setNotes(p.length > 3 ? cleanInput(p[3]) : "");

        if (teamDAO.assignTeamToDisaster(a)) {
            int uid = currentUser != null ? currentUser.getUserId() : 0;
            auditDAO.log(uid, "ASSIGN_TEAM", "disaster_team_assignments", a.getAssignmentId(), null, cleanInput(data));
            return Protocol.SUCCESS + Protocol.DELIMITER + a.getAssignmentId();
        }

        return fail("Assignment failed");
    }

    private String handleGetTeamAssignments(String data) {
        int disasterId = parsePositiveInt(data, "disaster id");
        StringBuilder sb = new StringBuilder(Protocol.SUCCESS + Protocol.DELIMITER);
        for (TeamAssignment ta : teamDAO.getAssignmentsByDisaster(disasterId)) {
            sb.append(serAssignment(ta)).append(Protocol.LIST_DELIMITER);
        }
        return sb.toString();
    }

    private String handleCompleteAssignment(String data) {
        int id = parsePositiveInt(data, "assignment id");
        return teamDAO.completeAssignment(id) ? ok("Assignment completed") : fail("Operation failed");
    }

    /* ---- RESOURCES ---- */

    private String handleGetAllResources() {
        StringBuilder sb = new StringBuilder(Protocol.SUCCESS + Protocol.DELIMITER);
        for (Resource r : resourceDAO.getAllResources()) {
            sb.append(serResource(r)).append(Protocol.LIST_DELIMITER);
        }
        return sb.toString();
    }

    private String handleGetAvailableResources() {
        StringBuilder sb = new StringBuilder(Protocol.SUCCESS + Protocol.DELIMITER);
        for (Resource r : resourceDAO.getAvailableResources()) {
            sb.append(serResource(r)).append(Protocol.LIST_DELIMITER);
        }
        return sb.toString();
    }

    private String handleCreateResource(String data) {
        String[] p = data.split(Protocol.FIELD_DELIMITER, -1);
        if (p.length < 5) return fail("Invalid resource data");

        Resource r = new Resource();
        r.setResourceName(requiredText(p[0], "resource name"));
        r.setResourceType(parseEnum(Resource.ResourceType.class, p[1], "resource type"));
        r.setQuantity(parseNonNegativeInt(p[2], "quantity"));
        r.setUnit(requiredText(p[3], "unit"));
        r.setLocation(requiredText(p[4], "location"));
        r.setStatus(Resource.ResourceStatus.AVAILABLE);
        r.setDepartment(parseOptionalEnum(Resource.Department.class, p.length > 5 ? p[5] : "", Resource.Department.OTHER, "department"));

        if (resourceDAO.createResource(r)) {
            int uid = currentUser != null ? currentUser.getUserId() : 0;
            auditDAO.log(uid, "CREATE_RESOURCE", "resources", r.getResourceId(), null, serResource(r));
            return Protocol.SUCCESS + Protocol.DELIMITER + r.getResourceId();
        }

        return fail("Failed to create resource");
    }

    private String handleUpdateResource(String data) {
        String[] p = data.split(Protocol.FIELD_DELIMITER, -1);
        if (p.length < 7) return fail("Invalid resource update data");

        Resource r = new Resource();
        r.setResourceId(parsePositiveInt(p[0], "resource id"));
        r.setResourceName(requiredText(p[1], "resource name"));
        r.setResourceType(parseEnum(Resource.ResourceType.class, p[2], "resource type"));
        r.setQuantity(parseNonNegativeInt(p[3], "quantity"));
        r.setUnit(requiredText(p[4], "unit"));
        r.setLocation(requiredText(p[5], "location"));
        r.setStatus(parseEnum(Resource.ResourceStatus.class, p[6], "resource status"));
        r.setDepartment(parseOptionalEnum(Resource.Department.class, p.length > 7 ? p[7] : "", Resource.Department.OTHER, "department"));

        return resourceDAO.updateResource(r) ? ok("Resource updated") : fail("Update failed");
    }

    private String handleAllocateResource(String data) {
        String[] p = data.split(Protocol.FIELD_DELIMITER, -1);
        if (p.length < 4) return fail("Invalid allocation data");

        ResourceAllocation a = new ResourceAllocation();
        a.setDisasterId(parsePositiveInt(p[0], "disaster id"));
        a.setResourceId(parsePositiveInt(p[1], "resource id"));
        a.setQuantityAllocated(parsePositiveInt(p[2], "allocated quantity"));
        a.setAllocatedBy(parsePositiveInt(p[3], "allocated by"));

        if (resourceDAO.allocateResource(a)) {
            int uid = currentUser != null ? currentUser.getUserId() : 0;
            auditDAO.log(uid, "ALLOCATE_RESOURCE", "resource_allocations", a.getAllocationId(), null, cleanInput(data));
            return Protocol.SUCCESS + Protocol.DELIMITER + a.getAllocationId();
        }

        return fail("Allocation failed. Check available quantity.");
    }

    private String handleGetResourceAllocations(String data) {
        int disasterId = parsePositiveInt(data, "disaster id");
        StringBuilder sb = new StringBuilder(Protocol.SUCCESS + Protocol.DELIMITER);
        for (ResourceAllocation ra : resourceDAO.getAllocationsByDisaster(disasterId)) {
            sb.append(serAllocation(ra)).append(Protocol.LIST_DELIMITER);
        }
        return sb.toString();
    }

    private String handleReturnAllocation(String data) {
        int id = parsePositiveInt(data, "allocation id");
        return resourceDAO.returnAllocation(id) ? ok("Resource returned") : fail("Return failed");
    }

    /* ---- USERS & AUDIT ---- */

    private String handleGetAllUsers() {
        StringBuilder sb = new StringBuilder(Protocol.SUCCESS + Protocol.DELIMITER);
        for (User u : userDAO.getAllUsers()) {
            sb.append(serUser(u)).append(Protocol.LIST_DELIMITER);
        }
        return sb.toString();
    }

    private String handleUpdateUser(String data) {
        String[] p = data.split(Protocol.FIELD_DELIMITER, -1);
        if (p.length < 6) return fail("Invalid user update data");

        User u = new User();
        u.setUserId(parsePositiveInt(p[0], "user id"));
        u.setFullName(requiredText(p[1], "full name"));
        u.setEmail(cleanInput(p[2]));
        u.setPhone(cleanInput(p[3]));
        u.setDepartment(cleanInput(p[4]));
        u.setRole(parseEnum(User.Role.class, p[5], "role"));
        u.setActive(p.length > 6 && Boolean.parseBoolean(p[6].trim()));

        if (userDAO.updateUser(u)) {
            int uid = currentUser != null ? currentUser.getUserId() : 0;
            auditDAO.log(uid, "UPDATE_USER", "users", u.getUserId(), null, cleanInput(data));
            return ok("User updated");
        }

        return fail("Update failed");
    }

    private String handleGetAuditLogs() {
        StringBuilder sb = new StringBuilder(Protocol.SUCCESS + Protocol.DELIMITER);
        for (String[] log : auditDAO.getRecentLogs(100)) {
            for (int i = 0; i < log.length; i++) {
                if (i > 0) sb.append(",");
                sb.append(safeOutput(log[i]));
            }
            sb.append(Protocol.LIST_DELIMITER);
        }
        return sb.toString();
    }

    /* ---- SERIALISERS ---- */

    private String serUser(User u) {
        return u.getUserId() + "," + safeOutput(u.getUsername()) + "," + u.getRole() + ","
                + safeOutput(u.getFullName()) + "," + safeOutput(u.getEmail()) + ","
                + safeOutput(u.getPhone()) + "," + safeOutput(u.getDepartment()) + "," + u.isActive();
    }

    private String serDisaster(Disaster d) {
        return d.getDisasterId() + "," + d.getDisasterType() + "," + safeOutput(d.getTitle()) + ","
                + safeOutput(d.getDescription()) + "," + safeOutput(d.getLocation()) + "," + d.getSeverity() + ","
                + d.getStatus() + "," + d.getReportedBy() + "," + d.getAssignedCoordinator() + ","
                + d.getEstimatedAffected() + "," + d.getReportedAt() + "," + d.getPriorityScore();
    }

    private String serTeam(ResponseTeam t) {
        return t.getTeamId() + "," + safeOutput(t.getTeamName()) + "," + t.getDepartment() + ","
                + safeOutput(t.getTeamLeader()) + "," + safeOutput(t.getContactNumber()) + ","
                + t.getStatus() + "," + t.getCapacity() + "," + t.getCurrentMembers();
    }

    private String serAssignment(TeamAssignment ta) {
        return ta.getAssignmentId() + "," + ta.getDisasterId() + "," + ta.getTeamId() + ","
                + ta.getStatus() + "," + ta.getAssignedAt() + ","
                + safeOutput(ta.getTeamName()) + "," + safeOutput(ta.getDisasterTitle()) + "," + safeOutput(ta.getNotes());
    }

    private String serResource(Resource r) {
        Resource.Department dept = r.getDepartment() != null ? r.getDepartment() : Resource.Department.OTHER;
        return r.getResourceId() + "," + safeOutput(r.getResourceName()) + "," + r.getResourceType() + ","
                + r.getQuantity() + "," + safeOutput(r.getUnit()) + "," + safeOutput(r.getLocation()) + ","
                + r.getStatus() + "," + dept;
    }

    private String serAllocation(ResourceAllocation ra) {
        return ra.getAllocationId() + "," + ra.getDisasterId() + "," + ra.getResourceId() + ","
                + ra.getQuantityAllocated() + "," + ra.getAllocatedAt() + "," + ra.getStatus() + ","
                + safeOutput(ra.getResourceName()) + "," + safeOutput(ra.getDisasterTitle());
    }

    /* ---- SAFE INPUT / VALIDATION HELPERS ---- */

    private String requiredText(String value, String fieldName) {
        String cleaned = cleanInput(value);
        if (cleaned.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return cleaned;
    }

    private String cleanInput(String value) {
        if (value == null) return "";
        return value.replace(Protocol.DELIMITER, " ")
                .replace(Protocol.FIELD_DELIMITER, " ")
                .replace(Protocol.LIST_DELIMITER, " ")
                .replace("\r", " ")
                .replace("\n", " ")
                .trim();
    }

    private String safeOutput(String value) {
        if (value == null || value.trim().isEmpty()) return "N/A";
        return value.replace(",", ";")
                .replace(Protocol.DELIMITER, " ")
                .replace(Protocol.FIELD_DELIMITER, " ")
                .replace(Protocol.LIST_DELIMITER, " ")
                .replace("\r", " ")
                .replace("\n", " ")
                .trim();
    }

    private int parsePositiveInt(String value, String fieldName) {
        try {
            int n = Integer.parseInt(value.trim());
            if (n <= 0) {
                throw new IllegalArgumentException(fieldName + " must be greater than 0");
            }
            return n;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(fieldName + " must be a valid number");
        }
    }

    private int parseNonNegativeInt(String value, String fieldName) {
        try {
            int n = Integer.parseInt(value.trim());
            if (n < 0) {
                throw new IllegalArgumentException(fieldName + " cannot be negative");
            }
            return n;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(fieldName + " must be a valid number");
        }
    }

    private <E extends Enum<E>> E parseEnum(Class<E> enumClass, String value, String fieldName) {
        try {
            if (value == null || value.trim().isEmpty()) {
                throw new IllegalArgumentException(fieldName + " is required");
            }
            return Enum.valueOf(enumClass, value.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid " + fieldName + ": " + cleanInput(value));
        }
    }

    private <E extends Enum<E>> E parseOptionalEnum(Class<E> enumClass, String value, E defaultValue, String fieldName) {
        if (value == null || value.trim().isEmpty() || "null".equalsIgnoreCase(value.trim())) {
            return defaultValue;
        }
        return parseEnum(enumClass, value, fieldName);
    }

    private String ok(String msg) {
        return Protocol.SUCCESS + Protocol.DELIMITER + safeOutput(msg);
    }

    private String fail(String msg) {
        return Protocol.FAILURE + Protocol.DELIMITER + safeOutput(msg);
    }
}
