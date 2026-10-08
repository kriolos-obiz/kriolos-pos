/*
 * Copyright (C) 2026 KriolOS
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
package com.openbravo.pos.catalog;

import com.openbravo.basic.BasicException;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.BeanContainer;
import com.openbravo.pos.pim.CategoryInfo;
import com.openbravo.pos.pim.DataLogicPIM;
import com.openbravo.pos.ticket.ProductInfoExt;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CatalogService Port Test Suite")
class CatalogServiceTest {

    @Test
    @DisplayName("Should resolve CatalogService in BeanContainer to DataLogicPIM")
    void shouldResolveCatalogServiceInBeanContainer() {
        AppView appViewProxy = (AppView) Proxy.newProxyInstance(
                AppView.class.getClassLoader(),
                new Class<?>[]{AppView.class},
                (proxy, method, args) -> null
        );

        Object bean = BeanContainer.getBean("com.openbravo.pos.catalog.CatalogService", appViewProxy);
        assertNotNull(bean, "BeanContainer should resolve CatalogService");
        assertInstanceOf(DataLogicPIM.class, bean, "CatalogService should resolve to DataLogicPIM");
        assertInstanceOf(CatalogService.class, bean, "DataLogicPIM should implement CatalogService");
    }

    @Test
    @DisplayName("CatalogService mock implementation should provide clean domain lists")
    void testCatalogServiceContract() throws BasicException {
        CatalogService service = (CatalogService) Proxy.newProxyInstance(
                CatalogService.class.getClassLoader(),
                new Class<?>[]{CatalogService.class},
                (proxy, method, args) -> {
                    if ("getCategoriesListAll".equals(method.getName())) {
                        CategoryInfo cat = new CategoryInfo("CAT1", "Beverages", null, null, true);
                        return Collections.singletonList(cat);
                    }
                    if ("getProductInfo".equals(method.getName())) {
                        ProductInfoExt prod = new ProductInfoExt();
                        prod.setReference("P001");
                        return prod;
                    }
                    return null;
                }
        );

        List<CategoryInfo> categories = service.getCategoriesListAll();
        assertNotNull(categories);
        assertEquals(1, categories.size());
        assertEquals("Beverages", categories.get(0).getName());

        ProductInfoExt product = service.getProductInfo("P001");
        assertNotNull(product);
        assertEquals("P001", product.getReference());
    }
}
