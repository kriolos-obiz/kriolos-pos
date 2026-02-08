//    KriolOS POS
//    Copyright (c) 2009-2019 uniCenta
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

package com.openbravo.pos.suppliers;

import com.openbravo.basic.BasicException;
import com.openbravo.data.user.DirtyManager;
import com.openbravo.data.user.EditorRecord;
import com.openbravo.format.Formats;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.BeanFactoryException;
import com.openbravo.pos.inventory.MovementReason;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import org.netbeans.validation.api.builtin.stringvalidation.StringValidators;

/**
 *
 * @author Jack Gerrard
 */
public final class SuppliersView extends com.openbravo.pos.panels.ValidationPanel implements EditorRecord {

    private static final Logger LOGGER = Logger.getLogger(SuppliersView.class.getName());
    private static final long serialVersionUID = 1L;
    private String m_oId;

    private DataLogicSuppliers dlSuppliers;

    private AppView appView;
    private SupplierInfo supplierInfo;

    /**
     * Creates new form SuppliersView
     *
     * @param app
     * @param dirty
     */
    public SuppliersView(AppView app, DirtyManager dirty) {
        super();
        try {
            setAppView(app);
            dlSuppliers = (DataLogicSuppliers) app.getBean("com.openbravo.pos.suppliers.DataLogicSuppliers");

            initComponents();
            
            m_jTaxID.getDocument().addDocumentListener(dirty);
            m_jVATID.getDocument().addDocumentListener(dirty);
            m_jSearchkey.getDocument().addDocumentListener(dirty);
            m_jName.getDocument().addDocumentListener(dirty);

            m_jNotes.getDocument().addDocumentListener(dirty);
            txtMaxdebt.getDocument().addDocumentListener(dirty);
            m_jVisible.addActionListener(dirty);

            txtFirstName.getDocument().addDocumentListener(dirty);
            txtLastName.getDocument().addDocumentListener(dirty);
            txtEmail.getDocument().addDocumentListener(dirty);
            txtPhone.getDocument().addDocumentListener(dirty);
            txtPhone2.getDocument().addDocumentListener(dirty);
            txtFax.getDocument().addDocumentListener(dirty);

            txtAddress.getDocument().addDocumentListener(dirty);
            txtAddress2.getDocument().addDocumentListener(dirty);
            txtPostal.getDocument().addDocumentListener(dirty);
            txtCity.getDocument().addDocumentListener(dirty);
            txtRegion.getDocument().addDocumentListener(dirty);
            txtCountry.getDocument().addDocumentListener(dirty);

            init();
            initValidator();
        } catch (BeanFactoryException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
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
    }

    /**
     * Refresh object
     */
    @Override
    public void refresh() {
        jLblTranCount.setText(null);
    }

    /**
     * Write EOF
     */
    @Override
    public void writeValueEOF() {
        m_oId = null;
        m_jTaxID.setText(null);
        m_jVATID.setText(null);
        m_jSearchkey.setText(null);
        m_jName.setText(null);
        m_jNotes.setText(null);
        txtMaxdebt.setText(null);
        txtCurdebt.setText(null);
        txtCurdate.setText(null);
        m_jVisible.setSelected(false);

        txtFirstName.setText(null);
        txtLastName.setText(null);
        txtEmail.setText(null);
        txtPhone.setText(null);
        txtPhone2.setText(null);
        txtFax.setText(null);
        txtAddress.setText(null);
        txtAddress2.setText(null);
        txtPostal.setText(null);
        txtCity.setText(null);
        txtRegion.setText(null);
        txtCountry.setText(null);

        m_jTaxID.setEnabled(false);
        m_jVATID.setEnabled(false);
        m_jSearchkey.setEnabled(false);
        m_jName.setEnabled(false);
        m_jNotes.setEnabled(false);
        txtMaxdebt.setEnabled(false);
        txtCurdebt.setEnabled(false);
        txtCurdate.setEnabled(false);
        m_jVisible.setEnabled(false);

        txtFirstName.setEnabled(false);
        txtLastName.setEnabled(false);
        txtEmail.setEnabled(false);
        txtPhone.setEnabled(false);
        txtPhone2.setEnabled(false);
        txtFax.setEnabled(false);

        txtAddress.setEnabled(false);
        txtAddress2.setEnabled(false);
        txtPostal.setEnabled(false);
        txtCity.setEnabled(false);
        txtRegion.setEnabled(false);
        txtCountry.setEnabled(false);

        jTableSupplierTransactions.setEnabled(false);

        repaint();
        refresh();
    }

    @Override
    public void writeValueInsert() {
        m_oId = null;
        m_jTaxID.setText(null);
        m_jVATID.setText(null);
        m_jSearchkey.setText(null);
        m_jName.setText(null);
        txtMaxdebt.setText(null);
        txtCurdebt.setText(null);
        txtCurdate.setText(null);

        txtFirstName.setText(null);
        txtLastName.setText(null);
        txtPhone.setText(null);
        txtPhone2.setText(null);
        txtEmail.setText(null);

        txtFax.setText(null);
        txtAddress.setText(null);
        txtAddress2.setText(null);
        txtPostal.setText(null);
        txtCity.setText(null);
        txtRegion.setText(null);
        txtCountry.setText(null);

        m_jNotes.setText(null);
        m_jVisible.setSelected(true);

        m_jTaxID.setEnabled(true);
        m_jVATID.setEnabled(true);
        m_jSearchkey.setEnabled(true);
        m_jName.setEnabled(true);

        txtFirstName.setEnabled(true);
        txtLastName.setEnabled(true);
        txtEmail.setEnabled(true);
        webBtnMail.setEnabled(true);
        txtPhone.setEnabled(true);
        txtPhone2.setEnabled(true);
        txtFax.setEnabled(true);

        txtAddress.setEnabled(true);
        txtAddress2.setEnabled(true);
        txtPostal.setEnabled(true);
        txtCity.setEnabled(true);
        txtRegion.setEnabled(true);
        txtCountry.setEnabled(true);

        m_jNotes.setEnabled(true);
        txtMaxdebt.setEnabled(true);
        txtCurdebt.setEnabled(true);
        txtCurdate.setEnabled(true);
        m_jVisible.setEnabled(true);
        jTableSupplierTransactions.setEnabled(false);

        repaint();
        refresh();
    }

    /**
     * Delete from object
     *
     * @param value
     */
    @Override
    public void writeValueDelete(Object value) {

        setValues(value);

        m_jTaxID.setEnabled(false);
        m_jVATID.setEnabled(false);
        m_jSearchkey.setEnabled(false);
        m_jName.setEnabled(false);
        m_jNotes.setEnabled(false);
        txtMaxdebt.setEnabled(false);
        txtCurdebt.setEnabled(false);
        txtCurdate.setEnabled(false);
        m_jVisible.setEnabled(false);

        txtFirstName.setEnabled(false);
        txtLastName.setEnabled(false);
        txtEmail.setEnabled(false);
        webBtnMail.setEnabled(false);
        txtPhone.setEnabled(false);
        txtPhone2.setEnabled(false);
        txtFax.setEnabled(false);

        txtAddress.setEnabled(false);
        txtAddress2.setEnabled(false);
        txtPostal.setEnabled(false);
        txtCity.setEnabled(false);
        txtRegion.setEnabled(false);
        txtCountry.setEnabled(false);

        TransactionTableModel transactionModel = new TransactionTableModel(new ArrayList<>());
        jTableSupplierTransactions.setModel(transactionModel);
        jTableSupplierTransactions.setEnabled(false);

        repaint();
        refresh();

    }
    
    private void setValues(Object value){
    Object[] supplier = (Object[]) value;
        m_oId = (String)supplier[0];
        m_jSearchkey.setText((String) supplier[1]);
        m_jTaxID.setText((String) supplier[2]);
        m_jName.setText((String) supplier[3]);
        txtMaxdebt.setText(Formats.CURRENCY.formatValue((Double)supplier[4]));

        txtAddress.setText(Formats.STRING.formatValue((String)supplier[5]));
        txtAddress2.setText(Formats.STRING.formatValue((String)supplier[6]));
        txtPostal.setText(Formats.STRING.formatValue((String)supplier[7]));
        txtCity.setText(Formats.STRING.formatValue((String)supplier[8]));
        txtRegion.setText(Formats.STRING.formatValue((String)supplier[9]));
        txtCountry.setText(Formats.STRING.formatValue((String)supplier[10]));

        txtFirstName.setText(Formats.STRING.formatValue((String)supplier[11]));
        txtLastName.setText(Formats.STRING.formatValue((String)supplier[12]));
        txtEmail.setText(Formats.STRING.formatValue((String)supplier[13]));
        txtPhone.setText(Formats.STRING.formatValue((String)supplier[14]));
        txtPhone2.setText(Formats.STRING.formatValue((String)supplier[15]));
        txtFax.setText(Formats.STRING.formatValue((String)supplier[16]));

        m_jNotes.setText((String) supplier[17]);
        m_jVisible.setSelected(((Boolean) supplier[18]));

        txtCurdate.setText(Formats.DATE.formatValue((Date)supplier[19]));
        txtCurdebt.setText(Formats.CURRENCY.formatValue((Double)supplier[20]));

        m_jVATID.setText((String) supplier[21]);
    }

    /**
     * Edit object
     *
     * @param value
     */
    @Override
    public void writeValueEdit(Object value) {
        
        setValues(value);

        m_jSearchkey.setEnabled(true);
        m_jTaxID.setEnabled(true);
        m_jName.setEnabled(true);
        txtMaxdebt.setEnabled(true);

        txtAddress.setEnabled(true);
        txtAddress2.setEnabled(true);
        txtPostal.setEnabled(true);
        txtCity.setEnabled(true);
        txtRegion.setEnabled(true);
        txtCountry.setEnabled(true);

        txtFirstName.setEnabled(true);
        txtLastName.setEnabled(true);
        txtEmail.setEnabled(true);
        webBtnMail.setEnabled(true);
        txtPhone.setEnabled(true);
        txtPhone2.setEnabled(true);
        txtFax.setEnabled(true);

        m_jNotes.setEnabled(true);
        m_jVisible.setEnabled(true);
        txtCurdebt.setEnabled(true);
        txtCurdate.setEnabled(true);
        m_jVATID.setEnabled(true);

        jTableSupplierTransactions.setVisible(false);
        jTableSupplierTransactions.setEnabled(true);
        resetTranxTable();

        txtCurdate.repaint();
        txtCurdebt.repaint();
        jTableSupplierTransactions.repaint();
        repaint();
        refresh();
    }

    public void resetTranxTable() {

        jTableSupplierTransactions.getColumnModel().getColumn(0).setPreferredWidth(100);
        jTableSupplierTransactions.getColumnModel().getColumn(1).setPreferredWidth(225);
        jTableSupplierTransactions.getColumnModel().getColumn(2).setPreferredWidth(30);
        jTableSupplierTransactions.getColumnModel().getColumn(3).setPreferredWidth(50);
        jTableSupplierTransactions.getColumnModel().getColumn(4).setPreferredWidth(55);

        // set font for headers
        Font f = new Font("Arial", Font.BOLD, 14);
        JTableHeader header = jTableSupplierTransactions.getTableHeader();
        header.setFont(f);

        jTableSupplierTransactions.getTableHeader().setReorderingAllowed(true);
        jTableSupplierTransactions.setAutoCreateRowSorter(true);

        jTableSupplierTransactions.repaint();
    }

    /**
     * Create object
     *
     * @return
     * @throws BasicException
     */
    @Override
    public Object createValue() throws BasicException {
        
        if(isFatalProblem()){
            throw new BasicException(getProblem().getMessage());
        }
        
        Object[] supplier = new Object[23];
        supplier[0] = m_oId == null ? UUID.randomUUID().toString() : m_oId;
        supplier[1] = m_jSearchkey.getText();
        supplier[2] = m_jTaxID.getText();
        supplier[3] = m_jName.getText();
        supplier[4] = Formats.CURRENCY.parseValue(txtMaxdebt.getText(), 0.0);
        supplier[5] = Formats.STRING.parseValue(txtAddress.getText());
        supplier[6] = Formats.STRING.parseValue(txtAddress2.getText());
        supplier[7] = Formats.STRING.parseValue(txtPostal.getText());
        supplier[8] = Formats.STRING.parseValue(txtCity.getText());
        supplier[9] = Formats.STRING.parseValue(txtRegion.getText());
        supplier[10] = Formats.STRING.parseValue(txtCountry.getText());
        supplier[11] = Formats.STRING.parseValue(txtFirstName.getText());
        supplier[12] = Formats.STRING.parseValue(txtLastName.getText());
        supplier[13] = Formats.STRING.parseValue(txtEmail.getText());
        supplier[14] = Formats.STRING.parseValue(txtPhone.getText());
        supplier[15] = Formats.STRING.parseValue(txtPhone2.getText());
        supplier[16] = Formats.STRING.parseValue(txtFax.getText());
        supplier[17] = m_jNotes.getText();
        supplier[18] = m_jVisible.isSelected();
        supplier[19] = Formats.TIMESTAMP.parseValue(txtCurdate.getText());
        supplier[20] = Formats.CURRENCY.parseValue(txtCurdebt.getText());
        supplier[21] = m_jVATID.getText();

        supplier[22] = (Object) getAppView();

        repaint();
        refresh();

        return supplier;
    }

    public AppView getAppView() {
        return appView;
    }

    public void setAppView(AppView appView) {
        this.appView = appView;
    }

    @Override
    public Component getComponent() {
        return this;
    }

    private List<SupplierTransaction> getTransactionOfName(String sId) {
        List<SupplierTransaction> supplierList = new ArrayList<>();
        try {
            supplierList = dlSuppliers.getSuppliersTransactionList(sId);

            for (SupplierTransaction supplierTransaction : supplierList) {
                String supplierId = supplierTransaction.getSupplierId();
                if (!supplierId.equals(sId)) {
                    supplierList.remove(supplierTransaction);
                }
            }

        } catch (BasicException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }

        return supplierList;
    }

    private void initValidator() {
        org.netbeans.validation.api.ui.ValidationGroup valGroup = getValidationGroup();
        valGroup.add(m_jSearchkey, StringValidators.REQUIRE_NON_EMPTY_STRING);
        valGroup.add(m_jName, StringValidators.REQUIRE_NON_EMPTY_STRING);
        valGroup.add(txtMaxdebt, StringValidators.REQUIRE_NON_NEGATIVE_NUMBER);
    }

    class TransactionTableModel extends AbstractTableModel {

        String dte = AppLocal.getIntString("label.suptblHeaderCol1");
        String prd = AppLocal.getIntString("label.suptblHeaderCol2");
        String qty = AppLocal.getIntString("label.suptblHeaderCol3");
        String pri = AppLocal.getIntString("label.suptblHeaderCol4");
        String rsn = AppLocal.getIntString("label.suptblHeaderCol5");

        List<SupplierTransaction> transactionList;
        String[] columnNames = {dte, prd, qty, pri, rsn};
        public Double Tamount;

        public TransactionTableModel(List<SupplierTransaction> list) {
            transactionList = list;
        }

        @Override
        public int getColumnCount() {
            return 5;
        }

        @Override
        public int getRowCount() {
            return transactionList.size();
        }

        // this method is called to set the value of each cell
        @Override
        public Object getValueAt(int row, int column) {
            SupplierTransaction supplierTransaction = transactionList.get(row);

            jTableSupplierTransactions.setRowHeight(25);

            switch (column) {

                case 0:
                    Date transactionDate = supplierTransaction.getTransactionDate();
                    String formattedDate = Formats.DATETIME.formatValue(transactionDate);
                    return formattedDate;
                case 1:
                    return supplierTransaction.getProductName();
                case 2:
                    return supplierTransaction.getUnit();
                case 3:
                    Double price = supplierTransaction.getPrice();
                    DecimalFormat df = new DecimalFormat("#.##");
                    String formattedAmount = df.format(price);
                    return formattedAmount;
                case 4:
                    Integer reason = supplierTransaction.getReason();
                    String s = String.valueOf(reason);

                    MovementReason movReason = MovementReason.get(reason);
                    if(movReason != null){
                        s = movReason.toString();
                    }
                    
                    return s;

                default:
                    return "";

            }
        }

        @Override
        public String getColumnName(int col) {
            return columnNames[col];
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        formTabbedPane = new javax.swing.JTabbedPane();
        jPanel5 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        m_jVATID = new javax.swing.JTextField();
        m_jTaxID = new javax.swing.JTextField();
        m_jSearchkey_Label = new javax.swing.JLabel();
        m_jSearchkey = new javax.swing.JTextField();
        m_jName_Label = new javax.swing.JLabel();
        m_jName = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        m_jVisible = new javax.swing.JCheckBox();
        lblMaxdebt = new javax.swing.JLabel();
        txtMaxdebt = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        txtCurdebt = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        txtCurdate = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        jLabel19 = new javax.swing.JLabel();
        txtFirstName = new javax.swing.JTextField();
        jLabel15 = new javax.swing.JLabel();
        txtLastName = new javax.swing.JTextField();
        jLabel16 = new javax.swing.JLabel();
        txtEmail = new javax.swing.JTextField();
        jLabel17 = new javax.swing.JLabel();
        txtPhone = new javax.swing.JTextField();
        jLabel18 = new javax.swing.JLabel();
        txtPhone2 = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        txtFax = new javax.swing.JTextField();
        webBtnMail = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel13 = new javax.swing.JLabel();
        txtAddress = new javax.swing.JTextField();
        jLabel20 = new javax.swing.JLabel();
        txtCountry = new javax.swing.JTextField();
        jLabel21 = new javax.swing.JLabel();
        txtAddress2 = new javax.swing.JTextField();
        jLabel22 = new javax.swing.JLabel();
        txtPostal = new javax.swing.JTextField();
        jLabel23 = new javax.swing.JLabel();
        txtCity = new javax.swing.JTextField();
        jLabel24 = new javax.swing.JLabel();
        txtRegion = new javax.swing.JTextField();
        jPanel4 = new javax.swing.JPanel();
        jBtnShowTrans = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTableSupplierTransactions = new javax.swing.JTable();
        jLblTranCount = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        m_jNotes = new javax.swing.JTextArea();

        setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        setPreferredSize(new java.awt.Dimension(680, 320));
        setLayout(new java.awt.BorderLayout());

        formTabbedPane.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        formTabbedPane.setPreferredSize(new java.awt.Dimension(650, 300));

        jLabel7.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel7.setText(AppLocal.getIntString("label.taxid")); // NOI18N
        jLabel7.setMaximumSize(new java.awt.Dimension(150, 30));
        jLabel7.setMinimumSize(new java.awt.Dimension(140, 25));
        jLabel7.setPreferredSize(new java.awt.Dimension(150, 30));
        jLabel7.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel7MouseClicked(evt);
            }
        });

