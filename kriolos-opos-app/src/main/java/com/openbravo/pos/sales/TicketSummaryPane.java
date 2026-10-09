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

import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.ticket.TicketInfo;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

/**
 * Presentation panel displaying ticket identifier and summary totals (Subtotal, Tax, Total).
 * Designed in pure Java Swing with Look &amp; Feel agnostic tokens and zero NetBeans Matisse form dependencies.
 */
public class TicketSummaryPane extends JPanel {

    private final JLabel ticketIdLabel;
    private final JLabel subtotalHeaderLabel;
    private final JLabel taxHeaderLabel;
    private final JLabel totalHeaderLabel;
    private final JLabel subtotalValueLabel;
    private final JLabel taxValueLabel;
    private final JLabel totalValueLabel;
    private final JPanel totalsPanel;

    public TicketSummaryPane() {
        super(new BorderLayout());
        setOpaque(false);

        Font baseFont = UIManager.getFont("Label.font");
        if (baseFont == null) {
            baseFont = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
        }

        Font idFont = baseFont.deriveFont(Font.BOLD, 12f);
        Font headerFont = baseFont.deriveFont(Font.BOLD, 14f);
        Font valueFont = baseFont.deriveFont(Font.PLAIN, 18f);
        Font totalValueFont = baseFont.deriveFont(Font.BOLD, 18f);

        // Leading spacing strut
        add(new Box.Filler(new Dimension(5, 0), new Dimension(5, 0), new Dimension(5, Short.MAX_VALUE)), BorderLayout.LINE_START);

        // Ticket ID label
        ticketIdLabel = new JLabel("ID");
        ticketIdLabel.setFont(idFont);
        ticketIdLabel.setHorizontalAlignment(SwingConstants.LEFT);
        ticketIdLabel.setVerticalAlignment(SwingConstants.BOTTOM);
        ticketIdLabel.setOpaque(false);
        ticketIdLabel.setPreferredSize(new Dimension(300, 40));
        ticketIdLabel.setRequestFocusEnabled(false);
        add(ticketIdLabel, BorderLayout.CENTER);

        // Totals Grid: 2 rows x 3 columns
        totalsPanel = new JPanel(new GridLayout(2, 3, 4, 0));
        totalsPanel.setOpaque(false);
        totalsPanel.setPreferredSize(new Dimension(450, 60));

        subtotalHeaderLabel = new JLabel(AppLocal.getIntString("label.subtotalcash"));
        subtotalHeaderLabel.setFont(headerFont);
        subtotalHeaderLabel.setHorizontalAlignment(SwingConstants.CENTER);
        totalsPanel.add(subtotalHeaderLabel);

        taxHeaderLabel = new JLabel(AppLocal.getIntString("label.taxcash"));
        taxHeaderLabel.setFont(headerFont);
        taxHeaderLabel.setHorizontalAlignment(SwingConstants.CENTER);
        totalsPanel.add(taxHeaderLabel);

        totalHeaderLabel = new JLabel(AppLocal.getIntString("label.totalcash"));
        totalHeaderLabel.setFont(headerFont);
        totalHeaderLabel.setHorizontalAlignment(SwingConstants.CENTER);
        totalsPanel.add(totalHeaderLabel);

        Color borderColor = UIManager.getColor("Separator.foreground");
        if (borderColor == null) {
            borderColor = UIManager.getColor("Component.borderColor");
        }
        if (borderColor == null) {
            borderColor = new Color(153, 153, 153);
        }
        Border valueBorder = BorderFactory.createCompoundBorder(
                new LineBorder(borderColor, 1, true),
                BorderFactory.createEmptyBorder(2, 6, 2, 6)
        );

        subtotalValueLabel = new JLabel();
        subtotalValueLabel.setFont(valueFont);
        subtotalValueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        subtotalValueLabel.setBorder(valueBorder);
        subtotalValueLabel.setPreferredSize(new Dimension(125, 25));
        subtotalValueLabel.setRequestFocusEnabled(false);
        subtotalHeaderLabel.setLabelFor(subtotalValueLabel);
        totalsPanel.add(subtotalValueLabel);

        taxValueLabel = new JLabel();
        taxValueLabel.setFont(valueFont);
        taxValueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        taxValueLabel.setBorder(valueBorder);
        taxValueLabel.setPreferredSize(new Dimension(125, 25));
        taxValueLabel.setRequestFocusEnabled(false);
        taxHeaderLabel.setLabelFor(taxValueLabel);
        totalsPanel.add(taxValueLabel);

        totalValueLabel = new JLabel();
        totalValueLabel.setFont(totalValueFont);
        totalValueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        totalValueLabel.setBorder(valueBorder);
        totalValueLabel.setPreferredSize(new Dimension(125, 25));
        totalValueLabel.setRequestFocusEnabled(false);
        totalHeaderLabel.setLabelFor(totalValueLabel);
        totalsPanel.add(totalValueLabel);

        add(totalsPanel, BorderLayout.LINE_END);
    }

    /**
     * Updates the displayed ticket identifier or name.
     *
     * @param name ticket identifier or descriptive text
     */
    public void setTicketName(String name) {
        ticketIdLabel.setText(name);
    }

    /**
     * Retrieves the current ticket identifier text.
     */
    public String getTicketName() {
        return ticketIdLabel.getText();
    }

    /**
     * Updates the three summary total labels with pre-formatted currency amounts.
     *
     * @param subtotal formatted subtotal amount or null to clear
     * @param tax formatted tax amount or null to clear
     * @param total formatted grand total amount or null to clear
     */
    public void updateTotals(String subtotal, String tax, String total) {
        subtotalValueLabel.setText(subtotal);
        taxValueLabel.setText(tax);
        totalValueLabel.setText(total);
        repaint();
    }

    /**
     * Automatically updates summary totals from the given ticket aggregate.
     * Clears amounts if ticket is null or has zero line items.
     *
     * @param ticket active ticket info
     */
    public void updateTotals(TicketInfo ticket) {
        if (ticket == null || ticket.getLinesCount() == 0) {
            updateTotals(null, null, null);
        } else {
            updateTotals(ticket.printSubTotal(), ticket.printTax(), ticket.printTotal());
        }
    }

    /**
     * Resets ticket identifier and all total displays to empty state.
     */
    public void clear() {
        setTicketName(null);
        updateTotals(null, null, null);
    }

    public JLabel getTicketIdLabel() {
        return ticketIdLabel;
    }

    public JLabel getSubtotalValueLabel() {
        return subtotalValueLabel;
    }

    public JLabel getTaxValueLabel() {
        return taxValueLabel;
    }

    public JLabel getTotalValueLabel() {
        return totalValueLabel;
    }

    public String getSubtotalText() {
        return subtotalValueLabel.getText();
    }

    public String getTaxText() {
        return taxValueLabel.getText();
    }

    public String getTotalText() {
        return totalValueLabel.getText();
    }
}
