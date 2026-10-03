/*
 * Copyright (C) 2022-2026 KriolOS
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
import com.openbravo.pos.spi.hardware.printer.PrinterDevice;
import com.openbravo.pos.spi.hardware.printer.PrinterProtocol;
import com.openbravo.pos.spi.hardware.printer.PrinterException;

/**
 * High-level contract for receipt and ticket printer operations.
 *
 * @author JG uniCenta / KriolOS Team
 */
public interface DevicePrinter extends PrinterDevice {

    @Override
    default PrinterProtocol getProtocol() {
        return PrinterProtocol.PLAIN;
    }

    @Override
    default boolean isConnected() {
        return true;
    }

    @Override
    default void print(byte[] data) throws PrinterException {
        // Raw byte print fallback
    }

    // Font Sizes
    public static final int SIZE_0 = 0;
    public static final int SIZE_1 = 1;
    public static final int SIZE_2 = 2;
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

    // QR Code
    public static final int QRCODE_DEFAULT_SIZE = 4;
    public static final char QRCODE_DEFAULT_ERROR_CODE = 'M';

    @Override
    String getPrinterName();

    @Override
    String getPrinterDescription();

    JComponent getPrinterComponent();

    // Initialise
    void reset();

    void beginReceipt();

    // Graphic renders
    void printImage(BufferedImage image);

    void printLogo();

    void printBarCode(String type, String position, String code);

    void printQRCode(String code, int size, char errorCorrection);

    // TextLine
    void beginLine(int iTextSize);

    void printText(int iStyle, String sText);


    default void printText(int iStyle, String sText, int textLenght, int textAlignment){
        String aligntext = PrinterTextUtils.alignText(textAlignment, sText, textLenght);
        this.printText(iStyle, aligntext);
    }

    void endLine();

    // Close
    void endReceipt();

    // Transact
    @Override
    void openDrawer();

    /**
     * Enum for printer font sizes.
     */
    enum FontSize {
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
                    return NORMAL;
            }
        }

        public static int getLineMultiplier(int iSize) {
            return FontSize.fromInt(iSize).getLineMultiplier();
        }
    }
}
