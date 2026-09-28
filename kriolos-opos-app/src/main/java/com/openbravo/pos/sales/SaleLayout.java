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
package com.openbravo.pos.sales;

import java.util.List;

/**
 *
 * @author dev
 */
public class SaleLayout {
    
    public static final String SIMPLE = "simple";
    public static final String STANDARD = "standard";
    public static final String RESTAURANT = "restaurant";
    public static final String MODERN_ONE = "modern_one";
    public static final String MODERN_TWO = "modern_two";
    
    private static final List<String> ALL = List.of(SIMPLE, STANDARD, RESTAURANT, MODERN_ONE, MODERN_TWO);
    
    public static List<String> getAll(){
        return ALL;
    }
    
}
