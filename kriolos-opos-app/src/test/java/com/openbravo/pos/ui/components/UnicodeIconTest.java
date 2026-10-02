/*
 * Copyright (C) 2026 KriolOS
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
package com.openbravo.pos.ui.components;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UnicodeIconTest {

    @Test
    @DisplayName("Verify all UnicodeIcon enum entries have non-empty code, name, and toString")
    void testEnumCompleteness() {
        UnicodeIcon[] icons = UnicodeIcon.values();
        assertTrue(icons.length >= 15, "Expected comprehensive list of icons");

        for (UnicodeIcon icon : icons) {
            assertNotNull(icon.getCode(), "Icon code must not be null for " + icon.name());
            assertFalse(icon.getCode().isBlank(), "Icon code must not be blank for " + icon.name());

            assertNotNull(icon.getName(), "Icon name must not be null for " + icon.name());
            assertFalse(icon.getName().isBlank(), "Icon name must not be blank for " + icon.name());

            assertEquals(icon.getCode(), icon.toString(), "toString() must return the Unicode code glyph");
        }
    }

    @Test
    @DisplayName("Verify specific core icon codes and names")
    void testSpecificIcons() {
        assertEquals("\uD83D\uDC64", UnicodeIcon.CUSTOMER.getCode());
        assertEquals("Customer", UnicodeIcon.CUSTOMER.getName());

        assertEquals("\uD83D\uDCCB", UnicodeIcon.ORDERS.getCode());
        assertEquals("Orders", UnicodeIcon.ORDERS.getName());

        assertEquals("\uD83D\uDCB3", UnicodeIcon.PAY.getCode());
        assertEquals("Pay", UnicodeIcon.PAY.getName());

        assertEquals("\uD83D\uDDD1", UnicodeIcon.DELETE.getCode());
        assertEquals("Delete", UnicodeIcon.DELETE.getName());

        assertEquals("\u23F8", UnicodeIcon.HOLD.getCode());
        assertEquals("Hold", UnicodeIcon.HOLD.getName());
    }

    @Test
    @DisplayName("Verify fromCode lookup matches correctly")
    void testFromCode() {
        assertSame(UnicodeIcon.PAY, UnicodeIcon.fromCode("\uD83D\uDCB3"));
        assertSame(UnicodeIcon.CUSTOMER, UnicodeIcon.fromCode("\uD83D\uDC64"));
        assertEquals(null, UnicodeIcon.fromCode("non-existent"));
        assertEquals(null, UnicodeIcon.fromCode(null));
    }

    @Test
    @DisplayName("Verify enumeration via values()")
    void testEnumeration() {
        boolean foundPay = false;
        boolean foundCustomer = false;
        boolean foundDelete = false;

        for (UnicodeIcon icon : UnicodeIcon.values()) {
            if (icon == UnicodeIcon.PAY) foundPay = true;
            if (icon == UnicodeIcon.CUSTOMER) foundCustomer = true;
            if (icon == UnicodeIcon.DELETE) foundDelete = true;
        }

        assertTrue(foundPay);
        assertTrue(foundCustomer);
        assertTrue(foundDelete);
    }
}
