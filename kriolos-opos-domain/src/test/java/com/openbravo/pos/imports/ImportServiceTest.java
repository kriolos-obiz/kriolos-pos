/*
 * Copyright (C) 2026 KriolOS POS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.openbravo.pos.imports;

import com.openbravo.basic.BasicException;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.BeanContainer;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ImportService Port Test Suite")
class ImportServiceTest {

    @Test
    @DisplayName("Should resolve ImportService in BeanContainer to DataLogicImport")
    void shouldResolveImportServiceInBeanContainer() {
        AppView appViewProxy = (AppView) Proxy.newProxyInstance(
                AppView.class.getClassLoader(),
                new Class<?>[]{AppView.class},
                (proxy, method, args) -> null
        );

        Object bean = BeanContainer.getBean("com.openbravo.pos.imports.ImportService", appViewProxy);
        assertNotNull(bean, "BeanContainer should resolve ImportService");
        assertInstanceOf(DataLogicImport.class, bean, "ImportService should resolve to DataLogicImport");
        assertInstanceOf(ImportService.class, bean, "DataLogicImport should implement ImportService");
    }

    @Test
    @DisplayName("Default convenience methods should delegate correctly to Object[] overloads")
    void shouldDelegateConvenienceMethods() throws BasicException {
        AtomicReference<Object[]> productRef = new AtomicReference<>();
        AtomicReference<Object[]> customerRef = new AtomicReference<>();

        ImportService testService = new ImportService() {
            @Override
            public void execCSVStockUpdate(Object[] csv) throws BasicException {}

            @Override
            public void execAddCSVEntry(Object[] csv) throws BasicException {}

            @Override
            public void execCustomerAddCSVEntry(Object[] csv) throws BasicException {}

            @Override
            public String getProductRecordType(Object[] myProduct) throws BasicException {
                productRef.set(myProduct);
                return "test-product-type";
            }

            @Override
            public String getCustomerRecordType(Object[] myCustomer) throws BasicException {
                customerRef.set(myCustomer);
                return "test-customer-type";
            }
        };

        String prodResult = testService.getProductRecordType("REF001", "12345678", "Product A");
        assertEquals("test-product-type", prodResult);
        assertNotNull(productRef.get());
        assertEquals("REF001", productRef.get()[0]);
        assertEquals("12345678", productRef.get()[1]);
        assertEquals("Product A", productRef.get()[2]);

        String custResult = testService.getCustomerRecordType("CUST001", "Customer A");
        assertEquals("test-customer-type", custResult);
        assertNotNull(customerRef.get());
        assertEquals("CUST001", customerRef.get()[0]);
        assertEquals("Customer A", customerRef.get()[1]);
    }
}
