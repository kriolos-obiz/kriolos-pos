package com.openbravo.pos.suppliers;

import com.openbravo.basic.BasicException;
import com.openbravo.data.user.DirtyManager;
import com.openbravo.pos.panels.AppView;
import java.lang.reflect.Proxy;
import java.util.Collections;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test suite verifying type-safe lifecycle, two-way data binding,
 * and validation of SuppliersView.
 *
 * @author KriolOS
 */
@DisplayName("Suppliers View Type-Safe Editor Test Suite")
class SuppliersEditorTest {

    @Test
    @DisplayName("Should handle SupplierInfo lifecycle type-safely without Object[] arrays")
    void shouldHandleSuppliersViewLifecycle() throws BasicException {
        SupplierService supplierServiceProxy = (SupplierService) Proxy.newProxyInstance(
                SupplierService.class.getClassLoader(),
                new Class<?>[]{SupplierService.class},
                (proxy, method, args) -> {
                    if ("getSuppliersTransactionList".equals(method.getName())) {
                        return Collections.emptyList();
                    }
                    return null;
                }
        );

        AppView appViewProxy = (AppView) Proxy.newProxyInstance(
                AppView.class.getClassLoader(),
                new Class<?>[]{AppView.class},
                (proxy, method, args) -> {
                    if ("getBean".equals(method.getName()) && SupplierService.class.equals(args[0])) {
                        return supplierServiceProxy;
                    }
                    return null;
                }
        );

        DirtyManager dirty = new DirtyManager();
        SuppliersView view = new SuppliersView(appViewProxy, dirty);

        // 1. Initial EOF
        SupplierInfo eof = view.createValue();
        assertNotNull(eof.getId());
        assertEquals("", eof.getName());
        assertEquals("", eof.getSearchkey());

        // 2. Insert
        view.writeValueInsert();
        SupplierInfo insertValue = view.createValue();
        assertNotNull(insertValue.getId());
        assertTrue(insertValue.isVisible());

        // 3. Edit
        SupplierInfo supplier = new SupplierInfo("supp-test");
        supplier.setSearchkey("SUPP-007");
        supplier.setTaxid("TAX-1122");
        supplier.setName("Fresh Produce Ltd");
        supplier.setMaxdebt(3000.0);
        supplier.setAddress("Calle Sol 12");
        supplier.setCity("Praia");
        supplier.setCountry("Cabo Verde");
        supplier.setEmail("fresh@produce.cv");
        supplier.setPhone("+238 261 0000");
        supplier.setNotes("Delivers daily at 07:00");
        supplier.setVisible(true);
        supplier.setVatid("VAT-0099");

        view.writeValueEdit(supplier);

        SupplierInfo created = view.createValue();
        assertEquals("supp-test", created.getId());
        assertEquals("SUPP-007", created.getSearchkey());
        assertEquals("TAX-1122", created.getTaxid());
        assertEquals("Fresh Produce Ltd", created.getName());
        assertEquals(3000.0, created.getMaxdebt());
        assertEquals("Calle Sol 12", created.getAddress());
        assertEquals("Praia", created.getCity());
        assertEquals("fresh@produce.cv", created.getEmail());
        assertEquals("+238 261 0000", created.getPhone());
        assertEquals("Delivers daily at 07:00", created.getNotes());
        assertTrue(created.isVisible());
        assertEquals("VAT-0099", created.getVatid());

        // 4. Delete
        view.writeValueDelete(supplier);
        SupplierInfo deletedState = view.createValue();
        assertEquals("supp-test", deletedState.getId());
        assertEquals("Fresh Produce Ltd", deletedState.getName());

        // 5. EOF reset
        view.writeValueEOF();
    }

