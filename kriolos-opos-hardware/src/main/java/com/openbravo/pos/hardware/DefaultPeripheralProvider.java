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

import com.google.auto.service.AutoService;
import com.openbravo.pos.printer.*;
import com.openbravo.pos.printer.escpos.*;
import com.openbravo.pos.printer.javapos.DeviceDisplayJavaPOS;
import com.openbravo.pos.printer.javapos.DeviceFiscalPrinterJavaPOS;
import com.openbravo.pos.printer.javapos.DevicePrinterJavaPOS;
import com.openbravo.pos.printer.printer.DevicePrinterPrinter;
import com.openbravo.pos.printer.screen.DeviceDisplayPanel;
import com.openbravo.pos.printer.screen.DeviceDisplayWindow;
import com.openbravo.pos.printer.screen.DeviceDisplayWindowDualScreen;
import com.openbravo.pos.printer.screen.DevicePrinterPanel;
import com.openbravo.pos.scale.*;
import com.openbravo.pos.scale.javapos.ScaleJavaPOS;
import com.openbravo.pos.scanpal2.DeviceScannerComm;
import com.openbravo.pos.spi.annotation.PluginMetadata;
import com.openbravo.pos.spi.hardware.HardwareException;
import com.openbravo.pos.spi.hardware.PeripheralProvider;
import com.openbravo.pos.display.led8.DeviceDisplayLED8;
import com.openbravo.pos.display.led8.DeviceDisplayPDLED8;
import com.openbravo.pos.spi.hardware.display.*;
import com.openbravo.pos.spi.hardware.printer.*;
import com.openbravo.pos.spi.hardware.scale.*;
import com.openbravo.pos.spi.hardware.scanner.*;
import com.openbravo.pos.spi.provider.ConfigurableProvider;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Standard default hardware peripheral provider delivering built-in drivers for
 * scales, printers, customer displays, scanners, and fiscal devices.
 *
 * @author KriolOS Team
 * @since 1.0.0
 */
@AutoService(PeripheralProvider.class)
@PluginMetadata(
        id = "kriolos:hardware:default",
        service = PeripheralProvider.class,
        selectors = {
                "hardware:scale",
                "hardware:printer",
                "hardware:display",
                "hardware:scanner",
                "hardware:fiscal_printer"
        }
)
public class DefaultPeripheralProvider implements PeripheralProvider, ConfigurableProvider {

    private static final Logger LOGGER = Logger.getLogger(DefaultPeripheralProvider.class.getName());
    private static final String PROVIDER_ID = "kriolos:hardware:default";
    private static final String PROVIDER_NAME = "Default Hardware Peripheral Provider";

    private final PrinterWritterPool writterPool = new PrinterWritterPool();
    private final Map<String, String> currentConfig = new HashMap<>();

