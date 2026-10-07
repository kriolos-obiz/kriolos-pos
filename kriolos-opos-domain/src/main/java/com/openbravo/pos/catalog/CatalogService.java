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
import com.openbravo.pos.inventory.AttributeSetInfo;
import com.openbravo.pos.inventory.UomInfo;
import com.openbravo.pos.pim.CategoryInfo;
import com.openbravo.pos.ticket.ProductInfoExt;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Service port interface for Catalog, Product Information Management (PIM), and UOM queries.
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

    BufferedImage getProductImage(String productId);

    List<ProductInfoExt> getProductCatalog(String categoryId) throws BasicException;

    List<ProductInfoExt> getProductConstant() throws BasicException;

    List<ProductInfoExt> getProductComposite(String productId) throws BasicException;

    UomInfo getUomInfoById(String uomId) throws BasicException;

    List<UomInfo> getUomListAll();

    List<AttributeSetInfo> getAttributeSetListAll();
}
