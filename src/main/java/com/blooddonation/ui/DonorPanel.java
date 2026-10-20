package com.blooddonation.ui;

import com.blooddonation.model.Donor;
import com.blooddonation.service.DonorService;
import com.blooddonation.util.UITheme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * ============================================================================
 * OOP CONCEPT: PRESENTATION LAYER & EVENT HANDLING
 * ============================================================================
 * DonorPanel provides full CRUD (Create, Read, Update, Delete) and search
 * functionality for Donors.
 */
public class DonorPanel extends JPanel {
    private final DonorService donorService = new DonorService();

    // Form input controls
    private JTextField txtDonorId;
    private JTextField txtName;
    private JTextField txtAge;
    private JComboBox<String> cmbBloodGroup;
    private JTextField txtPhone;
    private JTextField txtAddress;
    private JTextField txtLastDonationDate;

    // Search controls
    private JTextField txtSearch;
    private JComboBox<String> cmbFilterBloodGroup;

    // Table
    private JTable donorTable;
    private DefaultTableModel tableModel;

    public DonorPanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(UITheme.BG_MAIN);
        setBorder(new EmptyBorder(15, 20, 20, 20));

        initComponents();
        loadDonorData();
    }

    private void initComponents() {
        // --- Header ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("🩸 Donor Management & Registration");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.TEXT_DARK);

        JLabel lblSubtitle = new JLabel("Register new donors, update contact details, and search donor records.");
        lblSubtitle.setFont(UITheme.FONT_REGULAR);
        lblSubtitle.setForeground(UITheme.TEXT_MUTED);

        headerPanel.add(lblTitle, BorderLayout.NORTH);
        headerPanel.add(lblSubtitle, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        // --- Center Split: Form (Left) & Table + Search (Right) ---
        JPanel centerSplit = new JPanel(new BorderLayout(15, 0));
        centerSplit.setOpaque(false);

        // Left: Form Card
        JPanel formCard = createFormCard();
        centerSplit.add(formCard, BorderLayout.WEST);

        // Right: Search Bar & Table Card
        JPanel tableCard = createTableCard();
        centerSplit.add(tableCard, BorderLayout.CENTER);

        add(centerSplit, BorderLayout.CENTER);
    }

    private JPanel createFormCard() {
        JPanel card = UITheme.createCardPanel();
        card.setLayout(new BorderLayout(10, 10));
        card.setPreferredSize(new Dimension(360, 0));

        JLabel formTitle = new JLabel("Donor Details Form");
        formTitle.setFont(UITheme.FONT_HEADER);
        formTitle.setForeground(UITheme.PRIMARY);
        card.add(formTitle, BorderLayout.NORTH);

        JPanel fieldsPanel = new JPanel(new GridLayout(7, 1, 6, 6));
        fieldsPanel.setOpaque(false);

        txtDonorId = new JTextField();
        UITheme.styleTextField(txtDonorId);
        fieldsPanel.add(createFieldWrapper("Donor ID * (e.g. D001):", txtDonorId));

        txtName = new JTextField();
        UITheme.styleTextField(txtName);
        fieldsPanel.add(createFieldWrapper("Full Name *:", txtName));

        txtAge = new JTextField();
        UITheme.styleTextField(txtAge);
        fieldsPanel.add(createFieldWrapper("Age * (18 - 65):", txtAge));

        cmbBloodGroup = new JComboBox<>(new String[]{"Choose...", "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"});
        fieldsPanel.add(createFieldWrapper("Blood Group *:", cmbBloodGroup));

        txtPhone = new JTextField();
        UITheme.styleTextField(txtPhone);
        fieldsPanel.add(createFieldWrapper("Phone Number *:", txtPhone));

        txtAddress = new JTextField();
        UITheme.styleTextField(txtAddress);
        fieldsPanel.add(createFieldWrapper("Address *:", txtAddress));

        txtLastDonationDate = new JTextField(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        UITheme.styleTextField(txtLastDonationDate);
        fieldsPanel.add(createFieldWrapper("Last Donation Date (YYYY-MM-DD):", txtLastDonationDate));

        card.add(fieldsPanel, BorderLayout.CENTER);

        // Buttons Panel
        JPanel btnPanel = new JPanel(new GridLayout(2, 2, 8, 8));
        btnPanel.setOpaque(false);
        btnPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        JButton btnRegister = UITheme.createPrimaryButton("REGISTER DONOR");
        JButton btnUpdate = UITheme.createSecondaryButton("UPDATE");
        JButton btnDelete = UITheme.createDangerButton("DELETE");
        JButton btnClear = UITheme.createSecondaryButton("CLEAR");

        btnRegister.addActionListener(e -> handleRegister());
        btnUpdate.addActionListener(e -> handleUpdate());
        btnDelete.addActionListener(e -> handleDelete());
        btnClear.addActionListener(e -> clearForm());

        btnPanel.add(btnRegister);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);
        btnPanel.add(btnClear);

        card.add(btnPanel, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createTableCard() {
        JPanel card = UITheme.createCardPanel();
        card.setLayout(new BorderLayout(10, 10));

        // Search Bar at Top of Table
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchBar.setOpaque(false);

        searchBar.add(new JLabel("Search:"));
        txtSearch = new JTextField(14);
        UITheme.styleTextField(txtSearch);
        searchBar.add(txtSearch);

        searchBar.add(new JLabel("Blood Group:"));
        cmbFilterBloodGroup = new JComboBox<>(new String[]{"ALL", "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"});
        searchBar.add(cmbFilterBloodGroup);

        JButton btnSearch = UITheme.createPrimaryButton("SEARCH");
        JButton btnReset = UITheme.createSecondaryButton("RESET");

        btnSearch.addActionListener(e -> handleSearch());
        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            cmbFilterBloodGroup.setSelectedIndex(0);
            loadDonorData();
        });

        searchBar.add(btnSearch);
        searchBar.add(btnReset);

        card.add(searchBar, BorderLayout.NORTH);

        // Table
        String[] columnNames = {"Donor ID", "Name", "Age", "Blood Group", "Phone", "Address", "Last Donation"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        donorTable = new JTable(tableModel);
        UITheme.styleTable(donorTable);

        // Row selection listener to populate form
        donorTable.getSelectionModel().addListSelectionListener(e -> {
            int selectedRow = donorTable.getSelectedRow();
            if (selectedRow >= 0) {
                txtDonorId.setText(tableModel.getValueAt(selectedRow, 0).toString());
                txtName.setText(tableModel.getValueAt(selectedRow, 1).toString());
                txtAge.setText(tableModel.getValueAt(selectedRow, 2).toString());
                cmbBloodGroup.setSelectedItem(tableModel.getValueAt(selectedRow, 3).toString());
                txtPhone.setText(tableModel.getValueAt(selectedRow, 4).toString());
                txtAddress.setText(tableModel.getValueAt(selectedRow, 5).toString());
                Object lastDate = tableModel.getValueAt(selectedRow, 6);
                txtLastDonationDate.setText(lastDate != null ? lastDate.toString() : "");
            }
        });

        JScrollPane scrollPane = new JScrollPane(donorTable);
        scrollPane.setBorder(new LineBorder(UITheme.BORDER, 1));
        card.add(scrollPane, BorderLayout.CENTER);

        return card;
    }

    private JPanel createFieldWrapper(String labelText, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(2, 2));
        p.setOpaque(false);
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(UITheme.FONT_SMALL);
        lbl.setForeground(UITheme.TEXT_MUTED);
        p.add(lbl, BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    // --- Action Handlers ---

    public void loadDonorData() {
        try {
            List<Donor> list = donorService.getAllDonors();
            populateTable(list);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading donors: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void populateTable(List<Donor> donors) {
        tableModel.setRowCount(0);
        for (Donor d : donors) {
            tableModel.addRow(new Object[]{
                    d.getDonorId(),
                    d.getName(),
                    d.getAge(),
                    d.getBloodGroup(),
                    d.getPhone(),
                    d.getAddress(),
                    d.getLastDonationDate()
            });
        }
    }

    private void handleRegister() {
        try {
            String donorId = txtDonorId.getText();
            String name = txtName.getText();
            String age = txtAge.getText();
            String bloodGroup = (String) cmbBloodGroup.getSelectedItem();
            String phone = txtPhone.getText();
            String address = txtAddress.getText();
            String lastDate = txtLastDonationDate.getText();

            donorService.registerDonor(donorId, name, age, phone, address, bloodGroup, lastDate);
            JOptionPane.showMessageDialog(this, "Donor " + donorId + " registered successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            loadDonorData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Registration Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void handleUpdate() {
        try {
            String donorId = txtDonorId.getText();
            String name = txtName.getText();
            String age = txtAge.getText();
            String bloodGroup = (String) cmbBloodGroup.getSelectedItem();
            String phone = txtPhone.getText();
            String address = txtAddress.getText();
            String lastDate = txtLastDonationDate.getText();

            boolean updated = donorService.updateDonor(donorId, name, age, phone, address, bloodGroup, lastDate);
            if (updated) {
                JOptionPane.showMessageDialog(this, "Donor " + donorId + " updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadDonorData();
            } else {
                JOptionPane.showMessageDialog(this, "Donor not found.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Update Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void handleDelete() {
        String donorId = txtDonorId.getText().trim();
        if (donorId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select or enter a Donor ID to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete donor " + donorId + "?\nThis action cannot be undone.",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean deleted = donorService.deleteDonor(donorId);
                if (deleted) {
                    JOptionPane.showMessageDialog(this, "Donor deleted successfully.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
                    clearForm();
                    loadDonorData();
                } else {
                    JOptionPane.showMessageDialog(this, "Donor not found.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Cannot delete donor: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleSearch() {
        String keyword = txtSearch.getText().trim();
        String bloodGroup = (String) cmbFilterBloodGroup.getSelectedItem();
        try {
            List<Donor> list = donorService.searchDonors(keyword, bloodGroup);
            populateTable(list);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Search error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearForm() {
        txtDonorId.setText("");
        txtName.setText("");
        txtAge.setText("");
        cmbBloodGroup.setSelectedIndex(0);
        txtPhone.setText("");
        txtAddress.setText("");
        txtLastDonationDate.setText(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        donorTable.clearSelection();
    }
}
