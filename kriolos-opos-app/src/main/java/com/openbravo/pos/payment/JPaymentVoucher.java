//    KriolOS POS
//    Copyright (c) 2019-2023 KriolOS.
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
package com.openbravo.pos.payment;

import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.ComboBoxValModel;
import com.openbravo.data.loader.SentenceList;
import com.openbravo.format.Formats;
import com.openbravo.pos.customers.CustomerInfoExt;
import com.openbravo.pos.customers.DataLogicCustomers;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.util.RoundUtils;
import com.openbravo.pos.voucher.DataLogicVouchers;
import com.openbravo.pos.voucher.VoucherInfo;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JG uniCenta
 */
public class JPaymentVoucher extends javax.swing.JPanel implements JPaymentInterface {
    
    private static final Logger LOGGER = Logger.getLogger(JPaymentVoucher.class.getName());

    private final JPaymentNotifier paymentNotifier;

    private DataLogicCustomers dlCustomers;
    private DataLogicVouchers dataLogicVouchers;
    private ComboBoxValModel m_VoucherModel;

    private double voucherAmount;
    private double totalToPay;

    private final String voucherType; // "voucherin", "voucherout"
    private String voucherNumber;

    /**
     * Creates new form JPaymentTicket
     *
     * @param app
     * @param notifier
     * @param voucherType should be "voucherin", "voucherout"
     */
    public JPaymentVoucher(AppView app, JPaymentNotifier notifier, String voucherType) {

        this.paymentNotifier = notifier;
        this.voucherType = voucherType;
        this.voucherAmount = 0.0;
        this.totalToPay = 0.0;

        init(app);

        this.moneyEditor.addPropertyChangeListener("Edition", new RecalculateState());
    }

    private void init(AppView app) {

        try {
            dlCustomers = app.getBean(DataLogicCustomers.class);
            dataLogicVouchers = app.getBean(DataLogicVouchers.class);
   

            initComponents();

            List<VoucherInfo> voucherList = dataLogicVouchers.getVoucherList();

            m_VoucherModel = new ComboBoxValModel(voucherList);
            vouchersComboBox.setModel(m_VoucherModel);

            customerName.setText(null);

        } catch (BasicException ex) {
            LOGGER.log(Level.WARNING, "Exeception on init: ", ex);
        }
    }

    /**
     *
     * @param customerext
     * @param dTotal
     * @param transID
     */
    @Override
    public void activate(CustomerInfoExt customerext, double dTotal, String transID) {

        totalToPay = dTotal;
        moneyEditor.reset();
        keypadPanel.setEnabled(false);
        moneyEditor.setEnabled(false);

        setStates(null);
    }

    /**
     *
     * @return
     */
    @Override
    public Component getComponent() {
        return this;
    }

    /**
     *
     * @return
     */
    @Override
    public PaymentInfo executePayment() {
        return new PaymentInfoTicket(voucherAmount, voucherType, voucherNumber);

    }

