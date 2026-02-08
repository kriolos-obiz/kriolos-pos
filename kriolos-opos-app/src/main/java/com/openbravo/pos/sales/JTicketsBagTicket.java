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
package com.openbravo.pos.sales;

import com.openbravo.pos.customers.DataLogicCustomers;
import com.openbravo.pos.panels.JTicketsFinder;
import com.openbravo.pos.ticket.FindTicketsInfo;
import com.openbravo.pos.ticket.TicketTaxInfo;
import java.awt.*;
import java.util.ArrayList;
import javax.swing.*;
import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.JMessageDialog;
import com.openbravo.data.gui.ListKeyed;
import com.openbravo.data.gui.MessageInf;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppProperties;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.BeanFactoryApp;
import com.openbravo.pos.forms.BeanFactoryException;
import com.openbravo.pos.forms.DataLogicSales;
import com.openbravo.pos.forms.DataLogicSystem;
import com.openbravo.pos.printer.DeviceTicket;
import com.openbravo.pos.printer.TicketParser;
import com.openbravo.pos.printer.TicketPrinterException;
import com.openbravo.pos.scripting.ScriptEngine;
import com.openbravo.pos.scripting.ScriptException;
import com.openbravo.pos.scripting.ScriptFactory;
import com.openbravo.pos.ticket.TicketInfo;
import com.openbravo.pos.ticket.TicketLineInfo;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.openbravo.pos.sales.SalesService;
import com.openbravo.pos.sales.SalesServiceImpl;

/**
 *
 * @author JG uniCenta
 */
public class JTicketsBagTicket extends JTicketsBag {

    private static final Logger LOGGER = Logger.getLogger(JTicketsBagTicket.class.getName());
    private DataLogicSystem m_dlSystem = null;
    protected DataLogicCustomers dlCustomers = null;
    private final DataLogicSales m_dlSales;

    private SalesService salesService;
    // private TaxesLogic taxeslogic;
    private ListKeyed taxcollection;

    private final DeviceTicket previewDeviceTicket;
    private final TicketParser previewTicketParser;
    private final TicketParser systemTicketParser;

    private TicketInfo m_ticket;
    private TicketInfo m_ticketCopy;

    private final JTicketsBagTicketBag m_TicketsBagTicketBag;
    private final JPanelTicketEdits m_panelticketedit;

    private final AppView m_App;

    /**
     * Creates new form JTicketsBagTicket
     *
     * @param app
     * @param panelticket
     */
    public JTicketsBagTicket(AppView app, JPanelTicketEdits panelticket) {

        super(app, panelticket);
        m_App = app;

        m_panelticketedit = panelticket;
        m_dlSystem = (DataLogicSystem) m_App.getBean("com.openbravo.pos.forms.DataLogicSystem");
        m_dlSales = (DataLogicSales) m_App.getBean("com.openbravo.pos.forms.DataLogicSales");
        dlCustomers = (DataLogicCustomers) m_App.getBean("com.openbravo.pos.customers.DataLogicCustomers");
        AppProperties props = null;

        previewDeviceTicket = new DeviceTicket();

        previewTicketParser = new TicketParser(previewDeviceTicket, m_dlSystem);
        systemTicketParser = new TicketParser(m_App.getDeviceTicket(), m_dlSystem);

        initComponents();

        m_TicketsBagTicketBag = new JTicketsBagTicketBag(this);

        m_jTicketEditor.addEditorKeys(m_jKeys);

        m_jPanelTicket.add(previewDeviceTicket.getDevicePrinter("1").getPrinterComponent(), BorderLayout.CENTER);

        try {
            // taxeslogic = new TaxesLogic(m_dlSales.getTaxList().list());
            salesService = new SalesServiceImpl(new TaxesLogic(m_dlSales.getTaxList().list()));
        } catch (BasicException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        }
    }

    /**
     *
     */
    @Override
    public void activate() {

        m_ticket = null;
        m_ticketCopy = null;

        printTicket();

        m_jTicketEditor.reset();
        m_jTicketEditor.activate();

        m_panelticketedit.setActiveTicket(null, null);

        jrbSales.setSelected(true);

        m_jEdit.setVisible(m_App.hasPermission("sales.EditTicket"));
        m_jRefund.setVisible(m_App.hasPermission("sales.RefundTicket"));
        m_jPrint.setVisible(m_App.hasPermission("sales.PrintTicket"));

    }

