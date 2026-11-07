package com.openbravo.pos.inventory;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.Session;
import com.openbravo.data.loader.TableDefinition;
import com.openbravo.data.user.DefaultSaveProvider;
import com.openbravo.pos.pim.DataLogicUom;
import com.openbravo.pos.pim.UomInfo;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Contract and integration test suite verifying end-to-end type safety for UOM persistence.
 *
 * @author KriolOS
 */
@DisplayName("UOM Type-Safe Persistence Pipeline Test Suite")
class UomPipelineTest {

    @Test
    @DisplayName("Should execute type-safe CRUD operations via TableDefinition<UomInfo> and DefaultSaveProvider<UomInfo>")
    void shouldExecuteTypeSafeCrudOperations() throws Exception {
        String dbName = "uom_test_" + UUID.randomUUID().toString().replace("-", "");
        Session session = new Session("jdbc:hsqldb:mem:" + dbName + ";sql.syntax_mys=true", "SA", "");

        try {
            try (Statement st = session.getConnection().createStatement()) {
                st.executeUpdate("CREATE TABLE uom ("
                        + "id VARCHAR(36) PRIMARY KEY, "
                        + "name VARCHAR(255) NOT NULL)");
            }

            DataLogicUom dataLogicUom = new DataLogicUom();
            dataLogicUom.init(session);

            TableDefinition<UomInfo> tableUom = dataLogicUom.getTableUom();
            assertNotNull(tableUom, "TableDefinition should be instantiated");

            DefaultSaveProvider<UomInfo> saveProvider = new DefaultSaveProvider<>(tableUom);
            assertTrue(saveProvider.canInsert(), "Should support insert");
            assertTrue(saveProvider.canUpdate(), "Should support update");
            assertTrue(saveProvider.canDelete(), "Should support delete");

            // 1. Insert UomInfo
            UomInfo uomKg = new UomInfo("uom-kg", "Kilogram");
            int inserted = saveProvider.insertData(uomKg);
            assertEquals(1, inserted, "Should insert 1 row");

            // 2. Query via typed sentence
            List<UomInfo> list = tableUom.getListSentence().list();
            assertEquals(1, list.size());
            assertEquals("uom-kg", list.get(0).getId());
            assertEquals("Kilogram", list.get(0).getName());

            // 3. Update UomInfo
            uomKg.setName("Kilogram (kg)");
            int updated = saveProvider.updateData(uomKg);
            assertEquals(1, updated, "Should update 1 row");

            UomInfo fetched = dataLogicUom.getUomInfoById("uom-kg");
            assertNotNull(fetched);
            assertEquals("Kilogram (kg)", fetched.getName());

            // 4. Delete UomInfo
            int deleted = saveProvider.deleteData(uomKg);
            assertEquals(1, deleted, "Should delete 1 row");

            List<UomInfo> afterDelete = tableUom.getListSentence().list();
            assertTrue(afterDelete.isEmpty(), "Table should be empty after deletion");

        } finally {
            try (Statement st = session.getConnection().createStatement()) {
                st.execute("SHUTDOWN");
            } catch (Exception ignored) {
            }
        }
    }
}

