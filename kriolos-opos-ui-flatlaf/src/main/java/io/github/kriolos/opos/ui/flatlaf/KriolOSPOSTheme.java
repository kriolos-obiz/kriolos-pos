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

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.FlatLaf;
import javax.swing.UIManager;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * UI/UX Design System Guideline for KriolOS POS.
 * Heavily inspired by Adobe Spectrum density, platform scales, and visual clarity.
 * 
 * This class follows the Utility Class design pattern with a private constructor.
 * It uses Java Util Logging (JUL) to manage system diagnostic outputs safely.
 */
public final class KriolOSPOSTheme {

    // Instantiating the Java Util Logger for this specific class
    private static final Logger LOGGER = Logger.getLogger(KriolOSPOSTheme.class.getName());

    /**
     * Private constructor to enforce the Utility Class pattern.
     * Prevents instantiation of the orchestrator class within the Openbravo architecture.
     * 
     * @throws UnsupportedOperationException If instantiation is attempted.
     */
    private KriolOSPOSTheme() {
        throw new UnsupportedOperationException("KriolOSPOSTheme is a utility configuration class and cannot be instantiated.");
    }

    /**
     * Light Theme Variant (Adobe Spectrum Light adaptation)
     */
    public static final class Light extends FlatLightLaf {
        
        /**
         * Sets up the custom light look and feel.
         * 
         * @return true if successful, false otherwise.
         */
        public static boolean setup() {
            return setup(new Light());
        }

        @Override
        public String getName() {
            return "KriolOS POS Spectrum Light";
        }
    }

    /**
     * Dark Theme Variant (Adobe Spectrum Darkest adaptation)
     */
    public static final class Dark extends FlatDarkLaf {

        /**
         * Sets up the custom dark look and feel.
         * 
         * @return true if successful, false otherwise.
         */
        public static boolean setup() {
            return setup(new Dark());
        }

        @Override
        public String getName() {
            return "KriolOS POS Spectrum Dark";
        }
    }

    /**
     * Dynamically switches the active look and feel across all active components 
     * in the KriolOS POS system without requiring a software restart.
     * 
     * @param useDarkMode true to apply the Dark theme, false to apply the Light theme.
     */
    public static void switchTheme(boolean useDarkMode) {
        try {
            if (useDarkMode) {
                UIManager.setLookAndFeel(new KriolOSPOSTheme.Dark());
                LOGGER.log(Level.INFO, "KriolOS POS theme successfully switched to DARK mode.");
            } else {
                UIManager.setLookAndFeel(new KriolOSPOSTheme.Light());
                LOGGER.log(Level.INFO, "KriolOS POS theme successfully switched to LIGHT mode.");
            }
            // Notifies all open Java Swing panels to re-render in real-time
            FlatLaf.updateUI();
        } catch (Exception ex) {
            // Using JUL to log the severe exception with the stack trace instead of standard print
            LOGGER.log(Level.SEVERE, "CRITICAL: Failed to switch KriolOS POS theme environment.", ex);
        }
    }
}
