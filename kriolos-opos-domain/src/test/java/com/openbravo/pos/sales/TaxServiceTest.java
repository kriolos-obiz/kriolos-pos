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

import com.openbravo.data.loader.SentenceList;
import com.openbravo.data.loader.TableDefinition;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.BeanContainer;
import com.openbravo.pos.inventory.TaxCategoryInfo;
import com.openbravo.pos.inventory.TaxCustCategoryInfo;
import com.openbravo.pos.ticket.TaxInfo;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.List;
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
            public TableDefinition getTableTaxes() {
                return null;
            }

            @Override
            public TableDefinition getTableTaxCustCategories() {
                return null;
            }

            @Override
            public TableDefinition getTableTaxCategories() {
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
}
