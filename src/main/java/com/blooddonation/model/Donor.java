package com.blooddonation.model;

/**
 * ============================================================================
 * OOP CONCEPT: INHERITANCE, ENCAPSULATION & POLYMORPHISM
 * ============================================================================
 * Donor extends Person, inheriting common attributes (name, age, phone, address)
 * while encapsulating donor-specific data (donorId, bloodGroup, lastDonationDate).
 */
public class Donor extends Person {
    // Private attributes (Encapsulation)
    private String donorId;
    private String bloodGroup;
    private String lastDonationDate; // e.g. "YYYY-MM-DD" or "Never"

    public Donor() {
        super();
    }

    /**
     * Parameterized Constructor (Invokes superclass constructor)
     */
    public Donor(String donorId, String name, int age, String phone, String address,
                 String bloodGroup, String lastDonationDate) {
        super(name, age, phone, address); // INHERITANCE: super constructor call
        setDonorId(donorId);
        setBloodGroup(bloodGroup);
        setLastDonationDate(lastDonationDate);
    }

    // Getters and Setters (Encapsulation)
    public String getDonorId() {
        return donorId;
    }

    public void setDonorId(String donorId) {
        if (donorId == null || donorId.trim().isEmpty()) {
            throw new IllegalArgumentException("Donor ID cannot be empty.");
        }
        this.donorId = donorId.trim().toUpperCase();
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

    public String getLastDonationDate() {
        return lastDonationDate;
    }

    public void setLastDonationDate(String lastDonationDate) {
        if (lastDonationDate == null || lastDonationDate.trim().isEmpty()) {
            this.lastDonationDate = "None";
        } else {
            this.lastDonationDate = lastDonationDate.trim();
        }
    }

    /**
     * Business function: registers donor details and validates fields.
     */
    public boolean registerDonor() {
        return donorId != null && !donorId.isEmpty() &&
               getName() != null && !getName().isEmpty() &&
               getAge() >= 18 && getAge() <= 65 &&
               bloodGroup != null && !bloodGroup.isEmpty();
    }

    /**
     * Overloaded updateDetails method updating both Person and Donor attributes.
     */
    public void updateDetails(String name, int age, String phone, String address,
                              String bloodGroup, String lastDonationDate) {
        super.updateDetails(name, age, phone, address);
        setBloodGroup(bloodGroup);
        setLastDonationDate(lastDonationDate);
    }

    /**
     * ========================================================================
     * OOP CONCEPT: POLYMORPHISM (Method Overriding)
     * ========================================================================
     * Overrides abstract displayDetails() from superclass Person to provide
     * donor-specific formatted information.
     */
    @Override
    public String displayDetails() {
        return String.format("[DONOR #%s] %s | Age: %d | Blood Group: %s | Phone: %s | Last Donated: %s | Address: %s",
                donorId, getName(), getAge(), bloodGroup, getPhone(), lastDonationDate, getAddress());
    }

    @Override
    public String toString() {
        return displayDetails();
    }
}
