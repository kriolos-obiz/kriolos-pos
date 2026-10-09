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

import com.openbravo.pos.printer.ticket.BasicTicket;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Toolkit;
import java.util.Map;

class JTicket extends javax.swing.JPanel {

    private static final long serialVersionUID = 1L;

    private static final int DEFAULT_COLUMNS = 46;
    private static final int H_GAP = 8;
    private static final int V_GAP = 8;
    // Margem extra inferior para acomodar o descent da tipografia e o remate do bilhete
    private static final int BOTTOM_PADDING = 12;

    private final int columns;
    private final int linewidth;
    private final BasicTicket basict;

    private JTicket(BasicTicket t, int columns) {
        this.columns = columns;
        this.linewidth = columns * 7;
        this.basict = t;
        initComponents();
    }

    public JTicket(BasicTicket t) {
        this(t, DEFAULT_COLUMNS);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g.create();

        Toolkit tk = Toolkit.getDefaultToolkit();
        if (tk.getDesktopProperty("awt.font.desktophints") instanceof Map<?, ?> desktopHints) {
            g2d.addRenderingHints(desktopHints);
        }

        Insets i = getInsets();
        int contentWidth = getWidth() - i.left - i.right;
        int contentHeight = getHeight() - i.top - i.bottom;

        // Fundo com ligeiro gradiente
        g2d.setPaint(new GradientPaint(
                contentWidth - 100,
                contentHeight - 100,
                getBackground(),
                contentWidth,
                contentHeight,
                new Color(0xf0f0f0), 
                true));
        g2d.fillRect(i.left, i.top, contentWidth, contentHeight);

        // Desenha o conteúdo do bilhete
        g2d.setColor(getForeground());
        if (basict != null) {
            basict.draw(g2d, i.left + H_GAP, i.top + V_GAP, linewidth);
        }

        g2d.dispose();
    }

    @Override
    public Dimension getPreferredSize() {
        Insets ins = getInsets();
        int ticketHeight = (basict != null) ? basict.getHeight() : 100;

        return new Dimension(
                linewidth + (2 * H_GAP) + ins.left + ins.right,
                ticketHeight + (2 * V_GAP) + BOTTOM_PADDING + ins.top + ins.bottom);
    }

    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     */
    private void initComponents() {
        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        setFont(new java.awt.Font("Arial", 0, 14));
        setLayout(new java.awt.BorderLayout());
    }
}