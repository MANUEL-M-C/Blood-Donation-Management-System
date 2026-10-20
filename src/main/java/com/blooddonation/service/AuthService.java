package com.blooddonation.service;

import com.blooddonation.dao.AdminDAO;
import com.blooddonation.model.Admin;
import com.blooddonation.util.ValidationUtil;

import java.sql.SQLException;

/**
 * ============================================================================
 * OOP CONCEPT: SECURITY & AUTHENTICATION SERVICE
 * ============================================================================
 * AuthService controls user sessions and credential verification.
 */
public class AuthService {
    private static Admin currentAdmin = null;
    private final AdminDAO adminDAO;

    public AuthService() {
        this.adminDAO = new AdminDAO();
    }

    public AuthService(AdminDAO adminDAO) {
        this.adminDAO = adminDAO;
    }

    /**
     * Authenticates an admin and sets active session.
     */
    public boolean login(String username, String password) throws SQLException {
        ValidationUtil.requireNonEmpty(username, "Username");
        ValidationUtil.requireNonEmpty(password, "Password");

        Admin admin = adminDAO.authenticate(username, password);
        if (admin != null) {
            currentAdmin = admin;
            return true;
        }
        return false;
    }

    /**
     * Clears the current login session.
     */
    public static void logout() {
        currentAdmin = null;
    }

    /**
     * Returns the currently authenticated administrator.
     */
    public static Admin getCurrentAdmin() {
        return currentAdmin;
    }

    /**
     * Checks if a valid session exists.
     */
    public static boolean isAuthenticated() {
        return currentAdmin != null;
    }

    /**
     * Changes password for the active administrator.
     */
    public boolean changeCurrentPassword(String oldPassword, String newPassword, String confirmPassword) throws SQLException {
        if (currentAdmin == null) {
            throw new IllegalStateException("No active administrative session.");
        }
        ValidationUtil.requireNonEmpty(oldPassword, "Current Password");
        ValidationUtil.requireNonEmpty(newPassword, "New Password");

        if (newPassword.length() < 4) {
            throw new IllegalArgumentException("New password must be at least 4 characters long.");
        }

        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("New password and confirmation do not match.");
        }

        // Verify old password
        Admin verified = adminDAO.authenticate(currentAdmin.getUsername(), oldPassword);
        if (verified == null) {
            throw new IllegalArgumentException("Current password is incorrect.");
        }

        return adminDAO.changePassword(currentAdmin.getUsername(), newPassword);
    }
}
