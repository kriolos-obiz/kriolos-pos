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
package com.openbravo.pos.printer.pdf;

import com.openbravo.pos.spi.hardware.printer.DevicePrinter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class PdfPrinterTest {

    @Test
    void testPdf80mmGeneration(@TempDir File tempDir) throws IOException {
        PdfPrinter80mm printer = new PdfPrinter80mm("Test 80mm", tempDir);

        printer.beginReceipt();
        printer.beginLine(0);
        printer.printText(0, "========================================");
        printer.endLine();
        printer.beginLine(0);
        printer.printText(0, "Test Item                       10.00");
        printer.endLine();
        printer.endReceipt();

        // Check if file was saved
        File[] files = tempDir.listFiles((dir, name) -> name.endsWith(".pdf"));
        assertNotNull(files);
        assertTrue(files.length > 0);
        assertTrue(files[0].length() > 0);
    }

    public void printSaleTransaction() {
        File exportDir = new File(System.getProperty("user.home"), "kriolos/receipts");
        DevicePrinter printer = new PdfPrinter58mm("Counter Receipt", exportDir);

        // 1. Begin transaction
        printer.beginReceipt();

        // 2. Header
        printer.beginLine(0);
        printer.printText(DevicePrinter.STYLE_BOLD, "KriolOS POINT OF SALE");
        printer.endLine();

        // 3. Receipt body / line items
        printer.beginLine(0);
        printer.printText(DevicePrinter.STYLE_PLAIN, "1x Coffee                     100$00");
        printer.endLine();

        printer.beginLine(0);
        printer.printText(DevicePrinter.STYLE_PLAIN, "1x Pastel de Nata             150$00");
        printer.endLine();

        printer.beginLine(0);
        printer.printText(DevicePrinter.STYLE_PLAIN, "1x Coffee                     100$00");
        printer.endLine();

        printer.beginLine(0);
        printer.printText(DevicePrinter.STYLE_PLAIN, "1x Pastel de Nata             150$00");
        printer.endLine();

        printer.beginLine(0);
        printer.printText(DevicePrinter.STYLE_PLAIN, "1x Coffee                     100$00");
        printer.endLine();

        printer.beginLine(0);
        printer.printText(DevicePrinter.STYLE_PLAIN, "1x Pastel de Nata             150$00");
        printer.endLine();

        printer.beginLine(0);
        printer.printText(DevicePrinter.STYLE_PLAIN, "1x Coffee                     100$00");
        printer.endLine();

        printer.beginLine(0);
        printer.printText(DevicePrinter.STYLE_PLAIN, "1x Pastel de Nata             150$00");
        printer.endLine();

        // 4. Barcode / QR Code
        printer.printBarCode(DevicePrinter.BARCODE_CODE128, DevicePrinter.POSITION_BOTTOM, "TX-998877");
        printer.printQRCode("https://kriolos.org/verify/998877", 50, 'M');

        // 5. Commit & compile PDF to disk
        printer.endReceipt();
    }

    public byte[] createReceiptBytes() throws IOException {
        // Pass null for outputDirectory so no local file is saved to disk
        PdfPrinter80mm memoryPrinter = new PdfPrinter80mm("EmailReceipt", null);

        memoryPrinter.beginReceipt();
        memoryPrinter.beginLine(0);
        memoryPrinter.printText(0, "KriolOS POINT OF SALE - DIGITAL RECEIPT");
        memoryPrinter.endLine();
        memoryPrinter.beginLine(0);
        memoryPrinter.printText(0, "TOTAL: 250$00");
        memoryPrinter.endLine();

        // Obtain the compiled PDF binary data directly
        return memoryPrinter.generatePdfDocument();
    }

    public static void main(String[] args) {

        new PdfPrinterTest().printSaleTransaction();
    }
}
