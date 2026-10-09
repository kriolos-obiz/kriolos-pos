package com.openbravo.pos.catalog;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.SentenceExec;
import com.openbravo.data.loader.SentenceList;
import com.openbravo.data.loader.TableDefinition;
import com.openbravo.data.model.Row;
import com.openbravo.data.user.EditorCreator;
import com.openbravo.data.user.ListProvider;
import com.openbravo.data.user.SaveProvider;
import com.openbravo.pos.inventory.AttributeSetInfo;
import com.openbravo.pos.pim.CategoryInfo;
import com.openbravo.pos.pim.UomInfo;
import com.openbravo.pos.ticket.ProductInfoExt;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Service port interface for Catalog, Product Information Management (PIM), Pricing, and UOM queries.
 *
 * @author KriolOS
 */
public interface CatalogService {

    /**
     * Retrieves full product details by identifier.
     *
     * @param id product identifier
     * @return {@link ProductInfoExt} or null if not found
     * @throws BasicException on persistence failure
     */
    ProductInfoExt getProductInfo(String id) throws BasicException;

    /**
     * Retrieves full product details by exact barcode.
     *
     * @param code barcode string
     * @return {@link ProductInfoExt} or null if not found
     * @throws BasicException on persistence failure
     */
    ProductInfoExt getProductInfoByCode(String code) throws BasicException;

    /**
     * Retrieves product details matching the short barcode substring.
     *
     * @param code raw barcode string
     * @return {@link ProductInfoExt} or null if not found
     * @throws BasicException on persistence failure
     */
    ProductInfoExt getProductInfoByShortCode(String code) throws BasicException;

    /**
     * Retrieves product details matching the first 7 characters of a UPC-A barcode.
     *
     * @param code raw barcode string
     * @return {@link ProductInfoExt} or null if not found
     * @throws BasicException on persistence failure
     */
    ProductInfoExt getProductInfoByUShortCode(String code) throws BasicException;

    /**
     * Retrieves product details by exact product reference.
     *
     * @param reference product reference code
     * @return {@link ProductInfoExt} or null if not found
     * @throws BasicException on persistence failure
     */
    ProductInfoExt getProductInfoByReference(String reference) throws BasicException;

    /**
     * Retrieves category information by ID.
     *
     * @param id category identifier
     * @return {@link CategoryInfo} or null if not found
     * @throws BasicException on persistence failure
     */
    CategoryInfo getCategoryInfo(String id) throws BasicException;

    /**
     * Retrieves visible root-level categories.
     *
     * @return list of root {@link CategoryInfo}
     * @throws BasicException on persistence failure
     */
    List<CategoryInfo> getRootCategories() throws BasicException;

    /**
     * Retrieves subcategories belonging to a parent category.
     *
     * @param categoryId parent category identifier
     * @return list of child {@link CategoryInfo}
     * @throws BasicException on persistence failure
     */
    List<CategoryInfo> getSubcategories(String categoryId) throws BasicException;

    /**
     * Inserts a new category with explicit domain parameters.
     *
     * @param id       category unique identifier
     * @param name     category display name
     * @param showName whether to display the name on the touch button
     * @throws BasicException on persistence failure
     */
    void createCategory(String id, String name, boolean showName) throws BasicException;

    /**
     * Inserts a new category into persistence.
     *
     * @param category category record attributes [ID, NAME, CATSHOWNAME]
     * @throws BasicException on persistence failure
     * @deprecated Use {@link #createCategory(String, String, boolean)} instead.
     */
    @Deprecated
    void createCategory(Object[] category) throws BasicException;

    /**
     * Retrieves all categories as an in-memory list.
     *
     * @return list of all {@link CategoryInfo}
     */
    List<CategoryInfo> getCategoriesListAll();

    /**
     * Retrieves basic product identifiers and barcodes belonging to a category.
     *
     * @param categoryId category identifier
     * @return list of {@link CategoryStock}
     * @throws BasicException on persistence failure
     */
    List<CategoryStock> getCategorysProductList(String categoryId) throws BasicException;

    /**
     * Adds all products under a category to the touch catalog.
     *
     * @param categoryId category identifier
     * @return number of affected rows
     * @throws BasicException on persistence failure
     */
    int addProductsToCatalogWithCategoryId(String categoryId) throws BasicException;

    /**
     * Removes all products under a category from the touch catalog.
     *
     * @param categoryId category identifier
     * @return number of affected rows
     * @throws BasicException on persistence failure
     */
    int removeProductsFromCatalogWithCategoryId(String categoryId) throws BasicException;

    /**
     * Updates the sell price of a product.
     *
     * @param productId product identifier
     * @param newPrice  new sell price
     * @throws BasicException on persistence failure
     */
    void updateProductPrice(String productId, double newPrice) throws BasicException;

