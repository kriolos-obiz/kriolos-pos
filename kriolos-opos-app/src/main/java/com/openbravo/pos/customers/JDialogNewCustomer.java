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

import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.modal.PosUIModal;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppView;
import java.awt.Component;

/**
 * Backward-compatible adapter delegating to {@link JNewCustomerPanel}.
 *
 * @deprecated Use {@link JNewCustomerPanel#show(Component, AppView)} instead.
 */
@Deprecated
public class JDialogNewCustomer {

    private final Component parent;
    private final JNewCustomerPanel panel;

    private JDialogNewCustomer(Component parent, AppView app) {
        this.parent = parent;
        this.panel = new JNewCustomerPanel(app);
    }

    public static JDialogNewCustomer getDialog(Component parent, AppView app) {
        return new JDialogNewCustomer(parent, app);
    }

    public static CustomerInfoExt show(Component parent, AppView app) {
        return JNewCustomerPanel.show(parent, app);
    }

    public Object createValue() throws BasicException {
        return panel.createValue();
    }

    public CustomerInfoExt getSelectedCustomer() {
        return panel.getSelectedCustomer();
    }

    public void setVisible(boolean b) {
        if (b) {
            PosUIModal modal = PosUIModal.create(parent, panel)
                    .setTitle(AppLocal.getIntString("label.customer"))
                    .setModal(true)
                    .setResizable(false);
            panel.setModalContext(modal);
            modal.show();
        }
    }
}
