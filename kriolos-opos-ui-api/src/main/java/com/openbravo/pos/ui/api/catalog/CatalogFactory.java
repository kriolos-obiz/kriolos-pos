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

import java.util.Collection;

/**
 * Service Provider Interface (SPI) contract for Catalog provider discovery and instantiation.
 * <p>
 * Implementations are discovered via {@link java.util.ServiceLoader} and registered in
 * {@code META-INF/services/com.openbravo.pos.ui.api.catalog.CatalogFactory}.
 * </p>
 *
 * @author KriolOS Team
 */
public interface CatalogFactory {

    /**
     * Checks if this factory supports or supplies the target catalog ID.
     *
     * @param catalogId Unique catalog token (e.g. "classic", "modern").
     * @return {@code true} if supported, {@code false} otherwise.
     */
    boolean hasCatalog(String catalogId);

    /**
     * Returns metadata definitions for all catalogs provided by this factory.
     *
     * @return Collection of {@link CatalogDefinition} records.
     */
    Collection<CatalogDefinition> getAvailableCatalogs();

    /**
     * Instantiates the requested catalog component.
     *
     * @param catalogId Target catalog ID.
     * @param app       Application view context (e.g. {@code AppView}).
     * @return The constructed {@link CatalogSelector} instance.
     */
    CatalogSelector createCatalog(String catalogId, Object app);
}
