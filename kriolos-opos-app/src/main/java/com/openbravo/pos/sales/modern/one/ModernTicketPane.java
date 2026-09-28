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
import com.openbravo.pos.customers.CustomerInfoExt;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.ticket.TicketInfo;
import com.openbravo.pos.ticket.TicketLineInfo;
import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

/**
 * Modern touch-friendly Ticket Pane positioned on the side (using LINE_START).
 * Encapsulates the flat, borderless line items JTable (>= 50px row height),
 * real-time financial totals, customer selection pill, and the massive Pay button at PAGE_END.
 *
 * @author KriolOS Team
 */
public class ModernTicketPane extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final int MIN_ROW_HEIGHT = 52;

    private final ModernTicketTableModel tableModel;
    private final JTable table;
    private final JLabel ticketHeaderLabel;
    private final JButton customerButton;
    private final JButton holdButton;
    private final JButton parkedListButton;
    private final JButton clearTicketButton;

    // Totals labels
    private final JLabel subtotalValueLabel;
    private final JLabel taxValueLabel;
    private final JLabel totalValueLabel;

    // Line item action buttons
    private final JButton btnQtyMinus;
    private final JButton btnQtyPlus;
    private final JButton btnDeleteLine;
    private final JButton btnEditLine;

    // Massive Pay button
    private final JButton btnPay;

    // Listener callbacks
    private Runnable onPayClicked;
    private Runnable onCustomerClicked;
    private Runnable onClearTicketClicked;
    private Runnable onHoldClicked;
    private Runnable onParkedListClicked;
    private java.util.function.Consumer<Double> onQtyAdjusted;
    private Runnable onDeleteLineClicked;
    private Runnable onEditLineClicked;

    public ModernTicketPane() {
        setLayout(new BorderLayout(0, 8));
        setOpaque(true);
        setBackground(UIManager.getColor("Panel.background"));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setPreferredSize(new Dimension(380, 0));
        setMinimumSize(new Dimension(320, 0));
        setName("kriolos:sales:modern:ticket-pane");

        // ----------------------------------------------------
        // 1. TOP HEADER: Ticket ID, Customer Pill, Clear Button
        // ----------------------------------------------------
        JPanel headerPanel = new JPanel(new BorderLayout(8, 0));
        headerPanel.setOpaque(false);

        ticketHeaderLabel = new JLabel("Order #1");
        ticketHeaderLabel.setFont(ticketHeaderLabel.getFont().deriveFont(Font.BOLD, 16f));
        ticketHeaderLabel.setHorizontalAlignment(SwingConstants.LEADING);
        headerPanel.add(ticketHeaderLabel, BorderLayout.CENTER);

        JPanel headerActions = new JPanel(new FlowLayout(FlowLayout.TRAILING, 4, 0));
        headerActions.setOpaque(false);

        customerButton = new JButton(AppLocal.getIntString("label.customer"));
        customerButton.setFocusPainted(false);
        customerButton.setMargin(new Insets(4, 10, 4, 10));
        customerButton.putClientProperty("JButton.buttonType", "roundRect");
        customerButton.setName("kriolos:sales:modern:customer-btn");
        customerButton.addActionListener(e -> {
            if (onCustomerClicked != null) onCustomerClicked.run();
        });
        headerActions.add(customerButton);

        holdButton = new JButton("Hold");
        holdButton.setFocusPainted(false);
        holdButton.setMargin(new Insets(4, 10, 4, 10));
        holdButton.putClientProperty("JButton.buttonType", "roundRect");
        holdButton.setName("kriolos:sales:modern:hold-btn");
        holdButton.addActionListener(e -> {
            if (onHoldClicked != null) onHoldClicked.run();
        });
        headerActions.add(holdButton);

        parkedListButton = new JButton("Orders");
        parkedListButton.setFocusPainted(false);
        parkedListButton.setMargin(new Insets(4, 10, 4, 10));
        parkedListButton.putClientProperty("JButton.buttonType", "roundRect");
        parkedListButton.setName("kriolos:sales:modern:parked-btn");
        parkedListButton.addActionListener(e -> {
            if (onParkedListClicked != null) onParkedListClicked.run();
        });
        headerActions.add(parkedListButton);

        clearTicketButton = new JButton("Clear");
        clearTicketButton.setFocusPainted(false);
        clearTicketButton.setMargin(new Insets(4, 10, 4, 10));
        clearTicketButton.putClientProperty("JButton.buttonType", "roundRect");
        clearTicketButton.setName("kriolos:sales:modern:clear-btn");
        clearTicketButton.addActionListener(e -> {
            if (onClearTicketClicked != null) onClearTicketClicked.run();
        });
        headerActions.add(clearTicketButton);

        headerPanel.add(headerActions, BorderLayout.LINE_END);
        add(headerPanel, BorderLayout.PAGE_START);

        // ----------------------------------------------------
        // 2. CENTER: Flat, Borderless Line Items Table (>= 50px)
        // ----------------------------------------------------
        tableModel = new ModernTicketTableModel();
        table = new JTable(tableModel);
        table.setRowHeight(MIN_ROW_HEIGHT);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);
        table.setDefaultRenderer(Object.class, new ModernTicketCellRenderer());
        table.setName("kriolos:sales:modern:ticket-table");

        // Touch scrolling
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setPreferredSize(new Dimension(0, 36));
        table.getTableHeader().setFont(table.getTableHeader().getFont().deriveFont(Font.BOLD, 12f));

        // Column widths
        table.getColumnModel().getColumn(ModernTicketTableModel.COL_PRODUCT).setPreferredWidth(170);
        table.getColumnModel().getColumn(ModernTicketTableModel.COL_QTY).setPreferredWidth(45);
        table.getColumnModel().getColumn(ModernTicketTableModel.COL_PRICE).setPreferredWidth(65);
        table.getColumnModel().getColumn(ModernTicketTableModel.COL_TOTAL).setPreferredWidth(75);

        JScrollPane tableScrollPane = new JScrollPane(table);
        tableScrollPane.setBorder(BorderFactory.createEmptyBorder());
        tableScrollPane.setOpaque(false);
        tableScrollPane.getViewport().setOpaque(false);
        add(tableScrollPane, BorderLayout.CENTER);

        // ----------------------------------------------------
        // 3. BOTTOM: Actions, Summary Card, and Massive Pay Button
        // ----------------------------------------------------
        JPanel footerPanel = new JPanel();
        footerPanel.setLayout(new BoxLayout(footerPanel, BoxLayout.Y_AXIS));
        footerPanel.setOpaque(false);
        footerPanel.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));

        // Line Item Quick Controls (-1 / +1 / Edit / Delete) spanning full width
        JPanel lineActionsBar = new JPanel(new GridLayout(1, 4, 8, 0));
        lineActionsBar.setOpaque(false);
        lineActionsBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        lineActionsBar.setPreferredSize(new Dimension(0, 44));

        btnQtyMinus = createToolButton("-1", "kriolos:sales:modern:btn-minus");
        btnQtyMinus.addActionListener(e -> {
            if (onQtyAdjusted != null) onQtyAdjusted.accept(-1.0);
        });

        btnQtyPlus = createToolButton("+1", "kriolos:sales:modern:btn-plus");
        btnQtyPlus.addActionListener(e -> {
            if (onQtyAdjusted != null) onQtyAdjusted.accept(1.0);
        });

        btnEditLine = createToolButton(AppLocal.getIntString("button.edit"), "kriolos:sales:modern:btn-edit-line");
        btnEditLine.addActionListener(e -> {
            if (onEditLineClicked != null) onEditLineClicked.run();
        });

        btnDeleteLine = createToolButton(AppLocal.getIntString("button.delete"), "kriolos:sales:modern:btn-delete-line");
        btnDeleteLine.addActionListener(e -> {
            if (onDeleteLineClicked != null) onDeleteLineClicked.run();
        });

        lineActionsBar.add(btnQtyMinus);
        lineActionsBar.add(btnQtyPlus);
        lineActionsBar.add(btnEditLine);
        lineActionsBar.add(btnDeleteLine);
        footerPanel.add(lineActionsBar);
        footerPanel.add(Box.createVerticalStrut(10));

        // Financial Summary Box
        JPanel summaryCard = new JPanel(new GridLayout(3, 2, 8, 4));
        summaryCard.setOpaque(false);
        summaryCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 1, 0, UIManager.getColor("Separator.foreground") != null 
                        ? UIManager.getColor("Separator.foreground") : UIManager.getColor("Panel.background")),
                BorderFactory.createEmptyBorder(8, 4, 8, 4)
        ));

        JLabel subtotalLabel = new JLabel(AppLocal.getIntString("label.subtotalcash"));
        subtotalLabel.setHorizontalAlignment(SwingConstants.LEADING);
        subtotalValueLabel = new JLabel(Formats.CURRENCY.formatValue(0.0));
        subtotalValueLabel.setHorizontalAlignment(SwingConstants.TRAILING);

        JLabel taxLabel = new JLabel(AppLocal.getIntString("label.taxcash"));
        taxLabel.setHorizontalAlignment(SwingConstants.LEADING);
        taxValueLabel = new JLabel(Formats.CURRENCY.formatValue(0.0));
        taxValueLabel.setHorizontalAlignment(SwingConstants.TRAILING);

        JLabel totalLabel = new JLabel(AppLocal.getIntString("label.totalcash"));
        totalLabel.setFont(totalLabel.getFont().deriveFont(Font.BOLD, 17f));
        totalLabel.setHorizontalAlignment(SwingConstants.LEADING);
        totalValueLabel = new JLabel(Formats.CURRENCY.formatValue(0.0));
        totalValueLabel.setFont(totalValueLabel.getFont().deriveFont(Font.BOLD, 20f));
        totalValueLabel.setHorizontalAlignment(SwingConstants.TRAILING);

        summaryCard.add(subtotalLabel);
        summaryCard.add(subtotalValueLabel);
        summaryCard.add(taxLabel);
        summaryCard.add(taxValueLabel);
        summaryCard.add(totalLabel);
        summaryCard.add(totalValueLabel);

        footerPanel.add(summaryCard);
        footerPanel.add(Box.createVerticalStrut(10));

        // Massive Pay Button (full width, minimum 54px height)
        btnPay = new JButton(AppLocal.getIntString("button.pay") + "  " + Formats.CURRENCY.formatValue(0.0));
        btnPay.setFont(btnPay.getFont().deriveFont(Font.BOLD, 18f));
        btnPay.setPreferredSize(new Dimension(Integer.MAX_VALUE, 56));
        btnPay.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        btnPay.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnPay.setFocusPainted(false);
        btnPay.setName("kriolos:sales:modern:btn-pay");

        // FlatLaf accent pill styling
        btnPay.putClientProperty("JButton.buttonType", "roundRect");
        btnPay.putClientProperty("JComponent.roundRect", true);
        btnPay.putClientProperty("FlatLaf.styleClass", "accent");

        btnPay.addActionListener(e -> {
            if (onPayClicked != null) onPayClicked.run();
        });

        footerPanel.add(btnPay);
        add(footerPanel, BorderLayout.PAGE_END);

        // Apply RTL/LTR orientation dynamically
        applyComponentOrientation(ComponentOrientation.getOrientation(Locale.getDefault()));
        headerPanel.applyComponentOrientation(ComponentOrientation.getOrientation(Locale.getDefault()));
        headerActions.applyComponentOrientation(ComponentOrientation.getOrientation(Locale.getDefault()));
        summaryCard.applyComponentOrientation(ComponentOrientation.getOrientation(Locale.getDefault()));
        lineActionsBar.applyComponentOrientation(ComponentOrientation.getOrientation(Locale.getDefault()));
    }

    private JButton createToolButton(String text, String name) {
        JButton btn = new JButton(text);
        btn.setFont(btn.getFont().deriveFont(Font.BOLD, 12f));
        btn.setFocusPainted(false);
        btn.setMargin(new Insets(6, 12, 6, 12));
        btn.putClientProperty("JButton.buttonType", "roundRect");
        btn.setName(name);
        return btn;
    }

    public void updateTicketDisplay(TicketInfo ticket, CustomerInfoExt customer) {
        if (ticket == null) {
            tableModel.clear();
            ticketHeaderLabel.setText("New Order");
            subtotalValueLabel.setText(Formats.CURRENCY.formatValue(0.0));
            taxValueLabel.setText(Formats.CURRENCY.formatValue(0.0));
            totalValueLabel.setText(Formats.CURRENCY.formatValue(0.0));
            btnPay.setText(AppLocal.getIntString("button.pay") + "  " + Formats.CURRENCY.formatValue(0.0));
            customerButton.setText(AppLocal.getIntString("label.customer"));
            btnPay.setEnabled(false);
            holdButton.setEnabled(false);
            clearTicketButton.setEnabled(false);
            return;
        }

        // Header & Customer Info
        String ticketName = (ticket.getName() != null && !ticket.getName().isBlank()) 
                ? ticket.getName() : "Order #" + ticket.getPickupId();
        ticketHeaderLabel.setText(ticketName);

        if (customer != null && customer.getName() != null) {
            customerButton.setText(customer.getName());
        } else if (ticket.getCustomer() != null && ticket.getCustomer().getName() != null) {
            customerButton.setText(ticket.getCustomer().getName());
        } else {
            customerButton.setText(AppLocal.getIntString("label.customer"));
        }

        // Lines in table
        tableModel.setLines(ticket.getLines());

        // Financial totals
        double subtotal = ticket.getSubTotal();
        double taxes = ticket.getTax();
        double total = ticket.getTotal();

        subtotalValueLabel.setText(Formats.CURRENCY.formatValue(subtotal));
        taxValueLabel.setText(Formats.CURRENCY.formatValue(taxes));
        totalValueLabel.setText(Formats.CURRENCY.formatValue(total));

        btnPay.setText(AppLocal.getIntString("button.pay") + "  " + Formats.CURRENCY.formatValue(total));
        btnPay.setEnabled(ticket.getLinesCount() > 0 && total >= 0.0);
        holdButton.setEnabled(ticket.getLinesCount() > 0);
        clearTicketButton.setEnabled(ticket.getLinesCount() > 0);

        // Auto-scroll to selected or last line
        int lastRow = tableModel.getRowCount() - 1;
        if (lastRow >= 0 && table.getSelectedRow() < 0) {
            table.setRowSelectionInterval(lastRow, lastRow);
            table.scrollRectToVisible(table.getCellRect(lastRow, 0, true));
        }
    }

    public int getSelectedLineIndex() {
        return table.getSelectedRow();
    }

    public void setSelectedLineIndex(int index) {
        if (index >= 0 && index < tableModel.getRowCount()) {
            table.setRowSelectionInterval(index, index);
            table.scrollRectToVisible(table.getCellRect(index, 0, true));
        }
    }

    // Callbacks setters
    public void setOnPayClicked(Runnable r) { this.onPayClicked = r; }
    public void setOnCustomerClicked(Runnable r) { this.onCustomerClicked = r; }
    public void setOnClearTicketClicked(Runnable r) { this.onClearTicketClicked = r; }
    public void setOnHoldClicked(Runnable r) { this.onHoldClicked = r; }
    public void setOnParkedListClicked(Runnable r) { this.onParkedListClicked = r; }
    public void setParkedCount(int count) {
        parkedListButton.setText(count > 0 ? "Orders (" + count + ")" : "Orders");
    }
    public void setOnQtyAdjusted(java.util.function.Consumer<Double> c) { this.onQtyAdjusted = c; }
    public void setOnDeleteLineClicked(Runnable r) { this.onDeleteLineClicked = r; }
    public void setOnEditLineClicked(Runnable r) { this.onEditLineClicked = r; }
}
