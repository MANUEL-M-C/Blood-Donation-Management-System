package com.blooddonation.model;

/**
 * ============================================================================
 * OOP CONCEPT: ASSOCIATION & ENCAPSULATION
 * ============================================================================
 * Donation records a donation event made by a Donor.
 * Demonstrates ASSOCIATION:
 * A Donation is associated with a Donor via donorId (and optional donor reference).
 * A Donation generates one or more BloodUnits (Aggregation/Composition).
 */
public class Donation {
    // Private attributes (Encapsulation)
    private String donationId;
    private String donorId;       // Association to Donor
    private String donorName;     // Denormalized for display convenience
    private String date;          // e.g. "YYYY-MM-DD"
    private int units;            // Number of units donated
    private String bloodGroup;

    public Donation() {
    }

    public Donation(String donationId, String donorId, String date, int units, String bloodGroup) {
        setDonationId(donationId);
        setDonorId(donorId);
        setDate(date);
        setUnits(units);
        setBloodGroup(bloodGroup);
    }

    public Donation(String donationId, String donorId, String donorName, String date, int units, String bloodGroup) {
        this(donationId, donorId, date, units, bloodGroup);
        this.donorName = donorName;
    }

    // Getters and Setters
    public String getDonationId() {
        return donationId;
    }

    public void setDonationId(String donationId) {
        if (donationId == null || donationId.trim().isEmpty()) {
            throw new IllegalArgumentException("Donation ID cannot be empty.");
        }
        this.donationId = donationId.trim().toUpperCase();
    }

    public String getDonorId() {
        return donorId;
    }

    public void setDonorId(String donorId) {
        if (donorId == null || donorId.trim().isEmpty()) {
            throw new IllegalArgumentException("Donor ID cannot be empty.");
        }
        this.donorId = donorId.trim().toUpperCase();
    }

    public String getDonorName() {
        return donorName;
    }

    public void setDonorName(String donorName) {
        this.donorName = donorName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        if (date == null || date.trim().isEmpty()) {
            throw new IllegalArgumentException("Donation date cannot be empty.");
        }
        this.date = date.trim();
    }

    public int getUnits() {
        return units;
    }

    public void setUnits(int units) {
        if (units <= 0) {
            throw new IllegalArgumentException("Donated units must be greater than zero.");
        }
        this.units = units;
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

    /**
     * Business validation before recording donation
     */
    public boolean recordDonation() {
        return donationId != null && !donationId.isEmpty() &&
               donorId != null && !donorId.isEmpty() &&
               units > 0 && bloodGroup != null && !bloodGroup.isEmpty();
    }

    /**
     * Returns a formatted summary of the donation details
     */
    public String getDonationDetails() {
        return String.format("[DONATION #%s] Donor: %s (%s) | Date: %s | Units: %d | Blood Group: %s",
                donationId, (donorName != null ? donorName : donorId), donorId, date, units, bloodGroup);
    }

    @Override
    public String toString() {
        return getDonationDetails();
    }
}
