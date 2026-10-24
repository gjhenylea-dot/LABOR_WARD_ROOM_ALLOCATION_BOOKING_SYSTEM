package org.example;

import org.example.dao.DatabaseInitializer;
import org.example.ui.Authentication;

public class Main {

    public static void main(String[] args) {
        System.out.println("=====================================================");
        System.out.println("  Starting Labor Ward Room Allocation Booking System ");
        System.out.println("=====================================================");

        try {
            // Initialize database tables, constraints, and seed defaults
            DatabaseInitializer.initialize();
        } catch (Exception e) {
            System.err.println("Critical error initializing database: " + e.getMessage());
            return;
        }

        // Delegate authentication and UI routing
        Authentication auth = new Authentication();
        auth.start();
    }
}
