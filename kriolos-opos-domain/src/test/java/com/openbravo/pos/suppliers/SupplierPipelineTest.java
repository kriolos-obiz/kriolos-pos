package com.openbravo.pos.suppliers;

import com.openbravo.basic.BasicException;
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
 * Integration test suite verifying type-safe CRUD operations for SupplierInfo.
 *
 * @author KriolOS
 */
@DisplayName("Supplier Type-Safe Persistence Pipeline Test Suite")
class SupplierPipelineTest {

    @Test
    @DisplayName("Should execute type-safe CRUD operations for SupplierInfo via TableDefinition and DefaultSaveProvider")
    void shouldExecuteTypeSafeCrudForSuppliers() throws Exception {
        String dbName = "suppliers_test_" + UUID.randomUUID().toString().replace("-", "");
        Session session = new Session("jdbc:hsqldb:mem:" + dbName + ";sql.syntax_mys=true", "SA", "");

        try {
            try (Statement st = session.getConnection().createStatement()) {
                st.executeUpdate("CREATE TABLE suppliers ("
                        + "ID VARCHAR(36) PRIMARY KEY, "
                        + "SEARCHKEY VARCHAR(255) NOT NULL, "
                        + "TAXID VARCHAR(255), "
                        + "NAME VARCHAR(255) NOT NULL, "
                        + "MAXDEBT DOUBLE, "
                        + "ADDRESS VARCHAR(255), "
                        + "ADDRESS2 VARCHAR(255), "
                        + "POSTAL VARCHAR(255), "
                        + "CITY VARCHAR(255), "
                        + "REGION VARCHAR(255), "
                        + "COUNTRY VARCHAR(255), "
                        + "FIRSTNAME VARCHAR(255), "
                        + "LASTNAME VARCHAR(255), "
                        + "EMAIL VARCHAR(255), "
                        + "PHONE VARCHAR(255), "
                        + "PHONE2 VARCHAR(255), "
                        + "FAX VARCHAR(255), "
                        + "NOTES VARCHAR(1000), "
                        + "VISIBLE BOOLEAN NOT NULL, "
                        + "CURDATE TIMESTAMP, "
                        + "CURDEBT DOUBLE, "
                        + "VATID VARCHAR(255))");
            }

            DataLogicSuppliers dataLogic = new DataLogicSuppliers();
            dataLogic.init(session);

            TableDefinition<SupplierInfo> tableSuppliers = dataLogic.getTableSuppliers();
            assertNotNull(tableSuppliers);

            DefaultSaveProvider<SupplierInfo> saveProvider = new DefaultSaveProvider<>(tableSuppliers);
            assertTrue(saveProvider.canInsert());
            assertTrue(saveProvider.canUpdate());
            assertTrue(saveProvider.canDelete());

            // 1. Insert
            SupplierInfo supplier = new SupplierInfo("supp-1");
            supplier.setSearchkey("SUPP001");
            supplier.setTaxid("TAX-9988");
            supplier.setName("Acme Provisions Ltd");
            supplier.setMaxdebt(5000.0);
            supplier.setAddress("100 Industrial Parkway");
            supplier.setAddress2("Suite 4B");
            supplier.setPostal("CP-7000");
            supplier.setCity("Mindelo");
            supplier.setRegion("Sao Vicente");
            supplier.setCountry("Cabo Verde");
            supplier.setFirstname("Maria");
            supplier.setLastname("Silva");
            supplier.setEmail("orders@acmeprovisions.cv");
            supplier.setPhone("+238 232 1100");
            supplier.setPhone2("+238 991 2200");
            supplier.setFax("+238 232 1101");
            supplier.setNotes("Preferred seafood vendor");
            supplier.setVisible(true);
            Date now = new Date();
            supplier.setCurdate(now);
            supplier.setCurdebt(1250.50);
            supplier.setVatid("CV-123456789");

            assertEquals(1, saveProvider.insertData(supplier));

            // 2. Query
            List<SupplierInfo> list = tableSuppliers.getListSentence().list();
            assertEquals(1, list.size());
            SupplierInfo loaded = list.get(0);
            assertEquals("supp-1", loaded.getId());
            assertEquals("SUPP001", loaded.getSearchkey());
            assertEquals("TAX-9988", loaded.getTaxid());
            assertEquals("Acme Provisions Ltd", loaded.getName());
            assertEquals(5000.0, loaded.getMaxdebt());
            assertEquals("100 Industrial Parkway", loaded.getAddress());
            assertEquals("Suite 4B", loaded.getAddress2());
            assertEquals("CP-7000", loaded.getPostal());
            assertEquals("Mindelo", loaded.getCity());
            assertEquals("Sao Vicente", loaded.getRegion());
            assertEquals("Cabo Verde", loaded.getCountry());
            assertEquals("Maria", loaded.getFirstname());
            assertEquals("Silva", loaded.getLastname());
            assertEquals("orders@acmeprovisions.cv", loaded.getEmail());
            assertEquals("+238 232 1100", loaded.getPhone());
            assertEquals("+238 991 2200", loaded.getPhone2());
            assertEquals("+238 232 1101", loaded.getFax());
            assertEquals("Preferred seafood vendor", loaded.getNotes());
            assertTrue(loaded.isVisible());
            assertEquals(1250.50, loaded.getCurdebt());
            assertEquals("CV-123456789", loaded.getVatid());

            // 3. Update
            supplier.setName("Acme Global Provisions");
            supplier.setMaxdebt(10000.0);
            supplier.setNotes("Updated contract terms");
            assertEquals(1, saveProvider.updateData(supplier));

            List<SupplierInfo> updatedList = tableSuppliers.getListSentence().list();
            assertEquals("Acme Global Provisions", updatedList.get(0).getName());
            assertEquals(10000.0, updatedList.get(0).getMaxdebt());
            assertEquals("Updated contract terms", updatedList.get(0).getNotes());

            // 4. Delete
            assertEquals(1, saveProvider.deleteData(supplier));
            assertTrue(tableSuppliers.getListSentence().list().isEmpty());

        } finally {
            try (Statement st = session.getConnection().createStatement()) {
                st.execute("SHUTDOWN");
            } catch (Exception ignored) {
            }
        }
    }
}

