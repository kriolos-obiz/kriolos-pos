/*
 * Copyright (C) 2026 KriolOS
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
package com.openbravo.pos.ui.api.catalog;

/**
 * Immutable metadata record describing an available catalog component provider.
 *
 * @param id          Unique identifier token (e.g. "classic", "modern").
 * @param name        Human-readable display name.
 * @param description Brief description of visual style and ergonomics.
 *
 * @author KriolOS Team
 */
public record CatalogDefinition(
        String id,
        String name,
        String description
) {

    public CatalogDefinition {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Catalog ID cannot be null or blank");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Catalog Name cannot be null or blank");
        }
    }
}
