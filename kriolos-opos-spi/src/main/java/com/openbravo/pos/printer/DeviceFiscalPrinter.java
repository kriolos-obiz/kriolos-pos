/*
 * Copyright (C) 2022-2026 KriolOS
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
package com.openbravo.pos.printer;

import javax.swing.JComponent;
import com.openbravo.pos.spi.hardware.printer.FiscalPrinterDevice;
import com.openbravo.pos.spi.hardware.printer.PrinterException;

/**
 * High-level contract for fiscal receipt printing operations.
 *
 * @author JG uniCenta / KriolOS Team
 */
public interface DeviceFiscalPrinter extends FiscalPrinterDevice {

    @Override
    default boolean isConnected() {
        return true;
    }

    @Override
    default void printFiscalReceipt(Object receipt) throws PrinterException {
        // default no-op
    }

    String getFiscalName();

    JComponent getFiscalComponent();

    void beginReceipt();

    void endReceipt();

    void printLine(String sproct, double dprice, double dunits, int taxinfo);

    void printMessage(String smessage);

    void printTotal(String smessage, double dtotal);

    void printZReport();

    void printXReport();
}
