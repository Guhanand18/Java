package com.bnpparibas.model;

public class User {

    private Long id;
    private String username;
    private String password;
    private String role; // comma-separated

    public User() {}

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    // ===== HELPER METHODS =====

    public boolean hasRole(String checkRole) {
    if (role == null) return false;

    String[] roles = role.split(",");
    for (String r : roles) {
        if (r.trim().equalsIgnoreCase(checkRole)) {
            return true;
        }
    }
    return false;
}