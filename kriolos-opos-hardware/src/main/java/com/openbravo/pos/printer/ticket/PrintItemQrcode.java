/*
 * Copyright (C) 2026 Kriolos
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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.HashMap;
import java.util.Map;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

/**
 * Handles the generation and rendering of 2D QR Codes on receipts/tickets using ZXing,
 * using a primitive char for optimized Error Correction level matching.
 *
 * @author JG uniCenta
 */
public class PrintItemQrcode implements PrintItem {

    private static final double DEFAULT_SCALE = 2.0;
    private static final int DEFAULT_SIZE = 100;
    private static final char DEFAULT_CORRECTION_LEVEL = 'M';

    private final String m_scode;
    private final double scale;
    private final int m_size;
    private BitMatrix m_bitMatrix;

    /**
     * Minimal Constructor - Perfect for simple XML parsing like <qrcode>Text</qrcode>
     * Applies default retail safe metrics: scale=2.0, size=100, error correction='M'.
     */
    public PrintItemQrcode(String code) {
        this(code, DEFAULT_SCALE, DEFAULT_SIZE, DEFAULT_CORRECTION_LEVEL);
    }

    /**
     * XML Attribute Constructor - Used when attributes are partially available.
     */
    public PrintItemQrcode(String code, int size, char ecLevel) {
        this(code, DEFAULT_SCALE, size, ecLevel); // Automatically injects default scale 2.0
    }

    /**
     * Initializes a new instance of the QR code print item.
     *
     * @param code    The raw content text or URL to encode inside the QR matrix
     * @param scale   The scaling multiplier applied to each pixel block during layout rendering
     * @param size    The base requested size (width/height dimensions) for the matrix grid
     * @param ecLevel The Error Correction level character from XML ('L', 'M', 'Q', 'H')
     */
    public PrintItemQrcode(String code, double scale, int size, char ecLevel) {
        this.m_scode = code;
        this.scale = scale;
        this.m_size = size;

        try {
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 1); // Minimize quiet zone borders to conserve ticket space

            // Map the primitive char to ZXing's ErrorCorrectionLevel enum
            ErrorCorrectionLevel errorLevel;
            switch (Character.toUpperCase(ecLevel)) {
                case 'L':
                    errorLevel = ErrorCorrectionLevel.L; // ~7% correction
                    break;
                case 'Q':
                    errorLevel = ErrorCorrectionLevel.Q; // ~25% correction
                    break;
                case 'H':
                    errorLevel = ErrorCorrectionLevel.H; // ~30% correction
                    break;
                case 'M':
                default:
                    errorLevel = ErrorCorrectionLevel.M; // ~15% correction
                    break;
            }
            hints.put(EncodeHintType.ERROR_CORRECTION, errorLevel);

            // Generate the matrix data representation via MultiFormatWriter utilizing the core engine
            m_bitMatrix = new MultiFormatWriter().encode(m_scode, BarcodeFormat.QR_CODE, m_size, m_size, hints);
        } catch (Exception e) {
            // Fallback empty matrix safely initializing structural layout properties if error occurs
            m_bitMatrix = new BitMatrix(m_size);
        }
    }

    @Override
    public void draw(Graphics2D g2d, int x, int y, int width) {
        if (m_bitMatrix == null) {
            return;
        }

        AffineTransform oldTransform = g2d.getTransform();
        g2d.translate(x, y);

        int matrixWidth = m_bitMatrix.getWidth();
        int matrixHeight = m_bitMatrix.getHeight();

        Color oldColor = g2d.getColor();
        g2d.setColor(Color.BLACK);

        for (int verticalIndex = 0; verticalIndex < matrixHeight; verticalIndex++) {
            for (int horizontalIndex = 0; horizontalIndex < matrixWidth; horizontalIndex++) {
                if (m_bitMatrix.get(horizontalIndex, verticalIndex)) {
                    g2d.fillRect(
                            (int) (horizontalIndex * scale),
                            (int) (verticalIndex * scale),
                            (int) Math.ceil(scale),
                            (int) Math.ceil(scale)
                    );
                }
            }
        }

        g2d.setColor(oldColor);
        g2d.setTransform(oldTransform);
    }

    @Override
    public int getHeight() {
        return (int) (m_bitMatrix.getHeight() * scale);
    }
}
