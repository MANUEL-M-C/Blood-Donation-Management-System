package com.blooddonation.dao;

import com.blooddonation.model.BloodBank;
import com.blooddonation.model.BloodUnit;

import java.sql.*;
import java.util.*;

/**
 * ============================================================================
 * OOP CONCEPT: ABSTRACTION & DATA ACCESS OBJECT (DAO) PATTERN
 * ============================================================================
 * BloodUnitDAO manages inventory persistence for individual blood units,
 * including FIFO issue strategies and status lifecycle transitions.
 */
public class BloodUnitDAO {

    /**
     * Inserts an individual blood unit into the database.
     */
    public boolean insertUnit(BloodUnit unit) throws SQLException {
        String sql = "INSERT INTO blood_units (unit_id, donation_id, blood_group, collection_date, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, unit.getUnitId());
            pstmt.setString(2, unit.getDonationId());
            pstmt.setString(3, unit.getBloodGroup());
            pstmt.setString(4, unit.getCollectionDate());
            pstmt.setString(5, unit.getStatus());
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Inserts a batch of blood units transactionally.
     */
    public boolean insertUnits(List<BloodUnit> units, Connection conn) throws SQLException {
        String sql = "INSERT INTO blood_units (unit_id, donation_id, blood_group, collection_date, status) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (BloodUnit u : units) {
                pstmt.setString(1, u.getUnitId());
                pstmt.setString(2, u.getDonationId());
                pstmt.setString(3, u.getBloodGroup());
                pstmt.setString(4, u.getCollectionDate());
                pstmt.setString(5, u.getStatus());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            return true;
        }
    }

    /**
     * Updates status of a blood unit (e.g. AVAILABLE, ISSUED, EXPIRED).
     */
    public boolean updateUnitStatus(String unitId, String newStatus) throws SQLException {
        String sql = "UPDATE blood_units SET status = ? WHERE unit_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newStatus.toUpperCase());
            pstmt.setString(2, unitId);
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Retrieves the count of AVAILABLE blood units grouped by blood group.
     * Guarantees all 8 blood groups are represented in the returned map.
     */
    public Map<String, Integer> getAvailableUnitsGrouped() throws SQLException {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (String bg : BloodBank.BLOOD_GROUPS) {
            counts.put(bg, 0);
        }

        String sql = """
            SELECT blood_group, COUNT(*) as qty
            FROM blood_units
            WHERE status = 'AVAILABLE'
            GROUP BY blood_group
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String bg = rs.getString("blood_group");
                int qty = rs.getInt("qty");
                counts.put(bg, qty);
            }
        }
        return counts;
    }

    /**
     * Retrieves available count for a single blood group.
     */
    public int getAvailableCount(String bloodGroup) throws SQLException {
        String sql = "SELECT COUNT(*) FROM blood_units WHERE blood_group = ? AND status = 'AVAILABLE'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, bloodGroup);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /**
     * Retrieves available blood units for issue using FIFO order (earliest collection date first).
     */
    public List<BloodUnit> getAvailableUnitsFIFO(String bloodGroup, int limit, Connection conn) throws SQLException {
        String sql = """
            SELECT unit_id, donation_id, blood_group, collection_date, status
            FROM blood_units
            WHERE blood_group = ? AND status = 'AVAILABLE'
            ORDER BY collection_date ASC, unit_id ASC
            LIMIT ?
        """;

        List<BloodUnit> list = new ArrayList<>();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, bloodGroup);
            pstmt.setInt(2, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new BloodUnit(
                            rs.getString("unit_id"),
                            rs.getString("blood_group"),
                            rs.getString("collection_date"),
                            rs.getString("status"),
                            rs.getString("donation_id")
                    ));
                }
            }
        }
        return list;
    }

    /**
     * Retrieves all blood units in inventory.
     */
    public List<BloodUnit> getAllUnits() throws SQLException {
        String sql = "SELECT unit_id, donation_id, blood_group, collection_date, status FROM blood_units ORDER BY collection_date DESC, unit_id DESC";
        List<BloodUnit> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new BloodUnit(
                        rs.getString("unit_id"),
                        rs.getString("blood_group"),
                        rs.getString("collection_date"),
                        rs.getString("status"),
                        rs.getString("donation_id")
                ));
            }
        }
        return list;
    }

    /**
     * Retrieves all issued blood units.
     */
    public List<BloodUnit> getIssuedUnits() throws SQLException {
        String sql = "SELECT unit_id, donation_id, blood_group, collection_date, status FROM blood_units WHERE status = 'ISSUED' ORDER BY unit_id DESC";
        List<BloodUnit> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new BloodUnit(
                        rs.getString("unit_id"),
                        rs.getString("blood_group"),
                        rs.getString("collection_date"),
                        rs.getString("status"),
                        rs.getString("donation_id")
                ));
            }
        }
        return list;
    }

    /**
     * Returns total count of all AVAILABLE units across all groups.
     */
    public int getTotalAvailableUnitsCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM blood_units WHERE status = 'AVAILABLE'";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    /**
     * Returns total count of ISSUED units.
     */
    public int getTotalIssuedUnitsCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM blood_units WHERE status = 'ISSUED'";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    /**
     * Counts how many blood groups are in LOW STOCK (<= 3 units).
     */
    public int getLowStockGroupsCount() throws SQLException {
        Map<String, Integer> map = getAvailableUnitsGrouped();
        int count = 0;
        for (int qty : map.values()) {
            if (qty <= 3) {
                count++;
            }
        }
        return count;
    }
}
