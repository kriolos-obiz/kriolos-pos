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

import com.openbravo.pos.customers.CustomerInfoExt;

/**
 * Immutable record carrying customer VIP, discount, and account details
 * for display in CustomerDiscountInfoPanel.
 */
public record CustomerDiscountDetails(
        String customerName,
        String taxId,
        String card,
        boolean vip,
        Double discountPercent,
        Double currentDebt,
        Double maxDebt,
        String notes
) {
    public CustomerDiscountDetails {
        discountPercent = discountPercent != null ? discountPercent : 0.0;
    }

    /**
     * Factory method to extract customer discount details from a CustomerInfoExt domain model.
     */
    public static CustomerDiscountDetails fromCustomer(CustomerInfoExt customer) {
        if (customer == null) {
            return new CustomerDiscountDetails(null, null, null, false, 0.0, null, null, null);
        }
        return new CustomerDiscountDetails(
                customer.getName(),
                customer.getTaxid(),
                customer.getCard(),
                customer.isVIP(),
                customer.getDiscount(),
                customer.getCurDebt(),
                customer.getMaxdebt(),
                customer.getNotes()
        );
    }

    /**
     * Factory method for creating details with minimal parameters.
     */
    public static CustomerDiscountDetails of(String name, boolean vip, Double discount) {
        return new CustomerDiscountDetails(name, null, null, vip, discount, null, null, null);
    }
}
