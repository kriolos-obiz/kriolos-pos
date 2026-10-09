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

import java.io.Serializable;

/**
 * Domain record representing an inventory stock entry in a warehouse location.
 *
 * @param locationId             the warehouse location ID
 * @param productId              the product ID
 * @param attributeSetInstanceId optional attribute set instance ID (null if unassigned)
 * @param units                  stock quantity units
 */
public record StockEntry(
        String locationId,
        String productId,
        String attributeSetInstanceId,
        double units
) implements Serializable {

    /**
     * Convenience constructor for stock entries without attribute set instance.
     *
     * @param locationId warehouse location ID
     * @param productId  product ID
     * @param units      stock quantity units
     */
    public StockEntry(String locationId, String productId, double units) {
        this(locationId, productId, null, units);
    }
}
