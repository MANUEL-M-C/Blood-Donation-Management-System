package com.blooddonation.ui;

import com.blooddonation.model.Donation;
import com.blooddonation.service.DonationService;
import com.blooddonation.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * ============================================================================
 * OOP CONCEPT: PRESENTATION LAYER & AUDIT TRAIL
 * ============================================================================
 * DonationHistoryPanel provides a searchable log of all past blood donations.
 */
public class DonationHistoryPanel extends JPanel {
    private final DonationService donationService = new DonationService();

    private JTextField txtSearch;
    private JTable historyTable;
    private DefaultTableModel tableModel;

    public DonationHistoryPanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(UITheme.BG_MAIN);
        setBorder(new EmptyBorder(15, 20, 20, 20));

        initComponents();
        loadHistory();
    }

    private void initComponents() {
        // --- Header ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("📜 Blood Donation History");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.TEXT_DARK);

        JLabel lblSubtitle = new JLabel("Complete record of past donations, participating donors, dates, and units received.");
        lblSubtitle.setFont(UITheme.FONT_REGULAR);
        lblSubtitle.setForeground(UITheme.TEXT_MUTED);

        headerPanel.add(lblTitle, BorderLayout.NORTH);
        headerPanel.add(lblSubtitle, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        // --- Center Card ---
        JPanel card = UITheme.createCardPanel();
        card.setLayout(new BorderLayout(10, 10));

        // Filter / Search Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        toolbar.setOpaque(false);

        toolbar.add(new JLabel("Search (Donor ID, Name, Blood Group):"));
        txtSearch = new JTextField(18);
        UITheme.styleTextField(txtSearch);
        toolbar.add(txtSearch);

        JButton btnSearch = UITheme.createPrimaryButton("SEARCH");
        JButton btnReset = UITheme.createSecondaryButton("SHOW ALL");

        btnSearch.addActionListener(e -> handleSearch());
        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            loadHistory();
        });

        toolbar.add(btnSearch);
        toolbar.add(btnReset);

        card.add(toolbar, BorderLayout.NORTH);

        // Table
        String[] columns = {"Donation ID", "Donor ID", "Donor Name", "Donation Date", "Blood Group", "Units Donated"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        historyTable = new JTable(tableModel);
        UITheme.styleTable(historyTable);

        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.setBorder(new LineBorder(UITheme.BORDER, 1));
        card.add(scrollPane, BorderLayout.CENTER);

        add(card, BorderLayout.CENTER);
    }

    public void loadHistory() {
        try {
            List<Donation> list = donationService.getAllDonations();
            populateTable(list);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to load donation history: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleSearch() {
        String keyword = txtSearch.getText().trim();
        try {
            List<Donation> list = donationService.searchDonations(keyword);
            populateTable(list);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Search failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void populateTable(List<Donation> donations) {
        tableModel.setRowCount(0);
        for (Donation d : donations) {
            tableModel.addRow(new Object[]{
                    d.getDonationId(),
                    d.getDonorId(),
                    d.getDonorName() != null ? d.getDonorName() : "Donor #" + d.getDonorId(),
                    d.getDate(),
                    d.getBloodGroup(),
                    d.getUnits()
            });
        }
    }
}
