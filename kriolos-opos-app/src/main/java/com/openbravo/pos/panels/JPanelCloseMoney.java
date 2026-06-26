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

import com.openbravo.pos.forms.JPanelView;
import com.openbravo.pos.forms.DataLogicSystem;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.BeanFactoryException;
import com.openbravo.pos.forms.BeanFactoryApp;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.MessageInf;
import com.openbravo.data.gui.TableRendererBasic;
import com.openbravo.data.loader.Session;
import com.openbravo.format.Formats;
import com.openbravo.pos.printer.TicketParser;
import com.openbravo.pos.printer.TicketPrinterException;
import com.openbravo.pos.scripting.ScriptEngine;
import com.openbravo.pos.scripting.ScriptException;
import com.openbravo.pos.scripting.ScriptFactory;
import java.awt.Dimension;
import java.text.ParseException;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import com.openbravo.pos.payment.CashManagementService;
import com.openbravo.pos.payment.CashManagementServiceImpl;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

/**
 *
 * @author adrianromero
 */
public class JPanelCloseMoney extends JPanel implements JPanelView, BeanFactoryApp {

    private static final Logger LOGGER = Logger.getLogger(JPanelCloseMoney.class.getName());
    private AppView appView;
    private DataLogicSystem dataLogicSystem;

    private PaymentsModel m_PaymentsToClose = null;

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
        dataLogicSystem = (DataLogicSystem) appView.getBean("com.openbravo.pos.forms.DataLogicSystem");
        ticketParser = new TicketParser(appView.getDeviceTicket(), dataLogicSystem);

        cashManagementService = new CashManagementServiceImpl(appView.getSession(), dataLogicSystem);

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
        m_PaymentsToClose = PaymentsModel.loadInstance(appView);

        // Populate Data
        m_jSequence.setText(m_PaymentsToClose.printSequence());
        m_jMinDate.setText(m_PaymentsToClose.printDateStart());
        m_jMaxDate.setText(m_PaymentsToClose.printDateEnd());

        if (m_PaymentsToClose.getPayments() != 0
                || m_PaymentsToClose.getSales() != 0) {

            m_jPrintCashPreview.setEnabled(true);
            m_jCloseCash.setEnabled(true);

            m_jCount.setText(m_PaymentsToClose.printPayments());
            m_jCash.setText(m_PaymentsToClose.printPaymentsTotal());

            m_jSales.setText(m_PaymentsToClose.printSales());
            m_jSalesSubtotal.setText(m_PaymentsToClose.printSalesBase());
            m_jSalesTaxes.setText(m_PaymentsToClose.printSalesTaxes());
            m_jSalesTotal.setText(m_PaymentsToClose.printSalesTotal());
        }

        m_jTicketTable.setModel(m_PaymentsToClose.getPaymentsModel());

        TableColumnModel jColumns = m_jTicketTable.getColumnModel();
        jColumns.getColumn(0).setPreferredWidth(100);
        jColumns.getColumn(0).setResizable(false);
        jColumns.getColumn(1).setPreferredWidth(100);
        jColumns.getColumn(1).setResizable(false);
        jColumns.getColumn(2).setPreferredWidth(100);
        jColumns.getColumn(2).setResizable(false);

        m_jsalestable.setModel(m_PaymentsToClose.getSalesModel());

        jColumns = m_jsalestable.getColumnModel();
        jColumns.getColumn(0).setPreferredWidth(100);
        jColumns.getColumn(0).setResizable(false);
        jColumns.getColumn(1).setPreferredWidth(100);
        jColumns.getColumn(1).setResizable(false);
        jColumns.getColumn(2).setPreferredWidth(100);
        jColumns.getColumn(2).setResizable(false);

        try {
            numOfNoSales = cashManagementService.getNumOfNoSales(m_PaymentsToClose.getDateStart());
            numOfLinesRemoved = cashManagementService.getNumOfRemovedLines(m_PaymentsToClose.getDateStart());
        } catch (BasicException ex) {
            Logger.getLogger(JPanelCloseMoney.class.getName()).log(Level.WARNING,
                    "Exception on loading Close Cash data: ", ex);
        }

