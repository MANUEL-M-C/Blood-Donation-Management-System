package com.blooddonation;

import com.blooddonation.dao.SchemaInitializer;
import com.blooddonation.ui.LoginFrame;
import com.blooddonation.util.UITheme;

import javax.swing.SwingUtilities;

/**
 * ============================================================================
 * MAIN APPLICATION ENTRY POINT
 * ============================================================================
 * Blood Donation Management System (Academic Desktop Project).
 * Initializes SQLite database tables, sets modern UI look & feel,
 * and launches the Login authentication window.
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("Starting Blood Donation Management System...");

        // 1. Initialize SQLite Database Schema & Default Admin
        SchemaInitializer.initializeDatabase();

        // 2. Set up Modern Java Swing Look & Feel (FlatLaf / System)
        UITheme.setupLookAndFeel();

        // 3. Launch UI on the Swing Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
