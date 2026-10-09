/*
 * Copyright (C) 2026 KriolOS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.openbravo.pos.sales;

import static org.junit.jupiter.api.Assertions.*;

import com.openbravo.basic.BasicException;
import com.openbravo.pos.ticket.TaxInfo;
import com.openbravo.pos.ticket.TicketInfo;
import com.openbravo.pos.ticket.TicketLineInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

class RemoteOrderDisplayServiceTest {

    private MockRemoteOrderService remoteOrderService;
    private MockTicketLifecycleService ticketLifecycleService;

    @BeforeEach
    void setUp() {
        remoteOrderService = new MockRemoteOrderService();
        ticketLifecycleService = new MockTicketLifecycleService();
    }

    @Test
    @DisplayName("Should dispatch ticket lines to remote order service with double qty and String display")
    void testRemoteOrderDisplayDispatchesLines() {
        TicketInfo ticket = new TicketInfo();
        ticket.setPickupId(12);

        TaxInfo tax = new TaxInfo("tax1", "VAT", "cat1", null, null, 0.15, false, 0);
        TicketLineInfo line1 = new TicketLineInfo("p1", "Cheeseburger", "cat1", 2.5, 8.50, tax);
        line1.setProperty("notes", "Extra cheese");
        line1.setProperty("display", "KITCHEN_1");

        TicketLineInfo line2 = new TicketLineInfo("p2", "Fries", "cat1", 1.0, 3.00, tax);
        line2.setProperty("notes", "No salt");
        // No explicit display property -> should fall back to default "1" when primary=true

        ticket.addLine(line1);
        ticket.addLine(line2);

        RemoteOrderDisplayService display = new RemoteOrderDisplayService(
                remoteOrderService,
                ticketLifecycleService,
                ticket,
                "EXT-999"
        );

        display.remoteOrderDisplay();

        assertEquals(1, remoteOrderService.deletedOrders.size());
        assertEquals("EXT-999", remoteOrderService.deletedOrders.get(0));

        assertEquals(2, remoteOrderService.addedOrders.size());

        RemoteOrder order1 = remoteOrderService.addedOrders.get(0);
        assertEquals("EXT-999", order1.orderId());
        assertEquals(2.5, order1.qty());
        assertEquals("Cheeseburger", order1.details());
        assertEquals("Extra cheese", order1.notes());
        assertEquals("KITCHEN_1", order1.displayId());

        RemoteOrder order2 = remoteOrderService.addedOrders.get(1);
        assertEquals("EXT-999", order2.orderId());
        assertEquals(1.0, order2.qty());
        assertEquals("Fries", order2.details());
        assertEquals("No salt", order2.notes());
        assertEquals("1", order2.displayId());
    }

    @Test
    @DisplayName("Should resolve pickup ID when external ID and customer are absent")
    void testResolveRemoteOrderIdFallsBackToPickup() {
        TicketInfo ticket = new TicketInfo();
        ticket.setPickupId(0);
        ticketLifecycleService.nextPickupIndex = 42;

        RemoteOrderDisplayService display = new RemoteOrderDisplayService(
                remoteOrderService,
                ticketLifecycleService,
                ticket,
                null
        );

        display.remoteOrderDisplay();
        assertEquals(42, ticket.getPickupId());
        assertEquals(1, remoteOrderService.deletedOrders.size());
        assertEquals("42", remoteOrderService.deletedOrders.get(0));
    }

    private static class MockRemoteOrderService implements RemoteOrderService {
        final List<RemoteOrder> addedOrders = new ArrayList<>();
        final List<String> deletedOrders = new ArrayList<>();

        @Override
        public void addOrder(RemoteOrder order) throws BasicException {
            addedOrders.add(order);
        }

        @Override
        public void updateOrder(RemoteOrder order) throws BasicException {
        }

        @Override
        public void deleteOrder(String orderId) throws BasicException {
            deletedOrders.add(orderId);
        }
    }

    private static class MockTicketLifecycleService implements TicketLifecycleService {
        int nextPickupIndex = 1;

        @Override
        public TicketInfo loadTicket(int ticketType, int ticketId) { return null; }

        @Override
        public TicketInfo loadLastTicket(int ticketType) { return null; }

        @Override
        public void saveTicket(TicketInfo ticket, String location) {}

        @Override
        public void deleteTicket(TicketInfo ticket, String location) {}

        @Override
        public com.openbravo.data.loader.SentenceList<com.openbravo.pos.ticket.FindTicketsInfo> getTicketsList() { return null; }

        @Override
        public TicketInfo getReprintTicket(String id) { return null; }

        @Override
        public List<ReprintTicketInfo> getReprintTicketList() { return List.of(); }

        @Override
        public Integer getNextTicketIndex() { return 1; }

        @Override
        public Integer getNextTicketRefundIndex() { return 1; }

        @Override
        public Integer getNextPickupIndex() {
            return nextPickupIndex;
        }

        @Override
        public void resetPickup() {}
    }
}
