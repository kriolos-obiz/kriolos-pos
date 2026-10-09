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

import com.openbravo.pos.spi.hardware.display.DisplayConfig;
import com.openbravo.pos.spi.hardware.display.DisplayDevice;
import com.openbravo.pos.spi.hardware.display.DisplayProtocol;
import com.openbravo.pos.spi.hardware.printer.FiscalPrinterDevice;
import com.openbravo.pos.spi.hardware.printer.PrinterConfig;
import com.openbravo.pos.spi.hardware.printer.PrinterDevice;
import com.openbravo.pos.spi.hardware.printer.PrinterProtocol;
import com.openbravo.pos.spi.hardware.scale.ScaleConfig;
import com.openbravo.pos.spi.hardware.scale.ScaleDevice;
import com.openbravo.pos.spi.hardware.scale.ScaleException;
import com.openbravo.pos.spi.hardware.scale.ScaleProtocol;
import com.openbravo.pos.spi.hardware.scanner.ScannerConfig;
import com.openbravo.pos.spi.hardware.scanner.ScannerDevice;
import com.openbravo.pos.spi.hardware.scanner.ScannerProtocol;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PeripheralManagerTest {

    @Test
    @DisplayName("DeviceType should resolve standard codes, selectors, and URNs")
    void testDeviceTypeResolution() {
        assertEquals(DeviceType.SCALE, DeviceType.fromCode("scale"));
        assertEquals(DeviceType.SCALE, DeviceType.fromCode("SCALE"));
        assertEquals(DeviceType.SCALE, DeviceType.fromCode("hardware:scale"));
        assertEquals(DeviceType.SCALE, DeviceType.fromCode("device:scale"));
        assertEquals(DeviceType.SCALE, DeviceType.fromCode("urn:kriolos:hardware:scale"));

        assertEquals(DeviceType.PRINTER, DeviceType.fromCode("printer"));
        assertEquals(DeviceType.PRINTER, DeviceType.fromCode("hardware:printer"));
        assertEquals(DeviceType.PRINTER, DeviceType.fromCode("urn:kriolos:hardware:printer"));

        assertEquals(DeviceType.DISPLAY, DeviceType.fromCode("display"));
        assertEquals(DeviceType.DISPLAY, DeviceType.fromCode("hardware:display"));
        assertEquals(DeviceType.DISPLAY, DeviceType.fromCode("urn:kriolos:hardware:display"));

        assertEquals(DeviceType.SCANNER, DeviceType.fromCode("scanner"));
        assertEquals(DeviceType.SCANNER, DeviceType.fromCode("hardware:scanner"));
        assertEquals(DeviceType.SCANNER, DeviceType.fromCode("urn:kriolos:hardware:scanner"));

        assertEquals(DeviceType.FISCAL_PRINTER, DeviceType.fromCode("fiscal_printer"));
        assertEquals(DeviceType.FISCAL_PRINTER, DeviceType.fromCode("hardware:fiscal_printer"));

        assertNull(DeviceType.fromCode("unknown_device_type"));
        assertNull(DeviceType.fromCode(""));
        assertNull(DeviceType.fromCode(null));
    }

    @Test
    @DisplayName("ConnectorType should resolve standard codes, selectors, schemes, and transport aliases")
    void testConnectorTypeResolution() {
        assertEquals(ConnectorType.SERIAL, ConnectorType.fromCode("serial"));
        assertEquals(ConnectorType.SERIAL, ConnectorType.fromCode("rxtx"));
        assertEquals(ConnectorType.SERIAL, ConnectorType.fromCode("comm"));
        assertEquals(ConnectorType.SERIAL, ConnectorType.fromCode("rs232"));
        assertEquals(ConnectorType.SERIAL, ConnectorType.fromCode("tty"));
        assertEquals(ConnectorType.SERIAL, ConnectorType.fromCode("connector:serial"));
        assertEquals(ConnectorType.SERIAL, ConnectorType.fromCode("urn:kriolos:hardware:connector:serial"));

        assertEquals(ConnectorType.NETWORK, ConnectorType.fromCode("network"));
        assertEquals(ConnectorType.NETWORK, ConnectorType.fromCode("tcp"));
        assertEquals(ConnectorType.NETWORK, ConnectorType.fromCode("socket"));
        assertEquals(ConnectorType.NETWORK, ConnectorType.fromCode("ethernet"));
        assertEquals(ConnectorType.NETWORK, ConnectorType.fromCode("ip"));

        assertEquals(ConnectorType.USB, ConnectorType.fromCode("usb"));
        assertEquals(ConnectorType.USB, ConnectorType.fromCode("hid"));
        assertEquals(ConnectorType.FILE, ConnectorType.fromCode("file"));
        assertEquals(ConnectorType.PIPE, ConnectorType.fromCode("pipe"));
        assertEquals(ConnectorType.JAVAPOS, ConnectorType.fromCode("javapos"));
        assertEquals(ConnectorType.SYSTEM, ConnectorType.fromCode("system"));
        assertEquals(ConnectorType.SCREEN, ConnectorType.fromCode("screen"));
        assertEquals(ConnectorType.NONE, ConnectorType.fromCode("none"));
        assertEquals(ConnectorType.NONE, ConnectorType.fromCode("unknown"));
    }

    @Test
    @DisplayName("ScaleProtocol should resolve known protocols and default to NONE")
    void testScaleProtocols() {
        assertEquals(ScaleProtocol.CAS_PDII, ScaleProtocol.fromToken("caspdii"));
        assertEquals(ScaleProtocol.ACOM_PC100, ScaleProtocol.fromToken("acompc100"));
        assertEquals(ScaleProtocol.AVERY_BERKEL_6720, ScaleProtocol.fromToken("averyberkel6720"));
        assertEquals(ScaleProtocol.CASIO_PD1, ScaleProtocol.fromToken("casiopd1"));
        assertEquals(ScaleProtocol.DIALOG_1, ScaleProtocol.fromToken("dialog1"));
        assertEquals(ScaleProtocol.MT_IND221, ScaleProtocol.fromToken("mtind221"));
        assertEquals(ScaleProtocol.SAMSUNG_ESP, ScaleProtocol.fromToken("samsungesp"));
        assertEquals(ScaleProtocol.JAVAPOS, ScaleProtocol.fromToken("javapos"));
        assertEquals(ScaleProtocol.FAKE, ScaleProtocol.fromToken("fake"));
        assertEquals(ScaleProtocol.NONE, ScaleProtocol.fromToken("unknown"));
        assertEquals(ScaleProtocol.NONE, ScaleProtocol.fromToken(null));
    }

    @Test
    @DisplayName("PrinterConfig and DisplayConfig parse strict syntax without legacy normalization")
    void testStrictConfigParsing() {
        // Printer configs
        PrinterConfig screenPrinter = PrinterConfig.parse("screen");
        assertEquals(PrinterProtocol.SCREEN, screenPrinter.protocol());
        assertEquals(ConnectorType.SCREEN, screenPrinter.connector());
        assertEquals("", screenPrinter.target());

        PrinterConfig serialPrinter = PrinterConfig.parse("epson:serial,/dev/ttyUSB0");
        assertEquals(PrinterProtocol.EPSON, serialPrinter.protocol());
        assertEquals(ConnectorType.SERIAL, serialPrinter.connector());
        assertEquals("/dev/ttyUSB0", serialPrinter.target());

        PrinterConfig netPrinter = PrinterConfig.parse("epson:tcp,192.168.1.50:9100");
        assertEquals(PrinterProtocol.EPSON, netPrinter.protocol());
        assertEquals(ConnectorType.NETWORK, netPrinter.connector());
        assertEquals("192.168.1.50:9100", netPrinter.target());

        // Without legacy normalization, "serial" in protocol position does NOT become EPSON
        PrinterConfig legacySerial = PrinterConfig.parse("serial:/dev/ttyS0");
        assertEquals(PrinterProtocol.NONE, legacySerial.protocol());

        // Display configs
        DisplayConfig screenDisplay = DisplayConfig.parse("screen");
        assertEquals(DisplayProtocol.SCREEN, screenDisplay.protocol());
        assertEquals(ConnectorType.SCREEN, screenDisplay.connector());

        DisplayConfig serialDisplay = DisplayConfig.parse("epson:serial,COM2,9600");
        assertEquals(DisplayProtocol.EPSON, serialDisplay.protocol());
        assertEquals(ConnectorType.SERIAL, serialDisplay.connector());
        assertEquals("COM2,9600", serialDisplay.target());
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
        PrinterDevice nullPrinter = PeripheralManager.getPrinter(new PrinterConfig(PrinterProtocol.NONE, ConnectorType.NONE, ""));
        assertNotNull(nullPrinter);
        assertFalse(nullPrinter.isConnected());
        assertDoesNotThrow(() -> nullPrinter.print(new byte[]{}));
        assertDoesNotThrow(nullPrinter::cutReceipt);
        assertDoesNotThrow(nullPrinter::openDrawer);

        // Displays
        DisplayDevice nullDisplay = PeripheralManager.getDisplay(new DisplayConfig(DisplayProtocol.NONE, ConnectorType.NONE, ""));
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

        // Fiscal Printers
        FiscalPrinterDevice nullFiscal = PeripheralManager.getFiscalPrinter(null);
        assertNotNull(nullFiscal);
        assertFalse(nullFiscal.isConnected());
        assertDoesNotThrow(nullFiscal::printZReport);
        assertDoesNotThrow(nullFiscal::printXReport);
    }

    @Test
    @DisplayName("PeripheralManager discovery returns available providers")
    void testPeripheralDiscovery() {
        var providers = PeripheralManager.getAllProviders();
        assertNotNull(providers);
    }
}
