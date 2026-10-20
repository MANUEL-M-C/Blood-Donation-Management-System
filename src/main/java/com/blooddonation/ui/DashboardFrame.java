package com.blooddonation.ui;

import com.blooddonation.model.BloodBank;
import com.blooddonation.service.AuthService;
import com.blooddonation.service.BloodBankService;
import com.blooddonation.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * ============================================================================
 * OOP CONCEPT: MAIN DASHBOARD & CONTROLLER FRAME
 * ============================================================================
 * DashboardFrame acts as the primary administrative dashboard for the
 * Blood Donation Management System.
 */
public class DashboardFrame extends JFrame {
    private final BloodBankService bloodBankService = new BloodBankService();

    // Navigation and Cards
    private JPanel mainContentPanel;
    private CardLayout cardLayout;
    private final List<JButton> navButtons = new ArrayList<>();

    // Sub-panels
    private JPanel dashboardHomePanel;
    private DonorPanel donorPanel;
    private RecipientPanel recipientPanel;
    private InventoryPanel inventoryPanel;
    private BloodSearchPanel bloodSearchPanel;
    private DonationPanel donationPanel;
    private DonationHistoryPanel donationHistoryPanel;
    private ReportsPanel reportsPanel;
    private AdminPanel adminPanel;

    // Metric Card Labels
    private JLabel lblTotalDonors;
    private JLabel lblTotalRecipients;
    private JLabel lblTotalDonations;
    private JLabel lblAvailableUnits;
    private JLabel lblPendingRequests;
    private JLabel lblLowStockGroups;
    private JPanel snapshotContainer;

    // Header Labels
    private JLabel lblHeaderTitle;
    private JLabel lblAdminBadge;

    public DashboardFrame() {
        setTitle("BLOOD DONATION MANAGEMENT SYSTEM - Desktop Application");
        setSize(1200, 780);
        setMinimumSize(new Dimension(1000, 680));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initLayout();
        showView("Dashboard");
    }

