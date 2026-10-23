package org.example.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.SQLException;

public class DatabaseConnection {
    // SQLite database file will be created in your project root directory
    private static final String URL = "jdbc:sqlite:labor_ward.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void initializeDatabase() {
        // SQL statements to create necessary tables for the Labor Ward System
        String createRoomsTable = "CREATE TABLE IF NOT EXISTS rooms (" +
                "room_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "room_number TEXT NOT NULL UNIQUE, " +
                "room_type TEXT NOT NULL, " + // e.g., Delivery Suite, Recovery Room, Private Room
                "status TEXT DEFAULT 'AVAILABLE'" + // AVAILABLE, OCCUPIED, MAINTENANCE
                ");";

        String createPatientsTable = "CREATE TABLE IF NOT EXISTS patients (" +
                "patient_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "full_name TEXT NOT NULL, " +
                "contact_number TEXT, " +
                "expected_due_date TEXT" +
                ");";

        String createAllocationsTable = "CREATE TABLE IF NOT EXISTS allocations (" +
                "allocation_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "patient_id INTEGER, " +
                "room_id INTEGER, " +
                "admission_time TEXT, " +
                "status TEXT DEFAULT 'ACTIVE', " + // ACTIVE, DISCHARGED
                "FOREIGN KEY (patient_id) REFERENCES patients(patient_id), " +
                "FOREIGN KEY (room_id) REFERENCES rooms(room_id)" +
                ");";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Execute table creation queries
            stmt.execute(createRoomsTable);
            stmt.execute(createPatientsTable);
            stmt.execute(createAllocationsTable);

            System.out.println("-> Database tables initialized successfully.");

            // Seed initial sample rooms if the room table is empty
            seedDefaultRooms(conn);

        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
        }
    }

    private static void seedDefaultRooms(Connection conn) {
        String checkRooms = "SELECT COUNT(*) FROM rooms;";
        try (Statement stmt = conn.createStatement();
             var rs = stmt.executeQuery(checkRooms)) {
            if (rs.next() && rs.getInt(1) == 0) {
                stmt.execute("INSERT INTO rooms (room_number, room_type, status) VALUES ('LW-101', 'Delivery Suite', 'AVAILABLE');");
                stmt.execute("INSERT INTO rooms (room_number, room_type, status) VALUES ('LW-102', 'Delivery Suite', 'AVAILABLE');");
                stmt.execute("INSERT INTO rooms (room_number, room_type, status) VALUES ('RC-201', 'Recovery Room', 'AVAILABLE');");
                stmt.execute("INSERT INTO rooms (room_number, room_type, status) VALUES ('PR-301', 'Private Ward', 'AVAILABLE');");
                System.out.println("-> Default labor ward rooms seeded.");
            }
        } catch (SQLException e) {
            System.err.println("Error seeding default rooms: " + e.getMessage());
        }
    }
}