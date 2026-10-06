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

package com.openbravo.pos.suppliers;

import com.openbravo.pos.businesspartner.BusinessPartner;
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
 * Modern touch-friendly list cell renderer for Supplier items.
 *
 * @author Jack Gerrard, KriolOS Team
 */
public class SupplierRenderer extends JPanel implements ListCellRenderer<Object> {

    private static final long serialVersionUID = 1L;
    private static final int THUMB_SIZE = 36;
    private static final ThumbNailBuilder THUMB_BUILDER = new ThumbNailBuilder(THUMB_SIZE, THUMB_SIZE);

    private transient final Icon defaultIcon;
    private final JLabel lblIcon = new JLabel();
    private final JLabel lblName = new JLabel();
    private final JLabel lblDetails = new JLabel();
    private final JLabel lblExtra = new JLabel();
    private final JPanel pnlCenter = new JPanel();

    public SupplierRenderer() {
        super(new BorderLayout(10, 0));
        setOpaque(true);

        defaultIcon = ImageResources.ICON_SUPPLIER.getIcon(THUMB_SIZE, THUMB_SIZE);

        lblIcon.setPreferredSize(new Dimension(THUMB_SIZE, THUMB_SIZE));
        lblIcon.setMinimumSize(new Dimension(THUMB_SIZE, THUMB_SIZE));
        lblIcon.setHorizontalAlignment(SwingConstants.CENTER);
        lblIcon.setVerticalAlignment(SwingConstants.CENTER);

        pnlCenter.setLayout(new GridLayout(2, 1, 0, 2));
        pnlCenter.setOpaque(false);

        pnlCenter.add(lblName);
        pnlCenter.add(lblDetails);

        lblExtra.setHorizontalAlignment(SwingConstants.TRAILING);
        lblExtra.setVerticalAlignment(SwingConstants.CENTER);

        add(lblIcon, BorderLayout.LINE_START);
        add(pnlCenter, BorderLayout.CENTER);
        add(lblExtra, BorderLayout.LINE_END);
    }

    @Override
    public Component getListCellRendererComponent(
            JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

        applyComponentOrientation(list.getComponentOrientation());

        Font baseFont = list.getFont() != null ? list.getFont() : new Font("Arial", Font.PLAIN, 12);
        lblName.setFont(baseFont.deriveFont(Font.BOLD, 13f));
        lblDetails.setFont(baseFont.deriveFont(Font.PLAIN, 11f));
        lblExtra.setFont(baseFont.deriveFont(Font.BOLD, 12f));

        if (value instanceof BusinessPartner bp) {
            lblName.setText(bp.getName() != null ? bp.getName() : "");

            StringBuilder details = new StringBuilder();
            if (bp.getTaxid() != null && !bp.getTaxid().isBlank()) {
                details.append("Tax ID: ").append(bp.getTaxid());
            }
            if (bp.getPhone() != null && !bp.getPhone().isBlank()) {
                if (!details.isEmpty()) {
                    details.append("  •  ");
                }
                details.append(bp.getPhone());
            } else if (bp.getEmail() != null && !bp.getEmail().isBlank()) {
                if (!details.isEmpty()) {
                    details.append("  •  ");
                }
                details.append(bp.getEmail());
            }
            lblDetails.setText(details.toString());

            if (bp.getSearchkey() != null && !bp.getSearchkey().isBlank()) {
                lblExtra.setText("[" + bp.getSearchkey() + "]");
                if (!isSelected) {
                    Color muted = UIManager.getColor("Label.disabledForeground");
                    lblExtra.setForeground(muted != null ? muted : new Color(128, 128, 128));
                }
            } else {
                lblExtra.setText("");
            }

            if (bp.getImage() != null) {
                Image img = THUMB_BUILDER.getThumbNail(bp.getImage());
                lblIcon.setIcon(img != null ? new ImageIcon(img) : defaultIcon);
            } else {
                lblIcon.setIcon(defaultIcon);
            }

            setToolTipText(bp.getName() + (bp.getTaxid() != null ? " (" + bp.getTaxid() + ")" : ""));

            if (isSelected) {
                setBackground(list.getSelectionBackground());
                lblName.setForeground(list.getSelectionForeground());
                lblDetails.setForeground(list.getSelectionForeground());
                lblExtra.setForeground(list.getSelectionForeground());
            } else {
                Color alt = UIManager.getColor("List.alternateRowColor");
                if (alt == null) {
                    alt = UIManager.getColor("Table.alternateRowColor");
                }
                Color bg = (index % 2 == 1 && alt != null) ? alt : list.getBackground();
                setBackground(bg != null ? bg : Color.WHITE);

                lblName.setForeground(list.getForeground());

                Color muted = UIManager.getColor("Label.disabledForeground");
                lblDetails.setForeground(muted != null ? muted : new Color(128, 128, 128));
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
            lblDetails.setText("");
            lblExtra.setText("");
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
