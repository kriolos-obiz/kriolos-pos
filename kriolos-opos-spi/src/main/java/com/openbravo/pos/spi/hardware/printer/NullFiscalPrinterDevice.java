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
 * Null-Object implementation for unconfigured or disabled fiscal printers.
 *
 * @author KriolOS Team
 * @since 1.0.0
 */
public final class NullFiscalPrinterDevice implements FiscalPrinterDevice {

    private final String reason;

    public NullFiscalPrinterDevice() {
        this("Fiscal printer is not configured or defined.");
    }

    public NullFiscalPrinterDevice(String reason) {
        this.reason = reason != null ? reason : "Fiscal printer is not configured or defined.";
    }

    @Override
    public String getDeviceId() {
        return "null-fiscal";
    }

    @Override
    public String getFiscalName() {
        return "Disabled Fiscal Printer";
    }

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public void printFiscalReceipt(Object receipt) throws PrinterException {
        // No-op
    }

    @Override
    public void printZReport() throws PrinterException {
        // No-op
    }

    @Override
    public void printXReport() throws PrinterException {
        // No-op
    }

    public String getReason() {
        return reason;
    }
}
