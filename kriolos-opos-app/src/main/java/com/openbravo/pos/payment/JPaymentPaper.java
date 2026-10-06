//    KriolOS POS
//    Copyright (c) 2019-2023 KriolOS
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
import com.openbravo.format.Formats;
import com.openbravo.pos.customers.CustomerInfoExt;
import com.openbravo.pos.customers.DataLogicCustomers;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.DataLogicSales;
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
public class JPaymentPaper extends javax.swing.JPanel implements JPaymentInterface {
    
    private static final Logger LOGGER = Logger.getLogger(JPaymentPaper.class.getName());

    private final JPaymentNotifier paymentNotifier;

    private DataLogicSales datalogicSales;
    private DataLogicCustomers datalogicCustomers;
    private DataLogicVouchers dataLogicVouchers;
    private ComboBoxValModel voucherComboBoxValModel;

    private double totalPayed;
    private double totalToPay;
    private final String paymentMethod; 
    private String voucherNumber;

    /**
     * Creates new form JPaymentTicket
     *
     * @param app
     * @param notifier
     * @param paymentMethod
     */
    public JPaymentPaper(AppView app, JPaymentNotifier notifier, String paymentMethod) {

        this.paymentNotifier = notifier;
        this.paymentMethod = paymentMethod;
        this.totalToPay = 0.0;

        init(app);

        this.m_jTendered.addPropertyChangeListener("Edition", new RecalculateState());
        this.m_jTendered.addEditorKeys(m_jKeys);

    }

    private void init(AppView app) {

        try {
            datalogicSales = app.getBean(DataLogicSales.class);
            datalogicCustomers = app.getBean(DataLogicCustomers.class);
            dataLogicVouchers = app.getBean(DataLogicVouchers.class);
         

            initComponents();

            voucherComboBoxValModel = new ComboBoxValModel();
            List<VoucherInfo> voucherList = dataLogicVouchers.getVoucherList();
            voucherList.add(0, null);

            voucherComboBoxValModel = new ComboBoxValModel(voucherList);
            m_jVoucher.setModel(voucherComboBoxValModel);

            webLblcustomerName.setText(null);

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
        m_jTendered.reset();
        m_jKeys.setEnabled(false);
        m_jTendered.setEnabled(false);

        printState();
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
        try {
            String id = voucherComboBoxValModel.getSelectedKey().toString();
            VoucherInfo m_voucherInfo1 = dataLogicVouchers.getVoucherInfo(id);
            voucherNumber = m_voucherInfo1.getVoucherNumber();
        } catch (BasicException ex) {
            LOGGER.log(Level.WARNING, "Exeception on execute payment: ", ex);
        }
        
        return new PaymentInfoTicket(totalPayed, paymentMethod, voucherNumber);

    }

    private void printState() {

        Double tenderedValue = m_jTendered.getValue();
        if (tenderedValue == null) {
            totalPayed = totalToPay;
        } else {
            totalPayed = tenderedValue;
        }

        m_jMoneyEuros.setText(Formats.CURRENCY.formatValue(totalPayed));

        int iCompare = RoundUtils.compare(totalPayed, totalToPay);

        // it is allowed to pay more
        paymentNotifier.setStatus(totalPayed > 0.0, iCompare >= 0);
    }

    private class RecalculateState implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            printState();
        }
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
        m_jVoucher = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        m_jMoneyEuros = new javax.swing.JLabel();
        webLblCustomer = new javax.swing.JLabel();
        webLblcustomerName = new javax.swing.JLabel();
        voucherID = new javax.swing.JLabel();
        jPanel11 = new javax.swing.JPanel();
        jPanel12 = new javax.swing.JPanel();
        m_jKeys = new com.openbravo.editor.JEditorKeys();
        jPanel1 = new javax.swing.JPanel();
        m_jTendered = new com.openbravo.editor.JEditorCurrencyPositive();

        setLayout(new java.awt.BorderLayout());

        jLabel5.setFont(jLabel5.getFont());
        jLabel5.setLabelFor(m_jVoucher);
        jLabel5.setText(AppLocal.getIntString("label.voucher")); // NOI18N
        jLabel5.setPreferredSize(new java.awt.Dimension(100, 30));