        jLabel9.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel9.setText(AppLocal.getIntString("label.suppliervatid")); // NOI18N
        jLabel9.setMaximumSize(new java.awt.Dimension(150, 30));
        jLabel9.setMinimumSize(new java.awt.Dimension(140, 25));
        jLabel9.setPreferredSize(new java.awt.Dimension(150, 30));

        m_jVATID.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jVATID.setPreferredSize(new java.awt.Dimension(150, 30));

        m_jTaxID.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jTaxID.setPreferredSize(new java.awt.Dimension(150, 30));

        m_jSearchkey_Label.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jSearchkey_Label.setText(AppLocal.getIntString("label.searchkeym")); // NOI18N

        m_jSearchkey.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jSearchkey.setName(m_jSearchkey_Label.getText());
        m_jSearchkey.setPreferredSize(new java.awt.Dimension(0, 30));

        m_jName_Label.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jName_Label.setText(AppLocal.getIntString("label.supplier")); // NOI18N
        m_jName_Label.setMaximumSize(new java.awt.Dimension(140, 25));
        m_jName_Label.setMinimumSize(new java.awt.Dimension(140, 25));
        m_jName_Label.setPreferredSize(new java.awt.Dimension(150, 30));

        m_jName.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jName.setName(m_jName_Label.getText());
        m_jName.setPreferredSize(new java.awt.Dimension(0, 30));

