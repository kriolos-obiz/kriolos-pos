//    KriolOS POS
//    Copyright (c) 2019-2023 KriolOS
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
package com.openbravo.pos.inventory;

import com.openbravo.pos.forms.JPanelView;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.basic.BasicException;
import com.openbravo.beans.DateUtils;
import com.openbravo.beans.JCalendarPanel2;
import com.openbravo.data.gui.ComboBoxValModel;
import com.openbravo.data.gui.MessageInf;
import com.openbravo.format.Formats;
import com.openbravo.pos.catalog.CatalogSelector;
import com.openbravo.pos.catalog.JCatalog;
import com.openbravo.pos.catalog.CatalogService;
import com.openbravo.pos.forms.*;
import com.openbravo.pos.panels.JProductFinderPanel;
import com.openbravo.pos.pim.DataLogicPIM;
import com.openbravo.pos.printer.TicketParser;
import com.openbravo.pos.printer.TicketPrinterException;
import com.openbravo.pos.sales.JProductAttEdit2;
import com.openbravo.pos.scripting.ScriptEngine;
import com.openbravo.pos.scripting.ScriptException;
import com.openbravo.pos.scripting.ScriptFactory;
import com.openbravo.pos.suppliers.SupplierInfo;
import com.openbravo.pos.suppliers.SupplierService;
import com.openbravo.pos.ticket.ProductInfoExt;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

/**
 * Date : Aug 2017 Updated : Dec 2016
 *
 * @author jack gerrard
 * @author KriolOS (Modernized Layout)
 */
public class StockManagement extends JPanel implements JPanelView {

    private final static Logger LOGGER = Logger.getLogger(StockManagement.class.getName());

    private final AppView m_App;
    private final String user;

    private final DataLogicSystem m_dlSystem;
    private final StockService stockService;
    private final SupplierService m_dlSuppliers;
    private final CatalogService catalogService;
    private final DataLogicPIM dataLogicPIM;
    private final TicketParser m_TTP;

    private final CatalogSelector m_cat;
    private final ComboBoxValModel m_ReasonModel;

    private ComboBoxValModel m_LocationsModel;
    private ComboBoxValModel m_LocationsModelDes;

    private ComboBoxValModel m_SuppliersModel;

    private final JInventoryLines m_invlines = new JInventoryLines();

    private int NUMBER_STATE = 0;
    private int MULTIPLY = 0;
    private static final int DEFAULT = 0;
    private static final int ACTIVE = 1;
    private static final int DECIMAL = 2;

    private List<ProductStock> productStockList;
    private ProductStockTableModel stockModel;

    private final static int NUMBERZERO = 0;
    private final static int NUMBERVALID = 1;

    private final static int NUMBER_INPUTZERO = 0;
    private final static int NUMBER_INPUTZERODEC = 1;
    private final static int NUMBER_INPUTINT = 2;
    private final static int NUMBER_INPUTDEC = 3;
    private final static int NUMBER_PORZERO = 4;
    private final static int NUMBER_PORZERODEC = 5;
    private final static int NUMBER_PORINT = 6;
    private final static int NUMBER_PORDEC = 7;

    private int m_iNumberStatus;
    private int m_iNumberStatusInput;
    private int m_iNumberStatusPor;
    private StringBuffer m_sBarcode;

    public StockManagement(AppView app) {

        m_App = app;
        m_dlSystem = m_App.getBean(DataLogicSystem.class);
        stockService = m_App.getBean(StockService.class);
        m_dlSuppliers = m_App.getBean(SupplierService.class);
        catalogService = app.getBean(CatalogService.class);
        dataLogicPIM = (catalogService instanceof DataLogicPIM) ? (DataLogicPIM) catalogService : null;
        m_TTP = m_App.createTicketParser();
        stockModel = new ProductStockTableModel(new ArrayList<>());

        initComponents();

        jTableProductStock.setModel(stockModel);
        jTableProductStock.setVisible(false);

        user = m_App.getAppUserView().getUser().getName();

        jNumberKeys.setEnabled(true);

        lblTotalQtyValue.setText(null);
        lbTotalValue.setText(null);

        m_LocationsModel = new ComboBoxValModel();
        m_LocationsModelDes = new ComboBoxValModel();

        m_ReasonModel = new ComboBoxValModel();

        m_ReasonModel.add(MovementReason.IN_PURCHASE);                          //Supplier Purchase
        m_ReasonModel.add(MovementReason.OUT_SALE);                             //Sale
        m_ReasonModel.add(MovementReason.IN_REFUND);                            //Customer Refund
        m_ReasonModel.add(MovementReason.OUT_REFUND);                           //Supplier Return
        m_ReasonModel.add(MovementReason.IN_MOVEMENT);                          //Adjust Add
        m_ReasonModel.add(MovementReason.OUT_MOVEMENT);                         //Adjust Subtract
        m_ReasonModel.add(MovementReason.OUT_SUBTRACT);                         //Rectify error per JM requirement        
        m_ReasonModel.add(MovementReason.OUT_BREAK);                            //Breakage
        m_ReasonModel.add(MovementReason.OUT_FREE);                             //Given Free   
        m_ReasonModel.add(MovementReason.OUT_SAMPLE);                           //Given Sample
        m_ReasonModel.add(MovementReason.OUT_USED);                             //Used item   
        m_ReasonModel.add(MovementReason.OUT_CROSSING);                         //Inter-Location move

        m_jreason.setModel(m_ReasonModel);

        m_SuppliersModel = new ComboBoxValModel();

        m_cat = new JCatalog(app);
        m_cat.addActionListener(new CatalogListener());
        catcontainer.add(m_cat.getComponent(), BorderLayout.CENTER);
    }

