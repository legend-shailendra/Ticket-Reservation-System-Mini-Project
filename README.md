# Ticket Reservation System Mini-Project (JDBC with MySQL)

An end-to-end **Ticket Reservation System** built in Java demonstrating **Database Connectivity using JDBC** specifically with **MySQL Server**.

---

## 🎯 Practical Aim
> **Aim:** To implement a mini project (Ticket Reservation System) using MySQL and demonstrate relational database connectivity, CRUD operations, metadata inspection, and ACID transaction handling using JDBC.

---

## 🏗️ Project Architecture & Directory Structure

```text
Ticket Reservation System Mini-Project/
│
├── lib/                                      # JDBC Driver JAR
│   └── mysql-connector-j-8.4.0.jar           # Official MySQL Connector/J (Type 4 Driver)
│
├── src/com/ticketreservation/
│   ├── config/
│   │   └── DBConnection.java                 # MySQL JDBC Connection Manager & MetaData Diagnostics
│   ├── dao/
│   │   ├── TripDAO.java                      # DAO for Routes/Trains (SELECT, INSERT, SEARCH via PreparedStatements)
│   │   └── BookingDAO.java                   # DAO for Bookings (ACID Transactions: setAutoCommit, commit, rollback)
│   ├── model/
│   │   ├── Trip.java                         # Route/Transport Entity POJO
│   │   └── Booking.java                      # Reservation Entity POJO
│   ├── util/
│   │   └── TableFormatter.java               # ASCII Border Table & ResultSet Formatter
│   └── TicketReservationApp.java             # Main CLI Interactive Console Application
│
├── bin/                                      # Compiled Java .class bytecode
├── db.properties                             # MySQL Database connection configuration
├── schema_mysql.sql                          # MySQL Database DDL & Seed Script
├── compile.bat                               # Windows 1-click Compilation script
├── run.bat                                   # Windows 1-click Execution script
└── README.md                                 # Lab Documentation & Viva Guide
```

---

## 🧩 Relational Database Schema Design (MySQL)

### 1. `trips` Table (Trains / Services / Schedules)
| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `trip_id` | `INT` | `PRIMARY KEY`, `AUTO_INCREMENT` | Unique identifier for the service route |
| `service_code` | `VARCHAR(20)` | `NOT NULL`, `UNIQUE` | Train/Bus number (e.g. `12951`) |
| `service_name` | `VARCHAR(100)`| `NOT NULL` | Name of service (e.g. `Mumbai Rajdhani`) |
| `source_city` | `VARCHAR(50)` | `NOT NULL` | Departure city |
| `destination_city` | `VARCHAR(50)` | `NOT NULL` | Arrival city |
| `departure_time` | `VARCHAR(20)` | `NOT NULL` | Scheduled departure time |
| `arrival_time` | `VARCHAR(20)` | `NOT NULL` | Scheduled arrival time |
| `total_seats` | `INT` | `NOT NULL` | Max seating capacity |
| `available_seats`| `INT` | `NOT NULL`, `CHECK(>=0)` | Real-time seat inventory |
| `fare_per_seat` | `DECIMAL(10,2)`| `NOT NULL` | Cost per ticket in INR |

### 2. `bookings` Table (Passenger Reservations)
| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `booking_id` | `INT` | `PRIMARY KEY`, `AUTO_INCREMENT` | Internal booking sequence ID |
| `pnr_number` | `VARCHAR(12)` | `NOT NULL`, `UNIQUE` | Unique 6-digit PNR reference |
| `trip_id` | `INT` | `FOREIGN KEY REFERENCES trips(trip_id)`| Linked route/service |
| `passenger_name`| `VARCHAR(100)`| `NOT NULL` | Full name of primary traveler |
| `passenger_age` | `INT` | `NOT NULL` | Passenger age |
| `passenger_gender`| `VARCHAR(10)` | `NOT NULL` | Gender |
| `contact_phone` | `VARCHAR(15)` | `NOT NULL` | Mobile phone number |
| `seats_booked` | `INT` | `DEFAULT 1` | Quantity of seats booked |
| `total_amount` | `DECIMAL(10,2)`| `NOT NULL` | Total fare charged |
| `booking_status`| `VARCHAR(20)` | `DEFAULT 'CONFIRMED'` | `CONFIRMED` or `CANCELLED` |
| `booking_date` | `TIMESTAMP` | `DEFAULT CURRENT_TIMESTAMP` | Transaction timestamp |

---

## ⚡ Key JDBC Concepts Demonstrated in Code

### 1. The 6 Standard Steps of JDBC
1. **Load Driver Class**:
   ```java
   Class.forName("com.mysql.cj.jdbc.Driver");
   ```
2. **Establish Connection**:
   ```java
   Connection conn = DriverManager.getConnection(url, username, password);
   ```
3. **Create Statement / PreparedStatement**:
   ```java
   PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM trips WHERE trip_id = ?");
   pstmt.setInt(1, tripId);
   ```
4. **Execute SQL Statements**:
   - `pstmt.executeQuery()`: for `SELECT` queries returning `ResultSet`.
   - `pstmt.executeUpdate()`: for `INSERT`, `UPDATE`, `DELETE` returning affected row count.
