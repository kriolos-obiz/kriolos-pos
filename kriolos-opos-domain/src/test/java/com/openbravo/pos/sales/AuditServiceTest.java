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
package com.openbravo.pos.sales;

import com.openbravo.data.loader.Session;
import com.openbravo.pos.container.BeanContainer;
import com.openbravo.pos.panels.AppView;
import java.lang.reflect.Proxy;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AuditService Port & Audit Commands Contract Test Suite")
class AuditServiceTest {

    @Test
    @DisplayName("Should resolve AuditService in BeanContainer to DataLogicAudit")
    void shouldResolveAuditServiceInBeanContainer() {
        AppView appViewProxy = (AppView) Proxy.newProxyInstance(
                AppView.class.getClassLoader(),
                new Class<?>[]{AppView.class},
                (proxy, method, args) -> null
        );

        Object bean = BeanContainer.getBean("com.openbravo.pos.sales.AuditService", appViewProxy);
        assertNotNull(bean, "BeanContainer should resolve AuditService");
        assertInstanceOf(DataLogicAudit.class, bean, "AuditService should resolve to DataLogicAudit");
        assertInstanceOf(AuditService.class, bean, "DataLogicAudit should implement AuditService");
    }

    @Test
    @DisplayName("RecordAuditLineRemovedCommand should validate invariants and normalize strings")
    void shouldValidateRecordAuditLineRemovedCommandInvariants() {
        Date now = new Date();
        RecordAuditLineRemovedCommand cmd = RecordAuditLineRemovedCommand.of(
                "  admin  ",
                "  ticket-101  ",
                "  prod-99  ",
                "  Apple Pie  ",
                3.0,
                now
        );

        assertEquals("admin", cmd.username());
        assertEquals("ticket-101", cmd.ticketId());
        assertEquals("prod-99", cmd.productId());
        assertEquals("Apple Pie", cmd.productName());
        assertEquals(3.0, cmd.units(), 0.001);
        assertEquals(now.getTime(), cmd.date().getTime());

        // Normalization of blank optional fields
        RecordAuditLineRemovedCommand nullOptionalCmd = RecordAuditLineRemovedCommand.of(
                "cashier",
                "   ",
                "",
                "Soda",
                1.0
        );
        assertNull(nullOptionalCmd.ticketId());
        assertNull(nullOptionalCmd.productId());
        assertNotNull(nullOptionalCmd.date());

        // Invariant violations
        assertThrows(NullPointerException.class, () ->
                new RecordAuditLineRemovedCommand(null, "t1", "p1", "Item", 1.0, now));
        assertThrows(IllegalArgumentException.class, () ->
                new RecordAuditLineRemovedCommand("   ", "t1", "p1", "Item", 1.0, now));
        assertThrows(NullPointerException.class, () ->
                new RecordAuditLineRemovedCommand("admin", "t1", "p1", null, 1.0, now));
        assertThrows(IllegalArgumentException.class, () ->
                new RecordAuditLineRemovedCommand("admin", "t1", "p1", "   ", 1.0, now));
        assertThrows(NullPointerException.class, () ->
                new RecordAuditLineRemovedCommand("admin", "t1", "p1", "Item", 1.0, null));
    }

    @Test
    @DisplayName("RecordAuditLineRemovedCommand should defensively copy date")
    void shouldDefensivelyCopyDateInRecordAuditLineRemovedCommand() {
        Date originalDate = new Date(1_000_000L);
        RecordAuditLineRemovedCommand cmd = RecordAuditLineRemovedCommand.of(
                "admin", "t1", "p1", "Bread", 2.0, originalDate);

        // Mutating source date should not alter record
        originalDate.setTime(2_000_000L);
        assertEquals(1_000_000L, cmd.date().getTime());

        // Mutating returned date should not alter record
        cmd.date().setTime(3_000_000L);
        assertEquals(1_000_000L, cmd.date().getTime());
    }

