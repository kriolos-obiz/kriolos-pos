/*
 * Copyright (C) 2022 Kriolos
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but without any warranty; without even the implied warranty of
 * merchantability or fitness for a particular purpose.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://gnu.org>.
 */
package com.openbravo.pos.printer.ticket;

import com.openbravo.pos.printer.DevicePrinter;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import org.krysalis.barcode4j.BarcodeDimension;
import org.krysalis.barcode4j.HumanReadablePlacement;
import org.krysalis.barcode4j.impl.AbstractBarcodeBean;
import org.krysalis.barcode4j.impl.code128.Code128Bean;
import org.krysalis.barcode4j.impl.upcean.EAN13Bean;
import org.krysalis.barcode4j.output.java2d.Java2DCanvasProvider;

/**
 * Handles the generation and rendering of barcodes on receipts/tickets.
 *
 * @author JG uniCenta
 */
public class PrintItemBarcode implements PrintItem {

    private AbstractBarcodeBean m_barcode;
    private final String m_sType;
    private final String m_sPosition;
    private final String m_sCode;
    private final double scale;
    private int m_iWidth;
    private int m_iHeight;

    /**
     * Initializes a new instance of the barcode print item.
     *
     * @param type     The barcode symbology type (e.g., CODE128, EAN13)
     * @param position The position of the human-readable text (top, bottom, none)
     * @param code     The actual value string encoded inside the barcode
     * @param scale    The scaling factor used for rendering sizing
     */
    public PrintItemBarcode(String type, String position, String code, double scale) {
        this.m_sType = type;
        this.m_sPosition = position;
        this.m_sCode = code;
        this.scale = scale;

        // Choose appropriate barcode implementation bean based on config type
        if (DevicePrinter.BARCODE_CODE128.equals(m_sType)) {
            m_barcode = new Code128Bean();
        } else {
            m_barcode = new EAN13Bean();
        }

        // Set baseline default metrics for barcode rendering layout
        m_barcode.setModuleWidth(1.0);
        m_barcode.setBarHeight(40.0);
        m_barcode.setFontSize(10.0);
        m_barcode.setQuietZone(10.0);

        // Map textual position configuration to Barcode4J layout placement enums
        if ("none".equals(m_sPosition)) {
            m_barcode.setMsgPosition(HumanReadablePlacement.HRP_NONE);
        } else if ("top".equals(m_sPosition)) {
            m_barcode.setMsgPosition(HumanReadablePlacement.HRP_TOP);
        } else {
            m_barcode.setMsgPosition(HumanReadablePlacement.HRP_BOTTOM);
        }

        // Pre-calculate dimensional layout bounds factoring in scaling metrics
        BarcodeDimension dim = m_barcode.calcDimensions(m_sCode);
        m_iWidth = (int) (dim.getWidthPlusQuiet() * scale);
        m_iHeight = (int) (dim.getHeightPlusQuiet() * scale);
    }

    @Override
    public void draw(Graphics2D g2d, int x, int y, int width) {
        // Cache active transform state context before applying localized updates
        AffineTransform oldTransform = g2d.getTransform();

        // Translate context pointer grid coordinate location and apply scaling multipliers
        g2d.translate(x, y);
        g2d.scale(scale, scale);

        // Render graphical barcode element tracking directly onto device drawing canvas
        Java2DCanvasProvider provider = new Java2DCanvasProvider(g2d,0);
        m_barcode.generateBarcode(provider, m_sCode);

        // Revert contextual transformation properties back to prior origin state
        g2d.setTransform(oldTransform);
    }

    @Override
    public int getHeight() {
        return m_iHeight;
    }
}
