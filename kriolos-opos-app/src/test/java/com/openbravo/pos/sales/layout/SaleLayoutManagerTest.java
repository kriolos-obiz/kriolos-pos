//    KriolOS POS
//    Copyright (c) 2019-2026 KriolOS
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.

package com.openbravo.pos.sales.layout;

import com.openbravo.pos.ui.api.sales.SaleLayoutDefinition;
import com.openbravo.pos.ui.api.sales.SaleLayoutManager;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SaleLayoutManager SPI Discovery & Management Test")
public class SaleLayoutManagerTest {

    @Test
    @DisplayName("Should discover all registered SPI providers and core + modern layouts")
    void testDiscoverAllLayouts() {
        Collection<SaleLayoutDefinition> layouts = SaleLayoutManager.getAllAvailableLayouts();
        assertNotNull(layouts);
        assertFalse(layouts.isEmpty(), "SaleLayoutManager should discover at least one layout factory");

        Set<String> layoutIds = layouts.stream()
                .map(SaleLayoutDefinition::id)
                .collect(Collectors.toSet());

        assertTrue(layoutIds.contains("simple"), "Missing 'simple' layout");
        assertTrue(layoutIds.contains("standard"), "Missing 'standard' layout");
        assertTrue(layoutIds.contains("restaurant"), "Missing 'restaurant' layout");
        assertTrue(layoutIds.contains("modern_one"), "Missing 'modern_one' layout");
        assertTrue(layoutIds.contains("modern_two"), "Missing 'modern_two' layout");
    }

    @Test
    @DisplayName("Should correctly identify full-view layouts")
    void testIsFullView() {
        assertTrue(SaleLayoutManager.isFullView("modern_one"), "modern_one must be full-view");
        assertTrue(SaleLayoutManager.isFullView("modern_two"), "modern_two must be full-view");
        assertFalse(SaleLayoutManager.isFullView("standard"), "standard must not be full-view");
        assertFalse(SaleLayoutManager.isFullView("simple"), "simple must not be full-view");
        assertFalse(SaleLayoutManager.isFullView("restaurant"), "restaurant must not be full-view");
        assertFalse(SaleLayoutManager.isFullView("unknown_xyz"), "unknown must not be full-view");
    }

    @Test
    @DisplayName("Should correctly identify restaurant layout")
    void testIsRestaurant() {
        assertTrue(SaleLayoutManager.isRestaurant("restaurant"), "restaurant must be recognized");
        assertFalse(SaleLayoutManager.isRestaurant("standard"), "standard must not be restaurant");
        assertFalse(SaleLayoutManager.isRestaurant("modern_two"), "modern_two must not be restaurant");
        assertFalse(SaleLayoutManager.isRestaurant(null), "null must not be restaurant");
    }

    @Test
    @DisplayName("Should retrieve layout metadata definition by ID")
    void testGetLayoutDefinition() {
        Optional<SaleLayoutDefinition> opt = SaleLayoutManager.getLayoutDefinition("modern_two");
        assertTrue(opt.isPresent(), "modern_two definition must exist");
        assertEquals("Modern Two (Flex Touch)", opt.get().name());
        assertTrue(opt.get().fullView());
        assertFalse(opt.get().restaurant());
    }

    @Test
    @DisplayName("Should return empty optional for unknown layout ID")
    void testGetUnknownLayoutDefinition() {
        Optional<SaleLayoutDefinition> opt = SaleLayoutManager.getLayoutDefinition("non_existent_layout_xyz");
        assertTrue(opt.isEmpty());
    }
}
