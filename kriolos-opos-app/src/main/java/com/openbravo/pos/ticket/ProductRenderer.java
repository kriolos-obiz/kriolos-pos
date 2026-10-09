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

package com.openbravo.pos.ticket;

import com.openbravo.format.Formats;
import com.openbravo.pos.resources.ImageResources;
import com.openbravo.pos.util.ThumbNailBuilder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

/**
 * Modern, touch-friendly product list cell renderer.
 * <p>
 * Displays product thumbnail, bold name, reference/barcode metadata,
 * and highlighted selling price with Look-and-Feel aware styling.
 * </p>
 *
 * @author KriolOS
 */
public class ProductRenderer extends JPanel implements ListCellRenderer<Object> {

    private static final long serialVersionUID = 1L;
    private static final int THUMB_SIZE = 36;
    private static final ThumbNailBuilder THUMB_NAIL = new ThumbNailBuilder(THUMB_SIZE, THUMB_SIZE);

    private transient final Icon defaultIcon;
    private final JLabel lblIcon = new JLabel();
    private final JLabel lblName = new JLabel();
    private final JLabel lblRef = new JLabel();
    private final JLabel lblPrice = new JLabel();
    private final JPanel pnlCenter = new JPanel();

    public ProductRenderer() {
        super(new BorderLayout(10, 0));
        setOpaque(true);

        defaultIcon = ImageResources.ICON_PACKAGE.getIcon(THUMB_SIZE, THUMB_SIZE);

        lblIcon.setPreferredSize(new Dimension(THUMB_SIZE, THUMB_SIZE));
        lblIcon.setMinimumSize(new Dimension(THUMB_SIZE, THUMB_SIZE));
        lblIcon.setHorizontalAlignment(SwingConstants.CENTER);
        lblIcon.setVerticalAlignment(SwingConstants.CENTER);

        pnlCenter.setLayout(new GridLayout(2, 1, 0, 2));
        pnlCenter.setOpaque(false);

        pnlCenter.add(lblName);
        pnlCenter.add(lblRef);

        lblPrice.setHorizontalAlignment(SwingConstants.TRAILING);
        lblPrice.setVerticalAlignment(SwingConstants.CENTER);

        add(lblIcon, BorderLayout.LINE_START);
        add(pnlCenter, BorderLayout.CENTER);
        add(lblPrice, BorderLayout.LINE_END);
    }

    @Override
    public Component getListCellRendererComponent(
            JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

        applyComponentOrientation(list.getComponentOrientation());

        Font baseFont = list.getFont() != null ? list.getFont() : new Font("Arial", Font.PLAIN, 12);
        lblName.setFont(baseFont.deriveFont(Font.BOLD, 13f));
        lblRef.setFont(baseFont.deriveFont(Font.PLAIN, 11f));
        lblPrice.setFont(baseFont.deriveFont(Font.BOLD, 13f));

        if (value instanceof ProductInfoExt prod) {
            lblName.setText(prod.getName() != null ? prod.getName() : "");

            String ref = prod.getReference();
            String code = prod.getCode();
            if (code != null && !code.isBlank() && !code.equals(ref)) {
                lblRef.setText("Ref: " + (ref != null ? ref : "") + "  •  " + code);
            } else if (ref != null && !ref.isBlank()) {
                lblRef.setText("Ref: " + ref);
            } else {
                lblRef.setText("");
            }

            lblPrice.setText(Formats.CURRENCY.formatValue(prod.getPriceSell()));

            setToolTipText(prod.getReference() + " | " + prod.getName() + " = " + Formats.CURRENCY.formatValue(prod.getPriceSell()));

            if (prod.getImage() != null) {
                Image img = THUMB_NAIL.getThumbNail(prod.getImage());
                lblIcon.setIcon(img != null ? new ImageIcon(img) : defaultIcon);
            } else {
                lblIcon.setIcon(defaultIcon);
            }

            if (isSelected) {
                setBackground(list.getSelectionBackground());
                lblName.setForeground(list.getSelectionForeground());
                lblRef.setForeground(list.getSelectionForeground());
                lblPrice.setForeground(list.getSelectionForeground());
            } else {
                Color alt = UIManager.getColor("List.alternateRowColor");
                if (alt == null) {
                    alt = UIManager.getColor("Table.alternateRowColor");
                }
                Color bg = (index % 2 == 1 && alt != null) ? alt : list.getBackground();
                setBackground(bg != null ? bg : Color.WHITE);

                lblName.setForeground(list.getForeground());

                Color muted = UIManager.getColor("Label.disabledForeground");
                lblRef.setForeground(muted != null ? muted : new Color(128, 128, 128));

                Color priceColor = UIManager.getColor("Label.successForeground");
                if (priceColor == null) {
                    priceColor = new Color(34, 139, 34);
                }
                lblPrice.setForeground(priceColor);
            }

            Color sepColor = UIManager.getColor("Component.borderColor");
            if (sepColor == null) {
                sepColor = UIManager.getColor("Separator.foreground");
            }
            if (sepColor == null) {
                sepColor = new Color(225, 225, 225);
            }
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, sepColor),
                    BorderFactory.createEmptyBorder(6, 10, 6, 12)
            ));
        } else {
            lblName.setText(value != null ? value.toString() : "");
            lblRef.setText("");
            lblPrice.setText("");
            lblIcon.setIcon(null);
            setToolTipText(null);

            if (isSelected) {
                setBackground(list.getSelectionBackground());
                lblName.setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                lblName.setForeground(list.getForeground());
            }
            setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 12));
        }

        return this;
    }
}
