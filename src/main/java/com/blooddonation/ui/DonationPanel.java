package com.blooddonation.ui;

import com.blooddonation.dao.DonorDAO;
import com.blooddonation.model.Donor;
import com.blooddonation.service.DonationService;
import com.blooddonation.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * ============================================================================
 * OOP CONCEPT: PRESENTATION LAYER & DONATION RECORDING
 * ============================================================================
 * DonationPanel allows administrators to register a blood donation event,
 * validating the donor against the database and crediting the inventory.
 */
public class DonationPanel extends JPanel {
    private final DonationService donationService = new DonationService();
    private final DonorDAO donorDAO = new DonorDAO();

    private JTextField txtDonationId;
    private JTextField txtDonorId;
    private JLabel lblDonorName;
    private JTextField txtDate;
    private JComboBox<String> cmbBloodGroup;
    private JTextField txtUnits;

    public DonationPanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(UITheme.BG_MAIN);
        setBorder(new EmptyBorder(15, 20, 20, 20));

        initComponents();
    }

    private void initComponents() {
        // --- Header ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("💉 Record Blood Donation");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.TEXT_DARK);

        JLabel lblSubtitle = new JLabel("Log blood collected from registered donors and automatically generate inventory units.");
        lblSubtitle.setFont(UITheme.FONT_REGULAR);
        lblSubtitle.setForeground(UITheme.TEXT_MUTED);

        headerPanel.add(lblTitle, BorderLayout.NORTH);
        headerPanel.add(lblSubtitle, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        // --- Center Container Card ---
        JPanel centerCard = UITheme.createCardPanel();
        centerCard.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 12, 8, 12);

        JLabel cardTitle = new JLabel("Donation Event Form");
        cardTitle.setFont(UITheme.FONT_HEADER);
        cardTitle.setForeground(UITheme.PRIMARY);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        centerCard.add(cardTitle, gbc);

        // Reset gridwidth
        gbc.gridwidth = 1;

        // 1. Donation ID
        gbc.gridx = 0;
        gbc.gridy = 1;
        centerCard.add(new JLabel("Donation ID *:"), gbc);

        txtDonationId = new JTextField("DON-" + System.currentTimeMillis() % 100000, 20);
        UITheme.styleTextField(txtDonationId);
        gbc.gridx = 1;
        centerCard.add(txtDonationId, gbc);

        // 2. Donor ID + Verify Button
        gbc.gridx = 0;
        gbc.gridy = 2;
        centerCard.add(new JLabel("Donor ID * (e.g. D001):"), gbc);

        JPanel donorLookupPanel = new JPanel(new BorderLayout(5, 0));
        donorLookupPanel.setOpaque(false);
        txtDonorId = new JTextField(12);
        UITheme.styleTextField(txtDonorId);
        JButton btnLookup = UITheme.createSecondaryButton("Verify Donor");
        btnLookup.addActionListener(e -> verifyDonor());
        donorLookupPanel.add(txtDonorId, BorderLayout.CENTER);
        donorLookupPanel.add(btnLookup, BorderLayout.EAST);

        gbc.gridx = 1;
        centerCard.add(donorLookupPanel, gbc);

        // Donor Name Preview
        gbc.gridx = 0;
        gbc.gridy = 3;
        centerCard.add(new JLabel("Verified Donor Name:"), gbc);

        lblDonorName = new JLabel("(Click 'Verify Donor' to check donor)");
        lblDonorName.setFont(UITheme.FONT_REGULAR);
        lblDonorName.setForeground(UITheme.TEXT_MUTED);
        gbc.gridx = 1;
        centerCard.add(lblDonorName, gbc);

        // 3. Donation Date
        gbc.gridx = 0;
        gbc.gridy = 4;
        centerCard.add(new JLabel("Donation Date (YYYY-MM-DD):"), gbc);

        txtDate = new JTextField(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE), 20);
        UITheme.styleTextField(txtDate);
        gbc.gridx = 1;
        centerCard.add(txtDate, gbc);

        // 4. Blood Group
        gbc.gridx = 0;
        gbc.gridy = 5;
        centerCard.add(new JLabel("Blood Group *:"), gbc);

        cmbBloodGroup = new JComboBox<>(new String[]{"Choose...", "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"});
        gbc.gridx = 1;
        centerCard.add(cmbBloodGroup, gbc);

        // 5. Units Donated
        gbc.gridx = 0;
        gbc.gridy = 6;
        centerCard.add(new JLabel("Number of Units *:"), gbc);

        txtUnits = new JTextField("1", 20);
        UITheme.styleTextField(txtUnits);
        gbc.gridx = 1;
        centerCard.add(txtUnits, gbc);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnPanel.setOpaque(false);

        JButton btnClear = UITheme.createSecondaryButton("CLEAR FORM");
        JButton btnRecord = UITheme.createPrimaryButton("SAVE & RECORD DONATION");

        btnClear.addActionListener(e -> resetForm());
        btnRecord.addActionListener(e -> handleRecordDonation());

        btnPanel.add(btnClear);
        btnPanel.add(btnRecord);

        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        centerCard.add(btnPanel, gbc);

        add(centerCard, BorderLayout.CENTER);
    }

    private void verifyDonor() {
        String donorId = txtDonorId.getText().trim();
        if (donorId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a Donor ID to verify.", "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Donor donor = donorDAO.getDonorById(donorId);
            if (donor != null) {
                lblDonorName.setText("✓ " + donor.getName() + " (Age: " + donor.getAge() + ", Phone: " + donor.getPhone() + ")");
                lblDonorName.setForeground(UITheme.STATUS_SUCCESS);
                cmbBloodGroup.setSelectedItem(donor.getBloodGroup());
            } else {
                lblDonorName.setText("✗ Donor ID not found! Please register the donor first.");
                lblDonorName.setForeground(UITheme.STATUS_DANGER);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error looking up donor: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleRecordDonation() {
        String donationId = txtDonationId.getText().trim();
        String donorId = txtDonorId.getText().trim();
        String date = txtDate.getText().trim();
        String bloodGroup = (String) cmbBloodGroup.getSelectedItem();
        String units = txtUnits.getText().trim();

        try {
            boolean success = donationService.recordDonation(donationId, donorId, date, units, bloodGroup);
            if (success) {
                JOptionPane.showMessageDialog(this,
                        String.format("Donation %s recorded successfully!\n%s unit(s) of %s added to Blood Inventory.",
                                donationId, units, bloodGroup),
                        "Donation Success", JOptionPane.INFORMATION_MESSAGE);
                resetForm();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Recording Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resetForm() {
        txtDonationId.setText("DON-" + System.currentTimeMillis() % 100000);
        txtDonorId.setText("");
        lblDonorName.setText("(Click 'Verify Donor' to check donor)");
        lblDonorName.setForeground(UITheme.TEXT_MUTED);
        txtDate.setText(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        cmbBloodGroup.setSelectedIndex(0);
        txtUnits.setText("1");
    }
}
