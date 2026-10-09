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

package com.openbravo.pos.sales;

import java.util.Date;

/**
 * Immutable record carrying product, location, pricing, and stock metrics
 * for display in ProductStockInfoPanel.
 */
public record ProductStockDetails(
        String productName,
        String categoryName,
        String reference,
        String barcode,
        String locationName,
        Double units,
        Double minimum,
        Double maximum,
        Double priceSell,
        Date memoDate
) {
    public ProductStockDetails {
        units = units != null ? units : 0.0;
        minimum = minimum != null ? minimum : 0.0;
        maximum = maximum != null ? maximum : 0.0;
    }

    /**
     * Factory method for creating details with minimal parameters.
     */
    public static ProductStockDetails of(Double units, Double max, Double min, Date memoDate) {
        return new ProductStockDetails(null, null, null, null, null, units, min, max, null, memoDate);
    }

    /**
     * Factory method for creating details with product name, category, and stock metrics.
     */
    public static ProductStockDetails of(String productName, String categoryName, Double units, Double max, Double min, Date memoDate) {
        return new ProductStockDetails(productName, categoryName, null, null, null, units, min, max, null, memoDate);
    }
}
