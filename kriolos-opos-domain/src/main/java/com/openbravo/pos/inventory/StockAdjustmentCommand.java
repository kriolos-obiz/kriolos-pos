//    KriolOS POS
//    Copyright (c) 2019-2026 KriolOS
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.

package com.openbravo.pos.inventory;

/**
 * Immutable command record representing a typed stock level adjustment.
 *
 * @param productId               the identifier of the product to adjust (mandatory)
 * @param locationId              the warehouse or inventory location identifier (mandatory)
 * @param attributeSetInstanceId the attribute set instance identifier (nullable for standard products)
 * @param units                   the quantity to adjust (signed; positive adds stock, negative removes stock)
 */
public record StockAdjustmentCommand(
        String productId,
        String locationId,
        String attributeSetInstanceId,
        double units
) {

    /**
     * Compact constructor validating required invariants.
     */
    public StockAdjustmentCommand {
        if (productId == null || productId.isBlank()) {
            throw new IllegalArgumentException("productId must not be null or blank");
        }
        if (locationId == null || locationId.isBlank()) {
            throw new IllegalArgumentException("locationId must not be null or blank");
        }
    }

    /**
     * Convenience factory for stock adjustments without attribute set instances.
     *
     * @param productId  product identifier
     * @param locationId location identifier
     * @param units      quantity
     * @return new StockAdjustmentCommand
     */
    public static StockAdjustmentCommand of(String productId, String locationId, double units) {
        return new StockAdjustmentCommand(productId, locationId, null, units);
    }
}

