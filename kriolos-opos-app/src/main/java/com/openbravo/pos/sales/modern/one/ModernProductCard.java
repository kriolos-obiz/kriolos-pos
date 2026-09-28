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

package com.openbravo.pos.sales.modern.one;

import com.openbravo.format.Formats;
import com.openbravo.pos.ticket.ProductInfoExt;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

/**
 * Touch-optimized modern product card for the catalog grid in {@link ModernOne}.
 * Fully Look and Feel (LaF) agnostic with FlatLaf client properties for smooth modern aesthetics.
 *
 * @author KriolOS Team
 */
public class ModernProductCard extends JButton {

    private static final long serialVersionUID = 1L;
    private static final Dimension CARD_SIZE = new Dimension(135, 115);

    private final ProductInfoExt product;

    public ModernProductCard(ProductInfoExt product, ActionListener listener) {
        this.product = product;
        initCard(listener);
    }

    private void initCard(ActionListener listener) {
        setPreferredSize(CARD_SIZE);
        setMinimumSize(CARD_SIZE);
        setMaximumSize(CARD_SIZE);
        setLayout(new BorderLayout(4, 4));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setFocusPainted(false);
        setContentAreaFilled(true);
        setOpaque(true);

        // Modern FlatLaf button enhancements (gracefully degrades on standard Swing LaFs)
        putClientProperty("JButton.buttonType", "roundRect");
        putClientProperty("JComponent.roundRect", true);
        putClientProperty("FlatLaf.styleClass", "card");
        setName("kriolos:sales:modern:product-card:" + (product != null ? product.getID() : "null"));

        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(6, 6, 6, 6),
                BorderFactory.createEmptyBorder(4, 4, 4, 4)
        ));

        // Product image / thumbnail if available
        if (product != null && product.getImage() != null) {
            Image img = product.getImage().getScaledInstance(48, 48, Image.SCALE_SMOOTH);
            JLabel imgLabel = new JLabel(new ImageIcon(img), SwingConstants.CENTER);
            imgLabel.setOpaque(false);
            add(imgLabel, BorderLayout.CENTER);
        } else {
            // Product Name Label in Center (HTML multi-line wrap, clean typography)
            String name = (product != null && product.getName() != null) ? product.getName() : "";
            JLabel nameLabel = new JLabel("<html><center>" + escapeHtml(name) + "</center></html>", SwingConstants.CENTER);
            nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 12f));
            nameLabel.setOpaque(false);
            add(nameLabel, BorderLayout.CENTER);
        }

        // Bottom Price Pill Bar
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);

        double price = (product != null) ? product.getPriceSell() : 0.0;
        JLabel priceLabel = new JLabel(Formats.CURRENCY.formatValue(price), SwingConstants.CENTER);
        priceLabel.setFont(priceLabel.getFont().deriveFont(Font.BOLD, 13f));
        priceLabel.setOpaque(false);
        footer.add(priceLabel, BorderLayout.CENTER);

        add(footer, BorderLayout.PAGE_END);

        if (listener != null) {
            addActionListener(listener);
        }
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
