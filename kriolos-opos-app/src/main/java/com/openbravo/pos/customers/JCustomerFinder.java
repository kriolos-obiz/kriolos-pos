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

package com.openbravo.pos.customers;

import com.openbravo.data.gui.modal.PosUIModal;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppView;
import java.awt.Component;

/**
 * Backward-compatible adapter delegating to {@link JCustomerFinderPanel}.
 *
 * @deprecated Use {@link JCustomerFinderPanel#show(Component, DataLogicCustomers)} instead.
 */
@Deprecated
public class JCustomerFinder {

    private final Component parent;
    private final JCustomerFinderPanel panel;

    private JCustomerFinder(Component parent, DataLogicCustomers dlCustomers) {
        this.parent = parent;
        this.panel = new JCustomerFinderPanel(dlCustomers);
    }

    public static JCustomerFinder getCustomerFinder(Component parent, DataLogicCustomers dlCustomers) {
        return new JCustomerFinder(parent, dlCustomers);
    }

    public static CustomerInfo show(Component parent, DataLogicCustomers dlCustomers) {
        return JCustomerFinderPanel.show(parent, dlCustomers);
    }

    public static CustomerInfo show(Component parent, DataLogicCustomers dlCustomers, CustomerInfo customer) {
        return JCustomerFinderPanel.show(parent, dlCustomers, customer);
    }

    public void setAppView(AppView appView) {
        panel.setAppView(appView);
    }

    public void search(CustomerInfo customer) {
        panel.search(customer);
    }

    public void searchKey() {
        panel.searchKey();
    }

    public void resetKey() {
        panel.resetKey();
    }

    public void executeSearch() {
        panel.executeSearch();
    }

    public void setVisible(boolean b) {
        if (b) {
            PosUIModal modal = PosUIModal.create(parent, panel)
                    .setTitle(AppLocal.getIntString("form.customertitle"))
                    .setModal(true)
                    .setResizable(true);
            panel.setModalContext(modal);
            modal.show();
        }
    }

    public CustomerInfo getSelectedCustomer() {
        return panel.getSelectedCustomer();
    }
}
