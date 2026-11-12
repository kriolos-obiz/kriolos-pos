package com.openbravo.pos.customers;

import com.openbravo.data.loader.Session;
import com.openbravo.data.loader.TableDefinition;
import com.openbravo.data.user.SaveProvider;
import java.sql.Statement;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test suite verifying type-safe CRUD operations for CustomerInfoExt.
 *
 * @author KriolOS
 */
@DisplayName("Customer Type-Safe Persistence Pipeline Test Suite")
class CustomerPipelineTest {

    @Test
    @DisplayName("Should execute type-safe CRUD operations for CustomerInfoExt via TableDefinition and DefaultSaveProvider")
    void shouldExecuteTypeSafeCrudForCustomers() throws Exception {
        String dbName = "cust_test_" + UUID.randomUUID().toString().replace("-", "");
        Session session = new Session("jdbc:hsqldb:mem:" + dbName + ";sql.syntax_mys=true", "SA", "");

        try {
            try (Statement st = session.getConnection().createStatement()) {
                st.executeUpdate("CREATE TABLE customers ("
                        + "id VARCHAR(36) PRIMARY KEY, "
                        + "searchkey VARCHAR(255), "
                        + "taxid VARCHAR(255), "
                        + "name VARCHAR(255) NOT NULL, "
                        + "taxcategory VARCHAR(36), "
                        + "card VARCHAR(255), "
                        + "maxdebt DOUBLE, "
                        + "address VARCHAR(255), "
                        + "address2 VARCHAR(255), "
                        + "postal VARCHAR(255), "
                        + "city VARCHAR(255), "
                        + "region VARCHAR(255), "
                        + "country VARCHAR(255), "
                        + "firstname VARCHAR(255), "
                        + "lastname VARCHAR(255), "
                        + "email VARCHAR(255), "
                        + "phone VARCHAR(255), "
                        + "phone2 VARCHAR(255), "
                        + "fax VARCHAR(255), "
                        + "notes VARCHAR(1000), "
                        + "visible BOOLEAN NOT NULL, "
                        + "curdate TIMESTAMP, "
                        + "curdebt DOUBLE, "
                        + "image VARBINARY(16777215), "
                        + "isvip BOOLEAN NOT NULL, "
                        + "discount DOUBLE, "
                        + "memodate TIMESTAMP)");
            }

            DataLogicCustomers dataLogic = new DataLogicCustomers();
            dataLogic.init(session);

            TableDefinition<CustomerInfoExt> tableCustomers = dataLogic.getTableCustomers();
            assertNotNull(tableCustomers);

            SaveProvider<CustomerInfoExt> saveProvider = dataLogic.getCustomerSaveProvider();
            assertNotNull(saveProvider);
            assertTrue(saveProvider.canInsert());
            assertTrue(saveProvider.canUpdate());
            assertTrue(saveProvider.canDelete());

            // 1. Create and Insert Customer
            CustomerInfoExt cust = new CustomerInfoExt("cust-001");
            cust.setSearchkey("ACME");
            cust.setTaxid("TAX-999");
            cust.setName("Acme Corporation");
            cust.setTaxCustCategoryID("tc-retail");
            cust.setCard("CARD-12345");
            cust.setMaxdebt(5000.0);
            cust.setAddress("100 Industrial Way");
            cust.setAddress2("Suite 200");
            cust.setPostal("12345");
            cust.setCity("Metropolis");
            cust.setRegion("NY");
            cust.setCountry("USA");
            cust.setFirstname("John");
            cust.setLastname("Doe");
            cust.setEmail("johndoe@acme.com");
            cust.setPhone("555-0100");
            cust.setPhone2("555-0101");
            cust.setFax("555-0102");
            cust.setNotes("Preferred corporate customer");
            cust.setVisible(true);
            Date now = new Date();
            cust.setCurdate(now);
            cust.setCurdebt(150.0);
            cust.setisVIP(true);
            cust.setDiscount(0.10);
            cust.setMemoDate(now);

            assertEquals(1, saveProvider.insertData(cust));

            // 2. Query TableDefinition
            List<CustomerInfoExt> list = tableCustomers.getListSentence().list();
            assertEquals(1, list.size());
            CustomerInfoExt loaded = list.get(0);
            assertEquals("cust-001", loaded.getId());
            assertEquals("ACME", loaded.getSearchkey());
            assertEquals("TAX-999", loaded.getTaxid());
            assertEquals("Acme Corporation", loaded.getName());
            assertEquals("tc-retail", loaded.getTaxCustCategoryID());
            assertEquals("CARD-12345", loaded.getCard());
            assertEquals(5000.0, loaded.getMaxdebt(), 0.001);
            assertEquals("100 Industrial Way", loaded.getAddress());
            assertEquals("Suite 200", loaded.getAddress2());
            assertEquals("12345", loaded.getPostal());
            assertEquals("Metropolis", loaded.getCity());
            assertEquals("NY", loaded.getRegion());
            assertEquals("USA", loaded.getCountry());
            assertEquals("John", loaded.getFirstname());
            assertEquals("Doe", loaded.getLastname());
            assertEquals("johndoe@acme.com", loaded.getEmail());
            assertEquals("555-0100", loaded.getPhone());
            assertEquals("555-0101", loaded.getPhone2());
            assertEquals("555-0102", loaded.getFax());
            assertEquals("Preferred corporate customer", loaded.getNotes());
            assertTrue(loaded.isVisible());
            assertEquals(150.0, loaded.getCurdebt(), 0.001);
            assertTrue(loaded.isVIP());
            assertEquals(0.10, loaded.getDiscount(), 0.001);

            // 2b. Query findCustomerInfoExtById
            CustomerInfoExt byId = dataLogic.findCustomerInfoExtById("cust-001");
            assertNotNull(byId);
            assertEquals("Acme Corporation", byId.getName());

            // 3. Update
            cust.setName("Acme Global Inc.");
            cust.setMaxdebt(7500.0);
            cust.setDiscount(0.15);
            assertEquals(1, saveProvider.updateData(cust));

            List<CustomerInfoExt> updatedList = tableCustomers.getListSentence().list();
            assertEquals(1, updatedList.size());
            CustomerInfoExt updated = updatedList.get(0);
            assertEquals("Acme Global Inc.", updated.getName());
            assertEquals(7500.0, updated.getMaxdebt(), 0.001);
            assertEquals(0.15, updated.getDiscount(), 0.001);

            // 4. Delete
            assertEquals(1, saveProvider.deleteData(cust));
            assertTrue(tableCustomers.getListSentence().list().isEmpty());

        } finally {
            try (Statement st = session.getConnection().createStatement()) {
                st.execute("SHUTDOWN");
            } catch (Exception ignored) {
            }
        }
    }
}
