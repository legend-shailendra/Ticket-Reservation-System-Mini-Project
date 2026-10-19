@echo off
echo ===================================================
echo Compiling Ticket Reservation System (JDBC Project)
echo ===================================================

if not exist bin mkdir bin

REM Check for installed JDK 24 or fallback to javac in PATH
set JAVAC_CMD="C:\Program Files\Java\jdk-24\bin\javac.exe"
if not exist %JAVAC_CMD% (
    set JAVAC_CMD=javac
)

%JAVAC_CMD% -d bin -cp "lib/*;src" src\com\ticketreservation\*.java src\com\ticketreservation\config\*.java src\com\ticketreservation\model\*.java src\com\ticketreservation\dao\*.java src\com\ticketreservation\util\*.java

if %ERRORLEVEL% EQU 0 (
    echo [SUCCESS] Project compiled successfully!
    echo Class files generated in 'bin\' directory.
) else (
    echo [ERROR] Compilation failed. Check JDK installation.
)
pause
