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

package com.openbravo.pos.customers;

import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.MessageInf;
import com.openbravo.data.gui.modal.PosUIModal;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.data.user.DirtyManager;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.BeanFactoryException;
import com.openbravo.pos.forms.DataLogicSales;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class JNewCustomerPanel extends JPanel {

    private static final Logger LOGGER = Logger.getLogger(JNewCustomerPanel.class.getName());
    private static final long serialVersionUID = 1L;

    private DataLogicCustomers dlCustomer;
    private DataLogicSales dlSales;
    private CustomerInfoExt selectedCustomer;
    private CustomersView customersView;
    private PosUIModal modalContext;

    public JNewCustomerPanel() {
        initComponents();
        initDomainAdapters();
    }

    public JNewCustomerPanel(AppView app) {
        init(app);
        initDomainAdapters();
    }

    private void initDomainAdapters() {
        setName("kriolos:customers:new-customer-panel");
        m_jBtnOK.setName("kriolos:customers:btn-ok");
        m_jBtnCancel.setName("kriolos:customers:btn-cancel");
    }

    public void setModalContext(PosUIModal modalContext) {
        this.modalContext = modalContext;
    }

    public static CustomerInfoExt show(Component parent, AppView app) {
        JNewCustomerPanel panel = new JNewCustomerPanel(app);
        PosUIModal modal = PosUIModal.create(parent, panel)
                .setTitle(AppLocal.getIntString("label.customer"))
                .setModal(true)
                .setResizable(false);
        panel.setModalContext(modal);
        modal.show();
        return panel.getSelectedCustomer();
    }

    private void init(AppView app) {
        try {
            dlSales = (DataLogicSales) app.getBean("com.openbravo.pos.forms.DataLogicSales");
            dlCustomer = (DataLogicCustomers) app.getBean("com.openbravo.pos.customers.DataLogicCustomers");

            initComponents();

            DirtyManager dirty = new DirtyManager();
            customersView = new CustomersView(app, dirty);
            customersView.writeValueInsert();
            formPanel.setLayout(new BorderLayout());
            formPanel.add(customersView, BorderLayout.CENTER);

            if (getRootPane() != null) {
                getRootPane().setDefaultButton(m_jBtnOK);
            }
        } catch (BeanFactoryException ex) {
            LOGGER.log(Level.SEVERE, "Error ", ex);
        }
    }

    public Object createValue() throws BasicException {
        return customersView != null ? customersView.createValue() : null;
    }

    public CustomerInfoExt getSelectedCustomer() {
        return selectedCustomer;
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        formPanel = new JPanel();
        jPanel2 = new JPanel();
        m_jBtnCancel = new JButton();
        m_jBtnOK = new JButton();

        setLayout(new BorderLayout());

        formPanel.setFont(new Font("Arial", 0, 14)); // NOI18N
        formPanel.setMaximumSize(new Dimension(695, 420));
        formPanel.setPreferredSize(new Dimension(695, 420));

        GroupLayout formPanelLayout = new GroupLayout(formPanel);
        formPanel.setLayout(formPanelLayout);
        formPanelLayout.setHorizontalGroup(
            formPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGap(0, 700, Short.MAX_VALUE)
        );
        formPanelLayout.setVerticalGroup(
            formPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGap(0, 420, Short.MAX_VALUE)
        );

        add(formPanel, BorderLayout.NORTH);
        formPanel.getAccessibleContext().setAccessibleName("");

        jPanel2.setLayout(new FlowLayout(FlowLayout.RIGHT));

        m_jBtnCancel.setFont(new Font("Arial", 0, 12)); // NOI18N
        m_jBtnCancel.setIcon(new ImageIcon(getClass().getResource("/com/openbravo/images/cancel.png"))); // NOI18N
        m_jBtnCancel.setText(AppLocal.getIntString("button.cancel")); // NOI18N
        m_jBtnCancel.setFocusPainted(false);
        m_jBtnCancel.setFocusable(false);
        m_jBtnCancel.setMargin(new Insets(8, 16, 8, 16));
        m_jBtnCancel.setPreferredSize(new Dimension(110, 45));
        m_jBtnCancel.setRequestFocusEnabled(false);
        m_jBtnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jBtnCancelActionPerformed(evt);
            }
        });
        jPanel2.add(m_jBtnCancel);

        m_jBtnOK.setFont(new Font("Arial", 0, 12)); // NOI18N
        m_jBtnOK.setIcon(new ImageIcon(getClass().getResource("/com/openbravo/images/ok.png"))); // NOI18N
        m_jBtnOK.setText(AppLocal.getIntString("button.ok")); // NOI18N
        m_jBtnOK.setFocusPainted(false);
        m_jBtnOK.setFocusable(false);
        m_jBtnOK.setMargin(new Insets(8, 16, 8, 16));
        m_jBtnOK.setPreferredSize(new Dimension(110, 45));
        m_jBtnOK.setRequestFocusEnabled(false);
        m_jBtnOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jBtnOKActionPerformed(evt);
            }
        });
        jPanel2.add(m_jBtnOK);

        add(jPanel2, BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents

    private void m_jBtnOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jBtnOKActionPerformed
        try {
            Object customer = createValue();
            String m_oId = ((Object[]) customer)[0].toString();

            int status = dlCustomer.getTableCustomers().getInsertSentence().exec(customer);

            if (status > 0) {
                selectedCustomer = dlCustomer.findCustomerInfoExtById(m_oId);
                if (modalContext != null) {
                    modalContext.setResult(selectedCustomer);
                    modalContext.close();
                }
            } else {
                MessageInf msg = new MessageInf(MessageInf.SGN_NOTICE,
                        AppLocal.getIntString("message.nosave"), "Error save");
                msg.show(this);
            }

        } catch (BasicException ex) {
            LOGGER.log(Level.SEVERE, "Error ", ex);
            MessageInf msg = new MessageInf(MessageInf.SGN_NOTICE,
                    AppLocal.getIntString("message.nosave"), ex);
            msg.show(this);
        }
    }//GEN-LAST:event_m_jBtnOKActionPerformed

    private void m_jBtnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jBtnCancelActionPerformed
        selectedCustomer = null;
        if (modalContext != null) {
            modalContext.close();
        }
    }//GEN-LAST:event_m_jBtnCancelActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JPanel formPanel;
    private JPanel jPanel2;
    private JButton m_jBtnCancel;
    private JButton m_jBtnOK;
    // End of variables declaration//GEN-END:variables
}
