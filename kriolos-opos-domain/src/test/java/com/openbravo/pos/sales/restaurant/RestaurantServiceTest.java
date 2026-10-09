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
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.BeanContainer;
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
            public void updatePlaces(int x, int y, String id) throws BasicException {}

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
}
