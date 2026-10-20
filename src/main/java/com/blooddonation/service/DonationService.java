package com.blooddonation.service;

import com.blooddonation.dao.DonationDAO;
import com.blooddonation.dao.DonorDAO;
import com.blooddonation.model.Donation;
import com.blooddonation.model.Donor;
import com.blooddonation.util.ValidationUtil;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * ============================================================================
 * OOP CONCEPT: SERVICE LAYER & ASSOCIATION
 * ============================================================================
 * DonationService executes the end-to-end blood donation lifecycle:
 * 1. Validates inputs and confirms donor registration.
 * 2. Persists the donation record.
 * 3. Credits inventory by creating individual BloodUnit records.
 * 4. Updates donor's last donation date.
 */
public class DonationService {
    private final DonationDAO donationDAO;
    private final DonorDAO donorDAO;

    public DonationService() {
        this.donationDAO = new DonationDAO();
        this.donorDAO = new DonorDAO();
    }

    public DonationService(DonationDAO donationDAO, DonorDAO donorDAO) {
        this.donationDAO = donationDAO;
        this.donorDAO = donorDAO;
    }

    /**
     * Records a new blood donation event.
     */
    public boolean recordDonation(String donationId, String donorId, String date,
                                  String unitsStr, String bloodGroup) throws SQLException {
        ValidationUtil.requireNonEmpty(donationId, "Donation ID");
        ValidationUtil.requireNonEmpty(donorId, "Donor ID");
        int units = ValidationUtil.parseUnits(unitsStr, "Number of Units");

        if (date == null || date.trim().isEmpty()) {
            date = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        }

        // 1. Verify donor existence
        Donor donor = donorDAO.getDonorById(donorId.trim());
        if (donor == null) {
            throw new SQLException("Donor with ID '" + donorId + "' not found. Please register donor first.");
        }

        // Use donor's verified blood group if none provided
        if (bloodGroup == null || bloodGroup.trim().isEmpty() || bloodGroup.equalsIgnoreCase("AUTO")) {
            bloodGroup = donor.getBloodGroup();
        }

        if (donationDAO.donationExists(donationId.trim())) {
            throw new SQLException("Donation ID '" + donationId + "' already exists.");
        }

        Donation donation = new Donation(donationId.trim(), donorId.trim(), date.trim(), units, bloodGroup.trim());
        return donationDAO.recordDonation(donation);
    }

    /**
     * Retrieves all recorded donations.
     */
    public List<Donation> getAllDonations() throws SQLException {
        return donationDAO.getAllDonations();
    }

    /**
     * Searches donations by donor ID or keyword.
     */
    public List<Donation> searchDonations(String keyword) throws SQLException {
        return donationDAO.searchDonations(keyword);
    }

    /**
     * Returns total count of recorded donations.
     */
    public int getTotalDonations() throws SQLException {
        return donationDAO.getTotalDonationsCount();
    }
}
