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
package com.openbravo.pos.sales;

import com.openbravo.pos.forms.JPanelView;
import com.openbravo.pos.forms.DataLogicSales;
import com.openbravo.pos.forms.DataLogicSystem;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.basic.BasicException;
import com.openbravo.beans.JPasswordPanel;
import com.openbravo.data.gui.ComboBoxValModel;
import com.openbravo.data.gui.JMessagePanel;
import com.openbravo.data.gui.MessageInf;
import com.openbravo.data.gui.modal.PosUIModal;
import com.openbravo.data.loader.SentenceList;
import com.openbravo.pos.customers.CustomerInfo;
import com.openbravo.pos.customers.CustomerInfoExt;
import com.openbravo.pos.customers.CustomerInfoGlobal;
import com.openbravo.pos.customers.DataLogicCustomers;
import com.openbravo.pos.customers.JCustomerFinder;
import com.openbravo.pos.customers.JDialogNewCustomer;
import com.openbravo.pos.domain.utils.AmountCalculatorUtil;
import com.openbravo.pos.forms.*;
import com.openbravo.pos.inventory.LocationInfo;
import com.openbravo.pos.inventory.ProductStock;
import com.openbravo.pos.inventory.TaxCategoryInfo;
import com.openbravo.pos.pim.CategoryInfo;
import com.openbravo.pos.panels.JProductFinder;
import com.openbravo.pos.payment.JPaymentSelect;
import com.openbravo.pos.payment.JPaymentSelectReceipt;
import com.openbravo.pos.payment.JPaymentSelectRefund;
import com.openbravo.pos.sales.restaurant.PlaceServiceImpl;
import com.openbravo.pos.scripting.ScriptEngine;
import com.openbravo.pos.scripting.ScriptException;
import com.openbravo.pos.scripting.ScriptFactory;
import com.openbravo.pos.ticket.ProductInfoExt;
import com.openbravo.pos.ticket.TaxInfo;
import com.openbravo.pos.ticket.TicketInfo;
import com.openbravo.pos.ticket.TicketLineInfo;
import com.openbravo.pos.util.InactivityListener;

import com.openbravo.pos.payment.PaymentService;
import com.openbravo.pos.payment.PaymentServiceImpl;
import com.openbravo.pos.inventory.InventoryService;
import com.openbravo.pos.inventory.InventoryServiceImpl;
import com.openbravo.pos.pim.DataLogicPIM;
import com.openbravo.pos.reports.PrintReportUtils;
import java.awt.*;

import static java.awt.Window.getWindows;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;

/**
 *
 * @author JG uniCenta
 */
public abstract class JPanelTicket extends JPanel implements JPanelView, TicketsEditor {

    protected final static System.Logger LOGGER = System.getLogger(JPanelTicket.class.getName());

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
    private final static long serialVersionUID = 1L;

    private JTicketLines m_ticketlines;
    private JPanelButtons m_jbtnconfig;
    private AppView m_App;
    private DataLogicSystem dlSystem;
    private DataLogicSales dlSales;
    private DataLogicCustomers dlCustomers;
    private DataLogicPIM dataLogicPIM;
    private TicketsEditor m_panelticket;
    private TicketInfo m_oTicket;
    private String m_oTicketExt;

    private final SalesKeypadStateMachine keypadStateMachine = new SalesKeypadStateMachine();
    private int m_iNumberStatus;
    private int m_iNumberStatusInput;
    private int m_iNumberStatusPor;
    private StringBuffer m_sBarcode;

    private JTicketsBag m_ticketsbag;
    protected SalesPeripheralCoordinator peripheralCoordinator;
    private SentenceList senttax;

    private SentenceList senttaxcategories;
    // private ListKeyed taxcategoriescollection;
    private ComboBoxValModel taxcategoriesmodel;
    private TaxesLogic taxeslogic;
    private SalesService salesService;
    private TicketLineController ticketLineController;
    private SalesCustomerController salesCustomerController;
    private SalesPaymentCoordinator salesPaymentCoordinator;
    private SalesStockCoordinator salesStockCoordinator;
    private SalesBarcodeScanCoordinator salesBarcodeScanCoordinator;
    private PaymentService paymentService;
    private InventoryService inventoryService;
    private JPaymentSelect paymentdialogreceipt;
    private JPaymentSelect paymentdialogrefund;
    private InactivityListener inactivityListener;
    private DataLogicReceipts dlReceipts = null;
    private Boolean priceWith00;
    private PlaceServiceImpl restDB;
    private AppProperties m_config;
    // private Integer count = 0;
    // private Integer oCount = 0;

    /**
     * Creates new form JTicketView
     */
    public JPanelTicket(AppView app) {

        initComponents();

        LOGGER.log(System.Logger.Level.DEBUG, "JPanelTicket.init");
        m_config = app.getProperties();

        m_App = app;
        restDB = new PlaceServiceImpl(m_App.getSession());

        dlSystem = m_App.getBean(DataLogicSystem.class);
        dlSales = m_App.getBean(DataLogicSales.class);
        dlCustomers = m_App.getBean(DataLogicCustomers.class);
        dlReceipts = app.getBean(DataLogicReceipts.class);
        dataLogicPIM = app.getBean(DataLogicPIM.class);

        // Configuration>Peripheral options
        m_jbtnScale.setVisible(m_App.hasScale());
        m_jPanelScripts.setVisible(false);

        jTBtnShow.setSelected(false);

        if (Boolean.valueOf(getAppProperty("till.amountattop"))) {
            m_jPanEntries.remove(jPanelScanner);
            m_jPanEntries.remove(m_jNumberKeys);
            m_jPanEntries.add(jPanelScanner);
            m_jPanEntries.add(m_jNumberKeys);
        }

        priceWith00 = ("true".equals(getAppProperty("till.pricewith00")));
        keypadStateMachine.setPriceWith00(priceWith00);

        if (priceWith00) {
            m_jNumberKeys.dotIs00(true);
        }

        LOGGER.log(System.Logger.Level.DEBUG, "JPanelTicket.init: criar: Ticket.Line");
        m_ticketlines = new JTicketLines(dlSystem.getResourceAsXML(TicketConstants.RES_TICKET_LINES));
        m_jPanelLines.add(m_ticketlines, java.awt.BorderLayout.CENTER);
        peripheralCoordinator = new SalesPeripheralCoordinator(m_App, dlSystem);

        senttax = dlSales.getTaxList();
        senttaxcategories = dlSales.getTaxCategoriesList();
        taxcategoriesmodel = new ComboBoxValModel();

        stateToZero();

        m_oTicket = null;
        m_oTicketExt = null;
        jCheckStock.setText(AppLocal.getIntString("message.title.checkstock"));

        initExtButtons();

        initComponentFromChild();

        initDeviceDisplay();
    }

    private void initExtButtons() {
        // Script event buttons
        String resourceName = TicketConstants.RES_TICKET_BUTTONS;

        String sConfigRes = getResourceAsXML(resourceName);

        if (sConfigRes == null || sConfigRes.isBlank()) {
            LOGGER.log(System.Logger.Level.WARNING, "No found XML resource: " + resourceName);
            sConfigRes = "";
        }

        m_jbtnconfig = new JPanelButtons(m_App, new JPanelButtons.JPanelButtonListener() {
            @Override
            public void eval(String resource) {

                LOGGER.log(System.Logger.Level.INFO, "Rrocessing code (resource id): " + resource);
                evalScriptForExternalButtons(resource);
            }

            @Override
            public void print(String resource) {

                LOGGER.log(System.Logger.Level.INFO, "Rrocessing template (resource id): " + resource);
                printTicket(resource);
            }
        }, sConfigRes);

        m_jPanelBagExt.add(m_jbtnconfig);
        m_jPanelBagExt.setVisible(false);
    }