        m_jVoucher.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jVoucher.setPreferredSize(new java.awt.Dimension(180, 30));
        m_jVoucher.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jVoucherActionPerformed(evt);
            }
        });

        jLabel1.setFont(jLabel1.getFont());
        jLabel1.setText(AppLocal.getIntString("label.voucherValue")); // NOI18N
        jLabel1.setPreferredSize(new java.awt.Dimension(100, 30));

        m_jMoneyEuros.setBackground(new java.awt.Color(248, 248, 248));
        m_jMoneyEuros.setFont(m_jMoneyEuros.getFont());
        m_jMoneyEuros.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        m_jMoneyEuros.setAlignmentX(0.5F);
        m_jMoneyEuros.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(javax.swing.UIManager.getDefaults().getColor("Button.darkShadow")), javax.swing.BorderFactory.createEmptyBorder(1, 4, 1, 4)));
        m_jMoneyEuros.setOpaque(true);
        m_jMoneyEuros.setPreferredSize(new java.awt.Dimension(180, 30));

        webLblCustomer.setText(AppLocal.getIntString("label.customer")); // NOI18N
        webLblCustomer.setToolTipText("");
        webLblCustomer.setFont(webLblCustomer.getFont());
        webLblCustomer.setPreferredSize(new java.awt.Dimension(100, 30));

        webLblcustomerName.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        webLblcustomerName.setText(AppLocal.getIntString("label.customer")); // NOI18N
        webLblcustomerName.setToolTipText("");
        webLblcustomerName.setPreferredSize(new java.awt.Dimension(100, 30));

        voucherID.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        voucherID.setText(AppLocal.getIntString("label.customer")); // NOI18N
        voucherID.setToolTipText("");
        voucherID.setPreferredSize(new java.awt.Dimension(100, 30));

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(webLblCustomer, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(voucherID, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(webLblcustomerName, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(m_jVoucher, javax.swing.GroupLayout.PREFERRED_SIZE, 174, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(m_jMoneyEuros, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(4, 4, 4)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jVoucher, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jMoneyEuros, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(webLblCustomer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(webLblcustomerName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(voucherID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(197, 197, 197))
        );

        add(jPanel4, java.awt.BorderLayout.CENTER);

        jPanel11.setLayout(new java.awt.BorderLayout());

        jPanel12.setLayout(new javax.swing.BoxLayout(jPanel12, javax.swing.BoxLayout.Y_AXIS));
        jPanel12.add(m_jKeys);

        jPanel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jPanel1.setLayout(new java.awt.BorderLayout());
        jPanel1.add(m_jTendered, java.awt.BorderLayout.PAGE_START);

        jPanel12.add(jPanel1);

        jPanel11.add(jPanel12, java.awt.BorderLayout.NORTH);

        add(jPanel11, java.awt.BorderLayout.EAST);
    }// </editor-fold>//GEN-END:initComponents

    private void m_jVoucherActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jVoucherActionPerformed

        if (voucherComboBoxValModel.getSelectedKey() != null) {
            try {

                String id = voucherComboBoxValModel.getSelectedKey().toString();
                VoucherInfo m_voucherInfo = dataLogicVouchers.getVoucherInfo(id);

                if (m_voucherInfo != null) {
                    m_jTendered.setDoubleValue(m_voucherInfo.getAmount());
                    m_jMoneyEuros.setText(Formats.CURRENCY.formatValue(m_voucherInfo.getAmount()));

                    webLblcustomerName.setText(m_voucherInfo.getCustomerName());

                    printState();
                }
            } catch (BasicException ex) {
                LOGGER.log(Level.WARNING, "Exeception on select voucher: ", ex);
            }

        }
    }//GEN-LAST:event_m_jVoucherActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel4;
    private com.openbravo.editor.JEditorKeys m_jKeys;
    private javax.swing.JLabel m_jMoneyEuros;
    private com.openbravo.editor.JEditorCurrencyPositive m_jTendered;
    private javax.swing.JComboBox m_jVoucher;
    private javax.swing.JLabel voucherID;
    private javax.swing.JLabel webLblCustomer;
    private javax.swing.JLabel webLblcustomerName;
    // End of variables declaration//GEN-END:variables

}
