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
package com.openbravo.pos.payment;

import java.util.Date;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PaymentMovement Domain Record Tests")
class PaymentMovementTest {

    @Test
    @DisplayName("Should create PaymentMovement with provided values")
    void shouldCreatePaymentMovementWithProvidedValues() {
        Date now = new Date();
        PaymentMovement movement = new PaymentMovement(
                "rec-001",
                "cash-seq-1",
                now,
                "pay-001",
                "cashin",
                150.50,
                "Initial cash float"
        );

        assertEquals("rec-001", movement.receiptId());
        assertEquals("cash-seq-1", movement.activeCashIndex());
        assertEquals(now, movement.date());
        assertEquals("pay-001", movement.paymentId());
        assertEquals("cashin", movement.reason());
        assertEquals(150.50, movement.total(), 0.001);
        assertEquals("Initial cash float", movement.notes());
    }

    @Test
    @DisplayName("Should apply defaults for null date and notes")
    void shouldApplyDefaultsForNullDateAndNotes() {
        PaymentMovement movement = new PaymentMovement(
                "rec-002",
                "cash-seq-2",
                null,
                "pay-002",
                "cashout",
                -50.00,
                null
        );

        assertNotNull(movement.date());
        assertEquals("", movement.notes());
        assertEquals(-50.00, movement.total(), 0.001);
    }
}
