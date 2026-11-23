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

import java.util.Objects;

/**
 * Immutable command to revert a stock movement and adjust current inventory balances.
 *
 * @param diaryId unique diary record identifier to revert/delete
 * @param location warehouse location identifier
 * @param productId product identifier
 * @param attSetInstId attribute set instance identifier (optional)
 * @param units quantity to subtract from current stock
 * @author KriolOS
 */
public record RevertStockMovementCommand(
        String diaryId,
        String location,
        String productId,
        String attSetInstId,
        double units
) {

    public RevertStockMovementCommand {
        Objects.requireNonNull(diaryId, "diaryId must not be null");
        if (diaryId.isBlank()) {
            throw new IllegalArgumentException("diaryId must not be blank");
        }
        Objects.requireNonNull(location, "location must not be null");
        if (location.isBlank()) {
            throw new IllegalArgumentException("location must not be blank");
        }
        Objects.requireNonNull(productId, "productId must not be null");
        if (productId.isBlank()) {
            throw new IllegalArgumentException("productId must not be blank");
        }
        if (!Double.isFinite(units)) {
            throw new IllegalArgumentException("units must be a finite number: " + units);
        }

        diaryId = diaryId.trim();
        location = location.trim();
        productId = productId.trim();
        attSetInstId = (attSetInstId != null && !attSetInstId.isBlank()) ? attSetInstId.trim() : null;
    }

    public static RevertStockMovementCommand of(
            String diaryId,
            String location,
            String productId,
            String attSetInstId,
            double units
    ) {
        return new RevertStockMovementCommand(diaryId, location, productId, attSetInstId, units);
    }
}

