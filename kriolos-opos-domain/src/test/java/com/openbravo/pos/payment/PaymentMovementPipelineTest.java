package com.openbravo.pos.payment;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.Session;
import com.openbravo.data.user.SaveProvider;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test suite verifying type-safe persistence pipeline for PaymentMovement.
 *
 * @author KriolOS
 */
@DisplayName("PaymentMovement Type-Safe Persistence Pipeline Test Suite")
class PaymentMovementPipelineTest {

    @Test
    @DisplayName("Should persist and delete PaymentMovement via SaveProvider and DataLogicPayments")
    void shouldPersistAndDeletePaymentMovementViaSaveProvider() throws Exception {
        String dbName = "pay_pipe_db_" + UUID.randomUUID().toString().replace("-", "");
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

        SaveProvider<PaymentMovement> saveProvider = dlPayments.getPaymentMovementSaveProvider();
        assertNotNull(saveProvider, "SaveProvider<PaymentMovement> must not be null");
        assertTrue(saveProvider.canInsert(), "SaveProvider should allow insert");
        assertTrue(saveProvider.canDelete(), "SaveProvider should allow delete");
        assertFalse(saveProvider.canUpdate(), "SaveProvider should not allow update (immutable audit log)");

        Date now = new Date();
        PaymentMovement movement = new PaymentMovement(
                "rec-pipe-01",
                "session-cash-1",
                now,
                "pay-pipe-01",
                "cashin",
                300.0,
                "Register Cash In"
        );

        // 1. Insert via SaveProvider
        int insertResult = saveProvider.insertData(movement);
        assertEquals(1, insertResult);

        // Verify receipts table
        try (Statement stmt = session.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT ID, MONEY FROM receipts WHERE ID = 'rec-pipe-01'")) {
            assertTrue(rs.next(), "Receipt record must exist");
            assertEquals("rec-pipe-01", rs.getString("ID"));
            assertEquals("session-cash-1", rs.getString("MONEY"));
        }

        // Verify payments table
        try (Statement stmt = session.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT ID, RECEIPT, PAYMENT, TOTAL, NOTES FROM payments WHERE ID = 'pay-pipe-01'")) {
            assertTrue(rs.next(), "Payment record must exist");
            assertEquals("pay-pipe-01", rs.getString("ID"));
            assertEquals("rec-pipe-01", rs.getString("RECEIPT"));
            assertEquals("cashin", rs.getString("PAYMENT"));
            assertEquals(300.0, rs.getDouble("TOTAL"), 0.001);
            assertEquals("Register Cash In", rs.getString("NOTES"));
        }

        // 2. Reject updates
        assertThrows(UnsupportedOperationException.class, () -> saveProvider.updateData(movement));

        // 3. Delete via SaveProvider
        int deleteResult = saveProvider.deleteData(movement);
        assertEquals(1, deleteResult);

        // Verify both tables are clean
        try (Statement stmt = session.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM payments WHERE ID = 'pay-pipe-01'")) {
            assertTrue(rs.next());
            assertEquals(0, rs.getInt(1));
        }
        try (Statement stmt = session.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM receipts WHERE ID = 'rec-pipe-01'")) {
            assertTrue(rs.next());
            assertEquals(0, rs.getInt(1));
        }
    }
}
