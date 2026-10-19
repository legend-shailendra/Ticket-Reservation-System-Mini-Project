package com.ticketreservation;

import com.ticketreservation.config.DBConnection;
import com.ticketreservation.dao.BookingDAO;
import com.ticketreservation.dao.TripDAO;
import com.ticketreservation.model.Booking;
import com.ticketreservation.model.Trip;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

/**
 * Ticket Reservation System Mini-Project
 * Demonstrating Database Connectivity Using JDBC (Java Database Connectivity).
 */
public class TicketReservationApp {

    private static final Scanner scanner = new Scanner(System.in);
    private static final TripDAO tripDAO = new TripDAO();
    private static final BookingDAO bookingDAO = new BookingDAO();

    public static void main(String[] args) {
        printBanner();

        // Check DB connectivity at startup
        System.out.println("[*] Connecting to MySQL database using JDBC...");
        if (DBConnection.testConnection()) {
            System.out.println("[+] MySQL database connected successfully!");
            DBConnection.autoInitializeSchemaIfTablesMissing();
        } else {
            System.out.println("[-] Warning: Could not connect to MySQL database on startup.");
            System.out.println("    Please ensure your MySQL service is running,");
            System.out.println("    and verify your credentials in 'db.properties'.");
            System.out.println("    (You can still run option 8 or 9 to check diagnostics/reconfigure).\n");
        }

        boolean running = true;
        while (running) {
            printMenu();
            System.out.print("Enter your choice [0-9]: ");
            String input = scanner.nextLine().trim();

            switch (input) {
                case "1":
                    handleViewAllTrips();
                    break;
                case "2":
                    handleSearchTrips();
                    break;
                case "3":
                    handleBookTicket();
                    break;
                case "4":
                    handleCheckPNR();
                    break;
                case "5":
                    handleCancelTicket();
                    break;
                case "6":
                    handleViewAllBookings();
                    break;
                case "7":
                    handleAddNewTrip();
                    break;
                case "8":
                    DBConnection.printConnectionDiagnostics();
                    break;
                case "9":
                    handleDatabaseSetup();
                    break;
                case "0":
                    System.out.println("\nThank you for using the Ticket Reservation System. Goodbye!");
                    running = false;
                    break;
                default:
                    System.out.println("[!] Invalid choice. Please select an option between 0 and 9.");
            }

            if (running) {
                System.out.print("\nPress Enter to continue...");
                scanner.nextLine();
            }
        }
    }

    private static void printBanner() {
        System.out.println("============================================================================");
        System.out.println("                 TICKET RESERVATION SYSTEM (JDBC MINI-PROJECT)              ");
        System.out.println("      Demonstrating Database Connectivity, Transactions & CRUD using JDBC   ");
        System.out.println("============================================================================");
    }

    private static void printMenu() {
        System.out.println("\n----------------------------- MAIN MENU -----------------------------");
        System.out.println("  [1] View All Scheduled Services / Trains");
        System.out.println("  [2] Search Services (by Source & Destination)");
        System.out.println("  [3] Book a Ticket (ACID Transaction with JDBC)");
        System.out.println("  [4] Check Ticket Details by PNR");
        System.out.println("  [5] Cancel Ticket (Atomic Seat Restoration)");
        System.out.println("  [6] View All Bookings (Admin Audit Log)");
        System.out.println("  [7] Add New Route / Train Service (Admin)");
        System.out.println("  [8] Database Connectivity Diagnostics (JDBC MetaData Demo)");
        System.out.println("  [9] Verify / Initialize Database Tables");
        System.out.println("  [0] Exit");
        System.out.println("---------------------------------------------------------------------");
    }

    private static void handleViewAllTrips() {
        System.out.println("\n--- All Available Routes & Services ---");
        try {
            List<Trip> trips = tripDAO.getAllTrips();
            if (trips.isEmpty()) {
                System.out.println("[i] No routes found in the database. Use option 9 to populate sample data.");
            } else {
                TripDAO.displayTrips(trips);
            }
        } catch (SQLException e) {
            System.err.println("[-] Database Error: " + e.getMessage());
        }
    }

    private static void handleSearchTrips() {
        System.out.println("\n--- Search Routes / Services ---");
        System.out.print("Enter Source City (or press Enter to match all): ");
        String source = scanner.nextLine().trim();
        System.out.print("Enter Destination City (or press Enter to match all): ");
        String destination = scanner.nextLine().trim();

        try {
            List<Trip> trips = tripDAO.searchTrips(source, destination);
            if (trips.isEmpty()) {
                System.out.println("[i] No matching routes found between '" + source + "' and '" + destination + "'.");
            } else {
                System.out.println("\n[+] Found " + trips.size() + " matching route(s):");
                TripDAO.displayTrips(trips);
            }
        } catch (SQLException e) {
            System.err.println("[-] Database Error: " + e.getMessage());
        }
    }

