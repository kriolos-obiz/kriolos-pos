package com.openbravo.pos.catalog;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.SentenceExec;
import com.openbravo.data.loader.SentenceList;
import com.openbravo.data.loader.Session;
import com.openbravo.data.loader.TableDefinition;
import com.openbravo.data.model.Row;
import com.openbravo.data.user.EditorCreator;
import com.openbravo.data.user.ListProvider;
import com.openbravo.data.user.SaveProvider;
import com.openbravo.pos.forms.BeanFactoryDataSingle;
import com.openbravo.pos.inventory.AttributeSetInfo;
import com.openbravo.pos.pim.CategoryInfo;
import com.openbravo.pos.pim.DataLogicCategories;
import com.openbravo.pos.pim.DataLogicProducts;
import com.openbravo.pos.pim.DataLogicUom;
import com.openbravo.pos.pim.UomInfo;
import com.openbravo.pos.ticket.ProductInfoExt;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Service implementation for {@link CatalogService} port.
 * Coordinates data access across isolated PIM data logic components:
 * {@link DataLogicProducts}, {@link DataLogicCategories}, and {@link DataLogicUom}.
 *
 * @author KriolOS
 */
public class CatalogServiceImpl extends BeanFactoryDataSingle implements CatalogService {

    private final DataLogicProducts dataLogicProducts;
    private final DataLogicCategories dataLogicCategories;
    private final DataLogicUom dataLogicUom;

    /**
     * Default constructor for BeanFactory instantiation.
     */
    public CatalogServiceImpl() {
        this.dataLogicProducts = new DataLogicProducts();
        this.dataLogicCategories = new DataLogicCategories();
        this.dataLogicUom = new DataLogicUom();
    }

    /**
     * Constructor for dependency injection and unit testing.
     *
     * @param dataLogicProducts   the products data logic component
     * @param dataLogicCategories the categories data logic component
     * @param dataLogicUom        the UOM data logic component
     */
    public CatalogServiceImpl(DataLogicProducts dataLogicProducts,
                              DataLogicCategories dataLogicCategories,
                              DataLogicUom dataLogicUom) {
        this.dataLogicProducts = dataLogicProducts;
        this.dataLogicCategories = dataLogicCategories;
        this.dataLogicUom = dataLogicUom;
    }

    /**
     * Initializes all underlying data logic components with the database session.
     *
     * @param sessionDB active database session
     */
    @Override
    public void init(Session sessionDB) {
        dataLogicProducts.init(sessionDB);
        dataLogicCategories.init(sessionDB);
        dataLogicUom.init(sessionDB);
    }

    // =========================================================================
    // Product Operations -> DataLogicProducts
    // =========================================================================

    @Override
    public ProductInfoExt getProductInfo(String id) throws BasicException {
        return dataLogicProducts.getProductInfo(id);
    }

    @Override
    public ProductInfoExt getProductInfoByCode(String code) throws BasicException {
        return dataLogicProducts.getProductInfoByCode(code);
    }

    @Override
    public ProductInfoExt getProductInfoByShortCode(String code) throws BasicException {
        return dataLogicProducts.getProductInfoByShortCode(code);
    }

    @Override
    public ProductInfoExt getProductInfoByUShortCode(String code) throws BasicException {
        return dataLogicProducts.getProductInfoByUShortCode(code);
    }

    @Override
    public ProductInfoExt getProductInfoByReference(String reference) throws BasicException {
        return dataLogicProducts.getProductInfoByReference(reference);
    }

    @Override
    public List<ProductInfoExt> getProductConstant() throws BasicException {
        return dataLogicProducts.getProductConstant();
    }

    @Override
    public List<ProductInfoExt> getProductComposite(String productId) throws BasicException {
        return dataLogicProducts.getProductComposite(productId);
    }

    @Override
    public List<ProductInfoExt> getProductCatalog(String categoryId) throws BasicException {
        return dataLogicProducts.getProductCatalog(categoryId);
    }

