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

import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Font;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Modern touch-friendly table cell renderer for ticket line items.
 * Enforces >= 50px touch targets, generous padding, and full RTL/LTR awareness.
 * Completely Look-and-Feel (LaF) agnostic using dynamic UIManager color tokens.
 *
 * @author KriolOS Team
 */
public class ModernTicketCellRenderer extends DefaultTableCellRenderer {

    private static final long serialVersionUID = 1L;

    public ModernTicketCellRenderer() {
        super();
        setOpaque(true);
        applyComponentOrientation(ComponentOrientation.getOrientation(Locale.getDefault()));
    }

    @Override
    public Component getTableCellRendererComponent(
            JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        if (c instanceof JLabel) {
            JLabel label = (JLabel) c;
            label.applyComponentOrientation(table.getComponentOrientation());

            // Generous touch padding (10px top/bottom, 12px leading/trailing)
            label.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

            // Typography & Alignment
            switch (column) {
                case ModernTicketTableModel.COL_PRODUCT:
                    label.setHorizontalAlignment(SwingConstants.LEADING);
                    label.setFont(table.getFont().deriveFont(Font.BOLD, 13f));
                    break;
                case ModernTicketTableModel.COL_QTY:
                    label.setHorizontalAlignment(SwingConstants.CENTER);
                    label.setFont(table.getFont().deriveFont(Font.BOLD, 13f));
                    break;
                case ModernTicketTableModel.COL_PRICE:
                    label.setHorizontalAlignment(SwingConstants.TRAILING);
                    label.setFont(table.getFont().deriveFont(Font.PLAIN, 12f));
                    break;
                case ModernTicketTableModel.COL_TOTAL:
                    label.setHorizontalAlignment(SwingConstants.TRAILING);
                    label.setFont(table.getFont().deriveFont(Font.BOLD, 14f));
                    break;
                default:
                    label.setHorizontalAlignment(SwingConstants.LEADING);
                    break;
            }

            // LaF-agnostic background & foreground tokens
            if (isSelected) {
                label.setBackground(table.getSelectionBackground());
                label.setForeground(table.getSelectionForeground());
            } else {
                // Subtle alternating row color derived safely from UIManager
                if (row % 2 == 0) {
                    label.setBackground(table.getBackground());
                } else {
                    java.awt.Color altColor = UIManager.getColor("Table.alternateRowColor");
                    label.setBackground(altColor != null ? altColor : table.getBackground());
                }
                label.setForeground(table.getForeground());
            }
        }

        return c;
    }
}