5. **Process `ResultSet`**:
   ```java
   while (rs.next()) {
       int available = rs.getInt("available_seats");
       double fare = rs.getDouble("fare_per_seat");
   }
   ```
6. **Close Database Resources**:
   - Done using Java 7+ **try-with-resources** to prevent memory/socket leaks.

### 2. Transaction Management (ACID Compliance)
In [BookingDAO.java](file:///c:/Users/shail/OneDrive/Documents/DBMS/Practical%2010/src/com/ticketreservation/dao/BookingDAO.java):
```java
conn.setAutoCommit(false); // Begin Transaction

// Step A: Check available seats
// Step B: Insert booking record
// Step C: Update & decrement available seats in trips table

conn.commit(); // Atomically commit all changes!
```
If an exception occurs or seats are insufficient:
```java
conn.rollback(); // Undo changes, keeping database consistent!
```

### 3. Database Metadata Inspection
In [DBConnection.java](file:///c:/Users/shail/OneDrive/Documents/DBMS/Practical%2010/src/com/ticketreservation/config/DBConnection.java):
```java
DatabaseMetaData meta = conn.getMetaData();
System.out.println("Product: " + meta.getDatabaseProductName());
System.out.println("Version: " + meta.getDatabaseProductVersion());
System.out.println("Driver:  " + meta.getDriverName());
```

---

## 🚀 How to Set Up and Run with MySQL

### Step 1: Database Setup
1. Open **MySQL Workbench** or the **MySQL Command Line Client**.
2. Run the SQL script [schema_mysql.sql](file:///c:/Users/shail/OneDrive/Documents/DBMS/Practical%2010/schema_mysql.sql):
   ```sql
   source c:/Users/shail/OneDrive/Documents/DBMS/Practical 10/schema_mysql.sql;
   ```
   *(This creates the `ticket_reservation_db` database and populates sample routes/trains).*
3. Open [db.properties](file:///c:/Users/shail/OneDrive/Documents/DBMS/Practical%2010/db.properties) and update your MySQL username and password:
   ```properties
   db.driver=com.mysql.cj.jdbc.Driver
   db.url=jdbc:mysql://localhost:3306/ticket_reservation_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
   db.user=root
   db.password=Shailendra@5736
   ```

---

### Step 2: Compile the Project
Double-click `compile.bat` or run in terminal:
```powershell
& "C:\Program Files\Java\jdk-24\bin\javac.exe" -d bin -cp "lib/*;src" (Get-ChildItem -Path src -Filter *.java -Recurse | ForEach-Object { $_.FullName })
```

---

### Step 3: Run the Application
Double-click `run.bat` or run in terminal:
```powershell
& "C:\Program Files\Java\jdk-24\bin\java.exe" -cp "bin;lib/*;." com.ticketreservation.TicketReservationApp
```

---

## 🖥️ Application Features & Menu
```text
----------------------------- MAIN MENU -----------------------------
  [1] View All Scheduled Services / Trains
  [2] Search Services (by Source & Destination)
  [3] Book a Ticket (ACID Transaction with JDBC)
  [4] Check Ticket Details by PNR
  [5] Cancel Ticket (Atomic Seat Restoration)
  [6] View All Bookings (Admin Audit Log)
  [7] Add New Route / Train Service (Admin)
  [8] Database Connectivity Diagnostics (JDBC MetaData Demo)
  [9] Verify / Initialize Database Tables
  [0] Exit
---------------------------------------------------------------------
```

---

## 🎓 Viva Voce Questions & Answers

**Q1: What is JDBC and which JDBC driver type is used here?**
> **A:** JDBC (Java Database Connectivity) is a standard Java API that connects Java applications to relational database management systems. This project uses a **Type 4 JDBC Driver** (`mysql-connector-j`), which converts JDBC calls directly into MySQL's network protocol over TCP/IP without requiring any client-side native libraries.

**Q2: What is the difference between `Statement` and `PreparedStatement`?**
> **A:** 
> - `Statement` compiles and executes raw SQL strings every time. It is vulnerable to SQL Injection attacks.
> - `PreparedStatement` is precompiled by the MySQL database engine, supports parameterized placeholders (`?`), offers better execution performance for repetitive queries, and prevents SQL Injection.

**Q3: How are transactions managed in JDBC?**
> **A:** By default, JDBC connections have auto-commit set to `true`. To manage atomic multi-step transactions:
> 1. Call `connection.setAutoCommit(false)` to begin the transaction.
> 2. Execute related DML statements (e.g. insert booking and decrement seat count).
> 3. Call `connection.commit()` if all statements succeed.
> 4. In case of an exception, call `connection.rollback()` inside the `catch` block to revert uncommitted changes.

**Q4: What is `DatabaseMetaData` and `ResultSetMetaData`?**
> **A:** 
> - `DatabaseMetaData` provides comprehensive information about the database engine itself (MySQL product name, version, supported features, catalog, tables).
> - `ResultSetMetaData` provides structural information about the columns in a returned `ResultSet` (column count, column names, data types, width).

**Q5: What is the return type of `executeQuery()` vs `executeUpdate()`?**
> **A:**
> - `executeQuery()` returns a `ResultSet` object containing tabular query results (used for `SELECT`).
> - `executeUpdate()` returns an integer representing the number of rows affected (used for `INSERT`, `UPDATE`, `DELETE`, `CREATE`).
