/*
 * Copyright (C) 2026 KriolOS
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
package com.openbravo.pos.panels;

import com.openbravo.basic.AppLocal;
import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.MessageInf;
import com.openbravo.data.gui.TableRendererBasic;
import com.openbravo.format.Formats;
import com.openbravo.pos.admin.SystemService;
import com.openbravo.pos.cash.CashManagementService;
import com.openbravo.pos.cash.CashManagementServiceImpl;
import com.openbravo.pos.container.BeanFactoryApp;
import com.openbravo.pos.container.BeanFactoryException;
import com.openbravo.pos.domain.audit.ClosedCashSnapshot;
import com.openbravo.pos.domain.audit.DocumentIntegrityService;
import com.openbravo.pos.domain.audit.FinancialReportSnapshot;
import com.openbravo.pos.reports.CashReport;
import com.openbravo.pos.scripting.ScriptEngine;
import com.openbravo.pos.scripting.ScriptException;
import com.openbravo.pos.scripting.ScriptFactory;
import com.openbravo.pos.spi.hardware.printer.TicketPrinterException;
import com.openbravo.pos.ticket.TicketParser;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.ParseException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Modern Standard Cash Reporting Panel (X-Report vs. Z-Report).
 * Provides instantaneous mid-day audit (X) and verifiable historical audit
 * based on immutable WORM snapshots with SHA-256 integrity (#117).
 *
 * @author poolborges
 */
public class JPanelCashReport extends JPanel implements JPanelView, BeanFactoryApp {

    private static final Logger LOGGER = Logger.getLogger(JPanelCashReport.class.getName());

    private AppView appView;
    private SystemService systemService;
    private CashManagementService cashManagementService;
    private TicketParser ticketParser;

    // Report State
    private boolean isModeCurrentShift = true; // true = X-Report (Active), false = Z-Report (Historical Snapshot)
    private int currentSequence = 1;
    private CashReport activeCashReport;
    private ClosedCashSnapshot currentSnapshot;
    private FinancialReportSnapshot currentSnapshotPayload;

    // UI Components
    private JRadioButton rbModeCurrent;
    private JRadioButton rbModeHistorical;
    private JSpinner spSequence;
    private JButton btnPrev;
    private JButton btnNext;
    private JButton btnReload;
    private JLabel lblIntegrityBadge;

    private JTextField txtStartDate;
    private JTextField txtEndDate;
    private JTextField txtHost;
    private JTextField txtSequence;
    private JTextField txtGrossSales;
    private JTextField txtNetSales;
    private JTextField txtTaxes;
    private JTextField txtPayments;
    private JTextField txtDrawerCash;
    private JTextField txtChecksum;

    private JTable tblPayments;
    private JTable tblTaxes;
    private JTextArea txtJsonPayload;
    private JButton btnPrint;

    public JPanelCashReport() {
        initComponents();
    }

    @Override
    public void init(AppView app) throws BeanFactoryException {
        this.appView = app;
        this.systemService = appView.getBean(SystemService.class);
        this.cashManagementService = new CashManagementServiceImpl(appView.getSession());
        this.ticketParser = appView.createTicketParser();

        tblPayments.setDefaultRenderer(Object.class, new TableRendererBasic(
                new Formats[]{new FormatsPayment(), Formats.CURRENCY, Formats.INT}));
        tblTaxes.setDefaultRenderer(Object.class, new TableRendererBasic(
                new Formats[]{Formats.STRING, Formats.CURRENCY, Formats.CURRENCY, Formats.CURRENCY}));
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
        return AppLocal.getIntString("Menu.CashReport");
    }

    @Override
    public void activate() throws BasicException {
        if (appView != null) {
            currentSequence = appView.getActiveCashSequence();
            spSequence.setValue(currentSequence);
            loadReportData();
        }
    }

    @Override
    public boolean deactivate() {
        return true;
    }

