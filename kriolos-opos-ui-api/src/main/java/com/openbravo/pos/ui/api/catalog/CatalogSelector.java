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

import java.awt.Component;
import java.awt.event.ActionListener;

/**
 * Common component interface for product catalog selectors in POS screens.
 * <p>
 * Decouples sales and inventory panels from concrete catalog implementations,
 * allowing both Classic (button grid) and Modern (touch card) catalogs to be
 * plugged in transparently.
 * </p>
 *
 * @author KriolOS Team
 */
public interface CatalogSelector {

    /**
     * Loads categories and products into the catalog selector.
     *
     * @throws Exception if data loading fails.
     */
    void loadCatalog() throws Exception;

    /**
     * Navigates or focuses the catalog on a specific category or product identifier.
     *
     * @param id The category ID or null for root categories.
     */
    void showCatalogPanel(String id);

    /**
     * Enables or disables user interaction with catalog items.
     *
     * @param value {@code true} to enable, {@code false} to disable.
     */
    void setComponentEnabled(boolean value);

    /**
     * Returns the underlying Swing visual component representing this catalog.
     *
     * @return The Swing {@link Component}.
     */
    Component getComponent();

    /**
     * Registers a listener to be notified when a product is selected.
     *
     * @param l The action listener.
     */
    void addActionListener(ActionListener l);

    /**
     * Unregisters a product selection listener.
     *
     * @param l The action listener.
     */
    void removeActionListener(ActionListener l);
}
