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

package com.openbravo.pos.panels;

import com.openbravo.data.gui.modal.PosUIModal;
import com.openbravo.pos.customers.CustomerService;
import com.openbravo.pos.customers.DataLogicCustomers;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.DataLogicSales;
import com.openbravo.pos.sales.TaxService;
import com.openbravo.pos.sales.TicketLifecycleService;
import com.openbravo.pos.ticket.FindTicketsInfo;
import java.awt.Component;

/**
 * Backward-compatible adapter delegating to {@link JTicketsFinderPanel}.
 *
 * @deprecated Use {@link JTicketsFinderPanel#show(Component, TicketLifecycleService, TaxService, CustomerService)} instead.
 */
@Deprecated
public class JTicketsFinder {

    private final Component parent;
    private final JTicketsFinderPanel panel;

    public JTicketsFinder(Component parent, TicketLifecycleService ticketLifecycleService, TaxService taxService, CustomerService customerService) {
        this.parent = parent;
        this.panel = new JTicketsFinderPanel(ticketLifecycleService, taxService, customerService);
    }

    @Deprecated
    public JTicketsFinder(Component parent, TicketLifecycleService ticketLifecycleService, TaxService taxService, DataLogicCustomers dlCustomers) {
        this(parent, ticketLifecycleService, taxService, (CustomerService) dlCustomers);
    }

    @Deprecated
    private JTicketsFinder(Component parent, DataLogicSales dlSales, DataLogicCustomers dlCustomers) {
        this(parent, (TicketLifecycleService) dlSales, (TaxService) dlSales, (CustomerService) dlCustomers);
    }

    public static JTicketsFinder getReceiptFinder(Component parent, TicketLifecycleService ticketLifecycleService, TaxService taxService, CustomerService customerService) {
        return new JTicketsFinder(parent, ticketLifecycleService, taxService, customerService);
    }

    @Deprecated
    public static JTicketsFinder getReceiptFinder(Component parent, TicketLifecycleService ticketLifecycleService, TaxService taxService, DataLogicCustomers dlCustomers) {
        return new JTicketsFinder(parent, ticketLifecycleService, taxService, (CustomerService) dlCustomers);
    }

    @Deprecated
    public static JTicketsFinder getReceiptFinder(Component parent, DataLogicSales dlSales, DataLogicCustomers dlCustomers) {
        return new JTicketsFinder(parent, (TicketLifecycleService) dlSales, (TaxService) dlSales, (CustomerService) dlCustomers);
    }

    public static FindTicketsInfo show(Component parent, TicketLifecycleService ticketLifecycleService, TaxService taxService, CustomerService customerService) {
        return JTicketsFinderPanel.show(parent, ticketLifecycleService, taxService, customerService);
    }

    @Deprecated
    public static FindTicketsInfo show(Component parent, TicketLifecycleService ticketLifecycleService, TaxService taxService, DataLogicCustomers dlCustomers) {
        return JTicketsFinderPanel.show(parent, ticketLifecycleService, taxService, (CustomerService) dlCustomers);
    }

    @Deprecated
    public static FindTicketsInfo show(Component parent, DataLogicSales dlSales, DataLogicCustomers dlCustomers) {
        return JTicketsFinderPanel.show(parent, (TicketLifecycleService) dlSales, (TaxService) dlSales, (CustomerService) dlCustomers);
    }

    public FindTicketsInfo getSelectedCustomer() {
        return panel.getSelectedCustomer();
    }

    public FindTicketsInfo getSelectedTicket() {
        return panel.getSelectedTicket();
    }

    public void executeSearch() {
        panel.executeSearch();
    }

    public void setVisible(boolean b) {
        if (b) {
            PosUIModal modal = PosUIModal.create(parent, panel)
                    .setTitle(AppLocal.getIntString("form.tickettitle"))
                    .setModal(true)
                    .setResizable(true);
            panel.setModalContext(modal);
            modal.show();
        }
    }
}
