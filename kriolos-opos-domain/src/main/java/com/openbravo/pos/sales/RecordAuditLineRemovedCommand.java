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
package com.openbravo.pos.sales;

import java.util.Date;
import java.util.Objects;

/**
 * Immutable command record representing an audit entry for a removed ticket line.
 * Enforces validation invariants and defensive date copying.
 *
 * @param username    The user performing the line removal (mandatory)
 * @param ticketId    The ticket identifier (optional)
 * @param productId   The product identifier (optional)
 * @param productName The product description/name (mandatory)
 * @param units       The quantity of units removed
 * @param date        The timestamp of the removal (mandatory, defensively copied)
 */
public record RecordAuditLineRemovedCommand(
        String username,
        String ticketId,
        String productId,
        String productName,
        double units,
        Date date
) {
    public RecordAuditLineRemovedCommand {
        Objects.requireNonNull(username, "username must not be null");
        if (username.isBlank()) {
            throw new IllegalArgumentException("username must not be blank");
        }
        username = username.trim();

        ticketId = (ticketId == null || ticketId.isBlank()) ? null : ticketId.trim();
        productId = (productId == null || productId.isBlank()) ? null : productId.trim();

        Objects.requireNonNull(productName, "productName must not be null");
        if (productName.isBlank()) {
            throw new IllegalArgumentException("productName must not be blank");
        }
        productName = productName.trim();

        Objects.requireNonNull(date, "date must not be null");
        date = new Date(date.getTime());
    }

    @Override
    public Date date() {
        return new Date(date.getTime());
    }

    /**
     * Factory method creating a command with the current timestamp.
     */
    public static RecordAuditLineRemovedCommand of(
            String username,
            String ticketId,
            String productId,
            String productName,
            double units
    ) {
        return new RecordAuditLineRemovedCommand(username, ticketId, productId, productName, units, new Date());
    }

    /**
     * Factory method creating a command with an explicit timestamp.
     */
    public static RecordAuditLineRemovedCommand of(
            String username,
            String ticketId,
            String productId,
            String productName,
            double units,
            Date date
    ) {
        return new RecordAuditLineRemovedCommand(username, ticketId, productId, productName, units, date);
    }
}

