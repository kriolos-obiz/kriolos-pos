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
package com.openbravo.pos.sales.modern.two;

import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.modal.PosUIModal;
import com.openbravo.pos.customers.CustomerInfo;
import com.openbravo.pos.customers.CustomerInfoExt;
import com.openbravo.pos.customers.CustomerService;
import com.openbravo.pos.customers.DataLogicCustomers;
import com.openbravo.pos.customers.JCustomerFinderPanel;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.DataLogicSystem;
import com.openbravo.pos.forms.JPanelView;
import com.openbravo.pos.payment.JPaymentSelect;
import com.openbravo.pos.payment.JPaymentSelectReceipt;
import com.openbravo.pos.payment.PaymentService;
import com.openbravo.pos.payment.PaymentServiceImpl;
import com.openbravo.pos.catalog.CatalogService;
import com.openbravo.pos.pim.CategoryInfo;
import com.openbravo.pos.pim.DataLogicPIM;
import com.openbravo.pos.printer.TicketParser;
import com.openbravo.pos.sales.AuditService;
import com.openbravo.pos.sales.JProductLineEditPanel;
import com.openbravo.pos.sales.SalesService;
import com.openbravo.pos.sales.SalesServiceImpl;
import com.openbravo.pos.sales.SharedTicketInfo;
import com.openbravo.pos.sales.SharedTicketService;
import com.openbravo.pos.sales.TaxService;
import com.openbravo.pos.sales.TaxesException;
import com.openbravo.pos.sales.TaxesLogic;
import com.openbravo.pos.sales.TicketLifecycleService;
import com.openbravo.pos.sales.TicketsEditor;
import com.openbravo.pos.sales.modern.FlexFactory;
import com.openbravo.pos.sales.modern.FlexLayout;
import com.openbravo.pos.sales.shared.JTicketsBagSharedPanel;
import com.openbravo.pos.ticket.ProductInfoExt;
import com.openbravo.pos.ticket.TaxInfo;
import com.openbravo.pos.ticket.TicketInfo;
import com.openbravo.pos.ticket.TicketLineInfo;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;

/**
 * ModernTwo dual-pane sales layout entry point extending {@link JTicketsBag}.
 * <p>
 * Architecture:
 * <ul>
 *   <li><b>LINE_START (Left in LTR)</b>: Digital receipt Order Pane (~35% width, ~380px) with
 *       massive Pay button and consolidated checkout zone.</li>
 *   <li><b>LINE_END (Right in LTR)</b>: Touch Catalog Pane (~65% width) with global search,
 *       category pills, and responsive product wrapping grid.</li>
 *   <li><b>Divider</b>: 1px clean hairline divider with LaF-agnostic subtle border.</li>
 *   <li><b>Orientation</b>: Full RTL and LTR support using logical layout constants.</li>
 * </ul>
 *
 * @author KriolOS Team
 */
public class ModernTwo extends JPanel implements JPanelView, TicketsEditor {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(ModernTwo.class.getName());

    private final AppView app;
    private final TicketsEditor panelticket;

    // Data Logics and Services
    private SharedTicketService dlReceipts;
    private CatalogService catalogService;
    private DataLogicPIM dlPim;
    private TicketLifecycleService ticketLifecycleService;
    private TaxService taxService;
    private AuditService auditService;
    private CustomerService customerService;
    @Deprecated
    private DataLogicCustomers dlCustomers;
    private DataLogicSystem dlSystem;
    private TaxesLogic taxeslogic;
    private PaymentService paymentService;
    private SalesService salesService;
    private TicketParser ticketParser;
    private JPaymentSelect paymentDialog;

    // UI Sub-components
    private ModernTwoTicketPane ticketPane;
    private ModernTwoCatalogPane catalogPane;
    private ModernTwoActionPane actionPane;

    // Transaction State
    private TicketInfo activeTicket;
    private String activeTicketExt;
    private CustomerInfoExt activeCustomer;

    public ModernTwo(AppView app, TicketsEditor panelticket) {
        this.app = app;
        this.panelticket = panelticket;
        setName("kriolos:sales:modern-two:panel");

        initDataLogics();
        initUI();
    }

    public ModernTwo(AppView app) {
        this(app, null);
    }

