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

import com.openbravo.data.gui.modal.PosUIModal;
import com.openbravo.format.Formats;
import com.openbravo.pos.customers.CustomerInfoExt;
import com.openbravo.pos.forms.AppLocal;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

/**
 * LAF-agnostic modal panel displaying customer identification, VIP status,
 * discount privileges, and account limits using a structured record without HTML string concatenation.
 */
public class CustomerDiscountInfoPanel extends JPanel {

    private final CustomerDiscountDetails details;
    private PosUIModal modalContext;

    public CustomerDiscountInfoPanel(CustomerDiscountDetails details) {
        this.details = details != null ? details : CustomerDiscountDetails.of(null, false, 0.0);
        initUI();
    }

    public CustomerDiscountInfoPanel(CustomerInfoExt customer) {
        this(CustomerDiscountDetails.fromCustomer(customer));
    }

    public CustomerDiscountInfoPanel(String vipStatus, String discountStatus) {
        this.details = new CustomerDiscountDetails(null, null, null, "Yes".equalsIgnoreCase(vipStatus) || (vipStatus != null && vipStatus.contains("VIP")), 0.0, null, null, null);
        initLegacyUI(vipStatus, discountStatus);
    }

    private void initUI() {
        setName("kriolos:sales:customer_discount_info");
        setLayout(new BorderLayout(16, 16));
        setBorder(BorderFactory.createEmptyBorder(18, 22, 16, 22));

        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setName("kriolos:sales:customer_discount_content");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 8, 5, 8);
        gbc.anchor = GridBagConstraints.LINE_START;

        int row = 0;
        boolean hasCustomerInfo = false;

        // Section 1: Customer Identification
        if (hasValue(details.customerName())) {
            addRow(contentPanel, gbc, row++, AppLocal.getIntString("label.customer"), details.customerName(), true);
            hasCustomerInfo = true;
        }

        if (hasValue(details.card())) {
            addRow(contentPanel, gbc, row++, AppLocal.getIntString("label.card"), details.card(), false);
            hasCustomerInfo = true;
        }

        if (hasValue(details.taxId())) {
            addRow(contentPanel, gbc, row++, AppLocal.getIntString("label.taxid"), details.taxId(), false);
            hasCustomerInfo = true;
        }

        if (hasCustomerInfo) {
            addSeparator(contentPanel, gbc, row++);
        }

        // Section 2: VIP & Discount Privileges
        String vipValue = details.vip()
                ? AppLocal.getIntString("message.vipyes")
                : AppLocal.getIntString("message.vipno");
        addRow(contentPanel, gbc, row++, AppLocal.getIntString("label.vip"), vipValue, true);

        String discountValue;
        if (details.discountPercent() != null && details.discountPercent() > 0) {
            discountValue = details.discountPercent() + "%";
        } else {
            discountValue = AppLocal.getIntString("message.discno");
        }
        addRow(contentPanel, gbc, row++, AppLocal.getIntString("label.discount"), discountValue, true);

        // Section 3: Account & Credit Balances (if available)
        boolean hasFinancials = false;
        if (details.maxDebt() != null && details.maxDebt() > 0) {
            if (!hasFinancials) {
                addSeparator(contentPanel, gbc, row++);
                hasFinancials = true;
            }
            addRow(contentPanel, gbc, row++, AppLocal.getIntString("label.maxdebt"), Formats.CURRENCY.formatValue(details.maxDebt()), false);
        }

        if (details.currentDebt() != null && details.currentDebt() != 0.0) {
            if (!hasFinancials) {
                addSeparator(contentPanel, gbc, row++);
                hasFinancials = true;
            }
            addRow(contentPanel, gbc, row++, AppLocal.getIntString("label.curdebt"), Formats.CURRENCY.formatValue(details.currentDebt()), false);
        }

        if (hasValue(details.notes())) {
            addSeparator(contentPanel, gbc, row++);
            addRow(contentPanel, gbc, row++, AppLocal.getIntString("label.notes"), details.notes(), false);
        }

        add(contentPanel, BorderLayout.CENTER);

        // Action Buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JButton btnOk = new JButton(AppLocal.getIntString("button.ok"));
        btnOk.setName("kriolos:sales:customer_discount_ok");
        btnOk.addActionListener(e -> {
            if (modalContext != null) {
                modalContext.close();
            }
        });
        actionPanel.add(btnOk);
        add(actionPanel, BorderLayout.SOUTH);
    }

    private void initLegacyUI(String vipStatus, String discountStatus) {
        setName("kriolos:sales:customer_discount_info");
        setLayout(new BorderLayout(16, 16));
        setBorder(BorderFactory.createEmptyBorder(18, 22, 16, 22));

        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setName("kriolos:sales:customer_discount_content");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.anchor = GridBagConstraints.LINE_START;

        // Row 0: VIP Label and Value
        addRow(contentPanel, gbc, 0, AppLocal.getIntString("label.vip"), vipStatus != null ? vipStatus : "-", true);

        // Row 1: Discount Label and Value
        addRow(contentPanel, gbc, 1, AppLocal.getIntString("label.discount"), discountStatus != null ? discountStatus : "-", true);

        add(contentPanel, BorderLayout.CENTER);

        // Action Buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JButton btnOk = new JButton(AppLocal.getIntString("button.ok"));
        btnOk.setName("kriolos:sales:customer_discount_ok");
        btnOk.addActionListener(e -> {
            if (modalContext != null) {
                modalContext.close();
            }
        });
        actionPanel.add(btnOk);
        add(actionPanel, BorderLayout.SOUTH);
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int row, String title, String value, boolean highlightValue) {
        gbc.gridx = 0;
        gbc.gridy = row;
        JLabel lblTitle = new JLabel(title + ":");
        lblTitle.setFont(lblTitle.getFont().deriveFont(Font.BOLD));
        panel.add(lblTitle, gbc);

        gbc.gridx = 1;
        JLabel lblValue = new JLabel(value);
        if (highlightValue) {
            lblValue.setFont(lblValue.getFont().deriveFont(Font.BOLD));
        }
        panel.add(lblValue, gbc);
    }

    private void addSeparator(JPanel panel, GridBagConstraints gbc, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 4, 8, 4);
        panel.add(new JSeparator(), gbc);

        // Reset constraints
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(5, 8, 5, 8);
    }

    private boolean hasValue(String text) {
        return text != null && !text.isBlank();
    }

    public CustomerDiscountDetails getDetails() {
        return details;
    }

    public void setModalContext(PosUIModal modalContext) {
        this.modalContext = modalContext;
    }

    public static void show(Component parent, CustomerDiscountDetails details) {
        CustomerDiscountInfoPanel panel = new CustomerDiscountInfoPanel(details);
        PosUIModal modal = PosUIModal.create(parent, panel)
                .setTitle(AppLocal.getIntString("title.editor"))
                .setModal(true)
                .setResizable(false);
        panel.setModalContext(modal);
        modal.show();
    }

    public static void show(Component parent, CustomerInfoExt customer) {
        show(parent, CustomerDiscountDetails.fromCustomer(customer));
    }

    public static void show(Component parent, String vipStatus, String discountStatus) {
        CustomerDiscountInfoPanel panel = new CustomerDiscountInfoPanel(vipStatus, discountStatus);
        PosUIModal modal = PosUIModal.create(parent, panel)
                .setTitle(AppLocal.getIntString("title.editor"))
                .setModal(true)
                .setResizable(false);
        panel.setModalContext(modal);
        modal.show();
    }
}
