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
package com.openbravo.pos.sales.restaurant;

import java.util.Objects;

/**
 * Immutable command to move a customer and ticket session from one restaurant table to another.
 *
 * @param newTable name of the destination table
 * @param ticketId unique identifier of the active ticket session
 */
public record MoveCustomerCommand(
        String newTable,
        String ticketId
) {

    /**
     * Compact constructor validating business invariants.
     */
    public MoveCustomerCommand {
        Objects.requireNonNull(newTable, "Destination table must not be null");
        if (newTable.isBlank()) {
            throw new IllegalArgumentException("Destination table must not be blank");
        }
        Objects.requireNonNull(ticketId, "Ticket ID must not be null");
        if (ticketId.isBlank()) {
            throw new IllegalArgumentException("Ticket ID must not be blank");
        }
    }

    /**
     * Factory method creating a valid MoveCustomerCommand.
     *
     * @param newTable destination table name
     * @param ticketId ticket identifier
     * @return new validated MoveCustomerCommand
     */
    public static MoveCustomerCommand of(String newTable, String ticketId) {
        return new MoveCustomerCommand(newTable, ticketId);
    }
}

