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
package com.openbravo.pos.spi.hardware.scanner;

import com.openbravo.pos.spi.hardware.DeviceType;
import com.openbravo.pos.spi.hardware.HardwareDevice;

/**
 * Contract representing an operational barcode scanner or batch handheld device.
 *
 * @author KriolOS Team
 * @since 1.0.0
 */
public interface ScannerDevice extends HardwareDevice {

    @Override
    default DeviceType getDeviceType() {
        return DeviceType.SCANNER;
    }

    @Override
    default String getDeviceId() {
        return getDeviceType().getCode();
    }

    /**
     * Protocol implemented by this scanner device.
     *
     * @return {@link ScannerProtocol} enum.
     */
    ScannerProtocol getProtocol();

    /**
     * Starts listening or begins batch data transfer from the scanner.
     */
    void start();

    /**
     * Stops listening or terminates scanner communication.
     */
    void stop();
}
