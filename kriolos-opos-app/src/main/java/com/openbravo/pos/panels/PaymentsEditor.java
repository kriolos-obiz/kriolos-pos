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

package com.openbravo.pos.panels;

import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.ComboBoxValModel;
import com.openbravo.data.loader.IKeyed;
import com.openbravo.data.user.DirtyManager;
import com.openbravo.data.user.EditorRecord;
import com.openbravo.basic.AppLocal;
import com.openbravo.pos.payment.PaymentMovement;
import java.awt.Component;
import java.util.Date;
import java.util.UUID;


/**
 * Editor panel for cash drawer movements (Cash In / Cash Out).
 *
 * @author adrianromero
 * @author KriolOS
 */
public final class PaymentsEditor extends javax.swing.JPanel implements EditorRecord<PaymentMovement> {
    
    private final ComboBoxValModel m_ReasonModel;
    
    private String m_sId;
    private String m_sPaymentId;
    private Date datenew;
   
    private final AppView m_App;
    private String m_sNotes;
    
    /**
     * Creates new form PaymentsEditor without AppView for headless testing.
     *
     * @param dirty DirtyManager listener
     */
    public PaymentsEditor(DirtyManager dirty) {
        this(null, dirty);
    }

    /**
     * Creates new form PaymentsEditor
     *
     * @param oApp  active AppView
     * @param dirty DirtyManager listener
     */
    public PaymentsEditor(AppView oApp, DirtyManager dirty) {
        
        m_App = oApp;
        
        initComponents();
       
        m_ReasonModel = new ComboBoxValModel();
        m_ReasonModel.add(new PaymentReasonPositive("cashin", AppLocal.getIntString("transpayment.cashin")));
        m_ReasonModel.add(new PaymentReasonNegative("cashout", AppLocal.getIntString("transpayment.cashout")));              
        m_jreason.setModel(m_ReasonModel);
        
        jTotal.addEditorKeys(m_jKeys);
        
        m_jreason.addActionListener(dirty);
        jTotal.addPropertyChangeListener("Text", dirty);
        m_jNotes.addPropertyChangeListener("Text", dirty);
        m_jNotes.addEditorKeys(m_jKeys);        
        
        writeValueEOF();
    }
    
    /**
     *
     */
    @Override
    public void writeValueEOF() {
        m_sId = null;
        m_sPaymentId = null;
        datenew = null;
        setReasonTotal(null, null);
        m_jreason.setEnabled(false);
        jTotal.setEnabled(false);
        m_sNotes = null;
        m_jNotes.setText(null);
        m_jNotes.setEnabled(false);
    }

    /**
     *
     */
    @Override
    public void writeValueInsert() {
        m_sId = null;
        m_sPaymentId = null;
        datenew = null;
        setReasonTotal("cashin", null);
        m_jreason.setEnabled(true);
        jTotal.setEnabled(true);   
        jTotal.activate();
        m_sNotes = null;
        m_jNotes.setEnabled(true);
        m_jNotes.setText(null);
    }
    
    /**
     *
     * @param payment the payment movement record to delete
     */
    @Override
    public void writeValueDelete(PaymentMovement payment) {
        if (payment == null) {
            writeValueEOF();
            return;
        }
        m_sId = payment.receiptId();
        datenew = payment.date();
        m_sPaymentId = payment.paymentId();
        setReasonTotal(payment.reason(), payment.total());
        m_jreason.setEnabled(false);
        jTotal.setEnabled(false);
        m_sNotes = payment.notes();
        m_jNotes.setText(m_sNotes);
        m_jNotes.setEnabled(false);
    }
    
    /**
     *
     * @param payment the payment movement record to edit
     */
    @Override
    public void writeValueEdit(PaymentMovement payment) {
        if (payment == null) {
            writeValueEOF();
            return;
        }
        m_sId = payment.receiptId();
        datenew = payment.date();
        m_sPaymentId = payment.paymentId();
        setReasonTotal(payment.reason(), payment.total());
        m_jreason.setEnabled(false);
        jTotal.setEnabled(false);
        jTotal.activate();
        m_sNotes = payment.notes();
        m_jNotes.setText(m_sNotes);
        m_jNotes.setEnabled(false);
    }
    
