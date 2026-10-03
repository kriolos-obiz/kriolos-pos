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
package com.openbravo.pos.spi.hardware.scale;

/**
 * Null-Object implementation representing an inactive, unconfigured, or disconnected scale.
 * Protects caller code from NullPointerException while preserving standard ScaleException semantics.
 *
 * @author KriolOS Team
 * @since 1.0.0
 */
public final class NullScaleDevice implements ScaleDevice {

    private final String reason;

    public NullScaleDevice() {
        this("Scale is not configured or defined.");
    }

    public NullScaleDevice(String reason) {
        this.reason = reason != null ? reason : "Scale is not configured or defined.";
    }

    @Override
    public String getDeviceId() {
        return "null-scale";
    }

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public Double readWeight() throws ScaleException {
        throw new ScaleException(reason);
    }

    @Override
    public ScaleProtocol getProtocol() {
        return ScaleProtocol.NONE;
    }

    public String getReason() {
        return reason;
    }
}
