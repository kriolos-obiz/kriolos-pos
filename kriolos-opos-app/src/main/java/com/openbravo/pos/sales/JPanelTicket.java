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
import com.openbravo.pos.payment.JPaymentSelect;
import com.openbravo.pos.payment.JPaymentSelectReceipt;
import com.openbravo.pos.payment.JPaymentSelectRefund;
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
import com.openbravo.pos.inventory.DataLogicInventory;
import com.openbravo.pos.inventory.InventoryService;
import com.openbravo.pos.inventory.InventoryServiceImpl;
import com.openbravo.pos.panels.JProductFinderPanel;
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

    private final static long serialVersionUID = 1L;

    private JTicketLines m_ticketlines;
    private JPanelButtons m_jbtnconfig;
    private AppView m_App;
    private DataLogicSystem dlSystem;
    private DataLogicSales dlSales;
    private DataLogicInventory dlInventory;
    private DataLogicCustomers dlCustomers;
    private DataLogicPIM dataLogicPIM;
    private TicketsEditor m_panelticket;
    private TicketInfo m_oTicket;
    private String m_oTicketExt;

    private final SalesKeypadStateMachine keypadStateMachine = new SalesKeypadStateMachine();
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
    private SalesScriptCoordinator salesScriptCoordinator;
    private PaymentService paymentService;
    private InventoryService inventoryService;
    private JPaymentSelect paymentdialogreceipt;
    private JPaymentSelect paymentdialogrefund;
    private InactivityListener inactivityListener;
    private DataLogicReceipts dlReceipts = null;
    private Boolean priceWith00;
    private AppProperties m_config;
    // private Integer count = 0;
    // private Integer oCount = 0;

    /**
     * Creates new form JTicketView
     */
    public JPanelTicket(AppView app) {

        initUIComponents();

        LOGGER.log(System.Logger.Level.DEBUG, "JPanelTicket.init");
        m_config = app.getProperties();

        m_App = app;

        dlSystem = m_App.getBean(DataLogicSystem.class);
        dlSales = m_App.getBean(DataLogicSales.class);
        dlInventory = m_App.getBean(DataLogicInventory.class);
        dlCustomers = m_App.getBean(DataLogicCustomers.class);
        dlReceipts = app.getBean(DataLogicReceipts.class);
        dataLogicPIM = app.getBean(DataLogicPIM.class);

        // Configuration>Peripheral options
        ticketHeaderPane.setScaleVisible(m_App.hasScale());
        ticketHeaderPane.setScriptsVisible(false);
        ticketHeaderPane.setToggleScriptsSelected(false);

        if (Boolean.valueOf(getAppProperty("till.amountattop"))) {
            inputPane.setAmountAtTop(true);
        }

        priceWith00 = ("true".equals(getAppProperty("till.pricewith00")));
        keypadStateMachine.setPriceWith00(priceWith00);

        if (priceWith00) {
            inputPane.setDotIs00(true);
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
        ticketToolbarPane.setCheckStockText(AppLocal.getIntString("message.title.checkstock"));

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

        ticketHeaderPane.addScriptComponent(m_jbtnconfig);
    }

    private void initComponentFromChild() {
        m_ticketsbag = getJTicketsBag();

        // Set Configuration>General>Tickets toolbar simple : standard : restaurant option
        ticketHeaderPane.setBagComponent(m_ticketsbag.getBagComponent());
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

        inputPane.setTaxIncluded("true".equals(m_jbtnconfig.getProperty("taxesincluded")));

        List<TaxInfo> taxlist = senttax.list();
        List<TaxCategoryInfo> taxcategorieslist = senttaxcategories.list();

        // Initialize Services
        taxeslogic = new TaxesLogic(taxlist);
        salesService = new SalesServiceImpl(taxeslogic);
        ticketLineController = new TicketLineController(m_App, salesService, dlSales);
        salesCustomerController = new SalesCustomerController(m_App, dlCustomers);
        salesPaymentCoordinator = new SalesPaymentCoordinator(m_App, dlSales, salesService);
        paymentService = new PaymentServiceImpl();
        inventoryService = new InventoryServiceImpl(dlInventory, m_App.getSession());
        salesStockCoordinator = new SalesStockCoordinator(inventoryService, dataLogicPIM, dlInventory);
        salesBarcodeScanCoordinator = new SalesBarcodeScanCoordinator(dataLogicPIM, dlCustomers);
        salesScriptCoordinator = new SalesScriptCoordinator(dlSystem, () -> m_jbtnconfig);

        paymentdialogreceipt = JPaymentSelectReceipt.getDialog(this);
        paymentdialogreceipt.init(m_App, paymentService);
        paymentdialogrefund = JPaymentSelectRefund.getDialog(this);
        paymentdialogrefund.init(m_App, paymentService);

        String taxesid = m_jbtnconfig.getProperty("taxcategoryid");
        taxcategoriesmodel = new ComboBoxValModel(taxcategorieslist);
        taxcategoriesmodel.setSelectedKey(taxesid);

        inputPane.setTaxModel(taxcategoriesmodel);
        if (taxesid == null) {
            if (inputPane.getTaxComboBox().getItemCount() > 0) {
                inputPane.getTaxComboBox().setSelectedIndex(0);
            }
        } else {
            taxcategoriesmodel.setSelectedKey(taxesid);
        }

        inputPane.setTaxIncluded(Boolean.parseBoolean(getAppProperty("till.taxincluded")));
        boolean canChangeTax = m_App.getAppUserView().getUser().hasPermission("sales.ChangeTaxOptions");
        inputPane.setTaxControlsVisible(canChangeTax);

        ticketToolbarPane.setDeleteLineEnabled(m_App.hasPermission("sales.EditLines"));
        inputPane.setMinusEnabled(m_App.hasPermission("sales.EditLines"));
        inputPane.setEqualsEnabled(m_App.hasPermission("sales.Total"));
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

                ticketHeaderPane.setRemoteOrderVisible(m_App.hasPermission("sales.PrintRemote"));
                ticketHeaderPane.setRemoteOrderEnabled(m_App.hasPermission("sales.PrintRemote"));
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
            ticketSummaryPane.clear();
            m_ticketlines.clearTicketLines();

            checkStock();
            stateToZero();
            repaint();

            cl.show(this, "null");

            if ((m_oTicket != null) && (m_oTicket.getLinesCount() == 0)) {
                resetSouthComponent();
            }

        } else {
            if (m_oTicket.getTicketType() == TicketInfo.RECEIPT_REFUND) {
                ticketToolbarPane.setEditLineVisible(false);
                ticketToolbarPane.setFindProductVisible(false);
            } else {
                ticketToolbarPane.setEditLineVisible(true);
                ticketToolbarPane.setFindProductVisible(true);
            }

            m_oTicket.getLines().forEach((line) -> {
                line.setTaxInfo(taxeslogic.getTaxInfo(line
                        .getProductTaxCategoryID(), m_oTicket.getCustomer()));
            });

            ticketSummaryPane.setTicketName(m_oTicket.getName(m_oTicketExt));
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

            inputPane.setKeyFactoryText(null);
            java.awt.EventQueue.invokeLater(() -> {
                inputPane.requestKeyFactoryFocus();
            });
        }
    }

    private void countArticles() {

        if (m_oTicket != null) {
            ticketHeaderPane.setSplitEnabled(m_App.hasPermission("sales.Total") && m_oTicket.getArticlesCount() > 1);
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
        if (ticketSummaryPane != null) {
            ticketSummaryPane.updateTotals(m_oTicket);
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
            priceIncludesTax = inputPane.isTaxIncluded();
        } else {
            ticketHeaderPane.setRemoteOrderEnabled(true);
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
        return inputPane.isTaxIncluded();
    }

    private double includeTaxes(String tcid, double dValue) {
        if (inputPane.isTaxIncluded()) {
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

            return Double.parseDouble(inputPane.getPriceText());
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
            return Double.parseDouble(inputPane.getPorText().substring(1));
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
        inputPane.clearInput();
        m_sBarcode = new StringBuffer();
        repaint();
    }

    private void incProductByCode(String sCode) {
        if (salesBarcodeScanCoordinator != null) {
            salesBarcodeScanCoordinator.processBarcode(
                    this,
                    sCode,
                    taxeslogic,
                    m_oTicket != null ? m_oTicket.getCustomer() : null,
                    inputPane.isTaxIncluded(),
                    customer -> {
                        m_oTicket.setCustomer(customer);
                        ticketSummaryPane.setTicketName(m_oTicket.getName(m_oTicketExt));
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
                incProductByCode(m_sBarcode.toString());
            } else {
                com.openbravo.pos.util.NotifyUtils.beep();
            }

        } else {

            m_sBarcode.append(cTrans);

            if (cTrans == '\u007f') {
                stateToZero();
            } else if (keypadStateMachine.processKeypadChar(cTrans)) {
                inputPane.setPriceText(keypadStateMachine.getPriceText());
                inputPane.setPorText(keypadStateMachine.getPorText());
            } else if (cTrans == '\u00a7'
                    && keypadStateMachine.isInputValid()
                    && keypadStateMachine.isPorZero()) {

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
                    && keypadStateMachine.isInputZero()
                    && keypadStateMachine.isPorZero()) {

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

            } else if ((cTrans == '+' || cTrans == '-')
                    && keypadStateMachine.isInputZero()) {
                if (cTrans == '-' && !m_App.hasPermission("sales.EditLines")) {
                    com.openbravo.pos.util.NotifyUtils.beep();
                } else if (keypadStateMachine.isPorZero()) {
                    applyLineQuantityChange(cTrans == '-' ? -1.0 : 1.0, false);
                } else if (keypadStateMachine.isPorValid()) {
                    applyLineQuantityChange(getPorValue(), true);
                }
            } else if ((cTrans == '+' || cTrans == '-')
                    && keypadStateMachine.isInputValid()
                    && m_App.hasPermission("sales.EditLines")) {
                double sign = (cTrans == '-') ? -1.0 : 1.0;
                ProductInfoExt product = getInputProduct();
                if (keypadStateMachine.isPorZero()) {
                    addTicketLine(product, 1.0, sign * product.getPriceSell());
                    ticketToolbarPane.triggerEditLine();
                } else if (keypadStateMachine.isPorValid()) {
                    addTicketLine(product, getPorValue(), sign * product.getPriceSell());
                }

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
                    if (m_ticketsbag != null) {
                        m_ticketsbag.ticketClosed(ticket, ticketext);
                    }
                }
        );
    }

    private boolean warrantyCheck(TicketInfo ticket) {
        return salesPaymentCoordinator != null
                ? salesPaymentCoordinator.hasWarrantyProduct(ticket)
                : false;
    }

    public String getPickupString(TicketInfo pTicket) {
        return SalesPeripheralCoordinator.formatPickupId(pTicket, getAppProperty("till.pickupsize"));
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
        ticketHeaderPane.setRemoteOrderEnabled(false);
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

    private Object evalScript(String resource, ScriptArg... args) {
        if (salesScriptCoordinator == null) {
            return null;
        }
        return salesScriptCoordinator.evalScript(this, resource, args);
    }

    private void evalScriptForExternalButtons(String resource) {
        if (salesScriptCoordinator != null) {
            int prevIndex = m_ticketlines.getSelectedIndex();
            salesScriptCoordinator.evalScriptForExternalButton(
                    this,
                    resource,
                    m_oTicket,
                    m_App.getAppUserView().getUser(),
                    this,
                    () -> {
                        refreshTicket();
                        setSelectedIndex(prevIndex);
                    }
            );
        }
    }

    private void evalScriptAndRefresh(String resource, ScriptArg... args) {
        if (salesScriptCoordinator != null) {
            int prevIndex = m_ticketlines.getSelectedIndex();
            salesScriptCoordinator.evalScript(this, resource, args);
            refreshTicket();
            setSelectedIndex(prevIndex);
        }
    }

    private Object executeEvent(TicketInfo ticket, String ticketExt, String eventKey, ScriptArg... args) {
        if (salesScriptCoordinator == null) {
            return null;
        }
        boolean hasTicketArg = false;
        if (args != null) {
            for (ScriptArg a : args) {
                if (a != null && "ticket".equals(a.key())) {
                    hasTicketArg = true;
                    break;
                }
            }
        }
        if (!hasTicketArg && ticket != null) {
            ScriptArg[] extendedArgs = new ScriptArg[(args != null ? args.length : 0) + 1];
            if (args != null && args.length > 0) {
                System.arraycopy(args, 0, extendedArgs, 0, args.length);
            }
            extendedArgs[extendedArgs.length - 1] = new ScriptArg("ticket", ticket);
            return salesScriptCoordinator.executeEvent(this, eventKey, extendedArgs);
        }
        return salesScriptCoordinator.executeEvent(this, eventKey, args);
    }

    public String getResourceAsXML(String sresourcename) {
        return dlSystem.getResourceAsXML(sresourcename);
    }

    public BufferedImage getResourceAsImage(String sresourcename) {
        return dlSystem.getResourceAsImage(sresourcename);
    }

    public void setSelectedIndex(int i) {

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

    private void initUIComponents() {
        ticketToolbarPane = new TicketToolbarPane();
        ticketToolbarPane.setOnDeleteLine(this::deleteSelectedLine);
        ticketToolbarPane.setOnFindProduct(this::findProduct);
        ticketToolbarPane.setOnEditLine(this::editSelectedLine);
        ticketToolbarPane.setOnEditAttributes(this::editSelectedLineAttributes);
        ticketToolbarPane.setOnCheckStock(this::checkStock);

        ticketSummaryPane = new TicketSummaryPane();

        ticketHeaderPane = new TicketHeaderPane();
        ticketHeaderPane.setOnToggleScripts(this::handleToggleScripts);
        ticketHeaderPane.setOnScaleAction(this::readScale);
        ticketHeaderPane.setOnCustomerAction(this::selectOrClearCustomer);
        ticketHeaderPane.setOnSplitAction(this::splitTicket);
        ticketHeaderPane.setOnRemoteOrderAction(this::sendRemoteOrder);
        ticketHeaderPane.setOnReprintAction(this::reprintLastTicket);

        inputPane = new InputPane();
        inputPane.addNumberEventListener(this::handleKeypadInput);
        inputPane.setOnEnterAction(this::handleBarcodeEnter);
        inputPane.setOnKeyFactoryTyped(this::stateTransition);
        inputPane.setOnKeyFactoryAction(() -> {});
        inputPane.setOnAddTaxAction(inputPane::requestKeyFactoryFocus);

        setOpaque(false);
        setLayout(new CardLayout());

        m_jPanelContainer = new JPanel(new BorderLayout());
        m_jPanelContainer.add(ticketHeaderPane, BorderLayout.NORTH);

        m_jPanelTicket = new JPanel(new BorderLayout());
        m_jPanelTicket.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        m_jPanelTicket.add(ticketToolbarPane, BorderLayout.LINE_START);

        m_jPanelLines = new JPanel(new BorderLayout());
        m_jPanelLines.setFont(new Font("Arial", Font.PLAIN, 14));
        m_jPanelLines.setPreferredSize(new Dimension(450, 240));
        m_jPanelLines.add(ticketSummaryPane, BorderLayout.SOUTH);
        m_jPanelTicket.add(m_jPanelLines, BorderLayout.CENTER);

        m_jPanelTicket.add(inputPane, BorderLayout.LINE_END);

        m_jPanelContainer.add(m_jPanelTicket, BorderLayout.CENTER);

        m_jPanelCatalog = new JPanel(new BorderLayout());
        m_jPanelCatalog.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        m_jPanelCatalog.setFont(new Font("Arial", Font.PLAIN, 14));
        m_jPanelContainer.add(m_jPanelCatalog, BorderLayout.SOUTH);

        add(m_jPanelContainer, "ticket");
    }

    private void handleKeypadInput(com.openbravo.beans.JNumberEvent evt) {
        stateTransition(evt.getKey());
        ticketHeaderPane.setRemoteOrderEnabled(true);
        ticketHeaderPane.getBtnRemoteOrder().revalidate();
    }

    private void readScale() {
        stateTransition('\u00a7');
    }

    private void handleBarcodeEnter() {
        stateTransition('\n');
    }

    private void handleToggleScripts(boolean selected) {
        ticketHeaderPane.setScriptsVisible(selected);
        refreshTicket();
        inputPane.requestKeyFactoryFocus();
    }

    private void selectOrClearCustomer() {
        if (inactivityListener != null) {
            inactivityListener.stop();
        }
        if (salesCustomerController != null && m_oTicket != null) {
            CustomerInfoExt currentCustomer = m_oTicket.getCustomer();
            Optional<CustomerInfoExt> chosenCustomer = salesCustomerController.selectCustomer(this, m_oTicket);

            if (chosenCustomer.isPresent()) {
                CustomerInfoExt customerExt = chosenCustomer.get();
                m_oTicket.setCustomer(customerExt);
                if (m_ticketsbag != null) {
                    m_ticketsbag.customerUpdated(customerExt, m_oTicket.getId());
                }
                checkCustomer();
                ticketSummaryPane.setTicketName(m_oTicket.getName(m_oTicketExt));
            } else if (currentCustomer != null) {
                // Customer removed or cleared
                m_oTicket.setCustomer(null);
                if (m_ticketsbag != null) {
                    m_ticketsbag.customerUpdated(null, m_oTicket.getId());
                }
                Notify("notify.customerremove");
            }
        }

        refreshTicket();
    }

    private void splitTicket() {
        if (ticketLineController != null) {
            ticketLineController.splitTicket(
                    this, m_oTicket, m_oTicketExt, dlSystem, dlCustomers, taxeslogic,
                    ticket2 -> closeTicket(ticket2, m_oTicketExt))
                    .ifPresent(remainingTicket -> setActiveTicket(remainingTicket, m_oTicketExt));
        }
    }

    private void sendRemoteOrder() {
        if (peripheralCoordinator != null) {
            peripheralCoordinator.sendRemoteOrder(
                    m_oTicket,
                    m_oTicketExt,
                    taxeslogic,
                    inputPane.isTaxIncluded(),
                    warrantyCheck(m_oTicket),
                    getPickupString(m_oTicket),
                    this
            );
        }
        remoteOrderDisplay();
    }

    private void reprintLastTicket() {
        if (peripheralCoordinator != null) {
            peripheralCoordinator.reprintLastTicket(
                    this,
                    dlSales,
                    taxeslogic,
                    (res, tck) -> printTicket(res, tck, null),
                    this::Notify
            );
        }
    }

    private void editSelectedLine() {
        int i = m_ticketlines.getSelectedIndex();
        if (i < 0) {
            com.openbravo.pos.util.NotifyUtils.beep();
        } else {
            ticketLineController.editLine(this, m_oTicket.getLine(i))
                    .ifPresent(newline -> paintTicketLine(i, newline));
        }
    }

    private void deleteSelectedLine() {
        int i = m_ticketlines.getSelectedIndex();
        if (i < 0) {
            com.openbravo.pos.util.NotifyUtils.beep();
        } else {
            removeTicketLine(i);
        }
    }

    private void findProduct() {
        ProductInfoExt prod = JProductFinderPanel.showMessage(this, m_App);
        if (prod != null && m_oTicket != null) {
            buttonTransition(prod);
        } else {
            com.openbravo.pos.util.NotifyUtils.beep();
        }
    }

    private void editSelectedLineAttributes() {
        if (inactivityListener != null) {
            inactivityListener.stop();
        }
        int i = m_ticketlines.getSelectedIndex();
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
    }

    // UI Panes & Swing Components
    private TicketHeaderPane ticketHeaderPane;
    private TicketToolbarPane ticketToolbarPane;
    private TicketSummaryPane ticketSummaryPane;
    private InputPane inputPane;
    private JPanel m_jPanelContainer;
    private JPanel m_jPanelTicket;
    private JPanel m_jPanelLines;
    private JPanel m_jPanelCatalog;

    public TicketHeaderPane getTicketHeaderPane() {
        return ticketHeaderPane;
    }

    public InputPane getInputPane() {
        return inputPane;
    }

    public TicketToolbarPane getTicketToolbarPane() {
        return ticketToolbarPane;
    }

    public TicketSummaryPane getTicketSummaryPane() {
        return ticketSummaryPane;
    }

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
        if (ticketToolbarPane != null) {
            ticketToolbarPane.setStockAvailable(hasStock);
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
}

