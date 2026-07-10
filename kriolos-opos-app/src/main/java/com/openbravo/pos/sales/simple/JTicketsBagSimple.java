//    KriolOS POS
//    Copyright (c) 2019-2023 KriolOS
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

package com.openbravo.pos.sales.simple;

import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.sales.*;
import com.openbravo.pos.sales.shared.JTicketsBagShared;

/**
 * A simplified, single-ticket implementation of the tickets bag controller that extends {@link JTicketsBagShared}.
 * <p>
 * This class modifies the multi-ticket behavior of the shared ticket bag into a strict "direct checkout"
 * or retail workflow. By disabling the standard toolbar buttons upon initialization, it explicitly
 * prevents operators from creating new concurrent tickets, putting transactions on hold, or browsing
 * a suspended ticket list.
 * </p>
 *
 * <h3>Workflow Adjustments:</h3>
 * <ul>
 *   <li><b>UI Restriction:</b> Invokes {@link #disableAllButtons()} in the constructor to lock down multi-ticket features.</li>
 *   <li><b>Retained Operations:</b> Re-enables only the ticket deletion routine ({@link #setEnabledButtonDel(boolean)})
 *       to ensure that operators can still void or cancel the current single checkout session under standard system rules.</li>
 * </ul>
 *
 * @author JG uniCenta
 * @version 1.0
 * @see com.openbravo.pos.sales.shared.JTicketsBagShared
 * @see com.openbravo.pos.sales.JTicketsBag
 */
public class JTicketsBagSimple extends JTicketsBagShared {

    /**
     * Constructs a new simple ticket bag manager.
     * <p>
     * Initializes the underlying shared components via {@code super(app, ticketsEditor)}, activates the
     * primary container panel layout, and configures the button states to strictly enforce a single,
     * direct sales transaction environment.
     * </p>
     *
     * @param app         the application view context providing access to beans and permissions.
     * @param ticketsEditor the central visual editor where the single active ticket lines are modified.
     */
    public JTicketsBagSimple(AppView app, TicketsEditor ticketsEditor) {
        super(app, ticketsEditor);
        this.setEnabledPanel(true);
        this.disableAllButtons();
        this.setEnabledButtonDel(true);
    }

 
    
}
