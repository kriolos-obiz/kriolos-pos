//    KriolOS POS
//    Copyright (c) 2019-2026 KriolOS
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

package com.openbravo.pos.sales.modern.one;

import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.MessageInf;
import com.openbravo.data.gui.modal.PosUIModal;
import com.openbravo.pos.customers.CustomerInfoExt;
import com.openbravo.pos.customers.DataLogicCustomers;
import com.openbravo.pos.customers.JCustomerFinderPanel;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.DataLogicSales;
import com.openbravo.pos.forms.DataLogicSystem;
import com.openbravo.pos.forms.JPanelView;
import com.openbravo.pos.payment.JPaymentSelect;
import com.openbravo.pos.payment.JPaymentSelectReceipt;
import com.openbravo.pos.payment.PaymentService;
import com.openbravo.pos.payment.PaymentServiceImpl;
import com.openbravo.pos.pim.CategoryInfo;
import com.openbravo.pos.pim.DataLogicPIM;
import com.openbravo.pos.printer.TicketParser;
import com.openbravo.pos.sales.DataLogicReceipts;
import com.openbravo.pos.sales.JProductLineEditPanel;
import com.openbravo.pos.sales.SalesService;
import com.openbravo.pos.sales.SalesServiceImpl;
import com.openbravo.pos.sales.TaxesLogic;
import com.openbravo.pos.sales.TicketsEditor;
import com.openbravo.pos.ticket.ProductInfoExt;
import com.openbravo.pos.ticket.TaxInfo;
import com.openbravo.pos.ticket.TicketInfo;
import com.openbravo.pos.ticket.UserInfo;
import com.openbravo.pos.ticket.TicketLineInfo;
import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.UIManager;

import com.openbravo.pos.sales.JTicketsBag;
import com.openbravo.pos.sales.SharedTicketInfo;
import com.openbravo.pos.sales.shared.JTicketsBagSharedPanel;

/**
 * ModernOne: 4th Sales Screen Layout for KriolOS POS.
 * <p>
 * Pristine, modern dual-pane touch interface extending {@link JTicketsBag}
 * to plug seamlessly into the sales layout configuration flow.
 * </p>
 *
 * @author KriolOS Team
 */
public class ModernOne extends JTicketsBag implements JPanelView, TicketsEditor {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(ModernOne.class.getName());

    private final AppView app;
    private DataLogicSales dlSales;
    private DataLogicPIM dlPim;
    private DataLogicSystem dlSystem;
    private DataLogicReceipts dlReceipts;
    private DataLogicCustomers dlCustomers;

    private TaxesLogic taxeslogic;
    private SalesService salesService;
    private PaymentService paymentService;
    private TicketParser ticketParser;
    private JPaymentSelect paymentDialog;

    // Dual-pane UI components
    private ModernTicketPane ticketPane;
    private ModernCatalogPane catalogPane;
    private JSplitPane splitPane;

    // Active state
    private TicketInfo activeTicket;
    private String activeTicketExt;
    private CustomerInfoExt activeCustomer;

    public ModernOne(AppView app, TicketsEditor panelticket) {
        super(app, panelticket);
        this.app = app;
        setName("kriolos:sales:modern:panel");
        initDomainServices();
        initUI();
    }

    public ModernOne(AppView app) {
        this(app, null);
    }

    @Override
    public void deleteTicket() {
        clearCurrentTicket();
    }

    @Override
    protected JComponent getBagComponent() {
        JPanel empty = new JPanel();
        empty.setOpaque(false);
        empty.setPreferredSize(new java.awt.Dimension(0, 0));
        return empty;
    }

    @Override
    protected JComponent getNullComponent() {
        return this;
    }

