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

/**
 * LAF-agnostic modal panel displaying customer VIP and discount information
 * without HTML string concatenation.
 */
public class CustomerDiscountInfoPanel extends JPanel {

    private PosUIModal modalContext;

    public CustomerDiscountInfoPanel(String vipStatus, String discountStatus) {
        setName("kriolos:sales:customer_discount_info");
        setLayout(new BorderLayout(16, 16));
        setBorder(BorderFactory.createEmptyBorder(20, 24, 16, 24));

        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setName("kriolos:sales:customer_discount_content");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.LINE_START;

        // Row 0: VIP Label and Value
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel lblVipTitle = new JLabel(AppLocal.getIntString("label.vip") + ":");
        lblVipTitle.setFont(lblVipTitle.getFont().deriveFont(Font.BOLD));
        contentPanel.add(lblVipTitle, gbc);

        gbc.gridx = 1;
        JLabel lblVipValue = new JLabel(vipStatus);
        contentPanel.add(lblVipValue, gbc);

        // Row 1: Discount Label and Value
        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel lblDiscountTitle = new JLabel(AppLocal.getIntString("label.discount") + ":");
        lblDiscountTitle.setFont(lblDiscountTitle.getFont().deriveFont(Font.BOLD));
        contentPanel.add(lblDiscountTitle, gbc);

        gbc.gridx = 1;
        JLabel lblDiscountValue = new JLabel(discountStatus);
        contentPanel.add(lblDiscountValue, gbc);

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

    public void setModalContext(PosUIModal modalContext) {
        this.modalContext = modalContext;
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
