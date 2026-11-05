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

import com.openbravo.pos.ticket.TicketInfo;
import java.util.Objects;

/**
 * Immutable command to persist or update a parked / shared ticket.
 *
 * @param id       shared ticket identifier (e.g. place ID or receipt UUID)
 * @param ticket   ticket state and line items to serialize
 * @param pickupId pickup order sequence ID
 */
public record SaveSharedTicketCommand(
        String id,
        TicketInfo ticket,
        int pickupId
) {

    /**
     * Compact constructor enforcing domain invariants.
     */
    public SaveSharedTicketCommand {
        Objects.requireNonNull(id, "Shared ticket ID must not be null");
        if (id.isBlank()) {
            throw new IllegalArgumentException("Shared ticket ID must not be blank");
        }
        Objects.requireNonNull(ticket, "TicketInfo must not be null");
        if (pickupId < 0) {
            throw new IllegalArgumentException("Pickup ID cannot be negative");
        }
    }

    /**
     * Factory method creating a SaveSharedTicketCommand.
     *
     * @param id       shared ticket ID
     * @param ticket   ticket payload
     * @param pickupId pickup order sequence number
     * @return new validated SaveSharedTicketCommand
     */
    public static SaveSharedTicketCommand of(String id, TicketInfo ticket, int pickupId) {
        return new SaveSharedTicketCommand(id, ticket, pickupId);
    }
}

