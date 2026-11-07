package com.openbravo.pos.suppliers;

import com.openbravo.basic.BasicException;
import com.openbravo.data.user.DirtyManager;
import com.openbravo.pos.panels.AppView;
import java.lang.reflect.Proxy;
import java.util.Collections;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test suite verifying type-safe lifecycle of SuppliersView.
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
}

