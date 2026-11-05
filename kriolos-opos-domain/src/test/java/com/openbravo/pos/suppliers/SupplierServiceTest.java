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
import com.openbravo.data.loader.Session;
import com.openbravo.data.loader.SentenceList;
import com.openbravo.data.loader.TableDefinition;
import com.openbravo.pos.panels.AppView;
import com.openbravo.pos.container.BeanContainer;
import java.lang.reflect.Proxy;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
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
            public void createSupplier(SupplierCreateCommand supplier) throws BasicException {
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

    @Test
    @DisplayName("Typed supplier creation persists the supplied fields without positional mapping")
    @SuppressWarnings("deprecation")
    void testTypedSupplierCreationPersistsFields() throws Exception {
        String databaseName = "supplier_create_" + UUID.randomUUID().toString().replace("-", "");
        Session session = new Session("jdbc:hsqldb:mem:" + databaseName + ";sql.syntax_mys=true", "SA", "");
        try {
            try (Statement statement = session.getConnection().createStatement()) {
                statement.executeUpdate("CREATE TABLE suppliers ("
                        + "ID VARCHAR(36) PRIMARY KEY, "
                        + "NAME VARCHAR(255) NOT NULL, "
                        + "SEARCHKEY VARCHAR(255) NOT NULL, "
                        + "VISIBLE BOOLEAN NOT NULL)");
            }

            DataLogicSuppliers service = new DataLogicSuppliers();
            service.init(session);
            service.createSupplier(new SupplierCreateCommand(
                    "supplier-1", "Atlantic Foods", "ATL-001", true));
                service.createSupplier(new Object[]{"supplier-legacy", "Legacy Foods", "LEG-002", false});

            try (PreparedStatement query = session.getConnection().prepareStatement(
                    "SELECT ID, NAME, SEARCHKEY, VISIBLE FROM suppliers WHERE ID = ?")) {
                query.setString(1, "supplier-1");
                try (ResultSet result = query.executeQuery()) {
                    assertTrue(result.next());
                    assertEquals("supplier-1", result.getString("ID"));
                    assertEquals("Atlantic Foods", result.getString("NAME"));
                    assertEquals("ATL-001", result.getString("SEARCHKEY"));
                    assertTrue(result.getBoolean("VISIBLE"));
                    assertFalse(result.next());
                }
            }

            try (PreparedStatement query = session.getConnection().prepareStatement(
                    "SELECT NAME, SEARCHKEY, VISIBLE FROM suppliers WHERE ID = ?")) {
                query.setString(1, "supplier-legacy");
                try (ResultSet result = query.executeQuery()) {
                    assertTrue(result.next());
                    assertEquals("Legacy Foods", result.getString("NAME"));
                    assertEquals("LEG-002", result.getString("SEARCHKEY"));
                    assertFalse(result.getBoolean("VISIBLE"));
                    assertFalse(result.next());
                }
            }
        } finally {
            session.close();
        }
    }
}
