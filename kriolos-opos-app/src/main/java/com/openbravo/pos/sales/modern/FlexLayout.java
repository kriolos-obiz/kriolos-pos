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
package com.openbravo.pos.sales.modern;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A layout manager for Java Swing that replicates the core mechanics of the CSS
 * Flexbox specification. 
 *
 * @author OpenBravo UI Architecture Team
 * @see LayoutManager2
 * @see GridBagLayout
 */
public class FlexLayout implements LayoutManager2 {

    private static final Logger LOG = Logger.getLogger(FlexLayout.class.getName());

    public enum Direction { ROW, COLUMN }
    public enum JustifyContent { START, END, CENTER }
    public enum Wrap { WRAP, NO_WRAP }
    public enum AlignItems { START, END, CENTER, STRETCH }

    private final Direction direction;
    private final JustifyContent justifyContent;
    private final Wrap wrap;
    private final AlignItems alignItems;
    private final int gap;

    private final GridBagLayout gridBagEngine;
    private final Map<Component, Double> percentBasisMap = new HashMap<>();

    public FlexLayout(Direction direction, JustifyContent justifyContent, Wrap wrap, AlignItems alignItems, int gap) {
        this.direction = direction;
        this.justifyContent = justifyContent;
        this.wrap = wrap;
        this.alignItems = alignItems;
        this.gap = gap;
        this.gridBagEngine = (wrap == Wrap.NO_WRAP) ? new GridBagLayout() : null;
    }

    public static class Builder {
        private Direction direction = Direction.ROW;
        private JustifyContent justifyContent = JustifyContent.START;
        private Wrap wrap = Wrap.NO_WRAP;
        private AlignItems alignItems = AlignItems.START;
        private int gap = 0;

        public Builder direction(Direction direction) { this.direction = direction; return this; }
        public Builder justifyContent(JustifyContent justify) { this.justifyContent = justify; return this; }
        public Builder wrap(Wrap wrap) { this.wrap = wrap; return this; }
        public Builder alignItems(AlignItems align) { this.alignItems = align; return this; }
        public Builder gap(int gap) { this.gap = gap; return this; }
        public FlexLayout build() { return new FlexLayout(direction, justifyContent, wrap, alignItems, gap); }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class FlexConstraints {
        public double grow = 0.0;
        public int basis = -1;
        public double percentBasis = -1.0;

        public FlexConstraints(double grow) { this.grow = grow; }
        public FlexConstraints(double grow, int basis) { this.grow = grow; this.basis = basis; }
        public FlexConstraints(double grow, double percentBasis) { this.grow = grow; this.percentBasis = percentBasis; }
    }

