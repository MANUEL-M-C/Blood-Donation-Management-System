package com.blooddonation.service;

import com.blooddonation.dao.DonorDAO;
import com.blooddonation.model.Donor;
import com.blooddonation.util.ValidationUtil;

import java.sql.SQLException;
import java.util.List;

/**
 * ============================================================================
 * OOP CONCEPT: SERVICE LAYER & ABSTRACTION
 * ============================================================================
 * DonorService encapsulates business rules, input validations, and error handling
 * for donor operations, insulating the UI presentation layer from database concerns.
 */
public class DonorService {
    private final DonorDAO donorDAO;

    public DonorService() {
        this.donorDAO = new DonorDAO();
    }

    public DonorService(DonorDAO donorDAO) {
        this.donorDAO = donorDAO;
    }

    /**
     * Registers a new donor after strict validation.
     */
    public boolean registerDonor(String donorId, String name, String ageStr, String phone,
                                 String address, String bloodGroup, String lastDonationDate) throws SQLException {
        ValidationUtil.requireNonEmpty(donorId, "Donor ID");
        ValidationUtil.requireNonEmpty(name, "Donor Name");
        int age = ValidationUtil.parseDonorAge(ageStr);
        ValidationUtil.validatePhone(phone);
        ValidationUtil.requireNonEmpty(address, "Address");
        ValidationUtil.validateBloodGroup(bloodGroup);

        Donor donor = new Donor(donorId, name, age, phone, address, bloodGroup, lastDonationDate);
        return donorDAO.insertDonor(donor);
    }

    /**
     * Updates donor details after validation.
     */
    public boolean updateDonor(String donorId, String name, String ageStr, String phone,
                               String address, String bloodGroup, String lastDonationDate) throws SQLException {
        ValidationUtil.requireNonEmpty(donorId, "Donor ID");
        ValidationUtil.requireNonEmpty(name, "Donor Name");
        int age = ValidationUtil.parseDonorAge(ageStr);
        ValidationUtil.validatePhone(phone);
        ValidationUtil.requireNonEmpty(address, "Address");
        ValidationUtil.validateBloodGroup(bloodGroup);

        Donor donor = new Donor(donorId, name, age, phone, address, bloodGroup, lastDonationDate);
        return donorDAO.updateDonor(donor);
    }

    /**
     * Deletes a donor by ID.
     */
    public boolean deleteDonor(String donorId) throws SQLException {
        ValidationUtil.requireNonEmpty(donorId, "Donor ID");
        return donorDAO.deleteDonor(donorId);
    }

    /**
     * Fetches a donor by ID.
     */
    public Donor getDonor(String donorId) throws SQLException {
        ValidationUtil.requireNonEmpty(donorId, "Donor ID");
        return donorDAO.getDonorById(donorId);
    }

    /**
     * Retrieves all registered donors.
     */
    public List<Donor> getAllDonors() throws SQLException {
        return donorDAO.getAllDonors();
    }

    /**
     * Searches donors by search term and optional blood group filter.
     */
    public List<Donor> searchDonors(String keyword, String bloodGroup) throws SQLException {
        return donorDAO.searchDonors(keyword, bloodGroup);
    }

    /**
     * Returns total count of donors.
     */
    public int getTotalDonors() throws SQLException {
        return donorDAO.getTotalDonorsCount();
    }
}
