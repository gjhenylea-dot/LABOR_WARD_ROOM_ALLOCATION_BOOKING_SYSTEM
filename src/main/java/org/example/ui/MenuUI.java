package org.example.ui;

import org.example.dao.ActivityLogDao;
import org.example.dao.DatabaseConnection;
import org.example.dao.UserDao;
import org.example.model.ActivityLog;
import org.example.model.User;

import java.sql.*;
import java.util.List;
import java.util.Scanner;

public class MenuUI {
    private final UserDao userDao = new UserDao();
    private final ActivityLogDao activityLogDao = new ActivityLogDao();
    private final Scanner scanner = new Scanner(System.in);

    private void displayUserTable(List<User> users) {
        System.out.printf("%-5s | %-15s | %-12s | %-10s%n", "ID", "Username", "Role", "Status");
        System.out.println("---------------------------------------------------------");
        if (users.isEmpty()) {
            System.out.println("No accounts found.");
        } else {
            for (User u : users) {
                System.out.printf("%-5d | %-15s | %-12s | %-10s%n",
                        u.getId(),
                        u.getUsername(),
                        u.getRoles(),
                        u.getStatus());
            }
        }
    }

    private void displayAuditLogTable(List<ActivityLog> logs) {
        System.out.printf("%-5s | %-8s | %-18s | %-35s | %-20s%n", "ID", "User ID", "Action Type", "Description", "Timestamp");
        System.out.println("---------------------------------------------------------------------------------------------------------");
        if (logs.isEmpty()) {
            System.out.println("No activity logs found.");
        } else {
            for (ActivityLog log : logs) {
                System.out.printf("%-5d | %-8d | %-18s | %-35s | %-20s%n",
                        log.getLogId(),
                        log.getUserId(),
                        log.getActionType(),
                        log.getDescription(),
                        log.getTimestamp());
            }
        }
    }

    public void adminMenu(User admin) {
        boolean session = true;
        while (session) {
            System.out.println("\n--- ADMIN PANEL (" + admin.getUsername() + ") ---");
            System.out.println("1. View All System Users");
            System.out.println("2. View Pending Registrations");
            System.out.println("3. Approve User Account");
            System.out.println("4. Update User Role & Status");
            System.out.println("5. View System Audit Logs");
            System.out.println("6. View Room Status & Inventory");
            System.out.println("7. Add New Labor Ward Room");
            System.out.println("8. View All Active Allocations");
            System.out.println("9. Logout");
            System.out.print("Choose an option (1-9): ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    viewAllUsers();
                    break;
                case "2":
                    viewPendingUsers();
                    break;
                case "3":
                    approveUserAccount(admin);
                    break;
                case "4":
                    updateUserRoleAndStatusFeature(admin);
                    break;
                case "5":
                    viewSystemAuditLogsMenu();
                    break;
                case "6":
                    viewAllRooms();
                    break;
                case "7":
                    addNewRoom();
                    break;
                case "8":
                    viewActiveAllocations();
                    break;
                case "9":
                    System.out.println("Logging out...");
                    activityLogDao.logAction(
                            admin.getId(),
                            "USER_LOGOUT",
                            "Admin " + admin.getUsername() + " logged out of the system."
                    );
                    session = false;
                    break;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private int readIntInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("WARNING: Invalid numeric input. Please enter a valid number or 0 to cancel.");
            }
        }
    }

    private String readValidRole() {
        while (true) {
            System.out.print("Enter new Role (e.g., STUDENT, TEACHER, ADMIN, DOCTOR, NURSE): ");
            String role = scanner.nextLine().trim().toUpperCase();
            if (!role.isEmpty()) {
                return role;
            }
            System.out.println("WARNING: Role cannot be empty.");
        }
    }

    private String readValidStatus() {
        while (true) {
            System.out.print("Enter new Status (APPROVED, PENDING): ");
            String status = scanner.nextLine().trim().toUpperCase();
            if (status.equals("APPROVED") || status.equals("PENDING")) {
                return status;
            }
            System.out.println("WARNING: Invalid status. Accepted statuses: APPROVED, PENDING.");
        }
    }

    private void updateUserRoleAndStatusFeature(User currentAdmin) {
        System.out.println("\n--- UPDATE USER ROLE & STATUS ---");
        List<User> users = userDao.getAllUsers();
        displayUserTable(users);

        if (users.isEmpty()) {
            return;
        }

        int targetUserId = readIntInput("Enter Target User ID to update (Enter 0 to cancel): ");
        if (targetUserId == 0) {
            System.out.println("Update operation cancelled.");
            return;
        }

        if (targetUserId == currentAdmin.getId()) {
            System.out.println("WARNING: Modifying the currently active admin account is restricted.");
            return;
        }

        boolean userExists = users.stream().anyMatch(u -> u.getId() == targetUserId);
        if (!userExists) {
            System.out.println("Error: User ID does not exist in the system.");
            return;
        }

        String newRole = readValidRole();
        String newStatus = readValidStatus();

        boolean success = userDao.updateUserRoleAndStatus(targetUserId, newRole, newStatus);
        if (success) {
            System.out.println("Success: User ID " + targetUserId + " updated successfully to Role: " + newRole + ", Status: " + newStatus);
            activityLogDao.logAction(
                    currentAdmin.getId(),
                    "USER_UPDATED",
                    "Admin " + currentAdmin.getUsername() + " (ID " + currentAdmin.getId() + ") updated User ID " + targetUserId + " to Role " + newRole + " and Status " + newStatus
            );
        } else {
            System.out.println("Failed to update user role and status.");
        }
    }

