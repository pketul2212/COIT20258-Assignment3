# DRS-Enhanced — How to Open & Run in NetBeans
## COIT20258 Assignment 3

---

## WHY THE PROJECT OPENS IN NETBEANS

The folder contains the three required NetBeans project descriptor files:

```
DRS-Enhanced/
└── nbproject/
    ├── project.xml          ← tells NetBeans this is a Java SE project
    ├── project.properties   ← main class, source/build dirs, VM args
    └── build-impl.xml       ← Ant build implementation
```

Without these files NetBeans will not recognise the folder as a project.
This ZIP contains all three, so **File → Open Project** will work immediately.

---

## PREREQUISITES — install before opening

| Tool | Version | Where |
|---|---|---|
| JDK | 17 or 21 | https://adoptium.net |
| JavaFX SDK | 21 | https://gluonhq.com/products/javafx/ |
| MySQL Server | 8.0+ | https://dev.mysql.com/downloads/mysql/ |
| MySQL Connector/J | 8.x jar | https://dev.mysql.com/downloads/connector/j/ |
| NetBeans IDE | 18+ | https://netbeans.apache.org |

---

## STEP 1 — Open the project in NetBeans

1. Unzip `DRS-Enhanced.zip` somewhere (e.g. `C:\Users\You\Projects\`)
2. Open NetBeans
3. **File → Open Project**
4. Browse to the **DRS-Enhanced** folder (the one that contains `nbproject/`)
5. Click **Open Project**

NetBeans will show the project in the Projects pane.

---

## STEP 2 — Add required libraries

Right-click the project → **Properties** → **Libraries** tab

### Add MySQL Connector:
- Click **Add JAR/Folder**
- Browse to `mysql-connector-j-8.x.x.jar`
- Click OK

### Add JavaFX:
- Click **Add Library**
- If "JavaFX 21" is not listed:
  - Tools → Libraries → New Library → name it `JavaFX 21`
  - Add Classpath → add ALL `.jar` files from `javafx-sdk-21\lib\`
  - Click OK
- Back in project libraries: select **JavaFX 21** → Add to Project

---

## STEP 3 — Set VM Options (JavaFX requires this)

Right-click project → **Properties** → **Run** tab

**Main Class:** `client.DRSClientApp`

**VM Options** (update the path to match where you installed JavaFX):

*Windows:*
```
--module-path "C:\javafx-sdk-21\lib" --add-modules javafx.controls,javafx.fxml
```

*macOS / Linux:*
```
--module-path "/opt/javafx-sdk-21/lib" --add-modules javafx.controls,javafx.fxml
```

---

## STEP 4 — Set up the database

### Option A — MySQL command line:
```bash
mysql -u root -p < sql/drs_database.sql
```

### Option B — MySQL Workbench:
1. Connect to localhost
2. File → Open SQL Script → select `sql/drs_database.sql`
3. Click the ⚡ Execute All button

This creates the `drs_enhanced` database with all tables and sample data.

### Update the database password:
Open `src/database/DatabaseConnection.java`, line ~22:
```java
private static final String DB_PASSWORD = "root";  // ← change to YOUR MySQL password
```

---

## STEP 5 — Start the SERVER first

The server MUST be running before you start the client.

1. In the Projects pane expand: **Source Packages → server**
2. Right-click `DRSServer.java` → **Run File**

Console output:
```
╔══════════════════════════════════════╗
║     DRS-Enhanced Server Started      ║
║     Listening on port: 9090          ║
╚══════════════════════════════════════╝
[DB] Connected to MySQL successfully.
[DB] All tables initialised and seeded successfully.
```

---

## STEP 6 — Start the CLIENT

1. Right-click project → **Run** (F6)  
   OR right-click `DRSClientApp.java` → **Run File**

The Login window appears.

---

## STEP 7 — Login

| Username | Password | Role |
|---|---|---|
| `admin` | `admin123` | ADMIN — all features |
| `coordinator1` | `coord123` | COORDINATOR — disasters, teams, resources |
| `responder1` | `resp123` | RESPONDER — view + report |
| `public_user` | `public123` | PUBLIC — view + report |

---

## STEP 8 — Using the two new features

### Feature 1: Team Coordination (tab "Team Coordination")
1. **Add a team** — fill the form on the left, click Add Team
2. **Assign team to a disaster:**
   - Click a team row to select it
   - Type a Disaster ID (e.g. `1`) in the Disaster ID field
   - Add optional notes
   - Click **Assign Team** — the team status changes to DEPLOYED
3. **Complete an assignment:**
   - Enter the Disaster ID, click **Load Assignments**
   - Select the assignment row
   - Click **Complete Assignment** — team resets to AVAILABLE

### Feature 2: Resource Management (tab "Resources")
1. **Add a resource** — fill the form, click Add Resource
2. **Allocate resource to disaster:**
   - Click a resource row to select it
   - Enter Disaster ID and quantity
   - Click **Allocate Resource** — quantity auto-deducted
3. **Return a resource:**
   - Enter Disaster ID, click **Load Allocations**
   - Select the ACTIVE allocation row
   - Click **Return Allocation** — quantity restored

---

## STEP 9 — Run the unit tests

In a terminal from the project root:
```bash
javac -cp build -d build test/DRSTest.java
java  -cp build DRSTest
```

Expected:
```
=== Results: 81 passed, 0 failed ===
```

---

## TROUBLESHOOTING

| Problem | Fix |
|---|---|
| NetBeans won't open project | Make sure you open the `DRS-Enhanced` **folder**, not a file inside it |
| "Cannot connect to server" | Start `DRSServer.java` FIRST, then the client |
| "Access denied" MySQL error | Update `DB_PASSWORD` in `DatabaseConnection.java` |
| "ClassNotFoundException: com.mysql.cj.jdbc.Driver" | Add MySQL Connector JAR to project libraries |
| JavaFX classes not found | Set `--module-path` in Run → VM Options |
| "Communications link failure" | Start the MySQL service |
| Blank/broken UI | Ensure FXML files are inside `src/client/view/` (they are) |

---

## PROJECT STRUCTURE

```
DRS-Enhanced/
├── nbproject/               ← NetBeans project files (DO NOT DELETE)
│   ├── project.xml
│   ├── project.properties
│   └── build-impl.xml
├── build.xml                ← Ant build file
├── manifest.mf
├── src/
│   ├── model/               ← Domain model
│   │   ├── User.java
│   │   ├── Disaster.java
│   │   ├── ResponseTeam.java        (Feature 1)
│   │   ├── Resource.java            (Feature 2)
│   │   ├── TeamAssignment.java      (Feature 1)
│   │   └── ResourceAllocation.java  (Feature 2)
│   ├── database/            ← DAO / data layer
│   │   ├── DatabaseConnection.java
│   │   ├── UserDAO.java
│   │   ├── DisasterDAO.java
│   │   ├── ResponseTeamDAO.java     (Feature 1)
│   │   ├── ResourceDAO.java         (Feature 2)
│   │   └── AuditDAO.java
│   ├── server/              ← Multi-threaded server
│   │   ├── DRSServer.java           (port 9090, thread pool 20)
│   │   └── ClientHandler.java       (Runnable, all protocol commands)
│   ├── util/
│   │   ├── SecurityUtil.java        (SHA-256, AES-128, tokens)
│   │   └── Protocol.java            (command constants, delimiters)
│   └── client/              ← JavaFX MVC client
│       ├── DRSClientApp.java        (JavaFX Application, main class)
│       ├── ServerConnection.java    (singleton TCP client)
│       ├── Session.java             (singleton current-user tracker)
│       ├── controller/
│       │   ├── LoginController.java
│       │   ├── RegisterController.java
│       │   ├── MainController.java
│       │   ├── DisasterController.java
│       │   ├── TeamController.java      (Feature 1)
│       │   ├── ResourceController.java  (Feature 2)
│       │   ├── UserController.java      (Admin only)
│       │   └── AuditController.java     (Admin only)
│       └── view/
│           ├── Login.fxml
│           ├── Register.fxml
│           ├── Main.fxml
│           ├── DisasterView.fxml
│           ├── TeamView.fxml            (Feature 1)
│           ├── ResourceView.fxml        (Feature 2)
│           ├── UserView.fxml
│           └── AuditView.fxml
├── test/
│   └── DRSTest.java         ← 15 tests, 81 assertions (no JUnit)
├── sql/
│   └── drs_database.sql     ← schema + seed data
└── diagrams/
    ├── UseCaseDiagram.puml
    ├── ClassDiagram.puml
    ├── SequenceDiagrams.puml  (4 diagrams)
    └── ERD.puml
```

Render `.puml` files at: https://www.plantuml.com/plantuml/uml/

---

## ARCHITECTURE

```
┌──────────────────────────────────┐
│   CLIENT TIER  (JavaFX / MVC)    │
│  DRSClientApp → Controllers      │
│  → FXML Views (8 screens)        │
└────────────┬─────────────────────┘
             │ TCP Socket port 9090
┌────────────▼─────────────────────┐
│   MIDDLE TIER  (Java Server)     │
│  DRSServer (ExecutorService×20)  │
│  → ClientHandler (Runnable)      │
│  → SecurityUtil / Protocol       │
└────────────┬─────────────────────┘
             │ JDBC
┌────────────▼─────────────────────┐
│   DATA TIER  (MySQL 8)           │
│  DatabaseConnection (Singleton)  │
│  DAO layer → drs_enhanced DB     │
│  8 tables, FK constraints        │
└──────────────────────────────────┘

Security: SHA-256 passwords · AES-128 encryption
          Role-based access · Full audit logging
```
