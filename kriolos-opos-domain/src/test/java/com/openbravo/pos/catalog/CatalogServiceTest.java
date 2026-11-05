package com.openbravo.pos.catalog;

import com.openbravo.basic.BasicException;
import com.openbravo.pos.panels.AppView;
import com.openbravo.pos.container.BeanContainer;
import com.openbravo.pos.pim.CategoryInfo;
import com.openbravo.pos.pim.DataLogicCategories;
import com.openbravo.pos.pim.DataLogicProducts;
import com.openbravo.pos.pim.DataLogicUom;
import com.openbravo.pos.ticket.ProductInfoExt;
import com.openbravo.data.loader.Session;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test suite for {@link CatalogService} port and BeanContainer resolution.
 *
 * @author KriolOS
 */
@DisplayName("CatalogService Port Test Suite")
class CatalogServiceTest {

    @Test
    @DisplayName("Should resolve CatalogService in BeanContainer to DataLogicProducts")
    void shouldResolveCatalogServiceInBeanContainer() {
        AppView appViewProxy = (AppView) Proxy.newProxyInstance(
                AppView.class.getClassLoader(),
                new Class<?>[]{AppView.class},
                (proxy, method, args) -> null
        );

        Object bean = BeanContainer.getBean("com.openbravo.pos.catalog.CatalogService", appViewProxy);
        assertNotNull(bean, "BeanContainer should resolve CatalogService");
        assertInstanceOf(CatalogServiceImpl.class, bean, "CatalogService should resolve to CatalogServiceImpl");
        assertInstanceOf(CatalogService.class, bean, "CatalogServiceImpl should implement CatalogService");
    }

    @Test
    @DisplayName("Should resolve specific PIM DataLogics in BeanContainer")
    void shouldResolveSpecificPimDataLogics() {
        AppView appViewProxy = (AppView) Proxy.newProxyInstance(
                AppView.class.getClassLoader(),
                new Class<?>[]{AppView.class},
                (proxy, method, args) -> null
        );

        Object categoriesBean = BeanContainer.getBean("com.openbravo.pos.pim.DataLogicCategories", appViewProxy);
        assertNotNull(categoriesBean, "BeanContainer should resolve DataLogicCategories");
        assertInstanceOf(DataLogicCategories.class, categoriesBean);

        Object uomBean = BeanContainer.getBean("com.openbravo.pos.pim.DataLogicUom", appViewProxy);
        assertNotNull(uomBean, "BeanContainer should resolve DataLogicUom");
        assertInstanceOf(DataLogicUom.class, uomBean);
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

    @Test
    @DisplayName("Typed category creation persists the supplied fields without positional mapping")
    void testTypedCategoryCreationPersistsFields() throws Exception {
        String databaseName = "catalog_category_" + UUID.randomUUID().toString().replace("-", "");
        Session session = new Session("jdbc:hsqldb:mem:" + databaseName + ";sql.syntax_mys=true", "SA", "");
        try {
            try (Statement statement = session.getConnection().createStatement()) {
                statement.executeUpdate("CREATE TABLE categories ("
                        + "ID VARCHAR(36) PRIMARY KEY, "
                        + "NAME VARCHAR(255) NOT NULL, "
                        + "CATSHOWNAME BOOLEAN NOT NULL)");
            }

                CatalogServiceImpl service = new CatalogServiceImpl(
                    new DataLogicProducts(), new DataLogicCategories(), new DataLogicUom());
            service.init(session);
            service.createCategory("category-1", "Beverages", true);

            try (PreparedStatement query = session.getConnection().prepareStatement(
                    "SELECT ID, NAME, CATSHOWNAME FROM categories WHERE ID = ?")) {
                query.setString(1, "category-1");
                try (ResultSet result = query.executeQuery()) {
                    assertTrue(result.next());
                    assertEquals("category-1", result.getString("ID"));
                    assertEquals("Beverages", result.getString("NAME"));
                    assertTrue(result.getBoolean("CATSHOWNAME"));
                    assertFalse(result.next());
                }
            }
        } finally {
            session.close();
        }
    }
}