    @Override
    public void addLayoutComponent(Component comp, Object constraints) {
        double flexGrow = 0.0;
        int flexBasis = -1;
        double flexPercentBasis = -1.0;

        if (constraints instanceof String) {
            String str = ((String) constraints).trim();
            if (str.contains(":") || str.contains(";")) {
                String[] parts = str.split(";");
                for (String part : parts) {
                    String[] kv = part.split(":");
                    if (kv.length == 2) {
                        String key = kv[0].trim().toLowerCase();
                        String value = kv[1].trim();
                        try {
                            if ("grow".equals(key)) flexGrow = Double.parseDouble(value);
                            if ("basis".equals(key)) {
                                if (value.endsWith("%")) {
                                    flexPercentBasis = Double.parseDouble(value.replace("%", "").trim()) / 100.0;
                                } else {
                                    flexBasis = Integer.parseInt(value);
                                }
                            }
                        } catch (NumberFormatException e) {
                            // [FIX Issue #3]: Prevent silent swallowing of layout parsing errors
                            LOG.log(Level.WARNING, "FlexLayout syntax error parsing: ''{0}'' on component ''{1}''. Exception: {2}", 
                                    new Object[]{part, comp.getName(), e.getMessage()});
                        }
                    }
                }
            } else {
                try { flexGrow = Double.parseDouble(str); } catch (NumberFormatException ignored) {}
            }
        } else if (constraints instanceof FlexConstraints) {
            FlexConstraints fc = (FlexConstraints) constraints;
            flexGrow = fc.grow;
            flexBasis = fc.basis;
            flexPercentBasis = fc.percentBasis;
        } else if (constraints instanceof Double) {
            flexGrow = (Double) constraints;
        } else if (constraints instanceof Integer) {
            flexGrow = ((Integer) constraints).doubleValue();
        }

        if (flexPercentBasis >= 0.0) {
            percentBasisMap.put(comp, flexPercentBasis);
        } else if (flexBasis >= 0) {
            Dimension pref = comp.getPreferredSize();
            if (direction == Direction.ROW) {
                comp.setPreferredSize(new Dimension(flexBasis, pref.height));
            } else {
                comp.setPreferredSize(new Dimension(pref.width, flexBasis));
            }
        }

        if (wrap == Wrap.WRAP || gridBagEngine == null) {
            return;
        }

        GridBagConstraints gbc = new GridBagConstraints();
        if (direction == Direction.ROW) {
            gbc.gridy = 0;
            gbc.weighty = 1.0;
            gbc.fill = (alignItems == AlignItems.STRETCH) ? GridBagConstraints.BOTH : GridBagConstraints.VERTICAL;
            gbc.insets = new Insets(0, 0, 0, gap);
            if (flexGrow > 0) {
                gbc.weightx = flexGrow;
                gbc.fill = GridBagConstraints.BOTH;
            }
        } else {
            gbc.gridx = 0;
            gbc.weightx = 1.0;
            gbc.fill = (alignItems == AlignItems.STRETCH) ? GridBagConstraints.BOTH : GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(0, 0, gap, 0);
            if (flexGrow > 0) {
                gbc.weighty = flexGrow;
                gbc.fill = GridBagConstraints.BOTH;
            }
        }

        switch (justifyContent) {
            case START -> gbc.anchor = (direction == Direction.ROW) ? GridBagConstraints.WEST : GridBagConstraints.NORTH;
            case END -> gbc.anchor = (direction == Direction.ROW) ? GridBagConstraints.EAST : GridBagConstraints.SOUTH;
            case CENTER -> gbc.anchor = GridBagConstraints.CENTER;
        }

        gridBagEngine.addLayoutComponent(comp, gbc);
    }

    private Dimension getComponentSize(Component comp, Container parent) {
        Dimension d = new Dimension(comp.getPreferredSize());
        
        if (percentBasisMap.containsKey(comp)) {
            double percent = percentBasisMap.get(comp);
            Insets insets = parent.getInsets();
            
            if (direction == Direction.ROW) {
                int availableWidth = parent.getWidth() - (insets.left + insets.right);
                // [FIX Issue #2]: Only apply if > 0. Prevents Integer.MAX_VALUE explosions when unpacked.
                if (availableWidth > 0) {
                    d.width = (int) Math.round(availableWidth * percent);
                }
            } else {
                int availableHeight = parent.getHeight() - (insets.top + insets.bottom);
                if (availableHeight > 0) {
                    d.height = (int) Math.round(availableHeight * percent);
                }
            }

            // [FIX Issue #1]: Mutation boundary check to prevent PropertyChangeEvent UI loop storms
            Dimension currentPref = comp.getPreferredSize();
            if (currentPref.width != d.width || currentPref.height != d.height) {
                comp.setPreferredSize(new Dimension(d)); 
            }
        }
        return d;
    }

