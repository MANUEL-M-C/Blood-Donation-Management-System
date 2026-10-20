package com.blooddonation.dao;

import com.blooddonation.model.Admin;
import com.blooddonation.util.PasswordUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================================
 * OOP CONCEPT: ABSTRACTION & SECURITY DATA ACCESS
 * ============================================================================
 * AdminDAO manages administrator credentials and authentication against SQLite.
 */
public class AdminDAO {

    /**
     * Authenticates an administrator using username and plain-text password.
     */
    public Admin authenticate(String username, String plainPassword) throws SQLException {
        if (username == null || plainPassword == null) {
            return null;
        }

        String sql = "SELECT admin_id, username, password_hash, role FROM admins WHERE LOWER(username) = LOWER(?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username.trim());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password_hash");
                    if (PasswordUtil.verifyPassword(plainPassword, storedHash)) {
                        return new Admin(
                                rs.getString("admin_id"),
                                rs.getString("username"),
                                storedHash,
                                rs.getString("role")
                        );
                    }
                }
            }
        }
        return null;
    }

    /**
     * Updates an administrator's password.
     */
    public boolean changePassword(String username, String newPlainPassword) throws SQLException {
        String newHash = PasswordUtil.hashPassword(newPlainPassword);
        String sql = "UPDATE admins SET password_hash = ? WHERE LOWER(username) = LOWER(?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newHash);
            pstmt.setString(2, username.trim());
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Creates a new administrator account.
     */
    public boolean createAdmin(Admin admin, String plainPassword) throws SQLException {
        String sql = "INSERT INTO admins (admin_id, username, password_hash, role) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, admin.getAdminId());
            pstmt.setString(2, admin.getUsername());
            pstmt.setString(3, PasswordUtil.hashPassword(plainPassword));
            pstmt.setString(4, admin.getRole());
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Lists all admin accounts.
     */
    public List<Admin> getAllAdmins() throws SQLException {
        String sql = "SELECT admin_id, username, password_hash, role FROM admins ORDER BY admin_id ASC";
        List<Admin> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Admin(
                        rs.getString("admin_id"),
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        rs.getString("role")
                ));
            }
        }
        return list;
    }
}
