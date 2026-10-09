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
package com.openbravo.pos.hardware;

import com.openbravo.pos.scale.ScaleFake;
import com.openbravo.pos.spi.hardware.ConnectorType;
import com.openbravo.pos.spi.hardware.DeviceType;
import com.openbravo.pos.spi.hardware.HardwareException;
import com.openbravo.pos.spi.hardware.PeripheralManager;
import com.openbravo.pos.spi.hardware.PeripheralProvider;
import com.openbravo.pos.spi.hardware.display.*;
import com.openbravo.pos.spi.hardware.printer.*;
import com.openbravo.pos.spi.hardware.scale.*;
import com.openbravo.pos.spi.hardware.scanner.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DefaultPeripheralProvider SPI Test Suite")
class DefaultPeripheralProviderTest {

    private DefaultPeripheralProvider provider;

    @BeforeEach
    void setUp() {
        provider = new DefaultPeripheralProvider();
    }

    @Test
    @DisplayName("Provider identity and metadata")
    void testProviderIdentity() {
        assertEquals("kriolos:hardware:default", provider.getProviderId());
        assertEquals("Default Hardware Peripheral Provider", provider.getProviderName());
        assertTrue(provider.supports(DeviceType.SCALE));
        assertTrue(provider.supports(DeviceType.PRINTER));
        assertTrue(provider.supports(DeviceType.DISPLAY));
        assertTrue(provider.supports(DeviceType.SCANNER));
        assertTrue(provider.supports(DeviceType.FISCAL_PRINTER));
    }

    @Test
    @DisplayName("Scale resolution via provider")
    void testScaleResolution() throws HardwareException {
        ScaleDevice fakeScale = provider.getScale(new ScaleConfig(ScaleProtocol.FAKE, ""));
        assertNotNull(fakeScale);
        assertTrue(fakeScale instanceof ScaleFake);
        assertEquals(ScaleProtocol.FAKE, fakeScale.getProtocol());

        ScaleDevice nullScale = provider.getScale(new ScaleConfig(ScaleProtocol.NONE, ""));
        assertNotNull(nullScale);
        assertFalse(nullScale.isConnected());
        assertEquals(ScaleProtocol.NONE, nullScale.getProtocol());
    }

    @Test
    @DisplayName("Printer resolution via provider")
    void testPrinterResolution() throws HardwareException {
        PrinterDevice screenPrinter = provider.getPrinter(new PrinterConfig(PrinterProtocol.SCREEN, ConnectorType.SCREEN, ""));
        assertNotNull(screenPrinter);
        assertTrue(screenPrinter.isConnected());

        PrinterDevice nullPrinter = provider.getPrinter(new PrinterConfig(PrinterProtocol.NONE, ConnectorType.NONE, ""));
        assertNotNull(nullPrinter);
        assertFalse(nullPrinter.isConnected());
    }

    @Test
    @DisplayName("Display resolution via provider")
    void testDisplayResolution() throws HardwareException {
        DisplayDevice screenDisplay = provider.getDisplay(new DisplayConfig(DisplayProtocol.SCREEN, ConnectorType.SCREEN, ""));
        assertNotNull(screenDisplay);
        assertTrue(screenDisplay.isConnected());

        DisplayDevice nullDisplay = provider.getDisplay(new DisplayConfig(DisplayProtocol.NONE, ConnectorType.NONE, ""));
        assertNotNull(nullDisplay);
        assertFalse(nullDisplay.isConnected());
    }

    @Test
    @DisplayName("Scanner resolution via provider")
    void testScannerResolution() throws HardwareException {
        ScannerDevice nullScanner = provider.getScanner(new ScannerConfig(ScannerProtocol.NONE, ""));
        assertNotNull(nullScanner);
        assertFalse(nullScanner.isConnected());
    }

    @Test
    @DisplayName("PeripheralManager auto-discovers DefaultPeripheralProvider via SPI")
    void testPeripheralManagerDiscovery() {
        PeripheralManager.reload();
        var providers = PeripheralManager.getAllProviders();
        assertFalse(providers.isEmpty(), "PeripheralManager should discover at least DefaultPeripheralProvider");
        boolean foundDefault = providers.stream()
                .anyMatch(p -> "kriolos:hardware:default".equals(p.getProviderId()));
        assertTrue(foundDefault, "DefaultPeripheralProvider must be discovered via ServiceLoader");
    }
}
