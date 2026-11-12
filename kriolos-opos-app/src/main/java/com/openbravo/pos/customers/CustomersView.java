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
package com.openbravo.pos.customers;

import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.ComboBoxValModel;
import com.openbravo.data.loader.SentenceList;
import com.openbravo.data.user.DirtyManager;
import com.openbravo.data.user.EditorRecord;
import com.openbravo.format.Formats;
import com.openbravo.basic.AppLocal;
import com.openbravo.pos.panels.AppView;
import com.openbravo.pos.container.BeanFactoryException;
import com.openbravo.pos.sales.DataLogicTax;
import com.openbravo.pos.sales.TaxService;
import com.openbravo.ui.data.binding.Converter;
import com.openbravo.ui.data.binding.EmailValidator;
import com.openbravo.ui.data.binding.SwingBinder;
import com.openbravo.util.StringUtils;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;
import com.openbravo.beans.JCalendarPanel2;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import javax.swing.table.JTableHeader;

/**
 *
 * @author Jack Gerrard
 */
public final class CustomersView extends javax.swing.JPanel implements EditorRecord<CustomerInfoExt> {

    private final static System.Logger LOGGER = System.getLogger(CustomersView.class.getName());
    private static final long serialVersionUID = 1L;
    private String m_oId;
    private SwingBinder<CustomerInfoExt> binder;

    private SentenceList m_sentcat;
    private ComboBoxValModel m_CategoryModel;

    private DirtyManager m_Dirty;
    private CustomerService customerService;
    @Deprecated
    private DataLogicCustomers dlCustomers;
    private TaxService taxService;
    @Deprecated
    private DataLogicTax dlTax;

    //HS updates to get last added Customer 06.03.2014
    private AppView appView;
    private CustomerInfo customerInfo;

    /**
     * Creates new form CustomersView
     *
     * @param app
     * @param dirty
     */
    public CustomersView(AppView app, DirtyManager dirty) {
        this(app, dirty, app != null ? app.getBean(CustomerService.class) : null, app != null ? app.getBean(TaxService.class) : null);
    }

    public CustomersView(CustomerService customerService, TaxService taxService, DirtyManager dirty) {
        this(null, dirty, customerService, taxService);
    }

    public CustomersView(AppView app, DirtyManager dirty, CustomerService customerService, TaxService taxService) {
        try {
            appView = app;
            this.customerService = customerService != null ? customerService : (app != null ? app.getBean(CustomerService.class) : null);
            this.dlCustomers = (this.customerService instanceof DataLogicCustomers) ? (DataLogicCustomers) this.customerService : (app != null ? app.getBean(DataLogicCustomers.class) : null);
            this.taxService = taxService != null ? taxService : (app != null ? app.getBean(TaxService.class) : null);
            this.dlTax = (this.taxService instanceof DataLogicTax) ? (DataLogicTax) this.taxService : null;

            initComponents();

            m_sentcat = this.taxService != null ? this.taxService.getTaxCustCategoriesList() : null;
            m_CategoryModel = new ComboBoxValModel();

            m_Dirty = dirty;
            m_jTaxID.getDocument().addDocumentListener(dirty);
            m_jSearchkey.getDocument().addDocumentListener(dirty);
            m_jName.getDocument().addDocumentListener(dirty);
            m_jCategory.addActionListener(dirty);
            m_jNotes.getDocument().addDocumentListener(dirty);
            txtMaxdebt.getDocument().addDocumentListener(dirty);
            txtCurdebt.getDocument().addDocumentListener(dirty);
            txtCurdate.getDocument().addDocumentListener(dirty);
            m_jVisible.addActionListener(dirty);
            m_jVip.addActionListener(dirty);
            txtDiscount.getDocument().addDocumentListener(dirty);

            txtFirstName.getDocument().addDocumentListener(dirty);
            txtLastName.getDocument().addDocumentListener(dirty);
            txtEmail.getDocument().addDocumentListener(dirty);
            txtPhone.getDocument().addDocumentListener(dirty);
            txtPhone2.getDocument().addDocumentListener(dirty);
            txtFax.getDocument().addDocumentListener(dirty);
            m_jImage.addPropertyChangeListener(dirty);

            txtAddress.getDocument().addDocumentListener(dirty);
            txtAddress2.getDocument().addDocumentListener(dirty);
            txtPostal.getDocument().addDocumentListener(dirty);
            txtCity.getDocument().addDocumentListener(dirty);
            txtRegion.getDocument().addDocumentListener(dirty);
            txtCountry.getDocument().addDocumentListener(dirty);

            m_jdate.getDocument().addDocumentListener(dirty);

            initValidator();
            init();
        } catch (BeanFactoryException ex) {
            LOGGER.log(System.Logger.Level.WARNING, "", ex);
        }
    }

