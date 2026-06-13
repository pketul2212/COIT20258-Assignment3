package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Singleton database connection manager.
 * Programmatically initialises all MySQL tables on first run.
 */
public class DatabaseConnection {

    // ====== CHANGE DB_PASSWORD TO MATCH YOUR MYSQL PASSWORD ======
    private static final String DB_URL = "jdbc:mysql://localhost:3306/drs_enhanced"
            + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
            + "&createDatabaseIfNotExist=true";
    private static final String DB_USER     = "root";
    private static final String DB_PASSWORD = "root";   
    // =============================================================

    private static DatabaseConnection instance;
    private Connection connection;

    private DatabaseConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println("[DB] Connected to MySQL successfully.");
        } catch (ClassNotFoundException e) {
            System.err.println("[DB] MySQL JDBC Driver not found: " + e.getMessage());
            throw new RuntimeException("MySQL Driver not found", e);
        } catch (SQLException e) {
            System.err.println("[DB] Connection failed: " + e.getMessage());
            throw new RuntimeException("Database connection failed", e);
        }
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null || !instance.isConnected()) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            }
        } catch (SQLException e) {
            System.err.println("[DB] Re-connect error: " + e.getMessage());
        }
        return connection;
    }

    private boolean isConnected() {
        try {
            return connection != null && !connection.isClosed() && connection.isValid(2);
        } catch (SQLException e) {
            return false;
        }
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("[DB] Connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("[DB] Close error: " + e.getMessage());
        }
    }

    /**
     * Creates all tables programmatically if they do not yet exist.
     */
    public void initializeDatabase() {
        try (Statement stmt = connection.createStatement()) {

            stmt.execute("CREATE TABLE IF NOT EXISTS users ("
                    + "user_id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "username VARCHAR(50) NOT NULL UNIQUE,"
                    + "password_hash VARCHAR(256) NOT NULL,"
                    + "role ENUM('ADMIN','COORDINATOR','RESPONDER','PUBLIC') NOT NULL DEFAULT 'PUBLIC',"
                    + "full_name VARCHAR(100),"
                    + "email VARCHAR(100),"
                    + "phone VARCHAR(20),"
                    + "department VARCHAR(100),"
                    + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                    + "last_login TIMESTAMP NULL,"
                    + "is_active BOOLEAN DEFAULT TRUE)");

            stmt.execute("CREATE TABLE IF NOT EXISTS disasters ("
                    + "disaster_id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "disaster_type ENUM('HURRICANE','FIRE','EARTHQUAKE','FLOOD','TORNADO','TSUNAMI','OTHER') NOT NULL,"
                    + "title VARCHAR(200) NOT NULL,"
                    + "description TEXT,"
                    + "location VARCHAR(255) NOT NULL,"
                    + "latitude DECIMAL(10,7) DEFAULT 0.0,"
                    + "longitude DECIMAL(10,7) DEFAULT 0.0,"
                    + "severity ENUM('LOW','MEDIUM','HIGH','CRITICAL') NOT NULL DEFAULT 'MEDIUM',"
                    + "status ENUM('REPORTED','ASSESSED','RESPONDING','RESOLVED','CLOSED') NOT NULL DEFAULT 'REPORTED',"
                    + "reported_by INT,"
                    + "assigned_coordinator INT,"
                    + "reported_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                    + "assessed_at TIMESTAMP NULL,"
                    + "resolved_at TIMESTAMP NULL,"
                    + "estimated_affected INT DEFAULT 0,"
                    + "is_encrypted BOOLEAN DEFAULT FALSE,"
                    + "FOREIGN KEY (reported_by) REFERENCES users(user_id),"
                    + "FOREIGN KEY (assigned_coordinator) REFERENCES users(user_id))");

            stmt.execute("CREATE TABLE IF NOT EXISTS response_teams ("
                    + "team_id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "team_name VARCHAR(100) NOT NULL,"
                    + "department ENUM('FIRE_EMERGENCY','HOSPITAL','ELECTRICITY','TRANSPORTATION',"
                    + "'WASTE_MANAGEMENT','WATER_SUPPLY','SCHOOLS','LAW_ENFORCEMENT','OTHER') NOT NULL,"
                    + "team_leader VARCHAR(100),"
                    + "contact_number VARCHAR(20),"
                    + "status ENUM('AVAILABLE','DEPLOYED','STANDBY','UNAVAILABLE') NOT NULL DEFAULT 'AVAILABLE',"
                    + "capacity INT DEFAULT 10,"
                    + "current_members INT DEFAULT 0,"
                    + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            stmt.execute("CREATE TABLE IF NOT EXISTS disaster_team_assignments ("
                    + "assignment_id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "disaster_id INT NOT NULL,"
                    + "team_id INT NOT NULL,"
                    + "assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                    + "assigned_by INT,"
                    + "status ENUM('ACTIVE','COMPLETED','CANCELLED') DEFAULT 'ACTIVE',"
                    + "notes TEXT,"
                    + "FOREIGN KEY (disaster_id) REFERENCES disasters(disaster_id),"
                    + "FOREIGN KEY (team_id) REFERENCES response_teams(team_id))");

            stmt.execute("CREATE TABLE IF NOT EXISTS resources ("
                    + "resource_id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "resource_name VARCHAR(100) NOT NULL,"
                    + "resource_type ENUM('VEHICLE','MEDICAL_SUPPLY','FOOD_WATER','EQUIPMENT','PERSONNEL','SHELTER','OTHER') NOT NULL,"
                    + "quantity INT NOT NULL DEFAULT 0,"
                    + "unit VARCHAR(50) DEFAULT 'units',"
                    + "location VARCHAR(255),"
                    + "status ENUM('AVAILABLE','IN_USE','DEPLETED','MAINTENANCE') NOT NULL DEFAULT 'AVAILABLE',"
                    + "department ENUM('FIRE_EMERGENCY','HOSPITAL','ELECTRICITY','TRANSPORTATION',"
                    + "'WASTE_MANAGEMENT','WATER_SUPPLY','SCHOOLS','LAW_ENFORCEMENT','OTHER'),"
                    + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                    + "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)");

            stmt.execute("CREATE TABLE IF NOT EXISTS resource_allocations ("
                    + "allocation_id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "disaster_id INT NOT NULL,"
                    + "resource_id INT NOT NULL,"
                    + "quantity_allocated INT NOT NULL,"
                    + "allocated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                    + "allocated_by INT,"
                    + "returned_at TIMESTAMP NULL,"
                    + "status ENUM('ACTIVE','RETURNED','LOST') DEFAULT 'ACTIVE',"
                    + "FOREIGN KEY (disaster_id) REFERENCES disasters(disaster_id),"
                    + "FOREIGN KEY (resource_id) REFERENCES resources(resource_id))");

            stmt.execute("CREATE TABLE IF NOT EXISTS audit_log ("
                    + "log_id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "user_id INT,"
                    + "action VARCHAR(100) NOT NULL,"
                    + "table_affected VARCHAR(50),"
                    + "record_id INT,"
                    + "old_values TEXT,"
                    + "new_values TEXT,"
                    + "ip_address VARCHAR(50),"
                    + "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            stmt.execute("CREATE TABLE IF NOT EXISTS communications ("
                    + "comm_id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "disaster_id INT NOT NULL,"
                    + "sender_id INT NOT NULL,"
                    + "message TEXT NOT NULL,"
                    + "is_encrypted BOOLEAN DEFAULT FALSE,"
                    + "sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                    + "FOREIGN KEY (disaster_id) REFERENCES disasters(disaster_id),"
                    + "FOREIGN KEY (sender_id) REFERENCES users(user_id))");

            // Seed default admin user if table is empty
            stmt.execute("INSERT IGNORE INTO users (username,password_hash,role,full_name,email,department) VALUES"
                    + "('admin',SHA2('admin123',256),'ADMIN','System Administrator','admin@drs.gov','Administration'),"
                    + "('coordinator1',SHA2('coord123',256),'COORDINATOR','Jane Smith','jsmith@drs.gov','Emergency Management'),"
                    + "('responder1',SHA2('resp123',256),'RESPONDER','Bob Johnson','bjohnson@fire.gov','Fire & Emergency'),"
                    + "('public_user',SHA2('public123',256),'PUBLIC','John Doe','jdoe@email.com',NULL)");

            stmt.execute("INSERT IGNORE INTO response_teams (team_name,department,team_leader,contact_number,status,capacity,current_members) VALUES"
                    + "('Alpha Fire Unit','FIRE_EMERGENCY','Capt. Williams','555-0101','AVAILABLE',20,15),"
                    + "('Central Hospital Team','HOSPITAL','Dr. Patel','555-0102','AVAILABLE',30,25),"
                    + "('Power Restoration Crew','ELECTRICITY','Mike Torres','555-0103','AVAILABLE',10,8),"
                    + "('Transit Control','TRANSPORTATION','Sarah Lee','555-0104','AVAILABLE',15,12),"
                    + "('Water Services Unit','WATER_SUPPLY','Tom Rivera','555-0105','AVAILABLE',10,9),"
                    + "('Law Enforcement Squad','LAW_ENFORCEMENT','Lt. Garcia','555-0106','AVAILABLE',25,20)");

            stmt.execute("INSERT IGNORE INTO resources (resource_name,resource_type,quantity,unit,location,status,department) VALUES"
                    + "('Fire Trucks','VEHICLE',10,'trucks','Station A','AVAILABLE','FIRE_EMERGENCY'),"
                    + "('Ambulances','VEHICLE',15,'vehicles','Central Hospital','AVAILABLE','HOSPITAL'),"
                    + "('Medical Kits','MEDICAL_SUPPLY',500,'kits','Medical Warehouse','AVAILABLE','HOSPITAL'),"
                    + "('Food Rations','FOOD_WATER',10000,'packs','Supply Depot','AVAILABLE','OTHER'),"
                    + "('Water Bottles','FOOD_WATER',50000,'liters','Supply Depot','AVAILABLE','WATER_SUPPLY'),"
                    + "('Emergency Shelters','SHELTER',50,'units','Storage Yard','AVAILABLE','OTHER'),"
                    + "('Rescue Equipment','EQUIPMENT',200,'sets','Fire Station','AVAILABLE','FIRE_EMERGENCY'),"
                    + "('Police Vehicles','VEHICLE',20,'vehicles','Police HQ','AVAILABLE','LAW_ENFORCEMENT'),"
                    + "('Generators','EQUIPMENT',30,'units','Electrical Depot','AVAILABLE','ELECTRICITY'),"
                    + "('Rescue Boats','VEHICLE',8,'boats','Harbor','AVAILABLE','OTHER')");

            System.out.println("[DB] All tables initialised and seeded successfully.");
        } catch (SQLException e) {
            System.err.println("[DB] Init error: " + e.getMessage());
        }
    }
}
