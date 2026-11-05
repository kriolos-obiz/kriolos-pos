/*
 * Copyright (C) 2026 KriolOS POS
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

import com.openbravo.basic.BasicException;
import com.openbravo.pos.panels.AppView;
import com.openbravo.pos.container.BeanContainer;
import com.openbravo.pos.ticket.TicketInfo;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SharedTicketService Port Test Suite")
class SharedTicketServiceTest {

    @Test
    @DisplayName("Should resolve SharedTicketService in BeanContainer to DataLogicReceipts")
    void shouldResolveSharedTicketServiceInBeanContainer() {
        AppView appViewProxy = (AppView) Proxy.newProxyInstance(
                AppView.class.getClassLoader(),
                new Class<?>[]{AppView.class},
                (proxy, method, args) -> null
        );

        Object beanByString = BeanContainer.getBean("com.openbravo.pos.sales.SharedTicketService", appViewProxy);
        assertNotNull(beanByString, "BeanContainer should resolve SharedTicketService via String key");
        assertInstanceOf(DataLogicReceipts.class, beanByString, "SharedTicketService should resolve to DataLogicReceipts");
        assertInstanceOf(SharedTicketService.class, beanByString, "DataLogicReceipts should implement SharedTicketService");

        SharedTicketService beanByClass = BeanContainer.getBean(SharedTicketService.class, appViewProxy);
        assertNotNull(beanByClass, "BeanContainer should resolve SharedTicketService via Class literal");
        assertInstanceOf(DataLogicReceipts.class, beanByClass);
    }

    @Test
    @DisplayName("Mock SharedTicketService should satisfy domain port contracts")
    void testSharedTicketServiceContract() throws BasicException {
        MockSharedTicketService service = new MockSharedTicketService();

        TicketInfo ticket1 = new TicketInfo();
        service.insertSharedTicket("t1", ticket1, 101);

        assertEquals(1, service.getSharedTicketList().size(), "Should have 1 shared ticket");
        assertNotNull(service.getSharedTicket("t1"), "Should retrieve ticket by id");
        assertEquals(101, service.getPickupId("t1"), "Pickup ID should match 101");

        // Lock / Unlock lifecycle
        service.lockSharedTicket("t1", "user-admin");
        assertEquals("user-admin", service.getLockState("t1", null), "Lock state should reflect locking user");

        service.unlockSharedTicket("t1", "unlocked");
        assertEquals("unlocked", service.getLockState("t1", null), "Lock state should reflect unlocked value");

        // Update
        ticket1.setPickupId(102);
        service.updateSharedTicket("t1", ticket1, 102);
        assertEquals(102, service.getPickupId("t1"), "Pickup ID should be updated to 102");

        // Restaurant variant
        TicketInfo rTicket = new TicketInfo();
        service.insertRSharedTicket("r1", rTicket, 201);
        assertNotNull(service.getSharedTicket("r1"));
        service.updateRSharedTicket("r1", rTicket, 202);
        assertEquals(202, service.getPickupId("r1"));

        // User filtering
        List<SharedTicketInfo> userTickets = service.getUserSharedTicketList("admin");
        assertNotNull(userTickets);

        // Delete
        service.deleteSharedTicket("t1");
        assertNull(service.getSharedTicket("t1"), "Deleted ticket should return null");
    }

    @Test
    @DisplayName("SaveSharedTicketCommand should enforce invariants and immutability")
    void shouldEnforceSaveSharedTicketCommandInvariants() {
        TicketInfo ticket = new TicketInfo();

        assertThrows(NullPointerException.class,
                () -> new SaveSharedTicketCommand(null, ticket, 1),
                "Null id should throw NullPointerException");
        assertThrows(IllegalArgumentException.class,
                () -> new SaveSharedTicketCommand("", ticket, 1),
                "Empty id should throw IllegalArgumentException");
        assertThrows(IllegalArgumentException.class,
                () -> new SaveSharedTicketCommand("   ", ticket, 1),
                "Blank id should throw IllegalArgumentException");

        assertThrows(NullPointerException.class,
                () -> new SaveSharedTicketCommand("ticket-1", null, 1),
                "Null ticket should throw NullPointerException");

        assertThrows(IllegalArgumentException.class,
                () -> new SaveSharedTicketCommand("ticket-1", ticket, -1),
                "Negative pickupId should throw IllegalArgumentException");

        SaveSharedTicketCommand cmd = SaveSharedTicketCommand.of("ticket-42", ticket, 105);
        assertEquals("ticket-42", cmd.id());
        assertSame(ticket, cmd.ticket());
        assertEquals(105, cmd.pickupId());
    }

    @Test
    @DisplayName("SharedTicketService should dispatch SaveSharedTicketCommand and delegate legacy overloads")
    void shouldDispatchSaveSharedTicketCommands() throws BasicException {
        MockSharedTicketService service = new MockSharedTicketService();
        TicketInfo ticket = new TicketInfo();

        SaveSharedTicketCommand cmd = SaveSharedTicketCommand.of("place-1", ticket, 50);
        service.insertSharedTicket(cmd);
        assertSame(ticket, service.getSharedTicket("place-1"));
        assertEquals(50, service.getPickupId("place-1"));

        TicketInfo updated = new TicketInfo();
        SaveSharedTicketCommand updateCmd = SaveSharedTicketCommand.of("place-1", updated, 51);
        service.updateSharedTicket(updateCmd);
        assertSame(updated, service.getSharedTicket("place-1"));
        assertEquals(51, service.getPickupId("place-1"));

        // Restaurant typed variants
        TicketInfo rTicket = new TicketInfo();
        SaveSharedTicketCommand rCmd = SaveSharedTicketCommand.of("r-place", rTicket, 70);
        service.insertRSharedTicket(rCmd);
        assertSame(rTicket, service.getSharedTicket("r-place"));

        TicketInfo rUpdated = new TicketInfo();
        SaveSharedTicketCommand rUpdCmd = SaveSharedTicketCommand.of("r-place", rUpdated, 71);
        service.updateRSharedTicket(rUpdCmd);
        assertSame(rUpdated, service.getSharedTicket("r-place"));
    }

    private static class MockSharedTicketService implements SharedTicketService {
        private final Map<String, TicketInfo> tickets = new HashMap<>();
        private final Map<String, Integer> pickupIds = new HashMap<>();
        private final Map<String, String> locks = new HashMap<>();
        private final Map<String, String> users = new HashMap<>();

        @Override
        public TicketInfo getSharedTicket(String id) throws BasicException {
            return tickets.get(id);
        }

        @Override
        public List<SharedTicketInfo> getSharedTicketList() throws BasicException {
            List<SharedTicketInfo> list = new ArrayList<>();
            for (String id : tickets.keySet()) {
                SharedTicketInfo info = new SharedTicketInfo();
                info.setId(id);
                info.setName("Ticket-" + id);
                info.setUserName(users.getOrDefault(id, "admin"));
                list.add(info);
            }
            return list;
        }

        @Override
        public List<SharedTicketInfo> getUserSharedTicketList(String appuser) throws BasicException {
            return getSharedTicketList();
        }

        @Override
        public SharedTicketInfo getSharedTicketInfo(String sharedId) throws BasicException {
            SharedTicketInfo info = new SharedTicketInfo();
            info.setId(sharedId);
            info.setName("Ticket-" + sharedId);
            info.setUserName(users.getOrDefault(sharedId, "admin"));
            return info;
        }

        @Override
        public void insertSharedTicket(SaveSharedTicketCommand command) throws BasicException {
            tickets.put(command.id(), command.ticket());
            pickupIds.put(command.id(), command.pickupId());
            users.put(command.id(), "admin");
        }

        @Override
        public void updateSharedTicket(SaveSharedTicketCommand command) throws BasicException {
            tickets.put(command.id(), command.ticket());
            pickupIds.put(command.id(), command.pickupId());
        }

        @Override
        public void lockSharedTicket(String id, String locked) throws BasicException {
            locks.put(id, locked);
        }

        @Override
        public void unlockSharedTicket(String id, String unlocked) throws BasicException {
            locks.put(id, unlocked);
        }

        @Override
        public void insertRSharedTicket(String id, TicketInfo ticket, int pickupid) throws BasicException {
            insertSharedTicket(id, ticket, pickupid);
        }

        @Override
        public void deleteSharedTicket(String id) throws BasicException {
            tickets.remove(id);
            pickupIds.remove(id);
            locks.remove(id);
            users.remove(id);
        }

        @Override
        public Integer getPickupId(String sharedTicketId) throws BasicException {
            return pickupIds.getOrDefault(sharedTicketId, 0);
        }

        @Override
        public String getUserId(String sharedTicketId) throws BasicException {
            return users.get(sharedTicketId);
        }

        @Override
        public String getLockState(String sharedTicketId, String lockState) throws BasicException {
            return locks.getOrDefault(sharedTicketId, lockState);
        }
    }
}
