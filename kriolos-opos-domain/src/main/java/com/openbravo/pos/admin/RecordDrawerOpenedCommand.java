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
package com.openbravo.pos.admin;

import java.util.Date;
import java.util.Objects;

/**
 * Immutable command to record a physical cash drawer open audit event.
 *
 * @param name     user or terminal identifier responsible for opening the drawer
 * @param action   event description, operation name, or associated ticket identifier
 * @param openDate timestamp of the drawer open event
 */
public record RecordDrawerOpenedCommand(
        String name,
        String action,
        Date openDate
) {

    /**
     * Compact constructor validating business invariants.
     */
    public RecordDrawerOpenedCommand {
        Objects.requireNonNull(name, "User/terminal name must not be null");
        if (name.isBlank()) {
            throw new IllegalArgumentException("User/terminal name must not be blank");
        }
        if (action == null) {
            action = "";
        }
        if (openDate == null) {
            openDate = new Date();
        } else {
            openDate = new Date(openDate.getTime());
        }
    }

    @Override
    public Date openDate() {
        return new Date(openDate.getTime());
    }

    /**
     * Factory method creating a drawer open command with an explicit timestamp.
     *
     * @param name     user or terminal name
     * @param action   event context or ticket ID
     * @param openDate timestamp
     * @return a new valid {@link RecordDrawerOpenedCommand}
     */
    public static RecordDrawerOpenedCommand of(String name, String action, Date openDate) {
        return new RecordDrawerOpenedCommand(name, action, openDate);
    }

    /**
     * Factory method creating a drawer open command with the current timestamp.
     *
     * @param name   user or terminal name
     * @param action event context or ticket ID
     * @return a new valid {@link RecordDrawerOpenedCommand}
     */
    public static RecordDrawerOpenedCommand of(String name, String action) {
        return new RecordDrawerOpenedCommand(name, action, new Date());
    }
}

