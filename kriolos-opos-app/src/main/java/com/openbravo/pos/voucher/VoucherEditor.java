/*
 * Copyright (C) 2022 KiolOS<https://github.com/kriolos>
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
package com.openbravo.pos.voucher;

import com.openbravo.basic.BasicException;
import com.openbravo.data.user.DirtyManager;
import com.openbravo.data.user.EditorRecord;
import com.openbravo.format.Formats;
import com.openbravo.pos.customers.CustomerInfo;
import com.openbravo.pos.customers.CustomerService;
import com.openbravo.pos.customers.DataLogicCustomers;
import com.openbravo.pos.customers.JCustomerFinder;
import com.openbravo.pos.customers.JCustomerFinderPanel;
import com.openbravo.pos.customers.JDialogNewCustomer;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.DataLogicSystem;
import com.openbravo.pos.util.ValidateBuilder;
import java.awt.Component;
import java.awt.image.BufferedImage;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

public final class VoucherEditor extends javax.swing.JPanel implements EditorRecord {

    private static final Logger LOGGER = Logger.getLogger(VoucherEditor.class.getName());

    private static final long serialVersionUID = 1L;
    private String voucherId;
    private final CustomerService customerService;
    @Deprecated
    private final DataLogicCustomers dlCustomers;
    private final DataLogicSystem dlSystem;
    private final VoucherService voucherService;
    private CustomerInfo customerInfo;
    private final AppView m_app;

    public VoucherEditor(DirtyManager dirty, AppView app) {
        this(dirty, app, null, null);
    }

    public VoucherEditor(DirtyManager dirty, AppView app, VoucherService voucherService) {
        this(dirty, app, voucherService, null);
    }

    public VoucherEditor(DirtyManager dirty, AppView app, VoucherService voucherService, CustomerService customerService) {
        m_app = app;

        initComponents();

        this.customerService = customerService != null ? customerService : app.getBean(CustomerService.class);
        this.dlCustomers = (this.customerService instanceof DataLogicCustomers) ? (DataLogicCustomers) this.customerService : app.getBean(DataLogicCustomers.class);
        this.dlSystem = app.getBean(DataLogicSystem.class);
        this.voucherService = voucherService != null ? voucherService : app.getBean(VoucherService.class);
        voucherNumberTField.getDocument().addDocumentListener(dirty);
        voucherCustomerTField.getDocument().addDocumentListener(dirty);
        voucherAmountTField.getDocument().addDocumentListener(dirty);
        voucherStatusTField.getDocument().addDocumentListener(dirty);

        printBtn.setVisible(true);
        
        jLblStatus.setIcon(null);

        writeValueEOF();
    }

    @Deprecated
    public DataLogicVouchers getDataLogicVouchers() {
        return voucherService instanceof DataLogicVouchers dl ? dl : null;
    }

    @Override
    public void writeValueEOF() {
        voucherId = null;
        voucherNumberTField.setText(null);
        voucherNumberTField.setEnabled(false);
        voucherCustomerTField.setText(null);
        voucherCustomerTField.setEnabled(false);
        voucherAmountTField.setText(null);
        voucherAmountTField.setEnabled(false);
        voucherStatusTField.setText(null);
        voucherStatusTField.setEnabled(false);
        customerSelectorBtn.setEnabled(false);
        printBtn.setEnabled(false);
    }

    @Override
    public void writeValueInsert() {
        voucherId = UUID.randomUUID().toString();
        voucherNumberTField.setText(generateVoucherNumber());
        voucherNumberTField.setEnabled(true);
        voucherCustomerTField.setText(null);
        voucherCustomerTField.setEnabled(true);
        voucherAmountTField.setText(null);
        voucherAmountTField.setEnabled(true);
        voucherStatusTField.setText(null);
        voucherStatusTField.setText("A");
        customerSelectorBtn.setEnabled(true);
        printBtn.setEnabled(true);
    }

    @Override
    public void writeValueDelete(Object value) {
        if ("A".equals(voucherStatusTField.getText())) {
            try {
                Object[] attr = (Object[]) value;
                voucherId = (String) attr[0];
                voucherNumberTField.setText(Formats.STRING.formatValue((String) attr[1]));
                voucherNumberTField.setEnabled(false);
                customerInfo = customerService.getCustomerInfo(attr[2].toString());
                voucherCustomerTField.setText(customerInfo.getName());
                voucherCustomerTField.setEnabled(false);
                voucherAmountTField.setText(Formats.DOUBLE.formatValue((Double) attr[3]));
                voucherAmountTField.setEnabled(false);
                voucherStatusTField.setText(Formats.STRING.formatValue((String) attr[4]));
                voucherStatusTField.setEnabled(false);

                printBtn.setEnabled(false);
            } catch (BasicException ex) {
                LOGGER.log(Level.SEVERE, "Exception delete voucher object", ex);
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    AppLocal.getIntString("message.voucherdelete"),
                    AppLocal.getIntString("Check"),
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    @Override
    public void writeValueEdit(Object value) {

        try {
            Object[] attr = (Object[]) value;
            voucherId = (String) attr[0];
            voucherNumberTField.setText(Formats.STRING.formatValue((String) attr[1]));
            voucherNumberTField.setEnabled(true);
            customerInfo = customerService.getCustomerInfo(attr[2].toString());
            voucherCustomerTField.setText(customerInfo.getName());
            voucherCustomerTField.setEnabled(true);
            voucherAmountTField.setText(Formats.DOUBLE.formatValue((Double) attr[3]));
            voucherAmountTField.setEnabled(true);
            voucherStatusTField.setText(Formats.STRING.formatValue((String) attr[4]));

            printBtn.setEnabled(true);

            if (null == voucherStatusTField.getText()) {
                jLblStatus.setIcon(null);
                statusIcon.setIcon(null);
            } else {
                switch (voucherStatusTField.getText()) {
                    case VoucherInfo.VOUCHER_STATUS_AVAILABLE:
                        statusIcon.setIcon(new javax.swing.ImageIcon(getClass()
                                .getResource("/com/openbravo/images/ok.png")));
                        statusIcon.setToolTipText("Available");
                        voucherNumberTField.setEnabled(true);
                        voucherAmountTField.setEnabled(true);
                        customerSelectorBtn.setEnabled(true);
                        voucherStatusTField.setText("A");
                        break;
                    case VoucherInfo.VOUCHER_STATUS_REDEEMED:
                        statusIcon.setIcon(new javax.swing.ImageIcon(getClass()
                                .getResource("/com/openbravo/images/refundit.png")));
                        statusIcon.setToolTipText("Redeemed");
                        voucherNumberTField.setEnabled(false);
                        voucherAmountTField.setEnabled(false);
                        customerSelectorBtn.setEnabled(false);
                        voucherStatusTField.setText("D");

                        break;
                    default:
                        jLblStatus.setIcon(null);
                        statusIcon.setIcon(null);
                        break;
                }
            }

        } catch (BasicException ex) {
            LOGGER.log(Level.SEVERE, "Exception edit voucher object", ex);
        }
    }

    @Override
    public Object createValue() throws BasicException {

        Object[] attr = new Object[5];

        attr[0] = voucherId;
        attr[1] = voucherNumberTField.getText();
        attr[2] = customerInfo.getId();
        attr[3] = Formats.DOUBLE.parseValue(voucherAmountTField.getText());
        attr[4] = voucherStatusTField.getText();

        return attr;
    }

    @Override
    public Component getComponent() {
        return this;
    }

    @Override
    public void refresh() {
    }
    
    private static Double readCurrency(String sValue) throws BasicException {
        return Formats.CURRENCY.parseValue(sValue);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel2 = new javax.swing.JLabel();
        voucherNumberTField = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        voucherCustomerTField = new javax.swing.JTextField();
        voucherAmountTField = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        printBtn = new javax.swing.JButton();
        jLblStatus = new javax.swing.JLabel();
        voucherStatusTField = new javax.swing.JTextField();
        customerSelectorBtn = new javax.swing.JButton();
        statusIcon = new javax.swing.JLabel();

        setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N

        jLabel2.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel2.setText(AppLocal.getIntString("label.Number")); // NOI18N
        jLabel2.setPreferredSize(new java.awt.Dimension(100, 30));

        voucherNumberTField.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        voucherNumberTField.setPreferredSize(new java.awt.Dimension(240, 30));

        jLabel3.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel3.setText(AppLocal.getIntString("label.customer")); // NOI18N
        jLabel3.setPreferredSize(new java.awt.Dimension(100, 30));

        voucherCustomerTField.setEditable(false);
        voucherCustomerTField.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        voucherCustomerTField.setPreferredSize(new java.awt.Dimension(240, 30));

        voucherAmountTField.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        voucherAmountTField.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        voucherAmountTField.setPreferredSize(new java.awt.Dimension(240, 30));

        jLabel5.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel5.setText(AppLocal.getIntString("label.paymenttotal")); // NOI18N
        jLabel5.setPreferredSize(new java.awt.Dimension(100, 30));

        printBtn.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        printBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/printer24.png"))); // NOI18N
        printBtn.setText(AppLocal.getIntString("button.print")); // NOI18N
        printBtn.setToolTipText(AppLocal.getIntString("button.print")); // NOI18N
        printBtn.setFocusPainted(false);
        printBtn.setFocusable(false);
        printBtn.setMargin(new java.awt.Insets(8, 14, 8, 14));
        printBtn.setPreferredSize(new java.awt.Dimension(80, 45));
        printBtn.setRequestFocusEnabled(false);
        printBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                printBtnActionPerformed(evt);
            }
        });

        jLblStatus.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLblStatus.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/ok.png"))); // NOI18N
        jLblStatus.setText(AppLocal.getIntString("label.Status")); // NOI18N
        jLblStatus.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        jLblStatus.setPreferredSize(new java.awt.Dimension(100, 30));

        voucherStatusTField.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        voucherStatusTField.setPreferredSize(new java.awt.Dimension(240, 30));

        customerSelectorBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/customer_sml.png"))); // NOI18N
        customerSelectorBtn.setToolTipText("Open Customers");
        customerSelectorBtn.setFocusPainted(false);
        customerSelectorBtn.setFocusable(false);
        customerSelectorBtn.setMargin(new java.awt.Insets(8, 14, 8, 14));
        customerSelectorBtn.setPreferredSize(new java.awt.Dimension(100, 30));
        customerSelectorBtn.setRequestFocusEnabled(false);
        customerSelectorBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                customerSelectorBtnActionPerformed(evt);
            }
        });

        statusIcon.setToolTipText("");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(voucherNumberTField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(voucherCustomerTField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(26, 26, 26)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(statusIcon, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(customerSelectorBtn, javax.swing.GroupLayout.DEFAULT_SIZE, 62, Short.MAX_VALUE)))))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(voucherAmountTField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLblStatus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(voucherStatusTField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(printBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(72, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(voucherNumberTField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(voucherCustomerTField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(voucherAmountTField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLblStatus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(voucherStatusTField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(statusIcon, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addComponent(customerSelectorBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 7, Short.MAX_VALUE)
                .addComponent(printBtn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12))
        );
    }// </editor-fold>//GEN-END:initComponents

private void printBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_printBtnActionPerformed

    try {
        VoucherInfo voucherInfo = voucherService.getVoucherAll(voucherId);
        BufferedImage image = dlSystem.getResourceAsImage("Window.Logo");
        if (voucherInfo != null) {
            JDialogReportPanel dialog = JDialogReportPanel
                    .getDialog(this, m_app, voucherInfo, image);
            dialog.setVisible(true);
        }

    } catch (BasicException ex) {
        LOGGER.log(Level.WARNING, "Exception Create voucher printer dialog", ex);
    }
}//GEN-LAST:event_printBtnActionPerformed

    private void customerSelectorBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_customerSelectorBtnActionPerformed

        CustomerInfo selected = JCustomerFinderPanel.show(this, customerService);

        if (selected != null) {
            customerInfo = selected;
            voucherCustomerTField.setText(customerInfo.getName());
        }

        if (false) {
            JDialogNewCustomer dialog = JDialogNewCustomer.getDialog(this, m_app);
            dialog.setVisible(true);

            customerInfo = dialog.getSelectedCustomer();
            if (dialog.getSelectedCustomer() != null) {
                voucherCustomerTField.setText(customerInfo.getName());
            }
        }

    }//GEN-LAST:event_customerSelectorBtnActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton customerSelectorBtn;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLblStatus;
    private javax.swing.JButton printBtn;
    private javax.swing.JLabel statusIcon;
    private javax.swing.JTextField voucherAmountTField;
    private javax.swing.JTextField voucherCustomerTField;
    private javax.swing.JTextField voucherNumberTField;
    private javax.swing.JTextField voucherStatusTField;
    // End of variables declaration//GEN-END:variables

    public boolean isDataValid() {
        ValidateBuilder validate = new ValidateBuilder(this);
        validate.setValidate(voucherNumberTField.getText(), ValidateBuilder.IS_NOT_EMPTY,
                AppLocal.getIntString("message.message.emptynumber"));
        validate.setValidate(voucherCustomerTField.getText(), ValidateBuilder.IS_NOT_EMPTY,
                AppLocal.getIntString("message.emptycustomer"));
        validate.setValidate(voucherAmountTField.getText(), ValidateBuilder.IS_DOUBLE,
                AppLocal.getIntString("message.numericamount"));
        validate.setValidate(voucherStatusTField.getText(), ValidateBuilder.IS_NOT_EMPTY,
                AppLocal.getIntString("message.emptystatus"));
        return validate.getValid();
    }

    /** 
     * Voucher Number generator
     * 
     * @return voucher number ("VO-{MM-yy}-{SEQ}" e.g. "VO-10-26-00001")
     */
    public String generateVoucherNumber() {
        try {
            return voucherService.generateNextVoucherNumber();
        } catch (BasicException ex) {
            LOGGER.log(Level.WARNING, "Exception generate voucher number", ex);
            return "";
        }
    }

}
