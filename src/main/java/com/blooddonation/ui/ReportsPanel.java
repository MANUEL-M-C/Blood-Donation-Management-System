package com.blooddonation.ui;

import com.blooddonation.service.ReportService;
import com.blooddonation.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;

/**
 * ============================================================================
 * OOP CONCEPT: REPORTING INTERFACE & EXPORT MANAGEMENT
 * ============================================================================
 * ReportsPanel provides dynamic report generation across 7 key domain areas
 * and allows direct export to CSV format for academic presentation.
 */
public class ReportsPanel extends JPanel {
    private final ReportService reportService = new ReportService();

    private JComboBox<String> cmbReports;
    private JTable reportTable;
    private DefaultTableModel currentModel;
    private JLabel lblStatus;

    public ReportsPanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(UITheme.BG_MAIN);
        setBorder(new EmptyBorder(15, 20, 20, 20));

        initComponents();
        loadSelectedReport();
    }

    private void initComponents() {
        // --- Header ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("📈 Analytics & System Reports");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.TEXT_DARK);

        JLabel lblSubtitle = new JLabel("Generate real-time administrative reports and export data to CSV.");
        lblSubtitle.setFont(UITheme.FONT_REGULAR);
        lblSubtitle.setForeground(UITheme.TEXT_MUTED);

        headerPanel.add(lblTitle, BorderLayout.NORTH);
        headerPanel.add(lblSubtitle, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        // --- Center Card ---
        JPanel card = UITheme.createCardPanel();
        card.setLayout(new BorderLayout(10, 10));

        // Top Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 5));
        toolbar.setOpaque(false);

        toolbar.add(new JLabel("Select Report:"));
        cmbReports = new JComboBox<>(ReportService.REPORT_TYPES);
        cmbReports.setFont(UITheme.FONT_REGULAR);
        cmbReports.setPreferredSize(new Dimension(240, 32));
        toolbar.add(cmbReports);

        JButton btnGenerate = UITheme.createPrimaryButton("GENERATE REPORT");
        JButton btnExport = UITheme.createSecondaryButton("📥 EXPORT TO CSV");

        btnGenerate.addActionListener(e -> loadSelectedReport());
        btnExport.addActionListener(e -> handleExportCSV());

        toolbar.add(btnGenerate);
        toolbar.add(btnExport);

        lblStatus = new JLabel("");
        lblStatus.setFont(UITheme.FONT_BOLD);
        lblStatus.setForeground(UITheme.PRIMARY);
        toolbar.add(lblStatus);

        card.add(toolbar, BorderLayout.NORTH);

        // Table
        currentModel = new DefaultTableModel();
        reportTable = new JTable(currentModel);
        UITheme.styleTable(reportTable);

        JScrollPane scrollPane = new JScrollPane(reportTable);
        scrollPane.setBorder(new LineBorder(UITheme.BORDER, 1));
        card.add(scrollPane, BorderLayout.CENTER);

        add(card, BorderLayout.CENTER);
    }

    public void loadSelectedReport() {
        String selected = (String) cmbReports.getSelectedItem();
        if (selected == null) return;

        try {
            currentModel = reportService.generateReport(selected);
            reportTable.setModel(currentModel);
            UITheme.styleTable(reportTable);
            lblStatus.setText("Showing: " + selected + " (" + currentModel.getRowCount() + " records)");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to generate report: " + ex.getMessage(), "Report Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleExportCSV() {
        if (currentModel == null || currentModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No records available to export.", "Empty Report", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Report as CSV");
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV files (*.csv)", "csv"));
        String defaultName = ((String) cmbReports.getSelectedItem()).replaceAll("[^a-zA-Z0-9]", "_") + ".csv";
        fileChooser.setSelectedFile(new File(defaultName));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            if (!fileToSave.getName().toLowerCase().endsWith(".csv")) {
                fileToSave = new File(fileToSave.getParentFile(), fileToSave.getName() + ".csv");
            }

            try {
                reportService.exportToCSV(currentModel, fileToSave);
                JOptionPane.showMessageDialog(this, "Report successfully exported to:\n" + fileToSave.getAbsolutePath(), "Export Successful", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "File Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
