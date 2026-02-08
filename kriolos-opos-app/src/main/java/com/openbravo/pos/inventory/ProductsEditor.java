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

import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.ComboBoxValModel;
import com.openbravo.data.user.DirtyManager;
import com.openbravo.data.user.EditorRecord;
import com.openbravo.format.Formats;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.DataLogicSales;
import com.openbravo.pos.sales.TaxesLogic;
import com.openbravo.pos.suppliers.DataLogicSuppliers;
import com.openbravo.pos.suppliers.JDialogNewSupplier;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import com.openbravo.beans.JCalendarDialog;
import com.openbravo.pos.domain.utils.AmountCalculatorUtil;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import com.openbravo.pos.forms.DataLogicSystem;
import java.awt.Color;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.netbeans.validation.api.builtin.stringvalidation.StringValidators;

/**
 *
 * @author Jack Gerrard
 */
public final class ProductsEditor extends com.openbravo.pos.panels.ValidationPanel implements EditorRecord {

    private static final Logger LOGGER = Logger.getLogger(ProductsEditor.class.getName());
    private static final long serialVersionUID = 1L;

    private ComboBoxValModel m_CategoryModel;
    private ComboBoxValModel taxcatmodel;
    private ComboBoxValModel attmodel;
    private ComboBoxValModel m_SuppliersModel;
    private ComboBoxValModel m_UomModel;

    private String productId;
    private Double prodPricesell;
    private boolean prodPricesellLock = false;
    private boolean prodFieldEditLock = false;
    private int customOptionMode;
    private Color bckgColor = null;

    private AppView appView; 

    private TaxesLogic taxeslogic;
    private DirtyManager m_Dirty;
    private DataLogicSales dlSales;
    private DataLogicSuppliers dlSuppliers;
    private DataLogicSystem dlSystem; 


    /**
     * Creates new form JEditProduct
     *
     * @param app
     * @param dirty
     */
    public ProductsEditor(AppView app, DirtyManager dirty) {

        setAppView(app);
        dlSales = (DataLogicSales) app.getBean("com.openbravo.pos.forms.DataLogicSales");
        dlSystem = (DataLogicSystem) app.getBean("com.openbravo.pos.forms.DataLogicSystem");
        dlSuppliers = (DataLogicSuppliers) app.getBean("com.openbravo.pos.suppliers.DataLogicSuppliers");

        initComponents();
        
        m_CategoryModel = new ComboBoxValModel();
        taxcatmodel = new ComboBoxValModel();
        attmodel = new ComboBoxValModel();
        m_SuppliersModel = new ComboBoxValModel();
        m_UomModel = new ComboBoxValModel();

// Tab General        
        m_jRef.getDocument().addDocumentListener(dirty);
        m_jCode.getDocument().addDocumentListener(dirty);
        m_jCodetype.addActionListener(dirty);
        m_jName.getDocument().addDocumentListener(dirty);
        m_jCategory.addActionListener(dirty);
        m_jAtt.addActionListener(dirty);
        m_jVerpatrib.addActionListener(dirty);
        m_jTax.addActionListener(dirty);
        m_jUom.addActionListener(dirty);
        m_jPriceBuy.getDocument().addDocumentListener(dirty);
        m_jPriceSell.getDocument().addDocumentListener(dirty);
        m_jPrintTo.addActionListener(dirty);

// Tab Stock        
        m_jInCatalog.addActionListener(dirty);
        m_jConstant.addActionListener(dirty);
        m_jCatalogOrder.getDocument().addDocumentListener(dirty);
        m_jSupplier.addActionListener(dirty);

        m_jService.addActionListener(dirty);
        m_jCheckWarrantyReceipt.addActionListener(dirty);
        m_jCOM.addActionListener(dirty);
        m_jScale.addActionListener(dirty);
        m_jVprice.addActionListener(dirty);
        m_jstockcost.getDocument().addDocumentListener(dirty);
        m_jstockvolume.getDocument().addDocumentListener(dirty);
        m_jPrintKB.addActionListener(dirty);
        m_jSendStatus.addActionListener(dirty);

// Tab Image
        m_jImage.addPropertyChangeListener("image", dirty);

// Tab Button
        m_jDisplay.getDocument().addDocumentListener(dirty);
        m_jTextTip.getDocument().addDocumentListener(dirty);
        colourChooser.addActionListener(dirty);
        m_jDisplay.addCaretListener(null);

// Tab Properties
        txtAttributes.getDocument().addDocumentListener(dirty);

        FieldsManager fm = new FieldsManager();
        m_jPriceBuy.getDocument().addDocumentListener(fm);
        m_jPriceSell.getDocument().addDocumentListener(new PriceSellManager());
        m_jTax.addActionListener(fm);
        m_jPriceSellTax.getDocument().addDocumentListener(new PriceTaxManager());
        m_jmargin.getDocument().addDocumentListener(new MarginManager());
        m_jGrossProfit.getDocument().addDocumentListener(new MarginManager());

        m_jdate.getDocument().addDocumentListener(dirty);

        init();
        initValidator();
    }

    private void initValidator() {
        org.netbeans.validation.api.ui.ValidationGroup valGroup = getValidationGroup();
        valGroup.add(m_jRef, StringValidators.REQUIRE_NON_EMPTY_STRING);
        valGroup.add(m_jName, StringValidators.REQUIRE_NON_EMPTY_STRING);
        valGroup.add(m_jCode, StringValidators.REQUIRE_NON_EMPTY_STRING);
        valGroup.add(m_jPriceSellTax, StringValidators.REQUIRE_NON_EMPTY_STRING);
    }

    private void init() {
        writeValueEOF();
    }

    /**
     * Instantiate object
     *
     * @throws BasicException
     */
    @SuppressWarnings("unchecked")
    public void activate() throws BasicException {

        taxeslogic = new TaxesLogic(dlSales.getTaxListAll());

        m_CategoryModel = new ComboBoxValModel(dlSales.getCategoriesListAll());
        m_jCategory.setModel(m_CategoryModel);
  
        taxcatmodel = new ComboBoxValModel(dlSales.getTaxCategoriesListAll());
        m_jTax.setModel(taxcatmodel);

        attmodel = new ComboBoxValModel( dlSales.getAttributeSetListAll());
        attmodel.add(0, null);
        m_jAtt.setModel(attmodel);

        m_SuppliersModel = new ComboBoxValModel(dlSuppliers.getSupplierListAll());
        m_jSupplier.setModel(m_SuppliersModel);

        m_UomModel = new ComboBoxValModel(dlSales.getUomListAll());
        m_jUom.setModel(m_UomModel);

        String pId = null;
    }

    /**
     *
     */
    @Override
    public void refresh() {
    }

    /**
     *
     */
    @Override
    public void writeValueEOF() {

        prodFieldEditLock = true;

        m_jTitle.setText(AppLocal.getIntString("label.recordeof"));

// Tab General        
        productId = null;
        m_jRef.setText(null);
        m_jCode.setText(null);
        m_jCodetype.setSelectedIndex(0);
        m_jName.setText(null);
        m_CategoryModel.setSelectedKey(null);
        taxcatmodel.setSelectedKey(null);
        attmodel.setSelectedKey(null);
        m_jVerpatrib.setSelected(false);
        m_UomModel.setSelectedKey(0);
        m_jPriceBuy.setText("0");
        setPriceSell(null);
        m_SuppliersModel.setSelectedKey(0);

// Tab Stock        
        m_jInCatalog.setSelected(false);
        m_jConstant.setSelected(false);
        m_jCatalogOrder.setText(null);
        m_jPrintTo.setSelectedIndex(1);
        m_jService.setSelected(false);
        m_jCheckWarrantyReceipt.setSelected(false);
        m_jCOM.setSelected(false);
        m_jScale.setSelected(false);
        m_jVprice.setSelected(false);
        m_jstockcost.setText("0");
        m_jstockvolume.setText("0");
        m_jPrintKB.setVisible(false);
        m_jSendStatus.setVisible(false);
        m_jStockUnits.setVisible(false);
        m_jdate.setText(null);

// Tab Image
        m_jImage.setImage(null);

// Tab Button
        m_jDisplay.setText(null);
        m_jTextTip.setText(null);
//        colourChooser.setText("#000000");

// Tab Properties
        txtAttributes.setText(null);

        prodFieldEditLock = false;

// Tab General
        m_jRef.setEnabled(false);
        m_jCode.setEnabled(false);
        m_jCodetype.setEnabled(false);
        m_jName.setEnabled(false);
        m_jCategory.setEnabled(false);
        m_jAtt.setEnabled(false);
        m_jVerpatrib.setEnabled(false);
        m_jTax.setEnabled(false);
        m_jUom.setEnabled(false);
        m_jPriceBuy.setEnabled(false);
        m_jPriceSell.setEnabled(false);
        m_jPriceSellTax.setEnabled(false);
        m_jmargin.setEnabled(false);
        m_jSupplier.setEnabled(false);

// Tab Stock        
        m_jInCatalog.setEnabled(false);
        m_jConstant.setEnabled(false);
        m_jCatalogOrder.setEnabled(false);
        m_jPrintTo.setEnabled(false);
        m_jService.setEnabled(false);
        m_jCheckWarrantyReceipt.setEnabled(false);
        m_jCOM.setEnabled(false);
        m_jScale.setEnabled(false);
        m_jVprice.setEnabled(false);
        m_jstockcost.setEnabled(false);
        m_jstockvolume.setEnabled(false);
        jTableProductStock.setEnabled(false);
        m_jdate.setEnabled(false);

// Tab Image
        m_jImage.setEnabled(false);

// Tab Button
        m_jDisplay.setEnabled(false);
        m_jTextTip.setEnabled(false);
        colourChooser.setEnabled(false);

// Tab Properties
        txtAttributes.setEnabled(false);

        calculateMargin();
        calculatePriceSellTax();
        calculateGrossProfitPercentage();
    }

