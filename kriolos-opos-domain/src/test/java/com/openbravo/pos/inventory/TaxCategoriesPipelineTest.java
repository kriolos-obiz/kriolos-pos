package com.openbravo.pos.inventory;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.Session;
import com.openbravo.data.loader.TableDefinition;
import com.openbravo.data.user.DefaultSaveProvider;
import com.openbravo.pos.sales.DataLogicTax;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test suite verifying type-safe CRUD operations for Tax Categories and Customer Tax Categories.
 *
 * @author KriolOS
 */
@DisplayName("Tax Categories Type-Safe Persistence Pipeline Test Suite")
class TaxCategoriesPipelineTest {

    @Test
    @DisplayName("Should execute type-safe CRUD operations for TaxCategoryInfo")
    void shouldExecuteTypeSafeCrudForTaxCategories() throws Exception {
        String dbName = "tax_cat_" + UUID.randomUUID().toString().replace("-", "");
        Session session = new Session("jdbc:hsqldb:mem:" + dbName + ";sql.syntax_mys=true", "SA", "");

        try {
            try (Statement st = session.getConnection().createStatement()) {
                st.executeUpdate("CREATE TABLE taxcategories ("
                        + "id VARCHAR(36) PRIMARY KEY, "
                        + "name VARCHAR(255) NOT NULL)");
            }

            DataLogicTax dataLogicTax = new DataLogicTax();
            dataLogicTax.init(session);

            TableDefinition<TaxCategoryInfo> tableTaxCategories = dataLogicTax.getTableTaxCategories();
            assertNotNull(tableTaxCategories);

            DefaultSaveProvider<TaxCategoryInfo> saveProvider = new DefaultSaveProvider<>(tableTaxCategories);
            assertTrue(saveProvider.canInsert());
            assertTrue(saveProvider.canUpdate());
            assertTrue(saveProvider.canDelete());

            // 1. Insert
            TaxCategoryInfo info = new TaxCategoryInfo("tc-standard", "Standard Tax");
            assertEquals(1, saveProvider.insertData(info));

            // 2. Query
            List<TaxCategoryInfo> list = tableTaxCategories.getListSentence().list();
            assertEquals(1, list.size());
            assertEquals("tc-standard", list.get(0).getID());
            assertEquals("Standard Tax", list.get(0).getName());

            // 3. Update
            info.setName("Standard Tax (Updated)");
            assertEquals(1, saveProvider.updateData(info));

            List<TaxCategoryInfo> updatedList = tableTaxCategories.getListSentence().list();
            assertEquals("Standard Tax (Updated)", updatedList.get(0).getName());

            // 4. Delete
            assertEquals(1, saveProvider.deleteData(info));
            assertTrue(tableTaxCategories.getListSentence().list().isEmpty());

        } finally {
            try (Statement st = session.getConnection().createStatement()) {
                st.execute("SHUTDOWN");
            } catch (Exception ignored) {
            }
        }
    }

    @Test
    @DisplayName("Should execute type-safe CRUD operations for TaxCustCategoryInfo")
    void shouldExecuteTypeSafeCrudForTaxCustCategories() throws Exception {
        String dbName = "tax_cust_cat_" + UUID.randomUUID().toString().replace("-", "");
        Session session = new Session("jdbc:hsqldb:mem:" + dbName + ";sql.syntax_mys=true", "SA", "");

        try {
            try (Statement st = session.getConnection().createStatement()) {
                st.executeUpdate("CREATE TABLE taxcustcategories ("
                        + "id VARCHAR(36) PRIMARY KEY, "
                        + "name VARCHAR(255) NOT NULL)");
            }

            DataLogicTax dataLogicTax = new DataLogicTax();
            dataLogicTax.init(session);

            TableDefinition<TaxCustCategoryInfo> tableTaxCustCategories = dataLogicTax.getTableTaxCustCategories();
            assertNotNull(tableTaxCustCategories);

            DefaultSaveProvider<TaxCustCategoryInfo> saveProvider = new DefaultSaveProvider<>(tableTaxCustCategories);
            assertTrue(saveProvider.canInsert());
            assertTrue(saveProvider.canUpdate());
            assertTrue(saveProvider.canDelete());

            // 1. Insert
            TaxCustCategoryInfo info = new TaxCustCategoryInfo("tcc-vip", "VIP Exempt");
            assertEquals(1, saveProvider.insertData(info));

            // 2. Query
            List<TaxCustCategoryInfo> list = tableTaxCustCategories.getListSentence().list();
            assertEquals(1, list.size());
            assertEquals("tcc-vip", list.get(0).getID());
            assertEquals("VIP Exempt", list.get(0).getName());

            // 3. Update
            info.setName("VIP Exempt (Updated)");
            assertEquals(1, saveProvider.updateData(info));

            List<TaxCustCategoryInfo> updatedList = tableTaxCustCategories.getListSentence().list();
            assertEquals("VIP Exempt (Updated)", updatedList.get(0).getName());

            // 4. Delete
            assertEquals(1, saveProvider.deleteData(info));
            assertTrue(tableTaxCustCategories.getListSentence().list().isEmpty());

        } finally {
            try (Statement st = session.getConnection().createStatement()) {
                st.execute("SHUTDOWN");
            } catch (Exception ignored) {
            }
        }
    }
}

