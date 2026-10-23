package org.example;

import org.example.dao.DatabaseConnection;
import org.example.ui.MenuUI;

public class Main {

    public static void main(String[] args) {
        System.out.println("=====================================================");
        System.out.println("  Starting Labor Ward Room Allocation Booking System ");
        System.out.println("=====================================================");

        // Initialize database tables (users, rooms, patients, allocations)
        DatabaseConnection.initializeDatabase();

        // Launch the menu user interface
        MenuUI menu = new MenuUI();
        menu.start();
    }
}