    private void initDomainServices() {
        dlSales = (DataLogicSales) app.getBean("com.openbravo.pos.forms.DataLogicSales");
        dlPim = (DataLogicPIM) app.getBean("com.openbravo.pos.pim.DataLogicPIM");
        dlSystem = (DataLogicSystem) app.getBean("com.openbravo.pos.forms.DataLogicSystem");
        dlReceipts = (DataLogicReceipts) app.getBean("com.openbravo.pos.sales.DataLogicReceipts");
        dlCustomers = (DataLogicCustomers) app.getBean("com.openbravo.pos.customers.DataLogicCustomers");

        paymentService = new PaymentServiceImpl();
        ticketParser = new TicketParser(app.getDeviceTicket(), dlSystem);
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setOpaque(true);
        setBackground(UIManager.getColor("Panel.background"));

        // Left / Leading Pane: Ticket Line Items
        ticketPane = new ModernTicketPane();

        // Right / Center Pane: Responsive Catalog Grid
        catalogPane = new ModernCatalogPane(this::addProductToTicket);

        // Connect Ticket Pane Callbacks
        ticketPane.setOnPayClicked(this::processPayment);
        ticketPane.setOnCustomerClicked(this::selectCustomer);
        ticketPane.setOnClearTicketClicked(this::clearCurrentTicket);
        ticketPane.setOnHoldClicked(this::parkCurrentTicket);
        ticketPane.setOnParkedListClicked(this::showParkedTickets);
        ticketPane.setOnQtyAdjusted(this::adjustSelectedLineQuantity);
        ticketPane.setOnDeleteLineClicked(this::deleteSelectedLine);
        ticketPane.setOnEditLineClicked(this::editSelectedLine);

        // Catalog on Left (65% width) and Ticket on Right (35% width, ~420px)
        catalogPane.setMinimumSize(new java.awt.Dimension(480, 0));
        ticketPane.setMinimumSize(new java.awt.Dimension(360, 0));
        ticketPane.setPreferredSize(new java.awt.Dimension(420, 0));

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, catalogPane, ticketPane);
        splitPane.setContinuousLayout(true);
        splitPane.setDividerSize(6);
        splitPane.setBorder(null);
        splitPane.setOpaque(false);
        splitPane.setResizeWeight(1.0); // Catalog on left expands, Ticket on right stays ~420px

        add(splitPane, BorderLayout.CENTER);

