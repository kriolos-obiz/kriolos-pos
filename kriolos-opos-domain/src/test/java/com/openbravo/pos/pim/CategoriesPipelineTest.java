package com.openbravo.pos.pim;

import com.openbravo.data.loader.Session;
import com.openbravo.data.loader.TableDefinition;
import com.openbravo.data.user.DefaultSaveProvider;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test suite verifying type-safe persistence operations for CategoryInfo.
 *
 * @author KriolOS
 */
@DisplayName("Categories Type-Safe Persistence Pipeline Test Suite")
class CategoriesPipelineTest {

    @Test
    @DisplayName("Should execute type-safe CRUD operations for CategoryInfo via TableDefinition and DefaultSaveProvider")
    void shouldExecuteTypeSafeCrudForCategories() throws Exception {
        String dbName = "categories_test_" + UUID.randomUUID().toString().replace("-", "");
        Session session = new Session("jdbc:hsqldb:mem:" + dbName + ";sql.syntax_mys=true", "SA", "");

        try {
            try (Statement st = session.getConnection().createStatement()) {
                st.executeUpdate("CREATE TABLE categories ("
                        + "ID VARCHAR(36) PRIMARY KEY, "
                        + "NAME VARCHAR(255) NOT NULL, "
                        + "IMAGE VARBINARY(16777215), "
                        + "TEXTTIP VARCHAR(255), "
                        + "CATSHOWNAME BOOLEAN, "
                        + "CATORDER VARCHAR(255), "
                        + "CATALOGCOLOR VARCHAR(255), "
                        + "CATALOGENABLED BOOLEAN, "
                        + "PARENTID VARCHAR(36))");
            }

            DataLogicCategories dlCategories = new DataLogicCategories();
            dlCategories.init(session);

            TableDefinition<CategoryInfo> tableCategories = dlCategories.getTableCategories();
            assertNotNull(tableCategories);

            DefaultSaveProvider<CategoryInfo> saveProvider = new DefaultSaveProvider<>(tableCategories);
            assertTrue(saveProvider.canInsert());
            assertTrue(saveProvider.canUpdate());
            assertTrue(saveProvider.canDelete());

            // 1. Insert root category
            CategoryInfo beverages = new CategoryInfo("cat-bev", "Beverages", null, "Refreshing drinks", true);
            beverages.setCatalogOrder("01");
            beverages.setCatalogColor("#00FF00");
            beverages.setCatalogEnabled(true);
            assertEquals(1, saveProvider.insertData(beverages));

            // 2. Query via TableDefinition
            List<CategoryInfo> list = tableCategories.getListSentence().list();
            assertEquals(1, list.size());
            assertEquals("cat-bev", list.get(0).getId());
            assertEquals("Beverages", list.get(0).getName());
            assertEquals("Refreshing drinks", list.get(0).getTextTip());
            assertEquals("01", list.get(0).getCatalogOrder());
            assertEquals("#00FF00", list.get(0).getCatalogColor());
            assertTrue(list.get(0).isCatShowName());
            assertTrue(list.get(0).isCatalogEnabled());
            assertNull(list.get(0).getParentId());

            // 2b. Query via getCategoriesList()
            List<CategoryInfo> queryList = dlCategories.getCategoriesList().list();
            assertEquals(1, queryList.size());
            assertEquals("cat-bev", queryList.get(0).getId());
            assertEquals("Beverages", queryList.get(0).getName());

            // 3. Update
            beverages.setName("Cold Beverages");
            beverages.setTextTip("Chilled drinks and juices");
            beverages.setCatalogColor("#0000FF");
            assertEquals(1, saveProvider.updateData(beverages));

            List<CategoryInfo> updatedList = tableCategories.getListSentence().list();
            assertEquals(1, updatedList.size());
            assertEquals("Cold Beverages", updatedList.get(0).getName());
            assertEquals("Chilled drinks and juices", updatedList.get(0).getTextTip());
            assertEquals("#0000FF", updatedList.get(0).getCatalogColor());

            // 4. Insert subcategory
            CategoryInfo juices = new CategoryInfo("cat-juice", "Juices", null, "Fresh fruit juices", true);
            juices.setParentId("cat-bev");
            juices.setCatalogOrder("02");
            assertEquals(1, saveProvider.insertData(juices));

            List<CategoryInfo> subcategories = dlCategories.getSubcategories("cat-bev");
            assertEquals(1, subcategories.size());
            assertEquals("cat-juice", subcategories.get(0).getId());
            assertEquals("cat-bev", subcategories.get(0).getParentId());

            // 5. Delete
            assertEquals(1, saveProvider.deleteData(juices));
            assertEquals(1, saveProvider.deleteData(beverages));
            assertTrue(tableCategories.getListSentence().list().isEmpty());

        } finally {
            try (Statement st = session.getConnection().createStatement()) {
                st.execute("SHUTDOWN");
            } catch (Exception ignored) {
            }
        }
    }
}

