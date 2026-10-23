package org.example.ui;

import org.example.dao.DatabaseConnection;
import org.example.dao.UserDao;
import org.example.model.User;

import java.sql.*;
import java.util.Scanner;

public class MenuUI {
    private final UserDao userDao = new UserDao();
    private final Scanner scanner = new Scanner(System.in);

    public void start() {
        // Ensure user table exists on startup
        UserDao.initializeUserTable();

        boolean running = true;
        while (running) {
            System.out.println("\n=====================================================");
            System.out.println("   LABOR WARD ROOM ALLOCATION & BOOKING SYSTEM");
            System.out.println("=====================================================");
            System.out.println("1. Login");
            System.out.println("2. Register New Staff Account");
            System.out.println("3. Exit System");
            System.out.print("Choose an option (1-3): ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    loginUser();
                    break;
                case "2":
                    registerUser();
                    break;
                case "3":
                    System.out.println("Exiting system. Goodbye!");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option. Please choose between 1 and 3.");
            }
        }
    }

    private void loginUser() {
        System.out.println("\n--- STAFF LOGIN ---");
        System.out.print("Enter username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Enter password: ");
        String password = scanner.nextLine().trim();

        User user = userDao.login(username, password);

        if (user != null) {
            System.out.println("\nLogin successful! Welcome, " + user.getUsername() + " (" + user.getRoles() + ")");
            if ("ADMIN".equalsIgnoreCase(user.getRoles())) {
                adminMenu(user);
            } else {
                staffMenu(user);
            }
        } else {
            System.out.println("Login failed. Check your credentials or wait for admin approval.");
        }
    }

    private void registerUser() {
        System.out.println("\n--- STAFF REGISTRATION ---");
        System.out.print("Choose a username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Choose a password: ");
        String password = scanner.nextLine().trim();
        System.out.print("Enter Role (DOCTOR / NURSE / RECEPTIONIST): ");
        String role = scanner.nextLine().trim().toUpperCase();

        boolean success = userDao.register(username, password, role);
        if (success) {
            System.out.println("Registration submitted successfully! Your account status is PENDING admin approval.");
        } else {
            System.out.println("Registration failed. Username may already exist.");
        }
    }