    private void initValidator() {
        binder = new SwingBinder<>(new CustomerInfoExt());

        // Mandatory Customer Name
        binder.forField(m_jName)
                .asRequired(AppLocal.getIntString("validation.required", AppLocal.getIntString("label.name")))
                .bind(CustomerInfoExt::getName, CustomerInfoExt::setName);

        // Optional valid Email Address
        binder.forField(txtEmail)
                .withValidator(new EmailValidator(AppLocal.getIntString("validation.email.invalid")))
                .bind(CustomerInfoExt::getEmail, CustomerInfoExt::setEmail);

        // Max Debt (Currency format)
        binder.forField(txtMaxdebt)
                .withConverter(new CurrencyDoubleConverter())
                .withValidator(v -> v == null || v >= 0.0, AppLocal.getIntString("validation.number.positive", AppLocal.getIntString("label.maxdebt")))
                .bind(CustomerInfoExt::getMaxdebt, CustomerInfoExt::setMaxdebt);

        // Discount
        binder.forField(txtDiscount)
                .withConverter(new DoubleFormatConverter())
                .withValidator(v -> v == null || v >= 0.0, AppLocal.getIntString("validation.number.positive", AppLocal.getIntString("label.discount")))
                .bind(CustomerInfoExt::getDiscount, CustomerInfoExt::setDiscount);

        // General text fields
        binder.forField(m_jSearchkey).bind(CustomerInfoExt::getSearchkey, CustomerInfoExt::setSearchkey);
        binder.forField(m_jTaxID).bind(CustomerInfoExt::getTaxid, CustomerInfoExt::setTaxid);
        binder.forField(jcard).bind(CustomerInfoExt::getCard, CustomerInfoExt::setCard);
        binder.forField(txtFirstName).bind(CustomerInfoExt::getFirstname, CustomerInfoExt::setFirstname);
        binder.forField(txtLastName).bind(CustomerInfoExt::getLastname, CustomerInfoExt::setLastname);
        binder.forField(txtPhone).bind(CustomerInfoExt::getPhone, CustomerInfoExt::setPhone);
        binder.forField(txtPhone2).bind(CustomerInfoExt::getPhone2, CustomerInfoExt::setPhone2);
        binder.forField(txtFax).bind(CustomerInfoExt::getFax, CustomerInfoExt::setFax);
        binder.forField(txtAddress).bind(CustomerInfoExt::getAddress, CustomerInfoExt::setAddress);
        binder.forField(txtAddress2).bind(CustomerInfoExt::getAddress2, CustomerInfoExt::setAddress2);
        binder.forField(txtPostal).bind(CustomerInfoExt::getPostal, CustomerInfoExt::setPostal);
        binder.forField(txtCity).bind(CustomerInfoExt::getCity, CustomerInfoExt::setCity);
        binder.forField(txtRegion).bind(CustomerInfoExt::getRegion, CustomerInfoExt::setRegion);
        binder.forField(txtCountry).bind(CustomerInfoExt::getCountry, CustomerInfoExt::setCountry);
        binder.forField(m_jNotes).bind(CustomerInfoExt::getNotes, CustomerInfoExt::setNotes);

        // Boolean Checkboxes
        binder.forField(m_jVisible).bind(CustomerInfoExt::isVisible, CustomerInfoExt::setVisible);
        binder.forField(m_jVip).bind(CustomerInfoExt::isVIP, CustomerInfoExt::setisVIP);

        // Status change synchronization with DirtyManager
        binder.addStatusChangeListener(() -> {
            if (m_Dirty != null) {
                m_Dirty.setDirty(true);
            }
        });
    }

    public boolean isValid() {
        return binder == null || binder.validate();
    }

    public SwingBinder<CustomerInfoExt> getBinder() {
        return binder;
    }

    private static class CurrencyDoubleConverter implements Converter<String, Double> {
        @Override
        public Double toModel(String p) {
            if (p == null || p.trim().isEmpty()) {
                return null;
            }
            try {
                return (Double) Formats.CURRENCY.parseValue(p.trim(), 0.0);
            } catch (BasicException e) {
                try {
                    String cleaned = p.replaceAll("[^0-9.,\\-]", "").trim();
                    if (cleaned.contains(",") && !cleaned.contains(".")) {
                        cleaned = cleaned.replace(',', '.');
                    } else if (cleaned.contains(",") && cleaned.contains(".")) {
                        cleaned = cleaned.replace(",", "");
                    }
                    return cleaned.isEmpty() ? null : Double.parseDouble(cleaned);
                } catch (Exception ignored) {
                    return null;
                }
            }
        }

        @Override
        public String toPresentation(Double m) {
            return m != null ? Formats.CURRENCY.formatValue(m) : "";
        }
    }

    private static class DoubleFormatConverter implements Converter<String, Double> {
        @Override
        public Double toModel(String p) {
            if (p == null || p.trim().isEmpty()) {
                return null;
            }
            try {
                return (Double) Formats.DOUBLE.parseValue(p.trim(), 0.0);
            } catch (BasicException e) {
                try {
                    String cleaned = p.replaceAll("[^0-9.,\\-]", "").trim().replace(',', '.');
                    return cleaned.isEmpty() ? null : Double.parseDouble(cleaned);
                } catch (Exception ignored) {
                    return null;
                }
            }
        }

