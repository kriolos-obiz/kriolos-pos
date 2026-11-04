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

package com.openbravo.pos.customers;

import java.util.Date;
import java.util.Objects;

/**
 * Immutable command record representing a typed customer debt and transaction date update.
 *
 * @param customerId      the unique identifier of the customer (mandatory)
 * @param accumulatedDebt the new accumulated debt balance (mandatory)
 * @param date            the timestamp of the debt update (mandatory)
 */
public record CustomerDebtUpdateCommand(
        String customerId,
        Double accumulatedDebt,
        Date date
) {

    /**
     * Compact constructor validating required invariants and enforcing immutability.
     */
    public CustomerDebtUpdateCommand {
        Objects.requireNonNull(customerId, "customerId must not be null");
        if (customerId.isBlank()) {
            throw new IllegalArgumentException("customerId must not be blank");
        }
        Objects.requireNonNull(accumulatedDebt, "accumulatedDebt must not be null");
        Objects.requireNonNull(date, "date must not be null");
        date = new Date(date.getTime());
    }

    /**
     * Returns a defensive copy of the update timestamp.
     *
     * @return copy of the timestamp
     */
    @Override
    public Date date() {
        return new Date(date.getTime());
    }

    /**
     * Convenience factory for creating customer debt update commands.
     *
     * @param customerId      customer identifier
     * @param accumulatedDebt new debt balance
     * @param date            update timestamp
     * @return new CustomerDebtUpdateCommand
     */
    public static CustomerDebtUpdateCommand of(String customerId, Double accumulatedDebt, Date date) {
        return new CustomerDebtUpdateCommand(customerId, accumulatedDebt, date);
    }
}

