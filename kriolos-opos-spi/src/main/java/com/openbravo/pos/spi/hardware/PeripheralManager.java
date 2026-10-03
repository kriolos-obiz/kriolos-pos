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

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Centralized global Service Registry and Façade for discovering, configuring,
 * and instantiating hardware peripherals across the POS platform.
 * <p>
 * Follows the proven {@code SaleLayoutManager} and {@code POSThemeManager} design patterns:
 * <ul>
 *   <li>Dynamic provider discovery through {@link java.util.ServiceLoader}</li>
 *   <li>Thread-safe reload and unmodifiable collection retrieval</li>
 *   <li>Resilient Null-Object safety fallbacks when hardware is absent or unconfigured</li>
 * </ul>
 * </p>
 *
 * @author KriolOS Team
 * @since 1.0.0
 */
public final class PeripheralManager {

    private static final Logger LOGGER = Logger.getLogger(PeripheralManager.class.getName());
    private static final ServiceLoader<PeripheralProvider> PROVIDER_LOADER =
            ServiceLoader.load(PeripheralProvider.class);

    private PeripheralManager() {
        throw new UnsupportedOperationException("PeripheralManager cannot be instantiated.");
    }

    /**
     * Reloads all {@link PeripheralProvider} SPI implementations from the classpath.
     */
    public static synchronized void reload() {
        PROVIDER_LOADER.reload();
        LOGGER.info("PeripheralManager providers reloaded.");
    }

    /**
     * Retrieves all discovered {@link PeripheralProvider} implementations.
     *
     * @return Unmodifiable collection of loaded providers.
     */
    public static Collection<PeripheralProvider> getAllProviders() {
        List<PeripheralProvider> list = new ArrayList<>();
        for (PeripheralProvider provider : PROVIDER_LOADER) {
            list.add(provider);
        }
        return Collections.unmodifiableCollection(list);
    }

    /**
     * Finds a provider by its unique identifier token (e.g. "default", "javapos").
     *
     * @param providerId Target provider ID.
     * @return Optional containing the provider if discovered.
     */
    public static Optional<PeripheralProvider> getProvider(String providerId) {
        if (providerId == null || providerId.isBlank()) {
            return Optional.empty();
        }
        for (PeripheralProvider provider : PROVIDER_LOADER) {
            if (provider.getProviderId().equalsIgnoreCase(providerId.trim())) {
                return Optional.of(provider);
            }
        }
        return Optional.empty();
    }

    /**
     * Acquires the active peripheral provider. Returns the first available provider,
     * or a built-in safety fallback provider if no external provider is present.
     *
     * @return The active {@link PeripheralProvider}.
     */
    public static PeripheralProvider getProvider() {
        Iterator<PeripheralProvider> iterator = PROVIDER_LOADER.iterator();
        if (iterator.hasNext()) {
            return iterator.next();
        }
        LOGGER.log(Level.WARNING, "No PeripheralProvider discovered via ServiceLoader. Utilizing safety fallback provider.");
        return FallbackPeripheralProvider.INSTANCE;
    }

    // =========================================================================
    // Typed Peripheral Gateways with Resilient Fallback Semantics
    // =========================================================================