    @Override
    public void layoutContainer(Container parent) {
        for (Component comp : parent.getComponents()) {
            if (percentBasisMap.containsKey(comp)) {
                getComponentSize(comp, parent);
            }
        }

        if (wrap == Wrap.NO_WRAP) {
            gridBagEngine.layoutContainer(parent);
            return;
        }

        synchronized (parent.getTreeLock()) {
            Insets insets = parent.getInsets();
            List<Component> currentGroup = new ArrayList<>();
            int currentLength = 0;
            int currentCrossMax = 0;

            int maxAvailableLength = (direction == Direction.ROW)
                    ? parent.getWidth() - (insets.left + insets.right + gap * 2)
                    : parent.getHeight() - (insets.top + insets.bottom + gap * 2);

            int crossCoordinate = (direction == Direction.ROW) ? insets.top + gap : insets.left + gap;

            for (Component comp : parent.getComponents()) {
                if (!comp.isVisible()) continue;

                Dimension d = comp.getPreferredSize();
                int compLength = (direction == Direction.ROW) ? d.width : d.height;
                int compCross = (direction == Direction.ROW) ? d.height : d.width;

                if (!currentGroup.isEmpty() && currentLength + gap + compLength > maxAvailableLength) {
                    layoutSingleSegment(currentGroup, crossCoordinate, currentCrossMax, parent, insets);
                    crossCoordinate += currentCrossMax + gap;
                    currentGroup.clear();
                    currentLength = 0;
                    currentCrossMax = 0;
                }

                if (!currentGroup.isEmpty()) {
                    currentLength += gap;
                }
                currentGroup.add(comp);
                currentLength += compLength;
                currentCrossMax = Math.max(currentCrossMax, compCross);
            }

            if (!currentGroup.isEmpty()) {
                layoutSingleSegment(currentGroup, crossCoordinate, currentCrossMax, parent, insets);
            }
        }
    }

    private void layoutSingleSegment(List<Component> components, int crossCoord, int crossMax, Container parent, Insets insets) {
        int totalLength = 0;
        
        // [FIX Issue #4]: Precision tracking for percentage truncation
        double totalPercent = 0.0;
        Component lastPercentComp = null;
        int actualPercentPixels = 0;

        for (int i = 0; i < components.size(); i++) {
            Component comp = components.get(i);
            Dimension d = getComponentSize(comp, parent);
            int len = (direction == Direction.ROW) ? d.width : d.height;
            totalLength += len;
            if (i > 0) totalLength += gap;
            
            // Track percentages to fix fractional pixel gaps
            if (percentBasisMap.containsKey(comp)) {
                totalPercent += percentBasisMap.get(comp);
                actualPercentPixels += len;
                lastPercentComp = comp;
            }
        }
        
        // Calculate the leftover pixels from floating point truncation
        Map<Component, Integer> pixelCompensation = new HashMap<>();
        if (lastPercentComp != null) {
            int availableLayoutLength = (direction == Direction.ROW) 
                ? parent.getWidth() - (insets.left + insets.right) 
                : parent.getHeight() - (insets.top + insets.bottom);
                
            int expectedPercentPixels = (int) Math.round(availableLayoutLength * totalPercent);
            int remainder = expectedPercentPixels - actualPercentPixels;
            
            if (remainder != 0) {
                pixelCompensation.put(lastPercentComp, remainder);
                totalLength += remainder;
            }
        }

        int parentLength = (direction == Direction.ROW) ? parent.getWidth() : parent.getHeight();
        int startCoord = (direction == Direction.ROW) ? insets.left + gap : insets.top + gap;
        
        if (justifyContent == JustifyContent.CENTER) {
            startCoord = (parentLength - totalLength) / 2;
        } else if (justifyContent == JustifyContent.END) {
            int endInset = (direction == Direction.ROW) ? insets.right : insets.bottom;
            startCoord = parentLength - endInset - gap - totalLength;
        }

        for (Component comp : components) {
            Dimension d = getComponentSize(comp, parent);
            int compX, compY, compW, compH;
            
            // Apply the precision adjustment if this is the final percent component
            int precisionOffset = pixelCompensation.getOrDefault(comp, 0);

            if (direction == Direction.ROW) {
                compX = startCoord;
                compY = crossCoord;
                compW = d.width + precisionOffset;
                compH = (alignItems == AlignItems.STRETCH) ? crossMax : d.height;
                if (alignItems == AlignItems.CENTER) compY = crossCoord + (crossMax - d.height) / 2;
                if (alignItems == AlignItems.END) compY = crossCoord + (crossMax - d.height);
                
                comp.setBounds(compX, compY, compW, compH);
                startCoord += compW + gap;
            } else {
                compX = crossCoord;
                compY = startCoord;
                compW = (alignItems == AlignItems.STRETCH) ? crossMax : d.width;
                compH = d.height + precisionOffset;
                if (alignItems == AlignItems.CENTER) compX = crossCoord + (crossMax - d.width) / 2;
                if (alignItems == AlignItems.END) compX = crossCoord + (crossMax - d.width);
                
                comp.setBounds(compX, compY, compW, compH);
                startCoord += compH + gap;
            }
        }
    }

