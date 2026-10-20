package com.blooddonation.service;

import com.blooddonation.dao.BloodUnitDAO;
import com.blooddonation.dao.DatabaseConnection;
import com.blooddonation.model.BloodBank;

import javax.swing.table.DefaultTableModel;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.Map;
import java.util.Vector;

/**
 * ============================================================================
 * OOP CONCEPT: REPORTING & UTILITY ABSTRACTION
 * ============================================================================
 * ReportService executes analytical SQL queries for academic reporting
 * and exports tabular data to standard CSV format.
 */
public class ReportService {

    public static final String RPT_TOTAL_DONORS = "1. Total Donors";
    public static final String RPT_TOTAL_RECIPIENTS = "2. Total Recipients";
    public static final String RPT_TOTAL_DONATIONS = "3. Total Donations";
    public static final String RPT_BLOOD_INVENTORY = "4. Blood Inventory Summary";
    public static final String RPT_ISSUED_BLOOD = "5. Issued Blood History";
    public static final String RPT_PENDING_REQUESTS = "6. Pending Requests";
    public static final String RPT_LOW_STOCK = "7. Low Stock Blood Groups";

    public static final String[] REPORT_TYPES = {
            RPT_TOTAL_DONORS,
            RPT_TOTAL_RECIPIENTS,
            RPT_TOTAL_DONATIONS,
            RPT_BLOOD_INVENTORY,
            RPT_ISSUED_BLOOD,
            RPT_PENDING_REQUESTS,
            RPT_LOW_STOCK
    };

    /**
     * Generates a DefaultTableModel corresponding to the selected report.
     */
    public DefaultTableModel generateReport(String reportType) throws Exception {
        if (RPT_BLOOD_INVENTORY.equals(reportType)) {
            return generateInventoryReport();
        } else if (RPT_LOW_STOCK.equals(reportType)) {
            return generateLowStockReport();
        }

        String query;
        if (RPT_TOTAL_DONORS.equals(reportType)) {
            query = """
                SELECT d.donor_id AS 'Donor ID', p.name AS 'Donor Name', p.age AS 'Age',
                       d.blood_group AS 'Blood Group', p.phone AS 'Phone Number',
                       d.last_donation_date AS 'Last Donated', p.address AS 'Address'
                FROM donors d
                JOIN persons p ON d.person_id = p.person_id
                ORDER BY d.donor_id ASC
            """;
        } else if (RPT_TOTAL_RECIPIENTS.equals(reportType)) {
            query = """
                SELECT r.recipient_id AS 'Recipient ID', p.name AS 'Recipient Name', p.age AS 'Age',
                       r.blood_group AS 'Blood Group', r.units_required AS 'Units Needed',
                       r.hospital AS 'Hospital', p.phone AS 'Phone Number'
                FROM recipients r
                JOIN persons p ON r.person_id = p.person_id
                ORDER BY r.recipient_id ASC
            """;
        } else if (RPT_TOTAL_DONATIONS.equals(reportType)) {
            query = """
                SELECT d.donation_id AS 'Donation ID', d.donor_id AS 'Donor ID',
                       p.name AS 'Donor Name', d.donation_date AS 'Donation Date',
                       d.blood_group AS 'Blood Group', d.units AS 'Units Donated'
                FROM donations d
                JOIN donors dn ON d.donor_id = dn.donor_id
                JOIN persons p ON dn.person_id = p.person_id
                ORDER BY d.donation_date DESC
            """;
        } else if (RPT_ISSUED_BLOOD.equals(reportType)) {
            query = """
                SELECT u.unit_id AS 'Unit ID', u.blood_group AS 'Blood Group',
                       u.collection_date AS 'Collection Date', u.status AS 'Status',
                       u.donation_id AS 'Donation ID'
                FROM blood_units u
                WHERE u.status = 'ISSUED'
                ORDER BY u.unit_id DESC
            """;
        } else if (RPT_PENDING_REQUESTS.equals(reportType)) {
            query = """
                SELECT br.request_id AS 'Request ID', br.recipient_id AS 'Recipient ID',
                       p.name AS 'Recipient Name', br.blood_group AS 'Blood Group',
                       br.units_requested AS 'Units Requested', br.request_date AS 'Request Date',
                       br.hospital AS 'Hospital', br.status AS 'Status'
                FROM blood_requests br
                LEFT JOIN recipients r ON br.recipient_id = r.recipient_id
                LEFT JOIN persons p ON r.person_id = p.person_id
                WHERE br.status = 'PENDING'
                ORDER BY br.request_date DESC
            """;
        } else {
            throw new IllegalArgumentException("Unknown report type: " + reportType);
        }

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            return buildTableModel(rs);
        }
    }

    private DefaultTableModel generateInventoryReport() throws Exception {
        BloodUnitDAO dao = new BloodUnitDAO();
        Map<String, Integer> stock = dao.getAvailableUnitsGrouped();

        Vector<String> columnNames = new Vector<>();
        columnNames.add("Blood Group");
        columnNames.add("Available Units");
        columnNames.add("Status");

        Vector<Vector<Object>> data = new Vector<>();
        for (String bg : BloodBank.BLOOD_GROUPS) {
            int qty = stock.getOrDefault(bg, 0);
            Vector<Object> row = new Vector<>();
            row.add(bg);
            row.add(qty);
            row.add(BloodBank.evaluateStockStatus(qty));
            data.add(row);
        }

        return new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private DefaultTableModel generateLowStockReport() throws Exception {
        BloodUnitDAO dao = new BloodUnitDAO();
        Map<String, Integer> stock = dao.getAvailableUnitsGrouped();

        Vector<String> columnNames = new Vector<>();
        columnNames.add("Blood Group");
        columnNames.add("Available Units");
        columnNames.add("Status");
        columnNames.add("Urgency Level");

        Vector<Vector<Object>> data = new Vector<>();
        for (String bg : BloodBank.BLOOD_GROUPS) {
            int qty = stock.getOrDefault(bg, 0);
            if (qty <= 3) {
                Vector<Object> row = new Vector<>();
                row.add(bg);
                row.add(qty);
                row.add(BloodBank.evaluateStockStatus(qty));
                row.add(qty == 0 ? "CRITICAL (Immediate Donation Required)" : "WARNING (Stock Running Low)");
                data.add(row);
            }
        }

        return new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private DefaultTableModel buildTableModel(ResultSet rs) throws Exception {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        Vector<String> columnNames = new Vector<>();
        for (int i = 1; i <= columnCount; i++) {
            columnNames.add(metaData.getColumnLabel(i));
        }

        Vector<Vector<Object>> data = new Vector<>();
        while (rs.next()) {
            Vector<Object> row = new Vector<>();
            for (int i = 1; i <= columnCount; i++) {
                row.add(rs.getObject(i));
            }
            data.add(row);
        }

        return new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    /**
     * Exports a table model to a standard CSV file.
     */
    public void exportToCSV(DefaultTableModel model, File targetFile) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(targetFile))) {
            // Write column headers
            for (int col = 0; col < model.getColumnCount(); col++) {
                writer.print(escapeCSV(model.getColumnName(col)));
                if (col < model.getColumnCount() - 1) {
                    writer.print(",");
                }
            }
            writer.println();

            // Write row data
            for (int row = 0; row < model.getRowCount(); row++) {
                for (int col = 0; col < model.getColumnCount(); col++) {
                    Object val = model.getValueAt(row, col);
                    writer.print(escapeCSV(val != null ? val.toString() : ""));
                    if (col < model.getColumnCount() - 1) {
                        writer.print(",");
                    }
                }
                writer.println();
            }
        }
    }

    private String escapeCSV(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
