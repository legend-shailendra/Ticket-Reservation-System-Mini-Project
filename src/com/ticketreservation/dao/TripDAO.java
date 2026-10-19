package com.ticketreservation.dao;

import com.ticketreservation.config.DBConnection;
import com.ticketreservation.model.Trip;
import com.ticketreservation.util.TableFormatter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Data Access Object for Trip/Route entity.
 * Demonstrates JDBC Statement, PreparedStatement, and ResultSets.
 */
public class TripDAO {

    public List<Trip> getAllTrips() throws SQLException {
        List<Trip> list = new ArrayList<>();
        String sql = "SELECT * FROM trips ORDER BY trip_id ASC";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapResultSetToTrip(rs));
            }
        }
        return list;
    }

    public List<Trip> searchTrips(String source, String destination) throws SQLException {
        List<Trip> list = new ArrayList<>();
        String sql = "SELECT * FROM trips WHERE LOWER(source_city) LIKE LOWER(?) AND LOWER(destination_city) LIKE LOWER(?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + source + "%");
            pstmt.setString(2, "%" + destination + "%");

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToTrip(rs));
                }
            }
        }
        return list;
    }

    public Trip getTripById(int tripId) throws SQLException {
        String sql = "SELECT * FROM trips WHERE trip_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, tripId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTrip(rs);
                }
            }
        }
        return null;
    }

    public boolean addTrip(Trip trip) throws SQLException {
        String sql = "INSERT INTO trips (service_code, service_name, source_city, destination_city, " +
                     "departure_time, arrival_time, total_seats, available_seats, fare_per_seat) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, trip.getServiceCode());
            pstmt.setString(2, trip.getServiceName());
            pstmt.setString(3, trip.getSourceCity());
            pstmt.setString(4, trip.getDestinationCity());
            pstmt.setString(5, trip.getDepartureTime());
            pstmt.setString(6, trip.getArrivalTime());
            pstmt.setInt(7, trip.getTotalSeats());
            pstmt.setInt(8, trip.getAvailableSeats());
            pstmt.setDouble(9, trip.getFarePerSeat());

            int rows = pstmt.executeUpdate();
            return rows > 0;
        }
    }

    public static void displayTrips(List<Trip> trips) {
        List<String> headers = Arrays.asList(
            "Trip ID", "Service Code", "Name", "From", "To", "Departure", "Arrival", "Available", "Fare (INR)"
        );

        List<List<String>> rows = new ArrayList<>();
        for (Trip t : trips) {
            rows.add(Arrays.asList(
                String.valueOf(t.getTripId()),
                t.getServiceCode(),
                t.getServiceName(),
                t.getSourceCity(),
                t.getDestinationCity(),
                t.getDepartureTime(),
                t.getArrivalTime(),
                t.getAvailableSeats() + "/" + t.getTotalSeats(),
                String.format("%.2f", t.getFarePerSeat())
            ));
        }

        TableFormatter.printCustomTable(headers, rows);
    }

    private Trip mapResultSetToTrip(ResultSet rs) throws SQLException {
        return new Trip(
            rs.getInt("trip_id"),
            rs.getString("service_code"),
            rs.getString("service_name"),
            rs.getString("source_city"),
            rs.getString("destination_city"),
            rs.getString("departure_time"),
            rs.getString("arrival_time"),
            rs.getInt("total_seats"),
            rs.getInt("available_seats"),
            rs.getDouble("fare_per_seat")
        );
    }
}
