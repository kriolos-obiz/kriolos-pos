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
package com.openbravo.pos.epm;

import java.util.Objects;

/**
 * Immutable command to begin an employee shift break.
 *
 * @param userId  identifier of the employee starting the break
 * @param breakId identifier of the break type/category
 */
public record StartBreakCommand(String userId, String breakId) {

    /**
     * Compact constructor validating business invariants.
     */
    public StartBreakCommand {
        Objects.requireNonNull(userId, "User ID must not be null");
        if (userId.isBlank()) {
            throw new IllegalArgumentException("User ID must not be blank");
        }
        Objects.requireNonNull(breakId, "Break ID must not be null");
        if (breakId.isBlank()) {
            throw new IllegalArgumentException("Break ID must not be blank");
        }
    }

    /**
     * Static factory method to create a {@link StartBreakCommand}.
     *
     * @param userId  employee identifier
     * @param breakId break type identifier
     * @return a new valid {@link StartBreakCommand}
     */
    public static StartBreakCommand of(String userId, String breakId) {
        return new StartBreakCommand(userId, breakId);
    }
}

