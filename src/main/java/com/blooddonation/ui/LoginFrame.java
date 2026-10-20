package com.blooddonation.ui;

import com.blooddonation.service.AuthService;
import com.blooddonation.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * ============================================================================
 * OOP CONCEPT: SECURITY & AUTHENTICATION GUI
 * ============================================================================
 * LoginFrame provides a secure credential gateway into the application.
 */
public class LoginFrame extends JFrame {
    private final AuthService authService = new AuthService();

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JCheckBox chkShowPassword;
    private JButton btnLogin;
    private JButton btnExit;

    public LoginFrame() {
        setTitle("Blood Donation Management System - Authentication");
        setSize(480, 560);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
    }

    private void initComponents() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);

        // --- Top Crimson Header Banner ---
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.PRIMARY);
        header.setBorder(new EmptyBorder(30, 25, 30, 25));

        JLabel lblLogo = new JLabel("🩸", SwingConstants.CENTER);
        lblLogo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 46));

        JLabel lblTitle = new JLabel("BLOOD DONATION SYSTEM", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblSub = new JLabel("Academic OOP Project • Java Swing & SQLite", SwingConstants.CENTER);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(254, 202, 202));

        header.add(lblLogo, BorderLayout.NORTH);
        header.add(lblTitle, BorderLayout.CENTER);
        header.add(lblSub, BorderLayout.SOUTH);

        root.add(header, BorderLayout.NORTH);

        // --- Form Body ---
        JPanel formContainer = new JPanel();
        formContainer.setLayout(new BoxLayout(formContainer, BoxLayout.Y_AXIS));
        formContainer.setBackground(Color.WHITE);
        formContainer.setBorder(new EmptyBorder(30, 45, 30, 45));

        // Instructions / Welcome
        JLabel lblWelcome = new JLabel("Administrator Login");
        lblWelcome.setFont(UITheme.FONT_SUBTITLE);
        lblWelcome.setForeground(UITheme.TEXT_DARK);
        lblWelcome.setAlignmentX(Component.CENTER_ALIGNMENT);
        formContainer.add(lblWelcome);

        JLabel lblDefaultHint = new JLabel("Default Login: admin / admin123");
        lblDefaultHint.setFont(UITheme.FONT_SMALL);
        lblDefaultHint.setForeground(UITheme.TEXT_MUTED);
        lblDefaultHint.setAlignmentX(Component.CENTER_ALIGNMENT);
        formContainer.add(lblDefaultHint);
        formContainer.add(Box.createRigidArea(new Dimension(0, 22)));

        // Username Field
        JLabel lblUser = new JLabel("Username:");
        lblUser.setFont(UITheme.FONT_BOLD);
        lblUser.setForeground(UITheme.TEXT_DARK);
        lblUser.setAlignmentX(Component.LEFT_ALIGNMENT);
        formContainer.add(lblUser);
        formContainer.add(Box.createRigidArea(new Dimension(0, 4)));

        txtUsername = new JTextField("admin");
        txtUsername.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        UITheme.styleTextField(txtUsername);
        txtUsername.setAlignmentX(Component.LEFT_ALIGNMENT);
        formContainer.add(txtUsername);
        formContainer.add(Box.createRigidArea(new Dimension(0, 14)));

        // Password Field
        JLabel lblPwd = new JLabel("Password:");
        lblPwd.setFont(UITheme.FONT_BOLD);
        lblPwd.setForeground(UITheme.TEXT_DARK);
        lblPwd.setAlignmentX(Component.LEFT_ALIGNMENT);
        formContainer.add(lblPwd);
        formContainer.add(Box.createRigidArea(new Dimension(0, 4)));

        txtPassword = new JPasswordField("admin123");
        txtPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        txtPassword.setFont(UITheme.FONT_REGULAR);
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UITheme.BORDER, 1),
                new EmptyBorder(6, 10, 6, 10)
        ));
        txtPassword.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Enter key to submit
        KeyAdapter enterSubmit = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    handleLogin();
                }
            }
        };
        txtUsername.addKeyListener(enterSubmit);
        txtPassword.addKeyListener(enterSubmit);

        formContainer.add(txtPassword);
        formContainer.add(Box.createRigidArea(new Dimension(0, 8)));

        // Show/Hide Password Checkbox
        chkShowPassword = new JCheckBox("Show Password");
        chkShowPassword.setFont(UITheme.FONT_SMALL);
        chkShowPassword.setOpaque(false);
        chkShowPassword.setForeground(UITheme.TEXT_MUTED);
        chkShowPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
        chkShowPassword.addActionListener(e -> {
            if (chkShowPassword.isSelected()) {
                txtPassword.setEchoChar((char) 0);
            } else {
                txtPassword.setEchoChar('•');
            }
        });
        formContainer.add(chkShowPassword);
        formContainer.add(Box.createRigidArea(new Dimension(0, 20)));

        // Login Button
        btnLogin = UITheme.createPrimaryButton("SECURE LOGIN");
        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogin.addActionListener(e -> handleLogin());
        formContainer.add(btnLogin);
        formContainer.add(Box.createRigidArea(new Dimension(0, 10)));

        // Exit Button
        btnExit = UITheme.createSecondaryButton("Exit Application");
        btnExit.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnExit.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnExit.addActionListener(e -> System.exit(0));
        formContainer.add(btnExit);

        root.add(formContainer, BorderLayout.CENTER);
        setContentPane(root);
    }

    private void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both username and password.", "Login Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            boolean success = authService.login(username, password);
            if (success) {
                dispose();
                SwingUtilities.invokeLater(() -> {
                    DashboardFrame dashboard = new DashboardFrame();
                    dashboard.setVisible(true);
                });
            } else {
                JOptionPane.showMessageDialog(this, "Invalid username or password. Please try again.", "Authentication Failed", JOptionPane.ERROR_MESSAGE);
                txtPassword.setText("");
                txtPassword.requestFocus();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Unable to connect to database: " + ex.getMessage(), "System Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
