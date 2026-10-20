package com.blooddonation.util;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

/**
 * ============================================================================
 * UI & PRESENTATION THEME UTILITY
 * ============================================================================
 * Centralizes medical/blood-bank themed styling, custom typography,
 * consistent palettes, and responsive table decorators.
 */
public class UITheme {

    // Medical Crimson Palette
    public static final Color PRIMARY = new Color(183, 28, 28);       // #B71C1C Deep Crimson
    public static final Color PRIMARY_HOVER = new Color(211, 47, 47); // #D32F2F Bright Crimson
    public static final Color PRIMARY_DARK = new Color(127, 0, 0);    // #7F0000 Dark Burgundy
    public static final Color ACCENT = new Color(229, 57, 53);        // #E53935

    // Neutral / Structural Colors
    public static final Color SIDEBAR_BG = new Color(26, 32, 44);     // #1A202C Dark Slate
    public static final Color SIDEBAR_HOVER = new Color(45, 55, 72);  // #2D3748
    public static final Color SIDEBAR_ACTIVE = new Color(183, 28, 28);
    public static final Color SIDEBAR_TEXT = new Color(241, 245, 249);

    public static final Color BG_MAIN = new Color(248, 250, 252);     // #F8FAFC
    public static final Color CARD_BG = Color.WHITE;
    public static final Color BORDER = new Color(226, 232, 240);       // #E2E8F0
    public static final Color TEXT_DARK = new Color(15, 23, 42);      // #0F172A
    public static final Color TEXT_MUTED = new Color(100, 116, 139);  // #64748B

    // Status Colors
    public static final Color STATUS_SUCCESS = new Color(22, 163, 74); // Available #16A34A
    public static final Color STATUS_WARNING = new Color(217, 119, 6); // Low Stock #D97706
    public static final Color STATUS_DANGER = new Color(220, 38, 38);  // Out of Stock / Rejected #DC2626
    public static final Color STATUS_INFO = new Color(37, 99, 235);    // Issued #2563EB

    // Standard Fonts
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);

    private UITheme() {}

    /**
     * Initializes modern Look & Feel (FlatLaf if present, or System Look & Feel)
     */
    public static void setupLookAndFeel() {
        try {
            // Attempt to load modern FlatLightLaf
            UIManager.setLookAndFeel("com.formdev.flatlaf.FlatLightLaf");
        } catch (Exception e) {
            try {
                // Fallback to system look and feel
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
        }

        // Global UI tweaks
        UIManager.put("Button.arc", 8);
        UIManager.put("Component.arc", 8);
        UIManager.put("TextComponent.arc", 8);
        UIManager.put("ScrollBar.showButtons", false);
        UIManager.put("ScrollBar.width", 10);
    }

    /**
     * Creates a styled primary button with medical red accent.
     */
    public static JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setBackground(PRIMARY);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(9, 18, 9, 18));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(PRIMARY_HOVER);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(PRIMARY);
            }
        });
        return btn;
    }

    /**
     * Creates a secondary/neutral button.
     */
    public static JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setBackground(Color.WHITE);
        btn.setForeground(TEXT_DARK);
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(new LineBorder(BORDER, 1), new EmptyBorder(8, 16, 8, 16)));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(241, 245, 249));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(Color.WHITE);
            }
        });
        return btn;
    }

    /**
     * Creates a danger/delete button.
     */
    public static JButton createDangerButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setBackground(STATUS_DANGER);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(9, 18, 9, 18));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(185, 28, 28));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(STATUS_DANGER);
            }
        });
        return btn;
    }

    /**
     * Styles standard input fields with modern border and padding.
     */
    public static void styleTextField(JTextField field) {
        field.setFont(FONT_REGULAR);
        field.setBorder(new CompoundBorder(
                new LineBorder(BORDER, 1),
                new EmptyBorder(6, 10, 6, 10)
        ));
    }

    /**
     * Styles a JTable with professional header and row heights.
     */
    public static void styleTable(JTable table) {
        table.setFont(FONT_REGULAR);
        table.setRowHeight(32);
        table.setSelectionBackground(new Color(254, 226, 226)); // Soft red highlight
        table.setSelectionForeground(TEXT_DARK);
        table.setGridColor(new Color(241, 245, 249));
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BOLD);
        header.setBackground(new Color(241, 245, 249));
        header.setForeground(TEXT_DARK);
        header.setPreferredSize(new Dimension(0, 36));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));

        // Center align cell contents slightly
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.setDefaultRenderer(Integer.class, centerRenderer);
    }

    /**
     * Creates a styled card container panel.
     */
    public static JPanel createCardPanel() {
        JPanel card = new JPanel();
        card.setBackground(CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(BORDER, 1),
                new EmptyBorder(16, 20, 16, 20)
        ));
        return card;
    }
}