    @Override
    public Dimension preferredLayoutSize(Container parent) {
        if (wrap == Wrap.NO_WRAP) {
            return gridBagEngine.preferredLayoutSize(parent);
        }
        synchronized (parent.getTreeLock()) {
            Insets insets = parent.getInsets();
            
            // [FIX Issue #2]: Removed the Integer.MAX_VALUE fallback. 
            // We now measure natural unconstrained geometry if the window isn't packed yet.
            int targetWidth = parent.getWidth();
            int targetHeight = parent.getHeight();
            
            int maxAvailableLength = (direction == Direction.ROW) 
                    ? (targetWidth > 0 ? targetWidth - (insets.left + insets.right + gap * 2) : Integer.MAX_VALUE)
                    : (targetHeight > 0 ? targetHeight - (insets.top + insets.bottom + gap * 2) : Integer.MAX_VALUE);

            int totalCross = 0;
            int maxLineLength = 0;
            int currentLength = 0;
            int currentCrossMax = 0;
            boolean first = true;
            
            for (Component comp : parent.getComponents()) {
                if (!comp.isVisible()) continue;
                
                Dimension d = getComponentSize(comp, parent);
                int compLength = (direction == Direction.ROW) ? d.width : d.height;
                int compCross = (direction == Direction.ROW) ? d.height : d.width;
                
                if (!first && currentLength + gap + compLength > maxAvailableLength) {
                    totalCross += currentCrossMax + gap;
                    maxLineLength = Math.max(maxLineLength, currentLength);
                    currentLength = 0;
                    currentCrossMax = 0;
                    first = true;
                }
                
                if (!first) currentLength += gap;
                currentLength += compLength;
                currentCrossMax = Math.max(currentCrossMax, compCross);
                first = false;
            }
            
            totalCross += currentCrossMax;
            maxLineLength = Math.max(maxLineLength, currentLength);
            
            if (direction == Direction.ROW) {
                return new Dimension(maxLineLength + insets.left + insets.right + gap * 2, totalCross + insets.top + insets.bottom + gap * 2);
            } else {
                return new Dimension(totalCross + insets.left + insets.right + gap * 2, maxLineLength + insets.top + insets.bottom + gap * 2);
            }
        }
    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
        return preferredLayoutSize(parent);
    }

    @Override
    public void removeLayoutComponent(Component comp) {
        percentBasisMap.remove(comp);
        if (gridBagEngine != null) {
            gridBagEngine.removeLayoutComponent(comp);
        }
    }

    @Override
    public void addLayoutComponent(String name, Component comp) {}

    @Override
    public Dimension maximumLayoutSize(Container target) {
        return preferredLayoutSize(target);
    }

    @Override
    public float getLayoutAlignmentX(Container target) { return 0.5f; }

    @Override
    public float getLayoutAlignmentY(Container target) { return 0.5f; }

    @Override
    public void invalidateLayout(Container target) {
        if (gridBagEngine != null) {
            gridBagEngine.invalidateLayout(target);
        }
    }
}