        @Override
        public String toPresentation(Double m) {
            return m != null ? Formats.DOUBLE.formatValue(m) : "";
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
    public void activate() throws BasicException {

        List a = new ArrayList();
        a.add(null); // The null item
        if (m_sentcat != null) {
            List cats = m_sentcat.list();
            if (cats != null) {
                a.addAll(cats);
            }
        }
        m_CategoryModel = new ComboBoxValModel(a);
        m_jCategory.setModel(m_CategoryModel);
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
        if (binder != null) {
            binder.clear();
        }
        m_jTaxID.setText(null);
        m_jSearchkey.setText(null);
        m_jName.setText(null);
        m_CategoryModel.setSelectedKey(null);
        m_jNotes.setText(null);

        txtMaxdebt.setText(null);
        txtCurdebt.setText(null);
        txtCurdate.setText(null);
        m_jVisible.setSelected(false);
        m_jVip.setSelected(false);
        txtDiscount.setText(null);
        jcard.setText(null);

        txtFirstName.setText(null);
        txtLastName.setText(null);
        txtEmail.setText(null);
        txtPhone.setText(null);
        txtPhone2.setText(null);
        txtFax.setText(null);
        m_jImage.setImage(null);

        txtAddress.setText(null);
        txtAddress2.setText(null);
        txtPostal.setText(null);
        txtCity.setText(null);
        txtRegion.setText(null);
        txtCountry.setText(null);

        m_jdate.setText(null);

        m_jTaxID.setEnabled(false);
        m_jSearchkey.setEnabled(false);
        m_jName.setEnabled(false);
        m_jCategory.setEnabled(false);
        m_jNotes.setEnabled(false);
        txtMaxdebt.setEnabled(false);
        txtCurdebt.setEnabled(false);
        txtCurdate.setEnabled(false);
        m_jVisible.setEnabled(false);
        m_jVip.setEnabled(false);
        txtDiscount.setEnabled(false);
        jcard.setEnabled(false);

        txtFirstName.setEnabled(false);
        txtLastName.setEnabled(false);
        txtEmail.setEnabled(false);
        txtPhone.setEnabled(false);
        txtPhone2.setEnabled(false);
        txtFax.setEnabled(false);
        m_jImage.setEnabled(false);

        txtAddress.setEnabled(false);
        txtAddress2.setEnabled(false);
        txtPostal.setEnabled(false);
        txtCity.setEnabled(false);
        txtRegion.setEnabled(false);
        txtCountry.setEnabled(false);

        jBtnCreateCard.setEnabled(false);
        jBtnClearCard.setEnabled(false);

        m_jdate.setEnabled(false);
        m_jbtndate.setEnabled(false);

        jTableCustomerTransactions.setEnabled(false);

        repaint();
        refresh();
    }

    @Override
    public void writeValueInsert() {

        m_oId = null;
        m_jTaxID.setText(null);
        m_jSearchkey.setText(null);
        m_jName.setText(null);
        txtPhone.setText(null);
        txtEmail.setText(null);

        m_CategoryModel.setSelectedKey(null);
        m_jNotes.setText(null);
        txtMaxdebt.setText(null);
        txtCurdebt.setText(null);
        txtCurdate.setText(null);
        m_jVisible.setSelected(true);
        m_jVip.setSelected(false);
        txtDiscount.setText(null);
        jcard.setText(null);

        txtFirstName.setText(null);
        txtLastName.setText(null);
        txtPhone2.setText(null);
        txtFax.setText(null);
        m_jImage.setImage(null);

        txtAddress.setText(null);
        txtAddress2.setText(null);
        txtPostal.setText(null);
        txtCity.setText(null);
        txtRegion.setText(null);
        txtCountry.setText(null);

        m_jdate.setText(null);

        m_jTaxID.setEnabled(true);
        m_jSearchkey.setEnabled(true);
        m_jName.setEnabled(true);
        m_jCategory.setEnabled(true);
        m_jNotes.setEnabled(true);
        txtMaxdebt.setEnabled(true);
        txtCurdebt.setEnabled(true);
        txtCurdate.setEnabled(true);
        m_jVisible.setEnabled(true);
        m_jVip.setEnabled(true);
        txtDiscount.setEnabled(true);
        jcard.setEnabled(true);

        txtFirstName.setEnabled(true);
        txtLastName.setEnabled(true);
        txtEmail.setEnabled(true);
        webBtnMail.setEnabled(true);
        txtPhone.setEnabled(true);
        txtPhone2.setEnabled(true);
        txtFax.setEnabled(true);
        m_jImage.setEnabled(true);

        txtAddress.setEnabled(true);
        txtAddress2.setEnabled(true);
        txtPostal.setEnabled(true);
        txtCity.setEnabled(true);
        txtRegion.setEnabled(true);
        txtCountry.setEnabled(true);

        jBtnCreateCard.setEnabled(true);
        jBtnClearCard.setEnabled(true);

        jTableCustomerTransactions.setEnabled(false);

        m_jdate.setEnabled(true);
        m_jbtndate.setEnabled(true);

        repaint();
        refresh();
    }

    /**
     * Delete from object
     *
     * @param value
     */
    @Override
    public void writeValueDelete(CustomerInfoExt value) {

        setValues(value);

        m_jTaxID.setEnabled(false);
        m_jSearchkey.setEnabled(false);
        m_jName.setEnabled(false);
        m_jNotes.setEnabled(false);
        txtMaxdebt.setEnabled(false);
        txtCurdebt.setEnabled(false);
        txtCurdate.setEnabled(false);
        m_jVisible.setEnabled(false);
        m_jVip.setEnabled(false);
        txtDiscount.setEnabled(false);
        jcard.setEnabled(false);

        txtFirstName.setEnabled(false);
        txtLastName.setEnabled(false);
        txtEmail.setEnabled(false);
        webBtnMail.setEnabled(false);
        txtPhone.setEnabled(false);
        txtPhone2.setEnabled(false);
        txtFax.setEnabled(false);
        m_jImage.setEnabled(true);

        txtAddress.setEnabled(false);
        txtAddress2.setEnabled(false);
        txtPostal.setEnabled(false);
        txtCity.setEnabled(false);
        txtRegion.setEnabled(false);
        txtCountry.setEnabled(false);

        m_jCategory.setEnabled(false);

        jBtnCreateCard.setEnabled(false);
        jBtnClearCard.setEnabled(false);

        jTableCustomerTransactions.setEnabled(false);

        m_jdate.setEnabled(false);
        m_jbtndate.setEnabled(false);

        repaint();
        refresh();
    }
    
    private void setValues(CustomerInfoExt customer) {
        if (customer == null) {
            writeValueEOF();
            return;
        }
        m_oId = customer.getId();
        if (binder != null) {
            binder.readBean(customer);
        } else {
            m_jSearchkey.setText(customer.getSearchkey());
            m_jTaxID.setText(customer.getTaxid());
            m_jName.setText(customer.getName());
            jcard.setText(customer.getCard());
            txtMaxdebt.setText(Formats.CURRENCY.formatValue(customer.getMaxdebt()));
            txtAddress.setText(Formats.STRING.formatValue(customer.getAddress()));
            txtAddress2.setText(Formats.STRING.formatValue(customer.getAddress2()));
            txtPostal.setText(Formats.STRING.formatValue(customer.getPostal()));
            txtCity.setText(Formats.STRING.formatValue(customer.getCity()));
            txtRegion.setText(Formats.STRING.formatValue(customer.getRegion()));
            txtCountry.setText(Formats.STRING.formatValue(customer.getCountry()));
            txtFirstName.setText(Formats.STRING.formatValue(customer.getFirstname()));
            txtLastName.setText(Formats.STRING.formatValue(customer.getLastname()));
            txtEmail.setText(Formats.STRING.formatValue(customer.getEmail()));
            txtPhone.setText(Formats.STRING.formatValue(customer.getPhone()));
            txtPhone2.setText(Formats.STRING.formatValue(customer.getPhone2()));
            txtFax.setText(Formats.STRING.formatValue(customer.getFax()));
            m_jNotes.setText(customer.getNotes());
            m_jVisible.setSelected(customer.isVisible());
            m_jVip.setSelected(customer.isVIP());
            txtDiscount.setText(Formats.DOUBLE.formatValue(customer.getDiscount()));
        }
        if (m_CategoryModel != null) {
            m_CategoryModel.setSelectedKey(customer.getTaxCustCategoryID());
        }
        txtCurdate.setText(Formats.TIMESTAMP.formatValue(customer.getCurdate()));
        txtCurdebt.setText(Formats.CURRENCY.formatValue(customer.getCurdebt()));
        m_jImage.setImage(customer.getImage());
        m_jdate.setText(Formats.TIMESTAMP.formatValue(customer.getMemoDate()));
    }

    /**
     * Edit object
     *
     * @param value
     */
    @Override
    public void writeValueEdit(CustomerInfoExt value) {
        setValues(value);

        m_jTaxID.setEnabled(true);
        m_jSearchkey.setEnabled(true);
        m_jName.setEnabled(true);
        m_jNotes.setEnabled(true);
        txtMaxdebt.setEnabled(true);
        txtCurdebt.setEnabled(true);
        txtCurdate.setEnabled(true);
        m_jVisible.setEnabled(true);
        m_jVip.setEnabled(true);
        txtDiscount.setEnabled(true);
        jcard.setEnabled(true);

        txtFirstName.setEnabled(true);
        txtLastName.setEnabled(true);
        txtEmail.setEnabled(true);
        webBtnMail.setEnabled(true);
        txtPhone.setEnabled(true);
        txtPhone2.setEnabled(true);
        txtFax.setEnabled(true);
        m_jImage.setEnabled(true);

        txtAddress.setEnabled(true);
        txtAddress2.setEnabled(true);
        txtPostal.setEnabled(true);
        txtCity.setEnabled(true);
        txtRegion.setEnabled(true);
        txtCountry.setEnabled(true);

        m_jCategory.setEnabled(true);

        m_jdate.setEnabled(true);
        m_jbtndate.setEnabled(true);

        jBtnCreateCard.setEnabled(true);
        jBtnClearCard.setEnabled(true);

        jTableCustomerTransactions.setVisible(false);
        jTableCustomerTransactions.setEnabled(true);
        resetTranxTable();

        jTableCustomerTransactions.repaint();
        repaint();
        refresh();
    }

    public void resetTranxTable() {

        jTableCustomerTransactions.getColumnModel().getColumn(0).setPreferredWidth(50);
        jTableCustomerTransactions.getColumnModel().getColumn(1).setPreferredWidth(70);
        jTableCustomerTransactions.getColumnModel().getColumn(2).setPreferredWidth(280);
        jTableCustomerTransactions.getColumnModel().getColumn(3).setPreferredWidth(30);
        jTableCustomerTransactions.getColumnModel().getColumn(4).setPreferredWidth(50);

        // set font for headers
        Font f = new Font("Arial", Font.BOLD, 14);
        JTableHeader header = jTableCustomerTransactions.getTableHeader();
        header.setFont(f);

        jTableCustomerTransactions.getTableHeader().setReorderingAllowed(true);
        jTableCustomerTransactions.setAutoCreateRowSorter(true);
        jTableCustomerTransactions.repaint();

    }

    /**
     * Create object
     *
     * @return
     * @throws BasicException
     */
    @Override
    public CustomerInfoExt createValue() throws BasicException {

        CustomerInfoExt customer = new CustomerInfoExt(m_oId == null ? UUID.randomUUID().toString() : m_oId);

        if (binder != null) {
            try {
                binder.writeBean(customer);
            } catch (SwingBinder.ValidationException ex) {
                LOGGER.log(System.Logger.Level.WARNING, "Validation in createValue: " + ex.getMessage());
            }
        }

        if (customer.getName() == null || customer.getName().isEmpty()) {
            customer.setName(m_jName.getText());
        }
        if (customer.getSearchkey() == null) {
            customer.setSearchkey(m_jSearchkey.getText());
        }
        if (customer.getTaxid() == null) {
            customer.setTaxid(m_jTaxID.getText());
        }
        if (m_CategoryModel != null) {
            customer.setTaxCustCategoryID((String) m_CategoryModel.getSelectedKey());
        }
        if (customer.getCard() == null) {
            customer.setCard((String) Formats.STRING.parseValue(jcard.getText()));
        }
        if (customer.getMaxdebt() == null) {
            customer.setMaxdebt((Double) Formats.CURRENCY.parseValue(txtMaxdebt.getText(), 0.0));
        }
        if (customer.getAddress() == null) {
            customer.setAddress((String) Formats.STRING.parseValue(txtAddress.getText()));
        }
        if (customer.getAddress2() == null) {
            customer.setAddress2((String) Formats.STRING.parseValue(txtAddress2.getText()));
        }
        if (customer.getPostal() == null) {
            customer.setPostal((String) Formats.STRING.parseValue(txtPostal.getText()));
        }
        if (customer.getCity() == null) {
            customer.setCity((String) Formats.STRING.parseValue(txtCity.getText()));
        }
        if (customer.getRegion() == null) {
            customer.setRegion((String) Formats.STRING.parseValue(txtRegion.getText()));
        }
        if (customer.getCountry() == null) {
            customer.setCountry((String) Formats.STRING.parseValue(txtCountry.getText()));
        }
        if (customer.getFirstname() == null) {
            customer.setFirstname((String) Formats.STRING.parseValue(txtFirstName.getText()));
        }
        if (customer.getLastname() == null) {
            customer.setLastname((String) Formats.STRING.parseValue(txtLastName.getText()));
        }
        if (customer.getEmail() == null) {
            customer.setEmail((String) Formats.STRING.parseValue(txtEmail.getText()));
        }
        if (customer.getPhone() == null) {
            customer.setPhone((String) Formats.STRING.parseValue(txtPhone.getText()));
        }
        if (customer.getPhone2() == null) {
            customer.setPhone2((String) Formats.STRING.parseValue(txtPhone2.getText()));
        }
        if (customer.getFax() == null) {
            customer.setFax((String) Formats.STRING.parseValue(txtFax.getText()));
        }
        if (customer.getNotes() == null) {
            customer.setNotes(m_jNotes.getText());
        }
        customer.setVisible(m_jVisible.isSelected());
        customer.setCurdate((Date) Formats.TIMESTAMP.parseValue(txtCurdate.getText()));
        customer.setCurdebt((Double) Formats.CURRENCY.parseValue(txtCurdebt.getText()));
        customer.setImage(m_jImage.getImage());
        customer.setisVIP(m_jVip.isSelected());
        if (customer.getDiscount() == null) {
            customer.setDiscount((Double) Formats.CURRENCY.parseValue(txtDiscount.getText(), 0.0));
        }
        customer.setMemoDate((Date) Formats.TIMESTAMP.parseValue(m_jdate.getText()));

        return customer;
    }

    @Override
    public Component getComponent() {
        return this;
    }

// JG 3 Oct 2013 - Customer Transaction List
// JG 10 April 2016 - Revision    
    private List<CustomerTransaction> getTransactionOfName(String cId) {

        List<CustomerTransaction> customerTransactionList = new ArrayList<>();
        try {
            customerTransactionList = customerService.getCustomersTransactionList(cId);
            for (CustomerTransaction customerTransaction : customerTransactionList) {
                String customerId = customerTransaction.getCustomerId();
                if (!customerId.equals(cId)) {
                    customerTransactionList.remove(customerTransaction);
                }
            }
        } catch (BasicException ex) {
            LOGGER.log(System.Logger.Level.WARNING, "", ex);
        }

        txtCurdate.repaint();
        txtCurdebt.repaint();

        repaint();
        refresh();

        return customerTransactionList;
    }

    class TransactionTableModel extends AbstractTableModel {

        String tkt = AppLocal.getIntString("label.tblHeaderCol1");
        String dte = AppLocal.getIntString("label.tblHeaderCol2");
        String prd = AppLocal.getIntString("label.tblHeaderCol3");
        String qty = AppLocal.getIntString("label.tblHeaderCol4");
        String ttl = AppLocal.getIntString("label.tblHeaderCol5");

        List<CustomerTransaction> transactionList;
        String[] columnNames = {tkt, dte, prd, qty, ttl};
        public Double Tamount;

        public TransactionTableModel(List<CustomerTransaction> list) {
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
            CustomerTransaction customerTransaction = transactionList.get(row);

            jTableCustomerTransactions.setRowHeight(25);

            switch (column) {

                case 0:
                    return customerTransaction.getTicketId();
                case 1:
                    Date transactionDate = customerTransaction.getTransactionDate();
                    String formattedDate = Formats.TIMESTAMP.formatValue(transactionDate);
                    return formattedDate;
                case 2:
                    return customerTransaction.getProductName();
                case 3:
                    return customerTransaction.getUnit();
                case 4:
                    Double amount = customerTransaction.getTotal();
                    DecimalFormat df = new DecimalFormat("#.##");
                    String formattedAmount = df.format(amount);
                    return formattedAmount;
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

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanelGeneral = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        m_jSearchkey = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        m_jName = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jcard = new javax.swing.JTextField();
        txtDiscount = new javax.swing.JTextField();
        jBtnCreateCard = new javax.swing.JButton();
        jLblDiscountpercent = new javax.swing.JLabel();
        jBtnClearCard = new javax.swing.JButton();
        m_jbtndate = new javax.swing.JButton();
        jLabel9 = new javax.swing.JLabel();
        m_jdate = new javax.swing.JTextField();
        m_jCategory = new javax.swing.JComboBox();
        jLabel10 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        m_jVisible = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();
        txtMaxdebt = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        txtCurdebt = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        txtCurdate = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        m_jTaxID = new javax.swing.JTextField();
        jLblVIP = new javax.swing.JLabel();
        m_jVip = new javax.swing.JCheckBox();
        jLblDiscount = new javax.swing.JLabel();
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
        jTableCustomerTransactions = new javax.swing.JTable();
        jLblTranCount = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        m_jImage = new com.openbravo.data.gui.JImageEditor();
        jLabel34 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        m_jNotes = new javax.swing.JTextArea();

        setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        setPreferredSize(new java.awt.Dimension(700, 400));

        jTabbedPane1.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jTabbedPane1.setPreferredSize(new java.awt.Dimension(650, 300));

        jLabel8.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel8.setText(AppLocal.getIntString("label.searchkeym")); // NOI18N
        jLabel8.setPreferredSize(new java.awt.Dimension(100, 30));

        m_jSearchkey.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jSearchkey.setPreferredSize(new java.awt.Dimension(140, 30));

        jLabel3.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel3.setText(AppLocal.getIntString("label.namem")); // NOI18N
        jLabel3.setMaximumSize(new java.awt.Dimension(140, 25));
        jLabel3.setMinimumSize(new java.awt.Dimension(140, 25));
        jLabel3.setPreferredSize(new java.awt.Dimension(150, 30));

        m_jName.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jName.setPreferredSize(new java.awt.Dimension(410, 30));

        jLabel5.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel5.setText(AppLocal.getIntString("label.card")); // NOI18N
        jLabel5.setMaximumSize(new java.awt.Dimension(140, 25));
        jLabel5.setMinimumSize(new java.awt.Dimension(140, 25));
        jLabel5.setPreferredSize(new java.awt.Dimension(150, 30));

        jcard.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jcard.setPreferredSize(new java.awt.Dimension(150, 30));

        txtDiscount.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        txtDiscount.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtDiscount.setPreferredSize(new java.awt.Dimension(50, 30));

        jBtnCreateCard.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/encrypted.png"))); // NOI18N
        jBtnCreateCard.setToolTipText("Create Key");
        jBtnCreateCard.setMaximumSize(new java.awt.Dimension(64, 32));
        jBtnCreateCard.setMinimumSize(new java.awt.Dimension(64, 32));
        jBtnCreateCard.setPreferredSize(new java.awt.Dimension(40, 35));
        jBtnCreateCard.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnCreateCardActionPerformed(evt);
            }
        });

        jLblDiscountpercent.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLblDiscountpercent.setText("%");
        jLblDiscountpercent.setPreferredSize(new java.awt.Dimension(15, 30));

        jBtnClearCard.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/fileclose.png"))); // NOI18N
        jBtnClearCard.setToolTipText("Clear Key");
        jBtnClearCard.setMaximumSize(new java.awt.Dimension(64, 32));
        jBtnClearCard.setMinimumSize(new java.awt.Dimension(64, 32));
        jBtnClearCard.setPreferredSize(new java.awt.Dimension(40, 35));
        jBtnClearCard.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnClearCardActionPerformed(evt);
            }
        });

        m_jbtndate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/date.png"))); // NOI18N
        m_jbtndate.setToolTipText("Open Calendar");
        m_jbtndate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jbtndateActionPerformed(evt);
            }
        });

        jLabel9.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel9.setText(AppLocal.getIntString("label.custtaxcategory")); // NOI18N
        jLabel9.setToolTipText(AppLocal.getIntString("label.custtaxcategory")); // NOI18N
        jLabel9.setMaximumSize(new java.awt.Dimension(140, 25));
        jLabel9.setMinimumSize(new java.awt.Dimension(140, 25));
        jLabel9.setPreferredSize(new java.awt.Dimension(150, 30));

        m_jdate.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jdate.setPreferredSize(new java.awt.Dimension(130, 30));

        m_jCategory.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jCategory.setPreferredSize(new java.awt.Dimension(180, 30));

        jLabel10.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("pos_messages"); // NOI18N
        jLabel10.setText(bundle.getString("label.customerdate")); // NOI18N
        jLabel10.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        jLabel10.setPreferredSize(new java.awt.Dimension(130, 30));

        jLabel4.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel4.setText(AppLocal.getIntString("label.visible")); // NOI18N
        jLabel4.setMaximumSize(new java.awt.Dimension(140, 25));
        jLabel4.setMinimumSize(new java.awt.Dimension(140, 25));
        jLabel4.setPreferredSize(new java.awt.Dimension(150, 30));

        m_jVisible.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        m_jVisible.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        m_jVisible.setPreferredSize(new java.awt.Dimension(30, 30));

        jLabel1.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText(AppLocal.getIntString("label.maxdebt")); // NOI18N
        jLabel1.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        jLabel1.setMaximumSize(new java.awt.Dimension(140, 25));
        jLabel1.setMinimumSize(new java.awt.Dimension(120, 25));
        jLabel1.setPreferredSize(new java.awt.Dimension(130, 30));

        txtMaxdebt.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        txtMaxdebt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtMaxdebt.setPreferredSize(new java.awt.Dimension(130, 30));

        jLabel2.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText(AppLocal.getIntString("label.curdebt")); // NOI18N
        jLabel2.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        jLabel2.setMaximumSize(new java.awt.Dimension(140, 25));
        jLabel2.setMinimumSize(new java.awt.Dimension(120, 25));
        jLabel2.setPreferredSize(new java.awt.Dimension(130, 30));

        txtCurdebt.setEditable(false);
        txtCurdebt.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        txtCurdebt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtCurdebt.setPreferredSize(new java.awt.Dimension(130, 30));

        jLabel6.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel6.setText(AppLocal.getIntString("label.curdate")); // NOI18N
        jLabel6.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        jLabel6.setPreferredSize(new java.awt.Dimension(130, 30));

        txtCurdate.setEditable(false);
        txtCurdate.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        txtCurdate.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtCurdate.setPreferredSize(new java.awt.Dimension(130, 30));

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

        m_jTaxID.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jTaxID.setPreferredSize(new java.awt.Dimension(150, 30));

        jLblVIP.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLblVIP.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLblVIP.setText(AppLocal.getIntString("label.vip")); // NOI18N
        jLblVIP.setPreferredSize(new java.awt.Dimension(50, 30));

        m_jVip.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        m_jVip.setForeground(new java.awt.Color(0, 188, 243));
        m_jVip.setPreferredSize(new java.awt.Dimension(21, 30));
        m_jVip.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                m_jVipnone(evt);
            }
        });
        m_jVip.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jVipActionPerformed(evt);
            }
        });

        jLblDiscount.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLblDiscount.setText(AppLocal.getIntString("label.discount")); // NOI18N
        jLblDiscount.setPreferredSize(new java.awt.Dimension(70, 30));

        javax.swing.GroupLayout jPanelGeneralLayout = new javax.swing.GroupLayout(jPanelGeneral);
        jPanelGeneral.setLayout(jPanelGeneralLayout);
        jPanelGeneralLayout.setHorizontalGroup(
            jPanelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelGeneralLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(txtMaxdebt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLblDiscount, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelGeneralLayout.createSequentialGroup()
                        .addGroup(jPanelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanelGeneralLayout.createSequentialGroup()
                                .addGroup(jPanelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanelGeneralLayout.createSequentialGroup()
                                        .addComponent(m_jdate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(m_jbtndate, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txtCurdate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txtCurdebt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(jPanelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanelGeneralLayout.createSequentialGroup()
                                    .addGroup(jPanelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(jPanelGeneralLayout.createSequentialGroup()
                                            .addComponent(txtDiscount, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(jLblDiscountpercent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addGroup(jPanelGeneralLayout.createSequentialGroup()
                                            .addComponent(jcard, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addGap(30, 30, 30)))
                                    .addComponent(jBtnCreateCard, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(18, 18, 18)
                                    .addComponent(jBtnClearCard, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addComponent(m_jName, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 409, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(m_jCategory, javax.swing.GroupLayout.PREFERRED_SIZE, 281, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanelGeneralLayout.createSequentialGroup()
                        .addGroup(jPanelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(m_jTaxID, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(m_jSearchkey, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanelGeneralLayout.createSequentialGroup()
                                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(m_jVisible, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanelGeneralLayout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(jLblVIP, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(m_jVip, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(83, 83, 83))))
        );
        jPanelGeneralLayout.setVerticalGroup(
            jPanelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelGeneralLayout.createSequentialGroup()
                .addContainerGap(13, Short.MAX_VALUE)
                .addGroup(jPanelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(m_jTaxID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(m_jVisible, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(m_jSearchkey, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(m_jVip, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLblVIP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jBtnCreateCard, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jcard, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jBtnClearCard, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLblDiscountpercent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLblDiscount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtDiscount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanelGeneralLayout.createSequentialGroup()
                        .addGroup(jPanelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(m_jCategory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanelGeneralLayout.createSequentialGroup()
                                .addGroup(jPanelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(txtCurdate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txtMaxdebt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(jPanelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelGeneralLayout.createSequentialGroup()
                                    .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(m_jdate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addComponent(m_jbtndate, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(jPanelGeneralLayout.createSequentialGroup()
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtCurdebt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        jTabbedPane1.addTab(AppLocal.getIntString("label.general"), jPanelGeneral); // NOI18N

        jPanel1.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        jLabel19.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel19.setText(AppLocal.getIntString("label.firstname")); // NOI18N
        jLabel19.setAlignmentX(0.5F);
        jLabel19.setPreferredSize(new java.awt.Dimension(150, 30));

        txtFirstName.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        txtFirstName.setPreferredSize(new java.awt.Dimension(300, 30));

        jLabel15.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel15.setText(AppLocal.getIntString("label.lastname")); // NOI18N
        jLabel15.setPreferredSize(new java.awt.Dimension(150, 30));

        txtLastName.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        txtLastName.setPreferredSize(new java.awt.Dimension(300, 30));

        jLabel16.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel16.setText(AppLocal.getIntString("label.email")); // NOI18N
        jLabel16.setPreferredSize(new java.awt.Dimension(150, 30));

        txtEmail.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        txtEmail.setPreferredSize(new java.awt.Dimension(300, 30));

        jLabel17.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel17.setText(AppLocal.getIntString("label.phone")); // NOI18N
        jLabel17.setPreferredSize(new java.awt.Dimension(150, 30));

        txtPhone.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        txtPhone.setPreferredSize(new java.awt.Dimension(300, 30));

        jLabel18.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel18.setText(AppLocal.getIntString("label.phone2")); // NOI18N
        jLabel18.setPreferredSize(new java.awt.Dimension(150, 30));

        txtPhone2.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        txtPhone2.setPreferredSize(new java.awt.Dimension(300, 30));

        jLabel14.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel14.setText(AppLocal.getIntString("label.fax")); // NOI18N
        jLabel14.setPreferredSize(new java.awt.Dimension(150, 30));

        txtFax.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        txtFax.setPreferredSize(new java.awt.Dimension(300, 30));

        webBtnMail.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        webBtnMail.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/mail24.png"))); // NOI18N
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
                        .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtLastName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtFax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel17, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtPhone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtEmail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtPhone2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel19, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtFirstName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(webBtnMail, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(90, Short.MAX_VALUE))
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

        jTabbedPane1.addTab(AppLocal.getIntString("label.contact"), jPanel1); // NOI18N

        jPanel2.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        jLabel13.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel13.setText(AppLocal.getIntString("label.address")); // NOI18N
        jLabel13.setPreferredSize(new java.awt.Dimension(150, 30));

        txtAddress.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        txtAddress.setPreferredSize(new java.awt.Dimension(300, 30));

        jLabel20.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel20.setText(AppLocal.getIntString("label.country")); // NOI18N
        jLabel20.setPreferredSize(new java.awt.Dimension(150, 30));

        txtCountry.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        txtCountry.setPreferredSize(new java.awt.Dimension(300, 30));

        jLabel21.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel21.setText(AppLocal.getIntString("label.address2")); // NOI18N
        jLabel21.setPreferredSize(new java.awt.Dimension(150, 30));

        txtAddress2.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        txtAddress2.setPreferredSize(new java.awt.Dimension(300, 30));

        jLabel22.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel22.setText(AppLocal.getIntString("label.postal")); // NOI18N
        jLabel22.setPreferredSize(new java.awt.Dimension(110, 30));

        txtPostal.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        txtPostal.setPreferredSize(new java.awt.Dimension(0, 30));

        jLabel23.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel23.setText(AppLocal.getIntString("label.city")); // NOI18N
        jLabel23.setPreferredSize(new java.awt.Dimension(150, 30));

        txtCity.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        txtCity.setPreferredSize(new java.awt.Dimension(300, 30));

        jLabel24.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel24.setText(AppLocal.getIntString("label.region")); // NOI18N
        jLabel24.setPreferredSize(new java.awt.Dimension(150, 30));

        txtRegion.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        txtRegion.setPreferredSize(new java.awt.Dimension(300, 30));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel20, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel24, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel23, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel21, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel22, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtPostal, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(txtCity, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(txtAddress, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(txtAddress2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(txtRegion, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(txtCountry, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGap(113, 113, 113))
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
                    .addComponent(txtCity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel24, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtRegion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel22, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtPostal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel20, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtCountry, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jTabbedPane1.addTab(AppLocal.getIntString("label.locationaddress"), jPanel2); // NOI18N

        jPanel4.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jPanel4.setPreferredSize(new java.awt.Dimension(535, 0));

        jBtnShowTrans.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jBtnShowTrans.setText(bundle.getString("button.CustomerTrans")); // NOI18N
        jBtnShowTrans.setToolTipText("");
        jBtnShowTrans.setPreferredSize(new java.awt.Dimension(140, 30));
        jBtnShowTrans.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnShowTransActionPerformed(evt);
            }
        });

        jScrollPane3.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jScrollPane3.setPreferredSize(new java.awt.Dimension(620, 500));

        jTableCustomerTransactions.setAutoCreateRowSorter(true);
        jTableCustomerTransactions.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jTableCustomerTransactions.setModel(new javax.swing.table.DefaultTableModel(
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
                "TicketID", "Date", "Product", "Qty", "Total"
            }
        ));
        jTableCustomerTransactions.setGridColor(new java.awt.Color(102, 204, 255));
        jTableCustomerTransactions.setOpaque(false);
        jTableCustomerTransactions.setPreferredSize(new java.awt.Dimension(375, 500));
        jTableCustomerTransactions.setRowHeight(25);
        jScrollPane3.setViewportView(jTableCustomerTransactions);

        jLblTranCount.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLblTranCount.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLblTranCount.setOpaque(true);
        jLblTranCount.setPreferredSize(new java.awt.Dimension(50, 30));

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jBtnShowTrans, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLblTranCount, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(424, 424, 424))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLblTranCount, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jBtnShowTrans, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 260, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab(bundle.getString("label.Transactions"), jPanel4); // NOI18N

        m_jImage.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        m_jImage.setPreferredSize(new java.awt.Dimension(300, 250));

        jLabel34.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel34.setText(bundle.getString("label.imagesize")); // NOI18N
        jLabel34.setPreferredSize(new java.awt.Dimension(500, 30));

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel34, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jImage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(m_jImage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel34, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jTabbedPane1.addTab(bundle.getString("label.photo"), jPanel5); // NOI18N

        jPanel3.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        m_jNotes.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jNotes.setPreferredSize(new java.awt.Dimension(0, 0));
        jScrollPane1.setViewportView(m_jNotes);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 458, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 286, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab(AppLocal.getIntString("label.notes"), jPanel3); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 659, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 339, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jBtnCreateCardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnCreateCardActionPerformed
        if (JOptionPane.showConfirmDialog(this,
                AppLocal.getIntString("message.cardnew"),
                AppLocal.getIntString("title.editor"),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
            jcard.setText("c" + StringUtils.getCardNumber());
            m_Dirty.setDirty(true);
        }
    }//GEN-LAST:event_jBtnCreateCardActionPerformed

    private void jBtnClearCardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnClearCardActionPerformed
        if (JOptionPane.showConfirmDialog(this,
                AppLocal.getIntString("message.cardremove"),
                AppLocal.getIntString("title.editor"),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
            jcard.setText(null);
            m_Dirty.setDirty(true);
        }
    }//GEN-LAST:event_jBtnClearCardActionPerformed

    private void webBtnMailActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_webBtnMailActionPerformed

        if (!"".equals(txtEmail.getText())) {
            Desktop desktop;

            if (Desktop.isDesktopSupported()
                    && (desktop = Desktop.getDesktop()).isSupported(Desktop.Action.MAIL)) {
                URI mailto = null;
                try {
                    mailto = new URI("mailto:"
                            + txtEmail.getText());
                } catch (URISyntaxException ex) {
                    LOGGER.log(System.Logger.Level.WARNING, "", ex);
                }
                try {
                    desktop.mail(mailto);
                } catch (IOException ex) {
                    LOGGER.log(System.Logger.Level.WARNING, "", ex);
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
            jTableCustomerTransactions.setModel(transactionModel);
            if (transactionModel.getRowCount() > 0) {
                jTableCustomerTransactions.setVisible(true);
                String TranCount = String.valueOf(transactionModel.getRowCount());
                jLblTranCount.setText(TranCount + " for " + m_jName.getText());
            } else {
                jTableCustomerTransactions.setVisible(false);
                JOptionPane.showMessageDialog(this,
                        AppLocal.getIntString("message.nocustomertranx"),
                        AppLocal.getIntString("label.Transactions"),
                        JOptionPane.INFORMATION_MESSAGE);
            }
            resetTranxTable();
        } else {
            LOGGER.log(System.Logger.Level.DEBUG, "Customer ID is null");
            com.openbravo.util.NotifyUtils.beep();
        }
    }//GEN-LAST:event_jBtnShowTransActionPerformed

    private void m_jVipnone(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_m_jVipnone

    }//GEN-LAST:event_m_jVipnone

    private void m_jVipActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jVipActionPerformed

//        AbstractButton abstractButton = (AbstractButton)evt.getSource();
//        boolean selected = abstractButton.getModel().isSelected();
//        jLblDiscountpercent.setVisible(selected);
//        txtDiscount.setVisible(selected);
//        jLblDiscount.setVisible(selected);

    }//GEN-LAST:event_m_jVipActionPerformed

    private void m_jbtndateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jbtndateActionPerformed

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
    }//GEN-LAST:event_m_jbtndateActionPerformed

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
    private javax.swing.JButton jBtnClearCard;
    private javax.swing.JButton jBtnCreateCard;
    private javax.swing.JButton jBtnShowTrans;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
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
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLblDiscount;
    private javax.swing.JLabel jLblDiscountpercent;
    private javax.swing.JLabel jLblTranCount;
    private javax.swing.JLabel jLblVIP;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanelGeneral;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable jTableCustomerTransactions;
    private javax.swing.JTextField jcard;
    private javax.swing.JComboBox m_jCategory;
    private com.openbravo.data.gui.JImageEditor m_jImage;
    private javax.swing.JTextField m_jName;
    private javax.swing.JTextArea m_jNotes;
    private javax.swing.JTextField m_jSearchkey;
    private javax.swing.JTextField m_jTaxID;
    private javax.swing.JCheckBox m_jVip;
    private javax.swing.JCheckBox m_jVisible;
    private javax.swing.JButton m_jbtndate;
    private javax.swing.JTextField m_jdate;
    private javax.swing.JTextField txtAddress;
    private javax.swing.JTextField txtAddress2;
    private javax.swing.JTextField txtCity;
    private javax.swing.JTextField txtCountry;
    private javax.swing.JTextField txtCurdate;
    private javax.swing.JTextField txtCurdebt;
    private javax.swing.JTextField txtDiscount;
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