        // Keep Ticket pane at ~420px while Catalog absorbs all remaining window width
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                int totalWidth = getWidth();
                if (totalWidth > 700) {
                    splitPane.setDividerLocation(totalWidth - 420);
                } else if (totalWidth > 0) {
                    splitPane.setDividerLocation((int) (totalWidth * 0.65));
                }
            }
        });

        javax.swing.SwingUtilities.invokeLater(() -> {
            int totalWidth = getWidth();
            if (totalWidth > 700) {
                splitPane.setDividerLocation(totalWidth - 420);
            } else {
                splitPane.setDividerLocation(0.65);
            }
        });

        // Enforce RTL/LTR support on the panes themselves
        ComponentOrientation orientation = ComponentOrientation.getOrientation(Locale.getDefault());
        applyComponentOrientation(orientation);
        ticketPane.applyComponentOrientation(orientation);
        catalogPane.applyComponentOrientation(orientation);
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    @Override
    public String getTitle() {
        return AppLocal.getIntString("Menu.Ticket");
    }

    @Override
    public void activate() {
        LOGGER.log(Level.INFO, "Activating ModernOne sales layout");

        // 1. Initialize Taxes and Sales Services
        List<TaxInfo> taxlist = dlSales.getTaxListAll();
        taxeslogic = new TaxesLogic(taxlist);
        salesService = new SalesServiceImpl(taxeslogic);

        paymentDialog = JPaymentSelectReceipt.getDialog(this);
        paymentDialog.init(app, paymentService);

        // 2. Load Product Categories and Products
        loadCatalogData();

        // 3. Initialize or restore Active Ticket
        if (activeTicket == null) {
            createNewTicket();
        } else {
            refreshTicket();
        }

        updateParkedCount();
        catalogPane.focusSearch();
    }

    @Override
    public boolean deactivate() {
        LOGGER.log(Level.INFO, "Deactivating ModernOne sales layout");
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

    private void createNewTicket() {
        activeTicket = new TicketInfo();
        activeTicket.setUser(app.getAppUserView().getUser().getUserInfo());
        activeTicket.setActiveCash(app.getActiveCashIndex());
        activeTicket.setDate(new Date());
        activeTicketExt = null;
        activeCustomer = null;
        refreshTicket();
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

    private void refreshTicket() {
        if (activeTicket != null && taxeslogic != null) {
            // Recalculate taxes across all lines
            for (TicketLineInfo line : activeTicket.getLines()) {
                TaxInfo tax = taxeslogic.getTaxInfo(line.getProductTaxCategoryID(), activeCustomer);
                line.setTaxInfo(tax);
            }
        }
        ticketPane.updateTicketDisplay(activeTicket, activeCustomer);
    }

    private void loadCatalogData() {
        try {
            List<CategoryInfo> categories = dlPim.getRootCategories();
            catalogPane.setCategories(categories);

            List<ProductInfoExt> allProducts = new java.util.ArrayList<>();
            for (CategoryInfo cat : categories) {
                List<ProductInfoExt> prods = dlPim.getProductCatalog(cat.getID());
                if (prods != null) {
                    allProducts.addAll(prods);
                }
            }
            catalogPane.setProducts(allProducts);
        } catch (BasicException e) {
            LOGGER.log(Level.WARNING, "Error loading catalog data for ModernOne", e);
        }
    }

    public void addProductToTicket(ProductInfoExt product) {
        if (product == null)
            return;
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
        ticketPane.setSelectedLineIndex(activeTicket.getLinesCount() - 1);
    }

    private void adjustSelectedLineQuantity(double delta) {
        if (activeTicket == null)
            return;
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
                ticketPane.setSelectedLineIndex(idx);
            }
        }
    }

    private void deleteSelectedLine() {
        if (activeTicket == null)
            return;
        int idx = ticketPane.getSelectedLineIndex();
        if (idx >= 0 && idx < activeTicket.getLinesCount()) {
            activeTicket.removeLine(idx);
            refreshTicket();
            int nextIdx = Math.min(idx, activeTicket.getLinesCount() - 1);
            ticketPane.setSelectedLineIndex(nextIdx);
        }
    }

    private void editSelectedLine() {
        if (activeTicket == null)
            return;
        int idx = ticketPane.getSelectedLineIndex();
        if (idx >= 0 && idx < activeTicket.getLinesCount()) {
            TicketLineInfo line = activeTicket.getLine(idx);
            try {
                TicketLineInfo edited = JProductLineEditPanel.showMessage(this, app, line);
                if (edited != null) {
                    activeTicket.setLine(idx, edited);
                    refreshTicket();
                    ticketPane.setSelectedLineIndex(idx);
                }
            } catch (BasicException e) {
                LOGGER.log(Level.WARNING, "Error editing line", e);
            }
        }
    }

    private void selectCustomer() {
        JCustomerFinderPanel customerFinder = new JCustomerFinderPanel(dlCustomers);
        PosUIModal modal = PosUIModal.create(this, customerFinder)
                .setTitle(AppLocal.getIntString("title.customer"))
                .setModal(true)
                .setResizable(true);
        customerFinder.setModalContext(modal);
        modal.show();

        com.openbravo.pos.customers.CustomerInfo selected = customerFinder.getSelectedCustomer();
        if (selected != null) {
            try {
                activeCustomer = dlCustomers.findCustomerInfoExtById(selected.getId());
                if (activeTicket != null) {
                    activeTicket.setCustomer(activeCustomer);
                }
                refreshTicket();
            } catch (BasicException e) {
                LOGGER.log(Level.WARNING, "Error finding customer details", e);
            }
        }
    }

    private void processPayment() {
        if (activeTicket == null || activeTicket.getLinesCount() == 0) {
            return;
        }

        if (salesService != null) {
            try {
                salesService.calculateTaxes(activeTicket);
            } catch (com.openbravo.pos.sales.TaxesException e) {
                LOGGER.log(Level.WARNING, "Error calculating taxes during payment", e);
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
                // Persist ticket into database
                dlSales.saveTicket(activeTicket, app.getInventoryLocation());

                // Print receipt
                printReceipt(activeTicket);

                // Clear for next transaction
                createNewTicket();
                updateParkedCount();
            } catch (BasicException ex) {
                LOGGER.log(Level.SEVERE, "Failed to save ticket", ex);
                JOptionPane.showMessageDialog(
                        this,
                        AppLocal.getIntString("message.cannotsaveticket"),
                        AppLocal.getIntString("title.editor"),
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void printReceipt(TicketInfo ticket) {
        if (ticket == null || ticketParser == null)
            return;
        try {
            String template = dlSystem.getResourceAsXML("Printer.Ticket");
            if (template != null && !template.isBlank()) {
                ticketParser.printTicket(template, ticket);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Receipt printing failed or printer unavailable", e);
        }
    }

    private void ensureTicketUser(TicketInfo ticket) {
        if (ticket != null && ticket.getUser() == null) {
            if (app != null && app.getAppUserView() != null && app.getAppUserView().getUser() != null) {
                ticket.setUser(app.getAppUserView().getUser().getUserInfo());
            }
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
                // If current ticket has items, save it first before switching
                if (activeTicket != null && activeTicket.getLinesCount() > 0) {
                    saveOrUpdateSharedTicket(activeTicket);
                }
                // Load recalled ticket
                TicketInfo recalled = dlReceipts.getSharedTicket(selectedId);
                dlReceipts.deleteSharedTicket(selectedId);
                if (recalled != null) {
                    setActiveTicket(recalled, null);
                }
                updateParkedCount();
            }
        } catch (BasicException e) {
            LOGGER.log(Level.WARNING, "Failed to recall parked ticket", e);
        }
    }

    private void updateParkedCount() {
        if (dlReceipts == null) return;
        try {
            List<SharedTicketInfo> list = dlReceipts.getSharedTicketList();
            int count = (list != null) ? list.size() : 0;
            ticketPane.setParkedCount(count);
        } catch (BasicException e) {
            LOGGER.log(Level.WARNING, "Failed to query parked tickets count", e);
        }
    }
}
