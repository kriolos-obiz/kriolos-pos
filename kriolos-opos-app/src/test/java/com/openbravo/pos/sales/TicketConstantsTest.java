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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TicketConstants Unit Tests")
public class TicketConstantsTest {

    @Test
    @DisplayName("Should define expected standard ticket event and resource constants")
    void testConstants() {
        assertEquals("ticket.show", TicketConstants.EV_TICKET_SHOW);
        assertEquals("ticket.change", TicketConstants.EV_TICKET_CHANGE);
        assertEquals("ticket.close", TicketConstants.EV_TICKET_CLOSE);
        assertEquals("ticket.save", TicketConstants.EV_TICKET_SAVE);
        assertEquals("ticket.total", TicketConstants.EV_TICKET_TOTAL);
        assertEquals("ticket.updated", TicketConstants.PROP_TICKET_UPDATED);
        assertEquals("Ticket.Buttons", TicketConstants.RES_TICKET_BUTTONS);
        assertEquals("Ticket.Line", TicketConstants.RES_TICKET_LINES);
    }
}
