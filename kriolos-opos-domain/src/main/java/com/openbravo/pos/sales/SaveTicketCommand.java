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
 * Immutable command to save and finalize a sales ticket into persistence and inventory.
 *
 * @param ticket   the ticket entity to save
 * @param location warehouse inventory location identifier
 */
public record SaveTicketCommand(
        TicketInfo ticket,
        String location
) {

    /**
     * Compact constructor enforcing domain invariants.
     */
    public SaveTicketCommand {
        Objects.requireNonNull(ticket, "Ticket must not be null");
    }

    /**
     * Factory method creating a validated SaveTicketCommand.
     *
     * @param ticket   ticket entity
     * @param location warehouse inventory location
     * @return new SaveTicketCommand instance
     */
    public static SaveTicketCommand of(TicketInfo ticket, String location) {
        return new SaveTicketCommand(ticket, location);
    }
}