    private class RecalculateState implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            int iCompare = RoundUtils.compare(voucherAmount, totalToPay);
            paymentNotifier.setStatus(voucherAmount > 0.0, iCompare >= 0);
        }
    }

    private void setStates(VoucherInfo voucherInfo) {

        cleanStates();
        if (voucherInfo != null) {
            if (VoucherInfo.VOUCHER_STATUS_AVAILABLE.equals(voucherInfo.getStatus())) {

                voucherAmount = voucherInfo.getAmount();
                moneyEditor.setDoubleValue(voucherAmount);
                voucherValue.setText(Formats.CURRENCY.formatValue(voucherAmount));
                customerName.setText(voucherInfo.getCustomerName());
                voucherNumber = voucherInfo.getVoucherNumber();
                voucherStatus.setText(voucherInfo.printStatus());
            } else if (VoucherInfo.VOUCHER_STATUS_REDEEMED.equals(voucherInfo.getStatus())) {
                voucherStatus.setText(voucherInfo.printStatus());
            }
        }
    }

    private void cleanStates() {
        voucherNumber = null;
        voucherAmount = 0.0;
        voucherValue.setText(null);
        moneyEditor.setDoubleValue(null);
        customerName.setText(null);
        voucherStatus.setText(null);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel4 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        vouchersComboBox = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        voucherValue = new javax.swing.JLabel();
        webLblCustomer = new javax.swing.JLabel();
        customerName = new javax.swing.JLabel();
        voucherStatus = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jPanel11 = new javax.swing.JPanel();
        jPanel12 = new javax.swing.JPanel();
        keypadPanel = new com.openbravo.editor.JEditorKeys();
        jPanel1 = new javax.swing.JPanel();
        moneyEditor = new com.openbravo.editor.JEditorCurrencyPositive();

        setLayout(new java.awt.BorderLayout());

        jLabel5.setFont(jLabel5.getFont());
        jLabel5.setLabelFor(vouchersComboBox);
        jLabel5.setText(AppLocal.getIntString("label.voucher")); // NOI18N
        jLabel5.setPreferredSize(new java.awt.Dimension(100, 30));

        vouchersComboBox.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        vouchersComboBox.setPreferredSize(new java.awt.Dimension(180, 30));
        vouchersComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                vouchersComboBoxActionPerformed(evt);
            }
        });

        jLabel1.setFont(jLabel1.getFont());
        jLabel1.setLabelFor(voucherStatus);
        jLabel1.setText(AppLocal.getIntString("label.voucherValue")); // NOI18N
        jLabel1.setPreferredSize(new java.awt.Dimension(100, 30));

        voucherValue.setBackground(new java.awt.Color(248, 248, 248));
        voucherValue.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        voucherValue.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        voucherValue.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(javax.swing.UIManager.getDefaults().getColor("Button.darkShadow")), javax.swing.BorderFactory.createEmptyBorder(1, 4, 1, 4)));
        voucherValue.setOpaque(true);
        voucherValue.setPreferredSize(new java.awt.Dimension(180, 30));

        webLblCustomer.setFont(webLblCustomer.getFont());
        webLblCustomer.setLabelFor(customerName);
        webLblCustomer.setText(AppLocal.getIntString("label.customer")); // NOI18N
        webLblCustomer.setToolTipText(AppLocal.getIntString("label.customer")); // NOI18N
        webLblCustomer.setPreferredSize(new java.awt.Dimension(100, 30));

        customerName.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        customerName.setText(AppLocal.getIntString("label.customer")); // NOI18N
        customerName.setToolTipText("");
        customerName.setPreferredSize(new java.awt.Dimension(100, 30));

        voucherStatus.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        voucherStatus.setText(AppLocal.getIntString("label.voucherStatus")); // NOI18N
        voucherStatus.setToolTipText("");
        voucherStatus.setPreferredSize(new java.awt.Dimension(100, 30));

        jLabel6.setFont(jLabel6.getFont());
        jLabel6.setLabelFor(voucherStatus);
        jLabel6.setText(AppLocal.getIntString("label.voucherStatus")); // NOI18N
        jLabel6.setPreferredSize(new java.awt.Dimension(100, 30));

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(webLblCustomer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(customerName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(voucherValue, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(vouchersComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 2, Short.MAX_VALUE))
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(voucherStatus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(vouchersComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(voucherStatus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(voucherValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(webLblCustomer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(customerName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(245, 245, 245))
        );

        add(jPanel4, java.awt.BorderLayout.CENTER);

        jPanel11.setLayout(new java.awt.BorderLayout());

        jPanel12.setLayout(new javax.swing.BoxLayout(jPanel12, javax.swing.BoxLayout.Y_AXIS));
        jPanel12.add(keypadPanel);

        jPanel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jPanel1.setLayout(new java.awt.BorderLayout());

        moneyEditor.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        moneyEditor.setPreferredSize(new java.awt.Dimension(130, 30));
        jPanel1.add(moneyEditor, java.awt.BorderLayout.CENTER);

        jPanel12.add(jPanel1);

        jPanel11.add(jPanel12, java.awt.BorderLayout.NORTH);

        add(jPanel11, java.awt.BorderLayout.EAST);
    }// </editor-fold>//GEN-END:initComponents

    private void vouchersComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vouchersComboBoxActionPerformed

        VoucherInfo m_voucherInfo = null;
        if (m_VoucherModel.getSelectedKey() != null) {
            try {
                String id = m_VoucherModel.getSelectedKey().toString();
                m_voucherInfo = dataLogicVouchers.getVoucherInfo(id);
            } catch (BasicException ex) {
                LOGGER.log(Level.WARNING, "Exeception on select voucher: ", ex);
            }
        }

        setStates(m_voucherInfo);
    }//GEN-LAST:event_vouchersComboBoxActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel customerName;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel4;
    private com.openbravo.editor.JEditorKeys keypadPanel;
    private com.openbravo.editor.JEditorCurrencyPositive moneyEditor;
    private javax.swing.JLabel voucherStatus;
    private javax.swing.JLabel voucherValue;
    private javax.swing.JComboBox vouchersComboBox;
    private javax.swing.JLabel webLblCustomer;
    // End of variables declaration//GEN-END:variables

}
