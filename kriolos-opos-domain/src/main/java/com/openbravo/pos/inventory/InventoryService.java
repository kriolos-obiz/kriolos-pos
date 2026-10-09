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
import com.openbravo.data.loader.SentenceList;
import com.openbravo.data.loader.SerializerRead;
import com.openbravo.data.loader.TableDefinition;
import com.openbravo.data.user.EditorCreator;
import com.openbravo.data.user.ListProvider;
import com.openbravo.data.user.SaveProvider;
import java.util.Collections;
import java.util.List;

/**
 * Service port interface for Warehouse, Location, and Inventory level operations.
 */
public interface InventoryService {

    /**
     * Get stock information for a product at a specific location.
     * 
     * @param productId  The ID of the product.
     * @param locationId The ID of the warehouse/location.
     * @return ProductStock object containing stock details, or null if not found.
     * @throws BasicException If an error occurs during retrieval.
     */
    ProductStock getStock(String productId, String locationId) throws BasicException;

    /**
     * Adds or adjusts a stock entry using domain record {@link StockEntry}.
     *
     * @param entry the stock entry record
     * @throws BasicException on database operation failure
     */
    default void addStockEntry(StockEntry entry) throws BasicException {
        if (entry != null) {
            addStockEntry(entry.locationId(), entry.productId(), entry.attributeSetInstanceId(), entry.units());
        }
    }

    /**
     * Adds an initial or direct stock entry without attribute instance.
     *
     * @param locationId warehouse location ID
     * @param productId  product ID
     * @param units      stock units
     * @throws BasicException on database operation failure
     */
    default void addStockEntry(String locationId, String productId, double units) throws BasicException {
        addStockEntry(locationId, productId, null, units);
    }

    /**
     * Adds an initial or direct stock entry with optional attribute set instance ID.
     *
     * @param locationId             warehouse location ID
     * @param productId              product ID
     * @param attributeSetInstanceId optional attribute set instance ID
     * @param units                  stock units
     * @throws BasicException on database operation failure
     */
    default void addStockEntry(String locationId, String productId, String attributeSetInstanceId, double units) throws BasicException {
        createStock(locationId, productId, units);
    }

    void createStock(String locationId, String productId, double units) throws BasicException;

    void updateStock(String locationId, String productId, double units) throws BasicException;

    /**
     * Returns table definition for warehouse locations management.
     *
     * @return TableDefinition for locations
     */
    default TableDefinition getTableLocations() {
        return null;
    }

    /**
     * Retrieves all configured warehouse locations ordered by name.
     *
     * @return list of LocationInfo records
     * @throws BasicException on persistence query error
     */
    default List<LocationInfo> getLocationsList() throws BasicException {
        return Collections.emptyList();
    }

    /**
     * Returns all configured warehouse locations.
     *
     * @return list of LocationInfo records
     */
    default List<LocationInfo> getLocationsListAll() {
        return Collections.emptyList();
    }

    /**
     * Resolves human-readable name of a location by its ID.
     *
     * @param iLocation location ID
     * @return location name, or null if not found
     * @throws BasicException on query error
     */
    default String findLocationName(String iLocation) throws BasicException {
        return null;
    }

    /**
     * Returns the list provider for warehouse stock levels filtered by an editor filter.
     *
     * @param filter editor creator filter (e.g. location parameter)
     * @return ListProvider for warehouse stock records
     */
    default ListProvider getWarehouseStockListProvider(EditorCreator filter) {
        return null;
    }

    /**
     * Returns sentence query for stock levels in a specific warehouse location.
     *
     * @param sr serializer reader for rows
     * @return SentenceList for warehouse stock query
     * @deprecated Use {@link #getWarehouseStockListProvider(EditorRecord)} instead.
     */
    @Deprecated
    default SentenceList getWarehouseStockList(SerializerRead sr) {
        return null;
    }

    /**
     * Inserts minimum and maximum stock security levels for a product at a location.
     *
     * @param id            stocklevel record ID
     * @param locationId    location ID
     * @param productId     product ID
     * @param stockSecurity minimum security units
     * @param stockMaximum  maximum security units
     * @throws BasicException on persistence error
     */
    default void insertStockLevel(String id, String locationId, String productId, Double stockSecurity, Double stockMaximum) throws BasicException {
    }

    /**
     * Updates minimum and maximum stock security levels for an existing record.
     *
     * @param id            stocklevel record ID
     * @param stockSecurity minimum security units
     * @param stockMaximum  maximum security units
     * @throws BasicException on persistence error
     */
    default void updateStockLevel(String id, Double stockSecurity, Double stockMaximum) throws BasicException {
    }

    /**
     * Returns the save provider for warehouse stock levels (insert and update).
     *
     * @return SaveProvider for warehouse stocklevel records
     */
    default SaveProvider getWarehouseStockSaveProvider() {
        return null;
    }
}
