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
package com.openbravo.pos.catalog;

import com.openbravo.pos.ui.api.catalog.CatalogDefinition;
import com.openbravo.pos.ui.api.catalog.CatalogManager;
import java.util.Collection;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CatalogManagerTest {

    @Test
    @DisplayName("Should discover registered catalog SPI providers")
    void testDiscoveredProviders() {
        Collection<CatalogDefinition> catalogs = CatalogManager.getAllAvailableCatalogs();
        assertNotNull(catalogs, "Catalogs collection should not be null");
        assertFalse(catalogs.isEmpty(), "At least one Catalog provider must be discovered on classpath");

        boolean hasClassic = catalogs.stream().anyMatch(c -> CatalogManager.CLASSIC.equalsIgnoreCase(c.id()));
        boolean hasModern = catalogs.stream().anyMatch(c -> CatalogManager.MODERN.equalsIgnoreCase(c.id()));

        assertTrue(hasClassic, "Classic Catalog provider must be registered and discovered");
        assertTrue(hasModern, "Modern Touch Catalog provider must be registered and discovered");
    }

    @Test
    @DisplayName("Should resolve catalog metadata by ID case-insensitively")
    void testGetCatalogDefinition() {
        Optional<CatalogDefinition> classicOpt = CatalogManager.getCatalogDefinition("CLASSIC");
        assertTrue(classicOpt.isPresent(), "CLASSIC catalog should be resolved");
        assertEquals("Classic Catalog", classicOpt.get().name());

        Optional<CatalogDefinition> modernOpt = CatalogManager.getCatalogDefinition("modern");
        assertTrue(modernOpt.isPresent(), "modern catalog should be resolved");
        assertEquals("Modern Touch Catalog", modernOpt.get().name());

        Optional<CatalogDefinition> invalidOpt = CatalogManager.getCatalogDefinition("non_existent_catalog");
        assertTrue(invalidOpt.isEmpty(), "Non-existent catalog must return empty Optional");
    }
}
