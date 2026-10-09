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
package com.openbravo.pos.printer.screen;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;

/**
 * Custom layout manager that ensures JTicket components wrap to the next line
 * and are strictly top-aligned within their row. Supports dynamic scaling by
 * communicating with JTicketContainer.
 *
 * @author KriolOS
 */
public class TicketWrapLayout implements LayoutManager {

    private final int hGap;
    private final int vGap;
    private final int defaultWidth;
    private final int defaultHeight;

    public TicketWrapLayout(int hGap, int vGap, int defaultWidth, int defaultHeight) {
        this.hGap = hGap;
        this.vGap = vGap;
        this.defaultWidth = defaultWidth;
        this.defaultHeight = defaultHeight;
    }

    @Override
    public void addLayoutComponent(String name, Component comp) {
    }

    @Override
    public void removeLayoutComponent(Component comp) {
    }

    @Override
    public Dimension preferredLayoutSize(Container parent) {
        return layoutSize(parent, true);
    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
        double zoomFactor = getZoom(parent);
        return new Dimension((int) (defaultWidth * zoomFactor), (int) (defaultHeight * zoomFactor));
    }

    @Override
    public void layoutContainer(Container parent) {
        layoutSize(parent, false);
    }

    private double getZoom(Container parent) {
        if (parent instanceof JTicketContainer) {
            return ((JTicketContainer) parent).getZoom();
        }
        return 1.0;
    }

    private Dimension layoutSize(Container parent, boolean calculateOnly) {
        synchronized (parent.getTreeLock()) {
            Insets insets = parent.getInsets();
            int logicalWidth = parent.getWidth();
            double zoomFactor = getZoom(parent);

            if (logicalWidth == 0) {
                logicalWidth = defaultWidth;
            }

            int maxWidth = logicalWidth - insets.left - insets.right;
            int scaledMaxWidth = (int) (maxWidth / zoomFactor);

            int currentX = hGap;
            int currentY = insets.top + vGap;
            int maxRowHeight = 0;

            int componentCount = parent.getComponentCount();
            for (int i = 0; i < componentCount; i++) {
                Component comp = parent.getComponent(i);
                if (comp.isVisible()) {
                    Dimension dc = comp.getPreferredSize();

                    // Wrap to next line if component exceeds row width
                    if (currentX + dc.width > scaledMaxWidth && currentX > hGap) {
                        currentX = hGap;
                        currentY += vGap + maxRowHeight;
                        maxRowHeight = 0;
                    }

                    if (!calculateOnly) {
                        // Set exact bounds according to the ticket's preferred size
                        comp.setBounds(currentX, currentY, dc.width, dc.height);
                    }

                    currentX += dc.width + hGap;
                    maxRowHeight = Math.max(maxRowHeight, dc.height);
                }
            }

            int totalHeight = currentY + maxRowHeight + vGap + insets.bottom;

            // Use totalHeight so JScrollPane receives the actual required height
            return new Dimension(
                    logicalWidth,
                    (int) Math.ceil(Math.max(totalHeight, defaultHeight) * zoomFactor)
            );
        }
    }
}
