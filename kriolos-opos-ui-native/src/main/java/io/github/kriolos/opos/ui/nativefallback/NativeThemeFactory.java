package io.github.kriolos.opos.ui.nativefallback;

import com.openbravo.pos.ui.api.POSThemeDefinition;
import com.openbravo.pos.ui.api.POSThemeFactory;
import com.openbravo.pos.ui.api.ThemeMode;

import java.awt.Window;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Fallback Mock Theme Provider for KriolOS POS. Resolves to the host Operating
 * System's default look and feel. Used for testing SPI isolation or as an
 * emergency runtime fallback environment.
 */
public final class NativeThemeFactory implements POSThemeFactory {

    private static final Logger LOGGER = Logger.getLogger(NativeThemeFactory.class.getName());
    
    // Explicit theme identification tokens mapped to the factory instance
    private static final String NATIVE_THEME_ID = "system";
    private static final String NATIVE_THEME_NAME = "Host Operating System Native Look and Feel";

    // Static registry cache map for all discovered system Look and Feel engines
    private static final Map<String, POSThemeDefinition> SYS_MAP = new HashMap<>();

    static {
        // 1. Always inject the absolute dynamic system fallback token
        SYS_MAP.put(NATIVE_THEME_ID, new POSThemeDefinition(NATIVE_THEME_ID, NATIVE_THEME_NAME, ThemeMode.LIGHT));

        // 2. Safely scan the current platform runtime environment and map installed LAFs
        for (UIManager.LookAndFeelInfo lafInfo : UIManager.getInstalledLookAndFeels()) {
            String className = lafInfo.getClassName();
            String id = generateThemeIdFromClassName(className);
            
            if (id != null) {
                String formattedName = "System Theme (" + lafInfo.getName() + ")";
                SYS_MAP.put(id, new POSThemeDefinition(id, formattedName, ThemeMode.LIGHT));
            }
        }
    }

    /**
     * Internal lookup helper to translate long package class names into clean tokens.
     */
    private static String generateThemeIdFromClassName(String className) {
        if (className == null) return null;
        
        // Specific checks first to prevent general substring mismatching
        if (className.contains("WindowsClassicLookAndFeel")) return "system-windows-classic";
        if (className.contains("WindowsLookAndFeel")) return "system-windows";
        
        // Standard cross-platform configurations
        if (className.contains("MetalLookAndFeel")) return "system-metal";
        if (className.contains("NimbusLookAndFeel")) return "system-nimbus";
        if (className.contains("MotifLookAndFeel")) return "system-motif";
        if (className.contains("GTKLookAndFeel")) return "system-gtk";
        if (className.contains("AquaLookAndFeel")) return "system-aqua";
        return null;
    }

    /**
     * Internal lookup helper to resolve clean tokens back to concrete class names.
     */
    private static String resolveClassNameFromThemeId(String themeId) {
        if (NATIVE_THEME_ID.equalsIgnoreCase(themeId)) {
            return UIManager.getSystemLookAndFeelClassName();
        }
        for (UIManager.LookAndFeelInfo lafInfo : UIManager.getInstalledLookAndFeels()) {
            String id = generateThemeIdFromClassName(lafInfo.getClassName());
            if (themeId.equalsIgnoreCase(id)) {
                return lafInfo.getClassName();
            }
        }
        return null;
    }


    public String getId() {
        return "native";
    }
    
    public String getName() {
        return "Native Operating System Themes Provider";
    }

    @Override
    public boolean hasTheme(String themeId) {
        return themeId != null && SYS_MAP.containsKey(themeId.toLowerCase());
    }

    @Override
    public Collection<POSThemeDefinition> getAvailableThemes() {
        // Exposes an unmodifiable collection of all successfully mapped definitions
        return Collections.unmodifiableCollection(SYS_MAP.values());
    }

    @Override
    public boolean applyTheme(String themeId) {
        if (!hasTheme(themeId)) {
            LOGGER.log(Level.WARNING, "Requested theme ID ''{0}'' is not managed by this native factory.", themeId);
            return false;
        }

        try {
            // Resolves the correct target class name token dynamically from the map
            String targetLafClassName = resolveClassNameFromThemeId(themeId);

            if (targetLafClassName == null) {
                LOGGER.log(Level.WARNING, "Theme ID ''{0}'' mapped to key registry but class implementation is missing in this runtime environment.", themeId);
                return false;
            }

            // Safe fallback configuration handling for specific platform boundaries
            if (targetLafClassName.contains("Metal")) {
                LOGGER.log(Level.INFO, "Native Light/Dark differentiation not supported natively by Metal fallback environments.");
            }

            // Apply the resolved operating system class into Swing's rendering layer
            UIManager.setLookAndFeel(targetLafClassName);

            // Updates UI context safely across active desktop window structures on the EDT thread
            SwingUtilities.invokeLater(() -> {
                for (Window window : Window.getWindows()) {
                    SwingUtilities.updateComponentTreeUI(window);
                }
            });

            LOGGER.log(Level.INFO, "System UI environment successfully mapped to concrete LAF: {0}", targetLafClassName);
            return true;

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "CRITICAL SPI ERROR: Failed to inject requested theme environment fallback.", ex);
            return false;
        }
    }
}