    private void loadReportData() {
        try {
            if (isModeCurrentShift) {
                loadCurrentShiftReport();
            } else {
                loadHistoricalSnapshotReport();
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Failed to load cash report data", ex);
            MessageInf msg = new MessageInf(MessageInf.SGN_WARNING,
                    AppLocal.getIntString("message.cannotloadreportdata"), ex);
            msg.show(this);
        }
    }

    private void loadCurrentShiftReport() throws BasicException {
        activeCashReport = CashReport.loadInstance(appView);
        currentSnapshot = null;
        currentSnapshotPayload = null;

        txtStartDate.setText(activeCashReport.printDateStart());
        txtEndDate.setText(activeCashReport.printDateEnd() != null ? activeCashReport.printDateEnd() : AppLocal.getIntString("label.currentsession"));
        txtHost.setText(activeCashReport.getHost());
        txtSequence.setText(activeCashReport.printSequence());

        txtGrossSales.setText(activeCashReport.printSalesTotal());
        txtNetSales.setText(activeCashReport.printSalesBase());
        txtTaxes.setText(activeCashReport.printSalesTaxes());
        txtPayments.setText(activeCashReport.printPaymentsTotal());
        txtDrawerCash.setText(activeCashReport.printPaymentsTotal());

        txtChecksum.setText("N/A (Active Unclosed Session)");
        lblIntegrityBadge.setText("● Active Shift (X-Report - Live Non-Destructive)");
        lblIntegrityBadge.setForeground(new Color(0, 120, 215));

        tblPayments.setModel(activeCashReport.getPaymentsModel());
        tblTaxes.setModel(activeCashReport.getSalesModel());
        txtJsonPayload.setText("{\"status\": \"IN_PROGRESS\", \"mode\": \"X-REPORT\", \"closed\": false}");
    }

    private void loadHistoricalSnapshotReport() throws BasicException {
        int seq = ((Number) spSequence.getValue()).intValue();
        String host = appView.getProperties().getHost();

        currentSnapshot = cashManagementService.getSnapshotBySequence(host, seq);

        if (currentSnapshot != null) {
            txtStartDate.setText(Formats.TIMESTAMP.formatValue(currentSnapshot.getDateStart()));
            txtEndDate.setText(Formats.TIMESTAMP.formatValue(currentSnapshot.getDateEnd()));
            txtHost.setText(currentSnapshot.getHost());
            txtSequence.setText(String.valueOf(currentSnapshot.getHostSequence()));

            txtGrossSales.setText(formatCurrency(currentSnapshot.getTotalGrossSales()));
            txtNetSales.setText(formatCurrency(currentSnapshot.getTotalNetSales()));
            txtTaxes.setText(formatCurrency(currentSnapshot.getTotalTaxes()));
            txtPayments.setText(formatCurrency(currentSnapshot.getTotalPayments()));
            txtDrawerCash.setText(formatCurrency(currentSnapshot.getCashInDrawer()));
            txtChecksum.setText(currentSnapshot.getPayloadChecksum());

            boolean valid = DocumentIntegrityService.verifyIntegrity(currentSnapshot.getPayload(), currentSnapshot.getPayloadChecksum());
            if (valid) {
                lblIntegrityBadge.setText("✔ WORM Immutable Snapshot: Integrity VERIFIED (SHA-256)");
                lblIntegrityBadge.setForeground(new Color(34, 139, 34));
            } else {
                lblIntegrityBadge.setText("✖ INTEGRITY ALERT: SHA-256 Checksum Mismatch! Data may be tampered.");
                lblIntegrityBadge.setForeground(Color.RED);
            }

            txtJsonPayload.setText(currentSnapshot.getPayload());

            // Populate Tables from Deserialized Payload if available
            try {
                currentSnapshotPayload = DocumentIntegrityService.deserializeJson(
                        currentSnapshot.getPayload(), FinancialReportSnapshot.class);
                populateTablesFromSnapshot(currentSnapshotPayload);
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, "Failed to deserialize snapshot payload into UI tables", ex);
            }
        } else {
            clearFields();
            lblIntegrityBadge.setText("No snapshot found for Sequence #" + seq);
            lblIntegrityBadge.setForeground(Color.DARK_GRAY);
            txtJsonPayload.setText("");
        }
    }

    private void populateTablesFromSnapshot(FinancialReportSnapshot payload) {
        // Payments Table
        DefaultTableModel payModel = new DefaultTableModel(new String[]{"label.Payment", "label.paymenttotal", "label.qty"}, 0);
        if (payload.payments() != null) {
            for (FinancialReportSnapshot.PaymentSummary p : payload.payments()) {
                payModel.addRow(new Object[]{p.paymentMethod(), formatCurrency(p.totalAmount()), p.transactionCount()});
            }
        }
        tblPayments.setModel(payModel);

        // Taxes Table
        DefaultTableModel taxModel = new DefaultTableModel(new String[]{"label.taxcategory", "label.totaltax", "label.totalnet", "label.total"}, 0);
        if (payload.taxes() != null) {
            for (FinancialReportSnapshot.TaxSummary t : payload.taxes()) {
                double net = t.baseAmount().doubleValue();
                double tax = t.taxAmount().doubleValue();
                taxModel.addRow(new Object[]{t.taxName(), Formats.CURRENCY.formatValue(tax), Formats.CURRENCY.formatValue(net), Formats.CURRENCY.formatValue(net + tax)});
            }
        }
        tblTaxes.setModel(taxModel);
    }