    @Override
    public void writeValueInsert() {

        prodFieldEditLock = true;

        m_jTitle.setText(AppLocal.getIntString("label.recordnew"));

// Tab General        
        productId = UUID.randomUUID().toString();
        m_jRef.setText(null);
        m_jCode.setText(null);
        m_jCodetype.setSelectedIndex(0);
        m_jName.setText(null);
        m_CategoryModel.setSelectedKey("000");
        attmodel.setSelectedKey(null);
        m_jVerpatrib.setSelected(false);
        taxcatmodel.setSelectedKey("001");
        m_UomModel.setSelectedKey("0");
        m_jPriceBuy.setText("0");
        setPriceSell(null);

// Tab Stock        
        m_jInCatalog.setSelected(true);
        m_jConstant.setSelected(false);
        m_jCatalogOrder.setText(null);
        m_jPrintTo.setSelectedIndex(1);
        m_jService.setSelected(false);
        m_jCheckWarrantyReceipt.setSelected(false);
        m_jCOM.setSelected(false);
        m_jScale.setSelected(false);
        m_jVprice.setSelected(false);
        m_jstockcost.setText("0");
        m_jstockvolume.setText("0");
        m_jdate.setText(null);

// Tab Image
        m_jImage.setImage(null);

// Tab Button
        m_jDisplay.setText(null);
        m_jTextTip.setText(null);
        colourChooser.setEnabled(false);

// Tab Properties
        txtAttributes.setText(null);

        prodFieldEditLock = false;

// Tab General
        m_jRef.setEnabled(true);
        m_jCode.setEnabled(true);
        m_jCodetype.setEnabled(true);
        m_jName.setEnabled(true);
        m_jCategory.setEnabled(true);
        m_jTax.setEnabled(true);
        m_jAtt.setEnabled(true);
        m_jVerpatrib.setEnabled(true);
        m_jUom.setEnabled(true);
        m_jPriceBuy.setEnabled(true);
        m_jPriceSell.setEnabled(true);
        m_jPriceSellTax.setEnabled(true);
        m_jmargin.setEnabled(true);
        m_jSupplier.setEnabled(true);

// Tab Stock        
        m_jInCatalog.setEnabled(true);
        m_jConstant.setEnabled(true);
        m_jCatalogOrder.setEnabled(false);
        m_jPrintTo.setEnabled(true);
        m_jService.setEnabled(true);
        m_jCheckWarrantyReceipt.setEnabled(true);
        m_jCOM.setEnabled(true);
        m_jScale.setEnabled(true);
        m_jVprice.setEnabled(true);
        m_jstockcost.setEnabled(true);
        m_jstockvolume.setEnabled(true);
        jTableProductStock.setEnabled(false);
        m_jdate.setEnabled(true);

// Tab Image        
        m_jImage.setEnabled(true);

// Tab Button
        m_jDisplay.setEnabled(true);
        m_jTextTip.setEnabled(true);
        colourChooser.setEnabled(true);

// Tab Properties        
        txtAttributes.setEnabled(true);

        calculateMargin();
        calculatePriceSellTax();
        calculateGrossProfitPercentage();
    }

    /**
     *
     * @return myprod
     * @throws BasicException
     */
    @Override
    public Object createValue() throws BasicException {

        Object[] myprod = new Object[32];

        myprod[0] = productId == null ? UUID.randomUUID().toString() : productId;
        myprod[1] = m_jRef.getText();
        myprod[2] = m_jCode.getText();
        myprod[3] = m_jCodetype.getSelectedItem();
        myprod[4] = m_jName.getText();
        myprod[5] = Formats.CURRENCY.parseValue(m_jPriceBuy.getText());
        myprod[6] = prodPricesell;
        myprod[7] = m_CategoryModel.getSelectedKey();
        myprod[8] = taxcatmodel.getSelectedKey();
        myprod[9] = attmodel.getSelectedKey();
        myprod[10] = Formats.CURRENCY.parseValue(m_jstockcost.getText());
        myprod[11] = Formats.DOUBLE.parseValue(m_jstockvolume.getText());
        myprod[12] = m_jImage.getImage();
        myprod[13] = m_jCOM.isSelected();
        myprod[14] = m_jScale.isSelected();
        myprod[15] = m_jConstant.isSelected();
        myprod[16] = m_jPrintKB.isSelected();
        myprod[17] = m_jSendStatus.isSelected();
        myprod[18] = m_jService.isSelected();
        myprod[19] = Formats.BYTEA.parseValue(txtAttributes.getText());
        myprod[20] = m_jDisplay.getText();
        myprod[21] = m_jVprice.isSelected();
        myprod[22] = m_jVerpatrib.isSelected();
        myprod[23] = m_jTextTip.getText();
        myprod[24] = m_jCheckWarrantyReceipt.isSelected();
        myprod[25] = Formats.DOUBLE.parseValue(m_jStockUnits.getText());
        myprod[26] = m_jPrintTo.getSelectedItem().toString();
        myprod[27] = m_SuppliersModel.getSelectedKey();
        myprod[28] = m_UomModel.getSelectedKey();
        myprod[29] = Formats.TIMESTAMP.parseValue(m_jdate.getText());

        myprod[30] = m_jInCatalog.isSelected();
        myprod[31] = Formats.INT.parseValue(m_jCatalogOrder.getText());

        return myprod;
    }

    /**
     *
     * @param value
     */
    @Override
    public void writeValueEdit(Object value) {

        prodFieldEditLock = true;

        setValues(value);

        txtAttributes.setCaretPosition(0);
        prodFieldEditLock = false;

// Tab General
        m_jRef.setEnabled(true);
        m_jCode.setEnabled(true);
        m_jCodetype.setEnabled(true);
        m_jName.setEnabled(true);
        m_jCategory.setEnabled(true);
        m_jTax.setEnabled(true);
        m_jAtt.setEnabled(true);
        m_jVerpatrib.setEnabled(true);
        m_jUom.setEnabled(true);
        m_jPriceBuy.setEnabled(true);
        m_jPriceSell.setEnabled(true);
        m_jPriceSellTax.setEnabled(true);
        m_jmargin.setEnabled(true);
        m_jSupplier.setEnabled(true);

// Tab Stock        
        m_jInCatalog.setEnabled(true);
        m_jConstant.setEnabled(true);
        m_jCatalogOrder.setEnabled(m_jInCatalog.isSelected());
        m_jPrintTo.setEnabled(true);
        m_jService.setEnabled(true);
        m_jCheckWarrantyReceipt.setEnabled(true);
        m_jCOM.setEnabled(true);
        m_jScale.setEnabled(true);
        m_jVprice.setEnabled(true);
        m_jstockcost.setEnabled(true);
        m_jstockvolume.setEnabled(true);
        m_jdate.setEnabled(true);
        jTableProductStock.setEnabled(true);

// Tab Image        
        m_jImage.setEnabled(true);

// Tab Button        
        m_jDisplay.setEnabled(true);
        m_jTextTip.setEnabled(true);
        colourChooser.setEnabled(true);
        setButtonHTML();

        resetTranxTable();
        jTableProductStock.repaint();

// Tab Properties
        txtAttributes.setEnabled(true);

        calculateMargin();
        calculatePriceSellTax();
        calculateGrossProfitPercentage();
    }

