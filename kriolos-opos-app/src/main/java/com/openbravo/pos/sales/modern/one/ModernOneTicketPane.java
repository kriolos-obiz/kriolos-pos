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
package com.openbravo.pos.sales.modern.one;

import com.openbravo.format.Formats;
import com.openbravo.pos.customers.CustomerInfoExt;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.sales.modern.POSKeyBinder;
import com.openbravo.pos.ticket.TicketInfo;
import com.openbravo.pos.ui.components.ButtonSize;
import com.openbravo.pos.ui.components.POSButtonFactory;
import com.openbravo.pos.ui.components.UnicodeIcon;
import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;
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
 * real-time financial totals, customer selection pill, and the massive Pay
 * button at PAGE_END.
 *
 * @author KriolOS Team
 */
public class ModernOneTicketPane extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final int MIN_ROW_HEIGHT = 52;

    private final ModernOneTicketTableModel tableModel;
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

    public ModernOneTicketPane() {
        setLayout(new BorderLayout(0, 8));
        setOpaque(true);
        setBackground(UIManager.getColor("Panel.background"));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setPreferredSize(new Dimension(380, 0));
        setMinimumSize(new Dimension(320, 0));
        setName("kriolos:sales:modern:ticket-pane");

        // ----------------------------------------------------
        // 1. TOP HEADER: Ticket ID + Parked Pill (Row 1) & Full-width Customer Bar (Row 2)
        // ----------------------------------------------------
        JPanel headerContainer = new JPanel(new BorderLayout(0, 6));
        headerContainer.setOpaque(false);

        JPanel titleRow = new JPanel(new BorderLayout(8, 0));
        titleRow.setOpaque(false);

        ticketHeaderLabel = new JLabel("Order #1");
        ticketHeaderLabel.setFont(ticketHeaderLabel.getFont().deriveFont(Font.BOLD, 16f));
        ticketHeaderLabel.setHorizontalAlignment(SwingConstants.LEADING);
        titleRow.add(ticketHeaderLabel, BorderLayout.CENTER);

        parkedListButton = POSButtonFactory.createActionButton(
                AppLocal.getIntString("button.parked"),
                UnicodeIcon.ORDERS,
                ButtonSize.MEDIUM,
                KeyEvent.VK_O,
                AppLocal.getIntString("button.parked.tooltip"),
                e -> {
                    if (onParkedListClicked != null) {
                        onParkedListClicked.run();
                    }
                });
        parkedListButton.setName("kriolos:sales:modern:parked-btn");
        titleRow.add(parkedListButton, BorderLayout.LINE_END);
        headerContainer.add(titleRow, BorderLayout.PAGE_START);

        customerButton = POSButtonFactory.createActionButton(
                AppLocal.getIntString("label.customer") + ": " + AppLocal.getIntString("label.guest"),
                UnicodeIcon.CUSTOMER,
                ButtonSize.LARGE,
                KeyEvent.VK_C,
                AppLocal.getIntString("label.customer"),
                e -> {
                    if (onCustomerClicked != null) {
                        onCustomerClicked.run();
                    }
                });
        customerButton.setPreferredSize(new Dimension(Integer.MAX_VALUE, ButtonSize.LARGE.getHeight()));
        customerButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, ButtonSize.LARGE.getHeight()));
        customerButton.setName("kriolos:sales:modern:customer-btn");
        headerContainer.add(customerButton, BorderLayout.PAGE_END);

        add(headerContainer, BorderLayout.PAGE_START);

        // ----------------------------------------------------
        // 2. CENTER: Flat, Borderless Line Items Table (>= 50px)
        // ----------------------------------------------------
        tableModel = new ModernOneTicketTableModel();
        table = new JTable(tableModel);
        table.setRowHeight(MIN_ROW_HEIGHT);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);
        table.setDefaultRenderer(Object.class, new ModernOneTicketCellRenderer());
        table.setName("kriolos:sales:modern:ticket-table");

        // Touch scrolling
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setPreferredSize(new Dimension(0, 36));
        table.getTableHeader().setFont(table.getTableHeader().getFont().deriveFont(Font.BOLD, 12f));

        // Column widths
        table.getColumnModel().getColumn(ModernOneTicketTableModel.COL_PRODUCT).setPreferredWidth(170);
        table.getColumnModel().getColumn(ModernOneTicketTableModel.COL_QTY).setPreferredWidth(45);
        table.getColumnModel().getColumn(ModernOneTicketTableModel.COL_PRICE).setPreferredWidth(65);
        table.getColumnModel().getColumn(ModernOneTicketTableModel.COL_TOTAL).setPreferredWidth(75);

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

        // Line Item Controls (-1 / +1 / Edit / Delete) spanning full width
        JPanel lineActionsBar = new JPanel(new GridLayout(1, 4, 6, 0));
        lineActionsBar.setOpaque(false);
        lineActionsBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, ButtonSize.EXTRA_LARGE.getHeight()));
        lineActionsBar.setPreferredSize(new Dimension(0, ButtonSize.EXTRA_LARGE.getHeight()));

        btnQtyMinus = POSButtonFactory.createActionButton(
                "-",
                "",
                ButtonSize.LARGE,
                KeyEvent.VK_MINUS,
                AppLocal.getIntString("button.qtyminus.tooltip"),
                e -> {
                    if (onQtyAdjusted != null) {
                        onQtyAdjusted.accept(-1.0);
                    }
                });
        btnQtyMinus.setName("kriolos:sales:modern:btn-minus");
        btnQtyMinus.setMargin(new Insets(0, 0, 0, 0));  // Remove padding to prevent "..." truncation
        POSKeyBinder.bindNumpadKey(btnQtyMinus, KeyEvent.VK_SUBTRACT, "kriolos:sales:modern:btn-minus");

        btnQtyPlus = POSButtonFactory.createActionButton(
                "+",
                "",
                ButtonSize.LARGE,
                KeyEvent.VK_PLUS,
                AppLocal.getIntString("button.qtyplus.tooltip"),
                e -> {
                    if (onQtyAdjusted != null) {
                        onQtyAdjusted.accept(1.0);
                    }
                });
        btnQtyPlus.setName("kriolos:sales:modern:btn-plus");
        btnQtyPlus.setMargin(new Insets(0, 0, 0, 0));  // Remove padding to prevent "..." truncation
        POSKeyBinder.bindNumpadKey(btnQtyPlus, KeyEvent.VK_ADD, "kriolos:sales:modern:btn-plus");

        btnEditLine = POSButtonFactory.createActionButton(
                AppLocal.getIntString("button.edit"),
                UnicodeIcon.EDIT,
                ButtonSize.LARGE,
                KeyEvent.VK_D,
                AppLocal.getIntString("button.editline.tooltip"),
                e -> {
                    if (onEditLineClicked != null) {
                        onEditLineClicked.run();
                    }
                });
        btnEditLine.setName("kriolos:sales:modern:btn-edit-line");
        btnEditLine.setMargin(new Insets(0, 0, 0, 0));

        // Uses a red/danger color as seen in the top right of the UI mockup
        btnDeleteLine = POSButtonFactory.createDangerButton(
                AppLocal.getIntString("button.delete"),
                UnicodeIcon.DELETE,
                ButtonSize.LARGE,
                KeyEvent.VK_R,
                AppLocal.getIntString("button.deleteline.tooltip"),
                e -> {
                    if (onDeleteLineClicked != null) {
                        onDeleteLineClicked.run();
                    }
                });
        btnDeleteLine.setName("kriolos:sales:modern:btn-delete-line");
        btnDeleteLine.setMargin(new Insets(0, 0, 0, 0));

        lineActionsBar.add(btnQtyMinus);
        lineActionsBar.add(btnQtyPlus);
        lineActionsBar.add(btnEditLine);
        lineActionsBar.add(btnDeleteLine);
        
        footerPanel.add(lineActionsBar);
        footerPanel.add(Box.createVerticalStrut(6));

        // Order Action Controls (Clear / Hold) spanning full width
        JPanel orderActionsBar = new JPanel(new GridLayout(1, 2, 8, 0));
        orderActionsBar.setOpaque(false);
        orderActionsBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, ButtonSize.LARGE.getHeight()));
        orderActionsBar.setPreferredSize(new Dimension(0, ButtonSize.LARGE.getHeight()));

        clearTicketButton = POSButtonFactory.createActionButton(
                AppLocal.getIntString("button.clean"),
                UnicodeIcon.CLEAR,
                ButtonSize.LARGE,
                KeyEvent.VK_L,
                AppLocal.getIntString("button.clear.tooltip"),
                e -> {
                    if (onClearTicketClicked != null) {
                        onClearTicketClicked.run();
                    }
                });
        clearTicketButton.setName("kriolos:sales:modern:clear-btn");

        holdButton = POSButtonFactory.createActionButton(
                AppLocal.getIntString("button.hold"),
                UnicodeIcon.HOLD,
                ButtonSize.LARGE,
                KeyEvent.VK_H,
                AppLocal.getIntString("button.hold.tooltip"),
                e -> {
                    if (onHoldClicked != null) {
                        onHoldClicked.run();
                    }
                });
        holdButton.setName("kriolos:sales:modern:hold-btn");

        orderActionsBar.add(clearTicketButton);
        orderActionsBar.add(holdButton);
        footerPanel.add(orderActionsBar);
        footerPanel.add(Box.createVerticalStrut(8));

        // Financial Summary Box
        JPanel summaryCard = new JPanel(new GridLayout(3, 2, 8, 4));
        summaryCard.setOpaque(false);
        summaryCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 1, 0, UIManager.getColor("Separator.foreground") != null
                        ? UIManager.getColor("Separator.foreground")
                        : UIManager.getColor("Panel.background")),
                BorderFactory.createEmptyBorder(8, 4, 8, 4)));

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
        footerPanel.add(Box.createVerticalStrut(8));

        // Massive Pay Button (full width, 56px height)
        btnPay = POSButtonFactory.createSuccessButton(
                AppLocal.getIntString("button.pay") + "  " + Formats.CURRENCY.formatValue(0.0),
                UnicodeIcon.PAY,
                ButtonSize.MASSIVE,
                KeyEvent.VK_P,
                AppLocal.getIntString("button.pay.tooltip"),
                e -> {
                    if (onPayClicked != null) {
                        onPayClicked.run();
                    }
                });
        btnPay.setPreferredSize(new Dimension(Integer.MAX_VALUE, ButtonSize.MASSIVE.getHeight()));
        btnPay.setMaximumSize(new Dimension(Integer.MAX_VALUE, ButtonSize.MASSIVE.getHeight()));
        btnPay.setName("kriolos:sales:modern:btn-pay");

        // Bind Numpad "Enter"
        POSKeyBinder.bindNumpadKey(btnPay, KeyEvent.VK_ENTER, "kriolos:sales:modern:btn-pay");

        footerPanel.add(btnPay);
        add(footerPanel, BorderLayout.PAGE_END);

        // Apply RTL/LTR orientation dynamically
        applyComponentOrientation(ComponentOrientation.getOrientation(Locale.getDefault()));
        headerContainer.applyComponentOrientation(ComponentOrientation.getOrientation(Locale.getDefault()));
        titleRow.applyComponentOrientation(ComponentOrientation.getOrientation(Locale.getDefault()));
        summaryCard.applyComponentOrientation(ComponentOrientation.getOrientation(Locale.getDefault()));
        lineActionsBar.applyComponentOrientation(ComponentOrientation.getOrientation(Locale.getDefault()));
        orderActionsBar.applyComponentOrientation(ComponentOrientation.getOrientation(Locale.getDefault()));

        revalidate();
        repaint();
    }

    public void updateTicketDisplay(TicketInfo ticket, CustomerInfoExt customer) {
        if (ticket == null) {
            tableModel.clear();
            ticketHeaderLabel.setText(AppLocal.getIntString("button.newticket"));
            subtotalValueLabel.setText(Formats.CURRENCY.formatValue(0.0));
            taxValueLabel.setText(Formats.CURRENCY.formatValue(0.0));
            totalValueLabel.setText(Formats.CURRENCY.formatValue(0.0));
            btnPay.setText(POSButtonFactory.formatButtonText(
                    AppLocal.getIntString("button.pay") + "  " + Formats.CURRENCY.formatValue(0.0), UnicodeIcon.PAY));
            customerButton.setText(
                    POSButtonFactory.formatButtonText(AppLocal.getIntString("label.customer") + ": " + AppLocal.getIntString("label.guest"), UnicodeIcon.CUSTOMER));
            customerButton.putClientProperty("FlatLaf.styleClass", null);
            btnPay.setEnabled(false);
            holdButton.setEnabled(false);
            clearTicketButton.setEnabled(false);
            btnQtyMinus.setEnabled(false);
            btnQtyPlus.setEnabled(false);
            btnDeleteLine.setEnabled(false);
            btnEditLine.setEnabled(false);

            return;
        }

        // Header & Customer Info
        String ticketName = (ticket.getName() != null && !ticket.getName().isBlank())
                ? ticket.getName()
                : AppLocal.getIntString("button.newticket") + " #" + ticket.getPickupId();
        ticketHeaderLabel.setText(ticketName);

        if (customer != null && customer.getName() != null) {
            customerButton.setText(POSButtonFactory.formatButtonText(AppLocal.getIntString("label.customer") + ": " + customer.getName(), UnicodeIcon.CUSTOMER));
            customerButton.putClientProperty("FlatLaf.styleClass", "accent");
        } else if (ticket.getCustomer() != null && ticket.getCustomer().getName() != null) {
            customerButton.setText(POSButtonFactory.formatButtonText(AppLocal.getIntString("label.customer") + ": " + ticket.getCustomer().getName(), UnicodeIcon.CUSTOMER));
            customerButton.putClientProperty("FlatLaf.styleClass", "accent");
        } else {
            customerButton.setText(
                    POSButtonFactory.formatButtonText(AppLocal.getIntString("label.customer") + ": " + AppLocal.getIntString("label.guest"), UnicodeIcon.CUSTOMER));
            customerButton.putClientProperty("FlatLaf.styleClass", null);
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

        btnPay.setText(POSButtonFactory.formatButtonText(
                AppLocal.getIntString("button.pay") + "  " + Formats.CURRENCY.formatValue(total), UnicodeIcon.PAY));

        //ENABLED TicketPane Buttons
        boolean hasLines = ticket.getLinesCount() > 0;
        btnPay.setEnabled(hasLines && total >= 0.0);
        holdButton.setEnabled(hasLines);
        clearTicketButton.setEnabled(hasLines);
        btnQtyMinus.setEnabled(hasLines);
        btnQtyPlus.setEnabled(hasLines);
        btnDeleteLine.setEnabled(hasLines);
        btnEditLine.setEnabled(hasLines);

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
    public void setOnPayClicked(Runnable r) {
        this.onPayClicked = r;
    }

    public void setOnCustomerClicked(Runnable r) {
        this.onCustomerClicked = r;
    }

    public void setOnClearTicketClicked(Runnable r) {
        this.onClearTicketClicked = r;
    }

    public void setOnHoldClicked(Runnable r) {
        this.onHoldClicked = r;
    }

    public void setOnParkedListClicked(Runnable r) {
        this.onParkedListClicked = r;
    }

    public void setParkedCount(int count) {
        parkedListButton.setText(count > 0
                ? UnicodeIcon.ORDERS.getCode() + " " + AppLocal.getIntString("button.parked") + " (" + count + ")"
                : UnicodeIcon.ORDERS.getCode() + " " + AppLocal.getIntString("button.parked"));
        if (count > 0) {
            parkedListButton.putClientProperty("FlatLaf.styleClass", "accent");
        } else {
            parkedListButton.putClientProperty("FlatLaf.styleClass", null);
        }
        parkedListButton.repaint();
    }

    public void setOnQtyAdjusted(java.util.function.Consumer<Double> c) {
        this.onQtyAdjusted = c;
    }

    public void setOnDeleteLineClicked(Runnable r) {
        this.onDeleteLineClicked = r;
    }

    public void setOnEditLineClicked(Runnable r) {
        this.onEditLineClicked = r;
    }
}