    @Test
    @DisplayName("RecordAuditTicketDeletedCommand should validate invariants and defensively copy date")
    void shouldValidateRecordAuditTicketDeletedCommand() {
        Date now = new Date(1_500_000L);
        RecordAuditTicketDeletedCommand cmd = RecordAuditTicketDeletedCommand.of("  supervisor  ", now);

        assertEquals("supervisor", cmd.username());
        assertEquals(1_500_000L, cmd.date().getTime());

        // Invariant violations
        assertThrows(NullPointerException.class, () -> new RecordAuditTicketDeletedCommand(null, now));
        assertThrows(IllegalArgumentException.class, () -> new RecordAuditTicketDeletedCommand("  ", now));
        assertThrows(NullPointerException.class, () -> new RecordAuditTicketDeletedCommand("admin", null));

        // Defensive copying
        now.setTime(2_500_000L);
        assertEquals(1_500_000L, cmd.date().getTime());
        cmd.date().setTime(3_500_000L);
        assertEquals(1_500_000L, cmd.date().getTime());
    }

    @Test
    @DisplayName("DataLogicAudit should persist line removal and ticket deletion events in HSQLDB")
    void shouldPersistAuditEventsInDatabase() throws Exception {
        String dbName = "audit_db_" + UUID.randomUUID().toString().replace("-", "");
        String url = "jdbc:hsqldb:mem:" + dbName + ";sql.syntax_mys=true";

        Session session = new Session(url, "SA", "");
        try (Statement stmt = session.getConnection().createStatement()) {
            stmt.execute("""
                CREATE TABLE lineremoved (
                    NAME VARCHAR(255) NOT NULL,
                    TICKETID VARCHAR(255),
                    PRODUCTID VARCHAR(255),
                    PRODUCTNAME VARCHAR(255),
                    UNITS DOUBLE,
                    REMOVEDDATE TIMESTAMP NOT NULL
                )
            """);
        }

        DataLogicAudit dataLogicAudit = new DataLogicAudit();
        dataLogicAudit.init(session);

        // 1. Record line removed via command
        Date removalDate = new Date();
        RecordAuditLineRemovedCommand lineCmd = RecordAuditLineRemovedCommand.of(
                "cashier1",
                "TICKET-999",
                "PROD-001",
                "Croissant",
                2.5,
                removalDate
        );
        dataLogicAudit.recordLineRemoved(lineCmd);

        try (Statement stmt = session.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM lineremoved WHERE PRODUCTID = 'PROD-001'")) {
            assertTrue(rs.next(), "Row for line removal must exist");
            assertEquals("cashier1", rs.getString("NAME"));
            assertEquals("TICKET-999", rs.getString("TICKETID"));
            assertEquals("PROD-001", rs.getString("PRODUCTID"));
            assertEquals("Croissant", rs.getString("PRODUCTNAME"));
            assertEquals(2.5, rs.getDouble("UNITS"), 0.001);
            assertNotNull(rs.getTimestamp("REMOVEDDATE"));
        }

        // 2. Record ticket deleted via command
        Date deleteDate = new Date();
        RecordAuditTicketDeletedCommand delCmd = RecordAuditTicketDeletedCommand.of("manager1", deleteDate);
        dataLogicAudit.recordTicketDeleted(delCmd);

        try (Statement stmt = session.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM lineremoved WHERE TICKETID = 'Void'")) {
            assertTrue(rs.next(), "Row for void ticket must exist");
            assertEquals("manager1", rs.getString("NAME"));
            assertEquals("Void", rs.getString("TICKETID"));
            assertEquals("Ticket Deleted", rs.getString("PRODUCTNAME"));
            assertEquals(0.0, rs.getDouble("UNITS"), 0.001);
            assertNotNull(rs.getTimestamp("REMOVEDDATE"));
        }

        // 3. Test legacy adapter methods delegate properly
        dataLogicAudit.addTicketLineRemoved("cashier2", "TICKET-888", "PROD-002", "Coffee", 1.0);
        try (Statement stmt = session.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM lineremoved WHERE PRODUCTID = 'PROD-002'")) {
            assertTrue(rs.next(), "Legacy addTicketLineRemoved row must exist");
            assertEquals("cashier2", rs.getString("NAME"));
            assertEquals("TICKET-888", rs.getString("TICKETID"));
            assertEquals("Coffee", rs.getString("PRODUCTNAME"));
            assertEquals(1.0, rs.getDouble("UNITS"), 0.001);
        }

        dataLogicAudit.addTicketDeleted("supervisor2");
        try (Statement stmt = session.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM lineremoved WHERE NAME = 'supervisor2'")) {
            assertTrue(rs.next(), "Legacy addTicketDeleted row must exist");
            assertEquals("Void", rs.getString("TICKETID"));
            assertEquals("Ticket Deleted", rs.getString("PRODUCTNAME"));
        }
    }
}

