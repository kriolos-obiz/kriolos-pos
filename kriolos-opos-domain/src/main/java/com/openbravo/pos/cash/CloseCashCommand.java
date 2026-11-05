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
package com.openbravo.pos.cash;

import java.util.Date;
import java.util.Objects;

/**
 * Immutable command to close a cash session in the point of sale.
 *
 * @param host     terminal host identifier
 * @param sequence cash sequence number
 * @param money    money session identifier / token
 * @param dateEnd  session end timestamp
 * @param noSales  count of no-sale operations in this cash cycle
 */
public record CloseCashCommand(
        String host,
        int sequence,
        String money,
        Date dateEnd,
        int noSales
) {

    /**
     * Compact constructor enforcing business invariants and defensive date copying.
     */
    public CloseCashCommand {
        Objects.requireNonNull(host, "host must not be null");
        if (host.isBlank()) {
            throw new IllegalArgumentException("host must not be blank");
        }
        Objects.requireNonNull(money, "money identifier must not be null");
        if (money.isBlank()) {
            throw new IllegalArgumentException("money identifier must not be blank");
        }
        dateEnd = dateEnd != null ? new Date(dateEnd.getTime()) : new Date();
        if (noSales < 0) {
            throw new IllegalArgumentException("noSales cannot be negative");
        }
    }

    /**
     * Defensive copy getter for dateEnd.
     *
     * @return copy of session end timestamp
     */
    @Override
    public Date dateEnd() {
        return new Date(dateEnd.getTime());
    }

    /**
     * Factory method creating a CloseCashCommand with explicit closing timestamp.
     *
     * @param host     terminal host identifier
     * @param sequence cash sequence number
     * @param money    money session token
     * @param dateEnd  closing timestamp
     * @param noSales  count of no-sales
     * @return validated CloseCashCommand
     */
    public static CloseCashCommand of(String host, int sequence, String money, Date dateEnd, int noSales) {
        return new CloseCashCommand(host, sequence, money, dateEnd, noSales);
    }

    /**
     * Factory method creating a CloseCashCommand defaulting dateEnd to current time.
     *
     * @param host     terminal host identifier
     * @param sequence cash sequence number
     * @param money    money session token
     * @param noSales  count of no-sales
     * @return validated CloseCashCommand
     */
    public static CloseCashCommand of(String host, int sequence, String money, int noSales) {
        return new CloseCashCommand(host, sequence, money, new Date(), noSales);
    }
}

