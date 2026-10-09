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

import com.openbravo.pos.resources.ImageResources;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JList;
import javax.swing.UIManager;

/**
 * Modern touch-friendly list cell renderer for Ticket search items.
 *
 * @author Mikel Irurita, KriolOS Team
 */
public class FindTicketsRenderer extends DefaultListCellRenderer {

    private static final long serialVersionUID = 1L;

    public static final int RECEIPT_NORMAL = 0;
    public static final int RECEIPT_REFUND = 1;

    private final Icon icoTicketNormal;
    private final Icon icoTicketRefund;
    private final Icon icoTicketRefunded;

    public FindTicketsRenderer() {
        this.icoTicketNormal = ImageResources.getIcon("com/openbravo/images/pay.png");
        this.icoTicketRefund = ImageResources.getIcon("com/openbravo/images/refundit.png");
        this.icoTicketRefunded = ImageResources.getIcon("com/openbravo/images/cancel.png");
    }

    @Override
    public Component getListCellRendererComponent(
            JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

        super.getListCellRendererComponent(list, null, index, isSelected, cellHasFocus);

        applyComponentOrientation(list.getComponentOrientation());

        Font baseFont = list.getFont() != null ? list.getFont() : new Font("Arial", Font.PLAIN, 12);
        setFont(baseFont);

        if (value instanceof FindTicketsInfo info) {
            int ticketType = info.getTicketType();
            int ticketStatus = info.getTicketStatus();

            setText("<html><table cellpadding='1' cellspacing='0' style='font-family:sans-serif;'>" + info.toString() + "</table></html>");

            if (ticketType == RECEIPT_REFUND) {
                setIcon(icoTicketRefund);
            } else if (ticketType == RECEIPT_NORMAL && ticketStatus > 0) {
                setIcon(icoTicketRefunded);
            } else {
                setIcon(icoTicketNormal);
            }

            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                Color alt = UIManager.getColor("List.alternateRowColor");
                if (alt == null) {
                    alt = UIManager.getColor("Table.alternateRowColor");
                }
                Color bg = (index % 2 == 1 && alt != null) ? alt : list.getBackground();
                setBackground(bg != null ? bg : Color.WHITE);
                setForeground(list.getForeground());
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
        }

        return this;
    }
}
