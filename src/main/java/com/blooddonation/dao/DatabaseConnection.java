package com.blooddonation.dao;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * ============================================================================
 * OOP CONCEPT: SINGLETON & ABSTRACTION
 * ============================================================================
 * DatabaseConnection provides centralized, thread-safe database connectivity
 * to SQLite.
 *
 * Demonstrates:
 * 1. ABSTRACTION: Hides the specifics of JDBC driver registration, URL formulation,
 *    and foreign-key enforcement behind a simple static getConnection() method.
 * 2. SINGLETON / FACTORY: Manages database connection properties consistently.
 */
public class DatabaseConnection {
    private static final String DEFAULT_DB_NAME = "blood_donation.db";
    private static String dbUrl = "jdbc:sqlite:" + DEFAULT_DB_NAME;

    // Prevent direct instantiation (Encapsulation / Utility pattern)
    private DatabaseConnection() {}

    /**
     * Configures a custom database URL (primarily for integration tests).
     */
    public static synchronized void setDatabaseUrl(String newUrl) {
        dbUrl = newUrl;
    }

    /**
     * Resets database URL to default blood_donation.db.
     */
    public static synchronized void resetToDefault() {
        dbUrl = "jdbc:sqlite:" + DEFAULT_DB_NAME;
    }

    /**
     * Obtains a new active connection to the SQLite database with Foreign Keys enabled.
     *
     * @return Connection active SQLite connection
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        try {
            // Explicitly load SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC Driver not found: " + e.getMessage());
        }

        Connection conn = DriverManager.getConnection(dbUrl);
        // SQLite foreign key enforcement is disabled by default; enable it here
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
        }
        return conn;
    }

    /**
     * Checks if the SQLite database file exists.
     */
    public static boolean databaseExists() {
        File file = new File(DEFAULT_DB_NAME);
        return file.exists();
    }
}
