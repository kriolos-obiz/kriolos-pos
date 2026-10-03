/*
 * Copyright (C) 2022 KriolOS
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
 *
 * @author JG uniCenta
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

 
    /**
     *
     * @return
     */
    public String getFiscalName();

    /**
     *
     * @return
     */
    public JComponent getFiscalComponent();
    
    /**
     *
     */
    public void beginReceipt();

    /**
     *
     */
    public void endReceipt();

    /**
     *
     * @param sproduct
     * @param dprice
     * @param dunits
     * @param taxinfo
     */
    public void printLine(String sproduct, double dprice, double dunits, int taxinfo);

    /**
     *
     * @param smessage
     */
    public void printMessage(String smessage);

    /**
     *
     * @param sPayment
     * @param dpaid
     */
    public void printTotal(String sPayment, double dpaid);
    
    /**
     *
     */
    public void printZReport();

    /**
     *
     */
    public void printXReport();
}
