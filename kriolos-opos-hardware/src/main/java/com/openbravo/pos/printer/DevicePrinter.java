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
package com.openbravo.pos.printer;

import java.awt.image.BufferedImage;
import javax.swing.JComponent;

/**
 *
 * @author JG uniCenta
 */
public interface DevicePrinter {

// Font Sizes
    /**
     * Represents the "size" attribute value for a "line" XML element,
     * indicating Font Style Normal (widthScale: 1,  heightScale: 1)
     * <p>
     * This constant corresponds to the XML:
     * <pre>
     * &lt;line size="1"&gt;...&lt;/line&gt;
     * </pre>
     * </p>
     */
    public static final int SIZE_0 = 0;
    
    /**
     * Represents the "size" attribute value for a "line" XML element,
     * indicating Font Style 'Double Width' (widthScale: 2,  heightScale: 1).
     * Name: Condensed or expanded font, 
     * 
     * <p>
     * This constant corresponds to the XML:
     * <pre>
     * &lt;line size="2"&gt;...&lt;/line&gt;
     * </pre>
     * </p>
     */
    public static final int SIZE_1 = 1;   
    
    /**
     * Represents the "size" attribute value for a "line" XML element,
     * indicating Font Style 'Double Height' (widthScale: 1,  heightScale: 2). 
     * Name: Tall font
     * 
     * <p>
     * This constant corresponds to the XML:
     * <pre>
     * &lt;line size="3"&gt;...&lt;/line&gt;
     * </pre>
     * </p>
     */
    public static final int SIZE_2 = 2;
    
    /**
     * Represents the "size" attribute value for a "line" XML element,
     * indicating Font Style 'Double Width and Height' (widthScale: 2,  heightScale: 2).
     * Name: Font both condensed and tall or expanded and tall
     * 
     * <p>
     * This constant corresponds to the XML:
     * <pre>
     * &lt;line size="3"&gt;...&lt;/line&gt;
     * </pre>
     * </p>
     */
    public static final int SIZE_3 = 3;

// Font Enhancers
    public static final int STYLE_PLAIN = 0;
    public static final int STYLE_BOLD = 1;
    public static final int STYLE_UNDERLINE = 2;

// Layout    
    public static final int ALIGN_LEFT = 0;
    public static final int ALIGN_RIGHT = 1;
    public static final int ALIGN_CENTER = 2;

    public static final String POSITION_BOTTOM = "bottom";
    public static final String POSITION_NONE = "none";

// Barcodes    
    public static final String BARCODE_EAN8 = "EAN8";
    public static final String BARCODE_EAN13 = "EAN13";
    public static final String BARCODE_UPCA = "UPC-A";
    public static final String BARCODE_UPCE = "UPC-E";
    public static final String BARCODE_CODE128 = "CODE128";
    public static final String BARCODE_CODE39 = "CODE39";

//QrCode
    public static final int QRCODE_DEFAULT_SIZE = 4;
    public static final char QRCODE_DEFAULT_ERROR_CODE = 'M';

    public String getPrinterName();

    public String getPrinterDescription();

    public JComponent getPrinterComponent();

// Initialise    
    public void reset();

    public void beginReceipt();

// Graphic renders
    public void printImage(BufferedImage image);

    public void printLogo();

    public void printBarCode(String type, String position, String code);
    
    public void printQRCode(String code, int size, char errorCorrection);

// Do TextLine
    public void beginLine(int iTextSize);

    public void printText(int iStyle, String sText);

    public void endLine();

// Close
    public void endReceipt();

// Transact    
    public void openDrawer();

    /**
     * Enum for printer font sizes.
     */
    public enum FontSize {
        NORMAL(1.0, 1.0),
        DOUBLE_WIDTH(2.0, 1.0),
        DOUBLE_HEIGHT(1.0, 2.0),
        DOUBLE_WIDTH_HEIGHT(2.0, 2.0);

        private final double widthScale;
        private final double heightScale;

        FontSize(double widthScale, double heightScale) {
            this.widthScale = widthScale;
            this.heightScale = heightScale;
        }

        public double getWidthScale() {
            return widthScale;
        }

        public double getHeightScale() {
            return heightScale;
        }

        public int getLineMultiplier() {
            return (int) heightScale;
        }

        /**
         * A utility method to get the FontSize enum from the original integer
         * constant.
         *
         * This can be used for backwards compatibility if needed.
         *
         * @param iSize The original integer size constant (0-3).
         * @return The corresponding FontSize enum.
         */
        public static FontSize fromInt(int iSize) {
            switch (iSize) {
                case 0:
                    return NORMAL;
                case 1:
                    return DOUBLE_HEIGHT;
                case 2:
                    return DOUBLE_WIDTH;
                case 3:
                    return DOUBLE_WIDTH_HEIGHT;
                default:
                    return NORMAL; // Fallback for invalid values
            }
        }
        
        
        public static int getLineMultiplier(int iSize) {
            return FontSize.fromInt(iSize).getLineMultiplier();
        }
    }

}
