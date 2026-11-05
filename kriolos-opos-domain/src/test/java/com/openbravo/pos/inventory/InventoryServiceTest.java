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
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("InventoryService Port & Stock Commands Contract Test Suite")
class InventoryServiceTest {

    @Test
    @DisplayName("CreateStockCommand should validate invariants and trim inputs")
    void shouldValidateCreateStockCommandInvariants() {
        assertThrows(NullPointerException.class, () -> new CreateStockCommand(null, "prod-1", null, 10.0));
        assertThrows(IllegalArgumentException.class, () -> new CreateStockCommand("", "prod-1", null, 10.0));
        assertThrows(IllegalArgumentException.class, () -> new CreateStockCommand("   ", "prod-1", null, 10.0));

        assertThrows(NullPointerException.class, () -> new CreateStockCommand("loc-1", null, null, 10.0));
        assertThrows(IllegalArgumentException.class, () -> new CreateStockCommand("loc-1", "", null, 10.0));
        assertThrows(IllegalArgumentException.class, () -> new CreateStockCommand("loc-1", "   ", null, 10.0));

        assertThrows(IllegalArgumentException.class, () -> new CreateStockCommand("loc-1", "prod-1", null, Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> new CreateStockCommand("loc-1", "prod-1", null, Double.POSITIVE_INFINITY));
        assertThrows(IllegalArgumentException.class, () -> new CreateStockCommand("loc-1", "prod-1", null, Double.NEGATIVE_INFINITY));

        CreateStockCommand cmd = CreateStockCommand.of("  loc-1  ", "  prod-1  ", 42.5);
        assertEquals("loc-1", cmd.locationId());
        assertEquals("prod-1", cmd.productId());
        assertNull(cmd.attributeSetInstanceId());
        assertEquals(42.5, cmd.units());

        CreateStockCommand cmdWithAttr = CreateStockCommand.of("loc-2", "prod-2", "  attr-99  ", 12.0);
        assertEquals("attr-99", cmdWithAttr.attributeSetInstanceId());

        CreateStockCommand cmdEmptyAttr = CreateStockCommand.of("loc-2", "prod-2", "   ", 12.0);
        assertNull(cmdEmptyAttr.attributeSetInstanceId(), "Blank attribute should normalize to null");
    }

    @Test
    @DisplayName("UpdateStockCommand should validate invariants and trim inputs")
    void shouldValidateUpdateStockCommandInvariants() {
        assertThrows(NullPointerException.class, () -> new UpdateStockCommand(null, "prod-1", null, 10.0));
        assertThrows(IllegalArgumentException.class, () -> new UpdateStockCommand("", "prod-1", null, 10.0));
        assertThrows(IllegalArgumentException.class, () -> new UpdateStockCommand("   ", "prod-1", null, 10.0));

        assertThrows(NullPointerException.class, () -> new UpdateStockCommand("loc-1", null, null, 10.0));
        assertThrows(IllegalArgumentException.class, () -> new UpdateStockCommand("loc-1", "", null, 10.0));
        assertThrows(IllegalArgumentException.class, () -> new UpdateStockCommand("loc-1", "   ", null, 10.0));

        assertThrows(IllegalArgumentException.class, () -> new UpdateStockCommand("loc-1", "prod-1", null, Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> new UpdateStockCommand("loc-1", "prod-1", null, Double.POSITIVE_INFINITY));

        UpdateStockCommand cmd = UpdateStockCommand.of("  loc-warehouse  ", "  prod-A  ", 100.0);
        assertEquals("loc-warehouse", cmd.locationId());
        assertEquals("prod-A", cmd.productId());
        assertNull(cmd.attributeSetInstanceId());
        assertEquals(100.0, cmd.units());

        UpdateStockCommand cmdWithAttr = UpdateStockCommand.of("loc-warehouse", "prod-A", "lot-123", 95.0);
        assertEquals("lot-123", cmdWithAttr.attributeSetInstanceId());
        assertEquals(95.0, cmdWithAttr.units());
    }

    @Test
    @DisplayName("InventoryService default methods should delegate correctly to commands")
    void shouldDelegateDefaultMethodsToCommands() throws BasicException {
        final List<CreateStockCommand> createCalls = new ArrayList<>();
        final List<UpdateStockCommand> updateCalls = new ArrayList<>();

        InventoryService testService = new InventoryService() {
            @Override
            public ProductStock getStock(String productId, String locationId) {
                return null;
            }

            @Override
            public void createStock(CreateStockCommand command) {
                createCalls.add(command);
            }

            @Override
            public void updateStock(UpdateStockCommand command) {
                updateCalls.add(command);
            }
        };

        // Legacy 3-arg createStock
        testService.createStock("loc-1", "prod-1", 10.0);
        assertEquals(1, createCalls.size());
        assertEquals("loc-1", createCalls.get(0).locationId());
        assertEquals("prod-1", createCalls.get(0).productId());
        assertEquals(10.0, createCalls.get(0).units());

        // Legacy 3-arg updateStock
        testService.updateStock("loc-1", "prod-1", 15.0);
        assertEquals(1, updateCalls.size());
        assertEquals("loc-1", updateCalls.get(0).locationId());
        assertEquals("prod-1", updateCalls.get(0).productId());
        assertEquals(15.0, updateCalls.get(0).units());

        // addStockEntry with attribute
        testService.addStockEntry("loc-2", "prod-2", "attr-X", 20.0);
        assertEquals(2, createCalls.size());
        assertEquals("loc-2", createCalls.get(1).locationId());
        assertEquals("prod-2", createCalls.get(1).productId());
        assertEquals("attr-X", createCalls.get(1).attributeSetInstanceId());
        assertEquals(20.0, createCalls.get(1).units());

        // StockEntry record dispatch
        testService.addStockEntry(new StockEntry("loc-3", "prod-3", "attr-Y", 30.0));
        assertEquals(3, createCalls.size());
        assertEquals("loc-3", createCalls.get(2).locationId());
        assertEquals("prod-3", createCalls.get(2).productId());
        assertEquals("attr-Y", createCalls.get(2).attributeSetInstanceId());
        assertEquals(30.0, createCalls.get(2).units());
    }

    @Test
    @DisplayName("InventoryServiceImpl should execute CreateStockCommand and UpdateStockCommand against HSQLDB")
    void shouldExecuteCommandsAgainstDatabase() throws Exception {
        String dbName = "inv_db_" + UUID.randomUUID().toString().replace("-", "");
        Session session = new Session("jdbc:hsqldb:mem:" + dbName + ";sql.syntax_mys=true", "SA", "");

        try (Statement stmt = session.getConnection().createStatement()) {
            stmt.executeUpdate("CREATE TABLE stockcurrent ("
                    + "LOCATION VARCHAR(255) NOT NULL, "
                    + "PRODUCT VARCHAR(255) NOT NULL, "
                    + "ATTRIBUTESETINSTANCE_ID VARCHAR(255), "
                    + "UNITS DOUBLE NOT NULL)");
        }

        InventoryService service = new InventoryServiceImpl((DataLogicInventory) null, session);

        // 1. Create stock without attribute
        service.createStock(CreateStockCommand.of("loc-store", "prod-apple", 50.0));

        try (Statement stmt = session.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT UNITS, ATTRIBUTESETINSTANCE_ID FROM stockcurrent WHERE LOCATION = 'loc-store' AND PRODUCT = 'prod-apple'")) {
            assertTrue(rs.next(), "Row should exist in stockcurrent");
            assertEquals(50.0, rs.getDouble("UNITS"), 0.001);
            assertNull(rs.getString("ATTRIBUTESETINSTANCE_ID"));
        }

        // 2. Create stock with attribute
        service.createStock(CreateStockCommand.of("loc-store", "prod-banana", "lot-001", 30.0));

        try (Statement stmt = session.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT UNITS, ATTRIBUTESETINSTANCE_ID FROM stockcurrent WHERE LOCATION = 'loc-store' AND PRODUCT = 'prod-banana'")) {
            assertTrue(rs.next(), "Row with attribute should exist");
            assertEquals(30.0, rs.getDouble("UNITS"), 0.001);
            assertEquals("lot-001", rs.getString("ATTRIBUTESETINSTANCE_ID"));
        }

        // 3. Update stock without attribute
        service.updateStock(UpdateStockCommand.of("loc-store", "prod-apple", 75.0));

        try (Statement stmt = session.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT UNITS FROM stockcurrent WHERE LOCATION = 'loc-store' AND PRODUCT = 'prod-apple'")) {
            assertTrue(rs.next());
            assertEquals(75.0, rs.getDouble("UNITS"), 0.001);
        }

        // 4. Update stock with attribute
        service.updateStock(UpdateStockCommand.of("loc-store", "prod-banana", "lot-001", 10.0));

        try (Statement stmt = session.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT UNITS FROM stockcurrent WHERE LOCATION = 'loc-store' AND PRODUCT = 'prod-banana' AND ATTRIBUTESETINSTANCE_ID = 'lot-001'")) {
            assertTrue(rs.next());
            assertEquals(10.0, rs.getDouble("UNITS"), 0.001);
        }

        // 5. Null command safety (should not fail)
        service.createStock((CreateStockCommand) null);
        service.updateStock((UpdateStockCommand) null);
    }
}
