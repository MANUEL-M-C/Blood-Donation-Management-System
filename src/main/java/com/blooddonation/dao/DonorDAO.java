package com.blooddonation.dao;

import com.blooddonation.model.Donor;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================================
 * OOP CONCEPT: ABSTRACTION & DATA ACCESS OBJECT (DAO) PATTERN
 * ============================================================================
 * DonorDAO completely abstracts and encapsulates all database CRUD interactions
 * for Donor entities, preventing persistence logic from leaking into UI or Model.
 */
public class DonorDAO {

    /**
     * Inserts a new Donor record along with its underlying Person record.
     */
    public boolean insertDonor(Donor donor) throws SQLException {
        if (donorExists(donor.getDonorId())) {
            throw new SQLException("A donor with ID '" + donor.getDonorId() + "' already exists.");
        }

        String personSql = "INSERT INTO persons (name, age, phone, address, person_type) VALUES (?, ?, ?, ?, 'DONOR')";
        String donorSql = "INSERT INTO donors (donor_id, person_id, blood_group, last_donation_date) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                long personId;
                try (PreparedStatement pstmt = conn.prepareStatement(personSql, Statement.RETURN_GENERATED_KEYS)) {
                    pstmt.setString(1, donor.getName());
                    pstmt.setInt(2, donor.getAge());
                    pstmt.setString(3, donor.getPhone());
                    pstmt.setString(4, donor.getAddress());
                    pstmt.executeUpdate();

                    try (ResultSet rs = pstmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            personId = rs.getLong(1);
                        } else {
                            throw new SQLException("Failed to obtain generated person_id.");
                        }
                    }
                }

                try (PreparedStatement pstmt = conn.prepareStatement(donorSql)) {
                    pstmt.setString(1, donor.getDonorId());
                    pstmt.setLong(2, personId);
                    pstmt.setString(3, donor.getBloodGroup());
                    pstmt.setString(4, donor.getLastDonationDate());
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
     * Updates an existing Donor record and its Person fields.
     */
    public boolean updateDonor(Donor donor) throws SQLException {
        String query = """
            UPDATE persons
            SET name = ?, age = ?, phone = ?, address = ?
            WHERE person_id = (SELECT person_id FROM donors WHERE donor_id = ?)
        """;
        String donorQuery = """
            UPDATE donors
            SET blood_group = ?, last_donation_date = ?
            WHERE donor_id = ?
        """;

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int affected;
                try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                    pstmt.setString(1, donor.getName());
                    pstmt.setInt(2, donor.getAge());
                    pstmt.setString(3, donor.getPhone());
                    pstmt.setString(4, donor.getAddress());
                    pstmt.setString(5, donor.getDonorId());
                    affected = pstmt.executeUpdate();
                }

                if (affected == 0) {
                    conn.rollback();
                    return false;
                }

                try (PreparedStatement pstmt = conn.prepareStatement(donorQuery)) {
                    pstmt.setString(1, donor.getBloodGroup());
                    pstmt.setString(2, donor.getLastDonationDate());
                    pstmt.setString(3, donor.getDonorId());
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
     * Updates only the last donation date for a donor.
     */
    public boolean updateLastDonationDate(String donorId, String lastDate) throws SQLException {
        String sql = "UPDATE donors SET last_donation_date = ? WHERE donor_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, lastDate);
            pstmt.setString(2, donorId);
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Deletes a donor by ID (cascade deletes the person record).
     */
    public boolean deleteDonor(String donorId) throws SQLException {
        // Find person_id
        String findPersonSql = "SELECT person_id FROM donors WHERE donor_id = ?";
        String deletePersonSql = "DELETE FROM persons WHERE person_id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                long personId = -1;
                try (PreparedStatement pstmt = conn.prepareStatement(findPersonSql)) {
                    pstmt.setString(1, donorId);
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

                // Deleting person cascade-deletes the donor record
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
     * Fetches a donor by ID.
     */
    public Donor getDonorById(String donorId) throws SQLException {
        String sql = """
            SELECT d.donor_id, p.name, p.age, p.phone, p.address, d.blood_group, d.last_donation_date
            FROM donors d
            JOIN persons p ON d.person_id = p.person_id
            WHERE d.donor_id = ?
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, donorId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToDonor(rs);
                }
            }
        }
        return null;
    }

    /**
     * Retrieves all registered donors.
     */
    public List<Donor> getAllDonors() throws SQLException {
        String sql = """
            SELECT d.donor_id, p.name, p.age, p.phone, p.address, d.blood_group, d.last_donation_date
            FROM donors d
            JOIN persons p ON d.person_id = p.person_id
            ORDER BY d.donor_id ASC
        """;

        List<Donor> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToDonor(rs));
            }
        }
        return list;
    }

    /**
     * Searches donors by ID, Name, or Blood Group.
     */
    public List<Donor> searchDonors(String keyword, String bloodGroupFilter) throws SQLException {
        StringBuilder sql = new StringBuilder("""
            SELECT d.donor_id, p.name, p.age, p.phone, p.address, d.blood_group, d.last_donation_date
            FROM donors d
            JOIN persons p ON d.person_id = p.person_id
            WHERE 1=1
        """);

        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (LOWER(d.donor_id) LIKE ? OR LOWER(p.name) LIKE ? OR p.phone LIKE ?)");
            String pattern = "%" + keyword.trim().toLowerCase() + "%";
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
        }

        if (bloodGroupFilter != null && !bloodGroupFilter.equalsIgnoreCase("ALL") && !bloodGroupFilter.trim().isEmpty()) {
            sql.append(" AND d.blood_group = ?");
            params.add(bloodGroupFilter.trim().toUpperCase());
        }

        sql.append(" ORDER BY d.donor_id ASC");

        List<Donor> results = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapResultSetToDonor(rs));
                }
            }
        }
        return results;
    }

    /**
     * Checks if a donor with the given ID exists.
     */
    public boolean donorExists(String donorId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM donors WHERE donor_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, donorId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * Returns total count of registered donors.
     */
    public int getTotalDonorsCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM donors";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private Donor mapResultSetToDonor(ResultSet rs) throws SQLException {
        return new Donor(
                rs.getString("donor_id"),
                rs.getString("name"),
                rs.getInt("age"),
                rs.getString("phone"),
                rs.getString("address"),
                rs.getString("blood_group"),
                rs.getString("last_donation_date")
        );
    }
}
