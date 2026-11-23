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

import java.util.Objects;

/**
 * Immutable command to create a product tax category.
 *
 * @param id unique identifier of the tax category
 * @param name display name of the tax category
 * @author KriolOS
 */
public record CreateTaxCategoryCommand(String id, String name) {

    public CreateTaxCategoryCommand {
        Objects.requireNonNull(id, "id must not be null");
        if (id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        Objects.requireNonNull(name, "name must not be null");
        if (name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        id = id.trim();
        name = name.trim();
    }

    public static CreateTaxCategoryCommand of(String id, String name) {
        return new CreateTaxCategoryCommand(id, name);
    }
}

