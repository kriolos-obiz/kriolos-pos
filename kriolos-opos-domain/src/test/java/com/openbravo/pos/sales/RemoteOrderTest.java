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
package com.openbravo.pos.sales;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

class RemoteOrderTest {

    @Test
    @DisplayName("Should create RemoteOrder record with double qty and String displayId")
    void testRecordCreation() {
        RemoteOrder order = new RemoteOrder(
                "ORD-101",
                2.5,
                "Burger",
                "Medium Rare",
                "No onions",
                "TCK-55",
                "1700000000",
                "KITCHEN_MAIN",
                "0",
                null
        );

        assertEquals("ORD-101", order.orderId());
        assertEquals(2.5, order.qty());
        assertEquals("Burger", order.details());
        assertEquals("Medium Rare", order.attributes());
        assertEquals("No onions", order.notes());
        assertEquals("TCK-55", order.ticketId());
        assertEquals("1700000000", order.ordertime());
        assertEquals("KITCHEN_MAIN", order.displayId());
        assertEquals("0", order.auxiliary());
        assertNull(order.completetime());
    }

    @Test
    @DisplayName("Should create RemoteOrder via factory with String displayId")
    void testFactoryWithStringDisplayId() {
        RemoteOrder order = RemoteOrder.of(
                "ORD-102",
                1.75,
                "Espresso",
                "Double",
                "Extra hot",
                "TCK-56",
                "BAR_STATION"
        );

        assertEquals("ORD-102", order.orderId());
        assertEquals(1.75, order.qty());
        assertEquals("BAR_STATION", order.displayId());
        assertNull(order.ordertime());
        assertNull(order.auxiliary());
        assertNull(order.completetime());
    }

    @Test
    @DisplayName("Should create RemoteOrder via backwards-compatible factory with Integer displayId")
    void testFactoryWithIntegerDisplayId() {
        RemoteOrder order = RemoteOrder.of(
                "ORD-103",
                3.0,
                "Salad",
                null,
                null,
                "TCK-57",
                Integer.valueOf(2)
        );

        assertEquals("ORD-103", order.orderId());
        assertEquals(3.0, order.qty());
        assertEquals("2", order.displayId());
    }

    @Test
    @DisplayName("Should be serializable")
    void testSerializable() throws Exception {
        RemoteOrder original = RemoteOrder.of("ORD-104", 0.5, "Half Soup", null, null, "TCK-58", "1");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(original);
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        try (ObjectInputStream ois = new ObjectInputStream(bais)) {
            RemoteOrder deserialized = (RemoteOrder) ois.readObject();
            assertEquals(original, deserialized);
        }
    }
}