    @Override
    public List<CategoryStock> getCategorysProductList(String categoryId) throws BasicException {
        return dataLogicProducts.getCategorysProductList(categoryId);
    }

    @Override
    public int addProductsToCatalogWithCategoryId(String categoryId) throws BasicException {
        return dataLogicProducts.addProductsToCatalogWithCategoryId(categoryId);
    }

    @Override
    public int removeProductsFromCatalogWithCategoryId(String categoryId) throws BasicException {
        return dataLogicProducts.removeProductsFromCatalogWithCategoryId(categoryId);
    }

    @Override
    public void updateProductPrice(String productId, double newPrice) throws BasicException {
        dataLogicProducts.updateProductPrice(productId, newPrice);
    }

    @Override
    public BufferedImage getProductImage(String productId) {
        return dataLogicProducts.getProductImage(productId);
    }

    @Override
    public Row getProductsRow() {
        return dataLogicProducts.getProductsRow();
    }

    @Override
    public SaveProvider getProductSaveProvider() {
        return dataLogicProducts.getProductSaveProvider();
    }

    @Override
    public ListProvider getProductListProvider() {
        return dataLogicProducts.getProductListProvider();
    }

    @Override
    public ListProvider getProductListProvider(EditorCreator filter) {
        return dataLogicProducts.getProductListProvider(filter);
    }

    @Override
    public SentenceList<ProductInfoExt> getProductList() {
        return dataLogicProducts.getProductList();
    }

    @Override
    public SentenceList<ProductInfoExt> getProductListNormal() {
        return dataLogicProducts.getProductListNormal();
    }

    @Override
    public SentenceList<ProductInfoExt> getProductListAuxiliar() {
        return dataLogicProducts.getProductListAuxiliar();
    }

    @Override
    public SentenceList getProductCatQBF() {
        return dataLogicProducts.getProductCatQBF();
    }

    @Override
    public SentenceExec productInsert() {
        return dataLogicProducts.productInsert();
    }

    @Override
    public SentenceExec productUpdate() {
        return dataLogicProducts.productUpdate();
    }

    @Override
    public SentenceExec getProductCatDelete() {
        return dataLogicProducts.getProductCatDelete();
    }

    @Override
    public List<AttributeSetInfo> getAttributeSetListAll() {
        return dataLogicProducts.getAttributeSetListAll();
    }

    // =========================================================================
    // Category Operations -> DataLogicCategories
    // =========================================================================

    @Override
    public CategoryInfo getCategoryInfo(String id) throws BasicException {
        return dataLogicCategories.getCategoryInfo(id);
    }

    @Override
    public List<CategoryInfo> getRootCategories() throws BasicException {
        return dataLogicCategories.getRootCategories();
    }

    @Override
    public List<CategoryInfo> getSubcategories(String categoryId) throws BasicException {
        return dataLogicCategories.getSubcategories(categoryId);
    }

    @Override
    public void createCategory(String id, String name, boolean showName) throws BasicException {
        dataLogicCategories.createCategory(id, name, showName);
    }

    @Override
    public void createCategory(Object[] category) throws BasicException {
        dataLogicCategories.createCategory(category);
    }

    @Override
    public List<CategoryInfo> getCategoriesListAll() {
        return dataLogicCategories.getCategoriesListAll();
    }

    @Override
    public SentenceList<CategoryInfo> getCategoriesList_1() {
        return dataLogicCategories.getCategoriesList_1();
    }

    @Override
    public TableDefinition getTableCategories() {
        return dataLogicCategories.getTableCategories();
    }

    // =========================================================================
    // UOM Operations -> DataLogicUom
    // =========================================================================

    @Override
    public TableDefinition getTableUom() {
        return dataLogicUom.getTableUom();
    }

    @Override
    public UomInfo getUomInfoById(String uomId) throws BasicException {
        return dataLogicUom.getUomInfoById(uomId);
    }

    @Override
    public List<UomInfo> getUomListAll() {
        return dataLogicUom.getUomListAll();
    }
}
