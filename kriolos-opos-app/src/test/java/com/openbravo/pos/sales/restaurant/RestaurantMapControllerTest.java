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

package com.openbravo.pos.sales.restaurant;

import com.openbravo.pos.customers.CustomerInfoExt;
import com.openbravo.pos.forms.AppProperties;
import com.openbravo.pos.forms.AppUser;
import com.openbravo.pos.forms.AppUserView;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.sales.SharedTicketService;
import com.openbravo.pos.sales.TicketsEditor;
import com.openbravo.pos.ticket.TicketInfo;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RestaurantMapController Unit Tests")
public class RestaurantMapControllerTest {

    private RestaurantMapController controller;
    private FakePlaceService placeService;
    private AppProperties appProperties;

    @BeforeEach
    void setUp() {
        placeService = new FakePlaceService();
        appProperties = (AppProperties) Proxy.newProxyInstance(
                AppProperties.class.getClassLoader(),
                new Class<?>[]{AppProperties.class},
                (proxy, method, args) -> {
                    if ("getProperty".equals(method.getName()) && args != null && args.length > 0) {
                        if ("table.showcustomerdetails".equals(args[0])) {
                            return "true";
                        }
                        if ("table.showwaiterdetails".equals(args[0])) {
                            return "true";
                        }
                    }
                    return null;
                });

        AppUser appUser = new AppUser("u-1", "waiter-john", null, null, null, null);

        AppUserView appUserView = (AppUserView) Proxy.newProxyInstance(
                AppUserView.class.getClassLoader(),
                new Class<?>[]{AppUserView.class},
                (proxy, method, args) -> {
                    if ("getUser".equals(method.getName())) {
                        return appUser;
                    }
                    return null;
                });

        AppView appView = (AppView) Proxy.newProxyInstance(
                AppView.class.getClassLoader(),
                new Class<?>[]{AppView.class},
                (proxy, method, args) -> {
                    if ("getProperties".equals(method.getName())) {
                        return appProperties;
                    }
                    if ("getAppUserView".equals(method.getName())) {
                        return appUserView;
                    }
                    return null;
                });

        TicketsEditor ticketsEditor = (TicketsEditor) Proxy.newProxyInstance(
                TicketsEditor.class.getClassLoader(),
                new Class<?>[]{TicketsEditor.class},
                (proxy, method, args) -> null);

        controller = new RestaurantMapController(appView, ticketsEditor, (SharedTicketService) null, placeService);
    }

    @Test
    @DisplayName("Should sync ticket id, customer, and waiter on syncTableDetails")
    void testSyncTableDetails_Success() {
        Place place = new TestPlace("p-1", "Table 1");
        TicketInfo ticket = new TicketInfo();
        ticket.setOldTicket(false);
        CustomerInfoExt customer = new CustomerInfoExt("c-1");
        customer.setName("Bob Customer");
        ticket.setCustomer(customer);

        controller.syncTableDetails(place, ticket);

        assertEquals(ticket.getId(), placeService.ticketIdByTable.get("Table 1"));
        assertEquals("Bob Customer", placeService.customerByTable.get("Table 1"));
        assertEquals("waiter-john", placeService.waiterByTable.get("Table 1"));
    }

    @Test
    @DisplayName("Should not overwrite ticket id when ticket is old")
    void testSyncTableDetails_OldTicket() {
        Place place = new TestPlace("p-1", "Table 1");
        placeService.ticketIdByTable.put("Table 1", "existing-id");
        TicketInfo ticket = new TicketInfo();
        ticket.setOldTicket(true);

        controller.syncTableDetails(place, ticket);

        assertEquals("existing-id", placeService.ticketIdByTable.get("Table 1"));
    }

    @Test
    @DisplayName("Should move customer when tableMoved flag is set")
    void testSyncTableDetails_TableMoved() {
        Place place = new TestPlace("p-1", "Table 1");
        TicketInfo ticket = new TicketInfo();
        placeService.movedFlags.put(ticket.getId(), true);

        controller.syncTableDetails(place, ticket);

        assertTrue(placeService.movedCustomers.containsKey("Table 1"));
        assertEquals(ticket.getId(), placeService.movedCustomers.get("Table 1"));
    }

