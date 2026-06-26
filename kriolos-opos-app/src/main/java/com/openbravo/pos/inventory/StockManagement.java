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
package com.openbravo.pos.inventory;

import com.openbravo.pos.forms.JPanelView;
import com.openbravo.pos.forms.DataLogicSales;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.basic.BasicException;
import com.openbravo.beans.DateUtils;
import com.openbravo.beans.JCalendarDialog;
import com.openbravo.data.gui.ComboBoxValModel;
import com.openbravo.data.gui.MessageInf;
import com.openbravo.data.loader.LocalRes;
import com.openbravo.format.Formats;
import com.openbravo.pos.catalog.CatalogSelector;
import com.openbravo.pos.catalog.JCatalog;
import com.openbravo.pos.forms.*;
import com.openbravo.pos.panels.JProductFinder;
import com.openbravo.pos.printer.TicketParser;
import com.openbravo.pos.printer.TicketPrinterException;
import com.openbravo.pos.sales.JProductAttEdit2;
import com.openbravo.pos.scripting.ScriptEngine;
import com.openbravo.pos.scripting.ScriptException;
import com.openbravo.pos.scripting.ScriptFactory;
import com.openbravo.pos.suppliers.DataLogicSuppliers;
import com.openbravo.pos.suppliers.SupplierInfo;
import com.openbravo.pos.ticket.ProductInfoExt;
import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

/**
 * Date : Aug 2017 Updated : Dec 2016
 *
 * @author jack gerrard
 */
public class StockManagement extends JPanel implements JPanelView {

    private final static Logger LOGGER = Logger.getLogger(ProductsEditor.class.getName());

    private final AppView m_App;
    private final String user;

    private final DataLogicSystem m_dlSystem;
    private final DataLogicSales m_dlSales;
    private final DataLogicSuppliers m_dlSuppliers;
    private final TicketParser m_TTP;

    private final CatalogSelector m_cat;
    private final ComboBoxValModel m_ReasonModel;

    private ComboBoxValModel m_LocationsModel;
    private ComboBoxValModel m_LocationsModelDes;

    private ComboBoxValModel m_SuppliersModel;

    private final JInventoryLines m_invlines;

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

    /**
     * Creates new form StockManagement
     *
     * @param app
     */
    public StockManagement(AppView app) {

        m_App = app;
        m_dlSystem = (DataLogicSystem) m_App.getBean("com.openbravo.pos.forms.DataLogicSystem");
        m_dlSales = (DataLogicSales) m_App.getBean("com.openbravo.pos.forms.DataLogicSales");
        m_dlSuppliers = (DataLogicSuppliers) m_App.getBean("com.openbravo.pos.suppliers.DataLogicSuppliers");
        m_TTP = new TicketParser(m_App.getDeviceTicket(), m_dlSystem);
        stockModel = new ProductStockTableModel(new ArrayList<>());

        initComponents();

        jTableProductStock.setModel(stockModel);

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

        m_cat = new JCatalog(m_dlSales);
        m_cat.addActionListener(new CatalogListener());
        catcontainer.add(m_cat.getComponent(), BorderLayout.CENTER);

        m_invlines = new JInventoryLines();
        jPanel5.add(m_invlines, BorderLayout.CENTER);

        jTableProductStock.setVisible(false);

    }

    /**
     *
     * @return
     */
    @Override
    public String getTitle() {
        return AppLocal.getIntString("Menu.StockMovement");
    }

    /**
     *
     * @return
     */
    @Override
    public JComponent getComponent() {
        return this;
    }

