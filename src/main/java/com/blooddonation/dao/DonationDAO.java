package com.blooddonation.dao;

import com.blooddonation.model.BloodUnit;
import com.blooddonation.model.Donation;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================================
 * OOP CONCEPT: ASSOCIATION & TRANSACTIONAL PERSISTENCE
 * ============================================================================
 * DonationDAO handles recording blood donation events:
 * 1. Checks that the Donor exists.
 * 2. Saves the Donation.
 * 3. Adds the donated units to blood inventory by creating individual BloodUnit records.
 * 4. Updates the Donor's last donation date.
 * 5. Maintains full ACID transaction integrity.
 */
public class DonationDAO {
    private final BloodUnitDAO bloodUnitDAO = new BloodUnitDAO();

    /**
     * Records a donation event transactionally.
     */
    public boolean recordDonation(Donation donation) throws SQLException {
        String insertDonationSql = "INSERT INTO donations (donation_id, donor_id, donation_date, units, blood_group) VALUES (?, ?, ?, ?, ?)";
        String updateDonorSql = "UPDATE donors SET last_donation_date = ? WHERE donor_id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Verify donor existence
                String checkDonorSql = "SELECT blood_group FROM donors WHERE donor_id = ?";
                String actualBloodGroup = null;
                try (PreparedStatement checkStmt = conn.prepareStatement(checkDonorSql)) {
                    checkStmt.setString(1, donation.getDonorId());
                    try (ResultSet rs = checkStmt.executeQuery()) {
                        if (rs.next()) {
                            actualBloodGroup = rs.getString("blood_group");
                        } else {
                            throw new SQLException("Donor with ID '" + donation.getDonorId() + "' does not exist.");
                        }
                    }
                }

                // If bloodGroup was not specified or blank, match donor's blood group
                if (donation.getBloodGroup() == null || donation.getBloodGroup().isEmpty()) {
                    donation.setBloodGroup(actualBloodGroup);
                }

                // 2. Insert Donation record
                try (PreparedStatement pstmt = conn.prepareStatement(insertDonationSql)) {
                    pstmt.setString(1, donation.getDonationId());
                    pstmt.setString(2, donation.getDonorId());
                    pstmt.setString(3, donation.getDate());
                    pstmt.setInt(4, donation.getUnits());
                    pstmt.setString(5, donation.getBloodGroup());
                    pstmt.executeUpdate();
                }

                // 3. Create individual BloodUnit records
                List<BloodUnit> units = new ArrayList<>();
                for (int i = 1; i <= donation.getUnits(); i++) {
                    String unitId = String.format("U-%s-%s-%d",
                            donation.getDonationId(),
                            donation.getBloodGroup().replace("+", "P").replace("-", "N"),
                            i);
                    BloodUnit unit = new BloodUnit(
                            unitId,
                            donation.getBloodGroup(),
                            donation.getDate(),
                            BloodUnit.STATUS_AVAILABLE,
                            donation.getDonationId()
                    );
                    units.add(unit);
                }
                bloodUnitDAO.insertUnits(units, conn);

                // 4. Update Donor's last donation date
                try (PreparedStatement pstmt = conn.prepareStatement(updateDonorSql)) {
                    pstmt.setString(1, donation.getDate());
                    pstmt.setString(2, donation.getDonorId());
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
     * Checks if donation ID exists.
     */
    public boolean donationExists(String donationId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM donations WHERE donation_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, donationId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * Retrieves all recorded donations with donor names.
     */
    public List<Donation> getAllDonations() throws SQLException {
        String sql = """
            SELECT d.donation_id, d.donor_id, p.name AS donor_name, d.donation_date, d.units, d.blood_group
            FROM donations d
            JOIN donors dn ON d.donor_id = dn.donor_id
            JOIN persons p ON dn.person_id = p.person_id
            ORDER BY d.donation_date DESC, d.donation_id DESC
        """;

        List<Donation> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToDonation(rs));
            }
        }
        return list;
    }

    /**
     * Searches donations by donor ID, donor name, or blood group.
     */
    public List<Donation> searchDonations(String keyword) throws SQLException {
        String sql = """
            SELECT d.donation_id, d.donor_id, p.name AS donor_name, d.donation_date, d.units, d.blood_group
            FROM donations d
            JOIN donors dn ON d.donor_id = dn.donor_id
            JOIN persons p ON dn.person_id = p.person_id
            WHERE LOWER(d.donation_id) LIKE ? OR LOWER(d.donor_id) LIKE ? OR LOWER(p.name) LIKE ? OR d.blood_group = ?
            ORDER BY d.donation_date DESC
        """;

        List<Donation> list = new ArrayList<>();
        String pattern = "%" + (keyword != null ? keyword.trim().toLowerCase() : "") + "%";
        String groupSearch = keyword != null ? keyword.trim().toUpperCase() : "";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, pattern);
            pstmt.setString(2, pattern);
            pstmt.setString(3, pattern);
            pstmt.setString(4, groupSearch);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToDonation(rs));
                }
            }
        }
        return list;
    }

    /**
     * Returns total count of recorded donations.
     */
    public int getTotalDonationsCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM donations";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private Donation mapResultSetToDonation(ResultSet rs) throws SQLException {
        return new Donation(
                rs.getString("donation_id"),
                rs.getString("donor_id"),
                rs.getString("donor_name"),
                rs.getString("donation_date"),
                rs.getInt("units"),
                rs.getString("blood_group")
        );
    }
}
