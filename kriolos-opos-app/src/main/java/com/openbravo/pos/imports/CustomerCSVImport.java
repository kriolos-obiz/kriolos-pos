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
import com.openbravo.data.loader.Session;
import com.openbravo.data.user.DefaultSaveProvider;
import com.openbravo.data.user.SaveProvider;
import com.openbravo.pos.customers.CustomerInfoExt;
import com.openbravo.pos.customers.DataLogicCustomers;
import com.openbravo.pos.forms.*;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * GUI and code for importing Customer from a CSV file 
 * 
 * Allow to add or update many customers quickly and easily.
 */
public class CustomerCSVImport extends JPanel implements JPanelView {

    private static final Logger LOGGER = Logger.getLogger(CustomerCSVImport.class.getName());
    private ArrayList<String> Headers = new ArrayList<>();
    private Session dbSession;
    private String csvFileName;

    private String csvMessage = "";
    private CsvReader customers;
    private int currentRecord;
    private int rowCount = 0;
    private String last_folder;
    private File config_file;

    private DataLogicSales m_dlSales;
    private DataLogicSystem m_dlSystem;
    private DataLogicCustomers m_dlCustomer;

    protected SaveProvider spr;

    private String customerSearchKey;
    private String customerAccount;
    private String customerName;
    private String customerAddress1;
    private String customerAddress2;
    private String customerPostal;
    private String customerCity;
    private String customerRegion;
    private String customerFirstName;
    private String customerLastName;
    private String customerEmail;
    private String customerPhone;
    private String customerPhone2;

    private DocumentListener documentListener;
    private CustomerInfoExt custInfo;
    private String recordType = null;

    private int newRecords = 0;
    private int invalid = 0;
    private int updated = 0;
    private int missing = 0;
    private int noChange = 0;
    private int bad = 0;

    private Integer progress = 0;

    private String iso = "ISO-8859-1";

    /**
     * Constructs a new CustomerCSVImport object
     *
     * @param oApp AppView
     */
    public CustomerCSVImport(AppView oApp) {
        initComponents();

        dbSession = oApp.getSession();
        
        AppProperties props = oApp.getProperties();

        m_dlSales = new DataLogicSales();
        m_dlSales.init(dbSession);

        m_dlSystem = new DataLogicSystem();
        m_dlSystem.init(dbSession);

        spr = new DefaultSaveProvider(
                m_dlSales.getCustomerUpdate(),
                m_dlSales.getCustomerInsert(),
                m_dlSales.getCustomerDelete());

        last_folder = props.getProperty("CSV.last_folder");
        config_file = props.getConfigFile();

        jFileName.getDocument().addDocumentListener(documentListener);

        documentListener = new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent documentEvent) {
                jbtnRead.setEnabled(true);
            }

            @Override
            public void insertUpdate(DocumentEvent documentEvent) {
                if (!"".equals(jFileName.getText().trim())) {
                    jbtnRead.setEnabled(true);
                }
            }

            @Override
            public void removeUpdate(DocumentEvent documentEvent) {
                if (jFileName.getText().trim().equals("")) {
                    jbtnRead.setEnabled(false);
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
//            customers = new CsvReader(CSVFileName, ',' ,Charset.forName("UTF-8"));
            customers = new CsvReader(CSVFileName, ',', Charset.forName(jCBiso.getSelectedItem().toString()));
            customers.setDelimiter(((String) jComboSeparator.getSelectedItem()).charAt(0));
            customers.readHeaders();

            if (customers.getHeaderCount() < 5) {
                JOptionPane.showMessageDialog(this,
                        "Incorrect header in your source file",
                        "Header Error",
                        JOptionPane.WARNING_MESSAGE);
                customers.close();
                return;
            }
            rowCount = 0;
            int i = 0;
            Headers.clear();
            Headers.add("");
            jComboName.addItem("");
            jComboAccountID.addItem("");
            jComboSearchKey.addItem("");
            jComboAddress1.addItem("");
            jComboAddress2.addItem("");
            jComboPostal.addItem("");
            jComboCity.addItem("");
            jComboRegion.addItem("");
            jComboFirstName.addItem("");
            jComboLastName.addItem("");
            jComboEmail.addItem("");
            jComboPhone.addItem("");
            jComboPhone2.addItem("");

            while (i < customers.getHeaderCount()) {
                jComboName.addItem(customers.getHeader(i));
                jComboAccountID.addItem(customers.getHeader(i));
                jComboSearchKey.addItem(customers.getHeader(i));
                jComboAddress1.addItem(customers.getHeader(i));
                jComboAddress2.addItem(customers.getHeader(i));
                jComboPostal.addItem(customers.getHeader(i));
                jComboCity.addItem(customers.getHeader(i));
                jComboRegion.addItem(customers.getHeader(i));
                jComboFirstName.addItem(customers.getHeader(i));
                jComboLastName.addItem(customers.getHeader(i));
                jComboEmail.addItem(customers.getHeader(i));
                jComboPhone.addItem(customers.getHeader(i));
                jComboPhone2.addItem(customers.getHeader(i));

                Headers.add(customers.getHeader(i));
                ++i;
            }

            enableCheckBoxes();

            while (customers.readRecord()) {
                ++rowCount;
            }

            jtxtRecords.setText(Long.toString(rowCount));

            customers.close();

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
     */
    private void enableCheckBoxes() {
        jbtnRead.setEnabled(false);
        jbtnImport.setEnabled(false);
        jbtnReset.setEnabled(true);
        jComboAccountID.setEnabled(true);
        jComboName.setEnabled(true);
        jComboSearchKey.setEnabled(true);
        jComboAddress1.setEnabled(true);
        jComboAddress2.setEnabled(true);
        jComboPostal.setEnabled(true);
        jComboCity.setEnabled(true);
        jComboRegion.setEnabled(true);
        jComboFirstName.setEnabled(true);
        jComboLastName.setEnabled(true);
        jComboEmail.setEnabled(true);
        jComboPhone.setEnabled(true);
        jComboPhone2.setEnabled(true);

        jCheckVisible.setEnabled(true);

    }

    /**
     * Pushes the Import process into a new thread so it doesn't interfere with
     * the UI responsiveness.
     */
    private void setWorker() {
        progress = 0;
        webPBar.setStringPainted(true);

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
                webPBar.setValue(chunks.get(0));
                if (progress > 100) {
                    progress = 100;
                    webPBar.setString("Imported 100%");
                } else {
                    webPBar.setString("Imported " + progress + "%");
                }
            }
        };
        pbWorker.execute();
    }

    /**
     * Runs the setWorker.
     */
    private class workProcess implements Runnable {

