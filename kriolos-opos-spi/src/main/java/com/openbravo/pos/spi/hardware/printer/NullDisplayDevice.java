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

/**
 * Null-Object implementation for inactive, unconfigured, or disconnected customer visors.
 *
 * @author KriolOS Team
 * @since 1.0.0
 */
public final class NullDisplayDevice implements DisplayDevice {

    private final String reason;

    public NullDisplayDevice() {
        this("Display is not configured or defined.");
    }

    public NullDisplayDevice(String reason) {
        this.reason = reason != null ? reason : "Display is not configured or defined.";
    }

    @Override
    public String getDeviceId() {
        return "null-display";
    }

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public DisplayProtocol getProtocol() {
        return DisplayProtocol.NONE;
    }

    @Override
    public void clearVisor() {
        // No-op
    }

    @Override
    public void writeVisor(String sLine1, String sLine2) {
        // No-op
    }

    @Override
    public void writeVisor(int animation, String sLine1, String sLine2) {
        // No-op
    }

    public String getReason() {
        return reason;
    }
}
