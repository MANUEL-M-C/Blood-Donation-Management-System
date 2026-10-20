package com.blooddonation.model;

/**
 * ============================================================================
 * OOP CONCEPT: ENCAPSULATION & ABSTRACTION
 * ============================================================================
 * Person serves as the abstract superclass representing any individual in the
 * Blood Donation Management System.
 *
 * Demonstrates:
 * 1. ENCAPSULATION: All attributes (name, age, phone, address) are declared private
 *    and accessed solely through validated public getter and setter methods.
 * 2. ABSTRACTION: Declares the contract displayDetails() which must be uniquely
 *    implemented by subclasses (Donor and Recipient).
 */
public abstract class Person {
    // Private attributes (Encapsulation)
    private String name;
    private int age;
    private String phone;
    private String address;

    /**
     * Parameterized Constructor
     */
    public Person(String name, int age, String phone, String address) {
        setName(name);
        setAge(age);
        setPhone(phone);
        setAddress(address);
    }

    // Default Constructor
    public Person() {
    }

    // Getters and Setters with validation (Encapsulation)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty.");
        }
        this.name = name.trim();
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        if (age < 0 || age > 120) {
            throw new IllegalArgumentException("Age must be between 0 and 120.");
        }
        this.age = age;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number cannot be empty.");
        }
        this.phone = phone.trim();
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            throw new IllegalArgumentException("Address cannot be empty.");
        }
        this.address = address.trim();
    }

    /**
     * Returns a formatted overview of the person's common attributes.
     */
    public String getDetails() {
        return String.format("Name: %s | Age: %d | Phone: %s | Address: %s",
                name, age, phone, address);
    }

    /**
     * Updates common person attributes.
     */
    public void updateDetails(String name, int age, String phone, String address) {
        setName(name);
        setAge(age);
        setPhone(phone);
        setAddress(address);
    }

    /**
     * ========================================================================
     * OOP CONCEPT: POLYMORPHISM
     * ========================================================================
     * Abstract method to be overridden by subclasses (Donor, Recipient).
     * Enables polymorphic invocation where a Person reference calls the
     * appropriate subclass display logic at runtime.
     */
    public abstract String displayDetails();

    @Override
    public String toString() {
        return getDetails();
    }
}