    private void initLayout() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BG_MAIN);

        // 1. Sidebar Navigation (Left)
        JPanel sidebar = createSidebar();
        root.add(sidebar, BorderLayout.WEST);

        // 2. Main Content Area (Center)
        JPanel centerArea = new JPanel(new BorderLayout());
        centerArea.setOpaque(false);

        // Header Bar (Top)
        JPanel headerBar = createHeaderBar();
        centerArea.add(headerBar, BorderLayout.NORTH);

        // CardLayout Container
        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);
        mainContentPanel.setOpaque(false);

        // Initialize Views
        dashboardHomePanel = createDashboardHomeView();
        donorPanel = new DonorPanel();
        recipientPanel = new RecipientPanel();
        inventoryPanel = new InventoryPanel();
        bloodSearchPanel = new BloodSearchPanel();
        donationPanel = new DonationPanel();
        donationHistoryPanel = new DonationHistoryPanel();
        reportsPanel = new ReportsPanel();
        adminPanel = new AdminPanel();

        mainContentPanel.add(dashboardHomePanel, "Dashboard");
        mainContentPanel.add(donorPanel, "Donor Registration");
        mainContentPanel.add(recipientPanel, "Recipient Requests");
        mainContentPanel.add(inventoryPanel, "Blood Inventory");
        mainContentPanel.add(bloodSearchPanel, "Search Blood");
        mainContentPanel.add(donationPanel, "Donation Management");
        mainContentPanel.add(donationHistoryPanel, "Donation History");
        mainContentPanel.add(reportsPanel, "Reports");
        mainContentPanel.add(adminPanel, "Admin");

        centerArea.add(mainContentPanel, BorderLayout.CENTER);
        root.add(centerArea, BorderLayout.CENTER);

        setContentPane(root);
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(UITheme.SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(240, 0));

        // Sidebar Branding Header
        JPanel brandPanel = new JPanel(new BorderLayout(5, 5));
        brandPanel.setOpaque(false);
        brandPanel.setBorder(new EmptyBorder(22, 18, 22, 18));

        JLabel logo = new JLabel("🩸 BLOOD BANK");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        logo.setForeground(Color.WHITE);

        JLabel brandSub = new JLabel("MANAGEMENT SYSTEM");
        brandSub.setFont(new Font("Segoe UI", Font.BOLD, 10));
        brandSub.setForeground(UITheme.ACCENT);

        brandPanel.add(logo, BorderLayout.NORTH);
        brandPanel.add(brandSub, BorderLayout.SOUTH);
        sidebar.add(brandPanel, BorderLayout.NORTH);

        // Navigation Buttons List
        JPanel navList = new JPanel();
        navList.setLayout(new BoxLayout(navList, BoxLayout.Y_AXIS));
        navList.setOpaque(false);
        navList.setBorder(new EmptyBorder(10, 10, 10, 10));

        addNavButton(navList, "📊  Dashboard", "Dashboard");
        addNavButton(navList, "🩸  Donor Registration", "Donor Registration");
        addNavButton(navList, "📋  Recipient Requests", "Recipient Requests");
        addNavButton(navList, "📦  Blood Inventory", "Blood Inventory");
        addNavButton(navList, "🔍  Search Blood", "Search Blood");
        addNavButton(navList, "💉  Donation Management", "Donation Management");
        addNavButton(navList, "📜  Donation History", "Donation History");
        addNavButton(navList, "📈  Reports", "Reports");
        addNavButton(navList, "⚙️  Admin", "Admin");

        sidebar.add(navList, BorderLayout.CENTER);

        // Sidebar Footer (Logout & Exit)
        JPanel footer = new JPanel(new GridLayout(2, 1, 6, 6));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(12, 12, 16, 12));

        JButton btnLogout = createNavFooterButton("🚪  Logout", () -> handleLogout());
        JButton btnExit = createNavFooterButton("❌  Exit Application", () -> handleExit());

        footer.add(btnLogout);
        footer.add(btnExit);
        sidebar.add(footer, BorderLayout.SOUTH);

        return sidebar;
    }

    private void addNavButton(JPanel container, String text, String viewKey) {
        JButton btn = new JButton(text);
        btn.setFont(UITheme.FONT_BOLD);
        btn.setForeground(UITheme.SIDEBAR_TEXT);
        btn.setBackground(UITheme.SIDEBAR_BG);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(220, 38));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(8, 14, 8, 14));

        btn.addActionListener(e -> showView(viewKey));
        navButtons.add(btn);
        container.add(btn);
        container.add(Box.createRigidArea(new Dimension(0, 4)));
    }

    private JButton createNavFooterButton(String text, Runnable action) {
        JButton btn = new JButton(text);
        btn.setFont(UITheme.FONT_REGULAR);
        btn.setForeground(new Color(203, 213, 225));
        btn.setBackground(new Color(30, 41, 59));
        btn.setFocusPainted(false);
        btn.setBorder(new LineBorder(new Color(51, 65, 85), 1));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> action.run());
        return btn;
    }

    private JPanel createHeaderBar() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER),
                new EmptyBorder(14, 25, 14, 25)
        ));

        lblHeaderTitle = new JLabel("Dashboard Overview");
        lblHeaderTitle.setFont(UITheme.FONT_TITLE);
        lblHeaderTitle.setForeground(UITheme.TEXT_DARK);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightPanel.setOpaque(false);

        String username = AuthService.getCurrentAdmin() != null ? AuthService.getCurrentAdmin().getUsername() : "admin";
        String role = AuthService.getCurrentAdmin() != null ? AuthService.getCurrentAdmin().getRole() : "SUPER_ADMIN";
        lblAdminBadge = new JLabel("👤 " + username + " (" + role + ")");
        lblAdminBadge.setFont(UITheme.FONT_BOLD);
        lblAdminBadge.setForeground(UITheme.PRIMARY);

        JLabel lblTime = new JLabel("📅 " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
        lblTime.setFont(UITheme.FONT_REGULAR);
        lblTime.setForeground(UITheme.TEXT_MUTED);

        rightPanel.add(lblAdminBadge);
        rightPanel.add(new JSeparator(SwingConstants.VERTICAL));
        rightPanel.add(lblTime);

        header.add(lblHeaderTitle, BorderLayout.WEST);
        header.add(rightPanel, BorderLayout.EAST);
        return header;
    }

    private JPanel createDashboardHomeView() {
        JPanel view = new JPanel(new BorderLayout(0, 20));
        view.setOpaque(false);
        view.setBorder(new EmptyBorder(20, 25, 25, 25));

        // Top: 6 Metric Cards
        JPanel statsGrid = new JPanel(new GridLayout(2, 3, 16, 16));
        statsGrid.setOpaque(false);

        lblTotalDonors = new JLabel("0", SwingConstants.CENTER);
        lblTotalRecipients = new JLabel("0", SwingConstants.CENTER);
        lblTotalDonations = new JLabel("0", SwingConstants.CENTER);
        lblAvailableUnits = new JLabel("0", SwingConstants.CENTER);
        lblPendingRequests = new JLabel("0", SwingConstants.CENTER);
        lblLowStockGroups = new JLabel("0", SwingConstants.CENTER);

        statsGrid.add(createMetricCard("👥 Total Donors", lblTotalDonors, new Color(59, 130, 246)));
        statsGrid.add(createMetricCard("🏥 Total Recipients", lblTotalRecipients, new Color(16, 185, 129)));
        statsGrid.add(createMetricCard("🩸 Total Donations", lblTotalDonations, new Color(239, 68, 68)));
        statsGrid.add(createMetricCard("📦 Available Blood Units", lblAvailableUnits, new Color(139, 92, 246)));
        statsGrid.add(createMetricCard("⏳ Pending Requests", lblPendingRequests, new Color(245, 158, 11)));
        statsGrid.add(createMetricCard("⚠️ Low Stock Blood Groups", lblLowStockGroups, new Color(220, 38, 38)));

        view.add(statsGrid, BorderLayout.NORTH);

        // Center: Live Stock Snapshot + Quick Action Launchers
        JPanel bottomSection = new JPanel(new GridLayout(1, 2, 20, 0));
        bottomSection.setOpaque(false);

        // Left Box: Stock Level Snapshot
        JPanel snapshotCard = UITheme.createCardPanel();
        snapshotCard.setLayout(new BorderLayout(10, 10));
        JLabel snapTitle = new JLabel("🩸 Blood Inventory Live Levels");
        snapTitle.setFont(UITheme.FONT_HEADER);
        snapTitle.setForeground(UITheme.PRIMARY);
        snapshotCard.add(snapTitle, BorderLayout.NORTH);

        snapshotContainer = new JPanel(new GridLayout(2, 4, 8, 8));
        snapshotContainer.setOpaque(false);
        snapshotCard.add(snapshotContainer, BorderLayout.CENTER);

        bottomSection.add(snapshotCard);

        // Right Box: Quick Academic Demonstrations
        JPanel quickCard = UITheme.createCardPanel();
        quickCard.setLayout(new BorderLayout(10, 10));

        JLabel quickTitle = new JLabel("⚡ Quick Academic Actions");
        quickTitle.setFont(UITheme.FONT_HEADER);
        quickTitle.setForeground(UITheme.TEXT_DARK);
        quickCard.add(quickTitle, BorderLayout.NORTH);

        JPanel actionsGrid = new JPanel(new GridLayout(4, 1, 8, 8));
        actionsGrid.setOpaque(false);

        JButton btnQuickDonor = UITheme.createPrimaryButton("➕ Register New Donor");
        JButton btnQuickDonation = UITheme.createSecondaryButton("💉 Record New Donation");
        JButton btnQuickSearch = UITheme.createSecondaryButton("🔍 Search & Issue Blood");
        JButton btnQuickReports = UITheme.createSecondaryButton("📈 View Analytical Reports");

        btnQuickDonor.addActionListener(e -> showView("Donor Registration"));
        btnQuickDonation.addActionListener(e -> showView("Donation Management"));
        btnQuickSearch.addActionListener(e -> showView("Search Blood"));
        btnQuickReports.addActionListener(e -> showView("Reports"));

        actionsGrid.add(btnQuickDonor);
        actionsGrid.add(btnQuickDonation);
        actionsGrid.add(btnQuickSearch);
        actionsGrid.add(btnQuickReports);

        quickCard.add(actionsGrid, BorderLayout.CENTER);
        bottomSection.add(quickCard);

        view.add(bottomSection, BorderLayout.CENTER);
        return view;
    }

    private JPanel createMetricCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = UITheme.createCardPanel();
        card.setLayout(new BorderLayout(6, 6));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(UITheme.FONT_HEADER);
        lblTitle.setForeground(UITheme.TEXT_MUTED);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        valueLabel.setForeground(accentColor);

        // Bottom colored accent strip
        JPanel strip = new JPanel();
        strip.setBackground(accentColor);
        strip.setPreferredSize(new Dimension(0, 4));

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        card.add(strip, BorderLayout.SOUTH);
        return card;
    }

    public void showView(String viewKey) {
        cardLayout.show(mainContentPanel, viewKey);
        lblHeaderTitle.setText(viewKey);

        // Highlight active nav button
        for (JButton b : navButtons) {
            if (b.getText().contains(viewKey)) {
                b.setBackground(UITheme.PRIMARY);
                b.setForeground(Color.WHITE);
            } else {
                b.setBackground(UITheme.SIDEBAR_BG);
                b.setForeground(UITheme.SIDEBAR_TEXT);
            }
        }

        // Trigger view-specific data refresh
        if ("Dashboard".equals(viewKey)) {
            refreshDashboardMetrics();
        } else if ("Donor Registration".equals(viewKey)) {
            donorPanel.loadDonorData();
        } else if ("Recipient Requests".equals(viewKey)) {
            recipientPanel.loadRecipientData();
        } else if ("Blood Inventory".equals(viewKey)) {
            inventoryPanel.loadInventoryData();
        } else if ("Search Blood".equals(viewKey)) {
            bloodSearchPanel.refreshAll();
        } else if ("Donation History".equals(viewKey)) {
            donationHistoryPanel.loadHistory();
        } else if ("Reports".equals(viewKey)) {
            reportsPanel.loadSelectedReport();
        } else if ("Admin".equals(viewKey)) {
            adminPanel.loadAdminInfo();
        }
    }

    public void refreshDashboardMetrics() {
        try {
            Map<String, Integer> stats = bloodBankService.getDashboardStatistics();
            lblTotalDonors.setText(String.valueOf(stats.getOrDefault("totalDonors", 0)));
            lblTotalRecipients.setText(String.valueOf(stats.getOrDefault("totalRecipients", 0)));
            lblTotalDonations.setText(String.valueOf(stats.getOrDefault("totalDonations", 0)));
            lblAvailableUnits.setText(String.valueOf(stats.getOrDefault("availableUnits", 0)));
            lblPendingRequests.setText(String.valueOf(stats.getOrDefault("pendingRequests", 0)));
            lblLowStockGroups.setText(String.valueOf(stats.getOrDefault("lowStockGroups", 0)));

            // Update live inventory levels snapshot
            snapshotContainer.removeAll();
            Map<String, Integer> stock = bloodBankService.getInventoryStock();
            for (String bg : BloodBank.BLOOD_GROUPS) {
                int count = stock.getOrDefault(bg, 0);
                JPanel p = new JPanel(new GridLayout(2, 1));
                p.setBackground(count > 0 ? Color.WHITE : new Color(254, 242, 242));
                p.setBorder(new LineBorder(count > 0 ? UITheme.BORDER : UITheme.STATUS_DANGER, 1));

                JLabel lblBg = new JLabel(bg, SwingConstants.CENTER);
                lblBg.setFont(UITheme.FONT_BOLD);
                lblBg.setForeground(UITheme.PRIMARY);

                JLabel lblCount = new JLabel(count + " units", SwingConstants.CENTER);
                lblCount.setFont(UITheme.FONT_SMALL);
                lblCount.setForeground(count <= 3 ? UITheme.STATUS_WARNING : UITheme.STATUS_SUCCESS);

                p.add(lblBg);
                p.add(lblCount);
                snapshotContainer.add(p);
            }
            snapshotContainer.revalidate();
            snapshotContainer.repaint();

        } catch (Exception ex) {
            System.err.println("Error refreshing dashboard stats: " + ex.getMessage());
        }
    }

    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to log out of the system?",
                "Confirm Logout", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            AuthService.logout();
            dispose();
            SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
        }
    }

    private void handleExit() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to exit the application?",
                "Confirm Exit", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }
}
