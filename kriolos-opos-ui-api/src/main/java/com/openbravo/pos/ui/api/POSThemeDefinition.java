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
package com.openbravo.pos.ui.api;

/**
 * Immutable metadata representing a discovered UI theme.
 * 
 * @param id   The unique identification token used for lookups (e.g., "spectrum-dark").
 * @param name The human-readable label shown in the POS backoffice/settings panel.
 * @param mode Informational mode tag (LIGHT or DARK) for the system context layout.
 */
public record POSThemeDefinition(String id, String name, ThemeMode mode) {
    
    public POSThemeDefinition {
        if (id == null || name == null || mode == null) {
            throw new IllegalArgumentException("Theme definition metadata tokens cannot be null.");
        }
    }
}
