/*
 * Copyright (C) 2025 KriolOS
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
package com.openbravo.pos.spi.provider;

/**
 * Configuration property metadata.  
 * 
 * Used to render generic configuration Panel for POS extensions in the Configuration Panel.
 *
 * @author poolborges
 */
public class ProviderConfigProperty {
    
    /**
     * Boolean Value
     */
    public static final String BOOLEAN_TYPE="BOOLEAN";

    /**
     * Integral Value
     */
    public static final String INTEGER_TYPE="INTEGER";

    /**
     * Number Value, e.g. integral, floating-point.
     */
    public static final String NUMBER_TYPE="NUMBER";

    /**
     * String Value or Single line text
     */
    public static final String STRING_TYPE="STRING";

    /**
     * Multi Line String values
     */
    public static final String MULTIVALUED_STRING_TYPE="MULTILINESTRING";

    /**
     * Script content
     */
    public static final String SCRIPT_TYPE="SCRIPT";
    
    /**
     * File value. Or a Path to a file
     */
    public static final String FILE_TYPE="FILE";
    
    /**
     * Resource
     */
    public static final String RESOURCE_TYPE="RESOURCE";
    
    /**
     * Secret Value, such password, token or any security 
     */
    public static final String SECRET_TYPE="SECRET";
    
}
