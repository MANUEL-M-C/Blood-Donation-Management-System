package com.blooddonation.dao;

import com.blooddonation.model.Recipient;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================================
 * OOP CONCEPT: ABSTRACTION & DATA ACCESS OBJECT (DAO) PATTERN
 * ============================================================================
 * RecipientDAO abstracts database interactions for Recipient entities.
 */
public class RecipientDAO {

    /**
     * Inserts a new Recipient and associated Person record.
     */
    public boolean insertRecipient(Recipient recipient) throws SQLException {
        if (recipientExists(recipient.getRecipientId())) {
            throw new SQLException("A recipient with ID '" + recipient.getRecipientId() + "' already exists.");
        }

        String personSql = "INSERT INTO persons (name, age, phone, address, person_type) VALUES (?, ?, ?, ?, 'RECIPIENT')";
        String recSql = "INSERT INTO recipients (recipient_id, person_id, blood_group, units_required, hospital) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                long personId;
                try (PreparedStatement pstmt = conn.prepareStatement(personSql, Statement.RETURN_GENERATED_KEYS)) {
                    pstmt.setString(1, recipient.getName());
                    pstmt.setInt(2, recipient.getAge());
                    pstmt.setString(3, recipient.getPhone());
                    pstmt.setString(4, recipient.getAddress());
                    pstmt.executeUpdate();

                    try (ResultSet rs = pstmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            personId = rs.getLong(1);
                        } else {
                            throw new SQLException("Failed to obtain generated person_id.");
                        }
                    }
                }

                try (PreparedStatement pstmt = conn.prepareStatement(recSql)) {
                    pstmt.setString(1, recipient.getRecipientId());
                    pstmt.setLong(2, personId);
                    pstmt.setString(3, recipient.getBloodGroup());
                    pstmt.setInt(4, recipient.getUnitsRequired());
                    pstmt.setString(5, recipient.getHospital());
                    pstmt.executeUpdate();
                }

                conn.commit();
                return true;
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    /**
     * Updates an existing recipient record and associated person details.
     */
    public boolean updateRecipient(Recipient recipient) throws SQLException {
        String query = """
            UPDATE persons
            SET name = ?, age = ?, phone = ?, address = ?
            WHERE person_id = (SELECT person_id FROM recipients WHERE recipient_id = ?)
        """;
        String recQuery = """
            UPDATE recipients
            SET blood_group = ?, units_required = ?, hospital = ?
            WHERE recipient_id = ?
        """;

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int affected;
                try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                    pstmt.setString(1, recipient.getName());
                    pstmt.setInt(2, recipient.getAge());
                    pstmt.setString(3, recipient.getPhone());
                    pstmt.setString(4, recipient.getAddress());
                    pstmt.setString(5, recipient.getRecipientId());
                    affected = pstmt.executeUpdate();
                }

                if (affected == 0) {
                    conn.rollback();
                    return false;
                }

                try (PreparedStatement pstmt = conn.prepareStatement(recQuery)) {
                    pstmt.setString(1, recipient.getBloodGroup());
                    pstmt.setInt(2, recipient.getUnitsRequired());
                    pstmt.setString(3, recipient.getHospital());
                    pstmt.setString(4, recipient.getRecipientId());
                    pstmt.executeUpdate();
                }

                conn.commit();
                return true;
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    /**
     * Deletes a recipient by ID.
     */
    public boolean deleteRecipient(String recipientId) throws SQLException {
        String findPersonSql = "SELECT person_id FROM recipients WHERE recipient_id = ?";
        String deletePersonSql = "DELETE FROM persons WHERE person_id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                long personId = -1;
                try (PreparedStatement pstmt = conn.prepareStatement(findPersonSql)) {
                    pstmt.setString(1, recipientId);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            personId = rs.getLong("person_id");
                        }
                    }
                }

                if (personId == -1) {
                    conn.rollback();
                    return false;
                }

                try (PreparedStatement pstmt = conn.prepareStatement(deletePersonSql)) {
                    pstmt.setLong(1, personId);
                    pstmt.executeUpdate();
                }

                conn.commit();
                return true;
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    /**
     * Retrieves a recipient by ID.
     */
    public Recipient getRecipientById(String recipientId) throws SQLException {
        String sql = """
            SELECT r.recipient_id, p.name, p.age, p.phone, p.address, r.blood_group, r.units_required, r.hospital
            FROM recipients r
            JOIN persons p ON r.person_id = p.person_id
            WHERE r.recipient_id = ?
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, recipientId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToRecipient(rs);
                }
            }
        }
        return null;
    }

    /**
     * Retrieves all registered recipients.
     */
    public List<Recipient> getAllRecipients() throws SQLException {
        String sql = """
            SELECT r.recipient_id, p.name, p.age, p.phone, p.address, r.blood_group, r.units_required, r.hospital
            FROM recipients r
            JOIN persons p ON r.person_id = p.person_id
            ORDER BY r.recipient_id ASC
        """;

        List<Recipient> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToRecipient(rs));
            }
        }
        return list;
    }

    /**
     * Searches recipients by ID, Name, Phone, or Hospital.
     */
    public List<Recipient> searchRecipients(String keyword, String bloodGroupFilter) throws SQLException {
        StringBuilder sql = new StringBuilder("""
            SELECT r.recipient_id, p.name, p.age, p.phone, p.address, r.blood_group, r.units_required, r.hospital
            FROM recipients r
            JOIN persons p ON r.person_id = p.person_id
            WHERE 1=1
        """);

        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (LOWER(r.recipient_id) LIKE ? OR LOWER(p.name) LIKE ? OR LOWER(r.hospital) LIKE ?)");
            String pattern = "%" + keyword.trim().toLowerCase() + "%";
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
        }

        if (bloodGroupFilter != null && !bloodGroupFilter.equalsIgnoreCase("ALL") && !bloodGroupFilter.trim().isEmpty()) {
            sql.append(" AND r.blood_group = ?");
            params.add(bloodGroupFilter.trim().toUpperCase());
        }

        sql.append(" ORDER BY r.recipient_id ASC");

        List<Recipient> results = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapResultSetToRecipient(rs));
                }
            }
        }
        return results;
    }

    /**
     * Checks if a recipient with the given ID exists.
     */
    public boolean recipientExists(String recipientId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM recipients WHERE recipient_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, recipientId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * Returns total count of recipients.
     */
    public int getTotalRecipientsCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM recipients";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private Recipient mapResultSetToRecipient(ResultSet rs) throws SQLException {
        return new Recipient(
                rs.getString("recipient_id"),
                rs.getString("name"),
                rs.getInt("age"),
                rs.getString("phone"),
                rs.getString("address"),
                rs.getString("blood_group"),
                rs.getInt("units_required"),
                rs.getString("hospital")
        );
    }
}
