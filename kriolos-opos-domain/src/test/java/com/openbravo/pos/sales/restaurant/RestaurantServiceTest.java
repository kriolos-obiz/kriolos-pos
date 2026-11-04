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
package com.openbravo.pos.sales.restaurant;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.SentenceExec;
import com.openbravo.data.loader.SentenceList;
import com.openbravo.data.loader.TableDefinition;
import com.openbravo.data.user.EditorCreator;
import com.openbravo.data.user.ListProvider;
import com.openbravo.data.user.SaveProvider;
import com.openbravo.pos.panels.AppView;
import com.openbravo.pos.container.BeanContainer;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RestaurantService Port Test Suite")
class RestaurantServiceTest {

    @Test
    @DisplayName("Should resolve RestaurantService in BeanContainer to DataLogicRestaurant")
    void shouldResolveRestaurantServiceInBeanContainer() {
        AppView appViewProxy = (AppView) Proxy.newProxyInstance(
                AppView.class.getClassLoader(),
                new Class<?>[]{AppView.class},
                (proxy, method, args) -> null
        );

        Object bean = BeanContainer.getBean("com.openbravo.pos.sales.restaurant.RestaurantService", appViewProxy);
        assertNotNull(bean, "BeanContainer should resolve RestaurantService");
        assertInstanceOf(DataLogicRestaurant.class, bean, "RestaurantService should resolve to DataLogicRestaurant");
        assertInstanceOf(RestaurantService.class, bean, "DataLogicRestaurant should implement RestaurantService");
    }

    @Test
    @DisplayName("RestaurantService mock implementation should satisfy domain contract")
    void testRestaurantServiceContract() throws BasicException {
        RestaurantService service = new RestaurantService() {
            @Override
            public List<FloorsInfo> getFloorsListAll() throws BasicException {
                FloorsInfo floor = new FloorsInfo();
                floor.setID("1");
                return Collections.singletonList(floor);
            }

            @Override
            public List<FloorsInfo> getFloorTablesListAll() throws BasicException {
                return Collections.emptyList();
            }

            @Override
            public TableDefinition getTableFloors() {
                return null;
            }

            @Override
            public TableDefinition getTablePlaces() {
                return null;
            }

            @Override
            public void updatePlaces(UpdatePlacePositionCommand command) throws BasicException {}

            @Override
            public ListProvider getReservationsListProvider(EditorCreator filter) {
                return null;
            }

            @Override
            public SaveProvider getReservationsSaveProvider() {
                return null;
            }

            @Override
            public SentenceList<FloorsInfo> getFloorsList() {
                return null;
            }

            @Override
            public SentenceList<FloorsInfo> getFloorTablesList() {
                return null;
            }

            @Override
            public SentenceList getReservationsList() {
                return null;
            }

            @Override
            public SentenceExec getReservationsUpdate() {
                return null;
            }

            @Override
            public SentenceExec getReservationsDelete() {
                return null;
            }

            @Override
            public SentenceExec getReservationsInsert() {
                return null;
            }
        };

        List<FloorsInfo> floors = service.getFloorsListAll();
        assertNotNull(floors);
        assertEquals(1, floors.size());
        assertEquals("1", floors.get(0).getKey());
    }

    @Test
    @DisplayName("UpdatePlacePositionCommand should enforce invariants")
    void shouldEnforceCommandInvariants() {
        assertThrows(NullPointerException.class, () -> new UpdatePlacePositionCommand(null, 10, 20),
                "Null id should throw NullPointerException");
        assertThrows(IllegalArgumentException.class, () -> new UpdatePlacePositionCommand("", 10, 20),
                "Empty id should throw IllegalArgumentException");
        assertThrows(IllegalArgumentException.class, () -> new UpdatePlacePositionCommand("   ", 10, 20),
                "Blank id should throw IllegalArgumentException");

        UpdatePlacePositionCommand cmd = UpdatePlacePositionCommand.of("table-1", 100, 200);
        assertEquals("table-1", cmd.id());
        assertEquals(100, cmd.x());
        assertEquals(200, cmd.y());
    }

