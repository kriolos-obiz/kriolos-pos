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

import com.openbravo.pos.spi.hardware.printer.DisplayConfig;
import com.openbravo.pos.spi.hardware.printer.DisplayDevice;
import com.openbravo.pos.spi.hardware.printer.FiscalPrinterConfig;
import com.openbravo.pos.spi.hardware.printer.FiscalPrinterDevice;
import com.openbravo.pos.spi.hardware.printer.PrinterConfig;
import com.openbravo.pos.spi.hardware.printer.PrinterDevice;
import com.openbravo.pos.spi.hardware.scale.ScaleConfig;
import com.openbravo.pos.spi.hardware.scale.ScaleDevice;
import com.openbravo.pos.spi.hardware.scanner.ScannerConfig;
import com.openbravo.pos.spi.hardware.scanner.ScannerDevice;
import com.openbravo.pos.spi.provider.ConfigurableProvider;
import com.openbravo.pos.spi.provider.Provider;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * Service Provider Interface (SPI) contract for supplying POS hardware peripherals.
 * <p>
 * Implementations are discovered at runtime via {@link java.util.ServiceLoader} and
 * registered in {@code META-INF/services/com.openbravo.pos.spi.hardware.PeripheralProvider}.
 * </p>
 *
 * @author KriolOS Team
 * @since 1.0.0
 */
public interface PeripheralProvider extends Provider, ConfigurableProvider {

    /**
     * Unique identifier for this peripheral provider (e.g. "default", "javapos").
     *
     * @return unique provider token string.
     */
    String getProviderId();

    /**
     * Display name for operator selection and diagnostic logs.
     *
     * @return provider display name.
     */
    String getProviderName();

    /**
     * Resolves, constructs, and connects the weight scale peripheral described by the config.
     *
     * @param config Scale configuration descriptor.
     * @return Fully initialized {@link ScaleDevice}.
     * @throws HardwareException if device initialization fails.
     */
    ScaleDevice getScale(ScaleConfig config) throws HardwareException;

    /**
     * Resolves, constructs, and connects the ticket printer described by the config.
     *
     * @param config Printer configuration descriptor.
     * @return Fully initialized {@link PrinterDevice}.
     * @throws HardwareException if device initialization fails.
     */
    PrinterDevice getPrinter(PrinterConfig config) throws HardwareException;

    /**
     * Resolves, constructs, and connects the customer visor display described by the config.
     *
     * @param config Display configuration descriptor.
     * @return Fully initialized {@link DisplayDevice}.
     * @throws HardwareException if device initialization fails.
     */
    DisplayDevice getDisplay(DisplayConfig config) throws HardwareException;

    /**
     * Resolves, constructs, and connects the barcode scanner described by the config.
     *
     * @param config Scanner configuration descriptor.
     * @return Fully initialized {@link ScannerDevice}.
     * @throws HardwareException if device initialization fails.
     */
    ScannerDevice getScanner(ScannerConfig config) throws HardwareException;

    /**
     * Resolves, constructs, and connects the fiscal printer described by the config.
     *
     * @param config Fiscal configuration descriptor.
     * @return Fully initialized {@link FiscalPrinterDevice}.
     * @throws HardwareException if device initialization fails.
     */
    FiscalPrinterDevice getFiscalPrinter(FiscalPrinterConfig config) throws HardwareException;

    /**
     * Extensibility hook for future device categories (e.g. payment terminals, cash drawers, RFID).
     *
     * @param <T>        Target {@link HardwareDevice} subtype.
     * @param deviceType Target device interface class.
     * @param config     Configuration descriptor.
     * @return Optional containing the resolved peripheral device, or empty if unsupported.
     * @throws HardwareException if initialization fails.
     */
    default <T extends HardwareDevice> Optional<T> getDevice(Class<T> deviceType, DeviceConfig config)
            throws HardwareException {
        return Optional.empty();
    }

    @Override
    default void configure(Map<String, String> configurations) {
        // Optional default
    }

    @Override
    default Map<String, String> getCurrentConfiguration() {
        return Collections.emptyMap();
    }

    @Override
    default void close() {
        // Optional default
    }
}
