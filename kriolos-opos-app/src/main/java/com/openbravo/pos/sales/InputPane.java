//    KriolOS POS
//    Copyright (c) 2019-2026 KriolOS
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.

package com.openbravo.pos.sales;

import com.openbravo.beans.JNumberEventListener;
import com.openbravo.beans.JNumberKeys;
import com.openbravo.pos.forms.AppLocal;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

/**
 * Numeric keypad and input controls pane (price display, quantity indicator, barcode scanner enter, tax options).
 * Pure Java Swing layout with zero NetBeans Matisse form dependencies.
 */
public class InputPane extends JPanel {

    private final JNumberKeys numberKeys;
    private final JLabel priceLabel;
    private final JLabel porLabel;
    private final JTextField keyFactory;
    private final JPanel contentPanel;
    private final JButton btnEnter;
    private final JCheckBox chkAddTax;
    private final JComboBox<Object> comboTax;
    private final JPanel scannerPanel;

    public InputPane() {
        super(new BorderLayout());
        setOpaque(false);
        setMinimumSize(new Dimension(300, 350));

        contentPanel = new JPanel();
        contentPanel.setOpaque(false);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setPreferredSize(new Dimension(300, 350));

        // Keypad
        numberKeys = new JNumberKeys();
        numberKeys.setPreferredSize(new Dimension(250, 250));
        numberKeys.setMinimumSize(new Dimension(250, 250));
        numberKeys.setMaximumSize(new Dimension(300, 300));
        contentPanel.add(numberKeys);

        // Scanner / Entry strip panel
        scannerPanel = new JPanel(new GridBagLayout());
        scannerPanel.setOpaque(false);
        scannerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        scannerPanel.setMaximumSize(new Dimension(300, 105));

        Font baseFont = UIManager.getFont("Label.font");
        if (baseFont == null) {
            baseFont = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
        }

        // Multiplier label (e.g. "x", "..")
        porLabel = new JLabel("..");
        porLabel.setFont(baseFont);
        porLabel.setRequestFocusEnabled(false);

        // Hidden key listener textfield
        keyFactory = new JTextField("..");
        keyFactory.setEditable(false);
        keyFactory.setFont(baseFont.deriveFont(11f));
        keyFactory.setForeground(UIManager.getColor("Panel.background"));
        keyFactory.setCaretColor(UIManager.getColor("Panel.background"));
        keyFactory.setBorder(null);
        keyFactory.setAutoscrolls(false);
        keyFactory.setRequestFocusEnabled(false);
        keyFactory.setVerifyInputWhenFocusTarget(false);

        // Tax included checkbox
        chkAddTax = new JCheckBox();
        chkAddTax.setOpaque(false);
        chkAddTax.setToolTipText(AppLocal.getIntString("tooltip.switchtax"));

        // Price display label
        Color primaryAccent = UIManager.getColor("Component.accentColor");
        if (primaryAccent == null) {
            primaryAccent = new Color(76, 197, 237);
        }
        priceLabel = new JLabel();
        priceLabel.setFont(baseFont.deriveFont(Font.BOLD, 16f));
        priceLabel.setForeground(primaryAccent);
        priceLabel.setHorizontalAlignment(SwingConstants.LEFT);
        priceLabel.setOpaque(true);
        priceLabel.setBackground(UIManager.getColor("TextField.background"));
        priceLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(primaryAccent),
                BorderFactory.createEmptyBorder(1, 4, 1, 4)
        ));
        priceLabel.setPreferredSize(new Dimension(100, 25));
        priceLabel.setRequestFocusEnabled(false);

        // Barcode / Enter button
        btnEnter = new JButton();
        try {
            java.net.URL iconUrl = getClass().getResource("/com/openbravo/images/barcode.png");
            if (iconUrl != null) {
                btnEnter.setIcon(new ImageIcon(iconUrl));
            }
        } catch (Exception ignored) {
        }
        btnEnter.setToolTipText(AppLocal.getIntString("tooltip.salebarcode"));
        btnEnter.setFocusPainted(false);
        btnEnter.setFocusable(false);
        btnEnter.setRequestFocusEnabled(false);
        btnEnter.setPreferredSize(new Dimension(64, 45));

        // Tax category dropdown
        comboTax = new JComboBox<>();
        comboTax.setFont(baseFont.deriveFont(14f));
        comboTax.setToolTipText(AppLocal.getIntString("tooltip.salestaxswitch"));
        comboTax.setFocusable(false);

        // GridBag Layout Wiring
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.fill = GridBagConstraints.BOTH;

        // Col 0, Row 0: porLabel
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        scannerPanel.add(porLabel, gbc);

        // Col 0, Row 1: keyFactory
        gbc.gridx = 0;
        gbc.gridy = 1;
        scannerPanel.add(keyFactory, gbc);

        // Col 0, Row 2: chkAddTax
        gbc.gridx = 0;
        gbc.gridy = 2;
        scannerPanel.add(chkAddTax, gbc);

        // Col 1, Row 0-1: priceLabel
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridheight = 2;
        gbc.weightx = 1.0;
        scannerPanel.add(priceLabel, gbc);

        // Col 1, Row 2: comboTax
        gbc.gridy = 2;
        gbc.gridheight = 1;
        scannerPanel.add(comboTax, gbc);

        // Col 2, Row 0-2: btnEnter
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.gridheight = 3;
        gbc.weightx = 0.0;
        scannerPanel.add(btnEnter, gbc);

        contentPanel.add(scannerPanel);
        add(contentPanel, BorderLayout.LINE_START);
    }

    public void setPriceText(String text) {
        priceLabel.setText(text != null ? text : "");
    }

    public String getPriceText() {
        return priceLabel.getText();
    }

    public void setPorText(String text) {
        porLabel.setText(text != null ? text : "");
    }

    public String getPorText() {
        return porLabel.getText();
    }

    public void clearInput() {
        setPriceText("");
        setPorText("");
    }

    public void requestKeyFactoryFocus() {
        keyFactory.requestFocus();
    }

    public void setKeyFactoryText(String text) {
        keyFactory.setText(text);
    }

    public void setOnKeyFactoryTyped(Consumer<Character> listener) {
        keyFactory.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                keyFactory.setText(null);
                if (listener != null) {
                    listener.accept(e.getKeyChar());
                }
            }
        });
    }

    public void setOnKeyFactoryAction(Runnable action) {
        keyFactory.addActionListener(e -> {
            if (action != null) {
                action.run();
            }
        });
    }

    public void setOnEnterAction(Runnable action) {
        btnEnter.addActionListener(e -> {
            if (action != null) {
                action.run();
            }
        });
    }

    public void setOnAddTaxAction(Runnable action) {
        chkAddTax.addActionListener(e -> {
            if (action != null) {
                action.run();
            }
        });
    }

    public boolean isTaxIncluded() {
        return chkAddTax.isSelected();
    }

    public void setTaxIncluded(boolean included) {
        chkAddTax.setSelected(included);
    }

    public void setTaxControlsVisible(boolean visible) {
        chkAddTax.setVisible(visible);
        comboTax.setVisible(visible);
    }

    @SuppressWarnings("unchecked")
    public void setTaxModel(ComboBoxModel<?> model) {
        comboTax.setModel((ComboBoxModel<Object>) model);
    }

    public JComboBox<Object> getTaxComboBox() {
        return comboTax;
    }

    public JCheckBox getAddTaxCheckBox() {
        return chkAddTax;
    }

    public JNumberKeys getNumberKeys() {
        return numberKeys;
    }

    public JLabel getPriceLabel() {
        return priceLabel;
    }

    public JLabel getPorLabel() {
        return porLabel;
    }

    public JTextField getKeyFactory() {
        return keyFactory;
    }

    public JButton getBtnEnter() {
        return btnEnter;
    }

    public void setAmountAtTop(boolean amountAtTop) {
        contentPanel.removeAll();
        if (amountAtTop) {
            contentPanel.add(scannerPanel);
            contentPanel.add(numberKeys);
        } else {
            contentPanel.add(numberKeys);
            contentPanel.add(scannerPanel);
        }
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    public void setDotIs00(boolean is00) {
        numberKeys.dotIs00(is00);
    }

    public void setMinusEnabled(boolean enabled) {
        numberKeys.setMinusEnabled(enabled);
    }

    public void setEqualsEnabled(boolean enabled) {
        numberKeys.setEqualsEnabled(enabled);
    }

    public void addNumberEventListener(JNumberEventListener listener) {
        numberKeys.addJNumberEventListener(listener);
    }
}
