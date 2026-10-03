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

/**
 * Null-Object implementation representing an inactive, unconfigured, or disconnected barcode scanner.
 *
 * @author KriolOS Team
 * @since 1.0.0
 */
public final class NullScannerDevice implements ScannerDevice {

    private final String reason;

    public NullScannerDevice() {
        this("Scanner is not configured or defined.");
    }

    public NullScannerDevice(String reason) {
        this.reason = reason != null ? reason : "Scanner is not configured or defined.";
    }

    @Override
    public String getDeviceId() {
        return "null-scanner";
    }

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public ScannerProtocol getProtocol() {
        return ScannerProtocol.NONE;
    }

    @Override
    public void start() {
        // No-op
    }

    @Override
    public void stop() {
        // No-op
    }

    public String getReason() {
        return reason;
    }
}
