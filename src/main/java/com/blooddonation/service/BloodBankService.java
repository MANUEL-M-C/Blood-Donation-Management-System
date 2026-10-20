package com.blooddonation.service;

import com.blooddonation.dao.*;
import com.blooddonation.model.BloodBank;
import com.blooddonation.model.BloodRequest;
import com.blooddonation.model.BloodUnit;
import com.blooddonation.util.ValidationUtil;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ============================================================================
 * OOP CONCEPT: CONTROLLER / SERVICE & AGGREGATION
 * ============================================================================
 * BloodBankService coordinates inventory tracking, search queries, stock level
 * evaluations, and the atomic blood issue workflow.
 */
public class BloodBankService {
    private final BloodUnitDAO bloodUnitDAO;
    private final BloodRequestDAO bloodRequestDAO;
    private final DonorDAO donorDAO;
    private final RecipientDAO recipientDAO;
    private final DonationDAO donationDAO;

    public BloodBankService() {
        this.bloodUnitDAO = new BloodUnitDAO();
        this.bloodRequestDAO = new BloodRequestDAO();
        this.donorDAO = new DonorDAO();
        this.recipientDAO = new RecipientDAO();
        this.donationDAO = new DonationDAO();
    }

    public BloodBankService(BloodUnitDAO bloodUnitDAO, BloodRequestDAO bloodRequestDAO,
                            DonorDAO donorDAO, RecipientDAO recipientDAO, DonationDAO donationDAO) {
        this.bloodUnitDAO = bloodUnitDAO;
        this.bloodRequestDAO = bloodRequestDAO;
        this.donorDAO = donorDAO;
        this.recipientDAO = recipientDAO;
        this.donationDAO = donationDAO;
    }

    /**
     * Retrieves map of available units for all 8 standard blood groups.
     */
    public Map<String, Integer> getInventoryStock() throws SQLException {
        return bloodUnitDAO.getAvailableUnitsGrouped();
    }

    /**
     * Searches available count for a blood group.
     */
    public int searchBlood(String bloodGroup) throws SQLException {
        ValidationUtil.validateBloodGroup(bloodGroup);
        return bloodUnitDAO.getAvailableCount(bloodGroup.trim().toUpperCase());
    }

    /**
     * Processes a recipient blood request and issues units if stock is sufficient.
     */
    public boolean requestAndIssueBlood(String requestId, String recipientId, String bloodGroup,
                                        String unitsStr, String hospital) throws SQLException {
        ValidationUtil.requireNonEmpty(requestId, "Request ID");
        ValidationUtil.requireNonEmpty(recipientId, "Recipient ID");
        ValidationUtil.validateBloodGroup(bloodGroup);
        int units = ValidationUtil.parseUnits(unitsStr, "Units Requested");

        // Verify recipient exists
        if (!recipientDAO.recipientExists(recipientId.trim())) {
            throw new SQLException("Recipient with ID '" + recipientId + "' is not registered.");
        }

        // Verify stock sufficiency
        int available = bloodUnitDAO.getAvailableCount(bloodGroup.trim().toUpperCase());
        if (available < units) {
            throw new SQLException(String.format(
                    "Insufficient %s blood units! Requested: %d unit(s), Available: %d unit(s).",
                    bloodGroup.toUpperCase(), units, available
            ));
        }

        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        BloodRequest request = new BloodRequest(
                requestId.trim(),
                recipientId.trim(),
                bloodGroup.trim().toUpperCase(),
                units,
                today,
                BloodRequest.STATUS_ISSUED,
                hospital != null ? hospital.trim() : "General Hospital"
        );

        return bloodRequestDAO.processAndIssueBlood(request);
    }

    /**
     * Updates an individual blood unit's status (e.g. mark EXPIRED or AVAILABLE).
     */
    public boolean updateUnitStatus(String unitId, String status) throws SQLException {
        ValidationUtil.requireNonEmpty(unitId, "Unit ID");
        ValidationUtil.requireNonEmpty(status, "Status");
        return bloodUnitDAO.updateUnitStatus(unitId.trim(), status.trim());
    }

    /**
     * Retrieves all individual blood units in inventory.
     */
    public List<BloodUnit> getAllUnits() throws SQLException {
        return bloodUnitDAO.getAllUnits();
    }

    /**
     * Retrieves all recipient blood requests.
     */
    public List<BloodRequest> getAllRequests() throws SQLException {
        return bloodRequestDAO.getAllRequests();
    }

    /**
     * Gathers live dashboard KPI metric statistics.
     */
    public Map<String, Integer> getDashboardStatistics() throws SQLException {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("totalDonors", donorDAO.getTotalDonorsCount());
        stats.put("totalRecipients", recipientDAO.getTotalRecipientsCount());
        stats.put("totalDonations", donationDAO.getTotalDonationsCount());
        stats.put("availableUnits", bloodUnitDAO.getTotalAvailableUnitsCount());
        stats.put("pendingRequests", bloodRequestDAO.getPendingRequestsCount());
        stats.put("lowStockGroups", bloodUnitDAO.getLowStockGroupsCount());
        return stats;
    }
}
