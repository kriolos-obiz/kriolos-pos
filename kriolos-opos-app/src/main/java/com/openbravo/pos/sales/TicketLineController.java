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

package com.openbravo.pos.sales;

import com.openbravo.basic.BasicException;
import com.openbravo.beans.JPasswordPanel;
import com.openbravo.data.gui.JMessagePanel;
import com.openbravo.data.gui.MessageInf;
import com.openbravo.data.loader.Session;
import com.openbravo.pos.customers.CustomerService;
import com.openbravo.pos.customers.DataLogicCustomers;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.DataLogicSales;
import com.openbravo.pos.forms.DataLogicSystem;
import com.openbravo.pos.ticket.TicketInfo;
import com.openbravo.pos.ticket.TicketLineInfo;
import java.awt.Component;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Controller encapsulating ticket line modifications, line deletion with audit,
 * multiplier adjustment with override PIN checks, attribute editing, and ticket splitting.
 */
public class TicketLineController {

    private static final System.Logger LOGGER = System.getLogger(TicketLineController.class.getName());

    private final AppView app;
    private final SalesService salesService;
    private final DataLogicSales dlSales;
    private final AuditService auditService;

    /**
     * @deprecated Use {@link #TicketLineController(AppView, SalesService, AuditService)} instead.
     */
    @Deprecated
    public TicketLineController(AppView app, SalesService salesService, DataLogicSales dlSales) {
        this(app, salesService, dlSales, app != null ? safeGetAudit(app) : null);
    }

    public TicketLineController(AppView app, SalesService salesService) {
        this(app, salesService, (AuditService) null);
    }

    public TicketLineController(AppView app, SalesService salesService, AuditService auditService) {
        this(app, salesService, null, auditService);
    }

    public TicketLineController(AppView app, SalesService salesService, DataLogicSales dlSales, AuditService auditService) {
        this.app = app;
        this.salesService = salesService;
        this.dlSales = dlSales;
        this.auditService = auditService;
    }

    @Deprecated
    public TicketLineController(AppView app, SalesService salesService, DataLogicSales dlSales, DataLogicAudit dlAudit) {
        this(app, salesService, dlSales, (AuditService) dlAudit);
    }

