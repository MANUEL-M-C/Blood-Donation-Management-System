package com.blooddonation.model;

import java.util.*;

/**
 * ============================================================================
 * OOP CONCEPT: AGGREGATION & CONTROLLER MODELLING
 * ============================================================================
 * BloodBank coordinates the central domain model:
 * 1. AGGREGATION: Contains collections of BloodUnit, Donor, and BloodRequest.
 * 2. ENCAPSULATION & ABSTRACTION: Manages stock status rules and inventory validation
 *    independent of database and GUI details.
 */
public class BloodBank {
    public static final String[] BLOOD_GROUPS = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};

    public static final String STOCK_OUT_OF_STOCK = "OUT OF STOCK";
    public static final String STOCK_LOW = "LOW STOCK";
    public static final String STOCK_AVAILABLE = "AVAILABLE";

    // Aggregated domain attributes
    private Map<String, Integer> inventory; // BloodGroup -> Available Count
    private List<Donor> donors;
    private List<BloodRequest> requests;

    public BloodBank() {
        this.inventory = new LinkedHashMap<>();
        for (String bg : BLOOD_GROUPS) {
            this.inventory.put(bg, 0);
        }
        this.donors = new ArrayList<>();
        this.requests = new ArrayList<>();
    }

    public BloodBank(Map<String, Integer> inventory, List<Donor> donors, List<BloodRequest> requests) {
        this.inventory = (inventory != null) ? new LinkedHashMap<>(inventory) : new LinkedHashMap<>();
        for (String bg : BLOOD_GROUPS) {
            this.inventory.putIfAbsent(bg, 0);
        }
        this.donors = (donors != null) ? new ArrayList<>(donors) : new ArrayList<>();
        this.requests = (requests != null) ? new ArrayList<>(requests) : new ArrayList<>();
    }

    // Getters and Setters
    public Map<String, Integer> getInventory() {
        return Collections.unmodifiableMap(inventory);
    }

    public void setInventory(Map<String, Integer> inventory) {
        this.inventory = new LinkedHashMap<>(inventory);
        for (String bg : BLOOD_GROUPS) {
            this.inventory.putIfAbsent(bg, 0);
        }
    }

    public List<Donor> getDonors() {
        return donors;
    }

    public void setDonors(List<Donor> donors) {
        this.donors = (donors != null) ? new ArrayList<>(donors) : new ArrayList<>();
    }

    public List<BloodRequest> getRequests() {
        return requests;
    }

    public void setRequests(List<BloodRequest> requests) {
        this.requests = (requests != null) ? new ArrayList<>(requests) : new ArrayList<>();
    }

    /**
     * Checks available units for a specific blood group.
     */
    public int checkInventory(String bloodGroup) {
        if (bloodGroup == null) return 0;
        return inventory.getOrDefault(bloodGroup.trim().toUpperCase(), 0);
    }

    /**
     * Searches blood availability and returns formatted result.
     */
    public int searchBlood(String bloodGroup) {
        return checkInventory(bloodGroup);
    }

    /**
     * Updates inventory count for a blood group.
     */
    public void updateInventory(String bloodGroup, int delta) {
        if (bloodGroup == null) return;
        String bg = bloodGroup.trim().toUpperCase();
        int current = inventory.getOrDefault(bg, 0);
        int updated = Math.max(0, current + delta);
        inventory.put(bg, updated);
    }

    /**
     * Evaluates inventory status string based on unit count:
     * - 0 units -> OUT OF STOCK
     * - 1 to 3 units -> LOW STOCK
     * - 4+ units -> AVAILABLE
     */
    public static String evaluateStockStatus(int units) {
        if (units <= 0) {
            return STOCK_OUT_OF_STOCK;
        } else if (units <= 3) {
            return STOCK_LOW;
        } else {
            return STOCK_AVAILABLE;
        }
    }

    /**
     * Evaluates whether a blood request can be fulfilled from current inventory.
     */
    public boolean canFulfillRequest(String bloodGroup, int requestedUnits) {
        int available = checkInventory(bloodGroup);
        return available >= requestedUnits;
    }

    /**
     * Attempts to request/reserve blood from inventory.
     */
    public boolean requestBlood(String bloodGroup, int requestedUnits) {
        if (canFulfillRequest(bloodGroup, requestedUnits)) {
            updateInventory(bloodGroup, -requestedUnits);
            return true;
        }
        return false;
    }
}