    /**
     *
     * @throws BasicException
     */
    @Override
    public void activate() throws BasicException {
        m_cat.loadCatalog();

        java.util.List<LocationInfo> l = m_dlSales.getLocationsListAll();

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

    /**
     *
     */
    public void stateToInsert() {

        m_jdate.setText(Formats.TIMESTAMP.formatValue(DateUtils.getTodayMinutes()));
        m_ReasonModel.setSelectedItem(MovementReason.IN_PURCHASE);
        m_LocationsModel.setSelectedKey(m_App.getInventoryLocation());
//        m_LocationsModel.setSelectedFirst();
        m_LocationsModelDes.setSelectedKey(m_App.getInventoryLocation());
        m_jcodebar.setText(null);
        m_SuppliersModel.setSelectedFirst();
        m_jSupplierDoc.setText(null);
        m_invlines.clear();
        resetTranxTable();
    }

    /**
     *
     * @return
     */
    @Override
    public boolean deactivate() {

        if (m_invlines.getCount() > 0) {
            int res = JOptionPane.showConfirmDialog(this,
                    LocalRes.getIntString("message.wannasave"),
                    LocalRes.getIntString("title.editor"),
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
            Toolkit.getDefaultToolkit().beep();
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
            ProductInfoExt oProduct = m_dlSales.getProductInfoByCode(sCode);
            if (oProduct == null) {
                Toolkit.getDefaultToolkit().beep();
            } else {
                incProduct(oProduct, dQuantity);
            }
        }
        catch (BasicException eData) {
            MessageInf msg = new MessageInf(eData);
            msg.show(this);
        }
    }

    private List<ProductStock> getProductStockList(String pId) {

        List<ProductStock> productList = new ArrayList<>();
        try {
            productStockList = m_dlSales.getProductStockList(pId);
            productStockList.stream().forEach((productStock) -> {
                String productId = productStock.getProductId();
                if (productId.equals(pId)) {
                    productList.add(productStock);
                }
            });

        }
        catch (BasicException ex) {
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
            Toolkit.getDefaultToolkit().beep();
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
// deliberately explicit
            totalVal = Math.round(totalVal * 100);
            totalVal = totalVal / 100;
        }

        int i = m_invlines.getSelectedRow();
        lQty = m_invlines.getLine(i).getMultiply();
        lVal = m_invlines.getLine(i).getPrice() * lQty;
// deliberately explicit
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
                Toolkit.getDefaultToolkit().beep();
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
            Toolkit.getDefaultToolkit().beep();
        }
    }

    /**
     *
     * @param prod
     */
    protected void buttonTransition(ProductInfoExt prod) {

//        if (m_iNumberStatusInput == NUMBERZERO && m_iNumberStatusPor == NUMBERZERO) {
        incProduct(prod);
//        } else {
//            Toolkit.getDefaultToolkit().beep();
//        }      
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
        }
        catch (BasicException eData) {
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
            m_dlSales.saveStockDiary(pst);
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

            }
            catch (ScriptException | TicketPrinterException e) {
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
            return (this.stockList != null) ? this.stockList.size() :0;
        }

        @Override
        public Object getValueAt(int row, int column) {
            ProductStock productStock = stockList.get(row);

            switch (column) {
                case 0:
                    return productStock.getLocation();
                case 1:
                    return productStock.getUnits();
                case 2:
                    return productStock.getMinimum();
                case 3:
                    return productStock.getMaximum();
                case 4:
                    return productStock.getPriceSell();
                case 5:
                    return productStock.getUnits() * productStock.getPriceSell();
                case 6:
                    return productStock.getProductId();
                default:
                    return ""; //super.getValueAt(row, column);
            }

        }

        @Override
        public boolean isCellEditable(int row, int column) {
            // Example: make only certain columns editable
            if (column == 0) { // Make the first column uneditable
                return false;
            }
            if(column == 1){
                return false;
            }
            if(column == 2){
                return false;
            }
            if(column == 3){
                return false;
            }
            if(column == 4){
                return false;
            }
            if(column == 5){
                return false;
            }
            if(column == 6){
                return false;
            }
            return super.isCellEditable(row, column); // Default behavior for other columns
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 0) {
                return String.class;
            }
            if (columnIndex == 1) {
                return Double.class;
            }
            if (columnIndex == 2) {
                return Double.class;
            }
            if(columnIndex == 3){
                return Double.class;
            }
            if(columnIndex == 4){
                return Double.class;
            }
            if(columnIndex == 5){
                return Double.class;
            }
            if(columnIndex == 6){
                return String.class;
            }
            return super.getColumnClass(columnIndex); // Default behavior for other columns
        }

