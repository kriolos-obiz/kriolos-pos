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
package com.openbravo.pos.sales.restaurant;

import com.openbravo.data.loader.Session;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PlaceService Port & PlaceServiceImpl SQL Safety Test Suite")
class PlaceServiceTest {

    @Test
    @DisplayName("MoveCustomerCommand should validate business invariants")
    void shouldEnforceMoveCustomerCommandInvariants() {
        assertThrows(NullPointerException.class, () -> new MoveCustomerCommand(null, "tck-1"));
        assertThrows(IllegalArgumentException.class, () -> new MoveCustomerCommand("", "tck-1"));
        assertThrows(IllegalArgumentException.class, () -> new MoveCustomerCommand("   ", "tck-1"));

        assertThrows(NullPointerException.class, () -> new MoveCustomerCommand("Table 2", null));
        assertThrows(IllegalArgumentException.class, () -> new MoveCustomerCommand("Table 2", ""));
        assertThrows(IllegalArgumentException.class, () -> new MoveCustomerCommand("Table 2", "   "));

        MoveCustomerCommand cmd = MoveCustomerCommand.of("Table 3", "tck-42");
        assertEquals("Table 3", cmd.newTable());
        assertEquals("tck-42", cmd.ticketId());
    }

    @Test
    @DisplayName("PlaceServiceImpl should execute all floor plan queries and state updates safely on HSQLDB")
    void testPlaceServiceOperationsOnDatabase() throws Exception {
        String dbName = "places_db_" + UUID.randomUUID().toString().replace("-", "");
        Session session = new Session("jdbc:hsqldb:mem:" + dbName + ";sql.syntax_mys=true", "SA", "");

        try (Statement stmt = session.getConnection().createStatement()) {
            stmt.executeUpdate("CREATE TABLE floors ("
                    + "ID VARCHAR(255) PRIMARY KEY, "
                    + "NAME VARCHAR(255) NOT NULL, "
                    + "IMAGE VARBINARY(1000))");

            stmt.executeUpdate("CREATE TABLE places ("
                    + "ID VARCHAR(255) PRIMARY KEY, "
                    + "NAME VARCHAR(255) NOT NULL, "
                    + "SEATS INT, "
                    + "X INT, "
                    + "Y INT, "
                    + "FLOOR VARCHAR(255), "
                    + "CUSTOMER VARCHAR(255), "
                    + "WAITER VARCHAR(255), "
                    + "TICKETID VARCHAR(255), "
                    + "TABLEMOVED VARCHAR(10))");

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            BufferedImage dummyImg = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
            ImageIO.write(dummyImg, "png", baos);
            byte[] imageBytes = baos.toByteArray();

            try (PreparedStatement pstmt = session.getConnection().prepareStatement("INSERT INTO floors (ID, NAME, IMAGE) VALUES (?, ?, ?)")) {
                pstmt.setString(1, "floor-1");
                pstmt.setString(2, "Main Dining");
                pstmt.setBytes(3, imageBytes);
                pstmt.executeUpdate();
            }
            stmt.executeUpdate("INSERT INTO places (ID, NAME, SEATS, X, Y, FLOOR, CUSTOMER, WAITER, TICKETID, TABLEMOVED) "
                    + "VALUES ('place-1', 'Table 1', 4, 10, 20, 'floor-1', NULL, NULL, NULL, 'false')");
            stmt.executeUpdate("INSERT INTO places (ID, NAME, SEATS, X, Y, FLOOR, CUSTOMER, WAITER, TICKETID, TABLEMOVED) "
                    + "VALUES ('place-2', 'Table 2', 2, 50, 60, 'floor-1', NULL, NULL, NULL, 'false')");
        }

        PlaceService service = new PlaceServiceImpl(session);

        // Verify getFloors & getPlaces
        List<Floor> floors = service.getFloors();
        assertEquals(1, floors.size());
        assertEquals("Main Dining", floors.get(0).getName());

        List<Place> places = service.getPlaces();
        assertEquals(2, places.size());

        // Test customer name assignment by name, ID, and ticket ID
        service.setCustomerNameInTable("Alice Smith", "Table 1");
        assertEquals("Alice Smith", service.getCustomerNameInTable("Table 1"));
        assertEquals("Alice Smith", service.getCustomerNameInTableById("place-1"));

        service.clearCustomerNameInTable("Table 1");
        assertEquals("", service.getCustomerNameInTable("Table 1"));

        service.setCustomerNameInTableById("Bob Jones", "place-1");
        assertEquals("Bob Jones", service.getCustomerNameInTable("Table 1"));
        service.clearCustomerNameInTableById("place-1");
        assertEquals("", service.getCustomerNameInTableById("place-1"));

        // Test waiter assignment
        service.setWaiterNameInTable("Waiter Sam", "Table 1");
        assertEquals("Waiter Sam", service.getWaiterNameInTable("Table 1"));
        assertEquals("Waiter Sam", service.getWaiterNameInTableById("place-1"));

        service.clearWaiterNameInTable("Table 1");
        assertEquals("", service.getWaiterNameInTable("Table 1"));

        service.setWaiterNameInTableById("Waiter Lucy", "place-2");
        assertEquals("Waiter Lucy", service.getWaiterNameInTable("Table 2"));
        service.clearWaiterNameInTableById("place-2");
        assertEquals("", service.getWaiterNameInTableById("place-2"));

        // Test ticket ID assignment, count, and lookup
        service.setTicketIdInTable("ticket-100", "Table 1");
        assertEquals("ticket-100", service.getTicketIdInTable("place-1"));
        assertEquals("Table 1", service.getTableDetails("ticket-100"));
        assertEquals(1, service.countTicketIdInTable("ticket-100"));

        service.setCustomerNameInTableByTicketId("Charlie Brown", "ticket-100");
        assertEquals("Charlie Brown", service.getCustomerNameInTable("Table 1"));

        // Test table moved flag lifecycle
        assertFalse(service.getTableMovedFlag("ticket-100"));
        service.setTableMovedFlag("place-1");
        assertTrue(service.getTableMovedFlag("ticket-100"));
        assertEquals("Table 1", service.getTableMovedName("ticket-100"));

        service.clearTableMovedFlag("Table 1");
        assertFalse(service.getTableMovedFlag("ticket-100"));
        assertNull(service.getTableMovedName("ticket-100"));

        // Test moveCustomer when ticket is shared across multiple tables (count > 1)
        service.setTicketIdInTable("ticket-100", "Table 2");
        service.setCustomerNameInTable("Charlie Brown", "Table 1");
        service.setWaiterNameInTable("Waiter Sam", "Table 1");
        service.setTableMovedFlag("place-1");
        assertEquals(2, service.countTicketIdInTable("ticket-100"));

        service.moveCustomer(MoveCustomerCommand.of("Table 2", "ticket-100"));
        assertEquals("Charlie Brown", service.getCustomerNameInTable("Table 2"));
        assertEquals("Waiter Sam", service.getWaiterNameInTable("Table 2"));
        assertEquals("ticket-100", service.getTicketIdInTable("place-2"));
        assertEquals("", service.getCustomerNameInTable("Table 1"));
        assertEquals("", service.getWaiterNameInTable("Table 1"));

        // Test updatePlaces position command
        service.updatePlaces(UpdatePlacePositionCommand.of("place-1", 150, 250));
        Place updatedPlace = service.getPlaces().stream()
                .filter(p -> "place-1".equals(p.getId()))
                .findFirst()
                .orElseThrow();
        assertEquals(150, updatedPlace.getX());
        assertEquals(250, updatedPlace.getY());
    }
}