    /**
     *
     * @return PaymentMovement domain record created from editor inputs
     * @throws BasicException
     */
    @Override
    public PaymentMovement createValue() throws BasicException {
        String receiptId = m_sId == null ? UUID.randomUUID().toString() : m_sId;
        String activeCashIndex = m_App != null ? m_App.getActiveCashIndex() : "1";
        Date date = datenew == null ? new Date() : datenew;
        String paymentId = m_sPaymentId == null ? UUID.randomUUID().toString() : m_sPaymentId;
        String reasonKey = (String) m_ReasonModel.getSelectedKey();
        PaymentReason reason = (PaymentReason) m_ReasonModel.getSelectedItem();
        Double dtotal = jTotal.getValue();
        double total = 0.0;
        if (dtotal != null) {
            Double signed = reason == null ? dtotal : reason.addSignum(dtotal);
            total = signed != null ? signed : 0.0;
        }
        m_sNotes = m_jNotes.getText();
        String notes = m_sNotes == null ? "" : m_sNotes;

        return new PaymentMovement(
                receiptId,
                activeCashIndex,
                date,
                paymentId,
                reasonKey != null ? reasonKey : "cashin",
                total,
                notes
        );
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
     */
    @Override
    public void refresh() {
    }  
    
    private void setReasonTotal(String reasonfield, Double totalfield) {
        m_ReasonModel.setSelectedKey(reasonfield);
        PaymentReason reason = (PaymentReason) m_ReasonModel.getSelectedItem();     
        if (totalfield == null) {
            jTotal.setDoubleValue(null);
        } else if (reason == null) {
            jTotal.setDoubleValue(totalfield);
        } else {
            jTotal.setDoubleValue(reason.positivize(totalfield));
        }  
    }
    
    private static abstract class PaymentReason implements IKeyed<String> {
        private String m_sKey;
        private String m_sText;
        
        public PaymentReason(String key, String text) {
            m_sKey = key;
            m_sText = text;
        }

        @Override
        public String getKey() {
            return m_sKey;
        }
        public abstract Double positivize(Double d);
        public abstract Double addSignum(Double d);
        
        @Override
        public String toString() {
            return m_sText;
        }
    }
    private static class PaymentReasonPositive extends PaymentReason {
        public PaymentReasonPositive(String key, String text) {
            super(key, text);
        }
        @Override
        public Double positivize(Double d) {
            return d;
        }
        @Override
        public Double addSignum(Double value) {
            if (value == null) {
                return null;
            } else if (value < 0.0) {
                return -value;
            } else {
                return value;
            }
        }
    }
    private static class PaymentReasonNegative extends PaymentReason {
        public PaymentReasonNegative(String key, String text) {
            super(key, text);
        }
        @Override
        public Double positivize(Double d) {
            return d == null ? null : -d;
        }
        @Override
        public Double addSignum(Double d) {
            if (d == null) {
                return null;
            } else if (d > 0.0) {
                return -d;
            } else {
                return d;
            }
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel3 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        m_jreason = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        jTotal = new com.openbravo.editor.JEditorCurrency();
        m_jNotes = new com.openbravo.editor.JEditorString();
        jPanel2 = new javax.swing.JPanel();
        m_jKeys = new com.openbravo.editor.JEditorKeys();

        setLayout(new java.awt.BorderLayout());

        jPanel3.setMaximumSize(new java.awt.Dimension(362, 208));

        jLabel5.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel5.setText(AppLocal.getIntString("label.paymentreason")); // NOI18N
        jLabel5.setPreferredSize(new java.awt.Dimension(110, 30));

        m_jreason.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jreason.setFocusable(false);
        m_jreason.setPreferredSize(new java.awt.Dimension(200, 30));

        jLabel3.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel3.setText(AppLocal.getIntString("label.paymenttotal")); // NOI18N
        jLabel3.setPreferredSize(new java.awt.Dimension(110, 30));

        jTotal.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jTotal.setPreferredSize(new java.awt.Dimension(200, 30));

        m_jNotes.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jNotes.setPreferredSize(new java.awt.Dimension(132, 100));

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(m_jNotes, javax.swing.GroupLayout.DEFAULT_SIZE, 234, Short.MAX_VALUE)
                    .addComponent(m_jreason, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTotal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jreason, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jTotal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(m_jNotes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        add(jPanel3, java.awt.BorderLayout.CENTER);

        jPanel2.setLayout(new java.awt.BorderLayout());

        m_jKeys.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                m_jKeysPropertyChange(evt);
            }
        });
        jPanel2.add(m_jKeys, java.awt.BorderLayout.NORTH);

        add(jPanel2, java.awt.BorderLayout.LINE_END);
    }// </editor-fold>//GEN-END:initComponents

    private void m_jKeysPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_m_jKeysPropertyChange

    }//GEN-LAST:event_m_jKeysPropertyChange
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private com.openbravo.editor.JEditorCurrency jTotal;
    private com.openbravo.editor.JEditorKeys m_jKeys;
    private com.openbravo.editor.JEditorString m_jNotes;
    private javax.swing.JComboBox m_jreason;
    // End of variables declaration//GEN-END:variables
    
}
