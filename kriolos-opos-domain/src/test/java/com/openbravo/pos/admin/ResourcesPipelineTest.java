package com.openbravo.pos.admin;

import com.openbravo.data.loader.Session;
import com.openbravo.data.loader.TableDefinition;
import com.openbravo.data.user.SaveProvider;
import java.nio.charset.StandardCharsets;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test suite verifying type-safe CRUD operations for ResourceInfo.
 *
 * @author KriolOS
 */
@DisplayName("Resources Type-Safe Persistence Pipeline Test Suite")
class ResourcesPipelineTest {

    @Test
    @DisplayName("Should execute type-safe CRUD operations for ResourceInfo via TableDefinition and DefaultSaveProvider")
    void shouldExecuteTypeSafeCrudForResources() throws Exception {
        String dbName = "res_test_" + UUID.randomUUID().toString().replace("-", "");
        Session session = new Session("jdbc:hsqldb:mem:" + dbName + ";sql.syntax_mys=true", "SA", "");

        try {
            try (Statement st = session.getConnection().createStatement()) {
                st.executeUpdate("CREATE TABLE resources ("
                        + "id VARCHAR(36) PRIMARY KEY, "
                        + "name VARCHAR(255) NOT NULL, "
                        + "restype INT NOT NULL, "
                        + "content VARBINARY(16777215))");
            }

            DataLogicSystem dataLogic = new DataLogicSystem();
            dataLogic.init(session);

            TableDefinition<ResourceInfo> tableResources = dataLogic.getTableResources();
            assertNotNull(tableResources);

            SaveProvider<ResourceInfo> saveProvider = dataLogic.getResourceSaveProvider();
            assertTrue(saveProvider.canInsert());
            assertTrue(saveProvider.canUpdate());
            assertTrue(saveProvider.canDelete());

            // 1. Insert Text Resource
            byte[] textPayload = "<ticket-template>Print Test</ticket-template>".getBytes(StandardCharsets.UTF_8);
            ResourceInfo resText = new ResourceInfo("res-001", "Printer.Ticket", 0, textPayload);
            assertEquals(1, saveProvider.insertData(resText));

            // 2. Query TableDefinition (4 columns)
            List<ResourceInfo> list = tableResources.getListSentence().list();
            assertEquals(1, list.size());
            ResourceInfo loaded = list.get(0);
            assertEquals("res-001", loaded.getId());
            assertEquals("Printer.Ticket", loaded.getName());
            assertEquals(0, loaded.getRestype());
            assertNotNull(loaded.getContent());
            assertArrayEquals(textPayload, loaded.getContent());

            // 2b. Query SentenceList used in DataLogicAdmin (SELECT ID, NAME, RESTYPE, CONTENT FROM resources ORDER BY NAME)
            DataLogicAdmin adminLogic = new DataLogicAdmin();
            adminLogic.init(session);
            List<ResourceInfo> adminList = adminLogic.getResourceList().list();
            assertEquals(1, adminList.size());
            assertEquals("res-001", adminList.get(0).getId());
            assertEquals("Printer.Ticket", adminList.get(0).getName());

            // 3. Update
            byte[] updatedPayload = "<ticket-template>Updated</ticket-template>".getBytes(StandardCharsets.UTF_8);
            resText.setContent(updatedPayload);
            assertEquals(1, saveProvider.updateData(resText));

            List<ResourceInfo> updatedList = tableResources.getListSentence().list();
            assertEquals(1, updatedList.size());
            assertArrayEquals(updatedPayload, updatedList.get(0).getContent());

            // 4. Delete
            assertEquals(1, saveProvider.deleteData(resText));
            assertTrue(tableResources.getListSentence().list().isEmpty());

        } finally {
            try (Statement st = session.getConnection().createStatement()) {
                st.execute("SHUTDOWN");
            } catch (Exception ignored) {
            }
        }
    }
}
