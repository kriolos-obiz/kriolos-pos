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

import com.openbravo.pos.ticket.ProductInfoExt;
import com.openbravo.pos.ticket.TaxInfo;
import com.openbravo.pos.ticket.TicketInfo;
import com.openbravo.pos.ticket.TicketLineInfo;
import java.util.Properties;
import javax.swing.JPanel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TicketLineController Unit Tests")
public class TicketLineControllerTest {

    private TicketLineController controller;
    private TicketInfo ticket;

    @BeforeEach
    void setUp() {
        controller = new TicketLineController(null, null, null);
        ticket = new TicketInfo();
    }

    @Test
    @DisplayName("Should return NOOP for invalid line index on quantity change")
    void testChangeQuantityInvalidIndex() {
        LineChangeResult result = controller.changeLineQuantity(new JPanel(), ticket, -1, 1.0, false);
        assertEquals(LineChangeResult.Status.NOOP, result.status());

        result = controller.changeLineQuantity(new JPanel(), ticket, 0, 1.0, false);
        assertEquals(LineChangeResult.Status.NOOP, result.status());
    }

    @Test
    @DisplayName("Should increment quantity for normal receipt line")
    void testIncrementQuantityNormalReceipt() {
        TaxInfo tax = new TaxInfo("tax-01", "Standard", "1", "cat-01", null, 0.10, false, 0);
        TicketLineInfo line = new TicketLineInfo("Coffee", "cat-01", 1.0, 2.50, tax);
        ticket.addLine(line);

        LineChangeResult result = controller.changeLineQuantity(new JPanel(), ticket, 0, 1.0, false);

        assertEquals(LineChangeResult.Status.UPDATED, result.status());
        assertNotNull(result.updatedLine());
        assertEquals(2.0, result.updatedLine().getMultiply());
        assertEquals("true", result.updatedLine().getProperty(TicketConstants.PROP_TICKET_UPDATED));
    }

    @Test
    @DisplayName("Should handle absolute quantity update for normal receipt")
    void testSetAbsoluteQuantity() {
        TaxInfo tax = new TaxInfo("tax-01", "Standard", "1", "cat-01", null, 0.10, false, 0);
        TicketLineInfo line = new TicketLineInfo("Juice", "cat-01", 1.0, 3.00, tax);
        ticket.addLine(line);

        LineChangeResult result = controller.changeLineQuantity(new JPanel(), ticket, 0, 5.0, true);

        assertEquals(LineChangeResult.Status.UPDATED, result.status());
        assertNotNull(result.updatedLine());
        assertEquals(5.0, result.updatedLine().getMultiply());
    }

    @Test
    @DisplayName("Should handle refund ticket where increment means more negative")
    void testRefundQuantityAdjustment() {
        ticket.setTicketType(TicketInfo.RECEIPT_REFUND);
        TaxInfo tax = new TaxInfo("tax-01", "Standard", "1", "cat-01", null, 0.10, false, 0);
        TicketLineInfo line = new TicketLineInfo("Refund Item", "cat-01", -1.0, 10.00, tax);
        ticket.addLine(line);

        LineChangeResult result = controller.changeLineQuantity(new JPanel(), ticket, 0, 1.0, false);

        assertEquals(LineChangeResult.Status.UPDATED, result.status());
        assertEquals(-2.0, result.updatedLine().getMultiply());
    }
}
