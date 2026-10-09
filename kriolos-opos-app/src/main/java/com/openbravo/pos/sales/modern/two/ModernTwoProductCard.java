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
package com.openbravo.pos.sales.modern.two;

import com.openbravo.format.Formats;
import com.openbravo.pos.ticket.ProductInfoExt;
import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

/**
 * Touch-optimized modern product card for {@link ModernTwoCatalogPane}.
 * Displays product name and formatted price in a clean, thumb-friendly card.
 *
 * @author KriolOS Team
 */
public class ModernTwoProductCard extends JButton {

    private static final long serialVersionUID = 1L;
    private static final Dimension CARD_SIZE = new Dimension(150, 95);

    private final ProductInfoExt product;

    public ModernTwoProductCard(ProductInfoExt product, ActionListener onSelect) {
        this.product = product;

        setLayout(new BorderLayout(4, 4));
        setPreferredSize(CARD_SIZE);
        setMinimumSize(CARD_SIZE);
        setFocusPainted(false);
        setMargin(new Insets(8, 8, 8, 8));

        // Rectangular card styling
        putClientProperty("JButton.buttonType", null);
        putClientProperty("JComponent.roundRect", false);
        
        java.awt.Color borderCol = UIManager.getColor("Component.borderColor");
        if (borderCol == null) {
            borderCol = new java.awt.Color(210, 215, 220);
        }
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderCol, 1),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        setName("kriolos:sales:modern-two:product:" + (product != null ? product.getID() : "unknown"));

        // Product Name (top/center, multi-line HTML wrap)
        String name = product != null ? product.getName() : "";
        JLabel nameLabel = new JLabel("<html><center><b>" + escapeHtml(name) + "</b></center></html>");
        nameLabel.setHorizontalAlignment(SwingConstants.CENTER);
        nameLabel.setVerticalAlignment(SwingConstants.CENTER);
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 13f));
        add(nameLabel, BorderLayout.CENTER);

        // Price (bottom)
        double price = product != null ? product.getPriceSell() : 0.0;
        String priceFormatted = Formats.CURRENCY.formatValue(price);
        JLabel priceLabel = new JLabel(priceFormatted);
        priceLabel.setHorizontalAlignment(SwingConstants.CENTER);
        priceLabel.setFont(priceLabel.getFont().deriveFont(Font.BOLD, 14f));
        priceLabel.setForeground(UIManager.getColor("Actions.Blue"));
        if (priceLabel.getForeground() == null) {
            priceLabel.setForeground(UIManager.getColor("Button.foreground"));
        }
        add(priceLabel, BorderLayout.PAGE_END);

        if (onSelect != null) {
            addActionListener(onSelect);
        }

        applyComponentOrientation(ComponentOrientation.getOrientation(Locale.getDefault()));
    }

    public ProductInfoExt getProduct() {
        return product;
    }

    private static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;");
    }
}
