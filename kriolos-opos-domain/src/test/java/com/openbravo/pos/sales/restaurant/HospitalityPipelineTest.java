package com.openbravo.pos.sales.restaurant;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.Session;
import com.openbravo.data.loader.TableDefinition;
import com.openbravo.data.user.DefaultSaveProvider;
import java.awt.image.BufferedImage;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test suite verifying type-safe CRUD operations for Floors and Places.
 *
 * @author KriolOS
 */
@DisplayName("Hospitality Floors & Places Type-Safe Persistence Pipeline Test Suite")
class HospitalityPipelineTest {

    @Test
    @DisplayName("Should execute type-safe CRUD operations for FloorsInfo including floor image")
    void shouldExecuteTypeSafeCrudForFloors() throws Exception {
        String dbName = "floors_test_" + UUID.randomUUID().toString().replace("-", "");
        Session session = new Session("jdbc:hsqldb:mem:" + dbName + ";sql.syntax_mys=true", "SA", "");

        try {
            try (Statement st = session.getConnection().createStatement()) {
                st.executeUpdate("CREATE TABLE floors ("
                        + "id VARCHAR(36) PRIMARY KEY, "
                        + "name VARCHAR(255) NOT NULL, "
                        + "image VARBINARY(16777215))");
            }

            DataLogicRestaurant dataLogic = new DataLogicRestaurant();
            dataLogic.init(session);

            TableDefinition<FloorsInfo> tableFloors = dataLogic.getTableFloors();
            assertNotNull(tableFloors);

            DefaultSaveProvider<FloorsInfo> saveProvider = new DefaultSaveProvider<>(tableFloors);
            assertTrue(saveProvider.canInsert());
            assertTrue(saveProvider.canUpdate());
            assertTrue(saveProvider.canDelete());

            // 1. Insert floor with image
            BufferedImage img = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
            FloorsInfo floor = new FloorsInfo("floor-1", "Ground Floor", img);
            assertEquals(1, saveProvider.insertData(floor));

            // 2. Query TableDefinition (3 columns with image)
            List<FloorsInfo> list = tableFloors.getListSentence().list();
            assertEquals(1, list.size());
            assertEquals("floor-1", list.get(0).getID());
            assertEquals("Ground Floor", list.get(0).getName());
            assertNotNull(list.get(0).getImage());

            // 2b. Query 2-column SentenceList used for combo box dropdowns (no column 3 out of bounds)
            List<FloorsInfo> allFloors = dataLogic.getFloorsListAll();
            assertEquals(1, allFloors.size());
            assertEquals("floor-1", allFloors.get(0).getID());
            assertEquals("Ground Floor", allFloors.get(0).getName());

            // 3. Update
            floor.setName("Main Floor");
            assertEquals(1, saveProvider.updateData(floor));

            List<FloorsInfo> updatedList = tableFloors.getListSentence().list();
            assertEquals("Main Floor", updatedList.get(0).getName());

            // 4. Delete
            assertEquals(1, saveProvider.deleteData(floor));
            assertTrue(tableFloors.getListSentence().list().isEmpty());

        } finally {
            try (Statement st = session.getConnection().createStatement()) {
                st.execute("SHUTDOWN");
            } catch (Exception ignored) {
            }
        }
    }

    @Test
    @DisplayName("Should execute type-safe CRUD operations for Place")
    void shouldExecuteTypeSafeCrudForPlaces() throws Exception {
        String dbName = "places_test_" + UUID.randomUUID().toString().replace("-", "");
        Session session = new Session("jdbc:hsqldb:mem:" + dbName + ";sql.syntax_mys=true", "SA", "");

        try {
            try (Statement st = session.getConnection().createStatement()) {
                st.executeUpdate("CREATE TABLE places ("
                        + "id VARCHAR(36) PRIMARY KEY, "
                        + "name VARCHAR(255) NOT NULL, "
                        + "seats VARCHAR(10), "
                        + "x INT, "
                        + "y INT, "
                        + "floor VARCHAR(36))");
            }

            DataLogicRestaurant dataLogic = new DataLogicRestaurant();
            dataLogic.init(session);

            TableDefinition<Place> tablePlaces = dataLogic.getTablePlaces();
            assertNotNull(tablePlaces);

            DefaultSaveProvider<Place> saveProvider = new DefaultSaveProvider<>(tablePlaces);
            assertTrue(saveProvider.canInsert());
            assertTrue(saveProvider.canUpdate());
            assertTrue(saveProvider.canDelete());

            // 1. Insert
            Place place = new Place("table-1", "Table 1", "4", 100, 200, "floor-1");
            assertEquals(1, saveProvider.insertData(place));

            // 2. Query
            List<Place> list = tablePlaces.getListSentence().list();
            assertEquals(1, list.size());
            assertEquals("table-1", list.get(0).getId());
            assertEquals("Table 1", list.get(0).getName());
            assertEquals("4", list.get(0).getSeats());
            assertEquals(100, list.get(0).getX());
            assertEquals(200, list.get(0).getY());
            assertEquals("floor-1", list.get(0).getFloor());

            // 2b. Query floor tables list via getFloorTablesListAll() (SELECT ID, NAME, SEATS FROM places)
            List<FloorsInfo> tableFloorsList = dataLogic.getFloorTablesListAll();
            assertEquals(1, tableFloorsList.size());
            assertEquals("table-1", tableFloorsList.get(0).getID());
            assertEquals("Table 1", tableFloorsList.get(0).getName());

            // 3. Update coordinates
            place.setX(150);
            place.setY(250);
            place.setName("Table 1 (Window)");
            assertEquals(1, saveProvider.updateData(place));

            List<Place> updatedList = tablePlaces.getListSentence().list();
            assertEquals(150, updatedList.get(0).getX());
            assertEquals(250, updatedList.get(0).getY());
            assertEquals("Table 1 (Window)", updatedList.get(0).getName());

            // 4. Delete
            assertEquals(1, saveProvider.deleteData(place));
            assertTrue(tablePlaces.getListSentence().list().isEmpty());

        } finally {
            try (Statement st = session.getConnection().createStatement()) {
                st.execute("SHUTDOWN");
            } catch (Exception ignored) {
            }
        }
    }
}