    private void initDataLogics() {
        if (app != null) {
            this.dlReceipts = app.getBean(SharedTicketService.class);
            this.catalogService = app.getBean(CatalogService.class);
            this.dlPim = (catalogService instanceof DataLogicPIM) ? (DataLogicPIM) catalogService : null;
            this.ticketLifecycleService = app.getBean(TicketLifecycleService.class);
            this.taxService = app.getBean(TaxService.class);
            this.auditService = app.getBean(AuditService.class);
            this.customerService = app.getBean(CustomerService.class);
            this.dlCustomers = (customerService instanceof DataLogicCustomers) ? (DataLogicCustomers) customerService : app.getBean(DataLogicCustomers.class);
            this.dlSystem = app.getBean(DataLogicSystem.class);

            if (taxService != null) {
                List<TaxInfo> taxList = taxService.getTaxListAll();
                this.taxeslogic = new TaxesLogic(taxList);
                this.paymentService = new PaymentServiceImpl();
                this.salesService = new SalesServiceImpl(taxeslogic);
            }

            if (dlSystem != null) {
                this.ticketParser = app.createTicketParser();
            }
        }
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 10));
        setOpaque(true);
        setBackground(UIManager.getColor("Panel.background"));
        setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));

        // 1. TOP HEADER (Image 1 operator banner)
        JPanel headerRow = FlexFactory.createRow(
                FlexLayout.JustifyContent.START,
                FlexLayout.AlignItems.CENTER,
                10
        );
        headerRow.setOpaque(false);
        headerRow.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));

        String operator = (app != null && app.getAppUserView() != null && app.getAppUserView().getUser() != null)
                ? app.getAppUserView().getUser().getName()
                : "Operador 01";
        JLabel lblHeaderTitle = new JLabel("Caixa Dispon\u00edvel: " + operator);
        lblHeaderTitle.setFont(lblHeaderTitle.getFont().deriveFont(Font.BOLD, 15f));
        headerRow.add(lblHeaderTitle, "grow: 1");
        add(headerRow, BorderLayout.PAGE_START);

        // 2. MAIN BODY (FlexLayout: Catalog 70% on LEFT, Ticket 30% on RIGHT)
        JPanel bodyRow = FlexFactory.createRow(
                FlexLayout.JustifyContent.START,
                FlexLayout.AlignItems.STRETCH,
                12
        );
        bodyRow.setOpaque(false);

        // Catalog Pane (70% - LEFT)
        catalogPane = new ModernTwoCatalogPane(this::addProductToTicket);
        catalogPane.setOnBarcodeScanned(this::handleBarcodeScanned);

        // Ticket Pane (30% - RIGHT)
        ticketPane = new ModernTwoTicketPane();
        hookTicketPaneEvents();

        bodyRow.add(catalogPane, "grow: 7; basis: 70%");
        bodyRow.add(ticketPane,  "grow: 3; basis: 30%");

        add(bodyRow, BorderLayout.CENTER);

        // 3. BOTTOM ACTIONS (Customer, Parked count, Cash operations, Line modifiers & Order actions)
        actionPane = new ModernTwoActionPane();
        hookActionPaneEvents();
        add(actionPane, BorderLayout.SOUTH);

        setupKeyBindings();
        createNewTicket();
    }

    private void setupKeyBindings() {
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), "actionF3CashIn");
        getActionMap().put("actionF3CashIn", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                executeCashIn();
            }
        });

        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0), "actionF4CashOut");
        getActionMap().put("actionF4CashOut", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                executeCashOut();
            }
        });

        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0), "actionF12Pay");
        getActionMap().put("actionF12Pay", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                executeCheckout();
            }
        });
    }

    private void hookTicketPaneEvents() {
        ticketPane.setOnPayClicked(this::executeCheckout);
    }

    private void hookActionPaneEvents() {
        actionPane.setOnQtyAdjusted(this::adjustSelectedLineQuantity);
        actionPane.setOnEditLineClicked(this::editSelectedLine);
        actionPane.setOnDeleteLineClicked(this::deleteSelectedLine);
        actionPane.setOnClearClicked(this::clearCurrentTicket);
        actionPane.setOnHoldClicked(this::parkCurrentTicket);
        actionPane.setOnDiscountClicked(this::applyDiscount);
        actionPane.setOnCustomerClicked(this::selectCustomer);
        actionPane.setOnParkedOrdersClicked(this::showParkedTickets);
        actionPane.setOnCashInClicked(this::executeCashIn);
        actionPane.setOnCashOutClicked(this::executeCashOut);
        actionPane.setOnCloseCashClicked(this::executeCloseCash);
    }

    @Override
    public void activate() {
        LOGGER.log(Level.INFO, "Activating ModernTwo sales layout");
        loadCatalogData();

        if (activeTicket == null) {
            createNewTicket();
        } else {
            ensureTicketUser(activeTicket);
            if (activeTicket.getActiveCash() == null && app != null) {
                activeTicket.setActiveCash(app.getActiveCashIndex());
            }
            refreshTicket();
        }

        updateParkedCount();
        catalogPane.focusSearch();
    }

    @Override
    public boolean deactivate() {
        LOGGER.log(Level.INFO, "Deactivating ModernTwo sales layout");
        saveDraftTicket();
        return true;
    }

    @Override
    public void setActiveTicket(TicketInfo oTicket, String oTicketExt) {
        this.activeTicket = oTicket;
        this.activeTicketExt = oTicketExt;
        if (activeTicket != null) {
            ensureTicketUser(activeTicket);
            if (activeTicket.getCustomer() != null) {
                this.activeCustomer = activeTicket.getCustomer();
            }
        }
        refreshTicket();
    }

    @Override
    public TicketInfo getActiveTicket() {
        return activeTicket;
    }

    public void deleteTicket() {
        if (auditService != null && app != null && app.getAppUserView() != null && app.getAppUserView().getUser() != null) {
            auditService.addTicketDeleted(app.getAppUserView().getUser().getName());
        }
        clearCurrentTicket();
    }

    @Override
    public String getTitle() {
        return "";
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    private void createNewTicket() {
        activeTicket = new TicketInfo();
        if (app != null && app.getAppUserView() != null && app.getAppUserView().getUser() != null) {
            activeTicket.setUser(app.getAppUserView().getUser().getUserInfo());
            activeTicket.setActiveCash(app.getActiveCashIndex());
        }
        activeTicket.setDate(new Date());
        activeTicketExt = null;
        activeCustomer = null;
        refreshTicket();
    }

    private void ensureTicketUser(TicketInfo ticket) {
        if (ticket != null && ticket.getUser() == null) {
            if (app != null && app.getAppUserView() != null && app.getAppUserView().getUser() != null) {
                ticket.setUser(app.getAppUserView().getUser().getUserInfo());
            }
        }
    }

    private void refreshTicket() {
        if (activeTicket != null && taxeslogic != null) {
            for (TicketLineInfo line : activeTicket.getLines()) {
                TaxInfo tax = taxeslogic.getTaxInfo(line.getProductTaxCategoryID(), activeCustomer);
                line.setTaxInfo(tax);
            }
            try {
                taxeslogic.calculateTaxes(activeTicket);
            } catch (TaxesException e) {
                LOGGER.log(Level.WARNING, "Error calculating taxes for ticket in ModernTwo", e);
            }
        }
        ticketPane.updateTicket(activeTicket, activeCustomer);
        if (actionPane != null) {
            actionPane.setCustomer(activeCustomer);
        }
    }

    private void loadCatalogData() {
        try {
            if (catalogService != null) {
                List<CategoryInfo> categories = catalogService.getRootCategories();
                catalogPane.setCategories(categories);

                List<ProductInfoExt> allProducts = new ArrayList<>();
                for (CategoryInfo cat : categories) {
                    List<ProductInfoExt> prods = catalogService.getProductCatalog(cat.getID());
                    if (prods != null) {
                        allProducts.addAll(prods);
                    }
                }
                catalogPane.setProducts(allProducts);
            }
        } catch (BasicException e) {
            LOGGER.log(Level.WARNING, "Failed to load catalog data in ModernTwo", e);
        }
    }

    public void addProductToTicket(ProductInfoExt product) {
        if (product == null) return;
        if (activeTicket == null) {
            createNewTicket();
        } else {
            ensureTicketUser(activeTicket);
        }

        TaxInfo tax = (taxeslogic != null)
                ? taxeslogic.getTaxInfo(product.getTaxCategoryID(), activeCustomer)
                : null;

        double unitPrice = product.getPriceSell();
        TicketLineInfo line = new TicketLineInfo(product, 1.0, unitPrice, tax, new java.util.Properties());
        activeTicket.addLine(line);

        refreshTicket();
        catalogPane.focusSearch();
    }

    private void handleBarcodeScanned(String barcode) {
        if (catalogService != null && barcode != null && !barcode.isBlank()) {
            try {
                ProductInfoExt prod = catalogService.getProductInfoByCode(barcode);
                if (prod != null) {
                    addProductToTicket(prod);
                } else {
                    JOptionPane.showMessageDialog(
                            this,
                            AppLocal.getIntString("message.noproduct"),
                            AppLocal.getIntString("title.editor"),
                            JOptionPane.WARNING_MESSAGE);
                }
            } catch (BasicException e) {
                LOGGER.log(Level.WARNING, "Barcode lookup error: " + barcode, e);
            }
        }
    }

    private void adjustSelectedLineQuantity(double delta) {
        if (activeTicket == null) return;
        int idx = ticketPane.getSelectedLineIndex();
        if (idx >= 0 && idx < activeTicket.getLinesCount()) {
            TicketLineInfo line = activeTicket.getLine(idx);
            double newQty = line.getMultiply() + delta;
            if (newQty <= 0.0) {
                deleteSelectedLine();
            } else {
                line.setMultiply(newQty);
                line.setProperty("ticket.updated", "true");
                refreshTicket();
            }
        }
    }

    private void deleteSelectedLine() {
        if (activeTicket == null) return;
        int idx = ticketPane.getSelectedLineIndex();
        if (idx >= 0 && idx < activeTicket.getLinesCount()) {
            activeTicket.removeLine(idx);
            refreshTicket();
        }
    }

    private void editSelectedLine() {
        if (activeTicket == null) return;
        int idx = ticketPane.getSelectedLineIndex();
        if (idx >= 0 && idx < activeTicket.getLinesCount()) {
            TicketLineInfo line = activeTicket.getLine(idx);
            try {
                TicketLineInfo edited = JProductLineEditPanel.showMessage(this, app, line);
                if (edited != null) {
                    activeTicket.setLine(idx, edited);
                    refreshTicket();
                }
            } catch (BasicException e) {
                LOGGER.log(Level.WARNING, "Error editing line in ModernTwo", e);
            }
        }
    }

    private void applyDiscount() {
        if (activeTicket == null || activeTicket.getLinesCount() == 0) return;

        String input = JOptionPane.showInputDialog(
                this,
                "Introduza a percentagem de desconto (ex: 10 para 10%):",
                "Desconto Global",
                JOptionPane.QUESTION_MESSAGE);

        if (input != null && !input.isBlank()) {
            try {
                double pct = Double.parseDouble(input.trim());
                if (pct > 0 && pct <= 100) {
                    double rate = (100.0 - pct) / 100.0;
                    for (int i = 0; i < activeTicket.getLinesCount(); i++) {
                        TicketLineInfo l = activeTicket.getLine(i);
                        l.setPrice(l.getPrice() * rate);
                    }
                    refreshTicket();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Valor de desconto inv\u00e1lido.", "Aviso", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void selectCustomer() {
        if (customerService == null) return;
        CustomerInfo selected = JCustomerFinderPanel.show(this, customerService);
        if (selected != null) {
            try {
                activeCustomer = customerService.findCustomerInfoExtById(selected.getId());
                if (activeTicket != null) {
                    activeTicket.setCustomer(activeCustomer);
                }
                refreshTicket();
            } catch (BasicException e) {
                LOGGER.log(Level.WARNING, "Error finding customer details in ModernTwo", e);
            }
        }
    }

    private void clearCurrentTicket() {
        if (activeTicket != null && activeTicket.getLinesCount() > 0) {
            int res = JOptionPane.showConfirmDialog(
                    this,
                    AppLocal.getIntString("message.clearticketquestion"),
                    AppLocal.getIntString("title.editor"),
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (res == JOptionPane.YES_OPTION) {
                createNewTicket();
            }
        } else {
            createNewTicket();
        }
    }

    private void executeCheckout() {
        if (activeTicket == null || activeTicket.getLinesCount() == 0) {
            JOptionPane.showMessageDialog(
                    this,
                    AppLocal.getIntString("message.ticketempty"),
                    AppLocal.getIntString("title.editor"),
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        ensureTicketUser(activeTicket);
        if (salesService != null) {
            try {
                salesService.calculateTaxes(activeTicket);
            } catch (TaxesException e) {
                LOGGER.log(Level.WARNING, "Error calculating taxes during payment in ModernTwo", e);
            }
        }
        activeTicket.resetPayments();

        if (paymentDialog == null) {
            paymentDialog = JPaymentSelectReceipt.getDialog(this);
            paymentDialog.init(app, paymentService);
        }

        paymentDialog.setPrintSelected(true);
        paymentDialog.setTransactionID(activeTicket.getTransactionID());

        if (paymentDialog.showDialog(activeTicket.getTotal(), activeCustomer)) {
            activeTicket.setPayments(paymentDialog.getSelectedPayments());
            activeTicket.setUser(app.getAppUserView().getUser().getUserInfo());
            activeTicket.setActiveCash(app.getActiveCashIndex());
            activeTicket.setDate(new Date());

            try {
                if (ticketLifecycleService != null) {
                    ticketLifecycleService.saveTicket(activeTicket, app.getInventoryLocation());
                }

                if (paymentDialog.isPrintSelected()) {
                    printReceipt(activeTicket);
                }

                createNewTicket();
                updateParkedCount();
            } catch (BasicException ex) {
                LOGGER.log(Level.SEVERE, "Failed to save ticket in ModernTwo", ex);
                JOptionPane.showMessageDialog(
                        this,
                        AppLocal.getIntString("message.cannotsaveticket"),
                        AppLocal.getIntString("title.editor"),
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void printReceipt(TicketInfo ticket) {
        if (ticket == null || ticketParser == null) return;
        try {
            String template = dlSystem.getResourceAsXML("Printer.Ticket");
            if (template != null && !template.isBlank()) {
                ticketParser.printTicket(template, ticket);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Receipt printing failed or printer unavailable", e);
        }
    }

    private void saveOrUpdateSharedTicket(TicketInfo ticket) {
        if (ticket != null && ticket.getLinesCount() > 0 && dlReceipts != null) {
            ensureTicketUser(ticket);
            if (ticket.getUser() == null) {
                LOGGER.log(Level.WARNING, "Cannot save shared ticket: authenticated user is not available.");
                return;
            }
            try {
                String id = ticket.getId();
                int pickupId = ticket.getPickupId();
                try {
                    dlReceipts.insertSharedTicket(id, ticket, pickupId);
                } catch (Exception ex) {
                    dlReceipts.updateSharedTicket(id, ticket, pickupId);
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to save or update shared ticket", e);
            }
        }
    }

    private void saveDraftTicket() {
        if (activeTicket != null && activeTicket.getLinesCount() > 0) {
            saveOrUpdateSharedTicket(activeTicket);
        }
    }

    private void parkCurrentTicket() {
        if (activeTicket != null && activeTicket.getLinesCount() > 0) {
            saveOrUpdateSharedTicket(activeTicket);
            createNewTicket();
            updateParkedCount();
        }
    }

    private void showParkedTickets() {
        if (dlReceipts == null) return;
        try {
            List<SharedTicketInfo> list = dlReceipts.getSharedTicketList();
            String selectedId = JTicketsBagSharedPanel.show(this, list, dlReceipts);
            if (selectedId != null) {
                if (activeTicket != null && activeTicket.getLinesCount() > 0) {
                    saveOrUpdateSharedTicket(activeTicket);
                }
                TicketInfo recalled = dlReceipts.getSharedTicket(selectedId);
                dlReceipts.deleteSharedTicket(selectedId);
                if (recalled != null) {
                    setActiveTicket(recalled, null);
                }
                updateParkedCount();
            }
        } catch (BasicException e) {
            LOGGER.log(Level.WARNING, "Failed to recall parked ticket in ModernTwo", e);
        }
    }

    private void updateParkedCount() {
        if (dlReceipts == null) return;
        try {
            List<SharedTicketInfo> list = dlReceipts.getSharedTicketList();
            int count = (list != null) ? list.size() : 0;
            if (actionPane != null) {
                actionPane.setParkedCount(count);
            }
        } catch (BasicException e) {
            LOGGER.log(Level.WARNING, "Failed to read parked count in ModernTwo", e);
        }
    }

    private void executeCashIn() {
        saveDraftTicket();
        if (app != null && app.getAppUserView() != null) {
            app.getAppUserView().showTask("com.openbravo.pos.panels.JPanelPayments");
        }
    }

    private void executeCashOut() {
        saveDraftTicket();
        if (app != null && app.getAppUserView() != null) {
            app.getAppUserView().showTask("com.openbravo.pos.panels.JPanelPayments");
        }
    }

    private void executeCloseCash() {
        saveDraftTicket();
        if (app != null && app.getAppUserView() != null) {
            app.getAppUserView().showTask("com.openbravo.pos.panels.JPanelCloseMoney");
        }
    }

    public CatalogService getCatalogService() {
        return catalogService;
    }

    @Deprecated
    public DataLogicPIM getDataLogicPIM() {
        return dlPim;
    }
}
