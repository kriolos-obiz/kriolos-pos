/*
 * Copyright (C) 2022 KriolOS
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
package com.openbravo.pos.printer.printer;

import com.openbravo.pos.printer.ticket.BasicTicket;
import com.openbravo.pos.printer.ticket.PrintItem;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.util.List;

/**
 * Renders a {@link BasicTicket} onto a printable document across one or more pages
 * adhering to Java's {@link Printable} contract.
 *
 * <p>This renderer handles pagination by calculating the cumulative height of each
 * {@link PrintItem} against the target page's imageable height and bounds.</p>
 *
 * @author adrianromero
 */
public class PrintableBasicTicket implements Printable {

    /** The horizontal offset (origin X) of the printable page area in points. */
    private final int imageableX;

    /** The vertical offset (origin Y) of the printable page area in points. */
    private final int imageableY;

    /** The usable printing width available on the target page in points. */
    private final int imageableWidth;

    /** The usable printing height available on the target page in points. */
    private final int imageableHeight;

    /** The ticket data structure containing the sequence of items to print. */
    private final BasicTicket ticket;

    /**
     * Constructs a printable ticket adapter with defined page boundary constraints.
     *
     * @param ticket          the {@link BasicTicket} instance containing printable drawing commands
     * @param imageableX      the horizontal coordinate where printable content starts (in points)
     * @param imageableY      the vertical coordinate where printable content starts (in points)
     * @param imageableWidth  the total printable width allocated for the ticket content (in points)
     * @param imageableHeight the maximum printable height available per page before breaking (in points)
     */
    public PrintableBasicTicket(BasicTicket ticket, int imageableX, int imageableY, int imageableWidth, int imageableHeight) {
        this.ticket = ticket;
        this.imageableX = imageableX;
        this.imageableY = imageableY;
        this.imageableWidth = imageableWidth;
        this.imageableHeight = imageableHeight;
    }

    /**
     * Prints the ticket page at the specified page index into the provided {@link Graphics} context.
     *
     * @param graphics   the graphics context onto which the page is rendered
     * @param pageFormat the size and orientation of the page being drawn
     * @param pageIndex  the zero-based index of the page requested by the print engine
     * @return {@link Printable#PAGE_EXISTS} if the page contains ticket data; 
     *         {@link Printable#NO_SUCH_PAGE} if {@code pageIndex} exceeds total calculated pages
     * @throws PrinterException if an unrecoverable printing error occurs during drawing
     */
    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
        Graphics2D g2d = (Graphics2D) graphics;

        int currentLine = 0;
        int currentPage = 0;
        int currentPageY = 0;
        boolean printed = false;

        g2d.translate(imageableX, imageableY);

        List<PrintItem> commands = ticket.getCommands();

        while (currentLine < commands.size()) {
            PrintItem item = commands.get(currentLine);
            int itemHeight = item.getHeight();

            // Check if item fits on the current page or requires pagination
            if (currentPageY + itemHeight <= imageableHeight) {
                currentPageY += itemHeight;
            } else {
                currentPage++;
                currentPageY = itemHeight;
            }

            // Render only if the computed page matches the requested print pageIndex
            if (currentPage == pageIndex) {
                printed = true;
                item.draw(g2d, 0, currentPageY - itemHeight, imageableWidth);
            }

            currentLine++;
        }

        return printed ? Printable.PAGE_EXISTS : Printable.NO_SUCH_PAGE;
    }

}