    @Override
    public String getTitle() {
        return AppLocal.getIntString("Menu.StockMovement");
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    @Override
    public void activate() throws BasicException {
        m_cat.loadCatalog();

        java.util.List<LocationInfo> l = stockService.getLocationsListAll();

        m_LocationsModel = new ComboBoxValModel<LocationInfo>(l);
        m_jLocation.setModel(m_LocationsModel);
        m_LocationsModelDes = new ComboBoxValModel(l);
        m_jLocationDes.setModel(m_LocationsModelDes);

        List<SupplierInfo> sl = m_dlSuppliers.getSupplierListAll();
        m_SuppliersModel = new ComboBoxValModel<SupplierInfo>(sl);
        m_jSupplier.setModel(m_SuppliersModel);

        stateToInsert();

        java.awt.EventQueue.invokeLater(() -> {
            jTextField1.requestFocus();
        });
    }

    public void stateToInsert() {
        m_jdate.setText(Formats.TIMESTAMP.formatValue(DateUtils.getTodayMinutes()));
        m_ReasonModel.setSelectedItem(MovementReason.IN_PURCHASE);
        m_LocationsModel.setSelectedKey(m_App.getInventoryLocation());
        m_LocationsModelDes.setSelectedKey(m_App.getInventoryLocation());
        m_jcodebar.setText(null);
        m_SuppliersModel.setSelectedFirst();
        m_jSupplierDoc.setText(null);
        m_invlines.clear();
        resetTranxTable();
    }

    @Override
    public boolean deactivate() {
        if (m_invlines.getCount() > 0) {
            int res = JOptionPane.showConfirmDialog(this,
                    AppLocal.getIntString("message.wannasave"),
                    AppLocal.getIntString("title.editor"),
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (res == JOptionPane.YES_OPTION) {
                saveData();
                return true;
            } else {
                return res == JOptionPane.NO_OPTION;
            }
        } else {
            return true;
        }
    }

    private void addLine(ProductInfoExt oProduct, double dpor, double dprice) {
        m_invlines.addLine(new InventoryLine(oProduct, dpor, dprice));
        showStockTable();
    }

    private void deleteLine(int index) {
        if (index < 0) {
            com.openbravo.pos.util.NotifyUtils.beep();
        } else {
            m_invlines.deleteLine(index);
            clearStockTable();
            showStockTable();
        }
    }

    private void incProduct(ProductInfoExt product, double units) {
        MovementReason reason = (MovementReason) m_ReasonModel.getSelectedItem();
        addLine(product, units, reason.isInput()
                ? product.getPriceBuy()
                : product.getPriceSell());
    }

    private void incProductByCode(String sCode) {
        incProductByCode(sCode, 1.0);
    }

    private void incProductByCode(String sCode, double dQuantity) {
        try {
            ProductInfoExt oProduct = catalogService.getProductInfoByCode(sCode);
            if (oProduct == null) {
                com.openbravo.pos.util.NotifyUtils.beep();
            } else {
                incProduct(oProduct, dQuantity);
            }
        } catch (BasicException eData) {
            MessageInf msg = new MessageInf(eData);
            msg.show(this);
        }
    }

    private List<ProductStock> getProductStockList(String pId) {
        List<ProductStock> productList = new ArrayList<>();
        try {
            productStockList = stockService.getProductStockList(pId);
            productStockList.stream().forEach((productStock) -> {
                String productId = productStock.getProductId();
                if (productId.equals(pId)) {
                    productList.add(productStock);
                }
            });
        } catch (BasicException ex) {
            LOGGER.log(Level.SEVERE, "Exception get product stock moviments", ex);
        }
        return productList;
    }

    public void resetTranxTable() {
        ensureStockModelNotNull();

        jTableProductStock.getColumnModel().getColumn(0).setPreferredWidth(50);
        jTableProductStock.getColumnModel().getColumn(1).setPreferredWidth(50);
        jTableProductStock.getColumnModel().getColumn(2).setPreferredWidth(50);
        jTableProductStock.getColumnModel().getColumn(3).setPreferredWidth(50);
        jTableProductStock.getColumnModel().getColumn(4).setPreferredWidth(50);
        jTableProductStock.getColumnModel().getColumn(5).setPreferredWidth(50);

        jTableProductStock.repaint();
    }

    public void clearStockTable() {
        ensureStockModelNotNull();
        TableModel tModel = jTableProductStock.getModel();
        if (tModel != null) {
            ProductStockTableModel model = (ProductStockTableModel) tModel;
            while (model.getRowCount() > 0) {
                for (int i = 0; i < model.getRowCount(); ++i) {
                    model.stockList.removeAll(productStockList);
                }
            }
        }

        lblTotalQtyValue.setText(null);
        lbTotalValue.setText(null);

        jTableProductStock.repaint();
    }

    private void ensureStockModelNotNull() {
        if (stockModel == null) {
            stockModel = new ProductStockTableModel(new ArrayList<>());
        }
    }

    public void showStockTable() {
        ensureStockModelNotNull();

        String pId = null;
        int i = m_invlines.getSelectedRow();

        if (i < 0) {
            com.openbravo.pos.util.NotifyUtils.beep();
        } else {
            InventoryLine line = m_invlines.getLine(i);
            pId = line.getProductID();
        }
        if (pId != null) {
            List<ProductStock> stockMos = getProductStockList(pId);
            stockModel = new ProductStockTableModel(stockMos);

            jTableProductStock.setModel(stockModel);
            if (stockModel.getRowCount() > 0) {
                jTableProductStock.setVisible(true);
            } else {
                jTableProductStock.setVisible(false);
                JOptionPane.showMessageDialog(this,
                        AppLocal.getIntString("message.nostocklocation"),
                        AppLocal.getIntString("message.title.nostocklocation"),
                        JOptionPane.INFORMATION_MESSAGE);
            }
            sumStockTable();
            resetTranxTable();
        }

    }

    public void sumStockTable() {
        ensureStockModelNotNull();
        double totalQty = 0;
        double totalVal = 0;
        double lQty = 0;
        double lVal = 0;

        for (int i = 0; i < stockModel.getRowCount(); i++) {
            totalQty += Double.parseDouble(stockModel.getValueAt(i, 1).toString());
            totalVal += Double.parseDouble(stockModel.getValueAt(i, 5).toString());
            totalVal = Math.round(totalVal * 100);
            totalVal = totalVal / 100;
        }

        int i = m_invlines.getSelectedRow();
        lQty = m_invlines.getLine(i).getMultiply();
        lVal = m_invlines.getLine(i).getPrice() * lQty;
        lVal = Math.round(lVal * 100);
        lVal = lVal / 100;

        MovementReason reason = (MovementReason) m_ReasonModel.getSelectedItem();

        if (reason == MovementReason.OUT_BREAK
                || reason == MovementReason.OUT_FREE || reason == MovementReason.OUT_REFUND
                || reason == MovementReason.OUT_SALE || reason == MovementReason.OUT_SAMPLE
                || reason == MovementReason.OUT_SUBTRACT || reason == MovementReason.OUT_USED) {
            lblTotalQtyValue.setText(Double.toString(totalQty -= lQty));
            lbTotalValue.setText(Double.toString(totalVal -= lVal));
        } else {
            lblTotalQtyValue.setText(Double.toString(lQty += totalQty));
            lbTotalValue.setText(Double.toString(lVal += totalVal));
        }
    }

    private void addUnits(double dUnits) {
        int i = m_invlines.getSelectedRow();
        if (i >= 0) {
            InventoryLine inv = m_invlines.getLine(i);
            double dunits = inv.getMultiply() + dUnits;
            if (dunits <= 0.0) {
                deleteLine(i);
            } else {
                inv.setMultiply(inv.getMultiply() + dUnits);
                m_invlines.setLine(i, inv);
            }
            sumStockTable();
        }
    }

    private void setUnits(double dUnits) {
        int i = m_invlines.getSelectedRow();
        if (i >= 0) {
            InventoryLine inv = m_invlines.getLine(i);
            inv.setMultiply(dUnits);
            m_invlines.setLine(i, inv);
        }
    }

    private void stateTransition(char cTrans) {
        if (cTrans == '\n') {
            m_jEnter.doClick();
        }
        if (cTrans == '\u007f') {
            m_jcodebar.setText(null);
            NUMBER_STATE = DEFAULT;
        } else if (cTrans == '*') {
            MULTIPLY = ACTIVE;
        } else if (cTrans == '+') {
            if (MULTIPLY != DEFAULT && NUMBER_STATE != DEFAULT) {
                setUnits(Double.parseDouble(m_jcodebar.getText()));
                m_jcodebar.setText(null);
            } else {
                if (m_jcodebar.getText() == null || m_jcodebar.getText().equals("")) {
                    addUnits(1.0);
                } else {
                    addUnits(Double.parseDouble(m_jcodebar.getText()));
                    m_jcodebar.setText(null);
                }
            }
            NUMBER_STATE = DEFAULT;
            MULTIPLY = DEFAULT;
        } else if (cTrans == '-') {
            if (m_jcodebar.getText() == null || m_jcodebar.getText().equals("")) {
                addUnits(-1.0);
            } else {
                addUnits(-Double.parseDouble(m_jcodebar.getText()));
                m_jcodebar.setText(null);
            }
            NUMBER_STATE = DEFAULT;
            MULTIPLY = DEFAULT;
        } else if (cTrans == '.') {
            if (m_jcodebar.getText() == null || m_jcodebar.getText().equals("")) {
                m_jcodebar.setText("0.");
            } else if (NUMBER_STATE != DECIMAL) {
                m_jcodebar.setText(m_jcodebar.getText() + cTrans);
            }
            NUMBER_STATE = DECIMAL;
        } else if (cTrans == ' ' || cTrans == '=') {
            if (m_invlines.getCount() == 0) {
                com.openbravo.pos.util.NotifyUtils.beep();
            } else {
                saveData();
                jNumberKeys.setEnabled(true);
            }
        } else if (Character.isDigit(cTrans)) {
            if (m_jcodebar.getText() == null) {
                m_jcodebar.setText("" + cTrans);
            } else {
                m_jcodebar.setText(m_jcodebar.getText() + cTrans);
            }
            if (NUMBER_STATE != DECIMAL) {
                NUMBER_STATE = ACTIVE;
            }
        } else if (Character.isAlphabetic(cTrans)) {
            if (m_jcodebar.getText() == null) {
                m_jcodebar.setText("" + cTrans);
            } else {
                m_jcodebar.setText(m_jcodebar.getText() + cTrans);
            }
            if (NUMBER_STATE != DECIMAL) {
                NUMBER_STATE = ACTIVE;
            }
        } else {
            com.openbravo.pos.util.NotifyUtils.beep();
        }
    }

    protected void buttonTransition(ProductInfoExt prod) {
        incProduct(prod);
    }

    private void saveData() {
        try {
            Date d = Formats.TIMESTAMP.parseValue(m_jdate.getText());
            MovementReason reason = (MovementReason) m_ReasonModel.getSelectedItem();

            if (reason == MovementReason.OUT_CROSSING) {
                saveData(new InventoryRecord(
                        d, MovementReason.OUT_MOVEMENT,
                        (LocationInfo) m_LocationsModel.getSelectedItem(),
                        m_App.getAppUserView().getUser().getName(),
                        (SupplierInfo) m_SuppliersModel.getSelectedItem(),
                        m_invlines.getLines(),
                        m_jSupplierDoc.getText()
                ));
                saveData(new InventoryRecord(
                        d, MovementReason.IN_MOVEMENT,
                        (LocationInfo) m_LocationsModelDes.getSelectedItem(),
                        m_App.getAppUserView().getUser().getName(),
                        (SupplierInfo) m_SuppliersModel.getSelectedItem(),
                        m_invlines.getLines(),
                        m_jSupplierDoc.getText()
                ));
            } else {
                saveData(new InventoryRecord(
                        d, reason,
                        (LocationInfo) m_LocationsModel.getSelectedItem(),
                        m_App.getAppUserView().getUser().getName(),
                        (SupplierInfo) m_SuppliersModel.getSelectedItem(),
                        m_invlines.getLines(),
                        m_jSupplierDoc.getText()
                ));
            }

            stateToInsert();
        } catch (BasicException eData) {
            MessageInf msg = new MessageInf(MessageInf.SGN_NOTICE,
                    AppLocal.getIntString("message.cannotsaveinventorydata"), eData);
            msg.show(this);
        }
    }

    private void saveData(InventoryRecord rec) throws BasicException {
        for (int i = 0; i < m_invlines.getCount(); i++) {
            InventoryLine inv = rec.getLines().get(i);

            ProductStockTransaction pst = new ProductStockTransaction();
            pst.setId(UUID.randomUUID().toString());
            pst.setTransactionDate(rec.getDate());
            pst.setReasonId((int) rec.getReason().getKey());
            pst.setLocationId(rec.getLocation().getID());
            pst.setProductId(inv.getProductID());
            pst.setProductAttribSetId(inv.getProductAttSetInstId());
            pst.setUnits(rec.getReason().samesignum(inv.getMultiply()));
            pst.setPrice(inv.getPrice());
            pst.setUserId(rec.getUser());
            pst.setSupplierId(rec.getSupplier().getId());
            pst.setSupplierDoc(rec.getSupplierDoc());
            stockService.saveStockDiary(pst);
        }

        clearStockTable();
        printTicket(rec);
    }

    private void printTicket(InventoryRecord invrec) {
        String sresource = m_dlSystem.getResourceAsXML("Printer.Inventory");
        if (sresource == null) {
            MessageInf msg = new MessageInf(MessageInf.SGN_WARNING,
                    AppLocal.getIntString("message.cannotprintticket"));
            msg.show(this);
        } else {
            try {
                ScriptEngine script = ScriptFactory.getScriptEngine(ScriptFactory.VELOCITY);
                script.put("inventoryrecord", invrec);
                m_TTP.printTicket(script.eval(sresource).toString());

            } catch (ScriptException | TicketPrinterException e) {
                MessageInf msg = new MessageInf(MessageInf.SGN_WARNING,
                        AppLocal.getIntString("message.cannotprintticket"), e);
                msg.show(this);
            }
        }
    }

    class ProductStockTableModel extends DefaultTableModel {
        private static final long serialVersionUID = 1L;

        private String loc = AppLocal.getIntString("label.tblProdHeaderCol1");
        private String qty = AppLocal.getIntString("label.tblProdHeaderCol2");
        private String max = AppLocal.getIntString("label.tblProdHeaderCol3");
        private String min = AppLocal.getIntString("label.tblProdHeaderCol4");
        private String buy = AppLocal.getIntString("label.tblProdHeaderCol5");
        private String val = AppLocal.getIntString("label.tblProdHeaderCol6");

        private List<ProductStock> stockList = new ArrayList<>();
        private String[] columnNames = {loc, qty, max, min, buy, val};

        public ProductStockTableModel() {
            this.stockList = new ArrayList<>();
        }

        public ProductStockTableModel(List<ProductStock> list) {
            this.stockList = list;
        }

        @Override
        public int getColumnCount() {
            return 6;
        }

        @Override
        public int getRowCount() {
            return (this.stockList != null) ? this.stockList.size() : 0;
        }

        @Override
        public Object getValueAt(int row, int column) {
            ProductStock productStock = stockList.get(row);
            switch (column) {
                case 0: return productStock.getLocation();
                case 1: return productStock.getUnits();
                case 2: return productStock.getMinimum();
                case 3: return productStock.getMaximum();
                case 4: return productStock.getPriceSell();
                case 5: return productStock.getUnits() * productStock.getPriceSell();
                case 6: return productStock.getProductId();
                default: return "";
            }
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 0 || columnIndex == 6) {
                return String.class;
            }
            return Double.class;
        }

        @Override
        public String getColumnName(int col) {
            return columnNames[col];
        }
    }

    private class CatalogListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String sQty = m_jcodebar.getText();
            if (sQty != null && !sQty.isEmpty()) {
                Double dQty = (Double.valueOf(sQty) == 0) ? 1.0 : Double.valueOf(sQty);
                incProduct((ProductInfoExt) e.getSource(), dQty);
                m_jcodebar.setText(null);
            } else {
                incProduct((ProductInfoExt) e.getSource(), 1.0);
            }
        }
    }

    private void removeInvLine(int index) {
        if (index < 0) {
            com.openbravo.pos.util.NotifyUtils.beep();
        } else {
            m_invlines.deleteLine(index);
            clearStockTable();
            showStockTable();
        }
    }

    public void deleteTicket(int index) {
        while (index < m_invlines.getCount()) {
            m_invlines.deleteLine(index);
        }
    }

    private void incProduct(ProductInfoExt prod) {
        incProduct(1.0, prod);
    }

    private void incProduct(double dPor, ProductInfoExt prod) {
        addLine(prod, dPor, prod.getPriceBuy());
    }

    private void stateToZero() {
        m_sBarcode = new StringBuffer();
        m_iNumberStatus = NUMBER_INPUTZERO;
        m_iNumberStatusInput = NUMBERZERO;
        m_iNumberStatusPor = NUMBERZERO;
        repaint();
    }

    private void initComponents() {
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("pos_messages");

        setLayout(new BorderLayout());

        // --- TOP AREA: Form, Tables, and Keypad ---
        JPanel topAreaPanel = new JPanel(new BorderLayout(10, 10));
        topAreaPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 1. LEFT: Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;
        
        // Date
        JPanel datePanel = new JPanel(new BorderLayout(5, 0));
        m_jdate = new JTextField(15);
        m_jbtndate = new JButton(new ImageIcon(getClass().getResource("/com/openbravo/images/date.png")));
        m_jbtndate.setToolTipText("Open Calendar");
        m_jbtndate.addActionListener(this::m_jbtndateActionPerformed);
        datePanel.add(m_jdate, BorderLayout.CENTER);
        datePanel.add(m_jbtndate, BorderLayout.EAST);
        addFormField(formPanel, AppLocal.getIntString("label.stockdate"), datePanel, gbc, row++);

        // Reason
        m_jreason = new JComboBox<>();
        m_jreason.addActionListener(this::m_jreasonActionPerformed);
        addFormField(formPanel, AppLocal.getIntString("label.stockreason"), m_jreason, gbc, row++);

        // Location
        m_jLocation = new JComboBox<>();
        m_jLocation.addActionListener(this::m_jLocationActionPerformed);
        addFormField(formPanel, AppLocal.getIntString("label.locationplace"), m_jLocation, gbc, row++);

        // Location Dest
        m_jLocationDes = new JComboBox<>();
        addFormField(formPanel, " ", m_jLocationDes, gbc, row++);

        // Supplier
        m_jSupplier = new JComboBox<>();
        m_jSupplier.addActionListener(this::m_jSupplierActionPerformed);
        addFormField(formPanel, AppLocal.getIntString("label.supplier"), m_jSupplier, gbc, row++);

        // Supplier Doc
        m_jSupplierDoc = new JTextField(15);
        addFormField(formPanel, AppLocal.getIntString("label.supplierdocment"), m_jSupplierDoc, gbc, row++);

        // Push form components to the top
        gbc.gridy = row;
        gbc.weighty = 1.0;
        formPanel.add(Box.createVerticalGlue(), gbc);

        topAreaPanel.add(formPanel, BorderLayout.WEST);

        // 2. CENTER: Active Lines and Stock Table
        JPanel tablesPanel = new JPanel(new BorderLayout(0, 10));

        // Bottom Stock Panel (contains table and totals)
        JPanel stockBottomPanel = new JPanel(new BorderLayout(5, 5));
        
        jTableProductStock = new JTable();
        jTableProductStock.setRowHeight(25);
        JScrollPane scrollTableStock = new JScrollPane(jTableProductStock);
        scrollTableStock.setBorder(BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor")));
        stockBottomPanel.add(scrollTableStock, BorderLayout.CENTER);

        // Totals Bar
        JPanel totalsBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        m_jBtnShowStock = new JButton(new ImageIcon(getClass().getResource("/com/openbravo/images/pay.png")));
        m_jBtnShowStock.setToolTipText(AppLocal.getIntString("tooltip.salecheckstock"));
        m_jBtnShowStock.addActionListener(this::m_jBtnShowStockActionPerformed);

        webLblQty = new JLabel(AppLocal.getIntString("label.stock.quantity") + ":");
        lblTotalQtyValue = new JLabel("0.0");
        webLblValue = new JLabel(AppLocal.getIntString("label.stock.value") + ":");
        lbTotalValue = new JLabel("0.0");

        totalsBar.add(m_jBtnShowStock);
        totalsBar.add(webLblQty);
        totalsBar.add(lblTotalQtyValue);
        totalsBar.add(Box.createHorizontalStrut(20));
        totalsBar.add(webLblValue);
        totalsBar.add(lbTotalValue);
        
        stockBottomPanel.add(totalsBar, BorderLayout.SOUTH);

        JSplitPane tablesSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, m_invlines, stockBottomPanel);
        tablesSplitPane.setResizeWeight(0.65);
        tablesSplitPane.setDividerSize(3);
        tablesSplitPane.setBorder(null);

        tablesPanel.add(tablesSplitPane, BorderLayout.CENTER);
        topAreaPanel.add(tablesPanel, BorderLayout.CENTER);

        // 3. RIGHT: Actions, Keypad, and Barcode
        JPanel rightPanel = new JPanel(new BorderLayout(10, 5));

        // --- FIX: Prevent vertical stretching by anchoring to the NORTH ---
        JPanel topAnchoredGroup = new JPanel(new BorderLayout(10, 0));

        // Action Buttons
        JPanel actionButtons = new JPanel(new GridLayout(0, 1, 0, 5));
        m_jDelete = createActionButton("/com/openbravo/images/editdelete.png", bundle.getString("tooltip.saleremoveline"), this::m_jDeleteActionPerformed);
        m_jList = createActionButton("/com/openbravo/images/search32.png", bundle.getString("tooltip.saleproductfind"), this::m_jListActionPerformed);
        m_jEditLine = createActionButton("/com/openbravo/images/sale_editline.png", bundle.getString("tooltip.saleeditline"), this::m_jEditLineActionPerformed);
        m_jEditAttributes = createActionButton("/com/openbravo/images/attributes.png", bundle.getString("tooltip.saleattributes"), this::m_jEditAttributesActionPerformed);
        m_jBtnDelete = createActionButton("/com/openbravo/images/sale_delete.png", AppLocal.getIntString("button.delete"), this::m_jBtnDeleteActionPerformed);

        actionButtons.add(m_jDelete);
        actionButtons.add(m_jList);
        actionButtons.add(m_jEditLine);
        actionButtons.add(m_jEditAttributes);
        actionButtons.add(m_jBtnDelete);
        topAnchoredGroup.add(actionButtons, BorderLayout.WEST);

        // Keypad (Wrapped in FlowLayout so it respects preferred size and doesn't stretch)
        jNumberKeys = new com.openbravo.beans.JNumberKeys();
        jNumberKeys.setPreferredSize(new Dimension(300, 300));
        jNumberKeys.addJNumberEventListener(this::jNumberKeysKeyPerformed);
        
        JPanel keypadWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        keypadWrapper.add(jNumberKeys);
        topAnchoredGroup.add(keypadWrapper, BorderLayout.CENTER);
        
        // Add the group to NORTH so it aligns to the top perfectly 
        rightPanel.add(topAnchoredGroup, BorderLayout.NORTH);
        // ------------------------------------------------------------------

        // Barcode Input
        JPanel barcodePanel = new JPanel(new BorderLayout(5, 0));
        m_jcodebar = new JLabel();
        m_jcodebar.setOpaque(true);
        m_jcodebar.setBackground(Color.WHITE);
        m_jcodebar.setBorder(BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor")));
        m_jcodebar.setHorizontalAlignment(SwingConstants.RIGHT);
        m_jcodebar.setPreferredSize(new Dimension(130, 35));
        m_jcodebar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                m_jcodebarMouseClicked(evt);
            }
        });

        m_jEnter = new JButton(new ImageIcon(getClass().getResource("/com/openbravo/images/barcode.png")));
        m_jEnter.setFocusPainted(false);
        m_jEnter.addActionListener(this::m_jEnterActionPerformed);

        barcodePanel.add(m_jcodebar, BorderLayout.CENTER);
        barcodePanel.add(m_jEnter, BorderLayout.EAST);
        
        // Push Barcode input to the bottom
        rightPanel.add(barcodePanel, BorderLayout.SOUTH);

        topAreaPanel.add(rightPanel, BorderLayout.EAST);

        // Hidden listener for scanner input
        jTextField1 = new JTextField();
        jTextField1.setPreferredSize(new Dimension(0, 0));
        jTextField1.setBorder(null);
        jTextField1.setOpaque(false);
        jTextField1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextField1KeyTyped(evt);
            }
        });
        topAreaPanel.add(jTextField1, BorderLayout.NORTH);

        // --- BOTTOM AREA: Catalog ---
        catcontainer = new JPanel(new BorderLayout());

        // Master Split Pane linking Top Area and Catalog
        JSplitPane masterSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topAreaPanel, catcontainer);
        masterSplitPane.setResizeWeight(0.55);
        masterSplitPane.setDividerSize(4);
        masterSplitPane.setBorder(null);

        add(masterSplitPane, BorderLayout.CENTER);
    }

    private void addFormField(JPanel panel, String labelText, JComponent field, GridBagConstraints gbc, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel(labelText), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(field, gbc);
    }

    private JButton createActionButton(String iconPath, String tooltip, ActionListener listener) {
        JButton btn = new JButton(new ImageIcon(getClass().getResource(iconPath)));
        btn.setToolTipText(tooltip);
        btn.setFocusPainted(false);
        btn.setFocusable(false);
        btn.setMargin(new Insets(8, 14, 8, 14));
        btn.addActionListener(listener);
        return btn;
    }

    private void jTextField1KeyTyped(java.awt.event.KeyEvent evt) {                                      
        jTextField1.setText(null);
        stateTransition(evt.getKeyChar());
    }                                     

    private void jNumberKeysKeyPerformed(com.openbravo.beans.JNumberEvent evt) {                                         
        stateTransition(evt.getKey());
    }                                        

    private void m_jreasonActionPerformed(java.awt.event.ActionEvent evt) {                                          
        m_jLocationDes.setEnabled(m_ReasonModel.getSelectedItem() == MovementReason.OUT_CROSSING);
    }                                         

    private void m_jbtndateActionPerformed(java.awt.event.ActionEvent evt) {                                           
        Date date;
        try {
            date = (Date) Formats.TIMESTAMP.parseValue(m_jdate.getText());
        } catch (BasicException e) {
            date = null;
        }
        date = JCalendarPanel2.showCalendarTime(this, date);
        if (date != null) {
            m_jdate.setText(Formats.TIMESTAMP.formatValue(date));
        }
    }                                          

    private void m_jEnterActionPerformed(java.awt.event.ActionEvent evt) {                                         
        incProductByCode(m_jcodebar.getText());
        m_jcodebar.setText(null);
        if (m_jSupplier.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this,
                    AppLocal.getIntString("message.supplierinvalid"),
                    AppLocal.getIntString("message.title.supplierinvalid"),
                    JOptionPane.WARNING_MESSAGE);
        }
    }                                        

    private void m_jSupplierActionPerformed(java.awt.event.ActionEvent evt) {                                            
    }                                           

    private void m_jBtnShowStockActionPerformed(java.awt.event.ActionEvent evt) {                                                
        showStockTable();
    }                                               

    private void m_jDeleteActionPerformed(java.awt.event.ActionEvent evt) {                                          
        int i = m_invlines.getSelectedRow();
        if (i < 0) {
            com.openbravo.pos.util.NotifyUtils.beep();
        } else {
            removeInvLine(i);
        }
    }                                         

    private void m_jListActionPerformed(java.awt.event.ActionEvent evt) {                                        
        ProductInfoExt prod = JProductFinderPanel.showMessage(StockManagement.this, m_App);
        if (prod != null) {
            buttonTransition(prod);
        }
    }                                       

    private void m_jEditLineActionPerformed(java.awt.event.ActionEvent evt) {                                            
        int i = m_invlines.getSelectedRow();
        if (i < 0) {
            com.openbravo.pos.util.NotifyUtils.beep();
        } else {
            InventoryLine line = m_invlines.getLine(i);
            JFrame frame = new JFrame("New Price Buy");
            String spricebuy = JOptionPane.showInputDialog(frame,
                    AppLocal.getIntString("message.enterbuyprice"),
                    JOptionPane.INFORMATION_MESSAGE);

            if (spricebuy != null) {
                double dpricebuy = Double.parseDouble(spricebuy);
                line.setPrice(dpricebuy);
                m_invlines.setLine(i, line);
            }
        }
    }                                           

    private void m_jEditAttributesActionPerformed(java.awt.event.ActionEvent evt) {                                                  
        int i = m_invlines.getSelectedRow();
        if (i < 0) {
            com.openbravo.pos.util.NotifyUtils.beep();
        } else {
            try {
                InventoryLine line = m_invlines.getLine(i);
                JProductAttEdit2 attedit = JProductAttEdit2.getAttributesEditor(this, m_App.getSession());
                attedit.editAttributes(line.getProductAttSetId(), line.getProductAttSetInstId());
                attedit.setVisible(true);
                if (attedit.isOK()) {
                    line.setProductAttSetInstId(attedit.getAttributeSetInst());
                    line.setProductAttSetInstDesc(attedit.getAttributeSetInstDescription());
                    m_invlines.setLine(i, line);
                }
            } catch (BasicException ex) {
                MessageInf msg = new MessageInf(MessageInf.SGN_WARNING,
                        AppLocal.getIntString("message.cannotfindattributes"), ex);
                msg.show(this);
            }
        }
    }                                                 

    private void m_jBtnDeleteActionPerformed(java.awt.event.ActionEvent evt) {                                             
        int res = JOptionPane.showConfirmDialog(this,
                AppLocal.getIntString("message.wannadelete"),
                AppLocal.getIntString("title.editor"),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (res == JOptionPane.YES_OPTION) {
            int i = 0;
            while (i < m_invlines.getCount()) {
                m_invlines.deleteLine(i);
            }
            clearStockTable();
            showStockTable();
            jTableProductStock.repaint();
        }
    }                                            

    private void m_jcodebarMouseClicked(java.awt.event.MouseEvent evt) {                                        
        m_jcodebar.requestFocusInWindow();
        jTextField1.requestFocus();
        m_jcodebar.setEnabled(true);
        m_jcodebar.setText(null);
    }                                       

    private void m_jLocationActionPerformed(java.awt.event.ActionEvent evt) {                                            
    }                                           

    // Variables declaration
    private javax.swing.JPanel catcontainer;
    private com.openbravo.beans.JNumberKeys jNumberKeys;
    private javax.swing.JTable jTableProductStock;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JLabel lbTotalValue;
    private javax.swing.JLabel lblTotalQtyValue;
    private javax.swing.JButton m_jBtnDelete;
    private javax.swing.JButton m_jBtnShowStock;
    private javax.swing.JButton m_jDelete;
    private javax.swing.JButton m_jEditAttributes;
    private javax.swing.JButton m_jEditLine;
    private javax.swing.JButton m_jEnter;
    private javax.swing.JButton m_jList;
    private javax.swing.JComboBox m_jLocation;
    private javax.swing.JComboBox m_jLocationDes;
    private javax.swing.JComboBox m_jSupplier;
    private javax.swing.JTextField m_jSupplierDoc;
    private javax.swing.JButton m_jbtndate;
    private javax.swing.JLabel m_jcodebar;
    private javax.swing.JTextField m_jdate;
    private javax.swing.JComboBox m_jreason;
    private javax.swing.JLabel webLblQty;
    private javax.swing.JLabel webLblValue;
    public CatalogService getCatalogService() {
        return catalogService;
    }

    @Deprecated
    public DataLogicPIM getDataLogicPIM() {
        return dataLogicPIM;
    }

    // End of variables declaration                   
}