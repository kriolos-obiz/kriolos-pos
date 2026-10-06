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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CustomerDiscountInfoPanel Unit Tests")
public class CustomerDiscountInfoPanelTest {

    @Test
    @DisplayName("Should initialize CustomerDiscountInfoPanel with legacy parameters")
    void testPanelInitLegacy() {
        CustomerDiscountInfoPanel panel = new CustomerDiscountInfoPanel("Yes", "10%");
        assertEquals("kriolos:sales:customer_discount_info", panel.getName());
        assertTrue(panel.getComponentCount() > 0);
    }

    @Test
    @DisplayName("Should initialize CustomerDiscountInfoPanel with CustomerDiscountDetails record")
    void testPanelInitWithRecord() {
        CustomerDiscountDetails details = new CustomerDiscountDetails(
                "Jane Doe",
                "987654321",
                "CARD-001",
                true,
                15.0,
                50.0,
                500.0,
                "Preferred VIP client"
        );

        CustomerDiscountInfoPanel panel = new CustomerDiscountInfoPanel(details);
        assertEquals("kriolos:sales:customer_discount_info", panel.getName());
        assertEquals(details, panel.getDetails());
        assertEquals("Jane Doe", panel.getDetails().customerName());
        assertEquals(15.0, panel.getDetails().discountPercent());
        assertTrue(panel.getDetails().vip());
        assertTrue(panel.getComponentCount() > 0);
    }
}
