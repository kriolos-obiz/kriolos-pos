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

import com.openbravo.pos.customers.CustomerInfoExt;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
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
        setPreferredSize(new Dimension(0, 88));
        setMinimumSize(new Dimension(0, 84));
        setName("kriolos:sales:modern-two:action-pane");

        // ====================================================
        // ROW 1: Customer & Parked (Left) + Cash Operations (Right)
        // ====================================================
        JPanel row1 = new JPanel(new BorderLayout(12, 0));
        row1.setOpaque(false);

        // Left: Customer & Parked
        JPanel customerGroup = new JPanel(new BorderLayout(6, 0));
        customerGroup.setOpaque(false);

        customerButton = new JButton("Cliente: Consumidor Final");
        customerButton.setFocusPainted(false);
        customerButton.setFont(customerButton.getFont().deriveFont(Font.PLAIN, 12f));
        customerButton.setPreferredSize(new Dimension(210, 38));
        customerButton.putClientProperty("FlatLaf.style", "arc: 8;");
        customerButton.addActionListener(e -> {
            if (onCustomerClicked != null) onCustomerClicked.run();
        });
        customerGroup.add(customerButton, BorderLayout.CENTER);

        parkedOrdersButton = new JButton("0");
        parkedOrdersButton.setFocusPainted(false);
        parkedOrdersButton.setFont(parkedOrdersButton.getFont().deriveFont(Font.BOLD, 12f));
        parkedOrdersButton.setToolTipText("Contas suspensas");
        parkedOrdersButton.setPreferredSize(new Dimension(46, 38));
        parkedOrdersButton.putClientProperty("FlatLaf.style", "arc: 8;");
        parkedOrdersButton.addActionListener(e -> {
            if (onParkedOrdersClicked != null) onParkedOrdersClicked.run();
        });
        customerGroup.add(parkedOrdersButton, BorderLayout.LINE_END);
        row1.add(customerGroup, BorderLayout.LINE_START);

        // Right: Cash Operations (Entrada, Saída, Fechar Caixa)
        JPanel cashActions = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.TRAILING, 6, 0));
        cashActions.setOpaque(false);

        btnCashIn = createActionButton("Entrada/Refor\u00e7o (F3)", 155, e -> {
            if (onCashInClicked != null) onCashInClicked.run();
        });
        btnCashOut = createActionButton("Sa\u00edda/Retirada (F4)", 145, e -> {
            if (onCashOutClicked != null) onCashOutClicked.run();
        });
        btnCloseCash = createActionButton("Fechar Caixa", 115, e -> {
            if (onCloseCashClicked != null) onCloseCashClicked.run();
        });
        btnCloseCash.setBackground(new Color(239, 68, 68)); // Red #ef4444
        btnCloseCash.setForeground(Color.WHITE);
        btnCloseCash.setOpaque(true);

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

        btnQtyMinus = createActionButton("-1", 52, e -> {
            if (onQtyAdjusted != null) onQtyAdjusted.accept(-1.0);
        });
        btnQtyPlus = createActionButton("+1", 52, e -> {
            if (onQtyAdjusted != null) onQtyAdjusted.accept(1.0);
        });
        btnEditLine = createActionButton("Editar", 90, e -> {
            if (onEditLineClicked != null) onEditLineClicked.run();
        });
        btnDeleteLine = createActionButton("Remover", 95, e -> {
            if (onDeleteLineClicked != null) onDeleteLineClicked.run();
        });

        lineActions.add(btnQtyMinus);
        lineActions.add(btnQtyPlus);
        lineActions.add(btnEditLine);
        lineActions.add(btnDeleteLine);
        row2.add(lineActions, BorderLayout.LINE_START);

        // Right: Order Actions (Limpar, Suspender, Desconto)
        JPanel orderActions = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.TRAILING, 6, 0));
        orderActions.setOpaque(false);

        btnClear = createActionButton("Limpar", 95, e -> {
            if (onClearClicked != null) onClearClicked.run();
        });
        btnHold = createActionButton("Suspender", 105, e -> {
            if (onHoldClicked != null) onHoldClicked.run();
        });
        btnDiscount = createActionButton("Desconto", 100, e -> {
            if (onDiscountClicked != null) onDiscountClicked.run();
        });

        orderActions.add(btnClear);
        orderActions.add(btnHold);
        orderActions.add(btnDiscount);
        row2.add(orderActions, BorderLayout.LINE_END);

        add(row2);

        applyComponentOrientation(ComponentOrientation.getOrientation(Locale.getDefault()));
    }

    private JButton createActionButton(String text, int width, java.awt.event.ActionListener al) {
        JButton btn = new JButton(text);
        btn.setFont(btn.getFont().deriveFont(Font.BOLD, 12f));
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(width, 38));
        btn.putClientProperty("FlatLaf.style", "arc: 8;");
        btn.addActionListener(al);
        return btn;
    }

    public void setCustomer(CustomerInfoExt customer) {
        if (customer != null) {
            customerButton.setText("Cliente: " + customer.getName());
            customerButton.putClientProperty("FlatLaf.styleClass", "accent");
        } else {
            customerButton.setText("Cliente: Consumidor Final");
            customerButton.putClientProperty("FlatLaf.styleClass", null);
        }
        customerButton.repaint();
    }

    public void setParkedCount(int count) {
        parkedOrdersButton.setText(String.valueOf(count));
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