    @Override
    public String getProviderId() {
        return PROVIDER_ID;
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
    public ScaleDevice getScale(ScaleConfig config) throws HardwareException {
        if (config == null || config.protocol() == ScaleProtocol.NONE) {
            return new NullScaleDevice("Scale not configured");
        }
        try {
            switch (config.protocol()) {
                case CAS_PDII:
                    return new ScaleCASPDII(config.port());
                case ACOM_PC100:
                    return new ScaleAcomPC100(config.port());
                case AVERY_BERKEL_6720:
                    return new ScaleAvery(config.port());
                case CASIO_PD1:
                    return new ScaleCasioPD1(config.port());
                case DIALOG_1:
                    return new ScaleComm(config.port());
                case SAMSUNG_ESP:
                    return new ScaleSamsungEsp(config.port());
                case MT_IND221:
                    return new ScaleMTIND221(config.port());
                case FAKE:
                    return new ScaleFake();
                case SCREEN:
                    return new ScaleDialog(null);
                case JAVAPOS:
                    return new ScaleJavaPOS(config.port());
                default:
                    return new NullScaleDevice("Unsupported scale protocol: " + config.protocol());
            }
        } catch (Exception ex) {
            throw new HardwareException("Failed to initialize scale: " + ex.getMessage(), ex);
        }
    }

    @Override
    public PrinterDevice getPrinter(PrinterConfig config) throws HardwareException {
        if (config == null || config.protocol() == PrinterProtocol.NONE) {
            return new NullPrinterDevice("Printer not configured");
        }
        try {
            switch (config.protocol()) {
                case SCREEN:
                    return new DevicePrinterPanel();
                case EPSON:
                    return new DevicePrinterESCPOS(
                            writterPool.getPrinterWritter(config.connector(), config.target()),
                            new CodesEpson(), new UnicodeTranslatorInt());
                case TMU220:
                    return new DevicePrinterESCPOS(
                            writterPool.getPrinterWritter(config.connector(), config.target()),
                            new CodesTMU220(), new UnicodeTranslatorInt());
                case STAR:
                    return new DevicePrinterESCPOS(
                            writterPool.getPrinterWritter(config.connector(), config.target()),
                            new CodesStar(), new UnicodeTranslatorStar());
                case ITHACA:
                    return new DevicePrinterESCPOS(
                            writterPool.getPrinterWritter(config.connector(), config.target()),
                            new CodesIthaca(), new UnicodeTranslatorInt());
                case SUREPOS:
                    return new DevicePrinterESCPOS(
                            writterPool.getPrinterWritter(config.connector(), config.target()),
                            new CodesSurePOS(), new UnicodeTranslatorInt());
                case PLAIN:
                    return new DevicePrinterPlain(
                            writterPool.getPrinterWritter(config.connector(), config.target()));
                case JAVAPOS:
                    return new DevicePrinterJavaPOS(config.target(), config.properties().getOrDefault("drawer", ""));
                case PRINTER:
                    return new DevicePrinterPrinter(null, config.target(), 0, 0, 72, 72, "standard");
                default:
                    return new NullPrinterDevice("Unsupported printer protocol: " + config.protocol());
            }
        } catch (TicketPrinterException ex) {
            throw new HardwareException("Failed to initialize printer: " + ex.getMessage(), ex);
        } catch (Exception ex) {
            throw new HardwareException("Unexpected error initializing printer: " + ex.getMessage(), ex);
        }
    }

    @Override
    public DisplayDevice getDisplay(DisplayConfig config) throws HardwareException {
        if (config == null || config.protocol() == DisplayProtocol.NONE) {
            return new NullDisplayDevice("Display not configured");
        }
        try {
            switch (config.protocol()) {
                case SCREEN:
                    return new DeviceDisplayPanel();
                case WINDOW:
                    return new DeviceDisplayWindow();
                case DUAL:
                    return new DeviceDisplayWindowDualScreen();
                case EPSON:
                    return new DeviceDisplayESCPOS(
                            writterPool.getPrinterWritter(config.connector(), config.target()),
                            new UnicodeTranslatorInt());
                case SUREPOS:
                    return new DeviceDisplaySurePOS(
                            writterPool.getPrinterWritter(config.connector(), config.target()));
                case LD200:
                    return new DeviceDisplayESCPOS(
                            writterPool.getPrinterWritter(config.connector(), config.target()),
                            new UnicodeTranslatorEur());
                case LED8:
                    return new DeviceDisplayLED8(
                            writterPool.getPrinterWritter(config.connector(), config.target()));
                case PDLED8:
                    return new DeviceDisplayPDLED8(
                            writterPool.getDisplayPrinterWritter(config.connector(), config.target(), 2400));
                case JAVAPOS:
                    return new DeviceDisplayJavaPOS(config.target());
                default:
                    return new NullDisplayDevice("Unsupported display protocol: " + config.protocol());
            }
        } catch (TicketPrinterException ex) {
            throw new HardwareException("Failed to initialize display: " + ex.getMessage(), ex);
        } catch (Exception ex) {
            throw new HardwareException("Unexpected error initializing display: " + ex.getMessage(), ex);
        }
    }

    @Override
    public ScannerDevice getScanner(ScannerConfig config) throws HardwareException {
        if (config == null || config.protocol() == ScannerProtocol.NONE) {
            return new NullScannerDevice("Scanner not configured");
        }
        try {
            switch (config.protocol()) {
                case SCANPAL2:
                    return new DeviceScannerComm(config.port());
                default:
                    return new NullScannerDevice("Unsupported scanner protocol: " + config.protocol());
            }
        } catch (Exception ex) {
            throw new HardwareException("Failed to initialize scanner: " + ex.getMessage(), ex);
        }
    }

    @Override
    public FiscalPrinterDevice getFiscalPrinter(FiscalPrinterConfig config) throws HardwareException {
        if (config == null || "none".equalsIgnoreCase(config.protocol())) {
            return new NullFiscalPrinterDevice("Fiscal printer not configured");
        }
        try {
            if ("javapos".equalsIgnoreCase(config.protocol())) {
                return new DeviceFiscalPrinterJavaPOS(config.target());
            }
            return new NullFiscalPrinterDevice("Unsupported fiscal printer configuration");
        } catch (TicketPrinterException ex) {
            throw new HardwareException("Failed to initialize fiscal printer: " + ex.getMessage(), ex);
        } catch (Exception ex) {
            throw new HardwareException("Unexpected error initializing fiscal printer: " + ex.getMessage(), ex);
        }
    }

    @Override
    public void configure(Map<String, String> configurations) {
        if (configurations != null) {
            currentConfig.putAll(configurations);
        }
    }

    @Override
    public Map<String, String> getCurrentConfiguration() {
        return Collections.unmodifiableMap(currentConfig);
    }
}
