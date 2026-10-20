package com.blooddonation.ui;

import com.blooddonation.dao.SchemaInitializer;
import com.blooddonation.model.Admin;
import com.blooddonation.service.AuthService;
import com.blooddonation.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * ============================================================================
 * OOP CONCEPT: SECURITY & SYSTEM ADMINISTRATION INTERFACE
 * ============================================================================
 * AdminPanel provides credential management, password modification,
 * and academic demo data initialization.
 */
public class AdminPanel extends JPanel {
    private final AuthService authService = new AuthService();

    private JLabel lblAdminId;
    private JLabel lblUsername;
    private JLabel lblRole;

    private JPasswordField txtOldPassword;
    private JPasswordField txtNewPassword;
    private JPasswordField txtConfirmPassword;

    public AdminPanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(UITheme.BG_MAIN);
        setBorder(new EmptyBorder(15, 20, 20, 20));

        initComponents();
        loadAdminInfo();
    }

    private void initComponents() {
        // --- Header ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("⚙️ System Administration & Settings");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.TEXT_DARK);

        JLabel lblSubtitle = new JLabel("Manage administrative credentials, change password, and populate academic demo records.");
        lblSubtitle.setFont(UITheme.FONT_REGULAR);
        lblSubtitle.setForeground(UITheme.TEXT_MUTED);

        headerPanel.add(lblTitle, BorderLayout.NORTH);
        headerPanel.add(lblSubtitle, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        // --- Center Split ---
        JPanel centerSplit = new JPanel(new GridLayout(1, 2, 20, 0));
        centerSplit.setOpaque(false);

        // Left Card: Current Admin & Demo Data Loader
        centerSplit.add(createAdminInfoCard());

        // Right Card: Change Password Form
        centerSplit.add(createChangePasswordCard());

        add(centerSplit, BorderLayout.CENTER);
    }

    private JPanel createAdminInfoCard() {
        JPanel card = UITheme.createCardPanel();
        card.setLayout(new BorderLayout(15, 15));

        JLabel title = new JLabel("Administrator Profile");
        title.setFont(UITheme.FONT_HEADER);
        title.setForeground(UITheme.PRIMARY);
        card.add(title, BorderLayout.NORTH);

        JPanel details = new JPanel(new GridLayout(4, 2, 10, 15));
        details.setOpaque(false);

        details.add(new JLabel("Admin ID:"));
        lblAdminId = new JLabel("ADM001");
        lblAdminId.setFont(UITheme.FONT_BOLD);
        details.add(lblAdminId);

        details.add(new JLabel("Logged-in Username:"));
        lblUsername = new JLabel("admin");
        lblUsername.setFont(UITheme.FONT_BOLD);
        details.add(lblUsername);

        details.add(new JLabel("Role / Privileges:"));
        lblRole = new JLabel("SUPER_ADMIN");
        lblRole.setFont(UITheme.FONT_BOLD);
        lblRole.setForeground(UITheme.PRIMARY);
        details.add(lblRole);

        details.add(new JLabel("Database Backend:"));
        JLabel lblDb = new JLabel("SQLite (blood_donation.db)");
        lblDb.setFont(UITheme.FONT_BOLD);
        details.add(lblDb);

        card.add(details, BorderLayout.CENTER);

        // Demo Data Loader Box
        JPanel demoBox = new JPanel(new BorderLayout(8, 8));
        demoBox.setOpaque(false);
        demoBox.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(UITheme.BORDER),
                "Academic Demo Data",
                0, 0, UITheme.FONT_BOLD, UITheme.PRIMARY));

        JLabel demoHint = new JLabel("<html>Populate initial sample records:<br/>• Donors: Rahul (O+), Arun (A+), Anu (B+)<br/>• Inventory: A+=8, B+=6, O+=12, AB+=3, A-=2</html>");
        demoHint.setFont(UITheme.FONT_SMALL);
        demoHint.setForeground(UITheme.TEXT_MUTED);
        demoBox.add(demoHint, BorderLayout.CENTER);

        JButton btnLoadDemo = UITheme.createSecondaryButton("🚀 LOAD SAMPLE / DEMO DATA");
        btnLoadDemo.addActionListener(e -> handleLoadDemoData());
        demoBox.add(btnLoadDemo, BorderLayout.SOUTH);

        card.add(demoBox, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createChangePasswordCard() {
        JPanel card = UITheme.createCardPanel();
        card.setLayout(new BorderLayout(15, 15));

        JLabel title = new JLabel("Change Account Password");
        title.setFont(UITheme.FONT_HEADER);
        title.setForeground(UITheme.TEXT_DARK);
        card.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(3, 1, 10, 10));
        form.setOpaque(false);

        txtOldPassword = new JPasswordField();
        txtOldPassword.setFont(UITheme.FONT_REGULAR);
        form.add(createFieldWrapper("Current Password *:", txtOldPassword));

        txtNewPassword = new JPasswordField();
        txtNewPassword.setFont(UITheme.FONT_REGULAR);
        form.add(createFieldWrapper("New Password * (Min 4 characters):", txtNewPassword));

        txtConfirmPassword = new JPasswordField();
        txtConfirmPassword.setFont(UITheme.FONT_REGULAR);
        form.add(createFieldWrapper("Confirm New Password *:", txtConfirmPassword));

        card.add(form, BorderLayout.CENTER);

        JButton btnChangePwd = UITheme.createPrimaryButton("UPDATE PASSWORD");
        btnChangePwd.addActionListener(e -> handleChangePassword());
        card.add(btnChangePwd, BorderLayout.SOUTH);

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

    public void loadAdminInfo() {
        Admin current = AuthService.getCurrentAdmin();
        if (current != null) {
            lblAdminId.setText(current.getAdminId());
            lblUsername.setText(current.getUsername());
            lblRole.setText(current.getRole());
        }
    }

    private void handleChangePassword() {
        String oldPwd = new String(txtOldPassword.getPassword());
        String newPwd = new String(txtNewPassword.getPassword());
        String confirmPwd = new String(txtConfirmPassword.getPassword());

        try {
            boolean updated = authService.changeCurrentPassword(oldPwd, newPwd, confirmPwd);
            if (updated) {
                JOptionPane.showMessageDialog(this, "Password updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                txtOldPassword.setText("");
                txtNewPassword.setText("");
                txtConfirmPassword.setText("");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Password Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleLoadDemoData() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Do you want to load sample donors (Rahul, Arun, Anu) and sample blood inventory?",
                "Confirm Demo Data Load", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                SchemaInitializer.loadDemoData();
                JOptionPane.showMessageDialog(this, "Demo data successfully loaded into SQLite database!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to load demo data: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
