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
package com.openbravo.pos.sales;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.SentenceList;
import com.openbravo.data.loader.Session;
import com.openbravo.data.loader.TableDefinition;
import com.openbravo.pos.panels.AppView;
import com.openbravo.pos.container.BeanContainer;
import com.openbravo.pos.inventory.TaxCategoryInfo;
import com.openbravo.pos.inventory.TaxCustCategoryInfo;
import com.openbravo.pos.ticket.TaxInfo;
import java.lang.reflect.Proxy;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TaxService Port Test Suite")
class TaxServiceTest {

    @Test
    @DisplayName("Should resolve TaxService in BeanContainer to DataLogicTax")
    void shouldResolveTaxServiceInBeanContainer() {
        AppView appViewProxy = (AppView) Proxy.newProxyInstance(
                AppView.class.getClassLoader(),
                new Class<?>[]{AppView.class},
                (proxy, method, args) -> null
        );

        Object bean = BeanContainer.getBean("com.openbravo.pos.sales.TaxService", appViewProxy);
        assertNotNull(bean, "BeanContainer should resolve TaxService");
        assertInstanceOf(DataLogicTax.class, bean, "TaxService should resolve to DataLogicTax");
        assertInstanceOf(TaxService.class, bean, "DataLogicTax should implement TaxService");
    }

    @Test
    @DisplayName("TaxService mock implementation should satisfy domain contract")
    void testTaxServiceContract() {
        TaxService service = new TaxService() {
            @Override
            public SentenceList<TaxInfo> getTaxList() {
                return null;
            }

            @Override
            public List<TaxInfo> getTaxListAll() {
                return Collections.emptyList();
            }

            @Override
            public SentenceList<TaxCustCategoryInfo> getTaxCustCategoriesList() {
                return null;
            }

            @Override
            public List<TaxCustCategoryInfo> getTaxCustCategoriesListAll() {
                return Collections.emptyList();
            }

            @Override
            public SentenceList<TaxCategoryInfo> getTaxCategoriesList() {
                return null;
            }

            @Override
            public List<TaxCategoryInfo> getTaxCategoriesListAll() {
                return Collections.emptyList();
            }

            @Override
            public SentenceList<TaxCategoryInfo> getTaxCategoryInfoList() {
                return null;
            }

            @Override
            public void createTaxCategory(CreateTaxCategoryCommand command) throws BasicException {
            }

            @Override
            public void createTaxCustCategory(CreateTaxCustCategoryCommand command) throws BasicException {
            }

            @Override
            public void createTax(CreateTaxCommand command) throws BasicException {
            }

            @Override
            public TableDefinition<Object[]> getTableTaxes() {
                return null;
            }

            @Override
            public TableDefinition<Object[]> getTableTaxCustCategories() {
                return null;
            }

            @Override
            public TableDefinition<Object[]> getTableTaxCategories() {
                return null;
            }
        };

        assertNotNull(service.getTaxListAll());
        assertTrue(service.getTaxListAll().isEmpty());
        assertNotNull(service.getTaxCustCategoriesListAll());
        assertTrue(service.getTaxCustCategoriesListAll().isEmpty());
        assertNotNull(service.getTaxCategoriesListAll());
        assertTrue(service.getTaxCategoriesListAll().isEmpty());
    }

    @Test
    @DisplayName("CreateTaxCategoryCommand validates invariants and trims string fields")
    void testCreateTaxCategoryCommandInvariants() {
        assertThrows(NullPointerException.class, () -> new CreateTaxCategoryCommand(null, "Standard"));
        assertThrows(IllegalArgumentException.class, () -> new CreateTaxCategoryCommand("   ", "Standard"));
        assertThrows(NullPointerException.class, () -> new CreateTaxCategoryCommand("cat-1", null));
        assertThrows(IllegalArgumentException.class, () -> new CreateTaxCategoryCommand("cat-1", "   "));

        CreateTaxCategoryCommand cmd = CreateTaxCategoryCommand.of("  cat-1  ", "  Standard Tax  ");
        assertEquals("cat-1", cmd.id());
        assertEquals("Standard Tax", cmd.name());
    }

    @Test
    @DisplayName("CreateTaxCustCategoryCommand validates invariants and trims string fields")
    void testCreateTaxCustCategoryCommandInvariants() {
        assertThrows(NullPointerException.class, () -> new CreateTaxCustCategoryCommand(null, "VIP"));
        assertThrows(IllegalArgumentException.class, () -> new CreateTaxCustCategoryCommand("   ", "VIP"));
        assertThrows(NullPointerException.class, () -> new CreateTaxCustCategoryCommand("cust-1", null));
        assertThrows(IllegalArgumentException.class, () -> new CreateTaxCustCategoryCommand("cust-1", "   "));

        CreateTaxCustCategoryCommand cmd = CreateTaxCustCategoryCommand.of("  cust-1  ", "  VIP Category  ");
        assertEquals("cust-1", cmd.id());
        assertEquals("VIP Category", cmd.name());
    }

