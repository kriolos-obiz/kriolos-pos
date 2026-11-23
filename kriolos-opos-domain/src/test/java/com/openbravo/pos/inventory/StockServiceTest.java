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
import com.openbravo.data.loader.Session;
import com.openbravo.pos.panels.AppView;
import com.openbravo.pos.container.BeanContainer;
import java.lang.reflect.Proxy;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StockService & InventoryService Port Test Suite")
class StockServiceTest {

    private static class MockStockService implements StockService {
        final List<StockEntry> addedEntries = new ArrayList<>();
        final List<String> movements = new ArrayList<>();
        final List<StockAdjustmentCommand> adjustments = new ArrayList<>();

        @Override
        public ProductStock getStock(String productId, String locationId) {
            return null;
        }

        @Override
        public void createStock(CreateStockCommand command) {
            if (command != null) {
                addedEntries.add(new StockEntry(command.locationId(), command.productId(), command.attributeSetInstanceId(), command.units()));
            }
        }

        @Override
        public void updateStock(UpdateStockCommand command) {
        }

        @Override
        public void addStockEntry(String locationId, String productId, String attributeSetInstanceId, double units) {
            addedEntries.add(new StockEntry(locationId, productId, attributeSetInstanceId, units));
        }

        @Override
        public void recordStockMovement(RecordStockMovementCommand command) {
            movements.add(command.id() + ":" + command.productId() + ":" + command.units());
        }

        @Override
        public void revertStockMovement(RevertStockMovementCommand command) {
            movements.add("revert:" + command.diaryId());
        }

        @Override
        public void saveStockDiary(ProductStockTransaction prodStock) {
        }

        @Override
        public void adjustStock(StockAdjustmentCommand command) {
            if (command != null) {
                adjustments.add(command);
            }
        }

