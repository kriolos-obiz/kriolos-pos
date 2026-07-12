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

import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.printer.DevicePrinter;
import com.openbravo.pos.printer.TicketPrinterException;
import java.awt.image.BufferedImage;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;

/**
 *
 * @author JG uniCenta
 */
public class DevicePrinterESCPOS implements DevicePrinter {
    private static final Logger LOGGER = Logger.getLogger(DevicePrinterESCPOS.class.getName());
    private PrinterWritter m_PrinterWriter;
    private Codes m_codes;
    private UnicodeTranslator m_trans;

//    private boolean m_bInline;
    private String m_sName;

    // Creates new TicketPrinter
    /**
     *
     * @param printerWriter
     * @param codes
     * @param trans
     * @throws TicketPrinterException
     */
    public DevicePrinterESCPOS(PrinterWritter printerWriter, Codes codes, UnicodeTranslator trans) throws TicketPrinterException {

        m_sName = AppLocal.getIntString("printer.serial");
        m_PrinterWriter = printerWriter;
        m_codes = codes;
        m_trans = trans;

        // Inicializamos la impresora
        m_PrinterWriter.init(ESCPOS.INIT);

        m_PrinterWriter.write(ESCPOS.SELECT_PRINTER); // A la impresora
        m_PrinterWriter.init(m_codes.getInitSequence());
        m_PrinterWriter.write(m_trans.getCodeTable());

        m_PrinterWriter.flush();
    }

    /**
     *
     * @return
     */
    @Override
    public String getPrinterName() {
        return m_sName;
    }

    /**
     *
     * @return
     */
    @Override
    public String getPrinterDescription() {
        return null;
    }

    /**
     *
     * @return
     */
    @Override
    public JComponent getPrinterComponent() {
        return null;
    }

    /**
     *
     */
    @Override
    public void reset() {
    }

    /**
     *
     */
    @Override
    public void beginReceipt() {
    }

    /**
     *
     * @param image
     */
    @Override
    public void printImage(BufferedImage image) {

        m_PrinterWriter.write(ESCPOS.SELECT_PRINTER);
        m_PrinterWriter.write(m_codes.transImage(image));
    }

    /**
     *
     */
    @Override
    public void printLogo() {
        m_PrinterWriter.write(ESCPOS.SELECT_PRINTER);
        m_PrinterWriter.write(m_codes.getImageLogo());
    }

    /**
     *
     * @param type
     * @param position
     * @param code
     */
    @Override
    public void printBarCode(String type, String position, String code) {

        m_PrinterWriter.write(ESCPOS.SELECT_PRINTER);
        m_codes.printBarcode(m_PrinterWriter, type, position, code);
    }

    @Override
    public void printQRCode(String code, int size, char errorCorrection) {
        LOGGER.log(Level.WARNING, "NOT IMPLEMENTED:  printQRCode");
    }

    /**
     *
     * @param iTextSize
     */
    @Override
    public void beginLine(int iTextSize) {

        m_PrinterWriter.write(ESCPOS.SELECT_PRINTER);

        if (iTextSize == DevicePrinter.SIZE_0) {
            m_PrinterWriter.write(m_codes.getSize0());
        } else if (iTextSize == DevicePrinter.SIZE_1) {
            m_PrinterWriter.write(m_codes.getSize1());
        } else if (iTextSize == DevicePrinter.SIZE_2) {
            m_PrinterWriter.write(m_codes.getSize2());
        } else if (iTextSize == DevicePrinter.SIZE_3) {
            m_PrinterWriter.write(m_codes.getSize3());
        } else {
            m_PrinterWriter.write(m_codes.getSize0());
        }
    }

    /**
     *
     * @param iStyle
     * @param sText
     */
    @Override
    public void printText(int iStyle, String sText) {

        m_PrinterWriter.write(ESCPOS.SELECT_PRINTER);

        if ((iStyle & DevicePrinter.STYLE_BOLD) != 0) {
            m_PrinterWriter.write(m_codes.getBoldSet());
        }
        if ((iStyle & DevicePrinter.STYLE_UNDERLINE) != 0) {
            m_PrinterWriter.write(m_codes.getUnderlineSet());
        }
        m_PrinterWriter.write(m_trans.transString(sText));
        if ((iStyle & DevicePrinter.STYLE_UNDERLINE) != 0) {
            m_PrinterWriter.write(m_codes.getUnderlineReset());
        }
        if ((iStyle & DevicePrinter.STYLE_BOLD) != 0) {
            m_PrinterWriter.write(m_codes.getBoldReset());
        }
    }

    /**
     *
     */
    @Override
    public void endLine() {
        m_PrinterWriter.write(ESCPOS.SELECT_PRINTER);
        m_PrinterWriter.write(m_codes.getNewLine());
    }

    /**
     *
     */
    @Override
    public void endReceipt() {
        m_PrinterWriter.write(ESCPOS.SELECT_PRINTER);

        m_PrinterWriter.write(m_codes.getNewLine());
        m_PrinterWriter.write(m_codes.getNewLine());
        m_PrinterWriter.write(m_codes.getNewLine());
        m_PrinterWriter.write(m_codes.getNewLine());
        m_PrinterWriter.write(m_codes.getNewLine());

        m_PrinterWriter.write(m_codes.getCutReceipt());
        m_PrinterWriter.flush();
    }

    /**
     *
     */
    @Override
    public void openDrawer() {

        m_PrinterWriter.write(ESCPOS.SELECT_PRINTER);
        m_PrinterWriter.write(m_codes.getOpenDrawer());
        m_PrinterWriter.flush();
    }
}
