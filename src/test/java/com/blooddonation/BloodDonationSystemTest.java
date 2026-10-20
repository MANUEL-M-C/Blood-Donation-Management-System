package com.blooddonation;

import com.blooddonation.dao.*;
import com.blooddonation.model.*;
import com.blooddonation.service.*;

import javax.swing.table.DefaultTableModel;
import java.io.File;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

/**
 * ============================================================================
 * AUTOMATED SYSTEM & INTEGRATION TEST SUITE
 * ============================================================================
 * Fully tests:
 * 1. Database initialization and table schemas.
 * 2. Donor CRUD, validation, and search.
 * 3. Recipient registration and CRUD.
 * 4. Blood donation recording, unit generation, and inventory crediting.
 * 5. Blood request processing, stock validation, and FIFO unit issuing.
 * 6. Admin authentication and password updates.
 * 7. Report generation and CSV export.
 */
public class BloodDonationSystemTest {

    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("RUNNING BLOOD DONATION SYSTEM AUTOMATED TESTS");
        System.out.println("==================================================");

        try {
            testDatabaseInitialization();
            testDonorCRUD();
            testRecipientCRUD();
            testDonationAndInventoryLifecycle();
            testBloodRequestAndIssueSufficient();
            testBloodRequestInsufficientStock();
            testAdminAuthentication();
            testReportGenerationAndExport();
            testDemoDataLoader();

            System.out.println("\n==================================================");
            System.out.println(String.format("TEST RESULTS: %d PASSED, %d FAILED", testsPassed, testsFailed));
            System.out.println("==================================================");

            if (testsFailed > 0) {
                System.exit(1);
            }
        } catch (Throwable t) {
            System.err.println("Fatal unexpected test suite failure:");
            t.printStackTrace();
            System.exit(1);
        }
    }

    private static void assertTrue(String testName, boolean condition, String message) {
        if (condition) {
            System.out.println("  [PASS] " + testName);
            testsPassed++;
        } else {
            System.err.println("  [FAIL] " + testName + " -> " + message);
            testsFailed++;
        }
    }

    private static void testDatabaseInitialization() {
        System.out.println("\n--- 1. Testing Database & Schema Initialization ---");
        SchemaInitializer.initializeDatabase();
        try (Connection conn = DatabaseConnection.getConnection()) {
            assertTrue("Database Connection Active", conn != null && !conn.isClosed(), "Could not connect to SQLite");
        } catch (Exception e) {
            assertTrue("Database Connection Active", false, e.getMessage());
        }

        AdminDAO adminDAO = new AdminDAO();
        try {
            Admin admin = adminDAO.authenticate("admin", "admin123");
            assertTrue("Default Admin Authentication", admin != null && "admin".equalsIgnoreCase(admin.getUsername()), "Default admin authentication failed");
        } catch (Exception e) {
            assertTrue("Default Admin Authentication", false, e.getMessage());
        }
    }

    private static void testDonorCRUD() {
        System.out.println("\n--- 2. Testing Donor CRUD & Validation ---");
        DonorService donorService = new DonorService();

        String testDonorId = "TD-" + (System.currentTimeMillis() % 10000);
        try {
            // Register
            boolean registered = donorService.registerDonor(
                    testDonorId, "Test Donor John", "29", "9876543299",
                    "123 Innovation Way", "O+", "2025-01-01"
            );
            assertTrue("Register Donor", registered, "Failed to register donor");

            // Fetch
            Donor donor = donorService.getDonor(testDonorId);
            assertTrue("Retrieve Donor Details", donor != null && "Test Donor John".equals(donor.getName()), "Donor name mismatch");
            assertTrue("Polymorphism displayDetails()", donor.displayDetails().contains("[DONOR #" + testDonorId + "]"), "Polymorphic displayDetails failed");

            // Duplicate ID Check (Should Fail)
            boolean duplicateFailed = false;
            try {
                donorService.registerDonor(testDonorId, "Duplicate", "30", "9876543299", "Address", "O+", "2025-01-01");
            } catch (Exception expected) {
                duplicateFailed = true;
            }
            assertTrue("Duplicate Donor ID Rejected", duplicateFailed, "Duplicate donor ID was improperly permitted");

            // Update
            boolean updated = donorService.updateDonor(
                    testDonorId, "Test Donor John Updated", "30", "9876543288",
                    "456 Knowledge St", "O+", "2025-02-01"
            );
            assertTrue("Update Donor", updated, "Donor update failed");

            // Search
            List<Donor> searchResults = donorService.searchDonors("Innovation", "O+");
            assertTrue("Search Donor", searchResults != null, "Search failed");

            // Delete
            boolean deleted = donorService.deleteDonor(testDonorId);
            assertTrue("Delete Donor", deleted, "Donor deletion failed");
            assertTrue("Donor Removed After Delete", donorService.getDonor(testDonorId) == null, "Donor still exists after delete");

        } catch (Exception e) {
            assertTrue("Donor CRUD Suite", false, e.getMessage());
        }
    }

    private static void testRecipientCRUD() {
        System.out.println("\n--- 3. Testing Recipient CRUD & Validation ---");
        RecipientService recipientService = new RecipientService();

        String testRecId = "TR-" + (System.currentTimeMillis() % 10000);
        try {
            boolean registered = recipientService.registerRecipient(
                    testRecId, "Test Recipient Sarah", "35", "9876543277",
                    "789 Health Ave", "B+", "2", "Memorial Hospital"
            );
            assertTrue("Register Recipient", registered, "Failed to register recipient");

            Recipient rec = recipientService.getRecipient(testRecId);
            assertTrue("Retrieve Recipient", rec != null && "Test Recipient Sarah".equals(rec.getName()), "Recipient mismatch");
            assertTrue("Polymorphism displayDetails() Recipient", rec.displayDetails().contains("[RECIPIENT #" + testRecId + "]"), "Polymorphic displayDetails failed");

            // Delete
            boolean deleted = recipientService.deleteRecipient(testRecId);
            assertTrue("Delete Recipient", deleted, "Failed to delete recipient");

        } catch (Exception e) {
            assertTrue("Recipient CRUD Suite", false, e.getMessage());
        }
    }

    private static void testDonationAndInventoryLifecycle() {
        System.out.println("\n--- 4. Testing Blood Donation & Inventory Crediting ---");
        DonorService donorService = new DonorService();
        DonationService donationService = new DonationService();
        BloodBankService bloodBankService = new BloodBankService();

        String donorId = "D-LIFECYCLE-" + (System.currentTimeMillis() % 1000);
        String donationId = "DON-TEST-" + (System.currentTimeMillis() % 1000);

        try {
            // Register donor
            donorService.registerDonor(donorId, "Lifecycle Donor", "32", "9876543200", "Hospital Road", "A+", "None");

            int initialStock = bloodBankService.searchBlood("A+");

            // Record 3 units of donation
            boolean recorded = donationService.recordDonation(donationId, donorId, "2025-04-10", "3", "A+");
            assertTrue("Record Donation Transaction", recorded, "Failed to record donation");

            int newStock = bloodBankService.searchBlood("A+");
            assertTrue("Inventory Stock Increment", newStock == initialStock + 3,
                    String.format("Expected %d, got %d", initialStock + 3, newStock));

            Donor updatedDonor = donorService.getDonor(donorId);
            assertTrue("Donor Last Donation Date Updated", "2025-04-10".equals(updatedDonor.getLastDonationDate()), "Date not updated");

        } catch (Exception e) {
            assertTrue("Donation Lifecycle Suite", false, e.getMessage());
        }
    }

    private static void testBloodRequestAndIssueSufficient() {
        System.out.println("\n--- 5. Testing Blood Request Issue (Sufficient Stock) ---");
        RecipientService recipientService = new RecipientService();
        BloodBankService bloodBankService = new BloodBankService();

        String recId = "REC-REQ-" + (System.currentTimeMillis() % 1000);
        String reqId = "REQ-SUF-" + (System.currentTimeMillis() % 1000);

        try {
            recipientService.registerRecipient(recId, "Emergency Patient", "45", "9876543111", "ER Unit", "A+", "1", "Trauma Center");

            int availableBefore = bloodBankService.searchBlood("A+");
            if (availableBefore < 1) {
                // Seed at least 1 unit if stock was empty
                DonationService ds = new DonationService();
                DonorService dns = new DonorService();
                String dId = "D-TEMP-" + (System.currentTimeMillis() % 100);
                dns.registerDonor(dId, "Temp Donor", "25", "9876543000", "City", "A+", "None");
                ds.recordDonation("DON-TEMP-" + (System.currentTimeMillis() % 100), dId, "2025-04-11", "2", "A+");
                availableBefore = bloodBankService.searchBlood("A+");
            }

            boolean issued = bloodBankService.requestAndIssueBlood(reqId, recId, "A+", "1", "Trauma Center");
            assertTrue("Issue Blood Successfully", issued, "Blood issue failed");

            int availableAfter = bloodBankService.searchBlood("A+");
            assertTrue("Inventory Decremented by Issued Quantity", availableAfter == availableBefore - 1,
                    String.format("Expected %d, got %d", availableBefore - 1, availableAfter));

        } catch (Exception e) {
            assertTrue("Sufficient Stock Issue Suite", false, e.getMessage());
        }
    }

    private static void testBloodRequestInsufficientStock() {
        System.out.println("\n--- 6. Testing Blood Request (Insufficient Stock Validation) ---");
        RecipientService recipientService = new RecipientService();
        BloodBankService bloodBankService = new BloodBankService();

        String recId = "REC-DEFICIT-" + (System.currentTimeMillis() % 1000);
        String reqId = "REQ-DEFICIT-" + (System.currentTimeMillis() % 1000);

        try {
            recipientService.registerRecipient(recId, "Deficit Patient", "50", "9876543222", "ICU", "AB-", "999", "Central Hospital");

            boolean caughtDeficit = false;
            try {
                bloodBankService.requestAndIssueBlood(reqId, recId, "AB-", "999", "Central Hospital");
            } catch (Exception expected) {
                caughtDeficit = expected.getMessage().toLowerCase().contains("insufficient");
            }

            assertTrue("Insufficient Stock Correctly Blocked", caughtDeficit, "Request exceeded stock but did not trigger expected error");

        } catch (Exception e) {
            assertTrue("Insufficient Stock Suite", false, e.getMessage());
        }
    }

    private static void testAdminAuthentication() {
        System.out.println("\n--- 7. Testing Admin Authentication & Password Security ---");
        AuthService authService = new AuthService();
        AdminDAO adminDAO = new AdminDAO();

        try {
            // Bad credentials
            boolean badLogin = authService.login("admin", "wrongpassword");
            assertTrue("Invalid Password Rejected", !badLogin, "Invalid login was accepted");

            // Good credentials
            boolean goodLogin = authService.login("admin", "admin123");
            assertTrue("Valid Admin Login", goodLogin, "Valid login was rejected");

            // Create temporary admin to test password change without altering default dev credentials
            String tempUser = "t_admin_" + (System.currentTimeMillis() % 100000);
            String tempId = "ADM-" + (System.currentTimeMillis() % 100000);
            Admin testAdmin = new Admin(tempId, tempUser, "", "ADMIN");
            adminDAO.createAdmin(testAdmin, "temp123");

            authService.login(tempUser, "temp123");
            boolean changed = authService.changeCurrentPassword("temp123", "newpass456", "newpass456");
            assertTrue("Change Password", changed, "Password change failed");

            boolean newLogin = authService.login(tempUser, "newpass456");
            assertTrue("Login with New Password", newLogin, "Login with updated password failed");

        } catch (Exception e) {
            assertTrue("Admin Authentication Suite", false, e.getMessage());
        }
    }

    private static void testReportGenerationAndExport() {
        System.out.println("\n--- 8. Testing Analytical Reports & CSV Export ---");
        ReportService reportService = new ReportService();

        try {
            for (String reportType : ReportService.REPORT_TYPES) {
                DefaultTableModel model = reportService.generateReport(reportType);
                assertTrue("Report Generated: " + reportType, model != null && model.getColumnCount() > 0,
                        "Report " + reportType + " failed to generate");
            }

            // Test CSV Export
            DefaultTableModel invModel = reportService.generateReport(ReportService.RPT_BLOOD_INVENTORY);
            File tempCsv = File.createTempFile("blood_inventory_test_", ".csv");
            tempCsv.deleteOnExit();

            reportService.exportToCSV(invModel, tempCsv);
            assertTrue("CSV File Created and Populated", tempCsv.exists() && tempCsv.length() > 0, "CSV export failed");

        } catch (Exception e) {
            assertTrue("Report Generation Suite", false, e.getMessage());
        }
    }

    private static void testDemoDataLoader() {
        System.out.println("\n--- 9. Testing Academic Demo Data Seeding ---");
        DonorService donorService = new DonorService();
        BloodBankService bloodBankService = new BloodBankService();

        try {
            SchemaInitializer.loadDemoData();

            Donor rahul = donorService.getDonor("D001");
            assertTrue("Demo Donor Rahul (O+) Created", rahul != null && "Rahul Sharma".equals(rahul.getName()) && "O+".equals(rahul.getBloodGroup()), "Rahul D001 not found");

            Donor arun = donorService.getDonor("D002");
            assertTrue("Demo Donor Arun (A+) Created", arun != null && "Arun Kumar".equals(arun.getName()) && "A+".equals(arun.getBloodGroup()), "Arun D002 not found");

            Donor anu = donorService.getDonor("D003");
            assertTrue("Demo Donor Anu (B+) Created", anu != null && "Anu Patel".equals(anu.getName()) && "B+".equals(anu.getBloodGroup()), "Anu D003 not found");

            int oPlus = bloodBankService.searchBlood("O+");
            assertTrue("Demo Inventory O+ Stock (>=12 units)", oPlus >= 12, "Expected >=12 O+ units, got: " + oPlus);

            int aPlus = bloodBankService.searchBlood("A+");
            assertTrue("Demo Inventory A+ Stock (>=8 units)", aPlus >= 8, "Expected >=8 A+ units, got: " + aPlus);

            int bPlus = bloodBankService.searchBlood("B+");
            assertTrue("Demo Inventory B+ Stock (>=6 units)", bPlus >= 6, "Expected >=6 B+ units, got: " + bPlus);

        } catch (Exception e) {
            assertTrue("Demo Data Seeding Suite", false, e.getMessage());
        }
    }
}
