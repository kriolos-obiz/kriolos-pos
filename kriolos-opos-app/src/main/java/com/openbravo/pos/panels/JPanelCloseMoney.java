//    KriolOS POS
//    Copyright (c) 2019-2023 KriolOS
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
package com.openbravo.pos.panels;

import com.openbravo.pos.reports.CashReport;
import com.openbravo.pos.forms.JPanelView;
import com.openbravo.pos.forms.DataLogicSystem;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.BeanFactoryException;
import com.openbravo.pos.forms.BeanFactoryApp;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.DataLogicSystem;
import com.openbravo.pos.forms.SystemService;
import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.MessageInf;
import com.openbravo.data.gui.TableRendererBasic;
import com.openbravo.format.Formats;
import com.openbravo.pos.printer.TicketParser;
import com.openbravo.pos.printer.TicketPrinterException;
import com.openbravo.pos.scripting.ScriptEngine;
import com.openbravo.pos.scripting.ScriptException;
import com.openbravo.pos.scripting.ScriptFactory;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.text.ParseException;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import com.openbravo.pos.cash.CashManagementService;
import com.openbravo.pos.cash.CashManagementServiceImpl;
import com.openbravo.pos.cash.CashRegister;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

/**
 *
 * @author adrianromero
 * @author KriolOS (Modernized Layout)
 */
public class JPanelCloseMoney extends JPanel implements JPanelView, BeanFactoryApp {

    private static final Logger LOGGER = Logger.getLogger(JPanelCloseMoney.class.getName());
    private AppView appView;
    private SystemService systemService;
    @Deprecated
    private DataLogicSystem dataLogicSystem;

    private CashReport paymentsToClose = null;

    private CashManagementService cashManagementService;

    private TicketParser ticketParser;

    private Integer numOfNoSales = 0;
    private Integer numOfLinesRemoved = 0;

    public JPanelCloseMoney() {
        initComponents();
    }

