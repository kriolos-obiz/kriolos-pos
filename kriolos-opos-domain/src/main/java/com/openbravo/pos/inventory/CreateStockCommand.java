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
import java.util.Objects;

/**
 * Immutable command record for creating or initializing inventory stock in a warehouse location.
 *
 * @param locationId             the warehouse location ID (non-null, non-blank)
 * @param productId              the product ID (non-null, non-blank)
 * @param attributeSetInstanceId optional attribute set instance ID (null if unassigned)
 * @param units                  the stock quantity units (must be finite)
 */
public record CreateStockCommand(
        String locationId,
        String productId,
        String attributeSetInstanceId,
        double units
) implements Serializable {

    public CreateStockCommand {
        Objects.requireNonNull(locationId, "locationId cannot be null");
        if (locationId.isBlank()) {
            throw new IllegalArgumentException("locationId cannot be blank");
        }
        Objects.requireNonNull(productId, "productId cannot be null");
        if (productId.isBlank()) {
            throw new IllegalArgumentException("productId cannot be blank");
        }
        if (!Double.isFinite(units)) {
            throw new IllegalArgumentException("units must be a finite number");
        }
        locationId = locationId.trim();
        productId = productId.trim();
        if (attributeSetInstanceId != null) {
            attributeSetInstanceId = attributeSetInstanceId.trim();
            if (attributeSetInstanceId.isEmpty()) {
                attributeSetInstanceId = null;
            }
        }
    }

    /**
     * Creates a {@link CreateStockCommand} without an attribute set instance.
     *
     * @param locationId warehouse location ID
     * @param productId  product ID
     * @param units      stock quantity units
     * @return validated CreateStockCommand
     */
    public static CreateStockCommand of(String locationId, String productId, double units) {
        return new CreateStockCommand(locationId, productId, null, units);
    }

    /**
     * Creates a {@link CreateStockCommand} with an optional attribute set instance ID.
     *
     * @param locationId             warehouse location ID
     * @param productId              product ID
     * @param attributeSetInstanceId attribute set instance ID
     * @param units                  stock quantity units
     * @return validated CreateStockCommand
     */
    public static CreateStockCommand of(String locationId, String productId, String attributeSetInstanceId, double units) {
        return new CreateStockCommand(locationId, productId, attributeSetInstanceId, units);
    }
}
