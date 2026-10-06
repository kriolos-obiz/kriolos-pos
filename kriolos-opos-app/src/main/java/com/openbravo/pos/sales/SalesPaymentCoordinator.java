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
import com.openbravo.data.gui.MessageInf;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.DataLogicSales;
import com.openbravo.pos.payment.JPaymentSelect;
import com.openbravo.pos.ticket.TicketInfo;
import java.awt.Component;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.Date;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 * Coordinates ticket payment completion, event execution, persistence,
 * and post-settlement receipt printing.
 */
public class SalesPaymentCoordinator {

    private static final Logger LOGGER = System.getLogger(SalesPaymentCoordinator.class.getName());

    private final AppView app;
    private final DataLogicSales dlSales;
    private final SalesService salesService;

    public SalesPaymentCoordinator(AppView app, DataLogicSales dlSales, SalesService salesService) {
        this.app = Objects.requireNonNull(app, "AppView cannot be null");
        this.dlSales = Objects.requireNonNull(dlSales, "DataLogicSales cannot be null");
        this.salesService = Objects.requireNonNull(salesService, "SalesService cannot be null");
    }

    /**
     * Resolves the proper payment dialog based on ticket receipt type.
     */
    public JPaymentSelect resolvePaymentDialog(TicketInfo ticket, JPaymentSelect receiptDialog, JPaymentSelect refundDialog) {
        if (ticket == null) {
            return null;
        }
        if (ticket.getTicketType() == TicketInfo.RECEIPT_NORMAL) {
            return receiptDialog;
        } else if (ticket.getTicketType() == TicketInfo.RECEIPT_REFUND) {
            return refundDialog;
        }
        return null;
    }

    /**
     * Executes the complete ticket payment and settlement lifecycle.
     *
     * @param parent Component for modal dialogs and alerts
     * @param ticket The ticket being finalized
     * @param ticketExt The place / table or extension ID
     * @param paymentDialog The resolved payment dialog
     * @param printSelectedConfig Value of printselected configuration
     * @param eventExecutor BiFunction to trigger script events (eventKey, args -> scriptResult)
     * @param ticketPrinter BiConsumer to print ticket templates (scriptName, printSelected)
     * @param onSettlementSuccess Action to run on success (e.g. clear restaurant tables)
     * @return true if the ticket was paid, saved, and closed successfully
     */
    public boolean processPaymentAndClose(
            Component parent,
            TicketInfo ticket,
            String ticketExt,
            JPaymentSelect paymentDialog,
            boolean printSelectedConfig,
            BiFunction<String, ScriptArg[], Object> eventExecutor,
            BiConsumer<String, Boolean> ticketPrinter,
            Runnable onSettlementSuccess) {

        if (ticket == null || paymentDialog == null) {
            return false;
        }

        LOGGER.log(Level.INFO, "TicketInfo type (0:Receipt; 1:Refund) is " + ticket.getTicketType());

        try {
            salesService.calculateTaxes(ticket);
            if (ticket.getTotal() >= 0.0) {
                ticket.resetPayments();
            }

            Object totalEventResult = eventExecutor.apply(TicketConstants.EV_TICKET_TOTAL, new ScriptArg[0]);
            if (totalEventResult != null) {
                return false;
            }

            ticketPrinter.accept("Printer.TicketTotal", false);

            paymentDialog.setPrintSelected(printSelectedConfig);
            paymentDialog.setTransactionID(ticket.getTransactionID());

            if (!paymentDialog.showDialog(ticket.getTotal(), ticket.getCustomer())) {
                return false;
            }

            ticket.setPayments(paymentDialog.getSelectedPayments());

            String log = "Ticket payment Ticket total: " + ticket.getTotal()
                    + ";Dialog total: " + paymentDialog.getTotal()
                    + " ;Dialog paid: " + paymentDialog.getPaidTotal()
                    + " ;Payments Selected: " + paymentDialog.getSelectedPayments().size();
            LOGGER.log(Level.INFO, log);

            ticket.setUser(app.getAppUserView().getUser().getUserInfo());
            ticket.setActiveCash(app.getActiveCashIndex());
            ticket.setDate(new Date());

            Object saveEventResult = eventExecutor.apply(TicketConstants.EV_TICKET_SAVE, new ScriptArg[0]);
            if (saveEventResult != null) {
                return false;
            }

            try {
                dlSales.saveTicket(ticket, app.getInventoryLocation());
            } catch (BasicException ex) {
                LOGGER.log(Level.ERROR, "Exception on save ticket ", ex);
                new MessageInf(MessageInf.SGN_NOTICE, AppLocal.getIntString("message.nosaveticket"), ex).show(parent);
                return false;
            }

            String eventName = TicketConstants.EV_TICKET_CLOSE;
            try {
                eventExecutor.apply(eventName, new ScriptArg[]{
                        new ScriptArg("print", paymentDialog.isPrintSelected()),
                        new ScriptArg("ticket", ticket)
                });
            } catch (Exception ex) {
                LOGGER.log(Level.ERROR, "Exception on executeEvent: " + eventName, ex);
            }

            boolean warrantyPrint = hasWarrantyProduct(ticket);
            String scriptName = paymentDialog.isPrintSelected() || warrantyPrint
                    ? "Printer.Ticket"
                    : "Printer.Ticket2";

            ticketPrinter.accept(scriptName, paymentDialog.isPrintSelected());

            if (onSettlementSuccess != null) {
                onSettlementSuccess.run();
            }

            return true;
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Exception on close ticket: ", ex);
            new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotcalculatetaxes"), ex).show(parent);
            return false;
        } finally {
            ticket.resetTaxes();
            ticket.resetPayments();
        }
    }

    /**
     * Checks whether any item in the ticket includes a product warranty.
     */
    public boolean hasWarrantyProduct(TicketInfo ticket) {
        if (ticket == null) {
            return false;
        }
        for (int i = 0; i < ticket.getLinesCount(); i++) {
            if (ticket.getLine(i).isProductWarranty()) {
                return true;
            }
        }
        return false;
    }
}
