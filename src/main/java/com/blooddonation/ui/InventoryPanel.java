package com.blooddonation.ui;

import com.blooddonation.model.BloodBank;
import com.blooddonation.model.BloodUnit;
import com.blooddonation.service.BloodBankService;
import com.blooddonation.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * ============================================================================
 * OOP CONCEPT: PRESENTATION LAYER & INVENTORY AGGREGATION
 * ============================================================================
 * InventoryPanel renders real-time blood stock counts across all 8 blood groups
 * and displays calculated stock levels (OUT OF STOCK, LOW STOCK, AVAILABLE),
 * along with detailed individual blood unit tracking.
 */
public class InventoryPanel extends JPanel {
    private final BloodBankService bloodBankService = new BloodBankService();

    // Summary table
    private JTable summaryTable;
    private DefaultTableModel summaryModel;

    // Detailed units table
    private JTable unitsTable;
    private DefaultTableModel unitsModel;

    public InventoryPanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(UITheme.BG_MAIN);
        setBorder(new EmptyBorder(15, 20, 20, 20));

        initComponents();
        loadInventoryData();
    }

    private void initComponents() {
        // --- Header ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("📦 Blood Inventory & Stock Control");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.TEXT_DARK);

        JLabel lblSubtitle = new JLabel("Live monitoring of available units across all 8 standard blood groups.");
        lblSubtitle.setFont(UITheme.FONT_REGULAR);
        lblSubtitle.setForeground(UITheme.TEXT_MUTED);

        headerPanel.add(lblTitle, BorderLayout.NORTH);
        headerPanel.add(lblSubtitle, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        // --- Center Panel (Split vertically into Summary and Detailed units) ---
        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 0, 15));
        centerPanel.setOpaque(false);

        // Top: Group Summary Card
        centerPanel.add(createSummaryCard());

        // Bottom: Detailed Units Card
        centerPanel.add(createUnitsCard());

        add(centerPanel, BorderLayout.CENTER);
    }

    private JPanel createSummaryCard() {
        JPanel card = UITheme.createCardPanel();
        card.setLayout(new BorderLayout(10, 10));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JLabel title = new JLabel("Blood Group Stock Summary (Calculated Status)");
        title.setFont(UITheme.FONT_HEADER);
        title.setForeground(UITheme.PRIMARY);
        topBar.add(title, BorderLayout.WEST);

        JButton btnRefresh = UITheme.createSecondaryButton("🔄 Refresh Stock");
        btnRefresh.addActionListener(e -> loadInventoryData());
        topBar.add(btnRefresh, BorderLayout.EAST);

        card.add(topBar, BorderLayout.NORTH);

        // Summary Table
        String[] columns = {"Blood Group", "Available Units", "Stock Status"};
        summaryModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        summaryTable = new JTable(summaryModel);
        UITheme.styleTable(summaryTable);

        // Custom Cell Renderer for Stock Status coloring
        summaryTable.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setFont(UITheme.FONT_BOLD);
                String status = (value != null) ? value.toString() : "";
                if (BloodBank.STOCK_OUT_OF_STOCK.equalsIgnoreCase(status)) {
                    lbl.setForeground(UITheme.STATUS_DANGER);
                } else if (BloodBank.STOCK_LOW.equalsIgnoreCase(status)) {
                    lbl.setForeground(UITheme.STATUS_WARNING);
                } else {
                    lbl.setForeground(UITheme.STATUS_SUCCESS);
                }
                return lbl;
            }
        });

        JScrollPane scrollPane = new JScrollPane(summaryTable);
        scrollPane.setBorder(new LineBorder(UITheme.BORDER, 1));
        card.add(scrollPane, BorderLayout.CENTER);

        return card;
    }

    private JPanel createUnitsCard() {
        JPanel card = UITheme.createCardPanel();
        card.setLayout(new BorderLayout(10, 10));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JLabel title = new JLabel("Individual Blood Unit Records");
        title.setFont(UITheme.FONT_HEADER);
        title.setForeground(UITheme.TEXT_DARK);
        topBar.add(title, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);

        JButton btnMarkExpired = UITheme.createSecondaryButton("Mark Selected as EXPIRED");
        JButton btnMarkAvailable = UITheme.createSecondaryButton("Mark Selected as AVAILABLE");

        btnMarkExpired.addActionListener(e -> updateSelectedUnitStatus(BloodUnit.STATUS_EXPIRED));
        btnMarkAvailable.addActionListener(e -> updateSelectedUnitStatus(BloodUnit.STATUS_AVAILABLE));

        actions.add(btnMarkExpired);
        actions.add(btnMarkAvailable);
        topBar.add(actions, BorderLayout.EAST);

        card.add(topBar, BorderLayout.NORTH);

        // Units Table
        String[] columns = {"Unit ID", "Blood Group", "Collection Date", "Status", "Donation ID"};
        unitsModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        unitsTable = new JTable(unitsModel);
        UITheme.styleTable(unitsTable);

        JScrollPane scrollPane = new JScrollPane(unitsTable);
        scrollPane.setBorder(new LineBorder(UITheme.BORDER, 1));
        card.add(scrollPane, BorderLayout.CENTER);

        return card;
    }

    public void loadInventoryData() {
        try {
            // Load 8 blood groups summary
            Map<String, Integer> stock = bloodBankService.getInventoryStock();
            summaryModel.setRowCount(0);

            for (String bg : BloodBank.BLOOD_GROUPS) {
                int count = stock.getOrDefault(bg, 0);
                String status = BloodBank.evaluateStockStatus(count);
                summaryModel.addRow(new Object[]{bg, count, status});
            }

            // Load detailed units
            List<BloodUnit> units = bloodBankService.getAllUnits();
            unitsModel.setRowCount(0);
            for (BloodUnit u : units) {
                unitsModel.addRow(new Object[]{
                        u.getUnitId(),
                        u.getBloodGroup(),
                        u.getCollectionDate(),
                        u.getStatus(),
                        u.getDonationId() != null ? u.getDonationId() : "Direct / Initial"
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to load inventory: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateSelectedUnitStatus(String newStatus) {
        int row = unitsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a blood unit from the table.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String unitId = unitsModel.getValueAt(row, 0).toString();
        try {
            boolean ok = bloodBankService.updateUnitStatus(unitId, newStatus);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Unit " + unitId + " marked as " + newStatus, "Status Updated", JOptionPane.INFORMATION_MESSAGE);
                loadInventoryData();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to update unit: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