    private void clearFields() {
        txtStartDate.setText("");
        txtEndDate.setText("");
        txtHost.setText("");
        txtSequence.setText("");
        txtGrossSales.setText("");
        txtNetSales.setText("");
        txtTaxes.setText("");
        txtPayments.setText("");
        txtDrawerCash.setText("");
        txtChecksum.setText("");
        tblPayments.setModel(new DefaultTableModel());
        tblTaxes.setModel(new DefaultTableModel());
    }

    private void printReport() {
        String reportTemplate = isModeCurrentShift ? "Printer.PartialCash" : "Printer.CloseCash";
        String xml = (systemService != null) ? systemService.getResourceAsXML(reportTemplate) : null;
        if (xml == null) {
            xml = (systemService != null) ? systemService.getResourceAsXML("Printer.CloseCash") : null;
        }

        if (xml == null) {
            MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotprintticket"));
            msg.show(this);
            return;
        }

        try {
            ScriptEngine script = ScriptFactory.getScriptEngine(ScriptFactory.VELOCITY);
            if (isModeCurrentShift && activeCashReport != null) {
                script.put("payments", activeCashReport);
                script.put("nosales", "0");
            } else if (currentSnapshot != null) {
                script.put("snapshot", currentSnapshot);
                script.put("payload", currentSnapshotPayload);
            }
            ticketParser.printTicket(script.eval(xml).toString());
        } catch (ScriptException | TicketPrinterException e) {
            LOGGER.log(Level.WARNING, "Failed to print cash report", e);
            MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotprintticket"), e);
            msg.show(this);
        }
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(12, 12, 12, 12));

