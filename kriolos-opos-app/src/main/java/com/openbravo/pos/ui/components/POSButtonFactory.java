/*
 * Copyright (C) 2026 KriolOS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.openbravo.pos.ui.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

public class POSButtonFactory {

    public static final Color SUCCESS_COLOR = new Color(22, 163, 74);  // Emerald green #16a34a
    public static final Color DANGER_COLOR = new Color(239, 68, 68);   // Coral red #ef4444
    public static final Color ACCENT_COLOR = new Color(37, 99, 235);   // Royal blue #2563eb
    public static final Color WARNING_COLOR = new Color(245, 158, 11); // Amber #f59e0b

    /**
     * Creates a JButton configured for POS with native LTR/RTL support without
     * FlatLaf.
     *
     * @param size The desired size variant (Medium, Large, Extra_Large).
     * @return A customized native JButton.
     */
    public static JButton createButton(ButtonSize size) {
        JButton btn = new JButton();

        // 1. Logical Alignment: [Icon TEXT ......] compatible with LTR/RTL
        btn.applyComponentOrientation(btn.getComponentOrientation());
        btn.setHorizontalTextPosition(SwingConstants.TRAILING);
        btn.setHorizontalAlignment(SwingConstants.LEADING);

        // 2. Native Swing Sizing: Define height using minimum and preferred structures
        // We use a generic width of 100 as a baseline; layouts like JFlowPanel will respect the height
        Dimension baseSize = new Dimension(100, size.getHeight());
        btn.setPreferredSize(baseSize);
        btn.setMinimumSize(baseSize);

        // 3. Native Padding: Generous margins for touch-ready interactions using EmptyBorder
        btn.setBorder(BorderFactory.createCompoundBorder(
                btn.getBorder(),
                new EmptyBorder(0, size.getPaddingX(), 0, size.getPaddingX())
        ));

        // 4. Native Spacing: Scaled gap between Icon and Text
        btn.setIconTextGap(size.getFontSize() / 4);

        // 5. Touch Accessibility Settings
        btn.setFocusPainted(false);
        btn.setFocusable(false);
        btn.setRequestFocusEnabled(false);

        // 6. Native Typography Scaling
        Font currentFont = btn.getFont();
        if (currentFont != null) {
            btn.setFont(new Font(currentFont.getName(), Font.BOLD, size.getFontSize()));
        }

        // Focus-Clearing Hook: Defends the global Numpad Enter mapping from theft
        btn.addActionListener(e -> clearFocus(btn));

        return btn;
    }

    /**
     * Creates an ergonomic, touch-friendly POS action button with font-based
     * vector icon, accessible keyboard mnemonic, descriptive tooltip, and
     * FlatLaf styling.
     *
     * @param text Button label text
     * @param fontIcon Font-based icon glyph (Unicode) or null
     * @param size Sizing variant (MEDIUM, LARGE, EXTRA_LARGE, MASSIVE)
     * @param mnemonic KeyEvent.VK_* constant (or 0 if none)
     * @param tooltip Informative tooltip or null
     * @param listener Action listener to execute on click/trigger
     * @return Fully configured touch-friendly JButton
     */
    public static JButton createActionButton(
            String text,
            String fontIcon,
            ButtonSize size,
            int mnemonic,
            String tooltip,
            ActionListener listener) {

        JButton btn = new JButton();
        btn.setText(formatButtonText(text, fontIcon));

        // Logical Alignment: [Icon TEXT ......] compatible with LTR/RTL
        btn.applyComponentOrientation(btn.getComponentOrientation());
        btn.setHorizontalAlignment(SwingConstants.CENTER);
        btn.setHorizontalTextPosition(SwingConstants.TRAILING);
        btn.setVerticalTextPosition(SwingConstants.CENTER);

        // Native Swing Sizing: Define height using minimum and preferred structures
        int h = size != null ? size.getHeight() : ButtonSize.DEFAULT_HEIGHT;
        Dimension baseSize = new Dimension(100, h);
        btn.setPreferredSize(baseSize);
        btn.setMinimumSize(baseSize);

        // Native Padding: Generous margins for touch-ready interactions using EmptyBorder
        int padX = size != null ? size.getPaddingX() : ButtonSize.DEFAULT_PADDING;
        btn.setBorder(BorderFactory.createCompoundBorder(
                btn.getBorder(),
                new EmptyBorder(0, padX, 0, padX)
        ));

        int fontSize = size != null ? size.getFontSize() : ButtonSize.DEFAULT_FONT_SIZE;

        // Native Spacing: Scaled gap between Icon and Text
        btn.setIconTextGap(fontSize / 4);

        // Native Typography Scaling
        Font currentFont = btn.getFont();
        if (currentFont != null) {
            btn.setFont(new Font(currentFont.getName(), Font.BOLD, fontSize));
        }

        // Touch Accessibility Settings
        btn.setFocusPainted(false);
        btn.setFocusable(false);
        btn.setRequestFocusEnabled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Modern UI rounded corners
        btn.putClientProperty("JButton.buttonType", "roundRect");

        // Keyboard mnemonic and tooltip
        setupMnemonicAndTooltip(btn, mnemonic, tooltip, text);

        if (listener != null) {
            btn.addActionListener(listener);
        }

        // Focus-Clearing Hook: Defends the global Numpad Enter mapping from theft
        btn.addActionListener(e -> clearFocus(btn));

        return btn;
    }

    public static JButton createActionButton(
            String text,
            UnicodeIcon icon,
            ButtonSize size,
            int mnemonic,
            String tooltip,
            ActionListener listener) {
        return createActionButton(text, icon != null ? icon.getCode() : null, size, mnemonic, tooltip, listener);
    }

    public static JButton createActionButton(
            String text,
            String fontIcon,
            ButtonSize size,
            ActionListener listener) {
        return createActionButton(text, fontIcon, size, 0, null, listener);
    }

    public static JButton createActionButton(
            String text,
            UnicodeIcon icon,
            ButtonSize size,
            ActionListener listener) {
        return createActionButton(text, icon, size, 0, null, listener);
    }

    public static JButton createActionButton(
            String text,
            String fontIcon,
            ButtonSize size,
            int mnemonic,
            ActionListener listener) {
        return createActionButton(text, fontIcon, size, mnemonic, null, listener);
    }

    public static JButton createActionButton(
            String text,
            UnicodeIcon icon,
            ButtonSize size,
            int mnemonic,
            ActionListener listener) {
        return createActionButton(text, icon, size, mnemonic, null, listener);
    }
    
    public static JButton createActionButton(
            String text,
            ButtonSize size,
            int mnemonic,
            ActionListener listener) {
        return createActionButton(text, "", size, mnemonic, null, listener);
    }
    
    public static JButton createActionButton(
            String text,
            ButtonSize size,
            ActionListener listener) {
        return createActionButton(text, "", size, 0, null, listener);
    }

    /**
     * Creates a success / positive Call-To-Action button (e.g. Pay, Checkout).
     */
    public static JButton createSuccessButton(
            String text,
            String fontIcon,
            ButtonSize size,
            int mnemonic,
            String tooltip,
            ActionListener listener) {
        JButton btn = createActionButton(text, fontIcon, size, mnemonic, tooltip, listener);
        btn.setBackground(SUCCESS_COLOR);
        btn.setForeground(Color.WHITE);
        btn.setOpaque(true);
        btn.putClientProperty("FlatLaf.styleClass", "accent");
        return btn;
    }

    public static JButton createSuccessButton(
            String text,
            UnicodeIcon icon,
            ButtonSize size,
            int mnemonic,
            String tooltip,
            ActionListener listener) {
        return createSuccessButton(text, icon != null ? icon.getCode() : null, size, mnemonic, tooltip, listener);
    }

    /**
     * Creates a danger / destructive action button (e.g. Close Cash, Delete
     * Line).
     */
    public static JButton createDangerButton(
            String text,
            String fontIcon,
            ButtonSize size,
            int mnemonic,
            String tooltip,
            ActionListener listener) {
        JButton btn = createActionButton(text, fontIcon, size, mnemonic, tooltip, listener);
        btn.setBackground(DANGER_COLOR);
        btn.setForeground(Color.WHITE);
        btn.setOpaque(true);
        return btn;
    }

    public static JButton createDangerButton(
            String text,
            UnicodeIcon icon,
            ButtonSize size,
            int mnemonic,
            String tooltip,
            ActionListener listener) {
        return createDangerButton(text, icon != null ? icon.getCode() : null, size, mnemonic, tooltip, listener);
    }

    /**
     * Creates an accent-highlighted action button.
     */
    public static JButton createAccentButton(
            String text,
            String fontIcon,
            ButtonSize size,
            int mnemonic,
            String tooltip,
            ActionListener listener) {
        JButton btn = createActionButton(text, fontIcon, size, mnemonic, tooltip, listener);
        btn.putClientProperty("FlatLaf.styleClass", "accent");
        return btn;
    }

    public static JButton createAccentButton(
            String text,
            UnicodeIcon icon,
            ButtonSize size,
            int mnemonic,
            String tooltip,
            ActionListener listener) {
        return createAccentButton(text, icon != null ? icon.getCode() : null, size, mnemonic, tooltip, listener);
    }

    /**
     * Formats button text with optional font-based vector icon.
     */
    public static String formatButtonText(String text, String fontIcon) {
        if (fontIcon != null && !fontIcon.isBlank()) {
            if (text != null && !text.isBlank()) {
                return fontIcon + "  " + text;
            }
            return fontIcon;
        }
        return text != null ? text : "";
    }

    public static String formatButtonText(String text, UnicodeIcon icon) {
        return formatButtonText(text, icon != null ? icon.getCode() : null);
    }

    // ====================================================================
    // PRIVATE HELPER METHODS
    // ====================================================================
    private static void setupMnemonicAndTooltip(JButton btn, int mnemonic, String tooltip, String text) {
        if (mnemonic > 0) {
            btn.setMnemonic(mnemonic);
        }
        String keyText = mnemonic > 0 ? KeyEvent.getKeyText(mnemonic) : null;
        if (tooltip != null && !tooltip.isBlank()) {
            if (keyText != null && !tooltip.contains("Alt+") && !tooltip.contains("F")) {
                btn.setToolTipText(tooltip + " (Alt+" + keyText + ")");
            } else {
                btn.setToolTipText(tooltip);
            }
        } else if (keyText != null) {
            String base = (text != null && !text.isBlank()) ? text.trim() : "Ação";
            btn.setToolTipText(base + " (Alt+" + keyText + ")");
        }
    }

    /**
     * Safely returns focus back to the primary window ancestor.
     */
    private static void clearFocus(JButton button) {
        Component window = SwingUtilities.getWindowAncestor(button);
        if (window != null) {
            window.requestFocusInWindow();
        }
    }
}
