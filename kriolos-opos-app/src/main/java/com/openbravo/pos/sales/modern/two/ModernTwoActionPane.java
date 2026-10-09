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

import com.openbravo.pos.customers.CustomerInfoExt;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.ui.components.ButtonSize;
import com.openbravo.pos.ui.components.POSButtonFactory;
import com.openbravo.pos.ui.components.UnicodeIcon;
import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.util.Locale;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;

/**
 * Bottom action pane for {@link ModernTwoSalesLayout}.
 * Houses:
 * <ul>
 *   <li>Row 1: Customer info, Parked orders counter, Cash operations (Entrada/Reforço, Saída/Retirada, Fechar Caixa).</li>
 *   <li>Row 2: Selected line modifiers (-1, +1, Editar, Remover) and Order actions (Limpar, Suspender, Desconto).</li>
 * </ul>
 *
 * @author KriolOS Team
 */
public class ModernTwoActionPane extends JPanel {

    private static final long serialVersionUID = 1L;

    // Customer & Parked
    private final JButton customerButton;
    private final JButton parkedOrdersButton;

    // Cash operations
    private final JButton btnCashIn;
    private final JButton btnCashOut;
    private final JButton btnCloseCash;

    // Line Modifiers
    private final JButton btnQtyMinus;
    private final JButton btnQtyPlus;
    private final JButton btnEditLine;
    private final JButton btnDeleteLine;

    // Order Actions
    private final JButton btnClear;
    private final JButton btnHold;
    private final JButton btnDiscount;

    // Callbacks
    private Consumer<Double> onQtyAdjusted;
    private Runnable onEditLineClicked;
    private Runnable onDeleteLineClicked;
    private Runnable onClearClicked;
    private Runnable onHoldClicked;
    private Runnable onDiscountClicked;
    private Runnable onCustomerClicked;
    private Runnable onParkedOrdersClicked;
    private Runnable onCashInClicked;
    private Runnable onCashOutClicked;
    private Runnable onCloseCashClicked;

    public ModernTwoActionPane() {
        setLayout(new GridLayout(2, 1, 0, 6));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(6, 2, 4, 2));
        setPreferredSize(new Dimension(0, 114));
        setMinimumSize(new Dimension(0, 108));
        setName("kriolos:sales:modern-two:action-pane");

        final ButtonSize touchSize = ButtonSize.EXTRA_LARGE;

        // ====================================================
        // ROW 1: Customer & Parked (Left) + Cash Operations (Right)
        // ====================================================
        JPanel row1 = new JPanel(new BorderLayout(12, 0));
        row1.setOpaque(false);

        // Left: Customer & Parked
        JPanel customerGroup = new JPanel(new BorderLayout(6, 0));
        customerGroup.setOpaque(false);

        customerButton = POSButtonFactory.createActionButton(
                AppLocal.getIntString("label.customer") + ": " + AppLocal.getIntString("label.guest"),
                UnicodeIcon.CUSTOMER,
                touchSize,
                KeyEvent.VK_C,
                AppLocal.getIntString("label.customer"),
                e -> {
                    if (onCustomerClicked != null) onCustomerClicked.run();
                }
        );
        customerButton.setPreferredSize(new Dimension(240, touchSize.getHeight()));
        customerGroup.add(customerButton, BorderLayout.CENTER);

        parkedOrdersButton = POSButtonFactory.createActionButton(
                "0",
                UnicodeIcon.PARKED,
                touchSize,
                KeyEvent.VK_U,
                AppLocal.getIntString("button.parked.tooltip"),
                e -> {
                    if (onParkedOrdersClicked != null) onParkedOrdersClicked.run();
                }
        );
        parkedOrdersButton.setPreferredSize(new Dimension(65, touchSize.getHeight()));
        customerGroup.add(parkedOrdersButton, BorderLayout.LINE_END);
        row1.add(customerGroup, BorderLayout.LINE_START);

