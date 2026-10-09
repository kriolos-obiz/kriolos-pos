/*
 * Copyright (C) 2022 KriolOS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.openbravo.pos.printer.ticket;

import java.awt.Font;
import java.awt.geom.AffineTransform;

/**
 * Screen implementation of a basic receipt ticket view.
 * Uses Font.MONOSPACED to guarantee that text width metrics calculations 
 * for center and right alignments match pixels perfectly.
 * 
 * The font is intentionally scaled vertically to mimic the taller 
 * character rendering typical of physical ESC/POS thermal printers.
 * 
 * @author JG uniCenta
 * @author KriolOS
 */
public class BasicTicketForScreen extends BasicTicket {

    // --- Configuration Constants ---
    private static final int FONT_SIZE = 12;
    private static final double SCALE_X = 1.0;
    private static final double SCALE_Y = 1.20; // 20% taller characters for ESC/POS realism
    
    /** 
     * The fixed pixel height allocated for each text line. 
     * Bound to FONT_SIZE * SCALE_Y + line spacing. (12 * 1.20 = 14.4 + leading ≈ 16)
     */
    private static final int FONT_HEIGHT = 16; 
    private static final double IMAGE_SCALE = 1.0;

    // Using Font.MONOSPACED ensures alignment math doesn't break across operating systems
    private static final Font BASE_FONT = new Font(Font.MONOSPACED, Font.PLAIN, FONT_SIZE)
            .deriveFont(AffineTransform.getScaleInstance(SCALE_X, SCALE_Y));
            
    /**
     * @return The immutable monospaced font instance for correct pixel-width calculations
     */
    @Override
    protected Font getBaseFont() {
        return BASE_FONT;
    }

    /**
     * @return The height allowance allocated for each text line
     */
    @Override
    protected int getFontHeight() {
        return FONT_HEIGHT;
    }

    /**
     * @return The image scaling factor applied to logo or graphical renders
     */
    @Override
    protected double getImageScale() {
        return IMAGE_SCALE;
    }
}