        // 1. TOP TOOLBAR
        JPanel topPanel = new JPanel(new BorderLayout(10, 5));
        topPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("Component.borderColor")),
                new EmptyBorder(0, 0, 10, 0)));

        JPanel controlsLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        ButtonGroup bgMode = new ButtonGroup();
        rbModeCurrent = new JRadioButton("Relatório X (Sessão Atual)", true);
        rbModeHistorical = new JRadioButton("Relatório Z (Histórico WORM)", false);
        bgMode.add(rbModeCurrent);
        bgMode.add(rbModeHistorical);

        rbModeCurrent.addActionListener(e -> {
            isModeCurrentShift = true;
            spSequence.setEnabled(false);
            btnPrev.setEnabled(false);
            btnNext.setEnabled(false);
            loadReportData();
        });

        rbModeHistorical.addActionListener(e -> {
            isModeCurrentShift = false;
            spSequence.setEnabled(true);
            btnPrev.setEnabled(true);
            btnNext.setEnabled(true);
            loadReportData();
        });

        controlsLeft.add(new JLabel("Modo: "));
        controlsLeft.add(rbModeCurrent);
        controlsLeft.add(rbModeHistorical);

        controlsLeft.add(new JSeparator(SwingConstants.VERTICAL));
        controlsLeft.add(new JLabel("Sequência: "));
        spSequence = new JSpinner(new SpinnerNumberModel(1, 1, 999999, 1));
        spSequence.setPreferredSize(new Dimension(80, 26));
        spSequence.setEnabled(false);
        spSequence.addChangeListener(e -> {
            if (!isModeCurrentShift) {
                loadReportData();
            }
        });
        controlsLeft.add(spSequence);

        btnPrev = new JButton("<");
        btnPrev.setEnabled(false);
        btnPrev.addActionListener(e -> {
            int val = ((Number) spSequence.getValue()).intValue();
            if (val > 1) {
                spSequence.setValue(val - 1);
            }
        });
        controlsLeft.add(btnPrev);

        btnNext = new JButton(">");
        btnNext.setEnabled(false);
        btnNext.addActionListener(e -> {
            int val = ((Number) spSequence.getValue()).intValue();
            spSequence.setValue(val + 1);
        });
        controlsLeft.add(btnNext);

        btnReload = new JButton(AppLocal.getIntString("button.reload"));
        btnReload.addActionListener(e -> loadReportData());
        controlsLeft.add(btnReload);

        topPanel.add(controlsLeft, BorderLayout.WEST);

        lblIntegrityBadge = new JLabel("● Estado da Auditoria", SwingConstants.RIGHT);
        lblIntegrityBadge.setFont(lblIntegrityBadge.getFont().deriveFont(Font.BOLD));
        topPanel.add(lblIntegrityBadge, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // 2. CENTER CONTENT (SplitPane)
        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplit.setDividerSize(6);
        mainSplit.setResizeWeight(0.65);

        // Left Container: Tables
        JPanel tablesPanel = new JPanel(new GridLayout(2, 1, 0, 10));

        JPanel pnlPayments = new JPanel(new BorderLayout());
        pnlPayments.setBorder(BorderFactory.createTitledBorder("Formas de Pagamento (Tenders)"));
        tblPayments = new JTable();
        tblPayments.setRowHeight(24);
        pnlPayments.add(new JScrollPane(tblPayments), BorderLayout.CENTER);
        tablesPanel.add(pnlPayments);

        JPanel pnlTaxes = new JPanel(new BorderLayout());
        pnlTaxes.setBorder(BorderFactory.createTitledBorder("Discriminação Fiscal de Impostos (Taxes)"));
        tblTaxes = new JTable();
        tblTaxes.setRowHeight(24);
        pnlTaxes.add(new JScrollPane(tblTaxes), BorderLayout.CENTER);
        tablesPanel.add(pnlTaxes);

        mainSplit.setLeftComponent(tablesPanel);

        // Right Container: Tabs (Summary Form + Raw WORM JSON)
        JTabbedPane rightTabs = new JTabbedPane();

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        int row = 0;
        addFormField(formPanel, "Data Abertura:", txtStartDate = new JTextField(), gbc, row++);
        addFormField(formPanel, "Data Fecho:", txtEndDate = new JTextField(), gbc, row++);
        addFormField(formPanel, "Terminal (Host):", txtHost = new JTextField(), gbc, row++);
        addFormField(formPanel, "Sequência Z:", txtSequence = new JTextField(), gbc, row++);

        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        formPanel.add(new JSeparator(), gbc);
        gbc.gridwidth = 1;

        addFormField(formPanel, "Vendas Líquidas (Net):", txtNetSales = new JTextField(), gbc, row++);
        addFormField(formPanel, "Impostos Totais (Tax):", txtTaxes = new JTextField(), gbc, row++);
        addFormField(formPanel, "Vendas Brutas (Gross):", txtGrossSales = new JTextField(), gbc, row++);
        addFormField(formPanel, "Total Liquidado:", txtPayments = new JTextField(), gbc, row++);
        addFormField(formPanel, "Dinheiro em Caixa:", txtDrawerCash = new JTextField(), gbc, row++);

        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        formPanel.add(new JSeparator(), gbc);
        gbc.gridwidth = 1;

        addFormField(formPanel, "SHA-256 Checksum:", txtChecksum = new JTextField(), gbc, row++);

        gbc.gridx = 0; gbc.gridy = row; gbc.weighty = 1.0; gbc.gridwidth = 2;
        formPanel.add(Box.createVerticalGlue(), gbc);

        rightTabs.addTab("Resumo Financeiro", formPanel);

        txtJsonPayload = new JTextArea();
        txtJsonPayload.setEditable(false);
        txtJsonPayload.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        txtJsonPayload.setLineWrap(true);
        txtJsonPayload.setWrapStyleWord(true);
        rightTabs.addTab("WORM Payload (JSON)", new JScrollPane(txtJsonPayload));

        mainSplit.setRightComponent(rightTabs);
        add(mainSplit, BorderLayout.CENTER);

        // 3. BOTTOM PANEL: Actions
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPrint = new JButton("Imprimir Relatório (X/Z)");
        btnPrint.setIcon(new ImageIcon(getClass().getResource("/com/openbravo/images/printer.png")));
        btnPrint.addActionListener(e -> printReport());
        bottomPanel.add(btnPrint);

        add(bottomPanel, BorderLayout.SOUTH);
    }

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

    private String formatCurrency(java.math.BigDecimal value) {
        return value != null ? Formats.CURRENCY.formatValue(value.doubleValue()) : Formats.CURRENCY.formatValue(0.0);
    }

    private static class FormatsPayment extends Formats {
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
            return SwingConstants.LEFT;
        }
    }
}
