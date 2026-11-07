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
package com.openbravo.pos.catalog;

import java.util.Objects;

/**
 * Immutable command record representing a request to create a new product category.
 * Enforces non-blank validation invariants and defensive string trimming.
 *
 * @param id       The unique category identifier (mandatory)
 * @param name     The display name of the category (mandatory)
 * @param showName Whether the category name is displayed on touch screen buttons
 */
public record CreateCategoryCommand(
        String id,
        String name,
        boolean showName
) {
    public CreateCategoryCommand {
        Objects.requireNonNull(id, "id must not be null");
        if (id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        id = id.trim();

        Objects.requireNonNull(name, "name must not be null");
        if (name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        name = name.trim();
    }

    /**
     * Factory method creating a category command with showName enabled by default.
     */
    public static CreateCategoryCommand of(String id, String name) {
        return new CreateCategoryCommand(id, name, true);
    }

    /**
     * Factory method creating a category command with explicit showName preference.
     */
    public static CreateCategoryCommand of(String id, String name, boolean showName) {
        return new CreateCategoryCommand(id, name, showName);
    }
}

