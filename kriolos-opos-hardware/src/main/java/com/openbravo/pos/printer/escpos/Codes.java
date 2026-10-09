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
package com.openbravo.pos.printer.escpos;

import com.openbravo.pos.printer.DevicePrinter;
import com.openbravo.pos.printer.PrinterTextUtils;
import java.awt.image.BufferedImage;

/**
 *
 * @author JG uniCenta
 */
public abstract class Codes {

    /** Creates a new instance of Codes */
    public Codes() {
    }

    /**
     *
     * @return
     */
    public abstract byte[] getInitSequence();
    
    /**
     *
     * @return
     */
    public abstract byte[] getSize0();

    /**
     *
     * @return
     */
    public abstract byte[] getSize1();

    /**
     *
     * @return
     */
    public abstract byte[] getSize2();

    /**
     *
     * @return
     */
    public abstract byte[] getSize3();

    /**
     *
     * @return
     */
    public abstract byte[] getBoldSet();

    /**
     *
     * @return
     */
    public abstract byte[] getBoldReset();

    /**
     *
     * @return
     */
    public abstract byte[] getUnderlineSet();

    /**
     *
     * @return
     */
    public abstract byte[] getUnderlineReset();
    
    /**
     *
     * @return
     */
    public abstract byte[] getOpenDrawer();    

    /**
     *
     * @return
     */
    public abstract byte[] getCutReceipt();   

    /**
     *
     * @return
     */
    public abstract byte[] getNewLine();    

    /**
     *
     * @return
     */
    public abstract byte[] getImageHeader();

    /**
     *
     * @return
     */
    public abstract int getImageWidth();

    /**
     *
     * @return
     */
    public abstract byte[] getImageLogo();

    /**
     *
     * @param out
     * @param type
     * @param position
     * @param code
     */
    public void printBarcode(PrinterWritter out, String type, String position, String code) {

        if (DevicePrinter.BARCODE_EAN13.equals(type)) {

            out.write(getNewLine());

            out.write(ESCPOS.BAR_HEIGHT);
            if (DevicePrinter.POSITION_NONE.equals(position)) {
                out.write(ESCPOS.BAR_POSITIONNONE);
            } else {
                out.write(ESCPOS.BAR_POSITIONDOWN);
            }
            out.write(ESCPOS.BAR_HRIFONT1);
            out.write(ESCPOS.BAR_CODE02);
            out.write(PrinterTextUtils.transNumber(PrinterTextUtils.alignBarcode(code,13).substring(0,12)));
            out.write(new byte[] { 0x00 });

            out.write(getNewLine());
        }
    }
   
    /**
     *
     * @param image
     * @return
     */
    public byte[] transImage(BufferedImage image) {
        
            CenteredImage centeredimage = new CenteredImage(image, getImageWidth());

        // Imprimo los par\u00e1metros en cu\u00e1druple
        int iWidth = (centeredimage.getWidth() + 7) / 8; // n\u00famero de bytes
        int iHeight = centeredimage.getHeight();
        
        // Array de datos
        byte[] bData = new byte[getImageHeader().length + 4 + iWidth * iHeight];
        
        // Comando de impresion de imagen
        System.arraycopy(getImageHeader(), 0, bData, 0, getImageHeader().length);
        
        int index = getImageHeader().length;
        
        // Dimension de la imagen
        // JG note: nested ++'s not good construct need change later
        bData[index ++] = (byte) (iWidth % 256);
        bData[index ++] = (byte) (iWidth / 256);
        bData[index ++] = (byte) (iHeight % 256);
        bData[index ++] = (byte) (iHeight / 256);       
        
        // Raw data
        // JG note: nested ++'s  and var assignments not good construct need change later
        int iRGB;
        int p;
        for (int i = 0; i < centeredimage.getHeight(); i++) {
            for (int j = 0; j < centeredimage.getWidth(); j = j + 8) {
                p = 0x00;
                for (int d = 0; d < 8; d ++) {
                    p = p << 1;
                    if (centeredimage.isBlack(j + d, i)) {
                        p = p | 0x01;
                    }
                }
                
                bData[index ++] = (byte) p;
            }
        }        
        return bData;
    }

    /**
     *
     */
    protected class CenteredImage {

        private BufferedImage image;
        private int width;

        /**
         *
         * @param image
         * @param width
         */
        public CenteredImage(BufferedImage image, int width) {
            this.image = image;
            this.width = width;
        }

        /**
         *
         * @return
         */
        public int getHeight() {
            return image.getHeight();
        }

        /**
         *
         * @return
         */
        public int getWidth() {
            return width;
        }

        /**
         *
         * @param x
         * @param y
         * @return
         */
        public boolean isBlack(int x, int y) {

            int centeredx = x + (image.getWidth() - width) / 2;
            if (centeredx < 0 || centeredx >= image.getWidth() || y < 0 || y >= image.getHeight()) {
                return false;
            } else {
                int rgb = image.getRGB(centeredx, y);

                int gray = (int)(0.30 * ((rgb >> 16) & 0xff) +
                                 0.59 * ((rgb >> 8) & 0xff) +
                                 0.11 * (rgb & 0xff));

                return gray < 128;
            }
        }
    }
}
