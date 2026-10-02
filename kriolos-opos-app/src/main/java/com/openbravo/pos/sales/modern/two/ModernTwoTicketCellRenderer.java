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

package com.openbravo.pos.sales.modern.two;

import com.openbravo.format.Formats;
import com.openbravo.pos.ticket.TicketLineInfo;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;

/**
 * Modern digital receipt TableCellRenderer for {@link ModernTwoSalesLayout}.
 * <p>
 * Displays the line item as a clean digital receipt row:
 * <ul>
 *   <li><b>LINE_START</b>: Prominent bold Product Name on top, with Quantity, Unit Price,
 *       and modifiers/notes formatted on the line below.</li>
 *   <li><b>LINE_END</b>: Bold subtotal / line total amount.</li>
 * </ul>
 * Seamlessly supports RTL/LTR layouts and degrades gracefully across Look & Feels.
 *
 * @author KriolOS Team
 */
public class ModernTwoTicketCellRenderer extends JPanel implements TableCellRenderer {

    private static final long serialVersionUID = 1L;

    private final JLabel nameLabel;
    private final JLabel detailsLabel;
    private final JLabel totalLabel;
    private final JPanel textBlock;

    public ModernTwoTicketCellRenderer() {
        setLayout(new BorderLayout(12, 0));
        setOpaque(true);
        setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));

        // Center/Start: Text Block with 2 rows (Item name + Details)
        textBlock = new JPanel(new GridLayout(2, 1, 0, 2));
        textBlock.setOpaque(false);

        nameLabel = new JLabel();
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 14f));

        detailsLabel = new JLabel();
        detailsLabel.setFont(detailsLabel.getFont().deriveFont(Font.PLAIN, 12f));

        textBlock.add(nameLabel);
        textBlock.add(detailsLabel);
        add(textBlock, BorderLayout.CENTER);

        // End: Line Total Amount
        totalLabel = new JLabel();
        totalLabel.setFont(totalLabel.getFont().deriveFont(Font.BOLD, 15f));
        totalLabel.setHorizontalAlignment(SwingConstants.TRAILING);
        add(totalLabel, BorderLayout.LINE_END);

        applyComponentOrientation(ComponentOrientation.getOrientation(Locale.getDefault()));
        textBlock.applyComponentOrientation(ComponentOrientation.getOrientation(Locale.getDefault()));
    }

    @Override
    public Component getTableCellRendererComponent(
            JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

        // Synchronize component orientation with table / locale
        ComponentOrientation orientation = ComponentOrientation.getOrientation(Locale.getDefault());
        applyComponentOrientation(orientation);
        textBlock.applyComponentOrientation(orientation);

        if (value instanceof TicketLineInfo) {
            TicketLineInfo line = (TicketLineInfo) value;

            nameLabel.setText(line.printName());

            // Build subtitle: e.g. "2 × 500 CVE" or "2 × $5.00 (+Tax)"
            double multiply = line.getMultiply();
            String qtyStr = Formats.DOUBLE.formatValue(multiply);
            String priceStr = line.printPrice();
            StringBuilder details = new StringBuilder();
            details.append(qtyStr).append(" \u00d7 ").append(priceStr);

            // Append product property or notes if available
            String notes = line.getProperty("notes");
            if (notes != null && !notes.isBlank()) {
                details.append(" \u2022 ").append(notes);
            }
            detailsLabel.setText(details.toString());

            totalLabel.setText(line.printSubValue());
        } else {
            nameLabel.setText(value != null ? value.toString() : "");
            detailsLabel.setText("");
            totalLabel.setText("");
        }

        // Selection & Background styling (LaF-agnostic)
        if (isSelected) {
            Color selBg = table.getSelectionBackground();
            Color selFg = table.getSelectionForeground();
            setBackground(selBg != null ? selBg : UIManager.getColor("Table.selectionBackground"));
            nameLabel.setForeground(selFg != null ? selFg : UIManager.getColor("Table.selectionForeground"));
            detailsLabel.setForeground(selFg != null ? selFg : UIManager.getColor("Table.selectionForeground"));
            totalLabel.setForeground(selFg != null ? selFg : UIManager.getColor("Table.selectionForeground"));
        } else {
            Color rowBg = (row % 2 == 0)
                    ? table.getBackground()
                    : UIManager.getColor("Table.alternateRowColor");
            if (rowBg == null) {
                rowBg = table.getBackground();
            }
            setBackground(rowBg);

            Color textFg = table.getForeground();
            if (textFg == null) {
                textFg = UIManager.getColor("Table.foreground");
            }
            nameLabel.setForeground(textFg);
            totalLabel.setForeground(textFg);

            // Subdued color for details/quantity line
            Color secondaryFg = UIManager.getColor("Label.disabledForeground");
            if (secondaryFg == null) {
                secondaryFg = UIManager.getColor("textInactiveText");
            }
            detailsLabel.setForeground(secondaryFg != null ? secondaryFg : Color.GRAY);
        }

        return this;
    }
}