    /**
     * Retrieves the image associated with a product.
     *
     * @param productId product identifier
     * @return {@link BufferedImage} or null if not present
     */
    BufferedImage getProductImage(String productId);

    /**
     * Retrieves products assigned to the touch catalog for a category.
     *
     * @param categoryId category identifier
     * @return list of catalog {@link ProductInfoExt}
     * @throws BasicException on persistence failure
     */
    List<ProductInfoExt> getProductCatalog(String categoryId) throws BasicException;

    /**
     * Retrieves constant products.
     *
     * @return list of constant {@link ProductInfoExt}
     * @throws BasicException on persistence failure
     */
    List<ProductInfoExt> getProductConstant() throws BasicException;

    /**
     * Retrieves composite child products associated with a parent composite product.
     *
     * @param productId parent product ID
     * @return list of composite child {@link ProductInfoExt}
     * @throws BasicException on persistence failure
     */
    List<ProductInfoExt> getProductComposite(String productId) throws BasicException;

    /**
     * Returns the table definition for categories.
     *
     * @return categories {@link TableDefinition}
     */
    TableDefinition getTableCategories();

    /**
     * Returns the table definition for units of measure.
     *
     * @return UOM {@link TableDefinition}
     */
    TableDefinition getTableUom();

    /**
     * Retrieves Unit of Measure details by ID.
     *
     * @param uomId UOM identifier
     * @return {@link UomInfo} or null if not found
     * @throws BasicException on persistence failure
     */
    UomInfo getUomInfoById(String uomId) throws BasicException;

    /**
     * Retrieves all Units of Measure.
     *
     * @return list of all {@link UomInfo}
     */
    List<UomInfo> getUomListAll();

    /**
     * Retrieves all attribute sets.
     *
     * @return list of all {@link AttributeSetInfo}
     */
    List<AttributeSetInfo> getAttributeSetListAll();

    /**
     * Returns the product row metadata definition.
     *
     * @return product {@link Row}
     */
    Row getProductsRow();

    /**
     * Returns the product save provider.
     *
     * @return {@link SaveProvider}
     */
    SaveProvider getProductSaveProvider();

    /**
     * Returns the unfiltered product list provider.
     *
     * @return {@link ListProvider}
     */
    ListProvider getProductListProvider();

    /**
     * Returns the filtered product list provider.
     *
     * @param filter editor filter
     * @return {@link ListProvider}
     */
    ListProvider getProductListProvider(EditorCreator filter);

    /**
     * Returns sentence query for products matching QBF filter.
     *
     * @return {@link SentenceList} of {@link ProductInfoExt}
     * @deprecated Use {@link #getProductListProvider(EditorCreator)} instead.
     */
    @Deprecated
    SentenceList<ProductInfoExt> getProductList();

    /**
     * Returns sentence query for normal (non-composite) products matching QBF filter.
     *
     * @return {@link SentenceList} of {@link ProductInfoExt}
     * @deprecated Use {@link #getProductListProvider(EditorCreator)} instead.
     */
    @Deprecated
    SentenceList<ProductInfoExt> getProductListNormal();

    /**
     * Returns sentence query for auxiliary (composite modifier) products matching QBF filter.
     *
     * @return {@link SentenceList} of {@link ProductInfoExt}
     * @deprecated Use {@link #getProductListProvider(EditorCreator)} instead.
     */
    @Deprecated
    SentenceList<ProductInfoExt> getProductListAuxiliar();

    /**
     * Returns sentence query for root-level categories.
     *
     * @return {@link SentenceList} of {@link CategoryInfo}
     * @deprecated Use {@link #getRootCategories()} instead.
     */
    @Deprecated
    SentenceList<CategoryInfo> getCategoriesList_1();

    /**
     * Returns raw sentence list for product QBF query.
     *
     * @return {@link SentenceList}
     * @deprecated Use {@link #getProductListProvider()} instead.
     */
    @Deprecated
    SentenceList getProductCatQBF();

    /**
     * Returns sentence for inserting a product.
     *
     * @return {@link SentenceExec}
     * @deprecated Use {@link #getProductSaveProvider()} instead.
     */
    @Deprecated
    SentenceExec productInsert();

    /**
     * Returns sentence for updating a product.
     *
     * @return {@link SentenceExec}
     * @deprecated Use {@link #getProductSaveProvider()} instead.
     */
    @Deprecated
    SentenceExec productUpdate();

    /**
     * Returns sentence for deleting a product from catalog and products table.
     *
     * @return {@link SentenceExec}
     * @deprecated Use {@link #getProductSaveProvider()} instead.
     */
    @Deprecated
    SentenceExec getProductCatDelete();
}
