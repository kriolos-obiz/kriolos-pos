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

import com.openbravo.pos.customers.CustomerInfoExt;
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
    @DisplayName("Should initialize CustomerDiscountInfoPanel directly from CustomerInfoExt")
    void testPanelInitWithCustomer() {
        CustomerInfoExt customer = new CustomerInfoExt("cust-001");
        customer.setName("Jane Doe");
        customer.setTaxid("987654321");
        customer.setCard("CARD-001");
        customer.setisVIP(true);
        customer.setDiscount(15.0);
        customer.setMaxdebt(500.0);
        customer.setCurDebt(50.0);
        customer.setNotes("Preferred VIP client");

        CustomerDiscountInfoPanel panel = new CustomerDiscountInfoPanel(customer);
        assertEquals("kriolos:sales:customer_discount_info", panel.getName());
        assertSame(customer, panel.getCustomer());
        assertEquals("Jane Doe", panel.getCustomer().getName());
        assertEquals(15.0, panel.getCustomer().getDiscount());
        assertTrue(panel.getCustomer().isVIP());
        assertTrue(panel.getComponentCount() > 0);
    }
}