    @Override
    public void init(AppView app) throws BeanFactoryException {

        appView = app;
        systemService = appView.getBean(SystemService.class);
        dataLogicSystem = (systemService instanceof DataLogicSystem) ? (DataLogicSystem) systemService : null;
        ticketParser = appView.createTicketParser();

        cashManagementService = new CashManagementServiceImpl(appView.getSession());

        m_jTicketTable.setDefaultRenderer(Object.class, new TableRendererBasic(
                new Formats[] { new FormatsPayment(), Formats.CURRENCY, Formats.INT }));
        m_jTicketTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        m_jScrollTableTicket.getVerticalScrollBar().setPreferredSize(new Dimension(25, 25));
        m_jTicketTable.getTableHeader().setReorderingAllowed(false);
        m_jTicketTable.setRowHeight(25);
        m_jTicketTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        m_jsalestable.setDefaultRenderer(Object.class, new TableRendererBasic(
                new Formats[] { Formats.STRING, Formats.CURRENCY, Formats.CURRENCY, Formats.CURRENCY }));
        m_jsalestable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        m_jScrollSales.getVerticalScrollBar().setPreferredSize(new Dimension(25, 25));
        m_jsalestable.getTableHeader().setReorderingAllowed(false);
        m_jsalestable.setRowHeight(25);
        m_jsalestable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    @Override
    public Object getBean() {
        return this;
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    @Override
    public String getTitle() {
        return AppLocal.getIntString("Menu.CloseTPV");
    }

    @Override
    public void activate() throws BasicException {
        loadData();
    }

    @Override
    public boolean deactivate() {
        return true;
    }

    private void loadData() throws BasicException {

        // Reset
        m_jSequence.setText(null);
        m_jMinDate.setText(null);
        m_jMaxDate.setText(null);
        m_jPrintCashPreview.setEnabled(false);
        m_jCloseCash.setEnabled(false);

        m_jCount.setText(null);
        m_jCash.setText(null);

        m_jSales.setText(null);
        m_jSalesSubtotal.setText(null);
        m_jSalesTaxes.setText(null);
        m_jSalesTotal.setText(null);

        m_jTicketTable.setModel(new DefaultTableModel());
        m_jsalestable.setModel(new DefaultTableModel());

        // LoadData
        paymentsToClose = CashReport.loadInstance(appView);

        // Populate Data
        m_jSequence.setText(paymentsToClose.printSequence());
        m_jMinDate.setText(paymentsToClose.printDateStart());
        m_jMaxDate.setText(paymentsToClose.printDateEnd());

        if (paymentsToClose.getPayments() != 0
                || paymentsToClose.getSales() != 0) {

            m_jPrintCashPreview.setEnabled(true);
            m_jCloseCash.setEnabled(true);

            m_jCount.setText(paymentsToClose.printPayments());
            m_jCash.setText(paymentsToClose.printPaymentsTotal());

            m_jSales.setText(paymentsToClose.printSales());
            m_jSalesSubtotal.setText(paymentsToClose.printSalesBase());
            m_jSalesTaxes.setText(paymentsToClose.printSalesTaxes());
            m_jSalesTotal.setText(paymentsToClose.printSalesTotal());
        }

        m_jTicketTable.setModel(paymentsToClose.getPaymentsModel());

        TableColumnModel jColumns = m_jTicketTable.getColumnModel();
        jColumns.getColumn(0).setPreferredWidth(100);
        jColumns.getColumn(0).setResizable(false);
        jColumns.getColumn(1).setPreferredWidth(100);
        jColumns.getColumn(1).setResizable(false);
        jColumns.getColumn(2).setPreferredWidth(100);
        jColumns.getColumn(2).setResizable(false);

        m_jsalestable.setModel(paymentsToClose.getSalesModel());

        jColumns = m_jsalestable.getColumnModel();
        jColumns.getColumn(0).setPreferredWidth(100);
        jColumns.getColumn(0).setResizable(false);
        jColumns.getColumn(1).setPreferredWidth(100);
        jColumns.getColumn(1).setResizable(false);
        jColumns.getColumn(2).setPreferredWidth(100);
        jColumns.getColumn(2).setResizable(false);

        try {
            numOfNoSales = cashManagementService.getNumOfNoSales(paymentsToClose.getDateStart());
            numOfLinesRemoved = cashManagementService.getNumOfRemovedLines(paymentsToClose.getDateStart());
        } catch (BasicException ex) {
            Logger.getLogger(JPanelCloseMoney.class.getName()).log(Level.WARNING,
                    "Exception on loading Close Cash data: ", ex);
        }

        m_jLinesRemoved.setText(numOfLinesRemoved.toString());
        m_jNoCashSales.setText(numOfNoSales.toString());
    }

    private void printPayments(String report) {

        String sresource = (systemService != null) ? systemService.getResourceAsXML(report) : null;
        if (sresource == null) {
            MessageInf msg = new MessageInf(MessageInf.SGN_WARNING,
                    AppLocal.getIntString("message.cannotprintticket"));
            msg.show(this);
        } else {
            try {
                ScriptEngine script = ScriptFactory.getScriptEngine(ScriptFactory.VELOCITY);
                script.put("payments", paymentsToClose);
                script.put("nosales", numOfNoSales.toString());
                ticketParser.printTicket(script.eval(sresource).toString());
            } catch (ScriptException | TicketPrinterException e) {
                MessageInf msg = new MessageInf(MessageInf.SGN_WARNING,
                        AppLocal.getIntString("message.cannotprintticket"), e);
                msg.show(this);
            }
        }
    }

    private class FormatsPayment extends Formats {

        @Override
        protected String formatValueInt(Object value) {
            return AppLocal.getIntString("transpayment." + (String) value);
        }

        @Override
        protected Object parseValueInt(String value) throws ParseException {
            return value;
        }

        @Override
        public int getAlignment() {
            return javax.swing.SwingConstants.LEFT;
        }
    }

    private void initComponents() {
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("pos_messages");

        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // 1. LEFT PANEL: Sequence and Tables
        JPanel leftPanel = new JPanel(new BorderLayout(0, 15));
        
        // Sequence Header
        JPanel sequencePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        m_jSequence = new JTextField(15);
        m_jSequence.setEditable(false);
        m_jSequence.setHorizontalAlignment(JTextField.RIGHT);
        sequencePanel.add(new JLabel(AppLocal.getIntString("label.sequence") + ": "));
        sequencePanel.add(m_jSequence);
        leftPanel.add(sequencePanel, BorderLayout.NORTH);

        // Tables Split
        m_jTicketTable = new JTable();
        m_jScrollTableTicket = new JScrollPane(m_jTicketTable);
        m_jScrollTableTicket.setBorder(BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor")));
        
        m_jsalestable = new JTable();
        m_jScrollSales = new JScrollPane(m_jsalestable);
        m_jScrollSales.setBorder(BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor")));

        JPanel tablesContainer = new JPanel(new GridLayout(2, 1, 0, 15));
        tablesContainer.add(m_jScrollTableTicket);
        tablesContainer.add(m_jScrollSales);
        leftPanel.add(tablesContainer, BorderLayout.CENTER);


        // 2. RIGHT PANEL: Summary Form
        JPanel rightPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;
        addFormField(rightPanel, AppLocal.getIntString("label.StartDate"), m_jMinDate = new JTextField(15), gbc, row++);
        addFormField(rightPanel, AppLocal.getIntString("label.EndDate"), m_jMaxDate = new JTextField(15), gbc, row++);
        addFormField(rightPanel, AppLocal.getIntString("label.Tickets"), m_jCount = new JTextField(15), gbc, row++);
        addFormField(rightPanel, AppLocal.getIntString("label.sales"), m_jSales = new JTextField(15), gbc, row++);
        addFormField(rightPanel, AppLocal.getIntString("label.totalnet"), m_jSalesSubtotal = new JTextField(15), gbc, row++);
        addFormField(rightPanel, AppLocal.getIntString("label.taxes"), m_jSalesTaxes = new JTextField(15), gbc, row++);
        addFormField(rightPanel, AppLocal.getIntString("label.total"), m_jSalesTotal = new JTextField(15), gbc, row++);
        addFormField(rightPanel, AppLocal.getIntString("label.cash"), m_jCash = new JTextField(15), gbc, row++);
        
        // Separator
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        rightPanel.add(new JSeparator(), gbc);
        gbc.gridwidth = 1;

        addFormField(rightPanel, bundle.getString("label.linevoids"), m_jLinesRemoved = new JTextField(15), gbc, row++);
        addFormField(rightPanel, bundle.getString("label.nocashsales"), m_jNoCashSales = new JTextField(15), gbc, row++);
        
        // Push everything to the top
        gbc.gridx = 0; gbc.gridy = row; gbc.weighty = 1.0; gbc.gridwidth = 2;
        rightPanel.add(Box.createVerticalGlue(), gbc);

        // Modern SplitPane to separate tables from the form cleanly
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        mainSplitPane.setDividerSize(1);
        mainSplitPane.setBorder(null);
        mainSplitPane.setResizeWeight(0.60); // 60% for tables, 40% for form
        
        add(mainSplitPane, BorderLayout.CENTER);


        // 3. BOTTOM PANEL: Action Buttons
        JPanel bottomPanel = new JPanel(new BorderLayout());
        JPanel bottomLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        JPanel bottomRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));

        m_jPrintCash1 = new JButton(AppLocal.getIntString("button.closecashpreview"));
        m_jPrintCash1.setIcon(new ImageIcon(getClass().getResource("/com/openbravo/images/printer.png")));
        m_jPrintCash1.setToolTipText(bundle.getString("tooltip.btn.closecashpreview"));
        m_jPrintCash1.addActionListener(this::m_jPrintCash1ActionPerformed);

        m_jCloseCash = new JButton(AppLocal.getIntString("button.closecash"));
        m_jCloseCash.setIcon(new ImageIcon(getClass().getResource("/com/openbravo/images/calculator.png")));
        m_jCloseCash.setToolTipText(bundle.getString("tooltip.btn.closecash"));
        m_jCloseCash.addActionListener(this::m_jCloseCashActionPerformed);

        m_jPrintCashPreview = new JButton(AppLocal.getIntString("button.partialcash"));
        m_jPrintCashPreview.setIcon(new ImageIcon(getClass().getResource("/com/openbravo/images/printer.png")));
        m_jPrintCashPreview.setToolTipText(AppLocal.getIntString("tooltip.btn.partialcash"));
        m_jPrintCashPreview.addActionListener(this::m_jPrintCashPreviewActionPerformed);

        m_jReprintCash = new JButton(AppLocal.getIntString("button.closecashreprint"));
        m_jReprintCash.setIcon(new ImageIcon(getClass().getResource("/com/openbravo/images/printer.png")));
        m_jReprintCash.setToolTipText(bundle.getString("tooltip.btn.closecashreprint"));
        m_jReprintCash.addActionListener(this::m_jReprintCashActionPerformed);

        bottomLeft.add(m_jPrintCashPreview);
        bottomLeft.add(m_jPrintCash1);
        bottomLeft.add(m_jCloseCash);
        
        bottomRight.add(m_jReprintCash);

        bottomPanel.add(bottomLeft, BorderLayout.WEST);
        bottomPanel.add(bottomRight, BorderLayout.EAST);
        
        add(bottomPanel, BorderLayout.SOUTH);
    }

    /**
     * Helper to add a standardized Label + TextField pair to the GridBagLayout form
     */
    private void addFormField(JPanel panel, String labelText, JTextField field, GridBagConstraints gbc, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel(labelText), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        field.setEditable(false);
        field.setHorizontalAlignment(JTextField.RIGHT);
        panel.add(field, gbc);
    }

    private void m_jCloseCashActionPerformed(java.awt.event.ActionEvent evt) {

        int res = JOptionPane.showConfirmDialog(this,
                AppLocal.getIntString("message.wannaclosecash"),
                AppLocal.getIntString("message.title"),
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (res == JOptionPane.YES_OPTION) {

            String scriptId = "cash.close";
            try {
                // Fire cash.closed event
                ScriptEngine scriptEngine = ScriptFactory.getScriptEngine(ScriptFactory.BEANSHELL);
                String script = (systemService != null) ? systemService.getResourceAsXML(scriptId) : null;
                scriptEngine.eval(script);
            } catch (BeanFactoryException | ScriptException e) {
                LOGGER.log(Level.WARNING, "Exception on executing script: " + scriptId, e);
            }

            Date dNow = new Date();

            try {

                if (appView.getActiveCashDateEnd() == null) {
                    cashManagementService.closeCash(appView.getProperties().getHost(),
                            0, // sequence not used in update
                            appView.getActiveCashIndex(),
                            dNow,
                            numOfNoSales);
                }
            } catch (BasicException e) {
                MessageInf msg = new MessageInf(MessageInf.SGN_NOTICE,
                        AppLocal.getIntString("message.cannotclosecash"), e);
                msg.show(this);
            }

            try {
                // Creamos una nueva caja
                appView.setActiveCash(UUID.randomUUID().toString(),
                        appView.getActiveCashSequence() + 1, dNow, null);
                
                CashRegister cash = new CashRegister();
                cash.setMoney(appView.getActiveCashIndex());
                cash.setHost(appView.getProperties().getHost());
                cash.setHostsequence(appView.getActiveCashSequence());
                cash.setStartDate(appView.getActiveCashDateStart());
                cash.setEndDate(appView.getActiveCashDateEnd());
                
                cashManagementService.addCloseCash(cash);
                
                if (systemService != null) {
                    systemService.execDrawerOpened(appView.getAppUserView().getUser().getName(), "Close Cash", dNow);
                }

                // ponemos la fecha de fin
                paymentsToClose.setDateEnd(dNow);

                // print report
                printPayments("Printer.CloseCash");

                // Mostramos el mensaje
                JOptionPane.showMessageDialog(this,
                        AppLocal.getIntString("message.closecashok"),
                        AppLocal.getIntString("message.title"),
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (BasicException e) {
                MessageInf msg = new MessageInf(MessageInf.SGN_NOTICE,
                        AppLocal.getIntString("message.cannotclosecash"), e);
                msg.show(this);
            }

            try {
                loadData();
            } catch (BasicException e) {
                MessageInf msg = new MessageInf(MessageInf.SGN_NOTICE,
                        AppLocal.getIntString("label.noticketstoclose"), e);
                msg.show(this);
            }

        }
    }

    private void m_jPrintCashPreviewActionPerformed(java.awt.event.ActionEvent evt) {
        printPayments("Printer.PartialCash");
    }

    private void m_jPrintCash1ActionPerformed(java.awt.event.ActionEvent evt) {
        printPayments("Printer.CloseCash.Preview");
    }

    private void m_jReprintCashActionPerformed(java.awt.event.ActionEvent evt) {
        appView.getAppUserView().showTask("com.openbravo.pos.panels.JPanelCloseMoneyReprint");
    }

    // Variables declaration
    private javax.swing.JTextField m_jCash;
    private javax.swing.JButton m_jCloseCash;
    private javax.swing.JTextField m_jCount;
    private javax.swing.JTextField m_jLinesRemoved;
    private javax.swing.JTextField m_jMaxDate;
    private javax.swing.JTextField m_jMinDate;
    private javax.swing.JTextField m_jNoCashSales;
    private javax.swing.JButton m_jPrintCash1;
    private javax.swing.JButton m_jPrintCashPreview;
    private javax.swing.JButton m_jReprintCash;
    private javax.swing.JTextField m_jSales;
    private javax.swing.JTextField m_jSalesSubtotal;
    private javax.swing.JTextField m_jSalesTaxes;
    private javax.swing.JTextField m_jSalesTotal;
    private javax.swing.JScrollPane m_jScrollSales;
    private javax.swing.JScrollPane m_jScrollTableTicket;
    private javax.swing.JTextField m_jSequence;
    private javax.swing.JTable m_jTicketTable;
    private javax.swing.JTable m_jsalestable;
    // End of variables declaration

}