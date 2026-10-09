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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StockEntry Domain Record Test Suite")
class StockEntryTest {

    @Test
    @DisplayName("Should create StockEntry with full attributes")
    void testFullStockEntry() {
        StockEntry entry = new StockEntry("loc-1", "prod-1", "att-1", 15.5);

        assertEquals("loc-1", entry.locationId());
        assertEquals("prod-1", entry.productId());
        assertEquals("att-1", entry.attributeSetInstanceId());
        assertEquals(15.5, entry.units());
    }

    @Test
    @DisplayName("Should create StockEntry with convenience constructor without attribute instance")
    void testConvenienceConstructor() {
        StockEntry entry = new StockEntry("loc-2", "prod-2", 42.0);

        assertEquals("loc-2", entry.locationId());
        assertEquals("prod-2", entry.productId());
        assertNull(entry.attributeSetInstanceId());
        assertEquals(42.0, entry.units());
    }

    @Test
    @DisplayName("Should support equality and hashcode based on values")
    void testEqualityAndHashCode() {
        StockEntry entry1 = new StockEntry("loc-1", "prod-1", "att-1", 10.0);
        StockEntry entry2 = new StockEntry("loc-1", "prod-1", "att-1", 10.0);
        StockEntry entry3 = new StockEntry("loc-2", "prod-1", "att-1", 10.0);

        assertEquals(entry1, entry2);
        assertEquals(entry1.hashCode(), entry2.hashCode());
        assertNotEquals(entry1, entry3);
    }
}
