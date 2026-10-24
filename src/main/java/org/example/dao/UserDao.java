package org.example.dao;

import org.example.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UserDao {

    public boolean register(String username, String password, String role) {
        String sql = "INSERT INTO users (username, password, roles, status) VALUES (?, ?, ?, 'PENDING');";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, password);
            statement.setString(3, role);
            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Registration failed: " + e.getMessage());
            return false;
        }
    }

    public User login(String username, String password) {
        String sql = "SELECT id, username, password, roles, status FROM users WHERE username = ? AND password = ?;";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
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
            System.out.println("Login failed: " + e.getMessage());
        }
        return null;
    }

    // Required: getAllUsers()
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, username, password, roles, status FROM users;";
        try (Connection connection = DatabaseConnection.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                users.add(new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("roles"),
                        rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Error fetching all users: " + e.getMessage());
        }
        return users;
    }

    // Required: getPendingUsers()
    public List<User> getPendingUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, username, password, roles, status FROM users WHERE status = 'PENDING';";
        try (Connection connection = DatabaseConnection.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                users.add(new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("roles"),
                        rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Error fetching pending users: " + e.getMessage());
        }
        return users;
    }

    // Required: approveUser(int userId)
    public boolean approveUser(int userId) {
        String sql = "UPDATE users SET status = 'APPROVED' WHERE id = ?;";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            int affected = pstmt.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            System.out.println("Error approving user: " + e.getMessage());
            return false;
        }
    }
}