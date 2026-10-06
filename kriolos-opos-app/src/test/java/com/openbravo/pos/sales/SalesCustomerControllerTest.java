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
import com.openbravo.pos.ticket.TicketInfo;
import java.util.Optional;
import javax.swing.JPanel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SalesCustomerController Unit Tests")
public class SalesCustomerControllerTest {

    private SalesCustomerController customerController;
    private TicketInfo ticket;

    @BeforeEach
    void setUp() {
        customerController = new SalesCustomerController(null, null);
        ticket = new TicketInfo();
    }

    @Test
    @DisplayName("Should return empty optional when ticket is null")
    void testSelectCustomerNullTicket() {
        Optional<CustomerInfoExt> result = customerController.selectCustomer(new JPanel(), null);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should not throw when checking discount on null ticket or non-vip customer")
    void testCheckDiscountSafe() {
        assertDoesNotThrow(() -> customerController.checkAndShowCustomerDiscount(new JPanel(), null));

        CustomerInfoExt normalCustomer = new CustomerInfoExt("c-01");
        normalCustomer.setName("Alice");
        normalCustomer.setisVIP(false);
        ticket.setCustomer(normalCustomer);

        assertDoesNotThrow(() -> customerController.checkAndShowCustomerDiscount(new JPanel(), ticket));
    }
}
