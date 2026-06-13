-- =============================================================
-- DRS-Enhanced Database Schema
-- COIT20258 Assignment 3
-- Run: mysql -u root -p < sql/drs_database.sql
-- =============================================================

CREATE DATABASE IF NOT EXISTS drs_enhanced
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE drs_enhanced;

-- ---- users ----
CREATE TABLE IF NOT EXISTS users (
    user_id        INT AUTO_INCREMENT PRIMARY KEY,
    username       VARCHAR(50)  NOT NULL UNIQUE,
    password_hash  VARCHAR(256) NOT NULL,
    role           ENUM('ADMIN','COORDINATOR','RESPONDER','PUBLIC') NOT NULL DEFAULT 'PUBLIC',
    full_name      VARCHAR(100),
    email          VARCHAR(100),
    phone          VARCHAR(20),
    department     VARCHAR(100),
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login     TIMESTAMP NULL,
    is_active      BOOLEAN NOT NULL DEFAULT TRUE
);

-- ---- disasters ----
CREATE TABLE IF NOT EXISTS disasters (
    disaster_id          INT AUTO_INCREMENT PRIMARY KEY,
    disaster_type        ENUM('HURRICANE','FIRE','EARTHQUAKE','FLOOD','TORNADO','TSUNAMI','OTHER') NOT NULL,
    title                VARCHAR(200) NOT NULL,
    description          TEXT,
    location             VARCHAR(255) NOT NULL,
    latitude             DECIMAL(10,7) DEFAULT 0.0,
    longitude            DECIMAL(10,7) DEFAULT 0.0,
    severity             ENUM('LOW','MEDIUM','HIGH','CRITICAL') NOT NULL DEFAULT 'MEDIUM',
    status               ENUM('REPORTED','ASSESSED','RESPONDING','RESOLVED','CLOSED') NOT NULL DEFAULT 'REPORTED',
    reported_by          INT,
    assigned_coordinator INT,
    reported_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    assessed_at          TIMESTAMP NULL,
    resolved_at          TIMESTAMP NULL,
    estimated_affected   INT DEFAULT 0,
    is_encrypted         BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (reported_by)          REFERENCES users(user_id),
    FOREIGN KEY (assigned_coordinator) REFERENCES users(user_id)
);

