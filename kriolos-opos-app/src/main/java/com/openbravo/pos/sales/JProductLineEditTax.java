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

import java.awt.Component;
import com.openbravo.basic.BasicException;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.ticket.TicketLineInfo;

/**
 * Backward-compatible adapter delegating to {@link JProductLineEditTaxPanel}.
 *
 * @deprecated Use {@link JProductLineEditTaxPanel#showMessage(Component, AppView, TicketLineInfo)} instead.
 */
@Deprecated
public class JProductLineEditTax {

    private JProductLineEditTax() {
    }

    public static TicketLineInfo showMessage(Component parent, AppView app, TicketLineInfo oLine)
            throws BasicException {
        return JProductLineEditTaxPanel.showMessage(parent, app, oLine);
    }
}