    /**
     *
     * @return
     */
    @Override
    public boolean deactivate() {

        m_ticket = null;
        m_ticketCopy = null;
        return true;

    }

    /**
     *
     */
    @Override
    public void deleteTicket() {

        if (m_ticketCopy != null) {
            // Para editar borramos el ticket anterior
            try {
                m_dlSales.deleteTicket(m_ticketCopy, m_App.getInventoryLocation());
            } catch (BasicException eData) {
                LOGGER.log(Level.WARNING, null, eData);
                MessageInf msg = new MessageInf(MessageInf.SGN_NOTICE, AppLocal.getIntString("message.nosaveticket"),
                        eData);
                msg.show(this);
            }
        }

        m_ticket = null;
        m_ticketCopy = null;
        resetToTicket();
    }

    /**
     *
     */
    public void canceleditionTicket() {

        m_ticketCopy = null;
        resetToTicket();
    }

    private void resetToTicket() {
        printTicket();
        m_jTicketEditor.reset();
        m_jTicketEditor.activate();
        m_panelticketedit.setActiveTicket(null, null);
    }

    /**
     *
     * @return
     */
    @Override
    protected JComponent getBagComponent() {
        return m_TicketsBagTicketBag;
    }

    /**
     *
     * @return
     */
    @Override
    protected JComponent getNullComponent() {
        return this;
    }

    /**
     * Read ticket
     *
     * iTicketid (MUST be Integer to allow null)
     *
     * @param iTicketid
     * @param iTickettype Type of ticket (0 => Sales, 1=> Refound)
     */
    private void readTicket(Integer iTicketid, int iTickettype) {

        try {

            TicketInfo ticket = (iTicketid != null)
                    ? m_dlSales.loadTicket(iTickettype, iTicketid)
                    : null;

            if (ticket == null) {
                JFrame frame = new JFrame();
                JOptionPane.showMessageDialog(frame,
                        AppLocal.getIntString("message.notexiststicket"),
                        AppLocal.getIntString("message.notexiststickettitle"),
                        JOptionPane.WARNING_MESSAGE);

            } else {
                m_ticket = ticket;
                m_ticketCopy = null;
                if (m_ticket.getTicketStatus() > 0) {
                    // if(m_ticket.getTicketType()== 1 || m_ticket.getTicketStatus()> 0) {
                    JFrame frame = new JFrame();
                    JOptionPane.showMessageDialog(frame,
                            AppLocal.getIntString("message.ticketrefunded"),
                            AppLocal.getIntString("message.ticketrefundedtitle"),
                            JOptionPane.WARNING_MESSAGE);
                    m_jEdit.setEnabled(false);
                    m_jRefund.setEnabled(false);
                } else {
                    if (m_ticket.getTicketStatus() > 0) {
                        m_jEdit.setEnabled(false);
                        m_jRefund.setEnabled(false);
                    } else {
                        m_jEdit.setEnabled(true);
                        m_jRefund.setEnabled(true);

                    }
                }
                try {
                    salesService.calculateTaxes(m_ticket);
                    // TicketTaxInfo[] taxlist = m_ticket.getTaxLines();
                } catch (TaxesException ex) {
                    LOGGER.log(Level.WARNING, "Exception calculate taxes", ex);
                }

                printTicket();
            }

        } catch (BasicException e) {

            LOGGER.log(Level.WARNING, null, e);
            MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotloadticket"),
                    e);
            msg.show(this);
        }