    private void initComponentFromChild() {
        m_ticketsbag = getJTicketsBag();

        // Set Configuration>General>Tickets toolbar simple : standard : restaurant
        // option
        m_jPanelBag.add(m_ticketsbag.getBagComponent(), BorderLayout.LINE_START);
        add(m_ticketsbag.getNullComponent(), "null");

        m_jPanelCatalog.add(getSouthComponent(), BorderLayout.CENTER);
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    protected String getTicketBagMode() {
        return getTicketsbag();
    }

    private String getTicketsbag() {
        return getAppProperty("machine.ticketsbag");
    }

    private boolean isRestaurantMode() {
        return com.openbravo.pos.ui.api.sales.SaleLayoutManager.isRestaurant(getTicketsbag());
    }

    private boolean isAutoLogoutRestaurant() {
        return "true".equals(getAppProperty("till.autoLogoffrestaurant"));
    }

    private boolean isAutoLogout() {
        return "true".equals(getAppProperty("till.autoLogoff"));
    }

    private void closeAllDialogs() {
        Window[] windows = getWindows();

        for (Window window : windows) {
            if (window instanceof JDialog) {
                window.dispose();
            }
        }
    }

    private void saveCurrentTicket() {

        if (m_oTicket != null) {
            String currentTicket = m_oTicket.getId();
            try {
                dlReceipts.updateSharedTicket(currentTicket, m_oTicket, m_oTicket.getPickupId());
            }
            catch (BasicException ex) {
                LOGGER.log(System.Logger.Level.ERROR, "Exception on save current ticket: " + currentTicket, ex);
            }
        }
    }

    protected JTicketLines getTicketlines() {
        return m_ticketlines;
    }

    protected JPanelButtons getTicketButtons() {
        return m_jbtnconfig;
    }

    protected String getTicketId() {
        return m_oTicketExt;
    }

    protected AppView getAppView() {
        return m_App;
    }

    protected DataLogicSales getDataLogicSales() {
        return dlSales;
    }

    /**
     *
     * @throws BasicException
     */
    @Override
    public void activate() throws BasicException {

        LOGGER.log(System.Logger.Level.INFO, "JPanelTicket.activate");

        Action logoutAction = new LogoutAction();
        if (isAutoLogout()) {
            try {
                int delay = Integer.parseInt(getAppProperty("till.autotimer"));
                delay *= 1000;
                // Should be more that 1s (1000 milisecond)
                if (delay > 1000) {
                    inactivityListener = new InactivityListener(logoutAction, delay);
                    inactivityListener.start();
                }
            }
            catch (NumberFormatException ex) {
                LOGGER.log(System.Logger.Level.WARNING, "Exception on set auto logout timer: ", ex);
            }
        }

        m_jaddtax.setSelected("true".equals(m_jbtnconfig.getProperty("taxesincluded")));

        List<TaxInfo> taxlist = senttax.list();
        List<TaxCategoryInfo> taxcategorieslist = senttaxcategories.list();

        // Initialize Services
        taxeslogic = new TaxesLogic(taxlist);
        salesService = new SalesServiceImpl(taxeslogic);
        ticketLineController = new TicketLineController(m_App, salesService, dlSales);
        salesCustomerController = new SalesCustomerController(m_App, dlCustomers);
        salesPaymentCoordinator = new SalesPaymentCoordinator(m_App, dlSales, salesService);
        paymentService = new PaymentServiceImpl();
        inventoryService = new InventoryServiceImpl(dlSales, m_App.getSession());
        salesStockCoordinator = new SalesStockCoordinator(inventoryService, dataLogicPIM, dlSales);
        salesBarcodeScanCoordinator = new SalesBarcodeScanCoordinator(dataLogicPIM, dlCustomers);

        paymentdialogreceipt = JPaymentSelectReceipt.getDialog(this);
        paymentdialogreceipt.init(m_App, paymentService);
        paymentdialogrefund = JPaymentSelectRefund.getDialog(this);
        paymentdialogrefund.init(m_App, paymentService);

        String taxesid = m_jbtnconfig.getProperty("taxcategoryid");
        taxcategoriesmodel = new ComboBoxValModel(taxcategorieslist);
        taxcategoriesmodel.setSelectedKey(taxesid);

        m_jTax.setModel(taxcategoriesmodel);
        if (taxesid == null) {
            if (m_jTax.getItemCount() > 0) {
                m_jTax.setSelectedIndex(0);
            }
        } else {
            taxcategoriesmodel.setSelectedKey(taxesid);
        }

        m_jaddtax.setSelected((Boolean.parseBoolean(getAppProperty("till.taxincluded"))));
        if (m_App.getAppUserView().getUser().hasPermission("sales.ChangeTaxOptions")) {
            m_jTax.setVisible(true);
            m_jaddtax.setVisible(true);
        } else {
            m_jTax.setVisible(false);
            m_jaddtax.setVisible(false);
        }

        m_jDelete.setEnabled(m_App.hasPermission("sales.EditLines"));
        m_jNumberKeys.setMinusEnabled(m_App.hasPermission("sales.EditLines"));
        m_jNumberKeys.setEqualsEnabled(m_App.hasPermission("sales.Total"));
        m_jbtnconfig.setPermissions(m_App.getAppUserView().getUser());

        m_ticketsbag.setEnabled(false);
        m_ticketsbag.activate();

        CustomerInfoGlobal customerInfoGlobal = CustomerInfoGlobal.getInstance();

        if (customerInfoGlobal.getCustomerInfoExt() != null && m_oTicket != null) {
            m_oTicket.setCustomer(customerInfoGlobal.getCustomerInfoExt());
        }

        refreshTicket();
    }

    @Override
    public boolean deactivate() {
        LOGGER.log(System.Logger.Level.DEBUG, "JPanelTicket.deactivate");

        if (inactivityListener != null) {
            inactivityListener.stop();
        }

        saveCurrentTicket();

        return m_ticketsbag.deactivate();
    }

    protected abstract JTicketsBag getJTicketsBag();

    protected abstract Component getSouthComponent();

    protected abstract void resetSouthComponent();

    /**
     *
     * @param ticketInfo
     * @param oTicketExt
     */
    @Override
    public void setActiveTicket(TicketInfo ticketInfo, String oTicketExt) {
        m_oTicket = ticketInfo;
        m_oTicketExt = oTicketExt;

        LOGGER.log(System.Logger.Level.DEBUG, "JPanelTicket setActiveTicket: " + oTicketExt);

        if (m_oTicket != null) {
            m_oTicket.setUser(m_App.getAppUserView().getUser().getUserInfo());
            m_oTicket.setActiveCash(m_App.getActiveCashIndex());
            m_oTicket.setDate(new Date());

            if (isRestaurantMode()) {
                if (isAutoLogoutRestaurant()) {
                    if (inactivityListener != null) {
                        inactivityListener.restart();
                    }
                }

                j_btnRemotePrt.setVisible(m_App.hasPermission("sales.PrintRemote"));
                j_btnRemotePrt.setEnabled(m_App.hasPermission("sales.PrintRemote"));

                if (!m_oTicket.getOldTicket()) {
                    restDB.setTicketIdInTable(m_oTicket.getId(), m_oTicketExt);
                }

                if (Boolean.parseBoolean(getAppProperty("table.showcustomerdetails"))) {
                    String custname = restDB.getCustomerNameInTable(m_oTicketExt);
                    if (m_oTicket.getCustomer() != null && (custname == null || custname.isBlank())) {
                        restDB.setCustomerNameInTable(m_oTicket.getCustomer().getName(), m_oTicketExt);
                    }
                }

                if (Boolean.parseBoolean(getAppProperty("table.showwaiterdetails"))) {
                    String waiter = restDB.getWaiterNameInTable(m_oTicketExt);
                    if (waiter == null || waiter.isBlank()) {
                        restDB.setWaiterNameInTable(m_App.getAppUserView().getUser().getName(), m_oTicketExt);
                    }
                }

                if (restDB.getTableMovedFlag(m_oTicket.getId())) {
                    restDB.moveCustomer(m_oTicketExt, m_oTicket.getId());
                }
            }

            executeEvent(m_oTicket, m_oTicketExt, TicketConstants.EV_TICKET_SHOW);
        }

        refreshTicket();
    }

    /**
     *
     * @return
     */
    @Override
    public TicketInfo getActiveTicket() {
        return m_oTicket;
    }

    private void refreshTicket() {

        CardLayout cl = (CardLayout) (getLayout());

        if (m_oTicket == null) {
            m_jTicketId.setText(null);
            m_ticketlines.clearTicketLines();
            m_jSubtotalEuros.setText(null);
            m_jTaxesEuros.setText(null);
            m_jTotalEuros.setText(null);

            checkStock();
            stateToZero();
            repaint();

            cl.show(this, "null");

            if ((m_oTicket != null) && (m_oTicket.getLinesCount() == 0)) {
                resetSouthComponent();
            }

        } else {
            if (m_oTicket.getTicketType() == TicketInfo.RECEIPT_REFUND) {
                m_jEditLine.setVisible(false);
                m_jList.setVisible(false);
            }

            m_oTicket.getLines().forEach((line) -> {
                line.setTaxInfo(taxeslogic.getTaxInfo(line
                        .getProductTaxCategoryID(), m_oTicket.getCustomer()));
            });

            m_jTicketId.setText(m_oTicket.getName(m_oTicketExt));
            m_ticketlines.clearTicketLines();

            for (int i = 0; i < m_oTicket.getLinesCount(); i++) {
                m_ticketlines.addTicketLine(m_oTicket.getLine(i));
            }

            if (m_oTicket.getLinesCount() == 0) {
                resetSouthComponent();
            }

            countArticles();
            printPartialTotals();
            stateToZero();
            repaint();

            cl.show(this, "ticket");
            if (m_oTicket.getLinesCount() == 0) {
                resetSouthComponent();
            }

            m_jKeyFactory.setText(null);
            java.awt.EventQueue.invokeLater(() -> {
                m_jKeyFactory.requestFocus();
            });
        }
    }

    private void countArticles() {

        if (m_oTicket != null) {
            if (m_App.hasPermission("sales.Total") && m_oTicket.getArticlesCount() > 1) {
                btnSplit.setEnabled(true);
            } else {
                btnSplit.setEnabled(false);
            }
        }
    }

    private void applyLineQuantityChange(double amount, boolean isAbsolute) {
        int i = m_ticketlines.getSelectedIndex();
        if (i < 0) {
            com.openbravo.pos.util.NotifyUtils.beep();
            return;
        }
        if (ticketLineController != null) {
            LineChangeResult res = ticketLineController.changeLineQuantity(this, m_oTicket, i, amount, isAbsolute);
            if (res.status() == LineChangeResult.Status.UPDATED) {
                paintTicketLine(i, res.updatedLine());
            } else if (res.status() == LineChangeResult.Status.REMOVED) {
                refreshTicket();
            } else if (res.status() == LineChangeResult.Status.DENIED) {
                com.openbravo.pos.util.NotifyUtils.beep();
            }
        }
    }

    private void printPartialTotals() {

        if (m_oTicket == null || m_oTicket.getLinesCount() == 0) {
            m_jSubtotalEuros.setText(null);
            m_jTaxesEuros.setText(null);
            m_jTotalEuros.setText(null);
        } else {
            m_jSubtotalEuros.setText(m_oTicket.printSubTotal());
            m_jTaxesEuros.setText(m_oTicket.printTax());
            m_jTotalEuros.setText(m_oTicket.printTotal());
        }
        repaint();
    }

    private void paintTicketLine(int index, TicketLineInfo oLine) {
        if (m_oTicket != null) {
            m_oTicket.setLine(index, oLine);
            m_ticketlines.setTicketLine(index, oLine);
            m_ticketlines.setSelectedIndex(index);
            // oCount = count; // pass line old multiplier value

            countArticles();
            visorTicketLine(oLine);
            printPartialTotals();
            stateToZero();
        }
    }

    private void addTicketLine(ProductInfoExt oProduct, double dMul, double dPrice) {
        boolean priceIncludesTax = false;
        if (oProduct.isVprice()) {
            priceIncludesTax = m_jaddtax.isSelected();
        } else {
            j_btnRemotePrt.setEnabled(true);
        }

        TicketLineInfo line = salesService.createLine(m_oTicket, oProduct, dMul, dPrice, priceIncludesTax);
        addTicketLine(line);
        refreshTicket();
    }

    /**
     * Add a Ticket Line
     *
     * @param oLine Ticket line
     */
    protected void addTicketLine(TicketLineInfo oLine) {
        if (m_oTicket != null) {
            if (oLine.isProductCom()) {
                int i = ticketLineController != null
                        ? ticketLineController.calculateAuxiliaryInsertIndex(m_oTicket, m_ticketlines.getSelectedIndex())
                        : -1;

                if (i >= 0) {
                    m_oTicket.insertLine(i, oLine);
                    m_ticketlines.insertTicketLine(i, oLine);
                } else {
                    com.openbravo.pos.util.NotifyUtils.beep();
                }
            } else {
                m_oTicket.addLine(oLine);
                m_ticketlines.addTicketLine(oLine);

                int i = m_ticketlines.getSelectedIndex();
                if (i >= 0) {
                    TicketLineInfo line = m_oTicket.getLine(i);
                    if (line.isProductVerpatrib() && ticketLineController != null) {
                        if (ticketLineController.editLineAttributes(this, m_App.getSession(), line)) {
                            paintTicketLine(i, line);
                        }
                    }
                }
            }

            visorTicketLine(oLine);
            printPartialTotals();
            stateToZero();
            countArticles();

            executeEvent(m_oTicket, m_oTicketExt, TicketConstants.EV_TICKET_CHANGE);
        } else {
            com.openbravo.pos.util.NotifyUtils.beep();
        }
    }

    private TicketLineInfo getSelectedTicketLineInfo() {
        int i = m_ticketlines.getSelectedIndex();
        return getTicketLineInfo(i);
    }

    private TicketLineInfo getTicketLineInfo(int index) {
        return m_oTicket.getLine(index);
    }

    private void removeTicketLine(int ticketLineNumber) {
        if (ticketLineController != null && ticketLineController.deleteLineWithAudit(this, m_oTicket, ticketLineNumber)) {
            refreshTicket();
        }
    }

    private ProductInfoExt getInputProduct() {
        ProductInfoExt oProduct = new ProductInfoExt();
        // Always add Default Prod ID + Add Name to Misc.
        // THOSE ATTRIBUTE ARE IMPORTANT FOR Table foreign key rela
        oProduct.setReference("0000");
        oProduct.setCode("0000");
        oProduct.setName("***");
        oProduct.setTaxCategoryID(((TaxCategoryInfo) taxcategoriesmodel.getSelectedItem()).getID());
        oProduct.setPriceSell(includeTaxes(oProduct.getTaxCategoryID(), getInputValue()));

        return oProduct;
    }

    private boolean isPriceInclusiveTaxEnabled() {
        return m_jaddtax.isSelected();
    }

    private double includeTaxes(String tcid, double dValue) {
        if (m_jaddtax.isSelected()) {
            TaxInfo tax = taxeslogic.getTaxInfo(tcid, m_oTicket.getCustomer());
            return AmountCalculatorUtil.calcPriceWithoutTax(dValue, tax);
        } else {
            return dValue;
        }
    }

    /**
     * Scanner Input Value Get Price from a input field MUST be Public is used
     * by Script (
     *
     * @return
     */
    public double getInputValue() {
        try {

            return Double.parseDouble(m_jPrice.getText());
        }
        catch (NumberFormatException ex) {
            LOGGER.log(System.Logger.Level.WARNING, "Exception on get input value from user: ", ex);
            return 0.0;
        }
    }

    /**
     * Scanner Por Value
     *
     * @return
     */
    public double getPorValue() {
        try {
            return Double.parseDouble(m_jPor.getText().substring(1));
        }
        catch (NumberFormatException | StringIndexOutOfBoundsException ex) {
            LOGGER.log(System.Logger.Level.WARNING, "Exception on get Por value: ", ex);
            return 1.0;
        }
    }

    /**
     * Get selected ticket line
     *
     * @return line index
     */
    public int getSelectedIndex() {
        return m_ticketlines.getSelectedIndex();
    }

    private void stateToZero() {
        keypadStateMachine.reset();
        m_jPor.setText("");
        m_jPrice.setText("");
        m_sBarcode = new StringBuffer();

        m_iNumberStatus = NUMBER_INPUTZERO;
        m_iNumberStatusInput = NUMBERZERO;
        m_iNumberStatusPor = NUMBERZERO;
        repaint();
    }

    private void incProductByCode(String sCode) {
        if (salesBarcodeScanCoordinator != null) {
            salesBarcodeScanCoordinator.processBarcode(
                    this,
                    sCode,
                    taxeslogic,
                    m_oTicket != null ? m_oTicket.getCustomer() : null,
                    m_jaddtax.isSelected(),
                    customer -> {
                        m_oTicket.setCustomer(customer);
                        m_jTicketId.setText(m_oTicket.getName(m_oTicketExt));
                    },
                    (prod, units, price) -> addTicketLine(prod, units, price),
                    this::incProduct,
                    this::stateToZero
            );
        }
    }

    private void incProduct(ProductInfoExt prod) {
        incProduct(prod, 1.0);
    }

    private void incProduct(ProductInfoExt prod, double dPor) {

        if (prod.isScale() && m_App.hasScale()) {
            Double value = peripheralCoordinator.readWeight(this);
            if (value != null) {
                incProduct(prod, value);
            } else {
                stateToZero();
            }
        } else {

            if (prod.isVprice()) {
                addTicketLine(prod, getPorValue(), getInputValue());
            } else {
                addTicketLine(prod, dPor, prod.getPriceSell());
            }
        }

    }

    /**
     *
     * @param prod
     */
    protected void buttonTransition(ProductInfoExt prod) {

        if (keypadStateMachine.isInputZero() && keypadStateMachine.isPorZero()) {
            incProduct(prod);
        } else if (keypadStateMachine.isInputValid() && keypadStateMachine.isPorZero()) {
            incProduct(prod, getInputValue());
        } else if (prod.isVprice()) {
            addTicketLine(prod, getPorValue(), getInputValue());
        } else {
            com.openbravo.pos.util.NotifyUtils.beep();
        }
    }

    @SuppressWarnings("empty-statement")
    private void stateTransition(char cTrans) {

        if ((cTrans == '\n') || (cTrans == '?')) {

            if (m_sBarcode.length() > 0) {
                String sCode = m_sBarcode.toString();
                if (salesBarcodeScanCoordinator != null) {
                    salesBarcodeScanCoordinator.processBarcode(
                            this,
                            sCode,
                            taxeslogic,
                            m_oTicket != null ? m_oTicket.getCustomer() : null,
                            m_jaddtax.isSelected(),
                            customer -> {
                                m_oTicket.setCustomer(customer);
                                m_jTicketId.setText(m_oTicket.getName(m_oTicketExt));
                            },
                            (prod, units, price) -> addTicketLine(prod, units, price),
                            this::incProduct,
                            this::stateToZero
                    );
                }
            } else {
                com.openbravo.pos.util.NotifyUtils.beep();
            }

        } else {

            m_sBarcode.append(cTrans);

            if (cTrans == '\u007f') {
                stateToZero();
            } else if (keypadStateMachine.processKeypadChar(cTrans)) {
                m_jPrice.setText(keypadStateMachine.getPriceText());
                m_jPor.setText(keypadStateMachine.getPorText());
                m_iNumberStatus = keypadStateMachine.getNumberStatus();
                m_iNumberStatusInput = keypadStateMachine.getNumberStatusInput();
                m_iNumberStatusPor = keypadStateMachine.getNumberStatusPor();
            } else if (cTrans == '\u00a7'
                    && m_iNumberStatusInput == NUMBERVALID
                    && m_iNumberStatusPor == NUMBERZERO) {

                if (m_App.hasPermission("sales.EditLines")) {
                    Double value = peripheralCoordinator.readWeight(this);
                    if (value != null) {
                        ProductInfoExt product = getInputProduct();
                        addTicketLine(product, value, product.getPriceSell());
                    } else {
                        stateToZero();
                    }
                } else {
                    com.openbravo.pos.util.NotifyUtils.beep();
                }
            } else if (cTrans == '\u00a7'
                    && m_iNumberStatusInput == NUMBERZERO
                    && m_iNumberStatusPor == NUMBERZERO) {

                int i = m_ticketlines.getSelectedIndex();
                if (i < 0) {
                    com.openbravo.pos.util.NotifyUtils.beep();
                } else {
                    Double value = peripheralCoordinator.readWeight(this);
                    if (value != null) {
                        TicketLineInfo newline = new TicketLineInfo(m_oTicket.getLine(i));
                        newline.setMultiply(value);
                        newline.setPrice(Math.abs(newline.getPrice()));
                        paintTicketLine(i, newline);
                    } else {
                        stateToZero();
                    }
                }

            } else if (cTrans == '+'
                    && m_iNumberStatusInput == NUMBERZERO
                    && m_iNumberStatusPor == NUMBERZERO) {
                applyLineQuantityChange(1.0, false);
            } else if (cTrans == '-'
                    && m_iNumberStatusInput == NUMBERZERO
                    && m_iNumberStatusPor == NUMBERZERO
                    && m_App.hasPermission("sales.EditLines")) {
                applyLineQuantityChange(-1.0, false);
            } else if (cTrans == '+'
                    && m_iNumberStatusInput == NUMBERZERO
                    && m_iNumberStatusPor == NUMBERVALID) {
                applyLineQuantityChange(getPorValue(), true);
            } else if (cTrans == '-'
                    && m_iNumberStatusInput == NUMBERZERO
                    && m_iNumberStatusPor == NUMBERVALID
                    && m_App.hasPermission("sales.EditLines")) {
                applyLineQuantityChange(getPorValue(), true);
            } else if (cTrans == '+'
                    && m_iNumberStatusInput == NUMBERVALID
                    && m_iNumberStatusPor == NUMBERZERO
                    && m_App.hasPermission("sales.EditLines")) {
                ProductInfoExt product = getInputProduct();
                addTicketLine(product, 1.0, product.getPriceSell());
                m_jEditLine.doClick();

            } else if (cTrans == '-'
                    && m_iNumberStatusInput == NUMBERVALID
                    && m_iNumberStatusPor == NUMBERZERO
                    && m_App.hasPermission("sales.EditLines")) {
                ProductInfoExt product = getInputProduct();
                addTicketLine(product, 1.0, -product.getPriceSell());
                m_jEditLine.doClick();

            } else if (cTrans == '+'
                    && m_iNumberStatusInput == NUMBERVALID
                    && m_iNumberStatusPor == NUMBERVALID
                    && m_App.hasPermission("sales.EditLines")) {
                ProductInfoExt product = getInputProduct();
                addTicketLine(product, getPorValue(), product.getPriceSell());

            } else if (cTrans == '-'
                    && m_iNumberStatusInput == NUMBERVALID
                    && m_iNumberStatusPor == NUMBERVALID
                    && m_App.hasPermission("sales.EditLines")) {
                ProductInfoExt product = getInputProduct();
                addTicketLine(product, getPorValue(), -product.getPriceSell());

            } else if (cTrans == ' ' || cTrans == '=') {
                if (m_oTicket != null && m_oTicket.getLinesCount() > 0) {
                    if (closeTicket(m_oTicket, m_oTicketExt)) {
                        setActiveTicket(null, null);
                        refreshTicket();
                        // Delete will create a empty ticket
                        if (m_ticketsbag != null) {
                            m_ticketsbag.deleteTicket();
                        }

                        if (isAutoLogout()) {
                            if (isRestaurantMode() && isAutoLogoutRestaurant()) {
                                deactivate();
                            } else {
                                ((ApplicationShell) m_App).closeAppView();
                            }
                        }

                        createNewTicket();
                    }
                    refreshTicket();
                } else {
                    com.openbravo.pos.util.NotifyUtils.beep();
                    LOGGER.log(System.Logger.Level.DEBUG, "Canno close Ticket, because m_oTicket is " + m_oTicket
                            + ", and LinesCount is " + (m_oTicket != null ? m_oTicket.getLinesCount() : 0));
                }
            }
        }
    }

    private void createNewTicket() {
        // Create New Ticket
        TicketInfo ticket = new TicketInfo();
        setActiveTicket(ticket, null);
    }

    private boolean closeTicket(TicketInfo ticket, String ticketext) {
        if (inactivityListener != null) {
            inactivityListener.stop();
        }

        if (!m_App.hasPermission("sales.Total") || salesPaymentCoordinator == null) {
            return false;
        }

        JPaymentSelect paymentdialog = salesPaymentCoordinator.resolvePaymentDialog(
                ticket, paymentdialogreceipt, paymentdialogrefund);

        boolean printSelected = "true".equals(m_jbtnconfig.getProperty("printselected", "true"));

        return salesPaymentCoordinator.processPaymentAndClose(
                this,
                ticket,
                ticketext,
                paymentdialog,
                printSelected,
                (eventName, args) -> executeEvent(ticket, ticketext, eventName, args),
                (scriptName, isPrintSelected) -> {
                    printTicket(scriptName, ticket, ticketext);
                    Notify(AppLocal.getIntString("notify.printing"));
                },
                () -> {
                    if (isRestaurantMode() && !ticket.getOldTicket()) {
                        restDB.clearCustomerNameInTable(ticketext);
                        restDB.clearWaiterNameInTable(ticketext);
                        restDB.clearTicketIdInTable(ticketext);
                    }
                }
        );
    }

    private boolean warrantyCheck(TicketInfo ticket) {
        return salesPaymentCoordinator != null
                ? salesPaymentCoordinator.hasWarrantyProduct(ticket)
                : false;
    }

    /**
     *
     * @param pTicket
     * @return
     */
    public String getPickupString(TicketInfo pTicket) {
        if (pTicket == null) {
            return ("0");
        }
        String tmpPickupId = Integer.toString(pTicket.getPickupId());
        String pickupSize = (getAppProperty("till.pickupsize"));
        if (pickupSize != null && (Integer.parseInt(pickupSize) >= tmpPickupId.length())) {
            while (tmpPickupId.length() < (Integer.parseInt(pickupSize))) {
                tmpPickupId = "0" + tmpPickupId;
            }
        }
        return (tmpPickupId);
    }

    private void printTicket(String sresourcename, TicketInfo ticket, String ticketext) {
        refreshTicket();
        boolean ok = peripheralCoordinator.printTicket(sresourcename, ticket, ticketext, taxeslogic,
                warrantyCheck(ticket), getPickupString(ticket), this);
        if (ok) {
            Notify(AppLocal.getIntString("notify.printed"));
        }
    }

    public void printTicket(String resource) {
        printTicket(resource, m_oTicket, m_oTicketExt);
        j_btnRemotePrt.setEnabled(false);
    }

    public void customerAdd(String resource) {
        Notify(AppLocal.getIntString("notify.customeradd"));
    }

    public void customerRemove(String resource) {
        Notify(AppLocal.getIntString("notify.customerremove"));
    }

    public void customerChange(String resource) {
        Notify(AppLocal.getIntString("notify.customerchange"));
    }

    public void Notify(String msg) {

    }

    private void printReport(String resourcefile, TicketInfo ticket, String ticketext) {
        if (peripheralCoordinator != null) {
            String printerName = getAppProperty("machine.printername");
            peripheralCoordinator.printReport(printerName, resourcefile, ticket, ticketext, taxeslogic, this);
        }
    }

    private void initDeviceDisplay() {
        peripheralCoordinator.setupAdvancedDisplayListener(this.m_ticketlines, () -> this.m_oTicket, sProductId -> {
            try {
                ProductInfoExt prod = dataLogicPIM.getProductInfo(sProductId);
                return prod != null ? prod : dataLogicPIM.getProductInfoByCode(sProductId);
            } catch (BasicException ex) {
                return null;
            }
        });
    }

    private void visorTicketLine(TicketLineInfo oLine) {
        peripheralCoordinator.updateCustomerDisplay(oLine, this);
    }

    private Object evalScript(ScriptObject scr, String resource, ScriptArg... args) {

        // resource here is guaranteed to be not null
        try {
            scr.setSelectedIndex(m_ticketlines.getSelectedIndex());
            return scr.evalScript(dlSystem.getResourceAsXML(resource), args);
        }
        catch (ScriptException ex) {
            LOGGER.log(System.Logger.Level.WARNING, "Exception on executing script with resource id: " + resource, ex);
            MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotexecute"), ex);
            msg.show(this);
            return msg;
        }
    }

    private void evalScriptForExternalButtons(String resource) {
        ScriptArg sa1 = new ScriptArg("ticket", m_oTicket);
        ScriptArg sa2 = new ScriptArg("user", m_App.getAppUserView().getUser());
        ScriptArg sa3 = new ScriptArg("sales", this);

        evalScriptAndRefresh(resource, sa1, sa2, sa3);
    }

    private void evalScriptAndRefresh(String resource, ScriptArg... args) {

        if (resource == null) {
            MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotexecute"));
            msg.show(this);
        } else {
            ScriptObject scr = new ScriptObject(m_oTicket, m_oTicketExt);
            scr.setSelectedIndex(m_ticketlines.getSelectedIndex());
            evalScript(scr, resource, args);
            refreshTicket();

            setSelectedIndex(scr.getSelectedIndex());
        }
    }

    private Object executeEvent(TicketInfo ticket, String ticketExt, String eventKey, ScriptArg... args) {

        String resource = m_jbtnconfig.getEvent(eventKey);
        if (resource == null) {
            return null;
        } else {
            ScriptObject scr = new ScriptObject(ticket, ticketExt);
            return evalScript(scr, resource, args);
        }
    }

    public String getResourceAsXML(String sresourcename) {
        return dlSystem.getResourceAsXML(sresourcename);
    }

    public BufferedImage getResourceAsImage(String sresourcename) {
        return dlSystem.getResourceAsImage(sresourcename);
    }

    private void setSelectedIndex(int i) {

        if (i >= 0 && i < m_oTicket.getLinesCount()) {
            m_ticketlines.setSelectedIndex(i);
        } else if (m_oTicket.getLinesCount() > 0) {
            m_ticketlines.setSelectedIndex(m_oTicket.getLinesCount() - 1);
        }
    }


    public void checkStock() {
        checkAndShowStockForLine(false);
    }

    public void checkCustomer() {
        if (salesCustomerController != null) {
            salesCustomerController.checkAndShowCustomerDiscount(this, m_oTicket);
        } else if (m_oTicket != null && m_oTicket.getCustomer() != null && m_oTicket.getCustomer().isVIP()) {
            CustomerDiscountInfoPanel.show(this, m_oTicket.getCustomer());
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the FormEditor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        m_jPanelContainer = new javax.swing.JPanel();
        m_jPanelMainToolbar = new javax.swing.JPanel();
        m_jPanelBag = new javax.swing.JPanel();
        jTBtnShow = new javax.swing.JToggleButton();
        m_jbtnScale = new javax.swing.JButton();
        m_jButtons = new javax.swing.JPanel();
        btnSplit = new javax.swing.JButton();
        btnReprint1 = new javax.swing.JButton();
        j_btnRemotePrt = new javax.swing.JButton();
        jBtnCustomer = new javax.swing.JButton();
        m_jPanelScripts = new javax.swing.JPanel();
        m_jPanelBagExt = new javax.swing.JPanel();
        m_jPanelBagExtDefaultEmpty = new javax.swing.JPanel();
        m_jPanelTicket = new javax.swing.JPanel();
        m_jPanelLinesToolbar = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        m_jDelete = new javax.swing.JButton();
        m_jList = new javax.swing.JButton();
        m_jEditLine = new javax.swing.JButton();
        jEditAttributes = new javax.swing.JButton();
        jCheckStock = new javax.swing.JButton();
        m_jPanelLines = new javax.swing.JPanel();
        m_jPanelLinesSum = new javax.swing.JPanel();
        filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(5, 0), new java.awt.Dimension(5, 0), new java.awt.Dimension(5, 32767));
        m_jTicketId = new javax.swing.JLabel();
        m_jPanelTotals = new javax.swing.JPanel();
        m_jLblSubTotalEuros = new javax.swing.JLabel();
        m_jLblTaxEuros = new javax.swing.JLabel();
        m_jLblTotalEuros = new javax.swing.JLabel();
        m_jSubtotalEuros = new javax.swing.JLabel();
        m_jTaxesEuros = new javax.swing.JLabel();
        m_jTotalEuros = new javax.swing.JLabel();
        m_jContEntries = new javax.swing.JPanel();
        m_jPanEntries = new javax.swing.JPanel();
        m_jNumberKeys = new com.openbravo.beans.JNumberKeys();
        jPanelScanner = new javax.swing.JPanel();
        m_jPrice = new javax.swing.JLabel();
        m_jEnter = new javax.swing.JButton();
        m_jPor = new javax.swing.JLabel();
        m_jKeyFactory = new javax.swing.JTextField();
        m_jaddtax = new javax.swing.JCheckBox();
        m_jTax = new javax.swing.JComboBox();
        m_jPanelCatalog = new javax.swing.JPanel();

        setBackground(new java.awt.Color(255, 204, 153));
        setOpaque(false);
        setLayout(new java.awt.CardLayout());

        m_jPanelContainer.setLayout(new java.awt.BorderLayout());

        m_jPanelMainToolbar.setLayout(new java.awt.BorderLayout());

        m_jPanelBag.setAutoscrolls(true);
        m_jPanelBag.setMaximumSize(new java.awt.Dimension(10, 10));
        m_jPanelBag.setPreferredSize(new java.awt.Dimension(0, 60));

        jTBtnShow.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jTBtnShow.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/resources.png"))); // NOI18N
        jTBtnShow.setPreferredSize(new java.awt.Dimension(80, 45));
        jTBtnShow.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTBtnShowActionPerformed(evt);
            }
        });
        m_jPanelBag.add(jTBtnShow);

        m_jbtnScale.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        m_jbtnScale.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/scale.png"))); // NOI18N
        m_jbtnScale.setText(AppLocal.getIntString("button.scale")); // NOI18N
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("pos_messages"); // NOI18N
        m_jbtnScale.setToolTipText(bundle.getString("tooltip.scale")); // NOI18N
        m_jbtnScale.setFocusPainted(false);
        m_jbtnScale.setFocusable(false);
        m_jbtnScale.setMargin(new java.awt.Insets(8, 14, 8, 14));
        m_jbtnScale.setMaximumSize(new java.awt.Dimension(85, 44));
        m_jbtnScale.setMinimumSize(new java.awt.Dimension(85, 44));
        m_jbtnScale.setPreferredSize(new java.awt.Dimension(85, 45));
        m_jbtnScale.setRequestFocusEnabled(false);
        m_jbtnScale.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jbtnScaleActionPerformed(evt);
            }
        });
        m_jPanelBag.add(m_jbtnScale);

        m_jButtons.setPreferredSize(new java.awt.Dimension(350, 55));

        btnSplit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/sale_split_sml.png"))); // NOI18N
        btnSplit.setToolTipText(bundle.getString("tooltip.salesplit")); // NOI18N
        btnSplit.setEnabled(false);
        btnSplit.setFocusPainted(false);
        btnSplit.setFocusable(false);
        btnSplit.setMargin(new java.awt.Insets(8, 14, 8, 14));
        btnSplit.setMaximumSize(new java.awt.Dimension(50, 40));
        btnSplit.setMinimumSize(new java.awt.Dimension(50, 40));
        btnSplit.setPreferredSize(new java.awt.Dimension(80, 45));
        btnSplit.setRequestFocusEnabled(false);
        btnSplit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSplitActionPerformed(evt);
            }
        });

        btnReprint1.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        btnReprint1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/reprint24.png"))); // NOI18N
        btnReprint1.setToolTipText(bundle.getString("tooltip.reprintLastTicket")); // NOI18N
        btnReprint1.setFocusPainted(false);
        btnReprint1.setFocusable(false);
        btnReprint1.setMargin(new java.awt.Insets(8, 14, 8, 14));
        btnReprint1.setMaximumSize(new java.awt.Dimension(50, 40));
        btnReprint1.setMinimumSize(new java.awt.Dimension(50, 40));
        btnReprint1.setPreferredSize(new java.awt.Dimension(80, 45));
        btnReprint1.setRequestFocusEnabled(false);
        btnReprint1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnReprint1ActionPerformed(evt);
            }
        });

        j_btnRemotePrt.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        j_btnRemotePrt.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/remote_print.png"))); // NOI18N
        j_btnRemotePrt.setText(bundle.getString("button.sendorder")); // NOI18N
        j_btnRemotePrt.setToolTipText(bundle.getString("tooltip.printtoremote")); // NOI18N
        j_btnRemotePrt.setMargin(new java.awt.Insets(0, 4, 0, 4));
        j_btnRemotePrt.setMaximumSize(new java.awt.Dimension(50, 40));
        j_btnRemotePrt.setMinimumSize(new java.awt.Dimension(50, 40));
        j_btnRemotePrt.setPreferredSize(new java.awt.Dimension(80, 45));
        j_btnRemotePrt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                j_btnRemotePrtActionPerformed(evt);
            }
        });

        jBtnCustomer.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jBtnCustomer.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/customer.png"))); // NOI18N
        jBtnCustomer.setToolTipText(bundle.getString("tooltip.salescustomer")); // NOI18N
        jBtnCustomer.setPreferredSize(new java.awt.Dimension(80, 45));
        jBtnCustomer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnCustomerActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout m_jButtonsLayout = new javax.swing.GroupLayout(m_jButtons);
        m_jButtons.setLayout(m_jButtonsLayout);
        m_jButtonsLayout.setHorizontalGroup(
            m_jButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(m_jButtonsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jBtnCustomer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnSplit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(j_btnRemotePrt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnReprint1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        m_jButtonsLayout.setVerticalGroup(
            m_jButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(m_jButtonsLayout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(m_jButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(j_btnRemotePrt, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnSplit, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnReprint1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jBtnCustomer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        m_jPanelBag.add(m_jButtons);

        m_jPanelMainToolbar.add(m_jPanelBag, java.awt.BorderLayout.PAGE_START);

        m_jPanelScripts.setPreferredSize(new java.awt.Dimension(200, 60));
        m_jPanelScripts.setLayout(new java.awt.BorderLayout());

        m_jPanelBagExt.setPreferredSize(new java.awt.Dimension(20, 60));

        m_jPanelBagExtDefaultEmpty.setMinimumSize(new java.awt.Dimension(235, 50));
        m_jPanelBagExtDefaultEmpty.setPreferredSize(new java.awt.Dimension(10, 55));
        m_jPanelBagExt.add(m_jPanelBagExtDefaultEmpty);

        m_jPanelScripts.add(m_jPanelBagExt, java.awt.BorderLayout.PAGE_START);

        m_jPanelMainToolbar.add(m_jPanelScripts, java.awt.BorderLayout.CENTER);
        m_jPanelScripts.getAccessibleContext().setAccessibleDescription("");

        m_jPanelContainer.add(m_jPanelMainToolbar, java.awt.BorderLayout.NORTH);

        m_jPanelTicket.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        m_jPanelTicket.setLayout(new java.awt.BorderLayout());

        m_jPanelLinesToolbar.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        m_jPanelLinesToolbar.setPreferredSize(new java.awt.Dimension(75, 270));
        m_jPanelLinesToolbar.setLayout(new java.awt.BorderLayout());

        jPanel2.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 5, 0, 5));
        jPanel2.setPreferredSize(new java.awt.Dimension(70, 250));
        jPanel2.setLayout(new java.awt.GridLayout(0, 1, 5, 5));

        m_jDelete.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/editdelete.png"))); // NOI18N
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

        jEditAttributes.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/attributes.png"))); // NOI18N
        jEditAttributes.setToolTipText(bundle.getString("tooltip.saleattributes")); // NOI18N
        jEditAttributes.setFocusPainted(false);
        jEditAttributes.setFocusable(false);
        jEditAttributes.setMargin(new java.awt.Insets(8, 14, 8, 14));
        jEditAttributes.setMaximumSize(new java.awt.Dimension(42, 36));
        jEditAttributes.setMinimumSize(new java.awt.Dimension(42, 36));
        jEditAttributes.setPreferredSize(new java.awt.Dimension(50, 45));
        jEditAttributes.setRequestFocusEnabled(false);
        jEditAttributes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jEditAttributesActionPerformed(evt);
            }
        });
        jPanel2.add(jEditAttributes);

        jCheckStock.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        jCheckStock.setForeground(new java.awt.Color(76, 197, 237));
        jCheckStock.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/info.png"))); // NOI18N
        jCheckStock.setToolTipText(bundle.getString("tooltip.salecheckstock")); // NOI18N
        jCheckStock.setFocusPainted(false);
        jCheckStock.setFocusable(false);
        jCheckStock.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jCheckStock.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        jCheckStock.setMargin(new java.awt.Insets(8, 4, 8, 4));
        jCheckStock.setMaximumSize(new java.awt.Dimension(42, 36));
        jCheckStock.setMinimumSize(new java.awt.Dimension(42, 36));
        jCheckStock.setPreferredSize(new java.awt.Dimension(80, 45));
        jCheckStock.setRequestFocusEnabled(false);
        jCheckStock.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        jCheckStock.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckStockActionPerformed(evt);
            }
        });
        jPanel2.add(jCheckStock);

        m_jPanelLinesToolbar.add(jPanel2, java.awt.BorderLayout.NORTH);

        m_jPanelTicket.add(m_jPanelLinesToolbar, java.awt.BorderLayout.LINE_START);

        m_jPanelLines.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jPanelLines.setPreferredSize(new java.awt.Dimension(450, 240));
        m_jPanelLines.setLayout(new java.awt.BorderLayout());

        m_jPanelLinesSum.setLayout(new java.awt.BorderLayout());
        m_jPanelLinesSum.add(filler2, java.awt.BorderLayout.LINE_START);

        m_jTicketId.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        m_jTicketId.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        m_jTicketId.setText("ID");
        m_jTicketId.setToolTipText("");
        m_jTicketId.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        m_jTicketId.setOpaque(true);
        m_jTicketId.setPreferredSize(new java.awt.Dimension(300, 40));
        m_jTicketId.setRequestFocusEnabled(false);
        m_jTicketId.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        m_jPanelLinesSum.add(m_jTicketId, java.awt.BorderLayout.CENTER);

        m_jPanelTotals.setPreferredSize(new java.awt.Dimension(450, 60));
        m_jPanelTotals.setLayout(new java.awt.GridLayout(2, 3, 4, 0));

        m_jLblSubTotalEuros.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        m_jLblSubTotalEuros.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        m_jLblSubTotalEuros.setLabelFor(m_jSubtotalEuros);
        m_jLblSubTotalEuros.setText(AppLocal.getIntString("label.subtotalcash")); // NOI18N
        m_jPanelTotals.add(m_jLblSubTotalEuros);

        m_jLblTaxEuros.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        m_jLblTaxEuros.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        m_jLblTaxEuros.setLabelFor(m_jSubtotalEuros);
        m_jLblTaxEuros.setText(AppLocal.getIntString("label.taxcash")); // NOI18N
        m_jPanelTotals.add(m_jLblTaxEuros);

        m_jLblTotalEuros.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        m_jLblTotalEuros.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        m_jLblTotalEuros.setLabelFor(m_jTotalEuros);
        m_jLblTotalEuros.setText(AppLocal.getIntString("label.totalcash")); // NOI18N
        m_jPanelTotals.add(m_jLblTotalEuros);

        m_jSubtotalEuros.setBackground(m_jEditLine.getBackground());
        m_jSubtotalEuros.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        m_jSubtotalEuros.setForeground(m_jEditLine.getForeground());
        m_jSubtotalEuros.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        m_jSubtotalEuros.setLabelFor(m_jSubtotalEuros);
        m_jSubtotalEuros.setToolTipText(bundle.getString("tooltip.salesubtotal")); // NOI18N
        m_jSubtotalEuros.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(153, 153, 153), 1, true));
        m_jSubtotalEuros.setMaximumSize(new java.awt.Dimension(125, 25));
        m_jSubtotalEuros.setMinimumSize(new java.awt.Dimension(80, 25));
        m_jSubtotalEuros.setPreferredSize(new java.awt.Dimension(125, 25));
        m_jSubtotalEuros.setRequestFocusEnabled(false);
        m_jPanelTotals.add(m_jSubtotalEuros);

        m_jTaxesEuros.setBackground(m_jEditLine.getBackground());
        m_jTaxesEuros.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        m_jTaxesEuros.setForeground(m_jEditLine.getForeground());
        m_jTaxesEuros.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        m_jTaxesEuros.setLabelFor(m_jTaxesEuros);
        m_jTaxesEuros.setToolTipText(bundle.getString("tooltip.saletax")); // NOI18N
        m_jTaxesEuros.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(153, 153, 153), 1, true));
        m_jTaxesEuros.setMaximumSize(new java.awt.Dimension(125, 25));
        m_jTaxesEuros.setMinimumSize(new java.awt.Dimension(80, 25));
        m_jTaxesEuros.setPreferredSize(new java.awt.Dimension(125, 25));
        m_jTaxesEuros.setRequestFocusEnabled(false);
        m_jPanelTotals.add(m_jTaxesEuros);

        m_jTotalEuros.setBackground(m_jEditLine.getBackground());
        m_jTotalEuros.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        m_jTotalEuros.setForeground(m_jEditLine.getForeground());
        m_jTotalEuros.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        m_jTotalEuros.setLabelFor(m_jTotalEuros);
        m_jTotalEuros.setToolTipText(bundle.getString("tooltip.saletotal")); // NOI18N
        m_jTotalEuros.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(153, 153, 153), 1, true));
        m_jTotalEuros.setMaximumSize(new java.awt.Dimension(125, 25));
        m_jTotalEuros.setMinimumSize(new java.awt.Dimension(80, 25));
        m_jTotalEuros.setPreferredSize(new java.awt.Dimension(125, 25));
        m_jTotalEuros.setRequestFocusEnabled(false);
        m_jPanelTotals.add(m_jTotalEuros);

        m_jPanelLinesSum.add(m_jPanelTotals, java.awt.BorderLayout.LINE_END);

        m_jPanelLines.add(m_jPanelLinesSum, java.awt.BorderLayout.SOUTH);

        m_jPanelTicket.add(m_jPanelLines, java.awt.BorderLayout.CENTER);

        m_jContEntries.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        m_jContEntries.setMinimumSize(new java.awt.Dimension(300, 350));
        m_jContEntries.setLayout(new java.awt.BorderLayout());

        m_jPanEntries.setPreferredSize(new java.awt.Dimension(300, 350));
        m_jPanEntries.setLayout(new javax.swing.BoxLayout(m_jPanEntries, javax.swing.BoxLayout.Y_AXIS));

        m_jNumberKeys.setMaximumSize(new java.awt.Dimension(300, 300));
        m_jNumberKeys.setMinimumSize(new java.awt.Dimension(250, 250));
        m_jNumberKeys.setPreferredSize(new java.awt.Dimension(250, 250));
        m_jNumberKeys.addJNumberEventListener(new com.openbravo.beans.JNumberEventListener() {
            public void keyPerformed(com.openbravo.beans.JNumberEvent evt) {
                m_jNumberKeysKeyPerformed(evt);
            }
        });
        m_jPanEntries.add(m_jNumberKeys);

        jPanelScanner.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jPanelScanner.setMaximumSize(new java.awt.Dimension(300, 105));

        m_jPrice.setFont(new java.awt.Font("Arial", 1, 16)); // NOI18N
        m_jPrice.setForeground(new java.awt.Color(76, 197, 237));
        m_jPrice.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        m_jPrice.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(76, 197, 237)), javax.swing.BorderFactory.createEmptyBorder(1, 4, 1, 4)));
        m_jPrice.setOpaque(true);
        m_jPrice.setPreferredSize(new java.awt.Dimension(100, 25));
        m_jPrice.setRequestFocusEnabled(false);

        m_jEnter.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/barcode.png"))); // NOI18N
        m_jEnter.setToolTipText(bundle.getString("tooltip.salebarcode")); // NOI18N
        m_jEnter.setFocusPainted(false);
        m_jEnter.setFocusable(false);
        m_jEnter.setPreferredSize(new java.awt.Dimension(80, 45));
        m_jEnter.setRequestFocusEnabled(false);
        m_jEnter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jEnterActionPerformed(evt);
            }
        });

        m_jPor.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        m_jPor.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        m_jPor.setText("AS");
        m_jPor.setRequestFocusEnabled(false);

        m_jKeyFactory.setEditable(false);
        m_jKeyFactory.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        m_jKeyFactory.setForeground(javax.swing.UIManager.getDefaults().getColor("Panel.background"));
        m_jKeyFactory.setAutoscrolls(false);
        m_jKeyFactory.setBorder(null);
        m_jKeyFactory.setCaretColor(javax.swing.UIManager.getDefaults().getColor("Panel.background"));
        m_jKeyFactory.setRequestFocusEnabled(false);
        m_jKeyFactory.setVerifyInputWhenFocusTarget(false);
        m_jKeyFactory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jKeyFactoryActionPerformed(evt);
            }
        });
        m_jKeyFactory.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                m_jKeyFactoryKeyTyped(evt);
            }
        });

        m_jaddtax.setToolTipText(bundle.getString("tooltip.switchtax")); // NOI18N
        m_jaddtax.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jaddtaxActionPerformed(evt);
            }
        });

        m_jTax.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jTax.setToolTipText(bundle.getString("tooltip.salestaxswitch")); // NOI18N
        m_jTax.setFocusable(false);

        javax.swing.GroupLayout jPanelScannerLayout = new javax.swing.GroupLayout(jPanelScanner);
        jPanelScanner.setLayout(jPanelScannerLayout);
        jPanelScannerLayout.setHorizontalGroup(
            jPanelScannerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelScannerLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelScannerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelScannerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(m_jPor, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 9, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(m_jKeyFactory, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 8, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(m_jaddtax))
                .addGroup(jPanelScannerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelScannerLayout.createSequentialGroup()
                        .addComponent(m_jPrice, javax.swing.GroupLayout.PREFERRED_SIZE, 187, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 2, Short.MAX_VALUE))
                    .addComponent(m_jTax, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(m_jEnter, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanelScannerLayout.setVerticalGroup(
            jPanelScannerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelScannerLayout.createSequentialGroup()
                .addComponent(m_jPrice, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(m_jTax, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanelScannerLayout.createSequentialGroup()
                .addGroup(jPanelScannerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(m_jEnter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanelScannerLayout.createSequentialGroup()
                        .addComponent(m_jPor)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(m_jKeyFactory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(m_jaddtax, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        m_jPanEntries.add(jPanelScanner);

        m_jContEntries.add(m_jPanEntries, java.awt.BorderLayout.LINE_START);

        m_jPanelTicket.add(m_jContEntries, java.awt.BorderLayout.LINE_END);

        m_jPanelContainer.add(m_jPanelTicket, java.awt.BorderLayout.CENTER);

        m_jPanelCatalog.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        m_jPanelCatalog.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jPanelCatalog.setLayout(new java.awt.BorderLayout());
        m_jPanelContainer.add(m_jPanelCatalog, java.awt.BorderLayout.SOUTH);

        add(m_jPanelContainer, "ticket");
    }// </editor-fold>//GEN-END:initComponents

    private void m_jbtnScaleActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_m_jbtnScaleActionPerformed

        stateTransition('\u00a7');

    }// GEN-LAST:event_m_jbtnScaleActionPerformed

    private void m_jEditLineActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_m_jEditLineActionPerformed

        int i = m_ticketlines.getSelectedIndex();

        if (i < 0) {
            com.openbravo.pos.util.NotifyUtils.beep(); // no line selected
        } else {
            ticketLineController.editLine(this, m_oTicket.getLine(i))
                    .ifPresent(newline -> paintTicketLine(i, newline));
        }

    }// GEN-LAST:event_m_jEditLineActionPerformed

    private void m_jNumberKeysKeyPerformed(com.openbravo.beans.JNumberEvent evt) {// GEN-FIRST:event_m_jNumberKeysKeyPerformed

        stateTransition(evt.getKey());

        j_btnRemotePrt.setEnabled(true);
        j_btnRemotePrt.revalidate();

    }// GEN-LAST:event_m_jNumberKeysKeyPerformed

    private void m_jDeleteActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_m_jDeleteActionPerformed

        int i = m_ticketlines.getSelectedIndex();

        if (i < 0) {
            com.openbravo.pos.util.NotifyUtils.beep();
        } else {
            removeTicketLine(i);
        }
    }// GEN-LAST:event_m_jDeleteActionPerformed

    private void m_jListActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_m_jListActionPerformed

        ProductInfoExt prod = JProductFinder.showMessage(JPanelTicket.this, m_App);
        if (prod != null && m_oTicket != null) {
            buttonTransition(prod);
        } else {
            com.openbravo.pos.util.NotifyUtils.beep();
        }

    }// GEN-LAST:event_m_jListActionPerformed

    private void jEditAttributesActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jEditAttributesActionPerformed
        if (inactivityListener != null) {
            inactivityListener.stop();
        }
        int i = m_ticketlines.getSelectedIndex();
        // no line selected (-1)
        if (i < 0) {
            com.openbravo.pos.util.NotifyUtils.beep();
        } else {
            TicketLineInfo line = m_oTicket.getLine(i);
            if (ticketLineController.editLineAttributes(this, m_App.getSession(), line)) {
                paintTicketLine(i, line);
            }
        }

        if (inactivityListener != null) {
            inactivityListener.restart();
        }
    }// GEN-LAST:event_jEditAttributesActionPerformed

    private void j_btnRemotePrtActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_j_btnRemotePrtActionPerformed
        if (peripheralCoordinator != null) {
            peripheralCoordinator.sendRemoteOrder(
                    m_oTicket,
                    m_oTicketExt,
                    taxeslogic,
                    m_jaddtax.isSelected(),
                    warrantyCheck(m_oTicket),
                    getPickupString(m_oTicket),
                    this
            );
        }
        remoteOrderDisplay();
    }// GEN-LAST:event_j_btnRemotePrtActionPerformed

    private void btnReprint1ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnReprint1ActionPerformed
        if (peripheralCoordinator != null) {
            peripheralCoordinator.reprintLastTicket(
                    this,
                    dlSales,
                    taxeslogic,
                    (res, tck) -> printTicket(res, tck, null),
                    this::Notify
            );
        }
    }// GEN-LAST:event_btnReprint1ActionPerformed

    private void btnSplitActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnSplitActionPerformed
        if (ticketLineController != null) {
            ticketLineController.splitTicket(
                    this, m_oTicket, m_oTicketExt, dlSystem, dlCustomers, taxeslogic,
                    ticket2 -> closeTicket(ticket2, m_oTicketExt))
                    .ifPresent(remainingTicket -> setActiveTicket(remainingTicket, m_oTicketExt));
        }
    }// GEN-LAST:event_btnSplitActionPerformed

    private void jCheckStockActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jCheckStockActionPerformed

        checkAndShowStockForLine(true);
    }// GEN-LAST:event_jCheckStockActionPerformed

    private void jTBtnShowActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jTBtnShowActionPerformed
        if (jTBtnShow.isSelected()) {
            m_jPanelScripts.setVisible(true);
            m_jPanelBagExt.setVisible(true);
        } else {
            m_jPanelScripts.setVisible(false);
            m_jPanelBagExt.setVisible(false);
        }
        refreshTicket();
        m_jKeyFactory.requestFocus();
    }// GEN-LAST:event_jTBtnShowActionPerformed

    private void jBtnCustomerActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jBtnCustomerActionPerformed
        if (inactivityListener != null) {
            inactivityListener.stop();
        }
        if (salesCustomerController != null && m_oTicket != null) {
            CustomerInfoExt currentCustomer = m_oTicket.getCustomer();
            Optional<CustomerInfoExt> chosenCustomer = salesCustomerController.selectCustomer(this, m_oTicket);

            if (chosenCustomer.isPresent()) {
                CustomerInfoExt customerExt = chosenCustomer.get();
                m_oTicket.setCustomer(customerExt);
                if (isRestaurantMode()) {
                    restDB.setCustomerNameInTableByTicketId(customerExt.getName(), m_oTicket.getId());
                }
                checkCustomer();
                m_jTicketId.setText(m_oTicket.getName(m_oTicketExt));
            } else if (currentCustomer != null) {
                // Customer removed or cleared
                m_oTicket.setCustomer(null);
                if (isRestaurantMode()) {
                    restDB.setCustomerNameInTableByTicketId(null, m_oTicket.getId());
                }
                Notify("notify.customerremove");
            }
        }

        refreshTicket();

    }// GEN-LAST:event_jBtnCustomerActionPerformed

    private void m_jEnterActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_m_jEnterActionPerformed

        stateTransition('\n');
    }// GEN-LAST:event_m_jEnterActionPerformed

    private void m_jKeyFactoryKeyTyped(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_m_jKeyFactoryKeyTyped

        m_jKeyFactory.setText(null);

        stateTransition(evt.getKeyChar());
    }// GEN-LAST:event_m_jKeyFactoryKeyTyped

    private void m_jKeyFactoryActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_m_jKeyFactoryActionPerformed
        // TODO add your handling code here:
    }// GEN-LAST:event_m_jKeyFactoryActionPerformed

    private void m_jaddtaxActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_m_jaddtaxActionPerformed
        m_jKeyFactory.requestFocus();
    }// GEN-LAST:event_m_jaddtaxActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnReprint1;
    private javax.swing.JButton btnSplit;
    private javax.swing.Box.Filler filler2;
    private javax.swing.JButton jBtnCustomer;
    private javax.swing.JButton jCheckStock;
    private javax.swing.JButton jEditAttributes;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanelScanner;
    private javax.swing.JToggleButton jTBtnShow;
    private javax.swing.JButton j_btnRemotePrt;
    private javax.swing.JPanel m_jButtons;
    private javax.swing.JPanel m_jContEntries;
    private javax.swing.JButton m_jDelete;
    private javax.swing.JButton m_jEditLine;
    private javax.swing.JButton m_jEnter;
    private javax.swing.JTextField m_jKeyFactory;
    private javax.swing.JLabel m_jLblSubTotalEuros;
    private javax.swing.JLabel m_jLblTaxEuros;
    private javax.swing.JLabel m_jLblTotalEuros;
    private javax.swing.JButton m_jList;
    private com.openbravo.beans.JNumberKeys m_jNumberKeys;
    private javax.swing.JPanel m_jPanEntries;
    private javax.swing.JPanel m_jPanelBag;
    private javax.swing.JPanel m_jPanelBagExt;
    private javax.swing.JPanel m_jPanelBagExtDefaultEmpty;
    private javax.swing.JPanel m_jPanelCatalog;
    private javax.swing.JPanel m_jPanelContainer;
    private javax.swing.JPanel m_jPanelLines;
    private javax.swing.JPanel m_jPanelLinesSum;
    private javax.swing.JPanel m_jPanelLinesToolbar;
    private javax.swing.JPanel m_jPanelMainToolbar;
    private javax.swing.JPanel m_jPanelScripts;
    private javax.swing.JPanel m_jPanelTicket;
    private javax.swing.JPanel m_jPanelTotals;
    private javax.swing.JLabel m_jPor;
    private javax.swing.JLabel m_jPrice;
    private javax.swing.JLabel m_jSubtotalEuros;
    private javax.swing.JComboBox m_jTax;
    private javax.swing.JLabel m_jTaxesEuros;
    private javax.swing.JLabel m_jTicketId;
    private javax.swing.JLabel m_jTotalEuros;
    private javax.swing.JCheckBox m_jaddtax;
    private javax.swing.JButton m_jbtnScale;
    // End of variables declaration//GEN-END:variables

    /**
     * Internal Class utils methods, MUST never open to publics
     */

    /* Application Property */
    private String getAppProperty(String propertyName) {
        return m_App.getProperties().getProperty(propertyName);
    }

    /* Remote Orders Display - Utils methods */
    public void remoteOrderDisplay() {
        if (peripheralCoordinator != null) {
            peripheralCoordinator.displayRemoteOrder(m_oTicket, m_oTicketExt, getPickupString(m_oTicket));
        }
    }

    /* Remote Orders Display - Utils methods */
    public void remoteOrderDisplay(String orderId) {
        if (peripheralCoordinator != null) {
            peripheralCoordinator.displayRemoteOrder(m_oTicket, m_oTicketExt, getPickupString(m_oTicket), orderId);
        }
    }

    /* Remote Orders Display - Utils methods */
    public void remoteOrderDisplay(int display) {
        if (peripheralCoordinator != null) {
            peripheralCoordinator.displayRemoteOrder(m_oTicket, m_oTicketExt, getPickupString(m_oTicket), display);
        }
    }

    /* Remote Orders Display - Utils methods */
    public String remoteOrderId() {
        return peripheralCoordinator != null
                ? peripheralCoordinator.resolveRemoteOrderId(m_oTicket, m_oTicketExt, getPickupString(m_oTicket))
                : "";
    }

    /* Remote Orders Display - Utils methods */
    public void remoteOrderDisplay(String orderId, int display, boolean primary) {
        if (peripheralCoordinator != null) {
            peripheralCoordinator.displayRemoteOrder(m_oTicket, m_oTicketExt, getPickupString(m_oTicket), orderId, display, primary);
        }
    }

    private void updateStockButton(boolean hasStock) {
        if (!hasStock) {
            Color errorColor = javax.swing.UIManager.getColor("Component.error.focusedBorderColor");
            if (errorColor == null) {
                errorColor = javax.swing.UIManager.getColor("nb.errorForeground");
            }
            jCheckStock.setForeground(errorColor != null ? errorColor : javax.swing.UIManager.getColor("Button.foreground"));
        } else {
            jCheckStock.setForeground(javax.swing.UIManager.getColor("Button.foreground"));
        }
    }

    private void checkAndShowStockForLine(boolean showDialog) {
        if (inactivityListener != null) {
            inactivityListener.stop();
        }

        if (salesStockCoordinator != null) {
            int tckLineNumber = m_ticketlines.getSelectedIndex();
            String location = m_App != null ? m_App.getInventoryLocation() : null;
            salesStockCoordinator.checkAndShowStock(this, m_oTicket, tckLineNumber, location, showDialog)
                    .ifPresent(this::updateStockButton);
        }

        if (inactivityListener != null) {
            inactivityListener.restart();
        }
    }

    private class LogoutAction extends AbstractAction {

        public LogoutAction() {
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            closeAllDialogs();
            if (isRestaurantMode()) {
                deactivate();
                if (isAutoLogoutRestaurant()) {
                    ((ApplicationShell) m_App).closeAppView();
                } else {
                    setActiveTicket(null, null);
                }
            } else {
                deactivate();
                ((ApplicationShell) m_App).closeAppView();
            }
        }
    }

    /**
     * Script Argument
     */
    public static class ScriptArg {

        private final String key;
        private final Object value;

        /**
         *
         * @param key
         * @param value
         */
        public ScriptArg(String key, Object value) {
            this.key = key;
            this.value = value;
        }

        /**
         *
         * @return
         */
        public String getKey() {
            return key;
        }

        /**
         *
         * @return
         */
        public Object getValue() {
            return value;
        }
    }

    /**
     * Script Object
     */
    public class ScriptObject {

        private final TicketInfo ticket;
        private final String ticketext;

        private int selectedindex;

        private ScriptObject(TicketInfo ticket, String ticketext) {
            this.ticket = ticket;
            this.ticketext = ticketext;
        }

        /**
         *
         * @return
         */
        public double getInputValue() {
            if (m_iNumberStatusInput == NUMBERVALID && m_iNumberStatusPor == NUMBERZERO) {
                return JPanelTicket.this.getInputValue();
            } else {
                return 0.0;
            }
        }

        /**
         *
         * @return
         */
        public int getSelectedIndex() {
            return selectedindex;
        }

        /**
         *
         * @param i
         */
        public void setSelectedIndex(int i) {
            selectedindex = i;
        }

        /**
         *
         * @param resourcefile
         */
        public void printReport(String resourcefile) {
            JPanelTicket.this.printReport(resourcefile, ticket, ticketext);
        }

        /**
         *
         * @param sresourcename
         */
        public void printTicket(String sresourcename) {
            JPanelTicket.this.printTicket(sresourcename, ticket, ticketext);
            j_btnRemotePrt.setEnabled(false);
        }

        public Object evalScript(String code, ScriptArg... args) throws ScriptException {

            ScriptEngine script = ScriptFactory.getScriptEngine(ScriptFactory.BEANSHELL);

            for (ScriptArg arg : args) {
                script.put(arg.getKey(), arg.getValue());
            }

            return script.eval(code);
        }
    }
}

