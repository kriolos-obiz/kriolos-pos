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
 * Immutable command to update the layout coordinates (X, Y) of a restaurant dining place/table.
 *
 * @param id the unique identifier of the place/table
 * @param x  the X coordinate on the visual floor plan
 * @param y  the Y coordinate on the visual floor plan
 */
public record UpdatePlacePositionCommand(String id, int x, int y) {

    /**
     * Compact constructor validating business invariants.
     */
    public UpdatePlacePositionCommand {
        Objects.requireNonNull(id, "Place id must not be null");
        if (id.isBlank()) {
            throw new IllegalArgumentException("Place id must not be blank");
        }
    }

    /**
     * Static factory method to create an {@link UpdatePlacePositionCommand}.
     *
     * @param id the unique identifier of the place/table
     * @param x  the X coordinate
     * @param y  the Y coordinate
     * @return a new valid command instance
     */
    public static UpdatePlacePositionCommand of(String id, int x, int y) {
        return new UpdatePlacePositionCommand(id, x, y);
    }
}

