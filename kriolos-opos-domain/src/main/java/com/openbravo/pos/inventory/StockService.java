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
package com.openbravo.pos.inventory;

import com.openbravo.basic.BasicException;
import com.openbravo.data.user.SaveProvider;
import java.util.Date;
import java.util.List;

/**
 * Domain service port interface for stock ledger, diary entries, inventory movements, and stock calculations.
 */
public interface StockService extends InventoryService {

    /**
     * SQL template for bundle product breakdown.
     */
    String SQL_BUNDLE_LIST = DataLogicInventory.SQL_BUNDLE_LIST;

    /**
     * SQL template for auxiliary product associations.
     */
    String SQL_AUXILIAR_LIST = DataLogicInventory.SQL_AUXILIAR_LIST;

    /**
     * Records a stock movement ledger entry and adjusts current stock balances.
     *
     * @param id           unique movement UUID
     * @param date         timestamp of movement
     * @param reasonKey    movement reason identifier (e.g. sale, refund, break)
     * @param location     warehouse location ID
     * @param productId    product ID
     * @param attSetInstId attribute set instance ID (optional)
     * @param units        units moved (positive or negative)
     * @param price        unit cost/price
     * @param appUser      user performing the operation
     * @throws BasicException on persistence error
     */
    void recordStockMovement(
            String id,
            Date date,
            int reasonKey,
            String location,
            String productId,
            String attSetInstId,
            double units,
            double price,
            String appUser
    ) throws BasicException;

    /**
     * Reverts a stock movement by diary entry ID and adjusts current stock balances.
     *
     * @param diaryId      diary record UUID to delete
     * @param location     warehouse location ID
     * @param productId    product ID
     * @param attSetInstId attribute set instance ID (optional)
     * @param units        units to subtract from current stock
     * @throws BasicException on persistence error
     */
    void revertStockMovement(
            String diaryId,
            String location,
            String productId,
            String attSetInstId,
            double units
    ) throws BasicException;

    /**
     * Persists a structured stock movement transaction into the stock diary.
     *
     * @param prodStock the stock movement transaction record
     * @throws BasicException on persistence error
     */
    void saveStockDiary(ProductStockTransaction prodStock) throws BasicException;

    /**
     * Adjusts product stock levels, decomposing product bundles if applicable.
     *
     * @param params parameters array: [productId, locationId, attributesetinstanceId, units]
     * @throws BasicException on database operation error
     */
    void adjustStock(Object[] params) throws BasicException;

    /**
     * Returns the record save provider for stock diary movements (insert and delete).
     *
     * @return SaveProvider for stock diary records
     */
    SaveProvider getStockDiarySaveProvider();

    /**
     * Returns the current stock units for a specific warehouse, product, and attribute instance.
     *
     * @param warehouse    the warehouse/location ID
     * @param id           the product ID
     * @param attsetinstid optional attribute set instance ID
     * @return current stock units available
     * @throws BasicException on query error
     */
    double findProductStock(String warehouse, String id, String attsetinstid) throws BasicException;

    /**
     * Retrieves the stock state of a product at a specific warehouse location.
     *
     * @param pId      the product ID
     * @param location the location ID
     * @return ProductStock details or null
     * @throws BasicException on database query error
     */
    ProductStock getProductStockState(String pId, String location) throws BasicException;

    /**
     * Retrieves stock state records across all warehouse locations for a given product.
     *
     * @param pId the product ID
     * @return list of ProductStock records across locations
     * @throws BasicException on database query error
     */
    List<ProductStock> getProductStockList(String pId) throws BasicException;
}
