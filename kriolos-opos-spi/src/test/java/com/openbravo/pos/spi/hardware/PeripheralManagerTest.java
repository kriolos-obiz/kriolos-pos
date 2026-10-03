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
package com.openbravo.pos.spi.hardware;

import com.openbravo.pos.spi.hardware.printer.*;
import com.openbravo.pos.spi.hardware.scale.*;
import com.openbravo.pos.spi.hardware.scanner.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests verifying the SPI contracts, configuration parsers, DeviceType URNs,
 * and PeripheralManager fallback behaviors.
 */
public class PeripheralManagerTest {

    @Test
    @DisplayName("DeviceType should provide valid URN, short code, and bidirectional parsing")
    void testDeviceTypeUrnAndParsing() {
        assertEquals("scale", DeviceType.SCALE.getCode());
        assertEquals("urn:kriolos:device:scale", DeviceType.SCALE.getUrn());
        assertEquals("device:scale", DeviceType.SCALE.getSelector());

        // Test parsing via short code, enum name, selector, and full URN
        assertEquals(DeviceType.SCALE, DeviceType.fromCode("scale"));
        assertEquals(DeviceType.SCALE, DeviceType.fromCode("SCALE"));
        assertEquals(DeviceType.SCALE, DeviceType.fromCode("device:scale"));
        assertEquals(DeviceType.SCALE, DeviceType.fromCode("urn:kriolos:device:scale"));

        assertEquals(DeviceType.PRINTER, DeviceType.fromCode("printer"));
        assertEquals(DeviceType.PRINTER, DeviceType.fromCode("urn:kriolos:device:printer"));

        assertEquals(DeviceType.DISPLAY, DeviceType.fromCode("display"));
        assertEquals(DeviceType.SCANNER, DeviceType.fromCode("scanner"));
        assertEquals(DeviceType.FISCAL_PRINTER, DeviceType.fromCode("fiscal_printer"));
        assertEquals(DeviceType.CASH_DRAWER, DeviceType.fromCode("cash_drawer"));

        assertNull(DeviceType.fromCode(null));
        assertNull(DeviceType.fromCode("unknown_device"));
    }

    @Test
    @DisplayName("ScaleProtocol and ScaleConfig parsing should extract valid protocols and ports")
    void testScaleParsing() {
        assertEquals(ScaleProtocol.CAS_PDII, ScaleProtocol.fromToken("caspdii"));
        assertEquals(ScaleProtocol.ACOM_PC100, ScaleProtocol.fromToken("acompc100"));
        assertEquals(ScaleProtocol.FAKE, ScaleProtocol.fromToken("fake"));
        assertEquals(ScaleProtocol.NONE, ScaleProtocol.fromToken("nonexistent"));

        ScaleConfig c1 = ScaleConfig.parse("caspdii:/dev/ttyS0,9600");
        assertEquals(ScaleProtocol.CAS_PDII, c1.protocol());
        assertEquals("/dev/ttyS0", c1.port());
        assertEquals("9600", c1.properties().get("param2"));

        ScaleConfig cFake = ScaleConfig.parse("fake");
        assertEquals(ScaleProtocol.FAKE, cFake.protocol());
        assertEquals("", cFake.port());

        ScaleConfig cNull = ScaleConfig.parse(null);
        assertEquals(ScaleProtocol.NONE, cNull.protocol());
    }

    @Test
    @DisplayName("PrinterProtocol and PrinterConfig parsing should resolve aliases and parameters")
    void testPrinterParsing() {
        assertEquals(PrinterProtocol.EPSON, PrinterProtocol.fromToken("epson"));
        // Serial aliases
        assertEquals(PrinterProtocol.EPSON, PrinterProtocol.fromToken("serial"));
        assertEquals(PrinterProtocol.EPSON, PrinterProtocol.fromToken("rxtx"));
        assertEquals(PrinterProtocol.SCREEN, PrinterProtocol.fromToken("screen"));

        PrinterConfig cEpson = PrinterConfig.parse("epson:COM1,9600");
        assertEquals(PrinterProtocol.EPSON, cEpson.protocol());
        assertEquals("COM1", cEpson.param1());
        assertEquals("9600", cEpson.param2());

        // Serial alias normalization: "serial:/dev/ttyS0,9600" -> protocol EPSON, param1=serial, param2=/dev/ttyS0
        PrinterConfig cSerial = PrinterConfig.parse("serial:/dev/ttyUSB0");
        assertEquals(PrinterProtocol.EPSON, cSerial.protocol());
        assertEquals("serial", cSerial.param1());
        assertEquals("/dev/ttyUSB0", cSerial.param2());
    }

