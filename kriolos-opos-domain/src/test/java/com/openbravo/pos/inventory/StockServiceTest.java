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
package com.openbravo.pos.inventory;

import com.openbravo.basic.BasicException;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.BeanContainer;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StockService & InventoryService Port Test Suite")
class StockServiceTest {

    private static class MockStockService implements StockService {
        final List<StockEntry> addedEntries = new ArrayList<>();
        final List<String> movements = new ArrayList<>();

        @Override
        public ProductStock getStock(String productId, String locationId) {
            return null;
        }

        @Override
        public void createStock(String locationId, String productId, double units) {
            addedEntries.add(new StockEntry(locationId, productId, units));
        }

        @Override
        public void updateStock(String locationId, String productId, double units) {
        }

        @Override
        public void addStockEntry(String locationId, String productId, String attributeSetInstanceId, double units) {
            addedEntries.add(new StockEntry(locationId, productId, attributeSetInstanceId, units));
        }

        @Override
        public void recordStockMovement(
                String id, Date date, int reasonKey, String location,
                String productId, String attSetInstId, double units, double price, String appUser) {
            movements.add(id + ":" + productId + ":" + units);
        }

        @Override
        public void revertStockMovement(
                String diaryId, String location, String productId, String attSetInstId, double units) {
            movements.add("revert:" + diaryId);
        }

        @Override
        public void saveStockDiary(ProductStockTransaction prodStock) {
        }

        @Override
        public void adjustStock(Object[] params) {
        }

        @Override
        public com.openbravo.data.user.SaveProvider getStockDiarySaveProvider() {
            return null;
        }

        @Override
        public double findProductStock(String warehouse, String id, String attsetinstid) {
            return 10.0;
        }

        @Override
        public ProductStock getProductStockState(String pId, String location) {
            return null;
        }

        @Override
        public List<ProductStock> getProductStockList(String pId) {
            return new ArrayList<>();
        }
    }

    @Test
    @DisplayName("StockService addStockEntry should correctly dispatch StockEntry record")
    void testAddStockEntryRecord() throws BasicException {
        MockStockService service = new MockStockService();
        StockEntry entry = new StockEntry("loc-main", "prod-100", "attr-5", 25.0);

        service.addStockEntry(entry);

        assertEquals(1, service.addedEntries.size());
        assertEquals("loc-main", service.addedEntries.get(0).locationId());
        assertEquals("prod-100", service.addedEntries.get(0).productId());
        assertEquals("attr-5", service.addedEntries.get(0).attributeSetInstanceId());
        assertEquals(25.0, service.addedEntries.get(0).units());
    }

    @Test
    @DisplayName("StockService recordStockMovement and revertStockMovement should register movement")
    void testRecordAndRevertStockMovement() throws BasicException {
        MockStockService service = new MockStockService();

        service.recordStockMovement("mov-1", new Date(), 1, "loc-1", "p-1", null, -5.0, 10.0, "admin");
        service.revertStockMovement("mov-1", "loc-1", "p-1", null, 5.0);

        assertEquals(2, service.movements.size());
        assertEquals("mov-1:p-1:-5.0", service.movements.get(0));
        assertEquals("revert:mov-1", service.movements.get(1));
    }

    @Test
    @DisplayName("StockService constants SQL_BUNDLE_LIST and SQL_AUXILIAR_LIST should be non-empty")
    void testConstants() {
        assertNotNull(StockService.SQL_BUNDLE_LIST);
        assertTrue(StockService.SQL_BUNDLE_LIST.contains("products_bundle"));
        assertNotNull(StockService.SQL_AUXILIAR_LIST);
        assertTrue(StockService.SQL_AUXILIAR_LIST.contains("products_com"));
    }

    @Test
    @DisplayName("BeanContainer should map StockService and InventoryService to DataLogicInventory")
    void testBeanContainerResolution() {
        AppView appView = (AppView) Proxy.newProxyInstance(
                AppView.class.getClassLoader(),
                new Class<?>[]{AppView.class},
                (proxy, method, args) -> {
                    if ("getSession".equals(method.getName())) {
                        return null;
                    }
                    if ("getBean".equals(method.getName()) && args != null && args.length == 1) {
                        if (args[0] instanceof Class<?> c) {
                            return BeanContainer.getBean(c, (AppView) proxy);
                        }
                        if (args[0] instanceof String s) {
                            return BeanContainer.getBean(s, (AppView) proxy);
                        }
                    }
                    return null;
                });

        Object stockBean = BeanContainer.getBean(StockService.class, appView);
        Object invBean = BeanContainer.getBean(InventoryService.class, appView);

        assertNotNull(stockBean);
        assertNotNull(invBean);
        assertInstanceOf(StockService.class, stockBean);
        assertInstanceOf(InventoryService.class, invBean);
        assertInstanceOf(DataLogicInventory.class, stockBean);
        assertSame(stockBean, invBean, "Both StockService and InventoryService should resolve to the same DataLogicInventory instance");
    }
}
