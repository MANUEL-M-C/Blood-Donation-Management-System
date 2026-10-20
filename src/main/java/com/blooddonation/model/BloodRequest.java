package com.blooddonation.model;

/**
 * ============================================================================
 * OOP CONCEPT: ASSOCIATION & ENCAPSULATION
 * ============================================================================
 * BloodRequest represents an official blood request submitted by or for a Recipient.
 * Tracks units requested, required blood group, destination hospital, and status lifecycle:
 * PENDING -> APPROVED -> ISSUED / REJECTED
 */
public class BloodRequest {
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_ISSUED = "ISSUED";
    public static final String STATUS_REJECTED = "REJECTED";

    // Private attributes (Encapsulation)
    private String requestId;
    private String recipientId;    // Association to Recipient
    private String recipientName;  // Convenient display field
    private String bloodGroup;
    private int unitsRequested;
    private String requestDate;
    private String status;
    private String hospital;

    public BloodRequest() {
        this.status = STATUS_PENDING;
    }

    public BloodRequest(String requestId, String recipientId, String bloodGroup,
                        int unitsRequested, String requestDate, String status, String hospital) {
        setRequestId(requestId);
        setRecipientId(recipientId);
        setBloodGroup(bloodGroup);
        setUnitsRequested(unitsRequested);
        setRequestDate(requestDate);
        setStatus(status);
        setHospital(hospital);
    }

    // Getters and Setters
    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        if (requestId == null || requestId.trim().isEmpty()) {
            throw new IllegalArgumentException("Request ID cannot be empty.");
        }
        this.requestId = requestId.trim().toUpperCase();
    }

    public String getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(String recipientId) {
        if (recipientId == null || recipientId.trim().isEmpty()) {
            throw new IllegalArgumentException("Recipient ID cannot be empty.");
        }
        this.recipientId = recipientId.trim().toUpperCase();
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
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

    public int getUnitsRequested() {
        return unitsRequested;
    }

    public void setUnitsRequested(int unitsRequested) {
        if (unitsRequested <= 0) {
            throw new IllegalArgumentException("Requested units must be at least 1.");
        }
        this.unitsRequested = unitsRequested;
    }

    public String getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(String requestDate) {
        this.requestDate = (requestDate != null && !requestDate.trim().isEmpty()) ? requestDate.trim() : "Unknown";
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        if (status == null || (!status.equalsIgnoreCase(STATUS_PENDING) &&
                !status.equalsIgnoreCase(STATUS_APPROVED) &&
                !status.equalsIgnoreCase(STATUS_ISSUED) &&
                !status.equalsIgnoreCase(STATUS_REJECTED))) {
            throw new IllegalArgumentException("Invalid request status: " + status);
        }
        this.status = status.toUpperCase();
    }

    public String getHospital() {
        return hospital;
    }

    public void setHospital(String hospital) {
        this.hospital = (hospital != null && !hospital.trim().isEmpty()) ? hospital.trim() : "General Hospital";
    }

    @Override
    public String toString() {
        return String.format("[REQUEST #%s] Recipient: %s | Group: %s | Units: %d | Status: %s | Hospital: %s",
                requestId, recipientId, bloodGroup, unitsRequested, status, hospital);
    }
}
