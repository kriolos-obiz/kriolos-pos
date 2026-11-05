package com.openbravo.ui.icon;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.Icon;

/**
 * High-resolution, vector-based SVG icon implementation designed for modern POS touch interfaces.
 * Provides crisp geometric scaling across all HiDPI screens, theme awareness, dynamic coloring, and caching.
 *
 * @author KriolOS
 * @since 1.0.0
 */
public class SvgIcon implements Icon {

    private static final Map<String, FlatSVGIcon> CACHE = new ConcurrentHashMap<>();

    private final String resourcePath;
    private final FlatSVGIcon delegate;
    private final int width;
    private final int height;
    private final Color color;

    /**
     * Creates an SvgIcon loaded from the classpath with the specified target dimensions and optional color filter.
     *
     * @param resourcePath classpath path to the SVG asset (e.g., "com/openbravo/images/svg/user.svg")
     * @param width target width in pixels
     * @param height target height in pixels
     * @param color optional explicit color filter (null for ambient currentColor)
     */
    public SvgIcon(String resourcePath, int width, int height, Color color) {
        this.resourcePath = resourcePath;
        this.width = width;
        this.height = height;
        this.color = color;

        String key = resourcePath + "@" + width + "x" + height + (color != null ? "#" + Integer.toHexString(color.getRGB()) : "");
        this.delegate = CACHE.computeIfAbsent(key, k -> {
            String normalizedPath = resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath;
            FlatSVGIcon icon = new FlatSVGIcon(normalizedPath, width, height, SvgIcon.class.getClassLoader());
            if (color != null) {
                icon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> color));
            }
            return icon;
        });
    }

    /**
     * Creates an SvgIcon loaded from the classpath with ambient theming.
     *
     * @param resourcePath classpath path to the SVG asset
     * @param width target width in pixels
     * @param height target height in pixels
     */
    public SvgIcon(String resourcePath, int width, int height) {
        this(resourcePath, width, height, null);
    }

    /**
     * Creates a square SvgIcon loaded from the classpath with ambient theming.
     *
     * @param resourcePath classpath path to the SVG asset
     * @param size target width and height in pixels
     */
    public SvgIcon(String resourcePath, int size) {
        this(resourcePath, size, size, null);
    }

    /**
     * Creates a square SvgIcon loaded from the classpath with an explicit color filter.
     *
     * @param resourcePath classpath path to the SVG asset
     * @param size target width and height in pixels
     * @param color target icon color
     */
    public SvgIcon(String resourcePath, int size, Color color) {
        this(resourcePath, size, size, color);
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        if (delegate != null) {
            try {
                delegate.paintIcon(c, g, x, y);
            } catch (Throwable ignored) {
                // Defensive guard: prevent icon rendering exceptions from aborting Swing EDT repaint pipeline
            }
        }
    }

    @Override
    public int getIconWidth() {
        return width;
    }

    @Override
    public int getIconHeight() {
        return height;
    }

    public Color getColor() {
        return color;
    }

    public String getResourcePath() {
        return resourcePath;
    }

    /**
     * Derives a new SvgIcon with different dimensions while sharing the underlying vector asset.
     *
     * @param newWidth new target width
     * @param newHeight new target height
     * @return a resized SvgIcon instance
     */
    public SvgIcon derive(int newWidth, int newHeight) {
        return new SvgIcon(resourcePath, newWidth, newHeight, this.color);
    }

    /**
     * Derives a new square SvgIcon with different dimensions.
     *
     * @param newSize new target size
     * @return a resized square SvgIcon instance
     */
    public SvgIcon derive(int newSize) {
        return derive(newSize, newSize);
    }

    /**
     * Derives a new SvgIcon with a specific color filter while preserving dimensions.
     *
     * @param newColor new target color
     * @return a color-tinted SvgIcon instance
     */
    public SvgIcon withColor(Color newColor) {
        return new SvgIcon(resourcePath, width, height, newColor);
    }

    /**
     * Derives a new SvgIcon with specific dimensions and color.
     *
     * @param newWidth new target width
     * @param newHeight new target height
     * @param newColor new target color
     * @return a resized, color-tinted SvgIcon instance
     */
    public SvgIcon derive(int newWidth, int newHeight, Color newColor) {
        return new SvgIcon(resourcePath, newWidth, newHeight, newColor);
    }
}