    @Test
    @DisplayName("CreateTaxCommand validates invariants, rate boundaries, and normalizes order")
    void testCreateTaxCommandInvariants() {
        assertThrows(NullPointerException.class, () -> new CreateTaxCommand(null, "VAT", "cat-1", null, null, 0.15, false, 0));
        assertThrows(IllegalArgumentException.class, () -> new CreateTaxCommand("   ", "VAT", "cat-1", null, null, 0.15, false, 0));
        assertThrows(NullPointerException.class, () -> new CreateTaxCommand("tax-1", null, "cat-1", null, null, 0.15, false, 0));
        assertThrows(IllegalArgumentException.class, () -> new CreateTaxCommand("tax-1", "  ", "cat-1", null, null, 0.15, false, 0));
        assertThrows(NullPointerException.class, () -> new CreateTaxCommand("tax-1", "VAT", null, null, null, 0.15, false, 0));
        assertThrows(IllegalArgumentException.class, () -> new CreateTaxCommand("tax-1", "VAT", "  ", null, null, 0.15, false, 0));

        assertThrows(IllegalArgumentException.class, () -> new CreateTaxCommand("tax-1", "VAT", "cat-1", null, null, -0.01, false, 0));
        assertThrows(IllegalArgumentException.class, () -> new CreateTaxCommand("tax-1", "VAT", "cat-1", null, null, Double.NaN, false, 0));
        assertThrows(IllegalArgumentException.class, () -> new CreateTaxCommand("tax-1", "VAT", "cat-1", null, null, Double.POSITIVE_INFINITY, false, 0));

        CreateTaxCommand cmd = CreateTaxCommand.of(
                "  tax-1  ", "  Standard VAT  ", "  cat-1  ", "  custcat-1  ", "  parent-1  ", 0.20, true, null);
        assertEquals("tax-1", cmd.id());
        assertEquals("Standard VAT", cmd.name());
        assertEquals("cat-1", cmd.taxCategoryId());
        assertEquals("custcat-1", cmd.taxCustCategoryId());
        assertEquals("parent-1", cmd.parentId());
        assertEquals(0.20, cmd.rate());
        assertTrue(cmd.cascade());
        assertEquals(0, cmd.order(), "Null order should default to 0");

        TaxInfo taxInfo = new TaxInfo("tax-info-1", "Reduced VAT", "cat-2", null, null, 0.05, false, 2);
        CreateTaxCommand fromInfo = CreateTaxCommand.from(taxInfo);
        assertEquals("tax-info-1", fromInfo.id());
        assertEquals("Reduced VAT", fromInfo.name());
        assertEquals("cat-2", fromInfo.taxCategoryId());
        assertNull(fromInfo.taxCustCategoryId());
        assertNull(fromInfo.parentId());
        assertEquals(0.05, fromInfo.rate());
        assertFalse(fromInfo.cascade());
        assertEquals(2, fromInfo.order());
    }

    @Test
    @DisplayName("DataLogicTax persists categories and taxes in HSQLDB and returns typed list")
    void testTaxServicePersistenceWithHsqldb() throws Exception {
        String databaseName = "tax_db_" + UUID.randomUUID().toString().replace("-", "");
        Session session = new Session("jdbc:hsqldb:mem:" + databaseName + ";sql.syntax_mys=true", "SA", "");
        try {
            try (Statement statement = session.getConnection().createStatement()) {
                statement.executeUpdate("CREATE TABLE taxcategories ("
                        + "ID VARCHAR(36) PRIMARY KEY, "
                        + "NAME VARCHAR(255) NOT NULL)");
                statement.executeUpdate("CREATE TABLE taxcustcategories ("
                        + "ID VARCHAR(36) PRIMARY KEY, "
                        + "NAME VARCHAR(255) NOT NULL)");
                statement.executeUpdate("CREATE TABLE taxes ("
                        + "ID VARCHAR(36) PRIMARY KEY, "
                        + "NAME VARCHAR(255) NOT NULL, "
                        + "CATEGORY VARCHAR(36) NOT NULL, "
                        + "CUSTCATEGORY VARCHAR(36), "
                        + "PARENTID VARCHAR(36), "
                        + "RATE DOUBLE NOT NULL, "
                        + "RATECASCADE BOOLEAN NOT NULL, "
                        + "RATEORDER INTEGER)");
            }

            DataLogicTax dataLogicTax = new DataLogicTax();
            dataLogicTax.init(session);

            dataLogicTax.createTaxCategory(CreateTaxCategoryCommand.of("cat-1", "Standard Goods"));
            dataLogicTax.createTaxCustCategory(CreateTaxCustCategoryCommand.of("cust-1", "Exempt Customer"));
            dataLogicTax.createTax(CreateTaxCommand.of(
                    "tax-10", "15% Standard", "cat-1", "cust-1", null, 0.15, false, 1));

            List<TaxCategoryInfo> categories = dataLogicTax.getTaxCategoriesListAll();
            assertEquals(1, categories.size());
            assertEquals("cat-1", categories.get(0).getID());
            assertEquals("Standard Goods", categories.get(0).getName());

            List<TaxCustCategoryInfo> custCategories = dataLogicTax.getTaxCustCategoriesListAll();
            assertEquals(1, custCategories.size());
            assertEquals("cust-1", custCategories.get(0).getID());
            assertEquals("Exempt Customer", custCategories.get(0).getName());

            List<TaxInfo> taxes = dataLogicTax.getTaxListAll();
            assertEquals(1, taxes.size());
            TaxInfo tax = taxes.get(0);
            assertEquals("tax-10", tax.getId());
            assertEquals("15% Standard", tax.getName());
            assertEquals("cat-1", tax.getTaxCategoryID());
            assertEquals("cust-1", tax.getTaxCustCategoryID());
            assertNull(tax.getParentID());
            assertEquals(0.15, tax.getRate());
            assertFalse(tax.isCascade());
            assertEquals(1, tax.getOrder());

            // Check TableDefinitions return typed instances
            assertNotNull(dataLogicTax.getTableTaxes());
            assertEquals("taxes", dataLogicTax.getTableTaxes().getTableName());
            assertNotNull(dataLogicTax.getTableTaxCategories());
            assertEquals("taxcategories", dataLogicTax.getTableTaxCategories().getTableName());
            assertNotNull(dataLogicTax.getTableTaxCustCategories());
            assertEquals("taxcustcategories", dataLogicTax.getTableTaxCustCategories().getTableName());
        } finally {
            session.close();
        }
    }
}
