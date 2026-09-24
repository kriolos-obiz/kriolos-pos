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

import java.util.Collection;

/**
 * Service Provider Interface (SPI) contract for multi-theme lookup and initialization.
 */
public interface POSThemeFactory {

    /**
     * Checks if this specific factory implementation can construct or supplies the target theme.
     */
    boolean hasTheme(String themeId);

    /**
     * Returns a collection of all theme metadata profiles compiled or dynamically supported by this factory.
     */
    Collection<POSThemeDefinition> getAvailableThemes();

    /**
     * Installs and activates the Look and Feel infrastructure directly into the Swing context.
     * 
     * @param themeId The strict ID representing the targeted theme profiles.
     * @return true if successfully injected and applied, false otherwise.
     */
    boolean applyTheme(String themeId);
}
