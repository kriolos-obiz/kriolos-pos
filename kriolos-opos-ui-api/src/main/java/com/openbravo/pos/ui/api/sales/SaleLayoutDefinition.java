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
package com.openbravo.pos.ui.api.sales;

/**
 * Immutable metadata representing a discovered Sales Layout.
 *
 * @param id          Unique token used for identification and persistence (e.g. "standard", "modern_two").
 * @param name        Human-readable display name shown in configuration settings (e.g. "Modern Two (Flex Touch)").
 * @param description Brief description of the layout workflow and features.
 * @param fullView    {@code true} if the layout manages its own complete screen (catalog, receipt, actions);
 *                    {@code false} if it docks into the classic ticket framework.
 * @param restaurant  {@code true} if this layout manages a table floor map workflow.
 *
 * @author KriolOS Team
 */
public record SaleLayoutDefinition(
        String id,
        String name,
        String description,
        boolean fullView,
        boolean restaurant) {

    public SaleLayoutDefinition {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Layout ID cannot be null or blank.");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Layout Name cannot be null or blank.");
        }
    }

    public SaleLayoutDefinition(String id, String name, String description, boolean fullView) {
        this(id, name, description, fullView, false);
    }
}
