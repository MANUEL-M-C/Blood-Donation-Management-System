package com.blooddonation.model;

/**
 * ============================================================================
 * OOP CONCEPT: ENCAPSULATION & ROLE-BASED ACCESS
 * ============================================================================
 * Admin encapsulates credentials and role privileges for system operators.
 */
public class Admin {
    // Private attributes (Encapsulation)
    private String adminId;
    private String username;
    private String passwordHash;
    private String role; // e.g., "SUPER_ADMIN", "ADMIN"

    public Admin() {
        this.role = "ADMIN";
    }

    public Admin(String adminId, String username, String passwordHash, String role) {
        setAdminId(adminId);
        setUsername(username);
        this.passwordHash = passwordHash;
        setRole(role);
    }

    // Getters and Setters
    public String getAdminId() {
        return adminId;
    }

    public void setAdminId(String adminId) {
        if (adminId == null || adminId.trim().isEmpty()) {
            throw new IllegalArgumentException("Admin ID cannot be empty.");
        }
        this.adminId = adminId.trim();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty.");
        }
        this.username = username.trim();
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = (role != null && !role.trim().isEmpty()) ? role.trim() : "ADMIN";
    }

    /**
     * Administrative actions
     */
    public boolean manageRecords() {
        return true;
    }

    public boolean generateReports() {
        return true;
    }

    @Override
    public String toString() {
        return String.format("[ADMIN %s] %s (%s)", adminId, username, role);
    }
}
