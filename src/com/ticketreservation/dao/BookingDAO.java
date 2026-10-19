package com.ticketreservation.dao;

import com.ticketreservation.config.DBConnection;
import com.ticketreservation.model.Booking;
import com.ticketreservation.util.TableFormatter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Data Access Object for Booking entity.
 * Demonstrates JDBC Transaction Management (setAutoCommit(false), commit, rollback).
 */
public class BookingDAO {

    /**
     * Books a ticket using an ATOMIC TRANSACTION to prevent race conditions & overbooking.
     */
    public Booking bookTicket(int tripId, String passengerName, int age, String gender, 
                              String phone, int seatsRequested) throws SQLException {
        Connection conn = null;
        PreparedStatement checkStmt = null;
        PreparedStatement insertStmt = null;
        PreparedStatement updateTripStmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();

            // STEP 1: Begin JDBC Transaction
            conn.setAutoCommit(false);

            // STEP 2: Verify available seats (with row lock logic)
            String checkSql = "SELECT service_name, available_seats, fare_per_seat FROM trips WHERE trip_id = ?";
            checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setInt(1, tripId);
            rs = checkStmt.executeQuery();

            if (!rs.next()) {
                throw new SQLException("Trip ID #" + tripId + " does not exist.");
            }

            String serviceName = rs.getString("service_name");
            int availableSeats = rs.getInt("available_seats");
            double farePerSeat = rs.getDouble("fare_per_seat");

            if (availableSeats < seatsRequested) {
                conn.rollback(); // Rollback transaction
                throw new SQLException("Insufficient seats available! Requested: " + seatsRequested + 
                                       ", Available: " + availableSeats);
            }

            // STEP 3: Generate unique PNR
            String pnr = "PNR" + (100000 + new Random().nextInt(900000));
            double totalAmount = farePerSeat * seatsRequested;

            // STEP 4: Insert booking record
            String insertSql = "INSERT INTO bookings (pnr_number, trip_id, passenger_name, passenger_age, " +
                               "passenger_gender, contact_phone, seats_booked, total_amount, booking_status) " +
                               "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'CONFIRMED')";
            insertStmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
            insertStmt.setString(1, pnr);
            insertStmt.setInt(2, tripId);
            insertStmt.setString(3, passengerName);
            insertStmt.setInt(4, age);
            insertStmt.setString(5, gender);
            insertStmt.setString(6, phone);
            insertStmt.setInt(7, seatsRequested);
            insertStmt.setDouble(8, totalAmount);
            insertStmt.executeUpdate();

            // STEP 5: Decrement available seats in trips table
            String updateSql = "UPDATE trips SET available_seats = available_seats - ? WHERE trip_id = ?";
            updateTripStmt = conn.prepareStatement(updateSql);
            updateTripStmt.setInt(1, seatsRequested);
            updateTripStmt.setInt(2, tripId);
            updateTripStmt.executeUpdate();

            // STEP 6: Commit Transaction (Atomicity preserved)
            conn.commit();

            // Return booking object
            Booking booking = new Booking();
            booking.setPnrNumber(pnr);
            booking.setTripId(tripId);
            booking.setServiceName(serviceName);
            booking.setPassengerName(passengerName);
            booking.setPassengerAge(age);
            booking.setPassengerGender(gender);
            booking.setContactPhone(phone);
            booking.setSeatsBooked(seatsRequested);
            booking.setTotalAmount(totalAmount);
            booking.setBookingStatus("CONFIRMED");
            return booking;

        } catch (SQLException e) {
            // STEP 7: Rollback in case of failure
            if (conn != null) {
                try {
                    System.err.println("[!] Transaction failed. Executing ROLLBACK...");
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Rollback error: " + ex.getMessage());
                }
            }
            throw e;
        } finally {
            // STEP 8: Clean up and restore autoCommit
            if (rs != null) try { rs.close(); } catch (Exception ignored) {}
            if (checkStmt != null) try { checkStmt.close(); } catch (Exception ignored) {}
            if (insertStmt != null) try { insertStmt.close(); } catch (Exception ignored) {}
            if (updateTripStmt != null) try { updateTripStmt.close(); } catch (Exception ignored) {}
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (Exception ignored) {}
            }
        }
    }

    /**
     * Cancels a booking atomically and restores available seats.
     */
    public boolean cancelBooking(String pnr) throws SQLException {
        Connection conn = null;
        PreparedStatement findStmt = null;
        PreparedStatement cancelStmt = null;
        PreparedStatement restoreSeatsStmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Begin transaction

            // 1. Look up booking
            String findSql = "SELECT trip_id, seats_booked, booking_status FROM bookings WHERE pnr_number = ?";
            findStmt = conn.prepareStatement(findSql);
            findStmt.setString(1, pnr);
            rs = findStmt.executeQuery();

            if (!rs.next()) {
                conn.rollback();
                throw new SQLException("No booking found with PNR: " + pnr);
            }

            int tripId = rs.getInt("trip_id");
            int seatsBooked = rs.getInt("seats_booked");
            String status = rs.getString("booking_status");

            if ("CANCELLED".equalsIgnoreCase(status)) {
                conn.rollback();
                throw new SQLException("Booking " + pnr + " is ALREADY CANCELLED.");
            }

            // 2. Mark booking as CANCELLED
            String cancelSql = "UPDATE bookings SET booking_status = 'CANCELLED' WHERE pnr_number = ?";
            cancelStmt = conn.prepareStatement(cancelSql);
            cancelStmt.setString(1, pnr);
            cancelStmt.executeUpdate();

            // 3. Restore seats
            String restoreSql = "UPDATE trips SET available_seats = available_seats + ? WHERE trip_id = ?";
            restoreSeatsStmt = conn.prepareStatement(restoreSql);
            restoreSeatsStmt.setInt(1, seatsBooked);
            restoreSeatsStmt.setInt(2, tripId);
            restoreSeatsStmt.executeUpdate();

            // 4. Commit transaction
            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            throw e;
        } finally {
            if (rs != null) try { rs.close(); } catch (Exception ignored) {}
            if (findStmt != null) try { findStmt.close(); } catch (Exception ignored) {}
            if (cancelStmt != null) try { cancelStmt.close(); } catch (Exception ignored) {}
            if (restoreSeatsStmt != null) try { restoreSeatsStmt.close(); } catch (Exception ignored) {}
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (Exception ignored) {}
            }
        }
    }

    /**
     * Retrieves full booking details using SQL JOIN.
     */
    public Booking getBookingByPNR(String pnr) throws SQLException {
        String sql = "SELECT b.*, t.service_name, t.source_city, t.destination_city, t.departure_time " +
                     "FROM bookings b " +
                     "JOIN trips t ON b.trip_id = t.trip_id " +
                     "WHERE b.pnr_number = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, pnr);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Booking b = new Booking();
                    b.setBookingId(rs.getInt("booking_id"));
                    b.setPnrNumber(rs.getString("pnr_number"));
                    b.setTripId(rs.getInt("trip_id"));
                    b.setPassengerName(rs.getString("passenger_name"));
                    b.setPassengerAge(rs.getInt("passenger_age"));
                    b.setPassengerGender(rs.getString("passenger_gender"));
                    b.setContactPhone(rs.getString("contact_phone"));
                    b.setSeatsBooked(rs.getInt("seats_booked"));
                    b.setTotalAmount(rs.getDouble("total_amount"));
                    b.setBookingStatus(rs.getString("booking_status"));
                    b.setBookingDate(rs.getTimestamp("booking_date"));
                    b.setServiceName(rs.getString("service_name"));
                    b.setSourceCity(rs.getString("source_city"));
                    b.setDestinationCity(rs.getString("destination_city"));
                    b.setDepartureTime(rs.getString("departure_time"));
                    return b;
                }
            }
        }
        return null;
    }

    /**
     * Retrieves all bookings for reporting.
     */
    public List<Booking> getAllBookings() throws SQLException {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT b.*, t.service_name, t.source_city, t.destination_city, t.departure_time " +
                     "FROM bookings b " +
                     "JOIN trips t ON b.trip_id = t.trip_id " +
                     "ORDER BY b.booking_id DESC";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Booking b = new Booking();
                b.setBookingId(rs.getInt("booking_id"));
                b.setPnrNumber(rs.getString("pnr_number"));
                b.setTripId(rs.getInt("trip_id"));
                b.setPassengerName(rs.getString("passenger_name"));
                b.setPassengerAge(rs.getInt("passenger_age"));
                b.setPassengerGender(rs.getString("passenger_gender"));
                b.setContactPhone(rs.getString("contact_phone"));
                b.setSeatsBooked(rs.getInt("seats_booked"));
                b.setTotalAmount(rs.getDouble("total_amount"));
                b.setBookingStatus(rs.getString("booking_status"));
                b.setBookingDate(rs.getTimestamp("booking_date"));
                b.setServiceName(rs.getString("service_name"));
                b.setSourceCity(rs.getString("source_city"));
                b.setDestinationCity(rs.getString("destination_city"));
                b.setDepartureTime(rs.getString("departure_time"));
                list.add(b);
            }
        }
        return list;
    }

    public static void displayBookings(List<Booking> list) {
        List<String> headers = Arrays.asList(
            "PNR", "Passenger", "Service/Train", "Route", "Seats", "Total (INR)", "Status", "Date"
        );

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        List<List<String>> rows = new ArrayList<>();
        for (Booking b : list) {
            String route = (b.getSourceCity() != null ? b.getSourceCity() : "") + " -> " + 
                           (b.getDestinationCity() != null ? b.getDestinationCity() : "");
            String dateStr = b.getBookingDate() != null ? sdf.format(b.getBookingDate()) : "N/A";
            rows.add(Arrays.asList(
                b.getPnrNumber(),
                b.getPassengerName() + " (" + b.getPassengerAge() + ", " + b.getPassengerGender() + ")",
                b.getServiceName() != null ? b.getServiceName() : ("Trip #" + b.getTripId()),
                route,
                String.valueOf(b.getSeatsBooked()),
                String.format("%.2f", b.getTotalAmount()),
                b.getBookingStatus(),
                dateStr
            ));
        }

        TableFormatter.printCustomTable(headers, rows);
    }
}
