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

/**
 *
 * @author JG uniCenta
 */
public class DeviceFiscalPrinterNull implements DeviceFiscalPrinter {
    
    /** Creates a new instance of DeviceFiscalPrinterNull */
    public DeviceFiscalPrinterNull() {
    }

    /**
     *
     * @param desc
     */
    public DeviceFiscalPrinterNull(String desc) {
    }
 
    /**
     *
     * @return
     */
    @Override
    public String getFiscalName() {
        return null;
    }

    /**
     *
     * @return
     */
    @Override
    public JComponent getFiscalComponent() {
        return null;
    }
    
    /**
     *
     */
    @Override
    public void beginReceipt() {
    }

    /**
     *
     */
    @Override
    public void endReceipt() {
    }

    /**
     *
     * @param sproduct
     * @param dprice
     * @param dunits
     * @param taxinfo
     */
    @Override
    public void printLine(String sproduct, double dprice, double dunits, int taxinfo) {
    }

    /**
     *
     * @param smessage
     */
    @Override
    public void printMessage(String smessage) {
    }

    /**
     *
     * @param sPayment
     * @param dpaid
     */
    @Override
    public void printTotal(String sPayment, double dpaid) {
    }
    
    /**
     *
     */
    @Override
    public void printZReport() {
    }

    /**
     *
     */
    @Override
    public void printXReport() {
    }

    @Override
    public boolean isConnected() {
        return false;
    }
}

