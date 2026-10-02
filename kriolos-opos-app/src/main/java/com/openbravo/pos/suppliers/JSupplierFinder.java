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

package com.openbravo.pos.suppliers;

import com.openbravo.data.gui.modal.PosUIModal;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppView;
import java.awt.Component;

/**
 * Backward-compatible adapter delegating to {@link JSupplierFinderPanel}.
 *
 * @deprecated Use {@link JSupplierFinderPanel#show(Component, DataLogicSuppliers)} instead.
 */
@Deprecated
public class JSupplierFinder {

    private final Component parent;
    private final JSupplierFinderPanel panel;

    private JSupplierFinder(Component parent, DataLogicSuppliers dlSuppliers) {
        this.parent = parent;
        this.panel = new JSupplierFinderPanel(dlSuppliers);
    }

    public static JSupplierFinder getSupplierFinder(Component parent, DataLogicSuppliers dlSuppliers) {
        return new JSupplierFinder(parent, dlSuppliers);
    }

    public static SupplierInfo show(Component parent, DataLogicSuppliers dlSuppliers) {
        return JSupplierFinderPanel.show(parent, dlSuppliers);
    }

    public static SupplierInfo show(Component parent, DataLogicSuppliers dlSuppliers, SupplierInfo supplier) {
        return JSupplierFinderPanel.show(parent, dlSuppliers, supplier);
    }

    public void setAppView(AppView appView) {
        panel.setAppView(appView);
    }

    public void search(SupplierInfo supplier) {
        panel.search(supplier);
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

    public SupplierInfo getSelectedSupplier() {
        return panel.getSelectedSupplier();
    }
}