    private void adminMenu(User admin) {
        boolean session = true;
        while (session) {
            System.out.println("\n--- ADMIN PANEL (" + admin.getUsername() + ") ---");
            System.out.println("1. View Room Status & Inventory");
            System.out.println("2. Add New Labor Ward Room");
            System.out.println("3. View Pending Staff Approvals");
            System.out.println("4. Approve Pending Staff Accounts");
            System.out.println("5. View All Active Allocations");
            System.out.println("6. Logout");
            System.out.print("Choose an option (1-6): ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    viewAllRooms();
                    break;
                case "2":
                    addNewRoom();
                    break;
                case "3":
                    viewPendingUsers();
                    break;
                case "4":
                    approveUserAccount();
                    break;
                case "5":
                    viewActiveAllocations();
                    break;
                case "6":
                    System.out.println("Logging out...");
                    session = false;
                    break;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private void staffMenu(User staff) {
        boolean session = true;
        while (session) {
            System.out.println("\n--- LABOR WARD MANAGEMENT (" + staff.getRoles() + ": " + staff.getUsername() + ") ---");
            System.out.println("1. View Available Rooms");
            System.out.println("2. Register New Patient");
            System.out.println("3. Allocate Room / Book Bed for Patient");
            System.out.println("4. View Active Allocations");
            System.out.println("5. Discharge Patient & Release Room");
            System.out.println("6. Logout");
            System.out.print("Choose an option (1-6): ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    viewAvailableRooms();
                    break;
                case "2":
                    registerPatient();
                    break;
                case "3":
                    allocateRoom();
                    break;
                case "4":
                    viewActiveAllocations();
                    break;
                case "5":
                    dischargePatient();
                    break;
                case "6":
                    System.out.println("Logging out...");
                    session = false;
                    break;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

    // --- HELPER METHODS FOR SYSTEM FEATURES ---

    private void viewAllRooms() {
        String query = "SELECT * FROM rooms;";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            System.out.println("\n--- ROOM INVENTORY ---");
            System.out.printf("%-8s | %-15s | %-20s | %-10s%n", "ID", "Room Number", "Type", "Status");
            System.out.println("---------------------------------------------------------");
            while (rs.next()) {
                System.out.printf("%-8d | %-15s | %-20s | %-10s%n",
                        rs.getInt("room_id"),
                        rs.getString("room_number"),
                        rs.getString("room_type"),
                        rs.getString("status"));
            }
        } catch (SQLException e) {
            System.out.println("Error fetching rooms: " + e.getMessage());
        }
    }

    private void viewAvailableRooms() {
        String query = "SELECT * FROM rooms WHERE status = 'AVAILABLE';";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            System.out.println("\n--- AVAILABLE ROOMS / BEDS ---");
            System.out.printf("%-8s | %-15s | %-20s%n", "ID", "Room Number", "Type");
            System.out.println("----------------------------------------");
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("%-8d | %-15s | %-20s%n",
                        rs.getInt("room_id"),
                        rs.getString("room_number"),
                        rs.getString("room_type"));
            }
            if (!found) {
                System.out.println("No rooms currently available.");
            }
        } catch (SQLException e) {
            System.out.println("Error fetching available rooms: " + e.getMessage());
        }
    }

    private void addNewRoom() {
        System.out.print("Enter Room Number (e.g., LW-103): ");
        String roomNumber = scanner.nextLine().trim();
        System.out.print("Enter Room Type (e.g., Delivery Suite, Recovery Room, Private Ward): ");
        String roomType = scanner.nextLine().trim();

        String sql = "INSERT INTO rooms (room_number, room_type, status) VALUES (?, ?, 'AVAILABLE');";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, roomNumber);
            pstmt.setString(2, roomType);
            pstmt.executeUpdate();
            System.out.println("Room successfully added to inventory!");
        } catch (SQLException e) {
            System.out.println("Error adding room (Room number may already exist): " + e.getMessage());
        }
    }

    private void viewPendingUsers() {
        String query = "SELECT id, username, roles FROM users WHERE status = 'PENDING';";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            System.out.println("\n--- PENDING STAFF ACCOUNTS ---");
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("ID: %d | Username: %s | Role: %s%n",
                        rs.getInt("id"), rs.getString("username"), rs.getString("roles"));
            }
            if (!found) {
                System.out.println("No pending accounts found.");
            }
        } catch (SQLException e) {
            System.out.println("Error fetching pending users: " + e.getMessage());
        }
    }

    private void approveUserAccount() {
        viewPendingUsers();
        System.out.print("Enter User ID to APPROVE: ");
        String idStr = scanner.nextLine().trim();

        String sql = "UPDATE users SET status = 'ACTIVE' WHERE id = ?;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, Integer.parseInt(idStr));
            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                System.out.println("User account approved and activated successfully!");
            } else {
                System.out.println("User ID not found.");
            }
        } catch (Exception e) {
            System.out.println("Error approving user: " + e.getMessage());
        }
    }

    private void registerPatient() {
        System.out.println("\n--- REGISTER NEW PATIENT ---");
        System.out.print("Full Name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Contact Number: ");
        String contact = scanner.nextLine().trim();
        System.out.print("Expected Due Date (YYYY-MM-DD): ");
        String dueDate = scanner.nextLine().trim();

        String sql = "INSERT INTO patients (full_name, contact_number, expected_due_date) VALUES (?, ?, ?);";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, contact);
            pstmt.setString(3, dueDate);
            pstmt.executeUpdate();
            System.out.println("Patient registered successfully!");
        } catch (SQLException e) {
            System.out.println("Error registering patient: " + e.getMessage());
        }
    }

    private void allocateRoom() {
        System.out.println("\n--- ALLOCATE ROOM / BOOKING ---");
        // Show patients
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT patient_id, full_name FROM patients;")) {
            System.out.println("Patients:");
            while (rs.next()) {
                System.out.printf("[%d] %s%n", rs.getInt("patient_id"), rs.getString("full_name"));
            }
        } catch (SQLException e) {
            System.out.println("Error loading patients.");
            return;
        }

        System.out.print("Enter Patient ID: ");
        int patientId = Integer.parseInt(scanner.nextLine().trim());

        viewAvailableRooms();
        System.out.print("Enter Room ID to Allocate: ");
        int roomId = Integer.parseInt(scanner.nextLine().trim());

        String insertAllocation = "INSERT INTO allocations (patient_id, room_id, admission_time, status) VALUES (?, ?, datetime('now'), 'ACTIVE');";
        String updateRoom = "UPDATE rooms SET status = 'OCCUPIED' WHERE room_id = ?;";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false); // Transaction start

            try (PreparedStatement stmt1 = conn.prepareStatement(insertAllocation);
                 PreparedStatement stmt2 = conn.prepareStatement(updateRoom)) {

                stmt1.setInt(1, patientId);
                stmt1.setInt(2, roomId);
                stmt1.executeUpdate();

                stmt2.setInt(1, roomId);
                stmt2.executeUpdate();

                conn.commit();
                System.out.println("Room allocated successfully and room status updated to OCCUPIED!");
            } catch (SQLException e) {
                conn.rollback();
                System.out.println("Allocation failed, transaction rolled back: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println("Database connection error during allocation: " + e.getMessage());
        }
    }

    private void viewActiveAllocations() {
        String query = """
                SELECT a.allocation_id, p.full_name, r.room_number, r.room_type, a.admission_time, a.status
                FROM allocations a
                JOIN patients p ON a.patient_id = p.patient_id
                JOIN rooms r ON a.room_id = r.room_id
                WHERE a.status = 'ACTIVE';
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            System.out.println("\n--- ACTIVE ROOM ALLOCATIONS ---");
            System.out.printf("%-6s | %-20s | %-12s | %-18s | %-20s%n", "Alloc ID", "Patient Name", "Room", "Type", "Admission Time");
            System.out.println("-----------------------------------------------------------------------------------");
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("%-6d | %-20s | %-12s | %-18s | %-20s%n",
                        rs.getInt("allocation_id"),
                        rs.getString("full_name"),
                        rs.getString("room_number"),
                        rs.getString("room_type"),
                        rs.getString("admission_time"));
            }
            if (!found) {
                System.out.println("No active allocations found.");
            }
        } catch (SQLException e) {
            System.out.println("Error fetching allocations: " + e.getMessage());
        }
    }

    private void dischargePatient() {
        viewActiveAllocations();
        System.out.print("Enter Allocation ID to Discharge: ");
        int allocId = Integer.parseInt(scanner.nextLine().trim());

        // Find room_id for this allocation so we can free it up
        String findRoomSql = "SELECT room_id FROM allocations WHERE allocation_id = ? AND status = 'ACTIVE';";
        String updateAllocSql = "UPDATE allocations SET status = 'DISCHARGED' WHERE allocation_id = ?;";
        String updateRoomSql = "UPDATE rooms SET status = 'AVAILABLE' WHERE room_id = ?;";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            int roomId = -1;
            try (PreparedStatement findStmt = conn.prepareStatement(findRoomSql)) {
                findStmt.setInt(1, allocId);
                try (ResultSet rs = findStmt.executeQuery()) {
                    if (rs.next()) {
                        roomId = rs.getInt("room_id");
                    }
                }
            }

            if (roomId == -1) {
                System.out.println("Invalid Allocation ID or already discharged.");
                return;
            }

            try (PreparedStatement allocStmt = conn.prepareStatement(updateAllocSql);
                 PreparedStatement roomStmt = conn.prepareStatement(updateRoomSql)) {

                allocStmt.setInt(1, allocId);
                allocStmt.executeUpdate();

                roomStmt.setInt(1, roomId);
                roomStmt.executeUpdate();

                conn.commit();
                System.out.println("Patient discharged successfully! Room is now AVAILABLE.");
            } catch (SQLException e) {
                conn.rollback();
                System.out.println("Discharge failed, rolling back: " + e.getMessage());
            }

        } catch (SQLException e) {
            System.out.println("Database error during discharge: " + e.getMessage());
        }
    }
}