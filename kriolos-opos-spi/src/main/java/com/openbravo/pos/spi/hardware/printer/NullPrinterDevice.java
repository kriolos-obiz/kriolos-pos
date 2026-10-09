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
 * Null-Object implementation representing an inactive, unconfigured, or disconnected printer.
 *
 * @author KriolOS Team
 * @since 1.0.0
 */
public final class NullPrinterDevice implements PrinterDevice {

    private final String reason;

    public NullPrinterDevice() {
        this("Printer is not configured or defined.");
    }

    public NullPrinterDevice(String reason) {
        this.reason = reason != null ? reason : "Printer is not configured or defined.";
    }

    @Override
    public String getDeviceId() {
        return "null-printer";
    }

    @Override
    public String getPrinterName() {
        return "No Printer Configured";
    }

    @Override
    public String getPrinterDescription() {
        return reason;
    }

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public PrinterProtocol getProtocol() {
        return PrinterProtocol.NONE;
    }

    @Override
    public void print(byte[] data) throws PrinterException {
        // Silent ignore or log
    }

    @Override
    public void cutReceipt() throws PrinterException {
        // No-op
    }

    @Override
    public void openDrawer() throws PrinterException {
        // No-op
    }
}