    /**
     * Acquires an operational {@link ScaleDevice} configured with the given parameters.
     * If the scale is unconfigured or fails initialization, a safe {@link NullScaleDevice} is returned.
     *
     * @param config Scale configuration descriptor.
     * @return A valid {@link ScaleDevice} instance (never null).
     */
    public static ScaleDevice getScale(ScaleConfig config) {
        if (config == null || config.protocol() == ScaleProtocol.NONE) {
            return new NullScaleDevice("Scale is not configured.");
        }
        for (PeripheralProvider provider : getAllProviders()) {
            try {
                ScaleDevice scale = provider.getScale(config);
                if (scale != null && !(scale instanceof NullScaleDevice)) {
                    LOGGER.log(Level.INFO, "Scale resolved successfully via provider: {0} ({1})",
                            new Object[]{provider.getProviderId(), config.protocol()});
                    return scale;
                }
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, "Provider '" + provider.getProviderId() + "' failed to initialize scale", ex);
            }
        }
        return new NullScaleDevice("No provider could satisfy scale protocol: " + config.protocol());
    }

    /**
     * Acquires an operational {@link PrinterDevice} configured with the given parameters.
     *
     * @param config Printer configuration descriptor.
     * @return A valid {@link PrinterDevice} instance (never null).
     */
    public static PrinterDevice getPrinter(PrinterConfig config) {
        if (config == null || config.protocol() == PrinterProtocol.NONE) {
            return new NullPrinterDevice("Printer is not configured.");
        }
        for (PeripheralProvider provider : getAllProviders()) {
            try {
                PrinterDevice printer = provider.getPrinter(config);
                if (printer != null && !(printer instanceof NullPrinterDevice)) {
                    LOGGER.log(Level.INFO, "Printer resolved successfully via provider: {0} ({1})",
                            new Object[]{provider.getProviderId(), config.protocol()});
                    return printer;
                }
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, "Provider '" + provider.getProviderId() + "' failed to initialize printer", ex);
            }
        }
        return new NullPrinterDevice("No provider could satisfy printer protocol: " + config.protocol());
    }

    /**
     * Acquires an operational {@link DisplayDevice} configured with the given parameters.
     *
     * @param config Display configuration descriptor.
     * @return A valid {@link DisplayDevice} instance (never null).
     */
    public static DisplayDevice getDisplay(DisplayConfig config) {
        if (config == null || config.protocol() == DisplayProtocol.NONE) {
            return new NullDisplayDevice("Display is not configured.");
        }
        for (PeripheralProvider provider : getAllProviders()) {
            try {
                DisplayDevice display = provider.getDisplay(config);
                if (display != null && !(display instanceof NullDisplayDevice)) {
                    LOGGER.log(Level.INFO, "Display resolved successfully via provider: {0} ({1})",
                            new Object[]{provider.getProviderId(), config.protocol()});
                    return display;
                }
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, "Provider '" + provider.getProviderId() + "' failed to initialize display", ex);
            }
        }
        return new NullDisplayDevice("No provider could satisfy display protocol: " + config.protocol());
    }

    /**
     * Acquires an operational {@link ScannerDevice} configured with the given parameters.
     *
     * @param config Scanner configuration descriptor.
     * @return A valid {@link ScannerDevice} instance (never null).
     */
    public static ScannerDevice getScanner(ScannerConfig config) {
        if (config == null || config.protocol() == ScannerProtocol.NONE) {
            return new NullScannerDevice("Scanner is not configured.");
        }
        for (PeripheralProvider provider : getAllProviders()) {
            try {
                ScannerDevice scanner = provider.getScanner(config);
                if (scanner != null && !(scanner instanceof NullScannerDevice)) {
                    LOGGER.log(Level.INFO, "Scanner resolved successfully via provider: {0} ({1})",
                            new Object[]{provider.getProviderId(), config.protocol()});
                    return scanner;
                }
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, "Provider '" + provider.getProviderId() + "' failed to initialize scanner", ex);
            }
        }
        return new NullScannerDevice("No provider could satisfy scanner protocol: " + config.protocol());
    }

    /**
     * Acquires an operational {@link FiscalPrinterDevice} configured with the given parameters.
     *
     * @param config Fiscal printer configuration descriptor.
     * @return A valid {@link FiscalPrinterDevice} instance (never null).
     */
    public static FiscalPrinterDevice getFiscalPrinter(FiscalPrinterConfig config) {
        if (config == null || "none".equalsIgnoreCase(config.protocol())) {
            return new NullFiscalPrinterDevice("Fiscal printer is not configured.");
        }
        for (PeripheralProvider provider : getAllProviders()) {
            try {
                FiscalPrinterDevice fiscal = provider.getFiscalPrinter(config);
                if (fiscal != null && !(fiscal instanceof NullFiscalPrinterDevice)) {
                    return fiscal;
                }
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, "Provider '" + provider.getProviderId() + "' failed to initialize fiscal printer", ex);
            }
        }
        return new NullFiscalPrinterDevice("No provider could satisfy fiscal printer: " + config.protocol());
    }

    /**
     * Resolves generic or future hardware devices (e.g. payment terminals, cash drawers).
     *
     * @param <T>        Target {@link HardwareDevice} type.
     * @param deviceType Target class interface.
     * @param config     Configuration descriptor.
     * @return Optional containing the resolved peripheral.
     */
    public static <T extends HardwareDevice> Optional<T> getDevice(Class<T> deviceType, DeviceConfig config) {
        if (deviceType == null) {
            return Optional.empty();
        }
        for (PeripheralProvider provider : getAllProviders()) {
            try {
                Optional<T> dev = provider.getDevice(deviceType, config);
                if (dev.isPresent()) {
                    return dev;
                }
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, "Error querying device " + deviceType.getSimpleName() + " on " + provider.getProviderId(), ex);
            }
        }
        return Optional.empty();
    }

    /**
     * Safety fallback implementation used when no external PeripheralProvider is on classpath.
     */
    private static final class FallbackPeripheralProvider implements PeripheralProvider {
        static final FallbackPeripheralProvider INSTANCE = new FallbackPeripheralProvider();

        @Override
        public String getProviderId() {
            return "safety-fallback";
        }

        @Override
        public String getProviderName() {
            return "Safety Baseline Fallback Provider";
        }

        @Override
        public ScaleDevice getScale(ScaleConfig config) {
            return new NullScaleDevice("Safety baseline: No scale driver available.");
        }

        @Override
        public PrinterDevice getPrinter(PrinterConfig config) {
            return new NullPrinterDevice("Safety baseline: No printer driver available.");
        }

        @Override
        public DisplayDevice getDisplay(DisplayConfig config) {
            return new NullDisplayDevice("Safety baseline: No display driver available.");
        }

        @Override
        public ScannerDevice getScanner(ScannerConfig config) {
            return new NullScannerDevice("Safety baseline: No scanner driver available.");
        }

        @Override
        public FiscalPrinterDevice getFiscalPrinter(FiscalPrinterConfig config) {
            return new NullFiscalPrinterDevice("Safety baseline: No fiscal printer available.");
        }
    }
}
