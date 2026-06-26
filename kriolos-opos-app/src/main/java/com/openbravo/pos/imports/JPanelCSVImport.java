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
package com.openbravo.pos.imports;

import com.openbravo.pos.forms.JPanelView;
import com.csvreader.CsvReader;
import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.ComboBoxValModel;
import com.openbravo.data.loader.*;
import com.openbravo.data.user.DefaultSaveProvider;
import com.openbravo.data.user.SaveProvider;
import com.openbravo.pos.forms.*;
import com.openbravo.pos.sales.TaxesLogic;
import com.openbravo.pos.ticket.ProductInfoExt;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * GUI and code for importing Products from a CSV file 
 * 
 * Allow to add or update many products quickly and easily.
 *
 */
public class JPanelCSVImport extends JPanel implements JPanelView {

    private static final Logger LOGGER = Logger.getLogger(JPanelCSVImport.class.getName());

    private ArrayList<String> Headers = new ArrayList<>();
    private Session dbSession;
    private String csvFileName;
    private Double dOriginalRate = 0.0;
    private String dCategory;
    private String dSupplier;
    private String csvMessage = "";
    private CsvReader products;
    private double oldSellPrice = 0;
    private double oldBuyPrice = 0;
    private int currentRecord;
    private int rowCount = 0;
    private String last_folder;
    private File config_file;

    private static String category_default = "[ USE DEFAULT CATEGORY ]";
    private static String reject_bad_category = "[ REJECT ITEMS WITH BAD CATEGORIES ]";
    private static String supplier_default = "[ USE DEFAULT SUPPLIER ]";
    private static String reject_bad_supplier = "[ REJECT ITEMS WITH BAD SUPPLIER ]";

    private DataLogicSales m_dlSales;
    private DataLogicSystem m_dlSystem;

    protected SaveProvider spr;

    private String Category;
    private String categoryName;
    private String categoryParentid;
    private Integer categoryCatorder;
    private SentenceList m_sentcat;
    private ComboBoxValModel m_CategoryModel;
    private HashMap cat_list = new HashMap();
    private ArrayList badCategories = new ArrayList();

    private String productReference;
    private String productBarcode;
    private String productBarcodetype;
    private String productName;
    private Double productBuyPrice;
    private Double productSellPrice;
    private String productTax;

    private String Supplier;
    private String supplierName;
    private SentenceList m_sentsupp;
    private ComboBoxValModel m_SupplierModel;
    private HashMap supp_list = new HashMap();
    private ArrayList badSuppliers = new ArrayList();

    private SentenceList taxcatsent;
    private ComboBoxValModel taxcatmodel;
    private SentenceList taxsent;
    private TaxesLogic taxeslogic;

    private DocumentListener documentListener;
    private ProductInfoExt prodInfo;
    private String recordType = null;

    private int newRecords = 0;
    private int invalidRecords = 0;
    private int priceUpdates = 0;
    private int missingData = 0;
    private int noChanges = 0;
    private int badPrice = 0;

    private double dTaxRate;

    private Integer progress = 0;

    private String iso = "ISO-8859-1";

    /**
     * Constructs a new JPanelCSVImport object
     *
     * @param oApp AppView
     */
    public JPanelCSVImport(AppView oApp) {
        initComponents();

        dbSession = oApp.getSession();
        
        AppProperties props = oApp.getProperties();

        m_dlSales = new DataLogicSales();
        m_dlSales.init(dbSession);

        m_dlSystem = new DataLogicSystem();
        m_dlSystem.init(dbSession);

        spr = new DefaultSaveProvider(
                m_dlSales.getProductCatUpdate(),
                m_dlSales.getProductCatInsert(),
                m_dlSales.getProductCatDelete());

        last_folder = props.getProperty("CSV.last_folder");
        config_file = props.getConfigFile();

        jFileName.getDocument().addDocumentListener(documentListener);

        documentListener = new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent documentEvent) {
                jHeaderRead.setEnabled(true);
            }

            @Override
            public void insertUpdate(DocumentEvent documentEvent) {
                if (!"".equals(jFileName.getText().trim())) {
                    jHeaderRead.setEnabled(true);
                }
            }

