package com.openbravo.pos.epm;

import com.openbravo.data.loader.Session;
import com.openbravo.data.loader.TableDefinition;
import com.openbravo.data.user.DefaultSaveProvider;
import java.sql.Statement;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test suite verifying type-safe CRUD operations for BreaksInfo and LeavesInfo.
 *
 * @author KriolOS
 */
@DisplayName("EPM Breaks and Leaves Type-Safe Persistence Pipeline Test Suite")
class EpmPipelineTest {

    @Test
    @DisplayName("Should execute type-safe CRUD operations for BreaksInfo via TableDefinition and DefaultSaveProvider")
    void shouldExecuteTypeSafeCrudForBreaks() throws Exception {
        String dbName = "epm_breaks_test_" + UUID.randomUUID().toString().replace("-", "");
        Session session = new Session("jdbc:hsqldb:mem:" + dbName + ";sql.syntax_mys=true", "SA", "");

        try {
            try (Statement st = session.getConnection().createStatement()) {
                st.executeUpdate("CREATE TABLE breaks ("
                        + "id VARCHAR(36) PRIMARY KEY, "
                        + "name VARCHAR(255) NOT NULL, "
                        + "notes VARCHAR(255), "
                        + "visible BOOLEAN DEFAULT TRUE NOT NULL)");
                st.executeUpdate("CREATE TABLE leaves ("
                        + "id VARCHAR(36) PRIMARY KEY, "
                        + "pplid VARCHAR(36), "
                        + "name VARCHAR(255), "
                        + "startdate TIMESTAMP, "
                        + "enddate TIMESTAMP, "
                        + "notes VARCHAR(255))");
                st.executeUpdate("CREATE TABLE shifts ("
                        + "id VARCHAR(36) PRIMARY KEY, "
                        + "startshift TIMESTAMP, "
                        + "endshift TIMESTAMP, "
                        + "pplid VARCHAR(36))");
                st.executeUpdate("CREATE TABLE shift_breaks ("
                        + "id VARCHAR(36) PRIMARY KEY, "
                        + "shiftid VARCHAR(36), "
                        + "breakid VARCHAR(36), "
                        + "starttime TIMESTAMP, "
                        + "endtime TIMESTAMP)");
                st.executeUpdate("CREATE TABLE people ("
                        + "id VARCHAR(36) PRIMARY KEY, "
                        + "name VARCHAR(255), "
                        + "apppassword VARCHAR(255), "
                        + "card VARCHAR(255), "
                        + "role VARCHAR(36), "
                        + "visible BOOLEAN)");
            }

            DataLogicPresenceManagement dlPresence = new DataLogicPresenceManagement();
            dlPresence.init(session);

            TableDefinition<BreaksInfo> tableBreaks = dlPresence.getTableBreaks();
            assertNotNull(tableBreaks);

            DefaultSaveProvider<BreaksInfo> saveProvider = new DefaultSaveProvider<>(tableBreaks);
            assertTrue(saveProvider.canInsert());
            assertTrue(saveProvider.canUpdate());
            assertTrue(saveProvider.canDelete());

            // 1. Insert
            BreaksInfo breakItem = new BreaksInfo("b-lunch", "Lunch Break", "Standard 1h meal break", true);
            assertEquals(1, saveProvider.insertData(breakItem));

            // 2. Query
            List<BreaksInfo> breaksList = tableBreaks.getListSentence().list();
            assertEquals(1, breaksList.size());
            assertEquals("b-lunch", breaksList.get(0).getId());
            assertEquals("Lunch Break", breaksList.get(0).getName());
            assertEquals("Standard 1h meal break", breaksList.get(0).getNotes());
            assertTrue(breaksList.get(0).isVisible());

            // 3. Update
            breakItem.setName("Extended Lunch");
            breakItem.setNotes("45min meal break");
            breakItem.setVisible(false);
            assertEquals(1, saveProvider.updateData(breakItem));

            List<BreaksInfo> updatedList = tableBreaks.getListSentence().list();
            assertEquals("Extended Lunch", updatedList.get(0).getName());
            assertEquals("45min meal break", updatedList.get(0).getNotes());
            assertFalse(updatedList.get(0).isVisible());

            // 4. Delete
            assertEquals(1, saveProvider.deleteData(breakItem));
            assertTrue(tableBreaks.getListSentence().list().isEmpty());

        } finally {
            try (Statement st = session.getConnection().createStatement()) {
                st.execute("SHUTDOWN");
            } catch (Exception ignored) {
            }
        }
    }