        m_jTicketEditor.reset();
        m_jTicketEditor.activate();
    }

    private void printTicket() {

        try {
            m_jEdit.setEnabled(
                    m_ticket != null
                            && (m_ticket.getTicketType() == TicketInfo.RECEIPT_NORMAL
                                    && m_ticket.getTicketStatus() == 0)
                            && m_dlSystem.isCashActive(m_ticket.getActiveCash()));
        } catch (BasicException e) {

            LOGGER.log(Level.WARNING, null, e);
            m_jEdit.setEnabled(false);
            m_jRefund.setEnabled(false);
        }

        if (m_ticket != null
                && (m_ticket.getTicketType() == TicketInfo.RECEIPT_NORMAL
                        && m_ticket.getTicketStatus() == 0)) {
            m_jRefund.setEnabled(true);
        }

        m_jPrint.setEnabled(m_ticket != null);

        previewDeviceTicket.getDevicePrinter("1").reset();

        if (m_ticket == null) {
            m_jTicketId.setText(null);
        } else {
            m_jTicketId.setText(m_ticket.getName());

            printTicket(true);
        }
    }

    private void printTicket(boolean preview) {
        if (m_ticket != null) {

            if (preview) {
                try {
                    ScriptEngine script = ScriptFactory.getScriptEngine(ScriptFactory.VELOCITY);
                    script.put("ticket", m_ticket);
                    script.put("taxes", m_ticket.getTaxLines());
                    previewTicketParser
                            .printTicket(script.eval(m_dlSystem.getResourceAsXML("Printer.TicketPreview")).toString());
                } catch (ScriptException | TicketPrinterException e) {
                    LOGGER.log(Level.WARNING, null, e);
                    MessageInf msg = new MessageInf(MessageInf.SGN_WARNING,
                            AppLocal.getIntString("message.cannotprintticket"), e);
                    msg.show(this);
                }
            } else {
                try {
                    ScriptEngine script = ScriptFactory.getScriptEngine(ScriptFactory.VELOCITY);
                    script.put("ticket", m_ticket);
                    script.put("taxes", m_ticket.getTaxLines());
                    systemTicketParser
                            .printTicket(script.eval(m_dlSystem.getResourceAsXML("Printer.TicketPreview")).toString());
                } catch (ScriptException | TicketPrinterException e) {
                    LOGGER.log(Level.WARNING, null, e);
                    JMessageDialog.showMessage(this, new MessageInf(MessageInf.SGN_NOTICE,
                            AppLocal.getIntString("message.cannotprintticket"), e));
                }
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
        java.awt.GridBagConstraints gridBagConstraints;

        buttonGroup1 = new javax.swing.ButtonGroup();
        m_jOptions = new javax.swing.JPanel();
        m_jButtons = new javax.swing.JPanel();
        m_jTicketId = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        m_jEdit = new javax.swing.JButton();
        m_jRefund = new javax.swing.JButton();
        m_jPrint = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        m_jPanelTicket = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        m_jKeys = new com.openbravo.editor.JEditorKeys();
        jPanel5 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        m_jTicketEditor = new com.openbravo.editor.JEditorIntegerPositive();
        jPanel1 = new javax.swing.JPanel();
        jrbSales = new javax.swing.JRadioButton();
        jrbRefunds = new javax.swing.JRadioButton();

        setLayout(new java.awt.BorderLayout());

        m_jTicketId.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jTicketId.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        m_jTicketId.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory
                        .createLineBorder(javax.swing.UIManager.getDefaults().getColor("Button.darkShadow")),
                javax.swing.BorderFactory.createEmptyBorder(1, 4, 1, 4)));
        m_jTicketId.setOpaque(true);
        m_jTicketId.setPreferredSize(new java.awt.Dimension(200, 30));
        m_jTicketId.setRequestFocusEnabled(false);

        jButton2.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/search24.png"))); // NOI18N
        jButton2.setToolTipText(AppLocal.getIntString("tooltip.ticketsearch")); // NOI18N
        jButton2.setFocusPainted(false);
        jButton2.setFocusable(false);
        jButton2.setMargin(new java.awt.Insets(0, 4, 0, 4));
        jButton2.setMaximumSize(new java.awt.Dimension(50, 40));
        jButton2.setMinimumSize(new java.awt.Dimension(50, 40));
        jButton2.setPreferredSize(new java.awt.Dimension(80, 45));
        jButton2.setRequestFocusEnabled(false);
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        m_jEdit.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        m_jEdit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/sale_editline.png"))); // NOI18N
        m_jEdit.setToolTipText(AppLocal.getIntString("tooltip.ticketedit")); // NOI18N
        m_jEdit.setFocusPainted(false);
        m_jEdit.setFocusable(false);
        m_jEdit.setMargin(new java.awt.Insets(0, 4, 0, 4));
        m_jEdit.setMaximumSize(new java.awt.Dimension(50, 40));
        m_jEdit.setMinimumSize(new java.awt.Dimension(50, 40));
        m_jEdit.setPreferredSize(new java.awt.Dimension(80, 45));
        m_jEdit.setRequestFocusEnabled(false);
        m_jEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jEditActionPerformed(evt);
            }
        });

        m_jRefund.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        m_jRefund.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/inbox.png"))); // NOI18N
        m_jRefund.setToolTipText(AppLocal.getIntString("tooltip.ticketrefund")); // NOI18N
        m_jRefund.setFocusPainted(false);
        m_jRefund.setFocusable(false);
        m_jRefund.setMargin(new java.awt.Insets(0, 4, 0, 4));
        m_jRefund.setMaximumSize(new java.awt.Dimension(50, 40));
        m_jRefund.setMinimumSize(new java.awt.Dimension(50, 40));
        m_jRefund.setPreferredSize(new java.awt.Dimension(80, 45));
        m_jRefund.setRequestFocusEnabled(false);
        m_jRefund.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jRefundActionPerformed(evt);
            }
        });

        m_jPrint.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        m_jPrint.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/printer24.png"))); // NOI18N
        m_jPrint.setToolTipText(AppLocal.getIntString("tooltip.ticketreprint")); // NOI18N
        m_jPrint.setFocusPainted(false);
        m_jPrint.setFocusable(false);
        m_jPrint.setMargin(new java.awt.Insets(0, 4, 0, 4));
        m_jPrint.setMaximumSize(new java.awt.Dimension(50, 40));
        m_jPrint.setMinimumSize(new java.awt.Dimension(50, 40));
        m_jPrint.setPreferredSize(new java.awt.Dimension(80, 45));
        m_jPrint.setRequestFocusEnabled(false);
        m_jPrint.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jPrintActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout m_jButtonsLayout = new javax.swing.GroupLayout(m_jButtons);
        m_jButtons.setLayout(m_jButtonsLayout);
        m_jButtonsLayout.setHorizontalGroup(
                m_jButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(m_jButtonsLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(m_jTicketId, javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 12,
                                        Short.MAX_VALUE)
                                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(5, 5, 5)
                                .addComponent(m_jEdit, javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(5, 5, 5)
                                .addComponent(m_jRefund, javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(5, 5, 5)
                                .addComponent(m_jPrint, javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)));
        m_jButtonsLayout.setVerticalGroup(
                m_jButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(m_jButtonsLayout.createSequentialGroup()
                                .addGap(5, 5, 5)
                                .addGroup(m_jButtonsLayout
                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(m_jEdit, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(m_jRefund, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(m_jPrint, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, m_jButtonsLayout
                                                .createSequentialGroup()
                                                .addComponent(m_jTicketId, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addContainerGap()))));

        m_jOptions.add(m_jButtons);

        jPanel2.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        m_jOptions.add(jPanel2);

        add(m_jOptions, java.awt.BorderLayout.NORTH);

        m_jPanelTicket.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        m_jPanelTicket.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        m_jPanelTicket.setLayout(new java.awt.BorderLayout());
        add(m_jPanelTicket, java.awt.BorderLayout.CENTER);

        jPanel3.setLayout(new java.awt.BorderLayout());

        jPanel4.setLayout(new javax.swing.BoxLayout(jPanel4, javax.swing.BoxLayout.Y_AXIS));

        m_jKeys.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jKeysActionPerformed(evt);
            }
        });
        jPanel4.add(m_jKeys);

        jPanel5.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jPanel5.setLayout(new java.awt.GridBagLayout());

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/ok.png"))); // NOI18N
        jButton1.setToolTipText(AppLocal.getIntString("tooltip.edit.findticket")); // NOI18N
        jButton1.setFocusPainted(false);
        jButton1.setFocusable(false);
        jButton1.setMargin(new java.awt.Insets(8, 14, 8, 14));
        jButton1.setPreferredSize(new java.awt.Dimension(80, 45));
        jButton1.setRequestFocusEnabled(false);
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel5.add(jButton1, gridBagConstraints);

        m_jTicketEditor.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        m_jTicketEditor.setMinimumSize(new java.awt.Dimension(100, 30));
        m_jTicketEditor.setPreferredSize(new java.awt.Dimension(130, 30));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        jPanel5.add(m_jTicketEditor, gridBagConstraints);

        jPanel4.add(jPanel5);

        jPanel3.add(jPanel4, java.awt.BorderLayout.NORTH);

        buttonGroup1.add(jrbSales);
        jrbSales.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        jrbSales.setText(AppLocal.getIntString("label.sales")); // NOI18N
        jrbSales.setToolTipText(AppLocal.getIntString("tooltip.edit.salesopt")); // NOI18N
        jrbSales.setFocusPainted(false);
        jrbSales.setFocusable(false);
        jrbSales.setRequestFocusEnabled(false);
        jPanel1.add(jrbSales);

        buttonGroup1.add(jrbRefunds);
        jrbRefunds.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        jrbRefunds.setForeground(new java.awt.Color(255, 0, 0));
        jrbRefunds.setText(AppLocal.getIntString("label.refunds")); // NOI18N
        jrbRefunds.setToolTipText(AppLocal.getIntString("tooltip.edit.refundopt")); // NOI18N
        jrbRefunds.setFocusPainted(false);
        jrbRefunds.setFocusable(false);
        jrbRefunds.setRequestFocusEnabled(false);
        jPanel1.add(jrbRefunds);

        jPanel3.add(jPanel1, java.awt.BorderLayout.CENTER);

        add(jPanel3, java.awt.BorderLayout.EAST);
    }// </editor-fold>//GEN-END:initComponents

    private void m_jEditActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_m_jEditActionPerformed

        m_ticketCopy = m_ticket;
        m_TicketsBagTicketBag.showEdit();
        m_panelticketedit.showCatalog();
        // Indicate that this a ticket in edit mode
        m_ticketCopy.setOldTicket(true);
        m_panelticketedit.setActiveTicket(m_ticket.copyTicket(), null);

    }// GEN-LAST:event_m_jEditActionPerformed

    private void m_jPrintActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_m_jPrintActionPerformed

        printTicket(true);

    }// GEN-LAST:event_m_jPrintActionPerformed

    private void m_jRefundActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_m_jRefundActionPerformed

        java.util.List aRefundLines = new ArrayList();

        for (int i = 0; i < m_ticket.getLinesCount(); i++) {
            TicketLineInfo newline = new TicketLineInfo(m_ticket.getLine(i));
            aRefundLines.add(newline);
        }

        m_ticketCopy = null;
        m_TicketsBagTicketBag.showRefund();
        m_panelticketedit.showRefundLines(aRefundLines);

        TicketInfo refundticket = new TicketInfo();
        refundticket.setTicketType(TicketInfo.RECEIPT_REFUND);
        refundticket.setTicketStatus(m_ticket.getTicketId());
        refundticket.setCustomer(m_ticket.getCustomer());
        refundticket.setPayments(m_ticket.getPayments());
        refundticket.setOldTicket(true);
        m_panelticketedit.setActiveTicket(refundticket, null);
    }// GEN-LAST:event_m_jRefundActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButton1ActionPerformed

        readTicket(m_jTicketEditor.getValue(), jrbSales.isSelected() ? 0 : 1);

    }// GEN-LAST:event_jButton1ActionPerformed

    private void m_jKeysActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_m_jKeysActionPerformed

        readTicket(m_jTicketEditor.getValue(), jrbSales.isSelected() ? 0 : 1);

    }// GEN-LAST:event_m_jKeysActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButton2ActionPerformed
        JTicketsFinder finder = JTicketsFinder.getReceiptFinder(this, m_dlSales, dlCustomers);
        finder.setVisible(true);
        FindTicketsInfo selectedTicket = finder.getSelectedCustomer();
        if (selectedTicket == null) {
            m_jTicketEditor.reset();
            m_jTicketEditor.activate();
        } else {
            readTicket(selectedTicket.getTicketId(), selectedTicket.getTicketType());
        }
    }// GEN-LAST:event_jButton2ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JRadioButton jrbRefunds;
    private javax.swing.JRadioButton jrbSales;
    private javax.swing.JPanel m_jButtons;
    private javax.swing.JButton m_jEdit;
    private com.openbravo.editor.JEditorKeys m_jKeys;
    private javax.swing.JPanel m_jOptions;
    private javax.swing.JPanel m_jPanelTicket;
    private javax.swing.JButton m_jPrint;
    private javax.swing.JButton m_jRefund;
    private com.openbravo.editor.JEditorIntegerPositive m_jTicketEditor;
    private javax.swing.JLabel m_jTicketId;
    // End of variables declaration//GEN-END:variables

}
