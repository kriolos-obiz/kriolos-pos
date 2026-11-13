package com.openbravo.pos.inventory;

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
 * Integration test suite verifying type-safe persistence pipeline for StockDiaryInfo.
 *
 * @author KriolOS
 */
@DisplayName("StockDiaryInfo Type-Safe Persistence Pipeline Test Suite")
class StockDiaryPipelineTest {

    @Test
    @DisplayName("Should persist and revert StockDiaryInfo via SaveProvider and update stockcurrent balances")
    void shouldPersistAndRevertStockDiaryViaSaveProvider() throws Exception {
        String dbName = "stock_pipe_db_" + UUID.randomUUID().toString().replace("-", "");
        Session session = new Session("jdbc:hsqldb:mem:" + dbName + ";sql.syntax_mys=true", "SA", "");

        try (Statement statement = session.getConnection().createStatement()) {
            statement.executeUpdate("CREATE TABLE products ("
                    + "ID VARCHAR(36) PRIMARY KEY, "
                    + "ISCOM BOOLEAN NOT NULL)");

            statement.executeUpdate("CREATE TABLE products_bundle ("
                    + "ID VARCHAR(36) PRIMARY KEY, "
                    + "PRODUCT VARCHAR(36) NOT NULL, "
                    + "PRODUCT_BUNDLE VARCHAR(36) NOT NULL, "
                    + "QUANTITY DOUBLE NOT NULL)");

            statement.executeUpdate("CREATE TABLE stockcurrent ("
                    + "LOCATION VARCHAR(36) NOT NULL, "
                    + "PRODUCT VARCHAR(36) NOT NULL, "
                    + "ATTRIBUTESETINSTANCE_ID VARCHAR(36), "
                    + "UNITS DOUBLE NOT NULL)");

            statement.executeUpdate("CREATE TABLE stockdiary ("
                    + "ID VARCHAR(36) PRIMARY KEY, "
                    + "DATENEW TIMESTAMP NOT NULL, "
                    + "REASON INTEGER NOT NULL, "
                    + "LOCATION VARCHAR(36) NOT NULL, "
                    + "PRODUCT VARCHAR(36) NOT NULL, "
                    + "ATTRIBUTESETINSTANCE_ID VARCHAR(36), "
                    + "UNITS DOUBLE NOT NULL, "
                    + "PRICE DOUBLE NOT NULL, "
                    + "AppUser VARCHAR(255) NOT NULL)");

            statement.executeUpdate("INSERT INTO products (ID, ISCOM) VALUES ('prod-pipe-1', FALSE)");
        }

        DataLogicInventory dlInventory = new DataLogicInventory();
        dlInventory.init(session);

        SaveProvider<StockDiaryInfo> saveProvider = dlInventory.getStockDiarySaveProvider();
        assertNotNull(saveProvider, "SaveProvider<StockDiaryInfo> must not be null");
        assertTrue(saveProvider.canInsert(), "SaveProvider should allow insert");
        assertTrue(saveProvider.canDelete(), "SaveProvider should allow delete");
        assertFalse(saveProvider.canUpdate(), "SaveProvider should not allow update");

        Date now = new Date();
        StockDiaryInfo entry = new StockDiaryInfo(
                "mov-pipe-01",
                now,
                1,
                "loc-pipe-main",
                "prod-pipe-1",
                null,
                45.0,
                12.0,
                "admin"
        );

        // 1. Insert stock movement via SaveProvider
        int insertResult = saveProvider.insertData(entry);
        assertEquals(1, insertResult);

        // Verify stockdiary row
        try (Statement stmt = session.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT ID, REASON, LOCATION, PRODUCT, UNITS, PRICE, AppUser FROM stockdiary WHERE ID = 'mov-pipe-01'")) {
            assertTrue(rs.next(), "Stock diary row must exist");
            assertEquals("mov-pipe-01", rs.getString("ID"));
            assertEquals(1, rs.getInt("REASON"));
            assertEquals("loc-pipe-main", rs.getString("LOCATION"));
            assertEquals("prod-pipe-1", rs.getString("PRODUCT"));
            assertEquals(45.0, rs.getDouble("UNITS"), 0.001);
            assertEquals(12.0, rs.getDouble("PRICE"), 0.001);
            assertEquals("admin", rs.getString("AppUser"));
        }

        // Verify stockcurrent balance updated
        try (Statement stmt = session.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT UNITS FROM stockcurrent WHERE LOCATION = 'loc-pipe-main' AND PRODUCT = 'prod-pipe-1'")) {
            assertTrue(rs.next(), "Stock current entry must exist");
            assertEquals(45.0, rs.getDouble("UNITS"), 0.001);
        }

        // 2. Reject update
        assertThrows(UnsupportedOperationException.class, () -> saveProvider.updateData(entry));

        // 3. Revert stock movement via SaveProvider
        int deleteResult = saveProvider.deleteData(entry);
        assertEquals(1, deleteResult);

        // Verify stockdiary row is removed
        try (Statement stmt = session.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM stockdiary WHERE ID = 'mov-pipe-01'")) {
            assertTrue(rs.next());
            assertEquals(0, rs.getInt(1));
        }

        // Verify stockcurrent balance reverted to 0.0
        try (Statement stmt = session.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT UNITS FROM stockcurrent WHERE LOCATION = 'loc-pipe-main' AND PRODUCT = 'prod-pipe-1'")) {
            assertTrue(rs.next());
            assertEquals(0.0, rs.getDouble("UNITS"), 0.001);
        }
    }
}