    @Test
    @DisplayName("Should execute type-safe CRUD operations for LeavesInfo via TableDefinition and DefaultSaveProvider")
    void shouldExecuteTypeSafeCrudForLeaves() throws Exception {
        String dbName = "epm_leaves_test_" + UUID.randomUUID().toString().replace("-", "");
        Session session = new Session("jdbc:hsqldb:mem:" + dbName + ";sql.syntax_mys=true", "SA", "");

        try {
            try (Statement st = session.getConnection().createStatement()) {
                st.executeUpdate("CREATE TABLE breaks ("
                        + "id VARCHAR(36) PRIMARY KEY, "
                        + "name VARCHAR(255) NOT NULL, "
                        + "notes VARCHAR(255), "
                        + "visible BOOLEAN DEFAULT TRUE NOT NULL)");
                st.executeUpdate("CREATE TABLE leaves ("
                        + "id VARCHAR(36) PRIMARY KEY, "
                        + "pplid VARCHAR(36), "
                        + "name VARCHAR(255), "
                        + "startdate TIMESTAMP, "
                        + "enddate TIMESTAMP, "
                        + "notes VARCHAR(255))");
                st.executeUpdate("CREATE TABLE shifts ("
                        + "id VARCHAR(36) PRIMARY KEY, "
                        + "startshift TIMESTAMP, "
                        + "endshift TIMESTAMP, "
                        + "pplid VARCHAR(36))");
                st.executeUpdate("CREATE TABLE shift_breaks ("
                        + "id VARCHAR(36) PRIMARY KEY, "
                        + "shiftid VARCHAR(36), "
                        + "breakid VARCHAR(36), "
                        + "starttime TIMESTAMP, "
                        + "endtime TIMESTAMP)");
                st.executeUpdate("CREATE TABLE people ("
                        + "id VARCHAR(36) PRIMARY KEY, "
                        + "name VARCHAR(255), "
                        + "apppassword VARCHAR(255), "
                        + "card VARCHAR(255), "
                        + "role VARCHAR(36), "
                        + "visible BOOLEAN)");
            }

            DataLogicPresenceManagement dlPresence = new DataLogicPresenceManagement();
            dlPresence.init(session);

            TableDefinition<LeavesInfo> tableLeaves = dlPresence.getTableLeaves();
            assertNotNull(tableLeaves);

            DefaultSaveProvider<LeavesInfo> saveProvider = new DefaultSaveProvider<>(tableLeaves);
            assertTrue(saveProvider.canInsert());
            assertTrue(saveProvider.canUpdate());
            assertTrue(saveProvider.canDelete());

            // 1. Insert
            long nowMs = System.currentTimeMillis();
            Date start = new Date(nowMs);
            Date end = new Date(nowMs + 86400000L); // +1 day
            LeavesInfo leave = new LeavesInfo("leave-001", "emp-100", "John Doe", start, end, "Annual Vacation");
            assertEquals(1, saveProvider.insertData(leave));

            // 2. Query
            List<LeavesInfo> leavesList = tableLeaves.getListSentence().list();
            assertEquals(1, leavesList.size());
            assertEquals("leave-001", leavesList.get(0).getID());
            assertEquals("emp-100", leavesList.get(0).getPplId());
            assertEquals("John Doe", leavesList.get(0).getName());
            assertEquals("Annual Vacation", leavesList.get(0).getNotes());
            assertNotNull(leavesList.get(0).getStartDate());
            assertNotNull(leavesList.get(0).getEndDate());

            // 3. Update
            leave.setNotes("Extended Vacation Approved");
            assertEquals(1, saveProvider.updateData(leave));

            List<LeavesInfo> updatedList = tableLeaves.getListSentence().list();
            assertEquals("Extended Vacation Approved", updatedList.get(0).getNotes());

            // 4. Delete
            assertEquals(1, saveProvider.deleteData(leave));
            assertTrue(tableLeaves.getListSentence().list().isEmpty());

        } finally {
            try (Statement st = session.getConnection().createStatement()) {
                st.execute("SHUTDOWN");
            } catch (Exception ignored) {
            }
        }
    }
}

