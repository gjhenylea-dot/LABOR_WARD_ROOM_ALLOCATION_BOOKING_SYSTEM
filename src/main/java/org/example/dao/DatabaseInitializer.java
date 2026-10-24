package org.example.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void initialize() throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement()) {

            // Enable Foreign Keys explicitly
            statement.execute("PRAGMA foreign_keys = ON;");

            // 1. Initialize Rooms Table & Seed Default Inventory
            statement.execute("""
                CREATE TABLE IF NOT EXISTS rooms (
                    room_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    room_number TEXT NOT NULL UNIQUE,
                    room_type TEXT NOT NULL,
                    status TEXT DEFAULT 'AVAILABLE'
                );
            """);

            try (ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM rooms")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    statement.execute("INSERT INTO rooms (room_number, room_type, status) VALUES ('LW-101', 'Delivery Suite', 'AVAILABLE');");
                    statement.execute("INSERT INTO rooms (room_number, room_type, status) VALUES ('LW-102', 'Delivery Suite', 'AVAILABLE');");
                    statement.execute("INSERT INTO rooms (room_number, room_type, status) VALUES ('RC-201', 'Recovery Room', 'AVAILABLE');");
                    statement.execute("INSERT INTO rooms (room_number, room_type, status) VALUES ('PR-301', 'Private Ward', 'AVAILABLE');");
                    System.out.println("-> Default labor ward rooms seeded.");
                }
            }

            // 2. Initialize Patients Table
            statement.execute("""
                CREATE TABLE IF NOT EXISTS patients (
                    patient_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    full_name TEXT NOT NULL,
                    contact_number TEXT,
                    expected_due_date TEXT
                );
            """);

            // 3. Initialize Allocations Table
            statement.execute("""
                CREATE TABLE IF NOT EXISTS allocations (
                    allocation_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    patient_id INTEGER,
                    room_id INTEGER,
                    admission_time TEXT,
                    status TEXT DEFAULT 'ACTIVE',
                    FOREIGN KEY (patient_id) REFERENCES patients(patient_id),
                    FOREIGN KEY (room_id) REFERENCES rooms(room_id)
                );
            """);

            // 4. Initialize Users Table
            statement.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL UNIQUE,
                    password TEXT NOT NULL,
                    roles TEXT NOT NULL,
                    status TEXT NOT NULL DEFAULT 'PENDING'
                );
            """);

            // Seed default admin if users table is empty
            try (ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM users")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    statement.execute("""
                        INSERT INTO users (username, password, roles, status)
                        VALUES ('admin', 'admin123', 'ADMIN', 'APPROVED')
                    """);
                    System.out.println("-> Default Labor Ward Admin created (username: admin, password: admin123).");
                }
            }
        }

        System.out.println("-> Labor Ward database and tables initialized successfully.");
    }
}