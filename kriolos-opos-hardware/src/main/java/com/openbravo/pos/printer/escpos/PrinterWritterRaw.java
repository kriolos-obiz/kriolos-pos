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
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.DocAttributeSet;
import javax.print.attribute.HashDocAttributeSet;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.DocumentName;
import javax.print.attribute.standard.JobName;

public final class PrinterWritterRaw extends PrinterWritter {
    private static final Logger LOGGER = Logger.getLogger(PrinterWritterRaw.class.getName());
    
    // Replaced array concatenation with a stream to prevent memory reallocation leaks
    private final ByteArrayOutputStream m_printBuffer;
    private PrintService printService;
    private final DocFlavor DOC_Flavor = DocFlavor.BYTE_ARRAY.AUTOSENSE;

    private final static String DOC_NAME = "Ticket";
    private final String printerName;

    public PrinterWritterRaw(String printerName) {
        this.printerName = printerName;
        this.m_printBuffer = new ByteArrayOutputStream();

        // Look up the requested print service first
        PrintService[] services = PrintServiceLookup.lookupPrintServices(DOC_Flavor, null);
        for (PrintService ps : services) {
            if (ps.getName().contains(printerName)) {
                printService = ps;
                break;
            }
        }

        // Initialize the printer protocol only if the hardware device is located
        if (printService != null) {
            init();
            write(ESCPOS.INIT);
        } else {
            LOGGER.log(Level.SEVERE, "Printer service not found for device name: {0}", printerName);
        }
    }

    public void init() {
        write(ESCPOS.SELECT_PRINTER);
        write(new UnicodeTranslatorInt().getCodeTable());
    }

    @Override
    public synchronized void write(byte[] data) {
        if (data != null) {
            m_printBuffer.write(data, 0, data.length);
        }
    }

    @Override
    public synchronized void write(String sValue) {
        if (sValue != null) {
            // Explicitly map fallback charset to ISO-8859-1 for standard POS character tables
            byte[] bytes = sValue.getBytes(StandardCharsets.ISO_8859_1);
            write(bytes);
        }
    }

    @Override
    protected void internalWrite(byte[] data) {}

    @Override
    protected void internalClose() {}

    @Override
    protected void internalFlush() {}

    @Override
    public void flush() {
        printJob();
    }  

    private synchronized void printJob() {
        if (null == printService) {
            m_printBuffer.reset(); // Clear memory buffer safely even if target printer is offline
            return;
        }

        try {
            byte[] dataToSend = m_printBuffer.toByteArray();
            if (dataToSend.length == 0) return;

            DocPrintJob pj = printService.createPrintJob();
            DocAttributeSet docattributes = new HashDocAttributeSet();
            docattributes.add(new DocumentName(DOC_NAME, Locale.getDefault()));
            
            PrintRequestAttributeSet jobattributes = new HashPrintRequestAttributeSet();
            jobattributes.add(new JobName(AppLocal.APP_NAME, Locale.getDefault()));
            
            Doc doc = new SimpleDoc(dataToSend, DOC_Flavor, docattributes);
            pj.print(doc, jobattributes);
            
        } catch (PrintException ex) {
            LOGGER.log(Level.WARNING, "Exception occurred during print job execution: ", ex);
        } finally {
            m_printBuffer.reset(); // Flush memory safely to prevent duplicate outputs on the next ticket
        }
    }
}
