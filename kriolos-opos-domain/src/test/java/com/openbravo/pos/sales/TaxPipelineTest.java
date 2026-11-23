package com.openbravo.pos.sales;

import com.openbravo.data.loader.Session;
import com.openbravo.data.loader.TableDefinition;
import com.openbravo.data.user.SaveProvider;
import com.openbravo.pos.ticket.TaxInfo;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test suite verifying type-safe CRUD operations for TaxInfo.
 *
 * @author KriolOS
 */
@DisplayName("Taxes Type-Safe Persistence Pipeline Test Suite")
class TaxPipelineTest {

    @Test
    @DisplayName("Should execute type-safe CRUD operations for TaxInfo via TableDefinition and DefaultSaveProvider")
    void shouldExecuteTypeSafeCrudForTaxes() throws Exception {
        String dbName = "tax_test_" + UUID.randomUUID().toString().replace("-", "");
        Session session = new Session("jdbc:hsqldb:mem:" + dbName + ";sql.syntax_mys=true", "SA", "");

        try {
            try (Statement st = session.getConnection().createStatement()) {
                st.executeUpdate("CREATE TABLE taxcategories ("
                        + "id VARCHAR(36) PRIMARY KEY, "
                        + "name VARCHAR(255) NOT NULL)");

                st.executeUpdate("CREATE TABLE taxcustcategories ("
                        + "id VARCHAR(36) PRIMARY KEY, "
                        + "name VARCHAR(255) NOT NULL)");

                st.executeUpdate("CREATE TABLE taxes ("
                        + "id VARCHAR(36) PRIMARY KEY, "
                        + "name VARCHAR(255) NOT NULL, "
                        + "category VARCHAR(36) NOT NULL, "
                        + "custcategory VARCHAR(36), "
                        + "parentid VARCHAR(36), "
                        + "rate DOUBLE NOT NULL, "
                        + "ratecascade BOOLEAN NOT NULL, "
                        + "rateorder INT)");
            }

            DataLogicTax dataLogic = new DataLogicTax();
            dataLogic.init(session);

            TableDefinition<TaxInfo> tableTaxes = dataLogic.getTableTaxes();
            assertNotNull(tableTaxes);

            SaveProvider<TaxInfo> saveProvider = dataLogic.getTaxSaveProvider();
            assertNotNull(saveProvider);
            assertTrue(saveProvider.canInsert());
            assertTrue(saveProvider.canUpdate());
            assertTrue(saveProvider.canDelete());

            // 1. Insert Tax
            TaxInfo tax1 = new TaxInfo("tax-001", "Standard VAT 20%", "tc-001", null, null, 0.20, false, 1);
            assertEquals(1, saveProvider.insertData(tax1));

            // 2. Query TableDefinition
            List<TaxInfo> list = tableTaxes.getListSentence().list();
            assertEquals(1, list.size());
            TaxInfo loaded = list.get(0);
            assertEquals("tax-001", loaded.getId());
            assertEquals("Standard VAT 20%", loaded.getName());
            assertEquals("tc-001", loaded.getTaxCategoryID());
            assertNull(loaded.getTaxCustCategoryID());
            assertNull(loaded.getParentID());
            assertEquals(0.20, loaded.getRate(), 0.0001);
            assertFalse(loaded.isCascade());
            assertEquals(1, loaded.getOrder());

            // 2b. Query SentenceList in DataLogicTax
            List<TaxInfo> taxList = dataLogic.getTaxList().list();
            assertEquals(1, taxList.size());
            assertEquals("tax-001", taxList.get(0).getId());

            List<TaxInfo> taxListAll = dataLogic.getTaxListAll();
            assertEquals(1, taxListAll.size());
            assertEquals("tax-001", taxListAll.get(0).getId());

            // 3. Update
            tax1.setName("Standard VAT 21%");
            tax1.setRate(0.21);
            tax1.setCascade(true);
            tax1.setOrder(2);
            assertEquals(1, saveProvider.updateData(tax1));

            List<TaxInfo> updatedList = tableTaxes.getListSentence().list();
            assertEquals(1, updatedList.size());
            TaxInfo updated = updatedList.get(0);
            assertEquals("Standard VAT 21%", updated.getName());
            assertEquals(0.21, updated.getRate(), 0.0001);
            assertTrue(updated.isCascade());
            assertEquals(2, updated.getOrder());

            // 4. Delete
            assertEquals(1, saveProvider.deleteData(tax1));
            assertTrue(tableTaxes.getListSentence().list().isEmpty());

        } finally {
            try (Statement st = session.getConnection().createStatement()) {
                st.execute("SHUTDOWN");
            } catch (Exception ignored) {
            }
        }
    }
}
