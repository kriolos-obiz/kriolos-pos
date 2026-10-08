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

import com.openbravo.data.gui.MessageInf;
import com.openbravo.format.Formats;
import com.openbravo.pos.customers.CustomerInfoExt;
import com.openbravo.pos.forms.AppConfig;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.DataLogicSystem;
import com.openbravo.pos.forms.ResourceService;
import com.openbravo.pos.scripting.ScriptEngine;
import com.openbravo.pos.scripting.ScriptException;
import com.openbravo.pos.scripting.ScriptFactory;
import com.openbravo.pos.util.RoundUtils;
import com.openbravo.pos.util.ThumbNailBuilder;
import java.awt.Component;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.SwingConstants;

/**
 *
 * @author adrianromero
 */
public class JPaymentCashPos extends javax.swing.JPanel implements JPaymentInterface {

    private static final long serialVersionUID = 1L;

    private final JPaymentNotifier m_notifier;
    private double m_dPaid;
    private double m_dTotal;
    private final Boolean priceWith00;

    /**
     * Creates new form JPaymentCash
     *
     * @param notifier
     * @param resourceService
     */
    public JPaymentCashPos(JPaymentNotifier notifier, ResourceService resourceService) {

        m_notifier = notifier;

        initComponents();

        m_jTendered.addPropertyChangeListener("Edition", new RecalculateState());
        m_jTendered.addEditorKeys(m_jKeys);

        AppConfig m_config = AppConfig.getInstance();
        m_config.load();
        priceWith00 = ("true".equals(m_config.getProperty("till.pricewith00")));
        if (priceWith00) {
            // use '00' instead of '.'
            m_jKeys.dotIs00(true);
        }
//        m_config=null;

        String code = (resourceService != null) ? resourceService.getResourceAsXML("payment.cash") : null;
        if (code != null) {
            try {
                ScriptEngine script = ScriptFactory.getScriptEngine(ScriptFactory.BEANSHELL);
                script.put("payment", new ScriptPaymentCash(resourceService));
                script.eval(code);
            } catch (ScriptException e) {
                MessageInf msg = new MessageInf(MessageInf.SGN_NOTICE, AppLocal.getIntString("message.cannotexecute"), e);
                msg.show(this);
            }
        }
    }

    @Deprecated
    public JPaymentCashPos(JPaymentNotifier notifier, DataLogicSystem dlSystem) {
        this(notifier, (ResourceService) dlSystem);
    }

    @Override
    public void activate(CustomerInfoExt customerext, double dTotal, String transID) {
        m_dTotal = dTotal;
        m_jTendered.reset();
        m_jTendered.activate();

        printState();
    }

    @Override
    public PaymentInfo executePayment() {
        if (m_dPaid - m_dTotal >= 0.0) {
            // pago completo
            return new PaymentInfoCash(m_dTotal, m_dPaid);
        } else {
            // pago parcial
            return new PaymentInfoCash(m_dPaid, m_dPaid);
        }
    }

    @Override
    public Component getComponent() {
        return this;
    }

    private void printState() {

        Double value = m_jTendered.getValue();
        if (value == null || value == 0.0) {
            m_dPaid = m_dTotal;
        } else {
            m_dPaid = value;

        }

        int iCompare = RoundUtils.compare(m_dPaid, m_dTotal);

        m_jMoneyEuros.setText(Formats.CURRENCY.formatValue(m_dPaid));
        m_jChangeEuros.setText(iCompare > 0
                ? Formats.CURRENCY.formatValue(m_dPaid - m_dTotal)
                : null);

        m_notifier.setStatus(m_dPaid > 0.0, iCompare >= 0);
    }

