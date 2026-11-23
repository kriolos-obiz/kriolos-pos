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

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.Session;
import com.openbravo.data.user.SaveProvider;
import com.openbravo.pos.panels.AppView;
import com.openbravo.pos.container.BeanContainer;
import java.lang.reflect.Proxy;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TreasuryService Port & Payment Movement Commands Contract Test Suite")
class TreasuryServiceTest {

    @Test
    @DisplayName("RecordPaymentMovementCommand should enforce invariants and defensive copying")
    void shouldValidateRecordPaymentMovementCommandInvariants() {
        Date now = new Date();

        assertThrows(NullPointerException.class,
                () -> new RecordPaymentMovementCommand(null, "cash-1", now, "pay-1", "cashin", 10.0, "notes"));
        assertThrows(IllegalArgumentException.class,
                () -> new RecordPaymentMovementCommand("", "cash-1", now, "pay-1", "cashin", 10.0, "notes"));
        assertThrows(IllegalArgumentException.class,
                () -> new RecordPaymentMovementCommand("   ", "cash-1", now, "pay-1", "cashin", 10.0, "notes"));

        assertThrows(NullPointerException.class,
                () -> new RecordPaymentMovementCommand("rec-1", null, now, "pay-1", "cashin", 10.0, "notes"));
        assertThrows(IllegalArgumentException.class,
                () -> new RecordPaymentMovementCommand("rec-1", "  ", now, "pay-1", "cashin", 10.0, "notes"));

        assertThrows(NullPointerException.class,
                () -> new RecordPaymentMovementCommand("rec-1", "cash-1", now, null, "cashin", 10.0, "notes"));
        assertThrows(IllegalArgumentException.class,
                () -> new RecordPaymentMovementCommand("rec-1", "cash-1", now, "  ", "cashin", 10.0, "notes"));

        assertThrows(NullPointerException.class,
                () -> new RecordPaymentMovementCommand("rec-1", "cash-1", now, "pay-1", null, 10.0, "notes"));
        assertThrows(IllegalArgumentException.class,
                () -> new RecordPaymentMovementCommand("rec-1", "cash-1", now, "pay-1", "  ", 10.0, "notes"));

        assertThrows(IllegalArgumentException.class,
                () -> new RecordPaymentMovementCommand("rec-1", "cash-1", now, "pay-1", "cashin", Double.NaN, "notes"));
        assertThrows(IllegalArgumentException.class,
                () -> new RecordPaymentMovementCommand("rec-1", "cash-1", now, "pay-1", "cashin", Double.POSITIVE_INFINITY, "notes"));

        // Defaults and trimming
        RecordPaymentMovementCommand cmd = RecordPaymentMovementCommand.of("  rec-10  ", "  cash-99  ", null, "  pay-5  ", "  cashin  ", 25.5, null);
        assertEquals("rec-10", cmd.receiptId());
        assertEquals("cash-99", cmd.activeCashIndex());
        assertNotNull(cmd.date());
        assertEquals("pay-5", cmd.paymentId());
        assertEquals("cashin", cmd.reason());
        assertEquals(25.5, cmd.total(), 0.001);
        assertEquals("", cmd.notes());

        // Defensive copying
        Date originalDate = new Date(1000000000L);
        RecordPaymentMovementCommand copyCmd = RecordPaymentMovementCommand.of("rec-1", "cash-1", originalDate, "pay-1", "cashout", -15.0, "tip");
        originalDate.setTime(2000000000L);
        assertEquals(1000000000L, copyCmd.date().getTime());

        Date retrievedDate = copyCmd.date();
        retrievedDate.setTime(3000000000L);
        assertEquals(1000000000L, copyCmd.date().getTime());

        // Factory from PaymentMovement
        PaymentMovement movement = new PaymentMovement("rec-3", "cash-3", new Date(500000000L), "pay-3", "cashin", 50.0, "float");
        RecordPaymentMovementCommand fromMovement = RecordPaymentMovementCommand.from(movement);
        assertEquals("rec-3", fromMovement.receiptId());
        assertEquals(50.0, fromMovement.total(), 0.001);
    }

    @Test
    @DisplayName("DeletePaymentMovementCommand should enforce invariants and trim inputs")
    void shouldValidateDeletePaymentMovementCommandInvariants() {
        assertThrows(NullPointerException.class, () -> new DeletePaymentMovementCommand(null, "pay-1"));
        assertThrows(IllegalArgumentException.class, () -> new DeletePaymentMovementCommand("", "pay-1"));
        assertThrows(IllegalArgumentException.class, () -> new DeletePaymentMovementCommand("   ", "pay-1"));

        assertThrows(NullPointerException.class, () -> new DeletePaymentMovementCommand("rec-1", null));
        assertThrows(IllegalArgumentException.class, () -> new DeletePaymentMovementCommand("rec-1", ""));
        assertThrows(IllegalArgumentException.class, () -> new DeletePaymentMovementCommand("rec-1", "   "));

        DeletePaymentMovementCommand cmd = DeletePaymentMovementCommand.of("  rec-100  ", "  pay-200  ");
        assertEquals("rec-100", cmd.receiptId());
        assertEquals("pay-200", cmd.paymentId());
    }

