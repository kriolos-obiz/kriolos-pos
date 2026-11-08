package com.openbravo.pos.admin;

import com.openbravo.data.loader.Session;
import com.openbravo.data.loader.TableDefinition;
import com.openbravo.data.user.SaveProvider;
import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test suite verifying type-safe CRUD operations for PeopleInfo.
 *
 * @author KriolOS
 */
@DisplayName("People Type-Safe Persistence Pipeline Test Suite")
class PeoplePipelineTest {

    @Test
    @DisplayName("Should execute type-safe CRUD operations for PeopleInfo via TableDefinition and DefaultSaveProvider")
    void shouldExecuteTypeSafeCrudForPeople() throws Exception {
        String dbName = "people_test_" + UUID.randomUUID().toString().replace("-", "");
        Session session = new Session("jdbc:hsqldb:mem:" + dbName + ";sql.syntax_mys=true", "SA", "");

        try {
            try (Statement st = session.getConnection().createStatement()) {
                st.executeUpdate("CREATE TABLE roles ("
                        + "id VARCHAR(36) PRIMARY KEY, "
                        + "name VARCHAR(255) NOT NULL, "
                        + "permissions VARBINARY(16777215))");

                st.executeUpdate("CREATE TABLE people ("
                        + "id VARCHAR(36) PRIMARY KEY, "
                        + "name VARCHAR(255) NOT NULL, "
                        + "apppassword VARCHAR(255), "
                        + "role VARCHAR(36) NOT NULL, "
                        + "visible BOOLEAN NOT NULL, "
                        + "card VARCHAR(255), "
                        + "image VARBINARY(16777215))");
            }

            DataLogicAdmin dataLogic = new DataLogicAdmin();
            dataLogic.init(session);

            TableDefinition<PeopleInfo> tablePeople = dataLogic.getTablePeople();
            assertNotNull(tablePeople);

            SaveProvider<PeopleInfo> saveProvider = dataLogic.getPeopleSaveProvider();
            assertTrue(saveProvider.canInsert());
            assertTrue(saveProvider.canUpdate());
            assertTrue(saveProvider.canDelete());

            // 1. Insert Role prerequisite and Person
            RoleInfo role = new RoleInfo("role-cashier", "Cashier", "<permissions/>".getBytes(StandardCharsets.UTF_8));
            dataLogic.getTableRoles().getInsertSentence().exec(role);

            BufferedImage testImage = new BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB);
            PeopleInfo person = new PeopleInfo("user-001", "Alice Admin", "sha1:hash123", "role-cashier", true, "1234567890123456", testImage);
            assertEquals(1, saveProvider.insertData(person));

            // 2. Query TableDefinition (7 columns)
            List<PeopleInfo> list = tablePeople.getListSentence().list();
            assertEquals(1, list.size());
            PeopleInfo loaded = list.get(0);
            assertEquals("user-001", loaded.getId());
            assertEquals("Alice Admin", loaded.getName());
            assertEquals("sha1:hash123", loaded.getAppPassword());
            assertEquals("sha1:hash123", loaded.getPassword());
            assertEquals("role-cashier", loaded.getRole());
            assertTrue(loaded.isVisible());
            assertEquals("1234567890123456", loaded.getCard());
            assertNotNull(loaded.getImage());

            // 2b. Query 2-column SentenceList used for user combo box dropdowns (no column out of bounds)
            List<PeopleInfo> comboList = dataLogic.getPeopleList().list();
            assertEquals(1, comboList.size());
            assertEquals("user-001", comboList.get(0).getId());
            assertEquals("Alice Admin", comboList.get(0).getName());

            // 3. Update
            person.setName("Alice Supervisor");
            person.setAppPassword("sha1:updatedHash");
            person.setCard("6543210987654321");
            person.setVisible(false);
            assertEquals(1, saveProvider.updateData(person));

            List<PeopleInfo> updatedList = tablePeople.getListSentence().list();
            assertEquals(1, updatedList.size());
            PeopleInfo updated = updatedList.get(0);
            assertEquals("Alice Supervisor", updated.getName());
            assertEquals("sha1:updatedHash", updated.getAppPassword());
            assertEquals("6543210987654321", updated.getCard());
            assertFalse(updated.isVisible());

            // 4. Delete
            assertEquals(1, saveProvider.deleteData(person));
            assertTrue(tablePeople.getListSentence().list().isEmpty());

        } finally {
            try (Statement st = session.getConnection().createStatement()) {
                st.execute("SHUTDOWN");
            } catch (Exception ignored) {
            }
        }
    }
}