    private class RecalculateState implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            printState();
        }
    }

    public class ScriptPaymentCash {

        private final ResourceService resourceService;
        @Deprecated
        private final DataLogicSystem dlSystem;
        private final ThumbNailBuilder tnbbutton;
        private final AppConfig m_config;

        public ScriptPaymentCash(ResourceService resourceService) {
            this.m_config = AppConfig.getInstance();
            this.resourceService = resourceService;
            this.dlSystem = (resourceService instanceof DataLogicSystem) ? (DataLogicSystem) resourceService : null;
            tnbbutton = new ThumbNailBuilder(64, 48, "com/openbravo/images/cash.png");
        }

        @Deprecated
        public ScriptPaymentCash(DataLogicSystem dlSystem) {
            this((ResourceService) dlSystem);
        }

        public void addButton(String image, double amount) {
            JButton btn = new JButton();
            try {
                if ((m_config.getProperty("payments.textoverlay")).equals("false")) {
                    btn.setIcon(new ImageIcon(tnbbutton.getThumbNail(resourceService.getResourceAsImage(image))));
                } else {
                    btn.setIcon(new ImageIcon(tnbbutton.getThumbNail(resourceService.getResourceAsImage(image), Formats.CURRENCY.formatValue(amount))));
                }
            } catch (Exception e) {
                btn.setIcon(new ImageIcon(tnbbutton.getThumbNail(resourceService != null ? resourceService.getResourceAsImage(image) : null, Formats.CURRENCY.formatValue(amount))));
            }

            btn.setFocusPainted(false);
            btn.setFocusable(false);
            btn.setRequestFocusEnabled(false);
            btn.setHorizontalTextPosition(SwingConstants.CENTER);
            btn.setVerticalTextPosition(SwingConstants.BOTTOM);
            btn.setMargin(new Insets(2, 2, 2, 2));
            btn.addActionListener(new AddAmount(amount));
            jPanel6.add(btn);
        }
    }

    private class AddAmount implements ActionListener {

        private final double amount;

        public AddAmount(double amount) {
            this.amount = amount;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Double tendered = m_jTendered.getValue();

            if (tendered == null) {
                m_jTendered.setDoubleValue(amount);
            } else {
                m_jTendered.setDoubleValue(tendered + amount);

            }

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

        jPanel5 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        m_jChangeEuros = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        m_jMoneyEuros = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        m_jKeys = new com.openbravo.editor.JEditorKeys();
        jPanel3 = new javax.swing.JPanel();
        m_jTendered = new com.openbravo.editor.JEditorCurrencyPositive();

        setPreferredSize(new java.awt.Dimension(700, 400));
        setLayout(new java.awt.BorderLayout());

        jPanel5.setLayout(new java.awt.BorderLayout());

        jPanel4.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jPanel4.setPreferredSize(new java.awt.Dimension(450, 70));
        jPanel4.setLayout(null);

        m_jChangeEuros.setBackground(new java.awt.Color(255, 255, 255));
        m_jChangeEuros.setFont(m_jChangeEuros.getFont().deriveFont(m_jChangeEuros.getFont().getSize()+5f));
        m_jChangeEuros.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        m_jChangeEuros.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(javax.swing.UIManager.getDefaults().getColor("Button.darkShadow")), javax.swing.BorderFactory.createEmptyBorder(1, 4, 1, 4)));
        m_jChangeEuros.setOpaque(true);
        m_jChangeEuros.setPreferredSize(new java.awt.Dimension(180, 30));
        jPanel4.add(m_jChangeEuros);
        m_jChangeEuros.setBounds(120, 36, 180, 30);

        jLabel6.setFont(jLabel6.getFont());
        jLabel6.setText(AppLocal.getIntString("label.ChangeCash")); // NOI18N
        jLabel6.setPreferredSize(new java.awt.Dimension(100, 30));
        jPanel4.add(jLabel6);
        jLabel6.setBounds(10, 36, 100, 30);

        jLabel8.setFont(jLabel8.getFont());
        jLabel8.setText(AppLocal.getIntString("label.InputCash")); // NOI18N
        jLabel8.setPreferredSize(new java.awt.Dimension(100, 30));
        jPanel4.add(jLabel8);
        jLabel8.setBounds(10, 4, 100, 30);

        m_jMoneyEuros.setBackground(new java.awt.Color(248, 248, 248));
        m_jMoneyEuros.setFont(m_jMoneyEuros.getFont().deriveFont(m_jMoneyEuros.getFont().getSize()+5f));
        m_jMoneyEuros.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        m_jMoneyEuros.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(javax.swing.UIManager.getDefaults().getColor("Button.darkShadow")), javax.swing.BorderFactory.createEmptyBorder(1, 4, 1, 4)));
        m_jMoneyEuros.setOpaque(true);
        m_jMoneyEuros.setPreferredSize(new java.awt.Dimension(180, 30));
        jPanel4.add(m_jMoneyEuros);
        m_jMoneyEuros.setBounds(120, 4, 180, 30);

        jPanel5.add(jPanel4, java.awt.BorderLayout.NORTH);

        jPanel6.setPreferredSize(new java.awt.Dimension(450, 10));
        jPanel6.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        jPanel5.add(jPanel6, java.awt.BorderLayout.CENTER);

        add(jPanel5, java.awt.BorderLayout.CENTER);

        jPanel2.setLayout(new java.awt.BorderLayout());

        jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.Y_AXIS));
        jPanel1.add(m_jKeys);

        jPanel3.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jPanel3.setLayout(new java.awt.BorderLayout());

        m_jTendered.setFont(m_jTendered.getFont().deriveFont(m_jTendered.getFont().getSize()+5f));
        m_jTendered.setMaximumSize(new java.awt.Dimension(130, 30));
        m_jTendered.setMinimumSize(new java.awt.Dimension(130, 30));
        m_jTendered.setPreferredSize(new java.awt.Dimension(130, 30));
        jPanel3.add(m_jTendered, java.awt.BorderLayout.CENTER);

        jPanel1.add(jPanel3);

        jPanel2.add(jPanel1, java.awt.BorderLayout.NORTH);

        add(jPanel2, java.awt.BorderLayout.LINE_END);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JLabel m_jChangeEuros;
    private com.openbravo.editor.JEditorKeys m_jKeys;
    private javax.swing.JLabel m_jMoneyEuros;
    private com.openbravo.editor.JEditorCurrencyPositive m_jTendered;
    // End of variables declaration//GEN-END:variables

}