            @Override
            public void removeUpdate(DocumentEvent documentEvent) {
                if (jFileName.getText().trim().equals("")) {
                    jHeaderRead.setEnabled(false);
                }
            }
        };
        jFileName.getDocument().addDocumentListener(documentListener);

    }

    /**
     * Reads the headers from the CSV file and initializes subsequent form
     * fields. This function first reads the headers from the CSVFileName file,
     * then puts them into the header combo boxes and enables the other form
     * inputs.
     *
     * @param CSVFileName Name of the file (including the path) to open and read
     * CSV data from
     * @throws IOException If there is an issue reading the CSV file
     */
    private void GetheadersFromFile(String CSVFileName) throws IOException {

        File f = new File(CSVFileName);
        if (f.exists()) {
//            products = new CsvReader(CSVFileName, ',' ,Charset.forName("UTF-8"));
            products = new CsvReader(CSVFileName, ',', Charset.forName(jCBiso.getSelectedItem().toString()));
            products.setDelimiter(((String) jComboSeparator.getSelectedItem()).charAt(0));
            products.readHeaders();

            if (products.getHeaderCount() < 5) {
                JOptionPane.showMessageDialog(this,
                        "Incorrect header in your source file",
                        "Header Error",
                        JOptionPane.WARNING_MESSAGE);
                products.close();
                return;
            }
            rowCount = 0;
            int i = 0;
            Headers.clear();
            Headers.add("");
            jComboName.addItem("");
            jComboReference.addItem("");
            jComboBarcode.addItem("");
            jComboBuy.addItem("");
            jComboSell.addItem("");
            jComboTax.addItem("");
            jComboCategory.addItem("");
            jComboSupplier.addItem("");

            while (i < products.getHeaderCount()) {
                jComboName.addItem(products.getHeader(i));
                jComboReference.addItem(products.getHeader(i));
                jComboBarcode.addItem(products.getHeader(i));
                jComboBuy.addItem(products.getHeader(i));
                jComboSell.addItem(products.getHeader(i));
                jComboTax.addItem(products.getHeader(i));
                jComboCategory.addItem(products.getHeader(i));
                jComboSupplier.addItem(products.getHeader(i));

                Headers.add(products.getHeader(i));
                ++i;
            }

            enableCheckBoxes();

            while (products.readRecord()) {
                System.out.println(products.getRawRecord());
                ++rowCount;
            }

            jTextRecords.setText(Long.toString(rowCount));

            products.close();

        } else {
            JOptionPane.showMessageDialog(this, "Unable to locate csv file: "
                    + CSVFileName,
                    "File not found",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Enables all the selection options on the for to allow the user to
     * interact with the routine.
     *
     */
    private void enableCheckBoxes() {
        jHeaderRead.setEnabled(false);
        jImport.setEnabled(false);
        jbtnReset.setEnabled(true);
        jComboReference.setEnabled(true);
        jComboName.setEnabled(true);
        jComboBarcode.setEnabled(true);
        jComboBuy.setEnabled(true);
        jComboSell.setEnabled(true);
        jComboTax.setEnabled(true);
        jComboCategory.setEnabled(true);
        jComboDefaultCategory.setEnabled(true);
        jComboSupplier.setEnabled(true);
        jComboDefaultSupplier.setEnabled(true);

        jCheckInCatalogue.setEnabled(true);
        jCheckSellIncTax.setEnabled(true);
    }

    /**
     * Pushes the Import process into a new thread so it doesn't interfere with
     * the UI responsiveness.
     */
    private void setWorker() {
        progress = 0;
        importProgressBar.setStringPainted(true);

        final SwingWorker<Integer, Integer> pbWorker;
        pbWorker = new SwingWorker<Integer, Integer>() {

            @Override
            protected final Integer doInBackground() throws Exception {
                while ((progress >= 0) && (progress < 100)) {
                    Thread.sleep(50);
                    this.publish(progress);
                }
                this.publish(100);
                this.done();
                return 100;
            }

            @Override
            protected final void process(final List<Integer> chunks) {
                importProgressBar.setValue(chunks.get(0));
                if (progress > 100) {
                    progress = 100;
                    importProgressBar.setString("Imported 100%");
                } else {
                    importProgressBar.setString("Imported " + progress + "%");
                }
            }
        };
        pbWorker.execute();
    }

    /**
     * Runs the setWorker.
     *
     */
    private class workProcess implements Runnable {

        @Override
        public void run() {
            try {
                ImportCsvFile(jFileName.getText());
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
        }
    }

    /**
     * Imports the CSV File using specifications from the form.
     *
     * @param CSVFileName Name of the file (including path) to import.
     * @throws IOException If there are file reading issues.
     */
    private void ImportCsvFile(String CSVFileName) throws IOException {

        File f = new File(CSVFileName);
        if (f.exists()) {
            importProgressBar.setString("Starting...");
            importProgressBar.setVisible(true);
            jImport.setEnabled(false);

// Read file
//            products = new CsvReader(CSVFileName, ',' ,Charset.forName("UTF-8"));
            products = new CsvReader(CSVFileName, ',', Charset.forName(jCBiso.getSelectedItem().toString()));

            products.setDelimiter(((String) jComboSeparator.getSelectedItem()).charAt(0));
            products.readHeaders();

            currentRecord = 0;

            while (products.readRecord()) {
                productReference = products.get((String) jComboReference.getSelectedItem());
                productName = products.get((String) jComboName.getSelectedItem());
                productBarcode = products.get((String) jComboBarcode.getSelectedItem());
                String BuyPrice = products.get((String) jComboBuy.getSelectedItem());
                String SellPrice = products.get((String) jComboSell.getSelectedItem());
                productTax = products.get((String) jComboTax.getSelectedItem());

                Category = products.get((String) jComboCategory.getSelectedItem());
                Supplier = products.get((String) jComboSupplier.getSelectedItem());

                currentRecord++;
                progress = currentRecord;

// Strip Currencies Dollar GBP, euro 
                BuyPrice = StringUtils.replaceChars(BuyPrice, "$", "");
                SellPrice = StringUtils.replaceChars(SellPrice, "$", "");

                BuyPrice = StringUtils.replaceChars(BuyPrice, "£", "");
                SellPrice = StringUtils.replaceChars(SellPrice, "£", "");

                BuyPrice = StringUtils.replaceChars(BuyPrice, "€", "");
                SellPrice = StringUtils.replaceChars(SellPrice, "€", "");

                dCategory = getCategory();
                if ("Bad Category".equals(dCategory)) {
                    csvMessage = "Bad category details";
                } else {
                    csvMessage = "Missing data or Invalid number";
                }

                dSupplier = getSupplier();
                if ("Bad Supplier".equals(dSupplier)) {
                    csvMessage = "Bad Supplier details";
                } else {
                    csvMessage = "Missing data or Invalid number";
                }

                if (validateNumber(BuyPrice)) {
                    productBuyPrice = Double.parseDouble(BuyPrice);
                } else {
                    productBuyPrice = null;
                }

                if (validateNumber(SellPrice)) {
                    productSellPrice = getSellPrice(SellPrice);
                } else {
                    productSellPrice = null;
                }

                if ("".equals(productReference)
                        //                        | "".equals(productName)
                        | "".equals(productBarcode)
                        | "".equals(BuyPrice)
                        | "".equals(SellPrice)
                        | "".equals(productTax)
                        | productBuyPrice == null
                        | productSellPrice == null
                        | "Bad Category".equals(dCategory)) {

                    if (productBuyPrice == null
                            | productSellPrice == null) {
                        badPrice++;
                    } else {
                        missingData++;
                    }
                    createCSVEntry(csvMessage, null, null);
                } else {

                    recordType = getRecord();
                    switch (recordType) {
                        case "new":
                            createProduct("new");
                            newRecords++;
                            createCSVEntry("New product", null, null);
                            break;
                        case "Name change":
                        case "Barcode change":
                        case "Reference change":
                        case "Duplicate Reference found.":
                        case "Duplicate Barcode found.":
                        case "Duplicate Description found.":
                            createProduct("new");
                            newRecords++;
                            createCSVEntry("New product", null, null);
                            break;
                        case "Tax change":
                        case "Exception":
                            invalidRecords++;
                            createCSVEntry(recordType, null, null);
                            break;
                        default:
                            updateRecord(recordType);
                            break;
                    }
                }
            }
            products.close();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Unable to locate "
                    + CSVFileName,
                    "File not found",
                    JOptionPane.WARNING_MESSAGE);
        }

        jTextNew.setText(Integer.toString(newRecords));
        jTextUpdate.setText(Integer.toString(priceUpdates));
        jTextInvalid.setText(Integer.toString(invalidRecords));
        jTextMissing.setText(Integer.toString(missingData));
        jTextNoChange.setText(Integer.toString(noChanges));
        jTextBadPrice.setText(Integer.toString(badPrice));
        if (badCategories.size() == 1 && badCategories.get(0) == "") {
            jTextBadCats.setText("0");
        } else {
            jTextBadCats.setText(Integer.toString(badCategories.size()));
        }

        JOptionPane.showMessageDialog(this,
                "Import Complete",
                "Imported",
                JOptionPane.WARNING_MESSAGE);

        progress = 100;
        importProgressBar.setValue(progress);
        importProgressBar.setString("Imported : " + progress + " records");
//        }
    }

    /**
     * Tests <code>testString</code> for validity as a number
     *
     * @param testString the string to be checked
     * @return True if a real number False if not
     */
    private Boolean validateNumber(String testString) {
        try {
            Double res = Double.parseDouble(testString);
            return (true);
        } catch (NumberFormatException e) {
            return (false);
        }
    }

    /**
     * Lookup for an existing Product'dbSession Category CategoryID as string.
     */
    private String getCategory() {

        if (jComboCategory.getSelectedItem() != category_default) {
            String cat = (String) cat_list.get(Category);

            if (cat != null) {
                return (cat);
            }
        }

        if (!Category.equals("")) {
            Object[] newcat = new Object[3];
            newcat[0] = UUID.randomUUID().toString();
            newcat[1] = Category;
            newcat[2] = true;

            try {
                m_dlSales.createCategory(newcat);

                cat_list = new HashMap<>();
                for (Object category : m_sentcat.list()) {
                    m_CategoryModel.setSelectedItem(category);
                    cat_list.put(category.toString(), m_CategoryModel.getSelectedKey().toString());
                }
                String cat = (String) cat_list.get(Category);
                return (cat);
            } catch (BasicException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
        }

        if (!badCategories.contains(Category)) {
            badCategories.add(Category.trim());                                 // Save a list of the bad categories
        }
        return ((jComboDefaultCategory.getSelectedItem()
                == reject_bad_category)
                        ? "Bad Category"
                        : (String) cat_list.get(m_CategoryModel.getSelectedText()));
    }

    /**
     * Grab Supplier from Import file.
     *
     */
    private String getSupplier() {

        if (jComboSupplier.getSelectedItem() != supplier_default) {
            String supp = (String) supp_list.get(Supplier);

            if (supp != null) {
                return (supp);
            }
        }

        if (!Supplier.equals("")) {
            Object[] newsupp = new Object[4];
            newsupp[0] = UUID.randomUUID().toString();
            newsupp[1] = Supplier;
            newsupp[2] = Supplier;
            newsupp[3] = true;

            try {
                m_dlSales.createSupplier(newsupp);

                supp_list = new HashMap<>();
                for (Object supplier : m_sentsupp.list()) {
                    m_SupplierModel.setSelectedItem(supplier);
                    supp_list.put(supplier.toString(), m_SupplierModel.getSelectedKey().toString());
                }
                String supp = (String) supp_list.get(Supplier);
                return (supp);
            } catch (BasicException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
        }

        if (!badSuppliers.contains(Supplier)) {
            badSuppliers.add(Supplier.trim());
        }
        return ((jComboDefaultSupplier.getSelectedItem()
                == reject_bad_supplier)
                        ? "Bad Supplier"
                        : (String) supp_list.get(m_SupplierModel.getSelectedText()));
    }

    /**
     * Adjusts the sell price for included taxes
     *
     * @param pSellPrice sell price to be converted
     * @return sell price after adjustment for included taxes and converted to
     * double
     */
    private Double getSellPrice(String pSellPrice) {

        // Get the Tax Rate from the Import file
        dTaxRate = taxeslogic.getTaxRate(productTax);

        if (jCheckSellIncTax.isSelected()) {
            productSellPrice = ((Double.parseDouble(pSellPrice)) / (1 + dTaxRate));
            return productSellPrice;
        } else {
            return (Double.parseDouble(pSellPrice));
        }
    }

    /**
     * Updated the record in the database with the new prices and category if
     * needed.
     *
     * @param pID Unique product id of the record to be updated It then creates
     * an updated record for the product, subject to the prices being different
     *
     */
    private void updateRecord(String pID) {
        prodInfo = new ProductInfoExt();
        try {
            prodInfo = m_dlSales.getProductInfo(pID);
            dOriginalRate = taxeslogic.getTaxRate(prodInfo.getTaxCategoryID());
            dCategory = ((String) cat_list.get(prodInfo.getCategoryID())
                    == null) ? prodInfo.getCategoryID()
                            : (String) cat_list.get(prodInfo.getCategoryID());
            oldBuyPrice = prodInfo.getPriceBuy();
            oldSellPrice = prodInfo.getPriceSell();
//            productSellPrice *= (1 + dOriginalRate);

            dSupplier = ((String) supp_list.get(prodInfo.getSupplierID())
                    == null) ? prodInfo.getSupplierID()
                            : (String) supp_list.get(prodInfo.getSupplierID());

            if ((oldBuyPrice != productBuyPrice)
                    || (oldSellPrice != productSellPrice)) {
                createCSVEntry("Updated Price Details", oldBuyPrice,
                        (jCheckSellIncTax.isSelected())
                        ? oldSellPrice * (1 + dOriginalRate)
                        : oldSellPrice);
                createProduct("update");
                priceUpdates++;
            } else {
                noChanges++;
            }
        } catch (BasicException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        }
    }

    /**
     * Gets the title of the current panel
     *
     * @return The name of the panel
     */
    @Override
    public String getTitle() {
        return AppLocal.getIntString("Menu.CSVImport");
    }

    /**
     * Returns this object
     *
     * @return
     */
    @Override
    public JComponent getComponent() {
        return this;
    }

    /**
     * Loads Tax and Category data into their combo boxes.
     *
     * @throws com.openbravo.basic.BasicException
     */
    @Override
    public void activate() throws BasicException {
        // Get tax details and logic
        taxsent = m_dlSales.getTaxList();
        taxeslogic = new TaxesLogic(taxsent.list());
        taxcatsent = m_dlSales.getTaxCategoriesList();
        taxcatmodel = new ComboBoxValModel(taxcatsent.list());

        // Get categories list
        m_sentcat = m_dlSales.getCategoriesList();
        m_CategoryModel = new ComboBoxValModel(m_sentcat.list());
        m_CategoryModel.add(reject_bad_category);
        jComboDefaultCategory.setModel(m_CategoryModel);

        // Build the cat_list for later use
        cat_list = new HashMap<>();
        for (Object category : m_sentcat.list()) {
            m_CategoryModel.setSelectedItem(category);
            cat_list.put(category.toString(), m_CategoryModel.getSelectedKey().toString());
        }

        // Get suppliers list
        m_sentsupp = m_dlSales.getSuppList();
        m_SupplierModel = new ComboBoxValModel(m_sentsupp.list());
        m_SupplierModel.add(reject_bad_supplier);
        jComboDefaultSupplier.setModel(m_SupplierModel);

        // Build the supp_list for later
        supp_list = new HashMap<>();
        for (Object supplier : m_sentsupp.list()) {
            m_SupplierModel.setSelectedItem(supplier);
            supp_list.put(supplier.toString(), m_SupplierModel.getSelectedKey().toString());
        }

        // reset the selected to the first in the list
        m_CategoryModel.setSelectedItem(null);
        m_SupplierModel.setSelectedItem(null);

        // Set the column delimiter
        jComboSeparator.removeAllItems();
        jComboSeparator.addItem(",");
        jComboSeparator.addItem(";");
        jComboSeparator.addItem("~");
        jComboSeparator.addItem("^");

    }

    /**
     * Resets all the form fields
     */
    public void resetFields() {
        // Clear the form
        jComboReference.removeAllItems();
        jComboReference.setEnabled(false);

        jComboName.removeAllItems();
        jComboName.setEnabled(false);

        jComboBarcode.removeAllItems();
        jComboBarcode.setEnabled(false);

        jComboBuy.removeAllItems();
        jComboBuy.setEnabled(false);

        jComboSell.removeAllItems();
        jComboSell.setEnabled(false);

        jComboTax.removeAllItems();
        jComboTax.setEnabled(false);

        jComboCategory.removeAllItems();
        jComboCategory.setEnabled(false);
        jComboDefaultCategory.setEnabled(false);

        jComboSupplier.removeAllItems();
        jComboSupplier.setEnabled(false);
        jComboDefaultSupplier.setEnabled(false);

        jImport.setEnabled(false);
        jbtnReset.setEnabled(true);
        jHeaderRead.setEnabled(false);
        jCheckInCatalogue.setSelected(true);
        jCheckInCatalogue.setEnabled(true);
        jCheckSellIncTax.setSelected(true);
        jCheckSellIncTax.setEnabled(true);
        jFileName.setText(null);
        csvFileName = "";
        jTextNew.setText("");
        jTextUpdate.setText("");
        jTextInvalid.setText("");
        jTextMissing.setText("");
        jTextNoChange.setText("");
        jTextRecords.setText("");
        jTextBadPrice.setText("");
        jTextBadCats.setText("");

        progress = 0;

        Headers.clear();
        newRecords = 0;
        invalidRecords = 0;
        priceUpdates = 0;
        missingData = 0;
        noChanges = 0;
        badPrice = 0;
//        badCategories = 0;

    }

    /**
     * Checks the field mappings to ensure all compulsory fields have been
     * completed to allow import to proceed
     */
    public void checkFieldMapping() {
        if (jComboReference.getSelectedItem() != ""
                & jComboName.getSelectedItem() != ""
                & jComboBarcode.getSelectedItem() != ""
                & jComboBuy.getSelectedItem() != ""
                & jComboSell.getSelectedItem() != ""
                & jComboTax.getSelectedItem() != ""
                & jComboCategory.getSelectedItem() != ""
                & m_CategoryModel.getSelectedText() != null) {
            jImport.setEnabled(true);
            jbtnReset.setEnabled(true);
        } else {
            jImport.setEnabled(false);
            jbtnReset.setEnabled(false);
        }
    }

    /**
     * Deactivates and resets all form fields.
     *
     * @return
     */
    @Override
    public boolean deactivate() {
        resetFields();
        return (true);
    }

    /**
     * Creates a new Category if it doesn't exist
     *
     * @param cType
     */
    public void createCategory(String cType) {

        Object[] mycat = new Object[7];
        mycat[0] = UUID.randomUUID().toString();                                // ID string
        mycat[1] = categoryName;                                                // Name string
        mycat[2] = categoryParentid;                                            // Parent String     
        mycat[3] = null;                                                        // Image
        mycat[4] = "<html><center><h4>" + categoryName;                         // Text tip string
        mycat[5] = true;                                                        // Show
        mycat[6] = categoryCatorder;                                            // Order 

        try {
            if ("new".equals(cType)) {
                spr.insertData(mycat);
            } else {
                spr.updateData(mycat);
            }
        } catch (BasicException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        }
    }

    /**
     *
     * @param sType
     */
    public void createSupplier(String sType) {

        Object[] mysupp = new Object[4];
        mysupp[0] = UUID.randomUUID().toString();                               // ID string
        mysupp[1] = supplierName;                                               // SearchKey
        mysupp[2] = supplierName;                                               // Name string
        mysupp[3] = true;                                                       // Visible     

        try {
            if ("new".equals(sType)) {
                spr.insertData(mysupp);
            } else {
                spr.updateData(mysupp);
            }
        } catch (BasicException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        }
    }

    /**
     *
     * @param pType
     */
    public void createProduct(String pType) {

        Object[] myprod = new Object[32];
        if ("new".equals(pType)) {
            myprod[0] = UUID.randomUUID().toString();
        } else {
            myprod[0] = prodInfo.getID();
        }                                                                       // ID string
        myprod[1] = productReference;                                           // Reference string
        myprod[2] = productBarcode;                                             // Barcode String     
        myprod[3] = null;                                                       // Barcode Type
        myprod[4] = productName;                                                // Name string        
        myprod[5] = productBuyPrice;                                            // Buy price double
        myprod[6] = productSellPrice;                                           // Sell price double        
        myprod[7] = dCategory;                                                  // Category string
        myprod[8] = productTax;                                                 // User Tax string 
        myprod[9] = null;                                                       // Attributeset string
        myprod[10] = 0.0;                                                       // Stock cost double
        myprod[11] = 0.0;                                                       // Stock volume double
        myprod[12] = null;                                                      // Image        
        myprod[13] = false;                                                     // IScomment flag (Attribute modifier)
        myprod[14] = false;                                                     // ISscale flag
        myprod[15] = false;                                                     // IsConstant flag
        myprod[16] = false;                                                     // PrintKB flag
        myprod[17] = false;                                                     // SendStatus flag        
        myprod[18] = false;                                                     // isService flag
        myprod[19] = null;                                                      // Attributes
        myprod[20] = "<html><center>" + productName;                            // Setup Display button    
        myprod[21] = false;                                                     // isVariable price flag
        myprod[22] = false;                                                     // Compulsory Att flag
        myprod[23] = "<html><center><h4>" + productName;                        // Text tip string
        myprod[24] = false;                                                     // Warranty flag
        myprod[25] = 0.0;                                                       // Stock Units
        myprod[26] = "1";                                                       // Printer         
        myprod[27] = dSupplier;                                                 // Supplier
        myprod[28] = "0";                                                       // UOM
        myprod[29] = null;                                                        // memodate
        myprod[30] = jCheckInCatalogue.isSelected();                            // In catalog flag
        myprod[31] = null;                                                      // catalog order                

        try {
            if ("new".equals(pType)) {
                spr.insertData(myprod);
                addStockCurrent("0", myprod[0].toString(), 0.0);
                importProgressBar.setString("Adding record " + progress);
            } else {
                spr.updateData(myprod);
                importProgressBar.setString("Updating record " + progress);
            }

        } catch (BasicException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        }
    }

    public void addStockCurrent(String LocationID, String ProductID, Double Units) throws BasicException {

        Object[] values = new Object[3];
        values[0] = "0";
        values[1] = ProductID;
        values[2] = (double) Units;

        PreparedSentence sentence = new PreparedSentence(dbSession,
                "INSERT INTO stockcurrent ( "
                + "LOCATION, PRODUCT, UNITS) VALUES (?, ?, ?)",
                 new SerializerWriteBasicExt((new Datas[]{
            Datas.STRING, Datas.STRING, Datas.DOUBLE}),
                        new int[]{0, 1, 2}));

        sentence.exec(values);
    }

    /**
     *
     * @param csvError
     * @param previousBuy
     * @param previousSell
     */
    public void createCSVEntry(String csvError, Double previousBuy, Double previousSell) {

        Object[] myprod = new Object[13];
        myprod[0] = UUID.randomUUID().toString();                               // ID string
        myprod[1] = Integer.toString(currentRecord);                            // Record number
        myprod[2] = csvError;                                                   // Error description
        myprod[3] = productReference;                                           // Reference string
        myprod[4] = productBarcode;                                             // Barcode String        
        myprod[5] = productName;                                                // Name string        
        myprod[6] = productBuyPrice;                                            // Buy price
        myprod[7] = productSellPrice;                                           // Sell price
        myprod[8] = previousBuy;                                                // Previous Buy price double
        myprod[9] = previousSell;                                               // Previous Sell price double
        myprod[10] = Category;                                                  // Category
        myprod[11] = productTax;                                                // Tax
        myprod[12] = Supplier;                                                  // Supplier
        try {
            m_dlSystem.execAddCSVEntry(myprod);
        } catch (BasicException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        }
    }

    /**
     *
     * @return
     */
    public String getRecord() {
        // Get record type using using DataLogicSystem
        Object[] myprod = new Object[3];
        myprod[0] = productReference;
        myprod[1] = productBarcode;
        myprod[2] = productName;
        try {
            return (m_dlSystem.getProductRecordType(myprod));
        } catch (BasicException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        }
        return "Exception";
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jFileChooserPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jFileName = new javax.swing.JTextField();
        jbtnFileChoose = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jComboReference = new javax.swing.JComboBox();
        jComboBarcode = new javax.swing.JComboBox();
        jComboName = new javax.swing.JComboBox();
        jComboBuy = new javax.swing.JComboBox();
        jComboSell = new javax.swing.JComboBox();
        jComboDefaultCategory = new javax.swing.JComboBox();
        jComboTax = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jCheckInCatalogue = new javax.swing.JCheckBox();
        jLabel8 = new javax.swing.JLabel();
        jCheckSellIncTax = new javax.swing.JCheckBox();
        jLabel12 = new javax.swing.JLabel();
        jComboCategory = new javax.swing.JComboBox();
        jLabel20 = new javax.swing.JLabel();
        jImport = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        jbtnReset = new javax.swing.JButton();
        jComboSupplier = new javax.swing.JComboBox();
        jLabel21 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        jComboDefaultSupplier = new javax.swing.JComboBox();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jHeaderRead = new javax.swing.JButton();
        jPanelStatus = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jTextUpdates = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jTextRecords = new javax.swing.JTextField();
        jTextNew = new javax.swing.JTextField();
        jTextInvalid = new javax.swing.JTextField();
        jTextUpdate = new javax.swing.JTextField();
        jTextMissing = new javax.swing.JTextField();
        jTextBadPrice = new javax.swing.JTextField();
        jTextNoChange = new javax.swing.JTextField();
        jLabel19 = new javax.swing.JLabel();
        jTextBadCats = new javax.swing.JTextField();
        jComboSeparator = new javax.swing.JComboBox();
        importProgressBar = new javax.swing.JProgressBar();
        jLblImportNotice = new javax.swing.JLabel();
        jCBiso = new javax.swing.JComboBox<>();

        setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        setMaximumSize(new java.awt.Dimension(750, 550));
        setMinimumSize(new java.awt.Dimension(750, 550));
        setOpaque(false);
        setPreferredSize(new java.awt.Dimension(750, 550));

        jLabel1.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("pos_messages"); // NOI18N
        jLabel1.setText(bundle.getString("label.csvfile")); // NOI18N
        jLabel1.setPreferredSize(new java.awt.Dimension(100, 30));

        jFileName.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jFileName.setMaximumSize(new java.awt.Dimension(400, 45));
        jFileName.setName(""); // NOI18N
        jFileName.setPreferredSize(new java.awt.Dimension(400, 45));
        jFileName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jFileNameActionPerformed(evt);
            }
        });

        jbtnFileChoose.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/fileopen.png"))); // NOI18N
        jbtnFileChoose.setMaximumSize(new java.awt.Dimension(64, 32));
        jbtnFileChoose.setMinimumSize(new java.awt.Dimension(64, 32));
        jbtnFileChoose.setPreferredSize(new java.awt.Dimension(80, 45));
        jbtnFileChoose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbtnFileChooseActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jFileChooserPanelLayout = new javax.swing.GroupLayout(jFileChooserPanel);
        jFileChooserPanel.setLayout(jFileChooserPanelLayout);
        jFileChooserPanelLayout.setHorizontalGroup(
            jFileChooserPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jFileChooserPanelLayout.createSequentialGroup()
                .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jFileName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jbtnFileChoose, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(120, 120, 120))
        );
        jFileChooserPanelLayout.setVerticalGroup(
            jFileChooserPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jbtnFileChoose, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jFileChooserPanelLayout.createSequentialGroup()
                .addGroup(jFileChooserPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jFileChooserPanelLayout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addComponent(jFileName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        jPanel1.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jPanel1.setPreferredSize(new java.awt.Dimension(430, 400));

        jComboReference.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jComboReference.setEnabled(false);
        jComboReference.setMinimumSize(new java.awt.Dimension(32, 25));
        jComboReference.setPreferredSize(new java.awt.Dimension(300, 30));
        jComboReference.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboReferenceItemStateChanged(evt);
            }
        });
        jComboReference.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jComboReferenceFocusGained(evt);
            }
        });

        jComboBarcode.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jComboBarcode.setEnabled(false);
        jComboBarcode.setMinimumSize(new java.awt.Dimension(32, 25));
        jComboBarcode.setPreferredSize(new java.awt.Dimension(300, 30));
        jComboBarcode.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBarcodeItemStateChanged(evt);
            }
        });
        jComboBarcode.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jComboBarcodeFocusGained(evt);
            }
        });

        jComboName.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jComboName.setEnabled(false);
        jComboName.setMinimumSize(new java.awt.Dimension(32, 25));
        jComboName.setPreferredSize(new java.awt.Dimension(300, 30));
        jComboName.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboNameItemStateChanged(evt);
            }
        });
        jComboName.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jComboNameFocusGained(evt);
            }
        });

        jComboBuy.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jComboBuy.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "" }));
        jComboBuy.setSelectedIndex(-1);
        jComboBuy.setEnabled(false);
        jComboBuy.setMinimumSize(new java.awt.Dimension(32, 25));
        jComboBuy.setPreferredSize(new java.awt.Dimension(300, 30));
        jComboBuy.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBuyItemStateChanged(evt);
            }
        });
        jComboBuy.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jComboBuyFocusGained(evt);
            }
        });

        jComboSell.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jComboSell.setEnabled(false);
        jComboSell.setMinimumSize(new java.awt.Dimension(32, 25));
        jComboSell.setPreferredSize(new java.awt.Dimension(300, 30));
        jComboSell.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboSellItemStateChanged(evt);
            }
        });
        jComboSell.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jComboSellFocusGained(evt);
            }
        });

        jComboDefaultCategory.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jComboDefaultCategory.setEnabled(false);
        jComboDefaultCategory.setMinimumSize(new java.awt.Dimension(32, 25));
        jComboDefaultCategory.setPreferredSize(new java.awt.Dimension(300, 30));
        jComboDefaultCategory.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboDefaultCategoryItemStateChanged(evt);
            }
        });
        jComboDefaultCategory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboDefaultCategoryActionPerformed(evt);
            }
        });

        jComboTax.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jComboTax.setEnabled(false);
        jComboTax.setPreferredSize(new java.awt.Dimension(300, 30));
        jComboTax.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboTaxItemStateChanged(evt);
            }
        });
        jComboTax.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jComboTaxFocusGained(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel3.setText(bundle.getString("label.prodref")); // NOI18N
        jLabel3.setPreferredSize(new java.awt.Dimension(100, 30));

        jLabel4.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel4.setText(bundle.getString("label.prodbarcode")); // NOI18N
        jLabel4.setPreferredSize(new java.awt.Dimension(100, 30));

        jLabel5.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel5.setText(bundle.getString("label.prodname")); // NOI18N
        jLabel5.setPreferredSize(new java.awt.Dimension(100, 30));

        jLabel10.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel10.setText(bundle.getString("label.prodpricebuy")); // NOI18N
        jLabel10.setPreferredSize(new java.awt.Dimension(100, 30));

        jLabel11.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel11.setText(bundle.getString("label.prodcategory")); // NOI18N
        jLabel11.setPreferredSize(new java.awt.Dimension(100, 30));

        jLabel7.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel7.setText(bundle.getString("label.prodtaxcode")); // NOI18N
        jLabel7.setPreferredSize(new java.awt.Dimension(100, 30));

        jCheckInCatalogue.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jCheckInCatalogue.setEnabled(false);
        jCheckInCatalogue.setMaximumSize(new java.awt.Dimension(30, 30));
        jCheckInCatalogue.setPreferredSize(new java.awt.Dimension(30, 30));

        jLabel8.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel8.setText(bundle.getString("label.prodincatalog")); // NOI18N
        jLabel8.setPreferredSize(new java.awt.Dimension(100, 30));

        jCheckSellIncTax.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jCheckSellIncTax.setEnabled(false);
        jCheckSellIncTax.setPreferredSize(new java.awt.Dimension(30, 30));

        jLabel12.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel12.setText(bundle.getString("label.csvsellingintax")); // NOI18N
        jLabel12.setPreferredSize(new java.awt.Dimension(200, 30));

        jComboCategory.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jComboCategory.setEnabled(false);
        jComboCategory.setMinimumSize(new java.awt.Dimension(32, 25));
        jComboCategory.setName(""); // NOI18N
        jComboCategory.setPreferredSize(new java.awt.Dimension(300, 30));
        jComboCategory.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboCategoryItemStateChanged(evt);
            }
        });
        jComboCategory.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jComboCategoryFocusGained(evt);
            }
        });

        jLabel20.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel20.setText(bundle.getString("label.prodpricesell")); // NOI18N
        jLabel20.setPreferredSize(new java.awt.Dimension(100, 30));

        jImport.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jImport.setText(bundle.getString("label.csvimpostbtn")); // NOI18N
        jImport.setEnabled(false);
        jImport.setPreferredSize(new java.awt.Dimension(110, 45));
        jImport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jImportActionPerformed(evt);
            }
        });

        jLabel6.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel6.setText(bundle.getString("label.proddefaultcategory")); // NOI18N
        jLabel6.setPreferredSize(new java.awt.Dimension(100, 30));

        jbtnReset.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jbtnReset.setText(bundle.getString("button.reset")); // NOI18N
        jbtnReset.setPreferredSize(new java.awt.Dimension(110, 45));
        jbtnReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbtnResetActionPerformed(evt);
            }
        });

        jComboSupplier.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jComboSupplier.setEnabled(false);
        jComboSupplier.setMinimumSize(new java.awt.Dimension(32, 25));
        jComboSupplier.setName(""); // NOI18N
        jComboSupplier.setPreferredSize(new java.awt.Dimension(300, 30));
        jComboSupplier.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboSupplierItemStateChanged(evt);
            }
        });
        jComboSupplier.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jComboSupplierFocusGained(evt);
            }
        });
        jComboSupplier.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboSupplierActionPerformed(evt);
            }
        });

        jLabel21.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel21.setText(bundle.getString("label.suppliername")); // NOI18N
        jLabel21.setPreferredSize(new java.awt.Dimension(100, 30));

        jLabel22.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel22.setText(bundle.getString("label.proddefaultsupplier")); // NOI18N
        jLabel22.setPreferredSize(new java.awt.Dimension(100, 30));

        jComboDefaultSupplier.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jComboDefaultSupplier.setEnabled(false);
        jComboDefaultSupplier.setMinimumSize(new java.awt.Dimension(32, 25));
        jComboDefaultSupplier.setPreferredSize(new java.awt.Dimension(300, 30));
        jComboDefaultSupplier.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboDefaultSupplierItemStateChanged(evt);
            }
        });
        jComboDefaultSupplier.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboDefaultSupplierActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jCheckInCatalogue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(59, 59, 59)
                                .addComponent(jCheckSellIncTax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addComponent(jbtnReset, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jImport, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(93, 93, 93))))
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jComboTax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jComboBarcode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jComboReference, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jComboName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jComboBuy, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel20, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jComboSell, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboDefaultCategory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboCategory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel21, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboSupplier, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel22, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboDefaultSupplier, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(2, 2, 2)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboReference, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBarcode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBuy, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboSell, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel20, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboTax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel11, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboCategory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboDefaultCategory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel21, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboSupplier, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboDefaultSupplier, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel22, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCheckSellIncTax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jCheckInCatalogue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jImport, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jbtnReset, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel17.setFont(new java.awt.Font("Arial", 0, 10)); // NOI18N
        jLabel17.setText("....\n");

        jLabel18.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel18.setText(bundle.getString("label.csvdelimit")); // NOI18N
        jLabel18.setPreferredSize(new java.awt.Dimension(100, 30));

        jHeaderRead.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jHeaderRead.setText(bundle.getString("label.csvread")); // NOI18N
        jHeaderRead.setEnabled(false);
        jHeaderRead.setPreferredSize(new java.awt.Dimension(110, 45));
        jHeaderRead.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jHeaderReadActionPerformed(evt);
            }
        });

        jPanelStatus.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.LineBorder(new java.awt.Color(153, 153, 153), 1, true), bundle.getString("title.CSVImport"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Arial", 1, 14), new java.awt.Color(102, 102, 102))); // NOI18N

        jLabel9.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel9.setText(bundle.getString("label.csvrecordsfound")); // NOI18N
        jLabel9.setPreferredSize(new java.awt.Dimension(150, 30));

        jLabel14.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel14.setText(bundle.getString("label.csvnewproducts")); // NOI18N
        jLabel14.setMaximumSize(new java.awt.Dimension(77, 14));
        jLabel14.setMinimumSize(new java.awt.Dimension(77, 14));
        jLabel14.setPreferredSize(new java.awt.Dimension(150, 30));

        jLabel16.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel16.setText(bundle.getString("label.csvchanged")); // NOI18N
        jLabel16.setPreferredSize(new java.awt.Dimension(150, 30));

        jTextUpdates.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jTextUpdates.setText(bundle.getString("label.csvpriceupdated")); // NOI18N
        jTextUpdates.setPreferredSize(new java.awt.Dimension(150, 30));

        jLabel2.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel2.setText(bundle.getString("label.csvmissing")); // NOI18N
        jLabel2.setPreferredSize(new java.awt.Dimension(150, 30));

        jLabel15.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel15.setText(bundle.getString("label.csvbad")); // NOI18N
        jLabel15.setPreferredSize(new java.awt.Dimension(150, 30));

        jLabel13.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel13.setText(bundle.getString("label.csvnotchanged")); // NOI18N
        jLabel13.setPreferredSize(new java.awt.Dimension(150, 30));

        jTextRecords.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jTextRecords.setForeground(new java.awt.Color(102, 102, 102));
        jTextRecords.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextRecords.setBorder(null);
        jTextRecords.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        jTextRecords.setEnabled(false);
        jTextRecords.setPreferredSize(new java.awt.Dimension(100, 30));

        jTextNew.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jTextNew.setForeground(new java.awt.Color(102, 102, 102));
        jTextNew.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextNew.setBorder(null);
        jTextNew.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        jTextNew.setEnabled(false);
        jTextNew.setPreferredSize(new java.awt.Dimension(100, 30));

        jTextInvalid.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jTextInvalid.setForeground(new java.awt.Color(102, 102, 102));
        jTextInvalid.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextInvalid.setBorder(null);
        jTextInvalid.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        jTextInvalid.setEnabled(false);
        jTextInvalid.setPreferredSize(new java.awt.Dimension(100, 30));

        jTextUpdate.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jTextUpdate.setForeground(new java.awt.Color(102, 102, 102));
        jTextUpdate.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextUpdate.setBorder(null);
        jTextUpdate.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        jTextUpdate.setEnabled(false);
        jTextUpdate.setPreferredSize(new java.awt.Dimension(100, 30));

        jTextMissing.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jTextMissing.setForeground(new java.awt.Color(102, 102, 102));
        jTextMissing.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextMissing.setBorder(null);
        jTextMissing.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        jTextMissing.setEnabled(false);
        jTextMissing.setPreferredSize(new java.awt.Dimension(100, 30));

        jTextBadPrice.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jTextBadPrice.setForeground(new java.awt.Color(255, 0, 204));
        jTextBadPrice.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextBadPrice.setBorder(null);
        jTextBadPrice.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        jTextBadPrice.setEnabled(false);
        jTextBadPrice.setPreferredSize(new java.awt.Dimension(100, 30));

        jTextNoChange.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jTextNoChange.setForeground(new java.awt.Color(102, 102, 102));
        jTextNoChange.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextNoChange.setBorder(null);
        jTextNoChange.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        jTextNoChange.setEnabled(false);
        jTextNoChange.setPreferredSize(new java.awt.Dimension(100, 30));

        jLabel19.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel19.setText(bundle.getString("label.csvbadcats")); // NOI18N
        jLabel19.setPreferredSize(new java.awt.Dimension(150, 30));

        jTextBadCats.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jTextBadCats.setForeground(new java.awt.Color(255, 0, 204));
        jTextBadCats.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextBadCats.setBorder(null);
        jTextBadCats.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        jTextBadCats.setEnabled(false);
        jTextBadCats.setPreferredSize(new java.awt.Dimension(100, 30));

        javax.swing.GroupLayout jPanelStatusLayout = new javax.swing.GroupLayout(jPanelStatus);
        jPanelStatus.setLayout(jPanelStatusLayout);
        jPanelStatusLayout.setHorizontalGroup(
            jPanelStatusLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelStatusLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelStatusLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelStatusLayout.createSequentialGroup()
                        .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextRecords, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanelStatusLayout.createSequentialGroup()
                        .addGroup(jPanelStatusLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jLabel19, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel13, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel16, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextUpdates, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel15, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanelStatusLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextBadCats, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextNew, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextInvalid, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextUpdate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextMissing, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextBadPrice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextNoChange, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        jPanelStatusLayout.setVerticalGroup(
            jPanelStatusLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelStatusLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(jPanelStatusLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextRecords, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelStatusLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextNew, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelStatusLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextInvalid, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelStatusLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextUpdates, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextUpdate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelStatusLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextMissing, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelStatusLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextBadPrice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelStatusLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextNoChange, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelStatusLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel19, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextBadCats, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jComboSeparator.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jComboSeparator.setPreferredSize(new java.awt.Dimension(50, 45));

        importProgressBar.setFont(new java.awt.Font("Arial", 0, 13)); // NOI18N
        importProgressBar.setPreferredSize(new java.awt.Dimension(240, 45));

        jLblImportNotice.setBackground(new java.awt.Color(255, 255, 255));
        jLblImportNotice.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLblImportNotice.setForeground(new java.awt.Color(102, 102, 102));
        jLblImportNotice.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLblImportNotice.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/refundit.png"))); // NOI18N
        jLblImportNotice.setText(bundle.getString("label.importnotice")); // NOI18N
        jLblImportNotice.setOpaque(true);
        jLblImportNotice.setPreferredSize(new java.awt.Dimension(150, 37));

        jCBiso.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jCBiso.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "ISO-8859-1", "ISO-8859-2", "ISO-8859-3", "ISO-8859-4", "ISO-8859-5", "ISO-8859-6", "ISO-8859-7", "ISO-8859-8", "ISO-8859-9", "ISO-8859-10", "UTF-8", "UTF-16", " " }));
        jCBiso.setToolTipText(bundle.getString("tooltip.import.cbiso")); // NOI18N
        jCBiso.setPreferredSize(new java.awt.Dimension(125, 45));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jLabel17))
                    .addComponent(jFileChooserPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jComboSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jCBiso, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(12, 12, 12)
                                .addComponent(jHeaderRead, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jPanelStatus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(importProgressBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLblImportNotice, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel17)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jFileChooserPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCBiso, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jComboSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 46, Short.MAX_VALUE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jHeaderRead, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(importProgressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanelStatus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLblImportNotice, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 461, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(78, 78, 78))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jHeaderReadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jHeaderReadActionPerformed
        try {
            GetheadersFromFile(jFileName.getText());
            importProgressBar.setString("Source file Header OK");
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, null, ex);
            importProgressBar.setString("Source file Header error!");
        }
    }//GEN-LAST:event_jHeaderReadActionPerformed

    private void jImportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jImportActionPerformed

        jImport.setEnabled(false);

        workProcess work = new workProcess();
        Thread thread2 = new Thread(work);
        thread2.start();

    }//GEN-LAST:event_jImportActionPerformed

    private void jFileNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jFileNameActionPerformed
        jImport.setEnabled(false);
        jHeaderRead.setEnabled(true);
    }//GEN-LAST:event_jFileNameActionPerformed

    private void jbtnFileChooseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbtnFileChooseActionPerformed
        resetFields();
        setWorker();

        JFileChooser chooser = new JFileChooser(last_folder == null ? "C:\\" : last_folder);
        FileNameExtensionFilter filter = new FileNameExtensionFilter("csv files", "csv");
        chooser.setFileFilter(filter);
        chooser.showOpenDialog(null);
        File csvFile = chooser.getSelectedFile();

        if (csvFile == null) {
            return;
        }

        File current_folder = chooser.getCurrentDirectory();

        if (last_folder == null || !last_folder.equals(current_folder.getAbsolutePath())) {
            AppConfig CSVConfig = new AppConfig(config_file);
            CSVConfig.load();
            CSVConfig.setProperty("CSV.last_folder", current_folder.getAbsolutePath());
            last_folder = current_folder.getAbsolutePath();
            try {
                CSVConfig.save();
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
        }

        String csv = csvFile.getName();
        if (!(csv.trim().equals(""))) {
            csvFileName = csvFile.getAbsolutePath();
            jFileName.setText(csvFileName);
        }
    }//GEN-LAST:event_jbtnFileChooseActionPerformed

    private void jComboCategoryFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jComboCategoryFocusGained
        jComboCategory.removeAllItems();
        int i = 1;
        jComboCategory.addItem("");
        while (i < Headers.size()) {
            if ((Headers.get(i) != jComboBarcode.getSelectedItem())
                    & (Headers.get(i) != jComboReference.getSelectedItem())
                    & (Headers.get(i) != jComboName.getSelectedItem())
                    & (Headers.get(i) != jComboBuy.getSelectedItem())
                    & (Headers.get(i) != jComboSell.getSelectedItem())
                    & (Headers.get(i) != jComboTax.getSelectedItem())
                    & (Headers.get(i) != jComboSupplier.getSelectedItem())) {
                jComboCategory.addItem(Headers.get(i));
            }
            ++i;
        }
        jComboCategory.addItem(category_default);
    }//GEN-LAST:event_jComboCategoryFocusGained

    private void jComboCategoryItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboCategoryItemStateChanged

        try {
            if (jComboCategory.getSelectedItem() == "[ USE DEFAULT CATEGORY ]") {
                m_CategoryModel = new ComboBoxValModel(m_sentcat.list());
                jComboDefaultCategory.setModel(m_CategoryModel);
            } else {
                m_CategoryModel = new ComboBoxValModel(m_sentcat.list());
                m_CategoryModel.add(reject_bad_category);
                jComboDefaultCategory.setModel(m_CategoryModel);
            }
        } catch (BasicException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        }
        checkFieldMapping();
    }//GEN-LAST:event_jComboCategoryItemStateChanged

    private void jComboDefaultCategoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboDefaultCategoryActionPerformed
        checkFieldMapping();
    }//GEN-LAST:event_jComboDefaultCategoryActionPerformed

    private void jComboDefaultCategoryItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboDefaultCategoryItemStateChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboDefaultCategoryItemStateChanged

    private void jComboSellFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jComboSellFocusGained
        jComboSell.removeAllItems();
        int i = 1;
        jComboSell.addItem("");
        while (i < Headers.size()) {
            if ((Headers.get(i) != jComboCategory.getSelectedItem())
                    & (Headers.get(i) != jComboReference.getSelectedItem())
                    & (Headers.get(i) != jComboName.getSelectedItem())
                    & (Headers.get(i) != jComboBarcode.getSelectedItem())
                    & (Headers.get(i) != jComboBuy.getSelectedItem())
                    & (Headers.get(i) != jComboTax.getSelectedItem())
                    & (Headers.get(i) != jComboSupplier.getSelectedItem())) {

                jComboSell.addItem(Headers.get(i));
            }
            ++i;
        }
    }//GEN-LAST:event_jComboSellFocusGained

    private void jComboSellItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboSellItemStateChanged
        checkFieldMapping();
    }//GEN-LAST:event_jComboSellItemStateChanged

    private void jComboBuyFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jComboBuyFocusGained
        jComboBuy.removeAllItems();
        int i = 1;
        jComboBuy.addItem("");
        while (i < Headers.size()) {
            if ((Headers.get(i) != jComboCategory.getSelectedItem())
                    & (Headers.get(i) != jComboReference.getSelectedItem())
                    & (Headers.get(i) != jComboName.getSelectedItem())
                    & (Headers.get(i) != jComboBarcode.getSelectedItem())
                    & (Headers.get(i) != jComboSell.getSelectedItem())
                    & (Headers.get(i) != jComboTax.getSelectedItem())
                    & (Headers.get(i) != jComboSupplier.getSelectedItem())) {
                jComboBuy.addItem(Headers.get(i));
            }
            ++i;
        }
    }//GEN-LAST:event_jComboBuyFocusGained

    private void jComboBuyItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBuyItemStateChanged
        checkFieldMapping();
    }//GEN-LAST:event_jComboBuyItemStateChanged

    private void jComboNameFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jComboNameFocusGained
        jComboName.removeAllItems();
        int i = 1;
        jComboName.addItem("");
        while (i < Headers.size()) {
            if ((Headers.get(i) != jComboCategory.getSelectedItem())
                    & (Headers.get(i) != jComboReference.getSelectedItem())
                    & (Headers.get(i) != jComboBarcode.getSelectedItem())
                    & (Headers.get(i) != jComboBuy.getSelectedItem())
                    & (Headers.get(i) != jComboSell.getSelectedItem())
                    & (Headers.get(i) != jComboTax.getSelectedItem())
                    & (Headers.get(i) != jComboSupplier.getSelectedItem())) {
                jComboName.addItem(Headers.get(i));
            }
            ++i;
        }
    }//GEN-LAST:event_jComboNameFocusGained

    private void jComboNameItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboNameItemStateChanged
        checkFieldMapping();
    }//GEN-LAST:event_jComboNameItemStateChanged

    private void jComboBarcodeFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jComboBarcodeFocusGained
        jComboBarcode.removeAllItems();
        int i = 1;
        jComboBarcode.addItem("");
        while (i < Headers.size()) {
            if ((Headers.get(i) != jComboCategory.getSelectedItem())
                    & (Headers.get(i) != jComboReference.getSelectedItem())
                    & (Headers.get(i) != jComboName.getSelectedItem())
                    & (Headers.get(i) != jComboBuy.getSelectedItem())
                    & (Headers.get(i) != jComboSell.getSelectedItem())
                    & (Headers.get(i) != jComboTax.getSelectedItem())
                    & (Headers.get(i) != jComboSupplier.getSelectedItem())) {
                jComboBarcode.addItem(Headers.get(i));
            }
            ++i;
        }
    }//GEN-LAST:event_jComboBarcodeFocusGained

    private void jComboBarcodeItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBarcodeItemStateChanged
        checkFieldMapping();
    }//GEN-LAST:event_jComboBarcodeItemStateChanged

    private void jComboReferenceFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jComboReferenceFocusGained
        jComboReference.removeAllItems();
        int i = 1;
        jComboReference.addItem("");
        while (i < Headers.size()) {
            if ((Headers.get(i) != jComboCategory.getSelectedItem())
                    & (Headers.get(i) != jComboBarcode.getSelectedItem())
                    & (Headers.get(i) != jComboName.getSelectedItem())
                    & (Headers.get(i) != jComboBuy.getSelectedItem())
                    & (Headers.get(i) != jComboSell.getSelectedItem())
                    & (Headers.get(i) != jComboTax.getSelectedItem())
                    & (Headers.get(i) != jComboSupplier.getSelectedItem())) {
                jComboReference.addItem(Headers.get(i));
            }
            ++i;
        }
    }//GEN-LAST:event_jComboReferenceFocusGained

    private void jComboReferenceItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboReferenceItemStateChanged
        checkFieldMapping();
    }//GEN-LAST:event_jComboReferenceItemStateChanged

    private void jbtnResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbtnResetActionPerformed
        resetFields();
        progress = -1;
        importProgressBar.setString("Waiting...");
    }//GEN-LAST:event_jbtnResetActionPerformed

    private void jComboTaxFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jComboTaxFocusGained
        jComboTax.removeAllItems();
        int i = 1;
        jComboTax.addItem("");
        while (i < Headers.size()) {
            if ((Headers.get(i) != jComboCategory.getSelectedItem())
                    & (Headers.get(i) != jComboReference.getSelectedItem())
                    & (Headers.get(i) != jComboBarcode.getSelectedItem())
                    & (Headers.get(i) != jComboName.getSelectedItem())
                    & (Headers.get(i) != jComboBuy.getSelectedItem())
                    & (Headers.get(i) != jComboSell.getSelectedItem())
                    & (Headers.get(i) != jComboSupplier.getSelectedItem())) {
                jComboTax.addItem(Headers.get(i));
            }
            ++i;
        }
    }//GEN-LAST:event_jComboTaxFocusGained

    private void jComboTaxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboTaxItemStateChanged
        checkFieldMapping();
    }//GEN-LAST:event_jComboTaxItemStateChanged

    private void jComboSupplierItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboSupplierItemStateChanged

        try {
            if (jComboSupplier.getSelectedItem() == "[ USE DEFAULT SUPPLIER ]") {
                m_SupplierModel = new ComboBoxValModel(m_sentsupp.list());
                jComboDefaultSupplier.setModel(m_SupplierModel);
            } else {
                m_SupplierModel = new ComboBoxValModel(m_sentsupp.list());
                m_SupplierModel.add(reject_bad_supplier);
                jComboDefaultSupplier.setModel(m_SupplierModel);
            }
        } catch (BasicException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        }
        checkFieldMapping();
    }//GEN-LAST:event_jComboSupplierItemStateChanged

    private void jComboSupplierFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jComboSupplierFocusGained
        jComboSupplier.removeAllItems();
        int i = 1;
        jComboSupplier.addItem("");
        while (i < Headers.size()) {
            if ((Headers.get(i) != jComboCategory.getSelectedItem())
                    & (Headers.get(i) != jComboReference.getSelectedItem())
                    & (Headers.get(i) != jComboBarcode.getSelectedItem())
                    & (Headers.get(i) != jComboName.getSelectedItem())
                    & (Headers.get(i) != jComboBuy.getSelectedItem())
                    & (Headers.get(i) != jComboSell.getSelectedItem())
                    & (Headers.get(i) != jComboTax.getSelectedItem())) {

                jComboSupplier.addItem(Headers.get(i));
            }
            ++i;
        }
    }//GEN-LAST:event_jComboSupplierFocusGained

    private void jComboDefaultSupplierItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboDefaultSupplierItemStateChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboDefaultSupplierItemStateChanged

    private void jComboDefaultSupplierActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboDefaultSupplierActionPerformed
        checkFieldMapping();
    }//GEN-LAST:event_jComboDefaultSupplierActionPerformed

    private void jComboSupplierActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboSupplierActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboSupplierActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JProgressBar importProgressBar;
    private javax.swing.JComboBox<String> jCBiso;
    private javax.swing.JCheckBox jCheckInCatalogue;
    private javax.swing.JCheckBox jCheckSellIncTax;
    private javax.swing.JComboBox jComboBarcode;
    private javax.swing.JComboBox jComboBuy;
    private javax.swing.JComboBox jComboCategory;
    private javax.swing.JComboBox jComboDefaultCategory;
    private javax.swing.JComboBox jComboDefaultSupplier;
    private javax.swing.JComboBox jComboName;
    private javax.swing.JComboBox jComboReference;
    private javax.swing.JComboBox jComboSell;
    private javax.swing.JComboBox jComboSeparator;
    private javax.swing.JComboBox jComboSupplier;
    private javax.swing.JComboBox jComboTax;
    private javax.swing.JPanel jFileChooserPanel;
    private javax.swing.JTextField jFileName;
    private javax.swing.JButton jHeaderRead;
    private javax.swing.JButton jImport;
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
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLblImportNotice;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanelStatus;
    private javax.swing.JTextField jTextBadCats;
    private javax.swing.JTextField jTextBadPrice;
    private javax.swing.JTextField jTextInvalid;
    private javax.swing.JTextField jTextMissing;
    private javax.swing.JTextField jTextNew;
    private javax.swing.JTextField jTextNoChange;
    private javax.swing.JTextField jTextRecords;
    private javax.swing.JTextField jTextUpdate;
    private javax.swing.JLabel jTextUpdates;
    private javax.swing.JButton jbtnFileChoose;
    private javax.swing.JButton jbtnReset;
    // End of variables declaration//GEN-END:variables
}
