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

import java.util.Date;
import java.util.Objects;

/**
 * Immutable command to record a stock movement in the stock diary ledger.
 *
 * @param id unique movement identifier
 * @param date timestamp of the movement
 * @param reasonKey movement reason key (e.g. sale, refund, breakage)
 * @param location warehouse location identifier
 * @param productId product identifier
 * @param attSetInstId attribute set instance identifier (optional)
 * @param units quantity moved (positive or negative)
 * @param price unit price or cost of the movement
 * @param appUser user who performed or authorized the movement
 * @author KriolOS
 */
public record RecordStockMovementCommand(
        String id,
        Date date,
        int reasonKey,
        String location,
        String productId,
        String attSetInstId,
        double units,
        double price,
        String appUser
) {

    public RecordStockMovementCommand {
        Objects.requireNonNull(id, "id must not be null");
        if (id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        Objects.requireNonNull(date, "date must not be null");
        Objects.requireNonNull(location, "location must not be null");
        if (location.isBlank()) {
            throw new IllegalArgumentException("location must not be blank");
        }
        Objects.requireNonNull(productId, "productId must not be null");
        if (productId.isBlank()) {
            throw new IllegalArgumentException("productId must not be blank");
        }
        Objects.requireNonNull(appUser, "appUser must not be null");
        if (appUser.isBlank()) {
            throw new IllegalArgumentException("appUser must not be blank");
        }
        if (!Double.isFinite(units)) {
            throw new IllegalArgumentException("units must be a finite number: " + units);
        }
        if (!Double.isFinite(price)) {
            throw new IllegalArgumentException("price must be a finite number: " + price);
        }

        id = id.trim();
        date = new Date(date.getTime());
        location = location.trim();
        productId = productId.trim();
        attSetInstId = (attSetInstId != null && !attSetInstId.isBlank()) ? attSetInstId.trim() : null;
        appUser = appUser.trim();
    }

    @Override
    public Date date() {
        return new Date(date.getTime());
    }

    public static RecordStockMovementCommand of(
            String id,
            Date date,
            int reasonKey,
            String location,
            String productId,
            String attSetInstId,
            double units,
            double price,
            String appUser
    ) {
        return new RecordStockMovementCommand(
                id, date, reasonKey, location, productId, attSetInstId, units, price, appUser);
    }
}

