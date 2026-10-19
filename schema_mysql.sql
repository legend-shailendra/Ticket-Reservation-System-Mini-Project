-- ====================================================================
-- Ticket Reservation System Database Schema (MySQL)
-- Ticket Reservation System Mini-Project - JDBC Database Connectivity
-- ====================================================================

-- 1. Create and switch to Database
CREATE DATABASE IF NOT EXISTS ticket_reservation_db;
USE ticket_reservation_db;

-- 2. Drop existing tables if re-running
DROP TABLE IF EXISTS bookings;
DROP TABLE IF EXISTS trips;

-- 3. Create 'trips' table (Trains / Buses / Routes)
CREATE TABLE trips (
    trip_id INT AUTO_INCREMENT PRIMARY KEY,
    service_code VARCHAR(20) NOT NULL UNIQUE,       -- e.g. '12951' or 'VB-202'
    service_name VARCHAR(100) NOT NULL,             -- e.g. 'Mumbai Rajdhani Express'
    source_city VARCHAR(50) NOT NULL,               -- e.g. 'Mumbai'
    destination_city VARCHAR(50) NOT NULL,          -- e.g. 'New Delhi'
    departure_time VARCHAR(20) NOT NULL,            -- e.g. '16:35'
    arrival_time VARCHAR(20) NOT NULL,              -- e.g. '08:30'
    total_seats INT NOT NULL,                       -- e.g. 50
    available_seats INT NOT NULL,                   -- tracks current capacity
    fare_per_seat DECIMAL(10, 2) NOT NULL,          -- Ticket price
    CONSTRAINT chk_seats CHECK (available_seats >= 0 AND available_seats <= total_seats)
);

-- 4. Create 'bookings' table
CREATE TABLE bookings (
    booking_id INT AUTO_INCREMENT PRIMARY KEY,
    pnr_number VARCHAR(12) NOT NULL UNIQUE,         -- Generated PNR (e.g. 'PNR847291')
    trip_id INT NOT NULL,
    passenger_name VARCHAR(100) NOT NULL,
    passenger_age INT NOT NULL,
    passenger_gender VARCHAR(10) NOT NULL,
    contact_phone VARCHAR(15) NOT NULL,
    seats_booked INT NOT NULL DEFAULT 1,
    total_amount DECIMAL(10, 2) NOT NULL,
    booking_status VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED', -- 'CONFIRMED', 'CANCELLED'
    booking_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_trip FOREIGN KEY (trip_id) REFERENCES trips(trip_id) ON DELETE RESTRICT
);

-- 5. Seed Initial Data (Trains / Transport Services)
INSERT INTO trips (service_code, service_name, source_city, destination_city, departure_time, arrival_time, total_seats, available_seats, fare_per_seat)
VALUES 
('12951', 'Mumbai Rajdhani Express', 'Mumbai', 'New Delhi', '17:00', '08:32', 60, 60, 2450.00),
('20901', 'Vande Bharat Express', 'Mumbai', 'Gandhinagar', '06:00', '12:25', 50, 50, 1680.00),
('12009', 'Shatabdi Express', 'Mumbai', 'Ahmedabad', '06:20', '12:45', 55, 55, 1320.00),
('12123', 'Deccan Queen Express', 'Mumbai', 'Pune', '17:10', '20:25', 40, 40, 450.00),
('12626', 'Kerala Express', 'New Delhi', 'Trivandrum', '20:10', '14:15', 70, 70, 2800.00),
('22692', 'Bengaluru Rajdhani', 'New Delhi', 'Bengaluru', '20:50', '05:20', 65, 65, 2950.00),
('12840', 'Howrah Mail', 'Chennai', 'Howrah', '19:00', '03:50', 50, 50, 1850.00);

-- Verify Seed Data
SELECT * FROM trips;
