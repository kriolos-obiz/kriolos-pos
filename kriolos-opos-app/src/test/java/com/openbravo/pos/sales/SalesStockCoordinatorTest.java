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

import com.openbravo.basic.BasicException;
import com.openbravo.pos.inventory.InventoryService;
import com.openbravo.pos.inventory.ProductStock;
import com.openbravo.pos.ticket.TicketLineInfo;
import java.util.Date;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SalesStockCoordinator Test Suite")
class SalesStockCoordinatorTest {

    private static class StubInventoryService implements InventoryService {
        private ProductStock stockToReturn;
        private boolean shouldThrow;

        StubInventoryService(ProductStock stockToReturn) {
            this.stockToReturn = stockToReturn;
        }

        StubInventoryService(boolean shouldThrow) {
            this.shouldThrow = shouldThrow;
        }

        @Override
        public ProductStock getStock(String productId, String locationId) throws BasicException {
            if (shouldThrow) {
                throw new BasicException("Simulated inventory failure");
            }
            return stockToReturn;
        }

        @Override
        public void createStock(String locationId, String productId, double units) {
        }

        @Override
        public void updateStock(String locationId, String productId, double units) {
        }
    }

    @Test
    @DisplayName("isStockAvailable returns true when location matches and units > 0")
    void testIsStockAvailable_Success() {
        ProductStock stock = new ProductStock("PROD-1", "LOC-MAIN", 15.0, 2.0, 50.0, 5.0, 10.0, new Date());
        SalesStockCoordinator coordinator = new SalesStockCoordinator(new StubInventoryService(stock), null, null);

        TicketLineInfo line = new TicketLineInfo("PROD-1", "Product One", "tax-1", 1.0, 10.0, null);

        assertTrue(coordinator.isStockAvailable(line, "LOC-MAIN"));
    }

    @Test
    @DisplayName("isStockAvailable returns false when units <= 0")
    void testIsStockAvailable_ZeroOrNegativeUnits() {
        ProductStock zeroStock = new ProductStock("PROD-1", "LOC-MAIN", 0.0, 2.0, 50.0, 5.0, 10.0, new Date());
        SalesStockCoordinator coordinator = new SalesStockCoordinator(new StubInventoryService(zeroStock), null, null);

        TicketLineInfo line = new TicketLineInfo("PROD-1", "Product One", "tax-1", 1.0, 10.0, null);

        assertFalse(coordinator.isStockAvailable(line, "LOC-MAIN"));
    }

    @Test
    @DisplayName("isStockAvailable returns false when location does not match")
    void testIsStockAvailable_LocationMismatch() {
        ProductStock stock = new ProductStock("PROD-1", "LOC-WAREHOUSE", 10.0, 2.0, 50.0, 5.0, 10.0, new Date());
        SalesStockCoordinator coordinator = new SalesStockCoordinator(new StubInventoryService(stock), null, null);

        TicketLineInfo line = new TicketLineInfo("PROD-1", "Product One", "tax-1", 1.0, 10.0, null);

        assertFalse(coordinator.isStockAvailable(line, "LOC-MAIN"));
    }

    @Test
    @DisplayName("isStockAvailable handles null line or missing product ID gracefully")
    void testIsStockAvailable_NullInputs() {
        SalesStockCoordinator coordinator = new SalesStockCoordinator(new StubInventoryService((ProductStock) null), null, null);

        assertFalse(coordinator.isStockAvailable(null, "LOC-MAIN"));

        TicketLineInfo lineWithoutProd = new TicketLineInfo();
        assertFalse(coordinator.isStockAvailable(lineWithoutProd, "LOC-MAIN"));
    }

    @Test
    @DisplayName("isStockAvailable returns false on inventory exception")
    void testIsStockAvailable_ExceptionHandling() {
        SalesStockCoordinator coordinator = new SalesStockCoordinator(new StubInventoryService(true), null, null);

        TicketLineInfo line = new TicketLineInfo("PROD-1", "Product One", "tax-1", 1.0, 10.0, null);
        assertFalse(coordinator.isStockAvailable(line, "LOC-MAIN"));
    }