        // Right: Cash Operations (Entrada, Saída, Fechar Caixa)
        JPanel cashActions = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.TRAILING, 6, 0));
        cashActions.setOpaque(false);

        btnCashIn = POSButtonFactory.createActionButton(
                AppLocal.getIntString("button.cashin"),
                UnicodeIcon.CASH_IN,
                touchSize,
                KeyEvent.VK_E,
                AppLocal.getIntString("button.cashin.tooltip"),
                e -> {
                    if (onCashInClicked != null) onCashInClicked.run();
                }
        );
        btnCashIn.setPreferredSize(new Dimension(175, touchSize.getHeight()));

        btnCashOut = POSButtonFactory.createActionButton(
                AppLocal.getIntString("button.cashout"),
                UnicodeIcon.CASH_OUT,
                touchSize,
                KeyEvent.VK_S,
                AppLocal.getIntString("button.cashout.tooltip"),
                e -> {
                    if (onCashOutClicked != null) onCashOutClicked.run();
                }
        );
        btnCashOut.setPreferredSize(new Dimension(175, touchSize.getHeight()));

        btnCloseCash = POSButtonFactory.createDangerButton(
                AppLocal.getIntString("button.closecash"),
                UnicodeIcon.CLOSE_CASH,
                touchSize,
                KeyEvent.VK_F,
                AppLocal.getIntString("button.closecash"),
                e -> {
                    if (onCloseCashClicked != null) onCloseCashClicked.run();
                }
        );
        btnCloseCash.setPreferredSize(new Dimension(145, touchSize.getHeight()));

        cashActions.add(btnCashIn);
        cashActions.add(btnCashOut);
        cashActions.add(btnCloseCash);
        row1.add(cashActions, BorderLayout.LINE_END);

        add(row1);

        // ====================================================
        // ROW 2: Line Modifiers (Left) + Order Actions (Right)
        // ====================================================
        JPanel row2 = new JPanel(new BorderLayout(12, 0));
        row2.setOpaque(false);

        // Left: Line Modifiers (-1, +1, Editar, Remover)
        JPanel lineActions = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEADING, 6, 0));
        lineActions.setOpaque(false);

        btnQtyMinus = POSButtonFactory.createActionButton(
                "-1",
                UnicodeIcon.MINUS,
                touchSize,
                KeyEvent.VK_MINUS,
                AppLocal.getIntString("button.qtyminus.tooltip"),
                e -> {
                    if (onQtyAdjusted != null) onQtyAdjusted.accept(-1.0);
                }
        );
        btnQtyMinus.setPreferredSize(new Dimension(68, touchSize.getHeight()));

        btnQtyPlus = POSButtonFactory.createActionButton(
                "+1",
                UnicodeIcon.PLUS,
                touchSize,
                KeyEvent.VK_PLUS,
                AppLocal.getIntString("button.qtyplus.tooltip"),
                e -> {
                    if (onQtyAdjusted != null) onQtyAdjusted.accept(1.0);
                }
        );
        btnQtyPlus.setPreferredSize(new Dimension(68, touchSize.getHeight()));

        btnEditLine = POSButtonFactory.createActionButton(
                AppLocal.getIntString("button.edit"),
                UnicodeIcon.EDIT,
                touchSize,
                KeyEvent.VK_D,
                AppLocal.getIntString("button.editline.tooltip"),
                e -> {
                    if (onEditLineClicked != null) onEditLineClicked.run();
                }
        );
        btnEditLine.setPreferredSize(new Dimension(105, touchSize.getHeight()));

        btnDeleteLine = POSButtonFactory.createDangerButton(
                AppLocal.getIntString("button.delete"),
                UnicodeIcon.DELETE,
                touchSize,
                KeyEvent.VK_R,
                AppLocal.getIntString("button.deleteline.tooltip"),
                e -> {
                    if (onDeleteLineClicked != null) onDeleteLineClicked.run();
                }
        );
        btnDeleteLine.setPreferredSize(new Dimension(115, touchSize.getHeight()));

        lineActions.add(btnQtyMinus);
        lineActions.add(btnQtyPlus);
        lineActions.add(btnEditLine);
        lineActions.add(btnDeleteLine);
        row2.add(lineActions, BorderLayout.LINE_START);

        // Right: Order Actions (Limpar, Suspender, Desconto)
        JPanel orderActions = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.TRAILING, 6, 0));
        orderActions.setOpaque(false);

        btnClear = POSButtonFactory.createActionButton(
                AppLocal.getIntString("button.clean"),
                UnicodeIcon.CLEAR,
                touchSize,
                KeyEvent.VK_L,
                AppLocal.getIntString("button.clear.tooltip"),
                e -> {
                    if (onClearClicked != null) onClearClicked.run();
                }
        );
        btnClear.setPreferredSize(new Dimension(110, touchSize.getHeight()));

        btnHold = POSButtonFactory.createActionButton(
                AppLocal.getIntString("button.hold"),
                UnicodeIcon.HOLD,
                touchSize,
                KeyEvent.VK_H,
                AppLocal.getIntString("button.hold.tooltip"),
                e -> {
                    if (onHoldClicked != null) onHoldClicked.run();
                }
        );
        btnHold.setPreferredSize(new Dimension(125, touchSize.getHeight()));

        btnDiscount = POSButtonFactory.createActionButton(
                AppLocal.getIntString("button.discount"),
                UnicodeIcon.DISCOUNT,
                touchSize,
                KeyEvent.VK_T,
                AppLocal.getIntString("button.discount.tooltip"),
                e -> {
                    if (onDiscountClicked != null) onDiscountClicked.run();
                }
        );
        btnDiscount.setPreferredSize(new Dimension(120, touchSize.getHeight()));

        orderActions.add(btnClear);
        orderActions.add(btnHold);
        orderActions.add(btnDiscount);
        row2.add(orderActions, BorderLayout.LINE_END);

        add(row2);

        applyComponentOrientation(ComponentOrientation.getOrientation(Locale.getDefault()));
    }

    public void setCustomer(CustomerInfoExt customer) {
        if (customer != null) {
            customerButton.setText(POSButtonFactory.formatButtonText(AppLocal.getIntString("label.customer") + ": " + customer.getName(), UnicodeIcon.CUSTOMER));
            customerButton.putClientProperty("FlatLaf.styleClass", "accent");
        } else {
            customerButton.setText(POSButtonFactory.formatButtonText(AppLocal.getIntString("label.customer") + ": " + AppLocal.getIntString("label.guest"), UnicodeIcon.CUSTOMER));
            customerButton.putClientProperty("FlatLaf.styleClass", null);
        }
        customerButton.repaint();
    }

    public void setParkedCount(int count) {
        parkedOrdersButton.setText(UnicodeIcon.PARKED.getCode() + "  " + count);
        if (count > 0) {
            parkedOrdersButton.putClientProperty("FlatLaf.styleClass", "accent");
        } else {
            parkedOrdersButton.putClientProperty("FlatLaf.styleClass", null);
        }
        parkedOrdersButton.repaint();
    }

    public void setOnQtyAdjusted(Consumer<Double> c) { this.onQtyAdjusted = c; }
    public void setOnEditLineClicked(Runnable r) { this.onEditLineClicked = r; }
    public void setOnDeleteLineClicked(Runnable r) { this.onDeleteLineClicked = r; }
    public void setOnClearClicked(Runnable r) { this.onClearClicked = r; }
    public void setOnHoldClicked(Runnable r) { this.onHoldClicked = r; }
    public void setOnDiscountClicked(Runnable r) { this.onDiscountClicked = r; }
    public void setOnCustomerClicked(Runnable r) { this.onCustomerClicked = r; }
    public void setOnParkedOrdersClicked(Runnable r) { this.onParkedOrdersClicked = r; }
    public void setOnCashInClicked(Runnable r) { this.onCashInClicked = r; }
    public void setOnCashOutClicked(Runnable r) { this.onCashOutClicked = r; }
    public void setOnCloseCashClicked(Runnable r) { this.onCloseCashClicked = r; }
}
