package com.blooddonation.util;

import java.util.regex.Pattern;

/**
 * ============================================================================
 * OOP CONCEPT: DEFENSIVE PROGRAMMING & INPUT VALIDATION
 * ============================================================================
 * Centralized utility class enforcing system validation rules.
 */
public class ValidationUtil {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{10,15}$");
    private static final Pattern DATE_PATTERN = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");

    private ValidationUtil() {}

    /**
     * Validates that a string is neither null nor empty.
     */
    public static void requireNonEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be empty.");
        }
    }

    /**
     * Validates donor age (Must be numeric and within blood donation limits: 18-65).
     */
    public static int parseDonorAge(String ageStr) {
        requireNonEmpty(ageStr, "Age");
        try {
            int age = Integer.parseInt(ageStr.trim());
            if (age < 18 || age > 65) {
                throw new IllegalArgumentException("Donor age must be between 18 and 65 years for medical eligibility.");
            }
            return age;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Age must be a valid numeric whole number.");
        }
    }

    /**
     * Validates recipient age (Must be numeric and within 0-120).
     */
    public static int parseRecipientAge(String ageStr) {
        requireNonEmpty(ageStr, "Age");
        try {
            int age = Integer.parseInt(ageStr.trim());
            if (age < 0 || age > 120) {
                throw new IllegalArgumentException("Age must be between 0 and 120.");
            }
            return age;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Age must be a valid numeric whole number.");
        }
    }

    /**
     * Validates numeric unit count (must be >= 1).
     */
    public static int parseUnits(String unitsStr, String fieldName) {
        requireNonEmpty(unitsStr, fieldName);
        try {
            int units = Integer.parseInt(unitsStr.trim());
            if (units <= 0) {
                throw new IllegalArgumentException(fieldName + " must be at least 1 unit.");
            }
            return units;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(fieldName + " must be a valid numeric whole number.");
        }
    }

    /**
     * Validates phone number format.
     */
    public static void validatePhone(String phone) {
        requireNonEmpty(phone, "Phone number");
        String cleaned = phone.trim().replaceAll("[\\s\\-\\(\\)]", "");
        if (!PHONE_PATTERN.matcher(cleaned).matches()) {
            throw new IllegalArgumentException("Phone number must contain between 10 and 15 digits.");
        }
    }

    /**
     * Validates blood group selection.
     */
    public static void validateBloodGroup(String bloodGroup) {
        requireNonEmpty(bloodGroup, "Blood Group");
        if (bloodGroup.equalsIgnoreCase("Select Blood Group") || bloodGroup.equalsIgnoreCase("Choose...")) {
            throw new IllegalArgumentException("Please select a valid blood group.");
        }
    }

    /**
     * Validates date format (YYYY-MM-DD).
     */
    public static boolean isValidDateFormat(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty() || dateStr.equalsIgnoreCase("None") || dateStr.equalsIgnoreCase("Never")) {
            return true;
        }
        return DATE_PATTERN.matcher(dateStr.trim()).matches();
    }
}
