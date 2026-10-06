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

import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.ticket.TicketInfo;
import java.lang.reflect.Proxy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SalesPeripheralCoordinator Test Suite")
class SalesPeripheralCoordinatorTest {

    private AppView createMockAppView(boolean hasScale, Double weight) {
        return (AppView) Proxy.newProxyInstance(
                AppView.class.getClassLoader(),
                new Class<?>[]{AppView.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "hasScale":
                            return hasScale;
                        case "readWeight":
                            return weight;
                        case "createTicketParser":
                            return null;
                        case "getAppUserView":
                            return null;
                        default:
                            return null;
                    }
                }
        );
    }

    @Test
    @DisplayName("readWeight returns null when scale is not available")
    void testReadWeight_NoScale() {
        AppView app = createMockAppView(false, null);
        SalesPeripheralCoordinator coordinator = new SalesPeripheralCoordinator(app, null, null, key -> null, key -> null);
        assertNull(coordinator.readWeight(null));
    }

    @Test
    @DisplayName("readWeight returns measured weight when scale is present")
    void testReadWeight_Success() {
        AppView app = createMockAppView(true, 1.450);
        SalesPeripheralCoordinator coordinator = new SalesPeripheralCoordinator(app, null, null, key -> null, key -> null);
        Double weight = coordinator.readWeight(null);

        assertNotNull(weight);
        assertEquals(1.450, weight);
    }

    @Test
    @DisplayName("sendRemoteOrder returns false when script resource is missing")
    void testSendRemoteOrder_MissingScript() {
        AppView app = createMockAppView(false, null);
        SalesPeripheralCoordinator coordinator = new SalesPeripheralCoordinator(app, null, null, key -> null, key -> null);
        TicketInfo ticket = new TicketInfo();

        boolean result = coordinator.sendRemoteOrder(ticket, "Table 1", null, true, false, "01", this);
        assertFalse(result);
    }

    @Test
    @DisplayName("sendRemoteOrder executes script successfully when resource is provided")
    void testSendRemoteOrder_Success() {
        AppView app = createMockAppView(false, null);
        // Valid simple BeanShell script
        String script = "var sent = true;";
        SalesPeripheralCoordinator coordinator = new SalesPeripheralCoordinator(app, null, null, key -> script, key -> null);
        TicketInfo ticket = new TicketInfo();

        boolean result = coordinator.sendRemoteOrder(ticket, "Table 1", null, true, false, "01", this);
        assertTrue(result);
    }

    @Test
    @DisplayName("reprintLastTicket returns empty when loader is null or ticket not found")
    void testReprintLastTicket_NullOrNotFound() {
        AppView app = createMockAppView(false, null);
        SalesPeripheralCoordinator coordinator = new SalesPeripheralCoordinator(app, null, null, key -> null, key -> null);

        var resultNullLoader = coordinator.reprintLastTicket(null, (SalesPeripheralCoordinator.TicketLoader) null, null, null, null);
        assertTrue(resultNullLoader.isEmpty());

        var resultNotFound = coordinator.reprintLastTicket(null, type -> null, null, null, null);
        assertTrue(resultNotFound.isEmpty());
    }

    @Test
    @DisplayName("reprintLastTicket invokes printer and notifier when ticket is found")
    void testReprintLastTicket_Success() {
        AppView app = createMockAppView(false, null);
        SalesPeripheralCoordinator coordinator = new SalesPeripheralCoordinator(app, null, null, key -> null, key -> null);
        TicketInfo ticket = new TicketInfo();

        boolean[] printed = new boolean[]{false};
        boolean[] notified = new boolean[]{false};

        var result = coordinator.reprintLastTicket(
                null,
                type -> ticket,
                null,
                (res, tck) -> {
                    assertEquals("Printer.ReprintTicket", res);
                    assertSame(ticket, tck);
                    printed[0] = true;
                },
                msg -> {
                    assertEquals("'Printer.reprint.last.ticket'", msg);
                    notified[0] = true;
                }
        );

        assertTrue(result.isPresent());
        assertSame(ticket, result.get());
        assertTrue(printed[0]);
        assertTrue(notified[0]);
    }

    @Test
    @DisplayName("reprintLastTicket handles BasicException gracefully and returns empty")
    void testReprintLastTicket_Exception() {
        AppView app = createMockAppView(false, null);
        SalesPeripheralCoordinator coordinator = new SalesPeripheralCoordinator(app, null, null, key -> null, key -> null);

        var result = coordinator.reprintLastTicket(
                null,
                type -> { throw new com.openbravo.basic.BasicException("Database error"); },
                null,
                null,
                null
        );

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("resolveRemoteOrderId prioritizes customer name, then ticketExt, then pickupString")
    void testResolveRemoteOrderId() {
        AppView app = createMockAppView(false, null);
        SalesPeripheralCoordinator coordinator = new SalesPeripheralCoordinator(app, null, null, key -> null, key -> null);

        // 1. With customer
        TicketInfo ticketWithCust = new TicketInfo();
        com.openbravo.pos.customers.CustomerInfoExt cust = new com.openbravo.pos.customers.CustomerInfoExt("c1");
        cust.setName("Alice");
        ticketWithCust.setCustomer(cust);
        assertEquals("Alice", coordinator.resolveRemoteOrderId(ticketWithCust, "Table 10", "Pick-99"));

        // 2. Without customer, with ticketExt
        TicketInfo ticketTable = new TicketInfo();
        assertEquals("Table 10", coordinator.resolveRemoteOrderId(ticketTable, "Table 10", "Pick-99"));

        // 3. Without customer, without ticketExt, with pickupString
        TicketInfo ticketPickup = new TicketInfo();
        assertEquals("Pick-99", coordinator.resolveRemoteOrderId(ticketPickup, null, "Pick-99"));
    }
}
