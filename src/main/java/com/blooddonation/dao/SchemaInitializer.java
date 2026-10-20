package com.blooddonation.dao;

import com.blooddonation.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * ============================================================================
 * OOP CONCEPT: ABSTRACTION & DATABASE LIFECYCLE
 * ============================================================================
 * SchemaInitializer creates all necessary SQLite relational tables, indexes,
 * foreign key relationships, and default admin credentials.
 */
public class SchemaInitializer {

    private SchemaInitializer() {}

    /**
     * Initializes all database tables and ensures default administrator exists.
     */
    public static void initializeDatabase() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            // 1. Persons table (Base entity table)
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS persons (
                    person_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    age INTEGER NOT NULL,
                    phone TEXT NOT NULL,
                    address TEXT NOT NULL,
                    person_type TEXT NOT NULL
                );
            """);

            // 2. Donors table (Inherits conceptually from persons)
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS donors (
                    donor_id TEXT PRIMARY KEY,
                    person_id INTEGER NOT NULL UNIQUE,
                    blood_group TEXT NOT NULL,
                    last_donation_date TEXT,
                    FOREIGN KEY(person_id) REFERENCES persons(person_id) ON DELETE CASCADE
                );
            """);

            // 3. Recipients table (Inherits conceptually from persons)
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS recipients (
                    recipient_id TEXT PRIMARY KEY,
                    person_id INTEGER NOT NULL UNIQUE,
                    blood_group TEXT NOT NULL,
                    units_required INTEGER NOT NULL,
                    hospital TEXT,
                    FOREIGN KEY(person_id) REFERENCES persons(person_id) ON DELETE CASCADE
                );
            """);

            // 4. Donations table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS donations (
                    donation_id TEXT PRIMARY KEY,
                    donor_id TEXT NOT NULL,
                    donation_date TEXT NOT NULL,
                    units INTEGER NOT NULL,
                    blood_group TEXT NOT NULL,
                    FOREIGN KEY(donor_id) REFERENCES donors(donor_id) ON DELETE RESTRICT
                );
            """);

            // 5. Blood Units table (Individual tracked blood inventory)
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS blood_units (
                    unit_id TEXT PRIMARY KEY,
                    donation_id TEXT,
                    blood_group TEXT NOT NULL,
                    collection_date TEXT NOT NULL,
                    status TEXT NOT NULL,
                    FOREIGN KEY(donation_id) REFERENCES donations(donation_id) ON DELETE SET NULL
                );
            """);

            // 6. Blood Requests table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS blood_requests (
                    request_id TEXT PRIMARY KEY,
                    recipient_id TEXT NOT NULL,
                    blood_group TEXT NOT NULL,
                    units_requested INTEGER NOT NULL,
                    request_date TEXT NOT NULL,
                    status TEXT NOT NULL,
                    hospital TEXT,
                    FOREIGN KEY(recipient_id) REFERENCES recipients(recipient_id) ON DELETE RESTRICT
                );
            """);

            // 7. Admins table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS admins (
                    admin_id TEXT PRIMARY KEY,
                    username TEXT UNIQUE NOT NULL,
                    password_hash TEXT NOT NULL,
                    role TEXT NOT NULL
                );
            """);

            // Ensure default administrator exists (admin / admin123)
            ensureDefaultAdmin(conn);

        } catch (SQLException e) {
            System.err.println("Database initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Seeds the default development admin (username: admin, password: admin123)
     */
    private static void ensureDefaultAdmin(Connection conn) throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM admins WHERE username = 'admin'";
        try (Statement checkStmt = conn.createStatement();
             ResultSet rs = checkStmt.executeQuery(checkSql)) {
            if (rs.next() && rs.getInt(1) == 0) {
                String insertSql = "INSERT INTO admins (admin_id, username, password_hash, role) VALUES (?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                    pstmt.setString(1, "ADM001");
                    pstmt.setString(2, "admin");
                    pstmt.setString(3, PasswordUtil.hashPassword("admin123"));
                    pstmt.setString(4, "SUPER_ADMIN");
                    pstmt.executeUpdate();
                    System.out.println("Default administrator 'admin' initialized successfully.");
                }
            }
        }
    }

    /**
     * Seeds realistic academic sample / demo data into SQLite
     */
    public static void loadDemoData() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Check if demo donors already exist
                String checkSql = "SELECT COUNT(*) FROM donors WHERE donor_id IN ('D001', 'D002', 'D003')";
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(checkSql)) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        insertSampleDonor(conn, "D001", "Rahul Sharma", 28, "9876543210", "Main Street, Pune", "O+", "2025-01-15");
                        insertSampleDonor(conn, "D002", "Arun Kumar", 34, "9876543211", "MG Road, Bangalore", "A+", "2025-02-10");
                        insertSampleDonor(conn, "D003", "Anu Patel", 25, "9876543212", "Park View, Chennai", "B+", "2025-03-01");
                    }
                }

                // Check sample recipient
                String checkRec = "SELECT COUNT(*) FROM recipients WHERE recipient_id = 'R001'";
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(checkRec)) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        insertSampleRecipient(conn, "R001", "Vikram Singh", 42, "9876543220", "Civil Lines, Delhi", "O+", 2, "City Care Hospital");
                    }
                }

                // Check sample inventory (Requirement 26: A+=8, B+=6, O+=12, AB+=3, A-=2)
                String checkUnits = "SELECT COUNT(*) FROM blood_units";
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(checkUnits)) {
                    if (rs.next() && rs.getInt(1) < 10) {
                        seedBloodUnits(conn, "A+", 8);
                        seedBloodUnits(conn, "B+", 6);
                        seedBloodUnits(conn, "O+", 12);
                        seedBloodUnits(conn, "AB+", 3);
                        seedBloodUnits(conn, "A-", 2);
                        seedBloodUnits(conn, "B-", 2);
                        seedBloodUnits(conn, "AB-", 1);
                        seedBloodUnits(conn, "O-", 4);
                    }
                }

                conn.commit();
                System.out.println("Demo data successfully loaded into SQLite database.");
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private static void insertSampleDonor(Connection conn, String donorId, String name, int age,
                                          String phone, String address, String bloodGroup, String lastDate) throws SQLException {
        // Insert person
        String personSql = "INSERT INTO persons (name, age, phone, address, person_type) VALUES (?, ?, ?, ?, 'DONOR')";
        long personId;
        try (PreparedStatement pstmt = conn.prepareStatement(personSql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, name);
            pstmt.setInt(2, age);
            pstmt.setString(3, phone);
            pstmt.setString(4, address);
            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    personId = rs.getLong(1);
                } else {
                    throw new SQLException("Failed to retrieve generated person_id.");
                }
            }
        }

        // Insert donor
        String donorSql = "INSERT INTO donors (donor_id, person_id, blood_group, last_donation_date) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(donorSql)) {
            pstmt.setString(1, donorId);
            pstmt.setLong(2, personId);
            pstmt.setString(3, bloodGroup);
            pstmt.setString(4, lastDate);
            pstmt.executeUpdate();
        }
    }

    private static void insertSampleRecipient(Connection conn, String recipientId, String name, int age,
                                              String phone, String address, String bloodGroup, int units, String hospital) throws SQLException {
        // Insert person
        String personSql = "INSERT INTO persons (name, age, phone, address, person_type) VALUES (?, ?, ?, ?, 'RECIPIENT')";
        long personId;
        try (PreparedStatement pstmt = conn.prepareStatement(personSql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, name);
            pstmt.setInt(2, age);
            pstmt.setString(3, phone);
            pstmt.setString(4, address);
            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    personId = rs.getLong(1);
                } else {
                    throw new SQLException("Failed to retrieve generated person_id.");
                }
            }
        }

        // Insert recipient
        String recSql = "INSERT INTO recipients (recipient_id, person_id, blood_group, units_required, hospital) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(recSql)) {
            pstmt.setString(1, recipientId);
            pstmt.setLong(2, personId);
            pstmt.setString(3, bloodGroup);
            pstmt.setInt(4, units);
            pstmt.setString(5, hospital);
            pstmt.executeUpdate();
        }
    }

    private static void seedBloodUnits(Connection conn, String bloodGroup, int count) throws SQLException {
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        String insertSql = "INSERT OR IGNORE INTO blood_units (unit_id, donation_id, blood_group, collection_date, status) VALUES (?, NULL, ?, ?, 'AVAILABLE')";
        try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
            for (int i = 1; i <= count; i++) {
                String unitId = String.format("U-%s-%03d", bloodGroup.replace("+", "P").replace("-", "N"), i);
                pstmt.setString(1, unitId);
                pstmt.setString(2, bloodGroup);
                pstmt.setString(3, today);
                pstmt.executeUpdate();
            }
        }
    }
}
