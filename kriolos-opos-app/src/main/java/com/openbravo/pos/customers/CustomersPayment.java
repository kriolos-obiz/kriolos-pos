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
package com.openbravo.pos.customers;

import com.openbravo.pos.forms.JPanelView;
import com.openbravo.pos.forms.DataLogicSales;
import com.openbravo.pos.forms.DataLogicSystem;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.BeanFactoryApp;
import com.openbravo.pos.forms.BeanFactoryException;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.MessageInf;
import com.openbravo.data.user.DirtyManager;
import com.openbravo.format.Formats;
import com.openbravo.pos.payment.JPaymentSelect;
import com.openbravo.pos.payment.JPaymentSelectCustomer;
import com.openbravo.pos.payment.PaymentInfo;
import com.openbravo.pos.payment.PaymentService;
import com.openbravo.pos.payment.PaymentServiceImpl;
import com.openbravo.pos.payment.PaymentInfoTicket;
import com.openbravo.pos.printer.TicketParser;
import com.openbravo.pos.printer.TicketPrinterException;
import com.openbravo.pos.scripting.ScriptEngine;
import com.openbravo.pos.scripting.ScriptException;
import com.openbravo.pos.scripting.ScriptFactory;
import com.openbravo.pos.ticket.TicketInfo;
import com.openbravo.pos.util.RoundUtils;
import java.awt.Dimension;
import java.util.Date;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author adrianromero
 */
public class CustomersPayment extends javax.swing.JPanel implements JPanelView, BeanFactoryApp {

    private static final long serialVersionUID = 1L;

    private AppView app;
    private DataLogicCustomers dlcustomers;
    private DataLogicSales dlsales;
    private DataLogicSystem dlsystem;
    private TicketParser ttp;
    private JPaymentSelect paymentdialog;
    private PaymentService paymentService;

    private CustomerInfoExt customerext;
    private final DirtyManager dirty;

    public CustomersPayment() {

        initComponents();

        editorcard.addEditorKeys(m_jKeys);
        txtNotes.addEditorKeys(m_jKeys);
        txtPrePay.addEditorKeys(m_jKeys);

        dirty = new DirtyManager();
        txtNotes.addPropertyChangeListener("Text", dirty);
        txtPrePay.addPropertyChangeListener("Number", dirty);
    }

    @Override
    public void init(AppView app) throws BeanFactoryException {

        this.app = app;
        dlcustomers = (DataLogicCustomers) app.getBean("com.openbravo.pos.customers.DataLogicCustomers");
        dlsales = (DataLogicSales) app.getBean("com.openbravo.pos.forms.DataLogicSales");
        dlsystem = (DataLogicSystem) app.getBean("com.openbravo.pos.forms.DataLogicSystem");
        ttp = new TicketParser(app.getDeviceTicket(), dlsystem);
    }

    @Override
    public Object getBean() {
        return this;
    }

    @Override
    public String getTitle() {
        return AppLocal.getIntString("Menu.CustomersPayment");
    }

    @Override
    public void activate() throws BasicException {

        paymentService = new PaymentServiceImpl();
        paymentdialog = JPaymentSelectCustomer.getDialog(this);
        paymentdialog.init(app, paymentService);

        resetCustomer();

        editorcard.reset();
        editorcard.activate();
    }

