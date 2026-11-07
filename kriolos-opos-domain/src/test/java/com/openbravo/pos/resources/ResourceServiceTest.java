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
package com.openbravo.pos.resources;

import com.openbravo.data.loader.Session;
import com.openbravo.pos.admin.DataLogicSystem;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ResourceService Port & SetResourceCommand Contract Test Suite")
class ResourceServiceTest {

    @Test
    @DisplayName("SetResourceCommand should validate invariants and defensively copy data")
    void shouldValidateSetResourceCommandInvariants() {
        byte[] original = new byte[]{1, 2, 3};
        SetResourceCommand cmd = SetResourceCommand.of("  Printer.Ticket  ", 0, original);

        assertEquals("Printer.Ticket", cmd.name());
        assertEquals(0, cmd.type());
        assertArrayEquals(new byte[]{1, 2, 3}, cmd.data());

        // Mutating source array must not affect command
        original[0] = 99;
        assertEquals(1, cmd.data()[0]);

        // Mutating returned array must not affect command
        byte[] returned = cmd.data();
        returned[0] = 77;
        assertEquals(1, cmd.data()[0]);

        // Null/blank invariants
        assertThrows(NullPointerException.class, () -> new SetResourceCommand(null, 0, new byte[0]));
        assertThrows(IllegalArgumentException.class, () -> new SetResourceCommand("   ", 0, new byte[0]));

        // Null data is permitted
        SetResourceCommand nullDataCmd = SetResourceCommand.of("Empty", 0, null);
        assertNull(nullDataCmd.data());

        // Convenience factory helpers
        SetResourceCommand binaryCmd = SetResourceCommand.ofBinary("Logo.png", new byte[]{10, 20});
        assertEquals(2, binaryCmd.type());

        SetResourceCommand textCmd = SetResourceCommand.ofText("Config.xml", "<config/>");
        assertEquals(0, textCmd.type());
        assertEquals("<config/>", new String(textCmd.data(), StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("DataLogicSystem should persist and retrieve resources via SetResourceCommand in HSQLDB")
    void shouldPersistAndRetrieveResources() throws Exception {
        String dbName = "res_db_" + UUID.randomUUID().toString().replace("-", "");
        String url = "jdbc:hsqldb:mem:" + dbName + ";sql.syntax_mys=true";

        Session session = new Session(url, "SA", "");
        try {
            try (Statement stmt = session.getConnection().createStatement()) {
                stmt.execute("""
                    CREATE TABLE resources (
                        ID VARCHAR(255) PRIMARY KEY,
                        NAME VARCHAR(255) NOT NULL,
                        RESTYPE INTEGER NOT NULL,
                        CONTENT BLOB
                    )
                """);
            }

            DataLogicSystem dataLogic = new DataLogicSystem();
            dataLogic.init(session);

            // 1. Insert new text resource via command
            dataLogic.setResource(SetResourceCommand.ofText("Printer.Ticket", "<ticket>Hello</ticket>"));

            String text = dataLogic.getResourceAsText("Printer.Ticket");
            assertEquals("<ticket>Hello</ticket>", text);

            try (Statement stmt = session.getConnection().createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT RESTYPE FROM resources WHERE NAME = 'Printer.Ticket'")) {
                assertTrue(rs.next());
                assertEquals(0, rs.getInt("RESTYPE"));
            }

            // 2. Update existing resource via command
            dataLogic.setResource(SetResourceCommand.ofText("Printer.Ticket", "<ticket>Updated</ticket>"));
            assertEquals("<ticket>Updated</ticket>", dataLogic.getResourceAsText("Printer.Ticket"));

            // 3. Insert binary resource via command
            byte[] binaryData = new byte[]{(byte) 0x89, 'P', 'N', 'G'};
            dataLogic.setResource(SetResourceCommand.ofBinary("app.icon", binaryData));
            assertArrayEquals(binaryData, dataLogic.getResourceAsBinary("app.icon"));

            // 4. Test legacy adapter method
            dataLogic.setResource("legacy.res", 0, "legacy-value".getBytes(StandardCharsets.UTF_8));
            assertEquals("legacy-value", dataLogic.getResourceAsText("legacy.res"));

        } finally {
            session.close();
        }
    }
}

