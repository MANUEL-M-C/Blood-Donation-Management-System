package com.blooddonation.model;

/**
 * ============================================================================
 * OOP CONCEPT: INHERITANCE, ENCAPSULATION & POLYMORPHISM
 * ============================================================================
 * Recipient extends Person, inheriting common attributes while encapsulating
 * medical recipient-specific data (recipientId, bloodGroup, unitsRequired, hospital).
 */
public class Recipient extends Person {
    // Private attributes (Encapsulation)
    private String recipientId;
    private String bloodGroup;
    private int unitsRequired;
    private String hospital;

    public Recipient() {
        super();
    }

    /**
     * Parameterized Constructor (Invokes superclass constructor)
     */
    public Recipient(String recipientId, String name, int age, String phone, String address,
                     String bloodGroup, int unitsRequired, String hospital) {
        super(name, age, phone, address); // INHERITANCE: super constructor
        setRecipientId(recipientId);
        setBloodGroup(bloodGroup);
        setUnitsRequired(unitsRequired);
        setHospital(hospital);
    }

    // Getters and Setters (Encapsulation)
    public String getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(String recipientId) {
        if (recipientId == null || recipientId.trim().isEmpty()) {
            throw new IllegalArgumentException("Recipient ID cannot be empty.");
        }
        this.recipientId = recipientId.trim().toUpperCase();
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

    public int getUnitsRequired() {
        return unitsRequired;
    }

    public void setUnitsRequired(int unitsRequired) {
        if (unitsRequired <= 0) {
            throw new IllegalArgumentException("Units required must be at least 1.");
        }
        this.unitsRequired = unitsRequired;
    }

    public String getHospital() {
        return hospital;
    }

    public void setHospital(String hospital) {
        if (hospital == null || hospital.trim().isEmpty()) {
            this.hospital = "General Hospital";
        } else {
            this.hospital = hospital.trim();
        }
    }

    /**
     * Business function: initiates a blood request verification.
     */
    public boolean requestBlood() {
        return recipientId != null && !recipientId.isEmpty() &&
               bloodGroup != null && !bloodGroup.isEmpty() &&
               unitsRequired > 0;
    }

    /**
     * Overloaded updateDetails method updating Person and Recipient fields.
     */
    public void updateDetails(String name, int age, String phone, String address,
                              String bloodGroup, int unitsRequired, String hospital) {
        super.updateDetails(name, age, phone, address);
        setBloodGroup(bloodGroup);
        setUnitsRequired(unitsRequired);
        setHospital(hospital);
    }

    /**
     * ========================================================================
     * OOP CONCEPT: POLYMORPHISM (Method Overriding)
     * ========================================================================
     * Overrides abstract displayDetails() from superclass Person to provide
     * recipient-specific information.
     */
    @Override
    public String displayDetails() {
        return String.format("[RECIPIENT #%s] %s | Age: %d | Blood Group: %s | Units Needed: %d | Hospital: %s | Phone: %s",
                recipientId, getName(), getAge(), bloodGroup, unitsRequired, hospital, getPhone());
    }

    @Override
    public String toString() {
        return displayDetails();
    }
}