    @Override
    public boolean deactivate() {
        if (dirty.isDirty()) {
            int res = JOptionPane.showConfirmDialog(this, AppLocal.getIntString("message.wannasave"),
                    AppLocal.getIntString("title.editor"),
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

            if (res == JOptionPane.YES_OPTION) {
                save();
                return true;
            } else {
                return res == JOptionPane.NO_OPTION;
            }
        } else {
            return true;
        }
    }

    /**
     *
     * @return
     */
    @Override
    public JComponent getComponent() {
        return this;
    }

    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    private void editCustomer(CustomerInfoExt customer) {

        customerext = customer;

        txtTaxId.setText(customer.getTaxid());
        txtName.setText(customer.getName());
        txtCard.setText(customer.getCard());
        txtNotes.reset();
        txtNotes.setText(customer.getNotes());
        txtMaxdebt.setText(Formats.CURRENCY.formatValue(customer.getMaxdebt()));
        txtCurdebt.setText(Formats.CURRENCY.formatValue(customer.getAccdebt()));
        txtCurdate.setText(Formats.DATE.formatValue(customer.getCurdate()));
        txtPrePay.setValue(null);

        txtNotes.setEnabled(true);
        txtPrePay.setEnabled(true);

        dirty.setDirty(false);

        btnSave.setEnabled(true);
        btnPay.setEnabled(true);
        btnPay.setEnabled(enablePay());

        btnPrePay.setEnabled(true);

    }

    /**
     * Enable Pay only if Debt is more than 0.0
     *
     * @return true is pay is enabled, otherwich false
     */
    private boolean enablePay() {
        return customerext != null && customerext.getCurDebt() != null && customerext.getCurDebt() > 0.0;
    }

    private void resetCustomer() {

        customerext = null;

        txtTaxId.setText(null);
        txtName.setText(null);
        txtCard.setText(null);
        txtNotes.reset();
        txtMaxdebt.setText(null);
        txtCurdebt.setText(null);
        txtCurdate.setText(null);
        txtPrePay.setValue(null);

        txtNotes.setEnabled(false);
        txtPrePay.setEnabled(false);

        dirty.setDirty(false);

        btnSave.setEnabled(false);
        btnPay.setEnabled(false);
        btnPrePay.setEnabled(false);

    }

    private void readCustomer() {

        try {
            CustomerInfoExt customer = dlsales.findCustomerExt(editorcard.getText());
            if (customer == null) {
                MessageInf msg = new MessageInf(MessageInf.SGN_WARNING,
                        AppLocal.getIntString("message.cannotfindcustomer"));
                msg.show(this);
            } else {
                editCustomer(customer);
            }

        } catch (BasicException ex) {
            MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotfindcustomer"),
                    ex);
            msg.show(this);
        }

        editorcard.reset();
        editorcard.activate();
    }

    private void save() {

        customerext.setNotes(txtNotes.getText());
        customerext.setPrePay(txtPrePay.getText());

        try {
            dlcustomers.updateCustomerExt(customerext);
            editCustomer(customerext);
        } catch (BasicException e) {
            MessageInf msg = new MessageInf(MessageInf.SGN_NOTICE, AppLocal.getIntString("message.nosave"), e);
            msg.show(this);
        }

    }

    private void printTicket(String resname, TicketInfo ticket, CustomerInfoExt customer) {

        String resource = dlsystem.getResourceAsXML(resname);
        if (resource == null) {
            MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotprintticket"));
            msg.show(this);
        } else {
            try {
                ScriptEngine script = ScriptFactory.getScriptEngine(ScriptFactory.VELOCITY);
                script.put("ticket", ticket);
                script.put("customer", customer);
                ttp.printTicket(script.eval(resource).toString());
            } catch (ScriptException | TicketPrinterException e) {
                MessageInf msg = new MessageInf(MessageInf.SGN_WARNING,
                        AppLocal.getIntString("message.cannotprintticket"), e);
                msg.show(this);
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated
    // Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        btnCustomer = new javax.swing.JButton();
        btnSave = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        btnPay = new javax.swing.JButton();
        btnPrePay = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        txtCard = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        txtCurdebt = new javax.swing.JTextField();
        txtCurdate = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        txtName = new javax.swing.JTextField();
        txtMaxdebt = new javax.swing.JTextField();
        txtPrePay = new com.openbravo.editor.JEditorCurrency();
        txtTaxId = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        lblPrePay = new javax.swing.JLabel();
        txtNotes = new com.openbravo.editor.JEditorString();
        jPanel4 = new javax.swing.JPanel();
        m_jKeys = new com.openbravo.editor.JEditorKeys();
        editorcard = new com.openbravo.editor.JEditorString();
        jButton1 = new javax.swing.JButton();

        setMaximumSize(new java.awt.Dimension(750, 450));
        setMinimumSize(new java.awt.Dimension(650, 400));
        setPreferredSize(new java.awt.Dimension(750, 450));
        setLayout(new java.awt.BorderLayout());

        jPanel2.setMaximumSize(new java.awt.Dimension(500, 55));
        jPanel2.setMinimumSize(new java.awt.Dimension(500, 55));
        jPanel2.setPreferredSize(new java.awt.Dimension(500, 55));
        jPanel2.setLayout(new java.awt.BorderLayout());

        jPanel6.setMaximumSize(new java.awt.Dimension(500, 55));
        jPanel6.setMinimumSize(new java.awt.Dimension(500, 55));
        jPanel6.setPreferredSize(new java.awt.Dimension(500, 55));

        btnCustomer
                .setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/customer_sml.png"))); // NOI18N
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("pos_messages"); // NOI18N
        btnCustomer.setToolTipText(bundle.getString("tooltip.customerpay.customer")); // NOI18N
        btnCustomer.setFocusPainted(false);
        btnCustomer.setFocusable(false);
        btnCustomer.setMargin(new java.awt.Insets(8, 14, 8, 14));
        btnCustomer.setPreferredSize(new java.awt.Dimension(110, 45));
        btnCustomer.setRequestFocusEnabled(false);
        btnCustomer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCustomerActionPerformed(evt);
            }
        });
        jPanel6.add(btnCustomer);