    private static AuditService safeGetAudit(AppView app) {
        try {
            return app.getBean(AuditService.class);
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * Delete a line from ticket with permission check and audit recording.
     */
    public boolean deleteLineWithAudit(Component parent, TicketInfo ticket, int lineIndex) {
        if (ticket == null || lineIndex < 0 || lineIndex >= ticket.getLinesCount()) {
            return false;
        }

        LOGGER.log(Logger.Level.INFO, "Delete Ticket Line number: " + lineIndex);

        if (!app.hasPermission("sales.DeleteLines")) {
            new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.deletelineno")).show(parent);
            return false;
        }

        int input = JMessagePanel.showConfirmDialog(parent,
                new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.deletelineyes")));
        if (input != 0) {
            return false;
        }

        return executeLineRemovalAndAudit(ticket, lineIndex);
    }

    private boolean executeLineRemovalAndAudit(TicketInfo ticket, int lineIndex) {
        if (ticket == null || lineIndex < 0 || lineIndex >= ticket.getLinesCount()) {
            return false;
        }

        TicketLineInfo ticketLine = ticket.getLine(lineIndex);
        String ticketID = ticket.getTicketId() == 0 ? "Void" : Integer.toString(ticket.getTicketId());

        LOGGER.log(Logger.Level.INFO, "Delete Ticket Line number: " + lineIndex + "; for TicketId: " + ticketID);

        salesService.removeLine(ticket, lineIndex);

        if (app != null && app.getAppUserView() != null && app.getAppUserView().getUser() != null) {
            try {
                if (auditService != null) {
                    auditService.addTicketLineRemoved(
                            app.getAppUserView().getUser().getName(),
                            ticketID,
                            ticketLine.getProductID(),
                            ticketLine.getProductName(),
                            ticketLine.getMultiply()
                    );
                } else if (dlSales != null) {
                    dlSales.addTicketLineRemoved(
                            app.getAppUserView().getUser().getName(),
                            ticketID,
                            ticketLine.getProductID(),
                            ticketLine.getProductName(),
                            ticketLine.getMultiply()
                    );
                }
            } catch (Exception ex) {
                LOGGER.log(Logger.Level.WARNING, "Exception recording line removal audit: ", ex);
            }
        }

        return true;
    }

    /**
     * Adjusts the quantity (multiplier) of a selected line.
     * Evaluates override PIN authorization, refund receipt orientation, and auto-deletes when zeroed.
     */
    public LineChangeResult changeLineQuantity(Component parent, TicketInfo ticket, int lineIndex, double amount, boolean isAbsolute) {
        if (ticket == null || lineIndex < 0 || lineIndex >= ticket.getLinesCount()) {
            return LineChangeResult.noop();
        }

        if (isOverrideCheckEnabled()) {
            String pin = getAppProperty("override.pin");
            String iValue = JPasswordPanel.show(parent, AppLocal.getIntString("title.override.enterpin"));
            if (iValue == null || !iValue.equals(pin)) {
                new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.override.badpin")).show(parent);
                return LineChangeResult.denied();
            }
        }

        TicketLineInfo line = ticket.getLine(lineIndex);
        TicketLineInfo newline = new TicketLineInfo(line);
        boolean isRefund = ticket.getTicketType() == TicketInfo.RECEIPT_REFUND;

        double targetMultiply;
        if (isAbsolute) {
            targetMultiply = isRefund ? -Math.abs(amount) : Math.abs(amount);
            newline.setPrice(Math.abs(newline.getPrice()));
        } else {
            double step = isRefund ? -amount : amount;
            targetMultiply = newline.getMultiply() + step;
        }

        newline.setMultiply(targetMultiply);
        newline.setProperty(TicketConstants.PROP_TICKET_UPDATED, "true");

        boolean shouldRemove = isRefund ? (newline.getMultiply() >= 0.0) : (newline.getMultiply() <= 0.0);
        if (shouldRemove) {
            deleteLineWithAudit(parent, ticket, lineIndex);
            return LineChangeResult.removed();
        }

        return LineChangeResult.updated(newline);
    }

    /**
     * Open product line editor modal.
     *
     * @return Optional containing the updated line if accepted by the user, or empty if cancelled.
     */
    public Optional<TicketLineInfo> editLine(Component parent, TicketLineInfo line) {
        if (line == null) {
            return Optional.empty();
        }
        try {
            return Optional.ofNullable(JProductLineEditPanel.showMessage(parent, app, line));
        } catch (BasicException e) {
            new MessageInf(e).show(parent);
            return Optional.empty();
        }
    }

    /**
     * Edit line attributes (e.g., size, color, serial number).
     */
    public boolean editLineAttributes(Component parent, Session session, TicketLineInfo line) {
        if (line == null) {
            return false;
        }
        try {
            JProductAttEdit2 attedit = JProductAttEdit2.getAttributesEditor(parent, session);
            if (line.getProductAttSetId() != null) {
                attedit.editAttributes(line.getProductAttSetId(), line.getProductAttSetInstId());
                attedit.setVisible(true);
                if (attedit.isOK()) {
                    line.setProductAttSetInstId(attedit.getAttributeSetInst());
                    line.setProductAttSetInstDesc(attedit.getAttributeSetInstDescription());
                    return true;
                }
            }
        } catch (Exception ex) {
            LOGGER.log(Logger.Level.WARNING, "Exception on edit attributes: ", ex);
            new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotfindattributes"), ex).show(parent);
        }
        return false;
    }

    /**
     * Split ticket lines into a second ticket and close the split ticket.
     *
     * @return Optional containing the updated remaining ticket if split and closed successfully, or empty if cancelled/failed
     */
    public Optional<TicketInfo> splitTicket(Component parent, TicketInfo currentTicket, String ticketExt,
                                            DataLogicSystem dlSystem, CustomerService customerService,
                                            TaxesLogic taxesLogic, Predicate<TicketInfo> ticketCloser) {
        if (currentTicket == null || currentTicket.getLinesCount() <= 0) {
            return Optional.empty();
        }

        ReceiptSplit splitdialog = ReceiptSplit.getDialog(parent,
                dlSystem.getResourceAsXML(TicketConstants.RES_TICKET_LINES), customerService, taxesLogic);

        TicketInfo ticket1 = currentTicket.copyTicket();
        TicketInfo ticket2 = new TicketInfo();
        ticket2.setCustomer(currentTicket.getCustomer());

        if (splitdialog.showDialog(ticket1, ticket2, ticketExt)) {
            if (ticketCloser.test(ticket2)) {
                return Optional.of(ticket1);
            }
        }
        return Optional.empty();
    }

    @Deprecated
    public Optional<TicketInfo> splitTicket(Component parent, TicketInfo currentTicket, String ticketExt,
                                            DataLogicSystem dlSystem, DataLogicCustomers dlCustomers,
                                            TaxesLogic taxesLogic, Predicate<TicketInfo> ticketCloser) {
        return splitTicket(parent, currentTicket, ticketExt, dlSystem, (CustomerService) dlCustomers, taxesLogic, ticketCloser);
    }

    /**
     * Calculates the insertion index for an auxiliary (composition) product line.
     * Auxiliary lines are grouped immediately after their parent product and existing auxiliary items.
     *
     * @param ticket the active ticket
     * @param selectedIndex the currently selected line index in the ticket
     * @return the target 0-based insertion index, or -1 if no line is selected or out of bounds
     */
    public int calculateAuxiliaryInsertIndex(TicketInfo ticket, int selectedIndex) {
        if (ticket == null || selectedIndex < 0 || selectedIndex >= ticket.getLinesCount()) {
            return -1;
        }

        int i = selectedIndex;
        if (!ticket.getLine(i).isProductCom()) {
            i++;
        }

        while (i >= 0 && i < ticket.getLinesCount() && ticket.getLine(i).isProductCom()) {
            i++;
        }

        return i;
    }

    public boolean isOverrideCheckEnabled() {
        return "true".equals(getAppProperty("override.check"));
    }

    private String getAppProperty(String key) {
        if (app != null && app.getProperties() != null) {
            String val = app.getProperties().getProperty(key);
            return val != null ? val : "";
        }
        return "";
    }
}