    @Test
    @DisplayName("Should validate valid supplier successfully with SwingBinder")
    void shouldValidateValidSupplierWithSwingBinder() throws BasicException {
        DirtyManager dirty = new DirtyManager();
        SuppliersView view = new SuppliersView(dirty);

        SupplierInfo supplier = new SupplierInfo("supp-wh-1");
        supplier.setSearchkey("WH-001");
        supplier.setTaxid("TAX-5544");
        supplier.setName("Ocean Logistics");
        supplier.setMaxdebt(15000.0);
        supplier.setEmail("support@oceanlogistics.cv");
        supplier.setAddress("Port Area Pier 4");
        supplier.setCity("Mindelo");
        supplier.setCountry("Cabo Verde");
        supplier.setVisible(true);

        view.writeValueEdit(supplier);

        assertTrue(view.isValid(), "Valid supplier entity must pass SwingBinder validation");
        assertNotNull(view.getBinder(), "SwingBinder instance must be present");
        assertTrue(view.getBinder().getInvalidFields().isEmpty(), "No fields should be flagged as invalid");

        SupplierInfo created = view.createValue();
        assertEquals("supp-wh-1", created.getId());
        assertEquals("Ocean Logistics", created.getName());
        assertEquals("support@oceanlogistics.cv", created.getEmail());
        assertEquals(15000.0, created.getMaxdebt());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    @DisplayName("Should fail SwingBinder validation when required supplier name is missing or blank")
    void shouldFailValidationWhenRequiredNameIsMissingOrBlank(String invalidName) throws BasicException {
        DirtyManager dirty = new DirtyManager();
        SuppliersView view = new SuppliersView(dirty);

        SupplierInfo supplier = new SupplierInfo("supp-invalid");
        supplier.setName(invalidName);
        supplier.setEmail("valid@supplier.com");
        view.writeValueEdit(supplier);

        assertFalse(view.isValid(), "Supplier with missing or blank name must fail validation");
        assertFalse(view.getBinder().getInvalidFields().isEmpty(), "Name field must be marked as invalid");
        assertTrue(view.getBinder().getInvalidFields().contains(view.getNameField()),
                "The invalid fields collection must contain m_jName component");
    }

    @ParameterizedTest
    @ValueSource(strings = {"not-an-email", "missing-at.domain.com", "user@invalid", "@missing-user.com"})
    @DisplayName("Should fail SwingBinder validation when supplier email is malformed")
    void shouldFailValidationWhenEmailIsInvalid(String invalidEmail) throws BasicException {
        DirtyManager dirty = new DirtyManager();
        SuppliersView view = new SuppliersView(dirty);

        SupplierInfo supplier = new SupplierInfo("supp-bad-mail");
        supplier.setName("Valid Supplier Name");
        supplier.setEmail(invalidEmail);
        view.writeValueEdit(supplier);

        assertFalse(view.isValid(), "Supplier with malformed email must fail validation");
        assertTrue(view.getBinder().getInvalidFields().contains(view.getEmailField()),
                "The invalid fields collection must contain txtEmail component");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t"})
    @DisplayName("Should pass SwingBinder validation when optional supplier email is blank or null")
    void shouldPassValidationWhenEmailIsBlankOrNull(String optionalEmail) throws BasicException {
        DirtyManager dirty = new DirtyManager();
        SuppliersView view = new SuppliersView(dirty);

        SupplierInfo supplier = new SupplierInfo("supp-no-email");
        supplier.setName("Valid Supplier No Email");
        supplier.setEmail(optionalEmail);
        view.writeValueEdit(supplier);

        assertTrue(view.isValid(), "Optional email should pass when blank or null");
        assertFalse(view.getBinder().getInvalidFields().contains(view.getEmailField()),
                "txtEmail should not be marked as invalid when empty");
    }

    @Test
    @DisplayName("Should track dirty state when user edits bound supplier fields")
    void shouldTrackDirtyStateWhenBoundFieldIsModified() {
        DirtyManager dirty = new DirtyManager();
        SuppliersView view = new SuppliersView(dirty);

        view.writeValueInsert();
        dirty.setDirty(false);
        assertFalse(dirty.isDirty(), "Dirty flag should be cleared after manual reset");

        // Simulate typing in supplier name
        view.getNameField().setText("New Global Supplier");
        assertTrue(dirty.isDirty(), "Dirty flag must be set when bound name field is edited");
    }

    @Test
    @DisplayName("Should safely handle null values during writeValueEdit and writeValueDelete")
    void shouldHandleNullGuardsInLifecycle() {
        DirtyManager dirty = new DirtyManager();
        SuppliersView view = new SuppliersView(dirty);

        assertDoesNotThrow(() -> view.writeValueEdit(null),
                "writeValueEdit(null) must safely fallback to EOF without throwing NPE");
        assertDoesNotThrow(() -> view.writeValueDelete(null),
                "writeValueDelete(null) must safely fallback to EOF without throwing NPE");
    }
}