-- ---- response_teams  (Feature 1) ----
CREATE TABLE IF NOT EXISTS response_teams (
    team_id         INT AUTO_INCREMENT PRIMARY KEY,
    team_name       VARCHAR(100) NOT NULL,
    department      ENUM('FIRE_EMERGENCY','HOSPITAL','ELECTRICITY','TRANSPORTATION',
                         'WASTE_MANAGEMENT','WATER_SUPPLY','SCHOOLS','LAW_ENFORCEMENT','OTHER') NOT NULL,
    team_leader     VARCHAR(100),
    contact_number  VARCHAR(20),
    status          ENUM('AVAILABLE','DEPLOYED','STANDBY','UNAVAILABLE') NOT NULL DEFAULT 'AVAILABLE',
    capacity        INT DEFAULT 10,
    current_members INT DEFAULT 0,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ---- disaster_team_assignments  (Feature 1) ----
CREATE TABLE IF NOT EXISTS disaster_team_assignments (
    assignment_id INT AUTO_INCREMENT PRIMARY KEY,
    disaster_id   INT NOT NULL,
    team_id       INT NOT NULL,
    assigned_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    assigned_by   INT,
    status        ENUM('ACTIVE','COMPLETED','CANCELLED') NOT NULL DEFAULT 'ACTIVE',
    notes         TEXT,
    FOREIGN KEY (disaster_id) REFERENCES disasters(disaster_id),
    FOREIGN KEY (team_id)     REFERENCES response_teams(team_id)
);

-- ---- resources  (Feature 2) ----
CREATE TABLE IF NOT EXISTS resources (
    resource_id   INT AUTO_INCREMENT PRIMARY KEY,
    resource_name VARCHAR(100) NOT NULL,
    resource_type ENUM('VEHICLE','MEDICAL_SUPPLY','FOOD_WATER','EQUIPMENT','PERSONNEL','SHELTER','OTHER') NOT NULL,
    quantity      INT NOT NULL DEFAULT 0,
    unit          VARCHAR(50) DEFAULT 'units',
    location      VARCHAR(255),
    status        ENUM('AVAILABLE','IN_USE','DEPLETED','MAINTENANCE') NOT NULL DEFAULT 'AVAILABLE',
    department    ENUM('FIRE_EMERGENCY','HOSPITAL','ELECTRICITY','TRANSPORTATION',
                       'WASTE_MANAGEMENT','WATER_SUPPLY','SCHOOLS','LAW_ENFORCEMENT','OTHER'),
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- ---- resource_allocations  (Feature 2) ----
CREATE TABLE IF NOT EXISTS resource_allocations (
    allocation_id      INT AUTO_INCREMENT PRIMARY KEY,
    disaster_id        INT NOT NULL,
    resource_id        INT NOT NULL,
    quantity_allocated INT NOT NULL,
    allocated_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    allocated_by       INT,
    returned_at        TIMESTAMP NULL,
    status             ENUM('ACTIVE','RETURNED','LOST') NOT NULL DEFAULT 'ACTIVE',
    FOREIGN KEY (disaster_id) REFERENCES disasters(disaster_id),
    FOREIGN KEY (resource_id) REFERENCES resources(resource_id)
);

-- ---- audit_log ----
CREATE TABLE IF NOT EXISTS audit_log (
    log_id         INT AUTO_INCREMENT PRIMARY KEY,
    user_id        INT,
    action         VARCHAR(100) NOT NULL,
    table_affected VARCHAR(50),
    record_id      INT,
    old_values     TEXT,
    new_values     TEXT,
    ip_address     VARCHAR(50),
    timestamp      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ---- communications ----
CREATE TABLE IF NOT EXISTS communications (
    comm_id      INT AUTO_INCREMENT PRIMARY KEY,
    disaster_id  INT NOT NULL,
    sender_id    INT NOT NULL,
    message      TEXT NOT NULL,
    is_encrypted BOOLEAN DEFAULT FALSE,
    sent_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (disaster_id) REFERENCES disasters(disaster_id),
    FOREIGN KEY (sender_id)   REFERENCES users(user_id)
);

-- =============================================================
-- SEED DATA  (INSERT IGNORE = safe to re-run)
-- =============================================================

-- Default users (passwords hashed with SHA2(...,256))
INSERT IGNORE INTO users (user_id, username, password_hash, role, full_name, email, phone, department) VALUES
(1, 'admin',        SHA2('admin123',  256), 'ADMIN',       'System Administrator', 'admin@drs.gov',       '555-0000', 'Administration'),
(2, 'coordinator1', SHA2('coord123',  256), 'COORDINATOR', 'Jane Smith',           'jsmith@drs.gov',      '555-0001', 'Emergency Management'),
(3, 'responder1',   SHA2('resp123',   256), 'RESPONDER',   'Bob Johnson',          'bjohnson@fire.gov',   '555-0002', 'Fire & Emergency'),
(4, 'public_user',  SHA2('public123', 256), 'PUBLIC',      'John Doe',             'jdoe@email.com',      '555-0003',  NULL);

-- Sample response teams
INSERT IGNORE INTO response_teams (team_id, team_name, department, team_leader, contact_number, status, capacity, current_members) VALUES
(1, 'Alpha Fire Unit',        'FIRE_EMERGENCY',  'Capt. Williams',  '555-0101', 'AVAILABLE', 20, 15),
(2, 'Central Hospital Team',  'HOSPITAL',        'Dr. Patel',       '555-0102', 'AVAILABLE', 30, 25),
(3, 'Power Restoration Crew', 'ELECTRICITY',     'Mike Torres',     '555-0103', 'AVAILABLE', 10,  8),
(4, 'Transit Control',        'TRANSPORTATION',  'Sarah Lee',       '555-0104', 'AVAILABLE', 15, 12),
(5, 'Water Services Unit',    'WATER_SUPPLY',    'Tom Rivera',      '555-0105', 'AVAILABLE', 10,  9),
(6, 'Law Enforcement Squad',  'LAW_ENFORCEMENT', 'Lt. Garcia',      '555-0106', 'AVAILABLE', 25, 20);

-- Sample resources
INSERT IGNORE INTO resources (resource_id, resource_name, resource_type, quantity, unit, location, status, department) VALUES
(1,  'Fire Trucks',        'VEHICLE',        10,    'trucks',    'Station A',        'AVAILABLE', 'FIRE_EMERGENCY'),
(2,  'Ambulances',         'VEHICLE',        15,    'vehicles',  'Central Hospital', 'AVAILABLE', 'HOSPITAL'),
(3,  'Medical Kits',       'MEDICAL_SUPPLY', 500,   'kits',      'Medical Warehouse','AVAILABLE', 'HOSPITAL'),
(4,  'Food Rations',       'FOOD_WATER',     10000, 'packs',     'Supply Depot',     'AVAILABLE', 'OTHER'),
(5,  'Water Bottles',      'FOOD_WATER',     50000, 'liters',    'Supply Depot',     'AVAILABLE', 'WATER_SUPPLY'),
(6,  'Emergency Shelters', 'SHELTER',        50,    'units',     'Storage Yard',     'AVAILABLE', 'OTHER'),
(7,  'Rescue Equipment',   'EQUIPMENT',      200,   'sets',      'Fire Station',     'AVAILABLE', 'FIRE_EMERGENCY'),
(8,  'Police Vehicles',    'VEHICLE',        20,    'vehicles',  'Police HQ',        'AVAILABLE', 'LAW_ENFORCEMENT'),
(9,  'Generators',         'EQUIPMENT',      30,    'units',     'Electrical Depot', 'AVAILABLE', 'ELECTRICITY'),
(10, 'Rescue Boats',       'VEHICLE',        8,     'boats',     'Harbor',           'AVAILABLE', 'OTHER');