    @Test
    @DisplayName("DisplayProtocol and DisplayConfig parsing should recognize visor types")
    void testDisplayParsing() {
        assertEquals(DisplayProtocol.LED8, DisplayProtocol.fromToken("led8"));
        assertEquals(DisplayProtocol.PDLED8, DisplayProtocol.fromToken("pdled8"));
        assertEquals(DisplayProtocol.WINDOW, DisplayProtocol.fromToken("window"));

        DisplayConfig cLed8 = DisplayConfig.parse("led8:/dev/ttyUSB0,2400");
        assertEquals(DisplayProtocol.LED8, cLed8.protocol());
        assertEquals("/dev/ttyUSB0", cLed8.param1());
        assertEquals("2400", cLed8.param2());
    }

    @Test
    @DisplayName("ScannerProtocol and ScannerConfig parsing should recognize scanner types")
    void testScannerParsing() {
        assertEquals(ScannerProtocol.SCANPAL2, ScannerProtocol.fromToken("scanpal2"));
        assertEquals(ScannerProtocol.NONE, ScannerProtocol.fromToken("unknown"));

        ScannerConfig c = ScannerConfig.parse("scanpal2:COM3");
        assertEquals(ScannerProtocol.SCANPAL2, c.protocol());
        assertEquals("COM3", c.port());
    }

    @Test
    @DisplayName("PeripheralManager should provide safe Null-Object fallbacks when unconfigured")
    void testPeripheralManagerFallbacks() throws Exception {
        // Scales
        ScaleDevice nullScale = PeripheralManager.getScale(new ScaleConfig(ScaleProtocol.NONE, ""));
        assertNotNull(nullScale);
        assertFalse(nullScale.isConnected());
        assertThrows(ScaleException.class, nullScale::readWeight);

        // Printers
        PrinterDevice nullPrinter = PeripheralManager.getPrinter(new PrinterConfig(PrinterProtocol.NONE, "", ""));
        assertNotNull(nullPrinter);
        assertFalse(nullPrinter.isConnected());
        assertDoesNotThrow(() -> nullPrinter.print(new byte[]{}));
        assertDoesNotThrow(nullPrinter::cutReceipt);
        assertDoesNotThrow(nullPrinter::openDrawer);

        // Displays
        DisplayDevice nullDisplay = PeripheralManager.getDisplay(new DisplayConfig(DisplayProtocol.NONE, "", ""));
        assertNotNull(nullDisplay);
        assertFalse(nullDisplay.isConnected());
        assertDoesNotThrow(nullDisplay::clearVisor);
        assertDoesNotThrow(() -> nullDisplay.writeVisor("Line 1", "Line 2"));

        // Scanners
        ScannerDevice nullScanner = PeripheralManager.getScanner(new ScannerConfig(ScannerProtocol.NONE, ""));
        assertNotNull(nullScanner);
        assertFalse(nullScanner.isConnected());
        assertDoesNotThrow(nullScanner::start);
        assertDoesNotThrow(nullScanner::stop);

        // Fiscal Printer
        FiscalPrinterDevice nullFiscal = PeripheralManager.getFiscalPrinter(new FiscalPrinterConfig("none", ""));
        assertNotNull(nullFiscal);
        assertFalse(nullFiscal.isConnected());
        assertDoesNotThrow(() -> nullFiscal.printFiscalReceipt(null));

        // Unknown device
        assertTrue(PeripheralManager.getDevice(HardwareDevice.class, null).isEmpty());

        // Reload
        assertDoesNotThrow(PeripheralManager::reload);
    }
}