    private static void handleBookTicket() {
        System.out.println("\n--- Book a Ticket (JDBC Transaction) ---");
        try {
            List<Trip> trips = tripDAO.getAllTrips();
            if (trips.isEmpty()) {
                System.out.println("[!] No routes available to book. Please initialize sample data first.");
                return;
            }
            TripDAO.displayTrips(trips);

            System.out.print("\nEnter Trip ID to book: ");
            int tripId = Integer.parseInt(scanner.nextLine().trim());

            Trip selectedTrip = tripDAO.getTripById(tripId);
            if (selectedTrip == null) {
                System.out.println("[!] Invalid Trip ID. Trip does not exist.");
                return;
            }

            System.out.println("[*] Selected: " + selectedTrip.getServiceName() + 
                               " (" + selectedTrip.getSourceCity() + " -> " + selectedTrip.getDestinationCity() + ")");
            System.out.println("[*] Available Seats: " + selectedTrip.getAvailableSeats() + 
                               " | Fare per seat: INR " + selectedTrip.getFarePerSeat());

            if (selectedTrip.getAvailableSeats() <= 0) {
                System.out.println("[!] Sorry, this service is fully booked!");
                return;
            }

            System.out.print("Enter Passenger Name: ");
            String name = scanner.nextLine().trim();
            if (name.isEmpty()) {
                System.out.println("[!] Passenger name cannot be empty.");
                return;
            }

            System.out.print("Enter Passenger Age: ");
            int age = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("Enter Passenger Gender (M/F/Other): ");
            String gender = scanner.nextLine().trim();

            System.out.print("Enter Contact Phone Number: ");
            String phone = scanner.nextLine().trim();

            System.out.print("Enter Number of Seats to Book: ");
            int seats = Integer.parseInt(scanner.nextLine().trim());

            if (seats <= 0) {
                System.out.println("[!] Number of seats must be at least 1.");
                return;
            }

            System.out.println("\n[*] Executing JDBC transaction (setAutoCommit(false), insert, update, commit)...");
            Booking booked = bookingDAO.bookTicket(tripId, name, age, gender, phone, seats);

            System.out.println("\n=======================================================");
            System.out.println("             BOOKING CONFIRMED SUCCESSFULLY!           ");
            System.out.println("=======================================================");
            System.out.println("  PNR Number       : " + booked.getPnrNumber());
            System.out.println("  Service Name     : " + booked.getServiceName());
            System.out.println("  Passenger Name   : " + booked.getPassengerName() + " (" + booked.getPassengerAge() + ", " + booked.getPassengerGender() + ")");
            System.out.println("  Seats Booked     : " + booked.getSeatsBooked());
            System.out.println("  Total Amount Paid: INR " + String.format("%.2f", booked.getTotalAmount()));
            System.out.println("  Booking Status   : " + booked.getBookingStatus());
            System.out.println("=======================================================");

        } catch (NumberFormatException e) {
            System.out.println("[!] Invalid numeric input. Please enter valid numbers.");
        } catch (SQLException e) {
            System.err.println("[-] Booking Failed: " + e.getMessage());
        }
    }

    private static void handleCheckPNR() {
        System.out.println("\n--- Check Ticket by PNR ---");
        System.out.print("Enter PNR Number (e.g. PNR123456): ");
        String pnr = scanner.nextLine().trim();

        if (pnr.isEmpty()) {
            System.out.println("[!] PNR cannot be empty.");
            return;
        }

        try {
            Booking b = bookingDAO.getBookingByPNR(pnr);
            if (b == null) {
                System.out.println("[!] No ticket found with PNR: " + pnr);
            } else {
                System.out.println("\n=======================================================");
                System.out.println("                   TICKET DETAILS                      ");
                System.out.println("=======================================================");
                System.out.println("  PNR Number     : " + b.getPnrNumber());
                System.out.println("  Service / Train: " + b.getServiceName());
                System.out.println("  Journey Route  : " + b.getSourceCity() + " -> " + b.getDestinationCity());
                System.out.println("  Departure Time : " + b.getDepartureTime());
                System.out.println("  Passenger Name : " + b.getPassengerName() + " (Age: " + b.getPassengerAge() + ", Gender: " + b.getPassengerGender() + ")");
                System.out.println("  Contact Phone  : " + b.getContactPhone());
                System.out.println("  Seats Booked   : " + b.getSeatsBooked());
                System.out.println("  Total Amount   : INR " + String.format("%.2f", b.getTotalAmount()));
                System.out.println("  Booking Date   : " + b.getBookingDate());
                System.out.println("  Status         : " + b.getBookingStatus());
                System.out.println("=======================================================");
            }
        } catch (SQLException e) {
            System.err.println("[-] Database Error: " + e.getMessage());
        }
    }

