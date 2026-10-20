package com.blooddonation.model;

/**
 * ============================================================================
 * OOP CONCEPT: ENCAPSULATION & DOMAIN MODELLING
 * ============================================================================
 * BloodUnit models an individual unit of blood collected in the blood bank.
 * Tracks blood group, collection timestamp, donation association, and current
 * inventory status (AVAILABLE, ISSUED, EXPIRED).
 */
public class BloodUnit {
    public static final String STATUS_AVAILABLE = "AVAILABLE";
    public static final String STATUS_ISSUED = "ISSUED";
    public static final String STATUS_EXPIRED = "EXPIRED";

    // Private attributes (Encapsulation)
    private String unitId;
    private String bloodGroup;
    private String collectionDate;
    private String status;
    private String donationId; // Association to Donation

    public BloodUnit() {
        this.status = STATUS_AVAILABLE;
    }

    public BloodUnit(String unitId, String bloodGroup, String collectionDate, String status, String donationId) {
        setUnitId(unitId);
        setBloodGroup(bloodGroup);
        setCollectionDate(collectionDate);
        setStatus(status);
        setDonationId(donationId);
    }

    public BloodUnit(String unitId, String bloodGroup, String collectionDate, String donationId) {
        this(unitId, bloodGroup, collectionDate, STATUS_AVAILABLE, donationId);
    }

    // Getters and Setters
    public String getUnitId() {
        return unitId;
    }

    public void setUnitId(String unitId) {
        if (unitId == null || unitId.trim().isEmpty()) {
            throw new IllegalArgumentException("Unit ID cannot be empty.");
        }
        this.unitId = unitId.trim().toUpperCase();
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        if (bloodGroup == null || bloodGroup.trim().isEmpty()) {
            throw new IllegalArgumentException("Blood group cannot be empty.");
        }
        this.bloodGroup = bloodGroup.trim().toUpperCase();
    }

    public String getCollectionDate() {
        return collectionDate;
    }

    public void setCollectionDate(String collectionDate) {
        this.collectionDate = (collectionDate != null && !collectionDate.trim().isEmpty()) ? collectionDate.trim() : "Unknown";
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        if (status == null || (!status.equalsIgnoreCase(STATUS_AVAILABLE) &&
                !status.equalsIgnoreCase(STATUS_ISSUED) &&
                !status.equalsIgnoreCase(STATUS_EXPIRED))) {
            throw new IllegalArgumentException("Invalid status: " + status + ". Expected AVAILABLE, ISSUED, or EXPIRED.");
        }
        this.status = status.toUpperCase();
    }

    public String getDonationId() {
        return donationId;
    }

    public void setDonationId(String donationId) {
        this.donationId = donationId;
    }

    // Business functions
    public String checkStatus() {
        return this.status;
    }

    public void updateStatus(String newStatus) {
        setStatus(newStatus);
    }

    public boolean isAvailable() {
        return STATUS_AVAILABLE.equalsIgnoreCase(this.status);
    }

    @Override
    public String toString() {
        return String.format("[UNIT %s] Group: %s | Collected: %s | Status: %s | Donation: %s",
                unitId, bloodGroup, collectionDate, status, donationId);
    }
}