        m_jLinesRemoved.setText(numOfLinesRemoved.toString());
        m_jNoCashSales.setText(numOfNoSales.toString());
    }

    private void printPayments(String report) {

        String sresource = dataLogicSystem.getResourceAsXML(report);
        if (sresource == null) {
            MessageInf msg = new MessageInf(MessageInf.SGN_WARNING,
                    AppLocal.getIntString("message.cannotprintticket"));
            msg.show(this);
        } else {
            try {
                ScriptEngine script = ScriptFactory.getScriptEngine(ScriptFactory.VELOCITY);
                script.put("payments", m_PaymentsToClose);
                script.put("nosales", numOfNoSales.toString());
                ticketParser.printTicket(script.eval(sresource).toString());
                // JG 16 May 2012 use multicatch
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

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated
    // Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        m_jSequence = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        m_jMinDate = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        m_jMaxDate = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        m_jCash = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        m_jCount = new javax.swing.JTextField();
        m_jLinesRemoved = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        m_jScrollTableTicket = new javax.swing.JScrollPane();
        m_jTicketTable = new javax.swing.JTable();
        jLabel9 = new javax.swing.JLabel();
        m_jNoCashSales = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        m_jSalesTotal = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        m_jSalesTaxes = new javax.swing.JTextField();
        m_jScrollSales = new javax.swing.JScrollPane();
        m_jsalestable = new javax.swing.JTable();
        m_jSales = new javax.swing.JTextField();
        m_jSalesSubtotal = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        m_jCloseCash = new javax.swing.JButton();
        m_jPrintCashPreview = new javax.swing.JButton();
        m_jPrintCash1 = new javax.swing.JButton();
        m_jReprintCash = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        jPanel1.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jPanel1.setMaximumSize(new java.awt.Dimension(700, 500));
        jPanel1.setMinimumSize(new java.awt.Dimension(700, 500));
        jPanel1.setPreferredSize(new java.awt.Dimension(700, 500));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel11.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel11.setText(AppLocal.getIntString("label.sequence")); // NOI18N
        jLabel11.setPreferredSize(new java.awt.Dimension(125, 30));
        jPanel1.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 11, -1, -1));

        m_jSequence.setEditable(false);
        m_jSequence.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jSequence.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        m_jSequence.setPreferredSize(new java.awt.Dimension(150, 30));
        jPanel1.add(m_jSequence, new org.netbeans.lib.awtextra.AbsoluteConstraints(145, 12, -1, -1));

        jLabel2.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel2.setText(AppLocal.getIntString("label.StartDate")); // NOI18N
        jLabel2.setPreferredSize(new java.awt.Dimension(125, 30));
        jPanel1.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 20, 150, -1));

        m_jMinDate.setEditable(false);
        m_jMinDate.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jMinDate.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        m_jMinDate.setPreferredSize(new java.awt.Dimension(150, 30));
        jPanel1.add(m_jMinDate, new org.netbeans.lib.awtextra.AbsoluteConstraints(510, 20, 180, -1));

        jLabel3.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel3.setText(AppLocal.getIntString("label.EndDate")); // NOI18N
        jLabel3.setPreferredSize(new java.awt.Dimension(125, 30));
        jPanel1.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 60, 150, -1));

        m_jMaxDate.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jMaxDate.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        m_jMaxDate.setEnabled(false);
        m_jMaxDate.setPreferredSize(new java.awt.Dimension(150, 30));
        jPanel1.add(m_jMaxDate, new org.netbeans.lib.awtextra.AbsoluteConstraints(510, 60, 180, -1));

        jLabel5.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel5.setText(AppLocal.getIntString("label.sales")); // NOI18N
        jLabel5.setPreferredSize(new java.awt.Dimension(150, 30));
        jPanel1.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 140, -1, -1));

        m_jCash.setEditable(false);
        m_jCash.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jCash.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        m_jCash.setPreferredSize(new java.awt.Dimension(150, 30));
        jPanel1.add(m_jCash, new org.netbeans.lib.awtextra.AbsoluteConstraints(510, 300, 180, -1));

        jLabel4.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel4.setText(AppLocal.getIntString("label.cash")); // NOI18N
        jLabel4.setPreferredSize(new java.awt.Dimension(150, 30));
        jPanel1.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 300, -1, -1));

        m_jCount.setEditable(false);
        m_jCount.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jCount.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        m_jCount.setPreferredSize(new java.awt.Dimension(150, 30));
        jPanel1.add(m_jCount, new org.netbeans.lib.awtextra.AbsoluteConstraints(510, 100, 180, -1));

        m_jLinesRemoved.setEditable(false);
        m_jLinesRemoved.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jLinesRemoved.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        m_jLinesRemoved.setPreferredSize(new java.awt.Dimension(150, 30));
        jPanel1.add(m_jLinesRemoved, new org.netbeans.lib.awtextra.AbsoluteConstraints(513, 360, 180, -1));

        jLabel1.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel1.setText(AppLocal.getIntString("label.Tickets")); // NOI18N
        jLabel1.setPreferredSize(new java.awt.Dimension(150, 30));
        jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 100, -1, -1));
        jPanel1.add(jSeparator1, new org.netbeans.lib.awtextra.AbsoluteConstraints(353, 347, 340, 10));

        m_jScrollTableTicket.setBorder(null);
        m_jScrollTableTicket.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        m_jScrollTableTicket.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jScrollTableTicket.setMinimumSize(new java.awt.Dimension(350, 140));
        m_jScrollTableTicket.setPreferredSize(new java.awt.Dimension(325, 150));

        m_jTicketTable.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        m_jTicketTable.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][] {

                },
                new String[] {

                }));
        m_jTicketTable.setFocusable(false);
        m_jTicketTable.setIntercellSpacing(new java.awt.Dimension(0, 1));
        m_jTicketTable.setRequestFocusEnabled(false);
        m_jScrollTableTicket.setViewportView(m_jTicketTable);

        jPanel1.add(m_jScrollTableTicket, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 95, -1, -1));

        jLabel9.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("pos_messages"); // NOI18N
        jLabel9.setText(bundle.getString("label.linevoids")); // NOI18N
        jLabel9.setPreferredSize(new java.awt.Dimension(150, 30));
        jPanel1.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(353, 359, -1, -1));

        m_jNoCashSales.setEditable(false);
        m_jNoCashSales.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jNoCashSales.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        m_jNoCashSales.setPreferredSize(new java.awt.Dimension(150, 30));
        jPanel1.add(m_jNoCashSales, new org.netbeans.lib.awtextra.AbsoluteConstraints(513, 402, 180, -1));

        jLabel8.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel8.setText(bundle.getString("label.nocashsales")); // NOI18N
        jLabel8.setPreferredSize(new java.awt.Dimension(150, 30));
        jPanel1.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(353, 401, -1, -1));

        m_jSalesTotal.setEditable(false);
        m_jSalesTotal.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jSalesTotal.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        m_jSalesTotal.setPreferredSize(new java.awt.Dimension(150, 30));
        jPanel1.add(m_jSalesTotal, new org.netbeans.lib.awtextra.AbsoluteConstraints(510, 260, 180, -1));

        jLabel7.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel7.setText(AppLocal.getIntString("label.total")); // NOI18N
        jLabel7.setPreferredSize(new java.awt.Dimension(150, 30));
        jPanel1.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 260, -1, -1));

        jLabel12.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel12.setText(AppLocal.getIntString("label.taxes")); // NOI18N
        jLabel12.setPreferredSize(new java.awt.Dimension(150, 30));
        jPanel1.add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 220, -1, -1));

        m_jSalesTaxes.setEditable(false);
        m_jSalesTaxes.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jSalesTaxes.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        m_jSalesTaxes.setPreferredSize(new java.awt.Dimension(150, 30));
        jPanel1.add(m_jSalesTaxes, new org.netbeans.lib.awtextra.AbsoluteConstraints(510, 220, 180, -1));

        m_jScrollSales.setBorder(null);
        m_jScrollSales.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        m_jScrollSales.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jScrollSales.setPreferredSize(new java.awt.Dimension(325, 150));

        m_jsalestable.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        m_jsalestable.setFocusable(false);
        m_jsalestable.setIntercellSpacing(new java.awt.Dimension(0, 1));
        m_jsalestable.setRequestFocusEnabled(false);
        m_jScrollSales.setViewportView(m_jsalestable);

        jPanel1.add(m_jScrollSales, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 250, -1, -1));

        m_jSales.setEditable(false);
        m_jSales.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jSales.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        m_jSales.setPreferredSize(new java.awt.Dimension(150, 30));
        jPanel1.add(m_jSales, new org.netbeans.lib.awtextra.AbsoluteConstraints(510, 140, 180, -1));

        m_jSalesSubtotal.setEditable(false);
        m_jSalesSubtotal.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jSalesSubtotal.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        m_jSalesSubtotal.setPreferredSize(new java.awt.Dimension(150, 30));
        jPanel1.add(m_jSalesSubtotal, new org.netbeans.lib.awtextra.AbsoluteConstraints(510, 180, 180, -1));

        jLabel6.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel6.setText(AppLocal.getIntString("label.totalnet")); // NOI18N
        jLabel6.setPreferredSize(new java.awt.Dimension(150, 30));
        jPanel1.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 180, -1, -1));

        m_jCloseCash.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        m_jCloseCash.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/calculator.png"))); // NOI18N
        m_jCloseCash.setText(AppLocal.getIntString("button.closecash")); // NOI18N
        m_jCloseCash.setToolTipText(bundle.getString("tooltip.btn.closecash")); // NOI18N
        m_jCloseCash.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        m_jCloseCash.setIconTextGap(2);
        m_jCloseCash.setInheritsPopupMenu(true);
        m_jCloseCash.setMaximumSize(new java.awt.Dimension(85, 33));
        m_jCloseCash.setMinimumSize(new java.awt.Dimension(85, 33));
        m_jCloseCash.setPreferredSize(new java.awt.Dimension(150, 45));
        m_jCloseCash.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jCloseCashActionPerformed(evt);
            }
        });
        jPanel1.add(m_jCloseCash, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 450, -1, -1));

        m_jPrintCashPreview.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        m_jPrintCashPreview
                .setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/printer.png"))); // NOI18N
        m_jPrintCashPreview.setText(AppLocal.getIntString("button.partialcash")); // NOI18N
        m_jPrintCashPreview.setToolTipText(AppLocal.getIntString("tooltip.btn.partialcash")); // NOI18N
        m_jPrintCashPreview.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        m_jPrintCashPreview.setIconTextGap(2);
        m_jPrintCashPreview.setMaximumSize(new java.awt.Dimension(85, 33));
        m_jPrintCashPreview.setMinimumSize(new java.awt.Dimension(85, 33));
        m_jPrintCashPreview.setPreferredSize(new java.awt.Dimension(150, 45));
        m_jPrintCashPreview.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jPrintCashPreviewActionPerformed(evt);
            }
        });
        jPanel1.add(m_jPrintCashPreview, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 450, -1, -1));

        m_jPrintCash1.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        m_jPrintCash1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/printer.png"))); // NOI18N
        m_jPrintCash1.setText(AppLocal.getIntString("button.closecashpreview")); // NOI18N
        m_jPrintCash1.setToolTipText(bundle.getString("tooltip.btn.closecashpreview")); // NOI18N
        m_jPrintCash1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        m_jPrintCash1.setIconTextGap(2);
        m_jPrintCash1.setMaximumSize(new java.awt.Dimension(85, 33));
        m_jPrintCash1.setMinimumSize(new java.awt.Dimension(85, 33));
        m_jPrintCash1.setPreferredSize(new java.awt.Dimension(150, 45));
        m_jPrintCash1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jPrintCash1ActionPerformed(evt);
            }
        });
        jPanel1.add(m_jPrintCash1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 450, -1, -1));

        m_jReprintCash.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        m_jReprintCash.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/printer.png"))); // NOI18N
        m_jReprintCash.setText(AppLocal.getIntString("button.closecashreprint")); // NOI18N
        m_jReprintCash.setToolTipText(bundle.getString("tooltip.btn.closecashreprint")); // NOI18N
        m_jReprintCash.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        m_jReprintCash.setIconTextGap(2);
        m_jReprintCash.setMaximumSize(new java.awt.Dimension(85, 33));
        m_jReprintCash.setMinimumSize(new java.awt.Dimension(85, 33));
        m_jReprintCash.setPreferredSize(new java.awt.Dimension(150, 45));
        m_jReprintCash.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jReprintCashActionPerformed(evt);
            }
        });
        jPanel1.add(m_jReprintCash, new org.netbeans.lib.awtextra.AbsoluteConstraints(530, 450, -1, -1));

        add(jPanel1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void m_jCloseCashActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_m_jCloseCashActionPerformed

        int res = JOptionPane.showConfirmDialog(this,
                AppLocal.getIntString("message.wannaclosecash"),
                AppLocal.getIntString("message.title"),
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (res == JOptionPane.YES_OPTION) {

            String scriptId = "cash.close";
            try {
                // Fire cash.closed event
                ScriptEngine scriptEngine = ScriptFactory.getScriptEngine(ScriptFactory.BEANSHELL);
                DataLogicSystem dlSystem = (DataLogicSystem) appView.getBean("com.openbravo.pos.forms.DataLogicSystem");
                String script = dlSystem.getResourceAsXML(scriptId);
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

                // creamos la caja activa
                dataLogicSystem.execInsertCash(new Object[] { appView.getActiveCashIndex(),
                        appView.getProperties().getHost(),
                        appView.getActiveCashSequence(),
                        appView.getActiveCashDateStart(),
                        appView.getActiveCashDateEnd(), 0 });

                dataLogicSystem
                        .execDrawerOpened(new Object[] { appView.getAppUserView().getUser().getName(), "Close Cash" });

                // ponemos la fecha de fin
                m_PaymentsToClose.setDateEnd(dNow);

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
    }// GEN-LAST:event_m_jCloseCashActionPerformed

    private void m_jPrintCashPreviewActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_m_jPrintCashPreviewActionPerformed

        printPayments("Printer.PartialCash");

    }// GEN-LAST:event_m_jPrintCashPreviewActionPerformed

    private void m_jPrintCash1ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_m_jPrintCash1ActionPerformed
        printPayments("Printer.CloseCash.Preview");
    }// GEN-LAST:event_m_jPrintCash1ActionPerformed

    private void m_jReprintCashActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_m_jReprintCashActionPerformed
        appView.getAppUserView().showTask("com.openbravo.pos.panels.JPanelCloseMoneyReprint");
    }// GEN-LAST:event_m_jReprintCashActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSeparator jSeparator1;
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
    // End of variables declaration//GEN-END:variables

}
