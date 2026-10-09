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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import javax.swing.JPanel;
import javax.swing.Scrollable;
import javax.swing.SwingUtilities;

/**
 * Container panel that stores and dynamically positions JTicket view components.
 * Uses TicketWrapLayout to ensure top-alignment of tickets with different heights.
 * Includes APIs for zoom scaling, printing, and scroll tracking.
 * 
 * @author Adrian
 * @author KriolOS
 */
public class JTicketContainer extends JPanel implements Printable, Scrollable {

    private static final long serialVersionUID = 1L;

    private static final int DEFAULT_HEIGHT = 600;
    private static final int DEFAULT_WITH = 700;
    private static final int HORIZONTAL_GAP = 8;
    private static final int VERTICAL_GAP = 8;
    
    // Smooth scroll speed per wheel click
    private static final int SCROLL_UNIT_INCREMENT = 16; 
    
    private double zoomFactor = 1.0;

    public JTicketContainer() {
        initComponents();
        setLayout(new TicketWrapLayout(HORIZONTAL_GAP, VERTICAL_GAP, DEFAULT_WITH, DEFAULT_HEIGHT));
    }

    public void setZoom(double zoom) {
        this.zoomFactor = Math.max(0.1, zoom); 
        revalidate(); 
        repaint();    
    }

    public double getZoom() {
        return zoomFactor;
    }

    @Override
    protected void paintChildren(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();

        // 1. Maintain sharp text rendering via sub-pixel LCD antialiasing
        g2.setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING,
                            java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // 2. Apply viewport zoom scale
        g2.scale(zoomFactor, zoomFactor);
        super.paintChildren(g2);
        g2.dispose();

        // 3. For zoom-out levels, draw an unscaled 1-pixel border overlay 
        // to prevent sub-pixel border truncation on outer edges
        if (zoomFactor < 1.0) {
            Graphics2D gOverlay = (Graphics2D) g.create();
            gOverlay.setColor(java.awt.Color.BLACK);

            int count = getComponentCount();
            for (int i = 0; i < count; i++) {
                Component comp = getComponent(i);
                if (comp.isVisible()) {
                    int x = (int) Math.round(comp.getX() * zoomFactor);
                    int y = (int) Math.round(comp.getY() * zoomFactor);
                    int w = (int) Math.round(comp.getWidth() * zoomFactor);
                    int h = (int) Math.round(comp.getHeight() * zoomFactor);
                    gOverlay.drawRect(x, y, w, h);
                }
            }
            gOverlay.dispose();
        }
    }

    public void printTickets() {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable(this);
        if (job.printDialog()) {
            try {
                job.print();
            } catch (PrinterException ex) {
                System.err.println("Error printing tickets: " + ex.getMessage());
            }
        }
    }

    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
        if (pageIndex > 0) {
            return NO_SUCH_PAGE;
        }

        Graphics2D g2d = (Graphics2D) graphics;
        g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
        
        double pageWidth = pageFormat.getImageableWidth();
        double panelWidth = this.getWidth();
        if (panelWidth > pageWidth) {
            double scale = pageWidth / panelWidth;
            g2d.scale(scale, scale);
        }

        this.printAll(graphics);
        return PAGE_EXISTS;
    }

    public void addTicket(JTicket ticket) {
        add(ticket);
        revalidate();
        repaint();
        
        SwingUtilities.invokeLater(() -> {
            int componentCount = getComponentCount();
            if (componentCount > 0) {
                Component lastComp = getComponent(componentCount - 1);
                Rectangle bounds = lastComp.getBounds();
                bounds.x = (int) (bounds.x * zoomFactor);
                bounds.y = (int) (bounds.y * zoomFactor);
                bounds.width = (int) (bounds.width * zoomFactor);
                bounds.height = (int) (bounds.height * zoomFactor);
                scrollRectToVisible(bounds);
            }
        });
    }

    public void clearAllTickets() {
        removeAllTickets();
    }

    public void removeAllTickets() {
        removeAll();
        revalidate();
        repaint();
        scrollRectToVisible(new Rectangle(0, 0, 1, 1));   
    }

    // =========================================================================
    // SCROLLABLE INTERFACE & DYNAMIC PREFERRED SIZE
    // =========================================================================

    @Override
    public Dimension getPreferredSize() {
        // Delegate calculation to TicketWrapLayout to ensure dynamic height expansion
        if (getLayout() != null) {
            return getLayout().preferredLayoutSize(this);
        }
        return new Dimension(DEFAULT_WITH, DEFAULT_HEIGHT);
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return SCROLL_UNIT_INCREMENT;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        if (orientation == javax.swing.SwingConstants.VERTICAL) {
            return visibleRect.height;
        } else {
            return visibleRect.width;
        }
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        // Enforce panel width to match viewport width to trigger line wrapping
        return true; 
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        // Must return false to allow vertical growth and display scrollbars
        return false; 
    }

    /** 
     * Clean initialization: Omits setPreferredSize() to avoid locking the container height.
     */
    private void initComponents() {
        setFont(new java.awt.Font("Arial", 0, 12)); 
        setLayout(null);
    }                        
}