        @Override
        public void run() {
            try {
                ImportCsvFile(jFileName.getText());
            } catch (IOException | BasicException ex) {
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
    private void ImportCsvFile(String CSVFileName) throws IOException, BasicException {

        File f = new File(CSVFileName);
        if (f.exists()) {
            webPBar.setString("Starting...");
            webPBar.setVisible(true);
            jbtnImport.setEnabled(false);

// Read file
//            customers = new CsvReader(CSVFileName, ',' ,Charset.forName("UTF-8"));
            customers = new CsvReader(CSVFileName, ',', Charset.forName(jCBiso.getSelectedItem().toString()));
            customers.setDelimiter(((String) jComboSeparator.getSelectedItem()).charAt(0));
            customers.readHeaders();

            currentRecord = 0;

            while (customers.readRecord()) {
                customerSearchKey = customers.get((String) jComboSearchKey.getSelectedItem());
                customerAccount = customers.get((String) jComboAccountID.getSelectedItem());
                customerName = customers.get((String) jComboName.getSelectedItem());
                customerAddress1 = customers.get((String) jComboAddress1.getSelectedItem());
                customerAddress2 = customers.get((String) jComboAddress2.getSelectedItem());
                customerPostal = customers.get((String) jComboPostal.getSelectedItem());
                customerCity = customers.get((String) jComboCity.getSelectedItem());
                customerRegion = customers.get((String) jComboRegion.getSelectedItem());
                customerFirstName = customers.get((String) jComboFirstName.getSelectedItem());
                customerLastName = customers.get((String) jComboLastName.getSelectedItem());
                customerEmail = customers.get((String) jComboEmail.getSelectedItem());
                customerPhone = customers.get((String) jComboPhone.getSelectedItem());
                customerPhone2 = customers.get((String) jComboPhone2.getSelectedItem());

                currentRecord++;
                progress = currentRecord;

                if ("".equals(customerSearchKey)
                        | "".equals(customerName)) {

                    createCustomerCSVEntry(csvMessage, null, null);
                } else {

                    recordType = getRecord();
                    switch (recordType) {
                        case "new":
                            createCustomer("new");
                            newRecords++;
                            createCustomerCSVEntry("New Customer", null, null);
                            break;
                        case "name error":
                        case "searchkey error":
                        case "Duplicate searchkey found.":
                        case "Duplicate name found.":
                        case "Exception":
                            invalid++;
                            createCustomerCSVEntry(recordType, null, null);
                            break;
                        default:
                            updateRecord(recordType);
                            break;
                    }
                }
            }

        } else {
            JOptionPane.showMessageDialog(this,
                    "Unable to locate "
                    + CSVFileName, "File not found",
                    JOptionPane.WARNING_MESSAGE);
        }

        jtxtNew.setText(Integer.toString(newRecords));
        jtxtUpdate.setText(Integer.toString(updated));
        jtxtInvalid.setText(Integer.toString(invalid));
        jtxtMissing.setText(Integer.toString(missing));
        jtxtNoChange.setText(Integer.toString(noChange));
        jtxtBad.setText(Integer.toString(bad));

        JOptionPane.showMessageDialog(this,
                "Import Complete",
                "Imported",
                JOptionPane.WARNING_MESSAGE);

//            webPBar.setString("Imported " + progress);                
        progress = 100;
        webPBar.setValue(progress);
        webPBar.setString("Imported" + progress);
//        }
    }

    /**
     * testString for validity as a number
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
     * Updated the record in the database with the new prices and category if
     * needed.
     *
     * @param cID Unique Customer id of the record to be updated It then creates
     * an updated record for the Customer, subject to the ???? be different
     */
    private void updateRecord(String cID) throws BasicException {
//        custInfo = new CustomerInfoExt();
        custInfo = m_dlSales.getCustomerInfo(cID);
        createCustomer("update");
        noChange++;
    }

    /**
     * Gets the title of the current panel
     *
     * @return The name of the panel
     */
    @Override
    public String getTitle() {
        return AppLocal.getIntString("Menu.CustomerCSVImport");
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
     * Set CSV field and line separator
     *
     * @throws com.openbravo.basic.BasicException
     */
    @Override
    public void activate() throws BasicException {

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

        jComboAccountID.removeAllItems();
        jComboAccountID.setEnabled(false);

        jComboName.removeAllItems();
        jComboName.setEnabled(false);

        jComboSearchKey.removeAllItems();
        jComboSearchKey.setEnabled(false);

        jComboAddress1.removeAllItems();
        jComboAddress1.setEnabled(false);

        jComboAddress2.removeAllItems();
        jComboAddress2.setEnabled(false);

        jComboCity.removeAllItems();
        jComboCity.setEnabled(false);

        jComboRegion.removeAllItems();
        jComboRegion.setEnabled(false);

        jComboPostal.removeAllItems();
        jComboPostal.setEnabled(false);

        jComboFirstName.removeAllItems();
        jComboFirstName.setEnabled(false);

        jComboLastName.removeAllItems();
        jComboLastName.setEnabled(false);

        jComboEmail.removeAllItems();
        jComboEmail.setEnabled(false);

        jComboPhone.removeAllItems();
        jComboPhone.setEnabled(false);

        jComboPhone2.removeAllItems();
        jComboPhone2.setEnabled(false);

        jCheckVisible.setSelected(false);
        jCheckVisible.setEnabled(true);

        jbtnImport.setEnabled(false);
        jbtnReset.setEnabled(true);
        jbtnRead.setEnabled(false);

        jFileName.setText(null);
        csvFileName = "";

// clear Status area fields        
        jtxtNew.setText("");
        jtxtUpdate.setText("");
        jtxtInvalid.setText("");
        jtxtMissing.setText("");
        jtxtNoChange.setText("");
        jtxtRecords.setText("");
        jtxtBad.setText("");

        progress = 0;

        Headers.clear();
    }

    /**
     * Checks the field mappings to ensure all compulsory fields have been
     * completed to allow import to proceed
     */
    public void checkFieldMapping() {
        if (jComboSearchKey.getSelectedItem() != ""
                & jComboName.getSelectedItem() != "") {
            jbtnImport.setEnabled(true);
            jbtnReset.setEnabled(true);
        } else {
            jbtnImport.setEnabled(false);
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
     * @param cType
     */
    public void createCustomer(String cType) {

        Object[] mycust = new Object[27];
        if ("new".equals(cType)) {
            mycust[0] = UUID.randomUUID().toString();
        } else {
            mycust[0] = custInfo.getId();
        }                                                                       // id string
        mycust[1] = customerSearchKey;                                          // searchkey String
        mycust[2] = customerAccount;                                            // taxid string
        mycust[3] = customerName;                                               // name string
        mycust[4] = null;                                                       // taxCategory string
        mycust[5] = null;                                                       // card string
        mycust[6] = 0.0;                                                        // maxdebt Double
        mycust[7] = customerAddress1;                                           // address1 string
        mycust[8] = customerAddress2;                                           // address2 string
        mycust[9] = customerPostal;                                             // postal string
        mycust[10] = customerCity;                                              // city string
        mycust[11] = customerRegion;                                            // region string
        mycust[12] = null;                                                      // country string
        mycust[13] = customerFirstName;                                         // firstname string
        mycust[14] = customerLastName;                                          // lastname string
        mycust[15] = customerEmail;                                             // email string
        mycust[16] = customerPhone;                                             // phone string
        mycust[17] = customerPhone2;                                            // phone2 string
        mycust[18] = null;                                                      // fax string
        mycust[19] = null;                                                      // notes string
        mycust[20] = jCheckVisible.isSelected();                                // visible flag
        mycust[21] = null;                                                      // curdate string
        mycust[22] = 0.0;                                                       // curdate double
        mycust[23] = null;                                                      // image
        mycust[24] = false;                                                     // isvip
        mycust[25] = 0.0;                                                       // discount
        mycust[26] = null;                                                      // memodate date

        try {
            if ("new".equals(cType)) {
                spr.insertData(mycust);
                webPBar.setString("Adding record " + progress);
            } else {
                spr.updateData(mycust);
                webPBar.setString("Updating record " + progress);
            }
        } catch (BasicException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        }
    }

    /**
     * Insert file row read and adds to import table log
     *
     * @param csvError
     * @param searchKey
     * @param Name
     */
    public void createCustomerCSVEntry(String csvError, String searchKey, String Name) {

        Object[] mycust = new Object[5];
        mycust[0] = UUID.randomUUID().toString();                               // ID string
        mycust[1] = Integer.toString(currentRecord);                            // Record number
        mycust[2] = csvError;                                                   // Error description
        mycust[3] = customerSearchKey;                                          // SearchKey String
        mycust[4] = customerName;                                               // Name string

        try {
            m_dlSystem.execCustomerAddCSVEntry(mycust);
        } catch (BasicException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        }
    }

    /**
     * @return
     */
    public String getRecord() {
        // Get record type using using DataLogicSystem
        Object[] mycust = new Object[2];
        mycust[0] = customerSearchKey;
        mycust[1] = customerName;

        try {
            return (m_dlSystem.getCustomerRecordType(mycust));
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
        jLabel18 = new javax.swing.JLabel();
        jbtnRead = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jlblRecords = new javax.swing.JLabel();
        jlblNew = new javax.swing.JLabel();
        jlblInvalid = new javax.swing.JLabel();
        jlblUpdates = new javax.swing.JLabel();
        jlblMissing = new javax.swing.JLabel();
        jlblBad = new javax.swing.JLabel();
        jlblNotChanged = new javax.swing.JLabel();
        jtxtRecords = new javax.swing.JTextField();
        jtxtNew = new javax.swing.JTextField();
        jtxtInvalid = new javax.swing.JTextField();
        jtxtUpdate = new javax.swing.JTextField();
        jtxtMissing = new javax.swing.JTextField();
        jtxtBad = new javax.swing.JTextField();
        jtxtNoChange = new javax.swing.JTextField();
        jComboSeparator = new javax.swing.JComboBox();
        webPBar = new javax.swing.JProgressBar();
        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel1 = new javax.swing.JPanel();
        jComboSearchKey = new javax.swing.JComboBox();
        jComboName = new javax.swing.JComboBox();
        jComboAccountID = new javax.swing.JComboBox();
        jComboAddress1 = new javax.swing.JComboBox();
        jComboAddress2 = new javax.swing.JComboBox();
        jComboCity = new javax.swing.JComboBox();
        jComboRegion = new javax.swing.JComboBox();
        jComboPostal = new javax.swing.JComboBox();
        jComboFirstName = new javax.swing.JComboBox();
        jComboLastName = new javax.swing.JComboBox();
        jComboEmail = new javax.swing.JComboBox();
        jComboPhone = new javax.swing.JComboBox();
        jComboPhone2 = new javax.swing.JComboBox();
        jCheckVisible = new javax.swing.JCheckBox();
        jbtnReset = new javax.swing.JButton();
        jbtnImport = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jCBiso = new javax.swing.JComboBox<>();

        setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        setMaximumSize(new java.awt.Dimension(750, 700));
        setMinimumSize(new java.awt.Dimension(750, 700));
        setOpaque(false);
        setPreferredSize(new java.awt.Dimension(750, 700));

        jLabel1.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("pos_messages"); // NOI18N
        jLabel1.setText(bundle.getString("label.csvfile")); // NOI18N
        jLabel1.setPreferredSize(new java.awt.Dimension(100, 30));

        jFileName.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jFileName.setPreferredSize(new java.awt.Dimension(400, 30));
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
                .addComponent(jFileName, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jbtnFileChoose, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(120, 120, 120))
        );
        jFileChooserPanelLayout.setVerticalGroup(
            jFileChooserPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jbtnFileChoose, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jFileChooserPanelLayout.createSequentialGroup()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(jFileName, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        jLabel18.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel18.setText(bundle.getString("label.csvdelimit")); // NOI18N
        jLabel18.setPreferredSize(new java.awt.Dimension(100, 30));

        jbtnRead.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jbtnRead.setText(bundle.getString("label.csvread")); // NOI18N
        jbtnRead.setEnabled(false);
        jbtnRead.setPreferredSize(new java.awt.Dimension(110, 45));
        jbtnRead.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbtnReadActionPerformed(evt);
            }
        });

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.LineBorder(new java.awt.Color(153, 153, 153), 1, true), bundle.getString("title.CSVImport"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Arial", 1, 14), new java.awt.Color(102, 102, 102))); // NOI18N

        jlblRecords.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jlblRecords.setText(bundle.getString("label.csvrecordsfound")); // NOI18N
        jlblRecords.setPreferredSize(new java.awt.Dimension(150, 30));

        jlblNew.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jlblNew.setText(bundle.getString("label.csvnewcustomers")); // NOI18N
        jlblNew.setMaximumSize(new java.awt.Dimension(77, 14));
        jlblNew.setMinimumSize(new java.awt.Dimension(77, 14));
        jlblNew.setPreferredSize(new java.awt.Dimension(150, 30));

        jlblInvalid.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jlblInvalid.setText(bundle.getString("label.invalidcustomers")); // NOI18N
        jlblInvalid.setPreferredSize(new java.awt.Dimension(150, 30));

        jlblUpdates.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jlblUpdates.setText(bundle.getString("label.customerupdated")); // NOI18N
        jlblUpdates.setPreferredSize(new java.awt.Dimension(150, 30));

        jlblMissing.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jlblMissing.setText(bundle.getString("label.csvmissing")); // NOI18N
        jlblMissing.setPreferredSize(new java.awt.Dimension(150, 30));

        jlblBad.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jlblBad.setText(bundle.getString("label.csvbad")); // NOI18N
        jlblBad.setPreferredSize(new java.awt.Dimension(150, 30));

        jlblNotChanged.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jlblNotChanged.setText(bundle.getString("label.csvnotchanged")); // NOI18N
        jlblNotChanged.setPreferredSize(new java.awt.Dimension(150, 30));

        jtxtRecords.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jtxtRecords.setForeground(new java.awt.Color(102, 102, 102));
        jtxtRecords.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jtxtRecords.setBorder(null);
        jtxtRecords.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        jtxtRecords.setEnabled(false);
        jtxtRecords.setPreferredSize(new java.awt.Dimension(100, 30));

        jtxtNew.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jtxtNew.setForeground(new java.awt.Color(102, 102, 102));
        jtxtNew.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jtxtNew.setBorder(null);
        jtxtNew.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        jtxtNew.setEnabled(false);
        jtxtNew.setPreferredSize(new java.awt.Dimension(100, 30));

        jtxtInvalid.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jtxtInvalid.setForeground(new java.awt.Color(102, 102, 102));
        jtxtInvalid.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jtxtInvalid.setBorder(null);
        jtxtInvalid.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        jtxtInvalid.setEnabled(false);
        jtxtInvalid.setPreferredSize(new java.awt.Dimension(100, 30));

        jtxtUpdate.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jtxtUpdate.setForeground(new java.awt.Color(102, 102, 102));
        jtxtUpdate.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jtxtUpdate.setBorder(null);
        jtxtUpdate.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        jtxtUpdate.setEnabled(false);
        jtxtUpdate.setPreferredSize(new java.awt.Dimension(100, 30));

        jtxtMissing.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jtxtMissing.setForeground(new java.awt.Color(102, 102, 102));
        jtxtMissing.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jtxtMissing.setBorder(null);
        jtxtMissing.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        jtxtMissing.setEnabled(false);
        jtxtMissing.setPreferredSize(new java.awt.Dimension(100, 30));

        jtxtBad.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jtxtBad.setForeground(new java.awt.Color(255, 0, 204));
        jtxtBad.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jtxtBad.setBorder(null);
        jtxtBad.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        jtxtBad.setEnabled(false);
        jtxtBad.setPreferredSize(new java.awt.Dimension(100, 30));

        jtxtNoChange.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jtxtNoChange.setForeground(new java.awt.Color(102, 102, 102));
        jtxtNoChange.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jtxtNoChange.setBorder(null);
        jtxtNoChange.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        jtxtNoChange.setEnabled(false);
        jtxtNoChange.setPreferredSize(new java.awt.Dimension(100, 30));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jlblRecords, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jtxtRecords, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jlblNotChanged, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jlblInvalid, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jlblUpdates, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jlblMissing, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jlblBad, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jlblNew, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jtxtNew, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jtxtInvalid, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jtxtUpdate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jtxtMissing, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jtxtBad, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jtxtNoChange, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jlblRecords, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jtxtRecords, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jlblNew, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jtxtNew, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jlblInvalid, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jtxtInvalid, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jlblUpdates, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jtxtUpdate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jlblMissing, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jtxtMissing, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jlblBad, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jtxtBad, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jlblNotChanged, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jtxtNoChange, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jComboSeparator.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jComboSeparator.setPreferredSize(new java.awt.Dimension(50, 30));

        webPBar.setFont(new java.awt.Font("Arial", 0, 13)); // NOI18N
        webPBar.setPreferredSize(new java.awt.Dimension(240, 30));

        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        jScrollPane1.setAutoscrolls(true);
        jScrollPane1.setPreferredSize(new java.awt.Dimension(442, 575));
        jScrollPane1.setRequestFocusEnabled(false);

        jPanel1.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jPanel1.setPreferredSize(new java.awt.Dimension(430, 650));

        jComboSearchKey.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jComboSearchKey.setEnabled(false);
        jComboSearchKey.setMinimumSize(new java.awt.Dimension(32, 25));
        jComboSearchKey.setPreferredSize(new java.awt.Dimension(300, 30));
        jComboSearchKey.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboSearchKeyItemStateChanged(evt);
            }
        });
        jComboSearchKey.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jComboSearchKeyFocusGained(evt);
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

        jComboAccountID.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jComboAccountID.setEnabled(false);
        jComboAccountID.setMinimumSize(new java.awt.Dimension(32, 25));
        jComboAccountID.setPreferredSize(new java.awt.Dimension(300, 30));
        jComboAccountID.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboAccountIDItemStateChanged(evt);
            }
        });
        jComboAccountID.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jComboAccountIDFocusGained(evt);
            }
        });

        jComboAddress1.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jComboAddress1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "" }));
        jComboAddress1.setSelectedIndex(-1);
        jComboAddress1.setEnabled(false);
        jComboAddress1.setMinimumSize(new java.awt.Dimension(32, 25));
        jComboAddress1.setPreferredSize(new java.awt.Dimension(300, 30));
        jComboAddress1.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboAddress1ItemStateChanged(evt);
            }
        });
        jComboAddress1.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jComboAddress1FocusGained(evt);
            }
        });

        jComboAddress2.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jComboAddress2.setEnabled(false);
        jComboAddress2.setMinimumSize(new java.awt.Dimension(32, 25));
        jComboAddress2.setPreferredSize(new java.awt.Dimension(300, 30));
        jComboAddress2.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboAddress2ItemStateChanged(evt);
            }
        });
        jComboAddress2.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jComboAddress2FocusGained(evt);
            }
        });

        jComboCity.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jComboCity.setEnabled(false);
        jComboCity.setPreferredSize(new java.awt.Dimension(300, 30));
        jComboCity.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboCityItemStateChanged(evt);
            }
        });
        jComboCity.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jComboCityFocusGained(evt);
            }
        });

        jComboRegion.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jComboRegion.setEnabled(false);
        jComboRegion.setMinimumSize(new java.awt.Dimension(32, 25));
        jComboRegion.setName(""); // NOI18N
        jComboRegion.setPreferredSize(new java.awt.Dimension(300, 30));
        jComboRegion.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboRegionItemStateChanged(evt);
            }
        });
        jComboRegion.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jComboRegionFocusGained(evt);
            }
        });

        jComboPostal.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jComboPostal.setEnabled(false);
        jComboPostal.setMinimumSize(new java.awt.Dimension(32, 25));
        jComboPostal.setName(""); // NOI18N
        jComboPostal.setPreferredSize(new java.awt.Dimension(300, 30));
        jComboPostal.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboPostalItemStateChanged(evt);
            }
        });
        jComboPostal.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jComboPostalFocusGained(evt);
            }
        });

        jComboFirstName.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jComboFirstName.setEnabled(false);
        jComboFirstName.setMinimumSize(new java.awt.Dimension(32, 25));
        jComboFirstName.setPreferredSize(new java.awt.Dimension(300, 30));
        jComboFirstName.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboFirstNameItemStateChanged(evt);
            }
        });
        jComboFirstName.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jComboFirstNameFocusGained(evt);
            }
        });

        jComboLastName.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jComboLastName.setEnabled(false);
        jComboLastName.setMinimumSize(new java.awt.Dimension(32, 25));
        jComboLastName.setPreferredSize(new java.awt.Dimension(300, 30));
        jComboLastName.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboLastNameItemStateChanged(evt);
            }
        });
        jComboLastName.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jComboLastNameFocusGained(evt);
            }
        });

        jComboEmail.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jComboEmail.setEnabled(false);
        jComboEmail.setMinimumSize(new java.awt.Dimension(32, 25));
        jComboEmail.setName(""); // NOI18N
        jComboEmail.setPreferredSize(new java.awt.Dimension(300, 30));
        jComboEmail.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboEmailItemStateChanged(evt);
            }
        });
        jComboEmail.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jComboEmailFocusGained(evt);
            }
        });

        jComboPhone.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jComboPhone.setEnabled(false);
        jComboPhone.setMinimumSize(new java.awt.Dimension(32, 25));
        jComboPhone.setPreferredSize(new java.awt.Dimension(300, 30));
        jComboPhone.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboPhoneItemStateChanged(evt);
            }
        });
        jComboPhone.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jComboPhoneFocusGained(evt);
            }
        });

        jComboPhone2.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jComboPhone2.setEnabled(false);
        jComboPhone2.setMinimumSize(new java.awt.Dimension(32, 25));
        jComboPhone2.setPreferredSize(new java.awt.Dimension(300, 30));
        jComboPhone2.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboPhone2ItemStateChanged(evt);
            }
        });
        jComboPhone2.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jComboPhone2FocusGained(evt);
            }
        });

        jCheckVisible.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jCheckVisible.setSelected(true);
        jCheckVisible.setEnabled(false);
        jCheckVisible.setPreferredSize(new java.awt.Dimension(30, 30));

        jbtnReset.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jbtnReset.setText(bundle.getString("button.reset")); // NOI18N
        jbtnReset.setPreferredSize(new java.awt.Dimension(110, 45));
        jbtnReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbtnResetActionPerformed(evt);
            }
        });

        jbtnImport.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jbtnImport.setText(bundle.getString("label.csvimpostbtn")); // NOI18N
        jbtnImport.setEnabled(false);
        jbtnImport.setPreferredSize(new java.awt.Dimension(110, 45));
        jbtnImport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbtnImportActionPerformed(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel3.setText(bundle.getString("label.taxid")); // NOI18N
        jLabel3.setPreferredSize(new java.awt.Dimension(100, 30));

        jLabel4.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel4.setText(bundle.getString("label.searchkeym")); // NOI18N
        jLabel4.setPreferredSize(new java.awt.Dimension(100, 30));

        jLabel5.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel5.setText(bundle.getString("label.namem")); // NOI18N
        jLabel5.setPreferredSize(new java.awt.Dimension(100, 30));

        jLabel10.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel10.setText(bundle.getString("label.address")); // NOI18N
        jLabel10.setPreferredSize(new java.awt.Dimension(100, 30));

        jLabel11.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel11.setText(bundle.getString("label.region")); // NOI18N
        jLabel11.setPreferredSize(new java.awt.Dimension(100, 30));

        jLabel7.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel7.setText(bundle.getString("label.city")); // NOI18N
        jLabel7.setPreferredSize(new java.awt.Dimension(100, 30));

        jLabel8.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel8.setText(bundle.getString("label.visible")); // NOI18N
        jLabel8.setPreferredSize(new java.awt.Dimension(100, 30));

        jLabel20.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel20.setText(bundle.getString("label.address2")); // NOI18N
        jLabel20.setPreferredSize(new java.awt.Dimension(100, 30));

        jLabel6.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel6.setText(bundle.getString("label.postal")); // NOI18N
        jLabel6.setPreferredSize(new java.awt.Dimension(100, 30));

        jLabel21.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel21.setText(bundle.getString("label.email")); // NOI18N
        jLabel21.setPreferredSize(new java.awt.Dimension(100, 30));

        jLabel22.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel22.setText(bundle.getString("label.phone")); // NOI18N
        jLabel22.setPreferredSize(new java.awt.Dimension(100, 30));

        jLabel23.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel23.setText(bundle.getString("label.phone2")); // NOI18N
        jLabel23.setPreferredSize(new java.awt.Dimension(100, 30));

        jLabel9.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel9.setText(bundle.getString("label.firstname")); // NOI18N
        jLabel9.setPreferredSize(new java.awt.Dimension(100, 30));

        jLabel12.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel12.setText(bundle.getString("label.lastname")); // NOI18N
        jLabel12.setPreferredSize(new java.awt.Dimension(100, 30));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCheckVisible, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jbtnReset, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12)
                        .addComponent(jbtnImport, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(93, 93, 93))))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addContainerGap()
                            .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jComboCity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jComboSearchKey, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jComboAccountID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jComboName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jComboAddress1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel20, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jComboAddress2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel23, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jComboPhone2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel22, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jComboPhone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel21, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jComboEmail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jComboLastName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jComboFirstName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jComboRegion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jComboPostal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboSearchKey, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboAccountID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboAddress1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboAddress2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel20, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboCity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboRegion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboPostal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboFirstName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboLastName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboEmail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel21, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboPhone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel22, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboPhone2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel23, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCheckVisible, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jbtnImport, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jbtnReset, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jScrollPane1.setViewportView(jPanel1);

        jCBiso.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jCBiso.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "ISO-8859-1", "ISO-8859-2", "ISO-8859-3", "ISO-8859-4", "ISO-8859-5", "ISO-8859-6", "ISO-8859-7", "ISO-8859-8", "ISO-8859-9", "ISO-8859-10", "UTF-8", "UTF-16", " " }));
        jCBiso.setToolTipText(bundle.getString("tooltip.import.cbiso")); // NOI18N
        jCBiso.setPreferredSize(new java.awt.Dimension(125, 30));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jFileChooserPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jComboSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jCBiso, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jbtnRead, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(16, 16, 16)))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(webPBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap(18, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jFileChooserPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jbtnRead, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCBiso, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(webPBar, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 575, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(15, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

  private void jbtnReadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbtnReadActionPerformed
      try {
          GetheadersFromFile(jFileName.getText());
          webPBar.setString("Source file Header OK");
      } catch (IOException ex) {
          LOGGER.log(Level.WARNING, null, ex);
          webPBar.setString("Source file Header error!");
      }
  }//GEN-LAST:event_jbtnReadActionPerformed

  private void jFileNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jFileNameActionPerformed
      jbtnImport.setEnabled(false);
      jbtnRead.setEnabled(true);
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

      if (last_folder == null
              || !last_folder.equals(current_folder.getAbsolutePath())) {
          AppConfig CSVConfig = new AppConfig(config_file);
          CSVConfig.load();
          CSVConfig.setProperty("CSV.last_folder",
                  current_folder.getAbsolutePath());
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

  private void jComboPostalFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jComboPostalFocusGained
      jComboPostal.removeAllItems();
      int i = 1;
      jComboPostal.addItem("");

      while (i < Headers.size()) {
          if ((Headers.get(i)
                  != jComboSearchKey.getSelectedItem())
                  & (Headers.get(i) != jComboAccountID.getSelectedItem())
                  & (Headers.get(i) != jComboName.getSelectedItem())
                  & (Headers.get(i) != jComboAddress1.getSelectedItem())
                  & (Headers.get(i) != jComboAddress2.getSelectedItem())
                  //                & (Headers.get(i) != jComboPostal.getSelectedItem())                    
                  & (Headers.get(i) != jComboCity.getSelectedItem())
                  & (Headers.get(i) != jComboRegion.getSelectedItem())
                  & (Headers.get(i) != jComboFirstName.getSelectedItem())
                  & (Headers.get(i) != jComboLastName.getSelectedItem())
                  & (Headers.get(i) != jComboEmail.getSelectedItem())
                  & (Headers.get(i) != jComboPhone.getSelectedItem())
                  & (Headers.get(i) != jComboPhone2.getSelectedItem())) {

              jComboPostal.addItem(Headers.get(i));
          }
          ++i;
      }
  }//GEN-LAST:event_jComboPostalFocusGained

  private void jComboPostalItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboPostalItemStateChanged

      checkFieldMapping();

  }//GEN-LAST:event_jComboPostalItemStateChanged

  private void jComboLastNameItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboLastNameItemStateChanged

      checkFieldMapping();

  }//GEN-LAST:event_jComboLastNameItemStateChanged

  private void jComboFirstNameItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboFirstNameItemStateChanged

      checkFieldMapping();

  }//GEN-LAST:event_jComboFirstNameItemStateChanged

  private void jComboPhone2ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboPhone2ItemStateChanged

      checkFieldMapping();

  }//GEN-LAST:event_jComboPhone2ItemStateChanged

  private void jComboPhoneItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboPhoneItemStateChanged

      checkFieldMapping();

  }//GEN-LAST:event_jComboPhoneItemStateChanged

  private void jComboEmailFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jComboEmailFocusGained
      jComboEmail.removeAllItems();
      int i = 1;
      jComboEmail.addItem("");

      while (i < Headers.size()) {
          if ((Headers.get(i)
                  != jComboSearchKey.getSelectedItem())
                  & (Headers.get(i) != jComboAccountID.getSelectedItem())
                  & (Headers.get(i) != jComboName.getSelectedItem())
                  & (Headers.get(i) != jComboAddress1.getSelectedItem())
                  & (Headers.get(i) != jComboAddress2.getSelectedItem())
                  & (Headers.get(i) != jComboPostal.getSelectedItem())
                  & (Headers.get(i) != jComboCity.getSelectedItem())
                  & (Headers.get(i) != jComboRegion.getSelectedItem())
                  & (Headers.get(i) != jComboFirstName.getSelectedItem())
                  & (Headers.get(i) != jComboLastName.getSelectedItem())
                  //                & (Headers.get(i) != jComboEmail.getSelectedItem())
                  & (Headers.get(i) != jComboPhone.getSelectedItem())
                  & (Headers.get(i) != jComboPhone2.getSelectedItem())) {

              jComboEmail.addItem(Headers.get(i));
          }
          ++i;
      }
  }//GEN-LAST:event_jComboEmailFocusGained

  private void jComboEmailItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboEmailItemStateChanged

      checkFieldMapping();

  }//GEN-LAST:event_jComboEmailItemStateChanged

  private void jbtnResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbtnResetActionPerformed
      resetFields();
      progress = -1;
      webPBar.setString("Waiting...");
  }//GEN-LAST:event_jbtnResetActionPerformed

  private void jbtnImportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbtnImportActionPerformed

      jbtnImport.setEnabled(false);

      workProcess work = new workProcess();
      Thread thread2 = new Thread(work);
      thread2.start();
  }//GEN-LAST:event_jbtnImportActionPerformed

  private void jComboRegionFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jComboRegionFocusGained
      jComboRegion.removeAllItems();
      int i = 1;
      jComboRegion.addItem("");

      while (i < Headers.size()) {
          if ((Headers.get(i)
                  != jComboSearchKey.getSelectedItem())
                  & (Headers.get(i) != jComboAccountID.getSelectedItem())
                  & (Headers.get(i) != jComboName.getSelectedItem())
                  & (Headers.get(i) != jComboAddress1.getSelectedItem())
                  & (Headers.get(i) != jComboAddress2.getSelectedItem())
                  & (Headers.get(i) != jComboPostal.getSelectedItem())
                  & (Headers.get(i) != jComboCity.getSelectedItem())
                  //                & (Headers.get(i) != jComboRegion.getSelectedItem())
                  & (Headers.get(i) != jComboFirstName.getSelectedItem())
                  & (Headers.get(i) != jComboLastName.getSelectedItem())
                  & (Headers.get(i) != jComboEmail.getSelectedItem())
                  & (Headers.get(i) != jComboPhone.getSelectedItem())
                  & (Headers.get(i) != jComboPhone2.getSelectedItem())) {

              jComboRegion.addItem(Headers.get(i));
          }
          ++i;
      }
  }//GEN-LAST:event_jComboRegionFocusGained

  private void jComboRegionItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboRegionItemStateChanged

      checkFieldMapping();

  }//GEN-LAST:event_jComboRegionItemStateChanged

  private void jComboCityFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jComboCityFocusGained
      jComboCity.removeAllItems();
      int i = 1;
      jComboCity.addItem("");

      while (i < Headers.size()) {
          if ((Headers.get(i)
                  != jComboSearchKey.getSelectedItem())
                  & (Headers.get(i) != jComboAccountID.getSelectedItem())
                  & (Headers.get(i) != jComboName.getSelectedItem())
                  & (Headers.get(i) != jComboAddress1.getSelectedItem())
                  & (Headers.get(i) != jComboAddress2.getSelectedItem())
                  & (Headers.get(i) != jComboPostal.getSelectedItem())
                  //                & (Headers.get(i) != jComboCity.getSelectedItem())
                  & (Headers.get(i) != jComboRegion.getSelectedItem())
                  & (Headers.get(i) != jComboFirstName.getSelectedItem())
                  & (Headers.get(i) != jComboLastName.getSelectedItem())
                  & (Headers.get(i) != jComboEmail.getSelectedItem())
                  & (Headers.get(i) != jComboPhone.getSelectedItem())
                  & (Headers.get(i) != jComboPhone2.getSelectedItem())) {

              jComboCity.addItem(Headers.get(i));
          }
          ++i;
      }
  }//GEN-LAST:event_jComboCityFocusGained

  private void jComboCityItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboCityItemStateChanged

      checkFieldMapping();

  }//GEN-LAST:event_jComboCityItemStateChanged

  private void jComboAddress2FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jComboAddress2FocusGained
      jComboAddress2.removeAllItems();
      int i = 1;
      jComboAddress2.addItem("");

      while (i < Headers.size()) {
          if ((Headers.get(i)
                  != jComboSearchKey.getSelectedItem())
                  & (Headers.get(i) != jComboAccountID.getSelectedItem())
                  & (Headers.get(i) != jComboName.getSelectedItem())
                  & (Headers.get(i) != jComboAddress1.getSelectedItem())
                  //                & (Headers.get(i) != jComboAddress2.getSelectedItem())
                  & (Headers.get(i) != jComboPostal.getSelectedItem())
                  & (Headers.get(i) != jComboCity.getSelectedItem())
                  & (Headers.get(i) != jComboRegion.getSelectedItem())
                  & (Headers.get(i) != jComboFirstName.getSelectedItem())
                  & (Headers.get(i) != jComboLastName.getSelectedItem())
                  & (Headers.get(i) != jComboEmail.getSelectedItem())
                  & (Headers.get(i) != jComboPhone.getSelectedItem())
                  & (Headers.get(i) != jComboPhone2.getSelectedItem())) {

              jComboAddress2.addItem(Headers.get(i));
          }
          ++i;
      }
  }//GEN-LAST:event_jComboAddress2FocusGained

  private void jComboAddress2ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboAddress2ItemStateChanged

      checkFieldMapping();

  }//GEN-LAST:event_jComboAddress2ItemStateChanged

  private void jComboAddress1FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jComboAddress1FocusGained
      jComboAddress1.removeAllItems();
      int i = 1;
      jComboAddress1.addItem("");

      while (i < Headers.size()) {
          if ((Headers.get(i)
                  != jComboSearchKey.getSelectedItem())
                  & (Headers.get(i) != jComboAccountID.getSelectedItem())
                  & (Headers.get(i) != jComboName.getSelectedItem())
                  //                & (Headers.get(i) != jComboAddress1.getSelectedItem())
                  & (Headers.get(i) != jComboAddress2.getSelectedItem())
                  & (Headers.get(i) != jComboPostal.getSelectedItem())
                  & (Headers.get(i) != jComboCity.getSelectedItem())
                  & (Headers.get(i) != jComboRegion.getSelectedItem())
                  & (Headers.get(i) != jComboFirstName.getSelectedItem())
                  & (Headers.get(i) != jComboLastName.getSelectedItem())
                  & (Headers.get(i) != jComboEmail.getSelectedItem())
                  & (Headers.get(i) != jComboPhone.getSelectedItem())
                  & (Headers.get(i) != jComboPhone2.getSelectedItem())) {

              jComboAddress1.addItem(Headers.get(i));
          }
          ++i;
      }
  }//GEN-LAST:event_jComboAddress1FocusGained

  private void jComboAddress1ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboAddress1ItemStateChanged

      checkFieldMapping();

  }//GEN-LAST:event_jComboAddress1ItemStateChanged

  private void jComboNameFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jComboNameFocusGained
      jComboName.removeAllItems();
      int i = 1;
      jComboName.addItem("");

      while (i < Headers.size()) {
          if ((Headers.get(i)
                  != jComboSearchKey.getSelectedItem())
                  & (Headers.get(i) != jComboAccountID.getSelectedItem())
                  //                & (Headers.get(i) != jComboName.getSelectedItem())
                  & (Headers.get(i) != jComboAddress1.getSelectedItem())
                  & (Headers.get(i) != jComboAddress2.getSelectedItem())
                  & (Headers.get(i) != jComboPostal.getSelectedItem())
                  & (Headers.get(i) != jComboCity.getSelectedItem())
                  & (Headers.get(i) != jComboRegion.getSelectedItem())
                  & (Headers.get(i) != jComboFirstName.getSelectedItem())
                  & (Headers.get(i) != jComboLastName.getSelectedItem())
                  & (Headers.get(i) != jComboEmail.getSelectedItem())
                  & (Headers.get(i) != jComboPhone.getSelectedItem())
                  & (Headers.get(i) != jComboPhone2.getSelectedItem())) {

              jComboName.addItem(Headers.get(i));
          }
          ++i;
      }
  }//GEN-LAST:event_jComboNameFocusGained

  private void jComboNameItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboNameItemStateChanged

      checkFieldMapping();

  }//GEN-LAST:event_jComboNameItemStateChanged

  private void jComboSearchKeyFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jComboSearchKeyFocusGained
      jComboName.removeAllItems();
      int i = 1;
      jComboName.addItem("");

      while (i < Headers.size()) {
          if ((Headers.get(i)
                  //                != jComboSearchKey.getSelectedItem())
                  != jComboAccountID.getSelectedItem())
                  & (Headers.get(i) != jComboName.getSelectedItem())
                  & (Headers.get(i) != jComboAddress1.getSelectedItem())
                  & (Headers.get(i) != jComboAddress2.getSelectedItem())
                  & (Headers.get(i) != jComboPostal.getSelectedItem())
                  & (Headers.get(i) != jComboCity.getSelectedItem())
                  & (Headers.get(i) != jComboRegion.getSelectedItem())
                  & (Headers.get(i) != jComboFirstName.getSelectedItem())
                  & (Headers.get(i) != jComboLastName.getSelectedItem())
                  & (Headers.get(i) != jComboEmail.getSelectedItem())
                  & (Headers.get(i) != jComboPhone.getSelectedItem())
                  & (Headers.get(i) != jComboPhone2.getSelectedItem())) {

              jComboSearchKey.addItem(Headers.get(i));
          }
          ++i;
      }
  }//GEN-LAST:event_jComboSearchKeyFocusGained

  private void jComboSearchKeyItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboSearchKeyItemStateChanged

      checkFieldMapping();

  }//GEN-LAST:event_jComboSearchKeyItemStateChanged

  private void jComboAccountIDFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jComboAccountIDFocusGained
      jComboAccountID.removeAllItems();
      int i = 1;
      jComboAccountID.addItem("");

      while (i < Headers.size()) {
          if ((Headers.get(i)
                  != jComboSearchKey.getSelectedItem())
                  //                & (Headers.get(i) != jComboAccountID.getSelectedItem())
                  & (Headers.get(i) != jComboName.getSelectedItem())
                  & (Headers.get(i) != jComboAddress1.getSelectedItem())
                  & (Headers.get(i) != jComboAddress2.getSelectedItem())
                  & (Headers.get(i) != jComboPostal.getSelectedItem())
                  & (Headers.get(i) != jComboCity.getSelectedItem())
                  & (Headers.get(i) != jComboRegion.getSelectedItem())
                  & (Headers.get(i) != jComboFirstName.getSelectedItem())
                  & (Headers.get(i) != jComboLastName.getSelectedItem())
                  & (Headers.get(i) != jComboEmail.getSelectedItem())
                  & (Headers.get(i) != jComboPhone.getSelectedItem())
                  & (Headers.get(i) != jComboPhone2.getSelectedItem())) {

              jComboAccountID.addItem(Headers.get(i));
          }
          ++i;
      }
  }//GEN-LAST:event_jComboAccountIDFocusGained

  private void jComboAccountIDItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboAccountIDItemStateChanged

      checkFieldMapping();

  }//GEN-LAST:event_jComboAccountIDItemStateChanged

  private void jComboFirstNameFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jComboFirstNameFocusGained
      jComboFirstName.removeAllItems();
      int i = 1;
      jComboFirstName.addItem("");

      while (i < Headers.size()) {
          if ((Headers.get(i)
                  != jComboSearchKey.getSelectedItem())
                  & (Headers.get(i) != jComboAccountID.getSelectedItem())
                  & (Headers.get(i) != jComboName.getSelectedItem())
                  & (Headers.get(i) != jComboAddress1.getSelectedItem())
                  & (Headers.get(i) != jComboAddress2.getSelectedItem())
                  & (Headers.get(i) != jComboPostal.getSelectedItem())
                  & (Headers.get(i) != jComboCity.getSelectedItem())
                  & (Headers.get(i) != jComboRegion.getSelectedItem())
                  //                & (Headers.get(i) != jComboFirstName.getSelectedItem())
                  & (Headers.get(i) != jComboLastName.getSelectedItem())
                  & (Headers.get(i) != jComboEmail.getSelectedItem())
                  & (Headers.get(i) != jComboPhone.getSelectedItem())
                  & (Headers.get(i) != jComboPhone2.getSelectedItem())) {

              jComboFirstName.addItem(Headers.get(i));
          }
          ++i;
      }
  }//GEN-LAST:event_jComboFirstNameFocusGained

  private void jComboLastNameFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jComboLastNameFocusGained
      jComboLastName.removeAllItems();
      int i = 1;
      jComboLastName.addItem("");

      while (i < Headers.size()) {
          if ((Headers.get(i)
                  != jComboSearchKey.getSelectedItem())
                  & (Headers.get(i) != jComboAccountID.getSelectedItem())
                  & (Headers.get(i) != jComboName.getSelectedItem())
                  & (Headers.get(i) != jComboAddress1.getSelectedItem())
                  & (Headers.get(i) != jComboAddress2.getSelectedItem())
                  & (Headers.get(i) != jComboPostal.getSelectedItem())
                  & (Headers.get(i) != jComboCity.getSelectedItem())
                  & (Headers.get(i) != jComboRegion.getSelectedItem())
                  & (Headers.get(i) != jComboFirstName.getSelectedItem())
                  //                & (Headers.get(i) != jComboLastName.getSelectedItem())
                  & (Headers.get(i) != jComboEmail.getSelectedItem())
                  & (Headers.get(i) != jComboPhone.getSelectedItem())
                  & (Headers.get(i) != jComboPhone2.getSelectedItem())) {

              jComboLastName.addItem(Headers.get(i));
          }
          ++i;
      }
  }//GEN-LAST:event_jComboLastNameFocusGained

  private void jComboPhoneFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jComboPhoneFocusGained
      jComboPhone.removeAllItems();
      int i = 1;
      jComboPhone.addItem("");

      while (i < Headers.size()) {
          if ((Headers.get(i)
                  != jComboSearchKey.getSelectedItem())
                  & (Headers.get(i) != jComboAccountID.getSelectedItem())
                  & (Headers.get(i) != jComboName.getSelectedItem())
                  & (Headers.get(i) != jComboAddress1.getSelectedItem())
                  & (Headers.get(i) != jComboAddress2.getSelectedItem())
                  & (Headers.get(i) != jComboPostal.getSelectedItem())
                  & (Headers.get(i) != jComboCity.getSelectedItem())
                  & (Headers.get(i) != jComboRegion.getSelectedItem())
                  & (Headers.get(i) != jComboFirstName.getSelectedItem())
                  & (Headers.get(i) != jComboLastName.getSelectedItem())
                  & (Headers.get(i) != jComboEmail.getSelectedItem())
                  //                & (Headers.get(i) != jComboPhone.getSelectedItem())
                  & (Headers.get(i) != jComboPhone2.getSelectedItem())) {

              jComboPhone.addItem(Headers.get(i));
          }
          ++i;
      }
  }//GEN-LAST:event_jComboPhoneFocusGained

  private void jComboPhone2FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jComboPhone2FocusGained
      jComboPhone2.removeAllItems();
      int i = 1;
      jComboPhone2.addItem("");

      while (i < Headers.size()) {
          if ((Headers.get(i)
                  != jComboSearchKey.getSelectedItem())
                  & (Headers.get(i) != jComboAccountID.getSelectedItem())
                  & (Headers.get(i) != jComboName.getSelectedItem())
                  & (Headers.get(i) != jComboAddress1.getSelectedItem())
                  & (Headers.get(i) != jComboAddress2.getSelectedItem())
                  & (Headers.get(i) != jComboPostal.getSelectedItem())
                  & (Headers.get(i) != jComboCity.getSelectedItem())
                  & (Headers.get(i) != jComboRegion.getSelectedItem())
                  & (Headers.get(i) != jComboFirstName.getSelectedItem())
                  & (Headers.get(i) != jComboLastName.getSelectedItem())
                  & (Headers.get(i) != jComboEmail.getSelectedItem())
                  & (Headers.get(i) != jComboPhone.getSelectedItem()) //                & (Headers.get(i) != jComboPhone2.getSelectedItem())                    
                  ) {

              jComboPhone2.addItem(Headers.get(i));
          }
          ++i;
      }
  }//GEN-LAST:event_jComboPhone2FocusGained

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<String> jCBiso;
    private javax.swing.JCheckBox jCheckVisible;
    private javax.swing.JComboBox jComboAccountID;
    private javax.swing.JComboBox jComboAddress1;
    private javax.swing.JComboBox jComboAddress2;
    private javax.swing.JComboBox jComboCity;
    private javax.swing.JComboBox jComboEmail;
    private javax.swing.JComboBox jComboFirstName;
    private javax.swing.JComboBox jComboLastName;
    private javax.swing.JComboBox jComboName;
    private javax.swing.JComboBox jComboPhone;
    private javax.swing.JComboBox jComboPhone2;
    private javax.swing.JComboBox jComboPostal;
    private javax.swing.JComboBox jComboRegion;
    private javax.swing.JComboBox jComboSearchKey;
    private javax.swing.JComboBox jComboSeparator;
    private javax.swing.JPanel jFileChooserPanel;
    private javax.swing.JTextField jFileName;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton jbtnFileChoose;
    private javax.swing.JButton jbtnImport;
    private javax.swing.JButton jbtnRead;
    private javax.swing.JButton jbtnReset;
    private javax.swing.JLabel jlblBad;
    private javax.swing.JLabel jlblInvalid;
    private javax.swing.JLabel jlblMissing;
    private javax.swing.JLabel jlblNew;
    private javax.swing.JLabel jlblNotChanged;
    private javax.swing.JLabel jlblRecords;
    private javax.swing.JLabel jlblUpdates;
    private javax.swing.JTextField jtxtBad;
    private javax.swing.JTextField jtxtInvalid;
    private javax.swing.JTextField jtxtMissing;
    private javax.swing.JTextField jtxtNew;
    private javax.swing.JTextField jtxtNoChange;
    private javax.swing.JTextField jtxtRecords;
    private javax.swing.JTextField jtxtUpdate;
    private javax.swing.JProgressBar webPBar;
    // End of variables declaration//GEN-END:variables
}
