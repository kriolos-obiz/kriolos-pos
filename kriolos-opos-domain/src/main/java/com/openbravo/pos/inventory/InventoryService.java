package com.openbravo.pos.inventory;

import com.openbravo.basic.BasicException;

/**
 * Service interface for Inventory operations.
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

    void createStock(String locationId, String productId, double units) throws BasicException;

    void updateStock(String locationId, String productId, double units) throws BasicException;
}