    private void setValues(Object value) {
        Object[] myprod = (Object[]) value;

        m_jTitle.setText(Formats.STRING.formatValue((String) myprod[1])
                + " - " + Formats.STRING.formatValue((String) myprod[4]));
        productId = (String) myprod[0];
        m_jRef.setText(Formats.STRING.formatValue((String) myprod[1]));
        m_jCode.setText(Formats.STRING.formatValue((String) myprod[2]));
        m_jCodetype.setSelectedItem(myprod[3]);
        m_jName.setText(Formats.STRING.formatValue((String) myprod[4]));
        m_jPriceBuy.setText(Formats.CURRENCY.formatValue((Double) myprod[5]));
        setPriceSell((Double) myprod[6]);
        m_CategoryModel.setSelectedKey(myprod[7]);
        taxcatmodel.setSelectedKey(myprod[8]);
        attmodel.setSelectedKey(myprod[9]);
        m_jstockcost.setText(Formats.CURRENCY.formatValue((Double) myprod[10]));
        m_jstockvolume.setText(Formats.DOUBLE.formatValue((Double) myprod[11]));
//  JG 3 feb 16 speedup   m_jImage.setImage((BufferedImage) myprod[12]); 
        m_jImage.setImage(findImage(productId));
        m_jCOM.setSelected(((Boolean) myprod[13]));
        m_jScale.setSelected(((Boolean) myprod[14]));
        m_jConstant.setSelected(((Boolean) myprod[15]));
        m_jPrintKB.setSelected(((Boolean) myprod[16]));
        m_jSendStatus.setSelected(((Boolean) myprod[17]));
        m_jService.setSelected(((Boolean) myprod[18]));
        txtAttributes.setText(Formats.BYTEA.formatValue((byte[]) myprod[19]));
        m_jDisplay.setText(Formats.STRING.formatValue((String) myprod[20]));
        m_jVprice.setSelected(((Boolean) myprod[21]));
        m_jVerpatrib.setSelected(((Boolean) myprod[22]));
        m_jTextTip.setText(Formats.STRING.formatValue((String) myprod[23]));
        m_jCheckWarrantyReceipt.setSelected(((Boolean) myprod[24]));
        m_jStockUnits.setText(Formats.DOUBLE.formatValue((Double) myprod[25]));
        m_jPrintTo.setSelectedItem(myprod[26]);
        m_SuppliersModel.setSelectedKey(myprod[27]);
        m_UomModel.setSelectedKey(myprod[28]);
        m_jdate.setText(Formats.DATE.formatValue((Date) myprod[29]));
        m_jInCatalog.setSelected(((Boolean) myprod[30]));
        m_jCatalogOrder.setText(Formats.INT.formatValue((Integer) myprod[31]));
    }

    /**
     *
     * @param value
     */
    @Override
    public void writeValueDelete(Object value) {

        prodFieldEditLock = true;
        setValues(value);

        txtAttributes.setCaretPosition(0);

        prodFieldEditLock = false;

// Tab General
        m_jRef.setEnabled(false);
        m_jCode.setEnabled(false);
        m_jCodetype.setEnabled(false);
        m_jName.setEnabled(false);
        m_jCategory.setEnabled(false);
        m_jTax.setEnabled(false);
        m_jAtt.setEnabled(false);
        m_jVerpatrib.setEnabled(false);
        m_jUom.setEnabled(false);
        m_jPriceBuy.setEnabled(false);
        m_jPriceSell.setEnabled(false);
        m_jPriceSellTax.setEnabled(false);
        m_jmargin.setEnabled(false);
        m_jPrintTo.setEnabled(false);

// Tab Stock
        m_jInCatalog.setEnabled(false);
        m_jConstant.setEnabled(false);
        m_jCatalogOrder.setEnabled(false);
        m_jSupplier.setEnabled(false);
        m_jService.setEnabled(false);
        m_jCheckWarrantyReceipt.setEnabled(false);
        m_jCOM.setEnabled(false);
        m_jScale.setEnabled(false);
        m_jVprice.setEnabled(false);
        m_jstockcost.setEnabled(false);
        m_jstockvolume.setEnabled(false);
        jTableProductStock.setModel(new StockTableModel(new ArrayList<>()));
        jTableProductStock.setEnabled(false);
        m_jdate.setEnabled(false);

// Tab Image
        m_jImage.setEnabled(false);

// Tab Button
        m_jDisplay.setEnabled(false);
        m_jTextTip.setEnabled(false);
        colourChooser.setEnabled(false);

// Tab Properties        
        txtAttributes.setEnabled(false);

        calculateMargin();
        calculatePriceSellTax();
        calculateGrossProfitPercentage();
    }

    public void resetTranxTable() {

        /*
        jTableProductStock.getColumnModel().getColumn(0).setPreferredWidth(100);
        jTableProductStock.getColumnModel().getColumn(1).setPreferredWidth(50);
        jTableProductStock.getColumnModel().getColumn(2).setPreferredWidth(50);
        jTableProductStock.getColumnModel().getColumn(3).setPreferredWidth(50);
         */
        jTableProductStock.repaint();

    }

    /**
     *
     * @return this
     */
    @Override
    public Component getComponent() {
        return this;
    }

    private void setCode() {

        Long lDateTime = new Date().getTime();

        if (!prodFieldEditLock) {
            prodFieldEditLock = true;

            if (m_jRef == null) {
                m_jCode.setText(Long.toString(lDateTime));
            } else {
                if (m_jCode.getText() == null || "".equals(m_jCode.getText())) {
                    m_jCode.setText(m_jRef.getText());
                }
            }
            prodFieldEditLock = false;
        }
    }

    private List<ProductStock> getProductOfName(String pId) {

        List<ProductStock> productStockList = new ArrayList<>();
        try {
            productStockList = dlSales.getProductStockList(pId);
        }
        catch (BasicException ex) {
            LOGGER.log(Level.SEVERE, "ProductStock for PID: " + pId, ex);
        }

        for (ProductStock productStock : productStockList) {
            String productId = productStock.getProductId();
            //remove 
            if (!productId.equals(pId)) {
                productStockList.remove(productStock);
            }
        }

        repaint();
        refresh();

        return productStockList;
    }

    public AppView getAppView() {
        return appView;
    }

    public void setAppView(AppView appView) {
        this.appView = appView;
    }

    class StockTableModel extends AbstractTableModel {

        String loc = AppLocal.getIntString("label.tblProdHeaderCol1");
        String qty = AppLocal.getIntString("label.tblProdHeaderCol2");
        String min = AppLocal.getIntString("label.tblProdHeaderCol3");
        String max = AppLocal.getIntString("label.tblProdHeaderCol4");

        List<ProductStock> stockList;
        String[] columnNames = {loc, qty, min, max};

        public StockTableModel(List<ProductStock> list) {
            stockList = list;
        }

        @Override
        public int getColumnCount() {
            return 4;
        }

