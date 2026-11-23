package com.openbravo.pos.admin;

import com.openbravo.data.loader.Session;
import com.openbravo.data.loader.TableDefinition;
import com.openbravo.data.user.DefaultSaveProvider;
import java.nio.charset.StandardCharsets;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test suite verifying type-safe CRUD operations for RoleInfo.
 *
 * @author KriolOS
 */
@DisplayName("Roles Type-Safe Persistence Pipeline Test Suite")
class RolesPipelineTest {

    @Test
    @DisplayName("Should execute type-safe CRUD operations for RoleInfo via TableDefinition and DefaultSaveProvider")
    void shouldExecuteTypeSafeCrudForRoles() throws Exception {
        String dbName = "roles_test_" + UUID.randomUUID().toString().replace("-", "");
        Session session = new Session("jdbc:hsqldb:mem:" + dbName + ";sql.syntax_mys=true", "SA", "");

        try {
            try (Statement st = session.getConnection().createStatement()) {
                st.executeUpdate("CREATE TABLE roles ("
                        + "id VARCHAR(36) PRIMARY KEY, "
                        + "name VARCHAR(255) NOT NULL, "
                        + "permissions VARBINARY(16777215))");
            }

            DataLogicAdmin dataLogic = new DataLogicAdmin();
            dataLogic.init(session);

            TableDefinition<RoleInfo> tableRoles = dataLogic.getTableRoles();
            assertNotNull(tableRoles);

            DefaultSaveProvider<RoleInfo> saveProvider = new DefaultSaveProvider<>(tableRoles);
            assertTrue(saveProvider.canInsert());
            assertTrue(saveProvider.canUpdate());
            assertTrue(saveProvider.canDelete());

            // 1. Insert
            byte[] permissionsXml = "<permissions><class name=\"sales\"/></permissions>".getBytes(StandardCharsets.UTF_8);
            RoleInfo role = new RoleInfo("role-cashier", "Cashier", permissionsXml);
            assertEquals(1, saveProvider.insertData(role));

            // 2. Query TableDefinition (3 columns including permissions)
            List<RoleInfo> list = tableRoles.getListSentence().list();
            assertEquals(1, list.size());
            assertEquals("role-cashier", list.get(0).getID());
            assertEquals("Cashier", list.get(0).getName());
            assertNotNull(list.get(0).getPermissions());
            assertArrayEquals(permissionsXml, list.get(0).getPermissions());

            // 2b. Query 2-column SentenceList used for user combo box dropdowns (no column out of bounds)
            List<RoleInfo> comboList = dataLogic.getRolesList().list();
            assertEquals(1, comboList.size());
            assertEquals("role-cashier", comboList.get(0).getID());
            assertEquals("Cashier", comboList.get(0).getName());

            // 3. Update
            byte[] updatedXml = "<permissions><class name=\"sales\"/><class name=\"refund\"/></permissions>".getBytes(StandardCharsets.UTF_8);
            role.setName("Senior Cashier");
            role.setPermissions(updatedXml);
            assertEquals(1, saveProvider.updateData(role));

            List<RoleInfo> updatedList = tableRoles.getListSentence().list();
            assertEquals("Senior Cashier", updatedList.get(0).getName());
            assertArrayEquals(updatedXml, updatedList.get(0).getPermissions());

            // 4. Delete
            assertEquals(1, saveProvider.deleteData(role));
            assertTrue(tableRoles.getListSentence().list().isEmpty());

        } finally {
            try (Statement st = session.getConnection().createStatement()) {
                st.execute("SHUTDOWN");
            } catch (Exception ignored) {
            }
        }
    }
}