    @Test
    @DisplayName("TreasuryService default methods should delegate to commands")
    void shouldDelegateDefaultMethodsToCommands() throws BasicException {
        final List<RecordPaymentMovementCommand> recorded = new ArrayList<>();
        final List<DeletePaymentMovementCommand> deleted = new ArrayList<>();

        TreasuryService testService = new TreasuryService() {
            @Override
            public void recordPaymentMovement(RecordPaymentMovementCommand command) {
                recorded.add(command);
            }

            @Override
            public void deletePaymentMovement(DeletePaymentMovementCommand command) {
                deleted.add(command);
            }

            @Override
            public SaveProvider<PaymentMovement> getPaymentMovementSaveProvider() {
                return null;
            }

            @Override
            public Integer getNextTicketPaymentIndex() {
                return 1;
            }
        };

        // 1. PaymentMovement record delegation
        PaymentMovement pm = new PaymentMovement("rec-1", "cash-1", new Date(), "pay-1", "cashin", 100.0, "Float");
        testService.recordPaymentMovement(pm);
        assertEquals(1, recorded.size());
        assertEquals("rec-1", recorded.get(0).receiptId());
        assertEquals(100.0, recorded.get(0).total(), 0.001);

        // 2. Legacy 7-arg method delegation
        testService.recordPaymentMovement("rec-2", "cash-2", new Date(), "pay-2", "cashout", -40.0, "Expense");
        assertEquals(2, recorded.size());
        assertEquals("rec-2", recorded.get(1).receiptId());
        assertEquals(-40.0, recorded.get(1).total(), 0.001);

        // 3. Legacy delete delegation
        testService.deletePaymentMovement("rec-2", "pay-2");
        assertEquals(1, deleted.size());
        assertEquals("rec-2", deleted.get(0).receiptId());
        assertEquals("pay-2", deleted.get(0).paymentId());

        // 4. Null PaymentMovement safety
        testService.recordPaymentMovement((PaymentMovement) null);
        assertEquals(2, recorded.size());
    }

    @Test
    @DisplayName("DataLogicPayments should record and delete movements atomically on HSQLDB")
    void shouldRecordAndDeleteMovementsOnDatabase() throws Exception {
        String dbName = "treasury_db_" + UUID.randomUUID().toString().replace("-", "");
        Session session = new Session("jdbc:hsqldb:mem:" + dbName + ";sql.syntax_mys=true", "SA", "");

        try (Statement stmt = session.getConnection().createStatement()) {
            stmt.executeUpdate("CREATE TABLE receipts ("
                    + "ID VARCHAR(255) PRIMARY KEY, "
                    + "MONEY VARCHAR(255) NOT NULL, "
                    + "DATENEW TIMESTAMP NOT NULL)");

            stmt.executeUpdate("CREATE TABLE payments ("
                    + "ID VARCHAR(255) PRIMARY KEY, "
                    + "RECEIPT VARCHAR(255) NOT NULL, "
                    + "PAYMENT VARCHAR(255) NOT NULL, "
                    + "TOTAL DOUBLE NOT NULL, "
                    + "NOTES VARCHAR(255))");
        }

        DataLogicPayments dlPayments = new DataLogicPayments();
        dlPayments.init(session);

        // 1. Record payment movement
        Date timestamp = new Date();
        RecordPaymentMovementCommand recordCmd = RecordPaymentMovementCommand.of(
                "rec-001",
                "session-1",
                timestamp,
                "pay-001",
                "cashin",
                250.0,
                "Morning float"
        );
        dlPayments.recordPaymentMovement(recordCmd);

        // Verify receipts row
        try (Statement stmt = session.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT ID, MONEY FROM receipts WHERE ID = 'rec-001'")) {
            assertTrue(rs.next(), "Receipt row should be inserted");
            assertEquals("rec-001", rs.getString("ID"));
            assertEquals("session-1", rs.getString("MONEY"));
        }

        // Verify payments row
        try (Statement stmt = session.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT ID, RECEIPT, PAYMENT, TOTAL, NOTES FROM payments WHERE ID = 'pay-001'")) {
            assertTrue(rs.next(), "Payment row should be inserted");
            assertEquals("pay-001", rs.getString("ID"));
            assertEquals("rec-001", rs.getString("RECEIPT"));
            assertEquals("cashin", rs.getString("PAYMENT"));
            assertEquals(250.0, rs.getDouble("TOTAL"), 0.001);
            assertEquals("Morning float", rs.getString("NOTES"));
        }

        // 2. Delete payment movement
        DeletePaymentMovementCommand deleteCmd = DeletePaymentMovementCommand.of("rec-001", "pay-001");
        dlPayments.deletePaymentMovement(deleteCmd);

        // Verify deletion from both tables
        try (Statement stmt = session.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM payments WHERE ID = 'pay-001'")) {
            assertTrue(rs.next());
            assertEquals(0, rs.getInt(1), "Payment row should be deleted");
        }

        try (Statement stmt = session.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM receipts WHERE ID = 'rec-001'")) {
            assertTrue(rs.next());
            assertEquals(0, rs.getInt(1), "Receipt row should be deleted");
        }

        // 3. Null command safety
        dlPayments.recordPaymentMovement((RecordPaymentMovementCommand) null);
        dlPayments.deletePaymentMovement((DeletePaymentMovementCommand) null);

        // 4. SaveProvider presence
        assertNotNull(dlPayments.getPaymentMovementSaveProvider());
    }

    @Test
    @DisplayName("Should resolve TreasuryService in BeanContainer to DataLogicPayments")
    void shouldResolveTreasuryServiceInBeanContainer() {
        AppView appViewProxy = (AppView) Proxy.newProxyInstance(
                AppView.class.getClassLoader(),
                new Class<?>[]{AppView.class},
                (proxy, method, args) -> {
                    if ("getSession".equals(method.getName())) {
                        return null;
                    }
                    return null;
                }
        );

        Object bean = BeanContainer.getBean("com.openbravo.pos.payment.TreasuryService", appViewProxy);
        assertNotNull(bean, "BeanContainer should resolve TreasuryService");
        assertInstanceOf(DataLogicPayments.class, bean, "TreasuryService should resolve to DataLogicPayments");
        assertInstanceOf(TreasuryService.class, bean, "DataLogicPayments should implement TreasuryService");
    }
}
