package com.blooddonation.ui;

import com.blooddonation.model.BloodBank;
import com.blooddonation.model.BloodRequest;
import com.blooddonation.model.Recipient;
import com.blooddonation.service.BloodBankService;
import com.blooddonation.service.RecipientService;
import com.blooddonation.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * ============================================================================
 * OOP CONCEPT: CONTROLLER / WORKFLOW INTERFACE
 * ============================================================================
 * BloodSearchPanel provides searching blood stock by blood group, real-time
 * stock verification, and processing blood unit issue requests.
 */
public class BloodSearchPanel extends JPanel {
    private final BloodBankService bloodBankService = new BloodBankService();
    private final RecipientService recipientService = new RecipientService();

    // Search & Request controls
    private JComboBox<String> cmbBloodGroup;
    private JTextField txtUnitsRequired;
    private JTextField txtRecipientId;
    private JTextField txtHospital;
    private JTextField txtRequestId;

    // Real-time stock display labels
    private JPanel stockCardsContainer;

    // Requests table
    private JTable requestsTable;
    private DefaultTableModel requestsModel;

    public BloodSearchPanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(UITheme.BG_MAIN);
        setBorder(new EmptyBorder(15, 20, 20, 20));

        initComponents();
        refreshAll();
    }

    private void initComponents() {
        // --- Header ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("🔍 Search & Issue Blood");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.TEXT_DARK);

        JLabel lblSubtitle = new JLabel("Search available blood units and issue blood safely to verified recipients.");
        lblSubtitle.setFont(UITheme.FONT_REGULAR);
        lblSubtitle.setForeground(UITheme.TEXT_MUTED);

        headerPanel.add(lblTitle, BorderLayout.NORTH);
        headerPanel.add(lblSubtitle, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        // --- Center Split ---
        JPanel centerSplit = new JPanel(new BorderLayout(15, 0));
        centerSplit.setOpaque(false);

        // Left: Search & Request Form
        centerSplit.add(createRequestCard(), BorderLayout.WEST);

        // Right: Live Stock Overview & Recent Requests
        centerSplit.add(createStockAndHistoryCard(), BorderLayout.CENTER);

        add(centerSplit, BorderLayout.CENTER);
    }

    private JPanel createRequestCard() {
        JPanel card = UITheme.createCardPanel();
        card.setLayout(new BorderLayout(10, 10));
        card.setPreferredSize(new Dimension(380, 0));

        JLabel formTitle = new JLabel("Search & Request Blood Form");
        formTitle.setFont(UITheme.FONT_HEADER);
        formTitle.setForeground(UITheme.PRIMARY);
        card.add(formTitle, BorderLayout.NORTH);

        JPanel fieldsPanel = new JPanel(new GridLayout(5, 1, 8, 8));
        fieldsPanel.setOpaque(false);

        txtRequestId = new JTextField("REQ-" + System.currentTimeMillis() % 100000);
        UITheme.styleTextField(txtRequestId);
        fieldsPanel.add(createFieldWrapper("Request ID *:", txtRequestId));

        txtRecipientId = new JTextField();
        UITheme.styleTextField(txtRecipientId);
        fieldsPanel.add(createFieldWrapper("Recipient ID * (Must exist):", txtRecipientId));

        cmbBloodGroup = new JComboBox<>(new String[]{"Choose...", "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"});
        fieldsPanel.add(createFieldWrapper("Blood Group Needed *:", cmbBloodGroup));

        txtUnitsRequired = new JTextField("1");
        UITheme.styleTextField(txtUnitsRequired);
        fieldsPanel.add(createFieldWrapper("Units Required *:", txtUnitsRequired));

        txtHospital = new JTextField("City General Hospital");
        UITheme.styleTextField(txtHospital);
        fieldsPanel.add(createFieldWrapper("Hospital / Clinic Name *:", txtHospital));

        card.add(fieldsPanel, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new GridLayout(2, 1, 8, 8));
        btnPanel.setOpaque(false);
        btnPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        JButton btnSearch = UITheme.createSecondaryButton("🔍 SEARCH STOCK");
        JButton btnIssue = UITheme.createPrimaryButton("💉 REQUEST & ISSUE BLOOD");

        btnSearch.addActionListener(e -> handleSearchBlood());
        btnIssue.addActionListener(e -> handleIssueBlood());

        btnPanel.add(btnSearch);
        btnPanel.add(btnIssue);

        card.add(btnPanel, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createStockAndHistoryCard() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setOpaque(false);

        // Top: Live Stock Cards Panel (Shows A+: 8, B+: 6, O+: 12, etc.)
        JPanel stockCard = UITheme.createCardPanel();
        stockCard.setLayout(new BorderLayout(8, 8));

        JLabel stockHeader = new JLabel("Live Available Units Stock Overview");
        stockHeader.setFont(UITheme.FONT_HEADER);
        stockHeader.setForeground(UITheme.TEXT_DARK);
        stockCard.add(stockHeader, BorderLayout.NORTH);

        stockCardsContainer = new JPanel(new GridLayout(2, 4, 10, 10));
        stockCardsContainer.setOpaque(false);
        stockCard.add(stockCardsContainer, BorderLayout.CENTER);

        panel.add(stockCard, BorderLayout.NORTH);

        // Bottom: Recent Requests / Issues Table
        JPanel historyCard = UITheme.createCardPanel();
        historyCard.setLayout(new BorderLayout(8, 8));

        JLabel reqHeader = new JLabel("Recent Blood Requests & Issued Status");
        reqHeader.setFont(UITheme.FONT_HEADER);
        reqHeader.setForeground(UITheme.PRIMARY);
        historyCard.add(reqHeader, BorderLayout.NORTH);

        String[] columns = {"Request ID", "Recipient ID", "Patient Name", "Group", "Units", "Date", "Status", "Hospital"};
        requestsModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        requestsTable = new JTable(requestsModel);
        UITheme.styleTable(requestsTable);

        JScrollPane scrollPane = new JScrollPane(requestsTable);
        scrollPane.setBorder(new LineBorder(UITheme.BORDER, 1));
        historyCard.add(scrollPane, BorderLayout.CENTER);

        panel.add(historyCard, BorderLayout.CENTER);
        return panel;
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

    public void refreshAll() {
        renderStockCards();
        loadRequestsData();
    }

    private void renderStockCards() {
        stockCardsContainer.removeAll();
        try {
            Map<String, Integer> stock = bloodBankService.getInventoryStock();
            for (String bg : BloodBank.BLOOD_GROUPS) {
                int count = stock.getOrDefault(bg, 0);

                JPanel badge = new JPanel(new BorderLayout());
                badge.setBackground(count > 0 ? Color.WHITE : new Color(254, 242, 242));
                badge.setBorder(new LineBorder(count > 0 ? UITheme.BORDER : UITheme.STATUS_DANGER, 1));

                JLabel lblBg = new JLabel(bg, SwingConstants.CENTER);
                lblBg.setFont(new Font("Segoe UI", Font.BOLD, 18));
                lblBg.setForeground(UITheme.PRIMARY);

                JLabel lblCount = new JLabel(count + " units", SwingConstants.CENTER);
                lblCount.setFont(UITheme.FONT_BOLD);
                lblCount.setForeground(count <= 3 ? UITheme.STATUS_WARNING : UITheme.STATUS_SUCCESS);

                badge.add(lblBg, BorderLayout.CENTER);
                badge.add(lblCount, BorderLayout.SOUTH);

                stockCardsContainer.add(badge);
            }
        } catch (Exception ex) {
            System.err.println("Error fetching stock: " + ex.getMessage());
        }
        stockCardsContainer.revalidate();
        stockCardsContainer.repaint();
    }

    private void loadRequestsData() {
        try {
            List<BloodRequest> requests = bloodBankService.getAllRequests();
            requestsModel.setRowCount(0);
            for (BloodRequest br : requests) {
                requestsModel.addRow(new Object[]{
                        br.getRequestId(),
                        br.getRecipientId(),
                        br.getRecipientName() != null ? br.getRecipientName() : br.getRecipientId(),
                        br.getBloodGroup(),
                        br.getUnitsRequested(),
                        br.getRequestDate(),
                        br.getStatus(),
                        br.getHospital()
                });
            }
        } catch (Exception ex) {
            System.err.println("Error loading requests: " + ex.getMessage());
        }
    }

    private void handleSearchBlood() {
        String bloodGroup = (String) cmbBloodGroup.getSelectedItem();
        if (bloodGroup == null || bloodGroup.equalsIgnoreCase("Choose...")) {
            JOptionPane.showMessageDialog(this, "Please select a blood group to search.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int available = bloodBankService.searchBlood(bloodGroup);
            String status = BloodBank.evaluateStockStatus(available);
            String message = String.format("Blood Group: %s\nAvailable Units: %d\nStatus: %s", bloodGroup, available, status);
            JOptionPane.showMessageDialog(this, message, "Stock Availability Result", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Search error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleIssueBlood() {
        String reqId = txtRequestId.getText().trim();
        String recId = txtRecipientId.getText().trim();
        String bloodGroup = (String) cmbBloodGroup.getSelectedItem();
        String unitsStr = txtUnitsRequired.getText().trim();
        String hospital = txtHospital.getText().trim();

        try {
            boolean issued = bloodBankService.requestAndIssueBlood(reqId, recId, bloodGroup, unitsStr, hospital);
            if (issued) {
                JOptionPane.showMessageDialog(this,
                        String.format("Blood request %s processed successfully!\n%s unit(s) of %s marked as ISSUED.",
                                reqId, unitsStr, bloodGroup),
                        "Blood Issued Successfully", JOptionPane.INFORMATION_MESSAGE);

                txtRequestId.setText("REQ-" + System.currentTimeMillis() % 100000);
                txtUnitsRequired.setText("1");
                refreshAll();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Request / Issue Failed", JOptionPane.ERROR_MESSAGE);
        }
    }
}