        public Object setValueAt(int row, int column) {
            ProductStock productStock = stockList.get(row);

            switch (column) {
                case 0:
                    return productStock.getLocation();
                case 1:
                    return productStock.getUnits();
                case 2:
                    return productStock.getMinimum();
                case 3:
                    return productStock.getMaximum();
                case 4:
                    return productStock.getPriceSell();
                case 5:
                    return productStock.getUnits() * productStock.getPriceSell();
                case 6:
                    return productStock.getProductId();
                default:
                    return "";
            }
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
            if (sQty != null) {
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
            Toolkit.getDefaultToolkit().beep();
        } else {
            m_invlines.deleteLine(index);
            clearStockTable();
            showStockTable();

        }

    }

    /**
     *
     * @param index
     */
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

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel8 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        m_jdate = new javax.swing.JTextField();
        m_jbtndate = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        m_jreason = new javax.swing.JComboBox();
        jLabel8 = new javax.swing.JLabel();
        m_jLocation = new javax.swing.JComboBox();
        m_jLocationDes = new javax.swing.JComboBox();
        jLabel10 = new javax.swing.JLabel();
        m_jSupplier = new javax.swing.JComboBox();
        jLabel9 = new javax.swing.JLabel();
        m_jSupplierDoc = new javax.swing.JTextField();
        jPanel5 = new javax.swing.JPanel();
        m_jcodebar = new javax.swing.JLabel();
        m_jEnter = new javax.swing.JButton();
        jTextField1 = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        m_jDelete = new javax.swing.JButton();
        m_jList = new javax.swing.JButton();
        m_jEditLine = new javax.swing.JButton();
        m_jEditAttributes = new javax.swing.JButton();
        m_jBtnDelete = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jNumberKeys = new com.openbravo.beans.JNumberKeys();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTableProductStock = new javax.swing.JTable();
        m_jBtnShowStock = new javax.swing.JButton();
        lblTotalQtyValue = new javax.swing.JLabel();
        lbTotalValue = new javax.swing.JLabel();
        webLblQty = new javax.swing.JLabel();
        webLblValue = new javax.swing.JLabel();
        catcontainer = new javax.swing.JPanel();

        setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        setMinimumSize(new java.awt.Dimension(550, 250));
        setPreferredSize(new java.awt.Dimension(1000, 350));
        setLayout(new java.awt.BorderLayout());

