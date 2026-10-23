package org.example.model;

public class User {
    private int id;
    private String username;
    private String password;
    private String roles;
    private String status;

    // Constructor matching UserDao instantiation
    public User(int id, String username, String password, String roles, String status) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.roles = roles;
        this.status = status;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getRoles() {
        return roles;
    }

    public String getStatus() {
        return status;
    }

    // Setters (Optional, useful if you want to update user details later)
    public void setId(int id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", roles='" + roles + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}