        jLabel4.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel4.setText(AppLocal.getIntString("label.visible")); // NOI18N
        jLabel4.setMaximumSize(new java.awt.Dimension(140, 25));
        jLabel4.setMinimumSize(new java.awt.Dimension(140, 25));
        jLabel4.setPreferredSize(new java.awt.Dimension(150, 30));

        m_jVisible.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        lblMaxdebt.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        lblMaxdebt.setText(AppLocal.getIntString("label.maxdebt")); // NOI18N
        lblMaxdebt.setMaximumSize(new java.awt.Dimension(140, 25));
        lblMaxdebt.setMinimumSize(new java.awt.Dimension(140, 25));
        lblMaxdebt.setPreferredSize(new java.awt.Dimension(150, 30));

        txtMaxdebt.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        txtMaxdebt.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtMaxdebt.setName(lblMaxdebt.getText());
        txtMaxdebt.setPreferredSize(new java.awt.Dimension(150, 30));

        jLabel2.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel2.setText(AppLocal.getIntString("label.curdebt")); // NOI18N
        jLabel2.setMaximumSize(new java.awt.Dimension(140, 25));
        jLabel2.setMinimumSize(new java.awt.Dimension(140, 25));
        jLabel2.setPreferredSize(new java.awt.Dimension(150, 30));

