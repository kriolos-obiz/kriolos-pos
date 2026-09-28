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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;

/**
 * Global Service Registry aggregating all discovered {@link SaleLayoutFactory}
 * providers via Java SPI.
 * <p>
 * Follows the proven {@code POSThemeManager} design pattern:
 * <ul>
 *   <li>Dynamic provider lookup through {@link ServiceLoader}</li>
 *   <li>Thread-safe metadata retrieval for settings UI combo boxes</li>
 *   <li>Resilient safety fallback if a configured layout provider is absent</li>
 * </ul>
 * </p>
 *
 * @author KriolOS Team
 */
public final class SaleLayoutManager {

    private static final Logger LOGGER = Logger.getLogger(SaleLayoutManager.class.getName());
    private static final ServiceLoader<SaleLayoutFactory> FACTORY_LOADER =
            ServiceLoader.load(SaleLayoutFactory.class);

    private SaleLayoutManager() {
        throw new UnsupportedOperationException("SaleLayoutManager cannot be instantiated.");
    }

    /**
     * Reloads all SPI providers from the classpath.
     */
    public static synchronized void reload() {
        FACTORY_LOADER.reload();
    }

    /**
     * Retrieves all discovered {@link SaleLayoutFactory} providers.
     *
     * @return An unmodifiable collection of all loaded factories.
     */
    public static Collection<SaleLayoutFactory> getAllFactories() {
        Collection<SaleLayoutFactory> factories = new ArrayList<>();
        for (SaleLayoutFactory factory : FACTORY_LOADER) {
            factories.add(factory);
        }
        return Collections.unmodifiableCollection(factories);
    }

    /**
     * Aggregates and returns all layout definitions exposed by all loaded factories.
     * Perfect for populating the Configuration > General layout combo box dynamically.
     *
     * @return An unmodifiable collection of available layout definitions.
     */
    public static Collection<SaleLayoutDefinition> getAllAvailableLayouts() {
        Collection<SaleLayoutDefinition> allLayouts = new ArrayList<>();
        for (SaleLayoutFactory factory : FACTORY_LOADER) {
            allLayouts.addAll(factory.getAvailableLayouts());
        }
        return Collections.unmodifiableCollection(allLayouts);
    }

    /**
     * Searches across all registered providers to find a matching metadata profile.
     *
     * @param layoutId The layout ID token.
     * @return Optional containing the definition if found, or empty if not found.
     */
    public static Optional<SaleLayoutDefinition> getLayoutDefinition(String layoutId) {
        if (layoutId == null || layoutId.isBlank()) {
            return Optional.empty();
        }
        return getAllAvailableLayouts().stream()
                .filter(def -> def.id().equalsIgnoreCase(layoutId))
                .findFirst();
    }

    /**
     * Instantiates the requested sales layout component using the appropriate discovered factory.
     * If the requested layout ID is not fulfilled, a safety fallback layout is automatically created.
     *
     * @param layoutId    The configured layout ID (e.g. "standard", "modern_two").
     * @param app         The active application context.
     * @param panelticket The active ticket editor coordinator.
     * @return The constructed {@link JComponent} instance.
     */
    public static JComponent createLayout(String layoutId, Object app, Object panelticket) {
        if (layoutId != null && !layoutId.isBlank()) {
            for (SaleLayoutFactory factory : FACTORY_LOADER) {
                if (factory.hasLayout(layoutId)) {
                    try {
                        JComponent comp = factory.createLayout(layoutId, app, panelticket);
                        if (comp != null) {
                            LOGGER.log(Level.INFO, "Successfully initialized sales layout: {0}", layoutId);
                            return comp;
                        }
                    } catch (Exception ex) {
                        LOGGER.log(Level.SEVERE, "Error creating sales layout '" + layoutId + "'", ex);
                    }
                }
            }
        }

        LOGGER.log(Level.WARNING, "Requested sales layout ''{0}'' not fulfilled by any factory provider. Forcing safety baseline.", layoutId);
        return applyFallback(app, panelticket);
    }

    /**
     * Checks if the layout is a standalone full-screen view (e.g. Modern One, Modern Two).
     *
     * @param layoutId The layout ID.
     * @return {@code true} if full-view, {@code false} if legacy docked.
     */
    public static boolean isFullView(String layoutId) {
        if (layoutId == null || layoutId.isBlank()) {
            return false;
        }
        for (SaleLayoutFactory factory : FACTORY_LOADER) {
            if (factory.hasLayout(layoutId)) {
                return factory.isFullView(layoutId);
            }
        }
        return false;
    }

    /**
     * Checks if the layout manages a restaurant floor map.
     *
     * @param layoutId The layout ID.
     * @return {@code true} if restaurant floor map layout, {@code false} otherwise.
     */
    public static boolean isRestaurant(String layoutId) {
        if (layoutId == null || layoutId.isBlank()) {
            return false;
        }
        for (SaleLayoutFactory factory : FACTORY_LOADER) {
            if (factory.hasLayout(layoutId)) {
                return factory.isRestaurantLayout(layoutId);
            }
        }
        return false;
    }

    /**
     * Safety baseline fallback: attempts "standard", then "simple", then first available provider.
     */
    private static JComponent applyFallback(Object app, Object panelticket) {
        for (String baseline : List.of("standard", "simple")) {
            for (SaleLayoutFactory factory : FACTORY_LOADER) {
                if (factory.hasLayout(baseline)) {
                    try {
                        JComponent comp = factory.createLayout(baseline, app, panelticket);
                        if (comp != null) {
                            LOGGER.log(Level.INFO, "Safety fallback applied successfully via layout: {0}", baseline);
                            return comp;
                        }
                    } catch (Exception ex) {
                        LOGGER.log(Level.WARNING, "Fallback layout creation failed for: " + baseline, ex);
                    }
                }
            }
        }

        // Ultimate fallback: first available layout from any factory
        for (SaleLayoutFactory factory : FACTORY_LOADER) {
            for (SaleLayoutDefinition def : factory.getAvailableLayouts()) {
                try {
                    JComponent comp = factory.createLayout(def.id(), app, panelticket);
                    if (comp != null) {
                        LOGGER.log(Level.INFO, "Ultimate fallback applied via layout: {0}", def.id());
                        return comp;
                    }
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, "Failed ultimate fallback on: " + def.id(), ex);
                }
            }
        }

        throw new IllegalStateException("CRITICAL: No sales layout provider could be constructed by the available extensions.");
    }
}
