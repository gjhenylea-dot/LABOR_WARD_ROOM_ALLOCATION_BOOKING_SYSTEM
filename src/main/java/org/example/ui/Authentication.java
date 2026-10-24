package org.example.ui;

import org.example.dao.UserDao;
import org.example.model.User;

import java.util.Scanner;

public class Authentication {
    private final UserDao userDao = new UserDao();
    private final Scanner scanner = new Scanner(System.in);
    private final MenuUI menuUI = new MenuUI();

    public void start() {
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
                menuUI.adminMenu(user);
            } else {
                menuUI.staffMenu(user);
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
}