        jPanel8.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jPanel8.setMinimumSize(new java.awt.Dimension(0, 320));
        jPanel8.setPreferredSize(new java.awt.Dimension(0, 320));
        jPanel8.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel1.setText(AppLocal.getIntString("label.stockdate")); // NOI18N
        jLabel1.setMaximumSize(new java.awt.Dimension(40, 25));
        jLabel1.setMinimumSize(new java.awt.Dimension(40, 25));
        jLabel1.setPreferredSize(new java.awt.Dimension(70, 30));
        jPanel8.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(5, 5, -1, -1));

        m_jdate.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jdate.setPreferredSize(new java.awt.Dimension(160, 30));
        jPanel8.add(m_jdate, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 5, -1, -1));

        m_jbtndate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/date.png"))); // NOI18N
        m_jbtndate.setToolTipText("Open Calendar");
        m_jbtndate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jbtndateActionPerformed(evt);
            }
        });
        jPanel8.add(m_jbtndate, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 5, 40, 30));

        jLabel2.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel2.setText(AppLocal.getIntString("label.stockreason")); // NOI18N
        jLabel2.setMaximumSize(new java.awt.Dimension(40, 25));
        jLabel2.setMinimumSize(new java.awt.Dimension(40, 25));
        jLabel2.setPreferredSize(new java.awt.Dimension(70, 30));
        jPanel8.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(5, 40, -1, -1));

        m_jreason.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jreason.setMaximumRowCount(13);
        m_jreason.setPreferredSize(new java.awt.Dimension(160, 30));
        m_jreason.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jreasonActionPerformed(evt);
            }
        });
        jPanel8.add(m_jreason, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 40, -1, -1));

        jLabel8.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel8.setText(AppLocal.getIntString("label.locationplace")); // NOI18N
        jLabel8.setMaximumSize(new java.awt.Dimension(40, 25));
        jLabel8.setMinimumSize(new java.awt.Dimension(40, 25));
        jLabel8.setPreferredSize(new java.awt.Dimension(70, 30));
        jPanel8.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(5, 75, -1, -1));

        m_jLocation.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jLocation.setPreferredSize(new java.awt.Dimension(160, 30));
        m_jLocation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jLocationActionPerformed(evt);
            }
        });
        jPanel8.add(m_jLocation, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 75, -1, -1));

        m_jLocationDes.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jLocationDes.setPreferredSize(new java.awt.Dimension(160, 30));
        jPanel8.add(m_jLocationDes, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 110, -1, -1));

        jLabel10.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel10.setText(AppLocal.getIntString("label.supplier")); // NOI18N
        jLabel10.setMaximumSize(new java.awt.Dimension(40, 25));
        jLabel10.setMinimumSize(new java.awt.Dimension(40, 25));
        jLabel10.setPreferredSize(new java.awt.Dimension(70, 30));
        jPanel8.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(5, 145, -1, -1));

        m_jSupplier.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jSupplier.setPreferredSize(new java.awt.Dimension(160, 30));
        m_jSupplier.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jSupplierActionPerformed(evt);
            }
        });
        jPanel8.add(m_jSupplier, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 145, -1, -1));

        jLabel9.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel9.setText(AppLocal.getIntString("label.supplierdocment")); // NOI18N
        jLabel9.setToolTipText("null");
        jLabel9.setMaximumSize(new java.awt.Dimension(40, 25));
        jLabel9.setMinimumSize(new java.awt.Dimension(40, 25));
        jLabel9.setPreferredSize(new java.awt.Dimension(70, 30));
        jPanel8.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(5, 180, -1, -1));

        m_jSupplierDoc.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jSupplierDoc.setToolTipText(AppLocal.getIntString("button.exit")); // NOI18N
        m_jSupplierDoc.setPreferredSize(new java.awt.Dimension(160, 30));
        jPanel8.add(m_jSupplierDoc, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 180, -1, -1));

        jPanel5.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        jPanel5.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jPanel5.setMinimumSize(new java.awt.Dimension(455, 245));
        jPanel5.setPreferredSize(new java.awt.Dimension(455, 245));
        jPanel5.setLayout(new java.awt.BorderLayout());
        jPanel8.add(jPanel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 5, -1, 190));

        m_jcodebar.setBackground(java.awt.Color.white);
        m_jcodebar.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        m_jcodebar.setForeground(new java.awt.Color(76, 197, 237));
        m_jcodebar.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        m_jcodebar.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)), javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1)));
        m_jcodebar.setOpaque(true);
        m_jcodebar.setPreferredSize(new java.awt.Dimension(130, 25));
        m_jcodebar.setRequestFocusEnabled(false);
        m_jcodebar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                m_jcodebarMouseClicked(evt);
            }
        });
        jPanel8.add(m_jcodebar, new org.netbeans.lib.awtextra.AbsoluteConstraints(780, 270, -1, -1));

        m_jEnter.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jEnter.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/barcode.png"))); // NOI18N
        m_jEnter.setFocusPainted(false);
        m_jEnter.setFocusable(false);
        m_jEnter.setPreferredSize(new java.awt.Dimension(54, 45));
        m_jEnter.setRequestFocusEnabled(false);
        m_jEnter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jEnterActionPerformed(evt);
            }
        });
        jPanel8.add(m_jEnter, new org.netbeans.lib.awtextra.AbsoluteConstraints(920, 260, -1, -1));

        jTextField1.setBackground(javax.swing.UIManager.getDefaults().getColor("Panel.background"));
        jTextField1.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jTextField1.setForeground(new java.awt.Color(255, 255, 255));
        jTextField1.setCaretColor(javax.swing.UIManager.getDefaults().getColor("Panel.background"));
        jTextField1.setPreferredSize(new java.awt.Dimension(1, 1));
        jTextField1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextField1KeyTyped(evt);
            }
        });
        jPanel8.add(jTextField1, new org.netbeans.lib.awtextra.AbsoluteConstraints(1, 1, -1, 0));

        jPanel2.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 5, 0, 5));
        jPanel2.setPreferredSize(new java.awt.Dimension(70, 250));
        jPanel2.setLayout(new java.awt.GridLayout(0, 1, 5, 5));

        m_jDelete.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/editdelete.png"))); // NOI18N
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("pos_messages"); // NOI18N
        m_jDelete.setToolTipText(bundle.getString("tooltip.saleremoveline")); // NOI18N
        m_jDelete.setFocusPainted(false);
        m_jDelete.setFocusable(false);
        m_jDelete.setMargin(new java.awt.Insets(8, 14, 8, 14));
        m_jDelete.setMaximumSize(new java.awt.Dimension(42, 36));
        m_jDelete.setMinimumSize(new java.awt.Dimension(42, 36));
        m_jDelete.setPreferredSize(new java.awt.Dimension(50, 45));
        m_jDelete.setRequestFocusEnabled(false);
        m_jDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jDeleteActionPerformed(evt);
            }
        });
        jPanel2.add(m_jDelete);

        m_jList.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/search32.png"))); // NOI18N
        m_jList.setToolTipText(bundle.getString("tooltip.saleproductfind")); // NOI18N
        m_jList.setFocusPainted(false);
        m_jList.setFocusable(false);
        m_jList.setMargin(new java.awt.Insets(8, 14, 8, 14));
        m_jList.setMaximumSize(new java.awt.Dimension(42, 36));
        m_jList.setMinimumSize(new java.awt.Dimension(42, 36));
        m_jList.setPreferredSize(new java.awt.Dimension(50, 45));
        m_jList.setRequestFocusEnabled(false);
        m_jList.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jListActionPerformed(evt);
            }
        });
        jPanel2.add(m_jList);

        m_jEditLine.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/sale_editline.png"))); // NOI18N
        m_jEditLine.setToolTipText(bundle.getString("tooltip.saleeditline")); // NOI18N
        m_jEditLine.setFocusPainted(false);
        m_jEditLine.setFocusable(false);
        m_jEditLine.setMargin(new java.awt.Insets(8, 14, 8, 14));
        m_jEditLine.setMaximumSize(new java.awt.Dimension(42, 36));
        m_jEditLine.setMinimumSize(new java.awt.Dimension(42, 36));
        m_jEditLine.setPreferredSize(new java.awt.Dimension(50, 45));
        m_jEditLine.setRequestFocusEnabled(false);
        m_jEditLine.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jEditLineActionPerformed(evt);
            }
        });
        jPanel2.add(m_jEditLine);

        m_jEditAttributes.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/attributes.png"))); // NOI18N
        m_jEditAttributes.setToolTipText(bundle.getString("tooltip.saleattributes")); // NOI18N
        m_jEditAttributes.setFocusPainted(false);
        m_jEditAttributes.setFocusable(false);
        m_jEditAttributes.setMargin(new java.awt.Insets(8, 14, 8, 14));
        m_jEditAttributes.setMaximumSize(new java.awt.Dimension(42, 36));
        m_jEditAttributes.setMinimumSize(new java.awt.Dimension(42, 36));
        m_jEditAttributes.setPreferredSize(new java.awt.Dimension(50, 45));
        m_jEditAttributes.setRequestFocusEnabled(false);
        m_jEditAttributes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jEditAttributesActionPerformed(evt);
            }
        });
        jPanel2.add(m_jEditAttributes);

        m_jBtnDelete.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        m_jBtnDelete.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/sale_delete.png"))); // NOI18N
        m_jBtnDelete.setToolTipText(AppLocal.getIntString("button.delete")); // NOI18N
        m_jBtnDelete.setFocusPainted(false);
        m_jBtnDelete.setFocusable(false);
        m_jBtnDelete.setMargin(new java.awt.Insets(0, 4, 0, 4));
        m_jBtnDelete.setMaximumSize(new java.awt.Dimension(50, 40));
        m_jBtnDelete.setMinimumSize(new java.awt.Dimension(50, 40));
        m_jBtnDelete.setPreferredSize(new java.awt.Dimension(80, 45));
        m_jBtnDelete.setRequestFocusEnabled(false);
        m_jBtnDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jBtnDeleteActionPerformed(evt);
            }
        });
        jPanel2.add(m_jBtnDelete);

        jPanel8.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(705, 0, -1, -1));

        jPanel1.setMinimumSize(new java.awt.Dimension(150, 250));
        jPanel1.setPreferredSize(new java.awt.Dimension(200, 250));

        jNumberKeys.setPreferredSize(new java.awt.Dimension(210, 240));
        jNumberKeys.addJNumberEventListener(new com.openbravo.beans.JNumberEventListener() {
            public void keyPerformed(com.openbravo.beans.JNumberEvent evt) {
                jNumberKeysKeyPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 230, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel1Layout.createSequentialGroup()
                    .addGap(0, 10, Short.MAX_VALUE)
                    .addComponent(jNumberKeys, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 10, Short.MAX_VALUE)))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 260, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel1Layout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(jNumberKeys, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );

        jPanel8.add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(760, 0, 230, 260));

        jScrollPane2.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N

        jTableProductStock.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jTableProductStock.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "Location", "Current", "Maximum", "Minimum", "PriceSell", "PriceValue"
            }
        ));
        jTableProductStock.setRowHeight(25);
        jScrollPane2.setViewportView(jTableProductStock);

        jPanel8.add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(5, 220, 650, 70));

        m_jBtnShowStock.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        m_jBtnShowStock.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/pay.png"))); // NOI18N
        m_jBtnShowStock.setToolTipText(AppLocal.getIntString("tooltip.salecheckstock")); // NOI18N
        m_jBtnShowStock.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jBtnShowStockActionPerformed(evt);
            }
        });
        jPanel8.add(m_jBtnShowStock, new org.netbeans.lib.awtextra.AbsoluteConstraints(660, 250, 40, 40));

        lblTotalQtyValue.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        lblTotalQtyValue.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblTotalQtyValue.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        lblTotalQtyValue.setPreferredSize(new java.awt.Dimension(100, 30));
        jPanel8.add(lblTotalQtyValue, new org.netbeans.lib.awtextra.AbsoluteConstraints(118, 290, -1, -1));

        lbTotalValue.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        lbTotalValue.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lbTotalValue.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        lbTotalValue.setPreferredSize(new java.awt.Dimension(100, 30));
        jPanel8.add(lbTotalValue, new org.netbeans.lib.awtextra.AbsoluteConstraints(547, 290, -1, -1));

        webLblQty.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        webLblQty.setText(AppLocal.getIntString("label.stock.quantity")); // NOI18N
        webLblQty.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        webLblQty.setPreferredSize(new java.awt.Dimension(90, 30));
        jPanel8.add(webLblQty, new org.netbeans.lib.awtextra.AbsoluteConstraints(5, 290, 100, -1));

        webLblValue.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        webLblValue.setText(AppLocal.getIntString("label.stock.value")); // NOI18N
        webLblValue.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        webLblValue.setPreferredSize(new java.awt.Dimension(180, 30));
        jPanel8.add(webLblValue, new org.netbeans.lib.awtextra.AbsoluteConstraints(355, 290, 170, -1));

        add(jPanel8, java.awt.BorderLayout.PAGE_START);

        catcontainer.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        catcontainer.setMinimumSize(new java.awt.Dimension(0, 250));
        catcontainer.setPreferredSize(new java.awt.Dimension(0, 250));
        catcontainer.setRequestFocusEnabled(false);
        catcontainer.setLayout(new java.awt.BorderLayout());
        add(catcontainer, java.awt.BorderLayout.CENTER);
        catcontainer.getAccessibleContext().setAccessibleParent(jPanel8);
    }// </editor-fold>//GEN-END:initComponents

    private void jTextField1KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField1KeyTyped
        jTextField1.setText(null);
        stateTransition(evt.getKeyChar());
    }//GEN-LAST:event_jTextField1KeyTyped

    private void jNumberKeysKeyPerformed(com.openbravo.beans.JNumberEvent evt) {//GEN-FIRST:event_jNumberKeysKeyPerformed

        stateTransition(evt.getKey());

    }//GEN-LAST:event_jNumberKeysKeyPerformed


    private void m_jreasonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jreasonActionPerformed

        m_jLocationDes.setEnabled(m_ReasonModel.getSelectedItem() == MovementReason.OUT_CROSSING);

    }//GEN-LAST:event_m_jreasonActionPerformed

    private void m_jbtndateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jbtndateActionPerformed

        Date date;
        try {
            date = (Date) Formats.TIMESTAMP.parseValue(m_jdate.getText());
        }
        catch (BasicException e) {
            date = null;
        }
        date = JCalendarDialog.showCalendarTime(this, date);
        if (date != null) {
            m_jdate.setText(Formats.TIMESTAMP.formatValue(date));
        }
    }//GEN-LAST:event_m_jbtndateActionPerformed

    private void m_jEnterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jEnterActionPerformed

        incProductByCode(m_jcodebar.getText());
        m_jcodebar.setText(null);
        if (m_jSupplier.getSelectedItem() != null) {
//            saveData();
        } else {
            JOptionPane.showMessageDialog(this,
                    AppLocal.getIntString("message.supplierinvalid"),
                    AppLocal.getIntString("message.title.supplierinvalid"),
                    JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_m_jEnterActionPerformed

    private void m_jSupplierActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jSupplierActionPerformed
        /*        if (m_jSupplier != null) {
            catcontainer.setEnabled(false);            
            jNumberKeys.setEnabled(true);
            m_jcodebar.setEnabled(true);
            m_jEnter.setEnabled(true);           
            m_jSupplierDoc.setEnabled(true); 
            m_jcodebar.setEnabled(true);
            m_jEditLine.setEnabled(true);
            m_jEditAttributes.setEnabled(true);
            m_jBtnShowStock.setEnabled(true);
        } else {
            catcontainer.setEnabled(true);            
            jNumberKeys.setEnabled(false);
            m_jcodebar.setEnabled(false);
            m_jEnter.setEnabled(false);
            m_jSupplierDoc.setEnabled(false);             
            m_jcodebar.setEnabled(false);            
            m_jEditLine.setEnabled(false);
            m_jEditAttributes.setEnabled(false);
            m_jBtnShowStock.setEnabled(false);
            
       }
         */
    }//GEN-LAST:event_m_jSupplierActionPerformed

    private void m_jBtnShowStockActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jBtnShowStockActionPerformed

        showStockTable();

    }//GEN-LAST:event_m_jBtnShowStockActionPerformed

    private void m_jDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jDeleteActionPerformed

        int i = m_invlines.getSelectedRow();

        if (i < 0) {
            Toolkit.getDefaultToolkit().beep();
        } else {
            removeInvLine(i);

        }
    }//GEN-LAST:event_m_jDeleteActionPerformed

    private void m_jListActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jListActionPerformed

        ProductInfoExt prod = JProductFinder.showMessage(StockManagement.this, m_dlSales);
        if (prod != null) {
            buttonTransition(prod);
        }

    }//GEN-LAST:event_m_jListActionPerformed

    private void m_jEditLineActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jEditLineActionPerformed

        int i = m_invlines.getSelectedRow();
        if (i < 0) {
            Toolkit.getDefaultToolkit().beep();
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
    }//GEN-LAST:event_m_jEditLineActionPerformed

    private void m_jEditAttributesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jEditAttributesActionPerformed

        int i = m_invlines.getSelectedRow();
        if (i < 0) {
            Toolkit.getDefaultToolkit().beep();
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
            }
            catch (BasicException ex) {
                MessageInf msg = new MessageInf(MessageInf.SGN_WARNING,
                        AppLocal.getIntString("message.cannotfindattributes"), ex);
                msg.show(this);
            }
        }
    }//GEN-LAST:event_m_jEditAttributesActionPerformed

    private void m_jBtnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jBtnDeleteActionPerformed

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

    }//GEN-LAST:event_m_jBtnDeleteActionPerformed

    private void m_jcodebarMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_m_jcodebarMouseClicked
        m_jcodebar.requestFocusInWindow();
        jTextField1.requestFocus();
        m_jcodebar.setEnabled(true);
        m_jcodebar.setText(null);
    }//GEN-LAST:event_m_jcodebarMouseClicked

    private void m_jLocationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jLocationActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_m_jLocationActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel catcontainer;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private com.openbravo.beans.JNumberKeys jNumberKeys;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JScrollPane jScrollPane2;
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
    // End of variables declaration//GEN-END:variables

}
