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
package com.openbravo.pos.suppliers;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.SentenceList;
import com.openbravo.data.loader.TableDefinition;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.BeanContainer;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SupplierService Port Test Suite")
class SupplierServiceTest {

    @Test
    @DisplayName("Should resolve SupplierService in BeanContainer to DataLogicSuppliers")
    void shouldResolveSupplierServiceInBeanContainer() {
        AppView appViewProxy = (AppView) Proxy.newProxyInstance(
                AppView.class.getClassLoader(),
                new Class<?>[]{AppView.class},
                (proxy, method, args) -> null
        );

        Object bean = BeanContainer.getBean("com.openbravo.pos.suppliers.SupplierService", appViewProxy);
        assertNotNull(bean, "BeanContainer should resolve SupplierService");
        assertInstanceOf(DataLogicSuppliers.class, bean, "SupplierService should resolve to DataLogicSuppliers");
        assertInstanceOf(SupplierService.class, bean, "DataLogicSuppliers should implement SupplierService");
    }

    @Test
    @DisplayName("SupplierService contract mock should satisfy interface requirements")
    void testSupplierServiceContract() throws BasicException {
        SupplierService service = new SupplierService() {
            @Override
            public SentenceList<SupplierInfo> getSupplierList() {
                return null;
            }

            @Override
            public List<SupplierInfo> getSupplierListAll() {
                return Collections.emptyList();
            }

            @Override
            public SentenceList<SupplierInfo> getSuppList() {
                return null;
            }

            @Override
            public SentenceList<SupplierInfo> getSuppListExt() {
                return null;
            }

            @Override
            public SupplierInfoExt loadSupplierExt(String id) throws BasicException {
                SupplierInfoExt ext = new SupplierInfoExt(id);
                ext.setName("Test Supplier");
                return ext;
            }

            @Override
            public void createSupplier(Object[] supplier) throws BasicException {
            }

            @Override
            public int updateSupplierExt(SupplierInfoExt supplier) throws BasicException {
                return 1;
            }

            @Override
            public TableDefinition getTableSuppliers() {
                return null;
            }

            @Override
            public List<SupplierTransaction> getSuppliersTransactionList(String sId) throws BasicException {
                return Collections.emptyList();
            }
        };

        SupplierInfoExt ext = service.loadSupplierExt("s1");
        assertNotNull(ext);
        assertEquals("s1", ext.getId());
        assertEquals("Test Supplier", ext.getName());
        assertEquals(1, service.updateSupplierExt(ext));
        assertTrue(service.getSupplierListAll().isEmpty());
        assertTrue(service.getSuppliersTransactionList("s1").isEmpty());
    }
}