        @Override
        public int getRowCount() {
            return stockList.size();
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

    private void selectorBackgroundColor() {
        bckgColor = JColorChooser.showDialog(this, "", Color.WHITE);
    }

    private void setDisplay(int btn) {

        String htmlString = (m_jDisplay.getText());

        switch (btn) {
            case 1:
                m_jDisplay.insert("<br>", m_jDisplay.getCaretPosition());
                break;
            case 2:
                Color colo = JColorChooser.showDialog(this, "", Color.WHITE);
                String hexcolor = color2HexString(colo);
                m_jDisplay.insert("<font color=" + hexcolor + ">", m_jDisplay.getCaretPosition());
                break;
            case 3:
                m_jDisplay.insert("<font size=+2>",
                        m_jDisplay.getCaretPosition());
                break;
            case 4:
                m_jDisplay.insert("<font size=-2>",
                        m_jDisplay.getCaretPosition());
                break;
            case 5:
                m_jDisplay.insert("<b>",
                        m_jDisplay.getCaretPosition());
                break;
            case 6:
                m_jDisplay.insert("<i>",
                        m_jDisplay.getCaretPosition());
                break;
            case 7:
// defaults to file:/ for local disk
// http:// also usable for remote image                
                JFileChooser fc = new JFileChooser();
                FileFilter imageFilter = new FileNameExtensionFilter(
                        "Image files", ImageIO.getReaderFileSuffixes());
                fc.setFileFilter(imageFilter);
                int returnValue = fc.showOpenDialog(null);
                File selectedFile = fc.getSelectedFile();
                if (selectedFile != null) {
                    m_jDisplay.insert("<img src=file:" + selectedFile.getAbsolutePath() + ">",
                            m_jDisplay.getCaretPosition());
                }
                break;

            case 8:
                htmlString = "<html>" + m_jName.getText();
                m_jDisplay.setText(htmlString);
                break;
            case 9:
                m_jDisplay.insert("<div style=background-color:black;color:white;padding:10px;>",
                        m_jDisplay.getCaretPosition());
                break;
            default:
                htmlString += "";
                m_jDisplay.setText(htmlString);
        }
    }

    private void setButtonHTML() {

        jButtonHTML.setText(m_jDisplay.getText());
        if (bckgColor != null) {
            jButtonHTML.setBackground(bckgColor);
        }
    }

    public String color2HexString(Color color) {
        return "#" + Integer.toHexString(color.getRGB() & 0x00ffffff);
    }


    private BufferedImage findImage(String id) {
        return dlSales.getProductImage(id);
    }


    private void calculateMargin() {

        if (!prodFieldEditLock) {
            prodFieldEditLock = true;

            Double dPriceBuy = readCurrency(m_jPriceBuy.getText());
            Double dPriceSell = prodPricesell;

            if (dPriceBuy == null || dPriceSell == null) {
                m_jmargin.setText(null);
            } else {
                double markup = AmountCalculatorUtil.calcMarkupPercentage(dPriceSell, dPriceBuy);
                m_jmargin.setText(Formats.PERCENT.formatValue(markup));
            }
            prodFieldEditLock = false;
        }
    }

    private void calculatePriceSellTax() {

        if (!prodFieldEditLock) {
            prodFieldEditLock = true;

            if (prodPricesell == null) {
                m_jPriceSellTax.setText(null);
            } else {
                double dTaxRate = taxeslogic.getTaxRate((TaxCategoryInfo) taxcatmodel.getSelectedItem());
                Double dPriceSell = AmountCalculatorUtil.calcPriceWithTaxInclusive(prodPricesell, dTaxRate);
                m_jPriceSellTax.setText(Formats.CURRENCY.formatValue(dPriceSell));
            }
            prodFieldEditLock = false;
        }
    }

    private void calculateGrossProfitPercentage() {

        Double dPriceBuy = readCurrency(m_jPriceBuy.getText());
        Double dPriceSell = readCurrency(m_jPriceSell.getText());

        LOGGER.log(Level.SEVERE, "dPriceBuy: " + dPriceBuy + "; dPriceSell: " + dPriceSell);

        if (!prodFieldEditLock) {
            prodFieldEditLock = true;

            if (dPriceBuy == null || dPriceSell == null) {
                m_jGrossProfit.setText(null);
            } else {
                double profitMargin = AmountCalculatorUtil.calcProfitMarginPercentage(dPriceSell, dPriceBuy);
                m_jGrossProfit.setText(Formats.PERCENT.formatValue(profitMargin));
            }
            prodFieldEditLock = false;
        }
    }

    private void calculatePriceSellfromMargin() {

        if (!prodFieldEditLock) {
            prodFieldEditLock = true;

            Double dPriceBuy = readCurrency(m_jPriceBuy.getText());
            Double dMargin = readPercent(m_jmargin.getText());

            if (dMargin == null || dPriceBuy == null) {
                setPriceSell(null);
            } else {
                double shellPrice = AmountCalculatorUtil.calcSellPriceWithMargin(dPriceBuy, dMargin);
                setPriceSell(shellPrice);
            }

            prodFieldEditLock = false;
        }

    }

    private void calculatePriceSellfromPST() {

        if (!prodFieldEditLock) {
            prodFieldEditLock = true;

            Double dPriceSellTax = readCurrency(m_jPriceSellTax.getText());

            if (dPriceSellTax == null) {
                setPriceSell(null);
            } else {
                double dTaxRate = taxeslogic.getTaxRate((TaxCategoryInfo) taxcatmodel.getSelectedItem());
                double netShellPrice = AmountCalculatorUtil.calcPriceWithoutTax(dPriceSellTax, dTaxRate);
                setPriceSell(netShellPrice);
            }

            prodFieldEditLock = false;
        }
    }

    private void setPriceSell(Double value) {

        if (!prodPricesellLock) {
            prodPricesellLock = true;
            prodPricesell = value;
            m_jPriceSell.setText(Formats.CURRENCY.formatValue(prodPricesell));
            prodPricesellLock = false;
        }
    }

    private void setPriceSellFromUI() {
        if (!prodPricesellLock) {
            prodPricesellLock = true;
            prodPricesell = readCurrency(m_jPriceSell.getText());
            prodPricesellLock = false;
        }
    }

    private class PriceSellManager implements DocumentListener {

        @Override
        public void changedUpdate(DocumentEvent e) {
            setPriceSellFromUI();
            calculateMargin();
            calculatePriceSellTax();
            calculateGrossProfitPercentage();
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            setPriceSellFromUI();
            calculateMargin();
            calculatePriceSellTax();
            calculateGrossProfitPercentage();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            setPriceSellFromUI();
            calculateMargin();
            calculatePriceSellTax();
            calculateGrossProfitPercentage();
        }
    }

    private class FieldsManager implements DocumentListener, ActionListener {

        @Override
        public void changedUpdate(DocumentEvent e) {
            calculateMargin();
            calculatePriceSellTax();
            calculateGrossProfitPercentage();

        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            calculateMargin();
            calculatePriceSellTax();
            calculateGrossProfitPercentage();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            calculateMargin();
            calculatePriceSellTax();
            calculateGrossProfitPercentage();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            calculateMargin();
            calculatePriceSellTax();
            calculateGrossProfitPercentage();
        }
    }

    private class PriceTaxManager implements DocumentListener {

        @Override
        public void changedUpdate(DocumentEvent e) {
            calculatePriceSellfromPST();
            calculateMargin();
            calculateGrossProfitPercentage();
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            calculatePriceSellfromPST();
            calculateMargin();
            calculateGrossProfitPercentage();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            calculatePriceSellfromPST();
            calculateMargin();
            calculateGrossProfitPercentage();
        }
    }

    private class MarginManager implements DocumentListener {

        @Override
        public void changedUpdate(DocumentEvent e) {
            calculatePriceSellfromMargin();
            calculatePriceSellTax();
            calculateGrossProfitPercentage();
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            calculatePriceSellfromMargin();
            calculatePriceSellTax();
            calculateGrossProfitPercentage();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            calculatePriceSellfromMargin();
            calculatePriceSellTax();
            calculateGrossProfitPercentage();
        }
    }

    private static Double readCurrency(String sValue) {
        try {
            return Formats.CURRENCY.parseValue(sValue);
        }
        catch (BasicException e) {
            return null;
        }
    }

    private static Double readPercent(String sValue) {
        try {
            return Formats.PERCENT.parseValue(sValue);
        }
        catch (BasicException e) {
            return null;
        }
    }

    private String getEmptyProperty() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>  \n"
                + "<!DOCTYPE properties SYSTEM \"http://java.sun.com/dtd/properties.dtd\">\n"
                + "<properties>\n"
                + "    <entry key=\"identifier\">value</entry>\n"
                + "</properties>";
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        m_jRef = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        m_jCode = new javax.swing.JTextField();
        m_jCodetype = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        m_jName = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        m_jCategory = new javax.swing.JComboBox();
        jLabel13 = new javax.swing.JLabel();
        m_jAtt = new javax.swing.JComboBox();
        m_jVerpatrib = new javax.swing.JCheckBox();
        jLabel17 = new javax.swing.JLabel();
        m_jSupplier = new javax.swing.JComboBox();
        jBtnSupplier = new javax.swing.JButton();
        m_jTitle = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        m_jInCatalog = new javax.swing.JCheckBox();
        m_jCatalogOrder = new javax.swing.JTextField();
        jLabel18 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        m_jConstant = new javax.swing.JCheckBox();
        webLabel1 = new javax.swing.JLabel();
        m_jPrintTo = new javax.swing.JComboBox();
        m_jSendStatus = new javax.swing.JCheckBox();
        m_jPrintKB = new javax.swing.JCheckBox();
        pricePanel = new javax.swing.JPanel();
        m_jmargin = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        m_jPriceBuy = new javax.swing.JTextField();
        m_jGrossProfit = new javax.swing.JTextField();
        jLabel22 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        m_jUom = new javax.swing.JComboBox();
        jLabel7 = new javax.swing.JLabel();
        m_jTax = new javax.swing.JComboBox();
        jLabel16 = new javax.swing.JLabel();
        m_jPriceSellTax = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        m_jPriceSell = new javax.swing.JTextField();
        jLabel19 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        m_jstockcost = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        m_jstockvolume = new javax.swing.JTextField();
        jLabel15 = new javax.swing.JLabel();
        m_jService = new javax.swing.JCheckBox();
        jLabel11 = new javax.swing.JLabel();
        m_jCOM = new javax.swing.JCheckBox();
        jLabel12 = new javax.swing.JLabel();
        m_jScale = new javax.swing.JCheckBox();
        jLabel20 = new javax.swing.JLabel();
        m_jVprice = new javax.swing.JCheckBox();
        jLabel33 = new javax.swing.JLabel();
        m_jCheckWarrantyReceipt = new javax.swing.JCheckBox();
        jLabel23 = new javax.swing.JLabel();
        jBtnShowTrans = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTableProductStock = new javax.swing.JTable();
        m_jStockUnits = new javax.swing.JTextField();
        jLblDate = new javax.swing.JLabel();
        m_jbtndate = new javax.swing.JButton();
        m_jdate = new javax.swing.JTextField();
        jLabel24 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtAttributes = new javax.swing.JTextArea();
        jBtnXml = new javax.swing.JButton();
        jPanel6 = new javax.swing.JPanel();
        jLabel34 = new javax.swing.JLabel();
        m_jImage = new com.openbravo.data.gui.JImageEditor();
        jPanel4 = new javax.swing.JPanel();
        jLabel28 = new javax.swing.JLabel();
        jButtonHTML = new javax.swing.JButton();
        jLabel21 = new javax.swing.JLabel();
        m_jTextTip = new javax.swing.JTextField();
        jScrollPane3 = new javax.swing.JScrollPane();
        m_jDisplay = new javax.swing.JTextArea();
        jBtnBreak = new javax.swing.JButton();
        jBtnColour = new javax.swing.JButton();
        jBtnLarge = new javax.swing.JButton();
        jBtnSmall = new javax.swing.JButton();
        jBtnBold = new javax.swing.JButton();
        jBtnItalic = new javax.swing.JButton();
        jBtnImage = new javax.swing.JButton();
        jBtnReset = new javax.swing.JButton();
        jBtnStyle = new javax.swing.JButton();
        colourChooser = new javax.swing.JButton();

        setPreferredSize(new java.awt.Dimension(1000, 600));
        setLayout(null);

        jTabbedPane1.setToolTipText("");
        jTabbedPane1.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jTabbedPane1.setPreferredSize(new java.awt.Dimension(680, 420));

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("pos_messages"); // NOI18N
        jPanel1.setToolTipText(bundle.getString("tooltip.product.general.tab")); // NOI18N

        jLabel1.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel1.setText(AppLocal.getIntString("label.prodrefm")); // NOI18N
        jLabel1.setPreferredSize(new java.awt.Dimension(110, 30));
        jLabel1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel1MouseClicked(evt);
            }
        });

        m_jRef.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jRef.setToolTipText("");
        m_jRef.setPreferredSize(new java.awt.Dimension(150, 30));
        m_jRef.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                m_jRefFocusLost(evt);
            }
        });

        jLabel6.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel6.setText(AppLocal.getIntString("label.prodbarcodem")); // NOI18N
        jLabel6.setPreferredSize(new java.awt.Dimension(100, 30));

        m_jCode.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jCode.setPreferredSize(new java.awt.Dimension(125, 30));
        m_jCode.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                m_jCodeFocusLost(evt);
            }
        });

        m_jCodetype.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jCodetype.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "EAN-13", "EAN-8", "CODE128", "Upc-A", "Upc-E" }));
        m_jCodetype.setPreferredSize(new java.awt.Dimension(80, 30));

        jLabel2.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel2.setText(AppLocal.getIntString("label.prodnamem")); // NOI18N
        jLabel2.setPreferredSize(new java.awt.Dimension(110, 30));

        m_jName.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jName.setPreferredSize(new java.awt.Dimension(450, 30));
        m_jName.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                m_jNameFocusLost(evt);
            }
        });

        jLabel5.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel5.setText(AppLocal.getIntString("label.prodcategorym")); // NOI18N
        jLabel5.setPreferredSize(new java.awt.Dimension(110, 30));

        m_jCategory.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jCategory.setPreferredSize(new java.awt.Dimension(450, 30));

        jLabel13.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel13.setText(AppLocal.getIntString("label.attributes")); // NOI18N
        jLabel13.setPreferredSize(new java.awt.Dimension(110, 30));

        m_jAtt.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jAtt.setPreferredSize(new java.awt.Dimension(200, 30));

        m_jVerpatrib.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jVerpatrib.setText(bundle.getString("label.mandatory")); // NOI18N
        m_jVerpatrib.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        m_jVerpatrib.setPreferredSize(new java.awt.Dimension(49, 30));
        m_jVerpatrib.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                none(evt);
            }
        });

        jLabel17.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel17.setText(AppLocal.getIntString("label.prodsupplier")); // NOI18N
        jLabel17.setPreferredSize(new java.awt.Dimension(110, 30));

        m_jSupplier.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jSupplier.setPreferredSize(new java.awt.Dimension(200, 30));

        jBtnSupplier.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jBtnSupplier.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/customer_add_sml.png"))); // NOI18N
        jBtnSupplier.setText(bundle.getString("label.supplier")); // NOI18N
        jBtnSupplier.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnSupplierActionPerformed(evt);
            }
        });

        m_jTitle.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jTitle.setForeground(new java.awt.Color(102, 102, 102));
        m_jTitle.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        m_jTitle.setText("...");
        m_jTitle.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        m_jTitle.setFocusable(false);
        m_jTitle.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        m_jTitle.setOpaque(true);
        m_jTitle.setPreferredSize(new java.awt.Dimension(260, 25));

        jLabel8.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel8.setText(AppLocal.getIntString("label.prodincatalog")); // NOI18N
        jLabel8.setPreferredSize(new java.awt.Dimension(130, 30));

        m_jInCatalog.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        m_jInCatalog.setSelected(true);
        m_jInCatalog.setPreferredSize(new java.awt.Dimension(30, 30));
        m_jInCatalog.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jInCatalogActionPerformed(evt);
            }
        });

        m_jCatalogOrder.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jCatalogOrder.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        m_jCatalogOrder.setPreferredSize(new java.awt.Dimension(50, 30));

        jLabel18.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel18.setText(AppLocal.getIntString("label.prodorder")); // NOI18N
        jLabel18.setPreferredSize(new java.awt.Dimension(130, 30));

        jLabel14.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel14.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel14.setText(bundle.getString("label.prodconstant")); // NOI18N
        jLabel14.setPreferredSize(new java.awt.Dimension(130, 30));

        m_jConstant.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        m_jConstant.setPreferredSize(new java.awt.Dimension(30, 30));

        webLabel1.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        webLabel1.setText(bundle.getString("label.printto")); // NOI18N
        webLabel1.setToolTipText(bundle.getString("tooltip.printto")); // NOI18N
        webLabel1.setPreferredSize(new java.awt.Dimension(130, 30));

        m_jPrintTo.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jPrintTo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "0", "1", "2", "3", "4", "5", "6" }));
        m_jPrintTo.setPreferredSize(new java.awt.Dimension(50, 30));

        m_jSendStatus.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N

        m_jPrintKB.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jLabel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 167, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addComponent(m_jAtt, javax.swing.GroupLayout.PREFERRED_SIZE, 245, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(m_jVerpatrib, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(73, 73, 73))
                            .addComponent(m_jName, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 444, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addComponent(m_jCategory, javax.swing.GroupLayout.PREFERRED_SIZE, 245, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(199, 199, 199))))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel8, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 167, Short.MAX_VALUE)
                                    .addComponent(jLabel17, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(6, 6, 6)))
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(m_jSupplier, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jBtnSupplier)
                                .addGap(89, 89, 89))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(m_jRef, javax.swing.GroupLayout.PREFERRED_SIZE, 248, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(m_jCode, javax.swing.GroupLayout.PREFERRED_SIZE, 249, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(m_jCodetype, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(m_jInCatalog, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(42, 42, 42)
                                .addComponent(jLabel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(m_jConstant, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(162, 162, 162))))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(webLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 167, Short.MAX_VALUE)
                            .addComponent(jLabel18, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(m_jCatalogOrder, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(m_jTitle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(4, 4, 4))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(m_jPrintTo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(37, 37, 37)
                                .addComponent(m_jPrintKB, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(m_jSendStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE)))))
                .addGap(56, 56, 56))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jRef, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(11, 11, 11)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jCode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jCodetype, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(m_jCategory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jAtt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jVerpatrib, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel17, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jSupplier, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jBtnSupplier))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jInCatalog, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jConstant, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(m_jCatalogOrder, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jTitle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(webLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(m_jPrintTo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(m_jPrintKB, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(m_jSendStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        jTabbedPane1.addTab(AppLocal.getIntString("label.prodgeneral"), jPanel1); // NOI18N

        m_jmargin.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jmargin.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        m_jmargin.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        m_jmargin.setEnabled(false);
        m_jmargin.setPreferredSize(new java.awt.Dimension(110, 30));

        jLabel3.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel3.setText(AppLocal.getIntString("label.prodpricebuym")); // NOI18N
        jLabel3.setToolTipText(AppLocal.getIntString("label.prodpricebuym")); // NOI18N
        jLabel3.setPreferredSize(new java.awt.Dimension(110, 30));

        m_jPriceBuy.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jPriceBuy.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        m_jPriceBuy.setText("0");
        m_jPriceBuy.setPreferredSize(new java.awt.Dimension(200, 30));

        m_jGrossProfit.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jGrossProfit.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        m_jGrossProfit.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        m_jGrossProfit.setEnabled(false);
        m_jGrossProfit.setPreferredSize(new java.awt.Dimension(110, 30));
        m_jGrossProfit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jGrossProfitActionPerformed(evt);
            }
        });

        jLabel22.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel22.setText(bundle.getString("label.grossprofit")); // NOI18N
        jLabel22.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel22.setPreferredSize(new java.awt.Dimension(110, 30));

        jLabel26.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel26.setText(AppLocal.getIntString("label.UOM")); // NOI18N
        jLabel26.setToolTipText(AppLocal.getIntString("label.UOM")); // NOI18N
        jLabel26.setPreferredSize(new java.awt.Dimension(70, 30));

        m_jUom.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jUom.setPreferredSize(new java.awt.Dimension(150, 30));

        jLabel7.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel7.setText(AppLocal.getIntString("label.taxcategorym")); // NOI18N
        jLabel7.setToolTipText(AppLocal.getIntString("label.taxcategorym")); // NOI18N
        jLabel7.setPreferredSize(new java.awt.Dimension(110, 30));

        m_jTax.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jTax.setPreferredSize(new java.awt.Dimension(200, 30));

        jLabel16.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel16.setText(AppLocal.getIntString("label.prodpriceselltaxm")); // NOI18N
        jLabel16.setToolTipText(AppLocal.getIntString("label.prodpriceselltaxm")); // NOI18N
        jLabel16.setPreferredSize(new java.awt.Dimension(110, 30));

        m_jPriceSellTax.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jPriceSellTax.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        m_jPriceSellTax.setPreferredSize(new java.awt.Dimension(200, 30));

        jLabel4.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel4.setText(AppLocal.getIntString("label.prodpricesell")); // NOI18N
        jLabel4.setToolTipText(AppLocal.getIntString("label.prodpricesell")); // NOI18N
        jLabel4.setPreferredSize(new java.awt.Dimension(110, 30));

        m_jPriceSell.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jPriceSell.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        m_jPriceSell.setPreferredSize(new java.awt.Dimension(200, 30));

        jLabel19.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel19.setText(bundle.getString("label.margin")); // NOI18N
        jLabel19.setPreferredSize(new java.awt.Dimension(110, 30));

        javax.swing.GroupLayout pricePanelLayout = new javax.swing.GroupLayout(pricePanel);
        pricePanel.setLayout(pricePanelLayout);
        pricePanelLayout.setHorizontalGroup(
            pricePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pricePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pricePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pricePanelLayout.createSequentialGroup()
                        .addGroup(pricePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(16, 16, 16)
                        .addGroup(pricePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pricePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(m_jPriceSellTax, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(m_jPriceSell, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(pricePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(m_jUom, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(m_jTax, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                    .addComponent(jLabel26, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(pricePanelLayout.createSequentialGroup()
                        .addGroup(pricePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel22, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel19, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(16, 16, 16)
                        .addGroup(pricePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(m_jPriceBuy, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(m_jGrossProfit, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(m_jmargin, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap(308, Short.MAX_VALUE))
        );
        pricePanelLayout.setVerticalGroup(
            pricePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pricePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pricePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel26, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jUom, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pricePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(m_jTax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pricePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(m_jPriceSellTax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pricePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jPriceSell, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pricePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pricePanelLayout.createSequentialGroup()
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pricePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel22, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(m_jGrossProfit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(m_jPriceBuy, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pricePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel19, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jmargin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(176, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab(AppLocal.getIntString("label.price"), pricePanel); // NOI18N

        jPanel2.setToolTipText(bundle.getString("tooltip.product.stock.tab")); // NOI18N
        jPanel2.setPreferredSize(new java.awt.Dimension(0, 0));

        jLabel9.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel9.setText(AppLocal.getIntString("label.prodstockcost")); // NOI18N
        jLabel9.setPreferredSize(new java.awt.Dimension(110, 30));

        m_jstockcost.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jstockcost.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        m_jstockcost.setPreferredSize(new java.awt.Dimension(85, 30));

        jLabel10.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel10.setText(AppLocal.getIntString("label.prodstockvol")); // NOI18N
        jLabel10.setPreferredSize(new java.awt.Dimension(110, 30));

        m_jstockvolume.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jstockvolume.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        m_jstockvolume.setPreferredSize(new java.awt.Dimension(85, 30));

        jLabel15.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel15.setText(bundle.getString("label.service")); // NOI18N
        jLabel15.setPreferredSize(new java.awt.Dimension(130, 30));

        m_jService.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        m_jService.setToolTipText("A Service Item will not be deducted from the Inventory");
        m_jService.setPreferredSize(new java.awt.Dimension(30, 30));

        jLabel11.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel11.setText(AppLocal.getIntString("label.prodaux")); // NOI18N
        jLabel11.setPreferredSize(new java.awt.Dimension(130, 30));

        m_jCOM.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        m_jCOM.setPreferredSize(new java.awt.Dimension(30, 30));

        jLabel12.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel12.setText(AppLocal.getIntString("label.prodscale")); // NOI18N
        jLabel12.setPreferredSize(new java.awt.Dimension(130, 30));

        m_jScale.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        m_jScale.setPreferredSize(new java.awt.Dimension(30, 30));

        jLabel20.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel20.setText(bundle.getString("label.variableprice")); // NOI18N
        jLabel20.setPreferredSize(new java.awt.Dimension(130, 30));

        m_jVprice.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        m_jVprice.setPreferredSize(new java.awt.Dimension(30, 30));

        jLabel33.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel33.setText(bundle.getString("label.warranty")); // NOI18N
        jLabel33.setToolTipText(bundle.getString("label.warranty")); // NOI18N
        jLabel33.setPreferredSize(new java.awt.Dimension(130, 30));

        m_jCheckWarrantyReceipt.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jCheckWarrantyReceipt.setText(bundle.getString("label.productreceipt")); // NOI18N
        m_jCheckWarrantyReceipt.setPreferredSize(new java.awt.Dimension(30, 30));

        jLabel23.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jLabel23.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel23.setText(bundle.getString("label.prodminmax")); // NOI18N
        jLabel23.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jLabel23.setPreferredSize(new java.awt.Dimension(531, 20));

        jBtnShowTrans.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jBtnShowTrans.setText(bundle.getString("button.ProductStock")); // NOI18N
        jBtnShowTrans.setToolTipText("");
        jBtnShowTrans.setPreferredSize(new java.awt.Dimension(140, 25));
        jBtnShowTrans.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnShowTransActionPerformed(evt);
            }
        });

        jScrollPane2.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N

        jTableProductStock.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jTableProductStock.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Location", "Current", "Minimum", "Maximum"
            }
        ));
        jTableProductStock.setRowHeight(25);
        jScrollPane2.setViewportView(jTableProductStock);

        m_jStockUnits.setEditable(false);
        m_jStockUnits.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        m_jStockUnits.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        m_jStockUnits.setText("0");
        m_jStockUnits.setBorder(null);

        jLblDate.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLblDate.setText(bundle.getString("label.proddate")); // NOI18N
        jLblDate.setPreferredSize(new java.awt.Dimension(130, 30));

        m_jbtndate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/date.png"))); // NOI18N
        m_jbtndate.setToolTipText("Open Calendar");
        m_jbtndate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jbtndateActionPerformed(evt);
            }
        });

        m_jdate.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jdate.setPreferredSize(new java.awt.Dimension(160, 30));

        jLabel24.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel24.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel24.setText(AppLocal.getIntString("label.stock.quantity")); // NOI18N
        jLabel24.setPreferredSize(new java.awt.Dimension(110, 30));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel24, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(m_jStockUnits, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel23, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addGroup(jPanel2Layout.createSequentialGroup()
                                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGroup(jPanel2Layout.createSequentialGroup()
                                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(jLabel33, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(jLabel20, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                .addComponent(m_jService, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(m_jCheckWarrantyReceipt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(m_jCOM, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(m_jScale, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(m_jVprice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                    .addGap(18, 18, 18)
                                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 372, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                                    .addComponent(jLblDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(m_jbtndate, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(0, 0, 0)
                                    .addComponent(m_jdate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jBtnShowTrans, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(m_jstockcost, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(m_jstockvolume, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 66, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(m_jstockcost, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(m_jstockvolume, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(3, 3, 3)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel23, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jStockUnits, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel24, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jBtnShowTrans, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jbtndate, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jdate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLblDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 206, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(m_jService, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel33, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(m_jCheckWarrantyReceipt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(m_jCOM, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(m_jScale, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel20, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(m_jVprice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(11, 11, 11))
        );

        m_jService.getAccessibleContext().setAccessibleDescription("null");

        jTabbedPane1.addTab(AppLocal.getIntString("label.prodstock"), jPanel2); // NOI18N

        jPanel3.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jPanel3.setToolTipText(bundle.getString("tooltip.product.properties.tab")); // NOI18N
        jPanel3.setPreferredSize(new java.awt.Dimension(0, 0));
        jPanel3.setLayout(new java.awt.BorderLayout());

        jScrollPane1.setPreferredSize(new java.awt.Dimension(680, 400));

        txtAttributes.setFont(new java.awt.Font("Monospaced", 0, 14)); // NOI18N
        txtAttributes.setLineWrap(true);
        txtAttributes.setWrapStyleWord(true);
        txtAttributes.setPreferredSize(new java.awt.Dimension(580, 300));
        jScrollPane1.setViewportView(txtAttributes);

        jPanel3.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jBtnXml.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jBtnXml.setText(bundle.getString("button.injectxml")); // NOI18N
        jBtnXml.setToolTipText(bundle.getString("tooltip.xmlheader")); // NOI18N
        jBtnXml.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnXmlActionPerformed(evt);
            }
        });
        jPanel3.add(jBtnXml, java.awt.BorderLayout.PAGE_START);

        jTabbedPane1.addTab(AppLocal.getIntString("label.properties"), jPanel3); // NOI18N

        jPanel6.setToolTipText(bundle.getString("tooltip.product.image.tab")); // NOI18N
        jPanel6.setPreferredSize(new java.awt.Dimension(0, 0));

        jLabel34.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel34.setText(bundle.getString("label.imagesize")); // NOI18N
        jLabel34.setPreferredSize(new java.awt.Dimension(500, 30));

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jLabel34, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(134, Short.MAX_VALUE))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(m_jImage, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(243, 243, 243))))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(m_jImage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel34, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jTabbedPane1.addTab(bundle.getString("label.image"), jPanel6); // NOI18N

        jPanel4.setToolTipText(bundle.getString("tooltip.product.button.tab")); // NOI18N
        jPanel4.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jPanel4.setPreferredSize(new java.awt.Dimension(0, 0));

        jLabel28.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel28.setText(bundle.getString("label.prodbuttonhtml")); // NOI18N
        jLabel28.setPreferredSize(new java.awt.Dimension(250, 30));

        jButtonHTML.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jButtonHTML.setText(bundle.getString("button.htmltest")); // NOI18N
        jButtonHTML.setMargin(new java.awt.Insets(1, 1, 1, 1));
        jButtonHTML.setMaximumSize(new java.awt.Dimension(96, 72));
        jButtonHTML.setMinimumSize(new java.awt.Dimension(96, 72));
        jButtonHTML.setPreferredSize(new java.awt.Dimension(96, 72));
        jButtonHTML.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonHTMLActionPerformed(evt);
            }
        });

        jLabel21.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel21.setText(bundle.getString("label.texttip")); // NOI18N
        jLabel21.setPreferredSize(new java.awt.Dimension(110, 30));

        m_jTextTip.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jTextTip.setPreferredSize(new java.awt.Dimension(400, 30));

        m_jDisplay.setColumns(20);
        m_jDisplay.setLineWrap(true);
        m_jDisplay.setRows(4);
        m_jDisplay.setWrapStyleWord(true);
        m_jDisplay.setPreferredSize(new java.awt.Dimension(160, 100));
        jScrollPane3.setViewportView(m_jDisplay);

        jBtnBreak.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jBtnBreak.setText(bundle.getString("button.prodhtmldisplayBreak")); // NOI18N
        jBtnBreak.setToolTipText("<html><center><h4>Inserts a Line Break<br> (a new line) for the button text");
        jBtnBreak.setPreferredSize(new java.awt.Dimension(70, 35));
        jBtnBreak.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnBreakActionPerformed(evt);
            }
        });

        jBtnColour.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jBtnColour.setForeground(new java.awt.Color(0, 204, 255));
        jBtnColour.setText(bundle.getString("button.prodhtmldisplayColour")); // NOI18N
        jBtnColour.setToolTipText("<html><center><h4>Set the colour <br>for the button text");
        jBtnColour.setPreferredSize(new java.awt.Dimension(70, 35));
        jBtnColour.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnColourActionPerformed(evt);
            }
        });

        jBtnLarge.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jBtnLarge.setText(bundle.getString("button.prodhtmldisplayLarge")); // NOI18N
        jBtnLarge.setToolTipText("<html><center><h4>Set the button<br> text to Large");
        jBtnLarge.setPreferredSize(new java.awt.Dimension(70, 35));
        jBtnLarge.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnLargeActionPerformed(evt);
            }
        });

        jBtnSmall.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jBtnSmall.setText(bundle.getString("button.prodhtmldisplaySmall")); // NOI18N
        jBtnSmall.setToolTipText("<html><center><h4>Set the button<br>text to Small");
        jBtnSmall.setPreferredSize(new java.awt.Dimension(70, 35));
        jBtnSmall.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnSmallActionPerformed(evt);
            }
        });

        jBtnBold.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jBtnBold.setText(bundle.getString("button.prodhtmldisplayBold")); // NOI18N
        jBtnBold.setToolTipText("<html><center><h4>Set the button<br> text to Italic");
        jBtnBold.setPreferredSize(new java.awt.Dimension(70, 35));
        jBtnBold.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnBoldActionPerformed(evt);
            }
        });

        jBtnItalic.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jBtnItalic.setText(bundle.getString("button.prodhtmldisplayItalic")); // NOI18N
        jBtnItalic.setToolTipText("<html><center><h4>Set the button<br> text to Italic");
        jBtnItalic.setPreferredSize(new java.awt.Dimension(70, 35));
        jBtnItalic.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnItalicActionPerformed(evt);
            }
        });

        jBtnImage.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jBtnImage.setText(bundle.getString("button.prodhtmldisplayImage")); // NOI18N
        jBtnImage.setToolTipText("<html><center><h4>Insert image from<br>local disk or internet URL");
        jBtnImage.setPreferredSize(new java.awt.Dimension(70, 35));
        jBtnImage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnImageActionPerformed(evt);
            }
        });

        jBtnReset.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jBtnReset.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/reload.png"))); // NOI18N
        jBtnReset.setText(bundle.getString("button.prodhtmldisplayReset")); // NOI18N
        jBtnReset.setPreferredSize(new java.awt.Dimension(100, 35));
        jBtnReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnResetActionPerformed(evt);
            }
        });

        jBtnStyle.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jBtnStyle.setText(bundle.getString("button.prodhtmldisplayStyle")); // NOI18N
        jBtnStyle.setToolTipText("<html><center><h4>Insert <style> tag to change<br>button background colour");
        jBtnStyle.setPreferredSize(new java.awt.Dimension(70, 35));
        jBtnStyle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnStyleActionPerformed(evt);
            }
        });

        colourChooser.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        colourChooser.setText(AppLocal.getIntString("button.prodhtmldisplayBackground")); // NOI18N
        colourChooser.setToolTipText(bundle.getString("tooltip.prodhtmldisplayColourChooser")); // NOI18N
        colourChooser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                colourChooserActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(jPanel4Layout.createSequentialGroup()
                            .addComponent(jLabel28, javax.swing.GroupLayout.PREFERRED_SIZE, 230, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(272, 272, 272)
                            .addComponent(jBtnReset, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel4Layout.createSequentialGroup()
                                .addComponent(jBtnStyle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jBtnBreak, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jBtnColour, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jBtnLarge, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jBtnSmall, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jBtnBold, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jBtnItalic, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jBtnImage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel4Layout.createSequentialGroup()
                                .addComponent(jLabel21, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jButtonHTML, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(m_jTextTip, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                    .addComponent(colourChooser, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel28, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jBtnReset, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jBtnSmall, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jBtnColour, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jBtnLarge, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jBtnBold, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jBtnItalic, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jBtnImage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jBtnStyle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jBtnBreak, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12)
                .addComponent(colourChooser, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel21, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jTextTip, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jButtonHTML, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(84, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab(AppLocal.getIntString("label.button"), jPanel4); // NOI18N

        add(jTabbedPane1);
        jTabbedPane1.setBounds(0, 10, 640, 490);
        jTabbedPane1.getAccessibleContext().setAccessibleName("Product Editor Tab");
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonHTMLActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonHTMLActionPerformed
        setButtonHTML();
    }//GEN-LAST:event_jButtonHTMLActionPerformed

    private void jBtnXmlActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnXmlActionPerformed
        if (txtAttributes.getText() == null || txtAttributes.getText().isBlank()) {
            txtAttributes.setText(getEmptyProperty());
        }
    }//GEN-LAST:event_jBtnXmlActionPerformed

    private void jBtnSmallActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnSmallActionPerformed
        customOptionMode = 4;
        setDisplay(customOptionMode);
    }//GEN-LAST:event_jBtnSmallActionPerformed

    private void jBtnBreakActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnBreakActionPerformed
        customOptionMode = 1;
        setDisplay(customOptionMode);
    }//GEN-LAST:event_jBtnBreakActionPerformed

    private void jBtnColourActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnColourActionPerformed
        customOptionMode = 2;
        setDisplay(customOptionMode);
    }//GEN-LAST:event_jBtnColourActionPerformed

    private void jBtnLargeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnLargeActionPerformed
        customOptionMode = 3;
        setDisplay(customOptionMode);
    }//GEN-LAST:event_jBtnLargeActionPerformed

    private void jBtnBoldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnBoldActionPerformed
        customOptionMode = 5;
        setDisplay(customOptionMode);
    }//GEN-LAST:event_jBtnBoldActionPerformed

    private void jBtnItalicActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnItalicActionPerformed
        customOptionMode = 6;
        setDisplay(customOptionMode);
    }//GEN-LAST:event_jBtnItalicActionPerformed

    private void jBtnImageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnImageActionPerformed
        customOptionMode = 7;
        setDisplay(customOptionMode);
    }//GEN-LAST:event_jBtnImageActionPerformed

    private void jBtnResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnResetActionPerformed
        customOptionMode = 8;
        setDisplay(customOptionMode);
    }//GEN-LAST:event_jBtnResetActionPerformed

    private void jBtnStyleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnStyleActionPerformed
        customOptionMode = 9;
        setDisplay(customOptionMode);
    }//GEN-LAST:event_jBtnStyleActionPerformed

    private void m_jbtndateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jbtndateActionPerformed

        Date date;
        try {
            date = Formats.TIMESTAMP.parseValue(m_jdate.getText());
        }
        catch (BasicException e) {
            date = null;
        }
        date = JCalendarDialog.showCalendarTime(this, date);
        if (date != null) {
            m_jdate.setText(Formats.TIMESTAMP.formatValue(date));
        }
    }//GEN-LAST:event_m_jbtndateActionPerformed

    private void jBtnShowTransActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnShowTransActionPerformed

        if (productId != null) {
            StockTableModel stockModel = new StockTableModel(getProductOfName(productId));
            jTableProductStock.setModel(stockModel);
            if (stockModel.getRowCount() > 0) {
                jTableProductStock.setVisible(true);
            } else {
                jTableProductStock.setVisible(false);
                JOptionPane.showMessageDialog(this,
                        "No Stock Locations for this Product", "Locations", JOptionPane.INFORMATION_MESSAGE);
            }
            resetTranxTable();
        }
    }//GEN-LAST:event_jBtnShowTransActionPerformed

    private void m_jGrossProfitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jGrossProfitActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_m_jGrossProfitActionPerformed

    private void colourChooserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_colourChooserActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_colourChooserActionPerformed

    private void m_jInCatalogActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jInCatalogActionPerformed

        if (m_jInCatalog.isSelected()) {
            m_jCatalogOrder.setEnabled(true);
        } else {
            m_jCatalogOrder.setEnabled(false);
            m_jCatalogOrder.setText(null);
        }
    }//GEN-LAST:event_m_jInCatalogActionPerformed

    private void jBtnSupplierActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnSupplierActionPerformed
        JDialogNewSupplier dialog = JDialogNewSupplier.getDialog(this, appView);
        dialog.setVisible(true);

        if (dialog.getSelectedSupplier() != null) {
            m_SuppliersModel = new ComboBoxValModel(dlSuppliers.getSupplierListAll());
            m_jSupplier.setModel(m_SuppliersModel);
        }
    }//GEN-LAST:event_jBtnSupplierActionPerformed

    private void none(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_none

    }//GEN-LAST:event_none

    private void m_jNameFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_m_jNameFocusLost
        setDisplay(customOptionMode);
    }//GEN-LAST:event_m_jNameFocusLost

    private void m_jCodeFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_m_jCodeFocusLost
        if (m_jCode.getText().length() < 8) {
            m_jCodetype.setSelectedIndex(2);
        }
    }//GEN-LAST:event_m_jCodeFocusLost

    private void m_jRefFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_m_jRefFocusLost
        setCode();
    }//GEN-LAST:event_m_jRefFocusLost

    private void jLabel1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel1MouseClicked

        if (evt.getClickCount() == 2) {
            String uuidString = productId.toString();
            StringSelection stringSelection = new StringSelection(uuidString);
            Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
            clpbrd.setContents(stringSelection, null);

            JOptionPane.showMessageDialog(this,
                AppLocal.getIntString("message.uuidcopy"));
        }
    }//GEN-LAST:event_jLabel1MouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton colourChooser;
    private javax.swing.JButton jBtnBold;
    private javax.swing.JButton jBtnBreak;
    private javax.swing.JButton jBtnColour;
    private javax.swing.JButton jBtnImage;
    private javax.swing.JButton jBtnItalic;
    private javax.swing.JButton jBtnLarge;
    private javax.swing.JButton jBtnReset;
    private javax.swing.JButton jBtnShowTrans;
    private javax.swing.JButton jBtnSmall;
    private javax.swing.JButton jBtnStyle;
    private javax.swing.JButton jBtnSupplier;
    private javax.swing.JButton jBtnXml;
    private javax.swing.JButton jButtonHTML;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLblDate;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable jTableProductStock;
    private javax.swing.JComboBox m_jAtt;
    private javax.swing.JCheckBox m_jCOM;
    private javax.swing.JTextField m_jCatalogOrder;
    private javax.swing.JComboBox m_jCategory;
    private javax.swing.JCheckBox m_jCheckWarrantyReceipt;
    private javax.swing.JTextField m_jCode;
    private javax.swing.JComboBox m_jCodetype;
    private javax.swing.JCheckBox m_jConstant;
    private javax.swing.JTextArea m_jDisplay;
    private javax.swing.JTextField m_jGrossProfit;
    private com.openbravo.data.gui.JImageEditor m_jImage;
    private javax.swing.JCheckBox m_jInCatalog;
    private javax.swing.JTextField m_jName;
    private javax.swing.JTextField m_jPriceBuy;
    private javax.swing.JTextField m_jPriceSell;
    private javax.swing.JTextField m_jPriceSellTax;
    private javax.swing.JCheckBox m_jPrintKB;
    private javax.swing.JComboBox m_jPrintTo;
    private javax.swing.JTextField m_jRef;
    private javax.swing.JCheckBox m_jScale;
    private javax.swing.JCheckBox m_jSendStatus;
    private javax.swing.JCheckBox m_jService;
    private javax.swing.JTextField m_jStockUnits;
    private javax.swing.JComboBox m_jSupplier;
    private javax.swing.JComboBox m_jTax;
    private javax.swing.JTextField m_jTextTip;
    private javax.swing.JLabel m_jTitle;
    private javax.swing.JComboBox m_jUom;
    private javax.swing.JCheckBox m_jVerpatrib;
    private javax.swing.JCheckBox m_jVprice;
    private javax.swing.JButton m_jbtndate;
    private javax.swing.JTextField m_jdate;
    private javax.swing.JTextField m_jmargin;
    private javax.swing.JTextField m_jstockcost;
    private javax.swing.JTextField m_jstockvolume;
    private javax.swing.JPanel pricePanel;
    private javax.swing.JTextArea txtAttributes;
    private javax.swing.JLabel webLabel1;
    // End of variables declaration//GEN-END:variables

}