    private static void handleCancelTicket() {
        System.out.println("\n--- Cancel Ticket (Atomic Transaction) ---");
        System.out.print("Enter PNR Number to cancel: ");
        String pnr = scanner.nextLine().trim();

        if (pnr.isEmpty()) {
            System.out.println("[!] PNR cannot be empty.");
            return;
        }

        try {
            Booking b = bookingDAO.getBookingByPNR(pnr);
            if (b == null) {
                System.out.println("[!] No booking found with PNR: " + pnr);
                return;
            }

            if ("CANCELLED".equalsIgnoreCase(b.getBookingStatus())) {
                System.out.println("[!] This ticket is already marked as CANCELLED.");
                return;
            }

            System.out.println("[*] Ticket found for " + b.getPassengerName() + " on " + b.getServiceName());
            System.out.println("[*] Seats to be refunded/restored: " + b.getSeatsBooked());
            System.out.print("Are you sure you want to cancel this booking? (yes/no): ");
            String confirm = scanner.nextLine().trim();

            if (confirm.equalsIgnoreCase("yes") || confirm.equalsIgnoreCase("y")) {
                bookingDAO.cancelBooking(pnr);
                System.out.println("[+] Booking " + pnr + " has been CANCELLED successfully.");
                System.out.println("[+] " + b.getSeatsBooked() + " seat(s) restored back to service inventory.");
            } else {
                System.out.println("[i] Cancellation aborted by user.");
            }
        } catch (SQLException e) {
            System.err.println("[-] Cancellation Failed: " + e.getMessage());
        }
    }

    private static void handleViewAllBookings() {
        System.out.println("\n--- All System Bookings (Audit Log) ---");
        try {
            List<Booking> list = bookingDAO.getAllBookings();
            if (list.isEmpty()) {
                System.out.println("[i] No bookings recorded yet.");
            } else {
                BookingDAO.displayBookings(list);
            }
        } catch (SQLException e) {
            System.err.println("[-] Database Error: " + e.getMessage());
        }
    }

    private static void handleAddNewTrip() {
        System.out.println("\n--- Add New Route / Train Service (Admin) ---");
        try {
            System.out.print("Enter Service Code (e.g. 12215): ");
            String code = scanner.nextLine().trim();
            System.out.print("Enter Service Name (e.g. Garib Rath Express): ");
            String name = scanner.nextLine().trim();
            System.out.print("Enter Source City: ");
            String source = scanner.nextLine().trim();
            System.out.print("Enter Destination City: ");
            String dest = scanner.nextLine().trim();
            System.out.print("Enter Departure Time (e.g. 15:30): ");
            String dep = scanner.nextLine().trim();
            System.out.print("Enter Arrival Time (e.g. 06:15): ");
            String arr = scanner.nextLine().trim();
            System.out.print("Enter Total Capacity / Seats: ");
            int seats = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Enter Fare Per Seat (INR): ");
            double fare = Double.parseDouble(scanner.nextLine().trim());

            Trip trip = new Trip(0, code, name, source, dest, dep, arr, seats, seats, fare);
            boolean added = tripDAO.addTrip(trip);
            if (added) {
                System.out.println("[+] New service successfully added to database via PreparedStatement!");
            } else {
                System.out.println("[-] Failed to insert service.");
            }
        } catch (NumberFormatException e) {
            System.out.println("[!] Invalid number format for seats or fare.");
        } catch (SQLException e) {
            System.err.println("[-] Database Error: " + e.getMessage());
        }
    }

    private static void handleDatabaseSetup() {
        System.out.println("\n--- MySQL Database Setup & Verification ---");
        System.out.println("Active DBMS: MySQL Server");
        System.out.println("Target DB URL: " + DBConnection.getUrl());

        boolean ok = DBConnection.testConnection();
        if (ok) {
            System.out.println("[+] MySQL Connection Successful!");
            DBConnection.autoInitializeSchemaIfTablesMissing();
            DBConnection.printConnectionDiagnostics();
        } else {
            System.out.println("[-] Connection failed. Check MySQL service and credentials.");
            System.out.println("    Would you like to try entering MySQL username and password now? (yes/no): ");
            String ans = scanner.nextLine().trim();
            if ("yes".equalsIgnoreCase(ans) || "y".equalsIgnoreCase(ans)) {
                System.out.print("Enter Username: ");
                String u = scanner.nextLine().trim();
                System.out.print("Enter Password: ");
                String p = scanner.nextLine().trim();
                DBConnection.setCredentials(u, p);
                if (DBConnection.testConnection()) {
                    System.out.println("[+] Successfully connected to MySQL with provided credentials!");
                    DBConnection.autoInitializeSchemaIfTablesMissing();
                } else {
                    System.out.println("[-] Connection still failed. Please verify MySQL service and credentials in 'db.properties'.");
                }
            }
        }
    }
}
