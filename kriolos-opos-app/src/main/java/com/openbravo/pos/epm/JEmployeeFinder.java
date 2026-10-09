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

package com.openbravo.pos.epm;

import com.openbravo.data.gui.modal.PosUIModal;
import com.openbravo.pos.forms.AppLocal;
import java.awt.Component;

/**
 * Backward-compatible adapter delegating to {@link JEmployeeFinderPanel}.
 *
 * @deprecated Use {@link JEmployeeFinderPanel#show(Component, DataLogicPresenceManagement)} instead.
 */
@Deprecated
public class JEmployeeFinder {

    private final Component parent;
    private final JEmployeeFinderPanel panel;

    private JEmployeeFinder(Component parent, ShiftService shiftService) {
        this.parent = parent;
        this.panel = new JEmployeeFinderPanel(shiftService);
    }

    public static JEmployeeFinder getEmployeeFinder(Component parent, ShiftService shiftService) {
        return new JEmployeeFinder(parent, shiftService);
    }

    @Deprecated
    public static JEmployeeFinder getEmployeeFinder(Component parent, DataLogicPresenceManagement dlPresenceManagement) {
        return new JEmployeeFinder(parent, (ShiftService) dlPresenceManagement);
    }

    public static EmployeeInfo show(Component parent, ShiftService shiftService) {
        return JEmployeeFinderPanel.show(parent, shiftService);
    }

    public static EmployeeInfo show(Component parent, ShiftService shiftService, EmployeeInfo employee) {
        return JEmployeeFinderPanel.show(parent, shiftService, employee);
    }

    @Deprecated
    public static EmployeeInfo show(Component parent, DataLogicPresenceManagement dlPresenceManagement) {
        return JEmployeeFinderPanel.show(parent, (ShiftService) dlPresenceManagement);
    }

    @Deprecated
    public static EmployeeInfo show(Component parent, DataLogicPresenceManagement dlPresenceManagement, EmployeeInfo employee) {
        return JEmployeeFinderPanel.show(parent, (ShiftService) dlPresenceManagement, employee);
    }

    public void search(EmployeeInfo employee) {
        panel.search(employee);
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

    public EmployeeInfo getSelectedEmployee() {
        return panel.getSelectedEmployee();
    }
}
