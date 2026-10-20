package com.blooddonation.dao;

import com.blooddonation.model.BloodRequest;
import com.blooddonation.model.BloodUnit;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================================
 * OOP CONCEPT: ASSOCIATION, ABSTRACTION & TRANSACTION MANAGEMENT
 * ============================================================================
 * BloodRequestDAO manages blood request submissions and inventory allocation.
 * Enforces transactional safety: blood units are only marked ISSUED if sufficient
 * stock is verified within an atomic database transaction.
 */
public class BloodRequestDAO {
    private final BloodUnitDAO bloodUnitDAO = new BloodUnitDAO();

    /**
     * Submits a new Blood Request.
     */
    public boolean createRequest(BloodRequest request) throws SQLException {
        String sql = "INSERT INTO blood_requests (request_id, recipient_id, blood_group, units_requested, request_date, status, hospital) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, request.getRequestId());
            pstmt.setString(2, request.getRecipientId());
            pstmt.setString(3, request.getBloodGroup());
            pstmt.setInt(4, request.getUnitsRequested());
            pstmt.setString(5, request.getRequestDate());
            pstmt.setString(6, request.getStatus());
            pstmt.setString(7, request.getHospital());
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Fulfills and issues blood for a request atomically:
     * 1. Checks available units of requested blood group.
     * 2. If available >= requested:
     *    - Creates or updates the BloodRequest record with status 'ISSUED'.
     *    - Selects the required BloodUnits in FIFO order and marks them 'ISSUED'.
     *    - Commits transaction.
     * 3. If insufficient stock:
     *    - Throws SQLException detailing stock deficit.
     */
    public boolean processAndIssueBlood(BloodRequest request) throws SQLException {
        String bloodGroup = request.getBloodGroup().toUpperCase();
        int requestedUnits = request.getUnitsRequested();

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Fetch available blood units in FIFO order
                List<BloodUnit> availableUnits = bloodUnitDAO.getAvailableUnitsFIFO(bloodGroup, requestedUnits, conn);

                if (availableUnits.size() < requestedUnits) {
                    throw new SQLException(String.format(
                            "Insufficient stock for %s. Required: %d unit(s), Available: %d unit(s).",
                            bloodGroup, requestedUnits, availableUnits.size()
                    ));
                }

                // 2. Insert or update the Blood Request record
                String upsertSql = """
                    INSERT INTO blood_requests (request_id, recipient_id, blood_group, units_requested, request_date, status, hospital)
                    VALUES (?, ?, ?, ?, ?, 'ISSUED', ?)
                    ON CONFLICT(request_id) DO UPDATE SET status = 'ISSUED'
                """;
                try (PreparedStatement pstmt = conn.prepareStatement(upsertSql)) {
                    pstmt.setString(1, request.getRequestId());
                    pstmt.setString(2, request.getRecipientId());
                    pstmt.setString(3, bloodGroup);
                    pstmt.setInt(4, requestedUnits);
                    pstmt.setString(5, request.getRequestDate());
                    pstmt.setString(6, request.getHospital());
                    pstmt.executeUpdate();
                }

                // 3. Mark selected units as ISSUED
                String updateUnitSql = "UPDATE blood_units SET status = 'ISSUED' WHERE unit_id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(updateUnitSql)) {
                    for (BloodUnit unit : availableUnits) {
                        pstmt.setString(1, unit.getUnitId());
                        pstmt.addBatch();
                    }
                    pstmt.executeBatch();
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
     * Updates status of an existing blood request (e.g. APPROVED, REJECTED).
     */
    public boolean updateRequestStatus(String requestId, String status) throws SQLException {
        String sql = "UPDATE blood_requests SET status = ? WHERE request_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status.toUpperCase());
            pstmt.setString(2, requestId);
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Retrieves all blood requests with recipient details.
     */
    public List<BloodRequest> getAllRequests() throws SQLException {
        String sql = """
            SELECT br.request_id, br.recipient_id, p.name AS recipient_name, br.blood_group,
                   br.units_requested, br.request_date, br.status, br.hospital
            FROM blood_requests br
            LEFT JOIN recipients r ON br.recipient_id = r.recipient_id
            LEFT JOIN persons p ON r.person_id = p.person_id
            ORDER BY br.request_date DESC, br.request_id DESC
        """;

        List<BloodRequest> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                BloodRequest req = new BloodRequest(
                        rs.getString("request_id"),
                        rs.getString("recipient_id"),
                        rs.getString("blood_group"),
                        rs.getInt("units_requested"),
                        rs.getString("request_date"),
                        rs.getString("status"),
                        rs.getString("hospital")
                );
                req.setRecipientName(rs.getString("recipient_name"));
                list.add(req);
            }
        }
        return list;
    }

    /**
     * Returns count of requests currently in PENDING status.
     */
    public int getPendingRequestsCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM blood_requests WHERE status = 'PENDING'";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    /**
     * Checks if a request ID exists.
     */
    public boolean requestExists(String requestId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM blood_requests WHERE request_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, requestId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }
}
