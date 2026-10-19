@echo off
echo ===================================================
echo Starting Ticket Reservation System (JDBC Project)
echo ===================================================

REM Check for installed JDK 24 or fallback to java in PATH
set JAVA_CMD="C:\Program Files\Java\jdk-24\bin\java.exe"
if not exist %JAVA_CMD% (
    set JAVA_CMD=java
)

%JAVA_CMD% -cp "bin;lib/*;." com.ticketreservation.TicketReservationApp
pause
