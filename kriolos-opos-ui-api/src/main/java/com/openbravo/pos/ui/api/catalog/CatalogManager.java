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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Global Service Registry aggregating all discovered {@link CatalogFactory} providers via Java SPI.
 * <p>
 * Follows the proven SPI pattern (like {@code SaleLayoutManager} and {@code POSThemeManager}):
 * <ul>
 *   <li>Dynamic provider lookup through {@link ServiceLoader}</li>
 *   <li>Thread-safe metadata retrieval for settings UI</li>
 *   <li>Automatic safety baseline fallback to classic catalog if a requested provider fails</li>
 * </ul>
 * </p>
 *
 * @author KriolOS Team
 */
public final class CatalogManager {

    private static final Logger LOGGER = Logger.getLogger(CatalogManager.class.getName());
    private static final ServiceLoader<CatalogFactory> FACTORY_LOADER =
            ServiceLoader.load(CatalogFactory.class);

    public static final String CLASSIC = "classic";
    public static final String MODERN = "modern";

    private CatalogManager() {
        throw new UnsupportedOperationException("CatalogManager cannot be instantiated.");
    }

    /**
     * Reloads all SPI providers from the classpath.
     */
    public static synchronized void reload() {
        FACTORY_LOADER.reload();
    }

    /**
     * Returns all discovered catalog factories.
     *
     * @return Collection of loaded {@link CatalogFactory} instances.
     */
    public static Collection<CatalogFactory> getAllFactories() {
        Collection<CatalogFactory> factories = new ArrayList<>();
        for (CatalogFactory factory : FACTORY_LOADER) {
            factories.add(factory);
        }
        return Collections.unmodifiableCollection(factories);
    }

    /**
     * Aggregates and returns all catalog definitions exposed by all loaded factories.
     *
     * @return An unmodifiable collection of available catalog definitions.
     */
    public static Collection<CatalogDefinition> getAllAvailableCatalogs() {
        Collection<CatalogDefinition> allCatalogs = new ArrayList<>();
        for (CatalogFactory factory : FACTORY_LOADER) {
            allCatalogs.addAll(factory.getAvailableCatalogs());
        }
        return Collections.unmodifiableCollection(allCatalogs);
    }

    /**
     * Searches for a catalog definition matching the ID.
     *
     * @param catalogId Unique catalog ID token.
     * @return Optional containing the definition if found.
     */
    public static Optional<CatalogDefinition> getCatalogDefinition(String catalogId) {
        if (catalogId == null || catalogId.isBlank()) {
            return Optional.empty();
        }
        return getAllAvailableCatalogs().stream()
                .filter(def -> def.id().equalsIgnoreCase(catalogId))
                .findFirst();
    }

    /**
     * Instantiates the requested catalog component using the appropriate discovered factory.
     * If the requested catalog ID is not fulfilled, a classic safety baseline is constructed.
     *
     * @param catalogId The configured catalog ID (e.g. "classic", "modern").
     * @param app       The active application context.
     * @return The constructed {@link CatalogSelector} instance.
     */
    public static CatalogSelector createCatalog(String catalogId, Object app) {
        if (catalogId != null && !catalogId.isBlank()) {
            for (CatalogFactory factory : FACTORY_LOADER) {
                if (factory.hasCatalog(catalogId)) {
                    try {
                        CatalogSelector catalog = factory.createCatalog(catalogId, app);
                        if (catalog != null) {
                            LOGGER.log(Level.INFO, "Successfully initialized catalog: {0}", catalogId);
                            return catalog;
                        }
                    } catch (Exception ex) {
                        LOGGER.log(Level.SEVERE, "Error creating catalog '" + catalogId + "'", ex);
                    }
                }
            }
        }

        LOGGER.log(Level.WARNING, "Requested catalog ''{0}'' not fulfilled by any factory provider. Forcing classic baseline.", catalogId);
        return applyFallback(app);
    }

    /**
     * Creates a catalog selector using the default classic catalog baseline.
     *
     * @param app The active application context.
     * @return The default {@link CatalogSelector} instance.
     */
    public static CatalogSelector createDefaultCatalog(Object app) {
        return createCatalog(CLASSIC, app);
    }

    private static CatalogSelector applyFallback(Object app) {
        for (CatalogFactory factory : FACTORY_LOADER) {
            if (factory.hasCatalog(CLASSIC)) {
                try {
                    CatalogSelector catalog = factory.createCatalog(CLASSIC, app);
                    if (catalog != null) {
                        LOGGER.log(Level.INFO, "Safety fallback applied successfully via classic catalog.");
                        return catalog;
                    }
                } catch (Exception ex) {
                    LOGGER.log(Level.WARNING, "Fallback classic catalog creation failed", ex);
                }
            }
        }

        // Ultimate fallback: first available catalog from any factory
        for (CatalogFactory factory : FACTORY_LOADER) {
            for (CatalogDefinition def : factory.getAvailableCatalogs()) {
                try {
                    CatalogSelector catalog = factory.createCatalog(def.id(), app);
                    if (catalog != null) {
                        LOGGER.log(Level.INFO, "Ultimate fallback applied via catalog: {0}", def.id());
                        return catalog;
                    }
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, "Failed ultimate catalog fallback on: " + def.id(), ex);
                }
            }
        }

        throw new IllegalStateException("CRITICAL: No catalog provider could be constructed by the available extensions.");
    }
}
