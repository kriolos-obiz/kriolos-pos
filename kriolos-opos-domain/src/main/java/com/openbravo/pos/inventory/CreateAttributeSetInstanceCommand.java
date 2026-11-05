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
package com.openbravo.pos.inventory;

import java.util.List;
import java.util.Objects;

/**
 * Immutable command to query or create an attribute set instance with assigned values.
 *
 * @param attributeSetId attribute set identifier
 * @param description    combined description string
 * @param entries        immutable list of attribute instance value entries
 */
public record CreateAttributeSetInstanceCommand(
        String attributeSetId,
        String description,
        List<AttributeInstEntry> entries
) {

    /**
     * Compact constructor enforcing business invariants.
     */
    public CreateAttributeSetInstanceCommand {
        Objects.requireNonNull(attributeSetId, "attributeSetId must not be null");
        if (attributeSetId.isBlank()) {
            throw new IllegalArgumentException("attributeSetId must not be blank");
        }
        if (description == null) {
            description = "";
        }
        entries = entries == null ? List.of() : List.copyOf(entries);
    }

    /**
     * Factory method creating a validated command.
     *
     * @param attributeSetId attribute set ID
     * @param description    description string
     * @param entries        attribute instance values
     * @return validated CreateAttributeSetInstanceCommand
     */
    public static CreateAttributeSetInstanceCommand of(String attributeSetId, String description, List<AttributeInstEntry> entries) {
        return new CreateAttributeSetInstanceCommand(attributeSetId, description, entries);
    }
}

