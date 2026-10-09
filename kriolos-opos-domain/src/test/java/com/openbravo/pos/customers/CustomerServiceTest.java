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
package com.openbravo.pos.customers;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.SentenceList;
import com.openbravo.data.loader.TableDefinition;
import com.openbravo.data.model.Row;
import com.openbravo.data.user.EditorCreator;
import com.openbravo.data.user.ListProvider;
import com.openbravo.data.user.SaveProvider;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.BeanContainer;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CustomerService Port Test Suite")
class CustomerServiceTest {

    @Test
    @DisplayName("Should resolve CustomerService in BeanContainer to DataLogicCustomers")
    void shouldResolveCustomerServiceInBeanContainer() {
        AppView appViewProxy = (AppView) Proxy.newProxyInstance(
                AppView.class.getClassLoader(),
                new Class<?>[]{AppView.class},
                (proxy, method, args) -> null
        );

        Object bean = BeanContainer.getBean("com.openbravo.pos.customers.CustomerService", appViewProxy);
        assertNotNull(bean, "BeanContainer should resolve CustomerService");
        assertInstanceOf(DataLogicCustomers.class, bean, "CustomerService should resolve to DataLogicCustomers");
        assertInstanceOf(CustomerService.class, bean, "DataLogicCustomers should implement CustomerService");
    }

    @Test
    @DisplayName("CustomerService mock implementation should satisfy domain contract")
    void testCustomerServiceContract() throws BasicException {
        CustomerService service = new CustomerService() {
            @Override
            public CustomerInfo getCustomerInfo(String id) throws BasicException {
                CustomerInfo info = new CustomerInfo(id);
                info.setName("Test Customer");
                return info;
            }

            @Override
            public CustomerInfoExt findCustomerInfoExtById(String id) throws BasicException {
                CustomerInfoExt ext = new CustomerInfoExt(id);
                ext.setName("Test Customer Ext");
                return ext;
            }

            @Override
            public CustomerInfoExt findCustomerInfoExtByCard(String card) throws BasicException {
                return null;
            }

            @Override
            public CustomerInfoExt findCustomerInfoExtByName(String name) throws BasicException {
                return null;
            }

            @Override
            public CustomerInfoExt findCustomerInfoExtBySearchKey(String searchKey) throws BasicException {
                return null;
            }

            @Override
            public CustomerInfoExt findCustomerInfoExtByTaxId(String taxId) throws BasicException {
                return null;
            }

            @Override
            public int updateCustomerExt(CustomerInfoExt customer) throws BasicException {
                return 1;
            }

            @Override
            public int updateCustomerDebt(String customerId, Double accDebt, Date date) throws BasicException {
                return 1;
            }

            @Override
            public List<CustomerTransaction> getCustomersTransactionList(String customerId) throws BasicException {
                return Collections.emptyList();
            }

            @Override
            public TableDefinition getTableCustomers() {
                return null;
            }

            @Override
            public Row getCustomersRow() {
                return null;
            }

            @Override
            public SaveProvider getCustomerSaveProvider() {
                return null;
            }

            @Override
            public SentenceList<CustomerInfo> getCustomerList() {
                return null;
            }

            @Override
            public ListProvider<CustomerInfo> getCustomerListProvider(EditorCreator filter) {
                return null;
            }
        };

        CustomerInfo info = service.getCustomerInfo("c1");
        assertNotNull(info);
        assertEquals("c1", info.getId());
        assertEquals("Test Customer", info.getName());

        CustomerInfoExt ext = service.findCustomerInfoExtById("c1");
        assertNotNull(ext);
        assertEquals("c1", ext.getId());
        assertEquals(1, service.updateCustomerExt(ext));
        assertEquals(1, service.updateCustomerDebt("c1", 10.0, new Date()));
        assertTrue(service.getCustomersTransactionList("c1").isEmpty());
    }
}
