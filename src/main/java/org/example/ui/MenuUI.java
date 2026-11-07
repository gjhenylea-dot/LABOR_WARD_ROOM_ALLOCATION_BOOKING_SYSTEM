package org.example.ui;

import org.example.dao.DatabaseConnection;
import org.example.dao.UserDao;
import org.example.model.User;

import java.sql.*;
import java.util.List;
import java.util.Scanner;

public class MenuUI {
    private final UserDao userDao = new UserDao();
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

    public void adminMenu(User admin) {
        boolean session = true;
        while (session) {
            System.out.println("\n--- ADMIN PANEL (" + admin.getUsername() + ") ---");
            System.out.println("1. View All System Users");
            System.out.println("2. View Pending Registrations");
            System.out.println("3. Approve User Account");
            System.out.println("4. Update User Role & Status"); // Added Option
            System.out.println("5. View Room Status & Inventory");
            System.out.println("6. Add New Labor Ward Room");
            System.out.println("7. View All Active Allocations");
            System.out.println("8. Logout");
            System.out.print("Choose an option (1-8): ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    viewAllUsers();
                    break;
                case "2":
                    viewPendingUsers();
                    break;
                case "3":
                    approveUserAccount();
                    break;
                case "4":
                    updateUserRoleAndStatusFeature(admin); // New Feature Handler
                    break;
                case "5":
                    viewAllRooms();
                    break;
                case "6":
                    addNewRoom();
                    break;
                case "7":
                    viewActiveAllocations();
                    break;
                case "8":
                    System.out.println("Logging out...");
                    session = false;
                    break;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

    // ==========================================
    // INPUT VALIDATION WRAPPERS & UPDATE FEATURE
    // ==========================================

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
            System.out.print("Enter new Role (e.g., STUDENT, TEACHER, ADMIN): ");
            String role = scanner.nextLine().trim().toUpperCase();
            if (role.equals("STUDENT") || role.equals("TEACHER") || role.equals("ADMIN") || role.equals("STAFF")) {
                return role;
            }
            System.out.println("WARNING: Invalid role. Accepted roles: STUDENT, TEACHER, ADMIN, STAFF.");
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

        // Self-Edit Restriction Rule Check
        if (targetUserId == currentAdmin.getId()) {
            System.out.println("WARNING: Modifying the currently active admin account is restricted.");
            return;
        }

        // Verify if target user exists
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
        } else {
            System.out.println("Failed to update user role and status.");
        }
    }

    // ... (Keep the rest of your MenuUI methods: viewAllUsers, approveUserAccount, viewPendingUsers, staffMenu, etc.)
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

    private void approveUserAccount() {
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
        } else {
            System.out.println("Failed to approve user account.");
        }
    }

    // Include remaining methods from your previous MenuUI implementation (viewAllRooms, addNewRoom, registerPatient, allocateRoom, viewActiveAllocations, dischargePatient, staffMenu) below...
    public void staffMenu(User staff) {
        // ... (Keep existing implementation)
    }
    private void viewAllRooms() { /* ... */ }
    private void viewAvailableRooms() { /* ... */ }
    private void addNewRoom() { /* ... */ }
    private void registerPatient() { /* ... */ }
    private void allocateRoom() { /* ... */ }
    private void viewActiveAllocations() { /* ... */ }
    private void dischargePatient() { /* ... */ }
}