package org.example.dao;

import org.example.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDao {

    public static void initializeUserTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL UNIQUE,
                    password TEXT NOT NULL,
                    roles TEXT NOT NULL,
                    status TEXT NOT NULL
                )
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.execute();
            seedDefaultAdmin(connection);

        } catch (SQLException e) {
            System.out.println("Error initializing users table: " + e.getMessage());
        }
    }

    private static void seedDefaultAdmin(Connection connection) {
        String checkSql = "SELECT COUNT(*) FROM users;";
        try (PreparedStatement checkStmt = connection.prepareStatement(checkSql);
             ResultSet rs = checkStmt.executeQuery()) {
            if (rs.next() && rs.getInt(1) == 0) {
                String insertSql = """
                        INSERT INTO users (username, password, roles, status)
                        VALUES ('admin', 'admin123', 'ADMIN', 'ACTIVE')
                        """;
                try (PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
                    insertStmt.executeUpdate();
                    System.out.println("-> Default Labor Ward Admin created (username: admin, password: admin123).");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error seeding default admin: " + e.getMessage());
        }
    }

    public boolean register(
            String username,
            String password,
            String role
    ) {
        String sql = """
                INSERT INTO users
                (username, password, roles, status)
                VALUES (?, ?, ?, 'PENDING')
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, username);
            statement.setString(2, password);
            statement.setString(3, role);

            statement.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.out.println(
                    "Registration failed: " + e.getMessage()
            );
            return false;
        }
    }

    public User login(String username, String password) {
        String sql = """
                SELECT id, username, password, roles, status
                FROM users
                WHERE username = ? AND password = ?
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, username);
            statement.setString(2, password);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    String status = result.getString("status");
                    if ("PENDING".equalsIgnoreCase(status)) {
                        System.out.println("Login failed: Your account is pending admin approval.");
                        return null;
                    }

                    return new User(
                            result.getInt("id"),
                            result.getString("username"),
                            result.getString("password"),
                            result.getString("roles"),
                            result.getString("status")
                    );
                }
            }

        } catch (SQLException e) {
            System.out.println(
                    "Login failed: " + e.getMessage()
            );
        }

        return null;
    }
}