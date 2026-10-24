package org.example.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void initialize() throws SQLException {

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement()) {

            // 1. Initialize Rooms Table & Seed Default Inventory
            initializeRoomsTable(statement);

            // 2. Initialize Patients Table
            initializePatientsTable(statement);

            // 3. Initialize Allocations Table (with foreign keys)
            initializeAllocationsTable(statement);

            // 4. Initialize Users Table with schema checks & migration safety
            initializeUsersTable(connection, statement);
        }

        System.out.println("-> Labor Ward database and tables initialized successfully.");
    }

    private static void initializeRoomsTable(Statement statement) throws SQLException {
        statement.execute("""
            CREATE TABLE IF NOT EXISTS rooms (
                room_id INTEGER PRIMARY KEY AUTOINCREMENT,
                room_number TEXT NOT NULL UNIQUE,
                room_type TEXT NOT NULL,
                status TEXT DEFAULT 'AVAILABLE'
            )
            """);

        // Seed default labor ward rooms if table is empty
        try (ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM rooms")) {
            if (rs.next() && rs.getInt(1) == 0) {
                statement.execute("INSERT INTO rooms (room_number, room_type, status) VALUES ('LW-101', 'Delivery Suite', 'AVAILABLE');");
                statement.execute("INSERT INTO rooms (room_number, room_type, status) VALUES ('LW-102', 'Delivery Suite', 'AVAILABLE');");
                statement.execute("INSERT INTO rooms (room_number, room_type, status) VALUES ('RC-201', 'Recovery Room', 'AVAILABLE');");
                statement.execute("INSERT INTO rooms (room_number, room_type, status) VALUES ('PR-301', 'Private Ward', 'AVAILABLE');");
                System.out.println("-> Default labor ward rooms seeded.");
            }
        }
    }

    private static void initializePatientsTable(Statement statement) throws SQLException {
        statement.execute("""
            CREATE TABLE IF NOT EXISTS patients (
                patient_id INTEGER PRIMARY KEY AUTOINCREMENT,
                full_name TEXT NOT NULL,
                contact_number TEXT,
                expected_due_date TEXT
            )
            """);
    }

    private static void initializeAllocationsTable(Statement statement) throws SQLException {
        statement.execute("""
            CREATE TABLE IF NOT EXISTS allocations (
                allocation_id INTEGER PRIMARY KEY AUTOINCREMENT,
                patient_id INTEGER,
                room_id INTEGER,
                admission_time TEXT,
                status TEXT DEFAULT 'ACTIVE',
                FOREIGN KEY (patient_id) REFERENCES patients(patient_id),
                FOREIGN KEY (room_id) REFERENCES rooms(room_id)
            )
            """);
    }

    private static void initializeUsersTable(Connection connection, Statement statement) throws SQLException {
        boolean oldTableExists = false;
        String tableSql = null;

        try (ResultSet result = statement.executeQuery(
                "SELECT sql FROM sqlite_master " +
                        "WHERE type = 'table' AND name = 'users'")) {

            if (result.next()) {
                oldTableExists = true;
                tableSql = result.getString("sql");
            }
        }

        if (!oldTableExists) {
            statement.execute("""
                CREATE TABLE users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL UNIQUE,
                    password TEXT NOT NULL,
                    roles TEXT NOT NULL,
                    status TEXT NOT NULL DEFAULT 'PENDING'
                )
                """);
            seedDefaultAdmin(statement);

        } else {
            // Example of checking table definition or performing a safe migration if legacy roles exist
            if (tableSql != null && (tableSql.contains("'TEACHER'") || tableSql.contains("'STUDENT'"))) {

                statement.execute("PRAGMA foreign_keys = OFF");
                connection.setAutoCommit(false);

                try {
                    statement.execute("""
                        CREATE TABLE users_new (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            username TEXT NOT NULL UNIQUE,
                            password TEXT NOT NULL,
                            roles TEXT NOT NULL,
                            status TEXT NOT NULL DEFAULT 'PENDING'
                        )
                        """);

                    statement.execute("""
                        INSERT INTO users_new
                            (id, username, password, roles, status)
                        SELECT id, username, password, roles, status
                        FROM users
                        WHERE roles IN ('ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST')
                        """);

                    statement.execute("DROP TABLE users");
                    statement.execute("ALTER TABLE users_new RENAME TO users");

                    connection.commit();
                    System.out.println("-> Migrated users table successfully to Labor Ward roles.");

                } catch (SQLException e) {
                    connection.rollback();
                    throw e;

                } finally {
                    connection.setAutoCommit(true);
                    statement.execute("PRAGMA foreign_keys = ON");
                }
            }

            // Ensure default admin exists if table is empty
            try (ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM users")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    seedDefaultAdmin(statement);
                }
            }
        }
    }

    private static void seedDefaultAdmin(Statement statement) throws SQLException {
        statement.execute("""
            INSERT INTO users (username, password, roles, status)
            VALUES ('admin', 'admin123', 'ADMIN', 'ACTIVE')
            """);
        System.out.println("-> Default Labor Ward Admin created (username: admin, password: admin123).");
    }
}