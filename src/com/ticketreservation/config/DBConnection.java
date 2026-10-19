package com.ticketreservation.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Manages database connectivity via JDBC specifically for MySQL Server.
 * Reads connection credentials from db.properties or falls back to defaults.
 */
public class DBConnection {

    private static String driver = "com.mysql.cj.jdbc.Driver";
    private static String url = "jdbc:mysql://localhost:3306/ticket_reservation_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static String username = "root";
    private static String password = "root";

    static {
        loadProperties();
    }

    public static void loadProperties() {
        Properties props = new Properties();
        File propFile = new File("db.properties");
        if (propFile.exists()) {
            try (FileInputStream fis = new FileInputStream(propFile)) {
                props.load(fis);
                driver = props.getProperty("db.driver", "com.mysql.cj.jdbc.Driver").trim();
                url = props.getProperty("db.url", url).trim();
                username = props.getProperty("db.user", "root").trim();
                password = props.getProperty("db.password", "root").trim();
            } catch (IOException e) {
                System.err.println("[WARN] Could not read db.properties, using defaults: " + e.getMessage());
            }
        }
    }

    /**
     * Obtains a new JDBC Connection to MySQL.
     */
    public static Connection getConnection() throws SQLException {
        try {
            // Step 1: Explicitly load MySQL JDBC Driver class
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver class not found: " + driver + 
                                  "\nEnsure 'mysql-connector-j-8.4.0.jar' in /lib is included in your classpath.", e);
        }

        // Step 2: Establish connection via DriverManager
        return DriverManager.getConnection(url, username, password);
    }

    /**
     * Checks if MySQL database connection can be established.
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Demonstrates DatabaseMetaData API to show MySQL connectivity specifications.
     */
    public static void printConnectionDiagnostics() {
        System.out.println("\n=======================================================");
        System.out.println("       JDBC MYSQL DATABASE CONNECTIVITY DIAGNOSTICS    ");
        System.out.println("=======================================================");
        try (Connection conn = getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            System.out.println("[+] Database Product Name : " + meta.getDatabaseProductName());
            System.out.println("[+] Database Version      : " + meta.getDatabaseProductVersion());
            System.out.println("[+] MySQL Driver Name     : " + meta.getDriverName());
            System.out.println("[+] MySQL Driver Version  : " + meta.getDriverVersion());
            System.out.println("[+] Connected URL         : " + meta.getURL());
            System.out.println("[+] Database User         : " + meta.getUserName());
            System.out.println("[+] Catalog / Database    : " + conn.getCatalog());
            System.out.println("[+] Auto-Commit Default   : " + conn.getAutoCommit());
            System.out.println("[+] Transaction Isolation : " + conn.getTransactionIsolation());

            System.out.println("\n[+] Tables present in '" + conn.getCatalog() + "':");
            try (ResultSet rs = meta.getTables(conn.getCatalog(), null, "%", new String[]{"TABLE"})) {
                boolean found = false;
                while (rs.next()) {
                    found = true;
                    String tableName = rs.getString("TABLE_NAME");
                    String tableType = rs.getString("TABLE_TYPE");
                    System.out.println("    - " + tableName + " (" + tableType + ")");
                }
                if (!found) {
                    System.out.println("    (No tables found. Run option 9 to initialize!)");
                }
            }
        } catch (SQLException e) {
            System.err.println("[-] Connection Diagnostics Failed!");
            System.err.println("    Error: " + e.getMessage());
            System.err.println("    SQLState: " + e.getSQLState());
            System.err.println("    ErrorCode: " + e.getErrorCode());
        }
        System.out.println("=======================================================\n");
    }

    /**
     * Automatically initializes MySQL tables and seed data if missing.
     */
    public static void autoInitializeSchemaIfTablesMissing() {
        try (Connection conn = getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            try (ResultSet rs = meta.getTables(conn.getCatalog(), null, "trips", null)) {
                if (rs.next()) {
                    return; // Tables exist
                }
            }

            System.out.println("[*] Initializing MySQL tables and sample seed data...");
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS trips (" +
                    "  trip_id INT AUTO_INCREMENT PRIMARY KEY," +
                    "  service_code VARCHAR(20) NOT NULL UNIQUE," +
                    "  service_name VARCHAR(100) NOT NULL," +
                    "  source_city VARCHAR(50) NOT NULL," +
                    "  destination_city VARCHAR(50) NOT NULL," +
                    "  departure_time VARCHAR(20) NOT NULL," +
                    "  arrival_time VARCHAR(20) NOT NULL," +
                    "  total_seats INT NOT NULL," +
                    "  available_seats INT NOT NULL," +
                    "  fare_per_seat DECIMAL(10, 2) NOT NULL" +
                    ")"
                );

                stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS bookings (" +
                    "  booking_id INT AUTO_INCREMENT PRIMARY KEY," +
                    "  pnr_number VARCHAR(12) NOT NULL UNIQUE," +
                    "  trip_id INT NOT NULL," +
                    "  passenger_name VARCHAR(100) NOT NULL," +
                    "  passenger_age INT NOT NULL," +
                    "  passenger_gender VARCHAR(10) NOT NULL," +
                    "  contact_phone VARCHAR(15) NOT NULL," +
                    "  seats_booked INT NOT NULL DEFAULT 1," +
                    "  total_amount DECIMAL(10, 2) NOT NULL," +
                    "  booking_status VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED'," +
                    "  booking_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "  FOREIGN KEY (trip_id) REFERENCES trips(trip_id)" +
                    ")"
                );

                stmt.executeUpdate(
                    "INSERT IGNORE INTO trips (service_code, service_name, source_city, destination_city, departure_time, arrival_time, total_seats, available_seats, fare_per_seat) VALUES " +
                    "('12951', 'Mumbai Rajdhani Express', 'Mumbai', 'New Delhi', '17:00', '08:32', 60, 60, 2450.00)," +
                    "('20901', 'Vande Bharat Express', 'Mumbai', 'Gandhinagar', '06:00', '12:25', 50, 50, 1680.00)," +
                    "('12009', 'Shatabdi Express', 'Mumbai', 'Ahmedabad', '06:20', '12:45', 55, 55, 1320.00)," +
                    "('12123', 'Deccan Queen Express', 'Mumbai', 'Pune', '17:10', '20:25', 40, 40, 450.00)," +
                    "('12626', 'Kerala Express', 'New Delhi', 'Trivandrum', '20:10', '14:15', 70, 70, 2800.00)"
                );
                System.out.println("[+] MySQL tables and initial routes initialized successfully!");
            }
        } catch (SQLException e) {
            System.out.println("[INFO] Note on table initialization: " + e.getMessage());
        }
    }

    public static String getUrl() {
        return url;
    }

    public static String getUsername() {
        return username;
    }

    public static void setCredentials(String user, String pass) {
        username = user;
        password = pass;
    }
}
