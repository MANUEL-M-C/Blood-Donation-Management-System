package com.blooddonation.service;

import com.blooddonation.dao.RecipientDAO;
import com.blooddonation.model.Recipient;
import com.blooddonation.util.ValidationUtil;

import java.sql.SQLException;
import java.util.List;

/**
 * ============================================================================
 * OOP CONCEPT: SERVICE LAYER & ABSTRACTION
 * ============================================================================
 * RecipientService coordinates recipient registration and blood need management.
 */
public class RecipientService {
    private final RecipientDAO recipientDAO;

    public RecipientService() {
        this.recipientDAO = new RecipientDAO();
    }

    public RecipientService(RecipientDAO recipientDAO) {
        this.recipientDAO = recipientDAO;
    }

    /**
     * Registers a new recipient after validation.
     */
    public boolean registerRecipient(String recipientId, String name, String ageStr, String phone,
                                     String address, String bloodGroup, String unitsStr, String hospital) throws SQLException {
        ValidationUtil.requireNonEmpty(recipientId, "Recipient ID");
        ValidationUtil.requireNonEmpty(name, "Recipient Name");
        int age = ValidationUtil.parseRecipientAge(ageStr);
        ValidationUtil.validatePhone(phone);
        ValidationUtil.requireNonEmpty(address, "Address");
        ValidationUtil.validateBloodGroup(bloodGroup);
        int units = ValidationUtil.parseUnits(unitsStr, "Units Required");

        Recipient recipient = new Recipient(recipientId, name, age, phone, address, bloodGroup, units, hospital);
        return recipientDAO.insertRecipient(recipient);
    }

    /**
     * Updates an existing recipient.
     */
    public boolean updateRecipient(String recipientId, String name, String ageStr, String phone,
                                   String address, String bloodGroup, String unitsStr, String hospital) throws SQLException {
        ValidationUtil.requireNonEmpty(recipientId, "Recipient ID");
        ValidationUtil.requireNonEmpty(name, "Recipient Name");
        int age = ValidationUtil.parseRecipientAge(ageStr);
        ValidationUtil.validatePhone(phone);
        ValidationUtil.requireNonEmpty(address, "Address");
        ValidationUtil.validateBloodGroup(bloodGroup);
        int units = ValidationUtil.parseUnits(unitsStr, "Units Required");

        Recipient recipient = new Recipient(recipientId, name, age, phone, address, bloodGroup, units, hospital);
        return recipientDAO.updateRecipient(recipient);
    }

    /**
     * Deletes a recipient.
     */
    public boolean deleteRecipient(String recipientId) throws SQLException {
        ValidationUtil.requireNonEmpty(recipientId, "Recipient ID");
        return recipientDAO.deleteRecipient(recipientId);
    }

    /**
     * Retrieves recipient by ID.
     */
    public Recipient getRecipient(String recipientId) throws SQLException {
        ValidationUtil.requireNonEmpty(recipientId, "Recipient ID");
        return recipientDAO.getRecipientById(recipientId);
    }

    /**
     * Retrieves all recipients.
     */
    public List<Recipient> getAllRecipients() throws SQLException {
        return recipientDAO.getAllRecipients();
    }

    /**
     * Searches recipients by keyword and blood group filter.
     */
    public List<Recipient> searchRecipients(String keyword, String bloodGroup) throws SQLException {
        return recipientDAO.searchRecipients(keyword, bloodGroup);
    }

    /**
     * Returns total count of registered recipients.
     */
    public int getTotalRecipients() throws SQLException {
        return recipientDAO.getTotalRecipientsCount();
    }
}