        txtCurdebt.setEditable(false);
        txtCurdebt.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        txtCurdebt.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtCurdebt.setPreferredSize(new java.awt.Dimension(150, 30));

        jLabel6.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel6.setText(AppLocal.getIntString("label.curdate")); // NOI18N
        jLabel6.setPreferredSize(new java.awt.Dimension(150, 30));

        txtCurdate.setEditable(false);
        txtCurdate.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        txtCurdate.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtCurdate.setPreferredSize(new java.awt.Dimension(150, 30));

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtCurdebt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(m_jSearchkey_Label, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lblMaxdebt, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(txtMaxdebt, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(m_jSearchkey, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(m_jTaxID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(m_jVisible, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(jPanel5Layout.createSequentialGroup()
                                    .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(m_jVATID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(txtCurdate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGroup(jPanel5Layout.createSequentialGroup()
                                    .addComponent(m_jName_Label, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(m_jName, javax.swing.GroupLayout.PREFERRED_SIZE, 395, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addContainerGap(117, Short.MAX_VALUE))))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(m_jVisible, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(m_jTaxID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(11, 11, 11)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jVATID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(m_jName_Label, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(11, 11, 11)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(m_jSearchkey_Label, javax.swing.GroupLayout.DEFAULT_SIZE, 31, Short.MAX_VALUE)
                    .addComponent(m_jSearchkey, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblMaxdebt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtMaxdebt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(9, 9, 9)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtCurdebt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtCurdate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        formTabbedPane.addTab(AppLocal.getIntString("label.general"), jPanel5); // NOI18N

        jPanel1.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        jLabel19.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel19.setText(AppLocal.getIntString("label.firstname")); // NOI18N
        jLabel19.setAlignmentX(0.5F);
        jLabel19.setPreferredSize(new java.awt.Dimension(0, 30));

        txtFirstName.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        txtFirstName.setPreferredSize(new java.awt.Dimension(200, 30));

        jLabel15.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel15.setText(AppLocal.getIntString("label.lastname")); // NOI18N
        jLabel15.setPreferredSize(new java.awt.Dimension(0, 30));

        txtLastName.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        txtLastName.setPreferredSize(new java.awt.Dimension(200, 30));

        jLabel16.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel16.setText(AppLocal.getIntString("label.email")); // NOI18N
        jLabel16.setPreferredSize(new java.awt.Dimension(0, 30));

        txtEmail.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        txtEmail.setPreferredSize(new java.awt.Dimension(200, 30));

        jLabel17.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel17.setText(AppLocal.getIntString("label.phone")); // NOI18N
        jLabel17.setPreferredSize(new java.awt.Dimension(0, 30));

        txtPhone.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        txtPhone.setPreferredSize(new java.awt.Dimension(200, 30));

        jLabel18.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel18.setText(AppLocal.getIntString("label.phone2")); // NOI18N
        jLabel18.setPreferredSize(new java.awt.Dimension(0, 30));

        txtPhone2.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        txtPhone2.setPreferredSize(new java.awt.Dimension(200, 30));

        jLabel14.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel14.setText(AppLocal.getIntString("label.fax")); // NOI18N
        jLabel14.setPreferredSize(new java.awt.Dimension(0, 30));

        txtFax.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        txtFax.setPreferredSize(new java.awt.Dimension(200, 30));

        webBtnMail.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        webBtnMail.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/mail24.png"))); // NOI18N
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("pos_messages"); // NOI18N
        webBtnMail.setText(bundle.getString("button.email")); // NOI18N
        webBtnMail.setPreferredSize(new java.awt.Dimension(90, 30));
        webBtnMail.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                webBtnMailActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtLastName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtFax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel17, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtPhone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtEmail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtPhone2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel19, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtFirstName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(webBtnMail, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel19, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtFirstName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtLastName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtEmail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(webBtnMail, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel17, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtPhone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtPhone2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtFax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(57, 57, 57))
        );

        formTabbedPane.addTab(AppLocal.getIntString("label.contact"), jPanel1); // NOI18N

        jLabel13.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel13.setText(AppLocal.getIntString("label.address")); // NOI18N
        jLabel13.setPreferredSize(new java.awt.Dimension(110, 30));

        txtAddress.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        txtAddress.setPreferredSize(new java.awt.Dimension(200, 30));

        jLabel20.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel20.setText(AppLocal.getIntString("label.country")); // NOI18N
        jLabel20.setPreferredSize(new java.awt.Dimension(110, 30));

        txtCountry.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        txtCountry.setPreferredSize(new java.awt.Dimension(200, 30));

        jLabel21.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel21.setText(AppLocal.getIntString("label.address2")); // NOI18N
        jLabel21.setPreferredSize(new java.awt.Dimension(110, 30));

        txtAddress2.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        txtAddress2.setPreferredSize(new java.awt.Dimension(200, 30));

        jLabel22.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel22.setText(AppLocal.getIntString("label.postal")); // NOI18N
        jLabel22.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        jLabel22.setPreferredSize(new java.awt.Dimension(110, 30));

        txtPostal.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        txtPostal.setPreferredSize(new java.awt.Dimension(0, 30));

        jLabel23.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel23.setText(AppLocal.getIntString("label.city")); // NOI18N
        jLabel23.setPreferredSize(new java.awt.Dimension(110, 30));

        txtCity.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        txtCity.setPreferredSize(new java.awt.Dimension(200, 30));

        jLabel24.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel24.setText(AppLocal.getIntString("label.region")); // NOI18N
        jLabel24.setPreferredSize(new java.awt.Dimension(110, 30));

        txtRegion.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        txtRegion.setPreferredSize(new java.awt.Dimension(200, 30));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel20, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel24, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel23, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel21, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(txtCity, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(txtAddress, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(txtAddress2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(txtRegion, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(txtCountry, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addComponent(jLabel22, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtPostal, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtAddress, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel21, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtAddress2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel23, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtCity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel22, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtPostal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel24, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtRegion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel20, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtCountry, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        formTabbedPane.addTab(AppLocal.getIntString("label.locationaddress"), jPanel2); // NOI18N

        jPanel4.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jPanel4.setPreferredSize(new java.awt.Dimension(535, 0));

        jBtnShowTrans.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jBtnShowTrans.setText(bundle.getString("button.SupplierTrans")); // NOI18N
        jBtnShowTrans.setToolTipText("");
        jBtnShowTrans.setPreferredSize(new java.awt.Dimension(140, 30));
        jBtnShowTrans.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnShowTransActionPerformed(evt);
            }
        });

        jScrollPane3.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        jTableSupplierTransactions.setAutoCreateRowSorter(true);
        jTableSupplierTransactions.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jTableSupplierTransactions.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Date", "Product", "Qty", "Price", "Reason"
            }
        ));
        jTableSupplierTransactions.setGridColor(new java.awt.Color(102, 204, 255));
        jTableSupplierTransactions.setOpaque(false);
        jTableSupplierTransactions.setPreferredSize(new java.awt.Dimension(375, 150));
        jTableSupplierTransactions.setRowHeight(25);
        jScrollPane3.setViewportView(jTableSupplierTransactions);

        jLblTranCount.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLblTranCount.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLblTranCount.setOpaque(true);
        jLblTranCount.setPreferredSize(new java.awt.Dimension(50, 30));

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 622, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jBtnShowTrans, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLblTranCount, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jBtnShowTrans, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLblTranCount, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        formTabbedPane.addTab(bundle.getString("label.SupplierTransactions"), jPanel4); // NOI18N

        m_jNotes.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jNotes.setPreferredSize(new java.awt.Dimension(0, 0));
        jScrollPane1.setViewportView(m_jNotes);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 668, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 226, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(55, Short.MAX_VALUE))
        );

        formTabbedPane.addTab(AppLocal.getIntString("label.notes"), jPanel3); // NOI18N

        add(formTabbedPane, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void webBtnMailActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_webBtnMailActionPerformed

        if (!"".equals(txtEmail.getText())) {
            Desktop desktop;

            if (Desktop.isDesktopSupported()
                    && (desktop = Desktop.getDesktop()).isSupported(Desktop.Action.MAIL)) {
                try {
                    URI mailto = new URI("mailto:"+ txtEmail.getText());
                    desktop.mail(mailto);
                } catch (IOException | URISyntaxException ex) {
                    LOGGER.log(Level.SEVERE, "Exception mailto", ex);
                }
            } else {
                JOptionPane.showMessageDialog(this,
                        AppLocal.getIntString("message.email"),
                        "Email", JOptionPane.INFORMATION_MESSAGE);
            }
        }

    }//GEN-LAST:event_webBtnMailActionPerformed

    private void jBtnShowTransActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnShowTransActionPerformed
        
        if (m_oId != null) {
            String cId = m_oId.toString();
            TransactionTableModel transactionModel = new TransactionTableModel(getTransactionOfName(cId));
            jTableSupplierTransactions.setModel(transactionModel);
            if (transactionModel.getRowCount() > 0) {
                jTableSupplierTransactions.setVisible(true);
                String TranCount = String.valueOf(transactionModel.getRowCount());
                jLblTranCount.setText(TranCount + " for " + m_jName.getText());
            } else {
                jTableSupplierTransactions.setVisible(false);
                JOptionPane.showMessageDialog(this,
                        AppLocal.getIntString("message.nosupptranx"),
                        AppLocal.getIntString("label.Transactions"), 
                        JOptionPane.INFORMATION_MESSAGE);
            }
            resetTranxTable();

            repaint();
            refresh();
        }
    }//GEN-LAST:event_jBtnShowTransActionPerformed

    private void jLabel7MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel7MouseClicked

        if (evt.getClickCount() == 2) {
            String uuidString = m_oId.toString();
            StringSelection stringSelection = new StringSelection(uuidString);
            Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
            clpbrd.setContents(stringSelection, null);

            JOptionPane.showMessageDialog(this,
                    AppLocal.getIntString("message.uuidcopy"));
        }
    }//GEN-LAST:event_jLabel7MouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTabbedPane formTabbedPane;
    private javax.swing.JButton jBtnShowTrans;
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
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLblTranCount;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTable jTableSupplierTransactions;
    private javax.swing.JLabel lblMaxdebt;
    private javax.swing.JTextField m_jName;
    private javax.swing.JLabel m_jName_Label;
    private javax.swing.JTextArea m_jNotes;
    private javax.swing.JTextField m_jSearchkey;
    private javax.swing.JLabel m_jSearchkey_Label;
    private javax.swing.JTextField m_jTaxID;
    private javax.swing.JTextField m_jVATID;
    private javax.swing.JCheckBox m_jVisible;
    private javax.swing.JTextField txtAddress;
    private javax.swing.JTextField txtAddress2;
    private javax.swing.JTextField txtCity;
    private javax.swing.JTextField txtCountry;
    private javax.swing.JTextField txtCurdate;
    private javax.swing.JTextField txtCurdebt;
    private javax.swing.JTextField txtEmail;
    private javax.swing.JTextField txtFax;
    private javax.swing.JTextField txtFirstName;
    private javax.swing.JTextField txtLastName;
    private javax.swing.JTextField txtMaxdebt;
    private javax.swing.JTextField txtPhone;
    private javax.swing.JTextField txtPhone2;
    private javax.swing.JTextField txtPostal;
    private javax.swing.JTextField txtRegion;
    private javax.swing.JButton webBtnMail;
    // End of variables declaration//GEN-END:variables

}