    private void approveUserAccount(User currentAdmin) {
        List<User> pendingUsers = userDao.getPendingUsers();
        System.out.println("\n--- PENDING ACCOUNTS ---");
        displayUserTable(pendingUsers);

        if (pendingUsers.isEmpty()) {
            return;
        }

        int userId = readIntInput("Enter Target User ID to APPROVE (Enter 0 to cancel): ");
        if (userId == 0) {
            System.out.println("Approval action cancelled.");
            return;
        }

        boolean existsInPending = pendingUsers.stream().anyMatch(u -> u.getId() == userId);
        if (!existsInPending) {
            System.out.println("Error: User ID does not exist in the pending registrations list.");
            return;
        }

        boolean success = userDao.approveUser(userId);
        if (success) {
            System.out.println("Success: User ID " + userId + " status updated from 'PENDING' to 'APPROVED'.");
            activityLogDao.logAction(
                    currentAdmin.getId(),
                    "USER_APPROVED",
                    "Admin " + currentAdmin.getUsername() + " approved account for User ID " + userId
            );
        } else {
            System.out.println("Failed to approve user account.");
        }
    }

    private void viewSystemAuditLogsMenu() {
        boolean subSession = true;
        while (subSession) {
            System.out.println("\n--- SYSTEM AUDIT LOGS ---");
            System.out.println("1. View All System Logs");
            System.out.println("2. Filter Logs by User ID");
            System.out.println("3. Back to Admin Panel");
            System.out.print("Choose an option (1-3): ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    System.out.println("\n--- ALL SYSTEM-WIDE AUDIT LOGS ---");
                    List<ActivityLog> allLogs = activityLogDao.getAllLogs();
                    displayAuditLogTable(allLogs);
                    break;
                case "2":
                    int filterUserId = readIntInput("Enter User ID to filter logs: ");
                    System.out.println("\n--- AUDIT LOGS FOR USER ID " + filterUserId + " ---");
                    List<ActivityLog> userLogs = activityLogDao.getLogsByUserId(filterUserId);
                    displayAuditLogTable(userLogs);
                    break;
                case "3":
                    subSession = false;
                    break;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private void viewAllUsers() {
        System.out.println("\n--- ALL SYSTEM USERS ---");
        List<User> users = userDao.getAllUsers();
        displayUserTable(users);
    }

    private void viewPendingUsers() {
        System.out.println("\n--- PENDING REGISTRATIONS ---");
        List<User> pendingUsers = userDao.getPendingUsers();
        displayUserTable(pendingUsers);
    }

    public void staffMenu(User staff) {
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
                    allocateRoom(staff);
                    break;
                case "4":
                    viewActiveAllocations();
                    break;
                case "5":
                    dischargePatient(staff);
                    break;
                case "6":
                    System.out.println("Logging out...");
                    activityLogDao.logAction(
                            staff.getId(),
                            "USER_LOGOUT",
                            "Staff " + staff.getUsername() + " logged out of the system."
                    );
                    session = false;
                    break;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

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

    private void allocateRoom(User staff) {
        System.out.println("\n--- ALLOCATE ROOM / BOOKING ---");
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

        int patientId = readIntInput("Enter Patient ID: ");
        viewAvailableRooms();
        int roomId = readIntInput("Enter Room ID to Allocate: ");

        String insertAllocation = "INSERT INTO allocations (patient_id, room_id, admission_time, status) VALUES (?, ?, datetime('now'), 'ACTIVE');";
        String updateRoom = "UPDATE rooms SET status = 'OCCUPIED' WHERE room_id = ?;";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement stmt1 = conn.prepareStatement(insertAllocation);
                 PreparedStatement stmt2 = conn.prepareStatement(updateRoom)) {

                stmt1.setInt(1, patientId);
                stmt1.setInt(2, roomId);
                stmt1.executeUpdate();

                stmt2.setInt(1, roomId);
                stmt2.executeUpdate();

                conn.commit();
                System.out.println("Room allocated successfully and room status updated to OCCUPIED!");

                activityLogDao.logAction(
                        staff.getId(),
                        "ROOM_ALLOCATED",
                        staff.getUsername() + " allocated Room ID " + roomId + " to Patient ID " + patientId
                );
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

    private void dischargePatient(User staff) {
        viewActiveAllocations();
        int allocId = readIntInput("Enter Allocation ID to Discharge: ");
        if (allocId == 0) return;

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

                activityLogDao.logAction(
                        staff.getId(),
                        "PATIENT_DISCHARGED",
                        staff.getUsername() + " discharged patient for Allocation ID " + allocId + " and released Room ID " + roomId
                );
            } catch (SQLException e) {
                conn.rollback();
                System.out.println("Discharge failed, rolling back: " + e.getMessage());
            }

        } catch (SQLException e) {
            System.out.println("Database error during discharge: " + e.getMessage());
        }
    }
}