        @Override
        public com.openbravo.data.user.SaveProvider<StockDiaryInfo> getStockDiarySaveProvider() {
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
    @SuppressWarnings("deprecation")
    void testRecordAndRevertStockMovement() throws BasicException {
        MockStockService service = new MockStockService();

        service.recordStockMovement(RecordStockMovementCommand.of("mov-1", new Date(), 1, "loc-1", "p-1", null, -5.0, 10.0, "admin"));
        service.revertStockMovement(RevertStockMovementCommand.of("mov-1", "loc-1", "p-1", null, 5.0));

        // Test deprecated overloads
        service.recordStockMovement("mov-legacy", new Date(), 2, "loc-2", "p-2", null, 3.0, 15.0, "user");
        service.revertStockMovement("mov-legacy", "loc-2", "p-2", null, 3.0);

        assertEquals(4, service.movements.size());
        assertEquals("mov-1:p-1:-5.0", service.movements.get(0));
        assertEquals("revert:mov-1", service.movements.get(1));
        assertEquals("mov-legacy:p-2:3.0", service.movements.get(2));
        assertEquals("revert:mov-legacy", service.movements.get(3));
    }

    @Test
    @DisplayName("RecordStockMovementCommand validates invariants, non-blank strings, and defensive date copying")
    void testRecordStockMovementCommandInvariants() {
        Date originalDate = new Date();
        assertThrows(NullPointerException.class, () -> RecordStockMovementCommand.of(
                null, originalDate, 1, "loc-1", "p-1", null, 10.0, 5.0, "admin"));
        assertThrows(IllegalArgumentException.class, () -> RecordStockMovementCommand.of(
                "   ", originalDate, 1, "loc-1", "p-1", null, 10.0, 5.0, "admin"));
        assertThrows(NullPointerException.class, () -> RecordStockMovementCommand.of(
                "mov-1", null, 1, "loc-1", "p-1", null, 10.0, 5.0, "admin"));
        assertThrows(NullPointerException.class, () -> RecordStockMovementCommand.of(
                "mov-1", originalDate, 1, null, "p-1", null, 10.0, 5.0, "admin"));
        assertThrows(IllegalArgumentException.class, () -> RecordStockMovementCommand.of(
                "mov-1", originalDate, 1, "   ", "p-1", null, 10.0, 5.0, "admin"));
        assertThrows(NullPointerException.class, () -> RecordStockMovementCommand.of(
                "mov-1", originalDate, 1, "loc-1", null, null, 10.0, 5.0, "admin"));
        assertThrows(IllegalArgumentException.class, () -> RecordStockMovementCommand.of(
                "mov-1", originalDate, 1, "loc-1", "   ", null, 10.0, 5.0, "admin"));
        assertThrows(NullPointerException.class, () -> RecordStockMovementCommand.of(
                "mov-1", originalDate, 1, "loc-1", "p-1", null, 10.0, 5.0, null));
        assertThrows(IllegalArgumentException.class, () -> RecordStockMovementCommand.of(
                "mov-1", originalDate, 1, "loc-1", "p-1", null, 10.0, 5.0, "   "));

        assertThrows(IllegalArgumentException.class, () -> RecordStockMovementCommand.of(
                "mov-1", originalDate, 1, "loc-1", "p-1", null, Double.NaN, 5.0, "admin"));
        assertThrows(IllegalArgumentException.class, () -> RecordStockMovementCommand.of(
                "mov-1", originalDate, 1, "loc-1", "p-1", null, 10.0, Double.POSITIVE_INFINITY, "admin"));

        // Defensive date copying
        Date datePassed = new Date(1700000000000L);
        RecordStockMovementCommand cmd = RecordStockMovementCommand.of(
                "  mov-1  ", datePassed, 1, "  loc-1  ", "  p-1  ", "   ", 10.0, 25.5, "  admin  ");
        datePassed.setTime(0L);
        assertNotEquals(0L, cmd.date().getTime());
        assertEquals(1700000000000L, cmd.date().getTime());

        // Getter defensive copy
        Date getterDate = cmd.date();
        getterDate.setTime(0L);
        assertEquals(1700000000000L, cmd.date().getTime());

        assertEquals("mov-1", cmd.id());
        assertEquals("loc-1", cmd.location());
        assertEquals("p-1", cmd.productId());
        assertNull(cmd.attSetInstId(), "Blank attSetInstId should normalize to null");
        assertEquals(10.0, cmd.units());
        assertEquals(25.5, cmd.price());
        assertEquals("admin", cmd.appUser());
    }

    @Test
    @DisplayName("RevertStockMovementCommand validates invariants and trims strings")
    void testRevertStockMovementCommandInvariants() {
        assertThrows(NullPointerException.class, () -> RevertStockMovementCommand.of(
                null, "loc-1", "p-1", null, 5.0));
        assertThrows(IllegalArgumentException.class, () -> RevertStockMovementCommand.of(
                "   ", "loc-1", "p-1", null, 5.0));
        assertThrows(NullPointerException.class, () -> RevertStockMovementCommand.of(
                "mov-1", null, "p-1", null, 5.0));
        assertThrows(IllegalArgumentException.class, () -> RevertStockMovementCommand.of(
                "mov-1", "   ", "p-1", null, 5.0));
        assertThrows(NullPointerException.class, () -> RevertStockMovementCommand.of(
                "mov-1", "loc-1", null, null, 5.0));
        assertThrows(IllegalArgumentException.class, () -> RevertStockMovementCommand.of(
                "mov-1", "loc-1", "   ", null, 5.0));
        assertThrows(IllegalArgumentException.class, () -> RevertStockMovementCommand.of(
                "mov-1", "loc-1", "p-1", null, Double.NaN));

        RevertStockMovementCommand cmd = RevertStockMovementCommand.of(
                "  mov-1  ", "  loc-1  ", "  p-1  ", "  attr-1  ", 5.0);
        assertEquals("mov-1", cmd.diaryId());
        assertEquals("loc-1", cmd.location());
        assertEquals("p-1", cmd.productId());
        assertEquals("attr-1", cmd.attSetInstId());
        assertEquals(5.0, cmd.units());
    }

    @Test
    @DisplayName("DataLogicInventory persists RecordStockMovementCommand and RevertStockMovementCommand in HSQLDB")
    void testStockServiceHsqldbPersistence() throws Exception {
        String databaseName = "stock_db_" + UUID.randomUUID().toString().replace("-", "");
        Session session = new Session("jdbc:hsqldb:mem:" + databaseName + ";sql.syntax_mys=true", "SA", "");
        try {
            try (Statement statement = session.getConnection().createStatement()) {
                statement.executeUpdate("CREATE TABLE products ("
                        + "ID VARCHAR(36) PRIMARY KEY, "
                        + "ISCOM BOOLEAN NOT NULL)");
                statement.executeUpdate("CREATE TABLE products_bundle ("
                        + "ID VARCHAR(36) PRIMARY KEY, "
                        + "PRODUCT VARCHAR(36) NOT NULL, "
                        + "PRODUCT_BUNDLE VARCHAR(36) NOT NULL, "
                        + "QUANTITY DOUBLE NOT NULL)");
                statement.executeUpdate("CREATE TABLE stockcurrent ("
                        + "LOCATION VARCHAR(36) NOT NULL, "
                        + "PRODUCT VARCHAR(36) NOT NULL, "
                        + "ATTRIBUTESETINSTANCE_ID VARCHAR(36), "
                        + "UNITS DOUBLE NOT NULL)");
                statement.executeUpdate("CREATE TABLE stockdiary ("
                        + "ID VARCHAR(36) PRIMARY KEY, "
                        + "DATENEW TIMESTAMP NOT NULL, "
                        + "REASON INTEGER NOT NULL, "
                        + "LOCATION VARCHAR(36) NOT NULL, "
                        + "PRODUCT VARCHAR(36) NOT NULL, "
                        + "ATTRIBUTESETINSTANCE_ID VARCHAR(36), "
                        + "UNITS DOUBLE NOT NULL, "
                        + "PRICE DOUBLE NOT NULL, "
                        + "AppUser VARCHAR(255) NOT NULL)");

                statement.executeUpdate("INSERT INTO products (ID, ISCOM) VALUES ('p-100', FALSE)");
            }

            DataLogicInventory dlInventory = new DataLogicInventory();
            dlInventory.init(session);

            Date now = new Date();
            RecordStockMovementCommand recordCmd = RecordStockMovementCommand.of(
                    "mov-100", now, 1, "loc-main", "p-100", null, 15.0, 10.5, "manager");
            dlInventory.recordStockMovement(recordCmd);

            // Verify stockdiary record
            try (PreparedStatement query = session.getConnection().prepareStatement(
                    "SELECT ID, REASON, LOCATION, PRODUCT, UNITS, PRICE, AppUser FROM stockdiary WHERE ID = ?")) {
                query.setString(1, "mov-100");
                try (ResultSet rs = query.executeQuery()) {
                    assertTrue(rs.next());
                    assertEquals("mov-100", rs.getString("ID"));
                    assertEquals(1, rs.getInt("REASON"));
                    assertEquals("loc-main", rs.getString("LOCATION"));
                    assertEquals("p-100", rs.getString("PRODUCT"));
                    assertEquals(15.0, rs.getDouble("UNITS"));
                    assertEquals(10.5, rs.getDouble("PRICE"));
                    assertEquals("manager", rs.getString("AppUser"));
                    assertFalse(rs.next());
                }
            }

            // Verify stockcurrent balance adjusted
            try (PreparedStatement query = session.getConnection().prepareStatement(
                    "SELECT UNITS FROM stockcurrent WHERE LOCATION = ? AND PRODUCT = ?")) {
                query.setString(1, "loc-main");
                query.setString(2, "p-100");
                try (ResultSet rs = query.executeQuery()) {
                    assertTrue(rs.next());
                    assertEquals(15.0, rs.getDouble("UNITS"));
                    assertFalse(rs.next());
                }
            }

            // Revert movement
            RevertStockMovementCommand revertCmd = RevertStockMovementCommand.of(
                    "mov-100", "loc-main", "p-100", null, 15.0);
            dlInventory.revertStockMovement(revertCmd);

            // Verify stockdiary record deleted
            try (PreparedStatement query = session.getConnection().prepareStatement(
                    "SELECT ID FROM stockdiary WHERE ID = ?")) {
                query.setString(1, "mov-100");
                try (ResultSet rs = query.executeQuery()) {
                    assertFalse(rs.next(), "Diary record should be deleted");
                }
            }

            // Verify stockcurrent balance updated to 0.0
            try (PreparedStatement query = session.getConnection().prepareStatement(
                    "SELECT UNITS FROM stockcurrent WHERE LOCATION = ? AND PRODUCT = ?")) {
                query.setString(1, "loc-main");
                query.setString(2, "p-100");
                try (ResultSet rs = query.executeQuery()) {
                    assertTrue(rs.next());
                    assertEquals(0.0, rs.getDouble("UNITS"));
                    assertFalse(rs.next());
                }
            }
        } finally {
            session.close();
        }
    }

    @Test
    @DisplayName("StockAdjustmentCommand validates non-null and non-blank invariants")
    void testStockAdjustmentCommandInvariants() {
        assertThrows(IllegalArgumentException.class, () -> new StockAdjustmentCommand(null, "loc-1", null, 10.0));
        assertThrows(IllegalArgumentException.class, () -> new StockAdjustmentCommand("", "loc-1", null, 10.0));
        assertThrows(IllegalArgumentException.class, () -> new StockAdjustmentCommand("p-1", null, null, 10.0));
        assertThrows(IllegalArgumentException.class, () -> new StockAdjustmentCommand("p-1", "   ", null, 10.0));

        StockAdjustmentCommand cmd = StockAdjustmentCommand.of("p-1", "loc-1", 15.5);
        assertEquals("p-1", cmd.productId());
        assertEquals("loc-1", cmd.locationId());
        assertNull(cmd.attributeSetInstanceId());
        assertEquals(15.5, cmd.units());
    }

    @Test
    @DisplayName("StockService adjustStock with StockAdjustmentCommand dispatches correctly")
    void testAdjustStockCommandDispatch() throws BasicException {
        MockStockService service = new MockStockService();
        StockAdjustmentCommand cmd = new StockAdjustmentCommand("p-100", "loc-warehouse", "attr-9", -3.0);

        service.adjustStock(cmd);

        assertEquals(1, service.adjustments.size());
        StockAdjustmentCommand recorded = service.adjustments.get(0);
        assertEquals("p-100", recorded.productId());
        assertEquals("loc-warehouse", recorded.locationId());
        assertEquals("attr-9", recorded.attributeSetInstanceId());
        assertEquals(-3.0, recorded.units());
    }

    @Test
    @DisplayName("StockService adjustStock legacy array adapter delegates to typed method")
    void testAdjustStockLegacyAdapter() throws BasicException {
        MockStockService service = new MockStockService();
        Object[] legacyParams = new Object[]{"p-200", "loc-shelf", "attr-2", 8.0};

        service.adjustStock(legacyParams);

        assertEquals(1, service.adjustments.size());
        StockAdjustmentCommand recorded = service.adjustments.get(0);
        assertEquals("p-200", recorded.productId());
        assertEquals("loc-shelf", recorded.locationId());
        assertEquals("attr-2", recorded.attributeSetInstanceId());
        assertEquals(8.0, recorded.units());
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
