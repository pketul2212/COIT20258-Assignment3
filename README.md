# DRS-Enhanced — How to Open & Run in NetBeans

## COIT20258 Assignment 3

---

## PROJECT OPENS IN NETBEANS

The folder contains the required NetBeans project descriptor files:

```text
DRS-Enhanced/
└── nbproject/
    ├── project.xml          ← tells NetBeans this is a Java SE project
    ├── project.properties   ← main class, source/build dirs, VM args
    └── build-impl.xml       ← Ant build implementation
```

Without these files, NetBeans will not recognise the folder as a project.
This ZIP contains these files, so **File → Open Project** will work immediately.

---

## PREREQUISITES — INSTALL BEFORE OPENING

| Tool              | Version                | Where                                  |
| ----------------- | ---------------------- | -------------------------------------- |
| JDK               | 17 or 21               | https://adoptium.net                   |
| MySQL Server      | 8.0+                   | https://dev.mysql.com/downloads/mysql/ |
| NetBeans IDE      | 18+                    | https://netbeans.apache.org            |
| JavaFX JAR files  | Included in `dist/lib` | Already provided in project            |
| MySQL Connector/J | Included in `dist/lib` | Already provided in project            |

---

## STEP 1 — OPEN THE PROJECT IN NETBEANS

1. Unzip `DRS-Enhanced.zip` somewhere on your computer, for example:

```text
C:\Users\You\Projects\
```

2. Open **Apache NetBeans**.
3. Select **File → Open Project**.
4. Browse to the **DRS-Enhanced** folder.
5. Select the folder that contains `nbproject/`.
6. Click **Open Project**.

NetBeans will show the project in the Projects pane.

---

## STEP 2 — ADD REQUIRED LIBRARIES

The submitted project includes the required JavaFX and MySQL JAR files inside:

```text
dist/lib
```

To add the libraries:

1. Right-click the **DRS-Enhanced** project.
2. Click **Properties**.
3. Click **Libraries**.
4. Remove any old or broken JAR file references.
5. Click **Add JAR/Folder**.
6. Open the project folder.
7. Go to:

```text
dist/lib
```

8. Select all `.jar` files inside `dist/lib`.
9. Click **Open**.
10. Click **Apply** and **OK**.

The important JAR files include:

* `javafx.base.jar`
* `javafx.controls.jar`
* `javafx.fxml.jar`
* `javafx.graphics.jar`
* `javafx.media.jar`
* `javafx.swing.jar`
* `javafx.web.jar`
* `mysql-connector-j-9.6.0.jar`

---

## STEP 3 — SET VM OPTIONS

JavaFX requires VM options to run correctly.

1. Right-click the **DRS-Enhanced** project.
2. Click **Properties**.
3. Click **Run**.
4. Set the **Main Class** as:

```text
client.Launcher
```

5. Add the following VM Options:

```text
--module-path "dist/lib" --add-modules javafx.controls,javafx.fxml
```

6. Click **Apply** and **OK**.

---

## STEP 4 — SET UP THE DATABASE

The project uses a MySQL database named:

```text
drs_enhanced
```

The database tables are created programmatically through `DatabaseConnection.java`.
A SQL script is also provided in:

```text
sql/drs_database.sql
```

### Option A — MySQL Command Line

From the project root folder, run:

```bash
mysql -u root -p < sql/drs_database.sql
```

### Option B — MySQL Workbench

1. Open **MySQL Workbench**.
2. Connect to localhost.
3. Click **File → Open SQL Script**.
4. Select:

```text
sql/drs_database.sql
```

5. Click the execute button to run the script.

This creates the `drs_enhanced` database with the required tables and sample data.

### Update the Database Password

Open:

```text
src/database/DatabaseConnection.java
```

Find:

```java
private static final String DB_PASSWORD = "root";
```

If your MySQL password is different, change `"root"` to your own MySQL password.

Example:

```java
private static final String DB_PASSWORD = "your_password_here";
```

---

## STEP 5 — CLEAN AND BUILD THE PROJECT

After adding libraries and setting VM options:

1. Right-click the **DRS-Enhanced** project.
2. Click **Clean and Build**.
3. Wait until the build finishes.

Expected result:

```text
BUILD SUCCESSFUL
```

If the build fails because JavaFX packages are missing, go back to **Step 2** and make sure all JAR files from `dist/lib` are added.

---

## STEP 6 — START THE SERVER FIRST

The server must be running before starting the client.

1. In the NetBeans Projects pane, expand:

```text
Source Packages → server
```

2. Right-click:

```text
DRSServer.java
```

3. Click **Run File**.

Expected server output:

```text
DRS-Enhanced Server Started
Listening on port: 9090
[DB] Connected to MySQL successfully.
[DB] All tables initialised and seeded successfully.
```

Keep the server running.

---

## STEP 7 — START THE CLIENT

After the server is running:

1. Right-click the project and click **Run**.

OR

2. Expand:

```text
Source Packages → client
```

3. Right-click:

```text
DRSClientApp.java
```

4. Click **Run File**.

The login window should appear.

---

## STEP 8 — LOGIN

Use the following test accounts:

| Username       | Password    | Role                                      |
| -------------- | ----------- | ----------------------------------------- |
| `admin`        | `admin123`  | ADMIN — all features                      |
| `coordinator1` | `coord123`  | COORDINATOR — disasters, teams, resources |
| `responder1`   | `resp123`   | RESPONDER — view and report               |
| `public_user`  | `public123` | PUBLIC — view and report                  |

---

## STEP 9 — USING THE TWO NEW FEATURES

### Feature 1: Team Coordination

1. Open the **Team Coordination** tab.
2. Add a team by filling the form and clicking **Add Team**.
3. Select a team row from the table.
4. Enter a valid Disaster ID, for example:

```text
1
```

5. Add optional notes.
6. Click **Assign Team**.
7. Confirm the team status changes to `DEPLOYED`.
8. To complete the assignment:

   * Enter the Disaster ID.
   * Click **Load Assignments**.
   * Select the active assignment.
   * Click **Complete Assignment**.
9. Confirm the team status changes back to `AVAILABLE`.

### Feature 2: Resource Management

1. Open the **Resources** tab.
2. Add a resource by filling the form and clicking **Add Resource**.
3. Select a resource row from the table.
4. Enter a valid Disaster ID.
5. Enter the allocation quantity.
6. Click **Allocate Resource**.
7. Confirm the quantity is automatically deducted.
8. To return a resource:

   * Enter the Disaster ID.
   * Click **Load Allocations**.
   * Select an `ACTIVE` allocation.
   * Click **Return Allocation**.
9. Confirm the resource quantity is restored.

---

## STEP 10 — RUN THE UNIT TESTS

The project includes an automated test class:

```text
test/DRSTest.java
```

It contains 15 test methods and 81 assertions.

### Windows Command

Run this from the project root folder:

```bash
javac -cp "build\classes;dist\lib\*" -d build\classes test\DRSTest.java
java -cp "build\classes;dist\lib\*" DRSTest
```

### macOS / Linux Command

Run this from the project root folder:

```bash
javac -cp "build/classes:dist/lib/*" -d build/classes test/DRSTest.java
java -cp "build/classes:dist/lib/*" DRSTest
```

Expected result:

```text
=== Results: 81 passed, 0 failed ===
```

---

## TROUBLESHOOTING

| Problem                            | Fix                                                                |
| ---------------------------------- | ------------------------------------------------------------------ |
| NetBeans will not open the project | Make sure you open the `DRS-Enhanced` folder, not a file inside it |
| JavaFX classes not found           | Add all JAR files from `dist/lib` and set VM options               |
| `Module javafx.controls not found` | Check that JavaFX JAR files are added from `dist/lib`              |
| Cannot connect to server           | Run `DRSServer.java` first, then run the client                    |
| MySQL access denied                | Update `DB_PASSWORD` in `DatabaseConnection.java`                  |
| MySQL driver not found             | Add `mysql-connector-j-9.6.0.jar` from `dist/lib`                  |
| Communications link failure        | Start the MySQL service                                            |
| Blank or broken UI                 | Ensure FXML files are inside `src/client/view/`                    |
| Unit test cannot find classes      | Clean and build the project before running `DRSTest.java`          |

---

## PROJECT STRUCTURE

```text
DRS-Enhanced/
├── nbproject/               ← NetBeans project files
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
│   │   ├── DRSServer.java
│   │   └── ClientHandler.java
│   ├── util/
│   │   ├── SecurityUtil.java
│   │   └── Protocol.java
│   └── client/              ← JavaFX MVC client
│       ├── Launcher.java
│       ├── DRSClientApp.java
│       ├── ServerConnection.java
│       ├── Session.java
│       ├── controller/
│       │   ├── LoginController.java
│       │   ├── RegisterController.java
│       │   ├── MainController.java
│       │   ├── DisasterController.java
│       │   ├── TeamController.java
│       │   ├── ResourceController.java
│       │   ├── UserController.java
│       │   └── AuditController.java
│       └── view/
│           ├── Login.fxml
│           ├── Register.fxml
│           ├── Main.fxml
│           ├── DisasterView.fxml
│           ├── TeamView.fxml
│           ├── ResourceView.fxml
│           ├── UserView.fxml
│           └── AuditView.fxml
├── test/
│   └── DRSTest.java
├── sql/
│   └── drs_database.sql
└── dist/
    ├── DRS-Enhanced.jar
    └── lib/
```

---

## ARCHITECTURE

```text
┌──────────────────────────────────┐
│   CLIENT TIER  (JavaFX / MVC)    │
│  DRSClientApp → Controllers      │
│  FXML Views (8 screens)          │
└────────────┬─────────────────────┘
             │ TCP Socket port 9090
┌────────────▼─────────────────────┐
│   MIDDLE TIER  (Java Server)     │
│  DRSServer (ExecutorService)     │
│  ClientHandler (Runnable)        │
│  SecurityUtil / Protocol         │
└────────────┬─────────────────────┘
             │ JDBC
┌────────────▼─────────────────────┐
│   DATA TIER  (MySQL 8)           │
│  DatabaseConnection (Singleton)  │
│  DAO layer → drs_enhanced DB     │
│  8 tables with FK constraints    │
└──────────────────────────────────┘
```

Security features include:

* SHA-256 password hashing
* AES-128 encryption utility
* Role-based access control
* Audit logging

---

## RECOMMENDED TESTING ORDER

1. Start MySQL Server.
2. Open the project in NetBeans.
3. Add all required JAR files from `dist/lib`.
4. Set VM options.
5. Clean and build the project.
6. Run `server.DRSServer`.
7. Run `client.Launcher` or `client.DRSClientApp`.
8. Login as `admin`.
9. Test disaster reporting.
10. Test priority sorting.
11. Test team assignment.
12. Test complete assignment.
13. Test resource allocation.
14. Test return allocation.
15. Check user management.
16. Check audit log.
17. Run `DRSTest.java`.

---

## EXPECTED FINAL RESULT

The system should successfully demonstrate:

* JavaFX client GUI
* Multi-threaded server
* MySQL database integration
* MVC architecture
* Role-based access control
* Disaster reporting and priority sorting
* Team Coordination feature
* Resource Management feature
* Audit logging
* Automated testing with 81 passed assertions
