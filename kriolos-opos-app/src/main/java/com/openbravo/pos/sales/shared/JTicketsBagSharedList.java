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

package com.openbravo.pos.sales.shared;

import com.openbravo.pos.sales.DataLogicReceipts;
import com.openbravo.pos.sales.SharedTicketInfo;
import java.awt.Component;
import java.util.List;

/**
 * Backward-compatible adapter delegating to {@link JTicketsBagSharedPanel}.
 *
 * @deprecated Use {@link JTicketsBagSharedPanel#show(Component, List, DataLogicReceipts)} instead.
 */
@Deprecated
public class JTicketsBagSharedList {

    private final Component parent;

    private JTicketsBagSharedList(Component parent) {
        this.parent = parent;
    }

    public static JTicketsBagSharedList newJDialog(JTicketsBagShared ticketsbagshared) {
        return new JTicketsBagSharedList(ticketsbagshared);
    }

    public static String show(Component parent, List<SharedTicketInfo> atickets, DataLogicReceipts dlReceipts) {
        return JTicketsBagSharedPanel.show(parent, atickets, dlReceipts);
    }

    public String showTicketsList(List<SharedTicketInfo> atickets, DataLogicReceipts dlReceipts) {
        return JTicketsBagSharedPanel.show(parent, atickets, dlReceipts);
    }
}
