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
import com.openbravo.data.loader.SentenceList;
import com.openbravo.pos.container.BeanContainer;
import com.openbravo.pos.panels.AppView;
import com.openbravo.pos.ticket.FindTicketsInfo;
import com.openbravo.pos.ticket.TicketInfo;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TicketLifecycleService Port & Command Test Suite")
class TicketLifecycleServiceTest {

    @Test
    @DisplayName("SaveTicketCommand should enforce non-null ticket invariant")
    void saveTicketCommandShouldEnforceInvariants() {
        assertThrows(NullPointerException.class, () -> new SaveTicketCommand(null, "loc-1"));

        TicketInfo ticket = new TicketInfo();
        SaveTicketCommand cmd = new SaveTicketCommand(ticket, "loc-1");
        assertSame(ticket, cmd.ticket());
        assertEquals("loc-1", cmd.location());

        SaveTicketCommand cmdFactory = SaveTicketCommand.of(ticket, "loc-2");
        assertSame(ticket, cmdFactory.ticket());
        assertEquals("loc-2", cmdFactory.location());
    }

    @Test
    @DisplayName("DeleteTicketCommand should enforce non-null ticket invariant")
    void deleteTicketCommandShouldEnforceInvariants() {
        assertThrows(NullPointerException.class, () -> new DeleteTicketCommand(null, "loc-1"));

        TicketInfo ticket = new TicketInfo();
        DeleteTicketCommand cmd = new DeleteTicketCommand(ticket, "loc-1");
        assertSame(ticket, cmd.ticket());
        assertEquals("loc-1", cmd.location());

        DeleteTicketCommand cmdFactory = DeleteTicketCommand.of(ticket, "loc-2");
        assertSame(ticket, cmdFactory.ticket());
        assertEquals("loc-2", cmdFactory.location());
    }

    @Test
    @DisplayName("Should resolve TicketLifecycleService in BeanContainer to DataLogicSales")
    void shouldResolveTicketLifecycleServiceInBeanContainer() {
        AppView appViewProxy = (AppView) Proxy.newProxyInstance(
                AppView.class.getClassLoader(),
                new Class<?>[]{AppView.class},
                (proxy, method, args) -> null
        );

        Object beanByString = BeanContainer.getBean("com.openbravo.pos.sales.TicketLifecycleService", appViewProxy);
        assertNotNull(beanByString, "BeanContainer should resolve TicketLifecycleService via String key");
        assertInstanceOf(DataLogicSales.class, beanByString, "TicketLifecycleService should resolve to DataLogicSales");
        assertInstanceOf(TicketLifecycleService.class, beanByString, "DataLogicSales should implement TicketLifecycleService");

        TicketLifecycleService beanByClass = BeanContainer.getBean(TicketLifecycleService.class, appViewProxy);
        assertNotNull(beanByClass, "BeanContainer should resolve TicketLifecycleService via Class literal");
        assertInstanceOf(DataLogicSales.class, beanByClass);
    }

    @Test
    @DisplayName("Mock TicketLifecycleService should delegate deprecated calls to command methods")
    void testTicketLifecycleServiceDelegation() throws BasicException {
        MockLifecycleService service = new MockLifecycleService();

        TicketInfo ticket = new TicketInfo();
        ticket.setTicketId(42);

        // Test deprecated saveTicket delegates to command
        service.saveTicket(ticket, "warehouse-0");
        assertEquals(1, service.savedCommands.size());
        assertEquals(ticket, service.savedCommands.get(0).ticket());
        assertEquals("warehouse-0", service.savedCommands.get(0).location());

        // Test direct saveTicket with command
        SaveTicketCommand saveCmd = SaveTicketCommand.of(ticket, "warehouse-1");
        service.saveTicket(saveCmd);
        assertEquals(2, service.savedCommands.size());
        assertEquals("warehouse-1", service.savedCommands.get(1).location());

        // Test deprecated deleteTicket delegates to command
        service.deleteTicket(ticket, "warehouse-0");
        assertEquals(1, service.deletedCommands.size());
        assertEquals(ticket, service.deletedCommands.get(0).ticket());
        assertEquals("warehouse-0", service.deletedCommands.get(0).location());

        // Test direct deleteTicket with command
        DeleteTicketCommand deleteCmd = DeleteTicketCommand.of(ticket, "warehouse-1");
        service.deleteTicket(deleteCmd);
        assertEquals(2, service.deletedCommands.size());
        assertEquals("warehouse-1", service.deletedCommands.get(1).location());
    }

    private static class MockLifecycleService implements TicketLifecycleService {
        final List<SaveTicketCommand> savedCommands = new ArrayList<>();
        final List<DeleteTicketCommand> deletedCommands = new ArrayList<>();

        @Override
        public void saveTicket(SaveTicketCommand command) throws BasicException {
            savedCommands.add(command);
        }

        @Override
        public void deleteTicket(DeleteTicketCommand command) throws BasicException {
            deletedCommands.add(command);
        }

        @Override
        public TicketInfo loadTicket(int ticketType, int ticketId) throws BasicException {
            return null;
        }

        @Override
        public TicketInfo loadLastTicket(int ticketType) throws BasicException {
            return null;
        }

        @Override
        public SentenceList<FindTicketsInfo> getTicketsList() {
            return null;
        }

        @Override
        public TicketInfo getReprintTicket(String id) throws BasicException {
            return null;
        }

        @Override
        public List<ReprintTicketInfo> getReprintTicketList() throws BasicException {
            return List.of();
        }

        @Override
        public Integer getNextTicketIndex() throws BasicException {
            return 1;
        }

        @Override
        public Integer getNextTicketRefundIndex() throws BasicException {
            return 1;
        }

        @Override
        public Integer getNextPickupIndex() throws BasicException {
            return 1;
        }

        @Override
        public void resetPickup() throws BasicException {
        }
    }
}

