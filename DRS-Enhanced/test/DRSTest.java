import model.Disaster;
import model.ResponseTeam;
import model.Resource;
import model.ResourceAllocation;
import model.TeamAssignment;
import model.User;
import util.Protocol;
import util.SecurityUtil;

/**
 * DRSTest - Standalone unit tests for DRS-Enhanced.
 * No JUnit dependency.  Run from project root:
 *   javac -cp build -d build test/DRSTest.java
 *   java  -cp build DRSTest
 */
public class DRSTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("=== DRS-Enhanced Unit Tests ===\n");
        testDisasterPriorityScore();
        testDisasterStatusTransition();
        testUserPermissions();
        testPasswordHashing();
        testEncryptionDecryption();
        testTokenGeneration();
        testResponseTeamAvailability();
        testResourceStatusTracking();
        testTeamAssignmentStatus();
        testResourceAllocationStatus();
        testProtocolDelimiters();
        testProtocolCommands();
        testDisasterTypeEnums();
        testSeverityEnums();
        testDisasterToString();
        System.out.println("\n=== Results: " + passed + " passed, " + failed + " failed ===");
        if (failed > 0) System.exit(1);
    }

    static void testDisasterPriorityScore() {
        System.out.println("[TEST 01] Disaster Priority Score");
        Disaster d = new Disaster();
        d.setDisasterType(Disaster.DisasterType.EARTHQUAKE);
        d.setSeverity(Disaster.Severity.CRITICAL);
        d.setEstimatedAffected(15000);
        assertEquals("CRITICAL+EARTHQUAKE+>10000 = 100", 100, d.getPriorityScore());

        Disaster d2 = new Disaster();
        d2.setDisasterType(Disaster.DisasterType.OTHER);
        d2.setSeverity(Disaster.Severity.LOW);
        d2.setEstimatedAffected(50);
        assertEquals("LOW+OTHER+<100 = 20", 20, d2.getPriorityScore());

        Disaster d3 = new Disaster();
        d3.setDisasterType(Disaster.DisasterType.HURRICANE);
        d3.setSeverity(Disaster.Severity.HIGH);
        d3.setEstimatedAffected(500);
        assertEquals("HIGH+HURRICANE+100-1000 = 65", 65, d3.getPriorityScore());
    }

    static void testDisasterStatusTransition() {
        System.out.println("[TEST 02] Disaster Status Transitions");
        Disaster d = new Disaster();
        d.setStatus(Disaster.Status.REPORTED);
        assertEquals("REPORTED", "REPORTED", d.getStatus().name());
        d.setStatus(Disaster.Status.ASSESSED);
        assertEquals("ASSESSED", "ASSESSED", d.getStatus().name());
        d.setStatus(Disaster.Status.RESPONDING);
        assertEquals("RESPONDING", "RESPONDING", d.getStatus().name());
        d.setStatus(Disaster.Status.RESOLVED);
        assertEquals("RESOLVED", "RESOLVED", d.getStatus().name());
        d.setStatus(Disaster.Status.CLOSED);
        assertEquals("CLOSED", "CLOSED", d.getStatus().name());
    }

    static void testUserPermissions() {
        System.out.println("[TEST 03] User Role Permissions");
        User admin = new User(); admin.setRole(User.Role.ADMIN);
        User coord = new User(); coord.setRole(User.Role.COORDINATOR);
        User resp  = new User(); resp.setRole(User.Role.RESPONDER);
        User pub   = new User(); pub.setRole(User.Role.PUBLIC);

        assertTrue("Admin:MANAGE_USERS",       admin.hasPermission("MANAGE_USERS"));
        assertFalse("Coord:MANAGE_USERS=false", coord.hasPermission("MANAGE_USERS"));
        assertTrue("Coord:ASSIGN_TEAMS",        coord.hasPermission("ASSIGN_TEAMS"));
        assertFalse("Resp:ASSIGN_TEAMS=false",  resp.hasPermission("ASSIGN_TEAMS"));
        assertTrue("Public:REPORT_DISASTER",    pub.hasPermission("REPORT_DISASTER"));
        assertTrue("Public:VIEW_DISASTERS",     pub.hasPermission("VIEW_DISASTERS"));
        assertTrue("Admin:ASSIGN_TEAMS",        admin.hasPermission("ASSIGN_TEAMS"));
        assertTrue("Admin:MANAGE_RESOURCES",    admin.hasPermission("MANAGE_RESOURCES"));
    }

    static void testPasswordHashing() {
        System.out.println("[TEST 04] Password Hashing (SHA-256)");
        String h1 = SecurityUtil.hashPassword("admin123");
        String h2 = SecurityUtil.hashPassword("admin123");
        String h3 = SecurityUtil.hashPassword("different");
        assertEquals("Same input => same hash", h1, h2);
        assertNotEquals("Different input => different hash", h1, h3);
        assertEquals("SHA-256 produces 64 hex chars", 64, h1.length());
        // Verify known hash
        String knownHash = SecurityUtil.hashPassword("password");
        assertEquals("Known hash length 64", 64, knownHash.length());
    }

    static void testEncryptionDecryption() {
        System.out.println("[TEST 05] AES Encryption / Decryption");
        String plain = "Sensitive disaster location: 23.5N 78.2E";
        String enc   = SecurityUtil.encrypt(plain);
        String dec   = SecurityUtil.decrypt(enc);
        assertNotEquals("Encrypted != plain",    plain, enc);
        assertEquals("Decrypted == original",    plain, dec);

        // Edge case: empty string
        String encEmpty = SecurityUtil.encrypt("");
        String decEmpty = SecurityUtil.decrypt(encEmpty);
        assertEquals("Empty string round-trips", "", decEmpty);
    }

    static void testTokenGeneration() {
        System.out.println("[TEST 06] Non-Repudiation Token Generation");
        String t1 = SecurityUtil.generateToken("admin",  "LOGIN");
        String t2 = SecurityUtil.generateToken("admin",  "LOGIN");
        String t3 = SecurityUtil.generateToken("admin2", "LOGIN");
        assertEquals("Token length == 16",         16, t1.length());
        // Tokens differ when username differs
        assertNotEquals("Different user => different token", t1, t3);
        // Each call at different ms may differ; just assert non-null and correct length
        assertTrue("Token t2 length 16", t2.length() == 16);
    }

    static void testResponseTeamAvailability() {
        System.out.println("[TEST 07] ResponseTeam Availability");
        ResponseTeam t = new ResponseTeam();
        t.setStatus(ResponseTeam.TeamStatus.AVAILABLE);
        assertTrue("AVAILABLE.isAvailable()", t.isAvailable());
        t.setStatus(ResponseTeam.TeamStatus.DEPLOYED);
        assertFalse("DEPLOYED.isAvailable()=false", t.isAvailable());
        t.setStatus(ResponseTeam.TeamStatus.STANDBY);
        assertFalse("STANDBY.isAvailable()=false", t.isAvailable());
        t.setStatus(ResponseTeam.TeamStatus.UNAVAILABLE);
        assertFalse("UNAVAILABLE.isAvailable()=false", t.isAvailable());
    }

    static void testResourceStatusTracking() {
        System.out.println("[TEST 08] Resource Status & Quantity");
        Resource r = new Resource();
        r.setResourceType(Resource.ResourceType.MEDICAL_SUPPLY);
        r.setQuantity(100);
        r.setStatus(Resource.ResourceStatus.AVAILABLE);
        assertEquals("Type MEDICAL_SUPPLY",      "MEDICAL_SUPPLY", r.getResourceType().name());
        assertEquals("Quantity 100",              100,              r.getQuantity());
        assertEquals("Status AVAILABLE",          "AVAILABLE",      r.getStatus().name());
        r.setQuantity(0);
        r.setStatus(Resource.ResourceStatus.DEPLETED);
        assertEquals("Quantity now 0",            0,                r.getQuantity());
        assertEquals("Status DEPLETED",           "DEPLETED",       r.getStatus().name());
    }

    static void testTeamAssignmentStatus() {
        System.out.println("[TEST 09] TeamAssignment Status Values");
        TeamAssignment ta = new TeamAssignment();
        ta.setStatus(TeamAssignment.AssignmentStatus.ACTIVE);
        assertEquals("ACTIVE",    "ACTIVE",    ta.getStatus().name());
        ta.setStatus(TeamAssignment.AssignmentStatus.COMPLETED);
        assertEquals("COMPLETED", "COMPLETED", ta.getStatus().name());
        ta.setStatus(TeamAssignment.AssignmentStatus.CANCELLED);
        assertEquals("CANCELLED", "CANCELLED", ta.getStatus().name());
    }

    static void testResourceAllocationStatus() {
        System.out.println("[TEST 10] ResourceAllocation Status Values");
        ResourceAllocation ra = new ResourceAllocation();
        ra.setStatus(ResourceAllocation.AllocationStatus.ACTIVE);
        assertEquals("ACTIVE",   "ACTIVE",   ra.getStatus().name());
        ra.setStatus(ResourceAllocation.AllocationStatus.RETURNED);
        assertEquals("RETURNED", "RETURNED", ra.getStatus().name());
        ra.setStatus(ResourceAllocation.AllocationStatus.LOST);
        assertEquals("LOST",     "LOST",     ra.getStatus().name());
        ra.setQuantityAllocated(50);
        assertEquals("Qty 50", 50, ra.getQuantityAllocated());
    }

    static void testProtocolDelimiters() {
        System.out.println("[TEST 11] Protocol Delimiters");
        // Splitting a command+data request
        String req = Protocol.LOGIN + Protocol.DELIMITER + "user" + Protocol.FIELD_DELIMITER + "pass";
        String[] parts = req.split("\\" + Protocol.DELIMITER, 2);
        assertEquals("Command == LOGIN",         Protocol.LOGIN, parts[0]);
        assertEquals("Data == user~~pass",        "user" + Protocol.FIELD_DELIMITER + "pass", parts[1]);

        // List delimiter
        String list = "a" + Protocol.LIST_DELIMITER + "b" + Protocol.LIST_DELIMITER + "c";
        String[] items = list.split(Protocol.LIST_DELIMITER);
        assertEquals("List splits to 3", 3, items.length);
        assertEquals("First item a", "a", items[0]);
    }

    static void testProtocolCommands() {
        System.out.println("[TEST 12] Protocol Command Constants");
        assertNotNull("LOGIN constant",                   Protocol.LOGIN);
        assertNotNull("REGISTER constant",                Protocol.REGISTER);
        assertNotNull("REPORT_DISASTER constant",         Protocol.REPORT_DISASTER);
        assertNotNull("GET_ALL_DISASTERS constant",       Protocol.GET_ALL_DISASTERS);
        assertNotNull("CREATE_TEAM constant",             Protocol.CREATE_TEAM);
        assertNotNull("ASSIGN_TEAM constant",             Protocol.ASSIGN_TEAM);
        assertNotNull("COMPLETE_ASSIGNMENT constant",     Protocol.COMPLETE_ASSIGNMENT);
        assertNotNull("CREATE_RESOURCE constant",         Protocol.CREATE_RESOURCE);
        assertNotNull("ALLOCATE_RESOURCE constant",       Protocol.ALLOCATE_RESOURCE);
        assertNotNull("RETURN_ALLOCATION constant",       Protocol.RETURN_ALLOCATION);
        assertNotNull("SUCCESS constant",                 Protocol.SUCCESS);
        assertNotNull("FAILURE constant",                 Protocol.FAILURE);
    }

    static void testDisasterTypeEnums() {
        System.out.println("[TEST 13] DisasterType Enum Values");
        for (String name : new String[]{"HURRICANE","FIRE","EARTHQUAKE","FLOOD","TORNADO","TSUNAMI","OTHER"}) {
            Disaster.DisasterType dt = Disaster.DisasterType.valueOf(name);
            assertNotNull("DisasterType." + name, dt);
        }
    }

    static void testSeverityEnums() {
        System.out.println("[TEST 14] Severity & Status Enums");
        for (String s : new String[]{"LOW","MEDIUM","HIGH","CRITICAL"})
            assertNotNull("Severity." + s, Disaster.Severity.valueOf(s));
        for (String s : new String[]{"REPORTED","ASSESSED","RESPONDING","RESOLVED","CLOSED"})
            assertNotNull("Status." + s, Disaster.Status.valueOf(s));
        for (String s : new String[]{"ADMIN","COORDINATOR","RESPONDER","PUBLIC"})
            assertNotNull("Role." + s, User.Role.valueOf(s));
    }

    static void testDisasterToString() {
        System.out.println("[TEST 15] Disaster toString Contains Key Fields");
        Disaster d = new Disaster();
        d.setDisasterId(99);
        d.setTitle("Major Flood Event");
        d.setDisasterType(Disaster.DisasterType.FLOOD);
        d.setSeverity(Disaster.Severity.HIGH);
        d.setStatus(Disaster.Status.REPORTED);
        d.setLocation("Brisbane");
        d.setEstimatedAffected(3000);
        String s = d.toString();
        assertTrue("toString has id 99",          s.contains("99"));
        assertTrue("toString has FLOOD",           s.contains("FLOOD"));
        assertTrue("toString has Major Flood Event", s.contains("Major Flood Event"));
    }

    // ---- assertion helpers ----
    static void assertEquals(String msg, Object expected, Object actual) {
        if (expected == null ? actual == null : expected.equals(actual)) {
            System.out.println("  PASS: " + msg); passed++;
        } else {
            System.out.println("  FAIL: " + msg + " | expected=" + expected + " | got=" + actual); failed++;
        }
    }
    static void assertEquals(String msg, int expected, int actual) {
        assertEquals(msg, Integer.valueOf(expected), Integer.valueOf(actual));
    }
    static void assertNotEquals(String msg, Object a, Object b) {
        if (a == null ? b != null : !a.equals(b)) {
            System.out.println("  PASS: " + msg); passed++;
        } else {
            System.out.println("  FAIL: " + msg + " | both equal: " + a); failed++;
        }
    }
    static void assertTrue(String msg, boolean cond) {
        if (cond) { System.out.println("  PASS: " + msg); passed++; }
        else       { System.out.println("  FAIL: " + msg + " | expected true"); failed++; }
    }
    static void assertFalse(String msg, boolean cond) {
        if (!cond) { System.out.println("  PASS: " + msg); passed++; }
        else        { System.out.println("  FAIL: " + msg + " | expected false"); failed++; }
    }
    static void assertNotNull(String msg, Object obj) {
        if (obj != null) { System.out.println("  PASS: " + msg); passed++; }
        else              { System.out.println("  FAIL: " + msg + " | was null"); failed++; }
    }
}
