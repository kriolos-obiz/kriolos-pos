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
import com.openbravo.data.gui.JMessagePanel;
import com.openbravo.data.gui.MessageInf;
import com.openbravo.data.gui.modal.PosUIModal;
import com.openbravo.pos.customers.CustomerInfo;
import com.openbravo.pos.customers.CustomerInfoExt;
import com.openbravo.pos.customers.DataLogicCustomers;
import com.openbravo.pos.customers.JCustomerFinder;
import com.openbravo.pos.customers.JDialogNewCustomer;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.ticket.TicketInfo;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.Optional;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Controller managing customer selection, new customer registration,
 * and VIP discount checking for sales tickets.
 */
public class SalesCustomerController {

    private static final Logger LOGGER = System.getLogger(SalesCustomerController.class.getName());

    private final AppView app;
    private final DataLogicCustomers dlCustomers;

    public SalesCustomerController(AppView app, DataLogicCustomers dlCustomers) {
        this.app = app;
        this.dlCustomers = dlCustomers;
    }

    /**
     * Prompts the user to create a new customer, search for an existing customer,
     * or clear/update the customer associated with the ticket.
     *
     * @param parent UI parent component
     * @param currentTicket The active ticket
     * @return Optional containing the selected/created CustomerInfoExt, or Optional.empty() if no customer or cleared
     */
    public Optional<CustomerInfoExt> selectCustomer(Component parent, TicketInfo currentTicket) {
        if (currentTicket == null) {
            return Optional.empty();
        }

        final int[] choice = new int[]{-1};
        JPanel optPanel = new JPanel(new BorderLayout(15, 15));
        optPanel.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JLabel lbl = new JLabel(AppLocal.getIntString("message.customeradd"));
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 14f));
        optPanel.add(lbl, BorderLayout.NORTH);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JButton btnCreate = new JButton(AppLocal.getIntString("cboption.create"));
        JButton btnFind = new JButton(AppLocal.getIntString("cboption.find"));
        JButton btnCancel = new JButton(AppLocal.getIntString("label.cancel"));

        btnPanel.add(btnCreate);
        btnPanel.add(btnFind);
        btnPanel.add(btnCancel);
        optPanel.add(btnPanel, BorderLayout.SOUTH);

        PosUIModal modal = PosUIModal.create(parent, optPanel)
                .setTitle(AppLocal.getIntString("label.customer"))
                .setModal(true)
                .setResizable(false);

        btnCreate.addActionListener(e -> { choice[0] = 0; modal.close(); });
        btnFind.addActionListener(e -> { choice[0] = 1; modal.close(); });
        btnCancel.addActionListener(e -> { choice[0] = 2; modal.close(); });

        modal.show();
        int n = choice[0];

        if (n == 0) {
            return createNewCustomer(parent);
        } else if (n == 1) {
            return findCustomer(parent, currentTicket);
        }

        return Optional.ofNullable(currentTicket.getCustomer());
    }

    private Optional<CustomerInfoExt> createNewCustomer(Component parent) {
        JDialogNewCustomer dialog = JDialogNewCustomer.getDialog(parent, app);
        dialog.setVisible(true);

        CustomerInfoExt customerInfo = dialog.getSelectedCustomer();
        return Optional.ofNullable(customerInfo);
    }

    private Optional<CustomerInfoExt> findCustomer(Component parent, TicketInfo currentTicket) {
        JCustomerFinder finder = JCustomerFinder.getCustomerFinder(parent, dlCustomers);

        if (currentTicket.getCustomerId() == null) {
            finder.setAppView(app);
            finder.search(currentTicket.getCustomer());
            finder.executeSearch();
            finder.setVisible(true);

            CustomerInfo customerInfo = finder.getSelectedCustomer();
            if (customerInfo != null) {
                try {
                    CustomerInfoExt customerExt = dlCustomers.findCustomerInfoExtById(customerInfo.getId());
                    return Optional.ofNullable(customerExt);
                } catch (BasicException ex) {
                    LOGGER.log(Level.WARNING, "Exception on Select Customer: ", ex);
                    new MessageInf(MessageInf.SGN_WARNING,
                            AppLocal.getIntString("message.cannotfindcustomer"), ex).show(parent);
                }
            }
            return Optional.empty();
        } else {
            int confirm = JMessagePanel.showConfirmDialog(parent,
                    new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.customerchange")));
            if (confirm == 0) {
                finder.setAppView(app);
                finder.search(currentTicket.getCustomer());
                finder.executeSearch();
                finder.setVisible(true);

                if (finder.getSelectedCustomer() != null) {
                    try {
                        CustomerInfoExt customerExt = dlCustomers.findCustomerInfoExtById(finder.getSelectedCustomer().getId());
                        return Optional.ofNullable(customerExt);
                    } catch (BasicException ex) {
                        LOGGER.log(Level.WARNING, "Exception on change customer: ", ex);
                        new MessageInf(MessageInf.SGN_WARNING,
                                AppLocal.getIntString("message.cannotfindcustomer"), ex).show(parent);
                    }
                }
                return Optional.empty();
            }
            return Optional.ofNullable(currentTicket.getCustomer());
        }
    }

    /**
     * Checks if customer has VIP status and displays the discount details modal.
     */
    public void checkAndShowCustomerDiscount(Component parent, TicketInfo ticket) {
        if (ticket != null && ticket.getCustomer() != null && ticket.getCustomer().isVIP()) {
            CustomerDiscountInfoPanel.show(parent, ticket.getCustomer());
        }
    }
}
