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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Global Service Registry that aggregates all discovered POSThemeFactory providers at runtime.
 */
public final class POSThemeManager {

    private static final Logger LOGGER = Logger.getLogger(POSThemeManager.class.getName());
    private static final ServiceLoader<POSThemeFactory> FACTORY_LOADER = ServiceLoader.load(POSThemeFactory.class);

    private POSThemeManager() {
        throw new UnsupportedOperationException("POSThemeManager cannot be instantiated.");
    }
    
    /**
     * Retrieves all discovered POSThemeFactory implementations loaded via ServiceLoader.
     * 
     * @return An unmodifiable collection containing all loaded theme factories.
     */
    public static Collection<POSThemeFactory> getAllPOSThemeFactory() {
        Collection<POSThemeFactory> allThemes = new ArrayList<>();
        
        // Corrected from addAll(factory) to add(factory) since it's a single instance
        for (POSThemeFactory factory : FACTORY_LOADER) {
            allThemes.add(factory);
        }
        
        return Collections.unmodifiableCollection(allThemes);
    }
    /**
     * Aggregates and returns all theme definitions exposed by all loaded factories.
     * Perfect for populating the UI Settings combo box dynamically.
     */
    public static Collection<POSThemeDefinition> getAllAvailableThemes() {
        Collection<POSThemeDefinition> allThemes = new ArrayList<>();
        for (POSThemeFactory factory : FACTORY_LOADER) {
            allThemes.addAll(factory.getAvailableThemes());
        }
        return Collections.unmodifiableCollection(allThemes);
    }

    /**
     * Searches across all registered providers to find a matching metadata profile.
     */
    public static Optional<POSThemeDefinition> getThemeDefinition(String themeId) {
        return getAllAvailableThemes().stream()
                .filter(def -> def.id().equalsIgnoreCase(themeId))
                .findFirst();
    }

    /**
     * Executes the look and feel injection across runtime system components.
     */
    public static void applyTheme(String themeId) {
        for (POSThemeFactory factory : FACTORY_LOADER) {
            if (factory.hasTheme(themeId)) {
                if (factory.applyTheme(themeId)) {
                    LOGGER.log(Level.INFO, "Successfully initialized theme context configuration: {0}", themeId);
                    return;
                }
            }
        }

        LOGGER.log(Level.WARNING, "Requested theme ID ''{0}'' not fulfilled by any factory provider. Forcing safety baseline.", themeId);
        applyFallback();
    }

    private static void applyFallback() {
        for (POSThemeFactory factory : FACTORY_LOADER) {
            Optional<POSThemeDefinition> firstAvailable = factory.getAvailableThemes().stream().findFirst();
            if (firstAvailable.isPresent() && factory.applyTheme(firstAvailable.get().id())) {
                LOGGER.log(Level.INFO, "Safety fallback applied successfully via theme: {0}", firstAvailable.get().id());
                return;
            }
        }
        LOGGER.log(Level.SEVERE, "CRITICAL: No themes or fallbacks could be constructed by the available extensions.");
    }
}
