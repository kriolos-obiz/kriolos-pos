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
package com.openbravo.pos.forms;

import com.openbravo.format.Formats;
import com.openbravo.pos.spi.localization.LocalizationFactory;
import com.openbravo.pos.spi.localization.LocalizationProvider;
import com.openbravo.pos.ui.api.POSThemeManager;
import java.awt.Font;
import java.awt.Insets;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.UIManager;

/**
 *
 * @author dev
 */
public class SystemSettings {

    private static final Logger LOGGER = Logger.getLogger(SystemSettings.class.getName());

    public static void applySystemProperties(AppConfig config) {
        // Set the look and feel.
        
        String newThemeProperty = config.getProperty("pos.ui.theme.id");
        applyPosTheme(newThemeProperty);

        applyGlobalTouchSettings();

        // Localization configuration switch
        String localizationMode = config.getProperty("localization.configure", "legacy");
        if ("provider".equalsIgnoreCase(localizationMode)) {
            applyLocalizationProvider(config);
        } else {
            applyLocalizationLegacy(config);
        }
    }

    public static void applyPosTheme(String themeId) {
        POSThemeManager.applyTheme(themeId);
    }

    /**
     * Injects touch-first layout variables directly into the Swing UIManager
     * framework. Upscales text, buttons, form controls, and icons to achieve
     * standard 48px hitboxes.
     */
    private static void applyGlobalTouchSettings() {
        // --- 1. Typography & Readability Scale ---
        // Enforces clean, crisp, enlarged typography for 8+ hour shifts
        Font touchFontBold = new Font("SansSerif", Font.BOLD, 16);
        Font touchFontPlain = new Font("SansSerif", Font.PLAIN, 16);

        UIManager.put("defaultFont", touchFontPlain);
        UIManager.put("Button.font", touchFontBold);
        UIManager.put("ToggleButton.font", touchFontBold);
        UIManager.put("Label.font", touchFontPlain);
        UIManager.put("List.font", touchFontPlain);
        UIManager.put("Table.font", touchFontPlain);
        UIManager.put("TableHeader.font", touchFontBold);
        UIManager.put("TextField.font", touchFontPlain);

        // --- 2. Button Hitbox Scaling (The 48px Touch Rule) ---
        // Adds thick internal padding [Top, Left, Bottom, Right] for finger selection
        UIManager.put("Button.margin", new Insets(12, 24, 12, 24));
        UIManager.put("ToggleButton.margin", new Insets(12, 24, 12, 24));

        // Hard fallback layout boundaries to ensure buttons never collapse
        UIManager.put("Button.minimumHeight", 48);
        UIManager.put("ToggleButton.minimumHeight", 48);

        // --- 3. Form & Input Component Enhancements ---
        // Expands search fields and manual numeric price fields
        UIManager.put("TextField.margin", new Insets(8, 12, 8, 12));
        UIManager.put("TextField.minimumHeight", 45);

        // --- 4. Selection Toggles & Vectors ---
        // Scales native tiny selection boxes so they can be easily checked on tablets
        UIManager.put("CheckBox.iconSize", 24);
        UIManager.put("RadioButton.iconSize", 24);

        // --- 5. Component Borders & Rounded Corners ---
        // Gives the app a clean, modern, slightly rounded visual aesthetic
        UIManager.put("Button.arc", 8);
        UIManager.put("Component.arc", 8);

        // --- 6. Thick Fallback Scrollbars ---
        // Ensures fallback scrollbars are thick enough to grab if kinetic touch is disabled
        UIManager.put("ScrollBar.width", 26);
        UIManager.put("ScrollBar.thumbArc", 12);
    }

    private static void applyLocalizationLegacy(AppConfig config) {

        LOGGER.log(Level.INFO, "Localization from legacy properties");

        //Set I18n or Language 
        String langTagLang = config.getProperty("user.language");
        String langTagCountry = config.getProperty("user.country");
        String langTagVariant = config.getProperty("user.variant");
        String langTagScript = config.getProperty("user.script");
        var localeBuilder = new Locale.Builder();

        if (!isNullOrBlank(langTagVariant)) {
            localeBuilder.setVariant(langTagVariant);
        }

        if (!isNullOrBlank(langTagScript)) {
            localeBuilder.setScript(langTagScript);
        }

        if (!isNullOrBlank(langTagCountry)) {
            localeBuilder.setRegion(langTagCountry);
        }

        if (!isNullOrBlank(langTagLang)) {
            localeBuilder.setLanguage(langTagLang);

            Locale.setDefault(localeBuilder.build());
        }

        LOGGER.log(Level.INFO, "Localization locale default: " + Locale.getDefault().toLanguageTag());

        //Set Format/Pattern for: Number, Date, Currency 
        Formats.setIntegerPattern(config.getProperty("format.integer"));
        Formats.setDoublePattern(config.getProperty("format.double"));
        Formats.setCurrencyPattern(config.getProperty("format.currency"));
        Formats.setPercentPattern(config.getProperty("format.percent"));
        Formats.setDatePattern(config.getProperty("format.date"));
        Formats.setTimePattern(config.getProperty("format.time"));
        Formats.setDateTimePattern(config.getProperty("format.datetime"));
    }

    private static void applyLocalizationProvider(AppConfig config) {

        LOGGER.log(Level.INFO, "Localization from SPI Provider");

        // Build target locale from "localization.locale" (ex: "pt-CV")
        Locale targetLocale = parseLocaleTag(config.getProperty("localization.locale"));
        try {

            Locale.setDefault(targetLocale);

            LocalizationProvider provider = LocalizationFactory.getInstance().getProvider(targetLocale);

            //Set Format/Pattern for: Number, Date, Currency 
            Formats.setIntegerFormatter(provider.getIntegerFormatter());
            Formats.setDoubleFormat(provider.getDoubleFormatter());
            Formats.setCurrencyFormat(provider.getCurrencyFormatter());
            Formats.setPercentFormatter(provider.getPercentFormatter());
            Formats.setDateFormat(provider.getDateFormatter());
            Formats.setTimeFormatter(provider.getTimeFormatter());
            Formats.setDateTimeFormatter(provider.getDateTimeFormatter());

            LOGGER.log(Level.INFO, "Localization provider applied: {0} for locale: {1}",
                    new Object[]{provider.getClass().getSimpleName(), targetLocale.toLanguageTag()});
        }
        catch (Exception e) {
            LOGGER.log(Level.WARNING, "SPI localization failed, falling back to JDK defaults", e);
        }
    }

    /**
     * Parses a BCP 47 / IETF language tag like "pt-CV" into a Locale. Returns
     * system default if null/empty.
     */
    private static Locale parseLocaleTag(String tag) {
        if (tag == null || tag.isBlank()) {
            return Locale.getDefault();
        }
        return Locale.forLanguageTag(tag);
    }

    private static boolean isNullOrBlank(String text) {
        return text == null || text.isBlank();
    }
}
