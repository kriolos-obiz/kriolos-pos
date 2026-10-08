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
import com.openbravo.data.loader.SentenceExec;
import com.openbravo.data.loader.SentenceList;
import com.openbravo.data.loader.TableDefinition;
import com.openbravo.data.model.Row;
import com.openbravo.data.user.EditorCreator;
import com.openbravo.data.user.ListProvider;
import com.openbravo.data.user.SaveProvider;
import com.openbravo.pos.inventory.AttributeSetInfo;
import com.openbravo.pos.inventory.UomInfo;
import com.openbravo.pos.pim.CategoryInfo;
import com.openbravo.pos.ticket.ProductInfoExt;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Service port interface for Catalog, Product Information Management (PIM), Pricing, and UOM queries.
 *
 * @author poolborges
 */
public interface CatalogService {

    ProductInfoExt getProductInfo(String id) throws BasicException;

    ProductInfoExt getProductInfoByCode(String code) throws BasicException;

    ProductInfoExt getProductInfoByShortCode(String code) throws BasicException;

    ProductInfoExt getProductInfoByUShortCode(String code) throws BasicException;

    ProductInfoExt getProductInfoByReference(String reference) throws BasicException;

    CategoryInfo getCategoryInfo(String id) throws BasicException;

    List<CategoryInfo> getRootCategories() throws BasicException;

    List<CategoryInfo> getSubcategories(String categoryId) throws BasicException;

    void createCategory(Object[] category) throws BasicException;

    List<CategoryInfo> getCategoriesListAll();

    List<CategoryStock> getCategorysProductList(String categoryId) throws BasicException;

    int addProductsToCatalogWithCategoryId(String categoryId) throws BasicException;

    int removeProductsFromCatalogWithCategoryId(String categoryId) throws BasicException;

    void updateProductPrice(String productId, double newPrice) throws BasicException;

    BufferedImage getProductImage(String productId);

    List<ProductInfoExt> getProductCatalog(String categoryId) throws BasicException;

    List<ProductInfoExt> getProductConstant() throws BasicException;

    List<ProductInfoExt> getProductComposite(String productId) throws BasicException;

    TableDefinition getTableCategories();

    TableDefinition getTableUom();

    UomInfo getUomInfoById(String uomId) throws BasicException;

    List<UomInfo> getUomListAll();

    List<AttributeSetInfo> getAttributeSetListAll();

    Row getProductsRow();

    SaveProvider getProductSaveProvider();

    ListProvider getProductListProvider();

    ListProvider getProductListProvider(EditorCreator filter);

    @Deprecated
    SentenceList<ProductInfoExt> getProductList();

    @Deprecated
    SentenceList<ProductInfoExt> getProductListNormal();

    @Deprecated
    SentenceList<ProductInfoExt> getProductListAuxiliar();

    @Deprecated
    SentenceList<CategoryInfo> getCategoriesList_1();

    @Deprecated
    SentenceList getProductCatQBF();

    @Deprecated
    SentenceExec productInsert();

    @Deprecated
    SentenceExec productUpdate();

    @Deprecated
    SentenceExec getProductCatDelete();
}