    @Test
    @DisplayName("inspectStock returns details record when stock matches location")
    void testInspectStock_Success() {
        Date memoDate = new Date();
        ProductStock stock = new ProductStock("PROD-1", "LOC-MAIN", 25.0, 5.0, 100.0, 8.0, 14.50, memoDate);
        SalesStockCoordinator coordinator = new SalesStockCoordinator(new StubInventoryService(stock), null, null);

        TicketLineInfo line = new TicketLineInfo("PROD-1", "Product One", "tax-1", 2.0, 12.00, null);

        Optional<ProductStockDetails> details = coordinator.inspectStock(line, "LOC-MAIN");

        assertTrue(details.isPresent());
        ProductStockDetails d = details.get();
        assertEquals("Product One", d.productName());
        assertEquals("LOC-MAIN", d.locationName());
        assertEquals(25.0, d.units());
        assertEquals(5.0, d.minimum());
        assertEquals(100.0, d.maximum());
        assertEquals(14.50, d.priceSell());
        assertEquals(memoDate, d.memoDate());
    }

    @Test
    @DisplayName("inspectStock returns empty Optional when stock is missing or location mismatch")
    void testInspectStock_MismatchOrMissing() {
        ProductStock stock = new ProductStock("PROD-1", "OTHER-LOC", 25.0, 5.0, 100.0, 8.0, 14.50, new Date());
        SalesStockCoordinator coordinator = new SalesStockCoordinator(new StubInventoryService(stock), null, null);

        TicketLineInfo line = new TicketLineInfo("PROD-1", "Product One", "tax-1", 1.0, 10.0, null);

        assertTrue(coordinator.inspectStock(line, "LOC-MAIN").isEmpty());
    }

    @Test
    @DisplayName("showStockDetails returns Optional details when parent is null (headless)")
    void testShowStockDetails_Headless() {
        ProductStock stock = new ProductStock("PROD-1", "LOC-MAIN", 8.0, 1.0, 20.0, 5.0, 12.0, new Date());
        SalesStockCoordinator coordinator = new SalesStockCoordinator(new StubInventoryService(stock), null, null);

        TicketLineInfo line = new TicketLineInfo("PROD-1", "Product One", "tax-1", 1.0, 12.0, null);

        Optional<ProductStockDetails> result = coordinator.showStockDetails(null, line, "LOC-MAIN");
        assertTrue(result.isPresent());
        assertEquals(8.0, result.get().units());
    }

    @Test
    @DisplayName("checkAndShowStock returns empty Optional when ticket is null or index is out of bounds")
    void testCheckAndShowStock_InvalidIndexOrTicket() {
        SalesStockCoordinator coordinator = new SalesStockCoordinator(new StubInventoryService((ProductStock) null), null, null);

        assertTrue(coordinator.checkAndShowStock(null, null, 0, "LOC-MAIN", false).isEmpty());

        com.openbravo.pos.ticket.TicketInfo ticket = new com.openbravo.pos.ticket.TicketInfo();
        assertTrue(coordinator.checkAndShowStock(null, ticket, -1, "LOC-MAIN", false).isEmpty());
        assertTrue(coordinator.checkAndShowStock(null, ticket, 0, "LOC-MAIN", false).isEmpty());
    }

    @Test
    @DisplayName("checkAndShowStock returns stock availability for valid ticket line index")
    void testCheckAndShowStock_ValidIndex() {
        ProductStock stock = new ProductStock("PROD-1", "LOC-MAIN", 10.0, 1.0, 20.0, 5.0, 12.0, new Date());
        SalesStockCoordinator coordinator = new SalesStockCoordinator(new StubInventoryService(stock), null, null);

        com.openbravo.pos.ticket.TicketInfo ticket = new com.openbravo.pos.ticket.TicketInfo();
        ticket.addLine(new TicketLineInfo("PROD-1", "Product One", "tax-1", 1.0, 12.0, null));

        Optional<Boolean> inStock = coordinator.checkAndShowStock(null, ticket, 0, "LOC-MAIN", false);
        assertTrue(inStock.isPresent());
        assertTrue(inStock.get());

        Optional<Boolean> available = coordinator.isStockAvailable(false, ticket, 0, "LOC-MAIN");
        assertTrue(available.isPresent());
        assertTrue(available.get());
    }
}
