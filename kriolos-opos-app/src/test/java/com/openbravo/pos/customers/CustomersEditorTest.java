package com.openbravo.pos.customers;

import com.openbravo.basic.BasicException;
import com.openbravo.data.user.DirtyManager;
import java.util.Date;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test suite verifying type-safe lifecycle of CustomersView.
 *
 * @author KriolOS
 */
@DisplayName("Customers View Type-Safe Editor Test Suite")
class CustomersEditorTest {

    @Test
    @DisplayName("Should handle CustomerInfoExt lifecycle type-safely without Object[] arrays")
    void shouldHandleCustomersViewLifecycle() throws BasicException {
        DirtyManager dirty = new DirtyManager();
        CustomersView view = new CustomersView(null, null, dirty);

        // 1. Initial EOF
        view.writeValueEOF();
        CustomerInfoExt eof = view.createValue();
        assertNotNull(eof.getId());
        assertEquals("", eof.getName());
        assertFalse(eof.isVisible());
        assertFalse(eof.isVIP());

        // 2. Insert
        view.writeValueInsert();
        CustomerInfoExt insertVal = view.createValue();
        assertNotNull(insertVal.getId());
        assertEquals("", insertVal.getName());

        // 3. Edit Customer
        CustomerInfoExt cust = new CustomerInfoExt("cust-777");
        cust.setSearchkey("CUST-KEY");
        cust.setTaxid("TAX-123");
        cust.setName("Wayne Enterprises");
        cust.setCard("CARD-999");
        cust.setMaxdebt(10000.0);
        cust.setAddress("1007 Mountain Drive");
        cust.setCity("Gotham");
        cust.setRegion("NJ");
        cust.setCountry("USA");
        cust.setFirstname("Bruce");
        cust.setLastname("Wayne");
        cust.setEmail("bruce@wayne.com");
        cust.setPhone("555-BATMAN");
        cust.setNotes("VIP Client");
        cust.setVisible(true);
        Date now = new Date();
        cust.setCurdate(now);
        cust.setCurdebt(0.0);
        cust.setisVIP(true);
        cust.setDiscount(0.20);
        cust.setMemoDate(now);

        view.writeValueEdit(cust);
        CustomerInfoExt edited = view.createValue();
        assertEquals("cust-777", edited.getId());
        assertEquals("CUST-KEY", edited.getSearchkey());
        assertEquals("TAX-123", edited.getTaxid());
        assertEquals("Wayne Enterprises", edited.getName());
        assertEquals("CARD-999", edited.getCard());
        assertEquals(10000.0, edited.getMaxdebt(), 0.001);
        assertEquals("1007 Mountain Drive", edited.getAddress());
        assertEquals("Gotham", edited.getCity());
        assertEquals("NJ", edited.getRegion());
        assertEquals("USA", edited.getCountry());
        assertEquals("Bruce", edited.getFirstname());
        assertEquals("Wayne", edited.getLastname());
        assertEquals("bruce@wayne.com", edited.getEmail());
        assertEquals("555-BATMAN", edited.getPhone());
        assertEquals("VIP Client", edited.getNotes());
        assertTrue(edited.isVisible());
        assertTrue(edited.isVIP());
        assertEquals(0.20, edited.getDiscount(), 0.001);

        // 4. Delete
        view.writeValueDelete(cust);
        CustomerInfoExt deleted = view.createValue();
        assertEquals("cust-777", deleted.getId());
        assertEquals("Wayne Enterprises", deleted.getName());

        // 5. Null guards
        view.writeValueEdit(null);
        assertNotNull(view.createValue().getId());
        view.writeValueDelete(null);
        assertNotNull(view.createValue().getId());
    }

    @Test
    @DisplayName("Should validate valid customer successfully with SwingBinder")
    void shouldValidateValidCustomerWithSwingBinder() throws BasicException {
        DirtyManager dirty = new DirtyManager();
        CustomersView view = new CustomersView(null, null, dirty);

        CustomerInfoExt cust = new CustomerInfoExt("valid-1");
        cust.setName("Wayne Enterprises");
        cust.setEmail("bruce@wayne.com");
        cust.setMaxdebt(5000.0);
        cust.setDiscount(0.15);

        view.writeValueEdit(cust);
        assertTrue(view.isValid(), "Valid customer should pass SwingBinder validation");
        assertNotNull(view.getBinder());
        assertTrue(view.getBinder().getInvalidFields().isEmpty(), "No fields should be marked invalid");
    }

    @Test
    @DisplayName("Should fail SwingBinder validation when required name is missing")
    void shouldFailValidationWhenRequiredNameIsMissing() throws BasicException {
        DirtyManager dirty = new DirtyManager();
        CustomersView view = new CustomersView(null, null, dirty);

        CustomerInfoExt cust = new CustomerInfoExt("invalid-name");
        cust.setName("");
        cust.setEmail("contact@test.com");

        view.writeValueEdit(cust);
        assertFalse(view.isValid(), "Customer with blank name must fail validation");
        assertFalse(view.getBinder().getInvalidFields().isEmpty(), "Name field should be marked invalid");
    }

    @Test
    @DisplayName("Should fail SwingBinder validation when email format is invalid")
    void shouldFailValidationWhenEmailIsInvalid() throws BasicException {
        DirtyManager dirty = new DirtyManager();
        CustomersView view = new CustomersView(null, null, dirty);

        CustomerInfoExt cust = new CustomerInfoExt("invalid-email");
        cust.setName("Wayne Enterprises");
        cust.setEmail("not-a-valid-email-string");

        view.writeValueEdit(cust);
        assertFalse(view.isValid(), "Customer with invalid email syntax must fail validation");
        assertFalse(view.getBinder().getInvalidFields().isEmpty(), "Email field should be marked invalid");
    }
}
