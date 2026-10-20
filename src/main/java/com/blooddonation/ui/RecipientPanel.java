package com.blooddonation.ui;

import com.blooddonation.model.Recipient;
import com.blooddonation.service.RecipientService;
import com.blooddonation.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * ============================================================================
 * OOP CONCEPT: PRESENTATION LAYER & RECIPIENT MANAGEMENT
 * ============================================================================
 * RecipientPanel provides registration, updating, deleting, and searching of
 * blood recipients and patient requirements.
 */
public class RecipientPanel extends JPanel {
    private final RecipientService recipientService = new RecipientService();

    // Form inputs
    private JTextField txtRecipientId;
    private JTextField txtName;
    private JTextField txtAge;
    private JComboBox<String> cmbBloodGroup;
    private JTextField txtUnitsRequired;
    private JTextField txtHospital;
    private JTextField txtPhone;
    private JTextField txtAddress;

    // Search controls
    private JTextField txtSearch;
    private JComboBox<String> cmbFilterBloodGroup;

    // Table
    private JTable recipientTable;
    private DefaultTableModel tableModel;

    public RecipientPanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(UITheme.BG_MAIN);
        setBorder(new EmptyBorder(15, 20, 20, 20));

        initComponents();
        loadRecipientData();
    }

    private void initComponents() {
        // --- Header ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("📋 Recipient & Blood Needs Management");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.TEXT_DARK);

        JLabel lblSubtitle = new JLabel("Register blood recipients, track required units, and assign destination hospitals.");
        lblSubtitle.setFont(UITheme.FONT_REGULAR);
        lblSubtitle.setForeground(UITheme.TEXT_MUTED);

        headerPanel.add(lblTitle, BorderLayout.NORTH);
        headerPanel.add(lblSubtitle, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        // --- Center Split: Form (Left) & Table + Search (Right) ---
        JPanel centerSplit = new JPanel(new BorderLayout(15, 0));
        centerSplit.setOpaque(false);

        centerSplit.add(createFormCard(), BorderLayout.WEST);
        centerSplit.add(createTableCard(), BorderLayout.CENTER);

        add(centerSplit, BorderLayout.CENTER);
    }

    private JPanel createFormCard() {
        JPanel card = UITheme.createCardPanel();
        card.setLayout(new BorderLayout(10, 10));
        card.setPreferredSize(new Dimension(360, 0));

        JLabel formTitle = new JLabel("Recipient Registration Form");
        formTitle.setFont(UITheme.FONT_HEADER);
        formTitle.setForeground(UITheme.PRIMARY);
        card.add(formTitle, BorderLayout.NORTH);

        JPanel fieldsPanel = new JPanel(new GridLayout(8, 1, 5, 5));
        fieldsPanel.setOpaque(false);

        txtRecipientId = new JTextField();
        UITheme.styleTextField(txtRecipientId);
        fieldsPanel.add(createFieldWrapper("Recipient ID * (e.g. R001):", txtRecipientId));

        txtName = new JTextField();
        UITheme.styleTextField(txtName);
        fieldsPanel.add(createFieldWrapper("Recipient / Patient Name *:", txtName));

        txtAge = new JTextField();
        UITheme.styleTextField(txtAge);
        fieldsPanel.add(createFieldWrapper("Age *:", txtAge));

        cmbBloodGroup = new JComboBox<>(new String[]{"Choose...", "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"});
        fieldsPanel.add(createFieldWrapper("Blood Group Required *:", cmbBloodGroup));

        txtUnitsRequired = new JTextField("1");
        UITheme.styleTextField(txtUnitsRequired);
        fieldsPanel.add(createFieldWrapper("Units Required *:", txtUnitsRequired));

        txtHospital = new JTextField("City General Hospital");
        UITheme.styleTextField(txtHospital);
        fieldsPanel.add(createFieldWrapper("Hospital / Destination *:", txtHospital));

        txtPhone = new JTextField();
        UITheme.styleTextField(txtPhone);
        fieldsPanel.add(createFieldWrapper("Contact Phone *:", txtPhone));

        txtAddress = new JTextField();
        UITheme.styleTextField(txtAddress);
        fieldsPanel.add(createFieldWrapper("Address *:", txtAddress));

        card.add(fieldsPanel, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new GridLayout(2, 2, 8, 8));
        btnPanel.setOpaque(false);
        btnPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        JButton btnRegister = UITheme.createPrimaryButton("REQUEST BLOOD");
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

        // Search Bar
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
            loadRecipientData();
        });

        searchBar.add(btnSearch);
        searchBar.add(btnReset);

        card.add(searchBar, BorderLayout.NORTH);

        // Table
        String[] columnNames = {"Recipient ID", "Name", "Age", "Blood Group", "Units Needed", "Hospital", "Phone", "Address"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        recipientTable = new JTable(tableModel);
        UITheme.styleTable(recipientTable);

        // Selection Listener
        recipientTable.getSelectionModel().addListSelectionListener(e -> {
            int selectedRow = recipientTable.getSelectedRow();
            if (selectedRow >= 0) {
                txtRecipientId.setText(tableModel.getValueAt(selectedRow, 0).toString());
                txtName.setText(tableModel.getValueAt(selectedRow, 1).toString());
                txtAge.setText(tableModel.getValueAt(selectedRow, 2).toString());
                cmbBloodGroup.setSelectedItem(tableModel.getValueAt(selectedRow, 3).toString());
                txtUnitsRequired.setText(tableModel.getValueAt(selectedRow, 4).toString());
                txtHospital.setText(tableModel.getValueAt(selectedRow, 5).toString());
                txtPhone.setText(tableModel.getValueAt(selectedRow, 6).toString());
                txtAddress.setText(tableModel.getValueAt(selectedRow, 7).toString());
            }
        });

        JScrollPane scrollPane = new JScrollPane(recipientTable);
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

    public void loadRecipientData() {
        try {
            List<Recipient> list = recipientService.getAllRecipients();
            populateTable(list);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading recipients: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void populateTable(List<Recipient> recipients) {
        tableModel.setRowCount(0);
        for (Recipient r : recipients) {
            tableModel.addRow(new Object[]{
                    r.getRecipientId(),
                    r.getName(),
                    r.getAge(),
                    r.getBloodGroup(),
                    r.getUnitsRequired(),
                    r.getHospital(),
                    r.getPhone(),
                    r.getAddress()
            });
        }
    }

    private void handleRegister() {
        try {
            recipientService.registerRecipient(
                    txtRecipientId.getText(),
                    txtName.getText(),
                    txtAge.getText(),
                    txtPhone.getText(),
                    txtAddress.getText(),
                    (String) cmbBloodGroup.getSelectedItem(),
                    txtUnitsRequired.getText(),
                    txtHospital.getText()
            );
            JOptionPane.showMessageDialog(this, "Recipient " + txtRecipientId.getText().trim() + " registered successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            loadRecipientData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Registration Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void handleUpdate() {
        try {
            boolean updated = recipientService.updateRecipient(
                    txtRecipientId.getText(),
                    txtName.getText(),
                    txtAge.getText(),
                    txtPhone.getText(),
                    txtAddress.getText(),
                    (String) cmbBloodGroup.getSelectedItem(),
                    txtUnitsRequired.getText(),
                    txtHospital.getText()
            );
            if (updated) {
                JOptionPane.showMessageDialog(this, "Recipient updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadRecipientData();
            } else {
                JOptionPane.showMessageDialog(this, "Recipient not found.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Update Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void handleDelete() {
        String recipientId = txtRecipientId.getText().trim();
        if (recipientId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select or enter a Recipient ID to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete recipient " + recipientId + "?",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean deleted = recipientService.deleteRecipient(recipientId);
                if (deleted) {
                    JOptionPane.showMessageDialog(this, "Recipient deleted successfully.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
                    clearForm();
                    loadRecipientData();
                } else {
                    JOptionPane.showMessageDialog(this, "Recipient not found.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Cannot delete recipient: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleSearch() {
        String keyword = txtSearch.getText().trim();
        String bloodGroup = (String) cmbFilterBloodGroup.getSelectedItem();
        try {
            List<Recipient> list = recipientService.searchRecipients(keyword, bloodGroup);
            populateTable(list);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Search error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearForm() {
        txtRecipientId.setText("");
        txtName.setText("");
        txtAge.setText("");
        cmbBloodGroup.setSelectedIndex(0);
        txtUnitsRequired.setText("1");
        txtHospital.setText("City General Hospital");
        txtPhone.setText("");
        txtAddress.setText("");
        recipientTable.clearSelection();
    }
}