    @Test
    @DisplayName("Should clear customer, waiter, and ticketId on clearTable")
    void testClearTable() {
        placeService.customerByTable.put("Table 1", "Bob");
        placeService.waiterByTable.put("Table 1", "John");
        placeService.ticketIdByTable.put("Table 1", "tck-1");

        controller.clearTable("Table 1");

        assertNull(placeService.customerByTable.get("Table 1"));
        assertNull(placeService.waiterByTable.get("Table 1"));
        assertNull(placeService.ticketIdByTable.get("Table 1"));
    }

    @Test
    @DisplayName("Should sync customer by ticketId")
    void testSyncCustomerInTable() {
        controller.syncCustomerInTable("Alice", "tck-99");

        assertEquals("Alice", placeService.customerByTicketId.get("tck-99"));
    }

    @Test
    @DisplayName("Should expose SharedTicketService and support getTicketInfo via service")
    void testSharedTicketServiceIntegration() throws Exception {
        TicketInfo expectedTicket = new TicketInfo();
        SharedTicketService mockService = (SharedTicketService) Proxy.newProxyInstance(
                SharedTicketService.class.getClassLoader(),
                new Class<?>[]{SharedTicketService.class},
                (proxy, method, args) -> {
                    if ("getSharedTicket".equals(method.getName())) {
                        return expectedTicket;
                    }
                    return null;
                });

        RestaurantMapController ctrl = new RestaurantMapController(null, null, mockService, placeService);
        assertSame(mockService, ctrl.getSharedTicketService());

        Place place = new TestPlace("p-1", "Table 1");
        TicketInfo retrieved = ctrl.getTicketInfo(place);
        assertNotNull(retrieved);
        assertEquals(expectedTicket.getId(), retrieved.getId());
    }

    private static class TestPlace extends Place {
        private final String id;
        private final String name;

        TestPlace(String id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public String getName() {
            return name;
        }
    }

    private static class FakePlaceService extends PlaceServiceImpl {
        final Map<String, String> ticketIdByTable = new HashMap<>();
        final Map<String, String> customerByTable = new HashMap<>();
        final Map<String, String> waiterByTable = new HashMap<>();
        final Map<String, String> customerByTicketId = new HashMap<>();
        final Map<String, Boolean> movedFlags = new HashMap<>();
        final Map<String, String> movedCustomers = new HashMap<>();

        public FakePlaceService() {
            super(null);
        }

        @Override
        public void setTicketIdInTable(String ticketId, String tableName) {
            ticketIdByTable.put(tableName, ticketId);
        }

        @Override
        public String getCustomerNameInTable(String tableName) {
            return customerByTable.get(tableName);
        }

        @Override
        public void setCustomerNameInTable(String custName, String tableName) {
            customerByTable.put(tableName, custName);
        }

        @Override
        public String getWaiterNameInTable(String tableName) {
            return waiterByTable.get(tableName);
        }

        @Override
        public void setWaiterNameInTable(String waiterName, String tableName) {
            waiterByTable.put(tableName, waiterName);
        }

        @Override
        public Boolean getTableMovedFlag(String ticketID) {
            return movedFlags.get(ticketID);
        }

        @Override
        public void moveCustomer(String newTable, String ticketID) {
            movedCustomers.put(newTable, ticketID);
        }

        @Override
        public void setCustomerNameInTableByTicketId(String custName, String ticketID) {
            customerByTicketId.put(ticketID, custName);
        }

        @Override
        public void clearCustomerNameInTable(String tableName) {
            customerByTable.remove(tableName);
        }

        @Override
        public void clearWaiterNameInTable(String tableName) {
            waiterByTable.remove(tableName);
        }

        @Override
        public void clearTicketIdInTable(String tableName) {
            ticketIdByTable.remove(tableName);
        }
    }
}
