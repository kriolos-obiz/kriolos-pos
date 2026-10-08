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

import com.openbravo.data.gui.modal.PosUIModal;
import com.openbravo.pos.customers.CustomerService;
import com.openbravo.pos.customers.DataLogicCustomers;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.DataLogicSales;
import com.openbravo.pos.ticket.TicketInfo;
import java.awt.Component;

/**
 * Backward-compatible adapter delegating to {@link ReceiptSplitPanel}.
 *
 * @deprecated Use {@link ReceiptSplitPanel#show(Component, String, CustomerService, TaxesLogic, TicketInfo, TicketInfo, String)} instead.
 */
@Deprecated
public class ReceiptSplit {

    private final Component parent;
    private final ReceiptSplitPanel panel;

    public ReceiptSplit(Component parent, String ticketline, CustomerService customerService, TaxesLogic taxeslogic) {
        this.parent = parent;
        this.panel = new ReceiptSplitPanel(ticketline, customerService, taxeslogic);
    }

    @Deprecated
    public ReceiptSplit(Component parent, String ticketline, DataLogicCustomers dlCustomers, TaxesLogic taxeslogic) {
        this(parent, ticketline, (CustomerService) dlCustomers, taxeslogic);
    }

    @Deprecated
    public ReceiptSplit(Component parent, String ticketline, DataLogicSales dlSales, DataLogicCustomers dlCustomers, TaxesLogic taxeslogic) {
        this(parent, ticketline, (CustomerService) dlCustomers, taxeslogic);
    }

    public static ReceiptSplit getDialog(Component parent, String ticketline, CustomerService customerService, TaxesLogic taxeslogic) {
        return new ReceiptSplit(parent, ticketline, customerService, taxeslogic);
    }

    @Deprecated
    public static ReceiptSplit getDialog(Component parent, String ticketline, DataLogicCustomers dlCustomers, TaxesLogic taxeslogic) {
        return new ReceiptSplit(parent, ticketline, (CustomerService) dlCustomers, taxeslogic);
    }

    @Deprecated
    public static ReceiptSplit getDialog(Component parent, String ticketline, DataLogicSales dlSales, DataLogicCustomers dlCustomers, TaxesLogic taxeslogic) {
        return getDialog(parent, ticketline, (CustomerService) dlCustomers, taxeslogic);
    }

    public static boolean show(Component parent, String ticketline, CustomerService customerService, TaxesLogic taxeslogic, TicketInfo ticket, TicketInfo ticket2, String ticketext) {
        return ReceiptSplitPanel.show(parent, ticketline, customerService, taxeslogic, ticket, ticket2, ticketext);
    }

    @Deprecated
    public static boolean show(Component parent, String ticketline, DataLogicCustomers dlCustomers, TaxesLogic taxeslogic, TicketInfo ticket, TicketInfo ticket2, String ticketext) {
        return ReceiptSplitPanel.show(parent, ticketline, (CustomerService) dlCustomers, taxeslogic, ticket, ticket2, ticketext);
    }

    @Deprecated
    public static boolean show(Component parent, String ticketline, DataLogicSales dlSales, DataLogicCustomers dlCustomers, TaxesLogic taxeslogic, TicketInfo ticket, TicketInfo ticket2, String ticketext) {
        return show(parent, ticketline, (CustomerService) dlCustomers, taxeslogic, ticket, ticket2, ticketext);
    }

    public boolean showDialog(TicketInfo ticket, TicketInfo ticket2, String ticketext) {
        panel.setTickets(ticket, ticket2, ticketext);
        PosUIModal modal = PosUIModal.create(parent, panel)
                .setTitle(AppLocal.getIntString("caption.split"))
                .setModal(true)
                .setResizable(false);
        panel.setModalContext(modal);
        modal.show();
        return panel.isAccepted();
    }
}
