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

import java.util.Collection;
import javax.swing.JComponent;

/**
 * Service Provider Interface (SPI) contract for Sales Layout discovery and instantiation.
 * <p>
 * Implementations are discovered at runtime via {@link java.util.ServiceLoader} and registered
 * in {@code META-INF/services/com.openbravo.pos.ui.api.sales.SaleLayoutFactory}.
 * </p>
 *
 * @author KriolOS Team
 */
public interface SaleLayoutFactory {

    /**
     * Checks if this factory supports or supplies the target layout ID.
     *
     * @param layoutId The unique layout identifier token (e.g. "standard", "modern_two").
     * @return {@code true} if supported, {@code false} otherwise.
     */
    boolean hasLayout(String layoutId);

    /**
     * Returns metadata definitions for all layouts provided by this factory.
     *
     * @return Collection of immutable {@link SaleLayoutDefinition} records.
     */
    Collection<SaleLayoutDefinition> getAvailableLayouts();

    /**
     * Instantiates the requested layout component.
     *
     * @param layoutId    The target layout ID.
     * @param app         The active application context (e.g. {@code AppView}).
     * @param panelticket The active ticket editor coordinator (e.g. {@code TicketsEditor}).
     * @return The constructed {@link JComponent} layout instance.
     */
    JComponent createLayout(String layoutId, Object app, Object panelticket);

    /**
     * Indicates whether the specified layout is a self-contained full-screen interface.
     *
     * @param layoutId The layout ID.
     * @return {@code true} for modern/standalone full-view layouts; {@code false} for classic docked layouts.
     */
    default boolean isFullView(String layoutId) {
        return getAvailableLayouts().stream()
                .filter(def -> def.id().equalsIgnoreCase(layoutId))
                .findFirst()
                .map(SaleLayoutDefinition::fullView)
                .orElse(false);
    }

    /**
     * Indicates whether the specified layout manages restaurant table map workflows.
     *
     * @param layoutId The layout ID.
     * @return {@code true} if restaurant floor map layout, {@code false} otherwise.
     */
    default boolean isRestaurantLayout(String layoutId) {
        return getAvailableLayouts().stream()
                .filter(def -> def.id().equalsIgnoreCase(layoutId))
                .findFirst()
                .map(SaleLayoutDefinition::restaurant)
                .orElse(false);
    }
}
