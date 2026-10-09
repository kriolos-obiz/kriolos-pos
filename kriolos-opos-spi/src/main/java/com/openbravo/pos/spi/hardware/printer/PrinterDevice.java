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
package com.openbravo.pos.spi.hardware.printer;

import com.openbravo.pos.spi.hardware.DeviceType;
import com.openbravo.pos.spi.hardware.HardwareDevice;

/**
 * Contract representing an operational receipt, ticket, or kitchen printer.
 *
 * @author KriolOS Team
 * @since 1.0.0
 */
public interface PrinterDevice extends HardwareDevice {

    @Override
    default DeviceType getDeviceType() {
        return DeviceType.PRINTER;
    }

    @Override
    default String getDeviceId() {
        return getDeviceType().getCode();
    }

    /**
     * Human-readable printer name or assigned station name.
     *
     * @return printer name string.
     */
    String getPrinterName();

    /**
     * Technical description of the printer model and connection target.
     *
     * @return description string.
     */
    String getPrinterDescription();

    /**
     * The hardware protocol implemented by this printer.
     *
     * @return {@link PrinterProtocol} enum.
     */
    PrinterProtocol getProtocol();

    /**
     * Sends raw command bytes to the printer (ESC/POS stream, Star line mode, or plain text).
     *
     * @param data Byte payload to output.
     * @throws PrinterException if transmission fails or device is offline.
     */
    void print(byte[] data) throws PrinterException;

    /**
     * Signals the printer to cut receipt paper (full or partial depending on driver capabilities).
     *
     * @throws PrinterException if cutting fails.
     */
    default void cutReceipt() throws PrinterException {
        // Optional default
    }

    /**
     * Sends pulse to open the connected cash drawer.
     *
     * @throws PrinterException if trigger fails.
     */
    default void openDrawer() throws PrinterException {
        // Optional default
    }
}