    @Test
    @DisplayName("RestaurantService default updatePlaces(x, y, id) should delegate to updatePlaces(command)")
    void shouldDelegateLegacyUpdatePlacesInRestaurantService() throws BasicException {
        final UpdatePlacePositionCommand[] captured = new UpdatePlacePositionCommand[1];
        RestaurantService service = new RestaurantService() {
            @Override
            public List<FloorsInfo> getFloorsListAll() { return Collections.emptyList(); }
            @Override
            public List<FloorsInfo> getFloorTablesListAll() { return Collections.emptyList(); }
            @Override
            public TableDefinition getTableFloors() { return null; }
            @Override
            public TableDefinition getTablePlaces() { return null; }
            @Override
            public void updatePlaces(UpdatePlacePositionCommand command) {
                captured[0] = command;
            }
            @Override
            public ListProvider getReservationsListProvider(EditorCreator filter) { return null; }
            @Override
            public SaveProvider getReservationsSaveProvider() { return null; }
            @Override
            public SentenceList<FloorsInfo> getFloorsList() { return null; }
            @Override
            public SentenceList<FloorsInfo> getFloorTablesList() { return null; }
            @Override
            public SentenceList getReservationsList() { return null; }
            @Override
            public SentenceExec getReservationsUpdate() { return null; }
            @Override
            public SentenceExec getReservationsDelete() { return null; }
            @Override
            public SentenceExec getReservationsInsert() { return null; }
        };

        service.updatePlaces(40, 80, "place-99");
        assertNotNull(captured[0]);
        assertEquals("place-99", captured[0].id());
        assertEquals(40, captured[0].x());
        assertEquals(80, captured[0].y());
    }

    @Test
    @DisplayName("PlaceService default updatePlaces(x, y, id) should delegate to updatePlaces(command)")
    void shouldDelegateLegacyUpdatePlacesInPlaceService() throws BasicException {
        final UpdatePlacePositionCommand[] captured = new UpdatePlacePositionCommand[1];
        PlaceService placeService = new PlaceService() {
            @Override
            public List<Floor> getFloors() { return Collections.emptyList(); }
            @Override
            public List<Place> getPlaces() { return Collections.emptyList(); }
            @Override
            public void moveCustomer(String newTable, String ticketID) {}
            @Override
            public void setCustomerNameInTable(String custName, String tableName) {}
            @Override
            public void setCustomerNameInTableById(String custName, String tableID) {}
            @Override
            public void setCustomerNameInTableByTicketId(String custName, String ticketID) {}
            @Override
            public String getCustomerNameInTable(String tableName) { return null; }
            @Override
            public String getCustomerNameInTableById(String tableId) { return null; }
            @Override
            public void clearCustomerNameInTable(String tableName) {}
            @Override
            public void clearCustomerNameInTableById(String tableID) {}
            @Override
            public void setWaiterNameInTable(String waiterName, String tableName) {}
            @Override
            public void setWaiterNameInTableById(String waiterName, String tableID) {}
            @Override
            public String getWaiterNameInTable(String tableName) { return null; }
            @Override
            public String getWaiterNameInTableById(String tableID) { return null; }
            @Override
            public void clearWaiterNameInTable(String tableName) {}
            @Override
            public void clearWaiterNameInTableById(String tableID) {}
            @Override
            public String getTicketIdInTable(String tableID) { return null; }
            @Override
            public void setTicketIdInTable(String ticketId, String tableName) {}
            @Override
            public void clearTicketIdInTable(String tableName) {}
            @Override
            public void clearTicketIdInTableById(String tableID) {}
            @Override
            public Integer countTicketIdInTable(String ticketID) { return 0; }
            @Override
            public String getTableDetails(String ticketID) { return null; }
            @Override
            public void setTableMovedFlag(String tableID) {}
            @Override
            public String getTableMovedName(String ticketID) { return null; }
            @Override
            public Boolean getTableMovedFlag(String ticketID) { return false; }
            @Override
            public void clearTableMovedFlag(String tableID) {}
            @Override
            public void updatePlaces(UpdatePlacePositionCommand command) {
                captured[0] = command;
            }
        };

        placeService.updatePlaces(120, 240, "place-42");
        assertNotNull(captured[0]);
        assertEquals("place-42", captured[0].id());
        assertEquals(120, captured[0].x());
        assertEquals(240, captured[0].y());
    }
}
