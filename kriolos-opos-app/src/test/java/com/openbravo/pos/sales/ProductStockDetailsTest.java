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

import java.util.Date;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ProductStockDetails and ProductStockInfoPanel Tests")
public class ProductStockDetailsTest {

    @Test
    @DisplayName("Should create ProductStockDetails record with default non-null values for numeric metrics")
    void testRecordDefaults() {
        ProductStockDetails details = new ProductStockDetails(null, null, null, null, null, null, null, null, null, null);
        assertEquals(0.0, details.units());
        assertEquals(0.0, details.minimum());
        assertEquals(0.0, details.maximum());
        assertNull(details.productName());
        assertNull(details.categoryName());
        assertNull(details.reference());
        assertNull(details.barcode());
        assertNull(details.locationName());
        assertNull(details.priceSell());
        assertNull(details.memoDate());
    }

    @Test
    @DisplayName("Should create ProductStockDetails record with rich values")
    void testRichRecordValues() {
        Date now = new Date();
        ProductStockDetails details = new ProductStockDetails(
                "Espresso",
                "Beverages",
                "ESP-01",
                "1234567890",
                "Main Bar",
                25.5,
                5.0,
                100.0,
                3.50,
                now
        );

        assertEquals("Espresso", details.productName());
        assertEquals("Beverages", details.categoryName());
        assertEquals("ESP-01", details.reference());
        assertEquals("1234567890", details.barcode());
        assertEquals("Main Bar", details.locationName());
        assertEquals(25.5, details.units());
        assertEquals(5.0, details.minimum());
        assertEquals(100.0, details.maximum());
        assertEquals(3.50, details.priceSell());
        assertEquals(now, details.memoDate());
    }

    @Test
    @DisplayName("Should initialize ProductStockInfoPanel component hierarchy in LAF-agnostic manner")
    void testPanelComponentHierarchy() {
        ProductStockDetails details = new ProductStockDetails(
                "Croissant",
                "Bakery",
                "CR-02",
                "9876543210",
                "Kitchen Store",
                12.0,
                4.0,
                40.0,
                2.20,
                new Date()
        );

        ProductStockInfoPanel panel = new ProductStockInfoPanel(details);
        assertEquals("kriolos:sales:product_stock_info", panel.getName());
        assertEquals(details, panel.getDetails());
        assertTrue(panel.getComponentCount() > 0);
    }
}
