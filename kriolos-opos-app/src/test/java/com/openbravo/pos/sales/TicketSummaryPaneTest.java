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

import com.openbravo.pos.ticket.TicketInfo;
import com.openbravo.pos.ticket.TicketLineInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TicketSummaryPane Unit Tests")
class TicketSummaryPaneTest {

    @Test
    @DisplayName("Initial state displays default ID text and empty totals")
    void testInitialState() {
        TicketSummaryPane pane = new TicketSummaryPane();

        assertEquals("ID", pane.getTicketName());
        assertTrue(pane.getSubtotalText() == null || pane.getSubtotalText().isEmpty());
        assertTrue(pane.getTaxText() == null || pane.getTaxText().isEmpty());
        assertTrue(pane.getTotalText() == null || pane.getTotalText().isEmpty());
    }

    @Test
    @DisplayName("setTicketName updates the identifier label")
    void testSetTicketName() {
        TicketSummaryPane pane = new TicketSummaryPane();
        pane.setTicketName("Table 4 - Guest 2");

        assertEquals("Table 4 - Guest 2", pane.getTicketName());
    }

    @Test
    @DisplayName("updateTotals with explicit strings updates subtotal, tax, and total")
    void testUpdateTotals_ExplicitStrings() {
        TicketSummaryPane pane = new TicketSummaryPane();
        pane.updateTotals("100.00 €", "20.00 €", "120.00 €");

        assertEquals("100.00 €", pane.getSubtotalText());
        assertEquals("20.00 €", pane.getTaxText());
        assertEquals("120.00 €", pane.getTotalText());
    }

    @Test
    @DisplayName("updateTotals with null ticket clears total displays")
    void testUpdateTotals_NullTicket() {
        TicketSummaryPane pane = new TicketSummaryPane();
        pane.updateTotals("50.00 €", "10.00 €", "60.00 €");

        pane.updateTotals((TicketInfo) null);

        assertNull(pane.getSubtotalText());
        assertNull(pane.getTaxText());
        assertNull(pane.getTotalText());
    }

    @Test
    @DisplayName("updateTotals with empty lines ticket clears total displays")
    void testUpdateTotals_EmptyLinesTicket() {
        TicketSummaryPane pane = new TicketSummaryPane();
        pane.updateTotals("50.00 €", "10.00 €", "60.00 €");

        TicketInfo ticket = new TicketInfo();
        pane.updateTotals(ticket);

        assertNull(pane.getSubtotalText());
        assertNull(pane.getTaxText());
        assertNull(pane.getTotalText());
    }

    @Test
    @DisplayName("updateTotals with non-empty ticket populates formatted currency totals")
    void testUpdateTotals_WithTicketLines() {
        TicketSummaryPane pane = new TicketSummaryPane();

        TicketInfo ticket = new TicketInfo();
        TicketLineInfo line = new TicketLineInfo("prod-1", "Test Product", "tax-1", 1.0, 10.0, null);
        ticket.addLine(line);

        pane.updateTotals(ticket);

        assertNotNull(pane.getSubtotalText());
        assertNotNull(pane.getTaxText());
        assertNotNull(pane.getTotalText());
    }

    @Test
    @DisplayName("clear resets both ticket name and totals to null")
    void testClear() {
        TicketSummaryPane pane = new TicketSummaryPane();
        pane.setTicketName("Ticket #1234");
        pane.updateTotals("25.00 €", "5.00 €", "30.00 €");

        pane.clear();

        assertNull(pane.getTicketName());
        assertNull(pane.getSubtotalText());
        assertNull(pane.getTaxText());
        assertNull(pane.getTotalText());
    }
}
