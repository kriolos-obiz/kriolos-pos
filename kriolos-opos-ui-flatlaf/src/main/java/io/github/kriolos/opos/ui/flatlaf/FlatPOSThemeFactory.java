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
package io.github.kriolos.opos.ui.flatlaf;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.openbravo.pos.ui.api.POSThemeFactory;
import com.openbravo.pos.ui.api.POSThemeDefinition;
import com.openbravo.pos.ui.api.ThemeMode;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.FlatLaf;

import javax.swing.UIManager;
import java.util.Collection;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class FlatPOSThemeFactory implements POSThemeFactory {

    private static final Logger LOGGER = Logger.getLogger(FlatPOSThemeFactory.class.getName());
    private static final Map<String, POSThemeDefinition> THEME_REGISTRY = new LinkedHashMap<>();

    // Static registry defining the theme inventory without exposing or expanding class profiles directly
    static {
        THEME_REGISTRY.put("spectrum-dark",   new POSThemeDefinition("spectrum-dark",   "KriolOS Spectrum Darkest", ThemeMode.DARK));
        THEME_REGISTRY.put("spectrum-light",  new POSThemeDefinition("spectrum-light",  "KriolOS Spectrum Light",   ThemeMode.LIGHT));
        THEME_REGISTRY.put("flat-darcula",    new POSThemeDefinition("flat-darcula",    "FlatLaf Darcula Engine",   ThemeMode.DARK));
        THEME_REGISTRY.put("flat-intellij",   new POSThemeDefinition("flat-intellij",   "FlatLaf IntelliJ Theme",   ThemeMode.LIGHT));
    }

    @Override
    public boolean hasTheme(String themeId) {
        return themeId != null && THEME_REGISTRY.containsKey(themeId.toLowerCase());
    }

    @Override
    public Collection<POSThemeDefinition> getAvailableThemes() {
        return THEME_REGISTRY.values();
    }

    @Override
    public boolean applyTheme(String themeId) {
        if (!hasTheme(themeId)) return false;

        try {
            switch (themeId.toLowerCase()) {
                case "spectrum-dark" -> UIManager.setLookAndFeel(new SpectrumDarkVariant());
                case "spectrum-light" -> UIManager.setLookAndFeel(new SpectrumLightVariant());
                case "flat-darcula" -> FlatDarculaLaf.setup();
                case "flat-intellij" -> FlatIntelliJLaf.setup();
                default -> {
                    return false;
                }
            }
            // Trigger structural updates across Swing components UI tree mapping
            FlatLaf.updateUI();
            return true;
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "FlatPOSThemeFactory encountered an injection exception mapping theme: " + themeId, ex);
            return false;
        }
    }

    // Custom inner classes targeting properties binding assets accurately
    private static final class SpectrumLightVariant extends FlatLightLaf {
        @Override public String getName() { return "KriolOS Spectrum Light"; }
    }

    private static final class SpectrumDarkVariant extends FlatDarkLaf {
        @Override public String getName() { return "KriolOS Spectrum Darkest"; }
    }
}
