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

import java.util.List;

/**
 * Standard Sales Layout identifier constants.
 *
 * @author KriolOS Team
 */
public class SaleLayout {

    public static final String SIMPLE = "simple";
    public static final String STANDARD = "standard";
    public static final String RESTAURANT = "restaurant";
    public static final String MODERN_ONE = "modern_one";
    public static final String MODERN_TWO = "modern_two";

    private static final List<String> STATIC_ALL = List.of(SIMPLE, STANDARD, RESTAURANT, MODERN_ONE, MODERN_TWO);

    /**
     * Returns all dynamically available layout IDs from the SPI registry.
     *
     * @return List of layout IDs.
     */
    public static List<String> getAll() {
        List<String> dynamicList = SaleLayoutManager.getAllAvailableLayouts()
                .stream()
                .map(SaleLayoutDefinition::id)
                .toList();
        return dynamicList.isEmpty() ? STATIC_ALL : dynamicList;
    }
}