        btnSave.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/filesave.png"))); // NOI18N
        btnSave.setToolTipText(bundle.getString("tootltip.save")); // NOI18N
        btnSave.setFocusPainted(false);
        btnSave.setFocusable(false);
        btnSave.setMargin(new java.awt.Insets(8, 14, 8, 14));
        btnSave.setPreferredSize(new java.awt.Dimension(110, 45));
        btnSave.setRequestFocusEnabled(false);
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });
        jPanel6.add(btnSave);
        jPanel6.add(jSeparator1);

        btnPay.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        btnPay.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/pay.png"))); // NOI18N
        btnPay.setText(AppLocal.getIntString("button.pay")); // NOI18N
        btnPay.setToolTipText(bundle.getString("tooltip.customerpay.pay")); // NOI18N
        btnPay.setFocusPainted(false);
        btnPay.setFocusable(false);
        btnPay.setMargin(new java.awt.Insets(8, 14, 8, 14));
        btnPay.setMaximumSize(new java.awt.Dimension(110, 44));
        btnPay.setMinimumSize(new java.awt.Dimension(110, 44));
        btnPay.setPreferredSize(new java.awt.Dimension(110, 45));
        btnPay.setRequestFocusEnabled(false);
        btnPay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPayActionPerformed(evt);
            }
        });
        jPanel6.add(btnPay);

        btnPrePay.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        btnPrePay.setIcon(
                new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/customer_add_sml.png"))); // NOI18N
        btnPrePay.setText(AppLocal.getIntString("button.prepay")); // NOI18N
        btnPrePay.setToolTipText(bundle.getString("tooltip.prepay")); // NOI18N
        btnPrePay.setFocusPainted(false);
        btnPrePay.setFocusable(false);
        btnPrePay.setMargin(new java.awt.Insets(8, 14, 8, 14));
        btnPrePay.setMaximumSize(new java.awt.Dimension(110, 44));
        btnPrePay.setMinimumSize(new java.awt.Dimension(110, 44));
        btnPrePay.setPreferredSize(new java.awt.Dimension(110, 45));
        btnPrePay.setRequestFocusEnabled(false);
        btnPrePay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPrePayActionPerformed(evt);
            }
        });
        jPanel6.add(btnPrePay);

        jPanel2.add(jPanel6, java.awt.BorderLayout.LINE_START);

        add(jPanel2, java.awt.BorderLayout.NORTH);

        jPanel1.setMaximumSize(new java.awt.Dimension(400, 350));
        jPanel1.setMinimumSize(new java.awt.Dimension(400, 350));
        jPanel1.setName(""); // NOI18N
        jPanel1.setPreferredSize(new java.awt.Dimension(400, 350));

        jLabel3.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel3.setText(AppLocal.getIntString("label.name")); // NOI18N
        jLabel3.setPreferredSize(new java.awt.Dimension(150, 30));

        jLabel12.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel12.setText(AppLocal.getIntString("label.notes")); // NOI18N
        jLabel12.setPreferredSize(new java.awt.Dimension(150, 30));

        jLabel5.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel5.setText(AppLocal.getIntString("label.card")); // NOI18N
        jLabel5.setPreferredSize(new java.awt.Dimension(150, 30));

        txtCard.setEditable(false);
        txtCard.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        txtCard.setFocusable(false);
        txtCard.setMinimumSize(new java.awt.Dimension(64, 30));
        txtCard.setPreferredSize(new java.awt.Dimension(0, 30));
        txtCard.setRequestFocusEnabled(false);

        jLabel1.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText(AppLocal.getIntString("label.maxdebt")); // NOI18N
        jLabel1.setPreferredSize(new java.awt.Dimension(120, 30));

        jLabel2.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText(AppLocal.getIntString("label.curdebt")); // NOI18N
        jLabel2.setPreferredSize(new java.awt.Dimension(120, 30));

        txtCurdebt.setEditable(false);
        txtCurdebt.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        txtCurdebt.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtCurdebt.setFocusable(false);
        txtCurdebt.setPreferredSize(new java.awt.Dimension(120, 30));
        txtCurdebt.setRequestFocusEnabled(false);

        txtCurdate.setEditable(false);
        txtCurdate.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        txtCurdate.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtCurdate.setFocusable(false);
        txtCurdate.setPreferredSize(new java.awt.Dimension(120, 30));
        txtCurdate.setRequestFocusEnabled(false);

        jLabel6.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel6.setText(AppLocal.getIntString("label.curdate")); // NOI18N
        jLabel6.setPreferredSize(new java.awt.Dimension(120, 30));

        txtName.setEditable(false);
        txtName.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        txtName.setFocusable(false);
        txtName.setMinimumSize(new java.awt.Dimension(64, 30));
        txtName.setPreferredSize(new java.awt.Dimension(0, 30));
        txtName.setRequestFocusEnabled(false);

        txtMaxdebt.setEditable(false);
        txtMaxdebt.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        txtMaxdebt.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtMaxdebt.setFocusable(false);
        txtMaxdebt.setPreferredSize(new java.awt.Dimension(120, 30));
        txtMaxdebt.setRequestFocusEnabled(false);

        txtPrePay.setForeground(new java.awt.Color(0, 204, 255));
        txtPrePay.setToolTipText(bundle.getString("tooltip.customerpay.prepay")); // NOI18N
        txtPrePay.setEnabled(false);
        txtPrePay.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        txtPrePay.setPreferredSize(new java.awt.Dimension(200, 30));

        txtTaxId.setEditable(false);
        txtTaxId.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        txtTaxId.setFocusable(false);
        txtTaxId.setMinimumSize(new java.awt.Dimension(64, 30));
        txtTaxId.setPreferredSize(new java.awt.Dimension(150, 30));
        txtTaxId.setRequestFocusEnabled(false);

        jLabel7.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel7.setText(AppLocal.getIntString("label.taxid")); // NOI18N
        jLabel7.setPreferredSize(new java.awt.Dimension(150, 30));

        lblPrePay.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        lblPrePay.setText(AppLocal.getIntString("label.prepay")); // NOI18N
        lblPrePay.setPreferredSize(new java.awt.Dimension(120, 30));

        txtNotes.setToolTipText(bundle.getString("tooltip.customerpay.notes")); // NOI18N
        txtNotes.setEnabled(false);
        txtNotes.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        txtNotes.setPreferredSize(new java.awt.Dimension(250, 100));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 140,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, 240,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 140,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(txtCard, javax.swing.GroupLayout.PREFERRED_SIZE, 240,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 140,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(txtTaxId, javax.swing.GroupLayout.PREFERRED_SIZE, 240,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addGroup(jPanel1Layout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                140, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(lblPrePay, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addGroup(jPanel1Layout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING,
                                                                false)
                                                        .addComponent(txtNotes, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(txtPrePay, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addGroup(jPanel1Layout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                                                .addComponent(txtMaxdebt,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(
                                                                        javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(txtCurdebt,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                                                .addComponent(jLabel1,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(
                                                                        javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(jLabel2,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(jPanel1Layout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(txtCurdate,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE))))
                                .addContainerGap()));
        jPanel1Layout.setVerticalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(txtTaxId, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(txtCard, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(txtNotes, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(lblPrePay, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(txtPrePay, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(30, 30, 30)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(txtCurdebt, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(txtMaxdebt, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(txtCurdate, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap(40, Short.MAX_VALUE)));

        add(jPanel1, java.awt.BorderLayout.CENTER);

        jPanel4.setMaximumSize(new java.awt.Dimension(300, 400));
        jPanel4.setMinimumSize(new java.awt.Dimension(300, 400));
        jPanel4.setPreferredSize(new java.awt.Dimension(300, 400));
        jPanel4.setLayout(new javax.swing.BoxLayout(jPanel4, javax.swing.BoxLayout.Y_AXIS));

        m_jKeys.setMaximumSize(new java.awt.Dimension(300, 300));
        m_jKeys.setMinimumSize(new java.awt.Dimension(300, 300));
        m_jKeys.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jKeysActionPerformed(evt);
            }
        });
        jPanel4.add(m_jKeys);

        editorcard.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        editorcard.setMaximumSize(new java.awt.Dimension(2147483647, 30));
        editorcard.setMinimumSize(new java.awt.Dimension(100, 30));
        editorcard.setPreferredSize(new java.awt.Dimension(100, 30));
        jPanel4.add(editorcard);

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/ok.png"))); // NOI18N
        jButton1.setFocusPainted(false);
        jButton1.setFocusable(false);
        jButton1.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jButton1.setMargin(new java.awt.Insets(14, 14, 8, 14));
        jButton1.setMaximumSize(new java.awt.Dimension(104, 44));
        jButton1.setMinimumSize(new java.awt.Dimension(104, 44));
        jButton1.setPreferredSize(new java.awt.Dimension(110, 45));
        jButton1.setRequestFocusEnabled(false);
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel4.add(jButton1);

        add(jPanel4, java.awt.BorderLayout.EAST);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButton1ActionPerformed

        readCustomer();

    }// GEN-LAST:event_jButton1ActionPerformed

    private void m_jKeysActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_m_jKeysActionPerformed

        readCustomer();

    }// GEN-LAST:event_m_jKeysActionPerformed

    private void btnCustomerActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnCustomerActionPerformed

        JCustomerFinder finder = JCustomerFinder.getCustomerFinder(this, dlcustomers);
        finder.search(null);
        finder.setVisible(true);
        CustomerInfo customer = finder.getSelectedCustomer();
        if (customer != null) {
            try {
                CustomerInfoExt c = dlsales.loadCustomerExt(customer.getId());
                if (c == null) {
                    MessageInf msg = new MessageInf(MessageInf.SGN_WARNING,
                            AppLocal.getIntString("message.cannotfindcustomer"));
                    msg.show(this);
                } else {
                    editCustomer(c);
                }
            } catch (BasicException ex) {
                MessageInf msg = new MessageInf(MessageInf.SGN_WARNING,
                        AppLocal.getIntString("message.cannotfindcustomer"), ex);
                msg.show(this);
            }
        }
        editorcard.reset();
        editorcard.activate();

    }// GEN-LAST:event_btnCustomerActionPerformed

    private void btnPayActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnPayActionPerformed

        paymentdialog.setPrintSelected(true);

        // Check if Pay is enabled
        if (enablePay() && paymentdialog.showDialog(customerext.getAccdebt(), null)) {

            // Save the ticket
            TicketInfo ticket = new TicketInfo();
            ticket.setTicketType(TicketInfo.RECEIPT_PAYMENT);

            List<PaymentInfo> payments = paymentdialog.getSelectedPayments();

            double total = 0.0;
            for (PaymentInfo p : payments) {
                total += p.getTotal();
            }

            payments.add(new PaymentInfoTicket(-total, "debtpaid"));

            ticket.setPayments(payments);

            ticket.setUser(app.getAppUserView().getUser().getUserInfo());
            ticket.setActiveCash(app.getActiveCashIndex());
            ticket.setDate(new Date());
            ticket.setCustomer(customerext);

            try {
                dlsales.saveTicket(ticket, app.getInventoryLocation());
            } catch (BasicException eData) {
                MessageInf msg = new MessageInf(MessageInf.SGN_NOTICE, AppLocal.getIntString("message.nosaveticket"),
                        eData);
                msg.show(this);
            }

            // reload customer
            CustomerInfoExt c;
            try {
                c = dlsales.loadCustomerExt(customerext.getId());
                if (c == null) {
                    MessageInf msg = new MessageInf(MessageInf.SGN_WARNING,
                            AppLocal.getIntString("message.cannotfindcustomer"));
                    msg.show(this);
                } else {
                    editCustomer(c);
                }
            } catch (BasicException ex) {
                c = null;
                MessageInf msg = new MessageInf(MessageInf.SGN_WARNING,
                        AppLocal.getIntString("message.cannotfindcustomer"), ex);
                msg.show(this);
            }

            String ptinerSelected = paymentdialog.isPrintSelected() ? "Printer.CustomerPaid" : "Printer.CustomerPaid2";
            printTicket(ptinerSelected, ticket, c);
        }

        editorcard.reset();
        editorcard.activate();

    }// GEN-LAST:event_btnPayActionPerformed

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnSaveActionPerformed

        if (dirty.isDirty()) {
            save();

            editorcard.reset();
            editorcard.activate();
        }

    }// GEN-LAST:event_btnSaveActionPerformed

    private void btnPrePayActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnPrePayActionPerformed

        txtPrePay.setFocusable(true);
        txtPrePay.requestFocusInWindow();

        if (!StringUtils.isNumeric(txtPrePay.getText())) {
            double prepay = Double.parseDouble(txtPrePay.getText());
            Formats.CURRENCY.formatValue(RoundUtils.getValue(prepay));
            paymentdialog.setPrintSelected(true);

            if (paymentdialog.showDialog(prepay, null)) {

                TicketInfo ticket = new TicketInfo();
                ticket.setTicketType(TicketInfo.RECEIPT_PAYMENT);
                List<PaymentInfo> payments = paymentdialog.getSelectedPayments();

                double total = 0.0;
                for (PaymentInfo p : payments) {
                    total += p.getTotal();
                }

                total = Double.parseDouble(txtPrePay.getText());

                payments.add(new PaymentInfoTicket(-total, "debtpaid"));

                ticket.setPayments(payments);

                ticket.setUser(app.getAppUserView().getUser().getUserInfo());
                ticket.setActiveCash(app.getActiveCashIndex());
                ticket.setDate(new Date());
                ticket.setCustomer(customerext);

                try {
                    dlsales.saveTicket(ticket, app.getInventoryLocation());
                } catch (BasicException eData) {
                    MessageInf msg = new MessageInf(MessageInf.SGN_NOTICE,
                            AppLocal.getIntString("message.nosaveticket"), eData);
                    msg.show(this);
                }

                CustomerInfoExt c = null;
                ;
                try {
                    c = dlsales.loadCustomerExt(customerext.getId());
                    if (c == null) {
                        MessageInf msg = new MessageInf(MessageInf.SGN_WARNING,
                                AppLocal.getIntString("message.cannotfindcustomer"));
                        msg.show(this);
                    } else {
                        editCustomer(c);
                    }
                } catch (BasicException ex) {
                    MessageInf msg = new MessageInf(MessageInf.SGN_WARNING,
                            AppLocal.getIntString("message.cannotfindcustomer"), ex);
                    msg.show(this);
                }

                printTicket(paymentdialog.isPrintSelected()
                        ? "Printer.CustomerPaid"
                        : "Printer.CustomerPaid2",
                        ticket, c);
            }

            editorcard.reset();
            editorcard.activate();
        } else {
            MessageInf msg = new MessageInf(MessageInf.SGN_WARNING,
                    AppLocal.getIntString("customerspayment.prepay.empty"));
        }
    }// GEN-LAST:event_btnPrePayActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCustomer;
    private javax.swing.JButton btnPay;
    private javax.swing.JButton btnPrePay;
    private javax.swing.JButton btnSave;
    private com.openbravo.editor.JEditorString editorcard;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel lblPrePay;
    private com.openbravo.editor.JEditorKeys m_jKeys;
    private javax.swing.JTextField txtCard;
    private javax.swing.JTextField txtCurdate;
    private javax.swing.JTextField txtCurdebt;
    private javax.swing.JTextField txtMaxdebt;
    private javax.swing.JTextField txtName;
    private com.openbravo.editor.JEditorString txtNotes;
    private com.openbravo.editor.JEditorCurrency txtPrePay;
    private javax.swing.JTextField txtTaxId;
    // End of variables declaration//GEN-END:variables
}
