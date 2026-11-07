package com.openbravo.pos.inventory;

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
 * Integration test suite verifying type-safe persistence operations for LocationInfo.
 *
 * @author KriolOS
 */
@DisplayName("Locations Type-Safe Persistence Pipeline Test Suite")
class LocationsPipelineTest {

    @Test
    @DisplayName("Should execute type-safe CRUD operations for LocationInfo via TableDefinition and DefaultSaveProvider")
    void shouldExecuteTypeSafeCrudForLocations() throws Exception {
        String dbName = "locations_test_" + UUID.randomUUID().toString().replace("-", "");
        Session session = new Session("jdbc:hsqldb:mem:" + dbName + ";sql.syntax_mys=true", "SA", "");

        try {
            try (Statement st = session.getConnection().createStatement()) {
                st.executeUpdate("CREATE TABLE locations ("
                        + "id VARCHAR(36) PRIMARY KEY, "
                        + "name VARCHAR(255) NOT NULL, "
                        + "address VARCHAR(255))");
            }

            DataLogicInventory dlInventory = new DataLogicInventory();
            dlInventory.init(session);

            TableDefinition<LocationInfo> tableLocations = dlInventory.getTableLocations();
            assertNotNull(tableLocations);

            DefaultSaveProvider<LocationInfo> saveProvider = new DefaultSaveProvider<>(tableLocations);
            assertTrue(saveProvider.canInsert());
            assertTrue(saveProvider.canUpdate());
            assertTrue(saveProvider.canDelete());

            // 1. Insert
            LocationInfo loc = new LocationInfo("loc-main", "Main Warehouse", "Avenida Principal 123");
            assertEquals(1, saveProvider.insertData(loc));

            // 2. Query via TableDefinition
            List<LocationInfo> list = tableLocations.getListSentence().list();
            assertEquals(1, list.size());
            assertEquals("loc-main", list.get(0).getId());
            assertEquals("Main Warehouse", list.get(0).getName());
            assertEquals("Avenida Principal 123", list.get(0).getAddress());

            // 2b. Query via getLocationsList() (combo box lookup)
            List<LocationInfo> comboList = dlInventory.getLocationsList();
            assertEquals(1, comboList.size());
            assertEquals("loc-main", comboList.get(0).getId());
            assertEquals("Main Warehouse", comboList.get(0).getName());
            assertEquals("Avenida Principal 123", comboList.get(0).getAddress());

            // 3. Update
            loc.setName("Central Logistics Hub");
            loc.setAddress("Industrial Park Building B");
            assertEquals(1, saveProvider.updateData(loc));

            List<LocationInfo> updatedList = tableLocations.getListSentence().list();
            assertEquals(1, updatedList.size());
            assertEquals("Central Logistics Hub", updatedList.get(0).getName());
            assertEquals("Industrial Park Building B", updatedList.get(0).getAddress());

            // 4. Delete
            assertEquals(1, saveProvider.deleteData(loc));
            assertTrue(tableLocations.getListSentence().list().isEmpty());

        } finally {
            try (Statement st = session.getConnection().createStatement()) {
                st.execute("SHUTDOWN");
            } catch (Exception ignored) {
            }
        }
    }
}

